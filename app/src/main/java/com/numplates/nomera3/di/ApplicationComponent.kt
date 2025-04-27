package com.numplates.nomera3.di

import androidx.lifecycle.ViewModelProvider
import com.meera.application_api.ApplicationApi
import com.meera.core.di.CoreComponent
import com.meera.core.di.scopes.AppScope
import com.mera.bridge.featuretoggle.FeatureToggleValuesBridge
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.data.fcm.FirebaseIDService
import com.numplates.nomera3.data.fcm.NotificationHelper
import com.numplates.nomera3.data.fcm.NotificationReceiver
import com.numplates.nomera3.data.services.UploadService
import com.numplates.nomera3.data.workers.SyncContactsWorker
import com.numplates.nomera3.di.viewmodel.ViewModelModule
import com.numplates.nomera3.modules.appDialogs.ui.DialogQueueViewModel
import com.numplates.nomera3.modules.appInfo.ui.ForceUpdateDialog
import com.numplates.nomera3.modules.appInfo.ui.MeeraForceUpdateFragment
import com.numplates.nomera3.modules.auth.ui.AuthViewModel
import com.numplates.nomera3.modules.auth.util.LogoutDelegate
import com.numplates.nomera3.modules.baseCore.helper.AudioFeedHelper
import com.numplates.nomera3.modules.chat.ChatViewModel
import com.numplates.nomera3.modules.chat.MeeraChatFragment
import com.numplates.nomera3.modules.chat.helpers.StickersSuggestionsDelegate
import com.numplates.nomera3.modules.chat.helpers.editmessage.EditMessageWorker
import com.numplates.nomera3.modules.chat.helpers.resendmessage.ResendMessageWorker
import com.numplates.nomera3.modules.chat.helpers.sendmessage.SendMessageWorker
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.ui.fragment.PickerFragment
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.ui.viewmodel.PickerViewModel
import com.numplates.nomera3.modules.chat.requests.ui.viewmodel.ChatRequestViewModel
import com.numplates.nomera3.modules.chat.workers.ReadChatMessageWorker
import com.numplates.nomera3.modules.chatfriendlist.presentation.ChatFriendListViewModel
import com.numplates.nomera3.modules.chatrooms.ui.MeeraRoomsFragment
import com.numplates.nomera3.modules.comments.ui.viewmodel.PostViewModelV2
import com.numplates.nomera3.modules.communities.ui.fragment.CommunityRoadFragment
import com.numplates.nomera3.modules.communities.ui.fragment.list.CommunitiesListsContainerFragment
import com.numplates.nomera3.modules.communities.ui.fragment.list.MeeraCommunitiesListsContainerFragment
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityAdministrationScreenViewModel
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityDetailsViewModel
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityMembersViewModel
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityRoadViewModel
import com.numplates.nomera3.modules.communities.ui.viewmodel.blacklist.CommunityBlacklistViewModel
import com.numplates.nomera3.modules.communities.ui.viewmodel.list.CommunitiesListViewModel
import com.numplates.nomera3.modules.communities.ui.viewmodel.list.CommunitiesSearchViewModel
import com.numplates.nomera3.modules.communities.ui.viewmodel.list.UserCommunitiesListViewModel
import com.numplates.nomera3.modules.complains.domain.worker.UploadComplaintMediaWorker
import com.numplates.nomera3.modules.complains.ui.details.MeeraUserComplaintDetailsFragment
import com.numplates.nomera3.modules.complains.ui.details.UserComplainDetailsFragment
import com.numplates.nomera3.modules.complains.ui.reason.UserComplainReasonFragment
import com.numplates.nomera3.modules.devtools_bridge.presentation.DevToolsBridgeInteractor
import com.numplates.nomera3.modules.feed.ui.viewmodel.FeedViewModel
import com.numplates.nomera3.modules.gifservice.ui.GiphyChatMenuDelegateUI
import com.numplates.nomera3.modules.gifservice.ui.MeeraGiphyChatMenuDelegateUI
import com.numplates.nomera3.modules.gifservice.ui.viewmodel.GiphyViewModel
import com.numplates.nomera3.modules.gift_coffee.ui.coffee_select.CoffeeSelectViewModel
import com.numplates.nomera3.modules.gift_coffee.ui.viewmodel.CoffeeLikePromoCodeViewModel
import com.numplates.nomera3.modules.gift_coffee.ui.viewmodel.GiftListPlacesViewModel
import com.numplates.nomera3.modules.hashtag.ui.fragment.HashtagFragment
import com.numplates.nomera3.modules.holidays.ui.XmasTextSpannableProcessor
import com.numplates.nomera3.modules.holidays.ui.dialog.HolidayDialogViewModel
import com.numplates.nomera3.modules.holidays.ui.dialog.HolidayIntroDialog
import com.numplates.nomera3.modules.moments.show.data.MomentsRepositoryImpl
import com.numplates.nomera3.modules.moments.show.data.UploadMomentWorker
import com.numplates.nomera3.modules.moments.show.domain.MomentUploader
import com.numplates.nomera3.modules.moments.show.presentation.MomentCreateViewModel
import com.numplates.nomera3.modules.moments.widgets.EditorWidgetsFragment
import com.numplates.nomera3.modules.moments.widgets.EditorWidgetsViewModel
import com.numplates.nomera3.modules.moments.widgets.MeeraEditorWidgetsFragment
import com.numplates.nomera3.modules.music.ui.viewmodel.AddMusicViewModel
import com.numplates.nomera3.modules.music.ui.viewmodel.MeeraMusicViewModel
import com.numplates.nomera3.modules.newroads.MainPostRoadsFragment
import com.numplates.nomera3.modules.newroads.MainPostRoadsViewModel
import com.numplates.nomera3.modules.newroads.SubscriptionRoadViewModel
import com.numplates.nomera3.modules.newroads.data.PostsRepository
import com.numplates.nomera3.modules.purchase.ui.gift.GiftsListFragmentNew
import com.numplates.nomera3.modules.purchase.ui.gift.MeeraGiftsListFragment
import com.numplates.nomera3.modules.purchase.ui.send.MeeraSendGiftFragment
import com.numplates.nomera3.modules.purchase.ui.send.SendGiftFragment
import com.numplates.nomera3.modules.purchase.ui.vip.FragmentUpgradeToVipNew
import com.numplates.nomera3.modules.purchase.ui.vip.UpdateStatusFragment
import com.numplates.nomera3.modules.rateus.presentation.PopUpRateUsDialogFragment
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.MeeraActivityViewModel
import com.numplates.nomera3.modules.redesign.fragments.main.MainRoadFragment
import com.numplates.nomera3.modules.redesign.fragments.main.MeeraMainContainerFragment
import com.numplates.nomera3.modules.redesign.fragments.main.map.MeeraAddPostFragmentNew
import com.numplates.nomera3.modules.redesign.fragments.main.map.configuration.MeeraConfigurationStepThirdFragment
import com.numplates.nomera3.modules.redesign.fragments.secondary.MeeraCreatePostFragment
import com.numplates.nomera3.modules.registration.di.RegistrationComponent
import com.numplates.nomera3.modules.registration.di.RegistrationModule
import com.numplates.nomera3.modules.registration.ui.FirebasePushSubscriberDelegate
import com.numplates.nomera3.modules.registration.ui.code.RegistrationCodeViewModel
import com.numplates.nomera3.modules.registration.ui.email.RegistrationEmailViewModel
import com.numplates.nomera3.modules.registration.ui.phoneemail.RegistrationPhoneEmailViewModel
import com.numplates.nomera3.modules.search.ui.viewmodel.group.SearchGroupDefaultViewModel
import com.numplates.nomera3.modules.search.ui.viewmodel.group.SearchGroupResultViewModel
import com.numplates.nomera3.modules.search.ui.viewmodel.hashtag.SearchHashTagDefaultViewModel
import com.numplates.nomera3.modules.search.ui.viewmodel.hashtag.SearchHashTagResultViewModel
import com.numplates.nomera3.modules.search.ui.viewmodel.user.SearchUserDefaultViewModel
import com.numplates.nomera3.modules.search.ui.viewmodel.user.SearchUserResultViewModel
import com.numplates.nomera3.modules.tags.ui.viewmodel.TagMenuViewModel
import com.numplates.nomera3.modules.tags.ui.viewmodel.TagMenuViewModelNew
import com.numplates.nomera3.modules.upload.domain.usecase.post.CompressVideoUseCase
import com.numplates.nomera3.modules.upload.domain.usecase.post.UploadPostUseCase
import com.numplates.nomera3.modules.uploadpost.ui.AddMultipleMediaPostFragment
import com.numplates.nomera3.modules.uploadpost.ui.AddPostFragment
import com.numplates.nomera3.modules.userprofile.profilestatistics.ui.viewmodel.ProfileStatisticsContainerViewModel
import com.numplates.nomera3.modules.userprofile.ui.meerafriends.MyFriendsListViewModel
import com.numplates.nomera3.modules.userprofile.ui.meerafriends.subscribers.MeeraSubscribersViewModel
import com.numplates.nomera3.modules.usersettings.ui.viewmodel.PrivacyNewViewModel
import com.numplates.nomera3.presentation.download.DownloadVideoToGalleryWorker
import com.numplates.nomera3.presentation.presenter.GaragePresenter
import com.numplates.nomera3.presentation.upload.BaseMediaCoroutineWorker
import com.numplates.nomera3.presentation.upload.UploadImagesToGalleryWorker
import com.numplates.nomera3.presentation.upload.UploadPostWorker
import com.numplates.nomera3.presentation.view.fragments.AboutFragment
import com.numplates.nomera3.presentation.view.fragments.FriendsHostFragmentNew
import com.numplates.nomera3.presentation.view.fragments.MainFragment
import com.numplates.nomera3.presentation.view.fragments.MeeraFriendsHostFragment
import com.numplates.nomera3.presentation.view.fragments.ProfileSettingsFragmentNew
import com.numplates.nomera3.presentation.view.fragments.RoomsFragmentV2
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment
import com.numplates.nomera3.presentation.view.fragments.aboutapp.MeeraAboutFragment
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.BottomSheetCountryVehicleFragment
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.cityfilter.CityFilterViewModel
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.cityselector.CityPickerBottomSheetDialogViewModel
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.numbersearch.NumberSearchViewModel
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter.RoadFilterBottomSheetNew
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter.RoadFilterSubscriptionsViewModel
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter.RoadFilterViewModel
import com.numplates.nomera3.presentation.view.fragments.dialogs.CityPickerDialogViewModel
import com.numplates.nomera3.presentation.view.fragments.dialogs.CountryPickerDialogViewModel
import com.numplates.nomera3.presentation.view.fragments.meerasettings.MeeraProfileSettingsFragment
import com.numplates.nomera3.presentation.view.fragments.newchat.ChatGroupEditFragment
import com.numplates.nomera3.presentation.view.fragments.notificationsettings.message.MessageNotificationsAddUsersViewModel
import com.numplates.nomera3.presentation.view.fragments.notificationsettings.subscription.SubscriptionsNotificationAddUsersViewModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.BlacklistSettingsAddUsersViewModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.CallSettingsAddBlacklistViewModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.CallSettingsAddWhitelistViewModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.FriendsFollowersPrivacyViewModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.MapSettingsAddBlacklistViewModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.MapSettingsAddWhitelistViewModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.OnlineSettingsAddBlacklistViewModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.OnlineSettingsAddWhitelistViewModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.PersonalMessagesAddBlackListViewModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.PersonalMessagesAddWhiteListViewModel
import com.numplates.nomera3.presentation.view.utils.sharedialog.MeeraShareBottomSheet
import com.numplates.nomera3.presentation.view.utils.sharedialog.MeeraSharePostBottomSheet
import com.numplates.nomera3.presentation.view.utils.sharedialog.SharePostBottomSheet
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.TedBottomSheetDialogFragment
import com.numplates.nomera3.presentation.view.widgets.NavigationBarView
import com.numplates.nomera3.presentation.view.widgets.VipView
import com.numplates.nomera3.presentation.view.widgets.VipViewNew
import com.numplates.nomera3.presentation.view.widgets.facebar.AvatarView
import com.numplates.nomera3.presentation.viewmodel.AddNumberViewModel
import com.numplates.nomera3.presentation.viewmodel.AllVehicleTypesViewModel
import com.numplates.nomera3.presentation.viewmodel.AuthSmsViewModel
import com.numplates.nomera3.presentation.viewmodel.CallListUsersViewModel
import com.numplates.nomera3.presentation.viewmodel.ChatGroupShowUsersViewModel
import com.numplates.nomera3.presentation.viewmodel.EditVehicleGarageViewModel
import com.numplates.nomera3.presentation.viewmodel.EditVehicleMarketViewModel
import com.numplates.nomera3.presentation.viewmodel.FindByNumberViewModel
import com.numplates.nomera3.presentation.viewmodel.GroupEditViewModel
import com.numplates.nomera3.presentation.viewmodel.LocationViewModel
import com.numplates.nomera3.presentation.viewmodel.MainActivityViewModel
import com.numplates.nomera3.presentation.viewmodel.MainFragmentViewModel
import com.numplates.nomera3.presentation.viewmodel.MeeraMyFriendListFragmentViewModel
import com.numplates.nomera3.presentation.viewmodel.MyFriendListFragmentNewViewModel
import com.numplates.nomera3.presentation.viewmodel.OutgoingFriendshipRequestListViewModel
import com.numplates.nomera3.presentation.viewmodel.PrivacyCallsViewModel
import com.numplates.nomera3.presentation.viewmodel.ProfileDeleteRecoveryViewModel
import com.numplates.nomera3.presentation.viewmodel.PushSettingsViewModel
import com.numplates.nomera3.presentation.viewmodel.RepostViewModel
import com.numplates.nomera3.presentation.viewmodel.SharePostViewModel
import com.numplates.nomera3.presentation.viewmodel.UserGiftsViewModel
import com.numplates.nomera3.presentation.viewmodel.UserSubscriptionsFriendsInfoViewModel
import com.numplates.nomera3.presentation.viewmodel.VehicleListViewModel
import com.numplates.nomera3.presentation.viewmodel.VehicleParamFillViewModel
import com.numplates.nomera3.presentation.viewmodel.profilephoto.GridProfilePhotoViewModel
import com.numplates.nomera3.telecom.CallFragment
import com.numplates.nomera3.telecom.MeeraSignalingService
import com.numplates.nomera3.telecom.SignalingService
import dagger.Component

@AppScope
@Component(
    dependencies = [
        CoreComponent::class
    ],
    modules = [
        ApplicationModule::class,
        ViewModelModule::class,
        UseCasesModule::class,
        ApplicationApiModule::class,
        RepositoryModule::class,
        AudioModule::class,
        AnalyticsBindModule::class,
        UtilsModule::class,
        ApiModule::class
    ]
)
interface ApplicationComponent : ApplicationApi {
    fun addRegistrationComponent(registrationModule: RegistrationModule): RegistrationComponent

    fun inject(application: App)
    fun inject(devToolsBridge: DevToolsBridgeInteractor)
    fun inject(featureToggleValuesBridge: FeatureToggleValuesBridge)
    fun inject(act: Act)
    fun inject(meeraAct: MeeraAct)

    fun inject(dialog: HolidayIntroDialog)
    fun inject(fragment: MainPostRoadsFragment)
    fun inject(fragment: MainRoadFragment)
    fun inject(fragment: MeeraMainContainerFragment)
    fun inject(navbar: NavigationBarView)
    fun inject(avatarView: AvatarView)
    fun inject(vipView: VipView)
    fun inject(vipView: VipViewNew)
    fun inject(dialogFragment: PopUpRateUsDialogFragment)
    fun inject(bottomFragment: BottomSheetCountryVehicleFragment)
    fun inject(bottomFragment: SharePostBottomSheet)
    fun inject(bottomFragment: MeeraSharePostBottomSheet)
    fun inject(bottomFragment: MeeraShareBottomSheet)
    fun inject(communitiesContainerFragment: CommunitiesListsContainerFragment)
    fun inject(meeraCommunitiesContainerFragment: MeeraCommunitiesListsContainerFragment)
    fun inject(fragment: ForceUpdateDialog)
    fun inject(fragment: MeeraForceUpdateFragment)
    fun inject(fragment: TedBottomSheetDialogFragment)
    fun inject(fragment: CallFragment)
    fun inject(fragment: PickerFragment)
    fun inject(fragment: FriendsHostFragmentNew)
    fun inject(fragment: MeeraFriendsHostFragment)

    fun inject(fragment: AddPostFragment)
    fun inject(fragment: AddMultipleMediaPostFragment)

    fun inject(fragment: MeeraAddPostFragmentNew)
    fun inject(fragment: MeeraCreatePostFragment)
    fun inject(activity: MainActivityViewModel)
    fun inject(activity: MeeraActivityViewModel)
    fun inject(fragment: MainFragmentViewModel)

    fun inject(fragment: UserComplainDetailsFragment)
    fun inject(fragment: MeeraUserComplaintDetailsFragment)
    fun inject(fragment: FragmentUpgradeToVipNew)
    fun inject(fragment: GiftsListFragmentNew)
    fun inject(fragment: MeeraGiftsListFragment)
    fun inject(fragment: SendGiftFragment)
    fun inject(fragment: MeeraSendGiftFragment)
    fun inject(fragment: UpdateStatusFragment)

    fun inject(viewModel: AuthSmsViewModel)

    fun inject(viewModel: MainPostRoadsViewModel)
    fun inject(viewModel: ChatRequestViewModel)
    fun inject(viewModel: ChatViewModel)

    fun inject(viewModel: ChatGroupShowUsersViewModel)
    fun inject(viewModel: UserGiftsViewModel)
    fun inject(viewModel: GiftListPlacesViewModel)
    fun inject(viewModel: CityPickerDialogViewModel)
    fun inject(viewModel: CityPickerBottomSheetDialogViewModel)
    fun inject(viewModel: CountryPickerDialogViewModel)
    fun inject(viewModel: SearchUserResultViewModel)
    fun inject(viewModel: SearchUserDefaultViewModel)
    fun inject(viewModel: SearchGroupResultViewModel)
    fun inject(viewModel: SearchHashTagDefaultViewModel)
    fun inject(viewModel: SearchGroupDefaultViewModel)
    fun inject(viewModel: SearchHashTagResultViewModel)
    fun inject(viewModel: FriendsFollowersPrivacyViewModel)

    fun inject(viewModel: FindByNumberViewModel)
    fun inject(viewModel: NumberSearchViewModel)
    fun inject(viewModel: GroupEditViewModel)
    fun inject(viewModel: CommunityRoadViewModel)
    fun inject(viewModel: CommunityAdministrationScreenViewModel)
    fun inject(viewModel: CommunityDetailsViewModel)
    fun inject(viewModel: CommunitiesListViewModel)
    fun inject(viewModel: UserCommunitiesListViewModel)

    fun inject(viewModel: RegistrationEmailViewModel)
    fun inject(viewModel: CommunityBlacklistViewModel)
    fun inject(viewModel: CommunitiesSearchViewModel)
    fun inject(viewModel: CommunityMembersViewModel)
    fun inject(viewModel: CommunityRoadFragment)

    fun inject(viewModel: GridProfilePhotoViewModel)
    fun inject(viewModel: MyFriendListFragmentNewViewModel)
    fun inject(viewModel: MeeraMyFriendListFragmentViewModel)
    fun inject(viewModel: OutgoingFriendshipRequestListViewModel)
    fun inject(viewModel: AddNumberViewModel)
    fun inject(viewModel: SubscriptionRoadViewModel)
    fun inject(viewModel: PrivacyNewViewModel)
    fun inject(viewModel: EditVehicleGarageViewModel)
    fun inject(viewModel: EditVehicleMarketViewModel)
    fun inject(viewModel: VehicleParamFillViewModel)
    fun inject(viewModel: LocationViewModel)
    fun inject(viewModel: AllVehicleTypesViewModel)
    fun inject(viewModel: VehicleListViewModel)
    fun inject(viewModel: PostViewModelV2)
    fun inject(viewModel: CallListUsersViewModel)
    fun inject(viewModel: PrivacyCallsViewModel)
    fun inject(viewModel: HolidayDialogViewModel)
    fun inject(viewModel: MessageNotificationsAddUsersViewModel)
    fun inject(viewModel: OnlineSettingsAddWhitelistViewModel)
    fun inject(viewModel: OnlineSettingsAddBlacklistViewModel)
    fun inject(viewModel: MapSettingsAddBlacklistViewModel)
    fun inject(viewModel: MapSettingsAddWhitelistViewModel)
    fun inject(viewModel: BlacklistSettingsAddUsersViewModel)
    fun inject(viewModel: PersonalMessagesAddWhiteListViewModel)
    fun inject(viewModel: PersonalMessagesAddBlackListViewModel)
    fun inject(viewModel: CallSettingsAddBlacklistViewModel)
    fun inject(viewModel: CallSettingsAddWhitelistViewModel)
    fun inject(viewModel: SharePostViewModel)
    fun inject(viewModel: RepostViewModel)
    fun inject(viewModel: MeeraSubscribersViewModel)
    fun inject(viewModel: RoadFilterViewModel)
    fun inject(viewModel: RoadFilterSubscriptionsViewModel)
    fun inject(viewModel: CityFilterViewModel)
    fun inject(viewModel: SubscriptionsNotificationAddUsersViewModel)
    fun inject(viewModel: ProfileDeleteRecoveryViewModel)
    fun inject(viewModel: TagMenuViewModel)
    fun inject(viewModel: TagMenuViewModelNew)
    fun inject(viewModel: CoffeeSelectViewModel)
    fun inject(viewModel: CoffeeLikePromoCodeViewModel)
    fun inject(viewModel: AuthViewModel)
    fun inject(viewModel: AddMusicViewModel)
    fun inject(viewModel: MeeraMusicViewModel)
    fun inject(viewModel: GiphyViewModel)
    fun inject(viewModel: XmasTextSpannableProcessor)
    fun inject(viewModel: UserSubscriptionsFriendsInfoViewModel)
    fun inject(viewModel: MyFriendsListViewModel)

    // Repositories
    fun inject(repository: PostsRepository)
    fun inject(repository: MomentsRepositoryImpl)

    // Workers
    fun inject(worker: ReadChatMessageWorker)
    fun inject(worker: UploadImagesToGalleryWorker)
    fun inject(worker: UploadPostWorker)
    fun inject(worker: SendMessageWorker)
    fun inject(worker: EditMessageWorker)
    fun inject(worker: UploadMomentWorker)
    fun inject(worker: ResendMessageWorker)
    fun inject(worker: UploadComplaintMediaWorker)
    fun inject(worker: DownloadVideoToGalleryWorker)
    fun inject(worker: SyncContactsWorker)
    fun inject(worker: BaseMediaCoroutineWorker)

    // SL__:
    fun inject(Service: SignalingService)
    fun inject(Service: MeeraSignalingService)

    fun inject(service: UploadService)
    fun inject(notificationHelper: NotificationHelper)
    fun inject(viewModel: PushSettingsViewModel)
    fun inject(presenter: GaragePresenter)

    fun inject(fragment: ChatGroupEditFragment)
    fun inject(fragment: AboutFragment)

    fun inject(fragment: MeeraAboutFragment)
    fun inject(fragment: MeeraChatFragment)
    fun inject(fragment: MeeraConfigurationStepThirdFragment)

    fun inject(viewModel: HashtagFragment)
    fun inject(fragment: ProfileSettingsFragmentNew)
    fun inject(fragment: MeeraProfileSettingsFragment)
    fun inject(firebaseIDService: FirebaseIDService)
    fun inject(userInfoFragment: UserInfoFragment)
    fun inject(mainFragment: MainFragment)
    fun inject(fragment: RoadFilterBottomSheetNew)
    fun inject(userComplainReasonFragment: UserComplainReasonFragment)
    fun inject(notificationReceiver: NotificationReceiver)
    fun inject(fragment: RoomsFragmentV2)

    fun inject(useCase: UploadPostUseCase)
    fun inject(useCase: CompressVideoUseCase)
    fun inject(viewModel: RegistrationPhoneEmailViewModel)
    fun inject(viewModel: RegistrationCodeViewModel)
    fun inject(viewModel: FirebasePushSubscriberDelegate)
    fun inject(feedViewModel: FeedViewModel)
    fun inject(viewModel: ProfileStatisticsContainerViewModel)
    fun inject(viewModel: DialogQueueViewModel)
    fun inject(viewModel: ChatFriendListViewModel)
    fun inject(viewModel: PickerViewModel)
    fun inject(delegate: StickersSuggestionsDelegate)
    fun inject(delegate: GiphyChatMenuDelegateUI)
    fun inject(delegate: MeeraGiphyChatMenuDelegateUI)
    fun inject(uploader: MomentUploader)
    fun inject(viewModel: MomentCreateViewModel)
    fun inject(delegate: LogoutDelegate)

    fun inject(fragment: MeeraRoomsFragment)

    fun inject(fragment: MeeraEditorWidgetsFragment)
    fun inject(fragment: EditorWidgetsFragment)
    fun inject(viewModel: EditorWidgetsViewModel)

    fun getAudioFeedHelper(): AudioFeedHelper
    fun getViewModelFactory(): ViewModelProvider.Factory
}
