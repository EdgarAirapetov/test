package com.numplates.nomera3.presentation.view.utils

import io.reactivex.disposables.Disposable

fun Disposable.disposeIfNotDisposed() {
    if (!this.isDisposed) {
        this.dispose()
    }
}
