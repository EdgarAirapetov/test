package com.numplates.nomera3.modules.chat

import android.os.Bundle
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.meera.core.extensions.safeNavigate
import com.meera.db.models.dialog.UserChat
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.findfriends.AmplitudeFindFriendsWhereProperty
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAINT_ROOM_ID
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAINT_SEND_RESULT
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAINT_USER_ID
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAINT_WHERE_VALUE
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAIN_TYPE
import com.numplates.nomera3.modules.complains.ui.reason.ComplainType
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentClickOrigin
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_MOMENT_CLICK_ORIGIN
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_MOMENT_TARGET_ID
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_USER_ID
import com.numplates.nomera3.presentation.router.IArgContainer

class MeeraChatNavigator(private val fragment: Fragment) {

    private val navController: NavController get() = fragment.findNavController()

    fun gotoCommunityFragment(groupId: Int?) {
        navController.safeNavigate(
            resId = R.id.action_chatMessagesFragment_to_meeraCommunityFragment,
            bundle = Bundle().apply {
                putInt(IArgContainer.ARG_GROUP_ID, groupId ?: return)
            }
        )
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    fun openChatFragment(event: String) {
//        activityCallback?.onAddFragment(
//            fragment = MeeraChatFragment(),
//            isLightStatusBar = Act.LIGHT_STATUSBAR,
//            mapArgs = hashMapOf(
//                IArgContainer.ARG_USER_ID to event,
//                IArgContainer.ARG_ROOM_TYPE to ROOM_TYPE_DIALOG
//            )
//        )

        Toast.makeText(fragment.requireContext(), "openChatFragment", Toast.LENGTH_SHORT).show()
    }

    /**
     *  NEED TEST FROM DIFFERENT PLACES потому что в некотоорых метсах было вызванно не через активити в через фргмент
     *  3128 строка через фрагмент
     *  3162 через активити
     * */
    fun gotoUserInfoFragment(userId: Long?, where: AmplitudePropertyWhere) {
        navController.safeNavigate(
            resId = R.id.userInfoFragment,
            bundle = bundleOf(
                IArgContainer.ARG_USER_ID to userId,
                IArgContainer.ARG_TRANSIT_FROM to where.property
            )
        )
    }

    fun gotoAboutChatScreen(roomId: Long?) {
        if (roomId == null) return
        navController.safeNavigate(
            resId = R.id.action_chatMessagesFragment_to_groupChatAboutFragment,
            bundle = Bundle().apply {
                putLong(IArgContainer.ARG_ROOM_ID, roomId)
            }
        )
    }

    /**
     * Method for receive gif
     * */
    fun gotoUserGiftFragment(myUid: Long, birth: Long) {
        navController.safeNavigate(
            resId = R.id.action_chatMessagesFragment_to_meeraUserGiftsFragment,
            bundle = bundleOf(
                IArgContainer.ARG_USER_ID to myUid,
                IArgContainer.ARG_USER_DATE_OF_BIRTH to birth,
                IArgContainer.ARG_GIFT_SEND_WHERE to AmplitudePropertyWhere.CHAT
            )
        )
    }

    /**
     * Method for send gift
     * */
    fun gotoUserGiftFragment(companion: UserChat?) {
        navController.safeNavigate(
            resId = R.id.action_chatMessagesFragment_to_meeraUserGiftsFragment,
            bundle = bundleOf(
                IArgContainer.ARG_USER_ID to companion?.userId,
                IArgContainer.ARG_USER_NAME to companion?.name,
                IArgContainer.ARG_USER_DATE_OF_BIRTH to companion?.birthDate,
                IArgContainer.ARG_GIFT_SEND_WHERE to AmplitudePropertyWhere.CHAT
            )
        )
    }

    fun gotoGiftListFragment(user: UserChat, isSendBack: Boolean = false) {
        navController.safeNavigate(
            resId = R.id.action_chatMessagesFragment_to_meeraGiftsListFragment,
            bundle = Bundle().apply {
                putLong(IArgContainer.ARG_USER_ID, user.userId ?: return)
                putString(IArgContainer.ARG_USER_NAME, user.name)
                putLong(IArgContainer.ARG_USER_DATE_OF_BIRTH, user.birthDate ?: return)
                putBoolean(IArgContainer.ARG_SEND_BACK, isSendBack)
                putSerializable(IArgContainer.ARG_GIFT_SEND_WHERE, AmplitudePropertyWhere.CHAT)
            }
        )
    }

    fun gotoHashTagFragment(hashtag: String?) {
        navController.safeNavigate(
            resId = R.id.action_chatMessagesFragment_to_meeraHashTagFragment,
            bundle = bundleOf(
                IArgContainer.ARG_HASHTAG to hashtag
            )
        )
    }

    fun gotoPostFragment(postId: Long) {
        navController.safeNavigate(
            resId = R.id.action_chatMessagesFragment_to_meeraPostFragmentV2,
            bundle = bundleOf(
                IArgContainer.ARG_FEED_POST_ID to postId,
                IArgContainer.ARG_POST_ORIGIN to DestinationOriginEnum.CHAT
            )
        )
    }

    fun gotoMomentFragment(
        userId: Long? = null,
        momentId: Long? = null,
    ) {
        navController.safeNavigate(
            resId = R.id.action_global_meeraViewMomentFragment,
            bundle = bundleOf(
                KEY_USER_ID to userId,
                KEY_MOMENT_TARGET_ID to momentId,
                KEY_MOMENT_CLICK_ORIGIN to MomentClickOrigin.fromUserAvatar()
            )
        )
    }

    fun gotoProfilePhotoViewer(url: String) {
        navController.safeNavigate(
            resId = R.id.action_chatMessagesFragment_to_meeraProfilePhotoViewerFragment,
            bundle = bundleOf(
                IArgContainer.ARG_IMAGE_URL to url,
                IArgContainer.ARG_GALLERY_ORIGIN to DestinationOriginEnum.CHAT
            )
        )
    }

    fun gotoSearchMainFragment() {
        navController.safeNavigate(
            resId = R.id.action_chatMessagesFragment_to_searchNavGraph,
            bundle = bundleOf(
                IArgContainer.ARG_FIND_FRIENDS_OPENED_FROM_WHERE to AmplitudeFindFriendsWhereProperty.SHARE
            )
        )
    }

    fun openUserComplaintsFragment(
        complainType: ComplainType,
        userId: Long,
        roomId: Long?,
        sendResult: Boolean,
        where: AmplitudePropertyWhere
    ) {
        navController.safeNavigate(
            resId = R.id.action_chatMessagesFragment_to_userComplaintDetailsFragment,
            bundle = Bundle().apply {
                putInt(KEY_COMPLAIN_TYPE, complainType.key)
                putLong(KEY_COMPLAINT_USER_ID, userId)
                roomId?.let { putLong(KEY_COMPLAINT_ROOM_ID, roomId) }
                putBoolean(KEY_COMPLAINT_SEND_RESULT, sendResult)
                putSerializable(KEY_COMPLAINT_WHERE_VALUE, where)
            }
        )
    }
}
