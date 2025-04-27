package com.meera.core.base.viewmodel

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlinx.coroutines.flow.updateAndGet

/**
 * Базовый класс для ViewModel, которые работают с состояниями, событиями и эффектами
 */
open class StateViewModel<STATE: Parcelable, ACTION, EFFECT>(
    initialState: STATE
) : ViewModel() {

    private val _state: MutableStateFlow<STATE> = MutableStateFlow(initialState)
    private val _effect: MutableSharedFlow<EFFECT> = MutableSharedFlow()

    /**
     * Flow состояний
     */
    val state: StateFlow<STATE> get() = _state.asStateFlow()

    /**
     * Текущее состояние
     */
    protected val currentState: STATE get() = _state.value

    /**
     * Flow эффектов
     */
    val effect: SharedFlow<EFFECT> get() = _effect.asSharedFlow()


    /**
     * Отправляет событие [action]. Вызывается из view
     */
    fun send(action: ACTION) {
        Timber.d("process action: $action")
        process(action)
    }

    /**
     * Обрабатывает событие [action]
     */
    protected open fun process(action: ACTION) = Unit

    /**
     * Отправляет эффект [effect] во view
     */
    protected fun post(effect: EFFECT) {
        Timber.d("post effect: $effect")
        viewModelScope.launch { _effect.emit(effect) }
    }

    /**
     * Изменяет состояние.
     * В [block] передается текущее состояние, он должен вернуть измененное
     */
    protected fun dispatch(block: STATE.() -> STATE) {
        val values = _state.updateAndGet { it.block() }
        Timber.d( "dispatch state: $values")
    }
}
