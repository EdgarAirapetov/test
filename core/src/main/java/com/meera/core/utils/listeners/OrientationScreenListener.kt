package com.meera.core.utils.listeners

abstract class OrientationScreenListener {
    var orientationChangedListener:((orientation: Int) -> Unit) = {}
    abstract fun onOrientationChanged(orientation: Int)
}
