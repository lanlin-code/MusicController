package com.lanlin.controller.player.abs.listener

import com.lanlin.controller.data.Item


interface ChangeListener<T> {
    fun onChange(value: T)
    val key: ListenerKey
        get() = DefaultKey
}

// 监听播放状态变化的接口
interface PlayStateChangeListener : ChangeListener<Boolean> {
    override val key: ListenerKey
        get() = PlayStateKey
}

// 监听播放模式变化的接口
interface ModeStateChangeListener : ChangeListener<Int> {
    override val key: ListenerKey
        get() = ModeStateKey
}

// 监听播放音乐变化的接口
interface ItemChangeListener : ChangeListener<Item> {
    override val key: ListenerKey
        get() = ItemKey
}