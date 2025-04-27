package com.numplates.nomera3.presentation.view.fragments.bottomfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.VehicleType
import com.numplates.nomera3.databinding.FragmentBottomSheetCountryBinding
import com.numplates.nomera3.domain.interactornew.GetCountriesUseCase
import com.numplates.nomera3.domain.interactornew.VehicleTypesUseCase
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment
import com.numplates.nomera3.presentation.view.adapter.BottomSheetRecyclerAdapter
import com.numplates.nomera3.presentation.view.utils.NGraphics
import com.numplates.nomera3.presentation.view.utils.NSupport
import com.meera.core.extensions.empty
import com.meera.core.extensions.simpleName
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class BottomSheetCountryVehicleFragment : BaseBottomSheetDialogFragment<FragmentBottomSheetCountryBinding>() {

    private lateinit var rvCountries: RecyclerView

    @Inject
    lateinit var getCountriesUseCase: GetCountriesUseCase

    @Inject
    lateinit var getVehicleTypesUseCase: VehicleTypesUseCase

    internal var clickListener: (BottomSheetRecyclerAdapter.BottomSheetItem) -> Unit = { _ -> }

    var requestType: Int = COUNTRY

    private val disposables = CompositeDisposable()
    private val bottomSheetRecyclerAdapter = BottomSheetRecyclerAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentBottomSheetCountryBinding
        get() = FragmentBottomSheetCountryBinding::inflate

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme


    fun show(manager: FragmentManager?) {
        val fragment = manager?.findFragmentByTag(simpleName)
        if (fragment != null)
            return
        manager?.let {
            super.show(manager, simpleName)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initRecycler()
        if (requestType == COUNTRY) {
            binding?.bottomSheetTitle?.text = getString(R.string.garage_select_country)
            requestCounties()
        } else {
            binding?.bottomSheetTitle?.text = getString(R.string.choose_vehicle)
            requestVehicles()
        }
    }

    private fun requestCounties() {
        getCountriesUseCase.getCountries()?.let { countries ->
            disposables.add(
                    countries
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ response ->
                                response.data.countries?.let { countries ->
                                    val list = countries.map {
                                        BottomSheetRecyclerAdapter.BottomSheetItem(
                                            it.countryId,
                                            it.flag,
                                            it.name, 1)
                                    }
                                    bottomSheetRecyclerAdapter.collection = list
                                }
                            }, {
                                Timber.e(it)
                            })

            )
        }
    }

    private fun requestVehicles() {
        disposables.add(
                getVehicleTypesUseCase.getVehicleTypes()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            val res = mutableListOf<VehicleType>()
                            response.data?.let {
                                it.vehicleTypes?.forEach { vehicleType ->
                                    if (vehicleType?.hasNumber == 1){
                                        res.add(vehicleType)
                                    }
                                }
                                handleVehicles(res)
                            }
                        }, {
                            Timber.e(it)
                        })
        )
    }

    private fun handleVehicles(res: MutableList<VehicleType>) {
        val list = res.map {
            var imagePath: String = String.empty()
            it.getSelectedIcon(NGraphics.getVehicleTypeMap())?.let { icon -> imagePath = NSupport.getURLForResource(icon) }
            BottomSheetRecyclerAdapter.BottomSheetItem(it.typeId?.toInt(), imagePath, it.name, 0)
        }
        bottomSheetRecyclerAdapter.collection = list
    }

    private fun initRecycler() {
        rvCountries.setHasFixedSize(true)
        rvCountries.layoutManager = LinearLayoutManager(context)
        rvCountries.adapter = bottomSheetRecyclerAdapter

        bottomSheetRecyclerAdapter.clickListener = { item ->
            item.id?.let {
                clickListener.invoke(item)
            }
            this.dismiss()

        }
    }

    private fun initView() {
        rvCountries = binding?.recyclerAccount!!
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }

    companion object {
        const val COUNTRY = 1
        const val VEHICLES = 2
    }
}
