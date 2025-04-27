package com.numplates.nomera3.modules.moments.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

interface LiveDataExtension {
    fun <Type> LiveData<Type>.setValue(value: Type) {
        (this as? MutableLiveData<Type>)?.value = value
    }

    fun <Type> LiveData<Type>.postValue(value: Type) {
        (this as? MutableLiveData<Type>)?.postValue(value)
    }
}