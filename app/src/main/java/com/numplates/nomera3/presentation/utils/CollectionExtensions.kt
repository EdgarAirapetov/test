package com.numplates.nomera3.presentation.utils

fun <T> equalsIgnoreOrder(list1: List<T>, list2: List<T>) =
    list1.size == list2.size && list1.toSet() == list2.toSet()
