package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.numplates.nomera3.presentation.model.adaptermodel.SavedSearchModel

class SavedSearchViewModel: ViewModel() {

    var liveSavedSearch = MutableLiveData<MutableList<SavedSearchModel>>()

    fun init() {
        provideFakeData()
    }

    private fun provideFakeData() {
        val res = mutableListOf<SavedSearchModel>()
        repeat (4){
            res.add(SavedSearchModel())
        }
        liveSavedSearch.value = res
    }

}
