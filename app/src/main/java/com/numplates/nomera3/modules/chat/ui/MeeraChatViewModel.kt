package com.numplates.nomera3.modules.chat.ui

import android.net.Uri
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meera.core.extensions.copyToClipBoard
import com.meera.core.extensions.doAsyncViewModel
import com.meera.core.extensions.empty
import com.meera.core.extensions.simpleName
import com.meera.core.extensions.tickerFlow
import com.meera.core.extensions.toBoolean
import com.meera.core.network.websocket.ConnectionStatus
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.utils.DownloadHelper
import com.meera.core.utils.files.FileManager
import com.meera.core.utils.files.FileUtilsImpl
import com.meera.core.utils.files.isPathTypeContent
import com.meera.core.utils.isBirthdayToday
import com.meera.db.models.DraftUiModel
import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.dialog.DialogStyle
import com.meera.db.models.dialog.UserChat
import com.meera.db.models.message.MessageAttachment
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.App
import com.numplates.nomera3.App.Companion.IS_MOCKED_DATA
import com.numplates.nomera3.data.newmessenger.CHAT_ITEM_TYPE_EVENT
import com.numplates.nomera3.data.newmessenger.ROOM_TYPE_DIALOG
import com.numplates.nomera3.data.newmessenger.ROOM_TYPE_GROUP
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_AUDIO
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_GIF
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_IMAGE
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_STICKER
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_VIDEO
import com.numplates.nomera3.domain.interactornew.GetUserBirthdayUseCase
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.MarkPostAsNotSensitiveForUserParams
import com.numplates.nomera3.domain.interactornew.MarkPostAsNotSensitiveForUserUseCase
import com.numplates.nomera3.domain.interactornew.MarkRoomAsReadUseCase
import com.numplates.nomera3.domain.interactornew.SubscriptionUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.helper.HolidayInfoHelper
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyChatCreatedFromWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyChatMediaKeyboardCategory
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyChatType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.mediakeyboard.AmplitudeMediaKeyboardAnalytic
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.mediakeyboard.AmplitudeMediaKeyboardFavoriteWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.mediakeyboard.AmplitudeMediaKeyboardMediaTypeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.mediakeyboard.AmplitudeMediaKeyboardWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.createInfluencerAmplitudeProperty
import com.numplates.nomera3.modules.bump.domain.usecase.TryToRegisterShakeEventUseCase
import com.numplates.nomera3.modules.calls.domain.GetCallStatusUsecase
import com.numplates.nomera3.modules.chat.ChatAnalyticDelegate
import com.numplates.nomera3.modules.chat.ChatBlockReportResultUiModel
import com.numplates.nomera3.modules.chat.ChatMessageEditor
import com.numplates.nomera3.modules.chat.ChatWorkManagerDelegate
import com.numplates.nomera3.modules.chat.EditingEvents
import com.numplates.nomera3.modules.chat.ShareContentDelegate
import com.numplates.nomera3.modules.chat.data.DialogApproved
import com.numplates.nomera3.modules.chat.data.mapper.MessengerEntityMapper
import com.numplates.nomera3.modules.chat.data.toMessageEntity
import com.numplates.nomera3.modules.chat.domain.interactors.ChatInteractorImlp
import com.numplates.nomera3.modules.chat.domain.interactors.MessagesInteractor
import com.numplates.nomera3.modules.chat.domain.interactors.ToolTipsInteractor
import com.numplates.nomera3.modules.chat.domain.params.MessagePaginationDirection
import com.numplates.nomera3.modules.chat.domain.usecases.AddPhotoFromClipboardUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.ChangeMessageEditingStatusUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.CheckSocketConnectionUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.DeleteMessageByIdUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.GetGreetingStickerUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.GetImageFileForKeyboardContentUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.GetMessageByIdUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.GetMessageEventsObserverUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.GetSubscriptionDismissedUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.ListenSocketStatusUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.RemovePhotoFromMediaAttachmentCarouselUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.SaveMessageIntoDbUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.SetSubscriptionDismissedUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.SuccessGif
import com.numplates.nomera3.modules.chat.domain.usecases.SuccessImage
import com.numplates.nomera3.modules.chat.domain.usecases.UpdateBadgeStatusUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.UpdateDialogUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.messages.voice.DownloadVoiceMessageUseCase
import com.numplates.nomera3.modules.chat.drafts.domain.AddDraftUseCase
import com.numplates.nomera3.modules.chat.drafts.domain.DeleteDraftUseCase
import com.numplates.nomera3.modules.chat.drafts.domain.GetAllDraftsUseCase
import com.numplates.nomera3.modules.chat.drafts.domain.entity.DraftModel
import com.numplates.nomera3.modules.chat.drafts.ui.DraftsUiMapper
import com.numplates.nomera3.modules.chat.helpers.ChatBottomMenuPayload
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitializer
import com.numplates.nomera3.modules.chat.helpers.demo.DemoMessagesProvider
import com.numplates.nomera3.modules.chat.helpers.editmessage.models.EditMessageModel
import com.numplates.nomera3.modules.chat.helpers.isCommunityDeleted
import com.numplates.nomera3.modules.chat.helpers.isEdited
import com.numplates.nomera3.modules.chat.helpers.isLateForEdit
import com.numplates.nomera3.modules.chat.helpers.isMediaMessage
import com.numplates.nomera3.modules.chat.helpers.isMomentDeleted
import com.numplates.nomera3.modules.chat.helpers.isNotBlocked
import com.numplates.nomera3.modules.chat.helpers.isStickerMessage
import com.numplates.nomera3.modules.chat.helpers.isValidForCopy
import com.numplates.nomera3.modules.chat.helpers.pagination.ChatPaginationManager
import com.numplates.nomera3.modules.chat.helpers.resendmessage.ResendType
import com.numplates.nomera3.modules.chat.helpers.sendmessage.models.CurrentInProgressMessage
import com.numplates.nomera3.modules.chat.helpers.sendmessage.models.SendMessageType
import com.numplates.nomera3.modules.chat.mediakeyboard.data.entity.TemporaryMessageText
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.data.FAVORITES_PAGE_SIZE
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.AddToMediaKeyboardFavoritesUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.DeleteMediaKeyboardFavoriteUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.GetMediaKeyboardFavoritesFlowUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.LoadFavoritesUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.SaveMediakeyboardFavoritesInDbUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.entity.MediakeyboardFavoriteRecentModel
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.mapper.MediakeyboardFavoritesUiMapper
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.data.entity.MediaKeyboardViewEvent
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain.AddPhotoUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain.ClearMediaContentUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain.ClearPhotosUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain.GetMediaKeyboardEventUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain.GetMessageTextUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain.GetPhotosUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain.RemovePhotoUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain.SetMessageTextUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain.SetUserNameUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain.ShowOrHideMediaButtonsUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.ui.fragment.MAX_PICTURE_COUNT
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain.GetCachedStickersUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain.ReloadAllStickersInFlow
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain.ReloadRecentStickersUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain.SetStickerPackViewedUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain.SubscribeStickersFlow
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain.UpdateStickerOrderUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerPackUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickersAndRecentStickersUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.mapper.MediaKeyboardStickerUiMapper
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaPreviewType
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaPreviewUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaUiModel
import com.numplates.nomera3.modules.chat.messages.domain.mapper.EditMessageMapper
import com.numplates.nomera3.modules.chat.messages.domain.model.AttachmentType
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_AUDIO_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_NO_MEDIA_PLACEHOLDER_SEND
import com.numplates.nomera3.modules.chat.requests.domain.usecase.ChatRequestAvailabilityParams
import com.numplates.nomera3.modules.chat.requests.domain.usecase.ChatRequestAvailabilityUseCase
import com.numplates.nomera3.modules.chat.requests.domain.usecase.DisableChatRequestImageBlurParams
import com.numplates.nomera3.modules.chat.requests.domain.usecase.DisableChatRequestImageBlurUseCase
import com.numplates.nomera3.modules.chat.requests.domain.usecase.UpdateChatRequestApprovedStatusDbParams
import com.numplates.nomera3.modules.chat.requests.domain.usecase.UpdateChatRequestApprovedStatusDbUseCase
import com.numplates.nomera3.modules.chat.requests.domain.usecase.UpdateMessagesAsChatRequestParams
import com.numplates.nomera3.modules.chat.requests.domain.usecase.UpdateMessagesAsChatRequestUseCase
import com.numplates.nomera3.modules.chat.requests.ui.viewevent.ChatRequestViewEvent
import com.numplates.nomera3.modules.chat.toolbar.domain.usecase.GetChatUserInfoUseCase
import com.numplates.nomera3.modules.chat.toolbar.ui.isChatRequest
import com.numplates.nomera3.modules.chat.toolbar.ui.viewmodel.ChatToolbarViewModelDelegate
import com.numplates.nomera3.modules.chat.ui.action.ChatActions
import com.numplates.nomera3.modules.chat.ui.entity.ChatAttachmentUiModel
import com.numplates.nomera3.modules.chat.ui.helper.DateHeaderHelper
import com.numplates.nomera3.modules.chat.ui.helper.MediaEntitiesCacheHelper
import com.numplates.nomera3.modules.chat.ui.helper.MessageFilterHelper
import com.numplates.nomera3.modules.chat.ui.mapper.MessageUiMapper
import com.numplates.nomera3.modules.chat.ui.mapper.SendMessageWorkResultMapper
import com.numplates.nomera3.modules.chat.ui.mapper.UiKitMessageMapper
import com.numplates.nomera3.modules.chat.ui.model.ChatBackgroundType
import com.numplates.nomera3.modules.chat.ui.model.ChatMessageDataUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageType
import com.numplates.nomera3.modules.chat.ui.model.MessageUiModel
import com.numplates.nomera3.modules.chat.ui.model.PlayMeeraMessageDataModel
import com.numplates.nomera3.modules.chatrooms.domain.interactors.RoomsInteractor
import com.numplates.nomera3.modules.chatrooms.pojo.RoomTimeType
import com.numplates.nomera3.modules.complains.ui.ComplaintFlowResult
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.holidays.ui.entity.RoomType
import com.numplates.nomera3.modules.tracker.FireBaseAnalytics
import com.numplates.nomera3.modules.user.domain.effect.UserSettingsEffect
import com.numplates.nomera3.modules.user.domain.usecase.BlockStatusUseCase
import com.numplates.nomera3.modules.user.domain.usecase.ChatDisableUseCase
import com.numplates.nomera3.modules.user.domain.usecase.ChatEnableUseCase
import com.numplates.nomera3.modules.user.domain.usecase.EnableChatParams
import com.numplates.nomera3.modules.user.domain.usecase.UserPreferencesUseCase
import com.numplates.nomera3.modules.user.domain.usecase.UserProfileDefParams
import com.numplates.nomera3.modules.user.ui.utils.UserBirthdayUtils
import com.numplates.nomera3.modules.userprofile.domain.usecase.SendGreetingUseCase
import com.numplates.nomera3.modules.userprofile.domain.usecase.SendGreetingUseCaseParams
import com.numplates.nomera3.presentation.birthday.ui.BirthdayTextUtil
import com.numplates.nomera3.presentation.model.enums.ChatEventEnum
import com.numplates.nomera3.presentation.utils.networkconn.NetworkStatusProvider
import com.numplates.nomera3.presentation.view.utils.apphints.TooltipDuration
import com.numplates.nomera3.presentation.viewmodel.viewevents.ChatMediaKeyboardEvent
import com.numplates.nomera3.presentation.viewmodel.viewevents.ChatMessageViewEvent
import com.numplates.nomera3.presentation.viewmodel.viewevents.SingleLiveEvent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.LinkedList
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private const val DOWNLOAD_VOICE_MESSAGE_COMPLETE_PROGRESS = 100

class MeeraChatViewModel @Inject constructor(
    private val context: App,
    private val chatInitializer: ChatInitializer,
    private val fbAnalytic: FireBaseAnalytics,
    private val checkSocketConnectionUseCase: CheckSocketConnectionUseCase,
    private val listenSocketStatusUseCase: ListenSocketStatusUseCase,
    private val downloadVoiceMessageUseCase: DownloadVoiceMessageUseCase,
    val paginationUtil: ChatPaginationManager,
    private val markRoomAsReadUseCase: MarkRoomAsReadUseCase,
    private val saveMessageDbUseCase: SaveMessageIntoDbUseCase,
    private val messengerEntityMapper: MessengerEntityMapper,
    private val updateBadgeStatusUseCase: UpdateBadgeStatusUseCase,
    private val markPostAsNotSensitiveForUserUseCase: MarkPostAsNotSensitiveForUserUseCase,
    private val holidayInfoHelper: HolidayInfoHelper,
    private val blockStatusUseCase: BlockStatusUseCase,
    val networkStatusProvider: NetworkStatusProvider,
    private val userBirthdayUtils: UserBirthdayUtils,
    private val birthdayTextUtil: BirthdayTextUtil,
    private val disableChatRequestImageBlurUseCase: DisableChatRequestImageBlurUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val sendGreetingUseCase: SendGreetingUseCase,
    private val subscriptionsUseCase: SubscriptionUseCase,
    private val updateDialogUseCase: UpdateDialogUseCase,
    private val setSubsriptionDismissedUseCase: SetSubscriptionDismissedUseCase,
    private val getSubsriptionDismissedUseCase: GetSubscriptionDismissedUseCase,
    private val updateStickerOrderUseCase: UpdateStickerOrderUseCase,
    private val chatRequestAvailabilityUseCase: ChatRequestAvailabilityUseCase,
    private val updateChatRequestApprovedStatusDbUseCase: UpdateChatRequestApprovedStatusDbUseCase,
    private val removePhotoUseCase: RemovePhotoUseCase,
    private val clearPhotosUseCase: ClearPhotosUseCase,
    private val clearMediaContentUseCase: ClearMediaContentUseCase,
    private val getMediaKeyboardEventUseCase: GetMediaKeyboardEventUseCase,
    private val getPhotosUseCase: GetPhotosUseCase,
    private val getMessageTextUseCase: GetMessageTextUseCase,
    private val setMessageTextUseCase: SetMessageTextUseCase,
    private val deleteMessageByIdUseCase: DeleteMessageByIdUseCase,
    private val updateMessagesAsChatRequestUseCase: UpdateMessagesAsChatRequestUseCase,
    getMessageEvents: GetMessageEventsObserverUseCase,
    private val showOrHideMediaButtonsUseCase: ShowOrHideMediaButtonsUseCase,
    private val chatAnalyticDelegate: ChatAnalyticDelegate,
    private val chatWorkManagerDelegate: ChatWorkManagerDelegate,
    private val enableChatUseCase: ChatEnableUseCase,
    private val disableChatUseCase: ChatDisableUseCase,
    private val getChatUserInfoUseCase: GetChatUserInfoUseCase,
    private val getMessageByIdUsecase: GetMessageByIdUseCase,
    private val imageForKeyboardUseCase: GetImageFileForKeyboardContentUseCase,
    private var getUserUidUseCase: GetUserUidUseCase,
    private val getMediaKeyboardFavoritesUseCase: GetMediaKeyboardFavoritesFlowUseCase,
    private val addToMediaKeyboardFavoritesUseCase: AddToMediaKeyboardFavoritesUseCase,
    private val deleteMediaKeyboardFavoriteUseCase: DeleteMediaKeyboardFavoriteUseCase,
    private val mediakeyboardFavoritesMapper: MediakeyboardFavoritesUiMapper,
    private val loadFavoritesUseCase: LoadFavoritesUseCase,
    private val saveFavoritesToDbUseCase: SaveMediakeyboardFavoritesInDbUseCase,
    private val getAllDraftsUseCase: GetAllDraftsUseCase,
    private val deleteDraftUseCase: DeleteDraftUseCase,
    private val addDraftUseCase: AddDraftUseCase,
    private val getUserBirthdayUseCase: GetUserBirthdayUseCase,
    private val draftsMapper: DraftsUiMapper,
    val analyticsInteractor: AnalyticsInteractor,
    private val tooltipsInteractor: ToolTipsInteractor,
    private val addPhotoUseCase: AddPhotoUseCase,
    private val setStickerPackViewedUseCase: SetStickerPackViewedUseCase,
    private val changeMessageEditingStatusUseCase: ChangeMessageEditingStatusUseCase,
    private val addPhotoFromClipboardUseCase: AddPhotoFromClipboardUseCase,
    private val removePhotoFromMediaAttachmentCarouselUseCase: RemovePhotoFromMediaAttachmentCarouselUseCase,
    private val stickersMapper: MediaKeyboardStickerUiMapper,
    private val roomsInteractor: RoomsInteractor,
    private val messagesInteractor: MessagesInteractor,
    private val chatInteractor: ChatInteractorImlp,
    private val sendMessageWorkResultMapper: SendMessageWorkResultMapper,
    editMessageMapper: EditMessageMapper,
    private val featureTogglesContainer: FeatureTogglesContainer,
    private val amplitudeMediaKeyboardAnalytic: AmplitudeMediaKeyboardAnalytic,
    private val setRegistrationShakeEventUseCase: TryToRegisterShakeEventUseCase,
    private val getGreetingStickerUseCase: GetGreetingStickerUseCase,
    private val fileManager: FileManager,
    private val downloadHelper: DownloadHelper,
    private val reloadRecentStickersUseCase: ReloadRecentStickersUseCase,
    private val getCachedStickersUseCase: GetCachedStickersUseCase,
    private val toolbarViewModelDelegateDelegate: ChatToolbarViewModelDelegate,
    private val messageUiMapper: MessageUiMapper,
    private val uiKitMessageMapper: UiKitMessageMapper,
    private val demoMessagesProvider: DemoMessagesProvider,
    private val dateHeaderHelper: DateHeaderHelper,
    private val mediaEntitiesCacheHelper: MediaEntitiesCacheHelper,
    private val messageFilterHelper: MessageFilterHelper,
    private val setUserNameUseCase: SetUserNameUseCase,
    subscribeStickersFlow: SubscribeStickersFlow,
    private val reloadAllStickersInFlow: ReloadAllStickersInFlow,
    private val getCallStatusUsecase: GetCallStatusUsecase,
) : ViewModel(), WebSocketMainChannel.WebSocketConnectionListener {

    val isEventsOnMapEnabled get() = featureTogglesContainer.mapEventsFeatureToggle.isEnabled

    var wasCalledDeleteRoom = false
    var wasRemovedFromRoom = false
    var isChatBlockedVisually = false

    val liveChatRequestViewEvent = MutableLiveData<ChatRequestViewEvent>()
    val liveDownloadMediaProgress = MutableLiveData<Int>()

    var isRoomChatRequest = false

    private val _liveMessageViewEvent: MutableLiveData<ChatMessageViewEvent> = MutableLiveData()
    val liveMessageViewEvent: LiveData<ChatMessageViewEvent> = _liveMessageViewEvent

    val liveUnreadMessageCountRooms = MutableLiveData<Long>()

    val sendMessageEvents = getMessageEvents.invoke()

    private val _subscriptionDismissedUserIdListLiveData = MutableLiveData<List<Long>>()
    val subscriptionDismissedUserIdListLiveData: LiveData<List<Long>> = _subscriptionDismissedUserIdListLiveData

    private val _mediaKeyboardViewEvent = MutableSharedFlow<ChatMediaKeyboardEvent>()
    val mediaKeyboardViewEvent: SharedFlow<ChatMediaKeyboardEvent> = _mediaKeyboardViewEvent

    private var currentBlockReportResult: ChatBlockReportResultUiModel? = null
    private var currentBlockJob: Job? = null

    val photosSetLiveData: LiveData<Set<String>>
        get() = getPhotosUseCase.invoke()

    private val _chatAttachmentsUiData: MutableLiveData<List<ChatAttachmentUiModel>> = MutableLiveData()
    val chatAttachmentsUiData: LiveData<List<ChatAttachmentUiModel>> = _chatAttachmentsUiData

    val messageTextLiveData: LiveData<TemporaryMessageText>
        get() = getMessageTextUseCase.invoke()

    val toolbarDelegate: ChatToolbarViewModelDelegate
        get() = toolbarViewModelDelegateDelegate

    private val latestMediaKeyboardBehavior = MutableLiveData<MediaKeyboardState>()
    val showMediaKeyboard = SingleLiveEvent<MediaKeyboardState>()

    val currentMediaFavorites = mutableListOf<MediakeyboardFavoriteRecentUiModel>()

    val stickersFlow: Flow<MediaKeyboardStickersAndRecentStickersUiModel> = subscribeStickersFlow.invoke()
        .map { stickers ->
            MediaKeyboardStickersAndRecentStickersUiModel(
                stickerPacks = stickers.stickerPacks.map(stickersMapper::mapStickerPackDomainToUiModel),
                recentStickers = stickers.recentStickers.map(mediakeyboardFavoritesMapper::mapDomainToUiModel),
            )
        }

    private val _remapMessages: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val chatMessagesFlow: Flow<List<ChatMessageDataUiModel>> = combine(
        _remapMessages, selectMessagesSource()
    ) { _, messages -> messages }
        .map(mediaEntitiesCacheHelper::cacheMessageEntities)
        .map(dateHeaderHelper::insertedDateDividers)
        .map { messages ->
            messageUiMapper.mapToMessagesUi(
                messages = messages,
                isSomeoneBirthday = isSomeBodyHasBirthday(dialogEntity?.companion?.birthDate ?: 0)
            )
        }
        .map(messageFilterHelper::filterMessages)
        .map { messages ->
            uiKitMessageMapper.mapToUiKitChatMessagesData(
                messages = messages,
                isGroupChat = dialogEntity?.type == ROOM_TYPE_GROUP
            )
        }
        .catch { error -> Timber.e("FLOW Error messages: $error") }
        .flowOn(Dispatchers.Default)

    private val disposables = CompositeDisposable()

    private var dialogEntity: DialogEntity? = null
    private val receivedMessages = hashSetOf<String>()
    private val receiveMessageIdsSet = hashSetOf<String>()
    private val messageEditor = ChatMessageEditor(
        onSendEditedMessage = ::sendEditedMessage,
        editingEvents = ::handleEditingState,
        editMessageMapper = editMessageMapper,
    )
    private var getStickersJob: Job? = null

    // После переподключения к каналу необходимо заново подписаться на обсервер сообщений
    private var isStartedMessageObserver = false
    private var disposableObserveMessage: Disposable? = null

    private var saveDraftJob: Job? = null

    private val _liveIsChatRequestMenuVisible = MutableLiveData<Boolean>(false)
    val liveIsChatRequestMenuVisible = _liveIsChatRequestMenuVisible as LiveData<Boolean>

    private var editorMediaObserver: Observer<Set<String>>? = null
    private var editorTextObserver: Observer<TemporaryMessageText>? = null

    private var shareContentDelegate: ShareContentDelegate? = null

    init {
        toolbarViewModelDelegateDelegate.init(viewModelScope)
        viewModelScope.launch {
            val subscriptionDismissedUserIdList = getSubsriptionDismissedUseCase.invoke()
            _subscriptionDismissedUserIdListLiveData.postValue(subscriptionDismissedUserIdList)
            observeMediaKeyboardViewEvent()
            observeFavoritesChanges()
            loadMoreFavorites()
            observeReloadDialogs()
            initShareContentDelegate()
        }
        startObservingCallSignals()
    }

    override fun onCleared() {
        clearPhotosUseCase.invoke()
        disposables.dispose()
        paginationUtil.release()
        toolbarViewModelDelegateDelegate.clear()
        super.onCleared()
    }

    fun handleAction(action: ChatActions) {
        Timber.e("CHAT ViewModel handle action:$action")
        when (action) {
            is ChatActions.SetupChat -> setupChat(action.data)
            is ChatActions.SetRoomData -> setRoomData(action.room)
            is ChatActions.LoadStickerPacks -> getAndSetupStickers()
            is ChatActions.PrepareRoomAfterUpdateRoomData -> prepareRoomAfterUpdateRoomData(action.roomId)
            is ChatActions.LoadMessages -> loadListMessagesDb(action.room)
            is ChatActions.RemoveMessage -> removeMessage(action.message, action.isBoth)
            is ChatActions.RemoveOnlyNetworkMessage -> removeOnlyNetworkMessage(action.roomId, action.messageId)
            is ChatActions.CompletelyRemoveMessage -> completelyRemoveMessage(action.roomId, action.messageId)
            is ChatActions.AddToFavoritesToMessage -> addToFavoritesFromMessage(action.message)
            is ChatActions.AddToFavorites -> addToFavoritesHandleEvent(
                action.mediaPreview,
                action.mediaUrl,
                action.lottieUrl
            )

            is ChatActions.RemoveFromFavorites -> removeFromFavorites(action.mediaPreview)
            is ChatActions.UnsentCopyMessageClicked -> unsentCopyMessageClicked()
            is ChatActions.UnsentMessageDelete -> onUnsentMessageDelete(
                action.message,
                action.roomType,
                action.companion
            )

            is ChatActions.ClearMessageEditor -> clearMessageEditor()
            is ChatActions.EnableChat -> enableChat(action.companionUid)
            is ChatActions.DisableChat -> disableChat(action.companionUid)
            is ChatActions.BlockUser -> blockUser(action.companionUid)
            is ChatActions.UnblockUser -> unblockUser(action.companionUid)
            is ChatActions.SendEditedMessageWithConditionsCheck -> sendEditedMessageWithConditionsCheck()
            is ChatActions.RemoveRoom -> removeRoom(action.roomId, action.isBoth, action.isGroupChat)
            is ChatActions.BlockCompanionFromChatRequest ->
                sendEffect(ChatMessageViewEvent.OnBlockCompanionFromChatRequest)

            is ChatActions.BlockReportUserFromChat -> sendEffect(ChatMessageViewEvent.OnBlockReportUserFromChat)
            is ChatActions.CopyMessageContent -> copyMessageTextContent(action.message, action.messageView)

            is ChatActions.DownloadImageVideoAttachment -> sendEffect(
                ChatMessageViewEvent.OnDownloadImageVideoAttachment(
                    action.message
                )
            )

            is ChatActions.CopyImageAttachment -> sendEffect(
                ChatMessageViewEvent.OnCopyImageAttachment(
                    action.message
                )
            )

            is ChatActions.ShareMessageContent -> sendEffect(
                ChatMessageViewEvent.OnShareMessageContent(
                    action.message
                )
            )

            is ChatActions.ForbidCompanionToChat -> sendEffect(ChatMessageViewEvent.OnForbidCompanionToChat)
            is ChatActions.MessageDelete -> sendEffect(ChatMessageViewEvent.OnMessageDelete(action.message))
            is ChatActions.MessageEdit -> sendEffect(ChatMessageViewEvent.OnMessageEdit(action.message))
            is ChatActions.OnMessageForward -> sendEffect(ChatMessageViewEvent.OnMessageForward(action.message))
            is ChatActions.OpenChatComplaintMenu -> sendEffect(ChatMessageViewEvent.OnOpenChatComplaintMenu)
            is ChatActions.ReplyMessage -> handleReplyMessage(message = action.message)
            is ChatActions.ResendSingleMessage ->
                sendEffect(ChatMessageViewEvent.OnResendSingleMessage(action.message))

            is ChatActions.ResendAllMessages ->
                sendEffect(ChatMessageViewEvent.OnResendAllMessages(action.unsentMessageCounter))

            is ChatActions.SendFavoriteRecent -> {
                sendEffect(
                    ChatMessageViewEvent.OnSendFavoriteRecent(
                        action.favoriteRecentUiModel,
                        action.type
                    )
                )
            }

            is ChatActions.SetupMediaPreview ->
                sendEffect(
                    ChatMessageViewEvent.OnSetupMediaPreview(
                        action.media,
                        action.isMeeraMenu,
                        action.menuHeight
                    )
                )

            is ChatActions.OpenChatComplaintRequestMenu ->
                sendEffect(ChatMessageViewEvent.OnOpenChatComplaintRequestMenu)

            is ChatActions.OnSetChatBackground -> handleChatBackground(action.room, action.user, action.roomType)
            is ChatActions.InitGreetings -> handleGreetings()
            is ChatActions.SendFakeMessages -> sendFakeMessages(action.roomId, action.messageText)
            is ChatActions.ComplaintGroupChat ->
                sendEffect(ChatMessageViewEvent.ShowDialogComplaintGroupChat(action.roomId))

            is ChatActions.PlayVoiceMessages -> Unit
            is ChatActions.PlayMeeraVoiceMessage -> playMeeraVoiceMessages(action.message, action.position)
            is ChatActions.InProgressResendSendMessage -> inProgressResendSendMessage(action.roomId)
            is ChatActions.ShareContent -> shareContentDelegate?.handleShareContent(action.types)
            is ChatActions.CopyImageMessageAttachment -> getMediaAttachmentForCopy(
                action.message,
                action.attachmentsIndex
            )

            is ChatActions.OnFavoriteRecentLongClick -> mediaKeyboardFavoriteRecentLongClicked(
                action.model,
                action.type,
                action.deleteRecentClickListener
            )

            is ChatActions.ReloadRecentStickers -> reloadRecentStickers()
            is ChatActions.OnPhotoEdits -> Unit
            is ChatActions.OnVideoEdits -> Unit
            is ChatActions.MapAttachmentsData -> mapMessagesAttachments(action.attachments)
            else -> {}
        }
    }

    fun isHiddenAgeAndGender() = featureTogglesContainer.hiddenAgeAndSexFeatureToggle.isEnabled

    fun chatInitStateFlow() = chatInitializer.chatInitStateFlow.flowOn(Dispatchers.Default)

    fun messagesProgressFlow() = paginationUtil.messagesProgressFlow

    fun isMessageEditActive() = messageEditor.isEditInProgress()

    fun getMessageInEdit() = messageEditor.getOriginalMessage()

    fun startEditingMessage(message: MessageEntity) {
        messageEditor.startEditingMessage(message)
        message.attachments.plus(message.attachment)
            .filter { attachment ->
                attachment.type == AttachmentType.GIF.type
                    || attachment.type == AttachmentType.IMAGE.type
                    || attachment.type == AttachmentType.VIDEO.type
            }
            .map { it.url }
            .forEach(addPhotoUseCase::invoke)
        editorMediaObserver = Observer<Set<String>> { mediaUriSet ->
            messageEditor.updateMedias(mediaUriSet)
        }.also { photosSetLiveData.observeForever(it) }
        editorTextObserver = Observer<TemporaryMessageText> { tempContent ->
            messageEditor.updateText(tempContent.text)
        }.also { messageTextLiveData.observeForever(it) }
    }

    fun sendEditedMessageWithConditionsCheck() {
        val originalMessage = messageEditor.getOriginalMessage()
        val isEditTooLate = originalMessage?.isLateForEdit() ?: true
        if (isEditTooLate) {
            _liveMessageViewEvent.postValue(
                ChatMessageViewEvent.OnEditMessageError(
                    isEditTooLate = true
                )
            )
        } else if (!messageEditor.getEditedMediaUris().isNullOrEmpty()) {
            _liveMessageViewEvent.postValue(
                ChatMessageViewEvent.CheckEditedMediaItems(
                    urls = messageEditor.getEditedMediaUris().orEmpty().toList()
                )
            )
        } else {
            sendEditedMessage()
        }
    }

    fun clearMessageMedias() {
        clearPhotosUseCase.invoke()
    }

    fun addMessageMedias(
        uris: List<Uri>,
        mimeType: String = String.empty(),
        isMaxCountCheck: Boolean = false
    ) {
        val mediaCount = getPhotosUseCase.invoke().value?.size ?: 0
        if (mediaCount >= MAX_PICTURE_COUNT && isMaxCountCheck) {
            _liveMessageViewEvent.value =
                ChatMessageViewEvent.OnShowMaxSelectedMediaCountErrorMessage
            return
        }

        uris.map(Uri::toString).forEach { path ->
            if (isPathTypeContent(path)) {
                processMediaFromKeyboard(Uri.parse(path), mimeType)
            } else {
                addPhotoFromClipboardUseCase.invoke(path)
            }
        }
    }

    private fun processMediaFromKeyboard(media: Uri, mimeType: String) {
        viewModelScope.launch {
            val path = when (val res = imageForKeyboardUseCase.invoke(media, mimeType)) {
                is SuccessGif -> res.gifPath
                is SuccessImage -> res.imagePath
                else -> String.empty()
            }
            if (path.isNotEmpty()) addPhotoFromClipboardUseCase.invoke(path)
        }
    }

    fun logGifSend(gifSentWhereProp: AmplitudeMediaKeyboardWhereProperty) {
        amplitudeMediaKeyboardAnalytic.logGifSend(where = gifSentWhereProp)
    }

    fun sendEditedMessage() {
        viewModelScope.launch { messageEditor.sendEditedMessage() }
    }

    fun clearMessageEditor() {
        messageEditor.finishEditingMessage()
        clearPhotosUseCase.invoke()
        editorMediaObserver?.let { photosSetLiveData.removeObserver(it) }
        editorTextObserver?.let { messageTextLiveData.removeObserver(it) }
    }

    fun getMessageEditorState() = messageEditor.liveState

    fun getUserUid() = getUserUidUseCase.invoke()

    fun logScreenForFragment(screenName: String) = fbAnalytic.logScreenForFragment(screenName)

    fun isMyMessage(message: MessageEntity): Boolean = getUserUidUseCase.invoke() == message.creator?.userId

    fun changeChatRequestMenuVisibility(isVisible: Boolean) =
        _liveIsChatRequestMenuVisible.postValue(isVisible)

    fun messageTextChanged(roomId: Long, text: String) {
        setMessageTextUseCase.invoke(roomId, text)
    }

    fun setUserName(userName: String) {
        setUserNameUseCase.invoke(userName)
    }

    fun clearMediaContent() {
        clearMediaContentUseCase.invoke()
    }

    fun allowChatRequestWithSendMessage(room: DialogEntity) {
        liveChatRequestViewEvent.postValue(ChatRequestViewEvent.AllowChat(room))
        chatRequestAvailabilityDb(room.roomId, DialogApproved.ALLOW.key)
        chatAnalyticDelegate.trackAllowChatRequest(room.companion.userId, true)
    }

    fun chatRequestAvailabilityDb(roomId: Long, approvedStatus: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            updateChatRequestApprovedStatusDbUseCase.execute(
                params = UpdateChatRequestApprovedStatusDbParams(roomId, approvedStatus),
                success = {
                    Timber.d("Success update chat request status into Db")
                },
                fail = { exception ->
                    Timber.e("Internal Db error when update room chat request status:$exception")
                }
            )
        }
    }

    fun updateMessagesAsChatRequest(roomId: Long?, isShowBlur: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            updateMessagesAsChatRequestUseCase.execute(
                params = UpdateMessagesAsChatRequestParams(
                    roomId = roomId,
                    isShowBlur = isShowBlur
                ),
                success = {
                    Timber.d("Success update some messages as chat request")
                },
                fail = { exception ->
                    Timber.e("Internal Db error when update messages as chat request:$exception")
                }
            )
        }
    }

    @Deprecated("Will be removed in upcoming releases")
    fun getFromCacheById(messageId: String): MessageEntity? {
        return mediaEntitiesCacheHelper.getFromCacheById(messageId)
    }

    fun chatRequestsAvailability(
        roomId: Long?,
        isAllow: Boolean,
        companionUid: Long? = null,
        withSendMessage: Boolean = false
    ) {
        roomId?.let { id ->
            viewModelScope.launch(Dispatchers.IO) {
                chatRequestAvailabilityUseCase.execute(
                    params = ChatRequestAvailabilityParams(
                        roomId = id,
                        approved = if (isAllow)
                            DialogApproved.ALLOW.key else DialogApproved.FORBIDDEN.key
                    ),
                    success = { room ->
                        when (room?.approved) {
                            DialogApproved.ALLOW.key -> {
                                Timber.e("DialogApproved.ALLOW.key ${DialogApproved.ALLOW.key}")
                                liveChatRequestViewEvent.postValue(
                                    ChatRequestViewEvent.AllowChat(
                                        room
                                    )
                                )
                                _liveIsChatRequestMenuVisible.postValue(false)
                                chatAnalyticDelegate.trackAllowChatRequest(companionUid, withSendMessage)
                            }

                            DialogApproved.FORBIDDEN.key -> {
                                Timber.e("DialogApproved.FORBIDDEN.key ${DialogApproved.FORBIDDEN.key}")
                                liveChatRequestViewEvent.postValue(
                                    ChatRequestViewEvent.ForbidChatRequest(
                                        room
                                    )
                                )
                                _liveIsChatRequestMenuVisible.postValue(false)
                                chatAnalyticDelegate.trackBanChatRequest(companionUid)
                            }
                        }
                    },
                    fail = { exception ->
                        Timber.e("Chat set Availability error:$exception")
                        liveChatRequestViewEvent.postValue(ChatRequestViewEvent.NetworkErrorChatRequest)
                    }
                )
            }
        }
    }

    //получаем кол-во показов подсказки
    fun isLetRecordAudioMessageTooltipWasShownTimes() =
        tooltipsInteractor.isNeedShowRecordAudioMessageTip(TooltipDuration.DEFAULT_TIMES)

    // увеличиваем на 1 кол-во показов до тех пор пока кол-во не достигнет 3х
    fun incRecordAudioMessageTooltipWasShown() = tooltipsInteractor.recordAudioMessageTipWasShowed(
        TooltipDuration.DEFAULT_TIMES
    )

    fun shouldShowPhoneAbilityTooltip() = tooltipsInteractor.isNeedShowPhoneAbilityTip(
        TooltipDuration.DEFAULT_TIMES
    )

    fun isReleaseButtonSentAudioMessageTooltipWasShownTimes() =
        tooltipsInteractor.isNeedShowReleaseButtonSentAudioMessageTip(TooltipDuration.DEFAULT_TIMES)

    fun incReleaseButtonSentAudioMessageTooltipWasShown() =
        tooltipsInteractor.releaseButtonSentAudioMessageTipWasShowed(TooltipDuration.DEFAULT_TIMES)

    fun deletePhotoClicked(photoUri: String) = if (isMessageEditActive())
        removePhotoUseCase.invoke(photoUri) else removePhotoFromMediaAttachmentCarouselUseCase.invoke(photoUri)

    fun getMediaType(uri: Uri?) = fileManager.getMediaType(uri)

    /**
     * Observe all event messages from Db
     */
    private fun observeEventMessages(roomId: Long?) {
        roomId?.let { id ->
            messagesInteractor.observeEventMessages(id)
                .distinctUntilChanged { old, new ->
                    old.lastOrNull()?.msgId == new.lastOrNull()?.msgId
                }
                .onEach { eventMessages ->
                    val messages = preProcessEventMessages(id, eventMessages)
                    handleEventMessages(messages)
                }
                .flowOn(Dispatchers.IO)
                .launchIn(viewModelScope)
        }
    }

    private fun selectMessagesSource(): Flow<List<MessageEntity>?> {
        return if (IS_MOCKED_DATA) {
            demoMessagesProvider.provideMessagesFlow()
        } else {
            paginationUtil.messagesFlow
        }
    }

    private fun setupChat(data: ChatInitData?) {
        if (data != null) {
            chatInitializer.init(data, viewModelScope)
        } else {
            Timber.e("Chat init data bundle parcelize error")
        }
    }

    private suspend fun preProcessEventMessages(
        roomId: Long,
        messages: List<MessageEntity>
    ): List<MessageEntity?> {
        return if (messages.isEmpty()) {
            val dialog = roomsInteractor.getRoomData(roomId)
            if (dialog == null) {
                mutableListOf<MessageEntity>()
            } else {
                mutableListOf(dialog.lastMessage?.toMessageEntity())
            }
        } else {
            val dialog = roomsInteractor.getRoomData(roomId)
            val lastMessage = if (dialog != null) dialog.lastMessage?.toMessageEntity() else null
            if (lastMessage?.eventCode == ChatEventEnum.ROOM_DELETED.state) {
                val list = mutableListOf<MessageEntity>()
                list.addAll(messages)
                list.add(lastMessage)
                return list
            }
            messages
        }
    }

    private suspend fun handleEventMessages(messages: List<MessageEntity?>) {
        messages.forEach { message ->
            if (message?.eventCode == ChatEventEnum.ROOM_DELETED.state) {
                withContext(Dispatchers.Main) {
                    _liveMessageViewEvent.value = ChatMessageViewEvent.OnEventDeleteRoom
                }
                wasCalledDeleteRoom = true
            }
        }
        if (wasCalledDeleteRoom) return

        wasRemovedFromRoom = (messages.isNotEmpty()
            && messages.last()?.eventCode == ChatEventEnum.REMOVED_FROM_GROUP_CHAT.state
            && messages.last()?.metadata?.userId == getUserUidUseCase.invoke())
        _liveMessageViewEvent.postValue(
            ChatMessageViewEvent.OnEventDeletedFromRoom(needToShow = wasRemovedFromRoom)
        )
    }


    fun subscribeClicked(dialog: DialogEntity?, userId: Long) {
        viewModelScope.launch {
            runCatching {
                subscriptionsUseCase.addSubscription(mutableListOf(userId))
            }.onFailure {
                Timber.e(it)
                _liveMessageViewEvent.postValue(ChatMessageViewEvent.OnFailSubscribeToUser)
            }.onSuccess { response ->
                dialog?.let { updateDialogUseCase.invoke(it) }
                if (response.data != null) {
                    _liveMessageViewEvent.postValue(ChatMessageViewEvent.OnSuccessSubscribedToUser)
                    val influencerProperty = createInfluencerAmplitudeProperty(
                        approved = dialog?.companion?.approved.toBoolean(),
                        topContentMaker = dialog?.companion?.topContentMaker.toBoolean()
                    )
                    chatAnalyticDelegate.followAction(
                        toId = userId,
                        amplitudeInfluencerProperty = influencerProperty
                    )
                } else {
                    _liveMessageViewEvent.postValue(ChatMessageViewEvent.OnFailSubscribeToUser)
                }
            }
        }
    }

    fun dismissSubscriptionClicked(userId: Long) {
        viewModelScope.launch {
            setSubsriptionDismissedUseCase(userId)
        }
    }

    private fun setBadgeUnreadDialog(roomId: Long?, needToShowBadge: Boolean) {
        viewModelScope.launch {
            runCatching {
                updateBadgeStatusUseCase.invoke(
                    roomId = roomId,
                    needToShowBadge = needToShowBadge
                )
            }.onFailure { Timber.e(it) }
        }
    }

    /**
     * When enter a room we should subscribe for receive room events (online, typing e.t.c)
     */
    fun subscribeRoom(roomId: Long?) {
        roomId?.let { id ->
            viewModelScope.launch {
                runCatching {
                    chatInteractor.subscribeRoom(roomId)
                }.onFailure { error ->
                    Timber.e("ERROR when subscribe room:$error")
                }
            }
        }
    }

    fun isInternetConnected(): Boolean {
        return networkStatusProvider.isInternetConnected()
    }

    /**
     * Start when onResume() fragment (first start method roomID = 0)
     * Check after rest messages and if exists remove last message from Db for trigger chat boundary callback
     */
    fun checkRestMessages(roomId: Long?) {
        Timber.e("Check Rest messages RoomID:$roomId")
        roomId?.let { id ->
            if (id > 0) {
                viewModelScope.launch(Dispatchers.IO) {
                    delay(3000)
                    runCatching {
                        val lastMessageUpdatedAt = messagesInteractor.getLastMessageUpdatedTime(id)
                        val serverMessages = messagesInteractor.getMessages(
                            roomId = id,
                            lastUpdatedAtMessages = lastMessageUpdatedAt,
                            direction = MessagePaginationDirection.AFTER
                        )
                        if (serverMessages.isNotEmpty()) {
                            _liveMessageViewEvent.postValue(
                                ChatMessageViewEvent.SetUnreadRestMessageCount(serverMessages.size.toLong())
                            )
                            removeUnreadMessagesDivider()
                            paginationUtil.resetPaginationFlag()
                            paginationUtil.requestRemainMessages()
                        }
                    }.onFailure { error ->
                        Timber.e(error.message)
                    }
                }
            } else {
                Timber.e("RoomID = 0")
            }
        } ?: let {
            Timber.e("ERR RoomID = 0 First entry to the APP")
        }
    }

    fun sendSimpleMessage(
        roomId: Long? = null,
        message: String,
        parentMessage: MessageEntity?,
        images: List<Uri>?,
        gifAspectRatio: Double,
        currentScrollPosition: Int,
        roomType: String,
        userId: Long?,
        favoriteRecent: MediakeyboardFavoriteRecentUiModel? = null,
        favoriteRecentType: MediaPreviewType? = null,
        mediaKeyboardCategory: AmplitudePropertyChatMediaKeyboardCategory = AmplitudePropertyChatMediaKeyboardCategory.NONE
    ) {
        viewModelScope.launch {
            val workId = chatWorkManagerDelegate.sendMessage(
                roomId = roomId,
                message = message,
                parentMessage = parentMessage,
                images = images,
                gifAspectRatio = gifAspectRatio,
                currentScrollPosition = currentScrollPosition,
                roomType = roomType,
                userId = userId,
                sendType = roomId?.let { SendMessageType.SIMPLE_MESSAGE_ROOM_ID }
                    ?: SendMessageType.SIMPLE_MESSAGE_USER_ID,
                favoriteRecent = favoriteRecent,
                favoriteRecentType = favoriteRecentType
            )
            chatAnalyticDelegate.logAmplitudeSendMessage(
                text = message,
                images = images,
                roomType = roomType,
                userId = userId ?: -1L,
                userChat = dialogEntity?.companion,
                msgId = workId.toString(),
                mediaKeyboardCategory = mediaKeyboardCategory
            )
            _liveMessageViewEvent.postValue(
                ChatMessageViewEvent.OnWorkSubmitted(workId)
            )
        }
    }

    fun sendVoiceMessage(
        roomId: Long? = null,
        parentMessage: MessageEntity?,
        audioPath: String,
        listOfAmplitudes: List<Int>,
        currentScrollPosition: Int,
        roomType: String,
        userId: Long?,
        durationSec: Long
    ) {
        viewModelScope.launch {
            val workId = chatWorkManagerDelegate.sendMessage(
                roomId = roomId,
                parentMessage = parentMessage,
                audioPath = audioPath,
                listOfAmplitudes = listOfAmplitudes,
                currentScrollPosition = currentScrollPosition,
                roomType = roomType,
                userId = userId,
                durationSec = durationSec,
                sendType = roomId?.let { SendMessageType.VOICE_MESSAGE_ROOM_ID }
                    ?: SendMessageType.VOICE_MESSAGE_USER_ID
            )
            chatAnalyticDelegate.logAmplitudeSendVoiceMessage(
                roomType = roomType,
                userId = userId ?: -1L,
                durationSec = durationSec,
                dialogEntity?.companion,
                msgId = workId.toString()
            )
            _liveMessageViewEvent.postValue(
                ChatMessageViewEvent.OnWorkSubmitted(workId)
            )
        }
    }

    fun sendVideoMessage(
        roomId: Long? = null,
        message: String,
        parentMessage: MessageEntity?,
        videoUri: Uri,
        userId: Long?,
        roomType: String
    ) {
        viewModelScope.launch {
            val workId = chatWorkManagerDelegate.sendMessage(
                roomId = roomId,
                message = message,
                parentMessage = parentMessage,
                videoPath = videoUri,
                roomType = roomType,
                userId = userId,
                sendType = roomId?.let { SendMessageType.VIDEO_MESSAGE_ROOM_ID }
                    ?: SendMessageType.VIDEO_MESSAGE_USER_ID,
            )
            logAmplitudeSendVideoMessage(
                text = message,
                roomType = ROOM_TYPE_DIALOG,
                userId = userId ?: -1L,
                msgId = String.empty()
            )
            _liveMessageViewEvent.postValue(
                ChatMessageViewEvent.OnWorkSubmitted(workId)
            )
        }

    }

    fun checkBirthdayText(newInputText: String, dateOfBirth: Long) {
        if (isSomeBodyHasBirthday(dateOfBirth)) {
            val spannableList = birthdayTextUtil.getBirthdayTextListRanges(
                birthdayText = newInputText
            )
            _liveMessageViewEvent.postValue(
                ChatMessageViewEvent.UpdateBirthdayTextSpannable(
                    listRanges = spannableList
                )
            )
        }
    }

    fun isSomeBodyHasBirthday(companionDateOfBirth: Long?): Boolean {
        return userBirthdayUtils.isBirthdayToday(getUserBirthdayUseCase.invoke())
            || userBirthdayUtils.isBirthdayToday(companionDateOfBirth)
    }

    private fun onUnsentMessageDelete(message: MessageEntity, roomType: String, companion: UserChat?) {
        if (!message.sent && message.isResendAvailable) {
            chatAnalyticDelegate.onDeletedUnsentMessageClicked(
                message = message,
                roomType = roomType,
                companion = companion
            )
        }
    }

    fun onResendMenuShowed() = chatAnalyticDelegate.onMessageResendMenuShowed()

    fun onMessageResendClicked(message: MessageEntity, roomType: String, companion: UserChat?) =
        chatAnalyticDelegate.onMessageResendClicked(message = message, roomType = roomType, companion = companion)

    fun logAmplitudeSendVideoMessage(text: String, roomType: String?, userId: Long, msgId: String) {
        chatAnalyticDelegate.logAmplitudeSendVideoMessage(
            text = text,
            roomType = roomType,
            userId = userId,
            userChat = dialogEntity?.companion,
            msgId = msgId
        )
    }

    /**
     * Старт нового чата
     * !!! Пока логируем только анонимный чат
     */
    fun logAmplitudeStartChat(dialog: DialogEntity?, chatStartedFromWhere: AmplitudePropertyChatCreatedFromWhere) {
        chatAnalyticDelegate.logAmplitudeStartChat(dialog = dialog, where = chatStartedFromWhere)
    }

    fun logAmplitudeGifButtonPress() = chatAnalyticDelegate.logChatGifButtonPress()

    fun forwardMessageClicked() = chatAnalyticDelegate.logForwardMessageClicked()

    fun resendAllMessagesClicked(messagesCount: Int) = chatAnalyticDelegate.onAllMessagesResend(messagesCount)

    private fun unsentCopyMessageClicked() = chatAnalyticDelegate.unsentMessageCopy()

    fun logSendGiftBack() = chatAnalyticDelegate.logSendGiftBack()

    fun logCommunityScreenOpened() = chatAnalyticDelegate.logCommunityScreenOpened()

    fun logChatOpen(chatType: AmplitudePropertyChatType, chatOpenedFromWhere: AmplitudePropertyWhere) {
        chatAnalyticDelegate.logChatOpen(chatType = chatType, openedFromWhere = chatOpenedFromWhere)
    }

    fun onSuccessSendMessageWorker(pathImageTemp: String?) {
        _liveMessageViewEvent.value = ChatMessageViewEvent.OnSuccessSentMessage
        deleteTempImageFile(pathImageTemp)
        reloadRecentStickers()
    }

    fun deserializeActionSendMessageWorkResult(jsonResult: String) {
        val event = sendMessageWorkResultMapper.mapActionSendMessageWorkResult(jsonResult)
        _liveMessageViewEvent.value = ChatMessageViewEvent.ActionSendMessage(
            messageId = event.messageId,
            isSentError = event.isSentError,
            resultMessage = null
        )
    }

    fun deserializeActionMessageInProgressWorkResult(jsonResult: String): CurrentInProgressMessage {
        val inProgressMessage = sendMessageWorkResultMapper.mapActionMessageInProgress(jsonResult)
        return CurrentInProgressMessage(
            messageId = inProgressMessage.messageId,
            workId = inProgressMessage.workId
        )
    }

    /**
     * Send fake simple text messages for testing
     * type -> #send 150   (send 150 messages)
     */
    private fun sendFakeMessages(roomId: Long?, messageText: String) {
        val messageCount = messageText.substringAfter(FAKE_MESSAGES_PREFIX).trim()
        runCatching {
            val take = messageCount.toLong()
            var count = 1
            tickerFlow(DELAY_BEFORE_SEND_FAKE_MESSAGE_MS.toDuration(DurationUnit.MILLISECONDS))
                .take(take.toInt())
                .onEach {
                    roomId?.let {
                        messagesInteractor.sendOnlyNetworkMessage(
                            roomId = roomId,
                            messageText = "$FAKE_MESSAGE_TEXT $count"
                        )
                    }
                    count++
                }
                .launchIn(viewModelScope)
        }.onFailure { Timber.e("Send fake message error:$it") }
    }

    /**
     * Update message status Read
     */
    fun updateMeeraMessageReadDb(roomId: Long?, message: MessageUiModel) {
        roomId?.let { id ->
            viewModelScope.launch(Dispatchers.IO) {
                val messageId = message.id

                messagesInteractor.readAndDecrementMessageUseCase(
                    roomId = roomId,
                    messageId = messageId
                )

                if (isInternetConnected()) {
                    trySendReadStatus(roomId, message.id)
                } else {
                    chatWorkManagerDelegate.sendReadMessageDoWork(id, message.id)
                }
            }
        }
    }

    private suspend fun trySendReadStatus(roomId: Long, messageId: String) {
        val isSuccessReadStatus = messagesInteractor.readMessageNetwork(
            roomId = roomId,
            messageIds = mutableListOf(messageId)
        )
        if (isSuccessReadStatus) {
            Timber.d("Message read status successfully send MessageID:${messageId}")
        } else {
            Timber.e("Message read status fail send MessageID:${messageId} Attempt send through worker")
            chatWorkManagerDelegate.sendReadMessageDoWork(roomId, messageId)
        }
    }

    /**
     * Count unread messages by Room
     */
    fun observeUnreadMessagesV2(roomId: Long?) {
        messagesInteractor.observeUnreadMessageCounter(roomId)
            .distinctUntilChanged()
            .filterNotNull()
            .onEach { count -> liveUnreadMessageCountRooms.postValue(count) }
            .launchIn(viewModelScope)
    }

    /** Enable chat for companion and show toast on error */
    fun enableChatClicked(userId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            enableChatUseCase.execute(
                params = EnableChatParams(userId),
                success = {
                    handleSuccessUnblockUser(userId)
                    pushChatButtonEnabled()
                },
                fail = { _liveMessageViewEvent.postValue(ChatMessageViewEvent.OnFailEnableChatMessages) }
            )
        }
    }

    private fun enableChat(companionUid: Long) {
        viewModelScope.launch {
            runCatching {
                enableChatUseCase.invoke(companionUid)
            }.onFailure {
                Timber.tag(this@MeeraChatViewModel.simpleName).e(it)
            }
        }
    }

    private fun disableChat(companionUid: Long) {
        viewModelScope.launch {
            runCatching {
                disableChatUseCase.invoke(companionUid)
            }.onFailure {
                Timber.tag(this@MeeraChatViewModel.simpleName).e(it)
            }
        }
    }

    private fun copyMessageTextContent(message: MessageEntity, messageView: View?) {
        val copyText = if (message.isValidForCopy()) {
            message.tagSpan?.text?.trim().orEmpty()
        } else if (message.attachment.type == TYPING_TYPE_AUDIO) {
            message.attachment.audioRecognizedText
        } else {
            String.empty()
        }

        if (copyText.isNotEmpty()) {
            context.copyToClipBoard(copyText) {
                sendEffect(ChatMessageViewEvent.OnCopyMessageContent(message, messageView))
            }
        }
    }

    /**
     * Type Enum["text", "audio]
     */
    fun sendTyping(roomId: Long?, type: String) {
        roomId?.let {
            viewModelScope.launch {
                runCatching {
                    val isSuccessSend = chatInteractor.sendTypingUseCase(roomId, type)
                    if (!isSuccessSend) Timber.e("Error send typing...")
                }.onFailure { error ->
                    Timber.e("Error send typing:$error")
                }
            }
        }
    }

    private fun removeMessage(message: MessageEntity?, isBoth: Boolean) {
        Timber.d("Remove message : $message isBoth: $isBoth")
        message?.let { msg ->
            // Delete unsent message (resend mode)
            if (!message.sent) {
                markMessageAsDeletedForUnsent(message)
            }
            viewModelScope.launch {
                runCatching {
                    val isSuccessDeleteMessage = messagesInteractor.deleteMessageNetwork(
                        roomId = msg.roomId,
                        messageId = msg.msgId,
                        isBoth = isBoth
                    )
                    if (!isSuccessDeleteMessage) {
                        _liveMessageViewEvent.postValue(ChatMessageViewEvent.ErrorRemoveMessage)
                    } else {
                        Timber.d("Message (${msg.msgId}) delete successfully!")
                    }
                }.onFailure { error ->
                    Timber.e("ERROR Delete message network:$error")
                    _liveMessageViewEvent.postValue(ChatMessageViewEvent.ErrorRemoveMessage)
                }
            }
        }
    }

    private fun removeOnlyNetworkMessage(roomId: Long, messageId: String) {
        viewModelScope.launch {
            runCatching {
                val isSuccessDeleteMessage = messagesInteractor.deleteMessageNetwork(
                    roomId = roomId,
                    messageId = messageId,
                    isBoth = true
                )
                Timber.d("SUCCESS remove only network messageId:$messageId result:$isSuccessDeleteMessage")
            }.onFailure {
                Timber.e("ERROR when remove only network message msgId:$messageId roomId:$roomId")
            }
        }
    }

    /**
     * Download voice message via coroutine
     */
    @Deprecated("Old download voice message")
    fun downloadVoiceMessage(
        message: MessageEntity,
        isNeedRefresh: Boolean = false,
        isTapDownloadButton: Boolean = false
    ) {
        runCatching {
            downloadVoiceMessageUseCase.invoke(
                message = message,
                externalFilesDir = context.getExternalFilesDir(null)
            )
                .catch { error ->
                    Timber.e("ERROR download voice message:$error")
                    if (isTapDownloadButton) refreshMessageItem(message)
                }
                .onEach { progress ->
                    if (isNeedRefresh && progress == DOWNLOAD_VOICE_MESSAGE_COMPLETE_PROGRESS) {
                        refreshMessageItem(message)
                    }
                }.launchIn(viewModelScope)
        }.onFailure { Timber.e(it) }
    }

    private var downloadVoiceMessageJob: Job? = null

    fun downloadVoiceMessage(
        model: MessageUiModel,
        isNeedRefresh: Boolean = false,
        isTapDownloadButton: Boolean = false
    ) {
        runCatching {
            val voiceMessageUrl = model.attachments?.attachments?.first()?.url ?: String.empty()
            downloadVoiceMessageJob = downloadVoiceMessageUseCase.invoke(
                url = voiceMessageUrl,
                roomId = model.roomId,
                externalFilesDir = context.getExternalFilesDir(null)
            ).catch { error ->
                if (isTapDownloadButton) refreshMessageItem(model.id)
            }
                .onEach { progress ->
                    if (isNeedRefresh && progress == DOWNLOAD_VOICE_MESSAGE_COMPLETE_PROGRESS) {
                        refreshMessageItem(model.id)
                    }
                }.launchIn(viewModelScope)
        }.onFailure { Timber.e(it) }
    }

    fun cancelDownloadVoiceMessage() {
        downloadVoiceMessageJob?.cancel()
    }

    private fun playMeeraVoiceMessages(currentMessage: MessageUiModel?, startAdapterPosition: Int) {
        viewModelScope.launch {
            currentMessage?.let { message ->
                val playMessagesQueue = getMeeraMessagesQueueToPlay(message, startAdapterPosition)
                _liveMessageViewEvent.postValue(
                    ChatMessageViewEvent.OnPlayMeeraVoiceMessage(startAdapterPosition, playMessagesQueue)
                )
            }
        }
    }

    private suspend fun getMeeraMessagesQueueToPlay(
        message: MessageUiModel,
        startAdapterPosition: Int
    ): LinkedList<PlayMeeraMessageDataModel> {
        val allMessagesForPlay = mutableListOf<MessageUiModel?>()
        allMessagesForPlay.add(message)
        val playMessagesQueue = LinkedList<PlayMeeraMessageDataModel>()

        val nextMessages = messagesInteractor.getNextMessages(
            roomId = message.roomId,
            createdAt = message.createdAt
        )

        nextMessages.forEach { msg ->
            if (msg != null) allMessagesForPlay.add(messageUiMapper.mapToMessageUi(msg))
        }

        run breaking@{
            allMessagesForPlay.forEachIndexed { index, msg ->
                if (msg != null) {
                    if (msg.messageType != MessageType.AUDIO) return@breaking
                    playMessagesQueue.add(
                        PlayMeeraMessageDataModel(
                            position = startAdapterPosition - index,
                            message = uiKitMessageMapper.mapToUiKitChatMessageData(
                                message = msg,
                                isGroupChat = dialogEntity?.type == ROOM_TYPE_GROUP
                            )
                        )
                    )
                }
            }
        }
        return playMessagesQueue
    }

    fun onVoiceMessageExpandTextState(message: MessageEntity, isExpanded: Boolean) {
        logAnalyticRecognitionExpandButtonTap(message, isExpanded)
    }

    fun collapseAllVoiceMessagesText(roomId: Long?) {
        viewModelScope.launch(Dispatchers.IO) {
            messagesInteractor.updateIsExpandedVoiceMessages(
                roomId = roomId,
                isExpanded = false
            )
        }
    }

    /**
     * Remove room for everyone users
     */
    private fun removeRoom(roomId: Long?, isBoth: Boolean, isGroupChat: Boolean = false) {
        roomId?.let {
            viewModelScope.launch {
                runCatching {
                    roomsInteractor.deleteRoomAndMessages(
                        roomId = roomId,
                        isBoth = isBoth
                    )
                }.onSuccess {
                    if (isGroupChat) chatAnalyticDelegate.logGroupChatDelete()
                    _liveMessageViewEvent.postValue(ChatMessageViewEvent.OnSuccessDeleteRoom)
                }.onFailure { error ->
                    Timber.e("ERROR: delete room: $error")
                    _liveMessageViewEvent.postValue(ChatMessageViewEvent.OnFailureDeleteRoom)
                }
            }
        }
    }

    fun observeGetMessages(roomId: Long?) {
        disposables.add(
            listenSocketStatusUseCase.invoke()
                .subscribe({ socketStatus ->
                    if (socketStatus is ConnectionStatus.OnChannelJoined
                        && socketStatus.isJoined
                        && !isStartedMessageObserver
                    ) {
                        getMessagesObserver(roomId)
                        isStartedMessageObserver = true
                    } else if (socketStatus is ConnectionStatus.OnChannelJoined
                        && !socketStatus.isJoined
                        && isStartedMessageObserver
                    ) {
                        disposableObserveMessage?.dispose()
                        isStartedMessageObserver = false
                    }
                }, { Timber.e(it) })
        )
    }

    private fun setRoomData(room: DialogEntity) {
        Timber.d("mapToUiKitChatMessageData_2; room: $room")
        dialogEntity = room
        isRoomChatRequest = room.isChatRequest()
        changeChatRequestMenuVisibility(isVisible = room.isChatRequest())
        room.isNotBlocked { getAndSetupDrafts(room) }
        setBadgeUnreadDialog(room.roomId, false)
    }

    private fun prepareRoomAfterUpdateRoomData(roomId: Long) {
        observeEventMessages(roomId)
        subscribeRoom(roomId)
        getUnsentMessages(roomId)
    }

    fun isWebSocketEnabled(): Boolean = checkSocketConnectionUseCase.invoke()

    private fun refreshMessageItem(message: MessageEntity?) {
        message?.let { msg ->
            viewModelScope.launch {
                messagesInteractor.refreshMessage(
                    roomId = msg.roomId,
                    messageId = msg.msgId
                )
            }
        }
    }

    private fun refreshMessageItem(messageId: String) {
        viewModelScope.launch {
            messagesInteractor.refreshMessage(messageId)
        }
    }

    /**
     * Refresh first message for rebind
     */
    @Suppress("LocalVariableName")
    fun refreshFirstMessage(roomId: Long?) {
        if (roomId == null) return
        viewModelScope.launch {
            runCatching {
                delay(500)
                messagesInteractor.refreshFirstMessage(roomId)
            }.onFailure {
                Timber.e("ERROR When refresh First message in chat (roomID:$roomId) Error:$it")
            }
        }
    }

    /**
     * Remove unread message dividers everything
     */
    fun removeUnreadMessagesDivider() {
        viewModelScope.launch {
            messagesInteractor.removeUnreadDivider()
        }
    }

    fun scrollWithRefreshIfInternetConnected() {
        if (isInternetConnected()) {
            paginationUtil.scrollDownWithRefresh()
        }
    }

    fun removeAllMessages() {
        paginationUtil.scrollDownWithRefresh()
    }

    /**
     * Mark all messages in room as read
     */
    fun markRoomAsRead(roomId: Long?) {
        roomId?.let {
            viewModelScope.launch(Dispatchers.IO) {
                messagesInteractor.updateRoomAsRead(roomId)
                try {
                    val response = markRoomAsReadUseCase.markRoomAsRead(mutableListOf(roomId))
                    if (response.data != null) {
                        Timber.d("SUCCESS mark room:$roomId as read!")
                    } else {
                        Timber.e("SERVER ERROR: mark room:$roomId as read")
                    }
                } catch (e: Exception) {
                    Timber.e("ERROR: mark room as read:${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    fun stopVoiceMessagesBd(roomId: Long?) {
        roomId?.let { idRoom ->
            viewModelScope.launch {
                messagesInteractor.updateVoiceMessageAsStopped(idRoom)
            }
        }
    }

    // Update into Dialog - firstUnreadMessageTs field
    fun updateFirstUnreadMessageTs(roomId: Long?) {
        roomId?.let { idRoom ->
            viewModelScope.launch(Dispatchers.IO) {
                runCatching {
                    val rooms = roomsInteractor.getRooms(type = RoomTimeType.ROOMS_MAX_UPDATE)
                    val currentRoom = rooms.find { room -> room.roomId == idRoom }
                    if (rooms.isNotEmpty() && currentRoom != null) {
                        _liveMessageViewEvent.postValue(
                            ChatMessageViewEvent.OnRefreshRoomData(
                                currentRoom
                            )
                        )
                        messagesInteractor.updateLastUnreadMessageTs(
                            roomId = idRoom,
                            timestamp = currentRoom.firstUnreadMessageTs
                        )
                    } else {
                        messagesInteractor.updateLastUnreadMessageTs(roomId = idRoom, timestamp = 0L)
                    }
                }.onFailure {
                    Timber.e(it)
                    messagesInteractor.updateLastUnreadMessageTs(roomId = idRoom, timestamp = 0L)
                }
            }
        }
    }

    fun getSavedDateOfBirth() = getUserBirthdayUseCase.invoke()

    fun scrollToMessage(messageId: String) {
        sendEffect(ChatMessageViewEvent.OnScrollToMessage(messageId))
    }

    @Suppress("LocalVariableName")
    fun handleReplyMessage(messageId: String? = null, message: MessageEntity? = null) {
        viewModelScope.launch {
            var _message = message
            if (_message == null) {
                _message = mediaEntitiesCacheHelper.getFromCacheById(messageId ?: return@launch) ?: return@launch
            }
            sendEffect(ChatMessageViewEvent.OnMessageReply(_message))
            sendEffect(
                ChatMessageViewEvent.ShowMessageReplyTooltip(
                    messageUiMapper.mapToMessagesUi(listOf(_message)).first()
                )
            )
        }
    }

    fun scrollToReplyParentMessage(messageId: String) {
        viewModelScope.launch {
            runCatching {
                val message = getMessageByIdUsecase.invoke(messageId) ?: return@launch
                getMessageByIdUsecase.invoke(message.parentMessage?.parentId ?: return@launch)
            }
                .onFailure(Timber::e)
                .onSuccess { message -> scrollToMessage(message?.msgId ?: return@launch) }
        }
    }

    fun getUnsentMessages(roomId: Long?) {
        roomId?.let { _ ->
            viewModelScope.launch {
                runCatching {
                    messagesInteractor.getUnsentMessageCount(roomId)
                }.onSuccess { unsentMessageCount ->
                    _liveMessageViewEvent.postValue(
                        ChatMessageViewEvent.OnSetUnsentMessages(
                            unsentMessageCount
                        )
                    )
                }
            }
        } ?: kotlin.run { Timber.e("ERROR roomId is NULL") }
    }

    fun mediaKeyboardMessageLongClicked(
        messageId: String?,
        view: View? = null,
        voiceRecognizedText: String? = null
    ) {
        viewModelScope.launch {
            val message = getMessageByIdUsecase.invoke(messageId.orEmpty())
            if (message?.isMediaMessage() != true && message?.isStickerMessage() != true) {
                emitMediaKeyboardEvent(
                    ChatMediaKeyboardEvent.ShowMediaPreview(
                        message = message,
                        view = view,
                        bottomMenuPayload = ChatBottomMenuPayload(
                            voiceRecognizedText = voiceRecognizedText
                        )
                    )
                )
                return@launch
            }
            val favoriteForMessage =
                currentMediaFavorites.firstOrNull {
                    it.url == message.attachment.url ||
                        it.lottieUrl == message.attachment.url
                }
            val mediaUiModel = when (message.attachment.type) {
                TYPING_TYPE_GIF -> MediaUiModel.GifMediaUiModel(
                    mediaId = favoriteForMessage?.id,
                    url = message.attachment.url,
                    preview = message.attachment.url,
                    ratio = (message.attachment.metadata[METADATA_RATIO_KEY] as? Double?)?.toFloat()
                )

                TYPING_TYPE_VIDEO -> MediaUiModel.VideoMediaUiModel(
                    mediaId = favoriteForMessage?.id,
                    url = message.attachment.url,
                    preview = message.attachment.metadata[METADATA_PREVIEW_KEY]?.toString(),
                    duration = (message.attachment.metadata[METADATA_DURATION_KEY] as? Double?)?.roundToInt()
                )

                TYPING_TYPE_IMAGE -> MediaUiModel.ImageMediaUiModel(
                    mediaId = favoriteForMessage?.id,
                    url = message.attachment.url
                )

                TYPING_TYPE_STICKER -> MediaUiModel.StickerMediaUiModel(
                    favoriteId = favoriteForMessage?.id,
                    stickerUrl = message.attachment.url,
                    lottieUrl = message.attachment.lottieUrl,
                    webpUrl = message.attachment.webpUrl,
                    messageId = message.msgId,
                    stickerId = getStickerIdFromUrl(message.attachment.url)
                )

                else -> null
            }
            if (mediaUiModel == null) {
                emitMediaKeyboardEvent(
                    ChatMediaKeyboardEvent.ShowMediaPreview(
                        message = message,
                        view = view
                    )
                )
                return@launch
            }
            val mediaPreviewUiModel = MediaPreviewUiModel(
                media = mediaUiModel,
                type = MediaPreviewType.FAVORITE,
                isAdded = favoriteForMessage != null
            )
            emitMediaKeyboardEvent(
                ChatMediaKeyboardEvent.ShowMediaPreview(
                    mediaPreview = mediaPreviewUiModel,
                    message = message,
                    view = view
                )
            )
        }
    }

    fun mediaKeyboardGifLongClicked(id: String, preview: String, url: String, ratio: Double) {
        val media = MediaUiModel.GifMediaUiModel(
            mediaId = currentMediaFavorites.firstOrNull { it.url == url }?.id,
            gifId = id,
            preview = preview,
            url = url,
            ratio = ratio.toFloat()
        )
        val mediaPreview = MediaPreviewUiModel(
            media = media,
            type = MediaPreviewType.GIPHY,
            isAdded = currentMediaFavorites.any { it.url == url }
        )
        emitMediaKeyboardEvent(ChatMediaKeyboardEvent.ShowMediaPreview(mediaPreview))
    }

    fun mediaKeyboardStickerLongClicked(sticker: MediaKeyboardStickerUiModel) {
        val media = MediaUiModel.StickerMediaUiModel(
            favoriteId = currentMediaFavorites.firstOrNull { it.url == sticker.url }?.id,
            stickerId = sticker.id,
            stickerUrl = sticker.url,
            lottieUrl = sticker.lottieUrl,
            stickerPackTitle = sticker.stickerPackTitle
        )
        val mediaPreview = MediaPreviewUiModel(
            media = media,
            type = MediaPreviewType.STICKER,
            isAdded = currentMediaFavorites.any { it.url == sticker.url }
        )
        emitMediaKeyboardEvent(ChatMediaKeyboardEvent.ShowMediaPreview(mediaPreview))
    }

    private fun mediaKeyboardFavoriteRecentLongClicked(
        favoriteRecent: MediakeyboardFavoriteRecentUiModel,
        type: MediaPreviewType,
        deleteRecentListener: (Int) -> Unit
    ) {
        val mediaUiModel = MediaUiModel.fromMediakeyboardFavoriteRecentUiModel(
            favoriteRecent,
            dialogEntity?.roomId
        )
        val mediaPreviewUiModel = MediaPreviewUiModel(
            media = mediaUiModel,
            type = type,
            isAdded = currentMediaFavorites.any { it.url == favoriteRecent.url },
            favoriteRecentModel = favoriteRecent
        )
        emitMediaKeyboardEvent(
            ChatMediaKeyboardEvent.ShowMediaPreview(
                mediaPreview = mediaPreviewUiModel,
                deleteRecentClickListener = deleteRecentListener
            )
        )
    }

    fun addToFavoritesFromMessage(message: MessageEntity, position: Int? = null) {
        val model: MediaUiModel?
        val isGifMessage = message.attachment.type == TYPING_TYPE_GIF
        val isImageMessage =
            message.attachment.type == TYPING_TYPE_IMAGE ||
                (position != null && message.attachments[position].type == TYPING_TYPE_IMAGE)
        val isVideoMessage =
            message.attachment.type == TYPING_TYPE_VIDEO ||
                (position != null && message.attachments[position].type == TYPING_TYPE_VIDEO)
        val isStickerMessage = message.attachment.type == TYPING_TYPE_STICKER
        val mediaUrl = when (position) {
            null -> message.attachment.url
            else -> message.attachments[position].url
        }
        when {
            isGifMessage -> {
                val gifId = message.attachment.url.replace(".*/([^/?]+).*".toRegex(), "$1");
                model = MediaUiModel.GifMediaUiModel(
                    preview = message.attachment.url,
                    url = message.attachment.url,
                    gifId = gifId,
                    ratio = (message.attachment.metadata[METADATA_RATIO_KEY] as? Double?)?.toFloat(),
                    roomId = message.roomId,
                    messageId = message.msgId
                )
            }

            isImageMessage -> {
                model = MediaUiModel.ImageMediaUiModel(
                    url = message.attachment.url,
                    roomId = message.roomId,
                    messageId = message.msgId,
                    attachmentIndex = position
                )
            }

            isVideoMessage -> {
                model = MediaUiModel.VideoMediaUiModel(
                    url = message.attachment.url,
                    preview = message.attachment.metadata[METADATA_PREVIEW_KEY]?.toString(),
                    roomId = message.roomId,
                    messageId = message.msgId,
                    attachmentIndex = position
                )
            }

            isStickerMessage -> {
                model = MediaUiModel.StickerMediaUiModel(
                    stickerUrl = message.attachment.url,
                    roomId = message.roomId,
                    messageId = message.msgId
                )
            }

            else -> {
                model = null
            }
        }
        val isAttachmentGifFromGiphy = message.attachment.isGifFromGiphy
        logAddToFavorites(
            whereProperty = AmplitudeMediaKeyboardFavoriteWhereProperty.CHAT_SCREEN,
            mediaTypeProperty = when {
                isGifMessage && isAttachmentGifFromGiphy -> AmplitudeMediaKeyboardMediaTypeProperty.GIF_GIPHY
                isGifMessage -> AmplitudeMediaKeyboardMediaTypeProperty.GIF_GALLERY
                isStickerMessage -> AmplitudeMediaKeyboardMediaTypeProperty.STICKER
                isImageMessage -> AmplitudeMediaKeyboardMediaTypeProperty.PHOTO
                isVideoMessage -> AmplitudeMediaKeyboardMediaTypeProperty.VIDEO
                else -> return
            },
            stickerId = message.attachment.id?.toInt(),
            stickerCategory = getStickerCategoryFromStickerId(message.attachment.id?.toInt())
        )
        model?.let { addToFavorites(it, mediaUrl) }
    }

    private fun addToFavorites(media: MediaUiModel, mediaUrl: String) {
        media.toAddFavoriteBody()?.let { body ->
            viewModelScope.launch {
                runCatching {
                    addToMediaKeyboardFavoritesUseCase.invoke(body)
                }.onFailure {
                    Timber.e(it)
                }.onSuccess {
                    loadMoreFavorites()
                    _liveMessageViewEvent.postValue(ChatMessageViewEvent.OnAddedToFavorites(mediaUrl))
                }
            }
        }
    }

    private fun logAddToFavorites(
        whereProperty: AmplitudeMediaKeyboardFavoriteWhereProperty,
        mediaTypeProperty: AmplitudeMediaKeyboardMediaTypeProperty,
        stickerCategory: String? = null,
        stickerId: Int? = null
    ) {
        amplitudeMediaKeyboardAnalytic.logAddFavorite(
            where = whereProperty,
            mediaTypeProperty = mediaTypeProperty,
            userId = getUserUid(),
            stickerCategory = stickerCategory,
            stickerId = stickerId
        )
    }

    private fun logRemoveFromFavorites(
        whereProperty: AmplitudeMediaKeyboardFavoriteWhereProperty,
        mediaTypeProperty: AmplitudeMediaKeyboardMediaTypeProperty,
        stickerCategory: String? = null,
        stickerId: Int? = null
    ) {
        amplitudeMediaKeyboardAnalytic.logDeleteFavorite(
            where = whereProperty,
            mediaTypeProperty = mediaTypeProperty,
            userId = getUserUid(),
            stickerCategory = stickerCategory,
            stickerId = stickerId
        )
    }

    fun removeFromFavoritesByUrl(url: String) {
        currentMediaFavorites.firstOrNull { it.url == url }?.let { favorite ->
            viewModelScope.launch {
                runCatching {
                    deleteMediaKeyboardFavoriteUseCase.invoke(favoriteId = favorite.id)
                }.onFailure {
                    Timber.e(it)
                }.onSuccess {
                    loadMoreFavorites()
                }
            }
        }
    }

    private fun removeFromFavorites(mediaPreview: MediaPreviewUiModel) {
        val media = mediaPreview.media
        val favoriteId: Int?
        val mediaUrl: String
        when (media) {
            is MediaUiModel.GifMediaUiModel -> {
                mediaUrl = media.url
                favoriteId = currentMediaFavorites.firstOrNull { it.url == media.url }?.id ?: media.id
            }

            is MediaUiModel.ImageMediaUiModel -> {
                mediaUrl = media.url
                favoriteId = currentMediaFavorites.firstOrNull { it.url == media.url }?.id ?: media.id
            }

            is MediaUiModel.StickerMediaUiModel -> {
                mediaUrl = media.stickerUrl
                favoriteId = currentMediaFavorites.firstOrNull { it.url == media.stickerUrl }?.id ?: media.id
            }

            is MediaUiModel.VideoMediaUiModel -> {
                mediaUrl = media.url
                favoriteId = currentMediaFavorites.firstOrNull { it.url == media.url }?.id ?: media.id
            }
        }
        favoriteId?.let { id ->
            viewModelScope.launch {
                runCatching {
                    deleteMediaKeyboardFavoriteUseCase.invoke(id)
                }.onFailure {
                    Timber.e(it)
                }.onSuccess {
                    loadMoreFavorites()
                }
            }
        }
        val amplitudeProperties = getWhereAndMediaTypePropertiesFromMediaPreview(mediaUrl, mediaPreview)
        val whereProperty = amplitudeProperties.first
        val mediaTypeProperty = amplitudeProperties.second
        if (whereProperty == null || mediaTypeProperty == null) return
        logRemoveFromFavorites(
            whereProperty = whereProperty,
            mediaTypeProperty = mediaTypeProperty,
            stickerId = getStickerIdFromMediaPreview(mediaPreview),
            stickerCategory = getStickerCategoryFromMediaPreview(mediaPreview)
        )
    }

    fun resendMessage(type: ResendType) = chatWorkManagerDelegate.resendMessages(type)

    fun setNoMediaPlaceholderMessage(messageId: String) {
        viewModelScope.launch {
            val message = getMessageByIdUsecase.invoke(messageId) ?: return@launch
            message.itemType = ITEM_TYPE_NO_MEDIA_PLACEHOLDER_SEND
            message.attachment.url = MessageAttachment.EMPTY_URL
            messagesInteractor.updateMessage(message)
        }
    }

    private fun blockUser(companionUid: Long) {
        viewModelScope.launch {
            _liveMessageViewEvent.postValue(
                ChatMessageViewEvent.BlockUserResult(isSuccess = blockUserInternal(companionUid))
            )
        }
    }

    fun createPendingBlockReportResult(companionUid: Long) {
        currentBlockReportResult = ChatBlockReportResultUiModel()
        currentBlockJob = viewModelScope.launch {
            currentBlockReportResult?.isBlockSuccess = blockUserInternal(companionUid)
        }
    }

    fun setPendingReportResult(result: ComplaintFlowResult) {
        viewModelScope.launch {
            currentBlockReportResult?.reportResult = result
            currentBlockJob?.join()
            currentBlockJob = null
            tryPostBlockReportResult()
        }
    }

    fun unblockUser(userId: Long?) {
        val companionUid = userId ?: return
        val myId = getUserUidUseCase.invoke()
        viewModelScope.launch {
            runCatching {
                blockStatusUseCase.invoke(
                    userId = myId,
                    companionId = companionUid,
                    isBlocked = false
                )
            }.onSuccess {
                handleSuccessUnblockUser(userId)
                updateCompanionInfo(companionUid)
                chatAnalyticDelegate.logUnlockChat(from = myId, to = userId)
            }.onFailure { exception ->
                Timber.e(exception)
            }
        }
    }

    fun disableChatRequestImageBlur(messageId: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val message = getMessageByIdUsecase.invoke(messageId.orEmpty())
            disableChatRequestImageBlurUseCase.execute(
                params = DisableChatRequestImageBlurParams(message),
                success = {
                    chatAnalyticDelegate.trackMessageUnBlur(message)
                },
                fail = { exception ->
                    Timber.e("Internal Db error when update image blur:$exception")
                }
            )
        }
    }

    fun sendGreetingClicked(userId: Long, stickerId: Int?) {
        viewModelScope.launch {
            sendGreetingUseCase.execute(
                params = SendGreetingUseCaseParams(userId, stickerId),
                success = { Timber.d("Greeting successfully send:$it") },
                fail = { Timber.e(it) }
            )
        }
    }

    private fun loadListMessagesDb(room: DialogEntity) {
        paginationUtil.initRoomPagination(
            roomId = room.roomId,
            unreadMessageTimeStamp = room.firstUnreadMessageTs,
            isChatRoomRequest = isRoomChatRequest,
            eventsFlow = sendMessageEvents
        )
    }

    fun showOrHideMediaKeyboardButtons(isNeedShow: Boolean) {
        viewModelScope.launch {
            showOrHideMediaButtonsUseCase.invoke(isNeedShow)
        }
    }

    fun updateMediaKeyboardLatestState(behavior: Int, ordinal: Int) {
        latestMediaKeyboardBehavior.value = MediaKeyboardState(behavior, ordinal)
    }


    fun deleteDraft(roomId: Long?) {
        viewModelScope.launch {
            runCatching {
                val id = roomId ?: return@launch
                deleteDraftUseCase.invoke(id)
                getDialogSuspend(id)?.let {
                    updateDialogUseCase.invoke(it.apply { lastMessageUpdatedAt = lastMessage?.createdAt ?: 0 })
                }
            }
        }
    }

    fun saveDraft(userId: Long?, roomId: Long?, reply: MessageEntity?, text: String?) {
        if (isMessageEditActive()) return
        saveDraftJob?.cancel()
        saveDraftJob = CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
            runCatching {
                findAndSaveNewDraft(userId, roomId, reply, text)
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    fun disableShake() {
        viewModelScope.launch {
            setRegistrationShakeEventUseCase.invoke(false)
        }
    }

    fun enableShake() {
        viewModelScope.launch {
            setRegistrationShakeEventUseCase.invoke(true)
        }
    }

    fun changeMessageProgress(messageId: String, isInProgress: Boolean) {
        viewModelScope.launch {
            changeMessageEditingStatusUseCase.invoke(messageId = messageId, isEditing = isInProgress)
        }
    }

    fun markPostAsNotSensitiveForUser(postId: Long?) {
        markPostAsNotSensitiveForUserUseCase.invoke(
            params = MarkPostAsNotSensitiveForUserParams(
                postId = postId,
                parentPostId = null
            )
        )
        triggerMessagesRemap()
    }

    private fun triggerMessagesRemap() {
        _remapMessages.value = !_remapMessages.value
    }

    fun showMeeraCompletelyRemoveMessageDialog(messageId: String) {
        viewModelScope.launch {
            val message = getMessageByIdUsecase.invoke(messageId) ?: return@launch
            _liveMessageViewEvent.postValue(
                ChatMessageViewEvent.ShowMeeraCompletelyRemoveMessageDialog(message)
            )
        }
    }

    /**
     * Переотправить заново сообщения, которые находятся в статусе
     * текущей переотправки
     */
    private fun inProgressResendSendMessage(roomId: Long?) = viewModelScope.launch {
        if (isInternetConnected()) {
            roomId?.let {
                runCatching {
                    messagesInteractor.getResendProgressMessage(roomId)?.let { resendProgressMessages ->
                        resendProgressMessages.forEach { message ->
                            val type = ResendType.ResendByMessageId(message.msgId, roomId)
                            resendMessage(type)
                        }
                    }
                }
            }
        }
    }

    /**
     * Окончательное удаление сообщения из БД
     */
    private fun completelyRemoveMessage(roomId: Long, messageId: String) {
        viewModelScope.launch {
            runCatching {
                deleteMessageByIdUseCase.invoke(
                    roomId = roomId,
                    messageId = messageId
                )
            }.onFailure { Timber.e(it) }
        }
    }

    private suspend fun saveFavoritesToDb(favorites: List<MediakeyboardFavoriteRecentModel>) {
        saveFavoritesToDbUseCase.invoke(favorites)
    }

    private fun loadMoreFavorites(startId: Int? = null) {
        viewModelScope.launch {
            val favorites = runCatching { loadFavoritesUseCase.invoke(startId) }.onFailure {
                Timber.e(
                    it
                )
            }
                .getOrDefault(emptyList())
            saveFavoritesToDb(favorites)
            if (favorites.isEmpty() || favorites.size < FAVORITES_PAGE_SIZE) {
                return@launch
            } else {
                loadMoreFavorites(favorites.last().id)
            }
        }
    }

    private fun loadGreetingSticker() {
        viewModelScope.launch {
            val greetingSticker = runCatching {
                stickersMapper.mapStickerDomainToUiModel(
                    getGreetingStickerUseCase.invoke() ?: return@runCatching null
                )
            }.getOrNull()

            _liveMessageViewEvent.value =
                ChatMessageViewEvent.OnGreetingStickerFound(greetingSticker)
        }
    }

    private suspend fun updateCompanionInfo(companionUid: Long) {
        val companionInfo = runCatching { getChatUserInfoUseCase.invoke(companionUid) }
            .onFailure { Timber.e(it) }
            .getOrNull()
        companionInfo?.let {
            _liveMessageViewEvent.postValue(
                ChatMessageViewEvent.OnUpdateCompanionInfo(it)
            )
        }
    }

    private fun tryPostBlockReportResult() {
        val isBlockSuccess = currentBlockReportResult?.isBlockSuccess ?: return
        val reportResult = currentBlockReportResult?.reportResult ?: return
        _liveMessageViewEvent.postValue(
            ChatMessageViewEvent.BlockReportResult(
                isBlockSuccess = isBlockSuccess,
                reportResult = reportResult
            )
        )
        currentBlockReportResult = null
    }

    private suspend fun blockUserInternal(companionUid: Long): Boolean {
        val myId = getUserUidUseCase.invoke()
        return runCatching {
            blockStatusUseCase.invoke(
                userId = myId,
                companionId = companionUid,
                isBlocked = true
            )
        }.onSuccess {
            updateCompanionInfo(companionUid)
            chatAnalyticDelegate.onBlockedUser(userId = myId, blockedUserId = companionUid)
        }.onFailure { exception ->
            Timber.e(exception)
        }.fold(
            onSuccess = { true },
            onFailure = { false }
        )
    }

    private suspend fun findAndSaveNewDraft(userId: Long?, roomId: Long?, reply: MessageEntity?, text: String?) {
        val id = roomId ?: return
        val currentDraft = findCurrentDraft(roomId, userId)
        val timeStamp = System.currentTimeMillis()
        if (reply == null && text.isNullOrBlank()) {
            deleteDraftUseCase.invoke(id)
            getDialogSuspend(id)?.let {
                updateDialogUseCase.invoke(it.apply { lastMessageUpdatedAt = lastMessage?.createdAt ?: 0 })
            }
        } else if (currentDraft == null || currentDraft.text != text || currentDraft.reply != reply) {
            addDraftUseCase.invoke(
                DraftModel(
                    roomId = id,
                    userId = userId,
                    timeStamp,
                    reply = reply,
                    text = text,
                    draftId = currentDraft?.draftId
                )
            )
            getDialogSuspend(id)?.let {
                updateDialogUseCase.invoke(it.apply { lastMessageUpdatedAt = timeStamp })
            }
        }
    }

    private fun deleteTempImageFile(filePath: String?) {
        filePath?.let {
            doAsyncViewModel({
                try {
                    val extension = filePath.substring(filePath.lastIndexOf("."))
                    Timber.d("Temp image file = $filePath extension: $extension")
                    if (extension != ".gif")
                        return@doAsyncViewModel fileManager.deleteFile(it)
                    else
                        return@doAsyncViewModel false
                } catch (e: Exception) {
                    Timber.e(e)
                    return@doAsyncViewModel false
                }
            }, { isDeleted ->
                Timber.d("Temp image file isDeleted: $isDeleted")
            })
        }
    }

    /**
     * Помечаем неотправленное сообщение как удалённое, когда недоступен интернет
     * и мы не можем получить ответ от сервера
     */
    private fun markMessageAsDeletedForUnsent(message: MessageEntity) {
        viewModelScope.launch {
            messagesInteractor.deleteUnsentMessage(message)
            val unsentMessageCount = messagesInteractor.countAllUnsentMessages(message.roomId)
            _liveMessageViewEvent.postValue(
                ChatMessageViewEvent.OnSetUnsentMessages(
                    unsentMessageCount
                )
            )
        }
    }

    private fun logAnalyticRecognitionExpandButtonTap(message: MessageEntity, isExpanded: Boolean) {
        chatAnalyticDelegate.logVoiceMessageRecognitionTap(message = message, isExpanded = isExpanded)
    }

    private fun getAndSetupDrafts(room: DialogEntity) {
        viewModelScope.launch {
            val currentDraft = findCurrentDraft(room.roomId, room.companion.userId) ?: return@launch
            _liveMessageViewEvent.value = ChatMessageViewEvent.OnDraftFound(currentDraft)
        }
    }

    private fun getAndSetupStickers() {
        getStickersJob?.cancel()
        getStickersJob = viewModelScope.launch {
            runCatching {
                reloadAllStickersInFlow.invoke()
            }.onFailure { Timber.d(it) }
        }
    }

    fun onStickerPackViewed(stickerPack: MediaKeyboardStickerPackUiModel) {
        viewModelScope.launch {
            runCatching {
                setStickerPackViewedUseCase.invoke(stickerPack.id)
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    private suspend fun findCurrentDraft(roomId: Long?, userId: Long?): DraftUiModel? {
        val id = roomId ?: return null
        val drafts = runCatching {
            getAllDraftsUseCase.invoke().map(draftsMapper::mapDomainToUiModel)
        }.getOrDefault(emptyList())
        return drafts.firstOrNull {
            val roomIdSame = it.roomId != null && it.roomId != 0L && it.roomId == id
            val userIdSame = it.userId != null && it.userId != 0L && it.userId == userId
            return@firstOrNull roomIdSame || userIdSame
        }
    }

    private fun getMessagesObserver(roomId: Long?) {
        messagesInteractor.observeIncomingMessage()
            .onEach { incomeMessage ->
                val messageExists = messagesInteractor.getMessageById(incomeMessage.msgId)
                if (messageExists?.isIncomingRecognizedVoiceMessage() == true) return@onEach

                if (messageExists != null) {
                    saveMessageSomeFieldsBeforeInsertDb(incomeMessage, messageExists)
                } else {
                    playIncomeMessageSound(roomId, incomeMessage)
                }
                handleIncomeMessageBeforeSaveDb(incomeMessage)
            }
            .catch { e -> Timber.e(e) }
            .launchIn(viewModelScope)
    }

    private fun MessageEntity.isIncomingRecognizedVoiceMessage() =
        itemType == ITEM_TYPE_AUDIO_RECEIVE
            && attachment.audioRecognizedText.isNotEmpty()

    private fun saveMessageSomeFieldsBeforeInsertDb(
        incomeMessage: MessageEntity,
        messageExists: MessageEntity
    ) {
        incomeMessage.isShowUnreadDivider = messageExists.isShowUnreadDivider
    }

    private fun playIncomeMessageSound(roomId: Long?, incomeMessage: MessageEntity) {
        if (
            isMyMessage(incomeMessage).not()
            && roomId == incomeMessage.roomId
            && !receivedMessages.contains(incomeMessage.msgId)
        ) {
            receivedMessages.add(incomeMessage.msgId)
            _liveMessageViewEvent.postValue(ChatMessageViewEvent.PlayReceivedMessage)
        }
    }

    private fun handleIncomeMessageBeforeSaveDb(message: MessageEntity) {
        setExpandedFlagWhenRecognizedVoiceMessage(message)
        viewModelScope.launch {
            when {
                message.isMy() ||
                    message.isDeletedOrTypeEvent() ||
                    message.isEdited() ||
                    message.isMomentDeleted() ||
                    message.isCommunityDeleted() -> {
                    insertMessageIntoDb(message)
                }

                message.isUnreadYet() ||
                    message.isRecognizedVoiceMessage() -> {
                    saveMessageToUniqueSetAndSaveIntoDb(message)
                }

                else -> {
                    Timber.e("It message [${message.msgId}] have other condition")
                }
            }
        }
    }

    private fun setExpandedFlagWhenRecognizedVoiceMessage(message: MessageEntity) {
        if (message.attachment.audioRecognizedText.isNotEmpty()) {
            message.isExpandedRecognizedText = false
        }
    }

    private fun MessageEntity.isUnreadYet(): Boolean =
        delivered && sent && (!readed || isCallInfo())

    private fun MessageEntity.isCallInfo(): Boolean =
        eventCode == ChatEventEnum.CALL.state

    private fun MessageEntity.isDeletedOrTypeEvent(): Boolean =
        deleted || type == CHAT_ITEM_TYPE_EVENT

    private fun MessageEntity.isNotVoiceMessage(block: () -> Unit) {
        if (attachment.type != TYPING_TYPE_AUDIO) block()
    }

    private fun MessageEntity.isRecognizedVoiceMessage(): Boolean =
        attachment.type == TYPING_TYPE_AUDIO && attachment.audioRecognizedText.isNotEmpty()

    private suspend fun saveMessageToUniqueSetAndSaveIntoDb(message: MessageEntity) {
        if (!receiveMessageIdsSet.contains(message.msgId)) {
            insertMessageIntoDb(message)
            message.isNotVoiceMessage { receiveMessageIdsSet.add(message.msgId) }
        }
    }

    /**
     * Fetch itemType and insert message intoDb
     */
    private suspend fun insertMessageIntoDb(message: MessageEntity): Long {
        return try {
            val mappedMessage = messengerEntityMapper.mapBeforeInsertToDB(message, isRoomChatRequest)
            saveMessageDbUseCase.invoke(mappedMessage)
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }

    private fun handleSuccessUnblockUser(userId: Long) {
        _liveMessageViewEvent.postValue(ChatMessageViewEvent.UpdateCompanionAsUnblocked(userId))
    }

    private fun pushChatButtonEnabled() {
        viewModelScope.launch {
            userPreferencesUseCase.execute(
                params = UserProfileDefParams(
                    UserSettingsEffect.EnabledChatEffect(isChatEnabled = true)
                ),
                success = {

                },
                fail = { e ->
                    Timber.e(e)
                }
            )
        }
    }

    private suspend fun getDialogSuspend(roomId: Long?): DialogEntity? = roomsInteractor.getRoom(roomId)

    private fun observeMediaKeyboardViewEvent() {
        getMediaKeyboardEventUseCase.invoke()
            .onEach { event ->
                handleMediaKeyboardViewEvent(event)
            }
            .catch { e ->
                Timber.d(e)
            }
            .launchIn(viewModelScope)
    }

    private fun observeFavoritesChanges() {
        getMediaKeyboardFavoritesUseCase.invoke()
            .onEach { newFavorites ->
                this.currentMediaFavorites.clear()
                this.currentMediaFavorites += newFavorites.map(mediakeyboardFavoritesMapper::mapDomainToUiModel)
            }
            .launchIn(viewModelScope)
    }

    private fun handleMediaKeyboardViewEvent(event: MediaKeyboardViewEvent) {
        when (event) {
            MediaKeyboardViewEvent.SendSelectedViewEvent -> {
                emitMediaKeyboardEvent(ChatMediaKeyboardEvent.ExpandMediaKeyboardEvent)
            }

            MediaKeyboardViewEvent.CheckButtonsState -> {
                emitMediaKeyboardEvent(ChatMediaKeyboardEvent.CheckMediaKeyboardBehavior)
            }
        }
    }

    private fun reloadRecentStickers() {
        viewModelScope.launch {
            runCatching {
                reloadRecentStickersUseCase.invoke()
            }.onFailure { Timber.e(it) }
        }
    }

    private fun emitMediaKeyboardEvent(typeEvent: ChatMediaKeyboardEvent) {
        viewModelScope.launch {
            _mediaKeyboardViewEvent.emit(typeEvent)
        }
    }

    private fun handleEditingState(state: EditingEvents) {
        val mappedEvent = when (state) {
            is EditingEvents.EditingStarted -> ChatMessageViewEvent.StartMessageEditing
            is EditingEvents.EditingFinished -> ChatMessageViewEvent.FinishMessageEditing
        }
        _liveMessageViewEvent.postValue(mappedEvent)
    }

    private suspend fun sendEditedMessage(editedMessage: EditMessageModel) {
        runCatching {
            val workId = chatWorkManagerDelegate.editMessage(editedMessage)
            _liveMessageViewEvent.postValue(
                ChatMessageViewEvent.OnEditMessageWorkSubmitted(workId)
            )
        }.onFailure {
            Timber.e(it)
            _liveMessageViewEvent.postValue(
                ChatMessageViewEvent.OnEditMessageError(isEditTooLate = false)
            )
        }
    }

    private fun addToFavoritesHandleEvent(mediaPreview: MediaPreviewUiModel, mediaUrl: String, lottieUrl: String?) {
        val amplitudeProperties = getWhereAndMediaTypePropertiesFromMediaPreview(mediaUrl, mediaPreview)
        val whereProperty = amplitudeProperties.first
        val mediaTypeProperty = amplitudeProperties.second
        addToFavorites(mediaPreview.media, mediaUrl)
        sendEffect(ChatMessageViewEvent.OnSetupAddToFavoritesAnimation(lottieUrl ?: mediaUrl))
        if (whereProperty == null || mediaTypeProperty == null) return
        logAddToFavorites(
            whereProperty = whereProperty,
            mediaTypeProperty = mediaTypeProperty,
            stickerId = getStickerIdFromMediaPreview(mediaPreview),
            stickerCategory = getStickerCategoryFromMediaPreview(mediaPreview)
        )
    }

    private fun getWhereAndMediaTypePropertiesFromMediaPreview(
        mediaUrl: String,
        mediaPreview: MediaPreviewUiModel
    ): Pair<AmplitudeMediaKeyboardFavoriteWhereProperty?, AmplitudeMediaKeyboardMediaTypeProperty?> {
        var whereProperty: AmplitudeMediaKeyboardFavoriteWhereProperty? = null
        var mediaTypeProperty: AmplitudeMediaKeyboardMediaTypeProperty? = null
        when {
            mediaPreview.favoriteRecentModel == null -> {
                whereProperty = AmplitudeMediaKeyboardFavoriteWhereProperty.CHAT_MEDIA_SCREEN
                mediaTypeProperty = getMediaTypeFromMedia(mediaPreview.media)
            }

            mediaPreview.type == MediaPreviewType.STICKER -> {
                whereProperty = AmplitudeMediaKeyboardFavoriteWhereProperty.STICKERPACK
                mediaTypeProperty = AmplitudeMediaKeyboardMediaTypeProperty.STICKER
            }

            mediaPreview.type == MediaPreviewType.FAVORITE -> {
                whereProperty = AmplitudeMediaKeyboardFavoriteWhereProperty.FAVORITE
                mediaTypeProperty = getMediaTypeFromFavoriteRecentType(mediaUrl, mediaPreview.favoriteRecentModel.type)
            }

            mediaPreview.type == MediaPreviewType.RECENT &&
                mediaPreview.favoriteRecentModel.type == MediakeyboardFavoriteRecentUiModel.FavoriteRecentType.STICKER -> {
                whereProperty = AmplitudeMediaKeyboardFavoriteWhereProperty.RECENT_STICKERS
                mediaTypeProperty = AmplitudeMediaKeyboardMediaTypeProperty.STICKER
            }

            mediaPreview.type == MediaPreviewType.RECENT -> {
                whereProperty = AmplitudeMediaKeyboardFavoriteWhereProperty.OTHER
                mediaTypeProperty = getMediaTypeFromFavoriteRecentType(mediaUrl, mediaPreview.favoriteRecentModel.type)
            }

            mediaPreview.type == MediaPreviewType.GIPHY -> {
                whereProperty = AmplitudeMediaKeyboardFavoriteWhereProperty.GIPHY
                mediaTypeProperty = AmplitudeMediaKeyboardMediaTypeProperty.GIF_GIPHY
            }
        }
        return Pair(whereProperty, mediaTypeProperty)
    }

    private fun getMediaTypeFromFavoriteRecentType(
        url: String,
        favoriteRecentType: MediakeyboardFavoriteRecentUiModel.FavoriteRecentType?
    ): AmplitudeMediaKeyboardMediaTypeProperty? {
        return when (favoriteRecentType) {
            MediakeyboardFavoriteRecentUiModel.FavoriteRecentType.IMAGE -> AmplitudeMediaKeyboardMediaTypeProperty.PHOTO
            MediakeyboardFavoriteRecentUiModel.FavoriteRecentType.VIDEO -> AmplitudeMediaKeyboardMediaTypeProperty.VIDEO
            MediakeyboardFavoriteRecentUiModel.FavoriteRecentType.GIF -> {
                if (isGiphyUrl(url)) AmplitudeMediaKeyboardMediaTypeProperty.GIF_GIPHY else AmplitudeMediaKeyboardMediaTypeProperty.GIF_GALLERY
            }

            MediakeyboardFavoriteRecentUiModel.FavoriteRecentType.STICKER -> AmplitudeMediaKeyboardMediaTypeProperty.STICKER
            else -> null
        }
    }

    private fun getMediaTypeFromMedia(media: MediaUiModel?): AmplitudeMediaKeyboardMediaTypeProperty? {
        return when (media) {
            is MediaUiModel.GifMediaUiModel -> {
                if (isGiphyUrl(media.url)) AmplitudeMediaKeyboardMediaTypeProperty.GIF_GIPHY else AmplitudeMediaKeyboardMediaTypeProperty.GIF_GALLERY
            }

            is MediaUiModel.ImageMediaUiModel -> AmplitudeMediaKeyboardMediaTypeProperty.PHOTO
            is MediaUiModel.StickerMediaUiModel -> AmplitudeMediaKeyboardMediaTypeProperty.STICKER
            is MediaUiModel.VideoMediaUiModel -> AmplitudeMediaKeyboardMediaTypeProperty.VIDEO
            else -> null
        }
    }

    private fun getStickerIdFromMediaPreview(mediaPreview: MediaPreviewUiModel): Int? {
        return mediaPreview.favoriteRecentModel?.stickerId
            ?: (mediaPreview.media as? MediaUiModel.StickerMediaUiModel?)?.stickerId
    }

    private fun getStickerIdFromUrl(stickerUrl: String?): Int? {
        val stickerPacks = getCachedStickersUseCase.invoke()
        stickerPacks.forEach { pack ->
            pack.stickers.forEach { sticker ->
                if (sticker.url == stickerUrl) return sticker.id
            }
        }
        return null
    }

    private fun getStickerCategoryFromMediaPreview(mediaPreview: MediaPreviewUiModel): String? {
        val stickerCategory = mediaPreview.favoriteRecentModel?.stickerCategory
            ?: (mediaPreview.media as? MediaUiModel.StickerMediaUiModel?)?.stickerPackTitle
        if (stickerCategory != null) return stickerCategory
        val stickerId = getStickerIdFromMediaPreview(mediaPreview) ?: return null
        return getStickerCategoryFromStickerId(stickerId)
    }

    private fun getStickerCategoryFromStickerId(stickerId: Int?): String? {
        stickerId ?: return null
        val stickerPacks = getCachedStickersUseCase.invoke()
        val requiredStickerPack = stickerPacks.firstOrNull { stickerPack ->
            stickerPack.stickers.any { sticker ->
                sticker.id == stickerId
            }
        } ?: return null
        return requiredStickerPack.title
    }

    private fun isGiphyUrl(url: String): Boolean {
        return url.contains("giphy.com")
    }

    private fun sendEffect(effect: ChatMessageViewEvent) {
        _liveMessageViewEvent.value = effect
    }

    private fun handleChatBackground(room: DialogEntity?, user: UserChat?, roomType: RoomType) {
        val blackList = user?.blacklistedMe ?: 0
        val serverHolidayBackground = getHolidayBackground(room?.style, roomType)
        val backgroundType = when {
            user != null && isBirthdayToday(user.birthDate) && blackList == 0 -> ChatBackgroundType.Birthday
            serverHolidayBackground != null -> ChatBackgroundType.Holiday(serverHolidayBackground)
            room?.style?.background?.isNotEmpty() == true -> ChatBackgroundType.RoomStyle(room.style?.background)
            else -> ChatBackgroundType.None
        }
        _liveMessageViewEvent.value = ChatMessageViewEvent.OnSetChatBackground(backgroundType)
    }

    private fun getHolidayBackground(roomStyle: DialogStyle?, roomType: RoomType): String? {
        return if (holidayInfoHelper.isHolidayExistAndMatches()) {
            getCurrentHoliday(roomStyle, roomType)
        } else {
            null
        }
    }

    private fun getCurrentHoliday(roomStyle: DialogStyle?, roomType: RoomType): String? {
        return if (holidayInfoHelper.currentHoliday().chatRoomEntity.type == roomStyle?.type) {
            holidayInfoHelper.currentHoliday().chatRoomEntity.getBackground(roomType)
        } else {
            null
        }
    }

    private fun handleGreetings() {
        loadGreetingSticker()
    }

    private fun MessageEntity.isMy(): Boolean = getUserUidUseCase.invoke() == this.creator?.userId

    private fun observeReloadDialogs() {
        roomsInteractor.observeReloadDialogs()
            .onEach {
                val rooms = roomsInteractor.getRooms(type = RoomTimeType.ROOMS_MAX_UPDATE)
                checkBlockGroupChat(rooms)
            }
            .catch { e -> Timber.e(e) }
            .launchIn(viewModelScope)
    }

    private fun checkBlockGroupChat(rooms: List<DialogEntity>) {
        val groupChat = rooms.firstOrNull { it.type == ROOM_TYPE_GROUP && it.blocked == true }
        groupChat?.let {
            _liveMessageViewEvent.postValue(ChatMessageViewEvent.OnGroupChatBlocked)
        }
    }


    fun updateStickerPackOrder(packId: Int) = viewModelScope.launch {
        updateStickerOrderUseCase.invoke(packId)
    }

    private fun initShareContentDelegate() {
        shareContentDelegate = ShareContentDelegate(
            context = context,
            downloadHelper = downloadHelper,
            onViewEvent = { event -> _liveMessageViewEvent.postValue(event) },
            onDownloadProgress = { progress -> liveDownloadMediaProgress.postValue(progress) }
        )
    }

    private fun getMediaAttachmentForCopy(message: MessageEntity, attachmentsIndex: Int) {
        viewModelScope.launch {
            runCatching {
                val foundMessage = messagesInteractor.getMessageById(message.msgId)
                val attachment = if (foundMessage?.attachments?.isNotEmpty() == true)
                    foundMessage.attachments[attachmentsIndex] else foundMessage?.attachment
                if (attachment != null) {
                    _liveMessageViewEvent.value =
                        ChatMessageViewEvent.OnCopyAttachmentImageMessage(attachment)
                } else {
                    Timber.e("ERROR found media message for copy attachment to clipboard")
                }
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    private fun mapMessagesAttachments(items: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val mappedData = items.map { item ->
                val mediaType = fileManager.getMediaType(Uri.parse(item))
                if (mediaType == FileUtilsImpl.MEDIA_TYPE_VIDEO) {
                    val duration = fileManager.getVideoDurationMils(Uri.parse(item))
                    ChatAttachmentUiModel(url = item, duration = duration)
                } else {
                    ChatAttachmentUiModel(url = item)
                }
            }
            _chatAttachmentsUiData.postValue(mappedData)
        }
    }

    companion object {
        const val CHAT_MESSAGES_PAGE_SIZE = 20
        const val USER_TYPE_USER_CHAT = "UserChat"
        const val USER_ID = "user_id"

        private const val METADATA_PREVIEW_KEY = "preview"
        private const val METADATA_DURATION_KEY = "duration"
        private const val METADATA_RATIO_KEY = "ratio"
        const val FAKE_MESSAGES_PREFIX = "#send"
        const val EDIT_MESSAGE_PREFIX = "#edit"
        const val FAKE_MESSAGE_TEXT = "Message:"
        const val DELAY_BEFORE_SEND_FAKE_MESSAGE_MS = 500
    }

    override fun connectionStatus(isConnected: Boolean) {
        if (isConnected) {
            startObservingCallSignals()
        }
    }

    private fun startObservingCallSignals() {
        viewModelScope.launch {
            getCallStatusUsecase.invoke()
                .collectLatest {
                    val latestBehaviorValue = latestMediaKeyboardBehavior.value
                    latestBehaviorValue?.let {
                        Timber.d("CHAT SC $it")
                        showMediaKeyboard.value = it
                    }

                }
        }
    }

}
