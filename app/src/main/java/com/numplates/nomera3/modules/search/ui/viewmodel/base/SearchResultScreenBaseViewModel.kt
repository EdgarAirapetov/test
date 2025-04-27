package com.numplates.nomera3.modules.search.ui.viewmodel.base

import com.numplates.nomera3.modules.search.ui.util.PagingProperties
import com.meera.core.extensions.empty
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.numbersearch.NumberSearchParameters

const val DEFAULT_SEARCH_RESULT_PAGE_SIZE = 20

interface PagingContext {
    fun loadMore()

    fun getPagingProperties():PagingProperties
}

abstract class SearchResultScreenBaseViewModel : SearchBaseScreenViewModel(), PagingContext {

    protected var query: String = String.empty()

    private val pagingProperties = PagingProperties()

    open fun search(query: String) {
        this.query = query
        getPagingProperties().reset()
    }

    open fun isFilterChanged(): Boolean = false

    open fun searchUserByNumber(params: NumberSearchParameters?, offset: Int) {}

    fun getSearchQuery():String {
        return query
    }

    override fun getPagingProperties(): PagingProperties {
        return pagingProperties
    }
}
