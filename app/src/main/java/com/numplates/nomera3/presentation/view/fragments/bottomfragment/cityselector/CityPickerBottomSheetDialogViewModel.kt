package com.numplates.nomera3.presentation.view.fragments.bottomfragment.cityselector

import com.numplates.nomera3.App
import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.domain.interactornew.CitySuggestionUseCase
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

// замена для класса CityPickerDialogViewModel
class CityPickerBottomSheetDialogViewModel : BaseViewModel() {

    @Inject
    lateinit var citySuggestionUseCase: CitySuggestionUseCase

    private var foundCitiesDisposable: Disposable? = null

    init {
        App.component.inject(this)
    }

    override fun onCleared() {
        super.onCleared()

        foundCitiesDisposable?.dispose()
    }

    fun queryFindCity(countryId: Long?, query: String? = null, onResult: (List<City>?) -> Unit) {
        if (countryId != null) {
            citySuggestionUseCase
                    .getCitySuggestion(countryId, query)
                    ?.subscribeOn(Schedulers.io())
                    ?.map { response -> response.data.toList().toList() ?: mutableListOf() }
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe({ response: List<City>? -> onResult(response) }, { Timber.e(it) })
                    ?.let { foundCitiesDisposable = it }
        }
    }
}
