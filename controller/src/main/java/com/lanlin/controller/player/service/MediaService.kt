package com.lanlin.controller.player.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.lanlin.controller.IPCDataController
import com.lanlin.controller.IPCPlayerController
import com.lanlin.controller.data.Item
import com.lanlin.controller.data.Wrapper
import com.lanlin.controller.player.abs.ErrorHandler
import com.lanlin.controller.player.abs.Loader
import com.lanlin.controller.player.abs.core.MusicPlayer
import com.lanlin.controller.player.abs.core.Position
import com.lanlin.controller.player.abs.listener.*
import com.lanlin.controller.player.impl.MediaListenerMangerImpl
import com.lanlin.controller.player.impl.controller.MediaControllerImpl
import com.lanlin.controller.player.impl.controller.ModeControllerImpl
import com.lanlin.controller.player.impl.core.DefaultMusicPlayer
import com.lanlin.controller.player.impl.ipc.*
import com.lanlin.controller.player.impl.position.InitPosition
import com.lanlin.controller.player.setting.ErrorSetting
import com.lanlin.controller.player.setting.MediaModeSetting
import com.lanlin.controller.player.setting.PlayerSetting
import com.lanlin.controller.player.util.*
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList

abstract class MediaService<T : Item> : Service(), Loader {
    protected val mainHandler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }


    protected val pool by lazy {
        BinderPool()
    }

    protected val setting by lazy {
        providePlayerSetting()
    }


    override fun onCreate() {
        super.onCreate()
        initMediaModeSetting()
        initIPC()
    }

    protected abstract fun providePlayerSetting(): PlayerSetting<T>



    /**
     * 初始化播放模式
     */
    open fun initMediaModeSetting() {
        MediaModeSetting.getInstance().init(0, MediaModeSetting.INIT_POSITION)
    }

    /**
     * @param position 初始化DataController用的Position
     * 初始化IPCDataController
     */
    private fun initDataController(position: Position): IPCDataControllerImpl<T> {
        val local = loadLocalRecord()
        val list = CopyOnWriteArrayList<T>()
        local?.let {
            list.addAll(local)
        }
        val dataSource = DataSource<T>(list = CopyOnWriteArrayList())

        val wrappers = DataSource<Wrapper>(list = CopyOnWriteArrayList())
        
        position.max = dataSource.size
        val interceptor = setting.dataInterceptor
        return if (interceptor == null) {
            IPCDataControllerImpl(
                handler = mainHandler,
                source = wrappers,
                mediaSource = dataSource,
                position = position,
                transformation = setting.itemTransformation,
                operatorCallback = setting.operatorCallback)
        } else {
            IPCDataControllerImpl(
                handler = mainHandler,
                source = wrappers,
                mediaSource = dataSource,
                position = position,
                transformation = setting.itemTransformation,
                operatorCallback = setting.operatorCallback,
                interceptor = interceptor)
        }
    }

    /**
     * @param callback 音乐URL错误时使用callback来加载音乐URL
     * @param source 音乐实体集合
     * @param position 初始化MediaController要用到的位置信息
     * 初始化IPCPlayerController
     */
    private fun initPlayerController(source: MutableList<T>,
                                     position: Position
    ): IPCPlayerControllerImpl<T> {
        val config = setting.errorSetting
        val errorSetting = if (config == null) {
            ErrorSetting(handler = object : ErrorHandler<T> {
                override fun onError(value: T) {
                    showForeground(value, false)
                }
            })
        } else {
            ErrorSetting(config.retryCount, handler = object : ErrorHandler<T> {
                override fun onError(value: T) {
                    showForeground(value, false)
                    config.handler.onError(value)
                }

            }, config.record)
        }
        setting.errorSetting = config
        val p = setting.player
        if (p != null) {
            val pl = p.preparedListener
            val cl = p.completeListener
            val el = p.errorListener
            p.preparedListener = pl ?: object : MusicPlayer.PreparedListener {
                override fun prepared() {
                    p.listenerManger.invokeChangeListener(true, PlayStateFilter)
                }
            }
            p.completeListener = cl ?: object : MusicPlayer.CompleteListener {
                override fun completed() {
                    p.listenerManger.invokeChangeListener(false, PlayStateFilter)
                    p.next()
                }
            }
            p.errorListener = el ?: object : MusicPlayer.ErrorListener<T> {
                override fun onError(player: MusicPlayer<T>, what: Int, extra: Int): Boolean {
                    ensureSecurity(source, p.position) {
                        val value = source[p.position.current()]
                        val count = errorSetting.errorCount(value)
                        p.listenerManger.invokeChangeListener(false, PlayStateFilter)
                        player.reset()
                        if (count >= errorSetting.retryCount) {
                            errorSetting.handler.onError(value)
                        } else {
                            errorSetting.record[value] = count + 1
                            p.playOrPause()
                        }
                    }
                    return true
                }
            }
            return IPCPlayerControllerImpl(p, mainHandler)
        } else {
            val player = setting.basePlayer ?: DefaultMusicPlayer()
            val listenerManger = MediaListenerMangerImpl()
            val mediaController = MediaControllerImpl(
                player = player,
                position = position,
                listenerManger = listenerManger,
                loader = WeakReference(this),
                list = source,
                cacheStrategy = setting.cacheStrategy
            )

            player.completeListener = player.completeListener ?: object : MusicPlayer.CompleteListener {
                override fun completed() {
                    mainHandler.post {
                        mediaController.completeListener?.completed()
                        mediaController.listenerManger.invokeChangeListener(false, PlayStateFilter)
                        mediaController.next()
                    }
                }
            }
            player.preparedListener = player.preparedListener ?: object : MusicPlayer.PreparedListener {
                override fun prepared() {
                    mainHandler.post {
                        mediaController.listenerManger.invokeChangeListener(true, PlayStateFilter)
                        mediaController.preparedListener?.prepared()
                    }
                }

            }
            player.errorListener = player.errorListener ?: object : MusicPlayer.ErrorListener<String> {
                override fun onError(player: MusicPlayer<String>, what: Int, extra: Int): Boolean {
                    ensureSecurity(source, mediaController.position) {
                        val value = source[mediaController.position.current()]
                        val count = errorSetting.errorCount(value)
                        mediaController.listenerManger.invokeChangeListener(false, PlayStateFilter)
                        if (mediaController.errorListener?.onError(mediaController, what, extra) != true) {
                            player.reset()
                            if (count >= errorSetting.retryCount) {
                                errorSetting.handler.onError(value)
                            } else {
                                errorSetting.record[value] = count + 1
                                mediaController.playOrPause()
                            }
                        }
                    }
                    return true
                }
            }

            return IPCPlayerControllerImpl(realController = mediaController, handler = mainHandler)
        }
    }

    /**
     * 设置初始播放模式，默认为MediaModeSetting.getFirstMode()
     * 可重载此方法从磁盘加载播放模式
     */
    open fun loadInitMode(): Int {
        return MediaModeSetting.getInstance().getFirstMode()
    }

    private fun initIPCModeController(): IPCModeControllerImpl {
        return IPCModeControllerImpl(handler = mainHandler,
            realController = ModeControllerImpl(current = loadInitMode())
        )
    }

    private fun initIPCListenerController(): IPCListenerControllerImpl {
        return IPCListenerControllerImpl()
    }

    override fun onLoadItem(itemIndex: Int, item: Item) {
        ExecutorInstance.getInstance().execute {
            val newItem = loadItem(itemIndex, item)
            handleData(itemIndex, item, newItem)
        }
    }

    protected abstract fun loadItem(itemIndex: Int, item: Item): Item

    protected open fun handleData(itemIndex: Int, oldItem: Item, newItem: Item) {
        mainHandler.post {
            val dataController = IPCDataController.Stub.asInterface(
                pool.queryBinder(BinderPool.DATA_CONTROL_BINDER))?.castAs<IPCDataControllerImpl<T>>()!!
            val playerController = IPCPlayerController.Stub.asInterface(pool.queryBinder(
                BinderPool.PLAYER_CONTROL_BINDER))?.castAs<IPCPlayerControllerImpl<T>>()!!
            val source = dataController.source
            val mediaSource = dataController.mediaSource
            for (i in 0 until mediaSource.size) {
                if (oldItem == mediaSource[i]) {
                    mediaSource[i] = newItem.cast()!!
                    source[i] = setting.wrapperTransformation.transform(mediaSource[i])!!
                    break
                }
            }
            val cur = playerController.realController.position.current()
            if (mediaSource[cur] == newItem && !playerController.isPlaying) {
                playerController.realController.setDataSource(mediaSource[cur])
            }

        }
    }

    /**
     * 初始化IPC服务(包括[IPCModeControllerImpl], [IPCDataControllerImpl],
     * [IPCPlayerControllerImpl], [IPCListenerControllerImpl])
     * 并将这些服务放入[BinderPool]中
     */
    open fun initIPC() {
        val modeController = initIPCModeController()
        pool.map[BinderPool.MODE_CONTROL_BINDER] = modeController


        val dataController = initDataController(
            MediaModeSetting.getInstance().getPosition(modeController.currentMode())!!)
        pool.map[BinderPool.DATA_CONTROL_BINDER] = dataController
        val source = dataController.source

        val playerController = initPlayerController(
            source = dataController.mediaSource,
            position = dataController.position)
        pool.map[BinderPool.PLAYER_CONTROL_BINDER] = playerController
        val realController = playerController.realController.castAs<MediaControllerImpl<T>>()!!

        val listenerController = initIPCListenerController()
        pool.map[BinderPool.LISTENER_CONTROL_BINDER] = listenerController

        source.removeListeners.add(object : DataSource.RemoveListener<Wrapper> {
            override fun onRemoved(element: Wrapper, index: Int) {
                realController.position.max = source.size
                if (realController.position.current() == index) {
                    if (realController.isPlaying()) {
                        realController.playOrPause()
                    }
                    if (source.size > 0) {
                        realController.next()
                    } else {
                        realController.reset()
                    }
                }
                setting.itemTransformation.transform(element)?.let {
                    setting.errorSetting?.remove(it)
                }
            }

        })
        source.changeListeners.add(object : DataSource.DataSetChangeListener<Wrapper> {
            override fun onChange(changes: MutableList<Wrapper>) {

                listenerController.invokeDataSetChangeListener(changes)

                if (changes.isEmpty()) {
                    realController.position.with(InitPosition)
                    setting.errorSetting?.record?.clear()
                    if (realController.isPlaying()) {
                        realController.playOrPause()
                        realController.reset()
                    }
                } else {
                    realController.position.max = source.size
                    changes.forEach {
                        setting.itemTransformation.transform(it)?.let { item ->
                            setting.errorSetting?.remove(item)
                        }
                    }
                }
            }
        })

        (modeController.realController).
        listenerManager.registerChangeListener(object : ModeStateChangeListener {
            override fun onChange(value: Int) {
                val position = realController.position
                realController.position = MediaModeSetting.getInstance().getPosition(value)!!
                realController.position.with(position)
                dataController.position = realController.position
                listenerController.invokeModeChangeListener(value)
            }
        })

        realController.listenerManger.registerChangeListener(object : ItemChangeListener {
            override fun onChange(value: Item) {
                for(i in 0 until source.size) {
                    val v = setting.itemTransformation.transform(source[i])
                    if (value == v) {
                        listenerController.invokeItemChangeListener(source[i])
                        showForeground(v, realController.isPlaying())
                        break
                    }
                }
            }
        })
        realController.listenerManger.registerChangeListener(object : PlayStateChangeListener {
            override fun onChange(value: Boolean) {
                listenerController.invokePlayStateChangeListener(value)
                val p = realController.position.current()
                if (source.isNotEmpty() && p < source.size) {
                    showForeground(setting.itemTransformation.transform(
                        source[realController.position.current()])!!,
                        realController.isPlaying())
                }
            }
        })

    }

    override fun onBind(intent: Intent?): IBinder? {
        return pool
    }


    /**
     * @param value 播放失败的音乐实体
     * 播放失败时的回调
     */
    protected open fun reportPlayError(value: T) {

    }

    /**
     * @param value 当前播放音乐对应的实体
     * @param state 播放状态
     * 应在这个方法中生成通知等
     */
    protected abstract fun showForeground(value: T, state: Boolean)

    /**
     * 加载本地记录
     */
    protected open fun loadLocalRecord(): MutableList<T>? {
        return null
    }



    override fun onDestroy() {
        mainHandler.removeCallbacksAndMessages(null)
        (IPCPlayerController.Stub.asInterface(
            pool.queryBinder(BinderPool.PLAYER_CONTROL_BINDER)
        ) as? IPCPlayerControllerImpl<*>)?.realController?.release()
        super.onDestroy()
    }




}