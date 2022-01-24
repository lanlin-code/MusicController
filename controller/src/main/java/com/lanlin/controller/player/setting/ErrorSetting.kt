package com.lanlin.controller.player.setting

import com.lanlin.controller.data.Item
import com.lanlin.controller.player.abs.ErrorHandler

open class ErrorSetting<T : Item>(
    var retryCount: Int = 5,
    var handler: ErrorHandler<T>,
    var record: MutableMap<T, Int> = mutableMapOf()) {

    /**
     * @param value 要查询的键
     * 获取错误次数
     */
    fun errorCount(value: T): Int {
        return record[value] ?: 0
    }


    /**
     * 移除记录
     */
    fun remove(value: T) {
        record.remove(value)
    }

}