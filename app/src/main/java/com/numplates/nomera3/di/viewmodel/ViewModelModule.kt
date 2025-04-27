package com.numplates.nomera3.di.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.numplates.nomera3.modules.audioswitch.ui.AudioSwitchViewModel
import com.numplates.nomera3.modules.audioswitch.ui.MeeraAudioSwitchViewModel
import com.numplates.nomera3.modules.bump.ui.viewmodel.ShakeBottomDialogViewModel
import com.numplates.nomera3.modules.bump.ui.viewmodel.ShakeEventViewModel
import com.numplates.nomera3.modules.bump.ui.viewmodel.ShakeFriendRequestsViewModel
import com.numplates.nomera3.modules.chat.ChatViewModel
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.viewmodel.MediakeyboardFavoritesViewModel
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.ui.viewmodel.MeeraPickerViewModel
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.ui.viewmodel.PickerViewModel
import com.numplates.nomera3.modules.chat.mediakeyboard.recents.ui.viewmodel.MediaKeyboardRecentsViewModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.viewmodel.MediaKeyboardStickersViewModel
import com.numplates.nomera3.modules.chat.requests.ui.viewmodel.ChatRequestViewModel
import com.numplates.nomera3.modules.chat.requests.ui.viewmodel.MeeraChatRequestViewModel
import com.numplates.nomera3.modules.chat.ui.MeeraChatViewModel
import com.numplates.nomera3.modules.chatfriendlist.presentation.ChatFriendListViewModel
import com.numplates.nomera3.modules.chatrooms.ui.MeeraRoomsViewModel
import com.numplates.nomera3.modules.comments.bottomsheet.presentation.CommentsBottomSheetViewModel
import com.numplates.nomera3.modules.comments.ui.viewmodel.MeeraPostViewModelV2
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunitiesListsContainerViewModel
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityMembersViewModel
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunitySubscriptionViewModel
import com.numplates.nomera3.modules.complains.ui.UserComplainViewModel
import com.numplates.nomera3.modules.complains.ui.change.ChangeReasonViewModel
import com.numplates.nomera3.modules.complains.ui.details.UserComplaintDetailsViewModel
import com.numplates.nomera3.modules.complains.ui.reason.UserComplainReasonViewModel
import com.numplates.nomera3.modules.contentsharing.ui.ContentSharingViewModel
import com.numplates.nomera3.modules.contentsharing.ui.loader.SharingLoaderViewModel
import com.numplates.nomera3.modules.contentsharing.ui.rooms.SharingRoomsViewModel
import com.numplates.nomera3.modules.feed.ui.viewmodel.FeedViewModel
import com.numplates.nomera3.modules.feed.ui.viewmodel.MeeraFeedViewModel
import com.numplates.nomera3.modules.feedmediaview.ui.viewmodel.ViewMultimediaViewModel
import com.numplates.nomera3.modules.feedviewcontent.presentation.viewmodel.ViewContentViewModel
import com.numplates.nomera3.modules.maps.ui.events.navigation.EventNavigationDialogViewModel
import com.numplates.nomera3.modules.maps.ui.events.participants.list.EventParticipantsListViewModel
import com.numplates.nomera3.modules.maps.ui.events.road_privacy.EventRoadPrivacyDialogViewModel
import com.numplates.nomera3.modules.maps.ui.events.snippet.EventPostPageViewModel
import com.numplates.nomera3.modules.maps.ui.layers.MapLayersDialogViewModel
import com.numplates.nomera3.modules.maps.ui.snippet.UserSnippetViewModel
import com.numplates.nomera3.modules.moments.comments.presentation.MomentCommentsBottomSheetViewModel
import com.numplates.nomera3.modules.moments.settings.hidefrom.presentation.MomentSettingsHideFromAddUserViewModel
import com.numplates.nomera3.modules.moments.settings.hidefrom.presentation.MomentSettingsHideFromViewModel
import com.numplates.nomera3.modules.moments.settings.notshow.presentation.MomentSettingsNotShowAddUserViewModel
import com.numplates.nomera3.modules.moments.settings.notshow.presentation.MomentSettingsNotShowViewModel
import com.numplates.nomera3.modules.moments.settings.presentation.MomentSettingsViewModel
import com.numplates.nomera3.modules.moments.show.presentation.ViewMomentPositionViewModel
import com.numplates.nomera3.modules.moments.show.presentation.ViewMomentViewModel
import com.numplates.nomera3.modules.moments.widgets.EditorWidgetsViewModel
import com.numplates.nomera3.modules.music.ui.viewmodel.AddMusicViewModel
import com.numplates.nomera3.modules.music.ui.viewmodel.MeeraMusicViewModel
import com.numplates.nomera3.modules.newroads.MeeraMainPostRoadsViewModel
import com.numplates.nomera3.modules.notifications.ui.viewmodel.DetailNotificationViewModel
import com.numplates.nomera3.modules.notifications.ui.viewmodel.MeeraDetailNotificationViewModel
import com.numplates.nomera3.modules.notifications.ui.viewmodel.MeeraNotificationViewModel
import com.numplates.nomera3.modules.notifications.ui.viewmodel.NotificationViewModel
import com.numplates.nomera3.modules.notifications.ui.basefragment.BaseNotificationViewModel
import com.numplates.nomera3.modules.onboarding.OnboardingViewModel
import com.numplates.nomera3.modules.peoples.ui.viewmodel.MeeraPeoplesViewModel
import com.numplates.nomera3.modules.peoples.ui.viewmodel.PeopleOnboardingViewModel
import com.numplates.nomera3.modules.peoples.ui.viewmodel.PeoplesViewModel
import com.numplates.nomera3.modules.purchase.ui.gift.GiftsListViewModel
import com.numplates.nomera3.modules.purchase.ui.send.SendGiftViewModel
import com.numplates.nomera3.modules.purchase.ui.vip.UpgradeToVipViewModel
import com.numplates.nomera3.modules.rateus.presentation.PopUpRateUsDialogViewModel
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsPageViewModel
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsViewModel
import com.numplates.nomera3.modules.redesign.fragments.main.MeeraMainChatViewModel
import com.numplates.nomera3.modules.redesign.fragments.main.MeeraMainContainerViewModel
import com.numplates.nomera3.modules.redesign.fragments.main.map.configuration.MeeraMapViewModel
import com.numplates.nomera3.modules.redesign.fragments.main.map.snippet.MeeraEventPostPageViewModel
import com.numplates.nomera3.modules.redesign.fragments.main.map.snippet.MeeraUserSnippetViewModel
import com.numplates.nomera3.modules.registration.ui.country.viewmodel.MeeraRegistrationCountryViewModel
import com.numplates.nomera3.modules.registration.ui.country.viewmodel.RegistrationCountryViewModel
import com.numplates.nomera3.modules.screenshot.ui.viewmodel.ScreenshotPopupViewModel
import com.numplates.nomera3.modules.search.ui.viewmodel.user.SearchUserViewModel
import com.numplates.nomera3.modules.services.ui.viewmodel.MeeraServicesViewModel
import com.numplates.nomera3.modules.upload.ui.viewmodel.UploadStatusViewModel
import com.numplates.nomera3.modules.uploadpost.ui.viewmodel.AddPostViewModel
import com.numplates.nomera3.modules.uploadpost.ui.viewmodel.MeeraCreatePostViewModel
import com.numplates.nomera3.modules.userprofile.ui.fragment.MeeraUserInfoViewModel
import com.numplates.nomera3.modules.userprofile.ui.meerafriends.MyFriendsListViewModel
import com.numplates.nomera3.modules.userprofile.ui.meerafriends.mutual.MutualSubscriptionViewModel
import com.numplates.nomera3.modules.userprofile.ui.meerafriends.subscribers.MeeraSubscribersViewModel
import com.numplates.nomera3.modules.userprofile.ui.meerafriends.subscribtions.MeeraSubscriptionViewModel
import com.numplates.nomera3.modules.viewvideo.presentation.ViewVideoItemViewModel
import com.numplates.nomera3.modules.viewvideo.presentation.ViewVideoViewModel
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.filter.FilterViewModel
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.numbersearch.NumberSearchViewModel
import com.numplates.nomera3.presentation.view.fragments.meerasettings.MeeraRateUsViewModel
import com.numplates.nomera3.presentation.view.fragments.meerasettings.presentatin.pushnotif.PushNotificationsSettingsViewModel
import com.numplates.nomera3.presentation.view.fragments.notificationsettings.message.MessageNotificationsUsersViewModel
import com.numplates.nomera3.presentation.view.fragments.notificationsettings.subscription.SubscriptionsNotificationUsersViewModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.BlacklistSettingsViewModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.CallSettingsBlacklistViewModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.CallSettingsWhitelistViewModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.MapSettingsBlacklistViewModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.MapSettingsWhitelistViewModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.OnlineSettingsBlacklistViewModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.OnlineSettingsWhitelistViewModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.PersonalMessagesBlackListViewModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.PersonalMessagesWhiteListViewModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.RoadSettingsViewModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.ShowFriendsSubscribersPrivacyViewModel
import com.numplates.nomera3.presentation.view.fragments.vehiclebrandmodelselect.MeeraVehicleBrandModelSelectViewModel
import com.numplates.nomera3.presentation.view.fragments.vehicleedit.MeeraVehicleEditViewModel
import com.numplates.nomera3.presentation.viewmodel.CallViewModel
import com.numplates.nomera3.presentation.viewmodel.ChatGroupEditViewModel
import com.numplates.nomera3.presentation.viewmodel.ChatGroupFriendListViewModel
import com.numplates.nomera3.presentation.viewmodel.ChatGroupShowUsersViewModel
import com.numplates.nomera3.presentation.viewmodel.MapViewModel
import com.numplates.nomera3.presentation.viewmodel.MeeraMyFriendListViewModel
import com.numplates.nomera3.presentation.viewmodel.ProfileSettingsViewModel
import com.numplates.nomera3.presentation.viewmodel.RoomsContainerViewModel
import com.numplates.nomera3.presentation.viewmodel.RoomsViewModel
import com.numplates.nomera3.presentation.viewmodel.SubscribersViewModel
import com.numplates.nomera3.presentation.viewmodel.SubscriptionViewModel
import com.numplates.nomera3.presentation.viewmodel.UserFriendActionViewModel
import com.numplates.nomera3.presentation.viewmodel.UserMutualSubscriptionViewModel
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoViewModel
import com.numplates.nomera3.presentation.viewmodel.UserSubscriptionsFriendsInfoViewModel
import com.numplates.nomera3.presentation.viewmodel.profilephoto.ProfilePhotoViewerViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.FriendsHostViewModel
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import kotlin.reflect.KClass

@MapKey
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
internal annotation class ViewModelKey(val value: KClass<out ViewModel>)

@Module
abstract class ViewModelModule {
    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(RoomsViewModel::class)
    abstract fun bindsRoomsViewModel(roomsViewModel: RoomsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BaseNotificationViewModel::class)
    abstract fun bindsBaseNotificationViewModel(aseNotificationViewModel: BaseNotificationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserSnippetViewModel::class)
    abstract fun bindsUserSnippetViewModel(userSnippetViewModel: UserSnippetViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MeeraUserSnippetViewModel::class)
    abstract fun bindsMeeraUserSnippetViewModel(meeraUserSnippetViewModel: MeeraUserSnippetViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AddMusicViewModel::class)
    abstract fun bindAddMusicViewModel(viewModel: AddMusicViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ViewMomentViewModel::class)
    abstract fun bindViewMomentViewModel(viewModel: ViewMomentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ViewMomentPositionViewModel::class)
    abstract fun bindViewMomentPositionViewModel(viewModel: ViewMomentPositionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MomentSettingsViewModel::class)
    abstract fun bindMomentSettingsViewModel(viewModel: MomentSettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MomentSettingsNotShowViewModel::class)
    abstract fun bindMomentSettingsNotShowViewModel(viewModel: MomentSettingsNotShowViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MomentSettingsNotShowAddUserViewModel::class)
    abstract fun bindMomentSettingsNotShowAddUserViewModel(viewModel: MomentSettingsNotShowAddUserViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MomentSettingsHideFromViewModel::class)
    abstract fun bindMomentSettingsHideFromViewModel(viewModel: MomentSettingsHideFromViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MomentSettingsHideFromAddUserViewModel::class)
    abstract fun bindMomentSettingsHideFromAddUserViewModel(viewModel: MomentSettingsHideFromAddUserViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MomentCommentsBottomSheetViewModel::class)
    abstract fun bindMomentCommentsBottomSheetViewModel(viewModel: MomentCommentsBottomSheetViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MeeraMusicViewModel::class)
    abstract fun bindMusicViewModel(viewModel: MeeraMusicViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserComplainReasonViewModel::class)
    abstract fun bindUserComplainReasonViewModel(viewModel: UserComplainReasonViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChangeReasonViewModel::class)
    abstract fun bindChangeReasonViewModel(viewModel: ChangeReasonViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChatViewModel::class)
    abstract fun bindChatViewModel(viewModel: ChatViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MeeraChatViewModel::class)
    abstract fun bindMeeraChatViewModel(viewModel: MeeraChatViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChatRequestViewModel::class)
    abstract fun bindChatRequestViewModel(viewModel: ChatRequestViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MeeraChatRequestViewModel::class)
    abstract fun bindMeeraChatRequestViewModel(viewModel: MeeraChatRequestViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChatGroupShowUsersViewModel::class)
    abstract fun bindChatGroupShowUsersViewModel(viewModel: ChatGroupShowUsersViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PushNotificationsSettingsViewModel::class)
    abstract fun bindPushNotificationsSettingsViewModel(viewModel: PushNotificationsSettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ShowFriendsSubscribersPrivacyViewModel::class)
    abstract fun bindShowFriendsSubscribersPrivacyViewModel(
        viewModel: ShowFriendsSubscribersPrivacyViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserSubscriptionsFriendsInfoViewModel::class)
    abstract fun bindUserSubscriptionsFriendsInfoViewModel(
        viewModel: UserSubscriptionsFriendsInfoViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserMutualSubscriptionViewModel::class)
    abstract fun bindUserMutualSubscriptionViewModel(
        viewModel: UserMutualSubscriptionViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MutualSubscriptionViewModel::class)
    abstract fun bindMutualSubscriptionViewModel(
        viewModel: MutualSubscriptionViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MeeraSubscribersViewModel::class)
    abstract fun bindMeeraSubscribersViewModel(
        viewModel: MeeraSubscribersViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserComplaintDetailsViewModel::class)
    abstract fun bindUserComplainDetailsViewModel(
        viewModel: UserComplaintDetailsViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserComplainViewModel::class)
    abstract fun bindUserComplainViewModel(viewModel: UserComplainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AddPostViewModel::class)
    abstract fun bindUserAddPostViewModel(viewModel: AddPostViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MeeraPostViewModelV2::class)
    abstract fun bindMeeraPostViewModelV2(viewModel: MeeraPostViewModelV2): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FeedViewModel::class)
    abstract fun bindUserFeedViewModel(viewModel: FeedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MeeraFeedViewModel::class)
    abstract fun bindMeeraFeedViewModel(viewModel: MeeraFeedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RegistrationCountryViewModel::class)
    abstract fun bindRegistrationCountryViewModel(
        viewModel: RegistrationCountryViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MeeraVehicleEditViewModel::class)
    abstract fun bindMeeraVehicleEditViewModel(
        viewModel: MeeraVehicleEditViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MeeraRegistrationCountryViewModel::class)
    abstract fun bindMeeraRegistrationCountryViewModel(
        viewModel: MeeraRegistrationCountryViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserPersonalInfoViewModel::class)
    abstract fun bindUserPersonalInfoViewModel(
        viewModel: UserPersonalInfoViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MapViewModel::class)
    abstract fun bindMapViewModel(viewModel: MapViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MeeraMapViewModel::class)
    abstract fun bindMeeraMapViewModel(viewModel: MeeraMapViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChatFriendListViewModel::class)
    abstract fun bindChatFriendListViewModel(viewModel: ChatFriendListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PickerViewModel::class)
    abstract fun bindPickerViewModel(viewModel: PickerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MeeraPickerViewModel::class)
    abstract fun bindMeeraPickerViewModel(viewModel: MeeraPickerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GiftsListViewModel::class)
    abstract fun bindGiftsListViewModel(viewModel: GiftsListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SendGiftViewModel::class)
    abstract fun bindSendGiftViewModel(viewModel: SendGiftViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UpgradeToVipViewModel::class)
    abstract fun bindUpgradeToVipViewModel(viewModel: UpgradeToVipViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PeoplesViewModel::class)
    abstract fun bindPeoplesViewModel(viewModel: PeoplesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OnboardingViewModel::class)
    abstract fun bindOnboardingViewModel(viewModel: OnboardingViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RoomsContainerViewModel::class)
    abstract fun bindRoomsContainerViewModel(viewModel: RoomsContainerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DetailNotificationViewModel::class)
    abstract fun bindDetailNotificationViewModel(viewModel: DetailNotificationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MediakeyboardFavoritesViewModel::class)
    abstract fun bindMediakeyboardFavoritesViewModel(viewModel: MediakeyboardFavoritesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EventRoadPrivacyDialogViewModel::class)
    abstract fun bindEventRoadPrivacyDialogViewModel(viewModel: EventRoadPrivacyDialogViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MapLayersDialogViewModel::class)
    abstract fun bindMapLayersDialogViewModel(viewModel: MapLayersDialogViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AudioSwitchViewModel::class)
    abstract fun bindAudioSwitchViewModel(viewModel: AudioSwitchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MeeraAudioSwitchViewModel::class)
    abstract fun bindMeeraAudioSwitchViewModel(viewModel: MeeraAudioSwitchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UploadStatusViewModel::class)
    abstract fun bindUploadStatusViewModel(viewModel: UploadStatusViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CallViewModel::class)
    abstract fun bindCallViewModel(viewModel: CallViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChatGroupEditViewModel::class)
    abstract fun bindChatGroupEditViewModel(viewModel: ChatGroupEditViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChatGroupFriendListViewModel::class)
    abstract fun bindChatGroupFriendListViewModel(viewModel: ChatGroupFriendListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MediaKeyboardRecentsViewModel::class)
    abstract fun bindMediaKeyboardRecentsViewModel(viewModel: MediaKeyboardRecentsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ViewContentViewModel::class)
    abstract fun bindViewContentViewModel(viewModel: ViewContentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ViewMultimediaViewModel::class)
    abstract fun bindViewMultimediaViewModel(viewModel: ViewMultimediaViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PeopleOnboardingViewModel::class)
    abstract fun bindPeopleOnboardingViewModel(viewModel: PeopleOnboardingViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FriendsHostViewModel::class)
    abstract fun bindFriendsHostViewModel(viewModel: FriendsHostViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MyFriendsListViewModel::class)
    abstract fun bindMyFriendsListViewModel(viewModel: MyFriendsListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileSettingsViewModel::class)
    abstract fun bindProfileSettingsViewModel(viewModel: ProfileSettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SubscriptionViewModel::class)
    abstract fun bindSubscriptionViewModel(viewModel: SubscriptionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SubscribersViewModel::class)
    abstract fun bindSubscribersViewModel(viewModel: SubscribersViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MeeraSubscriptionViewModel::class)
    abstract fun bindMeeraSubscriptionViewModel(viewModel: MeeraSubscriptionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CommunitiesListsContainerViewModel::class)
    abstract fun bindCommunitiesListsContainerViewModel(viewModel: CommunitiesListsContainerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserFriendActionViewModel::class)
    abstract fun bindUserFriendActionViewModel(viewModel: UserFriendActionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MediaKeyboardStickersViewModel::class)
    abstract fun bindMediaKeyboardStickersViewModel(viewModel: MediaKeyboardStickersViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EventPostPageViewModel::class)
    abstract fun bindEventPostPageViewModel(viewModel: EventPostPageViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MeeraEventPostPageViewModel::class)
    abstract fun bindMeeraEventPostPageViewModel(viewModel: MeeraEventPostPageViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(ShakeBottomDialogViewModel::class)
    abstract fun bindShakeBottomDialogViewModel(viewModel: ShakeBottomDialogViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MeeraVehicleBrandModelSelectViewModel::class)
    abstract fun bindMeeraVehicleBrandModelSelectViewModel(viewModel: MeeraVehicleBrandModelSelectViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ReactionsViewModel::class)
    abstract fun bindReactionsViewModel(viewModel: ReactionsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ReactionsPageViewModel::class)
    abstract fun bindReactionsPageViewModel(viewModel: ReactionsPageViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ShakeFriendRequestsViewModel::class)
    abstract fun bindShakeFriendRequestsViewModel(viewModel: ShakeFriendRequestsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CommunityMembersViewModel::class)
    abstract fun bindCommunityMembersViewModel(viewModel: CommunityMembersViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ShakeEventViewModel::class)
    abstract fun bindShakeEventDelegateViewModel(viewModel: ShakeEventViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ViewVideoViewModel::class)
    abstract fun bindViewVideoViewModel(viewModel: ViewVideoViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ViewVideoItemViewModel::class)
    abstract fun bindViewVideoItemViewModel(viewModel: ViewVideoItemViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EventNavigationDialogViewModel::class)
    abstract fun bindEventNavigationViewModel(viewModel: EventNavigationDialogViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PopUpRateUsDialogViewModel::class)
    abstract fun bindPopUpRateUsDialogViewModel(viewModel: PopUpRateUsDialogViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MeeraRateUsViewModel::class)
    abstract fun bindMeeraRateUsViewModel(viewModel: MeeraRateUsViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(FilterViewModel::class)
    abstract fun bindFilterViewModel(viewModel: FilterViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EventParticipantsListViewModel::class)
    abstract fun bindParticipantsListViewModel(viewModel: EventParticipantsListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MessageNotificationsUsersViewModel::class)
    abstract fun bindMessageNotificationsUsersViewModel(viewModel: MessageNotificationsUsersViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SubscriptionsNotificationUsersViewModel::class)
    abstract fun bindSubscriptionsNotifUsersViewModel(viewModel: SubscriptionsNotificationUsersViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BlacklistSettingsViewModel::class)
    abstract fun bindBlacklistSettingsViewModel(viewModel: BlacklistSettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CallSettingsBlacklistViewModel::class)
    abstract fun bindCallSettingsBlacklistViewModel(viewModel: CallSettingsBlacklistViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CallSettingsWhitelistViewModel::class)
    abstract fun bindCallSettingsWhitelistViewModel(viewModel: CallSettingsWhitelistViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MapSettingsBlacklistViewModel::class)
    abstract fun bindMapSettingsBlacklistViewModel(viewModel: MapSettingsBlacklistViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MapSettingsWhitelistViewModel::class)
    abstract fun bindMapSettingsWhitelistViewModel(viewModel: MapSettingsWhitelistViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OnlineSettingsBlacklistViewModel::class)
    abstract fun bindOnlineSettingsBlacklistViewModel(viewModel: OnlineSettingsBlacklistViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OnlineSettingsWhitelistViewModel::class)
    abstract fun bindOnlineSettingsWhitelistViewModel(viewModel: OnlineSettingsWhitelistViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PersonalMessagesBlackListViewModel::class)
    abstract fun bindPersonalMessagesBlackListViewModel(viewModel: PersonalMessagesBlackListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NotificationViewModel::class)
    abstract fun bindNotificationViewModel(viewModel: NotificationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MeeraNotificationViewModel::class)
    abstract fun bindMeeraNotificationViewModel(viewModel: MeeraNotificationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MeeraDetailNotificationViewModel::class)
    abstract fun bindMeeraDetailNotificationViewModel(viewModel: MeeraDetailNotificationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PersonalMessagesWhiteListViewModel::class)
    abstract fun bindPersonalMessagesWhiteListViewModel(viewModel: PersonalMessagesWhiteListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RoadSettingsViewModel::class)
    abstract fun bindRoadSettingsViewModel(viewModel: RoadSettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CommunitySubscriptionViewModel::class)
    abstract fun bindCommunitySubscriptionViewModel(viewModel: CommunitySubscriptionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SearchUserViewModel::class)
    abstract fun bindSearchUserViewModel(viewModel: SearchUserViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SharingRoomsViewModel::class)
    abstract fun bindSharingRoomsViewModel(viewModel: SharingRoomsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SharingLoaderViewModel::class)
    abstract fun bindSharingLoaderViewModel(viewModel: SharingLoaderViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ContentSharingViewModel::class)
    abstract fun bindContentSharingViewModel(viewModel: ContentSharingViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ScreenshotPopupViewModel::class)
    abstract fun bindScreenshotPopupViewModel(viewModel: ScreenshotPopupViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MeeraCreatePostViewModel::class)
    abstract fun bindAddPostMultipleMediaViewModel(viewModel: MeeraCreatePostViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(CommentsBottomSheetViewModel::class)
    abstract fun bindCommentsBottomSheetViewModel(viewModel: CommentsBottomSheetViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MeeraRoomsViewModel::class)
    abstract fun bindMeeraRoomsViewModel(viewModel: MeeraRoomsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MeeraMainChatViewModel::class)
    abstract fun bindMeeraMainChatViewModel(viewModel: MeeraMainChatViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MeeraMainContainerViewModel::class)
    abstract fun bindMeeraMainContainerViewModel(viewModel: MeeraMainContainerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MeeraServicesViewModel::class)
    abstract fun bindMeeraServicesViewModel(viewModel: MeeraServicesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MeeraPeoplesViewModel::class)
    abstract fun bindMeeraPeoplesViewModel(viewModel: MeeraPeoplesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MeeraUserInfoViewModel::class)
    abstract fun bindMeeraUserInfoViewModel(viewModel: MeeraUserInfoViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MeeraMyFriendListViewModel::class)
    abstract fun bindMeeraMyFriendListViewModel(viewModel: MeeraMyFriendListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MeeraMainPostRoadsViewModel::class)
    abstract fun bindMeeraMainPostRoadsViewModel(viewModel: MeeraMainPostRoadsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfilePhotoViewerViewModel::class)
    abstract fun bindProfilePhotoViewerViewModel(viewModel: ProfilePhotoViewerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EditorWidgetsViewModel::class)
    abstract fun bindEditorWidgetsViewModel(viewModel: EditorWidgetsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NumberSearchViewModel::class)
    abstract fun bindNumberSearchViewModel(viewModel: NumberSearchViewModel): ViewModel
}
