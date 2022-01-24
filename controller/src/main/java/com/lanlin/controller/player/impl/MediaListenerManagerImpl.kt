package com.lanlin.controller.player.impl

import com.lanlin.controller.player.abs.MediaListenerManger
import com.lanlin.controller.player.abs.listener.ChangeListener
import com.lanlin.controller.player.abs.listener.ListenerFilter
import com.lanlin.controller.player.util.castAs

class MediaListenerMangerImpl(
    private val listeners: MutableList<ChangeListener<*>> = mutableListOf(),
) : MediaListenerManger {
    override fun <T> registerChangeListener(listener: ChangeListener<T>) {
        listeners.add(listener)
    }

    override fun <T> removeChangeListener(listener: ChangeListener<T>) {
        listeners.remove(listener)
    }

    override fun <T> invokeChangeListener(value: T, filter: ListenerFilter<T>) {
        listeners.forEach { li ->
            li.castAs<ChangeListener<T>> {
                if (filter.filter(it)) {
                    it.onChange(value)
                }
            }
        }
    }


}