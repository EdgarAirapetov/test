package com.numplates.nomera3.presentation.view.navigator

import com.meera.core.di.scopes.AppScope
import kotlinx.coroutines.channels.BufferOverflow
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.Collections

enum class NavigationActionType {
    FragmentAdded
}

//TODO: Добавлен для отслеживания навигации для моментов https://git.nomera.com/nomera/NUMAD/-/merge_requests/6025
@AppScope
class NavigationListener @Inject constructor() {

    private val _sharedFlow = MutableSharedFlow<NavigationActionType>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val sharedFlow = _sharedFlow.asSharedFlow()

    //Решение проблемы множественного создания вьюмоделей для моментов
    private val subscribers = Collections.synchronizedSet(mutableSetOf<String>())

    suspend fun fragmentChanged(dialogType: NavigationActionType) {
        _sharedFlow.emit(dialogType)
    }

    fun addSubscribers(viewModel: String) {
        subscribers.add(viewModel)
    }

    fun checkIfExists(viewModel: String) =
        subscribers.contains(viewModel)

    fun removeSubscriber(viewModel: String){
        subscribers.remove(viewModel)
    }
}
