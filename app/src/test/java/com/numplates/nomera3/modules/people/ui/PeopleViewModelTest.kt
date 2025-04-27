package com.numplates.nomera3.modules.people.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.numplates.nomera3.TestCoroutineRule
import com.numplates.nomera3.modules.peoples.domain.usecase.GetApprovedUsersUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.GetRelatedUsersUseCase
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.entity.PeopleUiStates
import com.numplates.nomera3.modules.peoples.ui.mapper.PeopleContentUiMapper
import com.numplates.nomera3.modules.peoples.ui.viewmodel.PeoplesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

internal class PeopleViewModelTest {

    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val getApprovedUsersUseCase: GetApprovedUsersUseCase = mock()
    private val getRelatedUsersUseCase: GetRelatedUsersUseCase = mock()
    private val mapper: PeopleContentUiMapper = mock()
    lateinit var viewModel: PeoplesViewModel
    private val observer: Observer<PeopleUiStates> = mock()
    private val contentCreator = PeopleContentTestCreator()

    @Before
    fun init() {
        viewModel = createViewModel()
        viewModel.peoplesContentState.observeForever(observer)
    }

    @Test
    fun `first people state supposed to be shimmer LoadingState`() {
        //given
        `when`(
            mapper.createDefaultContent(false)
        ).thenReturn(contentCreator.createDefaultContent())
        val viewModel = createViewModel()
        val observer: Observer<PeopleUiStates> = mock()
        viewModel.init()
        // when
        viewModel.peoplesContentState.observeForever(observer)

        // then
        verify(observer).onChanged(
            PeopleUiStates.LoadingState(
                contentCreator.createDefaultContent(),
            )
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testRefreshPeopleContentBySwipe() = runTest {
        //given
        val fakePeopleContent = contentCreator.createContent()
        `when`(
            getApprovedUsersUseCase.invoke(
                limit = 20,
                offset = 0
            )
        ).thenReturn(contentCreator.createFakePeopleApprovedUsers())
        `when`(
            getRelatedUsersUseCase.invoke(
                limit = 20,
                offset = 0
            )
        ).thenReturn(contentCreator.createFakeRelatedUsers())
        `when`(
            mapper.createPeopleContent(
                peopleApprovedUserModels = contentCreator.createFakePeopleApprovedUsers(),
                peopleRelatedUserModels = contentCreator.createFakeRelatedUsers(),
                myUserId = 0,
                allowSyncContacts = false
            )
        ).thenReturn(fakePeopleContent)

        //when
        viewModel.handleContentAction(FriendsContentActions.OnRefreshContentBySwipe)
        runCurrent()

        //then
        verify(observer).onChanged(PeopleUiStates.PeoplesContentUiState(fakePeopleContent))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testRefreshPeopleContentBySwipeWithError() = runTest {
        //given
        val fakePeopleContent = contentCreator.createContent()
        `when`(
            getApprovedUsersUseCase.invoke(
                limit = 20,
                offset = 0
            )
        ).thenThrow(java.lang.RuntimeException())
        `when`(
            getRelatedUsersUseCase.invoke(
                limit = 20,
                offset = 0
            )
        ).thenThrow(java.lang.RuntimeException())
        `when`(
            mapper.createPeopleContent(
                peopleApprovedUserModels = emptyList(),
                peopleRelatedUserModels = emptyList(),
                myUserId = 0,
                allowSyncContacts = false
            )
        ).thenReturn(fakePeopleContent)

        //when
        val observer: Observer<PeopleUiStates> = mock()
        viewModel.peoplesContentState.observeForever(observer)
        viewModel.handleContentAction(FriendsContentActions.OnRefreshContentBySwipe)
        runCurrent()

        //then
        verify(observer).onChanged(PeopleUiStates.PeoplesContentUiState(fakePeopleContent))
    }

    private fun createViewModel(): PeoplesViewModel {
        return PeoplesViewModel(
            peopleContentUiMapper = mapper,
            networkStatusProvider = mock(),
            subscribeUserUseCase = mock(),
            unsubscribeUserUseCase = mock(),
            addUserToFriendUseCase = mock(),
            removeFriendRequestUseCase = mock(),
            setSelectCommunityTooltipShownUseCase = mock(),
            getSelectCommunityTooltipShownUseCase = mock(),
            isShowTooltipSessionUseCase = mock(),
            getUserSettingsStateChangedUseCase = mock(),
            getUserUidUseCase = mock(),
            setPeopleOnboardingShownUseCase = mock(),
            getPeopleOnboardingShownUseCase = mock(),
            dialogDismissListener = mock(),
            peopleAnalyticDelegate = mock(),
            getApprovedUsersUseCase = getApprovedUsersUseCase,
            getRelatedUsersUseCase = getRelatedUsersUseCase,
            getPeopleAllSavedContentUseCase = mock(),
            pushSubscribeUserUseCase = mock(),
            getRelatedUsersAndCacheUseCase = mock(),
            getTopUsersAndCacheUseCase = mock(),
            readContactsPermissionProvider = mock(),
            observeSyncContactsUseCase = mock(),
            startSyncContactsUseCase = mock(),
            setSettingsUseCase = mock(),
            getSyncContactsPrivacyUseCase = mock(),
            setSyncContactsPrivacyUseCase = mock(),
            blockSuggestionUseCase = mock(),
            emitSuggestionRemovedUseCase = mock(),
            setNeedShowSyncContactsDialogUseCase = mock(),
            getNeedShowSyncContactsDialogUseCase = mock(),
            removeRelatedUserUseCase = mock(),
            needShowPeopleBadgeUseCase = mock(),
            setPeopleBadgeShownUseCase = mock(),
            friendInviteTapAnalytics = mock(),
            subscribeMomentsEventsUseCase = mock()
        )
    }
}
