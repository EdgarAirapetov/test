package com.numplates.nomera3.presentation.view.fragments

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.meera.core.extensions.getStatusBarHeight
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.LayoutDeleteFriendBinding
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_REMOVE_ACCOUNT_REASON_ID
import com.numplates.nomera3.presentation.view.fragments.dialogs.ConfirmDialogBuilder
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.viewmodel.ProfileDeleteRecoveryViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.UserProfileViewEvent
class DeleteProfileFragment: BaseFragmentNew<LayoutDeleteFriendBinding>() {

    val viewModel by viewModels<ProfileDeleteRecoveryViewModel>()
    private var reasonId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reasonId = arguments?.getInt(ARG_REMOVE_ACCOUNT_REASON_ID)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initClickListeners()
        initObservers()
    }

    private fun initView(){
        binding?.statusBarDeleteProfile?.layoutParams?.height = context.getStatusBarHeight()
        binding?.tvDeleteDesc1?.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(getString(R.string.delete_profile_desc_1), Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(getString(R.string.delete_profile_desc_1))
        }

        binding?.tvDeleteDesc2?.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(getString(R.string.delete_profile_desc_2), Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(getString(R.string.delete_profile_desc_2))
        }
    }

    private fun initObservers() {
        viewModel.liveViewEvent.observe(viewLifecycleOwner, Observer {  event->
            when(event){
                is UserProfileViewEvent.ProfileDeleteSuccess -> handleSuccessProfileDeleted()
                is UserProfileViewEvent.ProfileDeleteError -> handleErrorProfileDeleted()
                else -> {}
            }
        })
    }

    private fun handleErrorProfileDeleted() {
        NToast.with(view)
                .text(getString(R.string.error_while_deleting_profile))
                .typeError()
                .show()
    }

    private fun handleSuccessProfileDeleted() {
        act.logOutWithDelegate()
    }

    private fun initClickListeners(){
        binding?.buttonDelete?.setOnClickListener {
            showConfirmDialog()
        }

        binding?.ivBackDeleteProfile?.setOnClickListener {
            act.onBackPressed()
        }
    }

    private fun showConfirmDialog() {
        ConfirmDialogBuilder()
                .setHeader(getString(R.string.are_you_sure))
                .setDescription(getString(R.string.all_data_will_be_deleted))
                .setLeftBtnText(getString(R.string.account_remove_cancel))
                .setRightBtnText(getString(R.string.delete_caps))
                .setRightClickListener {
                    viewModel.deleteProfile(reasonId)
                }
                .show(childFragmentManager)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> LayoutDeleteFriendBinding
        get() = LayoutDeleteFriendBinding::inflate

}
