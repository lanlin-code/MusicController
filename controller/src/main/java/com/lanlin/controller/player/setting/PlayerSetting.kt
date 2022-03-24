package com.lanlin.controller.player.setting

import com.lanlin.controller.data.Item
import com.lanlin.controller.player.abs.cache.CacheStrategy
import com.lanlin.controller.player.abs.controller.MediaController
import com.lanlin.controller.player.abs.core.MusicPlayer
import com.lanlin.controller.player.abs.transformation.IWrapperTransformation
import com.lanlin.controller.player.abs.transformation.ItemTransformation
import com.lanlin.controller.player.impl.DataInterceptor
import com.lanlin.controller.player.impl.OperatorCallback
import java.lang.IllegalArgumentException

class PlayerSetting<T : Item>(
    val itemTransformation: ItemTransformation<T>,
    val wrapperTransformation: IWrapperTransformation<T>) {


    var dataInterceptor: DataInterceptor<T>? = null

    var cacheStrategy: CacheStrategy? = null

    var operatorCallback: OperatorCallback? = null

    var errorSetting: ErrorSetting<T>? = null

    var player: MediaController<T>? = null

    var basePlayer: MusicPlayer<String>? = null

    fun dataInterceptor(dataInterceptor: DataInterceptor<T>?) = apply {
        this.dataInterceptor = dataInterceptor
    }

    fun cacheStrategy(cacheStrategy: CacheStrategy?) = apply {
        this.cacheStrategy = cacheStrategy
    }

    fun operatorCallback(operatorCallback: OperatorCallback?) = apply {
        this.operatorCallback = operatorCallback
    }

    fun errorSetting(errorSetting: ErrorSetting<T>?) = apply {
        this.errorSetting = errorSetting
    }

    fun player(player: MediaController<T>?) = apply {
        if (basePlayer != null) throw IllegalArgumentException()
        this.player = player
    }

    fun basePlayer(base: MusicPlayer<String>?) = apply {
        if (player != null) throw IllegalArgumentException()
        this.basePlayer = base
    }

}