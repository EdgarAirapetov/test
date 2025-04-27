package com.meera.core.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer


fun <T> LiveData<T>.observeOnceButSkipNull(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(value: T) {
            if (value != null) {
                observer.onChanged(value)
                removeObserver(this)
            }
        }
    })
}

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(value: T) {
            observer.onChanged(value)
            removeObserver(this)
        }
    })
}


/**
 * LiveData that propagates only distinct emissions.
 */
fun <T> LiveData<T>.getDistinct(): LiveData<T> {
    val distinctLiveData = MediatorLiveData<T>()
    distinctLiveData.addSource(this, object : Observer<T> {
        private var initialized = false
        private var lastValue: T? = null

        override fun onChanged(value: T) {
            if (!initialized) {
                initialized = true
                lastValue = value
                lastValue?.let { distinctLiveData.postValue(it) }
            } else if ((value == null && lastValue != null) || value != lastValue) {
                lastValue = value
                lastValue?.let { distinctLiveData.postValue(it) }
            }
        }
    })

    return distinctLiveData
}

/**
 * Combine two live data
 * liveOne.combineWith(liveTwo){ one, two ->
 *  return one + two
 * }.observe(viewLifecycleOwner, Observe { ... })
 */
fun <T, K, R> LiveData<T>.combineWith(
    liveData: LiveData<K>,
    block: (T?, K?) -> R
): LiveData<R> {
    val result = MediatorLiveData<R>()
    result.addSource(this) {
        result.value = block(this.value, liveData.value)
    }
    result.addSource(liveData) {
        result.value = block(this.value, liveData.value)
    }
    return result
}
