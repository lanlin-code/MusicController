package com.lanlin.controller.player.abs

import com.lanlin.controller.player.abs.listener.ChangeListener
import com.lanlin.controller.player.abs.listener.ListenerFilter




// 真正的播放器回调管理接口
interface MediaListenerManger {
    fun <T> registerChangeListener(listener: ChangeListener<T>)
    fun <T> removeChangeListener(listener: ChangeListener<T>)
    fun <T> invokeChangeListener(value: T, filter: ListenerFilter<T>)
}