package com.numplates.nomera3.presentation.view.fragments.privacysettings

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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.meera.core.extensions.click
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentFriendsFollowersPrivacyBinding
import com.numplates.nomera3.presentation.view.adapter.FriendsFollowersActionCallback
import com.numplates.nomera3.presentation.view.fragments.entity.FriendsFollowersPrivacyUiEvent
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.FriendsFollowersPrivacyAdapter
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.FriendsFollowersPrivacyAdapter.Companion.PRIVACY_INFO_POSITION
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.FriendsFollowersPrivacyAdapter.Companion.PRIVACY_SELECT_POSITION
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.FriendsFollowersPrivacyViewModel
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.view.widgets.CustomRowSelector
import kotlinx.coroutines.flow.collect

class FriendsFollowersPrivacyFragment : DialogFragment(), FriendsFollowersActionCallback {

    private var binding: FragmentFriendsFollowersPrivacyBinding? = null
    private var friendsFollowersPrivacyAdapter: FriendsFollowersPrivacyAdapter? = null
    private val viewModel by viewModels<FriendsFollowersPrivacyViewModel>()
    private var dismissListener: (() -> Unit)? = null
    private val pagerChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) = Unit

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit

        override fun onPageSelected(position: Int) {
            updateButtonStateByPagerPosition(position)
        }
    }

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFriendsFollowersPrivacyBinding.inflate(inflater)
        return requireNotNull(binding?.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initStateObserver()
        initPagerListener()
        observeViewEvent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onPrivacyClicked(model: CustomRowSelector.CustomRowSelectorModel) {
        viewModel.setFollowersPrivacy(model)
    }

    override fun onDismiss(dialog: DialogInterface) {
        dismissListener?.invoke()
        viewModel.setDialogDismissed()
        super.onDismiss(dialog)
    }

    fun show(fragmentManager: FragmentManager) {
        if (fragmentManager.findFragmentByTag(TAG) == null) {
            super.showNow(fragmentManager, TAG)
        }
    }

    fun setDismissListener(dismissListener: () -> Unit) {
        this.dismissListener = dismissListener
    }

    private fun initView() {
        friendsFollowersPrivacyAdapter = FriendsFollowersPrivacyAdapter(this)
        binding?.vgFriendsFollowersPager?.adapter = friendsFollowersPrivacyAdapter
        binding?.spiIndicator?.attachToPager(binding?.vgFriendsFollowersPager ?: return)
        binding?.tvBtnOk?.click {
            handleBtnClick()
        }
    }

    private fun onDialogKeyEvent(keyCode: Int, event: KeyEvent): Boolean {
        return keyCode == KeyEvent.KEYCODE_BACK
                && event.action == KeyEvent.ACTION_UP
                && !event.isCanceled
    }

    private fun initStateObserver() {
        viewModel.friendsPrivacyLiveState.observe(viewLifecycleOwner) { state ->
            binding?.tvBtnOk?.isEnabled = state.isButtonEnabled
            binding?.tvBtnOk?.text = state.buttonText
        }
    }

    private fun handleBtnClick() {
        when (binding?.vgFriendsFollowersPager?.currentItem) {
            PRIVACY_INFO_POSITION -> {
                binding?.vgFriendsFollowersPager?.currentItem = PRIVACY_SELECT_POSITION
            }
            PRIVACY_SELECT_POSITION -> {
                viewModel.pushDialogShowed()
                dismiss()
            }
        }
    }

    private fun showErrorSnackBar() {
        NToast.with(activity as? Act)
            .typeError()
            .text(getString(R.string.error_check_internet))
            .inView(dialog?.window?.decorView)
            .show()
    }

    private fun updateButtonStateByPagerPosition(position: Int) {
        when (position) {
            PRIVACY_INFO_POSITION -> {
                viewModel.updateButtonState(
                    buttonText = context?.getString(R.string.next),
                    isButtonEnabled = true
                )
            }
            PRIVACY_SELECT_POSITION -> {
                viewModel.updateButtonState(
                    buttonText = context?.getString(R.string.ready_button),
                    isButtonEnabled = viewModel.privacyRow != null
                )
            }
        }
    }

    private fun observeViewEvent() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.friendsPrivacyViewUiEvent.collect { viewEvent ->
                handleViewEvent(viewEvent)
            }
        }
    }

    private fun handleViewEvent(viewUiEvent: FriendsFollowersPrivacyUiEvent) {
        when (viewUiEvent) {
            FriendsFollowersPrivacyUiEvent.ErrorSelectPrivacyUi -> {
                friendsFollowersPrivacyAdapter?.clearSelections()
                viewModel.privacyRow = null
                viewModel.updateButtonState(
                    buttonText = context?.getString(R.string.ready_button),
                    isButtonEnabled = false
                )
                showErrorSnackBar()
            }
        }
    }

    private fun initPagerListener() {
        binding?.vgFriendsFollowersPager?.addOnPageChangeListener(pagerChangeListener)
    }

    companion object {
        private const val TAG = "friendsFollowersPrivacyFragment"
    }
}
