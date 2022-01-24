package com.lanlin.controller.player.abs

import com.lanlin.controller.data.Item


interface ErrorHandler<T : Item> {
    /**
     * @param value 播放失败的音乐实体
     * 错误处理
     */
    fun onError(value: T)
}