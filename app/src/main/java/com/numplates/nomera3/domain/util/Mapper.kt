package com.numplates.nomera3.domain.util

interface Mapper<T, R> {

    fun map(entity: T): R

    fun map(collection: Collection<T>): List<R> = collection.map(::map)

    fun <C : MutableCollection<R>> map(collection: Collection<T>, resultCollection: C): C {
        return collection.mapTo(resultCollection, ::map)
    }
}