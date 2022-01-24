package com.lanlin.controller.player.impl.controller

import com.lanlin.controller.player.abs.MediaListenerManger
import com.lanlin.controller.player.abs.controller.ModeController
import com.lanlin.controller.player.abs.listener.ModeFilter
import com.lanlin.controller.player.impl.MediaListenerMangerImpl
import com.lanlin.controller.player.setting.MediaModeSetting

class ModeControllerImpl(val listenerManager: MediaListenerManger = MediaListenerMangerImpl(),
                         override var current: Int
) : ModeController {

    override fun nextMode(): Int {
        current = MediaModeSetting.getInstance().getNextMode(current)
        listenerManager.invokeChangeListener(current, ModeFilter)
        return current
    }

    override fun setMode(mode: Int) {
        if (MediaModeSetting.getInstance().getPosition(mode) == null)
            throw IllegalArgumentException("Mode $mode is not existed!")
        current = mode
        listenerManager.invokeChangeListener(current, ModeFilter)
    }
}