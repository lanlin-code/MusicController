package com.lanlin.controller.player.abs.listener

// 音乐播放器准备完成时的回调
interface PreparedListener<T> {
    fun onPrepared(value: T)
}