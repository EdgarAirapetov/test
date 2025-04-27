package com.numplates.nomera3.modules.userprofile.ui.navigation

import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.meera.core.base.BaseFragment
import com.meera.core.utils.checkAppRedesigned
import com.numplates.nomera3.Act
import com.numplates.nomera3.modules.avatar.ContainerAvatarFragment
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyChatCreatedFromWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.chat.ChatFragmentNew
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitType
import com.numplates.nomera3.modules.communities.ui.fragment.CommunityEditFragment
import com.numplates.nomera3.modules.communities.ui.fragment.CommunityRoadFragment
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.maps.ui.entity.MapVisibilitySettingsOrigin
import com.numplates.nomera3.modules.maps.ui.model.MapUserUiModel
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentClickOrigin
import com.numplates.nomera3.modules.peoples.ui.fragments.PeoplesFragment
import com.numplates.nomera3.modules.purchase.ui.gift.GiftsListFragmentNew
import com.numplates.nomera3.modules.purchase.ui.model.GiftItemUiModel
import com.numplates.nomera3.modules.purchase.ui.send.SendGiftFragment
import com.numplates.nomera3.modules.purchase.ui.vip.FragmentUpgradeToVipNew
import com.numplates.nomera3.modules.purchase.ui.vip.UpdateStatusFragment
import com.numplates.nomera3.modules.search.ui.fragment.SearchMainFragment
import com.numplates.nomera3.modules.uploadpost.ui.AddMultipleMediaPostFragment
import com.numplates.nomera3.modules.user.ui.event.UserProfileNavigation
import com.numplates.nomera3.presentation.model.VipStatus
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_CHAT_INIT_DATA
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_SHOW_SWITCHER
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_SHOW_SYNC_CONTACTS_WELCOME
import com.numplates.nomera3.presentation.view.fragments.FriendsHostFragmentNew
import com.numplates.nomera3.presentation.view.fragments.FriendsHostOpenedType
import com.numplates.nomera3.presentation.view.fragments.MapFragment
import com.numplates.nomera3.presentation.view.fragments.SubscribersListFragment
import com.numplates.nomera3.presentation.view.fragments.SubscriptionsListFragment
import com.numplates.nomera3.presentation.view.fragments.UserGiftsFragment
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment
import com.numplates.nomera3.presentation.view.fragments.UserPersonalInfoFragment
import com.numplates.nomera3.presentation.view.fragments.VehicleInfoFragment
import com.numplates.nomera3.presentation.view.fragments.VehicleListFragmentNew
import com.numplates.nomera3.presentation.view.fragments.VehicleSelectTypeFragment
import com.numplates.nomera3.presentation.view.fragments.privacysettings.MapSettingsFragment
import com.numplates.nomera3.presentation.view.fragments.privacysettings.MeeraMapSettingsFragment
import com.numplates.nomera3.presentation.view.fragments.profilephoto.GridProfilePhotoFragment
import com.numplates.nomera3.presentation.view.fragments.profilephoto.ProfilePhotoViewerFragment

class UserInfoNavigator(
    private val getFM: (() -> FragmentManager),
    private val openFragment: (BaseFragment, statusBar: Int, args: Array<Arg<*, *>>) -> Unit,
    private val openMoments: (
        existUserId: Long,
        openedFrom: MomentClickOrigin,
        hasNewMoments: Boolean?,
    ) -> Unit,
) {

    fun openContainerAvatarFragment(avatarState: String?) =
        openFragment(
            ContainerAvatarFragment(),
            Act.LIGHT_STATUSBAR,
            arrayOf(Arg(IArgContainer.ARG_AVATAR_STATE, avatarState))
        )

    fun openSearchFragment(searchPage: Int = SearchMainFragment.PAGE_SEARCH_PEOPLE) =
        openFragment(
            SearchMainFragment(),
            Act.LIGHT_STATUSBAR,
            arrayOf(Arg(IArgContainer.ARG_SEARCH_OPEN_PAGE, searchPage))
        )

    fun openUpdateStatusFragment() =
        openFragment(UpdateStatusFragment(), Act.LIGHT_STATUSBAR, arrayOf())

    fun openUpgradeToVipFragment() =
        openFragment(FragmentUpgradeToVipNew(), Act.LIGHT_STATUSBAR, arrayOf())

    private fun openGiftsListFragment(
        userId: Long?,
        userName: String?,
        dateOfBirth: Long?,
        where: AmplitudePropertyWhere,
        scrollToBottom: Boolean = false
    ) = checkAppRedesigned(
        isRedesigned = {
//            openFragment(
//                MeeraGiftsListFragment(),
//                Act.LIGHT_STATUSBAR_NOT_TRANSPARENT, arrayOf(
//                    Arg(IArgContainer.ARG_USER_ID, userId),
//                    Arg(IArgContainer.ARG_USER_NAME, userName),
//                    Arg(IArgContainer.ARG_USER_DATE_OF_BIRTH, dateOfBirth),
//                    Arg(IArgContainer.ARG_GIFT_SEND_WHERE, where),
//                    Arg(IArgContainer.ARG_SCROLL_TO_BOTTOM, scrollToBottom)
//                )
//            )
        },
        isNotRedesigned = {
            openFragment(
                GiftsListFragmentNew(),
                Act.LIGHT_STATUSBAR, arrayOf(
                    Arg(IArgContainer.ARG_USER_ID, userId),
                    Arg(IArgContainer.ARG_USER_NAME, userName),
                    Arg(IArgContainer.ARG_USER_DATE_OF_BIRTH, dateOfBirth),
                    Arg(IArgContainer.ARG_GIFT_SEND_WHERE, where),
                    Arg(IArgContainer.ARG_SCROLL_TO_BOTTOM, scrollToBottom)
                )
            )
        }
    )

    fun openPhotoViewer(
        image: String,
        isProfilePhoto: Boolean,
        isOwnProfile: Boolean,
        isAnimatedAvatar: Boolean,
        animatedAvatar: String?,
        userName: String?,
        userId: Long,
        position: Int
    ) {
        val origin = if (isOwnProfile) {
            DestinationOriginEnum.OWN_PROFILE
        } else {
            DestinationOriginEnum.OTHER_PROFILE
        }
        checkAppRedesigned(
            isRedesigned = {
//                openFragment(
//                    MeeraProfilePhotoViewerFragment(), Act.COLOR_STATUSBAR_BLACK_NAVBAR, arrayOf(
//                        Arg(IArgContainer.ARG_IMAGE_URL, image),
//                        Arg(IArgContainer.ARG_IS_PROFILE_PHOTO, isProfilePhoto),
//                        Arg(IArgContainer.ARG_IS_OWN_PROFILE, isOwnProfile),
//                        Arg(IArgContainer.ARG_IS_ANIMATED_AVATAR, isAnimatedAvatar),
//                        Arg(IArgContainer.ARG_ANIMATED_AVATAR, animatedAvatar),
//                        Arg(IArgContainer.ARG_USER_ID, userId),
//                        Arg(IArgContainer.ARG_USER_NAME, userName),
//                        Arg(IArgContainer.ARG_GALLERY_ORIGIN, origin),
//                        Arg(IArgContainer.ARG_GALLERY_POSITION, position),
//                    )
//                )
            },
            isNotRedesigned = {
                openFragment(
                    ProfilePhotoViewerFragment(), Act.COLOR_STATUSBAR_BLACK_NAVBAR, arrayOf(
                        Arg(IArgContainer.ARG_IMAGE_URL, image),
                        Arg(IArgContainer.ARG_IS_PROFILE_PHOTO, isProfilePhoto),
                        Arg(IArgContainer.ARG_IS_OWN_PROFILE, isOwnProfile),
                        Arg(IArgContainer.ARG_IS_ANIMATED_AVATAR, isAnimatedAvatar),
                        Arg(IArgContainer.ARG_ANIMATED_AVATAR, animatedAvatar),
                        Arg(IArgContainer.ARG_USER_ID, userId),
                        Arg(IArgContainer.ARG_USER_NAME, userName),
                        Arg(IArgContainer.ARG_GALLERY_ORIGIN, origin),
                        Arg(IArgContainer.ARG_GALLERY_POSITION, position),
                    )
                )
            }
        )
    }

    private fun openMapPinInCenter(mapUser: MapUserUiModel) =
        openFragment(
            MapFragment(), Act.LIGHT_STATUSBAR, arrayOf(
                Arg(IArgContainer.ARG_USER_MODEL, mapUser)
            )
        )

    fun openFriendsHostFragment(
        userId: Long,
        name: String? = null,
        actionType: FriendsHostFragmentNew.SelectedPage? = null
    ) {
        checkAppRedesigned(
            isRedesigned = {
//                openFragment(
//                    MeeraFriendsHostFragment(), Act.LIGHT_STATUSBAR, arrayOf(
//                        Arg(IArgContainer.ARG_USER_ID, userId),
//                        Arg(IArgContainer.ARG_TYPE_FOLLOWING, actionType),
//                        Arg(IArgContainer.ARG_USER_NAME, name),
//                        Arg(
//                            IArgContainer.ARG_FRIENDS_HOST_OPENED_FROM,
//                            FriendsHostOpenedType.FRIENDS_HOST_OPENED_FROM_PROFILE
//                        )
//                    )
//                )
            },
            isNotRedesigned = {
                openFragment(
                    FriendsHostFragmentNew(), Act.LIGHT_STATUSBAR, arrayOf(
                        Arg(IArgContainer.ARG_USER_ID, userId),
                        Arg(IArgContainer.ARG_TYPE_FOLLOWING, actionType),
                        Arg(IArgContainer.ARG_USER_NAME, name),
                        Arg(
                            IArgContainer.ARG_FRIENDS_HOST_OPENED_FROM,
                            FriendsHostOpenedType.FRIENDS_HOST_OPENED_FROM_PROFILE
                        )
                    )
                )
            }
        )

    }

    private fun openSubscribersListFragment() {
        checkAppRedesigned(
            isRedesigned = {
//                openFragment(
//                    MeeraFriendsHostFragment(), Act.LIGHT_STATUSBAR, arrayOf(
//                        Arg(ARG_FRIEND_LIST_MODE, SUBSCRIBERS)
//                    )
//                )
            },
            isNotRedesigned = {
                openFragment(SubscribersListFragment(), Act.LIGHT_STATUSBAR, arrayOf())
            }
        )
    }

    private fun openSubscriptionsListFragment() {
        checkAppRedesigned(
            isRedesigned = {
//                openFragment(
//                    MeeraFriendsHostFragment(), Act.LIGHT_STATUSBAR, arrayOf(
//                        Arg(ARG_FRIEND_LIST_MODE, SUBSCRIPTIONS)
//                    )
//                )
            },
            isNotRedesigned = {
                openFragment(SubscriptionsListFragment(), Act.LIGHT_STATUSBAR, arrayOf())
            }
        )
    }

    private fun openGridProfilePhotoFragment(userId: Long, photoCount: Int) =
        openFragment(
            GridProfilePhotoFragment(), Act.LIGHT_STATUSBAR, arrayOf(
                Arg(IArgContainer.ARG_USER_ID, userId),
                Arg(IArgContainer.ARG_GALLERY_IMAGES_COUNT, photoCount)
            )
        )

    private fun openCommunityEditFragment() =
        openFragment(
            CommunityEditFragment(),
            Act.LIGHT_STATUSBAR,
            arrayOf(Arg(IArgContainer.ARG_IS_GROUP_CREATOR, true))
        )

//    private fun openComplainFragment(userId: Long, where: AmplitudePropertyWhere) {
//        return openFragment(
//            MeeraUserComplaintDetailsFragment(),
//            Act.LIGHT_STATUSBAR,
//            arrayOf(Arg(KEY_COMPLAINT_USER_ID, userId), Arg(KEY_COMPLAINT_WHERE_VALUE, where))
//        )
//    }

    private fun openCommunityRoadFragment(groupId: Int) {
        checkAppRedesigned(
            isRedesigned = {
//                openFragment(MeeraCommunityRoadFragment(), Act.LIGHT_STATUSBAR, arrayOf(Arg(IArgContainer.ARG_GROUP_ID, groupId)))
            },
            isNotRedesigned = {
                openFragment(CommunityRoadFragment(), Act.LIGHT_STATUSBAR, arrayOf(Arg(IArgContainer.ARG_GROUP_ID, groupId)))
            }
        )
    }


    fun openProfilePhotoViewerFragment(
        isOwnProfile: Boolean,
        position: Int,
        userId: Long
    ) {
        val origin = if (isOwnProfile) {
            DestinationOriginEnum.OWN_PROFILE
        } else {
            DestinationOriginEnum.OTHER_PROFILE
        }
        checkAppRedesigned(
            isRedesigned = {
//                openFragment(
//                    MeeraProfilePhotoViewerFragment(), Act.COLOR_STATUSBAR_BLACK_NAVBAR, arrayOf(
//                        Arg(IArgContainer.ARG_IS_PROFILE_PHOTO, false),
//                        Arg(IArgContainer.ARG_IS_OWN_PROFILE, isOwnProfile),
//                        Arg(IArgContainer.ARG_USER_ID, userId),
//                        Arg(IArgContainer.ARG_GALLERY_POSITION, position),
//                        Arg(IArgContainer.ARG_GALLERY_ORIGIN, origin)
//                    )
//                )
            },
            isNotRedesigned = {
                openFragment(
                    ProfilePhotoViewerFragment(), Act.COLOR_STATUSBAR_BLACK_NAVBAR, arrayOf(
                        Arg(IArgContainer.ARG_IS_PROFILE_PHOTO, false),
                        Arg(IArgContainer.ARG_IS_OWN_PROFILE, isOwnProfile),
                        Arg(IArgContainer.ARG_USER_ID, userId),
                        Arg(IArgContainer.ARG_GALLERY_POSITION, position),
                        Arg(IArgContainer.ARG_GALLERY_ORIGIN, origin)
                    )
                )
            }
        )
    }

    private fun openUserGiftsFragment(
        userId: Long,
        name: String?,
        birthdayFlag: Long,
        where: AmplitudePropertyWhere
    ) = checkAppRedesigned(
        isRedesigned = {
//            openFragment(
//                MeeraUserGiftsFragment(),
//                Act.LIGHT_STATUSBAR_NOT_TRANSPARENT, arrayOf(
//                    Arg(IArgContainer.ARG_USER_ID, userId),
//                    Arg(IArgContainer.ARG_USER_NAME, name ?: ""),
//                    Arg(IArgContainer.ARG_USER_DATE_OF_BIRTH, birthdayFlag),
//                    Arg(IArgContainer.ARG_GIFT_SEND_WHERE, where)
//                )
//            )
        },
        isNotRedesigned = {
            openFragment(
                UserGiftsFragment(),
                Act.LIGHT_STATUSBAR, arrayOf(
                    Arg(IArgContainer.ARG_USER_ID, userId),
                    Arg(IArgContainer.ARG_USER_NAME, name ?: ""),
                    Arg(IArgContainer.ARG_USER_DATE_OF_BIRTH, birthdayFlag),
                    Arg(IArgContainer.ARG_GIFT_SEND_WHERE, where)
                )
            )
        }
    )

    private fun openVehicleSelectTypeFragment() {
        openFragment(VehicleSelectTypeFragment(), Act.LIGHT_STATUSBAR, arrayOf())
    }

    private fun openVehicleInfoFragment(vehicleId: String, userId: Long) =
        openFragment(
            VehicleInfoFragment(), Act.COLOR_STATUSBAR_LIGHT_NAVBAR, arrayOf(
                Arg(IArgContainer.ARG_CAR_ID, vehicleId),
                Arg(IArgContainer.ARG_USER_ID, userId)
            )
        )

    private fun openVehicleListFragmentNew(userId: Long, accountType: Int?, accountColor: Int?) {
        openFragment(
            VehicleListFragmentNew(), Act.LIGHT_STATUSBAR, arrayOf(
                Arg(IArgContainer.ARG_USER_ID, userId),
                Arg(IArgContainer.ARG_USER_VIP_STATUS, VipStatus(accountType, accountColor))
            )
        )
    }

    private fun openAddPostFragmentNew(showMediaGallery: Boolean) =
        openFragment(
            AddMultipleMediaPostFragment(), Act.LIGHT_STATUSBAR, arrayOf(
                Arg(IArgContainer.ARG_SHOW_MEDIA_GALLERY, showMediaGallery),
                Arg(AddMultipleMediaPostFragment.OpenFrom.EXTRA_KEY, AddMultipleMediaPostFragment.OpenFrom.Profile)
            )
        )

    private fun openMapSettingsFragment(
        value: Int?,
        countBlacklist: Int? = null,
        countWhitelist: Int? = null,
    ) {
        checkAppRedesigned(isRedesigned = {
            val args = bundleOf(
                IArgContainer.ARG_PRIVACY_TYPE_VALUE to value,
                IArgContainer.ARG_COUNT_USERS_BLACKLIST to countBlacklist,
                IArgContainer.ARG_COUNT_USERS_WHITELIST to countWhitelist,
                MapVisibilitySettingsOrigin.ARG to MapVisibilitySettingsOrigin.USER_PROFILE
            )
            MeeraMapSettingsFragment.show(fragmentManager = getFM.invoke(), args)
        }, isNotRedesigned = {
            openFragment(
                MapSettingsFragment(), Act.LIGHT_STATUSBAR, arrayOf(
                    Arg(IArgContainer.ARG_PRIVACY_TYPE_VALUE, value),
                    Arg(IArgContainer.ARG_COUNT_USERS_BLACKLIST, countBlacklist),
                    Arg(IArgContainer.ARG_COUNT_USERS_WHITELIST, countWhitelist),
                    Arg(MapVisibilitySettingsOrigin.ARG, MapVisibilitySettingsOrigin.USER_PROFILE)
                )
            )
        })
    }

    private fun openSendGiftFragment(
        gift: GiftItemUiModel,
        userId: Long,
        userName: String?,
        goBackTwice: Boolean,
        biAmplitudeWhere: AmplitudePropertyWhere,
        birth: Long?
    ) = openFragment(
        SendGiftFragment(),
        Act.LIGHT_STATUSBAR, arrayOf(
            Arg(IArgContainer.ARG_USER_NAME, userName),
            Arg(IArgContainer.ARG_USER_ID, userId),
            Arg(IArgContainer.ARG_GIFT_MODEL, gift),
            Arg(IArgContainer.ARG_GO_BACK_TWICE, goBackTwice),
            Arg(IArgContainer.ARG_USER_DATE_OF_BIRTH, birth),
            Arg(IArgContainer.ARG_GIFT_SEND_WHERE, biAmplitudeWhere)
        )
    )

    private fun openUserPersonalInfo() {
        checkAppRedesigned(
            isRedesigned = {},
            isNotRedesigned = {
                openFragment(
                    UserPersonalInfoFragment(),
                    Act.LIGHT_STATUSBAR, arrayOf(
                        Arg(IArgContainer.ARG_CALLED_FROM_PROFILE, true)
                    )
                )
            }
        )
    }

    private fun openChatFragment(
        biAmplitudeWhere: AmplitudePropertyWhere,
        userId: Long,
        biWhereCreated: AmplitudePropertyChatCreatedFromWhere
    ) {
        openFragment(
            ChatFragmentNew(), Act.LIGHT_STATUSBAR, arrayOf(
                Arg(IArgContainer.ARG_WHERE_CHAT_OPEN, biAmplitudeWhere),
                Arg(IArgContainer.ARG_FROM_WHERE_CHAT_CREATED, biWhereCreated),
                Arg(
                    ARG_CHAT_INIT_DATA, ChatInitData(
                        initType = ChatInitType.FROM_PROFILE,
                        userId = userId
                    )
                )
            )
        )
    }

    private fun openPeoplesFragment(
        event: UserProfileNavigation.NavigateToPeopleFragment
    ) = openFragment(
        PeoplesFragment(), Act.LIGHT_STATUSBAR, arrayOf(
            Arg(ARG_SHOW_SWITCHER, event.showSwitcher),
            Arg(ARG_SHOW_SYNC_CONTACTS_WELCOME, event.showSyncContactsWelcome)
        )
    )

    private fun openUserInfoFragment(
        userId: Long
    ) = openFragment(
        UserInfoFragment(), Act.COLOR_STATUSBAR_LIGHT_NAVBAR, arrayOf(
            Arg(IArgContainer.ARG_USER_ID, userId),
            Arg(IArgContainer.ARG_TRANSIT_FROM, AmplitudePropertyWhere.SUGGEST_USER_PROFILE.property)
        )
    )

    fun handleNavigationEvent(event: UserProfileNavigation) {
        when (event) {
            is UserProfileNavigation.GoToGroupTab ->
                openSearchFragment(SearchMainFragment.PAGE_SEARCH_COMMUNITY)

            is UserProfileNavigation.NavigateToPeopleFragment ->
                openPeoplesFragment(event)

            is UserProfileNavigation.OpenPeopleFragment ->
                openPeoplesFragment(
                    UserProfileNavigation.NavigateToPeopleFragment(
                        showSwitcher = false,
                        showSyncContactsWelcome = false
                    )
                )

            is UserProfileNavigation.OpenMoments -> {
                openMoments(
                    event.existUserId,
                    event.openedFrom,
                    event.hasNewMoments
                )
            }

            is UserProfileNavigation.OpenProfile ->
                openUserInfoFragment(event.userId)

            UserProfileNavigation.OpenProfileEdit ->
                openUserPersonalInfo()

            is UserProfileNavigation.OpenUserGiftsFragment -> {
                openUserGiftsFragment(
                    userId = event.user.userId,
                    name = event.user.name,
                    birthdayFlag = event.user.birthdayFlag,
                    where = event.where
                )
            }

            is UserProfileNavigation.OpenUserGiftsListFragment -> {
                openGiftsListFragment(
                    userId = event.user.userId,
                    userName = event.user.name,
                    dateOfBirth = event.user.birthdayFlag,
                    where = event.where,
                    scrollToBottom = event.scrollToBottom
                )
            }

            is UserProfileNavigation.ShowGiftScreen -> openSendGiftFragment(
                userName = event.userName,
                userId = event.userId,
                gift = event.gift,
                goBackTwice = event.goBackTwice,
                birth = event.birth,
                biAmplitudeWhere = event.biAmplitudeWhere
            )

            is UserProfileNavigation.ShowMap ->
                openMapPinInCenter(event.mapUser)

            is UserProfileNavigation.StartDialog ->
                openChatFragment(event.where, event.userId, event.fromWhere)

            is UserProfileNavigation.NavigateToPostFragment ->
                openAddPostFragmentNew(event.showMediaGallery)

            UserProfileNavigation.OpenAddVehicle -> openVehicleSelectTypeFragment()
            is UserProfileNavigation.OpenVehicle -> openVehicleInfoFragment(
                vehicleId = event.vehicleId,
                userId = event.userId
            )

            is UserProfileNavigation.OpenVehicleList -> openVehicleListFragmentNew(
                userId = event.userId,
                accountType = event.accountType,
                accountColor = event.accountColor
            )

            is UserProfileNavigation.OpenMapSettingsFragment -> openMapSettingsFragment(
                value = event.settingValue,
                countWhitelist = event.countWhitelist,
                countBlacklist = event.countBlacklist
            )

            is UserProfileNavigation.OpenCommunityFeed ->
                openCommunityRoadFragment(event.communityId)

            is UserProfileNavigation.OpenCommunityEditCreate ->
                openCommunityEditFragment()

            UserProfileNavigation.OpenSubscribers -> openSubscribersListFragment()
            UserProfileNavigation.OpenSubscriptions -> openSubscriptionsListFragment()
            is UserProfileNavigation.OpenGridProfile -> openGridProfilePhotoFragment(
                userId = event.userId,
                photoCount = event.photoCount
            )

            is UserProfileNavigation.OpenProfilePhotoViewer -> openProfilePhotoViewerFragment(
                isOwnProfile = event.isMe,
                position = event.position,
                userId = event.userId
            )

            is UserProfileNavigation.OpenComplainFragment -> Unit
//            openComplainFragment(
//                userId = event.userId,
//                where = event.where
//            )

            else -> Unit
        }
    }

}
