@file:Suppress("UNNECESSARY_SAFE_CALL")

package com.numplates.nomera3.modules.comments.ui.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListener
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.addAnimationTransitionByDefault
import com.meera.core.extensions.addSpanBoldRangesClickColored
import com.meera.core.extensions.animateHeight
import com.meera.core.extensions.animateHorizontalMargins
import com.meera.core.extensions.applyRoundedOutline
import com.meera.core.extensions.clearText
import com.meera.core.extensions.click
import com.meera.core.extensions.color
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.doOnUIThread
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.getNavigationBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.goneAnimation
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.invisible
import com.meera.core.extensions.isFalse
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.lightVibrate
import com.meera.core.extensions.returnReadExternalStoragePermissionAfter33
import com.meera.core.extensions.returnWriteExternalStoragePermissionAfter33
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setListener
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.toInt
import com.meera.core.extensions.vibrate
import com.meera.core.extensions.visible
import com.meera.core.extensions.visibleAnimation
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.blur.BlurHelper
import com.meera.core.utils.checkAppRedesigned
import com.meera.core.utils.graphics.SpanningLinearLayoutManager
import com.meera.core.utils.pagination.RecyclerPaginationUtil
import com.meera.core.views.MeeraEditTextExtended
import com.meera.db.models.message.ParsedUniquename
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.action.UiKitSnackBarActions
import com.meera.uikit.snackbar.state.PaddingState
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.buttons.ButtonType
import com.meera.uikit.widgets.nav.UiKitToolbarViewState
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.meera.uikit.widgets.snackbar.SnackLoadingUiState
import com.noomeera.nmrmediatools.extensions.hideKeyboard
import com.numplates.nomera3.App
import com.numplates.nomera3.POST_START_VIDEO_DELAY
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentPostv2Binding
import com.numplates.nomera3.modules.auth.ui.IAuthStateObserver
import com.numplates.nomera3.modules.auth.util.AuthStatusObserver
import com.numplates.nomera3.modules.auth.util.needAuth
import com.numplates.nomera3.modules.baseCore.helper.AUDIO_FEED_HELPER_VIEW_TAG
import com.numplates.nomera3.modules.baseCore.helper.AudioFeedHelper
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegate
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegateImpl
import com.numplates.nomera3.modules.baseCore.helper.ViewHolderAudio
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyMenuAction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.comments.AmplitudePropertyCommentMenuAction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.findfriends.AmplitudeFindFriendsWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.model.MapSnippetCloseMethod
import com.numplates.nomera3.modules.baseCore.helper.amplitude.post.AmplitudePropertySaveType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudePropertyReactionWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.createAmplitudeReactionsParams
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.createMeeraReactionSourcePost
import com.numplates.nomera3.modules.chat.helpers.replymessage.ReplySwipeController
import com.numplates.nomera3.modules.chat.helpers.replymessage.SwipeControllerActions
import com.numplates.nomera3.modules.chat.helpers.replymessage.SwipingItemType
import com.numplates.nomera3.modules.comments.data.api.OrderType
import com.numplates.nomera3.modules.comments.data.entity.CommentEntityResponse
import com.numplates.nomera3.modules.comments.ui.adapter.ICommentsActionsCallback
import com.numplates.nomera3.modules.comments.ui.adapter.MeeraCommentAdapter
import com.numplates.nomera3.modules.comments.ui.adapter.MeeraPostDetailAdapter
import com.numplates.nomera3.modules.comments.ui.entity.CommentChunk
import com.numplates.nomera3.modules.comments.ui.entity.CommentEntity
import com.numplates.nomera3.modules.comments.ui.entity.CommentUIType
import com.numplates.nomera3.modules.comments.ui.entity.PostDetailsMode
import com.numplates.nomera3.modules.comments.ui.util.SpeedyLinearLayoutManager
import com.numplates.nomera3.modules.comments.ui.viewholder.CommentViewHolderPlayAnimation
import com.numplates.nomera3.modules.comments.ui.viewmodel.MeeraPostViewModelV2
import com.numplates.nomera3.modules.comments.ui.viewmodel.PostDetailsActions
import com.numplates.nomera3.modules.common.ActivityToolsProvider
import com.numplates.nomera3.modules.communities.utils.copyCommunityLink
import com.numplates.nomera3.modules.complains.ui.ComplainEvents
import com.numplates.nomera3.modules.complains.ui.ComplainsNavigator
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.feed.data.entity.PostMediaViewInfo
import com.numplates.nomera3.modules.feed.domain.mapper.toPost
import com.numplates.nomera3.modules.feed.ui.CacheUtil
import com.numplates.nomera3.modules.feed.ui.MeeraPostCallback
import com.numplates.nomera3.modules.feed.ui.adapter.FeedType
import com.numplates.nomera3.modules.feed.ui.adapter.MediaLoadingState
import com.numplates.nomera3.modules.feed.ui.adapter.MeeraContentActionBar
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.entity.MediaAssetEntity
import com.numplates.nomera3.modules.feed.ui.entity.PostTypeEnum
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.entity.UIPostUpdate
import com.numplates.nomera3.modules.feed.ui.mapper.toUIPostUpdate
import com.numplates.nomera3.modules.feed.ui.viewholder.MeeraBasePostHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.MeeraMultimediaPostHolder
import com.numplates.nomera3.modules.feedmediaview.ui.fragment.ARG_VIEW_MULTIMEDIA_ASSET_ID
import com.numplates.nomera3.modules.feedmediaview.ui.fragment.ARG_VIEW_MULTIMEDIA_ASSET_TYPE
import com.numplates.nomera3.modules.feedmediaview.ui.fragment.ARG_VIEW_MULTIMEDIA_DATA
import com.numplates.nomera3.modules.feedmediaview.ui.fragment.ARG_VIEW_MULTIMEDIA_POST_ID
import com.numplates.nomera3.modules.feedmediaview.ui.fragment.ARG_VIEW_MULTIMEDIA_VIDEO_DATA
import com.numplates.nomera3.modules.maps.ui.MapUiActionHandler
import com.numplates.nomera3.modules.maps.ui.events.navigation.model.EventNavigationInitUiModel
import com.numplates.nomera3.modules.maps.ui.events.participants.list.EventParticipantsListFragment
import com.numplates.nomera3.modules.maps.ui.events.snippet.EventSnippetPage
import com.numplates.nomera3.modules.maps.ui.events.snippet.EventSnippetPageContent
import com.numplates.nomera3.modules.maps.ui.model.MapUiAction
import com.numplates.nomera3.modules.maps.ui.snippet.model.SnippetState
import com.numplates.nomera3.modules.maps.ui.view.MapSnippetPage
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentClickOrigin
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_MOMENT_CLICK_ORIGIN
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_USER_ID
import com.numplates.nomera3.modules.newroads.VideoFeedHelper
import com.numplates.nomera3.modules.newroads.data.ISensitiveContentManager
import com.numplates.nomera3.modules.newroads.fragments.BaseRoadsFragment
import com.numplates.nomera3.modules.newroads.ui.adapter.QuickAnswerAdapter
import com.numplates.nomera3.modules.post_view_statistic.data.PostViewRoadSource
import com.numplates.nomera3.modules.post_view_statistic.presentation.PostCollisionDetector
import com.numplates.nomera3.modules.posts.ui.model.PostHeaderEvent
import com.numplates.nomera3.modules.posts.ui.model.PostHeaderNavigationMode
import com.numplates.nomera3.modules.posts.ui.model.PostHeaderUiModel
import com.numplates.nomera3.modules.reaction.data.MeeraReactionUpdate
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingAnimationPlayListener
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingReaction
import com.numplates.nomera3.modules.reaction.ui.custom.MeeraReactionBubble
import com.numplates.nomera3.modules.reaction.ui.custom.ReactionBottomMenuItem
import com.numplates.nomera3.modules.reaction.ui.data.MeeraReactionSource
import com.numplates.nomera3.modules.reaction.ui.getDefaultReactionContainer
import com.numplates.nomera3.modules.reaction.ui.mapper.toMeeraContentActionBarParams
import com.numplates.nomera3.modules.reaction.ui.util.getMyReaction
import com.numplates.nomera3.modules.reaction.ui.util.reactionCount
import com.numplates.nomera3.modules.reactionStatistics.ui.MeeraReactionsStatisticsBottomDialogFragment
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsEntityType
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.SPEED_ANIMATION_CHANGING_HEIGHT_MLS
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.redesign.fragments.main.SUBSCRIPTION_ROAD_REQUEST_KEY
import com.numplates.nomera3.modules.redesign.fragments.main.map.MainMapFragment
import com.numplates.nomera3.modules.redesign.fragments.main.map.MeeraEventNavigationBottomsheetDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.main.map.snippet.MeeraEventPostPageFragment
import com.numplates.nomera3.modules.redesign.fragments.main.map.snippet.SNIPPET_DEFAULT_HEIGHT
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.modules.redesign.util.needAuthToNavigate
import com.numplates.nomera3.modules.redesign.util.needAuthToNavigateWithResult
import com.numplates.nomera3.modules.redesign.util.setExpandedState
import com.numplates.nomera3.modules.redesign.util.setHiddenState
import com.numplates.nomera3.modules.remotestyle.presentation.formatter.AllRemoteStyleFormatter
import com.numplates.nomera3.modules.screenshot.delegate.SAVING_PICTURE_DELAY
import com.numplates.nomera3.modules.screenshot.delegate.ScreenshotPopupController
import com.numplates.nomera3.modules.screenshot.ui.entity.ScreenshotPlace
import com.numplates.nomera3.modules.screenshot.ui.entity.ScreenshotPopupData
import com.numplates.nomera3.modules.screenshot.ui.fragment.ScreenshotTakenListener
import com.numplates.nomera3.modules.search.ui.fragment.SearchMainFragment
import com.numplates.nomera3.modules.share.ui.model.SharingDialogMode
import com.numplates.nomera3.modules.tags.data.entity.SpanDataClickType
import com.numplates.nomera3.modules.tags.data.entity.TagOrigin
import com.numplates.nomera3.modules.tags.ui.base.SuggestionsMenu
import com.numplates.nomera3.modules.tags.ui.entity.UITagEntity
import com.numplates.nomera3.modules.uploadpost.ui.entity.NotAvailableReasonUiEntity
import com.numplates.nomera3.modules.user.ui.fragments.AdditionalComplainCallback
import com.numplates.nomera3.modules.user.ui.fragments.UserComplainAdditionalBottomSheet
import com.numplates.nomera3.modules.userprofile.ui.fragment.MeeraUserInfoFragment.Companion.USERINFO_OPEN_FROM_NOTIFICATION
import com.numplates.nomera3.modules.viewvideo.presentation.ARG_VIEW_VIDEO_DATA
import com.numplates.nomera3.modules.viewvideo.presentation.ARG_VIEW_VIDEO_POST
import com.numplates.nomera3.modules.viewvideo.presentation.ARG_VIEW_VIDEO_POST_ID
import com.numplates.nomera3.modules.viewvideo.presentation.data.ViewVideoInitialData
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.modules.volume.presentation.VolumeStateCallback
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FEED_POST_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FEED_POST_NEED_TO_UPDATE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FROM_EVENT_SNIPPET
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FROM_MAP
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GROUP_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_IS_POST_FRAGMENT_CALLED_FROM_GROUP
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_NEED_TO_REPOST
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_NEED_TO_SHOW_HIDE_POSTS_BTN
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_TRANSIT_FROM
import com.numplates.nomera3.presentation.utils.ReactionAnimationHelper
import com.numplates.nomera3.presentation.utils.handleSpanTagsInPosts
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.numplates.nomera3.presentation.view.ui.BottomSheetDialogEventsListener
import com.numplates.nomera3.presentation.view.ui.CloseTypes
import com.numplates.nomera3.presentation.view.ui.MeeraPullToRefreshLayout
import com.numplates.nomera3.presentation.view.ui.VideoViewHolder
import com.numplates.nomera3.presentation.view.ui.bottomMenu.COMMENT_MENU_TAG
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import com.numplates.nomera3.presentation.view.ui.bottomMenu.POST_MENU_TAG
import com.numplates.nomera3.presentation.view.ui.bottomMenu.ReactionsStatisticBottomMenu
import com.numplates.nomera3.presentation.view.utils.sharedialog.IOnSharePost
import com.numplates.nomera3.presentation.view.utils.sharedialog.MeeraShareBottomSheetData
import com.numplates.nomera3.presentation.view.utils.sharedialog.MeeraShareSheet
import com.numplates.nomera3.presentation.view.utils.sharedialog.SharePostBottomSheet
import com.numplates.nomera3.presentation.view.utils.zoomy.Zoomy
import com.numplates.nomera3.presentation.viewmodel.exception.Failure
import com.numplates.nomera3.presentation.viewmodel.viewevents.PostViewEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

private const val DELAY_SCROLLING = 200L

private const val ADAPTER_MAX_COUNT = 2
private const val SNIPPET_HORIZONTAL_MARGIN = 16
private const val SNIPPET_RADIUS = 16
private const val SNIPPET_BOTTOM_MARGIN = 32
private const val SNIPPET_ELEVATION = 3

/**
 * Show post screen
 */

private const val DELAY_DELETE_COMMENT_SECONDS = 5L
private const val DELAY_DELETE_COMMENT_MILLISECONDS = 5000
private const val MARGIN_TOP_FULL_SNIPPET = 20
private const val MAX_HEIGHT_EVENT_SNIPPET_PERCENT = 0.95f

class MeeraPostFragmentV2 :
    MeeraBaseDialogFragment(R.layout.fragment_postv2, ScreenBehaviourState.Full),
    ICommentsActionsCallback,
    IOnBackPressed,
    MeeraPostCallback,
    VolumeStateCallback,
    IAuthStateObserver,
    MeeraMenuBottomSheet.Listener,
    BasePermission by BasePermissionDelegate(),
    SaveMediaFileDelegate by SaveMediaFileDelegateImpl(),
    EventSnippetPageContent,
    ScreenshotTakenListener {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private var mCommentsAdapter: MeeraCommentAdapter? = null

    private var adapter: MeeraPostDetailAdapter? = null
    private val appLifecycleObserver = AppLifecycleObserver {
        adapter?.forceUpdatePost()
    }
    private val viewModel by viewModels<MeeraPostViewModelV2> {
        App.component.getViewModelFactory()
    }
    private val binding by viewBinding(FragmentPostv2Binding::bind)
    private val complainsNavigator by lazy(LazyThreadSafetyMode.NONE) { ComplainsNavigator(requireActivity()) }

    private val act: MeeraAct by lazy {
        activity as MeeraAct
    }

    private val __audio: AudioFeedHelper
        get() = viewModel.getAudioHelper()
    private var mainAdapter: ConcatAdapter? = null

    private var postId: Long? = null
    private var postItem: PostUIEntity? = null
    private var needToUpdate: Boolean = false
    private var postAdapterPosition: Int? = null
    private var roadType: Int? = BaseRoadsFragment.RoadTypeEnum.MAIN_ROAD.index
    private var needToShowHideRoadBtn = true
    private var postOrigin: DestinationOriginEnum? = null
    private var postHaveReactions: Boolean = false
    private var fromMap: Boolean = false
    private var isMapEvent: Boolean = false
    private var postLatestReactionType: ReactionType? = null

    private var snippetScrollListener: RecyclerView.OnScrollListener? = null
    private var snippetOnLayoutChangeListener: OnLayoutChangeListener? = null
    private var currentSnippetLayoutWidth: Int? = null
    private var isSnippetCurrentlyCollapsed: Boolean = false

    private var startVideoPosition: Long = 0

    private var isShowProgress = false
    private var isCalledFromGroup = false
    private var selectedComment: CommentEntityResponse? = null
    private var selectedCommentID: Long? = null
    private var isNeedToShowRepostBtn = true
    private var isHidenScrollDownBtn = true
    private var isFirstTimeStarted = true
    private var uniqueNameSuggestionMenu: SuggestionsMenu? = null
    private var isScreenshotPopupShown = false
    private var isSavingPostPhoto = false

    //видео в ленте
    private var videoHelper: VideoFeedHelper? = null

    private var defaultEnableSound: Boolean = false

    private var undoSnackbar: UiKitSnackBar? = null

    //По данному полю мы определяем к какому комментарию нам нужно проскролить
    //если мы переходим из уведомления или пуша
    private var commentId: Long? = null

    //список id юзеров которые были заблокированы при помощи меню заблокировать
    private var blockedUsersList = mutableSetOf<Long>()

    private var currentBottomMenu: MeeraMenuBottomSheet? = null

    private var postCollisionDetector: PostCollisionDetector? = null

    var itemTouchHelper: ItemTouchHelper? = null

    private var reactionAnimationHelper: ReactionAnimationHelper? = null

    private var optionsMenuEnabled = false
    private val DEFAULT_TOAST_BOTTOM_PADDING = 16.dp

    private val screenshotPopupDialogListener = object : BottomSheetDialogEventsListener {
        var isVideoPaused: Boolean = false
        var isMusicPaused: Boolean = false

        override fun onCreateDialog() {
            if (videoHelper?.isVideoPlaying().isTrue()) {
                isVideoPaused = true
                videoHelper?.pauseVideo()
            }
            if (getMusicHolder()?.isPlayingMusic() == true) {
                isMusicPaused = true
                getMusicHolder()?.pausePlayingMusic()
            }
            resetAllZoomViews()
        }

        override fun onDismissDialog(closeTypes: CloseTypes?) {
            if (isVideoPaused) {
                isVideoPaused = false
                videoHelper?.playVideo()
            }
            if (isMusicPaused) {
                isMusicPaused = false
                getMusicHolder()?.startPlayingMusic()
            }
        }
    }

    private fun initPostViewCollisionDetector(recyclerView: RecyclerView?) {
        if (recyclerView == null) {
            return
        }

        if (postCollisionDetector == null) {
            val postViewHighlightLiveData = viewModel.getPostViewHighlightLiveData()

            postCollisionDetector = PostCollisionDetector.create(
                detectTime = PostCollisionDetector.getDurationMsFromSettings(viewModel.getSettings()),
                postViewHighlightEnable = postViewHighlightLiveData.value ?: false,
                recyclerView = recyclerView,
                roadFragment = this,
                roadSource = PostViewRoadSource.Post,
                detectPostViewCallback = { postViewDetectModel ->
                    viewModel.detectPostView(postViewDetectModel)
                },
                postUploadPostViewsCallback = {
                    viewModel.uploadPostViews()
                }
            )

            postViewHighlightLiveData.observe(viewLifecycleOwner) { postViewHighlightEnable ->
                postCollisionDetector?.setPostViewHighlightEnable(postViewHighlightEnable)
            }
        }
    }

    override fun onEventPostUpdated(postUIEntity: PostUIEntity) {
        viewModel.updatePost(postUIEntity)
    }

    override fun getParentWidth(): Int? {
        return currentSnippetLayoutWidth
    }

    override fun onPageSelected() {
        triggerAction(PostDetailsActions.Refresh)
    }

    override fun onSnippetStateChanged(snippetState: SnippetState) {
        context?.hideKeyboard(requireView())
        viewModel.setSnippetState(snippetState)
        if (snippetState != SnippetState.Expanded) {
            binding?.rvPostsComments?.scrollToPosition(0)
        }
        setSwipeRefreshDirections(snippetState)
    }

    override fun onBirthdayTextClicked() {
        (requireActivity() as MeeraAct).showFireworkAnimation()
    }

    override fun onCommentLikeClick(comment: CommentEntityResponse) {
        val post = postItem ?: return
        val reactionSource = MeeraReactionSource.PostComment(
            postId = post.postId,
            postUserId = post.user?.userId,
            commentUserId = comment.user.userId,
            commentId = comment.id,
            originEnum = postOrigin
        )

        val toolsProvider = activity as? ActivityToolsProvider ?: return
        toolsProvider
            .getMeeraReactionBubbleViewController()
            .onSelectDefaultReaction(
                reactionSource = reactionSource,
                currentReactionsList = comment.reactions,
                forceDefault = false,
                reactionsParams = post.createAmplitudeReactionsParams(reactionSource.originEnum),
                isShouldVibrate = false
            )
    }

    override fun onSnippetStateChanged(isCollapsed: Boolean) {
        if (isSnippetCurrentlyCollapsed == isCollapsed) return
        isSnippetCurrentlyCollapsed = isCollapsed
        binding.root.setBackgroundResource(R.drawable.bg_bottomsheet_header)
        if (isCollapsed) {
            setupCollapsedSnippet()
        } else {
            setupExpandedSnippet()
        }
        val adaptersCount = mainAdapter?.adapters?.size
        adaptersCount ?: return
        if (isCollapsed && adaptersCount == ADAPTER_MAX_COUNT) {
            mCommentsAdapter?.let { mainAdapter?.removeAdapter(it) }
            adapter?.submitList(adapter?.currentList?.filter { it.feedType != FeedType.EMPTY_PLACEHOLDER }
                ?: emptyList())
        } else if (!isCollapsed && adaptersCount < ADAPTER_MAX_COUNT) {
            mCommentsAdapter?.let { mainAdapter?.addAdapter(it) }
            val currentList = adapter?.currentList ?: return
            if (viewModel.needAddCommentsEmptyPlaceholder(currentList.firstOrNull())) {
                adapter?.submitList(currentList.plus(PostUIEntity(feedType = FeedType.EMPTY_PLACEHOLDER)))
            }
        }
    }

    override fun onCommentLinkClick(url: String?) {
        val activity = activity as? MeeraAct ?: return
        activity.emitDeeplinkCall(url)
    }

    override fun onScreenshotTaken() {
        resetAllZoomViews()
        if (isSavingPostPhoto) return
        triggerAction(PostDetailsActions.GetPostDataForScreenshotPopup(postId, postItem?.event))
    }

    override fun onCommentDoubleClick(comment: CommentEntityResponse) {
        needAuthToNavigate {
            val post = postItem ?: return@needAuthToNavigate
            val toolsProvider = activity as? ActivityToolsProvider ?: return@needAuthToNavigate
            val isCurrentUserAlreadySetLike = comment.reactions.getMyReaction() == ReactionType.GreenLight

            mCommentsAdapter?.playCommentAnimation(
                commentId = comment.id,
                animation = CommentViewHolderPlayAnimation.PlayLikeOnDoubleClickAnimation
            )

            if (!isCurrentUserAlreadySetLike) {
                val reactionSource = MeeraReactionSource.PostComment(
                    postId = post.postId,
                    postUserId = post.user?.userId,
                    commentUserId = comment.user.userId,
                    commentId = comment.id,
                    originEnum = postOrigin
                )

                toolsProvider
                    .getMeeraReactionBubbleViewController()
                    .onSelectDefaultReaction(
                        reactionSource = reactionSource,
                        currentReactionsList = comment.reactions,
                        forceDefault = true,
                        isShouldVibrate = false
                    )
            }
        }
    }

    override fun onCommentPlayClickAnimation(commentId: Long) {
        mCommentsAdapter?.playCommentAnimation(
            commentId = commentId,
            animation = CommentViewHolderPlayAnimation.PlayLikeOnDoubleClickAnimation
        )
    }

    override fun onCommentReactionAppearAnimation(reactionEntity: ReactionEntity, anchorViewLocation: Pair<Int, Int>) {
        val reactionType = ReactionType.getByString(reactionEntity.reactionType) ?: return
        reactionType.resourcePopupAnimation ?: return
        reactionAnimationHelper?.playLottieAtPosition(
            recyclerView = binding.rvPostsComments,
            requireContext(),
            parent = binding.clPostDetailsContainer,
            reactionType = reactionType,
            x = anchorViewLocation.first.toFloat(),
            y = anchorViewLocation.second.toFloat()
        )
    }

    override fun onReactionClickToShowScreenAnimation(
        reactionEntity: ReactionEntity,
        anchorViewLocation: Pair<Int, Int>
    ) {
        val reactionType = ReactionType.getByString(reactionEntity.reactionType) ?: return
        reactionAnimationHelper?.playLottieAtPosition(
            recyclerView = binding.rvPostsComments,
            context = requireContext(),
            parent = binding.clPostDetailsContainer,
            reactionType = reactionType,
            x = anchorViewLocation.first.toFloat(),
            y = anchorViewLocation.second.toFloat()
        )
    }

    override fun onReactionBadgeClick(comment: CommentEntityResponse) {
        needAuthToNavigate {
            if (viewModel.getFeatureTogglesContainer().detailedReactionsForCommentsFeatureToggle.isEnabled) {
                MeeraReactionsStatisticsBottomDialogFragment.makeInstance(
                    entityId = comment.id,
                    entityType = ReactionsEntityType.COMMENT
                ) { destination ->
                    when (destination) {
                        is MeeraReactionsStatisticsBottomDialogFragment.DestinationTransition.UserProfileDestination -> {
                            openUserFragment(destination.userEntity.userId)
                        }
                    }
                }.show(childFragmentManager)
            } else {
                val sortedReactions = comment.reactions.sortedByDescending { reaction -> reaction.count }
                val menu = ReactionsStatisticBottomMenu(context)
                menu.addTitle(R.string.post_comment_reactions, sortedReactions.reactionCount())
                sortedReactions.forEachIndexed { index, value ->
                    menu.addReaction(value, index != sortedReactions.size - 1)
                }
                menu.show(childFragmentManager)
            }

            viewModel.logStatisticReactionsTap(AmplitudePropertyReactionWhere.COMMENT)
        }
    }

    override fun onPostSnippetExpandedStateRequested(post: PostUIEntity) {
        (parentFragment as? MapSnippetPage)?.setSnippetState(SnippetState.Expanded)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            postId = bundle.getLong(ARG_FEED_POST_ID)
            needToUpdate = bundle.getBoolean(ARG_FEED_POST_NEED_TO_UPDATE)
            fromMap = bundle.getBoolean(ARG_FROM_MAP, false)
            isMapEvent = bundle.getBoolean(ARG_FROM_EVENT_SNIPPET)
            postItem = arguments?.getParcelable(IArgContainer.ARG_FEED_POST)
            postAdapterPosition = arguments?.getInt(IArgContainer.ARG_FEED_POST_POSITION)
            roadType = arguments?.getInt(IArgContainer.ARG_FEED_ROAD_TYPE)
            needToShowHideRoadBtn = bundle.getBoolean(ARG_NEED_TO_SHOW_HIDE_POSTS_BTN, true)
            isNeedToShowRepostBtn = arguments?.getBoolean(
                ARG_NEED_TO_REPOST,
                true
            ) ?: true
            postOrigin = arguments?.getSerializable(IArgContainer.ARG_POST_ORIGIN) as? DestinationOriginEnum

            postLatestReactionType =
                arguments?.getSerializable(IArgContainer.ARG_POST_LATEST_REACTION_TYPE) as? ReactionType

            viewModel.paginationHelper.flyingReactionType =
                arguments?.getSerializable(IArgContainer.ARG_COMMENT_LAST_REACTION) as? ReactionType

            postHaveReactions = arguments?.getBoolean(IArgContainer.ARG_FEED_POST_HAVE_REACTIONS) ?: false

            startVideoPosition = arguments?.getLong(
                IArgContainer.ARG_TIME_MILLS,
                0
            ) ?: 0

            defaultEnableSound = arguments?.getBoolean(
                IArgContainer.ARG_DEFAULT_VOLUME_ENABLED,
                false
            ) ?: false

            isCalledFromGroup = arguments?.getBoolean(ARG_IS_POST_FRAGMENT_CALLED_FROM_GROUP, false)
                ?: false

            commentId = if (arguments?.containsKey(IArgContainer.ARG_COMMENT_ID) == true) {
                arguments?.getLong(IArgContainer.ARG_COMMENT_ID)
            } else null

            viewModel.paginationHelper.flyingReactionCommentId = commentId
        }
        isOpened = true

        if (isMapEvent) {
            altSheetBehaviour = ScreenBehaviourState.EventSnippet(percentHeight = MAX_HEIGHT_EVENT_SNIPPET_PERCENT)
        } else {
            altSheetBehaviour = ScreenBehaviourState.Full
        }

        if (fromMap) {
            super.isApplyNavigationConfig = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initReactionAnimationHelper()
        if (fromMap) checkForMap()
        if (isMapEvent) {
            initAsEventPostDetails()
            makeFullSnippet()
            return
        }
        when (parentFragment) {
            is MapSnippetPage -> initAsSnippet()
            is MainMapFragment -> initAsEventPostDetails()
            else -> initAsFullPost()
        }
    }

    override fun onBackFromEventClicked() {
        when {
            isMapEvent -> {
                findNavController().popBackStack()
            }

            fromMap -> {
                (parentFragment as? EventSnippetPage?)?.onBackFromEventClicked()
                NavigationManager.getManager().getForceUpdatedTopBehavior()?.setExpandedState()
            }
        }
    }

    private fun initReactionAnimationHelper() {
        reactionAnimationHelper = ReactionAnimationHelper()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
    }

    override fun onDetach() {
        super.onDetach()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(appLifecycleObserver)
    }

    private fun checkForMap() {
        binding.viewMapDrag.visible()
        binding.appBarLayout3.gone()
        binding.appBarLayout3.applyRoundedOutline(radius = SNIPPET_RADIUS.dp.toFloat())
        binding.srlPostFrg.setPadding(0, MARGIN_TOP_FULL_SNIPPET.dp, 0, 0)
        setupSnippetAppBarConstraints()
        addSnippetScrollListener()
    }

    private fun setupSnippetAppBarConstraints() {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.root)
        constraintSet.clear(R.id.srl_post_frg, ConstraintSet.TOP)
        constraintSet.connect(
            R.id.srl_post_frg,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP
        )
        constraintSet.clear(R.id.ll_not_available_post, ConstraintSet.TOP)
        constraintSet.connect(
            R.id.ll_not_available_post,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP
        )
        constraintSet.applyTo(binding.root)
    }

    private fun addSnippetScrollListener() {
        if (isMapEvent) return
        snippetScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val isScrolledToTop = !recyclerView.canScrollVertically(-1)
                if (isScrolledToTop) {
                    binding.appBarLayout3.goneAnimation()
                } else {
                    if (binding.appBarLayout3.isVisible) return
                    binding.appBarLayout3.visibleAnimation()
                }
            }
        }
        snippetScrollListener?.let { binding.rvPostsComments.addOnScrollListener(it) }
    }

    private fun removeSnippetScrollListener() {
        snippetScrollListener?.let { binding.rvPostsComments.removeOnScrollListener(it) }
    }

    private fun makeFullSnippet() {
        checkForMap()
        binding.root.setBackgroundResource(R.drawable.bg_bottomsheet_header)
    }

    private fun initAsFullPost() {
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)
        initVideoHelper()
        initSendBtn()
        initView()
        initReplyViews()
        initViewModel(PostDetailsMode.DEFAULT)
        initToolbar(postItem)
        initPrivacy(postItem)
        initRecycler()
        initObservers()
        initAuthObserver()
        initKeyboardBehavior()
        setSwipeRefreshDirections(null, isMapEvent)
    }

    private fun initKeyboardBehavior() {
        ViewCompat.setOnApplyWindowInsetsListener(requireView()) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            binding.clPostDetailsContainer.setMargins(bottom = if (imeVisible) imeHeight else 0)
            insets
        }
    }

    private fun initAsSnippet() {
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)
        setupLayoutChangeListener()
        initSendBtn()
        initView()
        hideTagsList()
        initViewModel(PostDetailsMode.EVENT_SNIPPET)
        initToolbar(postItem)
        initPrivacy(postItem)
        initRecycler()
        initObservers()
        initAuthObserver()
        setSwipeRefreshDirections((parentFragment as? MapSnippetPage)?.getSnippetState())
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            binding.textInputContainer.setMargins(
                bottom = if (imeVisible) imeHeight else 0,
                top = if (imeVisible) 4.dp else 0
            )
            insets
        }
    }

    private fun initAsEventPostDetails() {
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)
        initSendBtn()
        initView()
        hideTagsList()
        initViewModel(PostDetailsMode.EVENTS_LIST)
        initToolbar(postItem)
        initPrivacy(postItem)
        initRecycler()
        initObservers()
        initAuthObserver()
        setSwipeRefreshDirections(SnippetState.Expanded)
    }

    private fun setupCollapsedSnippet() {
        val snippetPeekHeight =
            (parentFragment as? MeeraEventPostPageFragment)?.snippetPeekHeight ?: SNIPPET_DEFAULT_HEIGHT.dp
        binding.root.maxHeight = snippetPeekHeight
        binding.vgCreateBlockMainContainer.gone()
        val snippetMaxHeight = binding.root.measuredHeight
        if (snippetMaxHeight <= snippetPeekHeight) return
        val bottomMargin = snippetMaxHeight - snippetPeekHeight + SNIPPET_BOTTOM_MARGIN.dp
        binding.root.animateHorizontalMargins(
            SNIPPET_HORIZONTAL_MARGIN.dp,
            SPEED_ANIMATION_CHANGING_HEIGHT_MLS
        )
        binding.root.setMargins(bottom = bottomMargin)
        binding.root.applyRoundedOutline(SNIPPET_HORIZONTAL_MARGIN.dp.toFloat())
        binding.root.elevation = SNIPPET_ELEVATION.dp.toFloat()
    }

    private fun setupExpandedSnippet() {
        binding.root.maxHeight = Integer.MAX_VALUE
        binding.root.animateHorizontalMargins(
            0,
            SPEED_ANIMATION_CHANGING_HEIGHT_MLS
        )
        binding.root.setMargins(bottom = 0)
        binding.root.applyRoundedOutline(0F)
        binding.root.elevation = SNIPPET_ELEVATION.dp.toFloat()
        initPrivacy(postItem)
    }

    private fun setupLayoutChangeListener() {
        snippetOnLayoutChangeListener = object : OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View?,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                if (isVisible.not() || userVisibleHint.not()) return
                val newWidth = right - left
                if (currentSnippetLayoutWidth == null) {
                    currentSnippetLayoutWidth = newWidth
                    return
                }
                if (newWidth != currentSnippetLayoutWidth) {
                    currentSnippetLayoutWidth = newWidth
                    updateRecyclerItemsWidth()
                }
            }
        }

        binding.root.addOnLayoutChangeListener(snippetOnLayoutChangeListener)
    }

    private fun finishSnippetSetup() {
        initReplyViews()
    }

    private fun updateRecyclerItemsWidth() {
        val adapter = adapter ?: return
        for (position in 0 until adapter.itemCount) {
            val holder = binding.rvPostsComments.findViewHolderForAdapterPosition(position)
            if (holder != null && holder is MeeraBasePostHolder) {
                holder.updateViewsWithPresetWidth()
            }
        }
    }

    private fun hideTagsList() {
        binding?.tagsList?.root?.gone()
    }

    private fun initViewModel(postDetailsMode: PostDetailsMode) {
        viewModel.init(
            postId = postId,
            post = postItem,
            scrollCommentId = commentId,
            originEnum = postOrigin,
            postDetailsMode = postDetailsMode,
            needToUpdate = needToUpdate
        )
    }

    private fun initVideoHelper() {
        videoHelper = VideoFeedHelper(requireContext(), this)
        videoHelper?.init()
    }

    private fun initPrivacy(post: PostUIEntity?) {
        if (post == null) return
        optionsMenuEnabled = true
        if (
            parentFragment is MeeraEventPostPageFragment &&
            (parentFragment as MeeraEventPostPageFragment).getSnippetState() != SnippetState.Expanded &&
            (parentFragment as MeeraEventPostPageFragment).getSnippetState() != SnippetState.DraggedByUser
        ) {
            return
        }
        // параметры для настройки плашек блокирования
        val isMeBlocked = post.user?.blackListedMe == true
        val isPostCommentable = post.isAllowedToComment
        val isPostDeleted = post.deleted.toBoolean()

        // если пользователь который смотрит пост в ЧС,
        // то показываем плашку чтоб вы заблокированы
        // (остальные параметры не проверяем ибо ЧС выше по уровню)
        if (isPostDeleted) {
            binding?.vgCreateBlockMainContainer?.gone()
            binding?.vgBlockedHolder?.gone()
        } else if (isMeBlocked) {
            binding?.tvBlockMessage?.text = getString(R.string.you_was_blocked_by_user)
            binding?.vgCreateBlockMainContainer?.invisible()
            binding?.vgBlockedHolder?.visible()
        } else {
            // если пользователь не в ЧС и пост можно комментить
            // то ставим плашку ввода сообщения видимой,
            // иначе меняем текст у плашки блокирования
            if (isPostCommentable) {
                binding?.vgCreateBlockMainContainer?.visible()
                binding?.vgBlockedHolder?.gone()
            } else {
                binding?.tvBlockMessage?.text = getString(R.string.comments_disabled)
                binding?.vgCreateBlockMainContainer?.gone()
                binding?.vgBlockedHolder?.visible()
            }
        }

        binding?.vgBlockedHolder?.outlineProvider = null
    }

    override fun initAuthObserver(): AuthStatusObserver = object : AuthStatusObserver(requireActivity(), this) {
        override fun onAuthState() {
            binding?.etWriteComment?.setOnClickListener(null)
            binding?.etWriteComment?.isFocusable = true
            binding?.etWriteComment?.isLongClickable = true
        }

        override fun onNotAuthState() {
            binding?.etWriteComment?.setOnClickListener {
                needAuthToNavigate { }
            }
            binding?.etWriteComment?.isFocusable = false
            binding?.etWriteComment?.isLongClickable = false
        }

        /**
         * Обновить экран сразу по окончанию процесса авторизации/регистрации
         */
        override fun onJustAuthEvent() = refresh()
    }

    private fun registerComplaintListener() {
        complainsNavigator.registerAdditionalActionListener(this) { result ->
            when {
                result.isSuccess -> showAdditionalStepsForComplain(result.getOrThrow())
                result.isFailure -> showToastMessage(
                    R.string.user_complain_error,
                    iconState = AvatarUiState.ErrorIconState
                )
            }
        }
    }

    private fun unregisterComplaintListener() {
        complainsNavigator.unregisterAdditionalActionListener()
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    private fun showAdditionalStepsForComplain(userId: Long) {
        val bottomSheet = UserComplainAdditionalBottomSheet.newInstance(userId).apply {
            callback = object : AdditionalComplainCallback {
                override fun onSuccess(msg: String?, reason: ComplainEvents) {
                    showToastMessage(msg.orEmpty())
                }

                override fun onError(msg: String?) {
                    showToastMessage(msg.orEmpty(), iconState = AvatarUiState.ErrorIconState)
                }
            }
        }
        bottomSheet.show(childFragmentManager, "UserComplainAdditionalBottomSheet")
    }


    private fun refresh() {
        triggerAction(PostDetailsActions.Refresh)
        postId?.let { viewModel.markComments(it) }
    }

    private fun initView() {
        binding.srlPostFrg.setOnRefreshListener(object : MeeraPullToRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                refresh()
            }
        })
        binding?.rvPostsComments?.visible()
        optionsMenuEnabled = false

        binding?.etWriteComment?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.ivSendComment.isEnabled = s.toString().trim().isNotEmpty()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        })

        binding?.btnScrollDown?.setThrottledClickListener {
            viewModel.getLastComments(postId)
        }
        binding?.etWriteComment?.setDefaultTextColor(R.color.uiKitColorForegroundPrimary)
    }

    private fun initReplyViews() {
        initQuickAnswerMenu()
        initMentionableEditText()
    }

    private fun initMentionableEditText() {
        binding?.etWriteComment?.setCheckUniqueNameStrategy(MeeraEditTextExtended.CheckUniqueNameStrategyAddComment())
        binding?.etWriteComment?.setOnNewUniqueNameAfterTextChangedListener(object :
            MeeraEditTextExtended.OnNewUniqueNameListener {
            override fun onNewUniqueName(uniqueName: String) {
                searchUsersByUniqueName(uniqueName)
            }
        })

        binding?.etWriteComment?.setOnUniqueNameNotFoundListener(object :
            MeeraEditTextExtended.OnUniqueNameNotFoundListener {
            override fun onNotFound() {
                if (uniqueNameSuggestionMenu?.isHidden == false) {
                    uniqueNameSuggestionMenu?.forceCloseMenu()
                }
            }
        })

        binding?.tagsList?.let { tagsList ->
            tagsList.root.visible()
            val bottomSheetBehavior = BottomSheetBehavior.from(tagsList.root as View)
            uniqueNameSuggestionMenu = SuggestionsMenu(this, isDarkMode = false)
            binding?.tagsList?.recyclerTags?.let { recyclerTags ->
                binding?.etWriteComment?.let { etWriteComment ->
                    uniqueNameSuggestionMenu?.init(
                        recyclerTags,
                        etWriteComment,
                        bottomSheetBehavior
                    )

                    uniqueNameSuggestionMenu?.onSuggestedUniqueNameClicked =
                        fun(userData: UITagEntity) {
                            replaceUniqueNameBySuggestion(userData)
                            uniqueNameSuggestionMenu?.forceCloseMenu()
                        }
                }
            }
        }
    }

    private var lastSearchUniqueNameOrHashtag: String? = null

    private fun replaceUniqueNameBySuggestion(userData: UITagEntity?) {
        userData?.uniqueName?.also { suggestedUniqueName ->
            lastSearchUniqueNameOrHashtag?.also { oldUniqueName ->
                binding?.etWriteComment?.replaceUniqueNameBySuggestion(
                    oldUniqueName,
                    suggestedUniqueName
                )
            }
        }
    }

    private var searchUniqueNameOrHashtagJob: Job? = null

    private fun searchUsersByUniqueName(uniqueName: String) {
        searchUniqueNameOrHashtagJob?.cancel()
        searchUniqueNameOrHashtagJob = lifecycleScope.launch {
            delay(300)
            lastSearchUniqueNameOrHashtag = uniqueName
            val uniqueNameWithoutPrefix = uniqueName.replace("@", "", true)
            uniqueNameSuggestionMenu?.searchUsersByUniqueName(uniqueNameWithoutPrefix)
        }
    }

    override fun onBackPressed(): Boolean {
        return uniqueNameSuggestionMenu?.onBackPressed() ?: false
    }

    private fun initQuickAnswerMenu() {
        val adapter = QuickAnswerAdapter()

        binding?.rvQuickAnswer?.layoutManager = SpanningLinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL, false
        )
        binding?.rvQuickAnswer?.adapter = adapter
        adapter.addItems()
        adapter.clickListener = { emojiString, emojiName ->
            needAuthToNavigate {
                val cursorPosition = binding?.etWriteComment?.selectionEnd
                val newText = cursorPosition?.let {
                    binding?.etWriteComment?.text?.insert(it, emojiString)
                } ?: kotlin.run { "${binding?.etWriteComment?.text}$emojiString" }
                binding?.etWriteComment?.setText(newText)
                cursorPosition?.let {
                    binding?.etWriteComment?.setSelection(it + emojiString.length)
                } ?: kotlin.run {
                    binding?.etWriteComment?.setSelection(binding?.etWriteComment?.text.toString().length)
                }

                viewModel.getAnalyticsInteractor().logEmojiTap(emojiName)
            }
        }
    }

    private fun hideScrollDownBtn() {
        Timber.d("hideScrollDownBtn")
        if (isHidenScrollDownBtn) {
            binding?.btnScrollDown?.gone()
            return
        }

        isHidenScrollDownBtn = true
        binding?.btnScrollDown?.isEnabled = false
        binding?.btnScrollDown
            ?.animate()
            ?.scaleX(0f)
            ?.scaleY(0f)
            ?.setDuration(150)
            ?.setListener(onAnimationEnd = {
                binding?.btnScrollDown?.gone()
            })
            ?.start()
    }

    private fun showScrollDownBtn() {
        Timber.d("showScrollDownBtn")
        if (!isHidenScrollDownBtn || mCommentsAdapter?.itemCount == 0) {
            binding?.btnScrollDown?.buttonType = ButtonType.ELEVATED
            binding?.btnScrollDown?.visible()
            return
        }

        isHidenScrollDownBtn = false
        binding?.btnScrollDown?.isEnabled = true
        binding?.btnScrollDown
            ?.animate()
            ?.scaleX(1f)
            ?.scaleY(1f)
            ?.setDuration(150)
            ?.start()
        binding?.btnScrollDown?.buttonType = ButtonType.ELEVATED
        binding?.btnScrollDown?.visible()
    }


    private fun initSendBtn() {
        binding?.ivSendComment?.click {
            var message = binding?.etWriteComment?.text.toString()
            if (message.isEmpty()) return@click
            message = message.trim()

            Timber.d("comment to sent = $message")

            if (message.trim().isNotEmpty() && isShowProgress) {
                showToastMessage(
                    R.string.cant_send_comment_whil_loading,
                    iconState = AvatarUiState.ErrorIconState
                )
                return@click
            }

            // Temporary disabled until comment send
            binding?.ivSendComment?.isEnabled = false
            binding?.etWriteComment?.isEnabled = false
            context?.hideKeyboard(requireView())
            binding?.etWriteComment?.clearText()

            Timber.d("comment to sent = $message")
            viewModel.sendCommentToServer(
                postItem,
                postId,
                message,
                selectedCommentID ?: 0
            )

            closeSendMessageExtraInfo()
        }

        binding?.ivCancelBtn?.click {
            closeSendMessageExtraInfo()
        }
    }

    private fun initObservers() {
        viewModel.livePostState.observe(viewLifecycleOwner) { list ->
            adapter?.submitList(list)
            if (list.isNotEmpty()) {
                updatePost(list[0])
            }
            binding.srlPostFrg.setRefreshing(false)
        }
        viewModel.failure.observe(viewLifecycleOwner) { failure ->
            binding.srlPostFrg.setRefreshing(false)
            renderFailures(failure)
        }
        viewModel.livePostViewEvent.observe(viewLifecycleOwner) { event ->
            handleEvents(event)
        }
        viewModel.liveComments.observe(viewLifecycleOwner) {
            binding.srlPostFrg.setRefreshing(false)
            when (it.order) {
                OrderType.AFTER -> {
                    mCommentsAdapter?.addItemsNext(it)
                }

                OrderType.BEFORE -> {
                    mCommentsAdapter?.addItemsPrevious(it)
                }

                OrderType.INITIALIZE -> {
                    mCommentsAdapter?.refresh(it) { scroll ->
                        doDelayed(DELAY_SCROLLING) {
                            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                                binding?.rvPostsComments?.scrollToPosition(scroll + 1)
                                viewModel.paginationHelper.isTopPage = false
                            }
                        }
                    }
                    if (it.items.isEmpty() && viewModel.isPostCommentable) {
                        triggerAction(PostDetailsActions.AddEmptyCommentsPlaceHolder)
                    } else {
                        triggerAction(PostDetailsActions.RemoveEmptyCommentsPlaceHolder)
                    }
                }
            }
        }
        viewModel.birthdayRangesLiveData.observe(viewLifecycleOwner) { inputState ->
            binding?.etWriteComment?.addSpanBoldRangesClickColored(
                color = requireContext().color(R.color.uiKitColorForegroundLink),
                rangeList = inputState.wordsRanges,
                onClickListener = {
                    act.showFireworkAnimation {}
                }
            )
        }
    }

    private fun updatePost(post: PostUIEntity) {
        postItem = post
        initPrivacy(post)
        initToolbar(post)
        if (viewModel.isUserDeletedOwnPost().not()) {
            (parentFragment as? EventSnippetPage)?.onEventPostUpdated(post)
        }
    }

    private fun initRecycler() {
        // Чтоб не крашился с lateinit exception
        val formatterProvider = AllRemoteStyleFormatter(viewModel.getSettings())
        adapter = MeeraPostDetailAdapter(
            blurHelper = BlurHelper(
                context = requireContext(),
                lifecycle = lifecycle,
            ),
            contentManager = getSensitiveContentManager(),
            postCallback = this,
            volumeStateCallback = this,
            zoomyProvider = { Zoomy.Builder(act) },
            cacheUtil = CacheUtil(requireContext()),
            audioFeedHelper = __audio,
            formatter = formatterProvider,
            needToShowCommunityLabel = true,
            postDetailsMode = viewModel.postDetailsMode,
            featureTogglesContainer = viewModel.getFeatureTogglesContainer()
        )
        adapter?.isNeedToShowFlyingReactions =
            postOrigin == DestinationOriginEnum.NOTIFICATIONS_REACTIONS || postHaveReactions
        adapter?.isNeedToShowRepostBtn = isNeedToShowRepostBtn
        adapter?.postLatestReactionType = postLatestReactionType

        //слушатель на bind необходим для пометки постов и комментариев к нему как просмотренных
        adapter?.bindListener = {
            viewModel.onItemSeen(it)
        }

        mCommentsAdapter = MeeraCommentAdapter(
            commentListCallback = this
        ) {
            viewModel.addInnerComment(it)
        }
        mCommentsAdapter?.collectionUpdateListener = viewModel.commentObserver

        mCommentsAdapter?.innerSeparatorItemClickListener = {
            doDelayed(100) {
                handleScrollDownBtnVisibility()
            }
        }
        mainAdapter = ConcatAdapter(adapter, mCommentsAdapter)

        // handle comment placeholder
        mCommentsAdapter?.registerAdapterDataObserver(adapterDataObserver)

        binding.srlPostFrg.setOnRefreshListener(object : MeeraPullToRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                triggerAction(PostDetailsActions.Refresh)
                blockedUsersList.clear()
            }
        })

        val commentRecyclerViewLayoutManager = SpeedyLinearLayoutManager(requireContext())
        binding?.rvPostsComments?.apply {
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
            layoutManager = commentRecyclerViewLayoutManager
            adapter = mainAdapter
            addOnChildAttachStateChangeListener(object : OnChildAttachStateChangeListener {
                override fun onChildViewAttachedToWindow(view: View) {
                    val holder: ViewHolder = getChildViewHolder(view) ?: return
                    if (view.tag == AUDIO_FEED_HELPER_VIEW_TAG && holder is ViewHolderAudio) {
                        holder.subscribe()
                    }
                    if (holder is VideoViewHolder) {
                        holder.initPlayer()
                    }
                }

                override fun onChildViewDetachedFromWindow(view: View) {
                    val holder: ViewHolder = getChildViewHolder(view) ?: return
                    if (view.tag == AUDIO_FEED_HELPER_VIEW_TAG && holder is ViewHolderAudio) {
                        holder.unSubscribe()
                    }
                    if (holder is VideoViewHolder) clearForDetachedView(holder)
                }
            })
        }

        binding?.rvPostsComments?.addOnScrollListener(
            object : RecyclerPaginationUtil(commentRecyclerViewLayoutManager) {

                // Пагинация вверх
                override fun loadBefore() {
                    viewModel.addCommentsBefore()
                }

                // Пагинация вниз
                override fun loadAfter() {
                    viewModel.addCommentsAfter()
                }

                override fun isTopPage(): Boolean =
                    viewModel.paginationHelper.isTopPage

                override fun isBottomPage(): Boolean =
                    viewModel.paginationHelper.isLastPage

                override fun isLoadingAfter(): Boolean =
                    viewModel.paginationHelper.isLoadingAfter

                override fun isLoadingBefore(): Boolean =
                    viewModel.paginationHelper.isLoadingBefore
            }
        )

        val messageSwipeController = ReplySwipeController(
            requireContext(),
            SwipingItemType.POST_COMMENT,
            object : SwipeControllerActions {
                override fun onReply(absoluteAdapterPosition: Int) {
                    mCommentsAdapter?.getItem(absoluteAdapterPosition - 1)?.comment?.let {
                        onCommentReplyClick(it)
                    }
                }
            })

        itemTouchHelper = ItemTouchHelper(messageSwipeController)
        itemTouchHelper?.attachToRecyclerView(binding?.rvPostsComments)

        initOnScrollListener()

        viewModel.paginationHelper.isLoadingBeforeCallback = {
            if (it) {
                mCommentsAdapter?.addLoadingProgressBefore()
            } else mCommentsAdapter?.removeLoadingProgressBefore()
        }

        viewModel.paginationHelper.isLoadingAfterCallback = {
            if (it)
                mCommentsAdapter?.addLoadingProgressAfter()
            else mCommentsAdapter?.removeLoadingProgressAfter()
        }

        initPostViewCollisionDetector(binding?.rvPostsComments)
    }

    private fun clearForDetachedView(videoViewHolder: VideoViewHolder) {
        videoViewHolder.stopPlayingVideo()
        videoViewHolder.detachPlayer()
    }

    private fun setSwipeRefreshDirections(snippetState: SnippetState?, forceRefreshDisabled: Boolean = false) {
        val isRefresh = if (forceRefreshDisabled) {
            false
        } else {
            snippetState == null
        }
        binding.srlPostFrg.setRefreshEnable(isRefresh)
    }

    private val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            val commentsItemCount = mCommentsAdapter?.itemCount ?: 0

            if (commentsItemCount > 0) {
                triggerAction(PostDetailsActions.RemoveEmptyCommentsPlaceHolder)
            }
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            super.onItemRangeRemoved(positionStart, itemCount)

            if (mCommentsAdapter?.itemCount == 0) {
                triggerAction(PostDetailsActions.AddEmptyCommentsPlaceHolder)
            }
        }

        override fun onChanged() {
            super.onChanged()
            if (mCommentsAdapter?.itemCount == 0) {
                triggerAction(PostDetailsActions.AddEmptyCommentsPlaceHolder)
            }
        }
    }

    private fun getSensitiveContentManager(): ISensitiveContentManager =
        object : ISensitiveContentManager {
            override fun isMarkedAsNonSensitivePost(postId: Long?): Boolean {
                return viewModel.getSensitiveContentManager().isMarkedAsNonSensitivePost(postId)
            }

            override fun markPostAsNotSensitiveForUser(postId: Long?, parentPostId: Long?) {
                viewModel.getSensitiveContentManager()
                    .markPostAsNotSensitiveForUser(postId, parentPostId)
                triggerAction(PostDetailsActions.RefreshPost(postId))
                postAdapterPosition?.let {
//                    postListCallback?.onOpenPostClicked(it)
                }
            }
        }

    private fun handleScrollDownBtnVisibility(lastVisibleItemPosition: Int? = null) {
        var pos = lastVisibleItemPosition
        if (lastVisibleItemPosition == null) {
            pos =
                (binding?.rvPostsComments?.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
        }
        pos?.let {
            pos++
            if (pos != mainAdapter?.itemCount ?: -1 && !isItemVisible50Percent()) {
                showScrollDownBtn()
            } else hideScrollDownBtn()
        }
    }

    private fun initOnScrollListener() {
        binding?.rvPostsComments?.apply {
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val lastVisibleItemPosition =
                        (layoutManager as? LinearLayoutManager)?.findLastVisibleItemPosition()
                    handleScrollDownBtnVisibility(lastVisibleItemPosition)
                }
            })

            addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
                override fun onChildViewAttachedToWindow(view: View) {
                    val holder = findViewHolderForAdapterPosition(getChildAdapterPosition(view))
                    if (holder is VideoViewHolder) {
                        holder.initPlayer()
                        if (isPostDeletedOrHidden()) return
                        binding?.rvPostsComments?.postDelayed({ videoHelper?.playVideo(0L) }, POST_START_VIDEO_DELAY)
                    }
                }

                override fun onChildViewDetachedFromWindow(view: View) {
                    val holder = findViewHolderForAdapterPosition(getChildAdapterPosition(view))
                    if (holder is VideoViewHolder) {
                        holder.detachPlayer()
                    }
                }
            })
        }

        // when item is just binded we need to show video immediately
        adapter?.videoBinded = {
            binding?.rvPostsComments?.postDelayed({ startVideo(it, startVideoPosition) }, POST_START_VIDEO_DELAY)
        }
    }

    private fun isItemVisible50Percent(): Boolean {
        binding?.rvPostsComments?.let { rv ->
            val holder = rv.findViewHolderForAdapterPosition(0)
            holder?.itemView?.let { itemView ->
                val totalHeight = holder.itemView.height
                val visibleHolderLocationOnScreen = IntArray(2)
                itemView.getLocationOnScreen(visibleHolderLocationOnScreen)
                //70 - toolbar height
                val visibleChildTopY = visibleHolderLocationOnScreen[1] - 70.dp
                return rv.height / 2 <= kotlin.math.abs(visibleChildTopY + totalHeight)
            }
        }
        return false
    }

    private fun startVideo(holder: VideoViewHolder, startPosition: Long? = null) {
        videoHelper?.setVideoHolder(holder)

        if (isPostDeletedOrHidden()) return
        videoHelper?.playVideo(startPosition)
    }

    private fun initToolbar(post: PostUIEntity?) {
        binding?.appBarLayout3?.bringToFront()
        post ?: return
        val isBottomSheetToolbar = viewModel.postDetailsMode == PostDetailsMode.EVENT_SNIPPET
            || viewModel.postDetailsMode == PostDetailsMode.EVENTS_LIST
        val navigationMode = if (isBottomSheetToolbar) {
            PostHeaderNavigationMode.CLOSE
        } else {
            PostHeaderNavigationMode.BACK
        }
        val isUpdatingState = post.postUpdatingLoadingInfo.loadingState == MediaLoadingState.LOADING
            || post.postUpdatingLoadingInfo.loadingState == MediaLoadingState.LOADING_NO_CANCEL_BUTTON

        val isPostDeleted = post.deleted.toBoolean()

        val postHeaderUiModel = PostHeaderUiModel(
            post = post,
            navigationMode = navigationMode,
            isOptionsAvailable = !isPostDeleted,
            childPost = null,
            isCommunityHeaderEnabled = true,
            isLightNavigation = isBottomSheetToolbar,
            editInProgress = isUpdatingState
        )
        binding?.phvPostv2Header?.bind(postHeaderUiModel)
        binding?.phvPostv2Header?.setEventListener { event ->
            when (event) {
                PostHeaderEvent.BackClicked -> {
                    hideKeyboard()
                    findNavController().popBackStack()
                }

                is PostHeaderEvent.CommunityClicked -> onCommunityClicked(
                    communityId = event.communityId,
                    adapterPosition = 0
                )

                PostHeaderEvent.FollowClicked -> onFollowUserClicked(
                    post = post,
                    adapterPosition = 0
                )

                PostHeaderEvent.OptionsClicked -> onPostItemDotsMenuClick(post = post, currentMedia = getCurrentMedia())
                PostHeaderEvent.UserClicked -> onAvatarClicked(
                    post = post,
                    adapterPosition = 0
                )

                PostHeaderEvent.CloseClicked -> handleToolbarCloseClicked()

                is PostHeaderEvent.UserMomentsClicked -> {
                    onShowUserMomentsClicked(
                        userId = event.userId,
                        hasNewMoments = event.hasNewMoments
                    )
                }
            }
            binding?.phvPostv2Header?.hideFollowButton()
        }
    }

    private fun getCurrentMedia(): MediaAssetEntity? {
        val postHolder = binding?.rvPostsComments?.findViewHolderForAdapterPosition(0)
        return if (postHolder is MeeraMultimediaPostHolder) {
            postHolder.getCurrentMedia()
        } else null
    }

    private fun handleToolbarCloseClicked() {
        if (viewModel.postDetailsMode == PostDetailsMode.EVENTS_LIST) {
            (parentFragment as? MapUiActionHandler)
                ?.handleOuterMapUiAction(MapUiAction.EventsListUiAction.EventsListItemDetailsCloseClicked)
            return
        }
        val page = (parentFragment as? MapSnippetPage) ?: return
        when (page.getSnippetState()) {
            SnippetState.Expanded -> SnippetState.Preview
            SnippetState.Preview -> {
                viewModel.setMapEventSnippetCloseMethod(MapSnippetCloseMethod.CLOSE_BUTTON)
                SnippetState.Closed
            }

            else -> null
        }?.let(page::setSnippetState)
        binding.appBarLayout3.goneAnimation()
    }

    private fun updateHeaderAvatar(post: PostUIEntity) {
        binding?.phvPostv2Header?.updateUserAvatar(post)
    }

    private fun showScreenshotPopup(postLink: String, eventIconRes: Int?, eventDateAndTime: String?) {
        if (isScreenshotPopupShown) return
        isScreenshotPopupShown = true
        val post = postItem ?: return
        val screenshotPlace = when {
            parentFragment is MainMapFragment || parentFragment is MapSnippetPage -> ScreenshotPlace.MAP_EVENT
            post.groupId != null && post.groupId != 0L -> ScreenshotPlace.COMMUNITY_POST
            post.event != null -> ScreenshotPlace.POST_EVENT
            else -> ScreenshotPlace.FEED_POST
        }
        val postText = if (post.tagSpan != null) {
            setupSpanText(post.tagSpan)
        } else {
            post.postText
        }
        val popupData = getScreenshotPopupData(
            post = post,
            postLink = postLink,
            eventDateAndTime = eventDateAndTime,
            postText = postText,
            eventIconRes = eventIconRes,
            screenshotPlace = screenshotPlace
        )
        ScreenshotPopupController.show(this, popupData, screenshotPopupDialogListener)
    }

    private fun setupSpanText(tagSpan: ParsedUniquename): String {
        val stringBuilder = handleSpanTagsInPosts(requireContext(), tagSpan)
        return stringBuilder.toString()
    }

    private fun getScreenshotPopupData(
        post: PostUIEntity,
        postLink: String,
        eventDateAndTime: String?,
        postText: String,
        eventIconRes: Int?,
        screenshotPlace: ScreenshotPlace
    ) = ScreenshotPopupData(
        title = post.groupName ?: post.user?.name ?: String.empty(),
        description = if (post.event != null) getString(R.string.event) else getString(R.string.post),
        buttonTextStringRes = if (post.event != null) R.string.share_event else R.string.share_post,
        link = postLink,
        additionalInfo = eventDateAndTime ?: postText,
        imageLink = post.getSingleVideoPreview() ?: post.getSingleSmallImage(),
        eventIconRes = eventIconRes,
        isDeleted = post.deleted.toBoolean(),
        eventId = post.event?.id ?: 0,
        postId = if (post.event?.id != null) 0 else post.postId,
        tagSpan = post.tagSpan,
        screenshotPlace = screenshotPlace
    )

    private fun getMusicHolder(): MeeraBasePostHolder? {
        val postView = binding?.rvPostsComments?.getChildAt(0) ?: return null
        val postHolder = binding?.rvPostsComments?.getChildViewHolder(postView) ?: return null
        return postHolder as? MeeraBasePostHolder?
    }

    private fun handleEvents(event: PostViewEvent?) {
        when (event) {
            is PostViewEvent.ErrorPostComment -> onErrorPostComment()
            is PostViewEvent.DeletePost -> onDeletedPost()
            is PostViewEvent.SubscribePost -> onSubscribePost()
            is PostViewEvent.UnsubscribePost -> onUnsubscribePost()
            is PostViewEvent.AddComplaint -> onAddPostComplaint()
            is PostViewEvent.AddPostCommentComplaint -> onAddPostCommentComplaint()
            is PostViewEvent.HideUserRoad -> onHideUserRoad()
            is PostViewEvent.HideUserPost -> onHideUserPost()
            is PostViewEvent.MarkCommentForDeletion -> onCommentMarkedAsDeleted(event)
            is PostViewEvent.CancelDeleteComment -> onCancelDeleteComment(event)
            is PostViewEvent.DeleteComment -> onDeleteCommentNew(event)
            is PostViewEvent.OnRefresh -> refresh()
            is PostViewEvent.PostWasDeleted -> onPostWasDeleted()
            is PostViewEvent.PostWasHidden -> onPostWasHidden()
            is PostViewEvent.UpdatePostEvent -> adapter?.updateItem(event.adapterPosition, event.post)

            is PostViewEvent.EnableComments -> {
                binding?.ivSendComment?.isEnabled = true
                binding?.etWriteComment?.isEnabled = true
            }

            is PostViewEvent.UserBlocked -> {
                blockedUsersList.add(event.userId)
                showToastMessage(R.string.meera_you_blocked_user)
            }

            is PostViewEvent.ComplainSuccess ->
                showToastMessage(R.string.road_complaint_send_success)

            is PostViewEvent.NewCommentSuccess -> handleNewComment(
                event.beforeMyComment,
                event.hasIntersection,
                event.needSmoothScroll,
                event.needToShowLastFullComment
            )

            is PostViewEvent.NewInnerCommentSuccess ->
                handleNewInnerComment(
                    parentId = event.parentId,
                    chunk = event.chunk,
                )

            is PostViewEvent.ErrorInnerPagination -> {
                mCommentsAdapter?.stopProgressInnerPagination(event.data)
                showToastMessage(getString(R.string.no_internet), iconState = AvatarUiState.ErrorIconState)
            }

            is PostViewEvent.ShowTextError -> showToastMessage(event.message, iconState = AvatarUiState.ErrorIconState)
            is PostViewEvent.NoInternet -> showToastMessage(
                getString(R.string.no_internet),
                iconState = AvatarUiState.ErrorIconState
            )

            is PostViewEvent.NoInternetAction -> showToastMessage(
                getString(R.string.internet_connection_problem_action),
                iconState = AvatarUiState.ErrorIconState
            )

            is PostViewEvent.ErrorDeleteComment -> {
                showToastMessage(getString(R.string.no_internet), iconState = AvatarUiState.ErrorIconState)
                mCommentsAdapter?.restoreComment(event.comment)
            }

            is PostViewEvent.OnScrollToBottom -> {
                mainAdapter?.itemCount?.let {
                    binding?.rvPostsComments?.scrollToPosition(it - 1)
                }
            }

            is PostViewEvent.UpdateCommentsReplyAvailability -> {
                mCommentsAdapter?.updateAllReplyButtonsState(needToShowReplyBtn = event.needToShowReplyBtn)
            }

            is PostViewEvent.MeeraUpdateCommentReaction -> {
                mCommentsAdapter?.notifyItemChanged(event.position, event.reactionUpdate)
                updateBottomMenuReactions(event.reactionUpdate)
            }

            is PostViewEvent.MeeraUpdatePostReaction -> {
                postItem = event.post
                adapter?.updateItem(0, event.reactionUpdate.toUIPostUpdate())
                updateBottomMenuReactions(event.reactionUpdate)
            }

            is PostViewEvent.UpdateLoadingState -> {
                postItem = event.post
                adapter?.notifyItemChanged(0, UIPostUpdate.UpdateLoadingState(event.post.postId, event.loadingInfo))
            }

            is PostViewEvent.UpdateUserState -> initToolbar(event.post)

            is PostViewEvent.OpenRepostMenu -> {
                checkAppRedesigned(
                    isRedesigned = {
                        meeraOpenRepostMenu(event.post)
                    },
                    isNotRedesigned = {
                        openRepostMenu(event.post)
                    }
                )
            }

            is PostViewEvent.CopyLinkEvent -> copyLink(event.link)
            PostViewEvent.FinishSnippetSetupEvent -> finishSnippetSetup()
            is PostViewEvent.UpdateEventParticipationEvent -> {
                val post = event.post
                if (post.event != null &&
                    postItem?.event?.participation?.isParticipant != post.event.participation.isParticipant
                ) {
                    adapter?.updatePost(
                        UIPostUpdate.UpdateEventPostParticipationState(postId = post.postId, postUIEntity = post)
                    )
                } else {
                    adapter?.submitList(listOf(post))
                }
                updatePost(post)
            }

            is PostViewEvent.ShowEventSharingSuggestion -> {
                meeraOpenRepostMenu(
                    post = event.post,
                    mode = SharingDialogMode.SUGGEST_EVENT_SHARING
                )
            }

            is PostViewEvent.UpdateUserMomentsState -> {
                updateHeaderAvatar(event.post)
            }

            is PostViewEvent.ShowScreenshotPopup -> {
                showScreenshotPopup(event.link, event.eventIconRes, event.eventDateAndTime)
            }

            is PostViewEvent.PostEditAvailableEvent -> showMenu(
                post = event.post,
                isEditAvailable = event.isEditAvailable,
                currentMedia = event.currentMedia
            )

            is PostViewEvent.OpenEditPostEvent -> navigateToEditPost(post = event.post)
            is PostViewEvent.PostEditedEvent -> {
                initToolbar(event.post)
                restartPlayer()
            }

            is PostViewEvent.UpdateVolumeState -> handleUpdateVolumeState(event.volumeState)
            is PostViewEvent.UpdateTagSpan -> handleUpdateTagSpan(event.postUpdate)
            is PostViewEvent.UpdatePostValues -> handleUpdatePostValues(event.postUpdate)
            is PostViewEvent.ShowReactionStatisticsEvent -> showReactionStatistics(event)
            is PostViewEvent.ShowAvailabilityError -> showNotAvailableToEditPostError(event.reason)
            else -> Unit
        }
    }

    private fun onPostWasDeleted() {
        showNotAvailableView()
        binding.tvPostNotAvailableTitle.gone()
        binding.tvPostNotAvailableSubtitle.text = getString(R.string.post_deleted)
        binding.srlPostFrg.setRefreshing(false)
        this.postItem = this.postItem?.copy(deleted = true.toInt())

        stopVideoIfExist()
    }

    private fun onPostWasHidden() {
        showNotAvailableView()
        binding.tvPostNotAvailableSubtitle.text = getString(R.string.meera_user_hide_post)
        binding.srlPostFrg.setRefreshing(false)

        stopVideoIfExist()
    }

    private fun showNotAvailableView() {
        binding?.apply {
            phvPostv2Header.hideOptionsButton()
            rvPostsComments.gone()
            srlPostFrg.gone()
            vgCreateBlockMainContainer.gone()
            llNotAvailablePost.visible()
        }
    }

    private fun handleUpdateVolumeState(volumeState: VolumeState) {
        adapter?.updateVolumeState(volumeState)

        if (volumeState == VolumeState.ON) __audio.stopPlaying()
    }

    private fun handleUpdateTagSpan(uiPostUpdate: UIPostUpdate.UpdateTagSpan) {
        adapter?.updateItem(0, uiPostUpdate)
    }

    private fun handleUpdatePostValues(uiPostUpdate: UIPostUpdate) {
        adapter?.updateItem(0, uiPostUpdate)
    }

    private fun restartPlayer() {
        startVideoPosition = 0
        initVideoHelper()
    }

    private fun updateBottomMenuReactions(reactionUpdate: MeeraReactionUpdate) {
        val reactionsMenuItem = currentBottomMenu?.getMenuItem<ReactionBottomMenuItem>() ?: return

        reactionsMenuItem.setReaction(reactionUpdate.reactionList)
    }

    private fun handleNewComment(
        beforeMyComment: List<CommentUIType>, // including my comment
        hasIntersection: Boolean,
        needSmoothScroll: Boolean,
        needToShowLastFullComment: Boolean,
    ) {
        val itemAnimator = binding?.rvPostsComments?.itemAnimator
        binding?.rvPostsComments?.itemAnimator = null
        val index = mCommentsAdapter?.itemCount ?: 0

        if (needToShowLastFullComment) {
            showFullLastComment(beforeMyComment)
        }
        mCommentsAdapter?.addItemsNext(beforeMyComment)

        mainAdapter?.itemCount?.let {
            if (!needSmoothScroll) binding?.rvPostsComments?.scrollToPosition(it - 1)
            else binding?.rvPostsComments?.smoothScrollToPosition(it - 1)
        }

        if (!hasIntersection) {
            doDelayed(50) {
                mCommentsAdapter?.removeItemsBefore(index = index)
            }
        }
        doDelayed(200) {
            binding?.rvPostsComments?.itemAnimator = itemAnimator
        }

        doDelayed(400) {
            handleScrollDownBtnVisibility()
        }
    }

    private fun showFullLastComment(comments: List<CommentUIType>) {
        if (comments.isNotEmpty()) {
            val lastComment = comments.last()
            if (lastComment is CommentEntity) {
                lastComment.isShowFull = true
            }
        }
    }

    private fun handleNewInnerComment(parentId: Long, chunk: CommentChunk) {
        val itemAnimator = binding?.rvPostsComments?.itemAnimator
        binding?.rvPostsComments?.itemAnimator = null
        showFullLastComment(chunk.items)
        mCommentsAdapter?.addItemsNext(parentId, chunk) {
            binding?.rvPostsComments?.smoothScrollToPosition(it)
        }
        doDelayed(200) {
            binding?.rvPostsComments?.itemAnimator = itemAnimator
        }
    }

    private fun onDeletedPost() {
        showToastMessage(R.string.post_deleted_success)
        when (viewModel.postDetailsMode) {
            PostDetailsMode.EVENT_SNIPPET -> (parentFragment as? EventSnippetPage)?.onUserDeletedOwnPost()
            PostDetailsMode.EVENTS_LIST -> {
                val uiAction = MapUiAction.EventsListUiAction.EventsListItemDeleted(postId ?: return)
                NavigationManager.getManager().mainMapFragment?.handleOuterMapUiAction(uiAction)
                findNavController().popBackStack()
            }

            else -> findNavController().popBackStack()
        }
    }

    private fun onHideUserPost() {
        showToastMessage(R.string.post_hide_success)
        findNavController().popBackStack()
    }

    private fun onHideUserRoad() {
        (requireActivity() as? MeeraAct)?.showToastMessage(R.string.post_author_hide_success)
        findNavController().popBackStack()
    }

    private fun renderFailures(failure: Failure) {
        when (failure) {
            is Failure.ServerError -> {
                showToastMessage(R.string.error_try_later, iconState = AvatarUiState.ErrorIconState)
            }

            is Failure.NetworkConnection -> {
                showToastMessage(R.string.error_try_later, iconState = AvatarUiState.ErrorIconState)
            }

            else -> {}
        }
    }

    /**
     * Events from viewModel
     * */
    private fun onAddPostCommentComplaint() {
        showToastMessage(R.string.post_comment_complaint_added_successfully)
    }

    private fun onAddPostComplaint() {
        showToastMessage(R.string.road_complaint_send_success)
    }

    private fun onUnsubscribePost() =
        showToastMessage(R.string.unsubscribe_post)

    private fun onSubscribePost() =
        showToastMessage(R.string.subscribe_post)

    private fun onErrorPostComment() {
        showToastMessage(getString(R.string.error_while_sending_comment), iconState = AvatarUiState.ErrorIconState)
        binding?.ivSendComment?.isEnabled = true
        binding?.etWriteComment?.isEnabled = true
    }

    override fun onPressRepostHeader(post: PostUIEntity, adapterPosition: Int) {
        post.parentPost?.let {
            goToParentPost(post)
        }
    }

    override fun onFollowUserClicked(post: PostUIEntity, adapterPosition: Int) {
        needAuthToNavigateWithResult(SUBSCRIPTION_ROAD_REQUEST_KEY) {
            val userId = post.user?.userId
            val isPostAuthor = post.user?.userId == viewModel.getUserUid()
            if (isPostAuthor) return@needAuthToNavigateWithResult
            val isSubscribed = post.user?.subscriptionOn ?: return@needAuthToNavigateWithResult
            if (isSubscribed.isTrue()) {
                showConfirmDialogUnsubscribeUser(
                    postId = postId,
                    userId = userId,
                    fromFollowButton = true,
                    postOriginEnum = postOrigin ?: return@needAuthToNavigateWithResult,
                    approved = post.user.approved.toBoolean(),
                    topContentMaker = post.user.topContentMaker.toBoolean()
                )
            } else {
                triggerAction(
                    PostDetailsActions.SubscribeUser(
                        postId = postId,
                        userId = userId,
                        postOrigin = postOrigin,
                        needToHideFollowButton = false,
                        fromFollowButton = true,
                        approved = post.user.approved.toBoolean(),
                        topContentMaker = post.user.topContentMaker.toBoolean()
                    )
                )
            }
        }
    }

    private fun showBottomCommonAction(
        commentId: Long? = 0,
        postText: String,
    ) {
        Timber.e("Post comments: clicked commentID = $commentId")
        //selectedCommentID = 0
        val menu = MeeraMenuBottomSheet(context)

        if (viewModel.paginationHelper.needToShowReplyBtn) {
            menu.addItem(R.string.reply_txt, R.drawable.ic_outlined_reply_m) {
                viewModel.logCommentMenuAction(AmplitudePropertyCommentMenuAction.REPLY)
                commentId?.let { commentID ->
                    selectedComment?.id = commentID
                    selectedCommentID = commentID
                    Timber.d("Post comments: selectedcommentId = $selectedCommentID")
                }
                addInfoInInputMessageWidget()
            }
        }

        menu.addItem(R.string.text_copy_txt, R.drawable.ic_outlined_copy_m) {
            viewModel.logCommentMenuAction(AmplitudePropertyCommentMenuAction.COPY)
            val clipboardManager =
                activity?.applicationContext
                    ?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager?
            val clipData = ClipData.newPlainText("text", postText)
            clipboardManager?.setPrimaryClip(clipData)
            showToastMessage(R.string.comment_text_copied, iconState = AvatarUiState.WarningIconState)
        }

        menu.addItem(
            R.string.comment_complain, R.drawable.ic_outlined_attention_m,
            iconAndTitleColor = R.color.uiKitColorAccentWrong
        ) {
            //viewModel.complainComment(commentId)
            viewModel.logCommentMenuAction(AmplitudePropertyCommentMenuAction.REPORT)
            triggerAction(PostDetailsActions.AddComplaintPostComment(commentId))
        }

        menu.showWithTag(manager = parentFragmentManager, tag = COMMENT_MENU_TAG)

        currentBottomMenu = menu
    }

    private fun openRepostMenu(post: PostUIEntity, mode: SharingDialogMode = SharingDialogMode.DEFAULT) {
//        if (isFragmentStarted.not()) return
        needAuthToNavigate {
            SharePostBottomSheet(
                postOrigin = postOrigin,
                post = post.toPost(),
                event = post.event,
                mode = mode,
                callback = object : IOnSharePost {
                    override fun onShareFindGroup() {
                        openGroups()
                    }

                    override fun onShareFindFriend() {
                        openSearch()
                    }

                    override fun onShareToGroupSuccess(groupName: String?) {
                        viewModel.repostSuccess(post)
                        showToastMessage(getString(R.string.success_repost_to_group, groupName ?: ""))
                    }

                    override fun onShareToRoadSuccess() {
                        viewModel.repostSuccess(post)
                        showToastMessage(R.string.success_repost_to_own_road)
                    }

                    override fun onShareToChatSuccess(repostTargetCount: Int) {
                        viewModel.repostSuccess(post, repostTargetCount)
                        val strResId = if (post.isEvent()) {
                            R.string.success_event_repost_to_chat
                        } else {
                            R.string.success_repost_to_chat
                        }
                        showToastMessage(strResId)
                    }

                    override fun onPostItemUniqnameUserClick(userId: Long?) {
                        allowScreenshotPopupShowing()
                        openUserFragment(userId)

                    }
                }).show(childFragmentManager)
        }
    }

    private fun openUserFragment(userId: Long?, where: AmplitudePropertyWhere? = null) {
        val navController = if (parentFragment is MeeraEventPostPageFragment) {
            NavigationManager.getManager().mainMapFragment.closeSnippet(openOnResume = true)
            NavigationManager.getManager().topNavController
        } else {
            findNavController()
        }
        navController.safeNavigate(
            resId = R.id.userInfoFragment,
            bundle = bundleOf(
                IArgContainer.ARG_USER_ID to userId,
                ARG_TRANSIT_FROM to where,
                USERINFO_OPEN_FROM_NOTIFICATION to true
            ),
            navBuilder = {
                it.addAnimationTransitionByDefault()
            }
        )
    }

    private fun meeraOpenRepostMenu(post: PostUIEntity, mode: SharingDialogMode = SharingDialogMode.DEFAULT) {
//        if (isFragmentStarted.not()) return
        needAuthToNavigate {
            MeeraShareSheet().show(
                fm = childFragmentManager,
                data = MeeraShareBottomSheetData(
                    post = post.toPost(),
                    postOrigin = postOrigin,
                    event = post.event,
                    mode = mode,
                    callback = object : IOnSharePost {
                        override fun onShareFindGroup() {
                            openGroups()
                        }

                        override fun onShareFindFriend() {
                            openSearch()
                        }

                        override fun onShareToGroupSuccess(groupName: String?) {
                            viewModel.repostSuccess(post)
                            showToastMessage(getString(R.string.success_repost_to_group, groupName ?: ""))
                        }

                        override fun onShareToRoadSuccess() {
                            viewModel.repostSuccess(post)
                            showToastMessage(R.string.success_repost_to_own_road)
                        }

                        override fun onShareToChatSuccess(repostTargetCount: Int) {
                            viewModel.repostSuccess(post, repostTargetCount)
                            val strResId = if (post.isEvent()) {
                                R.string.success_event_repost_to_chat
                            } else {
                                R.string.success_repost_to_chat
                            }
                            showToastMessage(strResId)
                        }

                        override fun onPostItemUniqnameUserClick(userId: Long?) {
                            allowScreenshotPopupShowing()
                            openUserFragment(userId)
                        }
                    }
                )
            )
        }
    }

    private fun openSearch() {
        findNavController().safeNavigate(
            resId = R.id.action_meeraPostFragmentV2_to_meeraSearchFragment,
            bundle = Bundle().apply {
                putSerializable(
                    IArgContainer.ARG_FIND_FRIENDS_OPENED_FROM_WHERE,
                    AmplitudeFindFriendsWhereProperty.SHARE
                )
            }
        )
    }

    @Suppress("detekt:SwallowedException")
    private fun openGroups() {
        try {
            findNavController().navigate(R.id.action_meeraPostFragmentV2_to_meeraCommunitiesListsContainerFragment)
        } catch (e: Exception) {
            NavigationManager.getManager().topNavController.safeNavigate(
                R.id.action_meeraPostFragmentV2_to_meeraSearchFragment, bundle = bundleOf(
                    IArgContainer.ARG_SEARCH_OPEN_PAGE to SearchMainFragment.PAGE_SEARCH_COMMUNITY
                )
            )
        }
    }

    private fun copyLink(link: String) {
        copyCommunityLink(context, link) {
            showToastMessage(R.string.copy_link_success)
        }
    }

    private fun showBottomOwnerAction(
        commentId: Long? = 0,
        postText: String,
        isCommentAuthor: Boolean,
        commentAuthorId: Long?,
        isPostAuthor: Boolean,
    ) {
        val menu = MeeraMenuBottomSheet(context)

        if (isPostAuthor || viewModel.paginationHelper.needToShowReplyBtn) {
            menu.addItem(R.string.reply_txt, R.drawable.ic_outlined_repost_m) {
                viewModel.logCommentMenuAction(AmplitudePropertyCommentMenuAction.REPLY)
                commentId?.let { commentID ->
                    selectedCommentID = commentID
                    Timber.d("Post comments: selectedcommentId = $selectedCommentID")
                }
                addInfoInInputMessageWidget()
            }
        }

        menu.addItem(R.string.text_copy_txt, R.drawable.ic_outlined_copy_m) {
            viewModel.logCommentMenuAction(AmplitudePropertyCommentMenuAction.COPY)
            val clipboardManager =
                activity?.applicationContext
                    ?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager?
            val clipData = ClipData.newPlainText("text", postText)
            clipboardManager?.setPrimaryClip(clipData)
            showToastMessage(getString(R.string.comment_text_copied), iconState = AvatarUiState.WarningIconState)
        }

        menu.addItem(
            R.string.road_delete, R.drawable.ic_outlined_delete_m,
            iconAndTitleColor = R.color.uiKitColorAccentWrong
        ) {
            if (commentId != null) {
                viewModel.logCommentMenuAction(AmplitudePropertyCommentMenuAction.DELETE)
                val whoDeleteComment = getWhoDeletedComment(isPostAuthor, isCommentAuthor) ?: return@addItem
                val originalComment = mCommentsAdapter?.findCommentById(commentId) ?: return@addItem
                viewModel.markAsDeletePostComment(
                    originalComment = originalComment,
                    whoDeleteComment = whoDeleteComment
                )
            }
        }

        if (!isCommentAuthor) {
            menu.addItem(
                R.string.comment_complain, R.drawable.ic_outlined_attention_m,
                iconAndTitleColor = R.color.uiKitColorAccentWrong
            ) {
                viewModel.logCommentMenuAction(AmplitudePropertyCommentMenuAction.REPORT)
                triggerAction(PostDetailsActions.AddComplaintPostComment(commentId))
            }

            if (!blockedUsersList.contains(commentAuthorId)) {
                menu.addItem(
                    R.string.settings_privacy_block_user, R.drawable.ic_outlined_circle_block_m,
                    iconAndTitleColor = R.color.uiKitColorAccentWrong
                ) {
                    viewModel.logCommentMenuAction(AmplitudePropertyCommentMenuAction.BLOCK)
                    triggerAction(PostDetailsActions.BlockUser(viewModel.getUserUid(), commentAuthorId))
                }
            }
        }

        menu.showWithTag(manager = parentFragmentManager, tag = COMMENT_MENU_TAG)

        currentBottomMenu = menu
    }

    private fun getWhoDeletedComment(
        isPostAuthor: Boolean,
        isCommentAuthor: Boolean,
    ): WhoDeleteComment? {
        if (isPostAuthor && !isCommentAuthor) return WhoDeleteComment.POST_AUTHOR
        if (isPostAuthor && isCommentAuthor) return WhoDeleteComment.BOTH_POST_COMMENT_AUTHOR
        if (!isPostAuthor && isCommentAuthor) return WhoDeleteComment.COMMENT_AUTHOR
        return null
    }

    private fun onCommentMarkedAsDeleted(event: PostViewEvent.MarkCommentForDeletion) {
        val replacedCommentEntity = mCommentsAdapter?.replaceCommentByDeletion(
            event.commentID,
            event.whoDeleteComment
        ) ?: return

        showDeleteCommentCountdownToastNew { cancelDeletion ->
            if (cancelDeletion) {
                viewModel.cancelDeletePostComment(replacedCommentEntity)
            } else {
                viewModel.deletePostComment(
                    commentId = event.commentID,
                    whoDeleteComment = event.whoDeleteComment,
                    comment = replacedCommentEntity
                )
            }
        }
    }

    private fun onCancelDeleteComment(event: PostViewEvent.CancelDeleteComment) {
        mCommentsAdapter?.restoreComment(event.originalComment)
    }

    //если удалили тот комментарий на который хотели ответить закрываем плашку
    private fun onDeleteCommentNew(event: PostViewEvent.DeleteComment) {
        if (selectedCommentID == event.commentID) {
            selectedCommentID = 0
            closeSendMessageExtraInfo()
        }
    }

    private fun showDeleteCommentCountdownToastNew(onClosedTimerDialog: (cancelDeletion: Boolean) -> Unit) {
        undoSnackbar?.dismiss()
        undoSnackbar = UiKitSnackBar.make(
            view = binding.srlPostFrg,
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(R.string.comment_deleted),
                    loadingUiState = SnackLoadingUiState.DonutProgress(
                        timerStartSec = DELAY_DELETE_COMMENT_SECONDS,
                        onTimerFinished = { onClosedTimerDialog(false) }
                    ),
                    buttonActionText = getText(R.string.cancel),
                    buttonActionListener = {
                        undoSnackbar?.dismiss()
                        onClosedTimerDialog(true)
                    }
                ),
                dismissOnClick = false,
                duration = DELAY_DELETE_COMMENT_MILLISECONDS,
                paddingState = PaddingState(
                    bottom = countMessageInputBottomPadding()
                )
            )
        )
        undoSnackbar?.handleSnackBarActions(UiKitSnackBarActions.StartTimerIfNotRunning)
        undoSnackbar?.show()
    }

    override fun onCommentReplyClick(comment: CommentEntityResponse) {
        needAuthToNavigate {
            if (postItem?.user?.blackListedMe == true) return@needAuthToNavigate
            selectedComment = comment
            selectedCommentID = 0

            selectedComment?.id?.let { commentID ->
                selectedCommentID = commentID
            }
            addInfoInInputMessageWidget()
        }
    }

    override fun onCommentMention(userId: Long) {
        allowScreenshotPopupShowing()
        openUserFragment(userId)
    }

    override fun onHashtagClicked(hashtag: String?) {
        needAuthToNavigate {
            val navController = if (parentFragment is MeeraEventPostPageFragment) {
                NavigationManager.getManager().mainMapFragment.closeSnippet(openOnResume = true)
                NavigationManager.getManager().topNavController
            } else {
                findNavController()
            }
            navController.safeNavigate(
                resId = R.id.meeraHashTagFragment,
                bundle = bundleOf(
                    IArgContainer.ARG_HASHTAG to hashtag,
                )
            )
        }
    }

    override fun onStateChanged(newState: Int) {
        getMusicHolder()?.changeStateSnippet(newState, postItem)
        super.onStateChanged(newState)
        if (newState == BottomSheetBehavior.STATE_HIDDEN && isMapEvent) {
            NavigationManager.getManager().toolbarAndBottomInteraction.getToolbar().state =
                UiKitToolbarViewState.EXPANDED
        }
    }

    override fun onShowEventOnMapClicked(post: PostUIEntity, isRepost: Boolean, adapterPosition: Int) {
        NavigationManager.getManager().getForceUpdatedTopBehavior()?.isHideable = true
        NavigationManager.getManager().getForceUpdatedTopBehavior()?.setHiddenState()
        if (isRepost) {
            post.parentPost?.let {
                NavigationManager.getManager().mainMapFragment.openEventFromAnotherScreen(it, true)
            }
        } else {
            NavigationManager.getManager().mainMapFragment.openEventFromAnotherScreen(post, true)
        }
    }

    override fun onDotsMenuClicked(post: PostUIEntity, adapterPosition: Int, currentMedia: MediaAssetEntity?) {
        super.onDotsMenuClicked(post, adapterPosition, currentMedia)
        requireActivity().vibrate()
        triggerAction(PostDetailsActions.CheckUpdateAvailability(post, currentMedia))
    }

    override fun onPostCloseClicked() {
        super.onPostCloseClicked()
        if (isMapEvent) {
            findNavController().popBackStack()
        } else {
            val parentMapSnippetFragment = parentFragment as? MapSnippetPage? ?: return
            if (parentMapSnippetFragment.getSnippetState() == SnippetState.Expanded) {
                parentMapSnippetFragment.setSnippetState(SnippetState.Preview)
            } else {
                NavigationManager.getManager().mainMapFragment.closeSnippet()
            }
        }
    }

    override fun onNavigateToEventClicked(post: PostUIEntity) {
        val initUiModel = EventNavigationInitUiModel(
            event = post.event ?: return,
            authorId = post.user?.userId ?: return
        )
        MeeraEventNavigationBottomsheetDialogFragment.getInstance(initUiModel)
            .show(childFragmentManager, MeeraEventNavigationBottomsheetDialogFragment::class.java.name)
        viewModel.logMapEventGetTherePress(post)
    }

    override fun onShowEventParticipantsClicked(post: PostUIEntity) {
//        openEventParticipantsList(post)  todo eventParticipantsDelegate
        NavigationManager.getManager().topNavController.safeNavigate(
            resId = R.id.eventParticipantsListFragment,
            bundle = Bundle().apply {
                post.event?.let { event ->
                    putLong(
                        EventParticipantsListFragment.ARG_EVENT_ID, event.id
                    )
                    putLong(
                        EventParticipantsListFragment.ARG_POST_ID, post.postId
                    )
                    putInt(
                        EventParticipantsListFragment.ARG_PARTICIPANTS_COUNT,
                        event.participation.participantsCount
                    )
                }
            }
        )
    }

    override fun onJoinAnimationFinished(post: PostUIEntity, adapterPosition: Int) {
        viewModel.onJoinAnimationFinished(post)
    }

    override fun onJoinEventClicked(post: PostUIEntity, isRepost: Boolean, adapterPosition: Int) {
        if (isRepost) {
            post.parentPost?.let { viewModel.joinEvent(it) }
        } else {
            viewModel.joinEvent(post)
        }
        refresh()
    }

    override fun onLeaveEventClicked(post: PostUIEntity, isRepost: Boolean, adapterPosition: Int) {
        if (isRepost) {
            post.parentPost?.let { viewModel.leaveEvent(it) }
        } else {
            viewModel.leaveEvent(post)
        }
        refresh()
    }

    /**
     * Add user message name to Bottom comment widget
     */
    private fun addInfoInInputMessageWidget() {
        openRelyExtraContainer()
        if (selectedComment != null && selectedComment?.user?.name != null) {
            binding?.tvCommentOwner?.text = selectedComment?.user?.name
        }
        openKeyboard()
    }

    private fun openRelyExtraContainer() {
        binding?.rellayExtraInfoContainer?.visible()
        binding?.rellayExtraInfoContainer?.measure(
            ViewGroup.LayoutParams.MATCH_PARENT,
            30.dp
        )
        binding?.rellayExtraInfoContainer?.measuredHeight?.let {
            binding?.rellayExtraInfoContainer?.animateHeight(it, 100)
        }
    }

    private fun closeRelyExtraContainer() {
        binding?.rellayExtraInfoContainer?.visible()
        binding?.rellayExtraInfoContainer?.animateHeight(0, 100) {
            binding?.rellayExtraInfoContainer?.gone()
        }
    }

    private fun openKeyboard() {
        binding?.etWriteComment?.requestFocusFromTouch()
        val lManager =
            activity?.applicationContext
                ?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager?
        binding?.etWriteComment?.let {
            lManager?.showSoftInput(it, 0)
        }
    }

    /**
     * Hide user message name to Bottom comment widget
     */
    private fun closeSendMessageExtraInfo() {
        binding?.tvCommentOwner?.text = ""
        selectedComment = null
        selectedCommentID = null
        closeRelyExtraContainer()
    }

    override fun onCommentShowReactionBubble(
        commentId: Long,
        commentUserId: Long,
        showPoint: Point,
        viewsToHide: List<View>,
        reactionTip: TextView,
        currentReactionsList: List<ReactionEntity>,
        isMoveUpAnimationEnabled: Boolean,
    ) {
        val postId = postId ?: return
        val reactionSource = MeeraReactionSource.PostComment(
            postId = postId,
            commentId = commentId,
            originEnum = postOrigin,
            postUserId = postItem?.getUserId(),
            commentUserId = commentUserId

        )
        val act = (requireActivity() as MeeraAct)
        act.getMeeraReactionBubbleViewController().showReactionBubble(
            reactionSource = reactionSource,
            showPoint = showPoint,
            viewsToHide = viewsToHide,
            reactionTip = reactionTip,
            currentReactionsList = currentReactionsList,
            contentActionBarType = MeeraContentActionBar.ContentActionBarType.DEFAULT,
            isForceAdd = true,
            isMoveUpAnimationEnabled = isMoveUpAnimationEnabled,
            showMorningEvening = false,
            reactionsParams = postItem?.createAmplitudeReactionsParams(postOrigin),
            containerInfo = act.getDefaultReactionContainer()
        )
    }

    private fun showToastMessage(
        @StringRes messageRes: Int,
        iconState: AvatarUiState = AvatarUiState.SuccessIconState
    ) {
        showToastMessage(getString(messageRes), iconState)
    }

    private fun showToastMessage(messageString: String, iconState: AvatarUiState = AvatarUiState.SuccessIconState) =
        doOnUIThread {
            undoSnackbar = UiKitSnackBar.make(
                view = requireView(),
                params = SnackBarParams(
                    snackBarViewState = SnackBarContainerUiState(
                        messageText = messageString,
                        avatarUiState = iconState,
                    ),
                    duration = BaseTransientBottomBar.LENGTH_SHORT,
                    dismissOnClick = true,
                    paddingState = PaddingState(
                        bottom = countMessageInputBottomPadding()
                    )
                )
            )
            undoSnackbar?.show()
        }

    private fun countMessageInputBottomPadding(): Int {
        val createMessageBlockHeight = binding?.vgCreateBlockMainContainer?.height ?: 0
        val navBarHeight = requireContext().getNavigationBarHeight()
        return createMessageBlockHeight + navBarHeight + DEFAULT_TOAST_BOTTOM_PADDING
    }

    override fun onCommentClicked(post: PostUIEntity, adapterPosition: Int) =
        goToParentPost(post)

    override fun onShowMoreRepostClicked(post: PostUIEntity, adapterPosition: Int) =
        goToParentPost(post)

    override fun onShowMoreTextClicked(post: PostUIEntity, adapterPosition: Int, isOpenPostDetail: Boolean) {
        (parentFragment as? MapSnippetPage)?.setSnippetState(SnippetState.Expanded)
        val updatedList = mutableListOf(post.copy(tagSpan = post.tagSpan?.copy(showFullText = true)))
        val emptyCommentsPlaceholder = adapter?.currentList?.firstOrNull { it.feedType == FeedType.EMPTY_PLACEHOLDER }
        emptyCommentsPlaceholder?.let { updatedList.add(PostUIEntity(feedType = FeedType.EMPTY_PLACEHOLDER)) }
        adapter?.submitList(updatedList)
    }

    override fun onHideMoreTextClicked(post: PostUIEntity, adapterPosition: Int, isOpenPostDetail: Boolean) {
        (parentFragment as? MapSnippetPage)?.setSnippetState(SnippetState.Preview)
        adapter?.submitList(listOf(post.copy(tagSpan = post.tagSpan?.copy(showFullText = false))))
    }

    private fun goToParentPost(post: PostUIEntity) {
        val parentPost = post.parentPost ?: return
        if (parentPost.user?.blackListedMe == true) {
            showToastMessage(
                getString(R.string.parent_post_user_profile_denied_permisson_alert),
                iconState = AvatarUiState.ErrorIconState
            )
        } else {
            if (parentPost.deleted == 1) return
            val isVolumeEnabled = getVolumeState()
            findNavController().safeNavigate(
                resId = R.id.action_meeraPostFragmentV2_to_meeraPostFragmentV2,
                bundle = bundleOf(
                    ARG_FEED_POST_ID to parentPost.postId,
                    IArgContainer.ARG_FEED_POST_POSITION to 1,
                    IArgContainer.ARG_DEFAULT_VOLUME_ENABLED to isVolumeEnabled,
                    IArgContainer.ARG_POST_ORIGIN to postOrigin
                )
            )
        }
    }

    override fun onCommentLongClick(comment: CommentEntityResponse, position: Int) {
        needAuthToNavigate {
            requireContext().lightVibrate()
            if (postItem?.user?.blackListedMe == true) return@needAuthToNavigate
            selectedComment = comment
            val isCommentAuthor = selectedComment?.uid == viewModel.getUserUid()
            val postText = selectedComment?.text ?: ""
            val isPostAuthor = postItem?.user?.userId == viewModel.getUserUid()

            if (isPostAuthor) {
                showBottomOwnerAction(
                    commentId = selectedComment?.id,
                    postText = postText,
                    isCommentAuthor = isCommentAuthor,
                    commentAuthorId = selectedComment?.uid,
                    isPostAuthor = isPostAuthor
                )
            } else {
                if (isCommentAuthor) {
                    showBottomOwnerAction(
                        commentId = selectedComment?.id,
                        postText = postText,
                        isCommentAuthor = isCommentAuthor,
                        commentAuthorId = selectedComment?.uid,
                        isPostAuthor = isPostAuthor
                    )
                } else {
                    showBottomCommonAction(
                        commentId = selectedComment?.id,
                        postText = postText,
                    )
                }
            }

            viewModel.getAmplitudeComments().logOpenCommentOptionsMenu()
        }
    }

    private fun triggerAction(action: PostDetailsActions) =
        viewModel.triggerAction(action)


    override fun onRepostClicked(post: PostUIEntity) {
        requireActivity().vibrate()
        triggerAction(PostDetailsActions.RepostClick(post))
    }

    override fun onPostClicked(post: PostUIEntity, adapterPosition: Int) {
        if (viewModel.postDetailsMode == PostDetailsMode.EVENT_SNIPPET) {
            (parentFragment as? MapSnippetPage)?.setSnippetState(SnippetState.Expanded)
        } else {
            post.parentPost?.let {
                goToParentPost(post)
            } ?: run {
                triggerAction(PostDetailsActions.OnPostClick(post))
            }
        }
    }

    override fun onAvatarClicked(post: PostUIEntity, adapterPosition: Int) {
        post.user?.userId?.let {
            val where = viewModel.getWhereOrigin(post)
            allowScreenshotPopupShowing()
            openUserFragment(userId = post.user.userId, where = where)
        }
    }

    override fun onShowUserMomentsClicked(
        userId: Long,
        fromView: View?,
        hasNewMoments: Boolean?
    ) {
        if ((activity?.application as? FeatureTogglesContainer)?.momentsFeatureToggle?.isEnabled == false) {
            return
        }
        postItem?.user?.userId.let {
            findNavController().navigate(
                R.id.action_global_meeraViewMomentFragment,
                bundleOf(
                    KEY_USER_ID to userId,
                    KEY_MOMENT_CLICK_ORIGIN to MomentClickOrigin.fromUserAvatar(),

                    )
            )
        }
    }

    override fun onTagClicked(
        clickType: SpanDataClickType,
        adapterPosition: Int,
        tagOrigin: TagOrigin,
        post: PostUIEntity?,
    ) {
        clickCheckBubble {
            when (clickType) {
                is SpanDataClickType.ClickBadWord -> {
                    post?.let { triggerAction(PostDetailsActions.OnBadWordClicked(post, tagOrigin, clickType)) }
                }

                is SpanDataClickType.ClickHashtag -> needAuthToNavigate {
                    viewModel.logPressHashTag(post)
                    val navController = if (parentFragment is MeeraEventPostPageFragment) {
                        NavigationManager.getManager().mainMapFragment.closeSnippet(openOnResume = true)
                        NavigationManager.getManager().topNavController
                    } else {
                        findNavController()
                    }
                    navController.safeNavigate(
                        resId = R.id.meeraHashTagFragment,
                        bundle = bundleOf(IArgContainer.ARG_HASHTAG to clickType.hashtag)
                    )
                }

                is SpanDataClickType.ClickUserId -> {
                    allowScreenshotPopupShowing()
                    openUserFragment(clickType.userId)
                }

                is SpanDataClickType.ClickLink -> {
                    act.emitDeeplinkCall(clickType.link)
                }

                else -> {}
            }
        }
    }

    override fun onDismiss() {
        currentBottomMenu = null
    }

    override fun onCancelByUser(menuTag: String?) {
        when (menuTag) {
            COMMENT_MENU_TAG -> {
                viewModel.logCommentMenuAction(action = AmplitudePropertyCommentMenuAction.CANCEL)
            }

            POST_MENU_TAG -> {
                viewModel.logPostMenuAction(
                    action = AmplitudePropertyMenuAction.CANCEL,
                    authorId = postItem?.user?.userId,
                )
            }
        }
    }

    override fun onReactionBottomSheetShow(post: PostUIEntity, adapterPosition: Int) {
        needAuthToNavigate {
            viewModel.showReactionStatistics(post, ReactionsEntityType.POST)
        }
    }

    override fun onReactionRegularClicked(
        post: PostUIEntity,
        adapterPosition: Int,
        reactionHolderViewId: MeeraContentActionBar.ReactionHolderViewId,
        forceDefault: Boolean,
    ) {
        val reactionsParams = post.createAmplitudeReactionsParams(postOrigin)
        act.getMeeraReactionBubbleViewController().onSelectDefaultReaction(
            reactionSource = MeeraReactionSource.Post(
                postId = post.postId,
                reactionHolderViewId = reactionHolderViewId,
                originEnum = postOrigin
            ),
            currentReactionsList = post.reactions ?: emptyList(),
            reactionsParams = reactionsParams,
            forceDefault = forceDefault,
            isShouldVibrate = false
        )
    }

    override fun onReactionLongClicked(
        post: PostUIEntity,
        showPoint: Point,
        reactionTip: TextView,
        viewsToHide: List<View>,
        reactionHolderViewId: MeeraContentActionBar.ReactionHolderViewId,
    ) {
        val reactionsParams = post.createAmplitudeReactionsParams(postOrigin)
        val params = post.toMeeraContentActionBarParams()
        act.getMeeraReactionBubbleViewController().showReactionBubble(
            reactionSource = post.createMeeraReactionSourcePost(reactionHolderViewId),
            showPoint = showPoint,
            viewsToHide = viewsToHide,
            reactionTip = reactionTip,
            currentReactionsList = post.reactions ?: emptyList(),
            contentActionBarType = MeeraContentActionBar.ContentActionBarType.getType(params),
            reactionsParams = reactionsParams,
            containerInfo = act.getDefaultReactionContainer()
        )
    }

    override fun onCommunityClicked(communityId: Long, adapterPosition: Int) = needAuth {
        findNavController().safeNavigate(
            resId = R.id.action_meeraPostFragmentV2_to_meeraCommunityRoadFragment,
            bundle = Bundle().apply {
                putInt(ARG_GROUP_ID, communityId.toInt())
            }
        )
    }

    override fun onCommentProfileClick(comment: CommentEntityResponse) {
        allowScreenshotPopupShowing()
        openUserFragment(comment.uid)
    }

    override fun onFlyingAnimationInitialized(flyingReaction: FlyingReaction) {
        val container = act?.getRootView() as? ViewGroup
        container?.addView(flyingReaction)
        flyingReaction.startAnimationFlying()
        flyingReaction.setFlyingAnimationPlayListener(object : FlyingAnimationPlayListener {
            override fun onFlyingAnimationPlayed(playedFlyingReaction: FlyingReaction) {
                container?.removeView(playedFlyingReaction)
            }
        })
    }

    private fun onPostItemDotsMenuClick(post: PostUIEntity, currentMedia: MediaAssetEntity?) =
        needAuth { wasLoginAuthorization ->
            triggerAction(PostDetailsActions.CheckUpdateAvailability(post, currentMedia))
        }

    private fun showMenu(
        post: PostUIEntity,
        isEditAvailable: Boolean,
        currentMedia: MediaAssetEntity?
    ) {
        needAuthToNavigate {
            if (!optionsMenuEnabled) {
                return@needAuthToNavigate
            }
            requireActivity().vibrate()

            val postId = post.postId
            val userId = post.user?.userId

            // Get post author
            val isPostAuthor = userId == viewModel.getUserUid()

            val menu = MeeraMenuBottomSheet(context)
            if (isEditAvailable) {
                menu.addItem(
                    title = R.string.general_edit,
                    icon = R.drawable.ic_outlined_edit_m,
                    bottomSeparatorVisible = true
                ) {
                    viewModel.logPostMenuAction(
                        action = AmplitudePropertyMenuAction.CHANGE,
                        authorId = postItem?.user?.userId,
                    )
                    triggerAction(PostDetailsActions.EditPost(post))
                }
            }

            addSavingMediaItemsToMenu(menu, userId, currentMedia, post)

            // Подписаться
            if (!isPostAuthor) {
                val textRes = if (post.isPostSubscribed) {
                    if (post.event != null) R.string.unsubscribe_event_post_txt else R.string.unsubscribe_post_txt
                } else {
                    if (post.event != null) R.string.subscribe_event_post_txt else R.string.subscribe_post_txt
                }
                val itemTitle = getString(textRes)
                val img = if (post.isPostSubscribed)
                    R.drawable.ic_outlined_post_delete_m
                else R.drawable.ic_outlined_post_m
                menu.addItem(
                    title = itemTitle,
                    icon = img,
                    bottomSeparatorVisible = true
                ) {
                    if (!post.isPostSubscribed) {
                        viewModel.logPostMenuAction(
                            action = AmplitudePropertyMenuAction.POST_FOLLOW,
                            authorId = userId,
                        )
                        triggerAction(PostDetailsActions.SubscribePost(postId))
                    } else if (post.isPostSubscribed) {
                        viewModel.logPostMenuAction(
                            action = AmplitudePropertyMenuAction.POST_UNFOLLOW,
                            authorId = userId,
                        )
                        triggerAction(PostDetailsActions.UnsubscribePost(postId))
                    }
                }
                post.user?.subscriptionOn?.let { isSubscribed ->
                    if (isSubscribed.isFalse()) {
                        menu.addItem(
                            title = R.string.subscribe_user_txt,
                            icon = R.drawable.ic_outlined_user_add_m,
                            bottomSeparatorVisible = true
                        ) {
                            viewModel.logPostMenuAction(
                                action = AmplitudePropertyMenuAction.USER_FOLLOW,
                                authorId = userId,
                            )
                            triggerAction(
                                PostDetailsActions.SubscribeUser(
                                    postId = postId,
                                    userId = userId,
                                    postOrigin = postOrigin,
                                    needToHideFollowButton = true,
                                    fromFollowButton = false,
                                    approved = post.user.approved.toBoolean(),
                                    topContentMaker = post.user.topContentMaker.toBoolean()
                                )
                            )
                        }
                    }
                }
            }

            if (!post.isPrivateGroupPost) {
                menu.addItem(
                    title = R.string.general_share,
                    icon = R.drawable.ic_outlined_repost_m,
                    bottomSeparatorVisible = true
                ) {
                    triggerAction(PostDetailsActions.RepostClick(post))
                }
                menu.addItem(
                    title = R.string.copy_link,
                    icon = R.drawable.ic_outlined_copy_m,
                    bottomSeparatorVisible = true
                ) {
                    triggerAction(PostDetailsActions.CopyPostLink(post.postId))
                }
            }

            if (!isPostAuthor) {
                if (post.user?.isSystemAdministrator == false && post.user.subscriptionOn.toBoolean().not()) {
                    menu.addItem(
                        title = R.string.profile_complain_hide_all_posts,
                        icon = R.drawable.ic_outlined_eye_off_m,
                        iconAndTitleColor = R.color.uiKitColorAccentWrong
                    ) {
                        viewModel.logPostMenuAction(
                            action = AmplitudePropertyMenuAction.HIDE_USER_POSTS,
                            authorId = userId,
                        )
                        triggerAction(PostDetailsActions.HideUserRoad(userId))
                    }
                }

                // Жалоба на пост
                val complainTitleResId =
                    if (post.event != null) R.string.complain_about_event_post else R.string.complain_about_post
                menu.addItem(
                    complainTitleResId, R.drawable.ic_outlined_attention_m,
                    iconAndTitleColor = R.color.uiKitColorAccentWrong
                ) {
                    viewModel.logPostMenuAction(
                        action = AmplitudePropertyMenuAction.POST_REPORT,
                        authorId = userId,
                    )
                    triggerAction(PostDetailsActions.AddComplaintPost(postId))
                }
            }

            // Удалить
            if (isPostAuthor) {
                menu.addItem(
                    R.string.road_delete, R.drawable.ic_outlined_delete_m,
                    iconAndTitleColor = R.color.uiKitColorAccentWrong
                ) {
                    viewModel.logPostMenuAction(
                        action = AmplitudePropertyMenuAction.DELETE,
                        authorId = userId,
                    )
                    triggerAction(PostDetailsActions.DeletePost(postId))
                }
            }

            menu.showWithTag(manager = childFragmentManager, tag = POST_MENU_TAG)
        }
    }

    private fun addSavingMediaItemsToMenu(
        menu: MeeraMenuBottomSheet,
        postCreatorUid: Long?,
        currentMedia: MediaAssetEntity?,
        post: PostUIEntity
    ) {
        val image: String?
        val video: String?
        var assetId: String? = null

        when {
            currentMedia != null -> {
                image = currentMedia.image
                video = currentMedia.video
                assetId = currentMedia.id
            }

            else -> {
                image = post.getImageUrl()
                video = post.getVideoUrl()
            }
        }

        when {
            !image.isNullOrEmpty() -> addImageItemToMenu(menu, postCreatorUid, image)
            !video.isNullOrEmpty() -> addVideoItemToMenu(menu, postCreatorUid, post.postId, assetId)
        }
    }

    private fun addImageItemToMenu(menu: MeeraMenuBottomSheet, userId: Long?, image: String?) {
        if (image.isNullOrEmpty()) return

        menu.addItem(
            title = R.string.save_image,
            icon = R.drawable.ic_outlined_download_m,
            bottomSeparatorVisible = true
        ) {
            isSavingPostPhoto = true
            viewModel.logPostMenuAction(
                action = AmplitudePropertyMenuAction.SAVE,
                authorId = userId,
                saveType = AmplitudePropertySaveType.PHOTO
            )
            saveImageOrVideoFile(
                imageUrl = image,
                act = act,
                viewLifecycleOwner = viewLifecycleOwner,
                successListener = {
                    doDelayed(SAVING_PICTURE_DELAY) { isSavingPostPhoto = false }
                    showToastMessage(R.string.image_saved)
                }
            )
        }
    }

    private fun addVideoItemToMenu(menu: MeeraMenuBottomSheet, userId: Long?, postId: Long, mediaId: String? = null) {
        val savingVideoIsAvailable = (requireActivity().application as App).remoteConfigs.postVideoSaving
        if (!savingVideoIsAvailable) return

        menu.addItem(
            title = getString(R.string.save_to_device),
            icon = R.drawable.ic_outlined_download_m,
            bottomSeparatorVisible = true
        ) {
            viewModel.logPostMenuAction(
                action = AmplitudePropertyMenuAction.SAVE,
                authorId = userId,
                saveType = AmplitudePropertySaveType.VIDEO
            )
            saveVideo(postId, mediaId)
        }
    }

    override fun onStopLoadingClicked(post: PostUIEntity) {
        viewModel.stopDownloadingPostVideo(postId = post.postId)
    }

    override fun onPictureClicked(post: PostUIEntity) {
        goToContentViewer(post)
    }

    override fun onVideoClicked(post: PostUIEntity, adapterPosition: Int) {
        goToVideoPostFragment(post)
    }

    override fun onMediaClicked(post: PostUIEntity, mediaAsset: MediaAssetEntity, adapterPosition: Int) {
        goToMultimediaPostViewFragment(post, mediaAsset)
    }

    override fun onHolidayWordClicked() {
//        act.showFireworkAnimation {}
    }

    override fun onStartPlayingVideoRequested() {
//        if (isFragmentStarted.not() || isFragmentAdding) return
        startVideoIfExist()
    }

    override fun forceStartPlayingVideoRequested() {
        startVideoIfExist()
    }

    override fun onStopPlayingVideoRequested() {
        stopVideoIfExist()
    }

    override fun onMediaExpandCheckRequested() {
        val holder = binding?.rvPostsComments?.findViewHolderForAdapterPosition(0) as? MeeraMultimediaPostHolder
        holder?.showExpandMediaIndicator()
    }

    override fun onMultimediaPostSwiped(postId: Long, selectedMediaPosition: Int) {
        viewModel.updatePostSelectedMediaPosition(selectedMediaPosition)
    }

    override fun onClickRepostEvent(postId: Long) {
        findNavController().safeNavigate(
            resId = R.id.meeraPostFragmentV2,
            bundle = bundleOf(
                IArgContainer.ARG_FEED_POST_ID to postId,
                IArgContainer.ARG_POST_ORIGIN to DestinationOriginEnum.MAIN_ROAD
            )
        )
    }

    override fun setVolumeState(volumeState: VolumeState) {
        viewModel.setVolumeState(volumeState)
    }

    override fun getVolumeState() = viewModel.getVolumeState()

    private fun goToContentViewer(post: PostUIEntity) {
        if (post.type == PostTypeEnum.AVATAR_HIDDEN || post.type == PostTypeEnum.AVATAR_VISIBLE) {
            findNavController().safeNavigate(
                resId = R.id.action_meeraPostFragmentV2_to_meeraProfilePhotoViewerFragment,
                bundle = bundleOf(
                    IArgContainer.ARG_IS_PROFILE_PHOTO to false,
                    IArgContainer.ARG_IS_OWN_PROFILE to false,
                    IArgContainer.ARG_POST_ID to post.postId,
                    IArgContainer.ARG_GALLERY_ORIGIN to postOrigin
                )
            )
        } else {
//            add( //todo add ViewContentFragment
//                ViewContentFragment(),
//                Act.COLOR_STATUSBAR_BLACK_NAVBAR,
//                Arg(ARG_VIEW_CONTENT_DATA, post),
//                Arg(IArgContainer.ARG_PHOTO_WHERE, AmplitudePropertyWhere.POST_DETAIL),
//                Arg(IArgContainer.ARG_POST_ORIGIN, postOrigin)
//
//            )
        }
    }

    private fun showReactionStatistics(event: PostViewEvent.ShowReactionStatisticsEvent) {
        MeeraReactionsStatisticsBottomDialogFragment.makeInstance(
            event.post.postId,
            event.entityType
        ) { destination ->
            when (destination) {
                is MeeraReactionsStatisticsBottomDialogFragment.DestinationTransition.UserProfileDestination -> {
                    openUserFragment(destination.userEntity.userId)
                }
            }
        }.show(childFragmentManager)

        val where = if (event.post?.isEvent().isTrue()) {
            AmplitudePropertyReactionWhere.MAP_EVENT
        } else {
            AmplitudePropertyReactionWhere.POST
        }

        viewModel.logStatisticReactionsTap(where)
    }

    private fun allowScreenshotPopupShowing() {
        isScreenshotPopupShown = false
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    private fun goToVideoPostFragment(postItem: PostUIEntity) {
        val videoData = ViewVideoInitialData(
            position = videoHelper?.getCurrentPosition() ?: 0,
            duration = videoHelper?.getDuration() ?: 0
        )
        val isVolumeEnabled = getVolumeState()
        stopVideoIfExist()

        findNavController().safeNavigate(
            resId = R.id.action_meeraPostFragmentV2_to_meeraViewVideoFragment,
            bundleOf(
                ARG_VIEW_VIDEO_POST_ID to postItem.postId,
                ARG_VIEW_VIDEO_POST to postItem,
                ARG_VIEW_VIDEO_DATA to videoData,
                IArgContainer.ARG_DEFAULT_VOLUME_ENABLED to isVolumeEnabled,
                IArgContainer.ARG_POST_ORIGIN to postOrigin,
                ARG_NEED_TO_REPOST to !postItem.isPrivateGroupPost
            )
        )
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    private fun goToMultimediaPostViewFragment(postItem: PostUIEntity, mediaAsset: MediaAssetEntity) {
        val videoData = ViewVideoInitialData(
            id = mediaAsset.id,
            position = videoHelper?.getCurrentPosition() ?: 0,
            duration = videoHelper?.getDuration() ?: 0
        )
        stopVideoIfExist()

        findNavController().safeNavigate(
            resId = R.id.action_meeraPostFragmentV2_to_meeraViewMultimediaFragment,
            bundle = bundleOf(
                ARG_VIEW_MULTIMEDIA_POST_ID to postItem.postId,
                ARG_VIEW_MULTIMEDIA_ASSET_ID to mediaAsset.id,
                ARG_VIEW_MULTIMEDIA_ASSET_TYPE to mediaAsset.type,
                ARG_VIEW_MULTIMEDIA_DATA to postItem,
                ARG_VIEW_MULTIMEDIA_VIDEO_DATA to videoData,
                IArgContainer.ARG_POST_ORIGIN to postOrigin,
                ARG_NEED_TO_REPOST to !postItem.isPrivateGroupPost
            )
        )
    }

    private fun saveVideo(postId: Long, assetId: String?) {
        setPermissions(
            object : PermissionDelegate.Listener {
                override fun onGranted() {
                    viewModel.downloadPostVideo(postId, assetId)
                }

                override fun onDenied() {
                    showDeniedPermissionSnackBar(postId, assetId)
                }

                override fun onError(error: Throwable?) {
                    Timber.e("ERROR get Permissions: \$error")
                }
            },
            returnReadExternalStoragePermissionAfter33(),
            returnWriteExternalStoragePermissionAfter33(),
        )
    }

    private fun showDeniedPermissionSnackBar(postId: Long, assetId: String?) {
        undoSnackbar = UiKitSnackBar.make(
            view = binding.srlPostFrg,
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(R.string.you_must_grant_permissions),
                    buttonActionText = getText(R.string.general_retry),
                    buttonActionListener = {
                        saveVideo(postId, assetId)
                        undoSnackbar?.dismiss()
                    },
                    avatarUiState = AvatarUiState.ErrorIconState
                ),
                duration = BaseTransientBottomBar.LENGTH_LONG
            )
        )

        undoSnackbar?.show()
    }

    private fun showConfirmDialogUnsubscribeUser(
        postId: Long?,
        userId: Long?,
        postOriginEnum: DestinationOriginEnum,
        fromFollowButton: Boolean,
        approved: Boolean,
        topContentMaker: Boolean,
    ) {
        MeeraConfirmDialogBuilder()
            .setHeader(getString(R.string.user_info_unsub_dialog_header))
            .setDescription(getString(R.string.unsubscribe_desc))
            .setTopBtnText(getString(R.string.unsubscribe))
            .setBottomBtnText(getString(R.string.general_cancel))
            .setCancelable(true)
            .setTopClickListener {
                triggerAction(
                    PostDetailsActions.UnsubscribeUser(
                        postId = postId,
                        userId = userId,
                        postOrigin = postOriginEnum,
                        fromFollowButton = fromFollowButton,
                        approved = approved,
                        topContentMaker = topContentMaker
                    )
                )
            }
            .show(childFragmentManager)
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    private fun navigateToEditPost(post: PostUIEntity) {
        findNavController().safeNavigate(
            resId = R.id.action_meeraPostFragmentV2_to_meeraCreatePostFragment,
            bundle = bundleOf(
                ARG_GROUP_ID to post.groupId?.toInt(),
                IArgContainer.ARG_POST to post
            )
        )
    }

    private fun startVideoIfExist() {
        if (isPostDeletedOrHidden()) return

        var lastPostMediaViewInfo: PostMediaViewInfo? = null
        runCatching { lastPostMediaViewInfo = viewModel.getLastPostMediaViewInfo() }

        lastPostMediaViewInfo?.let { viewInfo ->
            val postId = viewInfo.postId ?: return@let
            val mediaPosition = viewInfo.viewedMediaPosition ?: return@let
            val postUpdate = UIPostUpdate.UpdateSelectedMediaPosition(postId, mediaPosition)
            adapter?.updateItem(0, postUpdate)
        }

        binding?.rvPostsComments?.postDelayed(
            { videoHelper?.onStart(lastPostMediaViewInfo = lastPostMediaViewInfo) },
            POST_START_VIDEO_DELAY
        )
    }

    private fun stopVideoIfExist() = videoHelper?.onStop()

    override fun onResume() {
        super.onResume()
        viewModel.logScreenForFragment(isCalledFromGroup)
        startVideoIfExist()
        resetLastPostMediaViewInfo()
        isOpened = true
    }

    override fun onPause() {
        super.onPause()
        stopVideoIfExist()
        undoSnackbar?.dismiss()
        __audio.stopPlaying(isLifecycleStop = true)
        resetAllZoomViews()
    }

    @Suppress("unused")
    private fun resetLastPostMediaViewInfo() {
        triggerAction(PostDetailsActions.SaveLastPostMediaViewInfo(null))
    }

    private val priorityListener: () -> Unit = {
        videoHelper?.turnOffAudioOfVideo()
    }

    override fun onStart() {
        super.onStart()
        onStartFragment()
        __audio.addAudioPriorityListener(priorityListener)
        videoHelper?.addVolumeSwitchListener(volumeSwitchListener)
    }

    private fun onStartFragment() {
//        onShowHints()
        viewModel.logScreenForFragment(isCalledFromGroup)
        if (isFirstTimeStarted) isFirstTimeStarted = false
        isOpened = true
        registerComplaintListener()
    }

    private val volumeSwitchListener: () -> Unit = {
        __audio.stopPlaying()
    }

    private fun resetAllZoomViews() {
        binding?.rvPostsComments?.apply {
            val viewHolders = ArrayList<RecyclerView.ViewHolder>()
            for (i in 0 until childCount) {
                val view = getChildAt(i)
                viewHolders.add(getChildViewHolder(view))
            }

            viewHolders.forEach { viewHolder ->
                when (viewHolder) {
                    is MeeraBasePostHolder -> viewHolder.endZoom()
                }
            }
        }
    }

    private fun saveLastMediaViewInfo() {
        val viewHolder = binding?.rvPostsComments?.findViewHolderForAdapterPosition(0)
        if (viewHolder !is VideoViewHolder) return

        val currentMediaPosition = viewHolder.getSelectedMediaPosition() ?: 0
        val currentVideoPlaybackPosition = viewHolder.getVideoPlayerView()?.player?.currentPosition
        val lastMediaViewInfo = PostMediaViewInfo(
            postId = postItem?.postId,
            viewedMediaPosition = currentMediaPosition,
            lastVideoPlaybackPosition = currentVideoPlaybackPosition
        )

        currentVideoPlaybackPosition?.let { startVideoPosition = it }
        postItem = postItem?.copy(selectedMediaPosition = currentMediaPosition)

        viewModel.triggerAction(PostDetailsActions.SaveLastPostMediaViewInfo(lastMediaViewInfo))
    }

    private fun isPostDeletedOrHidden(): Boolean {
        val post = postItem ?: return true

        return post.deleted.toBoolean()
            || post.isPostHidden
            || post.user?.blackListedMe.isTrue()
            || post.user?.blackListedByMe.isTrue()
    }

    override fun onStop() {
        super.onStop()
        onStopFragment()
        stopVideoIfExist()
        __audio.stopPlaying(isLifecycleStop = true)
        __audio.removeAudioPriorityListener(priorityListener)
        saveLastMediaViewInfo()
        viewModel?.clearEvents()
        videoHelper?.removeVolumeSwitchListener(volumeSwitchListener)
    }

    private fun onStopFragment() {
        undoSnackbar?.dismiss()
        __audio.stopPlaying(isLifecycleStop = true)
        resetAllZoomViews()
        unregisterComplaintListener()
    }

    override fun onDestroyView() {
        (binding?.rvPostsComments?.findViewHolderForAdapterPosition(0) as? VideoViewHolder?)?.detachPlayer()
        snippetOnLayoutChangeListener?.let { binding.root.removeOnLayoutChangeListener(it) }
            .also { snippetOnLayoutChangeListener = null }
        videoHelper?.releasePlayer()
        videoHelper = null
        uniqueNameSuggestionMenu = null
        postCollisionDetector = null
        isOpened = false
        reactionAnimationHelper = null
        itemTouchHelper = null
        mCommentsAdapter?.release()
        mCommentsAdapter?.unregisterAdapterDataObserver(adapterDataObserver)
        binding.srlPostFrg?.release()
        removeSnippetScrollListener()
        super.onDestroyView()
    }

    override fun onDestroy() {
        postItem = null
        adapter = null
        mCommentsAdapter = null
        super.onDestroy()
    }

    companion object {
        var isOpened: Boolean = false
    }

    private fun clickCheckBubble(click: () -> Unit) {
        if (isBubbleNotExist()) {
            click()
        }
    }

    private fun isBubbleNotExist(): Boolean {
        val act = context as? MeeraAct ?: return false
        val bubble =
            (act.getRootView() as? ViewGroup)?.children?.find { it is MeeraReactionBubble } as? MeeraReactionBubble
        return bubble == null
    }

    private fun showNotAvailableToEditPostError(reason: NotAvailableReasonUiEntity) {
        when (reason) {
            NotAvailableReasonUiEntity.POST_NOT_FOUND -> showToastMessage(
                getString(R.string.post_edit_error_not_found_message),
                iconState = AvatarUiState.WarningIconState
            )

            NotAvailableReasonUiEntity.USER_NOT_CREATOR -> showToastMessage(
                getString(R.string.post_edit_error_not_creator_message),
                iconState = AvatarUiState.WarningIconState
            )

            NotAvailableReasonUiEntity.POST_DELETED -> showToastMessage(
                getString(R.string.post_edit_error_deleted_message),
                iconState = AvatarUiState.WarningIconState
            )

            NotAvailableReasonUiEntity.EVENT_POST_UNABLE_TO_UPDATE,
            NotAvailableReasonUiEntity.UPDATE_TIME_IS_OVER -> {
                MeeraConfirmDialogBuilder()
                    .setHeader(getString(R.string.post_edit_error_expired_title))
                    .setDescription(getString(R.string.post_edit_error_expired_description))
                    .setTopBtnText(getString(R.string.i_have_read))
                    .setTopBtnType(ButtonType.FILLED)
                    .hideBottomBtn()
                    .show(childFragmentManager)
            }
        }
    }

}
