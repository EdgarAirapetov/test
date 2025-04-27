package com.numplates.nomera3.presentation.utils

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import com.numplates.nomera3.App

private const val ARGS_KEY = "screen:args"

/**
 * Интерфейс аргументов экрана
 */
interface ScreenArgs : Parcelable

fun ScreenArgs.addTo(bundle: Bundle): Bundle {
    bundle.putParcelable(ARGS_KEY, this)
    return bundle
}

/**
 * Получить аргументы экрана из фрагмента
 */
@MainThread
inline fun <reified Args : ScreenArgs> Fragment.screenArgs() = ScreenArgsLazy<Args> {
    arguments ?: error("Fragment $this has null arguments")
}

/**
 * Создать ViewModel через фабрику [ViewModelFactory]
 */
@MainThread
inline fun <reified VM : ViewModel> Fragment.viewModels(): Lazy<VM> =
    viewModels { App.component.getViewModelFactory() }

class ScreenArgsLazy<Args : ScreenArgs>(
    private val argumentProducer: () -> Bundle
) : Lazy<Args> {

    private var cached: Args? = null

    override val value: Args
        get() {
            var args = cached
            if (args == null) {
                val arguments = argumentProducer()
                @Suppress("UNCHECKED_CAST")
                args = arguments.getParcelable<Args>(ARGS_KEY) as Args
                cached = args
            }
            return args
        }

    override fun isInitialized() = cached != null
}
