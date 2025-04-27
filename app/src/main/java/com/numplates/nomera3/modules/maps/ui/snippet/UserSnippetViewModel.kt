package com.numplates.nomera3.modules.maps.ui.snippet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.AmplitudePropertyMapSnippetOpenType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.AmplitudePropertyMapSnippetType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.model.MapSnippetCloseMethod
import com.numplates.nomera3.modules.maps.domain.analytics.MapAnalyticsInteractor
import com.numplates.nomera3.modules.maps.domain.model.GetUserSnippetsParamsModel
import com.numplates.nomera3.modules.maps.domain.model.UserSnippetModel
import com.numplates.nomera3.modules.maps.domain.model.UserUpdateModel
import com.numplates.nomera3.modules.maps.domain.usecase.GetUserSnippetsUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.NeedToShowUserSnippetOnboardingUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.SetUserSnippetOnboardingShownUseCase
import com.numplates.nomera3.modules.maps.ui.model.MapUserUiModel
import com.numplates.nomera3.modules.maps.ui.snippet.UserSnippetBottomSheetWidget.Companion.PAGE_SIZE
import com.numplates.nomera3.modules.maps.ui.snippet.mapper.UserSnippetUIMapper
import com.numplates.nomera3.modules.maps.ui.snippet.model.DataFetchingStateModel
import com.numplates.nomera3.modules.maps.ui.snippet.model.SnippetEvent
import com.numplates.nomera3.modules.maps.ui.snippet.model.SnippetState
import com.numplates.nomera3.modules.maps.ui.snippet.model.UserPreviewItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class UserSnippetViewModel @Inject constructor(
    private val mapper: UserSnippetUIMapper,
    private val getUserSnippetsUseCase: GetUserSnippetsUseCase,
    private val needToShowUserSnippetOnboardingUseCase: NeedToShowUserSnippetOnboardingUseCase,
    private val setUserSnippetOnboardingShownUseCase: SetUserSnippetOnboardingShownUseCase,
    private val mapAnalyticsInteractor: MapAnalyticsInteractor
) : ViewModel() {

    private val selectedUserStateFlow = MutableStateFlow<MapUserUiModel?>(null)
    private val pagesStateFlow = MutableStateFlow(mapOf<Int, List<UserSnippetModel>>())
    private val userUpdatesStateFlow = MutableStateFlow(mapOf<Long, UserUpdateModel>())
    private val currentItemIndexStateFlow = MutableStateFlow(0)
    private val snippetStateFlow = MutableStateFlow<SnippetState>(SnippetState.Closed)
    private val errorStateFlow = MutableStateFlow<Throwable?>(null)
    private val loadingStateFlow = MutableStateFlow(false)
    private val dataFetchingStateFlow = combine(
        errorStateFlow,
        loadingStateFlow
    ) { error, loading ->
        DataFetchingStateModel(
            error = if (loading) null else error,
            loading = loading
        )
    }

    private var currentSnippetModel: UserSnippetModel? = null

    private val updatedPagesStateFlow = combine(
        pagesStateFlow,
        userUpdatesStateFlow,
        ::mapUpdatedPages
    )
    val liveUiModel = combine(
        selectedUserStateFlow,
        updatedPagesStateFlow,
        currentItemIndexStateFlow,
        snippetStateFlow,
        dataFetchingStateFlow,
        mapper::mapUiModel
    )
        .distinctUntilChanged()
        .asLiveData()

    private val auxUserStateFlow = MutableStateFlow<MapUserUiModel?>(null)
    private val auxUserFullModelStateFlow = MutableStateFlow<UserSnippetModel?>(null)
    val liveAuxUiModel = combine(
        auxUserStateFlow,
        auxUserFullModelStateFlow,
        snippetStateFlow,
        dataFetchingStateFlow,
        mapper::mapAuxUiModel
    )
        .distinctUntilChanged()
        .asLiveData()

    private val _eventStateFlow = MutableSharedFlow<SnippetEvent>()
    val eventStateFlow = _eventStateFlow.asSharedFlow()

    private var resetJob: Job? = null
    private var isAuxSnippet = false

    init {
        selectedUserStateFlow
            .distinctUntilChangedBy { it?.id }
            .onEach(::onNewUser)
            .launchIn(viewModelScope)
    }

    fun initialize(isAuxSnippet: Boolean) {
        this.isAuxSnippet = isAuxSnippet
    }

    fun setAuxUser(auxUser: MapUserUiModel?) {
        auxUserStateFlow.value = auxUser
        getAuxUserFullModel(INITIAL_DATA_LOADING_DELAY_MS)
    }

    fun setSelectedUser(selectedUser: MapUserUiModel?, isFull: Boolean = false, snippet: UserSnippetModel?) {
        resetJob?.cancel()
        currentSnippetModel = snippet
        val resultUser = selectedUser?.copy(isFull = isFull)
        selectedUserStateFlow.value = resultUser
    }

    fun setCurrentItem(currentItemIndex: Int) {
        currentItemIndexStateFlow.value = currentItemIndex
        liveUiModel.value?.items?.let { items ->
            val itemModel = items.getOrNull(currentItemIndex)
            ((itemModel as? UserPreviewItem)?.payload as? UserSnippetModel)?.let { payload ->
                viewModelScope.launch {
                    _eventStateFlow.emit(SnippetEvent.DispatchUserSelected(payload))
                }
            }
            if (items.size - currentItemIndex == 2) {
                getNextPage()
            }
        }
    }

    fun setSnippetState(snippetState: SnippetState) {
        viewModelScope.launch(Dispatchers.Main) {

            snippetStateFlow.value = snippetState
            viewModelScope.launch {
                _eventStateFlow.emit(SnippetEvent.DispatchNewSnippetState(snippetState))
            }
            resetJob?.cancel()
            if (snippetState == SnippetState.Closed) {
                currentSnippetModel = null
                resetJob = viewModelScope.launch {
                    delay(RESET_ON_CLOSE_DELAY_MS)
                    selectedUserStateFlow.value = null
                }
            }
        }
    }

    fun setSnippetSlideOffset(slideOffset: Float) {
        viewModelScope.launch {
            _eventStateFlow.emit(SnippetEvent.DispatchSnippetSlide(slideOffset))
        }
    }

    fun onLastPageOverscroll() {
        if (isAuxSnippet()) {
            getAuxUserFullModel()
        } else {
            getNextPage()
        }
    }

    fun onErrorAction() {
        if (isAuxSnippet()) {
            getAuxUserFullModel()
        } else {
            getNextPage()
        }
    }

    fun updateUserSnippet(userUpdateModel: UserUpdateModel) {
        if (isAuxSnippet()) {
            auxUserFullModelStateFlow.value?.let { snippetModel ->
                auxUserFullModelStateFlow.value = mapper.mapUpdateSnippetModel(
                    snippetModel = snippetModel,
                    updateModel = userUpdateModel
                )
            }
        } else {
            userUpdatesStateFlow.value = userUpdatesStateFlow.value
                .plus(userUpdateModel.uid to userUpdateModel)
        }
    }

    fun isAuxSnippet(): Boolean = isAuxSnippet

    fun setOnboardingShown() {
        runCatching {
            setUserSnippetOnboardingShownUseCase.invoke()
        }
    }

    fun logSnippetOpen(type: AmplitudePropertyMapSnippetOpenType) {
        mapAnalyticsInteractor.logMapSnippetOpen(openType = type, snippetType = AmplitudePropertyMapSnippetType.USER)
    }

    fun setCloseMethod(method: MapSnippetCloseMethod) = mapAnalyticsInteractor.setMapSnippetCloseMethod(method)

    fun logClose() = mapAnalyticsInteractor.logMapSnippetClose(AmplitudePropertyMapSnippetType.USER)

    private fun getNextPage(delayMs: Long = 0L) {
        selectedUserStateFlow.value?.let { selectedUser ->
            errorStateFlow.value = null
            viewModelScope.launch {
                delay(delayMs)
                doGetNextPage(selectedUser)
            }
        }
    }

    private fun getAuxUserFullModel(delayMs: Long = 0L) {
        auxUserStateFlow.value?.let { auxUser ->
            errorStateFlow.value = null
            viewModelScope.launch {
                delay(delayMs)
                doGetAuxUserFullModel(auxUser)
            }
        }
    }

    private fun mapUpdatedPages(
        pageMap: Map<Int, List<UserSnippetModel>>,
        userUpdatesMap: Map<Long, UserUpdateModel>
    ): Map<Int, List<UserSnippetModel>> {
        return pageMap
            .map { (pageIndex, snippets) ->
                val updatedPageSnippets = snippets.map { snippetModel ->
                    userUpdatesMap[snippetModel.uid]
                        ?.let { mapper.mapUpdateSnippetModel(snippetModel, it) }
                        ?: snippetModel
                }
                pageIndex to updatedPageSnippets
            }
            .toMap()
    }

    private fun onNewUser(userPinModel: MapUserUiModel?) {
        userUpdatesStateFlow.value = mapOf()
        pagesStateFlow.value = mapOf()
        currentItemIndexStateFlow.value = 0
        if (userPinModel != null) {
            getNextPage(INITIAL_DATA_LOADING_DELAY_MS)
        }
    }

    private suspend fun doGetNextPage(selectedUser: MapUserUiModel) {
        if (!loadingStateFlow.compareAndSet(expect = false, update = true) && currentSnippetModel == null) return
        val pages = pagesStateFlow.value
        val lastPageSize = pages.entries
            .maxByOrNull { it.key }
            ?.value
            ?.size
        if (lastPageSize == 0) {
            loadingStateFlow.value = false
            return
        }
        if (currentSnippetModel != null) {
            val pageIndex = pagesStateFlow.value.size
            pagesStateFlow.value = pagesStateFlow.value.plus(
                pageIndex to listOf(currentSnippetModel!!)
            )
            loadingStateFlow.value = false
        } else {

            try {
                val pages = pagesStateFlow.value
                val lastPageSize = pages.entries
                    .maxByOrNull { it.key }
                    ?.value
                    ?.size
                if (lastPageSize == 0) return
                val usersIds = pages.values.flatten().map { it.uid }
                val pageIndex = pagesStateFlow.value.size
                val params = GetUserSnippetsParamsModel(
                    selectedUserId = selectedUser.id,
                    excludedUserIds = usersIds,
                    lat = selectedUser.latLng.latitude,
                    lon = selectedUser.latLng.longitude,
                    limit = PAGE_SIZE
                )
                val userSnippets = getUserSnippetsUseCase.invoke(params)
                if (selectedUserStateFlow.value?.id == selectedUser.id) {
                    pagesStateFlow.value = pagesStateFlow.value.plus(
                        pageIndex to userSnippets
                    )
                    if (userSnippets.size >= MIN_PAGES_FOR_ONBOARDING_ANIMATION
                        && pageIndex == 0
                        && needToShowUserSnippetOnboardingUseCase.invoke()
                    ) {
                        _eventStateFlow.emit(SnippetEvent.ShowOnboarding)
                    }
                }
            } catch (t: Throwable) {
                Timber.e(t)
                delay(LOADER_ANIMATION_DELAY_MS)
                errorStateFlow.value = t
            } finally {
                loadingStateFlow.value = false
            }
        }
    }

    private suspend fun doGetAuxUserFullModel(auxUser: MapUserUiModel) {
        if (!loadingStateFlow.compareAndSet(expect = false, update = true)) return
        try {
            val params = GetUserSnippetsParamsModel(
                selectedUserId = auxUser.id,
                excludedUserIds = emptyList(),
                lat = auxUser.latLng.latitude,
                lon = auxUser.latLng.longitude,
                limit = 1
            )
            getUserSnippetsUseCase.invoke(params).getOrNull(0)?.let { auxUserFullModel ->
                auxUserFullModelStateFlow.value = auxUserFullModel
            }
        } catch (t: Throwable) {
            Timber.e(t)
            delay(LOADER_ANIMATION_DELAY_MS)
            errorStateFlow.value = t
        } finally {
            loadingStateFlow.value = false
        }
    }

    companion object {
        private const val INITIAL_DATA_LOADING_DELAY_MS = 100L
        private const val LOADER_ANIMATION_DELAY_MS = 1000L
        private const val RESET_ON_CLOSE_DELAY_MS = 1000L
        private const val MIN_PAGES_FOR_ONBOARDING_ANIMATION = 2
    }
}
