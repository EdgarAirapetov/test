package com.numplates.nomera3.modules.userprofile

import kotlinx.coroutines.ExperimentalCoroutinesApi


@OptIn(ExperimentalCoroutinesApi::class)
class UserProfileViewModelTest {
    // Тесты падают именно на сиай/ Переписать решение с экшенами потом написать нормальные тесты

//    @Mock
//    private lateinit var userRepository: UserRepository
//    @Mock
//    private lateinit var postRepository: PostRepository
//    @Mock
//    private lateinit var communityRepository: CommunityRepository
//    @Mock
//    private lateinit var observer: Observer<UserInfoViewEffect>
//    @Mock
//    private lateinit var analyticsInteractor: AnalyticsInteractor
//
//    @Rule
//    @JvmField
//    val instantTaskExecutorRule = InstantTaskExecutorRule()
//
//    @Rule
//    @JvmField
//    var testCoroutineRule = TestCoroutineRule()

//    @Before
//    fun init() {
//        MockitoAnnotations.openMocks(this)
//        Dispatchers.setMain(Dispatchers.Default)
//        setUpRxSchedulers()
//        observer = mock()
//        analyticsInteractor = mock()
//        `when`(userRepository.getIsNeedShowBirthdayDialogRx()).thenReturn(Observable.just(true))
//        `when`(postRepository.getFeedStateObserver()).thenReturn(Observable.just(FeedUpdateEvent.FeedUpdateAll(0)))
//        `when`(communityRepository.getCommunityListEvents()).thenReturn(flowOf())
//    }

//    @Test
//    fun `when add photo and no authed then do not emit OpenAddPhoto`() = runTest {
//        //given
//        val authRequester = TestAuthRequester(false)
//        val viewModel = generateViewModel(authRequester, analyticsInteractor)
//        viewModel.effect.asLiveData().observeForever(observer)
//
//        // when
//        viewModel.handleUIAction(UserProfileUIAction.AddPhoto)
//        authRequester.getCompleteAction().invoke(true)
//        runCurrent()
//
//        // then
//        verify(analyticsInteractor).logAvatarPickerOpen()
//        verify(observer).onChanged(UserInfoViewEffect.OpenAddPhoto)
//    }

//    @Test
//    fun `when add photo and authed then emit OpenAddPhoto`() = runTest {
//        //given
//        val authRequester = TestAuthRequester(true)
//        val viewModel = generateViewModel(authRequester, analyticsInteractor)
//        viewModel.effect.asLiveData().observeForever(observer)
//
//        // when
//        viewModel.handleUIAction(UserProfileUIAction.AddPhoto)
//        runCurrent()
//
//        // then
//        verify(analyticsInteractor).logAvatarPickerOpen()
//        verify(observer).onChanged(UserInfoViewEffect.OpenAddPhoto)
//    }

//    private fun generateViewModel(authRequester: TestAuthRequester, analyticsInteractor: AnalyticsInteractor): UserProfileViewModel {
//        return UserProfileViewModel(
//            hideUserPosts = mock(),
//            blockStatusUseCase = mock(),
//            enableChatUseCase = mock(),
//            disableChatUseCase = mock(),
//            phoneCallsDisableUseCase = mock(),
//            phoneCallsEnableUseCase = mock(),
//            uploadHelper = mock(),
//            subscriptions = mock(),
//            subscriptionNotificationUseCase = mock(),
//            myTracker = mock(),
//            getSettingsUseCase = mock(),
//            addUserToFriendUseCase = mock(),
//            removeUserFromFriendAndUnsubscribeUseCase = mock(),
//            removeUserFromFriendAndSaveSubscriptionUseCase = mock(),
//            processAnimatedAvatar = mock(),
//            appSettings = mock(),
//            getUserProfileUseCase = mock(),
//            getOwnProfileUseCase = mock(),
//            analyticsInteractor = analyticsInteractor,
//            amplitudeFollowButton = mock(),
//            updateUserDataObserverUseCase = mock(),
//            networkStatusProvider = mock(),
//            getUserUidUseCase = mock(),
//            getProfileStatisticsSlidesUseCase = mock(),
//            pushFriendStatusChanged = mock(),
//            pushBlockStatusChanged = mock(),
//            reactiveUpdateSubscribeUserUseCase = mock(),
//            privacySettingsUiMapper = mock(),
//            getUserBirthdayDialogShownRxUseCase = GetUserBirthdayDialogShownRxUseCase(userRepository),
//            amplitudeMutualFriends = mock(),
//            fbAnalytic = mock(),
//            followButtonAnalytic = mock(),
//            amplitudeAddFriendAnalytic = mock(),
//            communityChangesUseCase = CommunityListEventsUseCase(communityRepository),
//            userBirthdayUtils = mock(),
//            userProfileUseCaseNew = mock(),
//            updateBirthdayShownUseCase = mock(),
//            observeOwnProfileFlow = mock(),
//            getUserMapCoordinateUseCase = mock(),
//            userMapCoordinateUIMapper = mock(),
//            getWebSocketEnabledUseCase = mock(),
//            updateUserAvatarUseCase = mock(),
//            getUserSettingsUseCase = mock(),
//            saveAvatarStateLocally = mock(),
//            getShareProfileLinkUseCase = mock(),
//            userDetailsMapper = mock(),
//            giftItemUIMapper = mock(),
//            userProfileMapper = mock(),
//            profileUIListMapper = mock(),
//            amplitudeProfile = mock(),
//            getChatUserInfoUseCase = mock(),
//            getProfileSuggestionsUseCase = mock(),
//            getFeedStateUseCase = GetFeedStateUseCase(postRepository),
//            uploadState = mock(),
//            clearSavedPeopleContentUseCase = mock(),
//            fileManager = mock(),
//            amplitudePeopleAnalytics = mock(),
//            getUserSettingsStateChangedUseCase = mock(),
//            userSuggestionsUiMapper = mock(),
//            observeSyncContactsUseCase = mock(),
//            getSyncContactsPrivacyUseCase = mock(),
//            blockSuggestionUseCase = mock(),
//            emitSuggestionRemovedUseCase = mock(),
//            removeRelatedUserUseCase = mock(),
//            resourceManager = mock(),
//            profileTooltipInteractor = mock(),
//            syncContactsAnalytic = mock(),
//            cacheCompanionUserUseCase = mock(),
//            getLocalSettingsUseCase = mock(),
//            mapAnalyticsInteractor = mock(),
//            authRequester = authRequester,
//            userCallUnavailableUseCase = mock()
//        )
//    }
}
