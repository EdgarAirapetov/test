package com.numplates.nomera3.modules.comments.ui.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.extensions.addSpanBoldRangesClickColored
import com.meera.core.extensions.animateHeight
import com.meera.core.extensions.clearText
import com.meera.core.extensions.click
import com.meera.core.extensions.color
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.doOnUIThread
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.invisible
import com.meera.core.extensions.isFalse
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.lightVibrate
import com.meera.core.extensions.returnReadExternalStoragePermissionAfter33
import com.meera.core.extensions.returnWriteExternalStoragePermissionAfter33
import com.meera.core.extensions.setListener
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.string
import com.meera.core.extensions.stringNullable
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.vibrate
import com.meera.core.extensions.visible
import com.meera.core.extensions.visibleAppearAnimate
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.NSnackbar
import com.meera.core.utils.blur.BlurHelper
import com.meera.core.utils.checkAppRedesigned
import com.meera.core.utils.graphics.SpanningLinearLayoutManager
import com.meera.core.utils.pagination.RecyclerPaginationUtil
import com.meera.db.models.message.ParsedUniquename
import com.numplates.nomera3.Act
import com.numplates.nomera3.Act.Companion.COLOR_STATUSBAR_LIGHT_NAVBAR
import com.numplates.nomera3.App
import com.numplates.nomera3.FEED_START_VIDEO_DELAY
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentPostv2Binding
import com.numplates.nomera3.modules.auth.ui.IAuthStateObserver
import com.numplates.nomera3.modules.auth.util.AuthStatusObserver
import com.numplates.nomera3.modules.auth.util.needAuth
import com.numplates.nomera3.modules.baseCore.helper.AudioFeedHelper
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegate
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegateImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyMenuAction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.comments.AmplitudePropertyCommentMenuAction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.findfriends.AmplitudeFindFriendsWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.model.MapSnippetCloseMethod
import com.numplates.nomera3.modules.baseCore.helper.amplitude.post.AmplitudePropertySaveType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudePropertyReactionWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.createAmplitudeReactionsParams
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.createReactionSourcePost
import com.numplates.nomera3.modules.chat.helpers.replymessage.ReplySwipeController
import com.numplates.nomera3.modules.chat.helpers.replymessage.SwipeControllerActions
import com.numplates.nomera3.modules.chat.helpers.replymessage.SwipingItemType
import com.numplates.nomera3.modules.comments.data.api.OrderType
import com.numplates.nomera3.modules.comments.data.entity.CommentEntityResponse
import com.numplates.nomera3.modules.comments.ui.adapter.CommentAdapter
import com.numplates.nomera3.modules.comments.ui.adapter.ICommentsActionsCallback
import com.numplates.nomera3.modules.comments.ui.adapter.PostDetailAdapter
import com.numplates.nomera3.modules.comments.ui.entity.CommentChunk
import com.numplates.nomera3.modules.comments.ui.entity.CommentEntity
import com.numplates.nomera3.modules.comments.ui.entity.CommentUIType
import com.numplates.nomera3.modules.comments.ui.entity.PostDetailsMode
import com.numplates.nomera3.modules.comments.ui.util.PostViewTopDividerDecoration
import com.numplates.nomera3.modules.comments.ui.util.SpeedyLinearLayoutManager
import com.numplates.nomera3.modules.comments.ui.viewholder.CommentViewHolderPlayAnimation
import com.numplates.nomera3.modules.comments.ui.viewmodel.PostDetailsActions
import com.numplates.nomera3.modules.comments.ui.viewmodel.PostViewModelV2
import com.numplates.nomera3.modules.common.ActivityToolsProvider
import com.numplates.nomera3.modules.communities.ui.fragment.CommunityRoadFragment
import com.numplates.nomera3.modules.communities.utils.copyCommunityLink
import com.numplates.nomera3.modules.complains.ui.ComplainEvents
import com.numplates.nomera3.modules.complains.ui.ComplainsNavigator
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.feed.data.entity.PostMediaViewInfo
import com.numplates.nomera3.modules.feed.domain.mapper.toPost
import com.numplates.nomera3.modules.feed.ui.CacheUtil
import com.numplates.nomera3.modules.feed.ui.PostCallback
import com.numplates.nomera3.modules.feed.ui.adapter.ContentActionBar
import com.numplates.nomera3.modules.feed.ui.adapter.MediaLoadingState
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.entity.MediaAssetEntity
import com.numplates.nomera3.modules.feed.ui.entity.PostTypeEnum
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.entity.UIPostUpdate
import com.numplates.nomera3.modules.feed.ui.mapper.toUIPostUpdate
import com.numplates.nomera3.modules.feed.ui.viewholder.BasePostHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.MultimediaPostHolder
import com.numplates.nomera3.modules.feedmediaview.ui.fragment.ARG_VIEW_MULTIMEDIA_ASSET_ID
import com.numplates.nomera3.modules.feedmediaview.ui.fragment.ARG_VIEW_MULTIMEDIA_ASSET_TYPE
import com.numplates.nomera3.modules.feedmediaview.ui.fragment.ARG_VIEW_MULTIMEDIA_DATA
import com.numplates.nomera3.modules.feedmediaview.ui.fragment.ARG_VIEW_MULTIMEDIA_POST_ID
import com.numplates.nomera3.modules.feedmediaview.ui.fragment.ARG_VIEW_MULTIMEDIA_VIDEO_DATA
import com.numplates.nomera3.modules.feedmediaview.ui.fragment.ViewMultimediaFragment
import com.numplates.nomera3.modules.feedviewcontent.presentation.fragment.ARG_VIEW_CONTENT_DATA
import com.numplates.nomera3.modules.feedviewcontent.presentation.fragment.ViewContentFragment
import com.numplates.nomera3.modules.hashtag.ui.fragment.HashtagFragment
import com.numplates.nomera3.modules.maps.ui.MapUiActionHandler
import com.numplates.nomera3.modules.maps.ui.events.participants.openEventNavigation
import com.numplates.nomera3.modules.maps.ui.events.participants.openEventOnMap
import com.numplates.nomera3.modules.maps.ui.events.participants.openEventParticipantsList
import com.numplates.nomera3.modules.maps.ui.events.snippet.EventSnippetPage
import com.numplates.nomera3.modules.maps.ui.events.snippet.EventSnippetPageContent
import com.numplates.nomera3.modules.maps.ui.model.MapUiAction
import com.numplates.nomera3.modules.maps.ui.snippet.model.SnippetState
import com.numplates.nomera3.modules.maps.ui.view.MapSnippetPage
import com.numplates.nomera3.modules.newroads.VideoFeedHelper
import com.numplates.nomera3.modules.newroads.data.ISensitiveContentManager
import com.numplates.nomera3.modules.newroads.fragments.BaseRoadsFragment
import com.numplates.nomera3.modules.newroads.ui.adapter.QuickAnswerAdapter
import com.numplates.nomera3.modules.post_view_statistic.data.PostViewRoadSource
import com.numplates.nomera3.modules.post_view_statistic.presentation.PostCollisionDetector
import com.numplates.nomera3.modules.posts.ui.model.PostHeaderEvent
import com.numplates.nomera3.modules.posts.ui.model.PostHeaderNavigationMode
import com.numplates.nomera3.modules.posts.ui.model.PostHeaderUiModel
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.data.ReactionUpdate
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingAnimationPlayListener
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingReaction
import com.numplates.nomera3.modules.reaction.ui.custom.ReactionBottomMenuItem
import com.numplates.nomera3.modules.reaction.ui.data.ReactionSource
import com.numplates.nomera3.modules.reaction.ui.getDefaultReactionContainer
import com.numplates.nomera3.modules.reaction.ui.mapper.toContentActionBarParams
import com.numplates.nomera3.modules.reaction.ui.util.getMyReaction
import com.numplates.nomera3.modules.reaction.ui.util.reactionCount
import com.numplates.nomera3.modules.reactionStatistics.ui.MeeraReactionsStatisticsBottomSheetFragment
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsEntityType
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsStatisticsBottomSheetFragment
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
import com.numplates.nomera3.modules.uploadpost.ui.AddMultipleMediaPostFragment
import com.numplates.nomera3.modules.user.ui.fragments.AdditionalComplainCallback
import com.numplates.nomera3.modules.user.ui.fragments.UserComplainAdditionalBottomSheet
import com.numplates.nomera3.modules.viewvideo.presentation.ARG_VIEW_VIDEO_DATA
import com.numplates.nomera3.modules.viewvideo.presentation.ARG_VIEW_VIDEO_POST
import com.numplates.nomera3.modules.viewvideo.presentation.ARG_VIEW_VIDEO_POST_ID
import com.numplates.nomera3.modules.viewvideo.presentation.ViewVideoItemFragment
import com.numplates.nomera3.modules.viewvideo.presentation.data.ViewVideoInitialData
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.modules.volume.presentation.VolumeStateCallback
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FEED_POST_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FEED_POST_NEED_TO_UPDATE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GROUP_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_HASHTAG
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_IS_POST_FRAGMENT_CALLED_FROM_GROUP
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_NEED_TO_REPOST
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_NEED_TO_SHOW_HIDE_POSTS_BTN
import com.numplates.nomera3.presentation.utils.handleSpanTagsInPosts
import com.numplates.nomera3.presentation.view.adapter.newpostlist.PostListCallback
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.numplates.nomera3.presentation.view.fragments.MapFragment
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment
import com.numplates.nomera3.presentation.view.fragments.dialogs.ConfirmDialogBuilder
import com.numplates.nomera3.presentation.view.fragments.profilephoto.ProfilePhotoViewerFragment
import com.numplates.nomera3.presentation.view.ui.BottomSheetDialogEventsListener
import com.numplates.nomera3.presentation.view.ui.CloseTypes
import com.numplates.nomera3.presentation.view.ui.VideoViewHolder
import com.numplates.nomera3.presentation.view.ui.bottomMenu.COMMENT_MENU_TAG
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import com.numplates.nomera3.presentation.view.ui.bottomMenu.POST_MENU_TAG
import com.numplates.nomera3.presentation.view.ui.bottomMenu.ReactionsStatisticBottomMenu
import com.numplates.nomera3.presentation.view.utils.NToast
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
private const val DELAY_DELETE_COMMENT = 5

/**
 * Show post screen
 */
class PostFragmentV2(private var postListCallback: PostListCallback?) :
    BaseFragmentNew<FragmentPostv2Binding>(),
    ICommentsActionsCallback,
    IOnBackPressed,
    PostCallback,
    VolumeStateCallback,
    IAuthStateObserver,
    MeeraMenuBottomSheet.Listener,
    BasePermission by BasePermissionDelegate(),
    SaveMediaFileDelegate by SaveMediaFileDelegateImpl(),
    EventSnippetPageContent,
    ScreenshotTakenListener {

    constructor() : this(null)

    private lateinit var mCommentsAdapter: CommentAdapter

    private lateinit var adapter: PostDetailAdapter
    private val viewModel by viewModels<PostViewModelV2>()
    private val complainsNavigator by lazy(LazyThreadSafetyMode.NONE) { ComplainsNavigator(requireActivity()) }

    private val __audio: AudioFeedHelper
        get() = viewModel.audioFeedHelper
    private var mainAdapter: ConcatAdapter? = null

    private var postId: Long? = null
    private var postItem: PostUIEntity? = null
    private var needToUpdate: Boolean = false
    private var postAdapterPosition: Int? = null
    private var roadType: Int? = BaseRoadsFragment.RoadTypeEnum.MAIN_ROAD.index
    private var needToShowHideRoadBtn = true
    private var postOrigin: DestinationOriginEnum? = null
    private var postHaveReactions: Boolean = false
    private var postLatestReactionType: ReactionType? = null

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

    private var undoSnackbar: NSnackbar? = null

    //По данному полю мы определяем к какому комментарию нам нужно проскролить
    //если мы переходим из уведомления или пуша
    private var commentId: Long? = null

    //список id юзеров которые были заблокированы при помощи меню заблокировать
    private var blockedUsersList = mutableSetOf<Long>()

    private var currentBottomMenu: MeeraMenuBottomSheet? = null

    private var postCollisionDetector: PostCollisionDetector? = null

    var itemTouchHelper: ItemTouchHelper? = null

    private var optionsMenuEnabled = false

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
        act?.showFireworkAnimation()
    }

    override fun onCommentLikeClick(comment: CommentEntityResponse) {
        val post = postItem ?: return
        val reactionSource = ReactionSource.PostComment(
            postId = post.postId,
            postUserId = post.user?.userId,
            commentUserId = comment.user.userId,
            commentId = comment.id,
            originEnum = postOrigin
        )

        val toolsProvider = activity as? ActivityToolsProvider ?: return
        toolsProvider
            .getReactionBubbleViewController()
            .onSelectDefaultReaction(
                reactionSource = reactionSource,
                currentReactionsList = comment.reactions,
                forceDefault = false,
                reactionsParams = post.createAmplitudeReactionsParams(reactionSource.originEnum),
                isShouldVibrate = false
            )
    }

    override fun onCommentLinkClick(url: String?) {
        act.openLink(url)
    }

    override fun onScreenshotTaken() {
        resetAllZoomViews()
        if (isSavingPostPhoto) return
        triggerAction(PostDetailsActions.GetPostDataForScreenshotPopup(postId, postItem?.event))
    }

    override fun onCommentDoubleClick(comment: CommentEntityResponse) = act.needAuth {
        val post = postItem ?: return@needAuth
        val toolsProvider = activity as? ActivityToolsProvider ?: return@needAuth
        val isCurrentUserAlreadySetLike = comment.reactions.getMyReaction() == ReactionType.GreenLight

        mCommentsAdapter.playCommentAnimation(
            commentId = comment.id,
            animation = CommentViewHolderPlayAnimation.PlayLikeOnDoubleClickAnimation
        )

        if (!isCurrentUserAlreadySetLike) {
            val reactionSource = ReactionSource.PostComment(
                postId = post.postId,
                postUserId = post.user?.userId,
                commentUserId = comment.user.userId,
                commentId = comment.id,
                originEnum = postOrigin
            )

            toolsProvider
                .getReactionBubbleViewController()
                .onSelectDefaultReaction(
                    reactionSource = reactionSource,
                    currentReactionsList = comment.reactions,
                    forceDefault = true,
                    isShouldVibrate = false
                )
        }
    }

    override fun onCommentPlayClickAnimation(commentId: Long) {
        mCommentsAdapter.playCommentAnimation(
            commentId = commentId,
            animation = CommentViewHolderPlayAnimation.PlayLikeOnDoubleClickAnimation
        )
    }

    override fun onReactionBadgeClick(comment: CommentEntityResponse) {
        needAuth {
            if (viewModel.getFeatureTogglesContainer().detailedReactionsForCommentsFeatureToggle.isEnabled) {
                checkAppRedesigned (
                    isRedesigned = {
                        MeeraReactionsStatisticsBottomSheetFragment.getInstance(
                            entityId = comment.id,
                            entityType = ReactionsEntityType.COMMENT
                        ).show(childFragmentManager)
                    },
                    isNotRedesigned = {
                        ReactionsStatisticsBottomSheetFragment.getInstance(
                            entityId = comment.id,
                            entityType = ReactionsEntityType.COMMENT
                        ).show(childFragmentManager)
                    }
                )

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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (parentFragment) {
            is MapSnippetPage -> initAsSnippet()
            is MapFragment -> initAsEventPostDetails()
            else -> initAsFullPost()
        }
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
    }

    private fun initAsSnippet() {
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)
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

    private fun finishSnippetSetup() {
        initReplyViews()
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
        // параметры для настройки плашек блокирования
        val isMeBlocked = post.user?.blackListedMe == true
        val isPostCommentable = post.isAllowedToComment

        // если пользователь который смотрит пост в ЧС,
        // то показываем плашку чтоб вы заблокированы
        // (остальные параметры не проверяем ибо ЧС выше по уровню)
        if (isMeBlocked) {
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
                binding?.vgCreateBlockMainContainer?.invisible()
                binding?.vgBlockedHolder?.visible()
            }
        }
        optionsMenuEnabled = true
    }

    override fun initAuthObserver(): AuthStatusObserver = object : AuthStatusObserver(act, this) {
        override fun onAuthState() {
            binding?.etWriteComment?.setOnClickListener(null)
            binding?.etWriteComment?.isFocusable = true
            binding?.etWriteComment?.isLongClickable = true
        }

        override fun onNotAuthState() {
            binding?.etWriteComment?.setOnClickListener {
                needAuth()
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
                result.isFailure -> showError(context?.string(R.string.user_complain_error))
            }
        }
    }

    private fun unregisterComplaintListener() {
        complainsNavigator.unregisterAdditionalActionListener()
    }

    private fun showError(msg: String?) {
        NToast.with(view)
            .typeError()
            .text(msg)
            .show()
    }

    private fun showMessage(msg: String?) = doOnUIThread {
        NToast.with(view)
            .typeSuccess()
            .text(msg)
            .show()
    }

    private fun showAdditionalStepsForComplain(userId: Long) {
        val bottomSheet = UserComplainAdditionalBottomSheet.newInstance(userId).apply {
            callback = object : AdditionalComplainCallback {
                override fun onSuccess(msg: String?, reason: ComplainEvents) {
                    showMessage(msg)
                }

                override fun onError(msg: String?) {
                    showError(msg)
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
        //TODO legacy метод рефреш лисенера внутри легаси класса
//        binding?.srlPostFrg?.setOnRefreshListener {
//            if (it == SwipyRefreshLayoutDirection.TOP) refresh()
//            else if (it == SwipyRefreshLayoutDirection.BOTTOM) viewModel.addCommentsAfter()
//        }
        binding?.rvPostsComments?.visible()
        optionsMenuEnabled = false

        binding?.etWriteComment?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().trim().isEmpty())
                    binding?.ivSendComment?.gone()
                else if (binding?.ivSendComment?.visibility != View.VISIBLE) {
                    binding?.ivSendComment?.visibleAppearAnimate()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        })

        binding?.btnScrollDown?.setThrottledClickListener {
            viewModel.getLastComments(postId)
        }
    }

    private fun initReplyViews() {
        initQuickAnswerMenu()
        initMentionableEditText()
    }

    private fun initMentionableEditText() {
//        binding?.etWriteComment?.setCheckUniqueNameStrategy(EditTextExtended.CheckUniqueNameStrategyAddComment())
//        binding?.etWriteComment?.setOnNewUniqueNameAfterTextChangedListener(object :
//            EditTextExtended.OnNewUniqueNameListener {
//            override fun onNewUniqueName(uniqueName: String) {
//                searchUsersByUniqueName(uniqueName)
//            }
//        })
//
//        binding?.etWriteComment?.setOnUniqueNameNotFoundListener(object :
//            EditTextExtended.OnUniqueNameNotFoundListener {
//            override fun onNotFound() {
//                if (uniqueNameSuggestionMenu?.isHidden == false) {
//                    uniqueNameSuggestionMenu?.forceCloseMenu()
//                }
//            }
//        })
//
//        binding?.tagsList?.let { tagsList ->
//            tagsList.root.visible()
//            val bottomSheetBehavior = BottomSheetBehavior.from(tagsList.root as View)
//            uniqueNameSuggestionMenu = SuggestionsMenu(this)
//            binding?.tagsList?.recyclerTags?.let { recyclerTags ->
//                binding?.etWriteComment?.let { etWriteComment ->
//                    uniqueNameSuggestionMenu?.init(
//                        recyclerTags,
//                        etWriteComment,
//                        bottomSheetBehavior
//                    )
//
//                    uniqueNameSuggestionMenu?.onSuggestedUniqueNameClicked =
//                        fun(userData: UITagEntity) {
//                            replaceUniqueNameBySuggestion(userData)
//                            uniqueNameSuggestionMenu?.forceCloseMenu()
//                        }
//                }
//            }
//        }
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
            needAuth {
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

                viewModel.analyticsInteractor.logEmojiTap(emojiName)
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
        if (!isHidenScrollDownBtn || mCommentsAdapter.itemCount == 0) {
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

        binding?.btnScrollDown?.visible()
    }


    private fun initSendBtn() {
        binding?.ivSendComment?.click {
            var message = binding?.etWriteComment?.text.toString()
            if (message.isEmpty()) return@click
            message = message.trim()

            Timber.d("comment to sent = $message")

            if (message.trim().isNotEmpty() && isShowProgress) {
                showError(getString(R.string.cant_send_comment_whil_loading))
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
            adapter.submitList(list)
            if (list.isNotEmpty()) {
                updatePost(list[0])
            }
        }
        viewModel.failure.observe(viewLifecycleOwner) { failure ->
            renderFailures(failure)
        }
        viewModel.livePostViewEvent.observe(viewLifecycleOwner) { event ->
            handleEvents(event)
        }
        viewModel.liveComments.observe(viewLifecycleOwner) {
            when (it.order) {
                OrderType.AFTER -> {
                    mCommentsAdapter.addItemsNext(it)
                }

                OrderType.BEFORE -> {
                    mCommentsAdapter.addItemsPrevious(it)
                }

                OrderType.INITIALIZE -> {
                    mCommentsAdapter.refresh(it) { scroll ->
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
                color = requireContext().color(R.color.ui_purple),
                rangeList = inputState.wordsRanges,
                onClickListener = {
                    act.showFireworkAnimation {}
                }
            )
        }
    }

    private fun updatePost(post: PostUIEntity) {
        initPrivacy(post)
        postItem = post
        postItem?.let(::initToolbar)
        if (viewModel.isUserDeletedOwnPost().not()) {
            (parentFragment as? EventSnippetPage)?.onEventPostUpdated(post)
        }
    }

    private fun initRecycler() {
        // Чтоб не крашился с lateinit exception
        val formatterProvider = AllRemoteStyleFormatter(viewModel.getSettings())
        adapter = PostDetailAdapter(
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
        adapter.isNeedToShowFlyingReactions =
            postOrigin == DestinationOriginEnum.NOTIFICATIONS_REACTIONS || postHaveReactions
        adapter.isNeedToShowRepostBtn = isNeedToShowRepostBtn
        adapter.postLatestReactionType = postLatestReactionType

        //слушатель на bind необходим для пометки постов и комментариев к нему как просмотренных
        adapter.bindListener = {
            viewModel.onItemSeen(it)
        }

        mCommentsAdapter = CommentAdapter(
            commentListCallback = this
        ) {
            viewModel.addInnerComment(it)
        }
        mCommentsAdapter.collectionUpdateListener = viewModel.commentObserver

        mCommentsAdapter.innerSeparatorItemClickListener = {
            doDelayed(100) {
                handleScrollDownBtnVisibility()
            }
        }
        mainAdapter = ConcatAdapter(adapter, mCommentsAdapter)

        // handle comment placeholder
        mCommentsAdapter.registerAdapterDataObserver(adapterDataObserver)

        //TODO legacy метод рефреш лисенера внутри легаси класса
//        binding?.srlPostFrg?.setOnRefreshListener {
//            if (it == SwipyRefreshLayoutDirection.TOP) {
//                triggerAction(PostDetailsActions.Refresh)
//            } else if (it == SwipyRefreshLayoutDirection.BOTTOM) {
//                viewModel.addCommentsAfter()
//                binding?.rvPostsComments?.smoothScrollToPosition(mainAdapter?.itemCount ?: 1 - 1)
//            }
//
//            binding?.srlPostFrg?.isRefreshing = false
//            blockedUsersList.clear()
//        }

        val commentRecyclerViewLayoutManager = SpeedyLinearLayoutManager(requireContext())
        binding?.rvPostsComments?.apply {
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
            layoutManager = commentRecyclerViewLayoutManager
            adapter = mainAdapter
        }

        val divider = PostViewTopDividerDecoration.build(requireContext())
        binding?.rvPostsComments?.addItemDecoration(divider)

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
                    mCommentsAdapter.getItem(absoluteAdapterPosition - 1)?.comment?.let {
                        onCommentReplyClick(it)
                    }
                }
            })

        itemTouchHelper = ItemTouchHelper(messageSwipeController)
        itemTouchHelper?.attachToRecyclerView(binding?.rvPostsComments)

        initOnScrollListener()

        viewModel.paginationHelper.isLoadingBeforeCallback = {
            if (it) {
                mCommentsAdapter.addLoadingProgressBefore()
            } else mCommentsAdapter.removeLoadingProgressBefore()
        }

        viewModel.paginationHelper.isLoadingAfterCallback = {
            if (it)
                mCommentsAdapter.addLoadingProgressAfter()
            else mCommentsAdapter.removeLoadingProgressAfter()
        }

        initPostViewCollisionDetector(binding?.rvPostsComments)
    }

    private fun setSwipeRefreshDirections(snippetState: SnippetState?) {
        binding?.srlPostFrg?.setRefreshEnable(snippetState == null)
    }

    private val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)

            if (mCommentsAdapter.itemCount > 0) {
                triggerAction(PostDetailsActions.RemoveEmptyCommentsPlaceHolder)
            }
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            super.onItemRangeRemoved(positionStart, itemCount)

            if (mCommentsAdapter.itemCount == 0) {
                triggerAction(PostDetailsActions.AddEmptyCommentsPlaceHolder)
            }
        }

        override fun onChanged() {
            super.onChanged()
            if (mCommentsAdapter.itemCount == 0) {
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
                    postListCallback?.onOpenPostClicked(it)
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
                        binding?.rvPostsComments?.postDelayed({ videoHelper?.playVideo(0L) }, FEED_START_VIDEO_DELAY)
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
        adapter.videoBinded = {
            binding?.rvPostsComments?.postDelayed({ startVideo(it, startVideoPosition) }, FEED_START_VIDEO_DELAY)
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
            ||  post.postUpdatingLoadingInfo.loadingState == MediaLoadingState.LOADING_NO_CANCEL_BUTTON

        val postHeaderUiModel = PostHeaderUiModel(
            post = post,
            navigationMode = navigationMode,
            isOptionsAvailable = true,
            childPost = null,
            isCommunityHeaderEnabled = true,
            isLightNavigation = isBottomSheetToolbar,
            editInProgress = isUpdatingState
        )
        binding?.phvPostv2Header?.bind(postHeaderUiModel)
        binding?.phvPostv2Header?.setEventListener { event ->
            when (event) {
                PostHeaderEvent.BackClicked -> act.onBackPressed()
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
        return if (postHolder is MultimediaPostHolder) {
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
    }

    private fun updateHeaderAvatar(post: PostUIEntity) {
        binding?.phvPostv2Header?.updateUserAvatar(post)
    }

    private fun showScreenshotPopup(postLink: String, eventIconRes: Int?, eventDateAndTime: String?) {
        if (isScreenshotPopupShown) return
        isScreenshotPopupShown = true
        val post = postItem ?: return
        val screenshotPlace = when {
            parentFragment is MapFragment || parentFragment is MapSnippetPage -> ScreenshotPlace.MAP_EVENT
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
        screenshotPlace = screenshotPlace
    )

    private fun getMusicHolder() : BasePostHolder? {
        val postView = binding?.rvPostsComments?.getChildAt(0) ?: return null
        val postHolder = binding?.rvPostsComments?.getChildViewHolder(postView) ?: return null
        return postHolder as? BasePostHolder?
    }

    private fun handleEvents(event: PostViewEvent) {
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

            is PostViewEvent.EnableComments -> {
                binding?.ivSendComment?.isEnabled = true
                binding?.etWriteComment?.isEnabled = true
            }

            is PostViewEvent.UserBlocked -> {
                blockedUsersList.add(event.userId)
                showNToast(getString(R.string.you_blocked_user))
            }

            is PostViewEvent.ComplainSuccess ->
                showNToast(getString(R.string.complain_send))

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
                mCommentsAdapter.stopProgressInnerPagination(event.data)
                showTextError(getString(R.string.no_internet))
            }

            is PostViewEvent.ShowTextError -> showTextError(event.message)
            is PostViewEvent.NoInternet -> showTextError(getString(R.string.no_internet))
            is PostViewEvent.NoInternetAction -> showTextError(getString(R.string.internet_connection_problem_action))
            is PostViewEvent.ErrorDeleteComment -> {
                showTextError(getString(R.string.no_internet))
                mCommentsAdapter.restoreComment(event.comment)
            }

            is PostViewEvent.OnScrollToBottom -> {
                mainAdapter?.itemCount?.let {
                    binding?.rvPostsComments?.scrollToPosition(it - 1)
                }
            }

            is PostViewEvent.UpdateCommentsReplyAvailability -> {
                mCommentsAdapter.updateAllReplyButtonsState(needToShowReplyBtn = event.needToShowReplyBtn)
            }

            is PostViewEvent.UpdateCommentReaction -> {
                mCommentsAdapter.notifyItemChanged(event.position, event.reactionUpdate)
                updateBottomMenuReactions(event.reactionUpdate)
            }

            is PostViewEvent.UpdatePostReaction -> {
                postItem = event.post
                adapter.updateItem(0, event.reactionUpdate.toUIPostUpdate())
                updateBottomMenuReactions(event.reactionUpdate)
            }

            is PostViewEvent.UpdateLoadingState -> {
                postItem = event.post
                adapter.notifyItemChanged(0, UIPostUpdate.UpdateLoadingState(event.post.postId, event.loadingInfo))
            }

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
                    adapter.updatePost(
                        UIPostUpdate.UpdateEventPostParticipationState(postId = post.postId, postUIEntity = post)
                    )
                } else {
                    adapter.submitList(listOf(post))
                }
                updatePost(post)
            }

            is PostViewEvent.ShowEventSharingSuggestion -> {
                checkAppRedesigned(
                    isRedesigned = {
                        meeraOpenRepostMenu(
                            post = event.post,
                            mode = SharingDialogMode.SUGGEST_EVENT_SHARING
                        )
                    },
                    isNotRedesigned = {
                        openRepostMenu(
                            post = event.post,
                            mode = SharingDialogMode.SUGGEST_EVENT_SHARING
                        )
                    }
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
                restartPlayer()
            }
            is PostViewEvent.UpdateVolumeState -> handleUpdateVolumeState(event.volumeState)
            is PostViewEvent.UpdateTagSpan -> handleUpdateTagSpan(event.postUpdate)
            is PostViewEvent.UpdatePostValues -> handleUpdatePostValues(event.postUpdate)
            else -> Unit
        }
    }

    private fun handleUpdateVolumeState(volumeState: VolumeState) {
        adapter.updateVolumeState(volumeState)

        if (volumeState == VolumeState.ON) __audio.stopPlaying()
    }

    private fun handleUpdateTagSpan(uiPostUpdate: UIPostUpdate.UpdateTagSpan) {
        adapter.updateItem(0, uiPostUpdate)
    }

    private fun handleUpdatePostValues(uiPostUpdate: UIPostUpdate) {
        adapter.updateItem(0, uiPostUpdate)
    }

    private fun restartPlayer() {
        startVideoPosition = 0
        initVideoHelper()
    }

    private fun updateBottomMenuReactions(reactionUpdate: ReactionUpdate) {
        val reactionsMenuItem = currentBottomMenu?.getMenuItem<ReactionBottomMenuItem>() ?: return

        reactionsMenuItem.setReaction(reactionUpdate.reactionList)
    }

    private fun showTextError(message: String) {
        view?.let {
            NToast.with(it)
                .typeError()
                .text(message)
                .show()
        }
    }

    private fun handleNewComment(
        beforeMyComment: List<CommentUIType>, // including my comment
        hasIntersection: Boolean,
        needSmoothScroll: Boolean,
        needToShowLastFullComment: Boolean,
    ) {
        val itemAnimator = binding?.rvPostsComments?.itemAnimator
        binding?.rvPostsComments?.itemAnimator = null
        val index = mCommentsAdapter.itemCount

        if (needToShowLastFullComment) {
            showFullLastComment(beforeMyComment)
        }
        mCommentsAdapter.addItemsNext(beforeMyComment)

        mainAdapter?.itemCount?.let {
            if (!needSmoothScroll) binding?.rvPostsComments?.scrollToPosition(it - 1)
            else binding?.rvPostsComments?.smoothScrollToPosition(it - 1)
        }

        if (!hasIntersection) {
            doDelayed(50) {
                mCommentsAdapter.removeItemsBefore(index = index)
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
        mCommentsAdapter.addItemsNext(parentId, chunk) {
            binding?.rvPostsComments?.smoothScrollToPosition(it)
        }
        doDelayed(200) {
            binding?.rvPostsComments?.itemAnimator = itemAnimator
        }
    }

    private fun onDeletedPost() {
        showMessage(getString(R.string.post_deleted_success))
        when (viewModel.postDetailsMode) {
            PostDetailsMode.EVENT_SNIPPET -> (parentFragment as? EventSnippetPage)?.onUserDeletedOwnPost()
            PostDetailsMode.EVENTS_LIST -> {
                val uiAction = MapUiAction.EventsListUiAction.EventsListItemDeleted(postId ?: return)
                (parentFragment as? MapUiActionHandler)?.handleOuterMapUiAction(uiAction)
            }
            else -> act.onBackPressed()
        }
    }

    private fun onHideUserPost() {
        showMessage(getString(R.string.post_hide_success))
        act?.onBackPressed()
    }

    private fun onHideUserRoad() {
        showMessage(getString(R.string.post_author_hide_success))
        act?.onBackPressed()
    }

    private fun renderFailures(failure: Failure) {
        when (failure) {
            is Failure.ServerError -> {
                showError(getString(R.string.error_try_later))
            }

            is Failure.NetworkConnection -> {
                showError(getString(R.string.error_try_later))
            }

            else -> {}
        }
    }

    /**
     * Events from viewModel
     * */
    private fun onAddPostCommentComplaint() {
        showMessage(getString(R.string.post_comment_complaint_added_successfully))
    }

    private fun onAddPostComplaint() {
        showMessage(getString(R.string.road_complaint_send_success))
    }

    private fun onUnsubscribePost() =
        showMessage(getString(R.string.turn_off_notifications))

    private fun onSubscribePost() =
        showMessage(getString(R.string.turn_on_notifications))

    private fun onErrorPostComment() {
        showError(getString(R.string.error_while_sending_comment))
        binding?.ivSendComment?.isEnabled = true
        binding?.etWriteComment?.isEnabled = true
    }

    override fun onPressRepostHeader(post: PostUIEntity, adapterPosition: Int) {
        post.parentPost?.let {
            goToParentPost(post)
        }
    }

    override fun onFollowUserClicked(post: PostUIEntity, adapterPosition: Int) {
        needAuth {
            val userId = post.user?.userId
            val isPostAuthor = post.user?.userId == viewModel.getUserUid()
            if (isPostAuthor) return@needAuth
            val isSubscribed = post.user?.subscriptionOn ?: return@needAuth
            if (isSubscribed.isTrue()) {
                showConfirmDialogUnsubscribeUser(
                    postId = postId,
                    userId = userId,
                    fromFollowButton = true,
                    postOriginEnum = postOrigin ?: return@needAuth,
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
            menu.addItem(R.string.reply_txt, R.drawable.ic_reply_purple_new) {
                viewModel.logCommentMenuAction(AmplitudePropertyCommentMenuAction.REPLY)
                commentId?.let { commentID ->
                    selectedComment?.id = commentID
                    selectedCommentID = commentID
                    Timber.d("Post comments: selectedcommentId = $selectedCommentID")
                }
                addInfoInInputMessageWidget()
            }
        }

        menu.addItem(R.string.text_copy_txt, R.drawable.ic_chat_copy_message) {
            viewModel.logCommentMenuAction(AmplitudePropertyCommentMenuAction.COPY)
            val clipboardManager =
                activity?.applicationContext
                    ?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager?
            val clipData = ClipData.newPlainText("text", postText)
            clipboardManager?.setPrimaryClip(clipData)
            showNToast(getString(R.string.comment_text_copied))
        }

        menu.addItem(R.string.comment_complain, R.drawable.ic_send_error) {
            //viewModel.complainComment(commentId)
            viewModel.logCommentMenuAction(AmplitudePropertyCommentMenuAction.REPORT)
            triggerAction(PostDetailsActions.AddComplaintPostComment(commentId))
        }

        menu.showWithTag(manager = parentFragmentManager, tag = COMMENT_MENU_TAG)

        currentBottomMenu = menu
    }

    private fun showNToast(string: String) {
        view?.let {
            NToast.with(it)
                .text(string)
                .typeSuccess()
                .show()
        }
    }

    private fun openRepostMenu(post: PostUIEntity, mode: SharingDialogMode = SharingDialogMode.DEFAULT) {
        if (isFragmentStarted.not()) return
        needAuth {
            SharePostBottomSheet(
                postOrigin = postOrigin,
                post = post.toPost(),
                event = post.event,
                mode = mode,
                callback = object : IOnSharePost {
                    override fun onShareFindGroup() {
                        act.goToGroups()
                    }

                    override fun onShareFindFriend() {
                        add(
                            SearchMainFragment(),
                            Act.LIGHT_STATUSBAR,
                            Arg(
                                IArgContainer.ARG_FIND_FRIENDS_OPENED_FROM_WHERE,
                                AmplitudeFindFriendsWhereProperty.SHARE
                            )
                        )
                    }

                    override fun onShareToGroupSuccess(groupName: String?) {
                        viewModel.repostSuccess(post)
                        NToast.with(act)
                            .durationLong()
                            .text(getString(R.string.success_repost_to_group, groupName ?: ""))
                            .typeSuccess()
                            .show()
                    }

                    override fun onShareToRoadSuccess() {
                        viewModel.repostSuccess(post)
                        showMessage(getString(R.string.success_repost_to_own_road))
                    }

                    override fun onShareToChatSuccess(repostTargetCount: Int) {
                        viewModel.repostSuccess(post, repostTargetCount)
                        val strResId = if (post.isEvent()) {
                            R.string.success_event_repost_to_chat
                        } else {
                            R.string.success_repost_to_chat
                        }
                        showMessage(getString(strResId))
                    }

                    override fun onPostItemUniqnameUserClick(userId: Long?) {
                        allowScreenshotPopupShowing()
                        act.addFragment(
                            UserInfoFragment(), COLOR_STATUSBAR_LIGHT_NAVBAR,
                            Arg(IArgContainer.ARG_USER_ID, userId)
                        )
                    }
                }).show(childFragmentManager)
        }
    }

    private fun meeraOpenRepostMenu(post: PostUIEntity, mode: SharingDialogMode = SharingDialogMode.DEFAULT) {
        if (isFragmentStarted.not()) return
        needAuth {
            MeeraShareSheet().show(
                fm = childFragmentManager,
                data = MeeraShareBottomSheetData(
                    postOrigin = postOrigin,
                    post = post.toPost(),
                    event = post.event,
                    mode = mode,
                    callback = object : IOnSharePost {
                        override fun onShareFindGroup() {
                            act.goToGroups()
                        }

                        override fun onShareFindFriend() {
                            add(
                                SearchMainFragment(),
                                Act.LIGHT_STATUSBAR,
                                Arg(
                                    IArgContainer.ARG_FIND_FRIENDS_OPENED_FROM_WHERE,
                                    AmplitudeFindFriendsWhereProperty.SHARE
                                )
                            )
                        }

                        override fun onShareToGroupSuccess(groupName: String?) {
                            viewModel.repostSuccess(post)
                            NToast.with(act)
                                .durationLong()
                                .text(getString(R.string.success_repost_to_group, groupName ?: ""))
                                .typeSuccess()
                                .show()
                        }

                        override fun onShareToRoadSuccess() {
                            viewModel.repostSuccess(post)
                            showMessage(getString(R.string.success_repost_to_own_road))
                        }

                        override fun onShareToChatSuccess(repostTargetCount: Int) {
                            viewModel.repostSuccess(post, repostTargetCount)
                            val strResId = if (post.isEvent()) {
                                R.string.success_event_repost_to_chat
                            } else {
                                R.string.success_repost_to_chat
                            }
                            showMessage(getString(strResId))
                        }

                        override fun onPostItemUniqnameUserClick(userId: Long?) {
                            allowScreenshotPopupShowing()
                            act.addFragment(
                                UserInfoFragment(), COLOR_STATUSBAR_LIGHT_NAVBAR,
                                Arg(IArgContainer.ARG_USER_ID, userId)
                            )
                        }
                    }

                )
            )

        }
    }

    private fun copyLink(link: String) {
        copyCommunityLink(context, link) {
            (requireActivity() as? ActivityToolsProvider)
                ?.getTooltipController()
                ?.showSuccessTooltip(R.string.copy_link_success)
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
            menu.addItem(R.string.reply_txt, R.drawable.ic_reply_purple_new) {
                viewModel.logCommentMenuAction(AmplitudePropertyCommentMenuAction.REPLY)
                commentId?.let { commentID ->
                    selectedCommentID = commentID
                    Timber.d("Post comments: selectedcommentId = $selectedCommentID")
                }
                addInfoInInputMessageWidget()
            }
        }

        menu.addItem(R.string.text_copy_txt, R.drawable.ic_chat_copy_message) {
            viewModel.logCommentMenuAction(AmplitudePropertyCommentMenuAction.COPY)
            val clipboardManager =
                activity?.applicationContext
                    ?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager?
            val clipData = ClipData.newPlainText("text", postText)
            clipboardManager?.setPrimaryClip(clipData)
            showNToast(getString(R.string.comment_text_copied))
        }

        menu.addItem(R.string.road_delete, R.drawable.ic_delete_menu_red) {
            if (commentId != null) {
                viewModel.logCommentMenuAction(AmplitudePropertyCommentMenuAction.DELETE)
                val whoDeleteComment = getWhoDeletedComment(isPostAuthor, isCommentAuthor) ?: return@addItem
                val originalComment = mCommentsAdapter.findCommentById(commentId) ?: return@addItem
                viewModel.markAsDeletePostComment(
                    originalComment = originalComment,
                    whoDeleteComment = whoDeleteComment
                )
            }
        }

        if (!isCommentAuthor) {
            menu.addItem(R.string.comment_complain, R.drawable.ic_send_error) {
                viewModel.logCommentMenuAction(AmplitudePropertyCommentMenuAction.REPORT)
                triggerAction(PostDetailsActions.AddComplaintPostComment(commentId))
            }

            if (!blockedUsersList.contains(commentAuthorId)) {
                menu.addItem(R.string.settings_privacy_block_user, R.drawable.ic_block_user_red) {
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
        val replacedCommentEntity = mCommentsAdapter.replaceCommentByDeletion(
            event.commentID,
            event.whoDeleteComment
        ) ?: return

        showDeleteCommentCountdownToastNew { isDialogForceClosed ->
            if (isDialogForceClosed) {
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
        mCommentsAdapter.restoreComment(event.originalComment)
    }

    //если удалили тот комментарий на который хотели ответить закрываем плашку
    private fun onDeleteCommentNew(event: PostViewEvent.DeleteComment) {
        if (selectedCommentID == event.commentID) {
            selectedCommentID = 0
            closeSendMessageExtraInfo()
        }
    }

    private fun showDeleteCommentCountdownToastNew(onClosedManually: (Boolean) -> Unit) {
        undoSnackbar?.dismissNoCallbacks()
        undoSnackbar = NSnackbar.with(view)
            .inView(view)
            .text(context.stringNullable(R.string.comment_deleted))
            .description(context.stringNullable(R.string.touch_to_delete))
            .durationIndefinite()
            .button(context.stringNullable(R.string.general_cancel))
            .dismissManualListener { onClosedManually(true) }
            .timer(DELAY_DELETE_COMMENT) { onClosedManually(false) }
            .show()
    }

    override fun onCommentReplyClick(comment: CommentEntityResponse) = needAuth {
        if (postItem?.user?.blackListedMe == true) return@needAuth
        selectedComment = comment
        selectedCommentID = 0

        selectedComment?.id?.let { commentID ->
            selectedCommentID = commentID
        }
        addInfoInInputMessageWidget()
    }

    override fun onCommentMention(userId: Long) {
        allowScreenshotPopupShowing()
        act.addFragment(
            UserInfoFragment(), COLOR_STATUSBAR_LIGHT_NAVBAR,
            Arg(IArgContainer.ARG_USER_ID, userId)
        )
    }

    override fun onHashtagClicked(hashtag: String?) = needAuth {
        act.addFragment(
            HashtagFragment(), Act.LIGHT_STATUSBAR,
            Arg(IArgContainer.ARG_HASHTAG, hashtag)
        )
    }

    override fun onShowEventOnMapClicked(post: PostUIEntity) = openEventOnMap(post)

    override fun onNavigateToEventClicked(post: PostUIEntity) {
        openEventNavigation(post)
        viewModel.logMapEventGetTherePress(post)
    }

    override fun onShowEventParticipantsClicked(post: PostUIEntity) = openEventParticipantsList(post)

    override fun onJoinAnimationFinished(post: PostUIEntity, adapterPosition: Int) {
        viewModel.onJoinAnimationFinished(post)
    }
    override fun onJoinEventClicked(post: PostUIEntity) {
        viewModel.joinEvent(post)
    }

    override fun onLeaveEventClicked(post: PostUIEntity) {
        viewModel.leaveEvent(post)
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
        val reactionSource = ReactionSource.PostComment(
            postId = postId,
            commentId = commentId,
            originEnum = postOrigin,
            postUserId = postItem?.getUserId(),
            commentUserId = commentUserId

        )

        act.getReactionBubbleViewController().showReactionBubble(
            reactionSource = reactionSource,
            showPoint = showPoint,
            viewsToHide = viewsToHide,
            reactionTip = reactionTip,
            currentReactionsList = currentReactionsList,
            contentActionBarType = ContentActionBar.ContentActionBarType.DEFAULT,
            isForceAdd = true,
            isMoveUpAnimationEnabled = isMoveUpAnimationEnabled,
            showMorningEvening = false,
            reactionsParams = postItem?.createAmplitudeReactionsParams(postOrigin),
            containerInfo = act.getDefaultReactionContainer()
        )
    }

    override fun onCommentClicked(post: PostUIEntity, adapterPosition: Int) =
        goToParentPost(post)

    override fun onShowMoreRepostClicked(post: PostUIEntity, adapterPosition: Int) =
        goToParentPost(post)

    override fun onShowMoreTextClicked(post: PostUIEntity, adapterPosition: Int, isOpenPostDetail: Boolean) {
        (parentFragment as? MapSnippetPage)?.setSnippetState(SnippetState.Expanded)
    }

    private fun goToParentPost(post: PostUIEntity) {
        val parentPost = post.parentPost ?: return
        if (parentPost.user?.blackListedMe == true) {
            showError(getString(R.string.parent_post_user_profile_denied_permisson_alert))
        } else {
            if (parentPost.deleted == 1) return
            val isVolumeEnabled = getVolumeState()
            add(
                PostFragmentV2(null),
                Act.LIGHT_STATUSBAR,
                Arg(IArgContainer.ARG_FEED_POST_ID, parentPost.postId),
                Arg(IArgContainer.ARG_FEED_POST_POSITION, 1),
                Arg(IArgContainer.ARG_DEFAULT_VOLUME_ENABLED, isVolumeEnabled),
                Arg(IArgContainer.ARG_POST_ORIGIN, postOrigin)
            )
        }
    }

    override fun onCommentLongClick(comment: CommentEntityResponse, position: Int) = needAuth {
        requireContext().lightVibrate()
        if (postItem?.user?.blackListedMe == true) return@needAuth
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

        viewModel.amplitudeComments.logOpenCommentOptionsMenu()
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
            act.addFragmentIgnoringAuthCheck(
                UserInfoFragment(), COLOR_STATUSBAR_LIGHT_NAVBAR,
                Arg(IArgContainer.ARG_USER_ID, post.user.userId),
                Arg(IArgContainer.ARG_TRANSIT_FROM, where.property)
            )
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
            act.openUserMoments(
                userId,
                fromView = fromView,
                viewedEarly = hasNewMoments?.not()
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

                is SpanDataClickType.ClickHashtag -> needAuth {
                    viewModel.logPressHashTag(post)
                    act.addFragment(
                        HashtagFragment(),
                        Act.LIGHT_STATUSBAR,
                        Arg(ARG_HASHTAG, clickType.hashtag)
                    )
                }

                is SpanDataClickType.ClickUserId -> {
                    allowScreenshotPopupShowing()
                    act.addFragment(
                        UserInfoFragment(), COLOR_STATUSBAR_LIGHT_NAVBAR,
                        Arg(IArgContainer.ARG_USER_ID, clickType.userId)
                    )
                }

                is SpanDataClickType.ClickLink -> {
                    act.openLink(clickType.link)
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

    override fun onReactionBottomSheetShow(post: PostUIEntity, adapterPosition: Int) = needAuth {
        if (viewModel.getFeatureTogglesContainer().detailedReactionsForPostFeatureToggle.isEnabled) {
            checkAppRedesigned(
                isRedesigned = {
                    MeeraReactionsStatisticsBottomSheetFragment.getInstance(
                        entityId = post.postId,
                        entityType = ReactionsEntityType.POST
                    ).show(childFragmentManager)
                },
                isNotRedesigned = {
                    ReactionsStatisticsBottomSheetFragment.getInstance(
                        entityId = post.postId,
                        entityType = ReactionsEntityType.POST
                    ).show(childFragmentManager)
                }
            )

        } else {
            val reactions = post.reactions ?: return@needAuth
            val sortedReactions = reactions.sortedByDescending { reactionEntity -> reactionEntity.count }
            val menu = ReactionsStatisticBottomMenu(context)
            menu.addTitle(R.string.reactions_on_post, sortedReactions.reactionCount())
            sortedReactions.forEachIndexed { index, value ->
                menu.addReaction(value, index != sortedReactions.size - 1)
            }
            menu.show(childFragmentManager)
        }

        val where = if (post?.isEvent().isTrue()) {
            AmplitudePropertyReactionWhere.MAP_EVENT
        } else {
            AmplitudePropertyReactionWhere.POST
        }

        viewModel.logStatisticReactionsTap(where)
    }

    override fun onReactionRegularClicked(
        post: PostUIEntity,
        adapterPosition: Int,
        reactionHolderViewId: ContentActionBar.ReactionHolderViewId,
        forceDefault: Boolean,
    ) {
        val reactionsParams = post.createAmplitudeReactionsParams(postOrigin)
        act.getReactionBubbleViewController().onSelectDefaultReaction(
            reactionSource = ReactionSource.Post(
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
        reactionHolderViewId: ContentActionBar.ReactionHolderViewId,
    ) {
        val reactionsParams = post.createAmplitudeReactionsParams(postOrigin)
        val params = post.toContentActionBarParams()
        act.getReactionBubbleViewController().showReactionBubble(
            reactionSource = post.createReactionSourcePost(reactionHolderViewId),
            showPoint = showPoint,
            viewsToHide = viewsToHide,
            reactionTip = reactionTip,
            currentReactionsList = post.reactions ?: emptyList(),
            contentActionBarType = ContentActionBar.ContentActionBarType.getType(params),
            reactionsParams = reactionsParams,
            containerInfo = act.getDefaultReactionContainer()
        )
    }

    override fun onCommunityClicked(communityId: Long, adapterPosition: Int) = needAuth {
        add(CommunityRoadFragment(), Act.LIGHT_STATUSBAR, Arg(ARG_GROUP_ID, communityId.toInt()))
    }

    override fun onCommentProfileClick(comment: CommentEntityResponse) {
        allowScreenshotPopupShowing()
        act.addFragmentIgnoringAuthCheck(
            UserInfoFragment(), COLOR_STATUSBAR_LIGHT_NAVBAR,
            Arg(IArgContainer.ARG_USER_ID, comment.uid)
        )
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

    private fun onPostItemDotsMenuClick(post: PostUIEntity, currentMedia: MediaAssetEntity?) = needAuth { wasLoginAuthorization ->
        triggerAction(PostDetailsActions.CheckUpdateAvailability(post, currentMedia))
    }
    private fun showMenu(
        post: PostUIEntity,
        isEditAvailable: Boolean,
        currentMedia: MediaAssetEntity?
    ) = needAuth { wasLoginAuthorization ->
        if (!optionsMenuEnabled || wasLoginAuthorization) {
            return@needAuth
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
                icon = R.drawable.ic_edit_purple_plain,
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
                R.drawable.ic_unsubscribe_post_menu_purple
            else R.drawable.ic_subscribe_post_menu_purple
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
                        icon = R.drawable.ic_subscribe_on_user_new,
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
                icon = R.drawable.ic_share_purple_new,
                bottomSeparatorVisible = true
            ) {
                triggerAction(PostDetailsActions.RepostClick(post))
            }
            menu.addItem(
                title = R.string.copy_link,
                icon = R.drawable.ic_chat_copy_message,
                bottomSeparatorVisible = true
            ) {
                triggerAction(PostDetailsActions.CopyPostLink(post.postId))
            }
        }

        if (!isPostAuthor) {
            if (post.user?.isSystemAdministrator == false && post.user.subscriptionOn.toBoolean().not()) {
                menu.addItem(
                    title = R.string.profile_complain_hide_all_posts,
                    icon = R.drawable.ic_eye_off_all_menu_item_red,
                    bottomSeparatorVisible = true
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
            menu.addItem(complainTitleResId, R.drawable.ic_report_profile) {
                viewModel.logPostMenuAction(
                    action = AmplitudePropertyMenuAction.POST_REPORT,
                    authorId = userId,
                )
                triggerAction(PostDetailsActions.AddComplaintPost(postId))
            }
        }

        // Удалить
        if (isPostAuthor) {
            menu.addItem(R.string.road_delete, R.drawable.ic_delete_menu_red) {
                viewModel.logPostMenuAction(
                    action = AmplitudePropertyMenuAction.DELETE,
                    authorId = userId,
                )
                triggerAction(PostDetailsActions.DeletePost(postId))
            }
        }

        menu.showWithTag(manager = childFragmentManager, tag = POST_MENU_TAG)
    }

    private fun addSavingMediaItemsToMenu(menu: MeeraMenuBottomSheet, postCreatorUid: Long?, currentMedia: MediaAssetEntity?, post: PostUIEntity) {
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
            icon = R.drawable.ic_download_new,
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
                    showMessage(getString(R.string.image_saved))
                }
            )
        }
    }

    private fun addVideoItemToMenu(menu: MeeraMenuBottomSheet, userId: Long?, postId: Long, mediaId: String? = null) {
        val savingVideoIsAvailable = (requireActivity().application as App).remoteConfigs.postVideoSaving
        if (!savingVideoIsAvailable) return

        menu.addItem(
            title = getString(R.string.save_to_device),
            icon = R.drawable.ic_download_new,
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
        act.showFireworkAnimation {}
    }

    override fun onStartPlayingVideoRequested() {
        if (isFragmentStarted.not() || isFragmentAdding) return
        startVideoIfExist()
    }

    override fun forceStartPlayingVideoRequested() {
        startVideoIfExist()
    }

    override fun onStopPlayingVideoRequested() {
        stopVideoIfExist()
    }

    override fun onMediaExpandCheckRequested() {
        val holder = binding?.rvPostsComments?.findViewHolderForAdapterPosition(0) as? MultimediaPostHolder
        holder?.showExpandMediaIndicator()
    }

    override fun onMultimediaPostSwiped(postId: Long, selectedMediaPosition: Int) {
        viewModel.updatePostSelectedMediaPosition(selectedMediaPosition)
    }

    override fun setVolumeState(volumeState: VolumeState) {
        viewModel.setVolumeState(volumeState)
    }

    override fun getVolumeState() = viewModel.getVolumeState()

    private fun goToContentViewer(post: PostUIEntity) {
        if (post.type == PostTypeEnum.AVATAR_HIDDEN || post.type == PostTypeEnum.AVATAR_VISIBLE) {
            checkAppRedesigned(
                isRedesigned = {
//                    add(
//                        MeeraProfilePhotoViewerFragment(), Act.COLOR_STATUSBAR_BLACK_NAVBAR,
//                        Arg(IArgContainer.ARG_IS_PROFILE_PHOTO, false),
//                        Arg(IArgContainer.ARG_IS_OWN_PROFILE, false),
//                        Arg(IArgContainer.ARG_POST_ID, post.postId),
//                        Arg(IArgContainer.ARG_GALLERY_ORIGIN, postOrigin)
//                    )
                },
                isNotRedesigned = {
                    add(
                        ProfilePhotoViewerFragment(), Act.COLOR_STATUSBAR_BLACK_NAVBAR,
                        Arg(IArgContainer.ARG_IS_PROFILE_PHOTO, false),
                        Arg(IArgContainer.ARG_IS_OWN_PROFILE, false),
                        Arg(IArgContainer.ARG_POST_ID, post.postId),
                        Arg(IArgContainer.ARG_GALLERY_ORIGIN, postOrigin)
                    )
                }
            )
        } else {
            add(
                ViewContentFragment(),
                Act.COLOR_STATUSBAR_BLACK_NAVBAR,
                Arg(ARG_VIEW_CONTENT_DATA, post),
                Arg(IArgContainer.ARG_PHOTO_WHERE, AmplitudePropertyWhere.POST_DETAIL),
                Arg(IArgContainer.ARG_POST_ORIGIN, postOrigin)

            )
        }
    }

    private fun allowScreenshotPopupShowing() {
        isScreenshotPopupShown = false
    }

    private fun goToVideoPostFragment(postItem: PostUIEntity) {
        val videoData = ViewVideoInitialData(
            position = videoHelper?.getCurrentPosition() ?: 0,
            duration = videoHelper?.getDuration() ?: 0
        )
        val isVolumeEnabled = getVolumeState()
        stopVideoIfExist()
        add(
            ViewVideoItemFragment(),
            Act.COLOR_STATUSBAR_BLACK_NAVBAR,
            Arg(ARG_VIEW_VIDEO_POST_ID, postItem.postId),
            Arg(ARG_VIEW_VIDEO_POST, postItem),
            Arg(ARG_VIEW_VIDEO_DATA, videoData),
            Arg(IArgContainer.ARG_DEFAULT_VOLUME_ENABLED, isVolumeEnabled),
            Arg(IArgContainer.ARG_POST_ORIGIN, postOrigin),
            Arg(ARG_NEED_TO_REPOST, !postItem.isPrivateGroupPost)
        )
    }

    private fun goToMultimediaPostViewFragment(postItem: PostUIEntity, mediaAsset: MediaAssetEntity) {
        val videoData = ViewVideoInitialData(
            id = mediaAsset.id,
            position = videoHelper?.getCurrentPosition() ?: 0,
            duration = videoHelper?.getDuration() ?: 0
        )
        stopVideoIfExist()
        add(
            ViewMultimediaFragment(),
            Act.COLOR_STATUSBAR_BLACK_NAVBAR,
            Arg(ARG_VIEW_MULTIMEDIA_POST_ID, postItem.postId),
            Arg(ARG_VIEW_MULTIMEDIA_ASSET_ID, mediaAsset.id),
            Arg(ARG_VIEW_MULTIMEDIA_ASSET_TYPE, mediaAsset.type),
            Arg(ARG_VIEW_MULTIMEDIA_DATA, postItem),
            Arg(ARG_VIEW_MULTIMEDIA_VIDEO_DATA, videoData),
            Arg(IArgContainer.ARG_POST_ORIGIN, postOrigin),
            Arg(ARG_NEED_TO_REPOST, !postItem.isPrivateGroupPost)
        )
    }

    private fun saveVideo(postId: Long, assetId: String?) {
        setPermissions(
            object : PermissionDelegate.Listener {
                override fun onGranted() {
                    viewModel.downloadPostVideo(postId, assetId)
                }

                override fun onDenied() {
                    NToast.with(act)
                        .text(act.getString(R.string.you_must_grant_permissions))
                        .durationLong()
                        .button(act.getString(R.string.general_retry)) {
                            viewModel.downloadPostVideo(postId, assetId)
                        }.show()
                }

                override fun onError(error: Throwable?) {
                    Timber.e("ERROR get Permissions: \$error")
                }
            },
            returnReadExternalStoragePermissionAfter33(),
            returnWriteExternalStoragePermissionAfter33(),
        )
    }

    private fun showConfirmDialogUnsubscribeUser(
        postId: Long?,
        userId: Long?,
        postOriginEnum: DestinationOriginEnum,
        fromFollowButton: Boolean,
        approved: Boolean,
        topContentMaker: Boolean,
    ) {
        ConfirmDialogBuilder()
            .setHeader(getString(R.string.user_info_unsub_dialog_header))
            .setDescription(getString(R.string.unsubscribe_desc))
            .setLeftBtnText(getString(R.string.general_cancel))
            .setRightBtnText(getString(R.string.unsubscribe))
            .setRightClickListener {
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

    private fun navigateToEditPost(post: PostUIEntity) {
        add(
            AddMultipleMediaPostFragment(),
            Act.LIGHT_STATUSBAR,
            Arg(ARG_GROUP_ID, post.groupId?.toInt()),
            Arg(IArgContainer.ARG_POST, post)
        )
    }

    override fun onDestroy() {
        postListCallback = null
        super.onDestroy()
        videoHelper?.releasePlayer()
        if (::mCommentsAdapter.isInitialized) {
            mCommentsAdapter.release()
        }
    }

    private fun startVideoIfExist() {
        var lastPostMediaViewInfo: PostMediaViewInfo? = null
        runCatching { lastPostMediaViewInfo = viewModel.getLastPostMediaViewInfo() }

        lastPostMediaViewInfo?.let { viewInfo ->
            val postId = viewInfo.postId ?: return@let
            val mediaPosition = viewInfo.viewedMediaPosition ?: return@let
            val postUpdate = UIPostUpdate.UpdateSelectedMediaPosition(postId, mediaPosition)
            adapter.updateItem(0, postUpdate)
        }

        binding?.rvPostsComments?.postDelayed({ videoHelper?.onStart(lastPostMediaViewInfo = lastPostMediaViewInfo) }, FEED_START_VIDEO_DELAY)
    }

    private fun stopVideoIfExist() = videoHelper?.onStop()

    override fun onStartFragment() {
        super.onStartFragment()
        onShowHints()
        viewModel.logScreenForFragment(isCalledFromGroup)
        if (isFirstTimeStarted) isFirstTimeStarted = false
        startVideoIfExist()
        resetLastPostMediaViewInfo()
        isOpened = true
        registerComplaintListener()
    }

    private fun resetLastPostMediaViewInfo() {
        triggerAction(PostDetailsActions.SaveLastPostMediaViewInfo(null))
    }

    private val priorityListener: () -> Unit = {
        videoHelper?.turnOffAudioOfVideo()
    }

    override fun onStart() {
        super.onStart()

        __audio.addAudioPriorityListener(priorityListener)
        videoHelper?.addVolumeSwitchListener(volumeSwitchListener)
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

            viewHolders.forEach { viewHolder -> when (viewHolder) { is BasePostHolder -> viewHolder.endZoom() } }
        }
    }

    override fun onStopFragment() {
        super.onStopFragment()
        stopVideoIfExist()
        undoSnackbar?.dismissNoCallbacks()
        __audio.stopPlaying(isLifecycleStop = true)
        resetAllZoomViews()
        unregisterComplaintListener()
    }

    override fun onStop() {
        super.onStop()
        stopVideoIfExist()
        __audio.stopPlaying(isLifecycleStop = true)
        __audio.removeAudioPriorityListener(priorityListener)
        videoHelper?.removeVolumeSwitchListener(volumeSwitchListener)
    }

    override fun onReturnTransitionFragment() {
        startVideoIfExist()
    }

    override fun onStartAnimationTransitionFragment() {
        stopVideoIfExist()
        __audio.stopPlaying(isLifecycleStop = true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isOpened = false
        Timber.e("--------> DESTROY View")
        mCommentsAdapter.unregisterAdapterDataObserver(adapterDataObserver)
    }

    override fun onAppHidden() {
        super.onAppHidden()
        adapter.forceUpdatePost()
    }

    companion object {
        var isOpened: Boolean = false
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPostv2Binding
        get() = FragmentPostv2Binding::inflate

}
