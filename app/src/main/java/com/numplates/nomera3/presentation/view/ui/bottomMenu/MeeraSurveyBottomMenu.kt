package com.numplates.nomera3.presentation.view.ui.bottomMenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.simpleName
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraSurveyBottomMenuLayoutBinding
import com.numplates.nomera3.presentation.model.enums.RoadSelectionEnum
import com.numplates.nomera3.presentation.model.enums.WhoCanCommentPostEnum

enum class MeeraSurveyBottomMenuMode {
    POST, EVENT
}

class MeeraSurveyBottomMenu : UiKitBottomSheetDialog<MeeraSurveyBottomMenuLayoutBinding>() {

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraSurveyBottomMenuLayoutBinding
        get() = MeeraSurveyBottomMenuLayoutBinding::inflate

    var mode: MeeraSurveyBottomMenuMode = MeeraSurveyBottomMenuMode.POST
    var isCommunityCommentingOptionMode: Boolean = false
    var canCreatePostInMainRoad = false

    var allClickedListener: () -> Unit = {}
    var noOneClickedListener: () -> Unit = {}
    var friendsClickedListener: () -> Unit = {}
    var onMainRoadClickListener: () -> Unit = {}
    var onMainRoadPostForbidden: () -> Unit = {}
    var onMyRoadClickListener: () -> Unit = {}

    var commentsState = WhoCanCommentPostEnum.EVERYONE
    var roadType =  RoadSelectionEnum.MAIN
    var isRoad = true


    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (mode == MeeraSurveyBottomMenuMode.EVENT) {
            setupViewForEvents()
        } else {
            setupViewForPosts()
        }

        when (roadType) {
            RoadSelectionEnum.MAIN -> {
                mainRoadClicked()
            }

            RoadSelectionEnum.MY -> {
                myRoadClicked()
            }
        }

        when (commentsState) {
            WhoCanCommentPostEnum.EVERYONE -> {
                allClicked()
            }
            WhoCanCommentPostEnum.NOBODY -> {
                noOneClicked()
            }
            WhoCanCommentPostEnum.COMMUNITY_MEMBERS,
            WhoCanCommentPostEnum.FRIENDS -> {
                friendsClicked()
            }
        }
        initClickListeners()
    }


    private fun setupViewForPosts() {
        if (isCommunityCommentingOptionMode) {
            contentBinding?.llPublicationTarget?.gone()
            contentBinding?.cellCommentFriends?.setTitleValue(getString(R.string.community_member_can_comment_post_option))
        } else {
            if (!isRoad) {
                contentBinding?.cellCommentFriends?.gone()
            }
        }

        contentBinding?.cellMainRoad?.setRightElementContainerClickable(false)
        contentBinding?.cellMyRoad?.setRightElementContainerClickable(false)
        contentBinding?.cellCommentAll?.setRightElementContainerClickable(false)
        contentBinding?.cellCommentFriends?.setRightElementContainerClickable(false)
        contentBinding?.cellCommentNobody?.setRightElementContainerClickable(false)

        rootBinding?.tvBottomSheetDialogLabel?.text = getString(R.string.meera_post_settings)
    }

    private fun setupViewForEvents() {
        rootBinding?.tvBottomSheetDialogLabel?.text = getString(R.string.meera_event_settings)
        contentBinding?.apply {
            llPublicationTarget.gone()
            tvWhoCanComment.text = getString(R.string.who_can_comment_event)
        }
        contentBinding?.cellCommentAll?.setRightElementContainerClickable(false)
        contentBinding?.cellCommentFriends?.setRightElementContainerClickable(false)
        contentBinding?.cellCommentNobody?.setRightElementContainerClickable(false)
    }

    fun show(manager: FragmentManager?) {
        val fragment = manager?.findFragmentByTag(simpleName)
        if (fragment != null)
            return
        manager?.let {
            super.show(manager, simpleName)
        }
    }

    private fun initClickListeners() {
        contentBinding?.cellMainRoad?.setThrottledClickListener {
            if(canCreatePostInMainRoad){
                mainRoadClicked()
            } else {
                dismiss()
                onMainRoadPostForbidden()
            }
        }

        contentBinding?.cellMyRoad?.setThrottledClickListener {
            myRoadClicked()
        }

        contentBinding?.cellCommentAll?.setThrottledClickListener {
            allClicked()
        }

        contentBinding?.cellCommentFriends?.setThrottledClickListener {
            friendsClicked()
        }

        contentBinding?.cellCommentNobody?.setThrottledClickListener {
            noOneClicked()
        }
    }

    private fun mainRoadClicked() {
        onMainRoadClickListener()
        contentBinding?.cellMainRoad?.setCellRightElementChecked(true)
        contentBinding?.cellMyRoad?.setCellRightElementChecked(false)
    }

    private fun myRoadClicked() {
        onMyRoadClickListener()
        contentBinding?.cellMainRoad?.setCellRightElementChecked(false)
        contentBinding?.cellMyRoad?.setCellRightElementChecked(true)
    }

    private fun allClicked() {
        allClickedListener()
        contentBinding?.cellCommentAll?.setCellRightElementChecked(true)
        contentBinding?.cellCommentFriends?.setCellRightElementChecked(false)
        contentBinding?.cellCommentNobody?.setCellRightElementChecked(false)
    }

    private fun friendsClicked() {
        friendsClickedListener()
        contentBinding?.cellCommentAll?.setCellRightElementChecked(false)
        contentBinding?.cellCommentFriends?.setCellRightElementChecked(true)
        contentBinding?.cellCommentNobody?.setCellRightElementChecked(false)
    }

    private fun noOneClicked() {
        noOneClickedListener()
        contentBinding?.cellCommentAll?.setCellRightElementChecked(false)
        contentBinding?.cellCommentFriends?.setCellRightElementChecked(false)
        contentBinding?.cellCommentNobody?.setCellRightElementChecked(true)
    }
}


