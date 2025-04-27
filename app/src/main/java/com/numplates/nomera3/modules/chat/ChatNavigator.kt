package com.numplates.nomera3.modules.chat

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.meera.core.base.OnActivityInteractionCallback
import com.meera.core.common.COLOR_STATUSBAR_BLACK_NAVBAR
import com.meera.core.common.COLOR_STATUSBAR_LIGHT_NAVBAR
import com.meera.core.common.LIGHT_STATUSBAR
import com.meera.core.extensions.safeNavigate
import com.meera.core.utils.IS_APP_REDESIGNED
import com.meera.core.utils.checkAppRedesigned
import com.meera.db.models.dialog.UserChat
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.checkAppUseJetpackNavigation
import com.numplates.nomera3.data.newmessenger.ROOM_TYPE_DIALOG
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.findfriends.AmplitudeFindFriendsWhereProperty
import com.numplates.nomera3.modules.comments.ui.fragment.PostFragmentV2
import com.numplates.nomera3.modules.communities.ui.fragment.CommunityRoadFragment
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAINT_ROOM_ID
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAINT_SEND_RESULT
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAINT_USER_ID
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAINT_WHERE_VALUE
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAIN_TYPE
import com.numplates.nomera3.modules.complains.ui.reason.ComplainType
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.hashtag.ui.fragment.HashtagFragment
import com.numplates.nomera3.modules.purchase.ui.gift.GiftsListFragmentNew
import com.numplates.nomera3.modules.search.ui.fragment.SearchMainFragment
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GALLERY_ORIGIN
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GIFT_SEND_WHERE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GROUP_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_IMAGE_URL
import com.numplates.nomera3.presentation.view.fragments.RoomsContainerFragment
import com.numplates.nomera3.presentation.view.fragments.UserGiftsFragment
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment
import com.numplates.nomera3.presentation.view.fragments.newchat.ChatGroupAboutFragment
import com.numplates.nomera3.presentation.view.fragments.profilephoto.ProfilePhotoViewerFragment

class ChatNavigator(
    private val activityCallback: OnActivityInteractionCallback?,
    private val fragment: Fragment
) {

    private var navController: NavController? = null

    init {
        checkAppUseJetpackNavigation(
            isJetpackNavigation = {
              if (isDisabledJetpackNavigation()) return@checkAppUseJetpackNavigation
              navController = fragment.findNavController()
            }
        )
    }

    fun gotoCommuntyRoadFragment(groupId: Int?){
        activityCallback?.onAddFragment(
            fragment = CommunityRoadFragment(),
            isLightStatusBar = LIGHT_STATUSBAR,
            mapArgs = hashMapOf(ARG_GROUP_ID to groupId)
        )
    }

    fun openChatFragment(event: String){
        checkAppRedesigned(
            isNotRedesigned = {
                activityCallback?.onAddFragment(
                    fragment = ChatFragmentNew(),
                    isLightStatusBar = Act.LIGHT_STATUSBAR,
                    mapArgs = hashMapOf(
                        IArgContainer.ARG_USER_ID to event,
                        IArgContainer.ARG_ROOM_TYPE to ROOM_TYPE_DIALOG
                    )
                )
            },
            isRedesigned = {
//                activityCallback?.onAddFragment(
//                    fragment = MeeraChatFragment(),
//                    isLightStatusBar = Act.LIGHT_STATUSBAR,
//                    mapArgs = hashMapOf(
//                        IArgContainer.ARG_USER_ID to event,
//                        IArgContainer.ARG_ROOM_TYPE to ROOM_TYPE_DIALOG
//                    )
//                )
            }
        )
    }

    /**
     *  NEED TEST FROM DIFFERENT PLACES потому что в некотоорых метсах было вызванно не через активити в через фргмент
     *  3128 строка через фрагмент
     *  3162 через активити
     * */
    fun gotoUserInfoFragment(userId: Long?, where: AmplitudePropertyWhere) {
        activityCallback?.onAddFragment(
            fragment = UserInfoFragment(),
            isLightStatusBar = COLOR_STATUSBAR_LIGHT_NAVBAR,
            mapArgs = hashMapOf(IArgContainer.ARG_USER_ID to userId,
                IArgContainer.ARG_TRANSIT_FROM to where.property)
        )
    }

    fun gotoAboutChatScreen(roomId: Long?){
        if (roomId == null) return
        checkAppRedesigned(
            isRedesigned = {
                if (isDisabledJetpackNavigation()) return@checkAppRedesigned
                navController?.safeNavigate(
                    resId = R.id.action_chatMessagesFragment_to_groupChatAboutFragment,
                    bundle = Bundle().apply {
                        putLong(IArgContainer.ARG_ROOM_ID, roomId)
                    }
                )
            },
            isNotRedesigned = {
                activityCallback?.onAddFragment(
                    fragment = ChatGroupAboutFragment(),
                    isLightStatusBar = LIGHT_STATUSBAR,
                    mapArgs = hashMapOf(IArgContainer.ARG_ROOM_ID to roomId)
                )
            }
        )
    }

    /**
     * Method for receive gif
     * */
    fun gotoUserGiftFragment(myUid: Long, birth: Long) {
        checkAppRedesigned(
            isRedesigned = {
                navController?.safeNavigate(
                    resId = R.id.action_chatMessagesFragment_to_meeraUserGiftsFragment,
                    bundle = bundleOf(
                        IArgContainer.ARG_USER_ID to myUid,
                        IArgContainer.ARG_USER_DATE_OF_BIRTH to birth,
                        ARG_GIFT_SEND_WHERE to AmplitudePropertyWhere.CHAT
                    )
                )
            },
            isNotRedesigned = {
                activityCallback?.onAddFragment(
                    fragment = UserGiftsFragment(),
                    isLightStatusBar = LIGHT_STATUSBAR,
                    mapArgs = hashMapOf(
                        IArgContainer.ARG_USER_ID to myUid,
                        IArgContainer.ARG_USER_DATE_OF_BIRTH to birth,
                        ARG_GIFT_SEND_WHERE to AmplitudePropertyWhere.CHAT
                    )
                )
            }
        )
    }

    /**
     * Method for send gift
     * */
    fun gotoUserGiftFragment(companion: UserChat?) {
        checkAppRedesigned(
            isRedesigned = {
                navController?.safeNavigate(
                    resId = R.id.action_chatMessagesFragment_to_meeraUserGiftsFragment,
                    bundle = bundleOf(
                        IArgContainer.ARG_USER_ID to companion?.userId,
                        IArgContainer.ARG_USER_NAME to companion?.name,
                        IArgContainer.ARG_USER_DATE_OF_BIRTH to companion?.birthDate,
                        ARG_GIFT_SEND_WHERE to AmplitudePropertyWhere.CHAT
                    )
                )
            },
            isNotRedesigned = {
                activityCallback?.onAddFragment(
                    fragment = UserGiftsFragment(),
                    isLightStatusBar = LIGHT_STATUSBAR,
                    mapArgs = hashMapOf(
                        IArgContainer.ARG_USER_ID to companion?.userId,
                        IArgContainer.ARG_USER_NAME to companion?.name,
                        IArgContainer.ARG_USER_DATE_OF_BIRTH to companion?.birthDate,
                        ARG_GIFT_SEND_WHERE to AmplitudePropertyWhere.CHAT
                    )
                )
            }
        )
    }

    fun gotoGiftListFragment(user: UserChat, isSendBack: Boolean = false){
        checkAppRedesigned(
            isRedesigned = {
                user.userId?.let { id ->
                    user.birthDate?.let { date ->
                        navController?.safeNavigate(
                            resId = R.id.action_chatMessagesFragment_to_meeraGiftsListFragment,
                            bundle = Bundle().apply {
                                putLong( IArgContainer.ARG_USER_ID, id)
                                putString(IArgContainer.ARG_USER_NAME, user.name)
                                putLong(IArgContainer.ARG_USER_DATE_OF_BIRTH, date)
                                putBoolean(IArgContainer.ARG_SEND_BACK, isSendBack)
                                putSerializable(ARG_GIFT_SEND_WHERE, AmplitudePropertyWhere.CHAT)
                            }
                        )
                    }
                }
            },
            isNotRedesigned = {
                activityCallback?.onAddFragment(
                    fragment = GiftsListFragmentNew(),
                    isLightStatusBar = LIGHT_STATUSBAR,
                    mapArgs = hashMapOf(
                        IArgContainer.ARG_USER_ID to user.userId,
                        IArgContainer.ARG_USER_NAME to user.name,
                        IArgContainer.ARG_USER_DATE_OF_BIRTH to user.birthDate,
                        IArgContainer.ARG_SEND_BACK to isSendBack,
                        ARG_GIFT_SEND_WHERE to AmplitudePropertyWhere.CHAT
                    )
                )
            }
        )
    }

    fun gotoHashTagFragment(hashtag: String?) {
        activityCallback?.onAddFragment(
            fragment = HashtagFragment(),
            isLightStatusBar = LIGHT_STATUSBAR,
            mapArgs = hashMapOf(IArgContainer.ARG_HASHTAG to hashtag)
        )
    }

    fun gotoPostFragment(postId: Long) {
        activityCallback?.onAddFragment(
            fragment = PostFragmentV2(null),
            isLightStatusBar = LIGHT_STATUSBAR,
            mapArgs = hashMapOf(
                IArgContainer.ARG_FEED_POST_ID to postId,
                IArgContainer.ARG_POST_ORIGIN to DestinationOriginEnum.CHAT
            )
        )
    }

    fun gotoProfilePhotoViewer(url: String) {
        checkAppRedesigned(
            isRedesigned = {
//                activityCallback?.onAddFragment(
//                    fragment = MeeraProfilePhotoViewerFragment(),
//                    isLightStatusBar = COLOR_STATUSBAR_BLACK_NAVBAR,
//                    mapArgs = hashMapOf(ARG_IMAGE_URL to url, ARG_GALLERY_ORIGIN to DestinationOriginEnum.CHAT)
//                )
            },
            isNotRedesigned = {
                activityCallback?.onAddFragment(
                    fragment = ProfilePhotoViewerFragment(),
                    isLightStatusBar = COLOR_STATUSBAR_BLACK_NAVBAR,
                    mapArgs = hashMapOf(ARG_IMAGE_URL to url, ARG_GALLERY_ORIGIN to DestinationOriginEnum.CHAT)
                )
            }
        )
    }

    fun gotoSearchMainFragment() {
        activityCallback?.onAddFragment(
            fragment = SearchMainFragment(),
            isLightStatusBar = LIGHT_STATUSBAR,
            mapArgs = hashMapOf(
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
        if (IS_APP_REDESIGNED) {
            navController?.safeNavigate(
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

    fun openPreviousViewPagerItem() {
        activityCallback?.openPreviousViewPagerItem()
    }

    private fun isDisabledJetpackNavigation() = fragment.parentFragment is RoomsContainerFragment

}
