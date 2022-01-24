# MusicController -- 一个好用的封装原生MediaPlayer的音乐服务

## Download

```kotlin
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

```kotlin
dependencies {
	implementation 'com.github.lanlin-code:MusicController:1.0.4'
}
```



## 使用

### 基本使用

第一步，需要实现`ParcelableItem`接口定义音乐实体

```kotlin
// 音乐实体
data class Music(val id: Long, val url: String? = null) : ParcelableItem {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(url)
    }

    override fun url(): String? {
        return url
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Music> {
        override fun createFromParcel(parcel: Parcel): Music {
            return Music(parcel)
        }

        override fun newArray(size: Int): Array<Music?> {
            return arrayOfNulls(size)
        }
    }
}
```

第二步，实现`IWrapperTransformation`和`ItemTransformation`来完成Wrapper与音乐实体的相互转换。

```kotlin
object WrapperTransformation : IWrapperTransformation<Music> {
    const val MUSIC_KEY = "music"

    override fun transform(source: Music): Wrapper {
        val bundle = Bundle()
        bundle.putParcelable(MUSIC_KEY, source)
        return Wrapper(bundle, source.id)
    }

}

object MusicTransformation : ItemTransformation<Music> {
    override fun transform(source: Wrapper): Music? {
        return source.bundle.getParcelable(WrapperTransformation.MUSIC_KEY)
    }
}
```

第三步，继承`MediaService`实现音乐服务。

```kotlin
class MusicService : MediaService<Music>() {
    override fun onLoadItem(itemIndex: Int, item: Item, 
                            callback: Loader.Callback<Item>) {
        // 当Music实例的url字段为null时，将回调这个方法加载URL
    }

    override fun showForeground(value: Music, state: Boolean) {
        // 如果需要，可在此方法中实现前台服务
    }

    override fun transformation(): ItemTransformation<Music> = MusicTransformation

    override fun wrapperTransformation(): IWrapperTransformation<Music> = WrapperTransformation
}
```

第四步，创建`ControllerObserver`实例连接服务

```kotlin
class MainActivity : AppCompatActivity() {
    private val observer: ControllerObserver<Music> by lazy {
        ControllerObserver(this, MusicController(MusicService::class.java, MusicTransformation))
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        // 连接服务
        observer.controller.client.connectService()
    }
}
```



#### 数据相关

添加单个音乐实体

```kotlin
observer.controller.dataController?.add(WrapperTransformation.transform(Music(10000)))
```

添加音乐实体列表

```kot
observer.controller.dataController?.addAll(mutableListOf())
```

添加音乐实体到某个位置

```kotlin
// 添加到index为2的位置 
observer.controller.dataController?.addAfter(WrapperTransformation.transform(Music(10000)), 1)
```

移除单个音乐实体

```kotlin
observer.controller.dataController?.remove(WrapperTransformation.transform(Music(10000)))
```

清空列表

```kotlin
observer.controller.dataController?.clear()
```

获取当前播放列表

```kotlin
val playlist = observer.controller.playItems.value
```

获取当前播放歌曲

```kotlin
val current = observer.controller.currentItem.value
```



#### 播放模式

播放模式采用策略模式来实现，播放模式由`(Int, Position)`键值对定义，默认提供了列表循环(`(MediaModeSetting.ORDER, OrderPosition)`)、随机播放(`(MediaModeSetting.RANDOM, RandomPosition)`)、单曲循环(`(MediaModeSetting, LoopPosition)`)三种播放模式。

获取当前播放模式

```kotlin
val mode = observer.controller.currentMode.value
```

切换下一个播放模式

```kotlin
observer.controller.modeController?.nextMode()
```

当然，也可以直接设置`MediaModeSetting`中的任意模式

```kotlin
observer.controller.modeController?.setMode(MediaModeSetting.ORDER)
```



#### 播放控制

获取当前播放状态

```kotlin
val state = observer.controller.playState.value
```

获取播放歌曲的时长

```kotlin
val duration = observer.controller.playerController?.duration()
```

获取播放进度

```kotlin
observer.controller.playerController?.progress()
```

跳转到任意进度

```kotlin
// 跳转到歌曲6s位置播放
observer.controller.playerController?.seekTo(6000)
```

上一曲

```kotlin
observer.controller.playerController?.last()
```

下一曲

```kotlin
observer.controller.playerController?.next()
```

播放特定歌曲

```kotlin
// 播放列表中第一首歌曲
observer.controller.playerController?.jumpTo(0)
```

恢复/暂停歌曲

```kotlin
observer.controller.playerController?.playOrPause()
```



#### 监听

监听播放列表

```kotlin
observer.controller.listenerController?.addDataChangeListener(object : IPCDataSetChangeListener.Stub() {
	override fun onChange(source: MutableList<Wrapper>?) {
                
    }
})

// 或者观察playItems
observer.controller.playItems.observe(this) {}
```

监听播放歌曲

```kotlin
// 观察currentItem
observer.controller.currentItem.observe(this) {}
// 或者
observer.controller.listenerController?.addItemChangeListener(object : IPCItemChangeListener.Stub() {
    override fun onItemChange(wrapper: Wrapper?) {
                
    }

})
```

监听播放状态

```kotlin
// 观察playState
observer.controller.playState.observe(this) {}
// 或者
observer.controller.listenerController?.addPlayStateChangeListener(object : IPCPlayStateChangeListener.Stub() {
	override fun playStateChange(state: Boolean) {
                
    }

})
```

监听播放模式

```kotlin
// 观察currentMode
observer.controller.currentMode.observe(this) {}
// 或者
observer.controller.listenerController?.addModeChangeListener(object : IPCModeChangeListener.Stub() {
	override fun onModeChange(mode: Int) {
                
    }
})
```

需要注意的是，在使用`ListenerController`注册监听器的情况下，当不再需要监听相应的状态时，需要调用`ListenerController`相应的方法反注册，否则很可能会发生内存泄漏。



### 自定义

#### 播放模式

第一步，实现Position接口

```kotlin
class JumpPosition(override var max: Int, private var current: Int) : Position {
    override fun current(): Int {
        return current
    }

    override fun next(): Int {
        current = (current + 2) % max
        return current
    }

    override fun last(): Int {
        current = (current - 2 + max) % max
        return current
    }

    override fun with(position: Position) {
        current = position.current()
        max = position.max
    }
    
    companion object {
        const val JUMP = 4
    }
}
```

第二步，将`JumpPosition`添加到`MediaModeSetting`中

```kotlin
MediaModeSetting.getInstance().addMode(JumpPosition.JUMP, JumpPosition())
```

需要注意的是，当注册的Int值已经存在`MediaModeSetting`时，将添加失败。



#### 初始化播放模式

当需要在服务启动时添加自定义播放模式或调整播放模式顺序时，可以重写`MediaService::initMediaModeSetting`方法。

```kotlin
class MusicService : MediaService<Music>() {
    override fun initMediaModeSetting() {
        val setting = MediaModeSetting.getInstance()
        val max = 0
        val current = -1
        setting.addMode(JumpPosition.JUMP, JumpPosition(max, current))
        setting.addMode(MediaModeSetting.LOOP, LoopPosition(max, current))
    }
}
```

可通过重写`MediaService::loadInitMode`来设置服务启动后初始的播放模式。

```kotlin
class MusicService : MediaService<Music>() {
    override fun loadInitMode(): Int {
        return JumpPosition.JUMP
    }
}
```



#### 缓存配置

默认为无缓存。如果需要对歌曲进行缓存，可通过重写`MediaService::cacheStrategy`方法配置。

```kotlin
class MusicService : MediaService<Music>() {
   override fun cacheStrategy(): CacheStrategy {
        val dir = File(applicationContext.cacheDir, CACHE_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val maxSize = Runtime.getRuntime().freeMemory() / 8
        val cs = CacheStrategyImpl(dir, maxSize.toInt())
        cs.loader = object : CacheLoader {
            override fun load(url: String) {
                // steam为根据URL请求到的InputSteam
                cs.putInDisk(url, steam)
            }
        }
        return cs
    }
    
    companion object {
        const val CACHE_DIR = "music"
    }
}
```



#### 错误配置

默认歌曲播放失败5次后，将暂停播放歌曲，并回调`MediaService::reportPlayError`方法报告失败歌曲。如果需要自定义错误配置，可重写`MediaServic::errorSetting`方法。

```kotlin
class MusicService : MediaService<Music>() {
    override fun errorSetting(): ErrorSetting<Music> {
        return ErrorSetting(retryCount = 3, handler = object : ErrorHandler<Music> {
            override fun onError(value: Music) {
                Log.d("MusicService", "onError: $value")
                reportPlayError(value)
            }
        })
    }
}
```



#### 数据拦截器

默认拦截与播放列表中的音乐实体相等的实体。如果需要自定义拦截器，可重写`MediaService::dataInterceptor`方法。

```kotlin
class MusicService : MediaService<Music>() {
    override fun dataInterceptor(): DataInterceptor<Music> {
        return object : DataInterceptor<Music> {
            override fun isIntercepted(
                wrapper: Wrapper,
                wrappers: List<Wrapper>,
                source: List<Music>,
                item: Music
            ): Boolean {
                // 返回false代表所有数据都不拦截
                return false
            }

            override fun intercept(wrapper: Wrapper, item: Music) {
                
            }

        }
    }
}
```

