package com.lanlin.controller.player.impl.ipc

import android.os.IInterface
import android.os.RemoteCallbackList
import com.lanlin.controller.IPCDataSetChangeListener
import com.lanlin.controller.IPCItemChangeListener
import com.lanlin.controller.IPCModeChangeListener
import com.lanlin.controller.IPCPlayStateChangeListener
import com.lanlin.controller.data.Wrapper
import com.lanlin.controller.player.util.castAs

open class IPCListenerControllerImpl(
    private val listeners: RemoteCallbackList<IInterface> = RemoteCallbackList()
) : AbsIPCListenerController() {
    private val TAG = "ListenerController"
    override fun addItemChangeListener(listener: IPCItemChangeListener) {
        registerCallback(listener)
    }

    private fun registerCallback(callback: IInterface) {
        listeners.register(callback)
    }

    private fun unregisterCallback(callback: IInterface) {
        listeners.unregister(callback)
    }

    override fun addModeChangeListener(listener: IPCModeChangeListener) {
        registerCallback(listener)
    }

    override fun addPlayStateChangeListener(listener: IPCPlayStateChangeListener) {
        registerCallback(listener)
    }

    override fun removeItemChangeListener(listener: IPCItemChangeListener) {
        unregisterCallback(listener)
    }

    override fun removeModeChangeListener(listener: IPCModeChangeListener) {
        unregisterCallback(listener)
    }

    override fun removePlayStateChangeListener(listener: IPCPlayStateChangeListener) {
        unregisterCallback(listener)
    }

    override fun addDataChangeListener(listener: IPCDataSetChangeListener) {
        registerCallback(listener)
    }

    override fun removeDataChangeListener(listener: IPCDataSetChangeListener) {
        unregisterCallback(listener)
    }

    private fun traverseListeners(block: (IInterface) -> Unit) {
        synchronized(listeners) {
            val n = listeners.beginBroadcast()
            for (i in 0 until n) {
                block(listeners.getBroadcastItem(i))
            }
            listeners.finishBroadcast()
        }
    }

    override fun invokeDataSetChangeListener(change: List<Wrapper>?) {
        traverseListeners { i ->
            i.castAs<IPCDataSetChangeListener> {
                it.onChange(change)
            }
        }
    }

    override fun invokeItemChangeListener(change: Wrapper) {
        traverseListeners { i ->
            i.castAs<IPCItemChangeListener> {
                it.onItemChange(change)
            }
        }
    }

    override fun invokeModeChangeListener(mode: Int) {

        traverseListeners { i ->
            i.castAs<IPCModeChangeListener> {
                it.onModeChange(mode)
            }
        }
    }

    override fun invokePlayStateChangeListener(state: Boolean) {
        traverseListeners { i ->
            i.castAs<IPCPlayStateChangeListener> {
                it.playStateChange(state)
            }
        }
    }
}