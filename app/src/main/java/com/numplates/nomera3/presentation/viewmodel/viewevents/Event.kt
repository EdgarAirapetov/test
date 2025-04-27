package com.numplates.nomera3.presentation.viewmodel.viewevents

import com.numplates.nomera3.data.network.core.ResponseError

data class Event<out T>(val status: Status, val data: T?, val error: ResponseError?) {
    companion object {

        fun <T> loading(): Event<T> {
            return Event(Status.LOADING, null, null)
        }

        fun <T> success(data: T?): Event<T> {
            return Event(Status.SUCCESS, data, null)
        }

        fun <T> error(error: ResponseError?): Event<T> {
            return Event(Status.ERROR, null, error)
        }

    }
}
