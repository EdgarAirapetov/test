package com.numplates.nomera3.presentation.view.fragments.market.configurevehicle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.appbar.AppBarLayout
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.meera.core.extensions.visibleSlideInAnimate
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.market.Field
import com.numplates.nomera3.data.network.market.ResponseWizard
import com.numplates.nomera3.databinding.FragmentVehicleParamBinding
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.adapter.vehicleparam.VehicleColorAdapter
import com.numplates.nomera3.presentation.view.adapter.vehicleparam.VehicleParamSelectAdapter
import com.numplates.nomera3.presentation.viewmodel.VehicleParamViewModel

class VehicleParamWizardFragment : BaseFragmentNew<FragmentVehicleParamBinding>() {

    var mode = 1

    private lateinit var viewModel: VehicleParamViewModel
    private var wizard: ResponseWizard? = null
    private var wizardPos = 0

    private lateinit var colorAdapter: VehicleColorAdapter
    private lateinit var listAdapter: VehicleParamSelectAdapter

    internal var wizardListener: (String) -> Unit = { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.get(IArgContainer.ARG_WIZARD)?.let {
            wizard = it as ResponseWizard
        }
        arguments?.get(IArgContainer.ARG_WIZARD_POSITION)?.let {
            wizardPos = it as Int
        }

        viewModel = ViewModelProviders.of(this).get(VehicleParamViewModel::class.java)
    }


    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentVehicleParamBinding
        get() = FragmentVehicleParamBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        initToolbar()
        initClickListeners()
        initObservers()
        viewModel.init(wizard, wizardPos)
    }

    private fun initObservers() {
        viewModel.liveField.observe(viewLifecycleOwner, Observer {
            handleField(it)
        })

        viewModel.liveCounter.observe(viewLifecycleOwner, Observer {
            binding?.tvVehicleParamStep?.text = getString(R.string.wizard_steps, it.first, it.second)
        })
    }

    private fun handleField(field: Field?) {
        field?.let {
            when(it.type){
                Field.TYPE_COLOR -> {
                    setColorMode()
                    colorAdapter.replaceData(it.value)
                    val columns = calculateNumberOfColumns(102)
                    binding?.rvVehicleParam?.layoutManager = GridLayoutManager(context, columns)
                    binding?.rvVehicleParam?.adapter = colorAdapter
                    (binding?.rvVehicleParam?.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
                }
                Field.TYPE_LIST -> {
                    setSelectorMode()
                    binding?.rvVehicleParam?.layoutManager = LinearLayoutManager(context)
                    listAdapter.replaceData(it.value)
                    binding?.rvVehicleParam?.adapter = listAdapter
                }
                Field.TYPE_TEXT -> {
                    setTextMode()
                    if (it.units.isNotBlank()) {
                        binding?.tvVehicleRightText?.visible()
                        binding?.tvVehicleRightText?.text = it.units
                    }
                }
            }
            binding?.tvHeader?.text = it.name
        }
    }

    private fun initClickListeners(){
        binding?.textView7?.setOnClickListener {
            viewModel.getNextField()
        }
        setEnabledContinueBtn(true)
    }

    private fun initRecycler() {
        context?.let {
            colorAdapter = VehicleColorAdapter(mutableListOf())
            listAdapter = VehicleParamSelectAdapter(mutableListOf())
            (binding?.rvVehicleParam?.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
    }

    private fun calculateNumberOfColumns(columnWidthDp: Int): Int {
        val displayMetrics = context?.resources?.displayMetrics
        displayMetrics?.let {
            val screenWidthDp = it.widthPixels / it.density
            return (screenWidthDp / columnWidthDp + 0.5).toInt()
        } ?: kotlin.run {
            return 3
        }
    }

    private fun initToolbar() {
        binding?.tbVehicleParam?.setNavigationIcon(R.drawable.arrowback)
        binding?.tbVehicleParam?.setNavigationOnClickListener {
            act.onBackPressed()
        }
        val layoutParamsStatusBar = binding?.statusBarGroups?.layoutParams as AppBarLayout.LayoutParams
        layoutParamsStatusBar.height = context.getStatusBarHeight()
        binding?.statusBarGroups?.layoutParams = layoutParamsStatusBar
    }


    private fun setColorMode() {
        binding?.clVehicleParamEtContainer?.gone()
        binding?.rvVehicleParam?.visibleSlideInAnimate()
        binding?.cvVehicleContinue?.visibleSlideInAnimate()
        binding?.textView7?.visibleSlideInAnimate()
    }


    private fun setTextMode() {
        binding?.cvVehicleContinue?.gone()
        binding?.clVehicleParamEtContainer?.visibleSlideInAnimate()
        binding?.cvVehicleContinue?.visibleSlideInAnimate()
        binding?.rvVehicleParam?.gone()
        binding?.textView7?.visibleSlideInAnimate()

    }

    private fun setSelectorMode() {
        binding?.cvVehicleContinue?.gone()
        binding?.clVehicleParamEtContainer?.gone()
        binding?.rvVehicleParam?.visibleSlideInAnimate()
        binding?.textView7?.visibleSlideInAnimate()
    }

    private fun setEnabledContinueBtn(isEnabled: Boolean) {
        if (isEnabled) {
            binding?.tvContinueBtn?.setBackgroundResource(R.drawable.btnviolet)
            context?.let {
                binding?.cvVehicleContinue?.setCardBackgroundColor(ContextCompat.getColor(it, R.color.colorPrimaryDark))
            }
        } else {
            binding?.tvContinueBtn?.setBackgroundResource(R.drawable.btngray)
            context?.let {
                binding?.cvVehicleContinue?.setCardBackgroundColor(ContextCompat.getColor(it, R.color.colorGrey))
            }
        }
    }
}
