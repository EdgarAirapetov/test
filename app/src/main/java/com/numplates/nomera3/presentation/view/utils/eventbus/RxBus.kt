package com.numplates.nomera3.presentation.view.utils.eventbus

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject


class RxBus {

    val bus: PublishSubject<Any> = PublishSubject.create()

    fun send(obj: Any) {
        bus.onNext(obj)
    }

    fun complete() {
        bus.onComplete()
    }

    fun toObservable() : Observable<Any> {
        return bus
    }

}