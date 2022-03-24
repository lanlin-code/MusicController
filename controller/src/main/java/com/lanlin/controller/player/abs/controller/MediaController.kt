package com.lanlin.controller.player.abs.controller

import com.lanlin.controller.data.Item
import com.lanlin.controller.player.abs.Loader
import com.lanlin.controller.player.abs.MediaListenerManger
import com.lanlin.controller.player.abs.core.MusicPlayer
import com.lanlin.controller.player.abs.core.Position
import java.lang.ref.WeakReference

interface MediaController<T : Item> : MusicPlayer<T> {
    // 播放上一首音乐
    fun last()
    // 播放下一首音乐
    fun next()

    var position: Position

    var loader: WeakReference<Loader>

    var listenerManger: MediaListenerManger

    /**
     * @param index 想要播放歌曲的位置
     * 跳转到列表index位置播放
     */
    fun jumpTo(index: Int)


}