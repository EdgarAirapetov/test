package com.numplates.nomera3.data.network.core

import com.google.gson.annotations.SerializedName

abstract class ListResponse<T> (
    @SerializedName("more_items") var moreItems: Int? = 0) {
    abstract fun getList() :List<T>?
}