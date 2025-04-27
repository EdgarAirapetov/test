package com.numplates.nomera3.modules.baseCore.helper

import java.util.*


class ObservableMutableList <T> (
    private val collection: MutableList<T>,
): MutableList<T> by collection, Observable()  {

    override fun addAll(elements: Collection<T>): Boolean {
        val result = collection.addAll(elements)
        notifyWithCollection()
        return result
    }

    override fun add(element: T): Boolean {
        val result = collection.add(element)
        notifyWithCollection()
        return result
    }

    override fun add(index: Int, element: T) {
        collection.add(index, element)
        notifyWithCollection()
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        val result = collection.addAll(index, elements)
        notifyWithCollection()
        return result
    }

    override fun removeAt(index: Int): T {
        val result = collection.removeAt(index)
        notifyWithCollection()
        return result
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        val result = collection.removeAll(elements)
        notifyWithCollection()
        return result
    }

    override fun remove(element: T): Boolean {
        val result = collection.remove(element)
        notifyWithCollection()
        return result
    }

    override fun set(index: Int, element: T): T {
        val result = collection.set(index, element)
        notifyWithCollection()
        return result
    }

    private fun notifyWithCollection() {
        setChanged()
        notifyObservers(collection)
    }
}