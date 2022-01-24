package com.lanlin.controller.player.impl.ipc

import android.os.Handler
import android.os.Looper
import com.lanlin.controller.IPCPlayerController
import com.lanlin.controller.player.abs.controller.MediaController

class IPCPlayerControllerImpl(
    val realController: MediaController,
    val handler: Handler = Handler(Looper.getMainLooper())
) : IPCPlayerController.Stub() {

    private val TAG = "PlayerController"

    override fun last() {
        handler.post {
            realController.last()
        }
    }

    override fun next() {
        handler.post {
            realController.next()
        }
    }

    override fun playOrPause() {
        handler.post {
            realController.playOrPause()
        }
    }

    override fun duration(): Int {
        return realController.duration()
    }

    override fun seekTo(progress: Int) {
        handler.post {
            realController.seekTo(progress)
        }
    }

    override fun isPlaying(): Boolean {
        return realController.isPlaying()
    }

    override fun progress(): Int {
        return realController.progress()
    }

    override fun jumpTo(index: Int) {
        handler.post { 
            realController.jumpTo(index)
        }
    }


}