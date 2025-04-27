package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.numplates.nomera3.App
import com.numplates.nomera3.data.network.market.Field
import com.numplates.nomera3.data.network.market.ResponseBrand
import com.numplates.nomera3.data.network.market.ResponseModels
import com.numplates.nomera3.data.network.market.Value
import com.numplates.nomera3.data.newmessenger.response.ResponseWrapperWebSock
import com.meera.core.network.websocket.WebSocketMainChannel
import com.numplates.nomera3.presentation.model.adaptermodel.VehicleBrandModel
import com.numplates.nomera3.presentation.model.adaptermodel.ExpandedCheckedData
import com.numplates.nomera3.presentation.view.fragments.market.configurevehicle.VehicleParamFillFragment.Companion.VIEW_TYPE_BRAND
import com.numplates.nomera3.presentation.view.fragments.market.configurevehicle.VehicleParamFillFragment.Companion.VIEW_TYPE_FIELD
import com.numplates.nomera3.presentation.view.fragments.market.configurevehicle.VehicleParamFillFragment.Companion.VIEW_TYPE_MODEL
import com.numplates.nomera3.presentation.view.fragments.market.configurevehicle.VehicleParamFillModel
import com.meera.core.extensions.fromJson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class VehicleParamFillViewModel: ViewModel() {

    @Inject
    lateinit var webSocketMainChannel: WebSocketMainChannel

    @Inject
    lateinit var gson: Gson

    private val disposables = CompositeDisposable()
    private lateinit var args: VehicleParamFillModel

    val liveField = MutableLiveData<Field>()
    val liveBrand = MutableLiveData<List<VehicleBrandModel>>()
    val liveModel = MutableLiveData<MutableList<ExpandedCheckedData>>()
    val liveBrandNotExpanded = MutableLiveData<List<Value>>()

    fun init(mode: VehicleParamFillModel){
        args = mode
        App.component.inject(this)
        requestData()
    }

    private fun requestData() {
        when(args.mode){
            VIEW_TYPE_BRAND->{
                requestBrand()
            }
            VIEW_TYPE_MODEL->{
                requestModel()
            }
            VIEW_TYPE_FIELD ->{
                setUpField()
            }
        }
    }

    private fun setUpField() {
        liveField.value = args.field
    }

    private fun requestModel() {
        args.brandId?.let{ brandID->
            val payload = hashMapOf<String, Any>(
                    "type_id" to args.typeId,
                    "brand_id" to brandID
            )
            val d = webSocketMainChannel.pushGetVehicleModelsByBrand(payload)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({message ->
                        val response = gson.fromJson<ResponseWrapperWebSock<ResponseModels>>(message.payload)
                        val res = mutableListOf<Value>()
                        //val res = mutableListOf<VehicleModelBrandData>()
                        response?.response?.let { responseModels ->
                            responseModels.models.forEach { model->
                                //res.add(VehicleModelBrandData(model.name, mutableListOf(VehicleModel(model.name))))
                                res.add(Value(model.id, model.name, model.name))
                            }
                        }
                        //liveModel.value = res
                        liveBrandNotExpanded.value = res
                    },{
                        Timber.e(it)
                    })
            disposables.add(d)

        }
    }

    private fun requestBrand() {
        val payload = hashMapOf<String, Any>(
                "type_id" to args.typeId
        )
        val d = webSocketMainChannel.pushGetVehicleBrandByType(payload)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ message->
                    val response = gson.fromJson<ResponseWrapperWebSock<ResponseBrand>>(message.payload)
                    val res = mutableListOf<VehicleBrandModel>()
                    response.response?.let { brands->
                        brands.brands.forEach { brand->
                            res.add(VehicleBrandModel(brand))
                        }
                    }
                    liveBrand.value = res
                },{
                    Timber.e(it)
                })
        disposables.add(d)
    }

    override fun onCleared() {
        disposables.clear()
    }

}
