package com.numplates.nomera3.presentation.view.fragments.dialogs

import androidx.lifecycle.MutableLiveData
import com.numplates.nomera3.App
import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.domain.interactornew.CitySuggestionUseCase
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class CityPickerDialogViewModel : BaseViewModel() {

    @Inject
    lateinit var citySuggestionUseCase: CitySuggestionUseCase

    val foundCitiesLiveData = MutableLiveData<MutableList<City>>()

    private var foundCitiesDisposable: Disposable? = null

    init {
        App.component.inject(this)
    }

    fun queryFindCity(countryId: Long?, query: String? = null) {
        if (countryId != null) {
            citySuggestionUseCase
                    .getCitySuggestion(countryId, query)
                    ?.subscribeOn(Schedulers.io())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe(
                            {
                                foundCitiesLiveData.postValue(it.data)
                            },
                            {
                                Timber.e(it)
                            }
                    )
                    ?.let {
                        foundCitiesDisposable = it
                    }
        }
    }

    override fun onCleared() {
        super.onCleared()

        foundCitiesDisposable?.dispose()
    }
}