package com.numplates.nomera3.modules.redesign.deeplink

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.meera.core.di.scopes.AppScope
import com.meera.core.extensions.orFalse
import com.meera.core.extensions.safeNavigate
import com.meera.core.utils.MeeraNotificationController
import com.meera.db.models.dialog.UserChat
import com.meera.uikit.widgets.navigation.BottomType
import com.meera.uikit.widgets.navigation.NavigationBarActions
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitType
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.moments.show.data.ARG_MOMENT_ID
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_MOMENT_COMMENT_ID
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_MOMENT_TARGET_ID
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_USER_ID
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.util.NavTabItem
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.modules.redesign.util.NavigationUiSetter
import com.numplates.nomera3.modules.redesign.util.isHiddenState
import com.numplates.nomera3.modules.redesign.util.needAuthToNavigate
import com.numplates.nomera3.presentation.router.IActionContainer
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_COMMENT_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_COMMENT_LAST_REACTION
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FEED_POST_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GROUP_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_IS_FROM_PUSH
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_MOMENT_AUTHOR_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_POST_ORIGIN
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_ROOM_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_MODEL
import com.numplates.nomera3.presentation.view.fragments.MeeraFriendsHostFragment
import timber.log.Timber
import javax.inject.Inject

@AppScope
class DeeplinkNavigation @Inject constructor() {

    private val nm by lazy { NavigationManager.getManager() }

    // Метод предназначен для проверки возможности перехода по диплинку, взависимости от текущего местонахождения
    private fun NavigationManager.couldTransition(): Boolean {
        if (!MeeraNotificationController.shouldShowNotification.get()) return false
        return topNavController.currentDestination?.id != R.id.meeraViewMomentFragment &&
            topNavController.currentDestination?.id != R.id.meeraCreatePostFragment
    }

    fun handleTransition(act: MeeraAct, action: MeeraDeeplinkAction) = with(nm.topNavController) {
        if (nm.couldTransition().not()) return@with

        when (action) {
            is MeeraDeeplinkAction.OpenSpecificMomentAction -> {
                safeNavigate(
                    R.id.meeraViewMomentFragment,
                    bundleOf(ARG_MOMENT_ID to action.momentId)
                )
            }

            // По тапу на кнопку открывается экран создания поста (в главной дороге)
            MeeraDeeplinkAction.CreateNewPostAction -> {
                needAuthToNavigate(nm.act) {
                    openRoadTab()
                    safeNavigate(
                        resId = R.id.meeraCreatePostFragment, bundle = bundleOf(
                            IArgContainer.ARG_SHOW_MEDIA_GALLERY to false
                        )
                    )
                }
            }

            // Личная дорога
            MeeraDeeplinkAction.CreateNewPostPersonalAction -> {
                needAuthToNavigate(nm.act) {
                    openRoadTab()
                    safeNavigate(
                        resId = R.id.meeraCreatePostFragment, bundle = bundleOf(
                            IArgContainer.ARG_SHOW_MEDIA_GALLERY to false
                        )
                    )
                }
            }

            // По тапу на кнопку открывается вкладка “Мой профиль”
            MeeraDeeplinkAction.GoOwnProfileTabAction -> {
                needAuthToNavigate(nm.act) {
                    openServiceTab()
                    safeNavigate(R.id.userInfoFragment)
                }
            }

            // По тапу на кнопку открывается вкладка “Чаты”
            MeeraDeeplinkAction.OpenChatsAction -> needAuthToNavigate(nm.act) {
                openChatTab()
            }

            // По тапу на кнопку открывается вкладка “Карта” (Шторка из Дороги)
            MeeraDeeplinkAction.OpenMapAction -> openMapTab()

            // По тапу на кнопку открывается экран “Сообщества”,  вкладка “Мои сообщества”
            MeeraDeeplinkAction.OpenMyCommunityAction -> {
                needAuthToNavigate(nm.act) {
                    openServiceTab()
                    safeNavigate(R.id.action_servicesFragment_to_meeraCommunitiesListsContainerFragment)
                }
            }

            // По тапу на кнопку открывается экран “Уведомления” в “Общение”
            MeeraDeeplinkAction.OpenNotifications -> {
                needAuthToNavigate(nm.act) {
                    nm.initGraph(R.navigation.bottom_notifications_graph)
                }
            }

            // По тапу на кнопку выполняется переход в n-сообщество
            is MeeraDeeplinkAction.OpenSpecificCommunityAction -> {
                needAuthToNavigate(nm.act) {
                    openRoadTab()
                    safeNavigate(
                        resId = R.id.meeraCommunityRoadFragmentMainFlow,
                        bundle = bundleOf(IArgContainer.ARG_GROUP_ID to action.communityId.toInt())
                    )
                }
            }

            // По тапу на кнопку выполняется переход в n-пост
            is MeeraDeeplinkAction.OpenSpecificPostAction -> {
                needAuthToNavigate(nm.act) {
                    openRoadTab()
                    safeNavigate(
                        resId = R.id.meeraPostFragmentV2, bundle = bundleOf(ARG_FEED_POST_ID to action.postId)
                    )
                }
            }

            // По тапу на кнопку открывается экран “Настройки” у своего профиля (= кнопка “Шестерёнка“ в своём профиле)
            MeeraDeeplinkAction.OpenUserSettingsAction -> {
                needAuthToNavigate(nm.act) {
                    openServiceTab()
                    safeNavigate(
                        R.id.meeraProfileSettingsFragment, bundleOf(IArgContainer.ARG_CALLED_FROM_PROFILE to true)
                    )
                }
            }

            // По тапу на кнопку открывается экран “Приватность и безопасность” в “Настройки”
            MeeraDeeplinkAction.PrivacyAction -> {
                needAuthToNavigate(nm.act) {
                    openServiceTab()
                    safeNavigate(R.id.meeraPrivacyFragment)
                }
            }

            // По тапу на кнопку открывается экран настроек “Профиль” (= кнопка “Профиль“ на экране “Настройки“ у своего профиля)
            MeeraDeeplinkAction.ProfileEditAction -> {
                needAuthToNavigate(nm.act) {
                    openServiceTab()
                    safeNavigate(
                        R.id.meeraUserPersonalInfoFragment, bundleOf(IArgContainer.ARG_CALLED_FROM_PROFILE to true)
                    )
                }
            }

            // По тапу на кнопку открывается вкладка “Люди” в Поиске
            MeeraDeeplinkAction.SearchUserAction -> {
                needAuthToNavigate(nm.act) {
                    safeNavigate(R.id.searchNavGraph)
                }
            }

            // По тапу на кнопку открывается экран “Уведомления” в “Настройки”
            MeeraDeeplinkAction.UserNotificationSettingsAction -> {
                needAuthToNavigate(nm.act) {
                    openServiceTab()
                    safeNavigate(R.id.meeraPushNotificationSettingsFragment)
                }
            }

            // По тапу на кнопку открывается экран “Пригласить друзей” в “Настройки” у своего профиля
            MeeraDeeplinkAction.UserReferralAction -> TODO()

            // По тапу на кнопку выполняется переход в n-профиль
            is MeeraDeeplinkAction.OpenSpecificUserAction -> {
                needAuthToNavigate(nm.act) {
                    safeNavigate(
                        resId = R.id.userInfoFragment,
                        bundle = bundleOf(ARG_USER_ID to action.userId)
                    )
                }
            }

            // По тапу переход в раздел “Люди”
            MeeraDeeplinkAction.OpenPeopleAction -> {
                needAuthToNavigate(nm.act) {
                    safeNavigate(R.id.peoplesFragment)
                }
            }

            // По тапу на кнопку открывается свой профиль - экран “Обо мне”
            MeeraDeeplinkAction.OpenAboutMeAction -> {
                needAuthToNavigate(nm.act) {
                    openServiceTab()
                    safeNavigate(R.id.meeraGridProfilePhotoFragment)
                }
            }

            // Обертка для переходов из пушей
            is MeeraDeeplinkAction.PushWrapper -> handlePushAction(act, action.intent)

            // Переход в чат с юзером, диплинк не был описан в доке
            is MeeraDeeplinkAction.OpenSpecificChatAction -> {
                needAuthToNavigate(nm.act) {
                    safeNavigate(
                        R.id.meeraChatFragment, bundle = bundleOf(
                            IArgContainer.ARG_CHAT_INIT_DATA to ChatInitData(
                                initType = ChatInitType.FROM_PROFILE, userId = action.userId
                            )
                        )
                    )
                }
            }

            // Отсутсвие события, необходимо для обработки ошибок
            MeeraDeeplinkAction.None -> {
                Timber.e("Couldn't parse URI of deeplink")
            }
        }
    }

    private fun handlePushAction(act: MeeraAct, intent: Intent?) = with(nm.topNavController) {
        when (intent?.action) {
            // TODO: Окрытие главного экрана, протестировать и убрать при необходимости
            Intent.ACTION_MAIN -> {}

            // TODO: Обработка диплинков из оснвного приложения, маловеротяный сценарий,
            //  необходимо тщательно протестировать и убрать и при необходимости
            Intent.ACTION_VIEW -> {
                val action = MeeraDeeplink.getAction(intent.data.toString()) ?: return@with
//                handleTransition(action)
            }

            IActionContainer.ACTION_OPEN_MOMENT -> {
                val momentItemId = intent.extras?.getLong(ARG_MOMENT_ID) ?: return
                val momentAuthorId = intent.extras?.getLong(ARG_MOMENT_AUTHOR_ID)
                val momentCommentId = intent.extras?.getLong(ARG_COMMENT_ID, -1L)

                safeNavigate(
                    R.id.meeraViewMomentFragment,
                    bundleOf(
                        KEY_USER_ID to momentAuthorId,
                        KEY_MOMENT_TARGET_ID to momentItemId,
                        KEY_MOMENT_COMMENT_ID to momentCommentId
                    )
                )
            }

            IActionContainer.ACTION_START_CALL -> {
                Timber.e("Fetisov DeeplinkNavigation IActionContainer.ACTION_START_CALL")
                act.provideCallDelegate()?.actionStartCallFromPush(intent.extras)
            }

            IActionContainer.ACTION_OPEN_CHAT -> {
                val extras = intent.extras
                val roomId = extras?.getLong(ARG_ROOM_ID) ?: return
                needAuthToNavigate(nm.act) {
                    safeNavigate(
                        R.id.meeraChatFragment, bundle = bundleOf(
                            IArgContainer.ARG_CHAT_INIT_DATA to ChatInitData(
                                initType = ChatInitType.FROM_LIST_ROOMS,
                                roomId = roomId,
                            )
                        )
                    )
                }
            }

            // TODO: Необходимо убрать, так как в данном действии нет необходимости
            IActionContainer.ACTION_OPEN_AUTHORIZATION -> {}

            IActionContainer.ACTION_FRIEND_REQUEST -> {
                safeNavigate(
                    R.id.meeraFriendsHostFragment,
                    bundle = bundleOf(
                        IArgContainer.ARG_IS_GOTO_INCOMING to true,
                        IArgContainer.ARG_TYPE_FOLLOWING to MeeraFriendsHostFragment.SelectedPage.INCOMING_REQUESTS
                    )
                )
            }

            IActionContainer.ACTION_FRIEND_CONFIRM -> {
                safeNavigate(
                    R.id.meeraFriendsHostFragment,
                    bundleOf(
                        ARG_USER_ID to intent.extras?.getUser()?.userId,
                        ARG_IS_FROM_PUSH to true
                    )
                )
            }

            // TODO: Выпилить action из всех мест приложения, экрана подарков больше нет
            IActionContainer.ACTION_OPEN_GIFTS -> {}

            IActionContainer.ACTION_LEAVE_POST_COMMENTS -> {
                val postId = intent.extras?.getLong(ARG_FEED_POST_ID)
                val commentId = intent.extras?.getLong(ARG_COMMENT_ID)

                safeNavigate(
                    R.id.meeraPostFragmentV2, bundleOf(
                        ARG_FEED_POST_ID to postId,
                        ARG_COMMENT_ID to commentId,
                        ARG_POST_ORIGIN to DestinationOriginEnum.PUSH
                    )
                )
            }

            IActionContainer.ACTION_LEAVE_POST_COMMENT_REACTIONS -> {
                val extras = intent.extras
                val postId = extras?.getLong(ARG_FEED_POST_ID)
                val commentId = extras?.getLong(ARG_COMMENT_ID)
                val lastReaction = extras?.getSerializable(ARG_COMMENT_LAST_REACTION) as? ReactionType

                safeNavigate(
                    R.id.meeraPostFragmentV2, bundleOf(
                        ARG_FEED_POST_ID to postId,
                        ARG_COMMENT_ID to commentId,
                        ARG_COMMENT_LAST_REACTION to lastReaction,
                        ARG_POST_ORIGIN to DestinationOriginEnum.PUSH
                    )
                )
            }

            IActionContainer.ACTION_REPLY_POST_COMMENTS -> {
                val postId = intent.extras?.getLong(ARG_FEED_POST_ID)
                val commentId = intent.extras?.getLong(ARG_COMMENT_ID)

                safeNavigate(
                    R.id.meeraPostFragmentV2, bundleOf(
                        ARG_FEED_POST_ID to postId,
                        ARG_COMMENT_ID to commentId,
                        ARG_POST_ORIGIN to DestinationOriginEnum.PUSH
                    )
                )
            }

            IActionContainer.ACTION_ADD_TO_GROUP_CHAT -> {
                val extras = intent.extras
                val roomId = extras?.getLong(ARG_ROOM_ID) ?: return

                safeNavigate(
                    R.id.meeraChatFragment, bundle = bundleOf(
                        IArgContainer.ARG_CHAT_INIT_DATA to ChatInitData(
                            initType = ChatInitType.FROM_LIST_ROOMS,
                            roomId = roomId
                        )
                    )
                )
            }

            IActionContainer.ACTION_OPEN_PEOPLE -> {
                val userId = intent.extras?.getLong(ARG_USER_ID) ?: -1L
                safeNavigate(R.id.peoplesFragment, bundleOf(ARG_USER_ID to userId))
            }

            IActionContainer.ACTION_REQUEST_TO_GROUP -> {
                val groupId = intent.extras?.getInt(ARG_GROUP_ID)

                safeNavigate(
                    resId = R.id.meeraCommunityMembersContainerFragment,
                    bundle = bundleOf(
                        ARG_GROUP_ID to groupId,
                        ARG_IS_FROM_PUSH to true
                    )
                )
            }

            // TODO: Выпилить данные action так в нем больше нет необходимости
            IActionContainer.ACTION_OPEN_APP -> {}

            IActionContainer.ACTION_OPEN_MAP -> {
                openRoadTab()
            }

            IActionContainer.ACTION_OPEN_POST -> {
                val postId = intent.extras?.getLong(ARG_FEED_POST_ID)
                val commentId = intent.extras?.getLong(ARG_COMMENT_ID)

                safeNavigate(
                    R.id.meeraPostFragmentV2, bundleOf(
                        ARG_FEED_POST_ID to postId,
                        ARG_COMMENT_ID to commentId,
                        ARG_POST_ORIGIN to DestinationOriginEnum.PUSH
                    )
                )
            }

            IActionContainer.ACTION_OPEN_POST_WITH_REACTIONS -> {
                val extras = intent.extras
                val postId = extras?.getLong(ARG_FEED_POST_ID)
                val commentId = extras?.getLong(ARG_COMMENT_ID)
                val lastReaction = extras?.getSerializable(ARG_COMMENT_LAST_REACTION) as? ReactionType

                safeNavigate(
                    R.id.meeraPostFragmentV2, bundleOf(
                        ARG_FEED_POST_ID to postId,
                        ARG_COMMENT_ID to commentId,
                        ARG_COMMENT_LAST_REACTION to lastReaction,
                        ARG_POST_ORIGIN to DestinationOriginEnum.PUSH
                    )
                )
            }

            IActionContainer.ACTION_OPEN_GALLERY_WITH_REACTIONS -> {
                val postId = intent.extras?.getLong(ARG_FEED_POST_ID, 0L)
                val isProfilePhoto = intent.extras?.getBoolean(IArgContainer.ARG_IS_PROFILE_PHOTO, false)
                val isOwnProfile = intent.extras?.getBoolean(IArgContainer.ARG_IS_OWN_PROFILE, false)
                val isGallery = intent.extras?.getBoolean(IArgContainer.ARG_GALLERY_ORIGIN, false)

                safeNavigate(
                    R.id.meeraProfilePhotoViewerFragment,
                    bundle = bundleOf(
                        IArgContainer.ARG_IS_PROFILE_PHOTO to isProfilePhoto,
                        IArgContainer.ARG_IS_OWN_PROFILE to isOwnProfile,
                        IArgContainer.ARG_POST_ID to postId,
                        IArgContainer.ARG_GALLERY_ORIGIN to isGallery
                    )
                )
            }

            // TODO: Реализовать позже, так как присуствуют сложнол при открытии ивентов на экране карты
            IActionContainer.ACTION_OPEN_EVENT -> {}

            IActionContainer.ACTION_OPEN_OWN_PROFILE -> {
                openServiceTab()
                safeNavigate(R.id.userInfoFragment)
            }

            IActionContainer.ACTION_OPEN_OWN_CHAT_LIST -> {
                openChatTab()
            }

            // TODO: Проверить необходимость данног функционала
            IActionContainer.ACTION_SYSTEM_EVENT -> {}

            IActionContainer.ACTION_OPEN_SELF_BIRTHDAY -> {
                act.showFromActionBirthDay()
            }

            // TODO: В текущий момент ивенты не готовы для перехода из пушей
            IActionContainer.ACTION_OPEN_EVENT_ON_MAP -> {}

            // TODO: В текущий момент не реализован функционал по переходам в звонки
            IActionContainer.ACTION_CALL_UNAVAILABLE -> {}
        }
    }

// TAB //

    private fun openRoadTab() {
        openTab(false, NavTabItem.ROAD_TAB_ITEM, BottomType.Map)
    }

    private fun openMapTab() {
        openTab(true, NavTabItem.MAP_TAB_ITEM, BottomType.Peoples)
    }

    private fun openChatTab() {
        openTab(false, NavTabItem.CHAT_TAB_ITEM, BottomType.Messenger)
    }

    private fun openServiceTab() {
        if (nm.getTopBehaviour()?.isHiddenState().orFalse()) {
            nm.getTopBehaviour()?.state = BottomSheetBehavior.STATE_EXPANDED
        }

        openTab(false, NavTabItem.SERVICE_TAB_ITEM, BottomType.Profile)
    }

    private fun openTab(mapMode: Boolean, navItem: NavTabItem, type: BottomType) {
        rollBackToTabFragments()
        nm.isMapMode = mapMode

        NavigationUiSetter.onNavDesSelectedNew(
            navItem.itemNav, nm.topNavController
        )

        nm.toolbarAndBottomInteraction.getNavigationView().setNavigationBarAction(NavigationBarActions.SelectItem(type))
    }

// --- //

    @SuppressLint("RestrictedApi")
    private fun rollBackToTabFragments() {
        val position = nm.topNavController.currentBackStack.value.size

        val findResultTab = nm.topNavController.currentBackStack.value.withIndex()
            .last { destination -> setOfIdsTabs.any { it == destination.value.destination.id } }

        if (findResultTab.index < position) {
            nm.topNavController.popBackStack(findResultTab.value.destination.id, false)
        }
    }

    private val setOfIdsTabs = setOf(
        R.id.mainChatFragment, R.id.emptyMapFragment, R.id.mainRoadFragment, R.id.servicesFragment
    )

    private fun Bundle?.getUser() =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) this@getUser?.getParcelable(ARG_USER_MODEL)
        else this@getUser?.getParcelable(ARG_USER_MODEL, UserChat::class.java)

}
