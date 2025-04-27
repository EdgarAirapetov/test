package com.numplates.nomera3.modules.registration.ui.name

import android.os.Bundle
import android.text.InputFilter
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.meera.core.extensions.click
import com.meera.core.extensions.gone
import com.meera.core.extensions.showKeyboard
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentRegistrationNameBinding
import com.numplates.nomera3.modules.registration.ui.RegistrationContainerFragment.Companion.SHOW_KEYBOARD_DELAY
import com.numplates.nomera3.modules.registration.ui.RegistrationNavigationViewModel
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.view.utils.NToast
import com.meera.core.extensions.clickAnimateScaleUp
import com.meera.core.extensions.doDelayed
import com.numplates.nomera3.modules.registration.ui.RegistrationContainerFragment
import java.util.*

class RegistrationNameFragment : BaseFragmentNew<FragmentRegistrationNameBinding>() {

    private val viewModel by viewModels<RegistrationNameViewModel>()

    private val navigationViewModel by viewModels<RegistrationNavigationViewModel>(
        ownerProducer = { requireParentFragment() }
    )

    private val argCountryNumber by lazy { requireArguments().getString(RegistrationContainerFragment.ARG_COUNTRY_NUMBER) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observe()
        initView()
        initNameEditor()
        viewModel.initUserData()
    }

    override fun onResume() {
        super.onResume()
        doDelayed(SHOW_KEYBOARD_DELAY) {
            binding?.etName?.requestFocus()
            binding?.etName?.showKeyboard()
        }
    }

    private fun observe() {
        viewModel.liveData.observe(viewLifecycleOwner) { event ->
            when (event) {
                is RegistrationNameViewEvent.Name -> setName(event.name)
                is RegistrationNameViewEvent.Error -> showError(event.message)
                is RegistrationNameViewEvent.NameAccepted -> setContinueButtonAvailability(true)
                is RegistrationNameViewEvent.NameNotAccepted -> setContinueButtonAvailability(false)
                is RegistrationNameViewEvent.GoToNextStep -> {
                    viewModel.clearAll()
                    navigationViewModel.registrationNameNext(argCountryNumber)
                }
                else -> {}
            }
        }
        viewModel.progressLiveData.observe(viewLifecycleOwner) {
            binding?.cvNextButton?.showProgress(it)
            setContinueButtonAvailability(hasName())
        }
    }

    private fun initView() {
        if (navigationViewModel.isBackFromNameAvailable) {
            binding?.ivBackIcon?.click {
                it.clickAnimateScaleUp()
                navigationViewModel.goBack()
            }
            binding?.ivCloseIcon?.gone()
            binding?.ivBackIcon?.visible()
        } else {
            binding?.ivCloseIcon?.click {
                it.clickAnimateScaleUp()
                navigationViewModel.goBack()
            }
            binding?.ivCloseIcon?.visible()
            binding?.ivBackIcon?.gone()
        }
        binding?.cvNextButton?.click {
            viewModel.continueClicked()
        }
        binding?.tvStep?.text = getString(R.string.registration_step_count, STEP)
    }

    private fun initNameEditor() {
        binding?.etName?.apply {
            this.filters = arrayOf(InputFilter.LengthFilter(MAX_NAME_LENGTH))
            setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
                return@OnEditorActionListener actionId != EditorInfo.IME_ACTION_DONE &&
                        actionId != EditorInfo.IME_ACTION_GO &&
                        actionId != EditorInfo.IME_ACTION_NEXT &&
                        actionId != EditorInfo.IME_ACTION_SEND
            })
            addTextChangedListener(onTextChanged = { v, _, _, _ ->
                val s = v?.toString()?.replace("\n", " ")
                val lines = this.lineCount
                if (lines > MAX_NAME_LINES) {
                    this.text?.delete(this.selectionEnd - 1, selectionStart)
                }
                if (s?.length == 1 && s.first().isLowerCase()) {
                    this.setText(s.first().toString()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
                    this.setSelection(s.length)
                    return@addTextChangedListener
                }

                viewModel.setName(s.toString())
                if (s?.length ?: 0 >= MAX_NAME_LENGTH) {
                    binding?.tvNameDescription?.text = getString(R.string.maximal_symbos_30)
                    binding?.tvNameDescription?.visible()
                } else {
                    binding?.tvNameDescription?.gone()
                }
                setContinueButtonAvailability(hasName())
            })
            setOnKeyListener { _, keyCode, _ ->
                return@setOnKeyListener keyCode == KeyEvent.KEYCODE_ENTER
            }
        }
        setContinueButtonAvailability(false)
    }

    private fun setName(name: String?) {
        if (name.isNullOrEmpty()) return
        binding?.etName?.setText(name)
        binding?.etName?.setSelection(name.length)
    }

    private fun hasName(): Boolean {
        return !binding?.etName?.text.isNullOrEmpty()
    }

    private fun setContinueButtonAvailability(enabled: Boolean) {
        binding?.cvNextButton?.isEnabled = enabled
    }

    private fun showError(message: String?) {
        NToast.with(act)
            .typeError()
            .text(message)
            .show()
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentRegistrationNameBinding
        get() = FragmentRegistrationNameBinding::inflate

    companion object {
        const val STEP = "1"
        private const val MAX_NAME_LINES = 3
        private const val MAX_NAME_LENGTH = 30

    }
}
