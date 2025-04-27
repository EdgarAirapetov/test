package com.numplates.nomera3.modules.communities.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.meera.core.extensions.click
import com.meera.core.extensions.gone
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.visible
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentBottomCommunityDetailsBinding
import com.numplates.nomera3.modules.communities.data.entity.CommunityEntity
import com.numplates.nomera3.modules.communities.data.entity.CommunityMemberState
import com.numplates.nomera3.modules.communities.data.entity.CommunityUserRole
import com.numplates.nomera3.modules.communities.ui.fragment.members.CommunityMembersContainerFragment
import com.numplates.nomera3.modules.communities.ui.viewevent.CommunityMembersViewEvent
import com.numplates.nomera3.modules.communities.ui.viewevent.CommunityViewEvent
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityDetailsViewModel
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityMembersViewModel
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment
import com.numplates.nomera3.presentation.router.IArgContainer
import java.text.SimpleDateFormat
import java.util.Locale

class CommunityDetailsBottomSheetFragment :
    BaseBottomSheetDialogFragment<FragmentBottomCommunityDetailsBinding>() {

    private val detailsViewModel by viewModels<CommunityDetailsViewModel>()
    private val membersViewModel by viewModels<CommunityMembersViewModel>()
    private var isMembersShowingAllowed = false
    private var isInfoLoaded = false
    private var isMembersLoaded = false

    override fun getTheme(): Int = R.style.BottomSheetDialogTransparentTheme

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showProgress(true)
        initObservers()
        arguments?.getInt(IArgContainer.ARG_GROUP_ID)?.let {
            detailsViewModel.getData(it)
            membersViewModel.bind(it)
        }

        arguments?.getBoolean(IArgContainer.ARG_COMMUNITY_SHOW_MEMBERS)?.let {
            if (it) membersViewModel.getData(
                quantity = CommunityMembersViewModel.COMMUNITY_DETAILS_BOTTOM_SHEET_USERS_QUANTITY,
                userState = CommunityMemberState.APPROVED
            )
            isMembersShowingAllowed = it
        }
    }

    private fun initObservers() {
        detailsViewModel.liveViewEvent.observe(viewLifecycleOwner, ::handleDetailsViewEvent)
        membersViewModel.liveViewEvent.observe(viewLifecycleOwner, ::handleMembersViewEvent)
    }

    private fun handleDetailsViewEvent(event: CommunityViewEvent) {
        when (event) {
            is CommunityViewEvent.CommunityData -> showCommunityInfo(event.community)
            else -> {}
        }
    }

    private fun handleMembersViewEvent(event: CommunityMembersViewEvent) {
        when (event) {
            is CommunityMembersViewEvent.SuccessGetApprovedMembers -> {
//                showCommunityUsers(event.members)
            }
            else -> {}
        }
    }

    private fun showCommunityInfo(community: CommunityEntity) {
        if (community.isModerator == 1) {
            binding?.tvEdit?.visible()
            binding?.tvEdit?.click {
                openEditor(community)
            }
        }
        binding?.tvCommunityTitle?.text = community.name
        val formatter = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        binding?.tvCreated?.text =
            "${getString(R.string.community_info_created_text)} ${formatter.format(community.timeCreated)}"
        binding?.tvDescriptionText?.text = community.description
        binding?.tvAllSubscribersBtn?.click {
            openUsers(community)
        }
        isInfoLoaded = true
        if (!isMembersShowingAllowed || isMembersLoaded) showProgress(false)
    }

    private fun openEditor(community: CommunityEntity) {
        dismiss()
        add(
            CommunityEditFragment(),
            Act.LIGHT_STATUSBAR,
            Arg(IArgContainer.ARG_GROUP_ID, community.groupId),
            Arg(IArgContainer.ARG_IS_GROUP_CREATOR, community.isAuthor == 1)
        )
    }

    private fun openUsers(community: CommunityEntity) {
        dismiss()
        val role = when {
            community.isAuthor.isTrue() -> CommunityUserRole.AUTHOR
            community.isModerator.isTrue() -> CommunityUserRole.MODERATOR
            else -> CommunityUserRole.REGULAR
        }
        add(
            CommunityMembersContainerFragment(),
            Act.LIGHT_STATUSBAR,
            Arg(IArgContainer.ARG_GROUP_ID, community.groupId),
            Arg(IArgContainer.ARG_COMMUNITY_USER_ROLE, role),
            Arg(IArgContainer.ARG_COMMUNITY_IS_PRIVATE, community.private)
        )
    }

//    private fun showCommunityUsers(members: CommunityMembersEntity) {
//        val users = members.users
//        users?.forEachIndexed { index, model ->
//            binding?.llSubscribersContainer?.addUserItemView(model, index == users.lastIndex)
//        }
//
//        binding?.tvSubscribersTitle?.visible()
//        binding?.tvAllSubscribersBtn?.visible()
//        members.totalCount.let {
//            binding?.tvSubscribersQuantity?.text = it.toString()
//            binding?.tvSubscribersQuantity?.visible()
//        }
//        isMembersLoaded = true
//        if (isInfoLoaded) showProgress(false)
//        setDialogExpanded()
//    }

//    private fun ViewGroup.addUserItemView(user: UserInfoModel, isLastListItem: Boolean) {
//        val layout = LayoutInflater.from(requireContext())
//            .inflate(R.layout.item_community_user, this, false)
//        layout?.findViewById<TextView>(R.id.tv_user_name)?.text = user.name
//        val tvStatus = layout?.findViewById<TextView>(R.id.tv_user_status)
//        when {
//            user.isAuthor == true -> {
//                tvStatus?.text = getString(R.string.author)
//                tvStatus?.visible()
//            }
//            user.isModerator == true -> {
//                tvStatus?.text = getString(R.string.moderator)
//                tvStatus?.visible()
//            }
//            else -> {
//                tvStatus?.gone()
//            }
//        }
//        layout?.findViewById<ImageView>(R.id.iv_user_avatar)?.let {
//            Glide.with(it.context)
//                .load(user.avatar)
//                .apply(RequestOptions.circleCropTransform())
//                .placeholder(R.drawable.ic_group_avatar_new)
//                .into(it)
//        }
//
//        // hide last element bottom divider view
//        if (isLastListItem) {
//            layout
//                ?.findViewById<View>(R.id.itemBottomDivider)
//                ?.invisible()
//        }
//
//        addView(layout)
//    }

    private fun showProgress(inProgress: Boolean) {
        if (inProgress) {
            binding?.llHeaderContainer?.gone()
            binding?.scrollContainer?.gone()
            binding?.progressLayout?.visible()
        } else {
            binding?.llHeaderContainer?.visible()
            binding?.scrollContainer?.visible()
            binding?.progressLayout?.gone()
        }
    }

//    private fun setDialogExpanded() {
//        dialog?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
//            ?.let {
//                val behavior = BottomSheetBehavior.from(it)
//                behavior.state = BottomSheetBehavior.STATE_EXPANDED
//            }
//    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentBottomCommunityDetailsBinding
        get() = FragmentBottomCommunityDetailsBinding::inflate

}
