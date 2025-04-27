package com.numplates.nomera3.modules.maps.ui.events.list.delegate

import com.numplates.nomera3.modules.maps.ui.events.list.model.PagingDataUiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

open class ItemsListPagingDelegate<T>(
    private val scope: CoroutineScope,
    private val getNextPage: suspend (offset: Int, limit: Int) -> List<T>
) {

    private val pagingDataFlow = MutableStateFlow(
        PagingDataUiModel(
            isLoadingNextPage = false,
            isLastPage = false
        )
    )
    fun getPagingDataFlow(): Flow<PagingDataUiModel> = pagingDataFlow

    protected val itemsFlow = MutableStateFlow<List<T>>(emptyList())
    fun getItemsFlow(): Flow<List<T>> = itemsFlow

    private var fetchPageJob: Job? = null

    fun clear() {
        fetchPageJob?.cancel()
        itemsFlow.value = emptyList()
        pagingDataFlow.value = PagingDataUiModel(
            isLoadingNextPage = false,
            isLastPage = false
        )
    }

    fun fetchNextPage(shouldReset: Boolean = false) {
        if (pagingDataFlow.value.isLoadingNextPage) return
        fetchPageJob?.cancel()
        fetchPageJob = scope.launch {
            doFetchNextPage(shouldReset)
        }
    }

    private suspend fun doFetchNextPage(shouldReset: Boolean = false) {
        try {
            pagingDataFlow.value = pagingDataFlow.value.copy(isLoadingNextPage = true)
            val offset = if (shouldReset) 0 else itemsFlow.value.size
            val pageItems = getNextPage(offset, PAGE_SIZE)
            if (pageItems.size < PAGE_SIZE) {
                pagingDataFlow.value = pagingDataFlow.value.copy(isLastPage = true)
            }
            itemsFlow.value = if (shouldReset) {
                pageItems
            } else {
                itemsFlow.value.plus(pageItems)
            }
        } catch (e: Exception) {
            Timber.e(e)
        } finally {
            pagingDataFlow.value = pagingDataFlow.value.copy(isLoadingNextPage = false)
        }
    }

    companion object {
        private const val PAGE_SIZE = 30
    }
}
