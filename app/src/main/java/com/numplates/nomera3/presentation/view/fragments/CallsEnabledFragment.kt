package com.numplates.nomera3.presentation.view.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.CallsEnabledFragmentBinding
import com.numplates.nomera3.modules.appDialogs.ui.KEY_CALLS_DIALOG_DISMISS
import com.numplates.nomera3.presentation.model.adaptermodel.CallsEnabledViewPagerAdapter
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.view.widgets.CustomRowSelector
import com.numplates.nomera3.presentation.viewmodel.PrivacyCallsViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.CallsEnabledViewEvent
import timber.log.Timber


class CallsEnabledFragment : AppCompatDialogFragment() {

    private val viewModel by viewModels<PrivacyCallsViewModel>()
    private var adapterPos = CALLS_ENABLED_INFO
    private var vpAdapter: CallsEnabledViewPagerAdapter? = null
    private var binding: CallsEnabledFragmentBinding? = null
    private var selectedSetting: CustomRowSelector.CustomRowSelectorModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CallsEnabledFragmentBinding.inflate(inflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.init()
        init()
        initObservables()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun initObservables() {
        viewModel.liveEvent.observe(this, Observer { event ->
            when (event) {
                is CallsEnabledViewEvent.SettingSaved -> handleSettingsSaved(event.model)
                is CallsEnabledViewEvent.SettingSavedError -> handleSettingError()
            }
        })
    }

    private fun handleSettingError() {
        selectedSetting = null
        setupDeactivatedBtnNext()
        vpAdapter?.clearSelections()
        showError()
    }

    private fun showError() {
        NToast.with(view)
            .typeError()
            .text(getString(R.string.error_check_internet))
            .show()
    }

    private fun handleSettingsSaved(model: CustomRowSelector.CustomRowSelectorModel) {
        selectedSetting = model
        setupCommonBtnNext()
    }

    private fun init() {
        vpAdapter = CallsEnabledViewPagerAdapter(object :
            CallsEnabledViewPagerAdapter.ICallAdapterInteractor {
            override fun onRowSettingClicked(model: CustomRowSelector.CustomRowSelectorModel) {
                //Timber.d("model = $model")
                viewModel.setCallSetting(model)
            }

        })
        binding?.vpCallsEnabled?.adapter = vpAdapter
        context?.let { cntx ->
            binding?.spiIndicator?.selectedDotColor =
                ContextCompat.getColor(cntx, R.color.ui_purple)
        }

        binding?.vpCallsEnabled?.let {
            binding?.spiIndicator?.attachToPager(it)
        }


        binding?.tvNext?.setOnClickListener {
            if (adapterPos == CALLS_ENABLED_INFO) {
                binding?.vpCallsEnabled?.setCurrentItem(PRIVACY_SETTINGS, true)
            } else if (adapterPos == PRIVACY_SETTINGS && selectedSetting != null) {
                viewModel.dialogShowed()
                viewModel.closeClicked(selectedSetting?.selectorModelId)
                dismiss()
            }
        }

        binding?.vpCallsEnabled?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) = Unit
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) = Unit
            override fun onPageSelected(position: Int) {
                adapterPos = position
                Timber.d("adapter position = $position")
                if (position == CALLS_ENABLED_INFO) {
                    setupCommonBtnNext()
                } else if (position == PRIVACY_SETTINGS) {
                    selectedSetting?.let {
                        setupCommonBtnNext()
                    } ?: kotlin.run {
                        setupDeactivatedBtnNext()
                    }
                }
            }
        })
    }

    private fun setupDeactivatedBtnNext() {
        context?.let { cntx ->
            binding?.cvNext?.setCardBackgroundColor(
                ContextCompat.getColor(
                    cntx,
                    R.color.ui_light_gray
                )
            )
            binding?.tvNext?.background =
                ContextCompat.getDrawable(cntx, R.drawable.btn_gray_nomera)
        }
    }

    private fun setupCommonBtnNext() {
        context?.let { cntx ->
            binding?.cvNext?.setCardBackgroundColor(ContextCompat.getColor(cntx, R.color.ui_purple))
            binding?.tvNext?.background =
                ContextCompat.getDrawable(cntx, R.drawable.btn_violet_nomera)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        viewModel.onDialogDismissed()
        parentFragmentManager.setFragmentResult(KEY_CALLS_DIALOG_DISMISS, Bundle())
        super.onDismiss(dialog)
    }

    /**
     * Diasbling onBackPressed
     * */

    private fun onDialogKeyEvent(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK
            && event.action == KeyEvent.ACTION_UP
            && !event.isCanceled
        ) {
            return true
        }
        return false
    }

    /**
     *  Переопреляем метод oncreateDialog. Устанавливаем собственные лаяут параметры, отключаем слушатель
     *  на кнопку назад
     * */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val root = ConstraintLayout(requireContext())
        root.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(root)
        dialog.setOnKeyListener { _, keyCode, event -> onDialogKeyEvent(keyCode, event) }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return dialog
    }

    companion object {
        const val CALLS_ENABLED_INFO = 0
        const val PRIVACY_SETTINGS = 1
    }


}
