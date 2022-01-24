package com.lanlin.controller.player.client

import androidx.lifecycle.*
import com.lanlin.controller.data.Item

class ControllerObserver<T : Item>(
    owner: LifecycleOwner,
    val controller: MusicController<T>
) : LifecycleEventObserver {

    init {
        owner.lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when(event) {
            Lifecycle.Event.ON_CREATE -> controller.registerServiceListener()
            Lifecycle.Event.ON_DESTROY -> controller.unregisterServiceListener()
            else -> {

            }
        }
    }


}