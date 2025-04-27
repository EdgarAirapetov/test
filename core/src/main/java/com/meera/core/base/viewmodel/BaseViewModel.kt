package com.meera.core.base.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

abstract class BaseViewModel<S: State?, E: Effect?, A: Action?> : ViewModel() {

    protected open val _state: MutableStateFlow<S?> = MutableStateFlow(null)
    val state: Flow<S> by lazy { _state.filterNotNull() }

    protected open val _effect: MutableSharedFlow<E> = MutableSharedFlow()
    val effect: Flow<E> by lazy { _effect }

    open fun handleUIAction(action: A) {
        error("Method is not implemented")
    }

    protected fun launchState(newState: S) = viewModelScope.launch {
        _state.emit(newState)
    }

    protected suspend fun emitState(newState: S) {
        _state.emit(newState)
    }

    protected fun launchEffect(effect: E) = viewModelScope.launch {
        _effect.emit(effect)
    }

    protected suspend fun emitEffect(effect: E) {
        _effect.emit(effect)
    }
}
