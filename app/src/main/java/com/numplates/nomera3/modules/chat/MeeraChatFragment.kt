@file:Suppress("unused")

package com.numplates.nomera3.modules.chat

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Point
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.view.KeyEvent
import android.view.View
import android.webkit.URLUtil
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.allViews
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.addSpanBoldRangesClickColored
import com.meera.core.extensions.clearText
import com.meera.core.extensions.click
import com.meera.core.extensions.color
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.getSilentState
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.invisible
import com.meera.core.extensions.keepScreenOnDisable
import com.meera.core.extensions.keepScreenOnEnable
import com.meera.core.extensions.lightVibrate
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.loadGlideProgress
import com.meera.core.extensions.newHeight
import com.meera.core.extensions.openSettingsScreen
import com.meera.core.extensions.orFalse
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.setVisible
import com.meera.core.extensions.showKeyboard
import com.meera.core.extensions.textChanges
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.toInt
import com.meera.core.extensions.toast
import com.meera.core.extensions.vibrate
import com.meera.core.extensions.visible
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.KeyboardHeightProvider
import com.meera.core.utils.NSnackbar
import com.meera.core.utils.files.FileUtilsImpl.Companion.MEDIA_TYPE_IMAGE
import com.meera.core.utils.files.FileUtilsImpl.Companion.MEDIA_TYPE_IMAGE_GIF
import com.meera.core.utils.files.FileUtilsImpl.Companion.MEDIA_TYPE_VIDEO
import com.meera.core.utils.listeners.OrientationScreenListener
import com.meera.core.utils.showCommonError
import com.meera.core.utils.tedbottompicker.compat.OnImagesReady
import com.meera.db.models.DEFAULT_ORIGINAL_ASPECT_GIPHY_IMAGE
import com.meera.db.models.DraftUiModel
import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.dialog.UserChat
import com.meera.db.models.dialog.userRole
import com.meera.db.models.message.MessageAttachment
import com.meera.db.models.message.MessageEntity
import com.meera.db.models.userprofile.UserRole
import com.meera.media_controller_api.model.MediaControllerCallback
import com.meera.media_controller_api.model.MediaControllerNeedEditResponse
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.ErrorSnakeState
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.chat.container.UiKitMessagesContainerView
import com.meera.uikit.widgets.chat.voice.UiKitVoiceView
import com.meera.uikit.widgets.chat.voice.VoiceButtonState
import com.meera.uikit.widgets.roomcell.SendStatus
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.noomeera.nmrmediatools.NMRVideoAmplitude
import com.numplates.nomera3.App
import com.numplates.nomera3.BuildConfig
import com.numplates.nomera3.FRIEND_STATUS_CONFIRMED
import com.numplates.nomera3.MEDIA_VIDEO
import com.numplates.nomera3.R
import com.numplates.nomera3.data.newmessenger.CHAT_ITEM_TYPE_MESSAGE
import com.numplates.nomera3.data.newmessenger.ROOM_TYPE_DIALOG
import com.numplates.nomera3.data.newmessenger.ROOM_TYPE_GROUP
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_AUDIO
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_GIF
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_IMAGE
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_TEXT
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_VIDEO
import com.numplates.nomera3.databinding.MeeraChatFragmentBinding
import com.numplates.nomera3.modules.baseCore.helper.CopyMediaFileDelegate
import com.numplates.nomera3.modules.baseCore.helper.CopyMediaFileDelegateImpl
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegate
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegateImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyChatCreatedFromWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyChatMediaKeyboardCategory
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyChatType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.mediakeyboard.AmplitudeMediaKeyboardWhereProperty
import com.numplates.nomera3.modules.chat.ChatViewModel.Companion.EDIT_MESSAGE_PREFIX
import com.numplates.nomera3.modules.chat.ChatViewModel.Companion.FAKE_MESSAGES_PREFIX
import com.numplates.nomera3.modules.chat.common.utils.resolveChatEntryData
import com.numplates.nomera3.modules.chat.data.DialogApproved
import com.numplates.nomera3.modules.chat.helpers.ChatInfoDialogDelegate
import com.numplates.nomera3.modules.chat.helpers.InputReceiveContentListener
import com.numplates.nomera3.modules.chat.helpers.MeeraChatMenuDelegate
import com.numplates.nomera3.modules.chat.helpers.MeeraChatShareProgressDialogMenu
import com.numplates.nomera3.modules.chat.helpers.MeeraClickMessageBottomMenuDelegate
import com.numplates.nomera3.modules.chat.helpers.MeeraEditMessagePreviewDelegate
import com.numplates.nomera3.modules.chat.helpers.MeeraVoiceMessageRecordDelegate
import com.numplates.nomera3.modules.chat.helpers.PREVIEW_MARGIN_LARGE
import com.numplates.nomera3.modules.chat.helpers.StickersSuggestionsDelegate
import com.numplates.nomera3.modules.chat.helpers.VoiceMessageRecordCallback
import com.numplates.nomera3.modules.chat.helpers.calculateAndLaunchFavoritesAnimation
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitState
import com.numplates.nomera3.modules.chat.helpers.createAddToFavoritesImageViewForAnimation
import com.numplates.nomera3.modules.chat.helpers.createAddToFavoritesLottieViewForAnimation
import com.numplates.nomera3.modules.chat.helpers.editmessage.models.EditMessageWorkResultKey
import com.numplates.nomera3.modules.chat.helpers.isNotLockedMessages
import com.numplates.nomera3.modules.chat.helpers.isRepost
import com.numplates.nomera3.modules.chat.helpers.isValidForEdit
import com.numplates.nomera3.modules.chat.helpers.pagination.ChatPaginator
import com.numplates.nomera3.modules.chat.helpers.pagination.MessagesPaginationProgress
import com.numplates.nomera3.modules.chat.helpers.replymessage.ReplySwipeController
import com.numplates.nomera3.modules.chat.helpers.replymessage.SwipeControllerActions
import com.numplates.nomera3.modules.chat.helpers.replymessage.SwipingItemType
import com.numplates.nomera3.modules.chat.helpers.resendmessage.ResendType
import com.numplates.nomera3.modules.chat.helpers.sendmessage.SendMessageManager.Companion.UNREAD_THRESHOLD
import com.numplates.nomera3.modules.chat.helpers.sendmessage.models.CurrentInProgressMessage
import com.numplates.nomera3.modules.chat.helpers.sendmessage.models.MessageSendSuccessfullyEvent
import com.numplates.nomera3.modules.chat.helpers.sendmessage.models.SendMessageWorkResultKey
import com.numplates.nomera3.modules.chat.helpers.voicemessage.MeeraVoiceMessagePlayer
import com.numplates.nomera3.modules.chat.helpers.voicemessage.MeeraVoiceMessagePlayerCallback
import com.numplates.nomera3.modules.chat.helpers.voicemessage.setChatVoiceButtonConfig
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerPackUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaPreviewType
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaUiModel
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_VIDEO_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_VIDEO_SEND
import com.numplates.nomera3.modules.chat.requests.ui.fragment.ChatRequestBlockData
import com.numplates.nomera3.modules.chat.requests.ui.fragment.ChatRequestCallback
import com.numplates.nomera3.modules.chat.requests.ui.fragment.KEY_BUNDLE_CHAT_REQUEST_BLOCK_REPORT_USER_DATA
import com.numplates.nomera3.modules.chat.requests.ui.fragment.KEY_BUNDLE_CHAT_REQUEST_BLOCK_USER_DATA
import com.numplates.nomera3.modules.chat.requests.ui.fragment.KEY_CHAT_REQUEST_BLOCK_REPORT_USER_RESULT
import com.numplates.nomera3.modules.chat.requests.ui.fragment.KEY_CHAT_REQUEST_BLOCK_USER_RESULT
import com.numplates.nomera3.modules.chat.requests.ui.viewevent.ChatRequestViewEvent
import com.numplates.nomera3.modules.chat.toolbar.ui.ChatToolbarActionsUI
import com.numplates.nomera3.modules.chat.toolbar.ui.ChatToolbarDelegate
import com.numplates.nomera3.modules.chat.toolbar.ui.MeeraChatToolbarDelegateUI
import com.numplates.nomera3.modules.chat.toolbar.ui.entity.UpdatedChatData
import com.numplates.nomera3.modules.chat.toolbar.ui.isChatRequest
import com.numplates.nomera3.modules.chat.toolbar.ui.utils.ToolbarInteractionCallback
import com.numplates.nomera3.modules.chat.ui.ActivityInteractChatActions
import com.numplates.nomera3.modules.chat.ui.ActivityInteractionChatCallback
import com.numplates.nomera3.modules.chat.ui.MeeraChatViewModel
import com.numplates.nomera3.modules.chat.ui.action.ChatActions
import com.numplates.nomera3.modules.chat.ui.action.ShareContentTypes
import com.numplates.nomera3.modules.chat.ui.adapter.ChatAttachmentsAdapter
import com.numplates.nomera3.modules.chat.ui.adapter.ChatGreetBlockAdapter
import com.numplates.nomera3.modules.chat.ui.adapter.MeeraChatMessagesAdapter
import com.numplates.nomera3.modules.chat.ui.helper.MeeraReplyHelper
import com.numplates.nomera3.modules.chat.ui.highlight.scrollWithHighlightingMessage
import com.numplates.nomera3.modules.chat.ui.listeners.MeeraMessagesListener
import com.numplates.nomera3.modules.chat.ui.mapper.CallDataMapper
import com.numplates.nomera3.modules.chat.ui.model.ChatBackgroundType
import com.numplates.nomera3.modules.chat.ui.model.ChatMessageDataUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageUiModel
import com.numplates.nomera3.modules.chat.ui.model.PlayMeeraMessageDataModel
import com.numplates.nomera3.modules.complains.ui.ComplainsNavigator
import com.numplates.nomera3.modules.complains.ui.ComplaintFlowInteraction
import com.numplates.nomera3.modules.complains.ui.ComplaintFlowResult
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAIN_ON_USER_BLOCK
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAIN_ON_USER_REPORT
import com.numplates.nomera3.modules.complains.ui.reason.ComplainType
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.gifservice.ui.GifMenuDelegateEvents
import com.numplates.nomera3.modules.gifservice.ui.GifMenuState
import com.numplates.nomera3.modules.gifservice.ui.MediaKeyboardPagesPosition
import com.numplates.nomera3.modules.gifservice.ui.MeeraGiphyChatMenuDelegateUI
import com.numplates.nomera3.modules.gifservice.ui.entity.GiphyEntity
import com.numplates.nomera3.modules.holidays.ui.entity.RoomType
import com.numplates.nomera3.modules.maps.ui.friends.MapFriendsListsWidget
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.redesign.fragments.main.KEY_BUNDLE_TRANSIT_FROM_MAIN
import com.numplates.nomera3.modules.redesign.fragments.main.KEY_CHAT_TRANSIT_FROM_RESULT
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.modules.share.ui.entity.toUIShareMessage
import com.numplates.nomera3.modules.tags.ui.base.SuggestedTagListMenu
import com.numplates.nomera3.presentation.audio.VoiceMessageView
import com.numplates.nomera3.presentation.model.enums.ChatEventEnum
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_CHAT_INIT_DATA
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_CHAT_TRANSIT_FROM
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_WHERE_CHAT_OPEN
import com.numplates.nomera3.presentation.utils.runOnUiThread
import com.numplates.nomera3.presentation.view.adapter.newchat.chatimage.PostImage
import com.numplates.nomera3.presentation.view.navigator.NavigatorViewPager
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import com.numplates.nomera3.presentation.view.ui.edittextautocompletable.UniqueNameValidationStrategy
import com.numplates.nomera3.presentation.view.ui.mediaViewer.ImageViewerData
import com.numplates.nomera3.presentation.view.ui.mediaViewer.MediaViewer
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.pager.RecyclingPagerAdapter
import com.numplates.nomera3.presentation.view.utils.sharedialog.MeeraShareSheet
import com.numplates.nomera3.presentation.view.utils.sharedialog.ShareBottomSheetEvent
import com.numplates.nomera3.presentation.view.utils.sharedialog.ShareDialogType
import com.numplates.nomera3.presentation.viewmodel.viewevents.ChatMediaKeyboardEvent
import com.numplates.nomera3.presentation.viewmodel.viewevents.ChatMessageViewEvent
import com.numplates.nomera3.telecom.MeeraCallDelegate
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.lang.ref.WeakReference
import java.util.LinkedList
import java.util.UUID
import javax.inject.Inject

private const val ROOM_ID_INIT_DELAY = 1500L
private const val SINGLE_INCOMING_MESSAGE = 1L
private const val BOTTOM_SCROLL_POSITION = 0
private const val NEW_MESSAGE_DIFFERENCE = 1
private const val FIRST_MESSAGE_TODAY_DIFFERENCE = 2
private const val UNSEEN_HOLDERS_COUNT_TO_SHOW_SCROLL_DOWN = 2
private const val ADDED_TO_FAVORITES_NOTIFICATION_SMALL_MARGIN = 60
private const val ADDED_TO_FAVORITES_NOTIFICATION_BIG_MARGIN = 48
private const val MEDIA_ADAPTER_SCROLL_COMPENSATION = 1
private const val TYPING_START_CHAR_COUNT = 1
private const val TYPING_MULTIPLE_CHAR_COUNT = 5
private const val DELAY_KEYBOARD_CHANGE_STATE = 250L
private const val SNACK_BAR_MARGIN = 16
private const val MEDIA_PREVIEW_XML_MARGIN = 96
private const val MEDIA_PREVIEW_MIN_MARGIN = 56

class MeeraChatFragment : MeeraBaseDialogFragment(R.layout.meera_chat_fragment, ScreenBehaviourState.Full),
    MeeraMessagesListener,
    MediaKeyboardCallback,
    MeeraMenuBottomSheet.Listener,
    BasePermission by BasePermissionDelegate(),
    SaveMediaFileDelegate by SaveMediaFileDelegateImpl(),
    CopyMediaFileDelegate by CopyMediaFileDelegateImpl() {

    @Inject
    lateinit var callDataMapper: CallDataMapper

    private var transitFromData: TransitFrom? = null
    private var activityCallback: ActivityInteractionChatCallback? = null
    private var onActivityCallInteraction: MeeraCallDelegate.OnActivityCallInteraction? = null
    private var complaintFlowInteraction: ComplaintFlowInteraction? = null

    private var toolbarDelegate: ChatToolbarDelegate? = null
    private var voiceRecordDelegate: MeeraVoiceMessageRecordDelegate? = null
    private var meeraVoiceMessagePlayer: MeeraVoiceMessagePlayer? = null
    private var chatMenuDelegate: MeeraChatMenuDelegate? = null
    private var clickMessageBottomMenuDelegate: MeeraClickMessageBottomMenuDelegate? = null
    private var messageLongClickBottomMenu: MeeraMenuBottomSheet? = null
    private var shareProgressBottomSheet: MeeraChatShareProgressDialogMenu? = null
    private var chatDialogDelegate: ChatInfoDialogDelegate? = null

    private val chatNavigator: MeeraChatNavigator = MeeraChatNavigator(this)
    private val chatViewModel by viewModels<MeeraChatViewModel> { App.component.getViewModelFactory() }
    private val complainsNavigator by lazy(LazyThreadSafetyMode.NONE) { ComplainsNavigator(requireActivity()) }

    private val binding by viewBinding(MeeraChatFragmentBinding::bind)

    private val chatGreetBlockAdapter by lazy(LazyThreadSafetyMode.NONE) {
        ChatGreetBlockAdapter(this::onGreetingClicked)
    }
    var userSnippet: WeakReference<MapFriendsListsWidget>? = null

    private var currentSnackbar: UiKitSnackBar? = null

    private val act: MeeraAct get() = requireActivity() as MeeraAct

    private val chatMessagesAdapter by lazy {
        MeeraChatMessagesAdapter(
            context = requireContext(),
            messagesListener = this,
        )
    }

    private val chatAttachmentsAdapter: ChatAttachmentsAdapter by lazy {
        ChatAttachmentsAdapter(chatViewModel::deletePhotoClicked)
    }

    private var messageSwipeController: ReplySwipeController? = null
    private var mediaKeyboardCategory: AmplitudePropertyChatMediaKeyboardCategory =
        AmplitudePropertyChatMediaKeyboardCategory.NONE
    private var favoriteRecent: MediakeyboardFavoriteRecentUiModel? = null
    private var favoriteRecentType: MediaPreviewType? = null
    private var pathImage: String? = null
    private var imagesToSend: List<Uri>? = null
    private var pathImageTemp: String? = null

    private var companion: UserChat? = null
    private var isKeyboardOpen = false
    private var isKeyboardOpenBeforeOpeningMenu = false
    private var videoPreviewPlayer: ExoPlayer? = null
    private var roomId: Long? = null

    private var parentMessage: MessageEntity? = null
        set(value) {
            field = value
            chatViewModel.saveDraft(
                userId = dialog?.companion?.userId,
                roomId = roomId,
                reply = value,
                text = binding.sendMessageContainer.etWrite.text.toString()
            )
        }

    private var dialog: DialogEntity? = null

    private var unreadMessageCounter: Long = 0L

    private var unsentMessageCounter: Int = 0

    private var isScrollUnreadDivider = false
    private var isScrollUpUnreadDividerYet = false

    // Tracking read messages
    private val shownMessageIds = mutableSetOf<String>()

    // Tracking downloaded voice messages
    private val trackVoiceMessageIds = mutableSetOf<String>()

    private var keyboardState: KeyboardState = KeyboardState.MEDIA_KEYBOARD
    private var stickersSuggestionsDelegate: StickersSuggestionsDelegate? = null
    private var gifMenuDelegate: MeeraGiphyChatMenuDelegateUI? = null
    private var isGifDisplayed: Boolean? = null
    private var recyclerViewState: Parcelable? = null
    private var chatToolTipsController: MeeraChatToolTipsController? = null
    private var giphyAspectRatio: Double = DEFAULT_ORIGINAL_ASPECT_GIPHY_IMAGE
    private var networkImageAspectRatio: Double = DEFAULT_ORIGINAL_ASPECT_GIPHY_IMAGE
    private var isSendWithoutText = false

    private var subsriptionDismissedUserIdList: List<Long> = emptyList()
    private var messageSentByMe: Boolean = false
    private var greetingSent: Boolean = false

    private var currentScrollPosition = 0
    private var unreadMessageWidgetIsVisible = false

    private var currentListAdapterSize = 0

    private var isCompleteSendMessage = true

    private val effectMediaPlayer = MediaPlayer()
    var mediaViewer: MediaViewer? = null

    private var onStopFragmentWasCalled = false
    private var activeSnackBar: NSnackbar? = null
    private var infoSnackbar: UiKitSnackBar? = null

    private var uniqueNameSuggestionMenu: SuggestedTagListMenu? = null
    private var replyHelper: MeeraReplyHelper? = null

    private var editMessagePreviewDelegate: MeeraEditMessagePreviewDelegate? = null
    private var currentInProgressMessage: CurrentInProgressMessage? = null

    override val containerId: Int
        get() = R.id.fragment_first_container_view


    companion object {
        private const val RECYCLER_VIEW_STATE_KEY = "rv_mediakeyboard_tabs_state"
        private const val IS_GIF_DISPLAYED = "isGifDisplayed"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        App.component.inject(this)
        setActivityCallback(context)
        setComplainFlowInteraction(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transitFromData = arguments?.getSerializable(ARG_CHAT_TRANSIT_FROM) as? TransitFrom
        val isTransitFromListRooms = transitFromData == TransitFrom.OTHER
        setFragmentResult(
            KEY_CHAT_TRANSIT_FROM_RESULT,
            bundleOf(KEY_BUNDLE_TRANSIT_FROM_MAIN to isTransitFromListRooms)
        )

        arguments?.getParcelable<ChatInitData>(ARG_CHAT_INIT_DATA).apply {
            isFullDraggable = this?.isDraggable == true
        }
        recyclerViewState = arguments?.getParcelable(RECYCLER_VIEW_STATE_KEY)
        initFragmentResultListeners()
        onBackPressedHandler()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        compensateTopInset()
        postponeEnterTransition()

        initKeyPreImeInputHook()
        initMainChatComponents()
        val chatInitData = arguments?.getParcelable<ChatInitData>(ARG_CHAT_INIT_DATA)
        if (savedInstanceState != null) {
            isGifDisplayed = savedInstanceState.getBoolean(IS_GIF_DISPLAYED)
        }

        notFromMap = chatInitData?.isDraggable != true
        if (!notFromMap) {
            activity?.findViewById<FrameLayout>(getContainerFragmentId())?.let { containerView ->
                val bg = ContextCompat.getDrawable(
                    binding.root.context,
                    R.drawable.rect_rounded_top_16
                )
                containerView.background = bg
                binding.flChatContainer.background = bg
                binding.flChatContainer.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
                binding.meeraChatToolbar.llToolbarRoot.background = bg
                binding.appbarChat.background = bg
                binding.meeraChatToolbar.ukgvLayoutMapDialogTopbarGrabber.visible()
            }
        }
        chatViewModel.handleAction(ChatActions.SetupChat(chatInitData))

        (view.parent as? View)?.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onStart() {
        super.onStart()
        onStartFragment()
        messageTextChangeObserver()
        doDelayed(ROOM_ID_INIT_DELAY) {
            if (isAdded) {
                sendTyping(roomId)
                chatViewModel.observeGetMessages(roomId)
                chatViewModel.handleAction(ChatActions.InProgressResendSendMessage(roomId))
            }
        }
    }

    private fun compensateTopInset() {
        val statusBarHeight = requireContext().getStatusBarHeight()
        binding.flChatContainer.setMargins(top = -statusBarHeight)
    }

    private fun initFragmentResultListeners() {
        arguments?.let { chatViewModel.removeUnreadMessagesDivider() }
        setFragmentResultListener(KEY_SUBSCRIBED) { _, bundle ->
            val subscribed = bundle.getBoolean(KEY_SUBSCRIBED, false)
            dialog?.companion?.settingsFlags?.subscription_on = subscribed.toInt()
            updateToolbar()
        }
        setFragmentResultListener(KEY_SAID_HELLO) { _, bundle ->
            val greeted = bundle.getBoolean(KEY_SAID_HELLO, false)
            greetingSent = greeted
            setupGreeting()
        }
        setFragmentResultListener(KEY_MESSAGES_ALLOWED) { _, bundle ->
            val isResult = bundle.getBoolean(KEY_MESSAGES_ALLOWED, false)
            if (isResult) dialog?.approved = DialogApproved.ALLOW.key
        }
        complainsNavigator.registerDialogChainListener(this) { bundle ->
            complainsNavigator.unregisterDialogChainListener()
            if (isChatRequest().not()) return@registerDialogChainListener
            val complaintId = bundle.getInt(KEY_COMPLAIN_ON_USER_REPORT, -1)
            val dismissed = bundle.getBoolean(KEY_COMPLAIN_ON_USER_BLOCK, false)
            if (complaintId != -1) {
                blockReportCompanionFromChatRequest(complaintId)
            } else if (dismissed) {
                blockCompanionFromChatRequest()
            }
        }
    }

    private fun observeChatInitState() {
        chatViewModel.chatInitStateFlow()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .filterNotNull()
            .onEach { initState ->
                when (initState) {
                    is ChatInitState.OnTransitedByListRooms -> initChatByRoomsTransition(initState.room)
                    is ChatInitState.OnTransitedByProfile -> initChatByProfileTransition(initState.room)
                    is ChatInitState.OnUpdateRoomData -> initChatByRoomsTransition(
                        initState.room, isUpdateRoomMode = true
                    )
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    /**
     * Переход в чат и отображение всего UI и загрузка сообщений
     * когда чат уже создан
     */
    private fun initChatByRoomsTransition(
        room: DialogEntity?,
        isUpdateRoomMode: Boolean = false
    ) {
        room?.let {
            val roomId = room.roomId
            this.roomId = roomId
            this.companion = room.companion
            this.dialog = room
            chatViewModel.handleAction(ChatActions.SetRoomData(room))
            setUnreadMessageCounterWidget(roomId)
            checkIfTechnicalAccount(room)
            replaceCurrentDialogSavingSubscription(room)
            updateToolbar()
            checkBlockChat(dialog?.companion)

            if (!isUpdateRoomMode) {
                observeMessagesFlow(isShowPlaceholder = false)
                roomId.let { id -> activityCallback?.onGetActionFromChat(ActivityInteractChatActions.DisablePush(id)) }
                chatViewModel.handleAction(ChatActions.PrepareRoomAfterUpdateRoomData(roomId))
                chatViewModel.handleAction(ChatActions.LoadMessages(room))
                setSendMessageClickListener(roomId, companion?.userId)
                logAmplitudeChatStarted()
            }
        }
    }

    /**
     * Инициализация UI чата при переходе из профиля когда
     * чат ещё не создан
     * !!! Просто переход, комната ещё не создана на сервере
     */
    private fun initChatByProfileTransition(room: DialogEntity) {
        val companionUserId = room.companion.userId
        dialog = room
        dialog?.type = ROOM_TYPE_DIALOG
        observeMessagesFlow(isShowPlaceholder = true)
        setupGreeting()
        setSendMessageClickListener(null, companionUserId)
        checkIfTechnicalAccount(room)
        updateToolbar()
        checkBlockChat(room.companion)
    }

    private fun initMainChatComponents() {
        logAmplitudeChatOpened()
        binding.sendMessageContainer.etWrite.clearText()
        initMessagesRecyclerView()
        initPermissions()
        initDelegatesAndHelpers()
        initPhotoAttachments()
        initToolbar()
        initEditText()
        initGifMenu()
        initSuggestionMenu()
        initVoiceMessagePlayer()
        initGreetings()
        initStickerSuggestions()
        setupVideoPreviewPlayer()
        initObservers()
        initListeners()
        chatViewModel.handleAction(ChatActions.LoadStickerPacks)
    }

    private fun checkPermissionUseFullScreenIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            checkPermissions(Manifest.permission.USE_FULL_SCREEN_INTENT)
        }
    }

    private fun initKeyPreImeInputHook() {
        binding.sendMessageContainer.etWrite.keyPreImeHook = { keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event?.action == KeyEvent.ACTION_UP) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
                true
            }
            false
        }
    }

    private fun initMessagesRecyclerView() {
        binding.rvChat.adapter = ConcatAdapter(chatGreetBlockAdapter, chatMessagesAdapter)
        binding.rvChat.setHasFixedSize(true)
        binding.rvChat.itemAnimator = null
        binding.rvChat.let { recyclerView ->
            ChatPaginator(
                chatRecyclerView = recyclerView,
                layoutManager = recyclerView.layoutManager as LinearLayoutManager,
                paginationCallback = chatViewModel.paginationUtil
            )
        }
        messageSwipeController = ReplySwipeController(
            context = requireContext(),
            type = SwipingItemType.CHAT_MESSAGE,
            swipeControllerActions = object : SwipeControllerActions {
                override fun onReply(absoluteAdapterPosition: Int) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        val messageId = chatMessagesAdapter.messageIdByAbsolutePosition(absoluteAdapterPosition)
                        chatViewModel.handleReplyMessage(messageId = messageId)
                    }
                }
            }
        ).also { controller ->
            ItemTouchHelper(controller).attachToRecyclerView(binding.rvChat)
            controller.isSwipeEnabled = isNotChatRequest() && isNotAnnouncementChat()
        }
        listMessagesScrollListener(binding.rvChat, chatMessagesAdapter)
    }

    private fun initPermissions() {
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)
        checkPermissionUseFullScreenIntent()
    }

    private fun initDelegatesAndHelpers() {
        binding.layoutEditPreview.let { editMessagePreviewDelegate = MeeraEditMessagePreviewDelegate(it) }
        initTooltipController()
        initReplyHelper()
        initVoiceMessageRecordDelegate()
        initChatMenuDelegate()
        initClickMessageBottomMenuDelegate()
        initChatInfoDialogDelegate()
    }

    private fun initObservers() {
        observeChatRequestMenuVisibility()
        observeSubscriptionDismissedUserIdList()
        observePhotoAttachmentsList()
        observeSendMessageEvent()
        observeMessageText()
        observeMessageEditorState()
        observeViewEvents()
        observeNewMessage()
        observeChatInitState()
        observeMessagesProgressFlow()
        observeStickerPack()
    }

    override fun onGifLongClicked(id: String, preview: String, url: String, ratio: Double) {
        chatViewModel.mediaKeyboardGifLongClicked(
            id = id,
            preview = preview,
            url = url,
            ratio = ratio
        )
    }

    override fun onStickerClicked(sticker: MediaKeyboardStickerUiModel, emoji: String?) {
        val favoriteRecentModel = MediakeyboardFavoriteRecentUiModel(
            id = sticker.id,
            url = sticker.url,
            preview = sticker.url,
            type = MediakeyboardFavoriteRecentUiModel.FavoriteRecentType.STICKER,
            lottieUrl = sticker.lottieUrl,
            webpUrl = sticker.webpUrl,
            stickerId = sticker.id,
            emoji = emoji ?: sticker.emoji.firstOrNull(),
            stickerCategory = sticker.stickerPackTitle
        )
        chatViewModel.updateStickerPackOrder(sticker.stickerPackId)
        mediaKeyboardCategory = AmplitudePropertyChatMediaKeyboardCategory.STICKERPACK
        sendFavoriteRecentMessage(favoriteRecentModel, MediaPreviewType.STICKER)
    }

    override fun onStickerLongClicked(sticker: MediaKeyboardStickerUiModel) {
        chatViewModel.mediaKeyboardStickerLongClicked(sticker)
    }

    override fun onGifClicked(
        gifUri: Uri,
        aspect: Double,
        giphyEntity: GiphyEntity?,
        gifSentWhereProp: AmplitudeMediaKeyboardWhereProperty
    ) {
        onGifReady(listOf(gifUri), aspect)
        chatViewModel.logGifSend(gifSentWhereProp)
        gifMenuDelegate?.collapseMenu()
    }

    override fun onSearchFieldClicked() {
        gifMenuDelegate?.openMediaKeyboardFullScreen()
    }

    override fun onFavoriteRecentClicked(
        favoriteRecent: MediakeyboardFavoriteRecentUiModel,
        type: MediaPreviewType,
        deleteRecentListener: (Int) -> Unit
    ) {
        when (favoriteRecent.type) {
            MediakeyboardFavoriteRecentUiModel.FavoriteRecentType.IMAGE,
            MediakeyboardFavoriteRecentUiModel.FavoriteRecentType.VIDEO -> {
                onFavoriteRecentLongClicked(favoriteRecent, type, deleteRecentListener)
            }

            MediakeyboardFavoriteRecentUiModel.FavoriteRecentType.GIF -> {
                sendFavoriteRecentMessage(favoriteRecent, type)
            }

            MediakeyboardFavoriteRecentUiModel.FavoriteRecentType.STICKER -> {
                sendFavoriteRecentMessage(favoriteRecent, type)
            }
        }
    }

    override fun onFavoriteRecentLongClicked(
        favoriteRecent: MediakeyboardFavoriteRecentUiModel,
        type: MediaPreviewType,
        deleteClickListener: (Int) -> Unit
    ) {
        chatViewModel.handleAction(ChatActions.OnFavoriteRecentLongClick(favoriteRecent, type, deleteClickListener))
    }

    override fun onScrollToNewStickerPack(stickerPack: MediaKeyboardStickerPackUiModel) {
        gifMenuDelegate?.scrolledToNewStickerPack(stickerPack)
    }

    override fun onScrollToRecentStickers() {
        gifMenuDelegate?.scrolledToRecentStickers()
    }

    override fun onDismiss() {
        gifMenuDelegate?.enableViewPagerScrolling(true)
        hideMediaPreview()
        videoPreviewPlayer?.stop()
        videoPreviewPlayer?.clearMediaItems()
        if (isKeyboardOpenBeforeOpeningMenu) {
            openChatKeyboard()
        }
    }

    private fun observeViewEvents() {
        chatViewModel.liveMessageViewEvent
            .observe(viewLifecycleOwner, ::handleViewEvents)
        chatViewModel.liveChatRequestViewEvent
            .observe(viewLifecycleOwner, ::handleChatRequestViewEvent)
        observeBottomLoadingProgress()
        observeMediaKeyboardReopenEvent()
    }

    private fun observeMediaKeyboardReopenEvent() {
        chatViewModel.showMediaKeyboard.observe(viewLifecycleOwner) {
            Timber.d("CHAT SC $gifMenuDelegate $it")
            gifMenuDelegate?.openMediaKeyboardWithBehavior(it.behavior, it.startPosition)
        }
    }

    private fun tryToRegisterShake() {
        val isAudioRecording = voiceRecordDelegate?.isRecordingProcess() ?: false
        if (isAudioRecording) {
            chatViewModel.disableShake()
        } else {
            chatViewModel.enableShake()
        }
    }

    private fun setupAddToFavoritesAnimation(lottieUrl: String? = null) {
        val ivAddToFavoritesAnimation = createAddToFavoritesViewForAnimation(lottieUrl)

        val btnChatUpload = binding.sendMessageContainer.btnMediaFiles
        val neededPosition = when {
            gifMenuDelegate?.getBehavior() != BottomSheetBehavior.STATE_HIDDEN -> {
                gifMenuDelegate?.mediaKeyboardFavoritesTabPosition
            }

            else -> {
                val btnChatUploadPosition = IntArray(2)
                btnChatUpload.getLocationInWindow(btnChatUploadPosition)
                Point(btnChatUploadPosition[0], btnChatUploadPosition[1])
            }
        }

        val imageView = ivAddToFavoritesAnimation ?: return
        val pointTo = neededPosition ?: return
        binding.root.addView(imageView)
        imageView.visible()
        binding.root.let { root ->
            calculateAndLaunchFavoritesAnimation(root, imageView, pointTo) {
            }
            launchFavoritesLottieAnimation()
        }
    }

    private fun createAddToFavoritesViewForAnimation(lottieUrl: String?): ImageView? {
        return when {
            binding.lavMediaPreview.isVisible -> createAddToFavoritesLottieViewForAnimation(
                context = requireContext(),
                lavMediaPreview = binding.lavMediaPreview,
                lottieUrl = lottieUrl
            )

            binding.ivMediaPreview.isVisible -> createAddToFavoritesImageViewForAnimation(
                context = requireContext(),
                ivMediaPreview = binding.ivMediaPreview
            )

            else -> null
        }
    }

    private fun launchFavoritesLottieAnimation() {
        if (gifMenuDelegate?.getBehavior() != BottomSheetBehavior.STATE_HIDDEN) {
            gifMenuDelegate?.showAddToFavoritesAnimation()
        }
    }

    private fun showAddToFavoritesToast(mediaUrl: String?) {
        chatDialogDelegate?.showAddToFavoritesToast(
            fragment = this,
            mediaUrl = mediaUrl,
            marginBottom = getBottomMarginForAddedToFavoritesDialog(),
            scope = lifecycleScope
        )
    }

    private fun getBottomMarginForAddedToFavoritesDialog(): Int {
        return when (gifMenuDelegate?.getBehavior()) {
            BottomSheetBehavior.STATE_HIDDEN -> {
                val sendMessageContainerPosition = IntArray(2)
                binding.sendMessageContainer.root.getLocationInWindow(sendMessageContainerPosition)
                val screenHeight = Resources.getSystem().displayMetrics.heightPixels
                screenHeight - sendMessageContainerPosition[1] + ADDED_TO_FAVORITES_NOTIFICATION_SMALL_MARGIN.dp
            }

            else -> {
                ADDED_TO_FAVORITES_NOTIFICATION_BIG_MARGIN.dp
            }
        }
    }

    private fun hideMediaPreview() {
        binding.vgMediaPreview.gone()
    }

    private fun setupMediaPreview(media: MediaUiModel, isMeeraMenu: Boolean, menuHeight: Int? = null) {
        binding.apply {
            if (isMeeraMenu) {
                val topMargin =
                    MEDIA_PREVIEW_MIN_MARGIN.dp.takeIf { menuHeight != null }?.minus(context.getStatusBarHeight())
                        ?: MEDIA_PREVIEW_XML_MARGIN.dp
                val bottomMargin = menuHeight?.let { it + MEDIA_PREVIEW_MIN_MARGIN.dp } ?: PREVIEW_MARGIN_LARGE.dp
                ivMediaPreview.setMargins(top = topMargin, bottom = bottomMargin)
                vgVideoPreview.setMargins(top = topMargin, bottom = bottomMargin)
                vgMediaPreview.visible()
            }
            lavMediaPreview.gone()
            ivMediaPreview.visible()
            vgVideoPreview.gone()
            when (media) {
                is MediaUiModel.GifMediaUiModel -> {
                    ivMediaPreview.loadGlideProgress(media.url)
                }

                is MediaUiModel.ImageMediaUiModel -> {
                    ivMediaPreview.loadGlideProgress(media.url)
                }

                is MediaUiModel.VideoMediaUiModel -> {
                    ivMediaPreview.gone()
                    vgVideoPreview.visible()
                    val dataSource = DefaultDataSource.Factory(requireContext())
                    val cacheDataSource = CacheDataSource.Factory()
                        .setCache(MeeraAct.simpleCache!!)
                        .setUpstreamDataSourceFactory(dataSource)
                    val mediaSource: MediaSource = ProgressiveMediaSource.Factory(cacheDataSource)
                        .createMediaSource(MediaItem.fromUri(media.url))
                    videoPreviewPlayer?.setMediaSource(mediaSource)
                    videoPreviewPlayer?.playWhenReady = true
                    videoPreviewPlayer?.prepare()
                }

                is MediaUiModel.StickerMediaUiModel -> {
                    if (!media.lottieUrl.isNullOrBlank()) {
                        lavMediaPreview.visible()
                        ivMediaPreview.gone()
                        lavMediaPreview.setFailureListener { Timber.e(it) }
                        lavMediaPreview.setAnimationFromUrl(media.lottieUrl)
                    } else {
                        ivMediaPreview.loadGlideProgress(media.stickerUrl)
                    }
                }
            }
        }
    }

    private fun setupVideoPreviewPlayer() {
        val trackSelector = DefaultTrackSelector(requireContext())
        trackSelector.setParameters(trackSelector.buildUponParameters().setMaxVideoSizeSd())
        videoPreviewPlayer = ExoPlayer
            .Builder(requireContext(), DefaultRenderersFactory(requireContext()))
            .setTrackSelector(trackSelector)
            .build()
        videoPreviewPlayer?.repeatMode = Player.REPEAT_MODE_OFF
        videoPreviewPlayer?.volume = 0F
        binding.pvVideoPreview.player = videoPreviewPlayer
    }

    private fun observeChatRequestMenuVisibility() {
        chatViewModel.liveIsChatRequestMenuVisible.observe(viewLifecycleOwner) { isVisible ->
            toolbarDelegate?.handleAction(ChatToolbarActionsUI.ChangeChatRequestMenuVisibility(isVisible))
        }
    }

    private fun initPhotoAttachments() {
        val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.sendMessageContainer.rvChosenPhotos.apply {
            layoutManager = linearLayoutManager
            adapter = chatAttachmentsAdapter
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        }
        chatAttachmentsAdapter.registerAdapterDataObserver(photoAttachmentsAdapterDataObserver)
    }

    private fun initEditText() {
        setTemporarySendText(String.empty())

        binding.sendMessageContainer.etWrite.doAfterTextChanged { editable ->
            val text = editable?.toString() ?: return@doAfterTextChanged
            setTemporarySendText(text)
        }
        val etWrite = binding.sendMessageContainer.etWrite
        val receiver = InputReceiveContentListener(
            appContext = requireActivity().applicationContext,
            scope = lifecycleScope,
            isIgnoreMimeTypes = true,
            contentListener = { uri, mimeType ->
                etWrite.clearFocus()
                chatViewModel.addMessageMedias(
                    uris = listOf(uri),
                    mimeType = mimeType,
                    isMaxCountCheck = true
                )
            },
        )
        ViewCompat.setOnReceiveContentListener(
            etWrite,
            InputReceiveContentListener.SUPPORTED_MIME_TYPES,
            receiver
        )
    }

    private fun setTemporarySendText(text: String) {
        roomId?.let { chatViewModel.messageTextChanged(it, text) }
    }

    private fun observeSubscriptionDismissedUserIdList() {
        chatViewModel.subscriptionDismissedUserIdListLiveData.observe(viewLifecycleOwner) { list ->
            this.subsriptionDismissedUserIdList = list
            updateToolbar()
        }
    }

    private fun observePhotoAttachmentsList() {
        chatViewModel.photosSetLiveData.observe(viewLifecycleOwner) { set ->
            this.imagesToSend = set.map { Uri.parse(it.trim()) }
            this.pathImage = set.firstOrNull()?.trim()
            binding.sendMessageContainer.apply {
                if (set.isNotEmpty()) {
                    rvChosenPhotos.visible()
                    vDivider.visible()
                    showSendMessageButton()
                } else {
                    rvChosenPhotos.gone()
                    vDivider.gone()
                    if (etWrite.text.isNullOrBlank()) {
                        showRecordMessageButton()
                    } else {
                        showSendMessageButton()
                    }
                }
                chatViewModel.handleAction(ChatActions.MapAttachmentsData(set.toList()))
            }
        }

        chatViewModel.chatAttachmentsUiData.observe(viewLifecycleOwner) { attachments ->
            chatAttachmentsAdapter.submitList(attachments)
        }
    }

    private val photoAttachmentsAdapterDataObserver = object : RecyclerView.AdapterDataObserver() {

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            val lastItem = chatAttachmentsAdapter.itemCount.minus(MEDIA_ADAPTER_SCROLL_COMPENSATION)
            binding.sendMessageContainer.rvChosenPhotos.scrollToPosition(lastItem)
        }
    }

    private fun observeMessageText() {
        chatViewModel.messageTextLiveData.observe(viewLifecycleOwner) { tempContent ->
            val currentText = binding.sendMessageContainer.etWrite.text.toString()
            if (roomId == tempContent.roomId && currentText != tempContent.text) {
                binding.sendMessageContainer.etWrite.setText(tempContent.text)
            }
        }
    }

    private fun observeNewMessage() {
        chatViewModel.sendMessageEvents
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { event ->
                when (event) {
                    is MessageSendSuccessfullyEvent -> scrollToPositionWithOffset()
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun logAmplitudeChatStarted() {
        val chatCreatedFromWhere =
            arguments?.getSerializable(IArgContainer.ARG_FROM_WHERE_CHAT_CREATED)
                as? AmplitudePropertyChatCreatedFromWhere
        if (chatCreatedFromWhere != null) {
            chatViewModel.logAmplitudeStartChat(dialog, chatCreatedFromWhere)
        }
    }

    private fun logAmplitudeChatOpened() {
        val chatOpenedFromWhere = arguments?.getSerializable(ARG_WHERE_CHAT_OPEN)
            as? AmplitudePropertyWhere ?: AmplitudePropertyWhere.COMMUNICATION
        val chatType = if (dialog?.type == ROOM_TYPE_GROUP) {
            AmplitudePropertyChatType.GROUP
        } else {
            AmplitudePropertyChatType.DEFAULT
        }
        chatViewModel.logChatOpen(chatType, chatOpenedFromWhere)
    }

    /**
     * Тулбар необходимо инициализировать после того, как
     * будут получены данные комнаты для определения типа
     * создаваемого чата
     */
    private fun updateToolbar() {
        val entryData = resolveChatEntryData(
            companion = companion,
            room = dialog,
            userId = dialog?.companion?.userId.toString(),
            ownUserId = chatViewModel.getUserUid(),
            roomId = roomId,
            subscriptionDismissedUserIdList = subsriptionDismissedUserIdList,
            messageByMeSent = messageSentByMe
        )
        toolbarDelegate?.handleAction(ChatToolbarActionsUI.SetupToolbar(entryData))
    }

    private fun initToolbar() {
        if (toolbarDelegate != null) return
        toolbarDelegate = MeeraChatToolbarDelegateUI(
            fragment = this@MeeraChatFragment,
            binding = binding.meeraChatToolbar,
            networkStatusProvider = chatViewModel.networkStatusProvider,
            viewModelDelegate = chatViewModel.toolbarDelegate,
            actionCallback = toolbarActionCallback
        )
    }

    private val toolbarActionCallback: ToolbarInteractionCallback by lazy {
        object : ToolbarInteractionCallback {
            override fun onClickMenuBackArrow() {
                hideAllHints()
                NavigationManager.getManager().topNavController.popBackStack()
            }

            override fun onClickDialogAvatar(
                userChat: UserChat?,
                hasMoments: Boolean,
                hasNewMoments: Boolean,
                view: View?
            ) {
                if (hasMoments) {
                    chatNavigator.gotoMomentFragment(userId = userChat?.userId)
                } else {
                    openProfileScreen(userChat?.userId)
                }
            }

            override fun onClickGroupAvatar(roomId: Long?) {
                chatNavigator.gotoAboutChatScreen(roomId)
            }

            override fun onClickMenuGroupChatDetail(roomId: Long?) {
                chatNavigator.gotoAboutChatScreen(roomId)
            }

            override fun onClickMenuGroupChatMore(roomId: Long?) {
                clickMessageBottomMenuDelegate?.openMeeraGroupChatSettingsDialog(
                    room = dialog,
                    ownUserId = chatViewModel.getUserUid()
                )
            }

            override fun allowSwipeDirectionNavigator(direction: NavigatorViewPager.SwipeDirection?) {
                activityCallback?.onGetActionFromChat(ActivityInteractChatActions.SetAllowedSwipeDirection(direction))
            }

            override fun setCallVariableSettings(
                isCallToggleVisible: Boolean,
                isMeAvailableForCalls: Int
            ) {
                chatToolTipsController?.setCallVariablesSettings(
                    callToggleVisible = isCallToggleVisible,
                    meAvailableForCalls = isMeAvailableForCalls
                )
            }

            override fun startCallWithCompanion(companion: UserChat) {
                if (chatViewModel.isInternetConnected() && chatViewModel.isWebSocketEnabled()) {
                    val chatRequestStatus = dialog?.approved?.let { DialogApproved.fromInt(it) }
                    if (chatRequestStatus == DialogApproved.NOT_DEFINED) allowCompanionToChat()
                    startCall(companion)
                } else {
                    defaultToastErrorMessage(R.string.no_internet)
                }
            }

            override fun showCallNotAllowedTooltip() {
                chatToolTipsController?.showCallNotAllowedTooltip()
            }

            override fun setChatBackground(user: UserChat?, roomType: RoomType) {
                chatViewModel.handleAction(ChatActions.OnSetChatBackground(dialog, user, roomType))
            }

            override fun showEnableNotificationsMessage() {
                showSuccessEnabledNotifications()
            }

            override fun showDisableNotificationsMessage() {
                showSuccessDisabledNotifications()
            }

            override fun errorWhenUpdatedNotification() {
                showErrorPrivacySettings()
            }

            override fun updatedChatData(data: UpdatedChatData?, chatType: ChatRoomType) {
                if (chatType == ChatRoomType.DIALOG) {
                    this@MeeraChatFragment.companion = data?.companion
                    checkBlockChat(data?.companion)
                    checkChatRequestByFriendStatus(data?.companion)
                }
            }

            override fun updateChatInputEnabled(isChatEnabled: Boolean) {
                if (!isChatEnabled && dialog?.companion?.userRole != UserRole.SUPPORT_USER) {
                    showDisabledChatMessagesByMe()
                    initAllowChatBtn(companion?.userId)
                } else {
                    showChatInput()
                    setupGreeting(chatMessagesAdapter.getMessageItem(0))
                }
            }

            override fun chatRequestStatus(isRoomChatRequest: Boolean) {
                chatViewModel.isRoomChatRequest = isRoomChatRequest
            }

            override fun onBlockChatRequestClicked() {
                clickMessageBottomMenuDelegate?.openMeeraForbidChatRequestMenu()
            }

            override fun allowSendMessageChatRequest() = allowCompanionToChat()

            override fun subscribeToUserClicked() {
                val userId = dialog?.companion?.userId ?: return
                dialog?.companion?.settingsFlags?.subscription_on = true.toInt()
                chatViewModel.subscribeClicked(dialog, userId)
            }

            override fun dismissSubscriptionClicked() {
                val userId = dialog?.companion?.userId ?: return
                chatViewModel.dismissSubscriptionClicked(userId)
            }

            override fun onClickDialogMoreItem() {
                closeChatKeyboard()
                clickMessageBottomMenuDelegate?.openMeeraDialogMoreMenu(companion)
            }
        }
    }

    private fun blockReportUserFromChat() {
        val companionUid = companion?.userId ?: return
        chatViewModel.createPendingBlockReportResult(companionUid)
        complaintFlowInteraction?.addResultListener { result ->
            chatViewModel.setPendingReportResult(result)
        }
        startNewComplaintsFlow(ComplainType.USER, companionUid)
    }

    private fun startNewComplaintsFlow(complainType: ComplainType, companionUid: Long) {
        chatNavigator.openUserComplaintsFragment(
            complainType = complainType,
            userId = companionUid,
            roomId = dialog?.roomId,
            sendResult = false,
            where = AmplitudePropertyWhere.CHAT
        )
    }

    private fun getChatRequestCallback() = parentFragmentManager.fragments.find {
        it is ChatRequestCallback
    } as? ChatRequestCallback

    private fun blockCompanionFromChatRequest() {
        val userId = chatViewModel.getUserUid()
        val dialog = this@MeeraChatFragment.dialog
        if (dialog != null) {
            val userBlockData = ChatRequestBlockData(
                userId = userId,
                room = dialog,
                reasonId = 0
            )
            setBackToRoomListScreenFragmentResult()
            setFragmentResult(
                KEY_CHAT_REQUEST_BLOCK_USER_RESULT,
                bundleOf(KEY_BUNDLE_CHAT_REQUEST_BLOCK_USER_DATA to userBlockData)
            )
            findNavController().popBackStack()
        }
    }

    private fun blockReportCompanionFromChatRequest(complaintReasonId: Int) {
        val userId = chatViewModel.getUserUid()
        val dialog = this@MeeraChatFragment.dialog
        if (dialog != null) {
            val userBlockData = ChatRequestBlockData(
                userId = userId,
                room = dialog,
                reasonId = complaintReasonId
            )
            setBackToRoomListScreenFragmentResult()
            setFragmentResult(
                KEY_CHAT_REQUEST_BLOCK_REPORT_USER_RESULT,
                bundleOf(KEY_BUNDLE_CHAT_REQUEST_BLOCK_REPORT_USER_DATA to userBlockData)
            )
            findNavController().popBackStack()
        }
    }

    private fun setBackToRoomListScreenFragmentResult() {
        if (transitFromData == TransitFrom.CHAT_REQUEST_SINGLE_ROOM) {
            setFragmentResult(KEY_CHAT_TRANSIT_FROM_RESULT, bundleOf(KEY_BUNDLE_TRANSIT_FROM_MAIN to true))
        }
    }

    private fun openComplaintMenuForDialogChat() {
        val companionUid = companion?.userId ?: return
        complaintFlowInteraction?.addResultListener { result ->
            displayReportActionResult(result)
        }
        startNewComplaintsFlow(ComplainType.USER, companionUid)
    }

    private fun openComplaintMenuForGroupChat() {
        complaintFlowInteraction?.addResultListener { result ->
            displayReportActionResult(result)
        }
        startNewComplaintsFlow(ComplainType.CHAT, companionUid = 0)
    }

    private fun openChatRequestComplaintMenu() {
        changeLocalCompanionBlockedStatus(isBlockedVisually = true, isChatRequest = true)
        chatNavigator.openUserComplaintsFragment(
            complainType = ComplainType.USER,
            userId = dialog?.companion?.userId ?: error("User id can not be null here."),
            roomId = null,
            sendResult = false,
            where = AmplitudePropertyWhere.MSG_REQUEST
        )
    }

    private fun setupGreetingSticker(sticker: MediaKeyboardStickerUiModel?) {
        chatGreetBlockAdapter.sticker = sticker
    }

    private fun changeLocalCompanionBlockedStatus(isBlockedVisually: Boolean, isChatRequest: Boolean) {
        chatViewModel.isChatBlockedVisually = isBlockedVisually
        val dialog = dialog ?: return
        changeChatRequestMenuStatus(isDialogAllowed = false)
        if (isChatRequest) chatViewModel.changeChatRequestMenuVisibility(isVisible = !isBlockedVisually)
        if (isBlockedVisually) {
            showUserBlockedByMe(dialog.companion)
        } else {
            showChatInput()
        }
        chatGreetBlockAdapter.needToShowGreeting = shouldShowSayHiView(isChatBlockedVisually = isBlockedVisually)
    }

    private fun shouldShowSayHiView(isChatBlockedVisually: Boolean): Boolean {
        val canIGreet = !greetingSent && dialog?.companion?.settingsFlags?.iCanGreet?.toBoolean() ?: true
        return !isChatBlockedVisually && canIGreet
    }

    private fun initTooltipController() {
        binding.let {
            chatToolTipsController = MeeraChatToolTipsController(
                context = requireContext(),
                fragment = this,
                binding = it,
                chatViewModel = chatViewModel
            )
        }
    }

    private fun allowCompanionToChat() {
        chatViewModel.chatRequestsAvailability(
            roomId = roomId,
            isAllow = true,
            companionUid = dialog?.companion?.userId
        )
    }

    private fun sendFavoriteRecentMessage(
        favoriteRecentUiModel: MediakeyboardFavoriteRecentUiModel,
        type: MediaPreviewType? = null
    ) {
        this.mediaKeyboardCategory = when {
            type == MediaPreviewType.FAVORITE -> AmplitudePropertyChatMediaKeyboardCategory.FAVORITE
            type == MediaPreviewType.RECENT &&
                favoriteRecentUiModel.type == MediakeyboardFavoriteRecentUiModel.FavoriteRecentType.STICKER ->
                AmplitudePropertyChatMediaKeyboardCategory.RECENT_STICKERS

            type == MediaPreviewType.RECENT -> AmplitudePropertyChatMediaKeyboardCategory.RECENT
            type == MediaPreviewType.STICKER -> AmplitudePropertyChatMediaKeyboardCategory.STICKERPACK
            type == MediaPreviewType.GIPHY -> AmplitudePropertyChatMediaKeyboardCategory.GIPHY
            else -> AmplitudePropertyChatMediaKeyboardCategory.NONE
        }
        this.favoriteRecent = favoriteRecentUiModel
        this.favoriteRecentType = type
        isSendWithoutText = true
        binding.sendMessageContainer.btnSend.performClick()
        isSendWithoutText = false
        gifMenuDelegate?.collapseMenu()
    }

    private fun forbidCompanionToChat() {
        chatViewModel.chatRequestsAvailability(
            roomId = roomId,
            isAllow = false,
            companionUid = dialog?.companion?.userId
        )
    }

    private fun initGifMenu() {
        gifMenuDelegate = MeeraGiphyChatMenuDelegateUI(
            fragment = this,
            binding = binding,
            keyboardHeightProvider = KeyboardHeightProvider(requireView()),
            menuEvents = { events ->
                when (events) {
                    is GifMenuDelegateEvents.OnShowSoftwareKeyboard -> {
                        openChatKeyboard()
                    }

                    is GifMenuDelegateEvents.OnHideSoftwareKeyboard -> {
                        closeChatKeyboard()
                    }

                    is GifMenuDelegateEvents.OnDisplayGifMenuIcon -> {
                        keyboardState = KeyboardState.MEDIA_KEYBOARD
                        binding.sendMessageContainer.ivUnviewedStickerPacks.setVisible(
                            events.showNewStickerPacksIcon
                        )
                        binding.sendMessageContainer.btnGifUpload.setImageDrawable(
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_meera_gif_menu_open)
                        )
                    }

                    is GifMenuDelegateEvents.OnDisplayKeyboardIcon -> {
                        keyboardState = KeyboardState.NATIVE_KEYBOARD
                        binding.sendMessageContainer.ivUnviewedStickerPacks.gone()
                        binding.sendMessageContainer.btnGifUpload.setImageDrawable(
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_meera_open_keyboard)
                        )
                    }

                    is GifMenuDelegateEvents.OnClickGif ->
                        onGifReady(events.gifs, events.aspect)

                    is GifMenuDelegateEvents.OnKeyboardHeightChanged -> {
                        isKeyboardOpen = events.height > 0
                    }

                    is GifMenuDelegateEvents.OnDialogExpanded -> {
                        chatViewModel.showOrHideMediaKeyboardButtons(true)
                    }

                    is GifMenuDelegateEvents.OnDialogStateCollapsed -> {

                        if (dialog?.companion?.name?.isNotEmpty() == true) {
                            chatViewModel.setUserName(dialog?.companion?.name ?: "")
                        } else {
                            chatViewModel.setUserName(dialog?.title ?: "")
                        }
                        chatViewModel.showOrHideMediaKeyboardButtons(false)
                    }

                    is GifMenuDelegateEvents.OnStickerPackViewed -> {
                        chatViewModel.onStickerPackViewed(events.stickerPack)
                    }
                }
            },
            imagesCallback = object : OnImagesReady {
                override fun onReady(images: MutableList<Uri>?) {
                    if (!chatViewModel.isMessageEditActive()) {
                        handleSendMedia(images = images)
                    } else {
                        chatViewModel.addMessageMedias(images.orEmpty())
                        chatViewModel.sendEditedMessageWithConditionsCheck()
                    }
                }

                override fun onReadyWithText(images: MutableList<out Uri>?, text: String?) {
                    if (!chatViewModel.isMessageEditActive()) {
                        handleSendMedia(images = images, text = text)
                    } else {
                        chatViewModel.addMessageMedias(images.orEmpty())
                        chatViewModel.sendEditedMessageWithConditionsCheck()
                    }
                }
            },
        )
    }

    private fun initStickerSuggestions() {
        val rvStickersSuggestions = binding.rvStickersSuggestions
        val vStickerSuggestionsBackground = binding.vStickersSuggestionsBackground
        stickersSuggestionsDelegate = StickersSuggestionsDelegate(
            vStickerSuggestionsBackground,
            rvStickersSuggestions,
            lifecycleScope,
            getChatStickerSuggestionsFeatureToggle()
        ) { sticker, emoji ->
            onStickerClicked(sticker, emoji)
            binding.sendMessageContainer.etWrite.clearText()
        }
    }

    private fun onGreetingClicked(stickerId: Int?) {
        disableGreeting()
        val userId = dialog?.companion?.userId ?: return
        chatViewModel.sendGreetingClicked(userId, stickerId)
        if (dialog?.approved == DialogApproved.NOT_DEFINED.key) {
            dialog?.approved = DialogApproved.ALLOW.key
            changeChatRequestMenuStatus(isDialogAllowed = true)
            chatViewModel.chatRequestsAvailability(
                roomId = roomId,
                isAllow = true,
                companionUid = dialog?.companion?.userId
            )
        }
    }

    private fun disableGreeting() {
        greetingSent = true
        chatGreetBlockAdapter.needToShowGreeting = false
    }

    private fun initVoiceMessageRecordDelegate() {
        voiceRecordDelegate = MeeraVoiceMessageRecordDelegate(
            fragment = this,
            binding = binding,
            callback = voiceMessageRecordCallback
        )
        voiceRecordDelegate?.run()
    }

    private fun initGreetings() {
        chatViewModel.handleAction(ChatActions.InitGreetings)
    }

    private val voiceMessageRecordCallback = object : VoiceMessageRecordCallback {

        override fun requestPermissions() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                checkPermissions(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            } else {
                checkPermissions(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_MEDIA_AUDIO,
                )
            }
        }

        override fun tapRecordBtn() {
            Timber.e("tapRecordBtn")
            meeraVoiceMessagePlayer?.pausePlayer()
            requireContext().vibrate()
            chatViewModel.disableShake()
        }

        override fun releaseRecordBtn() {
            chatToolTipsController?.releaseRecordBtn()
        }

        override fun onLockButtonIsVisible(isVisible: Boolean) {
            scrollDownBtnVisibilityWhenVoiceRecord(isVisibleLockButton = isVisible)
        }

        override fun sendTypingStatus() {
            chatViewModel.sendTyping(roomId, TYPING_TYPE_AUDIO)
        }

        override fun sendVoiceMessage(
            filePath: String,
            amplitudes: List<Int>,
            durationSec: Long
        ) {
            if (durationSec > 0L && filePath.isNotEmpty()) {
                sendVoiceMessageByType(filePath, amplitudes, durationSec)
            } else {
                Timber.e("Internal error (empty voice message file path)")
            }
            replyHelper?.hideReply()
        }

        override fun onFinishRecordingProcess() {
            binding.sendMessageContainer.etWrite.text.let { messageText ->
                handleTextChanged(messageText)
            }
            chatViewModel.enableShake()
        }

        override fun onUpdateVoiceTimer(seconds: Int, milliseconds: Int) {
            chatToolTipsController?.tapVoiceRecordButtonTooltips(seconds, milliseconds)
        }

        override fun allowSwipeDirectionNavigator(direction: NavigatorViewPager.SwipeDirection?) {
            activityCallback?.onGetActionFromChat(ActivityInteractChatActions.SetAllowedSwipeDirection(direction))
        }
    }

    private fun scrollDownBtnVisibilityWhenVoiceRecord(isVisibleLockButton: Boolean) {
        val isScrollDownBtnVisible = binding.scrollDownButton.isVisible
        if (isVisibleLockButton && isScrollDownBtnVisible) {
            binding.scrollDownButton.invisible()
        } else if (!isVisibleLockButton && currentScrollPosition > 0) {
            binding.scrollDownButton.visible()
        }
    }

    private fun isLockBtnVisible() = voiceRecordDelegate?.isLockButtonVisible() ?: false

    private fun sendVoiceMessageByType(
        filePath: String,
        amplitudes: List<Int>,
        durationSec: Long
    ) {
        scrollToPositionWithOffset()
        disableGreeting()
        chatViewModel.sendVoiceMessage(
            roomId = roomId,
            parentMessage = parentMessage,
            audioPath = filePath,
            listOfAmplitudes = amplitudes,
            currentScrollPosition = currentScrollPosition,
            roomType = dialog?.type ?: ROOM_TYPE_DIALOG,
            userId = dialog?.companion?.userId ?: -1L,
            durationSec = durationSec
        )
    }

    private fun initVoiceMessagePlayer() {
        meeraVoiceMessagePlayer = MeeraVoiceMessagePlayer(
            context = requireContext(),
            lifecycle = lifecycle,
            callback = object : MeeraVoiceMessagePlayerCallback {
                override fun onDownloadVoiceMessage(message: MessageUiModel) {
                    chatViewModel.downloadVoiceMessage(
                        model = message,
                        isNeedRefresh = true,
                        isTapDownloadButton = true
                    )
                }

                override fun keepScreen(isEnable: Boolean) {
                    if (isEnable) {
                        keepScreenOnEnable()
                    } else {
                        keepScreenOnDisable()
                    }
                }
            }
        )
    }

    private fun initReplyHelper() {
        replyHelper = MeeraReplyHelper(binding.clReply, callDataMapper)
        replyHelper?.menuListener = { isVisible ->
            if (!isVisible) {
                this.parentMessage = null
            }
        }
    }

    private fun initSuggestionMenu() {
        binding.uniqueNameSuggestionMenuL.also {
            val editTextAutoCompletable = binding.sendMessageContainer.etWrite
            editTextAutoCompletable.isGetTextBeforeCursor = false
            editTextAutoCompletable.isHashtagFeatureActive = false
            editTextAutoCompletable.checkUniqueNameStrategy =
                UniqueNameValidationStrategy.GroupChat

            val bottomSheetBehavior: BottomSheetBehavior<View> = it.let {
                doDelayed(200) {
                    it.root.visible()
                }

                BottomSheetBehavior.from(it.root)
            }

            SuggestedTagListMenu(
                fragment = this,
                editText = editTextAutoCompletable,
                recyclerView = it.recyclerTags,
                bottomSheetBehavior = bottomSheetBehavior,
                chatRoomId = if (dialog?.type == ROOM_TYPE_GROUP) roomId else null
            ).also { newSuggestedTagListMenu ->
                editTextAutoCompletable.suggestionMenu = newSuggestedTagListMenu
                uniqueNameSuggestionMenu = newSuggestedTagListMenu
            }
        }
    }

    private fun replaceCurrentDialogSavingSubscription(newDialog: DialogEntity) {
        val subscribed = if (newDialog.companion.settingsFlags?.subscription_on.toBoolean()) {
            true
        } else {
            this.dialog?.companion?.settingsFlags?.subscription_on.toBoolean()
        }
        val iCanGreet = if (
            this.dialog?.companion?.settingsFlags?.iCanGreet != null
            && !this.dialog?.companion?.settingsFlags?.iCanGreet.toBoolean()
        ) {
            this.dialog?.companion?.settingsFlags?.iCanGreet
        } else {
            newDialog.companion.settingsFlags?.iCanGreet
        }
        this.dialog = newDialog
        this.dialog?.companion?.settingsFlags?.subscription_on = subscribed.toInt()
        this.dialog?.companion?.settingsFlags?.iCanGreet = iCanGreet
        this.dialog?.companion?.let { checkBlockChat(it) }
    }

    private fun refreshRoomData(room: DialogEntity?) {
        room?.let {
            if (room.approved == DialogApproved.ALLOW.key) {
                chatViewModel.allowChatRequestWithSendMessage(room)
            }
        }
    }

    private fun initListeners() {
        binding.sendMessageContainer.ivConfirmEdit.setThrottledClickListener {
            chatViewModel.sendEditedMessageWithConditionsCheck()
        }
        initScrollDownButtonListener()
        initInputFocusChangeListener()
    }

    /**
     * Bottom notification bar about chat deleted creator
     */
    private fun chatDeletedMessageBar(isDeletedFromRoom: Boolean = false) {
        if (dialog?.blocked == true) return
        binding.bottomInfoBars.layoutChatDeleted.visible()
        disableSendMessageBar()
        voiceRecordDelegate?.goneVoiceRecordBtn()
        chatViewModel.deleteDraft(roomId = dialog?.roomId)
        if (isDeletedFromRoom) {
            binding.bottomInfoBars.tvDeleteChatMessage.text =
                getString(R.string.chat_membership_is_over)
        } else {
            binding.bottomInfoBars.tvDeleteChatMessage.text =
                getString(R.string.group_chat_deleted_message)
        }
    }

    /**
     * Handle check block chat for tet-a-tet and group chat
     */
    private fun checkBlockChat(companion: UserChat?) {
        updateCompanionStatus(companion)
//        chatMessagesAdapter.isRoomBlocked = isRoomBlocked(dialog)
        when {
            dialog?.type == ROOM_TYPE_DIALOG && !isLockBtnVisible() -> checkBlockChatForDialogType(companion)
            dialog?.type == ROOM_TYPE_GROUP && !isLockBtnVisible() -> checkBlockChatForGroupType(dialog)
        }
    }

    private fun showUserBlockedByMe(companion: UserChat) {
        showUserBlockedMessageBar()
        binding.bottomInfoBars.apply {
            root.visible()
            tvUserBlockedMe.gone()
            tvUserBlockedByMe.visible()
        }
        initUnblockUserBtn(companion.userId)
    }

    private fun checkBlockChatForDialogType(companion: UserChat?) {
        companion?.let { companion ->
            when {
                chatViewModel.isChatBlockedVisually ->
                    changeLocalCompanionBlockedStatus(
                        isBlockedVisually = chatViewModel.isChatBlockedVisually,
                        isChatRequest = isChatRequest()
                    )

                companion.blacklistedByMe == 1 -> {
                    chatViewModel.changeChatRequestMenuVisibility(isVisible = false)
                    chatViewModel.clearMessageEditor()
                    showUserBlockedByMe(companion)
                }

                companion.settingsFlags?.userCanChatMe == 0 && companion.userRole == UserRole.USER -> {
                    chatViewModel.clearMessageEditor()
                    binding.bottomInfoBars.root.visible()
                    showDisabledChatMessagesByMe()
                    initAllowChatBtn(companion.userId)
                }

                companion.blacklistedMe == 1 -> {
                    chatViewModel.changeChatRequestMenuVisibility(isVisible = false)
                    chatViewModel.clearMessageEditor()
                    showUserBlockedMessageBar()
                    binding.bottomInfoBars.apply {
                        root.visible()
                        tvUserBlockedMe.visible()
                        tvUserBlockedByMe.gone()
                    }
                }

                companion.settingsFlags?.iCanChat == 0 -> {
                    chatViewModel.changeChatRequestMenuVisibility(isVisible = false)
                    chatViewModel.clearMessageEditor()
                    binding.bottomInfoBars.root.visible()
                    showDisabledChatMessagesByCompanion()
                }

                else -> {
                    val dialogStatus = dialog?.approved?.let { DialogApproved.fromInt(it) }
                    val isChatRequest = dialogStatus == DialogApproved.NOT_DEFINED
                    if (isChatRequest) {
                        chatViewModel.changeChatRequestMenuVisibility(isVisible = true)
                        chatViewModel.clearMessageEditor()
                    }
                    showChatInput()
                }
            }
        }
    }

    private fun updateCompanionStatus(companion: UserChat?) {
        companion?.let { dialog?.companion = companion }
    }

    private fun isRoomBlocked(room: DialogEntity?): Boolean {
        val companion = room?.companion
        return companion?.blacklistedMe == true.toInt()
            || companion?.blacklistedByMe == true.toInt()
    }

    private fun setGroupChatBlockByModerator() {
        dialog?.blocked = true
        checkBlockChatForGroupType(dialog)
    }

    private fun checkBlockChatForGroupType(room: DialogEntity?) {
        binding.sendMessageContainer.layoutGroupChatChatbox.visible()
        if (room?.blocked == true) {
            toolbarDelegate?.handleAction(ChatToolbarActionsUI.SetAvailabilityGroupMenuAbout(isEnabled = false))
            closeChatKeyboard()
            replyHelper?.hideReply()
            chatViewModel.clearMessageEditor()
            editMessagePreviewDelegate?.clearAndHide()
            gifMenuDelegate?.hideAllOpenedKeyboards()
            uniqueNameSuggestionMenu?.onBackPressed()
            stickersSuggestionsDelegate?.messageTextChanged(String.empty())
//            chatMessagesAdapter.refreshRoomData(room)
            disableSendMessageBar()
            showInfoBarBlockGroupChatByModerator()
        }
    }

    private fun showInfoBarBlockGroupChatByModerator() {
        binding.bottomInfoBars.apply {
            root.visible()
            vgGroupChatBlockedByModerator.root.visible()
        }
    }

    private fun checkChatRequestByFriendStatus(companion: UserChat?) {
        if (companion?.settingsFlags?.friendStatus == FRIEND_STATUS_CONFIRMED) {
            allowChatRequest()
        }
    }

    private fun showDisabledChatMessagesByCompanion() {
        chatViewModel.deleteDraft(roomId = dialog?.roomId)
        binding.bottomInfoBars.apply {
            lChatDisallowByCompanion.root.visible()
            layoutChatUserBlocked.gone()
            lChatDisallowByMe.root.gone()
        }
        binding.btnVoiceMessage.gone()
        goneInputBottomBar()
    }

    private fun showDisabledChatMessagesByMe() {
        chatViewModel.deleteDraft(roomId = dialog?.roomId)
        binding.bottomInfoBars.apply {
            lChatDisallowByMe.root.visible()
            layoutChatUserBlocked.gone()
            lChatDisallowByCompanion.root.gone()
        }
        goneInputBottomBar()
    }

    private fun initAllowChatBtn(userId: Long?) {
        userId?.let { id ->
            binding.bottomInfoBars.lChatDisallowByMe.tvChatMessageDisallowByMeDesc.setOnClickListener {
                chatViewModel.enableChatClicked(id)
            }
        }
    }

    private fun showChatInput() {
        binding.bottomInfoBars.root.gone()
        visibleInputBottomBar()
        voiceRecordDelegate?.initChatInput()
    }

    private fun initUnblockUserBtn(userId: Long?) {
        binding.bottomInfoBars.tvUserBlockedByMe.click {
            chatViewModel.unblockUser(userId)
        }
    }

    /**
     * Bottom notification bar about you companion blocked you
     */
    private fun showUserBlockedMessageBar() {
        chatViewModel.deleteDraft(roomId = dialog?.roomId)
        binding.bottomInfoBars.layoutChatUserBlocked.visible()
        binding.bottomInfoBars.lChatDisallowByMe.root.gone()
        binding.bottomInfoBars.lChatDisallowByCompanion.root.gone()
        voiceRecordDelegate?.goneVoiceRecordBtn()
        goneInputBottomBar()
    }

    private fun goneInputBottomBar() {
        binding.apply {
            vgWhiteContainer.gone()
            voiceRecordMotionContainer.gone()
            sendMessageContainer.root.gone()
            voiceRecordProcessContainer.root.gone()
        }
    }

    private fun visibleInputBottomBar() {
        if (dialog?.companion?.userRole == UserRole.ANNOUNCE_USER) return
        binding.apply {
            vgWhiteContainer.visible()
            voiceRecordMotionContainer.visible()
            sendMessageContainer.root.visible()
        }
    }

    private fun onStartFragment() {
        onStopFragmentWasCalled = false
        roomId?.let {
            activityCallback?.onGetActionFromChat(ActivityInteractChatActions.DisablePush(it))
        }
        chatToolTipsController?.setIsFragmentStarted(true)
        when (dialog?.type) {
            ROOM_TYPE_DIALOG -> chatViewModel.logScreenForFragment("P2PChatFragmentNew")
            ROOM_TYPE_GROUP -> chatViewModel.logScreenForFragment("GroupChatFragmentNew")
        }
        tryToRegisterShake()
    }

    private fun onBackPressedHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            val hasKeyboardsChanges = gifMenuDelegate?.hideAllOpenedKeyboards().orFalse()
            val suggestionMenuDisplayed = uniqueNameSuggestionMenu?.onBackPressed().orFalse()
            val isForbiddenExitFromScreen = hasKeyboardsChanges || suggestionMenuDisplayed || isEditCancelDialogShown()
            if (isForbiddenExitFromScreen) {
                return@addCallback
            } else {
                handlePopBackDependsOnTransitData()
            }
        }
    }

    private fun handlePopBackDependsOnTransitData() {
        if (transitFromData == TransitFrom.GROUP_CHAT) {
            findNavController().popBackStack(R.id.mainChatFragment, false)
        } else {
            findNavController().popBackStack()
        }
    }

    private fun isEditCancelDialogShown(): Boolean {
        return if (chatViewModel.isMessageEditActive()) {
            chatDialogDelegate?.showAbortEditMessageDialog(childFragmentManager) {
                chatViewModel.clearMessageEditor()
                requireActivity().onBackPressed()
            }
            true
        } else {
            false
        }
    }

    private fun hideAllHints() {
        activityCallback?.onGetActionFromChat(ActivityInteractChatActions.HideHints)
    }

    // Change button type (record voice / send message)
    private fun messageTextChangeObserver() {
        binding.sendMessageContainer.etWrite.textChanges()
            .onEach { text ->
                chatViewModel.saveDraft(
                    userId = dialog?.companion?.userId,
                    roomId = roomId,
                    reply = parentMessage,
                    text = text.toString()
                )
                stickersSuggestionsDelegate?.messageTextChanged(text.toString())
                doDelayed(100) { if (isAdded) handleTextChanged(text) }
                chatViewModel.checkBirthdayText(
                    newInputText = text.toString(),
                    dateOfBirth = dialog?.companion?.birthDate ?: 0
                )
            }
            .catch { Timber.e("Error et_write binding: $it") }
            .launchIn(lifecycleScope)
    }

    private fun handleTextChanged(text: CharSequence?) {
        val isVoiceRecordingProcess = voiceRecordDelegate?.isRecordingProcess() ?: false
        if (text?.trim()?.isNotEmpty() == true && !isVoiceRecordingProcess) {
            showSendMessageButton()
        } else {
            showRecordMessageButton()
        }
    }

    override fun onResume() {
        super.onResume()
        roomId?.let { id ->
            chatViewModel.checkRestMessages(id)
            chatViewModel.handleAction(ChatActions.PrepareRoomAfterUpdateRoomData(id))
        }
    }

    private fun getFeatureToggles(): FeatureTogglesContainer =
        (activity?.application as FeatureTogglesContainer)

    private fun getChatMessageEditFeatureToggle() =
        (activity?.application as? FeatureTogglesContainer)?.chatMessageEditFeatureToggle

    private fun getChatStickerSuggestionsFeatureToggle() =
        (activity?.application as? FeatureTogglesContainer)?.chatStickerSuggestionsFeatureToggle

    private fun startCall(companion: UserChat) {
        setPermissions(
            listener = object : PermissionDelegate.Listener {
                override fun onGranted() {
                    companion.let {
                        disableGreeting()
                        onActivityCallInteraction?.onStartCall(
                            user = companion,
                            isIncoming = false,
                            callAccepted = null,
                            roomId = null,
                            messageId = null
                        )
                    }
                }

                override fun onDenied() {
                    toastAlertMessageWithButton(
                        stringRes = R.string.you_must_grant_permissions,
                        buttonStringRes = R.string.allow,
                        onButtonClick = { onClickCallPermissionsButton(companion) }
                    )
                }

                override fun onError(error: Throwable?) {
                    defaultToastErrorMessage(R.string.access_is_denied)
                }
            },
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }

    private fun onClickCallPermissionsButton(companion: UserChat) {
        val rationaleCamera = shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
        val rationaleRecordAudio = shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)
        if (!rationaleCamera || !rationaleRecordAudio) {
            requireContext().openSettingsScreen()
        } else {
            startCall(companion)
        }
    }

    private fun showMessageEditingButton() {
        if (!chatViewModel.isMessageEditActive()) return
        binding.sendMessageContainer.ivConfirmEdit.visible()
        binding.sendMessageContainer.btnSend.gone()
        binding.sendMessageContainer.btnSend.isEnabled = false
        voiceRecordDelegate?.goneVoiceRecordBtn()
    }

    private fun showRecordMessageButton() {
        if (chatViewModel.isMessageEditActive()) return
        if (pathImage == null) {
            binding.sendMessageContainer.ivConfirmEdit.gone()
            binding.sendMessageContainer.btnSend.gone()
            binding.sendMessageContainer.btnSend.isEnabled = false
            voiceRecordDelegate?.visibleVoiceRecordBtn()
        } else {
            showSendMessageButton()
        }
    }

    private fun showSendMessageButton() {
        if (chatViewModel.isMessageEditActive()) return
        binding.sendMessageContainer.btnSend.visible()
        binding.sendMessageContainer.btnSend.isEnabled = true
        binding.sendMessageContainer.ivConfirmEdit.gone()
        voiceRecordDelegate?.goneVoiceRecordBtn()
    }

    private fun closeBottomSheetMenu() =
        binding.sendMessageContainer.etWrite.suggestionMenu?.dismiss()

    private fun observeSendMessageEvent() {
        chatViewModel.mediaKeyboardViewEvent
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach(::handleMediaKeyboardEvent)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun handleMediaKeyboardEvent(event: ChatMediaKeyboardEvent) {
        when (event) {
            is ChatMediaKeyboardEvent.ExpandMediaKeyboardEvent -> gifMenuDelegate?.collapseMenu()
            is ChatMediaKeyboardEvent.CheckMediaKeyboardBehavior -> checkMediaKeyboardBehavior()
            is ChatMediaKeyboardEvent.ShowMediaPreview -> {
                gifMenuDelegate?.enableViewPagerScrolling(false)
                if (event.message == null) {
                    chatMenuDelegate?.showMediaPreview(
                        mediaPreview = event.mediaPreview ?: return,
                        onDismiss = ::hideMediaPreview,
                        deleteRecentClickListener = event.deleteRecentClickListener
                    )
                    isKeyboardOpenBeforeOpeningMenu = isKeyboardOpen
                } else {
                    showLongClickBottomMenu(event)
                }
            }

            is ChatMediaKeyboardEvent.OnStickersLoaded -> {
                val containsNotViewedStickerPacks = event.stickerPacks.any { !it.viewed }
                val isAbleToShowIcon = keyboardState == KeyboardState.MEDIA_KEYBOARD
                val isUnviewedStickersVisible = containsNotViewedStickerPacks && isAbleToShowIcon
                binding.sendMessageContainer.ivUnviewedStickerPacks.setVisible(isUnviewedStickersVisible)
                gifMenuDelegate?.setStickerPacks(event.stickerPacks, event.recentStickers)
            }

            else -> Unit
        }
    }

    private fun showLongClickBottomMenu(event: ChatMediaKeyboardEvent.ShowMediaPreview) {
        if (event.message == null) return
        clickMessageBottomMenuDelegate?.showMeeraBottomMenu(
            message = event.message,
            room = dialog,
            messageView = event.view,
            unsentMessageCounter = unsentMessageCounter,
            mediaPreview = event.mediaPreview,
            isEditMessageAvailable = isEditMessageAvailable(event.message),
            menuPayload = event.bottomMenuPayload,
            isUploadMediaProgress = currentInProgressMessage != null
        )
        isKeyboardOpenBeforeOpeningMenu = isKeyboardOpen
    }

    private fun checkMediaKeyboardBehavior() {
        val behavior = gifMenuDelegate?.getBehavior()
        when (behavior) {
            BottomSheetBehavior.STATE_EXPANDED -> chatViewModel.showOrHideMediaKeyboardButtons(true)
            BottomSheetBehavior.STATE_COLLAPSED -> chatViewModel.showOrHideMediaKeyboardButtons(false)
        }

    }

    private fun setSendMessageClickListener(roomId: Long?, userId: Long?) {
        binding.sendMessageContainer.btnSend.setOnClickListener {
            this.isCompleteSendMessage = false
            closeBottomSheetMenu()
            val messageText = getMessageText()
            when {
                favoriteRecent != null -> {
                    scrollToPositionWithOffset()
                    chatViewModel.sendSimpleMessage(
                        roomId = roomId,
                        message = messageText,
                        parentMessage = parentMessage,
                        images = imagesToSend,
                        gifAspectRatio = giphyAspectRatio,
                        currentScrollPosition = currentScrollPosition,
                        roomType = dialog?.type ?: ROOM_TYPE_DIALOG,
                        userId = userId,
                        favoriteRecent = favoriteRecent,
                        favoriteRecentType = favoriteRecentType,
                        mediaKeyboardCategory = mediaKeyboardCategory
                    )
                }

                !pathImage.isNullOrBlank() &&
                    chatViewModel.getMediaType(imagesToSend?.firstOrNull()) == MEDIA_TYPE_VIDEO -> {
                    handleSendMedia(imagesToSend?.toMutableList(), messageText)
                    pathImage = null
                    imagesToSend = null
                    chatViewModel.clearMediaContent()
                }

                BuildConfig.DEBUG && messageText.startsWith(FAKE_MESSAGES_PREFIX) -> {
                    chatViewModel.handleAction(ChatActions.SendFakeMessages(roomId, messageText))
                }

                BuildConfig.DEBUG && messageText.startsWith(EDIT_MESSAGE_PREFIX) ->
                    debugMessageEditFeatureToggle(messageText)

                !pathImage.isNullOrBlank() || messageText.isNotEmpty() -> {
                    val mediaKeyboardCategory = if (imagesToSend.isNullOrEmpty()) {
                        AmplitudePropertyChatMediaKeyboardCategory.NONE
                    } else {
                        AmplitudePropertyChatMediaKeyboardCategory.GALLERY
                    }
                    scrollToPositionWithOffset()
                    chatViewModel.sendSimpleMessage(
                        roomId = roomId,
                        message = messageText,
                        parentMessage = parentMessage,
                        images = imagesToSend,
                        gifAspectRatio = giphyAspectRatio,
                        currentScrollPosition = currentScrollPosition,
                        roomType = dialog?.type ?: ROOM_TYPE_DIALOG,
                        userId = userId,
                        mediaKeyboardCategory = mediaKeyboardCategory
                    )
                    pathImage = null
                    imagesToSend = null
                    chatViewModel.clearMediaContent()
                }
            }
            disableGreeting()
            favoriteRecent = null
            favoriteRecentType = null
            mediaKeyboardCategory = AmplitudePropertyChatMediaKeyboardCategory.NONE
            replyHelper?.closeBtn?.performClick()
        }
    }

    /** Type "#edit on" to force enable chat message edit feature.
     * Type "#edit off" to force disable it.
     * Type "#edit null" to rely on remote config value only.
     * This is meant for debug/testing only.
     */
    private fun debugMessageEditFeatureToggle(toggleEditString: String) {
        val whitespaceDelimiter = " "
        val secondItemIndex = 1
        val valueToSet = toggleEditString.split(whitespaceDelimiter).getOrNull(secondItemIndex)
        getChatMessageEditFeatureToggle()?.localValue = when (valueToSet) {
            "off" -> false
            "on" -> true
            "null" -> null
            else -> return
        }
    }

    private fun allowChatRequest() {
        if (dialog?.approved == DialogApproved.NOT_DEFINED.key) {
            chatViewModel.chatRequestsAvailability(
                roomId = roomId,
                isAllow = true,
                companionUid = dialog?.companion?.userId,
                withSendMessage = true
            )
        }
    }

    private fun getMessageText(): String {
        val messageText: String
        if (isSendWithoutText) {
            messageText = String.empty()
        } else {
            messageText = binding.sendMessageContainer.etWrite.text.toString().trim()
            binding.sendMessageContainer.etWrite.clearText()
        }
        return messageText
    }

    private fun observeSendMessageResult(workId: UUID) {
        WorkManager.getInstance(requireContext())
            .getWorkInfoByIdLiveData(workId)
            .observe(viewLifecycleOwner) { workInfo ->
                if (workInfo.state.isFinished) {
                    handleSendMessageResult(workInfo)
                } else {
                    handleSendMessageProgress(workInfo)
                }
            }
    }

    private fun observeEditMessageResult(workId: UUID) {
        WorkManager.getInstance(requireContext())
            .getWorkInfoByIdLiveData(workId)
            .observe(viewLifecycleOwner) { workInfo ->
                when (workInfo.state) {
                    WorkInfo.State.ENQUEUED,
                    WorkInfo.State.SUCCEEDED,
                    WorkInfo.State.CANCELLED -> {
                        chatViewModel.clearMessageEditor()
                        handleEditingChangedState(isEditing = false)
                    }

                    WorkInfo.State.FAILED -> {
                        chatMenuDelegate?.showMessageEditErrorAlert()
                    }

                    else -> handleEditMessageProgress(workInfo)
                }
            }
    }

    private fun handleSendMessageResult(workInfo: WorkInfo) {
        when (workInfo.state) {
            WorkInfo.State.SUCCEEDED -> {
                if (unreadMessageCounter > UNREAD_THRESHOLD) {
                    binding.scrollDownButton.setCount(0)
                    chatViewModel.markRoomAsRead(roomId)
                    unreadMessageCounter = 0
                    chatViewModel.scrollWithRefreshIfInternetConnected()
                }
            }

            WorkInfo.State.FAILED -> {
                Timber.d("Fail executed send message worker")
            }

            else -> Unit
        }
    }

    private fun handleSendMessageProgress(workInfo: WorkInfo) {
        for ((key, value) in workInfo.progress.keyValueMap) {
            when (key) {
                SendMessageWorkResultKey.ACTION_INSERT_DB_MESSAGE.key -> {
                    val inProgressMessage = chatViewModel
                        .deserializeActionMessageInProgressWorkResult(jsonResult = (value as String))
                    currentInProgressMessage = CurrentInProgressMessage(
                        messageId = inProgressMessage.messageId,
                        workId = inProgressMessage.workId
                    )
                }

                SendMessageWorkResultKey.SUCCESS_SEND.key -> {
                    this.isCompleteSendMessage = true
                    currentInProgressMessage = null
                    chatViewModel.onSuccessSendMessageWorker(pathImageTemp)
                }

                SendMessageWorkResultKey.ACTION_SEND_MESSAGE.key -> {
                    this.isCompleteSendMessage = true
                    currentInProgressMessage = null
                    chatViewModel.deserializeActionSendMessageWorkResult(jsonResult = (value as String))
                }
            }
        }
    }

    private fun handleEditMessageProgress(workInfo: WorkInfo) {
        for ((key, value) in workInfo.progress.keyValueMap) {
            when (key) {
                EditMessageWorkResultKey.SHOW_MEDIA_PROGRESS.key -> {
                    chatViewModel.changeMessageProgress(messageId = value as String, isInProgress = true)
                }

                EditMessageWorkResultKey.HIDE_MEDIA_PROGRESS.key -> {
                    chatViewModel.changeMessageProgress(messageId = value as String, isInProgress = false)
                }
            }
        }
    }

    private fun observeMessagesFlow(isShowPlaceholder: Boolean) {
        chatViewModel.chatMessagesFlow
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .filterNotNull()
            .onEach { messages ->
                handleMessagesFlow(messages, isShowPlaceholder)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun handleMessagesFlow(
        messages: List<ChatMessageDataUiModel>,
        isShowPlaceholder: Boolean
    ) {
        if (isShowPlaceholder && messages.isEmpty()) {
            dialog?.type?.let { setupGreeting() }
        }
        renderChat(messages)
    }

    private fun List<MessageEntity>.firstMessageIsMy(block: () -> Unit) {
        if (this.size == 1 && this.first().creator?.userId == chatViewModel.getUserUid()) {
            block()
        }
    }

    private fun observeMessagesProgressFlow() {
        chatViewModel.messagesProgressFlow()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .filterNotNull()
            .onEach(::handleLoadingMessagesProgress)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun observeStickerPack() {
        chatViewModel.stickersFlow
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { pack ->
                val containsNotViewedStickerPacks = pack.stickerPacks.any { !it.viewed }
                val isAbleToShowIcon = keyboardState == KeyboardState.MEDIA_KEYBOARD
                val isUnviewedStickersVisible = containsNotViewedStickerPacks && isAbleToShowIcon
                binding.sendMessageContainer.ivUnviewedStickerPacks.setVisible(isUnviewedStickersVisible)
                gifMenuDelegate?.setStickerPacks(pack.stickerPacks, pack.recentStickers)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun handleLoadingMessagesProgress(progress: MessagesPaginationProgress) {
        when (progress) {
            MessagesPaginationProgress.BEFORE -> showTopLoadingMessagesProgress()
            MessagesPaginationProgress.AFTER -> showCenterLoadingMessagesProgress()
            MessagesPaginationProgress.NONE -> hideAllLoadingMessagesProgress()
        }
    }

    private fun showTopLoadingMessagesProgress() {
        binding.loadingMessagesProgressTop.visible()
    }

    private fun showCenterLoadingMessagesProgress() {
        binding.loadingMessagesProgressCenter.visible()
    }

    private fun hideAllLoadingMessagesProgress() {
        binding.loadingMessagesProgressTop.invisible()
        binding.loadingMessagesProgressCenter.invisible()
    }

    private fun initScrollDownButtonListener() {
        binding.scrollDownButton.setClickListener {
            binding.rvChat.stopScroll()
            binding.scrollDownButton.setCount(0)
            binding.scrollDownButton.invisible()

            // Scroll or Refresh message list if a lot of unread messages
            if (unreadMessageCounter < chatMessagesAdapter.itemCount) {
                scrollToPositionWithOffset()
            } else {
                if (chatViewModel.isInternetConnected()) {
                    chatViewModel.removeAllMessages()
                    unreadMessageCounter = 0
                } else {
                    scrollToPositionWithOffset()
                }
            }

            toolbarDelegate?.handleAction(ChatToolbarActionsUI.CloseToolbarMenu)
        }
    }

    private fun handleViewEvents(event: ChatMessageViewEvent) {
        when (event) {
            is ChatMessageViewEvent.OnRefreshRoomData -> refreshRoomData(event.room)
            is ChatMessageViewEvent.ActionSendMessage -> handleActionSendMessage(event)
            is ChatMessageViewEvent.OnSuccessSentMessage -> playSendMessageSound()
            is ChatMessageViewEvent.ErrorRemoveMessage ->
                defaultToastErrorMessage(R.string.error_chat_delete_message)

            is ChatMessageViewEvent.OnSuccessDeleteRoom -> {
                chatViewModel.deleteDraft(roomId = dialog?.roomId)
                requireActivity().onBackPressed()
            }

            is ChatMessageViewEvent.OnFailureDeleteRoom ->
                defaultToastErrorMessage(R.string.chat_room_remove_own_dialog_fail)

            is ChatMessageViewEvent.OnFailureDeleteRoomNotCreatedYet ->
                defaultToastErrorMessage(R.string.chat_room_remove_own_dialog_fail_not_created)

            is ChatMessageViewEvent.OnEventDeleteRoom -> chatDeletedMessageBar()
            is ChatMessageViewEvent.OnEventDeletedFromRoom -> handleOnEventDeletedFromRoom(event)
            is ChatMessageViewEvent.PlayReceivedMessage -> playReceivedMessageSound()
            is ChatMessageViewEvent.SetUnreadRestMessageCount ->
                setUnreadRestMessageCount(event.count)

            is ChatMessageViewEvent.OnLastInputMessage -> {
                binding.sendMessageContainer.etWrite.setText(event.lastText)
            }

            is ChatMessageViewEvent.OnScrollToMessage -> scrollToMessage(event.messageId)
            is ChatMessageViewEvent.OnSetUnsentMessages ->
                unsentMessageCounter = event.unsentMessageCount

            is ChatMessageViewEvent.UpdateCompanionAsUnblocked -> updateCompanionAsUnblocked(event)
            is ChatMessageViewEvent.OnFailEnableChatMessages ->
                defaultToastErrorMessage(R.string.personal_messages_toast_allow_messages_failed)

            is ChatMessageViewEvent.UpdateBirthdayTextSpannable -> {
                binding.sendMessageContainer.etWrite.addSpanBoldRangesClickColored(
                    rangeList = event.listRanges,
                    color = requireContext().color(R.color.uiKitBackAddOrange),
                    onClickListener = {
                        if (isKeyboardOpen) {
                            activityCallback?.onGetActionFromChat(ActivityInteractChatActions.ShowFireworkAnimation)
                        }
                    }
                )
            }

            is ChatMessageViewEvent.OnSuccessSubscribedToUser ->
                defaultToastSuccessMessage(R.string.subscribed_in_chat_success)

            is ChatMessageViewEvent.OnFailSubscribeToUser ->
                defaultToastErrorMessage(R.string.subscribe_error_message)

            is ChatMessageViewEvent.OnUpdateCompanionInfo -> checkBlockChat(event.companionData)
            is ChatMessageViewEvent.BlockUserResult -> displayBlockUserResult(isActionSuccess = event.isSuccess)
            is ChatMessageViewEvent.BlockReportResult -> handleBlockReportResultEvent(event)
            is ChatMessageViewEvent.OnWorkSubmitted -> observeSendMessageResult(event.workUuid)
            is ChatMessageViewEvent.OnEditMessageWorkSubmitted -> observeEditMessageResult(event.workUuid)
            is ChatMessageViewEvent.OnDraftFound -> setupDraft(event.draft)
            is ChatMessageViewEvent.OnAddedToFavorites -> showAddToFavoritesToast(event.mediaUrl)
            is ChatMessageViewEvent.OnEditMessageSuccess -> handleEditingChangedState(isEditing = false)
            is ChatMessageViewEvent.OnEditMessageError -> handleEditMessageError(event)
            is ChatMessageViewEvent.CheckEditedMediaItems -> handleEditMessageMediaCheck(event.urls)
            is ChatMessageViewEvent.StartMessageEditing -> handleEditingChangedState(isEditing = true)
            is ChatMessageViewEvent.FinishMessageEditing -> handleEditingChangedState(isEditing = false)
            is ChatMessageViewEvent.OnSetupAddToFavoritesAnimation -> setupAddToFavoritesAnimation(event.lottieUrl)
            is ChatMessageViewEvent.OnBlockCompanionFromChatRequest -> blockCompanionFromChatRequest()
            is ChatMessageViewEvent.OnBlockReportUserFromChat -> blockReportUserFromChat()
            is ChatMessageViewEvent.OnCopyMessageContent -> copyMessageContent(event.message)
            is ChatMessageViewEvent.OnDownloadImageVideoAttachment -> downloadImageVideoAttachment(event.message)
            is ChatMessageViewEvent.OnCopyImageAttachment -> copyImageAttachment(event.message)
            is ChatMessageViewEvent.OnShareMessageContent -> shareMessageContent(event.message)
            is ChatMessageViewEvent.OnForbidCompanionToChat -> forbidCompanionToChat()
            is ChatMessageViewEvent.OnMessageDelete -> deleteMessageHandleEvent(event.message)
            is ChatMessageViewEvent.OnMessageEdit -> editMessageHandleEvent(event.message)
            is ChatMessageViewEvent.OnMessageForward -> meeraShowForwardMessageBottomSheet(event.message)
            is ChatMessageViewEvent.OnMessageReply -> messageReplyHandleEvent(event.message)
            is ChatMessageViewEvent.ShowMessageReplyTooltip -> showReplyMessageTooltip(event.message)
            is ChatMessageViewEvent.OnOpenChatComplaintMenu -> openComplaintMenuForDialogChat()
            is ChatMessageViewEvent.OnResendAllMessages -> resendAllMessages(event.unsentMessageCounter)
            is ChatMessageViewEvent.OnResendSingleMessage -> resendSingleMessage(event.message)
            is ChatMessageViewEvent.OnSendFavoriteRecent -> sendFavoriteRecentMessage(
                event.favoriteRecentUiModel,
                event.type
            )

            is ChatMessageViewEvent.OnSetupMediaPreview -> setupMediaPreview(
                event.media,
                event.isMeeraMenu,
                event.menuHeight
            )

            is ChatMessageViewEvent.OnOpenChatComplaintRequestMenu -> openChatRequestComplaintMenu()
            is ChatMessageViewEvent.OnSetChatBackground -> setChatBackground(event.backgroundType)
            is ChatMessageViewEvent.OnGreetingStickerFound -> setupGreetingSticker(event.sticker)
            is ChatMessageViewEvent.ShowDialogComplaintGroupChat -> openComplaintMenuForGroupChat()
            is ChatMessageViewEvent.OnGroupChatBlocked -> setGroupChatBlockByModerator()
            is ChatMessageViewEvent.OnPlayVoiceMessage -> Unit
            is ChatMessageViewEvent.OnPlayMeeraVoiceMessage -> playMeeraVoiceMessage(
                event.startPos,
                event.messagesQueue
            )

            is ChatMessageViewEvent.OnShowMaxSelectedMediaCountErrorMessage -> showMaxMediaCountErrorMessage()
            is ChatMessageViewEvent.OnHideBottomDownloadMediaProgress -> hideBottomLoadingProgress()
            is ChatMessageViewEvent.OnShareContent -> handleShareContentViewEvent(event)
            is ChatMessageViewEvent.OnFailShareContent -> showCommonError(
                getText(R.string.error_try_later),
                requireView()
            )

            is ChatMessageViewEvent.OnCopyAttachmentImageMessage -> saveImageFileToClipboard(event.attachment)
            is ChatMessageViewEvent.ShowMeeraCompletelyRemoveMessageDialog -> {
                clickMessageBottomMenuDelegate?.showMeeraCompletelyRemoveMessageDialog(event.message)
            }

            else -> Unit
        }
    }

    private fun copyMessageContent(message: MessageEntity) {
        runOnUiThread {
            activeSnackBar = chatDialogDelegate?.showTextCopiedSnackbar()
        }

        if (!message.sent && message.isResendAvailable) {
            chatViewModel.handleAction(ChatActions.UnsentCopyMessageClicked)
        }
    }

    private fun editMessageHandleEvent(message: MessageEntity) {
        chatMenuDelegate?.abortMessageEdit(chatViewModel.isMessageEditActive()) {
            startEditingMessage(message)
        }
    }

    private fun deleteMessageHandleEvent(message: MessageEntity) {
        handleActionUnsentMessageDelete(message)
        if (currentInProgressMessage == null) {
            showConfirmDeleteMessageDialog(message)
        } else {
            cancelUploadMediaAndDeleteMessage(message)
        }
    }

    private fun handleActionUnsentMessageDelete(message: MessageEntity) {
        chatViewModel.handleAction(
            ChatActions.UnsentMessageDelete(
                message = message,
                roomType = dialog?.type.orEmpty(),
                companion = companion
            )
        )
    }

    private fun showConfirmDeleteMessageDialog(message: MessageEntity) {
        chatMenuDelegate?.deleteMessageDialog(
            message = message,
            ownUserId = chatViewModel.getUserUid(),
            onClickRemove = {
                this.currentInProgressMessage = null
            }
        )
    }

    private fun cancelUploadMediaAndDeleteMessage(message: MessageEntity) {
        currentInProgressMessage?.let { inProgressMessage ->
            WorkManager.getInstance(requireContext()).cancelWorkById(UUID.fromString(inProgressMessage.workId))
            val messageId = inProgressMessage.messageId
            roomId?.let {
                chatViewModel.handleAction(ChatActions.CompletelyRemoveMessage(it, messageId))
                chatViewModel.handleAction(ChatActions.RemoveOnlyNetworkMessage(message.roomId, message.msgId))
            }
            this.currentInProgressMessage = null
        }
    }

    private fun messageReplyHandleEvent(message: MessageEntity) {
        parentMessage = message
    }

    private fun showReplyMessageTooltip(message: MessageUiModel) {
        chatMenuDelegate?.abortMessageEdit(chatViewModel.isMessageEditActive()) {
            replyHelper?.showReply(message) {
                chatViewModel.scrollToMessage(message.id)
            }
            openChatKeyboard()
        }
    }

    private fun handleEditingChangedState(isEditing: Boolean) {
        if (isEditing) {
            stickersSuggestionsDelegate?.enableSuggestions(false)
            val originalMessage = chatViewModel.getMessageInEdit() ?: return
            binding.sendMessageContainer.apply {
                btnMediaFiles.isVisible = !originalMessage.isRepost()
                btnGifUpload.gone()
                etWrite.clearText()
                etWrite.append(originalMessage.tagSpan?.text ?: originalMessage.content)
            }
            openChatKeyboard()
            editMessagePreviewDelegate?.showPreview(
                message = originalMessage,
                onPreviewClicked = { chatViewModel.scrollToMessage(originalMessage.msgId) },
                onCancelled = { chatViewModel.clearMessageEditor() }
            )
            showMessageEditingButton()
        } else {
            binding.sendMessageContainer.apply {
                btnMediaFiles.visible()
                btnGifUpload.visible()
                etWrite.clearText()
            }
            stickersSuggestionsDelegate?.enableSuggestions(true)
            editMessagePreviewDelegate?.clearAndHide()
            gifMenuDelegate?.hideAllOpenedKeyboards()
            showRecordMessageButton()
        }
        val newGifMenuState = if (isEditing) GifMenuState.MESSAGE_EDITING else GifMenuState.DEFAULT
        gifMenuDelegate?.changeMenuState(newGifMenuState)
    }

    private fun handleEditMessageError(event: ChatMessageViewEvent.OnEditMessageError) {
        if (!event.isEditTooLate) {
            chatMenuDelegate?.showMessageEditErrorAlert()
        } else {
            showCommonError(getText(R.string.chat_edit_message_too_late), requireView())
        }
    }

    private fun handleEditMessageMediaCheck(urls: List<String>) {
        lifecycleScope.launch {
            val uris = urls.map(Uri::parse)
            val isVideoNeedEdit =
                act.getMediaControllerFeature().needEditMedia(uri = uris[0], openPlace = MediaControllerOpenPlace.Chat)
            if (uris.isEmpty()) return@launch
            if (chatViewModel.getMediaType(uris[0]) == MEDIA_TYPE_VIDEO &&
                isVideoNeedEdit == MediaControllerNeedEditResponse.NoNeedToEdit
            ) {
                openMediaEditor(
                    uri = uris.first(),
                    onVideoReady = {
                        (requireActivity().application as App).apply {
                            it.path?.let(hashSetVideoToDelete::add)
                        }
                        chatViewModel.clearMessageMedias()
                        chatViewModel.addMessageMedias(listOf(it))
                        chatViewModel.sendEditedMessage()
                    }
                )
            } else {
                chatViewModel.sendEditedMessage()
            }
        }
    }

    private fun openMediaEditor(
        uri: Uri,
        onPhotoReady: ((Uri) -> Unit)? = null,
        onVideoReady: (Uri) -> Unit,
    ) = activityCallback?.onProvideMediaEditorControllerToChat()?.open(
        uri = uri,
        callback = object : MediaControllerCallback {
            override fun onVideoReady(resultUri: Uri, nmrAmplitude: NMRVideoAmplitude?) {
                chatViewModel.handleAction(ChatActions.OnVideoEdits(nmrAmplitude))
                onVideoReady(resultUri)
            }

            override fun onPhotoReady(resultUri: Uri, nmrAmplitude: NMRPhotoAmplitude?) {
                chatViewModel.handleAction(ChatActions.OnPhotoEdits(nmrAmplitude))
                onPhotoReady?.invoke(resultUri)
            }
        },
        openPlace = MediaControllerOpenPlace.Chat,
    )

    private fun handleBlockReportResultEvent(event: ChatMessageViewEvent.BlockReportResult) {
        if (event.reportResult == ComplaintFlowResult.CANCELLED) {
            displayBlockUserResult(event.isBlockSuccess)
        } else {
            displayBlockReportResult(event.reportResult == ComplaintFlowResult.SUCCESS && event.isBlockSuccess)
        }
    }

    private fun displayBlockReportResult(isActionSuccess: Boolean) {
        if (isActionSuccess) {
            activeSnackBar = chatDialogDelegate?.blockReportResultSnackbar {
                chatViewModel.unblockUser(companion?.userId)
            }
        } else {
            defaultToastErrorMessage(R.string.user_complain_error)
        }
    }

    private fun displayBlockUserResult(isActionSuccess: Boolean) {
        if (isActionSuccess) {
            activeSnackBar = chatDialogDelegate?.blockUserResultSnackbar {
                chatViewModel.unblockUser(companion?.userId)
            }
        } else {
            defaultToastErrorMessage(R.string.complaints_failed_to_block_user)
        }
    }

    private fun displayReportActionResult(result: ComplaintFlowResult) {
        when (result) {
            ComplaintFlowResult.SUCCESS -> {
                activeSnackBar = chatDialogDelegate?.reportActionResultSnackbar()
            }

            ComplaintFlowResult.FAILURE -> {
                defaultToastErrorMessage(R.string.user_complain_error)
            }

            else -> Unit
        }
    }

    private fun setupDraft(draft: DraftUiModel) {
        if (onStopFragmentWasCalled.not() && !draft.text.isNullOrEmpty()) {
            binding.sendMessageContainer.etWrite.setText(draft.text)
            openChatKeyboard()
        }
        if (draft.reply != null) {
            draft.reply?.let(::messageReplyHandleEvent)
        }
    }

    private fun updateCompanionAsUnblocked(event: ChatMessageViewEvent.UpdateCompanionAsUnblocked) {
        roomId?.let { roomId ->
            toolbarDelegate?.handleAction(
                ChatToolbarActionsUI.UpdateCompanion(
                    roomId,
                    event.userId
                )
            )
        }
        if (dialog?.approved != DialogApproved.ALLOW.key) {
            chatViewModel.chatRequestsAvailability(roomId, isAllow = true)
        }
    }

    private fun handleChatRequestViewEvent(event: ChatRequestViewEvent) {
        when (event) {
            is ChatRequestViewEvent.AllowChat -> {
                if (dialog?.approved != DialogApproved.ALLOW.key) {
                    dialog?.companion?.settingsFlags?.iCanGreet = true.toInt()
                }
                setupGreeting()
                dialog?.approved = DialogApproved.ALLOW.key
                chatViewModel.isRoomChatRequest = false
                changeChatRequestMenuStatus(isDialogAllowed = true)
                messageSwipeController?.isSwipeEnabled = isNotAnnouncementChat()
                chatViewModel.updateMessagesAsChatRequest(event.room.roomId, false)
            }

            is ChatRequestViewEvent.ForbidChatRequest -> {
                dialog?.companion?.settingsFlags?.iCanGreet = false.toInt()
                setupGreeting()
                dialog?.approved = DialogApproved.FORBIDDEN.key
                chatViewModel.isRoomChatRequest = true
                changeChatRequestMenuStatus(isDialogAllowed = false)
                messageSwipeController?.isSwipeEnabled = false
                checkBlockChat(event.room.companion)
                defaultToastSuccessMessage(R.string.personal_messages_toast_disallow_messages)
            }

            is ChatRequestViewEvent.NetworkErrorChatRequest -> {
                defaultToastErrorMessage(R.string.internet_connection_problem)
            }

            else -> Unit
        }
    }

    private fun handleActionSendMessage(event: ChatMessageViewEvent.ActionSendMessage) {
        // Unsent message counter for resend menu
        if (event.isSentError) {
            chatViewModel.getUnsentMessages(roomId)
            startAutoResend(event.messageId)
        }
        if (event.isSentError && event.resultMessage == null) {
            defaultToastErrorMessage(R.string.error_chat_send_message)
        }
        if (event.isSentError && event.resultMessage != null) {
            defaultToastErrorMessage(event.resultMessage)

            // TODO: Костыль для обработки ошибок по тексту сообщения (т.к. сервер не присылает коды ошибок)
            if (event.resultMessage == "can't send message, blocked") {
                showUserBlockedMessageBar()
            }
        }

        allowChatRequest()
    }

    private fun handleOnEventDeletedFromRoom(event: ChatMessageViewEvent.OnEventDeletedFromRoom) {
        if (event.needToShow) {
            chatDeletedMessageBar(true)
        } else {
            binding.bottomInfoBars.layoutChatDeleted.gone()
            if (binding.bottomInfoBars.layoutChatUserBlocked.visibility == View.VISIBLE
                || binding.bottomInfoBars.lChatDisallowByCompanion.root.visibility == View.VISIBLE
                || binding.bottomInfoBars.lChatDisallowByMe.root.visibility == View.VISIBLE
            ) return

            visibleInputBottomBar()

            if (binding.sendMessageContainer.etWrite.text.isNullOrEmpty()) {
                voiceRecordDelegate?.visibleVoiceRecordBtn()
            } else {
                showSendMessageButton()
            }
        }
    }

    private fun scrollToMessage(messageId: String) {
        val position = chatMessagesAdapter.currentList.indexOfFirst { it.messageData.id == messageId }
        if (position in 0 until chatMessagesAdapter.itemCount) {
            binding.rvChat.scrollWithHighlightingMessage(position)
        }
    }

    private fun showErrorPrivacySettings() {
        defaultToastErrorMessage(R.string.error_while_changing_status)
    }

    private fun setReadIncomingMessage(position: Int) {
        chatMessagesAdapter.getMessageItem(position)?.let { message ->
            val isNotRead = message.sendStatus != SendStatus.READ
            val isTypeMessage = message.chatItemType == CHAT_ITEM_TYPE_MESSAGE
            val isNotMyMessage = message.creator?.id != chatViewModel.getUserUid()
            val isNotTrackedMessage = !shownMessageIds.contains(message.id)
            if (isNotRead && isTypeMessage && isNotMyMessage && isNotTrackedMessage) {
                chatViewModel.updateMeeraMessageReadDb(roomId, message)
                shownMessageIds.add(message.id)
            }
        }

    }

    @Deprecated("Old voice messages track")
    private fun trackAudioMessage(message: MessageEntity, onComplete: () -> Unit) {
        if (message.attachment.type == TYPING_TYPE_AUDIO
            && !trackVoiceMessageIds.contains(message.msgId)
        ) {
            trackVoiceMessageIds.add(message.msgId)
            onComplete.invoke()
        }
    }

    private fun trackAudioMessage(messageId: String, onComplete: () -> Unit) {
        if (trackVoiceMessageIds.contains(messageId).not()) {
            trackVoiceMessageIds.add(messageId)
            onComplete.invoke()
        }
    }


    private fun updateUnreadCounterRange(
        intRange: IntRange,
        adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    ) {
        for (pos in intRange) {
            if (pos >= 0 && adapter.itemCount > pos) {
                setReadIncomingMessage(pos)
            }
        }
    }

    /**
     * Observe scroll messages for message read status
     */
    private fun listMessagesScrollListener(
        recycler: RecyclerView,
        adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    ) {
        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recycler.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val firstItemPos = layoutManager.findFirstVisibleItemPosition()
                val lastItemPos = layoutManager.findLastVisibleItemPosition()

                scrollAboveUnreadDivider(firstItemPos)

                this@MeeraChatFragment.currentScrollPosition = firstItemPos

                updateUnreadCounterRange(firstItemPos..lastItemPos, adapter)

                if (visibleItemCount == 1) {
                    setReadIncomingMessage(firstItemPos)
                }
            }

            // Visibility unread message widget (right bottom corner)
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recycler.canScrollVertically(1)) {
                    binding.scrollDownButton.invisible()
                    unreadMessageWidgetIsVisible = false
                } else {
                    val layoutManager = recycler.layoutManager as LinearLayoutManager
                    // Bottom NOT reached
                    val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
                    if (firstVisiblePosition > UNSEEN_HOLDERS_COUNT_TO_SHOW_SCROLL_DOWN)
                        unreadMessageWidgetIsVisible = true
                    if (!isLockBtnVisible() && firstVisiblePosition > UNSEEN_HOLDERS_COUNT_TO_SHOW_SCROLL_DOWN) {
                        binding.scrollDownButton.visible()
                    }
                }
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    toolbarDelegate?.handleAction(ChatToolbarActionsUI.CloseToolbarMenu)
                }
            }
        })
    }

    /**
     * Scroll up on one position if unread message exists
     */
    private fun scrollAboveUnreadDivider(firstItemPos: Int) {
        if (firstItemPos > 0 && unreadMessageCounter > 0
            && !isScrollUnreadDivider && !isScrollUpUnreadDividerYet
        ) {
            isScrollUnreadDivider = true
            binding.rvChat.scrollToPosition(firstItemPos - 1)
            isScrollUpUnreadDividerYet = true

            val count = unreadMessageCounter - 1L
            binding.scrollDownButton.setCount(count.toInt())
        }

        isScrollUnreadDivider = !(firstItemPos == 0 && unreadMessageCounter > 0)
    }

    private var isRefreshedFirstMessage = false

    private fun renderChat(messages: List<ChatMessageDataUiModel>) {
        val preSubmitItemCount = chatMessagesAdapter.itemCount
        chatMessagesAdapter.submitList(messages) {
            currentListAdapterSize = messages.size
            scrollHandler(messages, preSubmitItemCount)
        }
        setupGreeting(messages.lastOrNull()?.messageData)
        refreshFirstMessage(messages)
    }

    private fun setupGreeting(lastMessage: MessageUiModel? = null) {
        val areMessagesEmpty = chatMessagesAdapter.itemCount == 0
        if (!areMessagesEmpty) {
            disableGreeting()
            return
        }

        val iCanGreet = dialog?.companion?.settingsFlags?.iCanGreet?.toBoolean() ?: true
        val lastMessageByCompanion = lastMessage != null && lastMessage.creator?.id != chatViewModel.getUserUid()
        val isGroupChat = dialog?.type == ROOM_TYPE_GROUP
        val shouldShowGreetBackView = shouldShowGreetBackView(
            isGreetingNotSent = !greetingSent,
            canIGreet = iCanGreet,
            isLastMessageByCompanion = lastMessageByCompanion,
            isNotGroupChat = !isGroupChat,
            isNotChatBlockedVisually = !chatViewModel.isChatBlockedVisually
        )
        val shouldShowGreetView = shouldShowGreetView(
            isGreetingNotSent = !greetingSent,
            canIGreet = iCanGreet,
            areMessagesEmpty = areMessagesEmpty,
            isNotGroupChat = !isGroupChat,
            isNotChatBlockedVisually = !chatViewModel.isChatBlockedVisually
        )
        val greetingString = when {
            shouldShowGreetBackView -> getString(R.string.greet_in_reply)
            shouldShowGreetView -> getString(R.string.greet)
            else -> String.empty()
        }
        chatGreetBlockAdapter.text = greetingString
        chatGreetBlockAdapter.needToShowGreeting = shouldShowGreetBackView || shouldShowGreetView
    }

    private fun shouldShowGreetBackView(
        isGreetingNotSent: Boolean,
        canIGreet: Boolean,
        isLastMessageByCompanion: Boolean,
        isNotGroupChat: Boolean,
        isNotChatBlockedVisually: Boolean,
    ): Boolean {
        return isGreetingNotSent &&
            canIGreet &&
            isLastMessageByCompanion &&
            isNotGroupChat &&
            isNotChatBlockedVisually
    }

    private fun shouldShowGreetView(
        isGreetingNotSent: Boolean,
        areMessagesEmpty: Boolean,
        canIGreet: Boolean,
        isNotGroupChat: Boolean,
        isNotChatBlockedVisually: Boolean,
    ): Boolean {
        return isGreetingNotSent &&
            canIGreet &&
            areMessagesEmpty &&
            isNotGroupChat &&
            isNotChatBlockedVisually
    }

    private fun scrollHandler(
        messages: List<ChatMessageDataUiModel>,
        preSubmitItemCount: Int
    ) {
        if (messages.isNotEmpty()) {
            val lastMsg = messages.firstOrNull()

            val isSingleMessage = lastMsg != null
                && (messages.size - preSubmitItemCount) == NEW_MESSAGE_DIFFERENCE
            val isFirstMessageToday = lastMsg != null
                && (messages.size - preSubmitItemCount) == FIRST_MESSAGE_TODAY_DIFFERENCE

            // if last message is NEW
            if (isSingleMessage || isFirstMessageToday) {
                val creatorUid = lastMsg?.messageData?.creator?.id
                // Скролл вниз, когда написано свое сообщение
                if (chatViewModel.getUserUid() == creatorUid) {
                    binding.rvChat.scrollToPosition(BOTTOM_SCROLL_POSITION)
                    binding.scrollDownButton.invisible()
                }
                // Скрол вниз, когда тебе написали сообщение и мы уже находимся внизу переписки
                if (currentScrollPosition == 0 && chatViewModel.getUserUid() != creatorUid) {
                    binding.rvChat.smoothScrollToPosition(BOTTOM_SCROLL_POSITION)
                }
            }
        }
    }

    // Refresh first message for update state if first Send gift
    private fun refreshFirstMessage(messages: List<ChatMessageDataUiModel>) {
        if (messages.size > 1 && !isRefreshedFirstMessage) {
            isRefreshedFirstMessage = true
            chatViewModel.refreshFirstMessage(roomId)
        }
    }

    /**
     * Observe unread messages by room unread data
     */
    private fun setUnreadMessageCounterWidget(roomId: Long) {
        chatViewModel.observeUnreadMessagesV2(roomId)
        chatViewModel.liveUnreadMessageCountRooms.observe(viewLifecycleOwner) { count ->
            handleUnreadCounterWidgetState(count)
            setRoomIsReadIfGettingSingleMessage(count)
        }
    }

    private fun handleUnreadCounterWidgetState(count: Long) {
        this.unreadMessageCounter = count
        val layoutManager = binding.rvChat.layoutManager as LinearLayoutManager
        if (count > 0 && layoutManager.findFirstVisibleItemPosition() > 0) {
            setVisibleUnreadWidget(count)
        } else {
            setInvisibleUnreadWidget(count)
        }

    }

    /**
     * появление последнего сообщения нужен для проставки статусов при первом заходе,
     * когда onScroll не отрабатывает
     */
    private fun setRoomIsReadIfGettingSingleMessage(count: Long) {
        if (count == SINGLE_INCOMING_MESSAGE && currentScrollPosition == 0) {
            chatViewModel.markRoomAsRead(roomId)
        }
    }

    private fun setVisibleUnreadWidget(count: Long) {
        binding.scrollDownButton.apply {
            visible()
            setCount(count.toInt())
        }
    }

    private fun setInvisibleUnreadWidget(count: Long) {
        binding.scrollDownButton.apply {
            setCount(count.toInt())
            invisible()
        }
    }

    private fun sendTyping(roomId: Long?) {
        binding.sendMessageContainer.etWrite.textChanges()
            .filterNot { it.isNullOrBlank() }
            .filter { text ->
                text?.length == TYPING_START_CHAR_COUNT
                    || text?.length?.rem(TYPING_MULTIPLE_CHAR_COUNT) == 0
            }
            .onEach { text ->
                chatViewModel.sendTyping(roomId, TYPING_TYPE_TEXT)
            }
            .catch { Timber.e("TYPING Error et_write binding: $it") }
            .launchIn(lifecycleScope)
    }

    override fun onStop() {
        super.onStop()
        onStopFragment()
        enablePush()
        currentSnackbar?.dismiss()
    }

    private fun onStopFragment() {
        closeChatKeyboard()
        onStopFragmentWasCalled = true
        enablePush()
        chatViewModel.updateFirstUnreadMessageTs(roomId)
        keepScreenOnDisable()
        chatViewModel.collapseAllVoiceMessagesText(roomId)
        if (chatViewModel.isRoomChatRequest) {
            chatViewModel.updateMessagesAsChatRequest(roomId, true)
        }
        chatViewModel.enableShake()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        gifMenuDelegate?.mediaKeyboard?.mediaKeyboardPagesPosition?.let { mediaKeyboardPagesPosition ->
            val behaviour = gifMenuDelegate?.getBehavior()
            if (mediaKeyboardPagesPosition == MediaKeyboardPagesPosition.DEFAULT && behaviour != null) {
                val lastPagePosition = gifMenuDelegate?.mediaKeyboard?.getLatestAdapterPosition() ?: 0
                chatViewModel.updateMediaKeyboardLatestState(behaviour, lastPagePosition)
            }

        }
        editMessagePreviewDelegate?.clearAndHide()
        editMessagePreviewDelegate = null
        toolbarDelegate = null
        chatViewModel.stopVoiceMessagesBd(roomId)
        executePendingBlockReportActionAndHideSnackBar()
        chatAttachmentsAdapter.unregisterAdapterDataObserver(photoAttachmentsAdapterDataObserver)
    }

    override fun onDestroy() {
        videoPreviewPlayer?.release()
        complainsNavigator.unregisterDialogChainListener()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_GIF_DISPLAYED, isGifDisplayed ?: false)
        outState.putParcelable(RECYCLER_VIEW_STATE_KEY, recyclerViewState)
    }

    private fun executePendingBlockReportActionAndHideSnackBar() {
        activeSnackBar?.dismissNoCallbacks()
        activeSnackBar = null
        infoSnackbar?.dismiss()
        infoSnackbar = null
    }

    //убираем рум айди из исключений для входящих пушей
    private fun enablePush() {
        roomId?.let { activityCallback?.onGetActionFromChat(ActivityInteractChatActions.EnablePush(it)) }
    }

    private fun setChatBackground(type: ChatBackgroundType) {
        when (type) {
            is ChatBackgroundType.None -> {
                binding.ivImageBackgroundOverlay.gone()
            }

            is ChatBackgroundType.Birthday,
            is ChatBackgroundType.Holiday,
            is ChatBackgroundType.RoomStyle -> {
                binding.ivImageBackgroundOverlay.visible()
                binding.ivImageBackgroundOverlay.loadGlide(type.background)
            }
        }
    }

    private fun handleSendMedia(images: MutableList<out Uri>?, text: String? = "") {
        if (images.isNullOrEmpty()) return

        //блокируем кнопку отправки на некоторе время, так как в момент открытия редактора есть промежуток
        //времени в который можно быстро прожать кнопку отправить https://nomera.atlassian.net/browse/BR-6045
        blockSendButton()

        lifecycleScope.launch {
            images.forEach { mediaUri ->
//                when (val editType =
//                    act.getMediaControllerFeature()
//                        .needEditMedia(uri = mediaUri, openPlace = MediaControllerOpenPlace.Chat)) {
//                    is MediaControllerNeedEditResponse.VideoTooLong -> {
//                        act.getMediaControllerFeature().showVideoTooLongDialog(
//                            openPlace = MediaControllerOpenPlace.Chat,
//                            needEditResponse = editType,
//                            showInMinutes = true,
//                            openEditorCallback = {
//                                openMediaEditor(mediaUri) {
//                                    handlePreparedMedia(listOf(it), text.orEmpty())
//                                }
//                            }
//                        )
//                        return@launch
//                    }
//
//                    MediaControllerNeedEditResponse.NeedToCrop -> {
//                        cropMedia(images, mediaUri, text.orEmpty())
//                        return@launch
//                    }
//
//                    else -> Unit
//                }
            }
            handlePreparedMedia(images, text.orEmpty())
        }
    }

    private fun cropMedia(
        mediaList: MutableList<out Uri>,
        mediaUriToCrop: Uri,
        text: String
    ) = openMediaEditor(
        uri = mediaUriToCrop,
        onPhotoReady = { resultUri ->
            val croppedMediaList = mediaList.mapTo(mutableListOf()) { uri ->
                if (uri == mediaUriToCrop) resultUri else uri
            }
            if (croppedMediaList.size == 1) {
                handlePreparedMedia(mediaList)
            } else {
                handleSendMedia(croppedMediaList, text)
            }
        },
        onVideoReady = {
            handlePreparedMedia(listOf(it), text)
        }
    )

    private fun handlePreparedMedia(mediaFiles: List<Uri>, text: String = "") {
        when {
            mediaFiles.size == 1 && chatViewModel.getMediaType(mediaFiles.first()) == MEDIA_TYPE_VIDEO -> {
                sendVideoMessage(mediaFiles.first(), text)
            }

            mediaFiles.all { uri ->
                when (chatViewModel.getMediaType(uri)) {
                    MEDIA_TYPE_IMAGE, MEDIA_TYPE_IMAGE_GIF -> true
                    else -> false
                }
            } -> {
                onImagesReady(mediaFiles, text)
            }
        }
    }

    //block button for a while
    private fun blockSendButton() {
        binding.sendMessageContainer.btnSend.isEnabled = false
        doDelayed(500) {
            binding.sendMessageContainer.btnSend.isEnabled = true
        }

    }

    private fun sendVideoMessage(resultUri: Uri, text: String?) {
        binding.sendMessageContainer.etWrite.setText(String.empty())
        isCompleteSendMessage = true
        disableGreeting()
        chatViewModel.sendVideoMessage(
            roomId = roomId,
            message = text ?: "",
            parentMessage = parentMessage,
            videoUri = resultUri,
            userId = dialog?.companion?.userId,
            roomType = dialog?.type ?: ROOM_TYPE_DIALOG,
        )
        pathImage = null
        imagesToSend = null
        chatViewModel.clearMediaContent()
    }

    private fun onImagesReady(images: List<Uri>, text: String) {
        pathImage = images[0].path
        imagesToSend = images
        binding.sendMessageContainer.etWrite.setText(text)
        binding.sendMessageContainer.btnSend.performClick()
    }

    private fun onGifReady(images: List<Uri>, aspectRatio: Double) {
        if (isCompleteSendMessage) {
            this.giphyAspectRatio = aspectRatio
            pathImage = images[0].path
            imagesToSend = images
            isSendWithoutText = true
            mediaKeyboardCategory = AmplitudePropertyChatMediaKeyboardCategory.GIPHY
            binding.sendMessageContainer.btnSend.performClick()
            isSendWithoutText = false
            this.giphyAspectRatio = networkImageAspectRatio
        }
    }

    override fun onResendMessageClicked(messageId: String?) {
        chatViewModel.onResendMenuShowed()
        chatViewModel.mediaKeyboardMessageLongClicked(messageId)
    }

    override fun onAttachmentClicked(message: MessageEntity?) {
        message?.let { msg ->
            val isAllowedToView = msg.isShowImageBlurChatRequest?.not() ?: false
            if (isAllowedToView) {
                val attachment = msg.attachment
                if (msg.attachment.type == MEDIA_VIDEO) {
                    openVideo(msg)
                } else {
                    chatNavigator.gotoProfilePhotoViewer(attachment.url)
                }
            }
        }
    }

    override fun disableImageBlur(messageId: String?) {
        chatViewModel.disableChatRequestImageBlur(messageId)
    }

    private val orientationScreenListener = object : OrientationScreenListener() {
        override fun onOrientationChanged(orientation: Int) {
            orientationChangedListener.invoke(orientation)
        }
    }

    //Configuration need to configure mediaviewerView
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        orientationScreenListener.onOrientationChanged(newConfig.orientation)
    }

    private fun openVideo(message: MessageEntity) {
        val video = message.attachment.url
        Timber.d(video)
        val imageList = mutableListOf<ImageViewerData>()
        imageList.add(
            ImageViewerData(
                imageUrl = video,
                viewType = RecyclingPagerAdapter.VIEW_TYPE_VIDEO
            )
        )

        if (imageList.isNotEmpty()) {
            MediaViewer.with(context)
                .setImageList(imageList)
                .startPosition(0)
                .setOrientationChangedListener(orientationScreenListener)
                .onSaveImage { saveImage(it) }
                .onSaveVideo { url ->
                    toast(getString(R.string.saving_video))
                    context?.downloadFile(Uri.parse(url))
                }
                .onImageShare {
                    shareSingleMediaContent(message = message, isShareText = false)
                }
                .onVideoShare {
                    showLoadingMenu()
                    shareSingleMediaContent(message = message, isShareText = false)
                }
                .onDismissListener {}
                .setLifeCycle(lifecycle)
                .show()
        }
    }

    private fun showLoadingMenu() {
        shareProgressBottomSheet = MeeraChatShareProgressDialogMenu()
        shareProgressBottomSheet?.show(childFragmentManager)
    }

    private fun initChatMenuDelegate() {
        chatMenuDelegate = MeeraChatMenuDelegate(
            fragment = this,
            onAction = { chatViewModel.handleAction(it) }
        )
    }

    private fun initClickMessageBottomMenuDelegate() {
        clickMessageBottomMenuDelegate = MeeraClickMessageBottomMenuDelegate(
            fragment = this,
            featureToggles = getFeatureToggles(),
            onAction = { chatViewModel.handleAction(it) }
        )
    }

    private fun initChatInfoDialogDelegate() {
        chatDialogDelegate = ChatInfoDialogDelegate(
            context = requireContext(),
            view = requireView()
        )
    }

    private fun isEditMessageAvailable(message: MessageEntity): Boolean {
        return (getChatMessageEditFeatureToggle()?.isEnabled == true
            && message.isValidForEdit()
            && chatViewModel.isMyMessage(message)
            && (companion?.isNotLockedMessages() == true ||
            (dialog?.type == ROOM_TYPE_GROUP
                && !chatViewModel.wasRemovedFromRoom
                && !chatViewModel.wasCalledDeleteRoom)))
    }

    private fun startEditingMessage(message: MessageEntity) {
        chatViewModel.startEditingMessage(message)
    }

    private fun observeMessageEditorState() {
        chatViewModel.getMessageEditorState().observe(viewLifecycleOwner) { state ->
            val buttonAlpha = when {
                !state.isEmptyMessage || state.isRepostMessage -> 1f
                else -> 0.3f
            }
            binding.sendMessageContainer.ivConfirmEdit.alpha = buttonAlpha
            binding.sendMessageContainer.ivConfirmEdit.isEnabled = buttonAlpha == 1f
        }
    }

    private fun meeraShowForwardMessageBottomSheet(message: MessageEntity) {
        chatViewModel.forwardMessageClicked()
        MeeraShareSheet().showByType(
            fm = childFragmentManager,
            shareType = ShareDialogType.MessageForwarding(message.toUIShareMessage()),
            event = { event ->
                when (event) {
                    is ShareBottomSheetEvent.OnSuccessForwardChatMessage -> {
                        currentSnackbar = UiKitSnackBar.make(
                            view = requireView(),
                            params = SnackBarParams(
                                snackBarViewState = SnackBarContainerUiState(
                                    messageText = getString(R.string.chat_fwd_message_success, event.text),
                                    avatarUiState = AvatarUiState.SuccessIconState
                                )
                            )
                        )
                        currentSnackbar?.show()
                    }

                    is ShareBottomSheetEvent.OnErrorForwardChatMessage -> {
                        defaultToastErrorMessage(event.message)
                    }

                    is ShareBottomSheetEvent.OnFailForwardChatMessage -> {
                        defaultToastErrorMessage(R.string.chat_fwd_message_error)
                    }

                    is ShareBottomSheetEvent.OnClickFindFriendButton -> {
                        chatNavigator.gotoSearchMainFragment()
                    }

                    else -> Unit
                }
            }
        )
    }

    private fun downloadImageVideoAttachment(msg: MessageEntity) {
        when (msg.attachment.type) {
            TYPING_TYPE_IMAGE,
            TYPING_TYPE_GIF -> {
                val imageUrl = msg.attachment.url
                saveImage(imageUrl)
            }

            TYPING_TYPE_VIDEO -> {
                val videoUrl = msg.attachment.url
                val isFileUrl = URLUtil.isFileUrl(videoUrl)
                if (isFileUrl) {
                    saveImage(videoUrl)
                    return
                }
                toast(getString(R.string.saving_video))
                context?.downloadFile(Uri.parse(videoUrl))
            }
        }
    }

    override fun onStateChanged(newState: Int) {
        super.onStateChanged(newState)
        when {
            newState == STATE_HIDDEN && !notFromMap -> {
                findNavController().popBackStack()
            }
        }
    }

    private fun copyImageAttachment(message: MessageEntity, attachmentsIndex: Int = 0) {
        val attachment = message.attachment
        when (attachment.type) {
            TYPING_TYPE_IMAGE, TYPING_TYPE_GIF -> {
                val ratio = attachment.ratio
                this.giphyAspectRatio = ratio
                this.networkImageAspectRatio = ratio
                val isLocalAttachmentFile = URLUtil.isFileUrl(attachment.url)
                if (isLocalAttachmentFile.not()) {
                    saveImageFileToClipboard(attachment)
                } else {
                    chatViewModel.handleAction(ChatActions.CopyImageMessageAttachment(message, attachmentsIndex))
                }
            }
        }
    }

    private fun saveImageFileToClipboard(attachment: MessageAttachment) {
        copyImageFile(
            attachment = attachment,
            act = requireActivity(),
            viewLifecycleOwner = viewLifecycleOwner,
            successListener = {
                runOnUiThread {
                    currentSnackbar = UiKitSnackBar.make(
                        view = requireView(),
                        params = SnackBarParams(
                            snackBarViewState = SnackBarContainerUiState(
                                messageText = getText(R.string.image_is_copied),
                                avatarUiState = AvatarUiState.SuccessIconState
                            ),
                        )
                    ).apply { show() }
                }
            }
        )
    }


    private fun shareMessageContent(message: MessageEntity) {
        binding.vgVideoPreview.gone()
        when (message.eventCode) {
            ChatEventEnum.TEXT.state -> shareTextContent(message)
            ChatEventEnum.IMAGE.state,
            ChatEventEnum.GIF.state,
            ChatEventEnum.VIDEO.state -> shareSingleMediaContent(message)

            ChatEventEnum.LIST.state -> shareMultipleMediaContent(message)
        }
    }

    private fun shareTextContent(message: MessageEntity) {
        val shareContentType = ShareContentTypes.TextContent(message)
        chatViewModel.handleAction(ChatActions.ShareContent(shareContentType))
    }

    private fun shareSingleMediaContent(message: MessageEntity, isShareText: Boolean = true) {
        val shareContentType = ShareContentTypes.SingleMedia(message, isShareText)
        chatViewModel.handleAction(ChatActions.ShareContent(shareContentType))
    }

    private fun observeBottomLoadingProgress() {
        chatViewModel.liveDownloadMediaProgress.distinctUntilChanged().observe(viewLifecycleOwner) { progress ->
            shareProgressBottomSheet?.setLoadingProgress(progress)
        }
    }

    private fun shareMultipleMediaContent(message: MessageEntity) {
        val shareContentType = ShareContentTypes.MultipleMedias(message)
        chatViewModel.handleAction(ChatActions.ShareContent(shareContentType))
    }

    private fun hideBottomLoadingProgress() {
        shareProgressBottomSheet?.dismiss()
    }

    private fun handleShareContentViewEvent(event: ChatMessageViewEvent.OnShareContent) {
        if (event.isDismissProgress) {
            hideBottomLoadingProgress()
        }
        messageLongClickBottomMenu?.dismiss()
        startActivity(Intent.createChooser(event.intent, getString(R.string.app_name)))
    }

    private fun startAutoResend(messageId: String) {
        checkRoomExistsBeforeResend { roomId ->
            roomId?.let { _roomId ->
                checkRoomExistsBeforeResend {
                    chatViewModel.resendMessage(
                        ResendType.ResendByMessageId(messageId, _roomId)
                    ).observeResendResult()
                }
            }
        }
    }

    private fun resendSingleMessage(message: MessageEntity) {
        chatViewModel.onMessageResendClicked(message, dialog?.type.orEmpty(), companion)
        checkConnectionBeforeResend {
            checkRoomExistsBeforeResend { roomId ->
                roomId?.let { _roomId ->
                    checkRoomExistsBeforeResend {
                        chatViewModel.resendMessage(
                            ResendType.ResendByMessageId(message.msgId, _roomId)
                        ).observeResendResult()
                    }
                }
            }
        }
    }

    private fun resendAllMessages(messagesCount: Int) {
        chatViewModel.resendAllMessagesClicked(messagesCount)
        checkConnectionBeforeResend {
            checkRoomExistsBeforeResend { roomId ->
                roomId?.let { _roomId ->
                    chatViewModel.resendMessage(
                        ResendType.ResendByRoomId(_roomId)
                    ).observeResendResult()
                }
            }
        }
    }

    private fun LiveData<WorkInfo>.observeResendResult() {
        this.observe(viewLifecycleOwner) { workInfo ->
            if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                chatViewModel.getUnsentMessages(roomId)
            }
        }
    }

    private fun checkConnectionBeforeResend(connected: () -> Unit) {
        if (chatViewModel.isInternetConnected()) {
            connected()
        } else {
            defaultToastErrorMessage(R.string.error_chat_send_message)
        }
    }

    /**
     * Переотправка возможна только по roomId
     * Если это первые сообщения и roomId ещё неизвестен
     * надо перезагрузить диалог
     */
    private fun checkRoomExistsBeforeResend(block: (roomId: Long?) -> Unit) {
        block(roomId)
    }

    override fun onMessageLongClicked(messageId: String?, messageView: View?) {
        Timber.d("onMessageLongClicked messageId: $messageId")
        closeChatKeyboard()
        doDelayed(DELAY_KEYBOARD_CHANGE_STATE) {
            isNotChatRequestOrAnnouncementChat {
                requireContext().lightVibrate()
                chatViewModel.onResendMenuShowed()
                chatViewModel.mediaKeyboardMessageLongClicked(messageId, messageView)
            }
        }
    }

    override fun onClickReplyParentMessage(messageId: String) {
        Timber.d("onClickReplyParentMessage called with messageId: $messageId")
        chatViewModel.scrollToReplyParentMessage(messageId)
    }

    override fun onVoiceMessageLongClicked(
        messageId: String?,
        messageView: View?,
        recognizedText: String?
    ) {
        requireContext().lightVibrate()
        isNotChatRequestOrAnnouncementChat {
            chatViewModel.mediaKeyboardMessageLongClicked(
                messageId = messageId,
                view = messageView,
                voiceRecognizedText = recognizedText
            )
        }
    }

    override fun onExpandVoiceMessageText(message: MessageEntity, isExpanded: Boolean) {
        chatViewModel.onVoiceMessageExpandTextState(message, isExpanded)
    }

    override fun onExpandBtnAnimationCompleteVoiceMessage(message: MessageEntity) = Unit

    override fun onImageClicked(
        messageId: String,
        data: List<PostImage>,
        childPosition: Int,
    ) {
        chatViewModel.getFromCacheById(messageId)?.let { msg ->
            val isAllowedToView = msg.isShowImageBlurChatRequest?.not() ?: false
            if (isAllowedToView) {
                closeChatKeyboard()
                val imageList = mutableListOf<ImageViewerData>()
                data.forEach {
                    imageList.add(
                        ImageViewerData(
                            imageUrl = it.url,
                            viewType = when (msg.itemType) {
                                ITEM_TYPE_VIDEO_SEND,
                                ITEM_TYPE_VIDEO_RECEIVE -> RecyclingPagerAdapter.VIEW_TYPE_VIDEO

                                else -> RecyclingPagerAdapter.VIEW_TYPE_IMAGE
                            }
                        )
                    )
                }
                if (imageList.isNotEmpty()) {
                    chatViewModel.disableShake()
                    mediaViewer = MediaViewer.with(context)
                        .setImageList(imageList)
                        .onSaveImage {
                            saveImage(it)
                        }
                        .onDismissListener(chatViewModel::enableShake)
                        .onImageCopy { path ->
                            copyAttachmentFromMediaViewer(msg, path)
                        }
                        .onImageShare { url ->
                            handleImageShareThroughMediaPicker(msg, url)
                        }
                        .onVideoShare { url ->
                            handleImageShareThroughMediaPicker(msg, url)
                        }
                        .onAddToFavorite { position, isInFavorites ->
                            if (isInFavorites) {
                                chatViewModel.removeFromFavoritesByUrl(
                                    url = if (imageList.size == 1)
                                        msg.attachment.url
                                    else
                                        msg.attachments[position].url
                                )
                            } else {
                                chatViewModel.addToFavoritesFromMessage(
                                    message = msg,
                                    position = if (imageList.size == 1) null else position
                                )
                            }
                        }
                        .onGetFavorites { chatViewModel.currentMediaFavorites }
                        .startPosition(childPosition)
                        .setMeeraAct(requireActivity() as MeeraAct)
                        .show()
                }
            }
        }
    }

    private fun handleImageShareThroughMediaPicker(message: MessageEntity, chosenUrl: String) {
        when (message.eventCode) {
            ChatEventEnum.IMAGE.state,
            ChatEventEnum.VIDEO.state,
            ChatEventEnum.GIF.state -> shareSingleMediaContent(message = message, isShareText = false)

            ChatEventEnum.LIST.state -> {
                message.attachments.find { it.url == chosenUrl }?.let { attachment ->
                    val convertedMessage = message.copy(
                        eventCode = ChatEventEnum.IMAGE.state,
                        attachment = attachment
                    )
                    shareSingleMediaContent(message = convertedMessage, isShareText = false)
                }
            }
        }
    }

    private fun copyAttachmentFromMediaViewer(
        message: MessageEntity,
        pickedUri: String
    ) {
        if (message.attachments.isNotEmpty()) {
            message.attachments.forEachIndexed { index, messageAttachment ->
                if (messageAttachment.url == pickedUri) {
                    val updMessage = message.copy(attachment = messageAttachment)
                    copyImageAttachment(updMessage, attachmentsIndex = index)
                }
            }
        } else {
            copyImageAttachment(message)
        }
    }

    override fun onShowMoreRepost(postId: Long) {
        chatNavigator.gotoPostFragment(postId)
    }

    override fun onShowRepostMoment(momentId: Long) {
        chatNavigator.gotoMomentFragment(momentId = momentId)
    }

    /**
     * Есть кейс: в текстовом сообщении, состоящем только из упоминания,
     * при long click на текст упоминания появляется нижнее контекстное
     * меню, а затем происходит переходит в профиль который упомянули.
     *
     * Пока что пофиксил баг добавив метод [isMenuBottomSheetShown], но лучше
     * было бы переделать класс [MovementMethod], добавив слушатели на обычный
     * и long click. В этой либе хороший пример https://github.com/saket/Better-Link-Movement-Method
     *
     * https://github.com/saket/Better-Link-Movement-Method/blob/master/better-link-movement-method/src/main/java/me/saket/bettermovementmethod/BetterLinkMovementMethod.java
     * */
    override fun onUniquenameClicked(userId: Long?) {
        if (isMenuBottomSheetShown()) {
            return
        }
        openProfileScreen(userId)
    }

    override fun onUniquenameUnknownProfileError() {
        if (isMenuBottomSheetShown()) {
            return
        }
        defaultToastErrorMessage(R.string.uniqname_unknown_profile_message)
    }

    private fun isMenuBottomSheetShown(): Boolean {
        return fragmentManager?.findFragmentByTag(MeeraMenuBottomSheet::class.simpleName) != null
    }

    override fun onHashtagClicked(hashtag: String?) {
        chatNavigator.gotoHashTagFragment(hashtag)
    }

    override fun onLinkClicked(url: String?) {
        activityCallback?.onGetActionFromChat(ActivityInteractChatActions.OpenLink(url))
    }

    override fun onAvatarClicked(userId: Long?) {
        chatNavigator.gotoUserInfoFragment(userId = userId, where = AmplitudePropertyWhere.PROFILE_SHARE)
    }

    override fun onCommunityClicked(groupId: Int?, isDeleted: Boolean) {
        if (!isDeleted) {
            chatViewModel.logCommunityScreenOpened()
            chatNavigator.gotoCommunityFragment(groupId)
        } else {
            defaultToastErrorMessage(R.string.community_unavailable)
        }
    }

    override fun onReceiverGiftClicked(myUid: Long) {
        chatNavigator.gotoUserGiftFragment(myUid = myUid, birth = chatViewModel.getSavedDateOfBirth())
    }

    override fun onSenderGiftClicked() {
        chatNavigator.gotoUserGiftFragment(companion)
    }

    override fun onChooseGiftClicked(userId: Long) {
        chatViewModel.logSendGiftBack()
        dialog?.companion?.let { user ->
            chatNavigator.gotoGiftListFragment(user, true)
        }
    }

    @Deprecated("Old voice messages")
    override fun onVoicePlayClicked(message: MessageEntity?, position: Int) = Unit

    private fun playMeeraVoiceMessage(
        startPosition: Int,
        messagesQueue: LinkedList<PlayMeeraMessageDataModel>
    ) {
        runCatching {
            val view: View? = binding.rvChat.layoutManager?.findViewByPosition(startPosition)
            val voiceCell: UiKitVoiceView? = view?.allViews?.first { it is UiKitVoiceView } as? UiKitVoiceView

            val voiceMessagePositions = findVoiceMessagePositions(startPosition)
            val recalculatedQueue = recalculatePlayVoicePositions(messagesQueue, voiceMessagePositions)

            if (voiceCell != null) {
                playQueueMeeraVoiceMessages(recalculatedQueue)
            }
        }.onFailure {
            Timber.e("MEERA_VOICE_MSG_LOG ERROR found positions:$it")
        }
    }

    private fun findVoiceMessagePositions(startPosition: Int): List<Int> {
        val result = mutableListOf<Int>()
        for (pos in startPosition downTo 0) {
            val view: View? = binding.rvChat.layoutManager?.findViewByPosition(pos)
            if (view is UiKitMessagesContainerView) result.add(pos)
        }
        return result.toList()
    }

    private fun recalculatePlayVoicePositions(
        messageQueue: LinkedList<PlayMeeraMessageDataModel>,
        adapterPositions: List<Int>
    ): LinkedList<PlayMeeraMessageDataModel> {
        val updatedQueue = LinkedList<PlayMeeraMessageDataModel>()
        messageQueue.forEachIndexed { index, message ->
            val updatedMessage = message.copy(position = adapterPositions[index])
            updatedQueue.add(updatedMessage)
        }
        return updatedQueue
    }

    private fun playQueueMeeraVoiceMessages(messagesQueue: LinkedList<PlayMeeraMessageDataModel>) {
        val view: View? = binding.rvChat.layoutManager?.findViewByPosition(messagesQueue.first.position)
        val voiceCell: UiKitVoiceView? = view?.allViews?.first { it is UiKitVoiceView } as? UiKitVoiceView
        voiceCell?.let { cell ->
            val messageDataUiModel = messagesQueue.first().message
            val voiceMessageUrl =
                messageDataUiModel.messageData.attachments?.attachments?.first()?.url ?: String.empty()
            meeraVoiceMessagePlayer?.clickPlay(cell, messageDataUiModel, voiceMessageUrl) {
                messagesQueue.pollFirst()
                if (messagesQueue.isEmpty()) return@clickPlay
                playQueueMeeraVoiceMessages(messagesQueue)
            }
        }
    }

    private fun keepScreenOnEnable() {
        activity?.keepScreenOnEnable()
    }

    private fun keepScreenOnDisable() {
        activity?.keepScreenOnDisable()
    }

    override fun onVoiceMessagebinded(
        voiceMessageView: VoiceMessageView?,
        message: MessageEntity,
        isIncomingMessage: Boolean
    ) = Unit

    override fun onBindMeeraVoiceMessage(
        cell: UiKitVoiceView,
        data: ChatMessageDataUiModel,
        isAudioFileExists: Boolean
    ) {
        if (isAudioFileExists.not()) {
            trackAudioMessage(data.messageData.id) {
                setChatVoiceButtonConfig(cell, data, VoiceButtonState.InfinityProgressDownload)
                chatViewModel.downloadVoiceMessage(
                    model = data.messageData,
                    isNeedRefresh = true
                )
            }
        }
    }

    override fun onDownloadClickMeeraVoiceMessage(cell: UiKitVoiceView, data: ChatMessageDataUiModel?) {
        data?.messageData?.let { message ->
            chatViewModel.downloadVoiceMessage(
                model = message,
                isNeedRefresh = true,
                isTapDownloadButton = true
            )
        }
    }

    override fun onPlayClickMeeraVoiceMessage(cell: UiKitVoiceView, data: ChatMessageDataUiModel?, position: Int) {
        chatViewModel.handleAction(ChatActions.PlayMeeraVoiceMessage(data?.messageData, position))
    }

    override fun onPauseClickMeeraVoiceMessage(cell: UiKitVoiceView, data: ChatMessageDataUiModel?, position: Int) {
        data?.messageData?.let { message ->
            val voiceMessageUrl = message.attachments?.attachments?.first()?.url ?: String.empty()
            meeraVoiceMessagePlayer?.pauseVoiceMessage(voiceMessageUrl)
        }
    }

    override fun onClickMeeraVoiceMessageProgress(cell: UiKitVoiceView, progress: Int) {
        meeraVoiceMessagePlayer?.handlePlayProgress(cell, progress)
    }

    override fun onStopDownloadClickMeeraVoiceMessage(cell: UiKitVoiceView, data: ChatMessageDataUiModel?) {
        chatViewModel.cancelDownloadVoiceMessage()
    }

    override fun onSetNoMediaPlaceholderMessage(messageId: String) {
        chatViewModel.setNoMediaPlaceholderMessage(messageId)
    }

    override fun onBirthdayTextClicked() {
        activityCallback?.onGetActionFromChat(ActivityInteractChatActions.ShowFireworkAnimation)
    }

    override fun onShowPostClicked(postId: Long?) {
        chatViewModel.markPostAsNotSensitiveForUser(postId)
    }

    override fun onDeletedMessageLongClicked(messageId: String?) {
        chatViewModel.showMeeraCompletelyRemoveMessageDialog(messageId ?: return)
    }

    private fun checkPermissions(permission: String, vararg permissions: String) {
        when {
            arrayOf(permission)
                .plus(permissions)
                .any { ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), it) } -> {
                toastAlertMessageWithButton(
                    stringRes = R.string.you_must_grant_permissions,
                    buttonStringRes = R.string.allow,
                    onButtonClick = { requireActivity().openSettingsScreen() }
                )
            }

            else -> {
                setPermissions(
                    listener = object : PermissionDelegate.Listener {
                        override fun onGranted() = Unit
                        override fun onDenied() = Unit
                        override fun onError(error: Throwable?) {
                            defaultToastErrorMessage(R.string.access_is_denied)
                        }
                    },
                    permission = permission,
                    permissions = permissions
                )
            }
        }
    }

    private fun checkIfTechnicalAccount(room: DialogEntity) {
        val isSupportAccount = room.companion.userRole == UserRole.SUPPORT_USER
        chatToolTipsController?.areCallTooltipsEnabled = !isSupportAccount
        val isAnnounceAccount = room.companion.userRole == UserRole.ANNOUNCE_USER
        if (isAnnounceAccount) {
            chatToolTipsController?.areTooltipsEnabled = false
            binding.apply {
                vgWhiteContainer.gone()
                voiceRecordMotionContainer.gone()
                uniquenameMenuContainer.gone()
                vgSendContainerShadow.invisible()
                vgSendContainer.gone()
                vgSendContainer.newHeight(0.dp)
                ivImageBackgroundOverlay.newHeight(0.dp)
            }
        }
    }

    private fun openProfileScreen(userId: Long?) {
        chatNavigator.gotoUserInfoFragment(
            userId = userId,
            where = AmplitudePropertyWhere.CHAT
        )
    }

    private fun showSuccessEnabledNotifications() {
        defaultToastSuccessMessage(R.string.notifications_is_on)
    }

    private fun showSuccessDisabledNotifications() {
        defaultToastSuccessMessage(R.string.notifications_is_off)
    }

    private fun initInputFocusChangeListener() {
        binding.sendMessageContainer.etWrite.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                toolbarDelegate?.handleAction(ChatToolbarActionsUI.CloseToolbarMenu)
            }
        }
    }

    private fun scrollToPositionWithOffset() {
        val lm = binding.rvChat.layoutManager as? LinearLayoutManager
        lm?.scrollToPositionWithOffset(0, currentListAdapterSize)
    }

    private fun playSendMessageSound() {
        if (context?.getSilentState() != false || onStopFragmentWasCalled) return
        effectMediaPlayer.stop()
        effectMediaPlayer.reset()
        val afd = resources.openRawResourceFd(R.raw.oneshot4) ?: return
        effectMediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        afd.close()
        effectMediaPlayer.prepare()
        effectMediaPlayer.isLooping = false
        effectMediaPlayer.start()
    }

    private fun playReceivedMessageSound() {
        if (context?.getSilentState() != false || onStopFragmentWasCalled) return
        effectMediaPlayer.stop()
        effectMediaPlayer.reset()
        val afd = resources.openRawResourceFd(R.raw.oneshot3) ?: return
        effectMediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        afd.close()
        effectMediaPlayer.prepare()
        effectMediaPlayer.isLooping = false
        effectMediaPlayer.start()
    }

    // After call method checkRestMessages()
    private fun setUnreadRestMessageCount(count: Long) {
        isScrollUnreadDivider = false
        unreadMessageCounter = count
    }

    private fun isNotAnnouncementChat() = dialog?.companion?.userRole != UserRole.ANNOUNCE_USER
    private fun isNotChatRequest() = dialog == null || dialog?.approved == DialogApproved.ALLOW.key
    private fun isChatRequest() = !isNotChatRequest()

    private fun isNotChatRequestOrAnnouncementChat(result: () -> Unit) {
        if (isNotChatRequest() && isNotAnnouncementChat()) {
            result.invoke()
        }
    }

    private fun Context.downloadFile(uri: Uri) {
        val appName = getString(R.string.app_name)
        val dirPicture = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            appName
        )
        if (!dirPicture.exists()) {
            dirPicture.mkdirs()
        }
        val downloadingFile = File(dirPicture.absolutePath + File.separator + uri.lastPathSegment)
        val request: DownloadManager.Request = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            DownloadManager.Request(uri)
                .setTitle(uri.lastPathSegment)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(Uri.fromFile(downloadingFile))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
        } else {
            DownloadManager.Request(uri)
                .setTitle(uri.lastPathSegment)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(Uri.fromFile(downloadingFile))
                .setAllowedOverRoaming(true)
        }
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        downloadManager?.enqueue(request)
    }

    private fun changeChatRequestMenuStatus(isDialogAllowed: Boolean) {
        toolbarDelegate?.handleAction(
            ChatToolbarActionsUI.ChangeChatRequestMenuStatus(
                isChatRequest = dialog.isChatRequest(),
                isGroupChat = dialog?.type == ROOM_TYPE_GROUP,
                isDialogAllowed = isDialogAllowed,
                isSubscribed = dialog?.companion?.settingsFlags?.subscription_on.toBoolean(),
                isBlocked = dialog?.companion?.blacklistedByMe.toBoolean() ||
                    dialog?.companion?.blacklistedMe.toBoolean(),
                hasConversationStarted = chatMessagesAdapter.currentList.isNotEmpty()
            )
        )
    }

    private fun defaultToastSuccessMessage(@StringRes stringRes: Int) {
        currentSnackbar = UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(stringRes),
                    avatarUiState = AvatarUiState.SuccessIconState
                ),
            )
        ).also { snackBar ->
            snackBar.setAnchorView(binding.vgSendContainer)
            snackBar.show()
        }
    }

    private fun defaultToastErrorMessage(@StringRes stringRes: Int) {
        currentSnackbar = UiKitSnackBar.makeError(
            view = requireView(),
            params = SnackBarParams(
                errorSnakeState = ErrorSnakeState(
                    messageText = getText(stringRes)
                )
            )
        ).apply {
            setAnchorView(binding.vgSendContainer)
        }
        currentSnackbar?.show()
    }

    private fun defaultToastErrorMessage(text: String) {
        currentSnackbar = UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = text,
                    avatarUiState = AvatarUiState.ErrorIconState,
                )
            )
        )
        currentSnackbar?.show()
    }

    private fun toastAlertMessageWithButton(
        @StringRes stringRes: Int,
        @StringRes buttonStringRes: Int,
        onButtonClick: () -> Unit = {}
    ) {
        currentSnackbar = UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(stringRes),
                    avatarUiState = AvatarUiState.SuccessIconState,
                    buttonActionText = getText(buttonStringRes),
                    buttonActionListener = onButtonClick
                )
            )
        )
        currentSnackbar?.show()
    }

    private fun saveImage(imageUrl: String) {
        saveImageOrVideoFile(
            imageUrl = imageUrl,
            act = requireActivity(),
            viewLifecycleOwner = viewLifecycleOwner,
            successListener = {}
        )
    }

    private fun setActivityCallback(context: Context) {
        try {
            activityCallback = context as ActivityInteractionChatCallback
            onActivityCallInteraction = context as MeeraCallDelegate.OnActivityCallInteraction
        } catch (e: ClassCastException) {
            Timber.e(e)
            throw ClassCastException("$context must implement interface ActivityInteractionChatCallback or OnActivityCallInteraction")
        }
    }

    private fun setComplainFlowInteraction(context: Context) {
        try {
            complaintFlowInteraction = context as ComplaintFlowInteraction
        } catch (e: ClassCastException) {
            Timber.e(e)
            throw ClassCastException("$context must implement interface ComplaintFlowInteraction")
        }
    }

    private fun closeChatKeyboard(delay: Long = DELAY_KEYBOARD_CHANGE_STATE) {
        doDelayed(delay) {
            if (isAdded) activity?.currentFocus?.hideKeyboard() ?: context?.hideKeyboard(requireView())
        }
    }

    private fun openChatKeyboard(delay: Long = DELAY_KEYBOARD_CHANGE_STATE) {
        doDelayed(delay) {
            if (isAdded) activity?.currentFocus?.showKeyboard() ?: context?.showKeyboard(requireView())
        }
    }

    private fun disableSendMessageBar() {
        binding.apply {
            sendMessageContainer.layoutGroupChatChatbox.gone()
            voiceRecordMotionContainer.gone()
            voiceRecordProcessContainer.root.gone()
        }
    }

    private fun showMaxMediaCountErrorMessage() {
        infoSnackbar = UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(R.string.maximum_photo_files_selected),
                    avatarUiState = AvatarUiState.WarningIconState,
                )
            )
        ).also { uiKitSnackBar ->
            uiKitSnackBar.setAnchorView(binding.vgSendContainer)
            uiKitSnackBar.show()
        }
    }

    enum class TransitFrom {
        CHAT_REQUEST, CHAT_REQUEST_SINGLE_ROOM, GROUP_CHAT, OTHER
    }
}
