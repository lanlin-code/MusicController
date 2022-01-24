package com.lanlin.controller.player.abs.transformation

import com.lanlin.controller.data.Item
import com.lanlin.controller.data.Wrapper

interface ItemTransformation<T : Item> : Transformation<Wrapper, T> {
}