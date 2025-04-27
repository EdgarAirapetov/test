package com.numplates.nomera3.presentation.view.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding


inline fun <T : ViewBinding> ViewGroup.inflateBinding(
    crossinline bindingInflater: (LayoutInflater, ViewGroup, Boolean) -> T, attachToParent: Boolean = false
) = bindingInflater.invoke(LayoutInflater.from(this.context), this, attachToParent)
