package com.lanlin.controller.player.impl.ipc

import android.os.Handler
import android.os.Looper
import com.lanlin.controller.IPCModeController
import com.lanlin.controller.player.abs.controller.ModeController

class IPCModeControllerImpl(
    val handler: Handler = Handler(Looper.getMainLooper()),
    val realController: ModeController
) : IPCModeController.Stub() {
    override fun nextMode() {
        handler.post {
            realController.nextMode()
        }
    }

    private val TAG = "ModeController"

    override fun currentMode(): Int {
        return realController.current
    }

    override fun setMode(mode: Int) {
        handler.post {
            realController.setMode(mode)
        }
    }
}