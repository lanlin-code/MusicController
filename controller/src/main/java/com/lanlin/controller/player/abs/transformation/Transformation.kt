package com.lanlin.controller.player.abs.transformation

interface Transformation<S, T> {

    fun transform(source: S): T?

}