package com.numplates.nomera3.presentation.view.fragments.market.configurevehicle

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.Vehicle
import com.numplates.nomera3.data.network.core.INetworkValues
import com.numplates.nomera3.data.network.market.ResponseWizard
import com.meera.db.models.userprofile.VehicleCountry
import com.meera.db.models.userprofile.VehicleEntity
import com.meera.db.models.userprofile.VehicleType
import com.numplates.nomera3.data.services.UploadService
import com.numplates.nomera3.databinding.FragmentEditVehicleMarketBinding
import com.numplates.nomera3.presentation.model.adaptermodel.VehicleParamModel
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_CAR_MODEL
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_VEHICLE_PARAM_FILL
import com.numplates.nomera3.presentation.view.adapter.vehicleparam.VehicleParamAdapter
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.view.widgets.NSelectPhoto
import com.numplates.nomera3.presentation.view.widgets.numberplateview.NumberPlateEditView
import com.numplates.nomera3.presentation.viewmodel.EditVehicleMarketViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.EditVehicleMarketEvents

class EditVehicleMarketFragment : BaseFragmentNew<FragmentEditVehicleMarketBinding>() {

    private lateinit var imagePickers: MutableList<NSelectPhoto>
    private lateinit var viewModel: EditVehicleMarketViewModel
    private var vehicle: Vehicle? = null
    private var wizard: ResponseWizard? = null
    private lateinit var wizardAdapter: VehicleParamAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this)
                .get(EditVehicleMarketViewModel::class.java)

        arguments?.let { args ->
            args.get(ARG_CAR_MODEL)?.let {
                vehicle = it as Vehicle
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let {
            binding->
            imagePickers = mutableListOf(binding.layoutAddImage.selectPhoto1,
                    binding.layoutAddImage.selectPhoto2,
                    binding.layoutAddImage.selectPhoto3,
                    binding.layoutAddImage.selectPhoto4,
                    binding.layoutAddImage.selectPhoto5)
        }

        initBar()

        initClickListeners()
        initRecycler()
        initObservers()
        setUpView()
        doDelayed(100){
            viewModel.init(vehicle, viewModel.getUserUid())
        }
    }


    private fun setUpView() {
        vehicle?.let {
            binding?.tvBrandName?.text = it.make?.name
            binding?.tvBrandModel?.text = it.model?.name
            binding?.etVehicleDescription?.setText(it.description)
            if (it.type?.hasNumber == 0)
                setUpNumPlateWithoutNumber(it)
            else setUpNumPlateWithNumber(it)
        }
    }

    private fun setUpNumPlateWithoutNumber(it: Vehicle) {
        binding?.nvWithoutNumber?.visible()
        binding?.nvWithoutNumber?.setType(INetworkValues.ACCOUNT_TYPE_REGULAR, null)
        binding?.nvWithoutNumber?.setName(it.make?.name ?: "")
        binding?.nvWithoutNumber?.setModel(it.model?.name ?: "")
    }

    private fun setUpNumPlateWithNumber(it: Vehicle) {
        binding?.nvContainer?.visible()
        val vehicle = VehicleEntity(
                it.number!!,
                VehicleType(it.type!!.typeId!!.toInt()),
                VehicleCountry(it.country!!.countryId!!.toLong()))
        NumberPlateEditView.Builder(binding?.nvNumberPlate!!)
            .setVehicleNew(vehicle.number, vehicle.country?.countryId, vehicle.type?.typeId)
                .build()

        if (it.type!!.typeId!!.toInt() == 1) {
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(
                    dpToPx(-74),
                    dpToPx(-16),
                    dpToPx(-74),
                    dpToPx(-16)
            )
            binding?.nvNumberPlate?.layoutParams = params
        } else if (it.type!!.typeId!!.toInt() == 2) {
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(
                    dpToPx(-47),
                    dpToPx(-26),
                    dpToPx(-47),
                    dpToPx(-26)
            )
            binding?.nvNumberPlate?.layoutParams = params
        }
        binding?.nvNumberPlate?.scaleX = 0.5f
        binding?.nvNumberPlate?.scaleY = 0.5f
    }

    private fun initRecycler() {
        binding?.rvEditMarket?.layoutManager = LinearLayoutManager(context)
        wizardAdapter = VehicleParamAdapter(mutableListOf())
        wizardAdapter.interactor = object : VehicleParamAdapter.IVehicleParamAdapterInteractor {
            override fun onParamClicked(param: VehicleParamModel) {
                act.addFragment(VehicleParamWizardFragment(), Act.LIGHT_STATUSBAR,
                        Arg(IArgContainer.ARG_WIZARD, wizard),
                        Arg(IArgContainer.ARG_WIZARD_POSITION, param.index - 1))
            }
        }
        binding?.rvEditMarket?.adapter = wizardAdapter
    }

    private fun provideFakeData(count: Int): MutableList<VehicleParamModel> {
        val res = mutableListOf<VehicleParamModel>()
        for (i in 0 until count) {
            res.add(VehicleParamModel(i + 1))
        }
        return res
    }

    private fun initBar() {
        val layoutParamsStatusBar = binding?.statusBarMarket?.layoutParams as AppBarLayout.LayoutParams
        layoutParamsStatusBar.height = context.getStatusBarHeight()
        binding?.statusBarMarket?.layoutParams = layoutParamsStatusBar

    }

    private fun initObservers() {
        viewModel.liveImages.observe(viewLifecycleOwner, Observer {

            initService(it)
            for (i in 0 until it.size) {
                run loop@{
                    imagePickers.forEach { photo ->
                        if (!photo.hasContent) {
                            photo.loadImageWithGlide(it[i])
                            return@loop
                        }
                    }
                }
            }
        })

        viewModel.liveSetProgress.observe(viewLifecycleOwner, Observer {
            for (i in 0 until it) {
                run loop@{
                    imagePickers.forEach { photo ->
                        if (!photo.isLoading) {
                            photo.setProgress(true)
                            return@loop
                        }
                    }
                }
            }
        })

        viewModel.liveWizard.observe(viewLifecycleOwner, Observer {
            binding?.pbEditVehicleMarket?.gone()
            wizard = it
            initWizard()
        })

        viewModel.liveEvent.observe(viewLifecycleOwner, Observer {
            when(it) {
                is EditVehicleMarketEvents.OnError ->{
                    binding?.pbEditVehicleMarket?.gone()
                    NToast.with(view)
                            .text(getString(R.string.error_try_later))
                            .show()
                }
            }
        })

    }

    private fun initService(images: MutableList<String>?) {
        images?.let{
            val arrImages = ArrayList(it)
            val intentUploadService = Intent(context, UploadService::class.java)
            intentUploadService.putExtra(
                    UploadService.SERVICE_MODE,
                    UploadService.SERVICE_MODE_REQUEST_UPLOAD
            )
            act.startService(intentUploadService.putExtra(
                    UploadService.IMAGES, arrImages
            ))
        }
    }

    private fun initWizard() {
        wizard?.let {
            wizardAdapter.add(provideFakeData(it.fields.size))
        }
    }

    private fun initClickListeners() {
        /**Когда пользователь нажимает на кнопку добавить, вычисляются пустые ячейки для вставки и открывается окно выбора фото*/
        imagePickers.forEach {
            it.setOnAddClickListener(View.OnClickListener {
//                loadMultiImage(getFreSpace()) { uris ->
//                    viewModel.onImagesChosen(uris)
//                }
            })
        }

        binding?.ivClose?.setOnClickListener {
            act.onBackPressed()
        }

        binding?.ivSaveBtn?.setOnClickListener {
            act.onBackPressed()
        }

        /**Слушатели на удаление фото*/
        binding?.layoutAddImage?.selectPhoto1?.setOnCloseClickListener(View.OnClickListener {
            binding?.layoutAddImage?.selectPhoto1?.imageRemoved()
        })
        binding?.layoutAddImage?.selectPhoto2?.setOnCloseClickListener(View.OnClickListener {
            binding?.layoutAddImage?.selectPhoto2?.imageRemoved()
        })
        binding?.layoutAddImage?.selectPhoto3?.setOnCloseClickListener(View.OnClickListener {
            binding?.layoutAddImage?.selectPhoto3?.imageRemoved()
        })
        binding?.layoutAddImage?.selectPhoto4?.setOnCloseClickListener(View.OnClickListener {
            binding?.layoutAddImage?.selectPhoto4?.imageRemoved()
        })
        binding?.layoutAddImage?.selectPhoto5?.setOnCloseClickListener(View.OnClickListener {
            binding?.layoutAddImage?.selectPhoto5?.imageRemoved()
        })

        // изменение брэнда
        binding?.tvBrandName?.setOnClickListener {
            vehicle?.type?.typeId?.let {typeID->
                add(VehicleParamFillFragment(), Act.LIGHT_STATUSBAR,
                        Arg(ARG_VEHICLE_PARAM_FILL, VehicleParamFillModel(
                                VehicleParamFillFragment.VIEW_TYPE_BRAND,
                                typeID.toInt(),
                                null,
                                null
                        )))
            }
        }
        // изменение модели
        binding?.tvBrandModel?.setOnClickListener {
            vehicle?.type?.typeId?.let {typeID->
                add(VehicleParamFillFragment(), Act.LIGHT_STATUSBAR,
                        Arg(ARG_VEHICLE_PARAM_FILL, VehicleParamFillModel(
                                VehicleParamFillFragment.VIEW_TYPE_MODEL,
                                typeID.toInt(),
                                vehicle?.make?.makeId,
                                null
                        )))
            }
        }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentEditVehicleMarketBinding
        get() = FragmentEditVehicleMarketBinding::inflate

}
