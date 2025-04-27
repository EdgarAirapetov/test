package com.numplates.nomera3.modules.user.ui.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentUserComplainAdditionalBinding
import com.numplates.nomera3.modules.complains.ui.ComplainEvents
import com.numplates.nomera3.modules.complains.ui.UserComplainViewModel
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment
import com.meera.core.extensions.click
import com.meera.core.extensions.color
import com.meera.core.extensions.string
import com.meera.core.extensions.textColor
import android.content.res.ColorStateList
import androidx.lifecycle.lifecycleScope
import com.numplates.nomera3.modules.baseCore.helper.amplitude.ComplainExtraActions
import com.numplates.nomera3.App
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class UserComplainAdditionalBottomSheet : BaseBottomSheetDialogFragment<FragmentUserComplainAdditionalBinding>() {

    private val viewModel by viewModels<UserComplainViewModel> { App.component.getViewModelFactory() }
    private var userId: Long? = null
    private var optionHideMoments: Boolean = false
    var callback: AdditionalComplainCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getLong(KEY_COMPLAIN_USER_ID)
            optionHideMoments = it.getBoolean(KEY_COMPLAIN_OPTION_HIDE_MOMENTS)
        }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentUserComplainAdditionalBinding
        get() = FragmentUserComplainAdditionalBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.llBtnApply?.setBackgroundResource(R.drawable.gradient_purple_disabled_button_ripple)

        binding?.rbHideUserMoments?.isVisible = optionHideMoments
        binding?.rbHideUserPosts?.isVisible = !optionHideMoments

        binding?.rgAdditionaSteps?.setOnCheckedChangeListener { group, checkedId ->
            group.children.forEach { view ->
                (view as? RadioButton)?.apply {
                    if (id == checkedId) {
                        textColor(R.color.colorPrimary)
                    } else {
                        textColor(R.color.black_1000)
                    }
                }
            }
            if (checkedId != -1) binding?.llBtnApply?.setBackgroundResource(R.drawable.bg_user_complaint)
        }
        binding?.llBtnApply?.click {
            when (binding?.rgAdditionaSteps?.checkedRadioButtonId) {
                R.id.rb_block -> {
                    viewModel.blockUser(userId, true)
                }
                R.id.rb_hide_user_posts -> {
                    viewModel.hideUserRoad(userId)
                }
                R.id.rb_hide_user_moments -> {
                    viewModel.hideUserMoments(userId)
                }
            }
        }
        binding?.llBtnDismiss?.click {
            viewModel.logAdditionalEvent(ComplainExtraActions.NO_THX)
            dismiss()
        }

        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
            ), intArrayOf(
                context?.color(R.color.colorGrayA7A5) ?: 0,
                context?.color(R.color.colorPrimary) ?: 0// Checked
            )
        )
        binding?.rbBlock?.buttonTintList = colorStateList
        binding?.rbHideUserPosts?.buttonTintList = colorStateList
        binding?.rbHideUserMoments?.buttonTintList = colorStateList
        initObserver()
    }

    private fun initObserver() {
        viewModel.complainEvents.onEach { event ->
            when (event) {
                is ComplainEvents.MomentsHidden -> {
                    callback?.onSuccess(
                        msg = context?.string(R.string.user_complain_moments_hidden),
                        reason = event
                    )
                    dismiss()
                }
                is ComplainEvents.PostsDisabledEvents -> {
                    callback?.onSuccess(
                        msg = context?.string(R.string.user_complain_posts_hided),
                        reason = ComplainEvents.PostsDisabledEvents
                    )
                    viewModel.logAdditionalEvent(ComplainExtraActions.HIDE)
                    dismiss()
                }
                is ComplainEvents.UserBlocked -> {
                    callback?.onSuccess(
                        msg = context?.string(R.string.user_complain_user_blocked),
                        reason = ComplainEvents.UserBlocked
                    )
                    viewModel.logAdditionalEvent(ComplainExtraActions.BLOCK)
                    dismiss()
                }
                is ComplainEvents.ComplainFailed -> {
                    callback?.onError(context?.string(R.string.user_complain_error))
                    viewModel.logAdditionalEvent(ComplainExtraActions.NONE)
                    dismiss()
                }
                else -> Unit
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onDismiss(dialog: DialogInterface) {
        dialogListener?.onDismissDialog()
        super.onDismiss(dialog)
    }

    companion object {

        const val KEY_COMPLAIN_USER_ID = "KEY_COMPLAIN_USER_ID"
        const val KEY_COMPLAIN_OPTION_HIDE_MOMENTS = "KEY_COMPLAIN_OPTION_HIDE_MOMENTS"

        fun newInstance(userId: Long, optionHideMoments: Boolean = false): UserComplainAdditionalBottomSheet {
            val arg = Bundle()
            arg.putLong(KEY_COMPLAIN_USER_ID, userId)
            arg.putBoolean(KEY_COMPLAIN_OPTION_HIDE_MOMENTS, optionHideMoments)
            val fragment = UserComplainAdditionalBottomSheet()
            fragment.arguments = arg
            return fragment
        }
    }

}

interface AdditionalComplainCallback {
    fun onSuccess(msg: String?, reason: ComplainEvents)
    fun onError(msg: String?)
}
