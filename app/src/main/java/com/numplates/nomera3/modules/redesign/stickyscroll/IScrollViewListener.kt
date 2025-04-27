package com.numplates.nomera3.modules.redesign.stickyscroll
interface IScrollViewListener {
    fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int){}
    fun onScrollStopped(isStopped: Boolean){}
}
