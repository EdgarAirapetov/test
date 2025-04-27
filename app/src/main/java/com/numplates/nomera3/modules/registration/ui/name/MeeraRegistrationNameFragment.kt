package com.numplates.nomera3.modules.registration.ui.name

import android.os.Bundle
import android.text.InputFilter
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.showKeyboard
import com.meera.core.extensions.visible
import com.meera.core.utils.KeyboardHeightProvider
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentRegistrationNameBinding
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.modules.registration.ui.RegistrationContainerFragment
import com.numplates.nomera3.modules.registration.ui.RegistrationContainerFragment.Companion.SHOW_KEYBOARD_DELAY
import com.numplates.nomera3.modules.registration.ui.RegistrationNavigationViewModel
import java.util.Locale

private const val MARGIN_BUTTON = 16

class MeeraRegistrationNameFragment : MeeraBaseFragment(R.layout.meera_fragment_registration_name) {

    private val binding by viewBinding(MeeraFragmentRegistrationNameBinding::bind)

    private val viewModel by viewModels<RegistrationNameViewModel>()

    private val navigationViewModel by viewModels<RegistrationNavigationViewModel>(
        ownerProducer = { requireParentFragment() }
    )

    private var keyboardHeightProvider: KeyboardHeightProvider? = null

    private val argCountryNumber by lazy { requireArguments().getString(RegistrationContainerFragment.ARG_COUNTRY_NUMBER) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observe()
        initView()
        initNameEditor()
        viewModel.initUserData()
    }

    override fun onStart() {
        super.onStart()
        keyboardHeightProvider?.release()
        keyboardHeightProvider = KeyboardHeightProvider(binding.root)
        keyboardHeightProvider?.observer = { height ->
            binding.btnContinue.setMargins(bottom = height + MARGIN_BUTTON.dp, end = MARGIN_BUTTON.dp)
        }
    }

    override fun onStop() {
        super.onStop()
        keyboardHeightProvider?.release()
    }

    override fun onResume() {
        super.onResume()
        doDelayed(SHOW_KEYBOARD_DELAY) {
            binding.etName.requestFocus()
            binding.etName.showKeyboard()
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
                is RegistrationNameViewEvent.NameError -> {
                    showNameError(event.errorMessageRes)
                }
                else -> {}
            }
        }
        viewModel.progressLiveData.observe(viewLifecycleOwner) {
            setContinueButtonAvailability(hasName())
        }
    }

    private fun initView() {
        if (navigationViewModel.isBackFromNameAvailable) {
            binding.btnBack.setThrottledClickListener {
                navigationViewModel.goBack()
            }
            binding.btnBack.visible()
        } else {
            binding.btnBack.gone()
        }
        binding.btnContinue.setThrottledClickListener {
            binding.vgErrorContainer.gone()
            viewModel.continueClicked()
        }
    }

    private fun initNameEditor() {
        binding.etName.apply {
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
                if (s?.length == 1 && s.isBlank()) {
                    this.setText(String.empty())
                    return@addTextChangedListener
                }
                if (s?.length == 1 && s.first().isLowerCase()) {
                    this.setText(s.first().toString()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
                    this.setSelection(s.length)
                    return@addTextChangedListener
                }

                viewModel.setName(s.toString())
                if ((s?.length ?: 0) >= MAX_NAME_LENGTH) {
                    binding.tvErrorMessage.text = getString(R.string.maximal_symbos_30)
                    binding.vgErrorContainer.visible()
                } else {
                    binding.vgErrorContainer.gone()
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
        binding.etName.setText(name)
        binding.etName.setSelection(name.length)
    }

    private fun hasName(): Boolean {
        return !binding.etName.text.isNullOrEmpty()
    }

    private fun setContinueButtonAvailability(enabled: Boolean) {
        binding.btnContinue.isEnabled = enabled
    }

    private fun showError(message: String?) {
        UiKitSnackBar.make(
            requireView(), SnackBarParams(
                SnackBarContainerUiState(
                    messageText = message,
                    avatarUiState = AvatarUiState.ErrorIconState
                )
            )
        ).show()
    }

    private fun showNameError(@StringRes errorMessageRes: Int) {
        binding.vgErrorContainer.visible()
        binding.tvErrorMessage.setText(errorMessageRes)
    }

    companion object {
        private const val MAX_NAME_LINES = 3
        private const val MAX_NAME_LENGTH = 30

    }
}
