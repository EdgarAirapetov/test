package com.numplates.nomera3.modules.moments.util

fun <T> List<T>.moveToStart(item: T): List<T> {
    val mutableList = toMutableList()
    val currentIndex = mutableList.indexOf(item)
    if (currentIndex < 0) return mutableList
    mutableList.removeAt(currentIndex)
    mutableList.addToStartOfMutableList(item)
    return mutableList
}

fun <T> List<T>.addToStartOfList(item: T): List<T> {
    return toMutableList().addToStartOfMutableList(item)
}

fun <T> MutableList<T>.addToStartOfMutableList(item: T): List<T> {
    add(0, item)
    return this
}
