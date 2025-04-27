package com.mera.bridge.devtools

interface IDevToolsBridge {

    fun initDevToolsBridge()

    /**
     * Включить/отключить подсветку постов попадающих в зону "просмотра"
     */
    fun onPostViewCollisionHighlightEnable(enable: Boolean)
}