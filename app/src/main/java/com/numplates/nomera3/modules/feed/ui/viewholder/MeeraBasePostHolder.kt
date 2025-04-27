package com.numplates.nomera3.modules.feed.ui.viewholder

import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import android.net.Uri
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.glideClear
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.newSize
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setPaddingTop
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.vibrate
import com.meera.core.extensions.visible
import com.meera.core.utils.blur.BlurHelper
import com.meera.core.utils.listeners.DoubleOrOneClickListener
import com.meera.db.models.PostViewLocalData
import com.meera.db.models.message.ParsedUniquename
import com.meera.db.models.message.UniquenameSpanData
import com.meera.uikit.widgets.buttons.UiKitButton
import com.numplates.nomera3.ASPECT_16x9
import com.numplates.nomera3.App
import com.numplates.nomera3.LOTTIE_MELODY_ANIMATION
import com.numplates.nomera3.MAX_ASPECT
import com.numplates.nomera3.MIN_ASPECT
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.auth.util.needAuth
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.helper.AUDIO_FEED_HELPER_VIEW_TAG
import com.numplates.nomera3.modules.baseCore.helper.AudioEventListener
import com.numplates.nomera3.modules.baseCore.helper.AudioFeedHelper
import com.numplates.nomera3.modules.baseCore.helper.ViewHolderAudio
import com.numplates.nomera3.modules.comments.ui.entity.PostDetailsMode
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.feed.ui.MeeraPostCallback
import com.numplates.nomera3.modules.feed.ui.VideoZoomDelegate
import com.numplates.nomera3.modules.feed.ui.adapter.MediaLoadingState
import com.numplates.nomera3.modules.feed.ui.adapter.MeeraContentActionBar
import com.numplates.nomera3.modules.feed.ui.data.LoadingPostVideoInfoUIModel
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.entity.UIPostUpdate
import com.numplates.nomera3.modules.feed.ui.entity.UiMedia
import com.numplates.nomera3.modules.feed.ui.util.PostMediaDownloadControllerUtil
import com.numplates.nomera3.modules.feed.ui.util.divider.IDividedPost
import com.numplates.nomera3.modules.feed.ui.view.MeeraPostLoaderView
import com.numplates.nomera3.modules.feed.ui.view.MeeraPostMultimediaViewPager
import com.numplates.nomera3.modules.holidays.ui.processHolidayText
import com.numplates.nomera3.modules.maps.domain.events.model.EventStatus
import com.numplates.nomera3.modules.maps.ui.events.EventChipsView
import com.numplates.nomera3.modules.maps.ui.events.EventLabelView
import com.numplates.nomera3.modules.maps.ui.events.mapper.EventLabelUiMapper
import com.numplates.nomera3.modules.maps.ui.events.mapper.EventsCommonUiMapper
import com.numplates.nomera3.modules.maps.ui.events.model.EventChipsType
import com.numplates.nomera3.modules.maps.ui.events.model.EventChipsUiModel
import com.numplates.nomera3.modules.maps.ui.events.participants.view.model.EventParticipantsUiAction
import com.numplates.nomera3.modules.maps.ui.events.participants.view.model.EventParticipantsUiModel
import com.numplates.nomera3.modules.newroads.data.ISensitiveContentManager
import com.numplates.nomera3.modules.post_view_statistic.presentation.IPostViewDetectViewHolder
import com.numplates.nomera3.modules.posts.ui.model.PostHeaderEvent
import com.numplates.nomera3.modules.posts.ui.model.PostHeaderNavigationMode
import com.numplates.nomera3.modules.posts.ui.model.PostHeaderUiModel
import com.numplates.nomera3.modules.posts.ui.view.MeeraPostHeaderView
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingReaction
import com.numplates.nomera3.modules.reaction.ui.custom.ReactionBubble
import com.numplates.nomera3.modules.reaction.ui.mapper.toMeeraContentActionBarParams
import com.numplates.nomera3.modules.reaction.ui.util.getMyReaction
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.main.map.events.MeeraEventAddressView
import com.numplates.nomera3.modules.redesign.fragments.main.map.participant.MeeraEventParticipantsView
import com.numplates.nomera3.modules.tags.data.entity.SpanDataClickType
import com.numplates.nomera3.modules.tags.data.entity.TagOrigin
import com.numplates.nomera3.modules.uploadpost.ui.view.MeeraPostTextBackgroundView
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.modules.volume.presentation.VolumeStateCallback
import com.numplates.nomera3.presentation.utils.setTextNoSpans
import com.numplates.nomera3.presentation.utils.spanTagsTextInPosts
import com.numplates.nomera3.presentation.view.ui.MeeraTextViewWithImages
import com.numplates.nomera3.presentation.view.ui.customView.MediaPlayerListener
import com.numplates.nomera3.presentation.view.ui.customView.MusicPlayerCell
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.globalVisibleRect
import com.numplates.nomera3.presentation.view.utils.NTime.Companion.timeAgo
import com.numplates.nomera3.presentation.view.utils.zoomy.Zoomy
import com.numplates.nomera3.presentation.view.utils.zoomy.Zoomy.ZoomyProvider
import com.numplates.nomera3.presentation.view.widgets.DOUBLE_CLICK_TIME_DELAY
import com.numplates.nomera3.presentation.view.widgets.OnlyDoubleClickView
import com.numplates.nomera3.presentation.view.widgets.anim.AnimationEndListener
import kotlin.math.max
import kotlin.math.min

//TODO ROAD_FIX
//const val MAX_ASPECT_RATIO = 1.0
//const val MELODY_ANIM_SPEED = 1f
//const val EXPAND_MEDIA_INDICATOR_ANIM_DELAY_MS = 100L
const val TEXT_BACKGROUND_ASPECT_RATIO = 0.7

abstract class MeeraBasePostHolder(
    view: View,
    private val needToShowCommunityLabel: Boolean = true,
    private val isPostsWithBackgroundEnabled: Boolean = false
) : RecyclerView.ViewHolder(view), IDividedPost, ViewHolderAudio, IPostViewDetectViewHolder {

    protected var isRepost = false
    protected var postCallback: MeeraPostCallback? = null
    protected var zoomyProvider: ZoomyProvider? = null
    protected var volumeStateCallback: VolumeStateCallback? = null
    protected var contentManager: ISensitiveContentManager? = null
    protected var audioFeedHelper: AudioFeedHelper? = null
    protected var blurHelper: BlurHelper? = null
    private var doubleOrOneClickListener: DoubleOrOneClickListener? = null
    private var spanDataClickTextListener : ((SpanDataClickType)-> Unit)? = null
    private var spanDataClickTitleListener : ((SpanDataClickType)-> Unit)? = null

    protected val ivPicture: ImageView? = itemView.findViewById(R.id.ivPicture)
    protected var vTextBackground: MeeraPostTextBackgroundView? = itemView.findViewById(R.id.v_text_background)
    protected val tvText: MeeraTextViewWithImages? = itemView.findViewById(R.id.tvText)
    protected val tvEdited: TextView? = itemView.findViewById(R.id.tvEdited)
    protected val tvTitleText: MeeraTextViewWithImages? = itemView.findViewById(R.id.tv_item_post_title)
    protected val ivSensitiveContentIcon: ImageView? = itemView.findViewById(R.id.iv_stop)
    protected val ivBluredContent: ImageView? = itemView.findViewById(R.id.iv_blured_content)
    protected open val sensitiveContent: ConstraintLayout? = itemView.findViewById(R.id.sensetive_content)
    protected open val tvSensitiveContentHeader: TextView? = itemView.findViewById(R.id.tv_sensitive_content_header)
    protected open val tvSensitiveContentDesc: TextView? =
        itemView.findViewById(R.id.tv_extra_sensitive_content_description)
    protected open val uiKitButtonShowPost: UiKitButton? = itemView.findViewById(R.id.cv_show_post)
    protected val musicPlayerCell: MusicPlayerCell? = view.findViewById(R.id.mpc_media)
    private val lavMelodyAnimation: LottieAnimationView? = view.findViewById(R.id.lav_melody_anim)
    private val recognizedScreenShotContent: View? = view.findViewById(R.id.recognized_screenshot_content)
    private val ivBlurredMusicContent: ImageView? = view.findViewById(R.id.iv_blured_music_content)
    private val tvListenAppleMusic: TextView? = recognizedScreenShotContent?.findViewById(R.id.tv_listen_in_apple_music)
    private val mediaContainer: FrameLayout = itemView.findViewById(R.id.media_container)
    private val actionBar: MeeraContentActionBar? = view.findViewById(R.id.post_action_bar)
    private val loaderView: MeeraPostLoaderView? = itemView.findViewById(R.id.plv_post_loader)
    private val postUpdatingFl: FrameLayout? = itemView.findViewById(R.id.fl_post_updating)
    private val postUpdatingViewDim: View? = itemView.findViewById(R.id.view_post_updating_dim)
    private val postUpdatingLoaderView: MeeraPostLoaderView? =
        itemView.findViewById(R.id.plv_post_updating_loader)
    private val postHeaderView: MeeraPostHeaderView? = itemView.findViewById(R.id.phv_post_header)
    private val contentLayout: ConstraintLayout? = itemView.findViewById(R.id.vg_content)
    val doubleClickContainer: OnlyDoubleClickView? = itemView.findViewById(R.id.odcv_double_click_container)
    protected val eventLabelView: EventLabelView? = itemView.findViewById(R.id.elv_event)
    protected val tvEventStatus: TextView? = itemView.findViewById(R.id.tv_event_status)
    protected val ivMediaExpandView: ImageView? = itemView.findViewById(R.id.iv_media_expand)
    private val btnEventBack: UiKitButton? = itemView.findViewById(R.id.btn_event_post_header_back)
    protected val ivPostHeaderClose: ImageView? = itemView.findViewById(R.id.iv_event_post_header_close)
    protected val ivPostHeaderOptions: ImageView? = itemView.findViewById(R.id.iv_event_post_header_options)
    protected val eventParticipantsView: MeeraEventParticipantsView? =
        itemView.findViewById(R.id.epv_event_participants)
    protected val ecvEventChips: EventChipsView? = itemView.findViewById(R.id.ecv_item_post_event_chips)
    protected val ecvEventChipsImg: EventChipsView? = itemView.findViewById(R.id.ecv_item_post_event_chips_image)
    protected val eavEventAddress: MeeraEventAddressView? = itemView.findViewById(R.id.eav_distance_address)
    protected val multimediaPager: MeeraPostMultimediaViewPager? = itemView.findViewById(R.id.pmvp_multimedia_pager)
    protected var zoomInProcess = false

    private val loadingAnimationUtil: PostMediaDownloadControllerUtil? by lazy {
        if (loaderView != null) {
            PostMediaDownloadControllerUtil(loaderView) {
                stopMediaDownload()
            }
        } else {
            null
        }
    }
    val postUpdatingLoadingAnimationUtil: PostMediaDownloadControllerUtil? by lazy {
        if (postUpdatingLoaderView != null) {
            PostMediaDownloadControllerUtil(postUpdatingLoaderView) {
                stopMediaDownload()
            }
        } else {
            null
        }
    }
    protected val eventLabelUiMapper = EventLabelUiMapper(EventsCommonUiMapper(view.context))

    private val MEDIA_TEXT_MAX_LIMIT = 11
    private val MEDIA_TEXT_MIN_LIMIT = 5
    private val NO_MEDIA_TEXT_MAX_LIMIT = 23
    private val NO_MEDIA_TEXT_MIN_LIMIT = 11
    private val SNIPPET_TEXT_LIMIT = 3
    protected val MEDIA_MARGIN_HORIZONTAL = 16.dp
    protected val MEDIA_CORNER_RADIUS = 12.dp

    protected val SENSITIVE_CONTENT_ITEMS_SMALL_MARGIN = 8.dp
    protected val SENSITIVE_CONTENT_ITEMS_MEDIUM_MARGIN = 12.dp
    protected val SENSITIVE_CONTENT_ITEMS_BIG_MARGIN = 16.dp

    private val fadeInAnim by lazy { AnimationUtils.loadAnimation(view.context, R.anim.fade_in_scale_down) }
    private var audioEventListener : AudioEventListener? = null
    private var mediaPlayerListener: MediaPlayerListener? = null
    private var fadeAnimListener: AnimationListener? = null
    private var actionBarListener : MeeraContentActionBar.Listener? = null

    protected var zoomBuilder: Zoomy.Builder? = null
    protected var videoZoomDelegate: VideoZoomDelegate? = null

    fun endZoom() {
        zoomBuilder?.endZoom()
        videoZoomDelegate?.endZoom()
    }

    fun isPlayingMusic(): Boolean {
        return audioFeedHelper?.isPlaying() ?: false
    }

    fun pausePlayingMusic() {
        audioFeedHelper?.stopPlaying()
    }

    fun startPlayingMusic() {
        postUIEntity?.let { post ->
            val trackPreviewUrl = post.media?.trackPreviewUrl ?: return
            audioFeedHelper?.startPlaying(
                post.postId,
                trackPreviewUrl,
                audioEventListener,
                holderPosition = absoluteAdapterPosition,
                musicPlayerCell
            )
        }
    }

    fun changeStateSnippet(state: Int, postItem: PostUIEntity?) {
        if (state == BottomSheetBehavior.STATE_EXPANDED) {
            postItem?.let {
                postCallback?.onShowMoreTextClicked(
                    post = postItem,
                    adapterPosition = bindingAdapterPosition,
                    isOpenPostDetail = false,
                )
            }
        } else {
            postItem?.let {
                postCallback?.onHideMoreTextClicked(
                    post = postItem,
                    adapterPosition = bindingAdapterPosition,
                    isOpenPostDetail = false,
                )
            }
        }
    }

    fun getFeatureToggles(): FeatureTogglesContainer =
        (App.get() as FeatureTogglesContainer)

    fun initCallbacks(
        meeraPostCallback: MeeraPostCallback?,
        contentManager: ISensitiveContentManager?,
        audioFeedHelper: AudioFeedHelper?,
        blurHelper: BlurHelper?,
        zoomyProvider: ZoomyProvider?,
        volumeStateCallback: VolumeStateCallback?
    ) {
        this.contentManager = contentManager
        this.audioFeedHelper = audioFeedHelper
        this.blurHelper = blurHelper
        this.postCallback = meeraPostCallback
        this.zoomyProvider = zoomyProvider
        this.volumeStateCallback = volumeStateCallback
    }

    var headerNavigationMode: PostHeaderNavigationMode = PostHeaderNavigationMode.NONE
    var isPostDetails: Boolean = false
    var postDetailsMode: PostDetailsMode? = null
    var isInSnippet: Boolean = false
    var isEventsEnabled: Boolean = false
    var needToShowRepostBtn: Boolean = true
    protected var postUIEntity: PostUIEntity? = null
    protected var spanData: List<UniquenameSpanData>? = null

    private var containerDoubleClickListener : ((Point)->Unit)? = null

    override fun isVip(): Boolean = false

    val handler = android.os.Handler(Looper.getMainLooper())

    open fun bind(post: PostUIEntity) {
        this.postUIEntity = post
        initDoubleClickListener()
        initAudioEventListener()
        initLoadingAnimation(post.loadingInfo)
        initPostUpdatingLoadingAnimation(post.postUpdatingLoadingInfo)
        resetView()
        setupBackground()
        setupPostTitle(post)
        setupPostText(post)
        setupPostEdited(post)
        setupTextBackground(post)
        setupClickListeners(post)
        setupContent(post)
        setupZoom(post)
        setupSensitiveContentSize()
        setupBlur(post)
        setupActionBar(post)
        setupHeader(post)
        setupDoubleClick()
        hideExpandMediaIndicatorView()
    }

    open fun getInitialParentWidth() : Int = itemView.width

    private fun initAudioEventListener() {
        audioEventListener = object : AudioEventListener {
            override fun onPlay(withListener: Boolean) {
                musicPlayerCell?.startPlaying(withListener)
            }

            override fun onLoad(isDownload: Boolean) {
                if (isDownload) musicPlayerCell?.startDownloading()
                else musicPlayerCell?.stopDownloading()
            }

            override fun onPause(isReset: Boolean) {
                musicPlayerCell?.stopPlaying(false)
            }

            override fun onProgress(percent: Int) {
                musicPlayerCell?.setProgress(percent)
            }
        }
    }

    private fun initDoubleClickListener() {
        containerDoubleClickListener = { point: Point ->
            val post = postUIEntity
            if (post != null) {
                val imagePosition = IntArray(2)
                val textBackgroundPosition = IntArray(2)
                val videoPosition = IntArray(2)
                val musicPosition = IntArray(2)
                val textPosition = IntArray(2)
                val containerPosition = IntArray(2)
                val titlePosition = IntArray(2)
                val eventLabelPosition = IntArray(2)

                ivPicture?.getLocationInWindow(imagePosition)
                vTextBackground?.getLocationInWindow(textBackgroundPosition)
                mediaContainer.getLocationInWindow(videoPosition)
                musicPlayerCell?.getLocationInWindow(musicPosition)
                tvText?.getLocationInWindow(textPosition)
                doubleClickContainer?.getLocationInWindow(containerPosition)
                tvTitleText?.getLocationInWindow(titlePosition)
                eventLabelView?.getLocationInWindow(eventLabelPosition)
                val hasText = post.postText.isNotEmpty() || post.parentPost?.postText?.isNotEmpty() == true
                val hasTitle = post.event?.title.isNullOrEmpty().not()
                when {
                    hasText && isPointInContainer(tvText, point) -> sendLikeWithAnimation(
                        post = post, likeContainerResId = R.id.tvText
                    )

                    hasTitle && isPointInContainer(tvTitleText, point) -> sendLikeWithAnimation(
                        post = post, likeContainerResId = R.id.tv_item_post_title
                    )
                    isPointInContainer(eventLabelView, point) -> sendLikeWithAnimation(
                        post = post,
                        likeContainerResId = R.id.elv_event
                    )

                    isPointInContainer(musicPlayerCell, point) -> Unit
                    isPointInContainer(mediaContainer, point) -> sendLikeWithAnimation(
                        post = post, likeContainerResId = R.id.media_container
                    )

                    isPointInContainer(ivPicture, point) -> sendLikeWithAnimation(
                        post = post, likeContainerResId = R.id.ivPicture
                    )

                    isPointInContainer(vTextBackground, point) -> sendLikeWithAnimation(
                        post = post, likeContainerResId = R.id.v_text_background
                    )

                    else -> Unit
                }
            }
        }
    }

    private fun setupSensitiveContentSize() {
        val aspect = postUIEntity?.getSingleAspect() ?: return

        when {
            aspect >= MAX_ASPECT -> {
                tvSensitiveContentHeader?.setMargins(top = SENSITIVE_CONTENT_ITEMS_SMALL_MARGIN)
                uiKitButtonShowPost?.setMargins(top = SENSITIVE_CONTENT_ITEMS_SMALL_MARGIN)
                tvSensitiveContentDesc?.gone()
            }

            aspect >= ASPECT_16x9 && aspect < MAX_ASPECT -> {
                tvSensitiveContentHeader?.setMargins(top = SENSITIVE_CONTENT_ITEMS_MEDIUM_MARGIN)
                uiKitButtonShowPost?.setMargins(top = SENSITIVE_CONTENT_ITEMS_MEDIUM_MARGIN)
                tvSensitiveContentDesc?.gone()
            }

            else -> {
                tvSensitiveContentHeader?.setMargins(top = SENSITIVE_CONTENT_ITEMS_BIG_MARGIN)
                uiKitButtonShowPost?.setMargins(top = SENSITIVE_CONTENT_ITEMS_MEDIUM_MARGIN)
                tvSensitiveContentDesc?.visible()
            }
        }
    }

    fun isEditProgress() =
        postUIEntity?.postUpdatingLoadingInfo?.loadingState == MediaLoadingState.LOADING_NO_CANCEL_BUTTON

    fun getCurrentMedia() = multimediaPager?.getCurrentMedia()

    protected open fun resetView() {
        sensitiveContent?.gone()
    }

    protected fun setupTextBackground(post: PostUIEntity) {
        val isTextBackgroundVisible = isPostsWithBackgroundEnabled && post.isTextWithBackgroundPost()

        vTextBackground?.isVisible = isTextBackgroundVisible

        if (isTextBackgroundVisible) {
            setupInitialBackgroundSize(vTextBackground)
            itemView.post {
                vTextBackground?.bind(post)
                setupPostTextOnBackground(post)
            }
        }
    }

    private fun setupInitialBackgroundSize(vTextBackground: MeeraPostTextBackgroundView?) {
        val width = getInitialParentWidth() - getViewParentHorizontalMargins(vTextBackground)
        val height = (width * TEXT_BACKGROUND_ASPECT_RATIO).toInt()
        vTextBackground?.newSize(width, height)
    }

    protected fun clearImageView() {
        ivPicture?.glideClear()
        ivPicture?.setImageDrawable(null)
    }

    protected fun isNeedShowBlur(post: PostUIEntity): Boolean {
        val mediaUrl = post.getSingleAssetUrl()

        val isMarked = !(contentManager?.isMarkedAsNonSensitivePost(post.postId) ?: false)
        return post.isAdultContent == true && mediaUrl?.isEmpty() == false && isMarked
    }

    private fun stopMediaDownload() {
        val post = postUIEntity ?: return

        postCallback?.onStopLoadingClicked(post)
    }

    private fun setupBackground() {
        val contentBgColor = ContextCompat.getColor(
            itemView.context, if (isVip()) R.color.colorVipPostGoldBlack else R.color.ui_white
        )
        contentLayout?.setBackgroundColor(contentBgColor)
    }

    private fun setupHeader(post: PostUIEntity) {
        if (postDetailsMode != null) {
            postHeaderView?.gone()
        } else {
            val postHeaderUiModel = PostHeaderUiModel(
                post = post,
                navigationMode = headerNavigationMode,
                isOptionsAvailable = true,
                childPost = null,
                isCommunityHeaderEnabled = needToShowCommunityLabel,
                isLightNavigation = true,
                editInProgress = isEditProgress(),
                bigAvatar = true
            )
            postHeaderView?.bind(postHeaderUiModel)
            postHeaderView?.visible()
        }
    }

    private fun setupActionBar(post: PostUIEntity) {
        val needToShow = this.needToShowRepostBtn && !post.isPrivateGroupPost

        actionBarListener =  object : MeeraContentActionBar.Listener {
            override fun onReactionButtonDisabledClick() = Unit

            override fun onCommentsClick() {
                val post = postUIEntity ?: return
                if (isEditProgress()) return

                postCallback?.onPostClicked(post, bindingAdapterPosition)
            }

            override fun onRepostClick() {
                val post = postUIEntity ?: return
                if (isEditProgress()) return

                postCallback?.onRepostClicked(post)
            }

            override fun onReactionBadgeClick() {
                val post = postUIEntity ?: return
                if (isEditProgress()) return

                postCallback?.onReactionBottomSheetShow(post, bindingAdapterPosition)
            }

            override fun onReactionLongClick(
                showPoint: Point,
                reactionTip: TextView,
                viewsToHide: List<View>,
                reactionHolderViewId: MeeraContentActionBar.ReactionHolderViewId
            ) {
                val post = postUIEntity ?: return
                if (isEditProgress()) return

                postCallback?.onReactionLongClicked(post, showPoint, reactionTip, viewsToHide, reactionHolderViewId)
            }

            override fun onFlyingAnimationInitialized(flyingReaction: FlyingReaction) {
                postCallback?.onFlyingAnimationInitialized(flyingReaction)
            }

            override fun onReactionRegularClick(reactionHolderViewId: MeeraContentActionBar.ReactionHolderViewId) {
                val post = postUIEntity ?: return
                if (isEditProgress()) return

                postCallback?.onReactionRegularClicked(post, bindingAdapterPosition, reactionHolderViewId)
            }

            override fun onReactionClickToShowScreenAnimation(
                reactionEntity: ReactionEntity,
                anchorViewLocation: Pair<Int, Int>
            ) {
                postCallback?.onReactionClickToShowScreenAnimation(reactionEntity, anchorViewLocation)
            }
        }.also { actionBarListener ->
            actionBar?.init(
                post.toMeeraContentActionBarParams(),
                actionBarListener,
                needToShow
            )
        }


    }

    private fun setupPostTitle(post: PostUIEntity) {
        tvTitleText ?: return
        if (isEventsEnabled.not() || post.deleted.toBoolean()) {
            tvTitleText.gone()
            return
        }
        val event = post.event
        if (event != null) {
            val textColor = ContextCompat.getColor(itemView.context, R.color.ui_black)
            tvTitleText.setTextColor(textColor)
            spanDataClickTitleListener = { clickType ->
                if (clickType is SpanDataClickType.ClickBadWord) {
                    doubleClickContainer?.removeOnDoubleClickListener()
                }
                clickCheckBubble {
                    postCallback?.onTagClicked(
                        clickType = clickType,
                        adapterPosition = bindingAdapterPosition,
                        tagOrigin = TagOrigin.POST_TITLE,
                        post = postUIEntity
                    )
                }
            }
            spanDataClickTitleListener?.let { listener ->
                spanTagsTextInPosts(
                    context = itemView.context,
                    tvText = tvTitleText,
                    post = event.tagSpan,
                    linkColor = R.color.uiKitColorForegroundLink,
                    click = listener
                )
            }

            tvTitleText.processHolidayText(isVip()) {
                postCallback?.onHolidayWordClicked()
            }
            tvTitleText.visible()
        } else {
            tvTitleText.gone()
        }
    }

    protected fun setupEventTitleTopMargin(titleTextView: TextView, post: PostUIEntity) {
        val event = post.event ?: return
        val eventStatus = eventLabelUiMapper.mapEventStatus(event = event, isVip = isVip())
        val topMarginDp = when {
            eventStatus != null -> EVENT_TITLE_WITH_STATUS_TOP_MARGIN_DP
            post.containsMedia() -> EVENT_TITLE_NO_STATUS_WITH_IMAGE_TOP_MARGIN_DP
            else -> EVENT_TITLE_NO_STATUS_NO_IMAGE_TOP_MARGIN_DP
        }
        titleTextView.setMargins(top = topMarginDp.dp)
    }

    protected fun showExpandMediaIndicatorView(withAnimation: Boolean = false) {
        if (zoomInProcess) return
        runCatching {
            ivMediaExpandView?.visible()
            if (withAnimation) {
                resetMediaExpandScale()
                ivMediaExpandView?.animate()
                    ?.scaleY(MEDIA_EXPAND_SCALE_MAX)
                    ?.scaleX(MEDIA_EXPAND_SCALE_MAX)
                    ?.setDuration(EXPAND_MEDIA_INDICATOR_ANIM_DELAY_MS)
                    ?.start()
            }
        }
    }

    private fun resetMediaExpandScale() {
        ivMediaExpandView?.scaleX = MEDIA_EXPAND_SCALE_MIN
        ivMediaExpandView?.scaleY = MEDIA_EXPAND_SCALE_MIN
    }

    protected fun hideExpandMediaIndicatorView() {
        runCatching { ivMediaExpandView?.gone() }
    }

    protected fun isExpandMediaIndicatorViewVisible(): Boolean = ivMediaExpandView?.isVisible == true

    protected fun contentBarHeight(): Long = actionBar?.height?.toLong() ?: 0

    protected fun setupPostText(post: PostUIEntity) {
        if (isPostsWithBackgroundEnabled && post.isTextWithBackgroundPost()) {
            tvText?.gone()
            return
        }
        spanData = post.tagSpan?.spanData
        if (post.postText.isNotEmpty()) {
            post.tagSpan?.let { notNullSpan ->
                setupSpanText(notNullSpan)
            } ?: kotlin.run {
                tvText?.setTextNoSpans(post.postText)
            }
            tvText?.processHolidayText(isVip()) {
                postCallback?.onHolidayWordClicked()
            }
            val textColor = ContextCompat.getColor(itemView.context, R.color.ui_black)
            tvText?.setTextColor(textColor)
            val textColorLink = ContextCompat.getColor(itemView.context, R.color.uiKitColorForegroundLink)
            tvText?.setLinkTextColor(textColorLink)
            setTextMargin(post)
            tvText?.visible()
        } else {
            tvText?.gone()
        }

        if (post.isNotExpandedSnippetState) {
            doubleOrOneClickListener = object : DoubleOrOneClickListener() {
                override fun onClick() {
                    postCallback?.onPostSnippetExpandedStateRequested(post)
                }
            }
            tvText?.setOnClickListener(doubleOrOneClickListener)
            tvText?.movementMethod = ScrollingMovementMethod.getInstance()
            tvText?.linksClickable = false
        } else {
            tvText?.setOnClickListener(null)
            tvText?.linksClickable = true
        }
    }

    protected fun setupPostTextOnBackground(post: PostUIEntity) {
        val textView = vTextBackground?.getTextView()
        post.tagSpan?.let { notNullSpan ->
            setupSpanText(notNullSpan, post.isWhiteFont())
        } ?: kotlin.run {
            textView?.setTextNoSpans(post.postText)
        }
        textView?.processHolidayText(isVip()) {
            postCallback?.onHolidayWordClicked()
        }

        val textColorLink = ContextCompat.getColor(
            itemView.context,
            if (post.isWhiteFont()) {
                R.color.uiKitColorForegroundLightGreen
            } else {
                R.color.uiKitColorForegroundAddNavy
            }
        )
        textView?.setLinkTextColor(textColorLink)
    }

    private fun setupPostEdited(post: PostUIEntity) {
        val isEnabled =
            (itemView.context as? MeeraAct)?.featureTogglesContainer?.editPostFeatureToggle?.isEnabled ?: false
        val isHidden = post.editedAt == null || !post.isEditable()
        tvEdited?.isVisible = !isHidden && isEnabled
        if (!isHidden) {
            post.editedAt?.let {
                tvEdited?.text = timeAgo(post.editedAt, true)
            }
        }
    }

    private fun setupDoubleClick() {
        doubleClickContainer?.postDelayed({
            containerDoubleClickListener?.let { listener ->
                doubleClickContainer?.setOnDoubleClickListener(listener)
            }
        }, DOUBLE_CLICK_TIME_DELAY)
    }

    private fun isPointInContainer(
        view: View?, point: Point
    ): Boolean {
        doubleClickContainer?.let {
            if (view == null || view.isGone) return false
            val viewPosition = IntArray(2)
            val containerPosition = IntArray(2)
            view.getLocationInWindow(viewPosition)
            doubleClickContainer.getLocationInWindow(containerPosition)
            val viewY = viewPosition[1]
            val topY = viewY - containerPosition[1]
            val bottomY = viewY - containerPosition[1] + view.bottom
            return point.y in (topY + 1) until bottomY
        } ?: return false
    }

    private fun actionMore(post: PostUIEntity) {
        if (post.tagSpan?.showFullText == true) return
        val lineCount = post.tagSpan?.lineCount ?: 0
        when {
            isInSnippet -> {
                if (lineCount > SNIPPET_TEXT_LIMIT) {
                    actionPostDetail(post)
                }
            }

            post.containsMedia() -> {
                when {
                    lineCount in (MEDIA_TEXT_MIN_LIMIT + 1) until MEDIA_TEXT_MAX_LIMIT -> {
                        actionFullText(post)
                    }

                    lineCount >= MEDIA_TEXT_MAX_LIMIT -> {
                        actionPostDetail(post)
                    }
                }
            }

            else -> {
                when {
                    lineCount in (NO_MEDIA_TEXT_MIN_LIMIT + 1) until NO_MEDIA_TEXT_MAX_LIMIT -> {
                        actionFullText(post)
                    }

                    lineCount >= NO_MEDIA_TEXT_MAX_LIMIT -> {
                        actionPostDetail(post)
                    }
                }
            }
        }
    }

    private fun actionPostDetail(post: PostUIEntity) {
        postCallback?.onShowMoreTextClicked(
            post = post,
            adapterPosition = bindingAdapterPosition,
            isOpenPostDetail = true,
        )
    }

    private fun actionFullText(post: PostUIEntity) {
        postCallback?.onShowMoreTextClicked(
            post = post,
            adapterPosition = bindingAdapterPosition,
            isOpenPostDetail = false,
        )
    }

    protected fun setupEventAddressListener(post: PostUIEntity) {
        eavEventAddress?.setThrottledClickListener {
            postCallback?.onNavigateToEventClicked(post)
        }
    }

    protected fun expandAllText() {
        postUIEntity?.let { postUIEntity ->
            postCallback?.onShowMoreTextClicked(
                post = postUIEntity,
                adapterPosition = bindingAdapterPosition,
                isOpenPostDetail = false,
            )
        }
    }

    protected open fun setupClickListeners(post: PostUIEntity) {
        ivPicture?.clickCheckBubble {
            if (isNeedShowBlur(post)) return@clickCheckBubble
            clickPicture(post)
        }
        setupPostHeaderListener(post)
        setupEventParticipantsListener(post)
        setupEventAddressListener(post)
    }

    protected fun setupPostHeaderListener(post: PostUIEntity) {
        postHeaderView?.setEventListener { event ->
            when (event) {
                PostHeaderEvent.FollowClicked -> postCallback?.onFollowUserClicked(
                    post = post, adapterPosition = bindingAdapterPosition
                )

                PostHeaderEvent.OptionsClicked -> postCallback?.onDotsMenuClicked(
                    post = post, adapterPosition = bindingAdapterPosition, currentMedia = getCurrentMedia()
                )

                PostHeaderEvent.UserClicked -> postCallback?.onAvatarClicked(
                    post = post, adapterPosition = bindingAdapterPosition
                )


                is PostHeaderEvent.UserMomentsClicked -> {
                    postCallback?.onShowUserMomentsClicked(
                        userId = event.userId,
                        fromView = event.fromView,
                        hasNewMoments = event.hasNewMoments
                    )
                }

                is PostHeaderEvent.CommunityClicked -> postCallback?.onCommunityClicked(
                    communityId = event.communityId, adapterPosition = bindingAdapterPosition
                )

                else -> Unit
            }
        }
    }

    //TODO ROAD_FIX
    protected fun setupEventParticipantsListener(post: PostUIEntity) {
        ivPostHeaderOptions?.setThrottledClickListener {
            postCallback?.onDotsMenuClicked(
                post = post, adapterPosition = bindingAdapterPosition, currentMedia = getCurrentMedia()
            )
        }
        ivPostHeaderClose?.setThrottledClickListener {
            postCallback?.onPostCloseClicked()
        }
        eventParticipantsView?.setActionListener { uiAction ->
            if (isRepost) {
                postCallback?.onClickRepostEvent(post.parentPost?.postId ?: 0)
                return@setActionListener
            }

            when (uiAction) {
                EventParticipantsUiAction.JoinEvent -> {
                    (itemView.context as? MeeraAct)?.needAuth {
                        if (post.parentPost != null) {
                            postCallback?.onJoinEventClicked(
                                post = post,
                                isRepost = true,
                                adapterPosition = absoluteAdapterPosition
                            )
                        } else {
                            postCallback?.onJoinEventClicked(post)
                        }
                    }
                }

                EventParticipantsUiAction.LeaveEvent -> {
                    if (post.parentPost != null) {
                        postCallback?.onLeaveEventClicked(
                            post = post,
                            isRepost = true,
                            adapterPosition = absoluteAdapterPosition
                        )
                    } else {
                        postCallback?.onLeaveEventClicked(post)
                    }
                }

                EventParticipantsUiAction.NavigateToEvent -> {
                    if (post.parentPost != null) {
                        postCallback?.onNavigateToEventClicked(post.parentPost)
                    } else {
                        postCallback?.onNavigateToEventClicked(post)
                    }
                }

                EventParticipantsUiAction.ShowEventOnMap -> {
                    if (post.parentPost != null) {
                        postCallback?.onShowEventOnMapClicked(
                            post = post,
                            isRepost = true,
                            adapterPosition = absoluteAdapterPosition
                        )
                    } else {
                        postCallback?.onShowEventOnMapClicked(post)
                    }
                }

                EventParticipantsUiAction.ShowEventCreator -> {
                    (itemView.context as? MeeraAct)?.needAuth {
                        if (post.parentPost != null) {
                            postCallback?.onAvatarClicked(
                                post = post.parentPost,
                                adapterPosition = bindingAdapterPosition
                            )
                        } else {
                            postCallback?.onAvatarClicked(post = post, adapterPosition = bindingAdapterPosition)
                        }
                    }
                }

                EventParticipantsUiAction.ShowEventParticipants ->
                    (itemView.context as? MeeraAct)?.needAuth {
                        if (post.parentPost != null) {
                            postCallback?.onShowEventParticipantsClicked(post.parentPost)
                        } else {
                            postCallback?.onShowEventParticipantsClicked(post)
                        }
                    }

                EventParticipantsUiAction.HandleJoinAnimationFinished -> {
                    if (post.parentPost != null) {
                        postCallback?.onJoinAnimationFinished(
                            post = post.parentPost,
                            adapterPosition = bindingAdapterPosition
                        )
                    } else {
                        postCallback?.onJoinAnimationFinished(
                            post = post,
                            adapterPosition = bindingAdapterPosition
                        )
                    }
                }
            }
        }
    }

    fun updatePayload(payload: UIPostUpdate) {
        postUIEntity = postUIEntity?.updateModel(payload)?.also {
            setupClickListeners(it)
            if (this is MeeraImagePostHolder) setupZoom(it)
        }

        when (payload) {
            is UIPostUpdate.MeeraUpdateReaction -> {
                actionBar?.update(
                    params = postUIEntity?.toMeeraContentActionBarParams(),
                    reactionHolderViewId = payload.reactionUpdate.reactionSource.reactionHolderViewId
                )
            }

            is UIPostUpdate.UpdateLoadingState -> {
                loadingAnimationUtil?.setupLoading(
                    postId = payload.postId, loadingInfo = payload.loadingInfo
                )
            }

            is UIPostUpdate.UpdateUpdatingState -> {
                val state = payload.loadingInfo.loadingState
                val updatingState = state != MediaLoadingState.LOADING
                    && state != MediaLoadingState.LOADING_NO_CANCEL_BUTTON
                postUpdatingLoaderView?.isGone = !payload.loadingInfo.isShowLoadingProgress
                postUpdatingFl?.isGone = updatingState
                postUpdatingViewDim?.isGone = updatingState

//                if (payload.loadingInfo.isShowLoadingProgress) {
//                    postUpdatingLoadingAnimationUtil?.setupLoading(
//                        postId = payload.postId,
//                        loadingInfo = payload.loadingInfo
//                    )
//                }
            }

            is UIPostUpdate.UpdateEventPostParticipationState -> {
                val post = payload.postUIEntity
                val event = post.event ?: return
                val eventStatus = eventLabelUiMapper.mapEventStatus(event = event, isVip = isVip())
                val eventParticipantsUiModel = EventParticipantsUiModel(
                    hostAvatar = post.user?.avatarSmall,
                    participantsAvatars = event.participantAvatars,
                    participation = event.participation,
                    showMap = postDetailsOnMap().not(),
                    isCompact = false,
                    isVip = isVip(),
                    isFinished = eventStatus?.status == EventStatus.FINISHED
                )
                eventParticipantsView?.setModel(eventParticipantsUiModel)
            }

            is UIPostUpdate.UpdateUserMomentsState -> {
                postUIEntity?.let { setupHeader(it) }
            }

            is UIPostUpdate.UpdateVolumeState -> {
                updateVolume(payload.volumeState)
            }

            is UIPostUpdate.UpdateSelectedMediaPosition -> {
                updateMediaSelectedPosition(payload.selectedMediaPosition)
            }

            is UIPostUpdate.UpdateTagSpan -> {
                updateTagSpan(payload.post)
            }

            is UIPostUpdate.UpdateTimeAgo -> {
                updateTimeAgo()
            }

            else -> updateActionBarValues(payload)
        }
    }

    fun playFlyingReactions(postLatestReactionType: ReactionType?) {
        if (postLatestReactionType == null) return
        actionBar?.playFlyingReactions(postLatestReactionType)
    }

    abstract fun setupContent(post: PostUIEntity)

    abstract fun getAccountType(): AccountTypeEnum

    abstract fun setupZoom(post: PostUIEntity)

    abstract fun setupBlur(post: PostUIEntity)

    abstract fun updateVolume(volumeState: VolumeState)

    open fun clearResources() {
        endZoom()
        releaseMusicPlayer()
        clearImageView()
        ivPicture?.setOnClickListener(null)
        tvText?.clearResources()
        tvTitleText?.clearResources()
        eavEventAddress?.setOnClickListener(null)
        tvListenAppleMusic?.setOnClickListener(null)
        doubleClickContainer?.removeOnDoubleClickListener()
        vTextBackground?.clearResources()
        postHeaderView?.clearResources()
        actionBar?.clearResources()
        eventParticipantsView?.clearResources()
        loadingAnimationUtil?.clearResources()
        postUpdatingLoadingAnimationUtil?.clearResources()
        fadeAnimListener = null
        mediaPlayerListener = null
        actionBarListener = null
        audioEventListener = null
        containerDoubleClickListener = null
        doubleOrOneClickListener = null
        spanDataClickTextListener = null
        spanDataClickTitleListener = null
        zoomyProvider = null
        volumeStateCallback = null
        postCallback = null
        contentManager = null
        audioFeedHelper = null
        blurHelper = null
    }

    /* Метод необходим для обновления вью с предустановленной шириной,
    в случае сужения/расширения родителя, в котором отображается ViewHolder */
    open fun updateViewsWithPresetWidth() = Unit

    open fun clearVideoResource() = Unit

    private fun updateTimeAgo() {
        val post = this.postUIEntity ?: return
        postHeaderView?.updateTimeAgo(post)
    }

    private fun setupSpanText(tagSpan: ParsedUniquename, isWhiteText: Boolean = false) {
        val isTextWithBackgroundPost = isPostsWithBackgroundEnabled && postUIEntity?.isTextWithBackgroundPost().isTrue()
        val textView = if (isTextWithBackgroundPost) vTextBackground?.getTextView() else tvText
        val linkColor = if (isTextWithBackgroundPost) {
            if (isWhiteText) R.color.uiKitColorForegroundLightGreen else R.color.uiKitColorForegroundAddNavy
        } else {
            R.color.uiKitColorForegroundLink
        }

        textView?.let { tvText ->
            spanDataClickTextListener = { clickType ->
                if (clickType is SpanDataClickType.ClickMore) {
                    postUIEntity?.let(::actionMore)
                } else {
                    if (clickType is SpanDataClickType.ClickBadWord) {
                        doubleClickContainer?.removeOnDoubleClickListener()
                        setupDoubleClick()
                    }

                    postCallback?.onTagClicked(
                        clickType = clickType,
                        adapterPosition = bindingAdapterPosition,
                        tagOrigin = TagOrigin.POST_TEXT,
                        post = postUIEntity
                    )
                }
            }
            spanDataClickTextListener?.let { listener ->
                spanTagsTextInPosts(
                    context = itemView.context,
                    tvText = tvText,
                    post = tagSpan,
                    linkColor = linkColor,
                    font = if (isTextWithBackgroundPost) textView.typeface else null,
                    click = listener
                )
            }



        }
    }

    protected fun setupImageAspect(aspect: Double, parentWidth: Int): Int {
        var height = 0
        if (aspect > 0) {
            val newAspect = max(MIN_ASPECT, min(aspect, MAX_ASPECT))
            val layoutParams = ivPicture?.layoutParams
            layoutParams?.width = parentWidth
            height = (parentWidth / newAspect).toInt()
            layoutParams?.height = height
            ivPicture?.layoutParams = layoutParams
        }

        return height
    }

    protected fun setupMusicCell(post: PostUIEntity) {
        if (post.media?.trackId.isNullOrEmpty().not()) {
            itemView.tag = AUDIO_FEED_HELPER_VIEW_TAG
            musicPlayerCell?.visible()
            if (audioFeedHelper?.getCurrentAudio() == post.postId && audioFeedHelper?.isPlaying() == true) return
            lavMelodyAnimation?.setAnimation(LOTTIE_MELODY_ANIMATION)
            lavMelodyAnimation?.speed = MELODY_ANIM_SPEED
            lavMelodyAnimation?.repeatCount = LottieDrawable.INFINITE
            clearMusicPlayerState()
            musicPlayerCell?.setMediaInformation(post.media?.albumUrl, post.media?.artist, post.media?.track)
            audioEventListener?.let { audioEventListener ->
                audioFeedHelper?.addAudioEventListener(audioEventListener)
            }
            mediaPlayerListener = object : MediaPlayerListener {
                override fun onPlay(withListener: Boolean) {
                    showPlayingMusicAnimations(post.getSingleSmallImage(), post.getSingleAspect(), post.media)
                    if (withListener) {
                        val trackPreviewUrl = post.media?.trackPreviewUrl ?: return
                        audioFeedHelper?.startPlaying(
                            post.postId,
                            trackPreviewUrl,
                            audioEventListener,
                            holderPosition = absoluteAdapterPosition,
                            musicPlayerCell
                        )
                    }
                }

                override fun onStop(withListener: Boolean, isReset: Boolean) {
                    if (withListener) audioFeedHelper?.stopPlaying(needToLog = true)
                    lavMelodyAnimation?.cancelAnimation()
                    if (isReset) {
                        recognizedScreenShotContent?.gone()
                        if (post.getImageUrl() != null) ivPicture?.visible()
                        fadeInAnim.reset()
                    } else {
                        fadeAnimListener = object : AnimationEndListener() {
                            override fun onAnimationEnd(animation: Animation?) {
                                recognizedScreenShotContent?.gone()
                                if (post.getImageUrl() != null) ivPicture?.visible()
                                fadeInAnim.reset()
                            }
                        }.also { fadeAnimListener ->
                            fadeInAnim.setAnimationListener(fadeAnimListener)
                        }
                        recognizedScreenShotContent?.startAnimation(fadeInAnim)
                    }
                }

                override fun clickShare() {
                    goToAppleMusic(post.media?.trackUrl ?: return)
                }

                override fun onDoubleClick() {
                    sendLikeWithAnimation(
                        post = postUIEntity ?: post, likeContainerResId = R.id.mpc_media
                    )
                }
            }.also { mediaPlayerListener ->
                musicPlayerCell?.initMediaController(mediaPlayerListener)
            }

        } else {
            musicPlayerCell?.gone()
        }
    }

    protected fun getViewParentHorizontalMargins(view: View?): Int {
        val parent = view?.parent as? FrameLayout?
        val marginStart = parent?.marginStart ?: 0
        val marginEnd = parent?.marginEnd ?: 0
        return marginStart + marginEnd
    }

    override fun unSubscribe() {
        if (musicPlayerCell?.isPlaying() == false) {
            return
        }
        lavMelodyAnimation?.cancelAnimation()
        fadeInAnim.reset()
        ivPicture?.visible()
        audioFeedHelper?.stopPlaying(isReset = true)
        musicPlayerCell?.stopPlaying(isReset = true)
        audioEventListener?.let { audioEventListener ->
            audioFeedHelper?.removeAudioEventListener(audioEventListener)
        }
        clearMusicPlayerState()
    }

    override fun subscribe() {
        audioEventListener?.let { audioEventListener ->
            audioFeedHelper?.addAudioEventListener(audioEventListener)
        }
    }

    override fun getPostViewData(): PostViewLocalData {
        return PostViewLocalData(
            postId = postUIEntity?.postId ?: -1,
            postUserId = postUIEntity?.user?.userId ?: -1,
        )
    }

    override fun getViewAreaCollisionRect(): Rect {
        val mediaContainerRect = mediaContainer.globalVisibleRect
        val textRect = tvText?.globalVisibleRect
        val musicPlayer = musicPlayerCell?.globalVisibleRect
        return mediaContainerRect.merge(textRect).merge(musicPlayer)
    }

    private fun releaseMusicPlayer() {
        lavMelodyAnimation?.cancelAnimation()
        fadeInAnim.reset()
        audioEventListener?.let { audioEventListener ->
            audioFeedHelper?.removeAudioEventListener(audioEventListener)
            this.audioEventListener = null
        }
        musicPlayerCell?.clearResources()
    }

    private fun initLoadingAnimation(loadingInfo: LoadingPostVideoInfoUIModel) {
        val loadingAnimationUtil = loadingAnimationUtil ?: return

        loadingAnimationUtil.setupLoading(postUIEntity?.postId, loadingInfo)
    }

    private fun initPostUpdatingLoadingAnimation(loadingInfo: LoadingPostVideoInfoUIModel) {
        val state = loadingInfo.loadingState
        val updatingState = state != MediaLoadingState.LOADING
            && state != MediaLoadingState.LOADING_NO_CANCEL_BUTTON
        postUpdatingFl?.isGone = updatingState
        postUpdatingViewDim?.isGone = updatingState
        postUpdatingViewDim?.setOnClickListener { }
        if (loadingInfo.isShowLoadingProgress.not()) return
        val postUpdatingLoadingAnimationUtil = postUpdatingLoadingAnimationUtil ?: return
        postUpdatingLoadingAnimationUtil.setupLoading(postUIEntity?.postId, loadingInfo)
    }

    protected fun sendLikeWithAnimation(post: PostUIEntity, likeContainerResId: Int) {
        val act = itemView.context as? MeeraAct ?: return
        act.needAuth {
            contentManager?.let { cm->
                val selectedPost = post.parentPost ?: post
                val isMarked = !cm.isMarkedAsNonSensitivePost(selectedPost.postId)
                val isNSFW = selectedPost.isAdultContent() && isMarked
                if (!isNSFW) {
                    actionBar?.getReactionHolderViewId()?.let { reactionHolderViewId ->
                        val constraintSet = ConstraintSet()
                        constraintSet.clone(contentLayout)
                        constraintSet.connect(
                            R.id.lav_progress,
                            ConstraintSet.TOP,
                            likeContainerResId,
                            ConstraintSet.TOP
                        )
                        constraintSet.connect(
                            R.id.lav_progress,
                            ConstraintSet.BOTTOM,
                            likeContainerResId,
                            ConstraintSet.BOTTOM
                        )
                        constraintSet.applyTo(contentLayout)

                        val myReaction = post.reactions?.getMyReaction()
                        when {
                            myReaction == null -> {
                                postCallback?.onReactionRegularClicked(
                                    post = post,
                                    adapterPosition = bindingAdapterPosition,
                                    reactionHolderViewId = reactionHolderViewId
                                )
                            }

                            myReaction != ReactionType.GreenLight -> {
                                postCallback?.onReactionRegularClicked(
                                    post = post,
                                    adapterPosition = bindingAdapterPosition,
                                    reactionHolderViewId = reactionHolderViewId,
                                    forceDefault = true
                                )
                            }

                            else -> {
                                act.vibrate()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showPlayingMusicAnimations(postSmallImage: String?, aspectRatio: Double?, media: UiMedia?) {
        if (media?.recognized == true) {
            blurHelper?.blurByUrl(postSmallImage) {
                ivBlurredMusicContent?.loadGlide(it)
                if (aspectRatio ?: 0.0 < MAX_ASPECT_RATIO) {
                    lavMelodyAnimation?.visible()
                    lavMelodyAnimation?.playAnimation()
                } else {
                    lavMelodyAnimation?.gone()
                }

                fadeInAnim.setAnimationListener(object : AnimationEndListener() {
                    override fun onAnimationEnd(animation: Animation?) {
                        ivPicture?.invisible()
                        recognizedScreenShotContent?.visible()
                        fadeInAnim.reset()
                    }
                })

                ivPicture?.startAnimation(fadeInAnim)
                ivBlurredMusicContent?.setOnClickListener { }

                tvListenAppleMusic?.click {
                    goToAppleMusic(media.trackUrl ?: return@click)
                }
            }
        }
    }

    private fun goToAppleMusic(url: String) {
        musicPlayerCell?.stopPlaying()
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        itemView.context.startActivity(intent)
    }

    private fun clearMusicPlayerState() {
        musicPlayerCell?.stopPlaying(withListener = false, isReset = true)
        lavMelodyAnimation?.cancelAnimation()
        ivPicture?.visible()
        recognizedScreenShotContent?.gone()
    }

    // TODO(BR-19454) - Find a way to use static margins in the layout file without breaking the design,
    //  instead of setting them here programmatically
    private fun setTextMargin(post: PostUIEntity) {
        tvText ?: return
        when {
            isEventsEnabled && post.event != null -> tvText.setPaddingTop(EVENT_TEXT_TOP_MARGIN_DP.dp)
            post.containsAssets() -> tvText.setPaddingTop(
                tvText.resources.getDimension(R.dimen.margin_vertical_content_general).toInt()
            )

            else -> tvText.setPaddingTop(0)
        }
    }

    protected fun setupEvent(post: PostUIEntity) {
        if (tvEventStatus == null
            || eventParticipantsView == null
            || ecvEventChips == null
            || ecvEventChipsImg == null
            || eavEventAddress == null
        ) return
        if (isEventsEnabled.not()) {
            ivPostHeaderOptions?.gone()
            ivPostHeaderClose?.gone()
            btnEventBack?.gone()
            tvEventStatus.gone()
            eventParticipantsView.gone()
            ecvEventChips.gone()
            ecvEventChipsImg.gone()
            eavEventAddress.gone()
            return
        }
        val event = post.event
        if (event != null && !post.deleted.toBoolean()) {
            val eventStatus = eventLabelUiMapper.mapEventStatus(event = event, isVip = isVip())
            if (eventStatus != null) {
                val statusString = itemView.context.getString(eventStatus.statusTextResId)
                tvEventStatus.text = statusString
                tvEventStatus.setTextColor(itemView.context.getColor(eventStatus.textColorResId))
                tvEventStatus.visible()
            } else {
                tvEventStatus.gone()
            }
            val eventLabelUiModel = eventLabelUiMapper.mapEventLabelUiModel(
                eventUiModel = event,
                isVip = isVip()
            )
            ecvEventChips.gone()
            ecvEventChipsImg.visible()
            val eventChipsUiModel = EventChipsUiModel(
                type = EventChipsType.DARK,
                label = eventLabelUiModel
            )
            ecvEventChipsImg.setModel(eventChipsUiModel)
            val eventParticipantsUiModel = EventParticipantsUiModel(
                hostAvatar = post.user?.avatarSmall,
                participantsAvatars = event.participantAvatars,
                participation = event.participation,
                showMap = postDetailsOnMap().not(),
                isCompact = false,
                isVip = isVip(),
                isFinished = eventStatus?.status == EventStatus.FINISHED
            )
            if (eventLabelUiModel.distanceAddress != null) {
                eavEventAddress.setModel(eventLabelUiModel.distanceAddress) {
                    postCallback?.onNavigateToEventClicked(post)
                }
                eavEventAddress.visible()
            } else {
                eavEventAddress.gone()
            }
            eventParticipantsView.setModel(eventParticipantsUiModel)
            eventParticipantsView.visible()
            if (post.openedFromRoad) {
                btnEventBack?.visible()
                ivPostHeaderClose?.gone()
                btnEventBack?.setThrottledClickListener {
                    postCallback?.onBackFromEventClicked()
                }
                ivPostHeaderOptions?.setBackgroundResource(R.drawable.bg_snippet_button)
            } else {
                btnEventBack?.gone()
                ivPostHeaderClose?.visible()
                ivPostHeaderOptions?.setBackgroundResource(R.drawable.bg_snippet_button_left)
            }
            ivPostHeaderOptions?.visible()
        } else {
            ivPostHeaderOptions?.gone()
            ivPostHeaderClose?.gone()
            btnEventBack?.gone()
            tvEventStatus.gone()
            eventParticipantsView.gone()
            ecvEventChips.gone()
            ecvEventChipsImg.gone()
            eavEventAddress.gone()
        }
    }

    private fun onShowMoreTextClicked() {
        postUIEntity?.let { postUIEntity ->
            postCallback?.onShowMoreTextClicked(
                post = postUIEntity,
                adapterPosition = bindingAdapterPosition,
                isOpenPostDetail = true,
            )
        }
    }


    private fun View.clickCheckBubble(click: (View) -> Unit) {
        click { view ->
            if (isBubbleNotExist()) {
                click(view)
            }
        }
    }

    private fun postDetailsOnMap(): Boolean =
        postDetailsMode == PostDetailsMode.EVENT_SNIPPET || postDetailsMode == PostDetailsMode.EVENTS_LIST

    private fun clickCheckBubble(click: () -> Unit) {
        if (isBubbleNotExist()) {
            click()
        }
    }

    private fun isBubbleNotExist(): Boolean {
        val act = itemView.context as? MeeraAct ?: return false
        val bubble = (act.getRootView() as? ViewGroup)?.children?.find { it is ReactionBubble } as? ReactionBubble
        return bubble == null
    }

    private fun updateMediaSelectedPosition(position: Int) {
        multimediaPager?.setCurrentMediaPosition(position)
    }

    private fun updateTagSpan(post: PostUIEntity) {
        setupPostTitle(post)
        setupPostText(post)
        setupPostTextOnBackground(post)
    }

    private fun updateActionBarValues(uiPostUpdate: UIPostUpdate) {
        actionBar?.updateValues(
            repostCount = uiPostUpdate.repostCount,
            commentCount = uiPostUpdate.commentCount,
            reactions = uiPostUpdate.reactions
        )
    }

    private fun clickPicture(post: PostUIEntity) {
        post.getAvailableAsset()?.let { asset ->
            postCallback?.onMediaClicked(
                post = post,
                mediaAsset = asset,
                adapterPosition = absoluteAdapterPosition
            )
        }
    }

    companion object {
        private const val MEDIA_EXPAND_SCALE_MIN = 0f
        private const val MEDIA_EXPAND_SCALE_MAX = 1f
        private const val EVENT_TEXT_TOP_MARGIN_DP = 3
        private const val EVENT_TITLE_NO_STATUS_WITH_IMAGE_TOP_MARGIN_DP = 16
        private const val EVENT_TITLE_NO_STATUS_NO_IMAGE_TOP_MARGIN_DP = 11
        private const val EVENT_TITLE_WITH_STATUS_TOP_MARGIN_DP = 3
    }
}
