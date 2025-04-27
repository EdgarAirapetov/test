package com.numplates.nomera3.presentation.view.fragments.dialogs

import androidx.lifecycle.MutableLiveData
import com.numplates.nomera3.App
import com.numplates.nomera3.data.network.Country
import com.numplates.nomera3.domain.interactornew.GetCountriesUseCase
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class CountryPickerDialogViewModel : BaseViewModel() {

    @Inject
    lateinit var getCountriesUseCase: GetCountriesUseCase

    val countriesLiveData = MutableLiveData<List<Country>>()

    private var getCountryDisposable: Disposable? = null

    init {
        App.component.inject(this)
    }

    fun loadCountryListFromNetwork() {
        getCountryDisposable = getCountriesUseCase.getCountries()
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(
                        {
                            it.data.countries?.let {
                                countriesLiveData.value = it.filterNotNull()
                            }
                        },
                        { Timber.e(it) }
                )
    }

    override fun onCleared() {
        super.onCleared()

        getCountryDisposable?.dispose()
    }
}