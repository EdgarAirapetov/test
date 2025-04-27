package com.numplates.nomera3.presentation.view.utils.sharedialog

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.meera.core.extensions.dp
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.pxToDp
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.simpleName
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.visible
import com.meera.core.keyboard.isKeyboardOpen
import com.meera.core.preferences.AppSettings
import com.meera.core.utils.KeyboardHeightProvider
import com.meera.uikit.bottomsheetdialog.LabelIconUiState
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogBehDelegate
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogState
import com.meera.uikit.widgets.tablayout.UiKitTwoLinesTabLayout
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.Post
import com.numplates.nomera3.databinding.MeeraShareBottomSheetContainerBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyPublicType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereSent
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AnalyticsPostShare
import com.numplates.nomera3.modules.baseCore.helper.amplitude.NO_USER_ID
import com.numplates.nomera3.modules.communities.data.entity.CommunityEntity
import com.numplates.nomera3.modules.communities.utils.shareLinkOutside
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.entity.toAmplitudePropertyWhere
import com.numplates.nomera3.modules.maps.ui.events.model.EventUiModel
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentItemUiModel
import com.numplates.nomera3.modules.share.ui.ShareItemsCallback
import com.numplates.nomera3.modules.share.ui.adapter.MeeraShareItemAdapter
import com.numplates.nomera3.modules.share.ui.entity.UIShareItem
import com.numplates.nomera3.modules.share.ui.entity.UIShareMessageEntity
import com.numplates.nomera3.modules.share.ui.model.SharingDialogMode
import com.numplates.nomera3.presentation.model.enums.WhoCanCommentPostEnum
import com.numplates.nomera3.presentation.utils.bottomsheet.BottomSheetCloseUtil
import com.numplates.nomera3.presentation.utils.bottomsheet.toAmplitudePropertyHow
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.view.utils.sharedialog.adapter.MeeraShareGroupAdapter
import com.numplates.nomera3.presentation.viewmodel.RepostViewModel
import com.numplates.nomera3.presentation.viewmodel.SharePostViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.SharePostViewEvent
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject

private const val KEYBOARD_HIDE_DELAY = 300L
private const val POST_SETTINGS_MENU = "POST_SETTINGS_MENU"
private const val LIMIT_SEARCH_GROUP = 50
private const val LIMIT_SEARCH_GROUP_PAGINATION = 20
private const val SELECT_MAX_USER = 10
private const val COUNT_BUTTON_MENU_NO_MY_POST = 4
private const val COUNT_BUTTON_MENU_MY_POST = 3
private const val COUNT_BUTTON_MENU_NO_MY_REPOST = 2
private const val HEADER_BOTTOM_SHEET_HEIGHT = 72
private const val FOOTER_FULL_HEIGHT = 136
private const val FOOTER_EDIT_TEXT_HEIGHT = 60
private const val FOOTER_OFFSET_IF_KEYBOARD_OPEN = 68
private const val FOOTER_OFFSET_IF_KEYBOARD_CLOSE = 36

@Deprecated("Must delete! Work with bugs")
class MeeraShareBottomSheet(
    private var groupId: Long = -1L,
    private val post: Post,
    private val event: EventUiModel?,
    private val callback: IOnSharePost,
    private val mode: SharingDialogMode = SharingDialogMode.DEFAULT,
    private val postOrigin: DestinationOriginEnum? = null,
): UiKitBottomSheetDialog<MeeraShareBottomSheetContainerBinding>() {
    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraShareBottomSheetContainerBinding
        get() = MeeraShareBottomSheetContainerBinding::inflate

    @Inject
    lateinit var appSettings: AppSettings
    private var shareType: ShareDialogType? = ShareDialogType.SharePost
    private var isSecondRepost = false
    private var shareDialogEvent: (event: ShareBottomSheetEvent) -> Unit = { }

    @Suppress("LocalVariableName")
    constructor(
        _shareType: ShareDialogType,
        postOrigin: DestinationOriginEnum? = null,
        event: (event: ShareBottomSheetEvent) -> Unit,
    ) : this(-1L, Post(), null, IOnSharePostStub(), postOrigin = postOrigin) {
        this.shareType = _shareType
        this.shareDialogEvent = event
        if (_shareType is ShareDialogType.ShareCommunity) {
            this.groupId = _shareType.groupId.toLong()
        }
    }

    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    private var keyboardHeightProvider: KeyboardHeightProvider? = null

    private var groupsAdapter: MeeraShareGroupAdapter? = null
    private var friendsAdapter: MeeraShareItemAdapter? = null
    private lateinit var viewModel: SharePostViewModel
    private var selectedCommunity: CommunityEntity? = null
    private var paginator: RecyclerViewPaginator? = null
    private var currentLayout = -1

    private var whoCanComment = WhoCanCommentPostEnum.EVERYONE
    private var messageData: UIShareMessageEntity? = null

    private var analyticsPostShare: AnalyticsPostShare? = null
    private var tabLayout: UiKitTwoLinesTabLayout? = null

    private var calculatedMarginIfHalfExpandedPx = 0
    private var calculatedMarginIfFullExpanded = 0f
    private var bottomSheetCurrentState = 0
    private var expandedKeyboardHeight = 0

    private val bottomSheetCloseUtil = BottomSheetCloseUtil(object : BottomSheetCloseUtil.Listener {
        override fun bottomSheetClosed(method: BottomSheetCloseUtil.BottomSheetCloseMethod) {
            viewModel.logPostShareClose(method.toAmplitudePropertyHow())
        }
    })

    private val repostViewModel by viewModels<RepostViewModel>()

    init {
        App.component.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SharePostViewModel::class.java)
    }

    override fun createDialogState(): UiKitBottomSheetDialogParams {
        if(mode != SharingDialogMode.SUGGEST_EVENT_SHARING) {
            return UiKitBottomSheetDialogParams(
                labelText = context?.getString(R.string.meera_post_settings),
                labelIconUiState = LabelIconUiState(
                    labelIcon = R.drawable.ic_outlined_arrow_left_m,
                    padding = 16.dp
                )
            )
        } else {
            return UiKitBottomSheetDialogParams(
                labelIconUiState = LabelIconUiState(
                    labelIcon = R.drawable.ic_outlined_arrow_left_m,
                    padding = 16.dp
                ),
            )
        }
    }

    override fun getBehaviorDelegate(): UiKitBottomSheetDialogBehDelegate {
        return UiKitBottomSheetDialogBehDelegate.Builder()
            .setBottomSheetState(UiKitBottomSheetDialogState.HALF_EXPANDED)
            .setDraggable(true)
            .setSkipCollapsed(true)
            .create(dialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetCloseUtil.reset()
        postOrigin?.let {
            val publicType = when {
                event != null -> AmplitudePropertyPublicType.MAP_EVENT
                post.hasPostVideo() -> AmplitudePropertyPublicType.VIDEO_POST
                else -> AmplitudePropertyPublicType.POST
            }
            analyticsPostShare = AnalyticsPostShare(
                postId = post.id,
                authorId = post.user?.userId ?: NO_USER_ID,
                where = it.toAmplitudePropertyWhere(),
                publicType = publicType
            )
        }

        view.post {
            contentBinding?.appbarShareSearch?.visible()
            when (mode) {
                SharingDialogMode.DEFAULT -> showSharing()
                SharingDialogMode.SUGGEST_EVENT_SHARING -> showEventSharingSuggestionLayout()
            }
        }

        if (savedInstanceState == null) {
            when (shareType) {
                is ShareDialogType.SharePost -> {
                    postOrigin?.let {
                        val publicType = when {
                            event != null -> AmplitudePropertyPublicType.MAP_EVENT
                            post.hasPostVideo() -> AmplitudePropertyPublicType.VIDEO_POST
                            else -> AmplitudePropertyPublicType.POST
                        }
                        viewModel.logPostShareOpen(
                            postId = post.id,
                            momentId = 0,
                            authorId = post.user?.userId ?: NO_USER_ID,
                            where = it.toAmplitudePropertyWhere(),
                            publicType = publicType
                        )
                    }
                }

                is ShareDialogType.ShareMoment -> {
                    val moment = (shareType as ShareDialogType.ShareMoment).moment
                    viewModel.logPostShareOpen(
                        postId = 0,
                        momentId = moment.id,
                        authorId = moment.userId,
                        where = AmplitudePropertyWhere.MOMENT,
                        publicType = AmplitudePropertyPublicType.MOMENT
                    )
                }

                else -> {
                    Timber.d("Empty state share dialog type")
                }
            }
        }
        isSecondRepost = post.parentPost != null

        setupBottomSheetCallback()
        setFooterBottomPosition()
    }

    private fun setFooterBottomPosition() {
        val screenHeight = getScreenHeight()
        rootBinding?.vgBottomSheetToolbar?.doOnLayout { view ->
            val halfScreen = screenHeight / 2
            val toolbarHeight = view.height
            val footerHeight = dpToPx(FOOTER_FULL_HEIGHT)
            val marginTop = halfScreen - (toolbarHeight + footerHeight)
            this.calculatedMarginIfHalfExpandedPx = marginTop
            contentBinding?.cvMenuContainer?.setMargins(top = pxToDp(marginTop).dp)
        }
    }

    private fun setupBottomSheetCallback() {
        if (mode == SharingDialogMode.SUGGEST_EVENT_SHARING) {
            getBehavior()?.state = BottomSheetBehavior.STATE_EXPANDED
        }
        getBehavior()?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(p0: View, newState: Int) {
                bottomSheetCurrentState = newState
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val bottomSheetVisibleHeight = bottomSheet.height - bottomSheet.top
                contentBinding?.cvMenuContainer?.let { pinnedBottom ->
                    val offsetY = (bottomSheetVisibleHeight - pinnedBottom.height).toFloat()
                    val keyboardOffset = if (requireActivity().isKeyboardOpen())
                        FOOTER_OFFSET_IF_KEYBOARD_OPEN else FOOTER_OFFSET_IF_KEYBOARD_CLOSE
                    pinnedBottom.y = offsetY - dpToPx(keyboardOffset)
                    calculatedMarginIfFullExpanded = offsetY - dpToPx(keyboardOffset)
                }
            }
        })
    }

    private fun setupSharingUi() {
        rootBinding?.ibBottomSheetDialogAction?.gone()
        contentBinding?.btnShareSend?.isEnabled = false
        shareDialogTypeConfig()
        showTabMenu()
        initPostLayout()
        initBottomMenu(tabLayout)
        dialog?.window?.decorView?.let { keyboardHeightProvider = KeyboardHeightProvider(it) }
        setKeyboardHeightObserver()
        setButtonListeners()
        initGroupsLiveData()
        initFriendsLiveData()
        initCheckedCountLive()
        initRepostLiveData()
        setSearchInputFocusListener()
        setSearchInputTextChangeListener()
        setSendRepostClickListener()
        if (mode == SharingDialogMode.SUGGEST_EVENT_SHARING) {
            selectChatTab()
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun showTabMenu(show: Boolean = true) {
        tabLayout?.isVisible = show
    }

    private fun shareDialogTypeConfig() {
        when (shareType) {
            is ShareDialogType.ShareProfile,
            is ShareDialogType.ShareCommunity,
            is ShareDialogType.ShareMoment -> {
                tabLayout = contentBinding?.vBottomMenuHideRoadAndGroupButtons
            }

            is ShareDialogType.SharePost -> {
                when {
                    isSecondRepost -> {
                        tabLayout = contentBinding?.vBottomMenuHideRoadAndGroupButtons
                        contentBinding?.vBottomMenu?.gone()
                    }
                    post.user?.userId == appSettings.readUID() -> {
                        tabLayout = contentBinding?.vBottomMenuHideRoad
                        contentBinding?.vBottomMenu?.gone()
                    }
                    else -> {
                        tabLayout = contentBinding?.vBottomMenu
                    }
                }
            }

            is ShareDialogType.MessageForwarding -> {
                tabLayout = null
            }

            else -> {
                tabLayout = contentBinding?.vBottomMenuHideMoreButton
            }
        }
    }

    private fun showEventSharingSuggestionLayout() {
        setupBottomSheetCallback()
        configureFriendsAdapter()
        rootBinding?.ibBottomSheetDialogAction?.gone()

        contentBinding?.apply {
            layoutEventShare.tvEventShareEnable.setThrottledClickListener {
                getBehavior()?.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                showSharing()
            }
            layoutEventShare.tvEventShareSkip.setThrottledClickListener {
                dismiss()
            }
            layoutEventShare.root.visible()
            cvMenuContainer.gone()
        }
    }

    private fun showSharing() {
        contentBinding?.apply {
            layoutEventShare.root.gone()
            cvMenuContainer.visible()
            setupSharingUi()
            appbarShareSearch.visible()
            cvMenuContainer.visible()
        }
    }

    private fun initRepostLiveData() {
        repostViewModel.liveEvent.observe(this, {
            handleViewEvent(it)
        })

        viewModel.sharePostLiveEvent.observe(viewLifecycleOwner) {
            handleViewEvent(it)
        }
    }

    private fun handleViewEvent(event: SharePostViewEvent) {
        when (event) {
            is SharePostViewEvent.onSuccessGroupRepost -> {
                callback.onShareToGroupSuccess(selectedCommunity?.name)
                dismiss()
            }

            is SharePostViewEvent.onSuccessRoadTypeRepost -> {
                callback.onShareToRoadSuccess()
                dismiss()
            }

            is SharePostViewEvent.onSuccessMessageRepost -> {
                callback.onShareToChatSuccess(event.repostTargetCount)
                dismiss()
            }

            is SharePostViewEvent.OnSuccessShareUserProfile -> {
                shareDialogEvent.invoke(ShareBottomSheetEvent.OnSuccessShareProfile)
                dismiss()
            }

            is SharePostViewEvent.OnSuccessShareMoment -> {
                if (event.momentItemUiModel != null) {
                    shareDialogEvent.invoke(
                        ShareBottomSheetEvent.OnSuccessShareMoment(event.momentItemUiModel)
                    )
                } else {
                    showCommonRepostError(getString(R.string.moment_share_error))
                }
                dismiss()
            }

            is SharePostViewEvent.OnSuccessShareCommunity -> {
                shareDialogEvent.invoke(ShareBottomSheetEvent.OnSuccessShareCommunity)
                dismiss()
            }

            is SharePostViewEvent.onErrorGroupRepost -> {
                contentBinding?.btnShareSend?.isEnabled = true
                showCommonRepostError(event.errorMessage)
            }

            is SharePostViewEvent.onErrorRoadTypeRepost -> {
                contentBinding?.btnShareSend?.isEnabled = true
                showCommonRepostError()
            }

            is SharePostViewEvent.OnErrorMessageRepost -> {
                contentBinding?.btnShareSend?.isEnabled = true
                showCommonRepostError(event.message)
            }

            is SharePostViewEvent.OnErrorShareUserProfile -> {
                showCommonRepostError(event.message)
            }

            is SharePostViewEvent.OnFailShareUserProfile -> {
                showCommonRepostError(getString(R.string.share_profile_error))
            }

            is SharePostViewEvent.OnFailShareMoment -> {
                showCommonRepostError(event.messageText ?: getString(event.message ?: R.string.moment_share_error))
            }

            is SharePostViewEvent.OnErrorShareCommunity -> {
                showCommonRepostError(event.message)
            }

            is SharePostViewEvent.OnFailShareCommunity -> {
                showCommonRepostError(getString(R.string.share_community_error))
            }

            is SharePostViewEvent.OnSuccessForwardChatMessage -> {
                lifecycleScope.launchWhenResumed {
                    context?.hideKeyboard(requireView())
                    delay(KEYBOARD_HIDE_DELAY)
                    shareDialogEvent.invoke(ShareBottomSheetEvent.OnSuccessForwardChatMessage(event.text))
                    dismiss()
                }
            }

            is SharePostViewEvent.OnErrorForwardChatMessage -> {
                shareDialogEvent.invoke(ShareBottomSheetEvent.OnErrorForwardChatMessage(event.message))
                dismiss()
            }

            is SharePostViewEvent.OnFailForwardChatMessage -> {
                shareDialogEvent.invoke(ShareBottomSheetEvent.OnFailForwardChatMessage)
                dismiss()
            }

            is SharePostViewEvent.onSuccessSharePostLink -> {
                callback.onOpenShareOutside()
                shareLinkOutside(context, event.postLink)
                dismiss()
            }

            is SharePostViewEvent.onErrorSharePostLink,
            is SharePostViewEvent.onErrorShareMomentLink -> {
                showCommonRepostError()
            }

            is SharePostViewEvent.onSuccessShareMomentLink -> {
                callback.onOpenShareOutside()
                shareLinkOutside(context, event.momentLink)
                dismiss()
            }

            is SharePostViewEvent.PlaceHolderShareEvent -> handlePlaceHolder(event.placeHolder)
            is SharePostViewEvent.BlockSendBtn -> setSendButtonDisabled()
            is SharePostViewEvent.UnBlockSendBtn -> setSendButtonEnabled()
        }
    }

    private fun handlePlaceHolder(placeHolder: SharePlaceHolderEnum) {
        when (placeHolder) {
            SharePlaceHolderEnum.EMPTY_SHARE_ITEMS -> showEmptyFriends()
            SharePlaceHolderEnum.ERROR_SEARCH,
            SharePlaceHolderEnum.ERROR_SHARE_ITEMS -> {
                Timber.d("Error share")
            }

            SharePlaceHolderEnum.OK -> hideEmptyFriends()
            else -> {
                Timber.d("Empty state share")
            }
        }
    }

    private fun showCommonRepostError(message: String? = null) {
        val act = activity as? Act
        act?.getRootView()?.let {
            val messageToShow = message ?: getString(R.string.error_while_processing_repost)
            NToast.with(act)
                .inView(dialog?.window?.decorView)
                .text(messageToShow)
                .typeError()
                .show()
        }
    }

    fun show(manager: FragmentManager?) {
        val fragment = manager?.findFragmentByTag(simpleName)
        if (fragment != null)
            return
        manager?.let {
            super.show(manager, simpleName)
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        bottomSheetCloseUtil.onCancel()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        bottomSheetCloseUtil.onDismiss()
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        keyboardHeightProvider?.release()
        shareDialogEvent.invoke(ShareBottomSheetEvent.OnDismissDialog)
    }

    private fun showCommentsMenu() {
        MeeraSharePosSettingsBottomSheet(
            currentLayout = currentLayout,
            currentWhoCanComment = whoCanComment,
            listener = ::postSettingsMenuListener
        ).show(parentFragmentManager, POST_SETTINGS_MENU)
    }

    private fun postSettingsMenuListener(action: WhoCanCommentPostEnum) {
        whoCanComment = action
    }

    /**
     * To use images in a text field, use this TextViewWithImages
     * @see com.numplates.nomera3.presentation.view.ui.TextViewWithImages
     */
    @SuppressLint("SetTextI18n")
    private fun initPostLayout() {

        contentBinding?.vPost?.setIconDescription(R.drawable.ic_outlined_repost_s)
        contentBinding?.vPost?.cellTitleVerified = post.user?.profileVerified.toBoolean()
        post.user?.name?.let {
            contentBinding?.vPost?.setTitleValue(it)
        }

        contentBinding?.vPost?.setLeftUserPicConfig(
            UserpicUiModel(
                userAvatarUrl = post.user?.avatarSmall
            )
        )
    }

    private fun showPostLayout() {
        setSendButtonEnabled()
        rootBinding?.ivBottomSheetDialogSettings?.visible()
        currentLayout = LAYOUT_REPOST_TO_ROADTAPE
    }

    private fun showGroupsLayout() {
        configureGroupAdapter()
        setSendButtonDisabled()
    }

    private fun showFriendsLayout() {
        configureFriendsAdapter()
        setSendButtonDisabled()
    }

    private fun showEmptyGroups() {
        contentBinding?.apply {
            vGroupEmptyState.visible()
            appbarShareSearch.gone()
            vShareInput.gone()
            btnShareSend.gone()
            vSearchGroupBtn.setThrottledClickListener {
                callback.onShareFindGroup()
                dismiss()
            }
        }
    }

    private fun hideEmptyGroups() {
        contentBinding?.vGroupEmptyState?.gone()
    }

    @SuppressLint("SetTextI18n")
    private fun showEmptyFriends() {
        contentBinding?.vEmptyState?.visible()
        contentBinding?.vShareInput?.gone()
        contentBinding?.rvSharePostList?.gone()
        contentBinding?.vSearchFriendsBtn?.setThrottledClickListener {
            callback.onShareFindFriend()
            shareDialogEvent.invoke(ShareBottomSheetEvent.OnClickFindFriendButton)
            dismiss()
        }
    }

    private fun hideEmptyFriends() {
        contentBinding?.vEmptyState?.gone()
    }

    private fun setKeyboardHeightObserver() {
        if (this.shareType is ShareDialogType.MessageForwarding) return
        keyboardHeightProvider?.observer = { keyboardHeight ->
            adjustRootView(keyboardHeight)
            handleFooterPositionDependsOnKeyboardHeight(keyboardHeight)
        }
    }

    private fun handleFooterPositionDependsOnKeyboardHeight(keyboardHeight: Int) {
        if (keyboardHeight > 0) {
            expandedKeyboardHeight = keyboardHeight
            handleFooterPositionIfKeyboardOpen()
        } else {
            handleFooterPositionIfKeyboardClose()
        }
    }

    private fun  handleFooterPositionIfKeyboardOpen() {
        if (bottomSheetCurrentState == BottomSheetBehavior.STATE_EXPANDED) {
            val screenHeight = getScreenHeight()
            contentBinding?.cvMenuContainer?.y =
                (screenHeight - expandedKeyboardHeight - dpToPx(HEADER_BOTTOM_SHEET_HEIGHT) - dpToPx(FOOTER_EDIT_TEXT_HEIGHT)).toFloat()
        } else {
            val newMargin = calculatedMarginIfHalfExpandedPx - dpToPx(FOOTER_EDIT_TEXT_HEIGHT)
            contentBinding?.cvMenuContainer?.setMargins(top = pxToDp(newMargin).dp)
        }
    }

    private fun  handleFooterPositionIfKeyboardClose() {
        if (bottomSheetCurrentState == BottomSheetBehavior.STATE_EXPANDED) {
            contentBinding?.cvMenuContainer?.y =
                (getScreenHeight() - dpToPx(HEADER_BOTTOM_SHEET_HEIGHT)
                    - dpToPx(FOOTER_FULL_HEIGHT) - dpToPx(FOOTER_OFFSET_IF_KEYBOARD_CLOSE)).toFloat()
        } else {
            contentBinding?.cvMenuContainer?.setMargins(top = pxToDp(calculatedMarginIfHalfExpandedPx).dp)
        }
    }

    private fun getScreenHeight(): Int {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    private fun adjustRootView(keyboardHeight: Int) {
        showTabMenu(keyboardHeight <= 0)
        shareDialogTypeConfig()
    }

    private fun setButtonListeners() {
        rootBinding?.ivBottomSheetDialogSettings?.setThrottledClickListener {
            val where = when (currentLayout) {
                LAYOUT_REPOST_TO_ROADTAPE -> {
                    AmplitudePropertyWhere.SELF_FEED
                }

                LAYOUT_REPOST_TO_GROUP -> {
                    AmplitudePropertyWhere.COMMUNITY
                }

                else -> {
                    AmplitudePropertyWhere.OTHER
                }
            }
            viewModel.logPostShareSettingsTap(where)
            showCommentsMenu()
        }
    }

    private fun shareMore() {
        val shareDialogType = shareType ?: return
        when (shareDialogType) {
            is ShareDialogType.SharePost -> repostViewModel.getPostLink(post.id)
            is ShareDialogType.ShareMoment -> repostViewModel.getMomentLink(shareDialogType.moment.id)
            else -> {
                shareDialogEvent.invoke(ShareBottomSheetEvent.OnMoreShareButtonClick)
                dismiss()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun selectChatTab() {
        showFriendsLayout()
        analyticsPostShare?.let {
            analyticsPostShare = it.copy(whereSent = AmplitudePropertyWhereSent.CHAT)
        }
    }

    private fun selectGroupTab() {
        showGroupsLayout()

        analyticsPostShare?.let {
            analyticsPostShare = it.copy(whereSent = AmplitudePropertyWhereSent.COMMUNITY)
        }
    }

    private fun setSendButtonEnabled() {
        contentBinding?.btnShareSend?.isEnabled = true
    }

    private fun setSendButtonDisabled() {
        contentBinding?.btnShareSend?.isEnabled = false
    }

    private fun configureGroupAdapter() {
        if (currentLayout == LAYOUT_REPOST_TO_GROUP) return
        friendsAdapter = null
        groupsAdapter = MeeraShareGroupAdapter(groupClick = ::initGroupClickListener)
        if (groupsAdapter?.hasObservers()?.not() == true)
            groupsAdapter?.setHasStableIds(true)

        contentBinding?.rvSharePostList?.adapter = groupsAdapter

        initGroupsPagination()
        viewModel.getMyGroups(isShowLoading = false, isShowError = false, startIndex = 0, limit = LIMIT_SEARCH_GROUP)
        currentLayout = LAYOUT_REPOST_TO_GROUP
    }

    private fun configureGroupSearchAdapter() {
        friendsAdapter = null
        groupsAdapter = MeeraShareGroupAdapter(groupClick = ::initGroupClickListener)
        if (groupsAdapter?.hasObservers()?.not() == true)
            groupsAdapter?.setHasStableIds(true)
        contentBinding?.rvSharePostList?.adapter = groupsAdapter

        initGroupsSearchPagination()

        viewModel.searchGroupRequest(
            contentBinding?.appbarShareSearch?.searchInputText.toString(),
            offset = 0,
            limit = LIMIT_SEARCH_GROUP
        )
        currentLayout = LAYOUT_REPOST_TO_GROUP_SEARCH
    }

    private fun initGroupClickListener(community: CommunityEntity) {
        selectedCommunity = community
        contentBinding?.rvSharePostList?.gone()
        contentBinding?.vPost?.visible()
        contentBinding?.btnShareSend?.isEnabled = true
        contentBinding?.appbarShareSearch?.gone()
        rootBinding?.ibBottomSheetDialogAction?.visible()
        rootBinding?.ibBottomSheetDialogAction?.setThrottledClickListener {
            contentBinding?.rvSharePostList?.visible()
            contentBinding?.vPost?.gone()
            contentBinding?.btnShareSend?.isEnabled = false
            contentBinding?.appbarShareSearch?.visibility
            rootBinding?.ibBottomSheetDialogAction?.gone()
        }
    }

    private fun initGroupsSearchPagination() {
        contentBinding?.rvSharePostList?.clearOnScrollListeners()
        contentBinding?.rvSharePostList?.let { rvSharePostList ->
            paginator = RecyclerViewPaginator(
                rvSharePostList,
                isLoading = {
                    viewModel.isLoadingGroupSearch()
                },
                onLast = {
                    viewModel.isLastGroupSearch()
                },
                loadMore = {
                    viewModel.searchGroupRequest(
                        contentBinding?.appbarShareSearch?.searchInputText.toString(), offset = groupsAdapter?.itemCount
                            ?: 0, limit = LIMIT_SEARCH_GROUP_PAGINATION
                    )
                }
            )
        }
    }

    private fun initGroupsPagination() {
        contentBinding?.rvSharePostList?.clearOnScrollListeners()
        contentBinding?.rvSharePostList?.let { rvSharePostList ->
            paginator = RecyclerViewPaginator(
                rvSharePostList,
                isLoading = {
                    viewModel.isLoadingGroups()
                },
                onLast = {
                    viewModel.isLastGroup()
                },
                loadMore = {
                    viewModel.getMyGroups(
                        false, isShowError = false, startIndex = groupsAdapter?.itemCount
                            ?: 0, limit = LIMIT_SEARCH_GROUP
                    )
                }
            )
        }
    }

    private fun initGroupsLiveData() {
        viewModel.liveMyGroups.observe(viewLifecycleOwner) {
            onSuccess { data ->
                if (data.isEmpty() && groupsAdapter?.itemCount == 0 && currentLayout == LAYOUT_REPOST_TO_GROUP)
                    showEmptyGroups()
                else {
                    groupsAdapter?.submitList(data)
                }
            }
            onProgress {}
            onError { _, _ -> }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun configureFriendsAdapter() {
        if (currentLayout == LAYOUT_REPOST_TO_CHAT) return
        viewModel.setShareItems()
        groupsAdapter = null
        friendsAdapter = MeeraShareItemAdapter(shareItemsCallback)
        contentBinding?.rvSharePostList?.layoutManager = LinearLayoutManager(context)
        contentBinding?.rvSharePostList?.adapter = friendsAdapter
        initFriendsPagination()
        viewModel.getShareItems(shareType)
        currentLayout = LAYOUT_REPOST_TO_CHAT
    }

    private val shareItemsCallback = object : ShareItemsCallback {
        override fun onChecked(item: UIShareItem, isChecked: Boolean) {
            viewModel.itemSearchChecked(item, isChecked)
        }

        override fun canBeChecked(): Boolean {
            return viewModel.canBeChecked()
        }
    }

    private fun configureFriendsSearchAdapter() {
        viewModel.clearShareItemsForSearch()
        friendsAdapter = null
        friendsAdapter = MeeraShareItemAdapter(shareItemsCallback)
        contentBinding?.rvSharePostList?.layoutManager = LinearLayoutManager(context)
        contentBinding?.rvSharePostList?.adapter = friendsAdapter
        initFriendsSearchPagination()
        viewModel.searchShareItems(contentBinding?.appbarShareSearch?.searchInputText.toString())
        currentLayout = LAYOUT_REPOST_TO_CHAT_SEARCH
    }

    private fun initFriendsSearchPagination() {
        contentBinding?.rvSharePostList?.clearOnScrollListeners()
        contentBinding?.rvSharePostList?.let { rvSharePostList ->
            paginator = RecyclerViewPaginator(
                rvSharePostList,
                isLoading = {
                    viewModel.isLoadingUserSearch()
                },
                onLast = {
                    viewModel.isLastUserSearch()
                },
                loadMore = {}
            )
        }
    }

    private fun initFriendsPagination() {
        contentBinding?.rvSharePostList?.clearOnScrollListeners()
        contentBinding?.rvSharePostList?.let { rvSharePostList ->
            paginator = RecyclerViewPaginator(
                rvSharePostList,
                isLoading = {
                    viewModel.isLoadingFriends()
                },
                onLast = {
                    viewModel.isLastFriend()
                },
                loadMore = {}
            )
        }
    }

    private fun initFriendsLiveData() {
        configureFriendsAdapter()
        viewModel.liveShareItems.observe(viewLifecycleOwner) {
            friendsAdapter?.submitList(it)
            setTitleDialog(0)
        }
    }

    private fun initCheckedCountLive() {
        viewModel.liveCheckedCount.observe(viewLifecycleOwner) {
            setTitleDialog(it)
            if (it > 0) setSendButtonEnabled()
            else setSendButtonDisabled()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setTitleDialog(countCheckUser: Int) {
        rootBinding?.tvBottomSheetDialogLabel?.visible()
        rootBinding?.tvBottomSheetDialogLabel?.text = getString(R.string.menu_chat_forward_message)
        rootBinding?.tvBottomSheetDialogLabelExtra?.text = "$countCheckUser/$SELECT_MAX_USER"
    }

    private fun initBottomMenu(tab: UiKitTwoLinesTabLayout?) {
        tab?.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    hideEmptyGroups()
                    hideEmptyFriends()
                    when(tabLayout?.tabCount){
                        COUNT_BUTTON_MENU_NO_MY_POST -> initSelectedMenuAllPost(tab?.position)
                        COUNT_BUTTON_MENU_MY_POST -> initSelectedMenuMyPost(tab?.position)
                        COUNT_BUTTON_MENU_NO_MY_REPOST -> initSelectedMenuNoMyRepost(tab?.position)
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    when(tabLayout?.tabCount){
                        COUNT_BUTTON_MENU_NO_MY_POST -> initUnselectedMenuAllPost(tab?.position)
                        COUNT_BUTTON_MENU_MY_POST -> initUnselectedMenuMyPost(tab?.position)
                        COUNT_BUTTON_MENU_NO_MY_REPOST -> initUnselectedMenuNoMyRepost(tab?.position)
                    }
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                    Timber.d("Unused function")
                }
            }
        )
    }

    private fun initSelectedMenuMyPost(position: Int?){
        when (position) {
            CHAT_ID_MY_POST_MENU -> {
                initBottomChatMenu()
            }

            GROUP_ID_MY_POST_MENU -> {
                initBottomGroupMenu()
            }

            MORE_ID_MY_POST_MENU -> {
                shareMore()
            }
        }
    }

    private fun initUnselectedMenuMyPost(position: Int?){
        when (position) {
            CHAT_ID_MY_POST_MENU -> {
                contentBinding?.rvSharePostList?.gone()
            }

            GROUP_ID_MY_POST_MENU -> {
                contentBinding?.rvSharePostList?.gone()
            }

            MORE_ID_MY_POST_MENU -> {
                Timber.d("No action state")
            }
        }
    }

    private fun initSelectedMenuNoMyRepost(position: Int?){
        when (position) {
            CHAT_ID_NO_MY_REPOST_MENU -> {
                initBottomChatMenu()
            }

            MORE_ID_NO_MY_REPOST_MENU -> {
                shareMore()
            }
        }
    }

    private fun initUnselectedMenuNoMyRepost(position: Int?){
        when (position) {
            CHAT_ID_NO_MY_REPOST_MENU -> {
                contentBinding?.rvSharePostList?.gone()
            }

            MORE_ID_NO_MY_REPOST_MENU -> {
                Timber.d("No action state")
            }
        }
    }

    private fun initSelectedMenuAllPost(position: Int?){
        when (position) {
            CHAT_ID_NO_MY_POST_MENU -> {
                initBottomChatMenu()
            }

            MY_FEED_IMAGE_POST_ID_NO_MY_POST_MENU -> {
                initBottomFeedMenu()
            }

            GROUP_ID_NO_MY_POST_MENU -> {
                initBottomGroupMenu()
            }

            MORE_ID_NO_MY_POST_MENU -> {
                shareMore()
            }
        }
    }

    private fun initUnselectedMenuAllPost(position: Int?){
        when (position) {
            CHAT_ID_NO_MY_POST_MENU -> {
                contentBinding?.rvSharePostList?.gone()
            }

            MY_FEED_IMAGE_POST_ID_NO_MY_POST_MENU -> {
                contentBinding?.vPost?.gone()
            }

            GROUP_ID_NO_MY_POST_MENU -> {
                contentBinding?.rvSharePostList?.gone()
            }

            MORE_ID_NO_MY_POST_MENU -> {
                Timber.d("No action state")
            }
        }
    }

    private fun initBottomChatMenu(){
        contentBinding?.rvSharePostList?.visible()
        contentBinding?.appbarShareSearch?.visible()
        rootBinding?.ivBottomSheetDialogSettings?.gone()
        rootBinding?.tvBottomSheetDialogLabelExtra?.visible()
        selectChatTab()
    }

    private fun initBottomGroupMenu(){
        contentBinding?.rvSharePostList?.visible()
        contentBinding?.appbarShareSearch?.visible()
        selectGroupTab()
        rootBinding?.tvBottomSheetDialogLabel?.text = getString(R.string.general_share)
        rootBinding?.tvBottomSheetDialogLabelExtra?.gone()
        rootBinding?.ivBottomSheetDialogSettings?.visible()
    }

    private fun initBottomFeedMenu(){
        showPostLayout()
        contentBinding?.vPost?.visible()
        contentBinding?.appbarShareSearch?.gone()
        rootBinding?.tvBottomSheetDialogLabelExtra?.gone()
        rootBinding?.tvBottomSheetDialogLabel?.text = getString(R.string.general_share)
    }

    private fun setSearchInputFocusListener() {

        contentBinding?.appbarShareSearch?.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        contentBinding?.appbarShareSearch?.setThrottledClickListener {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }


    private fun setSearchInputTextChangeListener() {
        contentBinding?.appbarShareSearch?.doAfterSearchTextChanged { text ->
            analyticsPostShare?.let {
                if (!text.isNullOrEmpty()) {
                    analyticsPostShare = it.copy(search = true)
                }
            }

            if (bottomSheetBehavior?.state != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
            }
            when (currentLayout) {
                LAYOUT_REPOST_TO_CHAT, LAYOUT_REPOST_TO_CHAT_SEARCH -> {
                    if (!text.isNullOrEmpty()) {
                        configureFriendsSearchAdapter()
                    } else {
                        configureFriendsAdapter()
                    }
                }

                LAYOUT_REPOST_TO_GROUP, LAYOUT_REPOST_TO_GROUP_SEARCH -> {
                    if (!text.isNullOrEmpty()) {
                        configureGroupSearchAdapter()
                    } else {
                        configureGroupAdapter()
                    }
                }
            }
        }
    }


    private fun setSendRepostClickListener() {
        contentBinding?.btnShareSend?.setOnClickListener {
            when (shareType) {
                is ShareDialogType.ShareMoment -> {
                    val moment = (shareType as ShareDialogType.ShareMoment).moment
                    viewModel.logPostShare(
                        analyticsPostShare = AnalyticsPostShare(
                            momentId = moment.id,
                            authorId = moment.userId,
                            whereSent = AmplitudePropertyWhereSent.OUTSIDE,
                            where = AmplitudePropertyWhere.MOMENT,
                            publicType = AmplitudePropertyPublicType.MOMENT,
                            textAdded = contentBinding?.vShareInput?.etInput?.text?.trim().isNullOrEmpty()
                        )
                    )
                }

                else -> {
                    analyticsPostShare?.let {
                        val textAdded = contentBinding?.vShareInput?.etInput?.text?.trim().isNullOrEmpty()
                        viewModel.logPostShare(
                            analyticsPostShare = it.copy(textAdded = !textAdded)
                        )
                    }
                }
            }
            sendButtonConfig()
        }
    }

    private fun sendButtonConfig() {
        when (shareType) {
            is ShareDialogType.SharePost -> sendButtonSharePostConfig()
            is ShareDialogType.ShareProfile ->
                sendButtonShareProfileConfig((shareType as ShareDialogType.ShareProfile).userId)

            is ShareDialogType.ShareMoment ->
                sendButtonShareMomentConfig((shareType as ShareDialogType.ShareMoment).moment)

            is ShareDialogType.ShareCommunity -> {
                val typeCommunity = shareType as ShareDialogType.ShareCommunity
                sendButtonShareCommunityConfig(typeCommunity.groupId)
            }

            is ShareDialogType.MessageForwarding -> {
                val type = (shareType as ShareDialogType.MessageForwarding)
                messageData = type.data
                sendButtonForwardChatMessageConfig(
                    messageId = type.data.messageId,
                    roomId = type.data.roomId
                )
            }

            else -> {
                Timber.d("Empty state share dialog type")
            }
        }
    }

    private fun sendButtonSharePostConfig() {
        contentBinding?.btnShareSend?.isEnabled = false
        if (selectedCommunity != null) {
            selectedCommunity?.let { selectedGroup ->
                shareToGroup(selectedGroup, post)
            }
        } else {
            val message = contentBinding?.vShareInput?.etInput?.text?.trim() ?: ""

            viewModel.sendRepost(post, message.toString(), whoCanComment.state)
        }
    }

    private fun sendButtonShareProfileConfig(userId: Long) {
        shareUserProfile(userId)
    }

    private fun sendButtonShareMomentConfig(moment: MomentItemUiModel) {
        shareMoment(moment)
    }

    private fun sendButtonShareCommunityConfig(groupId: Int) {
        shareCommunity(groupId)
    }

    private fun sendButtonForwardChatMessageConfig(messageId: String, roomId: Long) {
        forwardChatMessage(messageId, roomId)
    }

    private fun shareToGroup(community: CommunityEntity, post: Post) {
        val message = contentBinding?.vShareInput?.etInput?.text?.trim() ?: ""
        repostViewModel.doGroupRepost(
            comment = message.toString(),
            post = post,
            groupId = community.groupId.toLong(),
            commentSettings = whoCanComment.state,
        )
    }

    private fun shareUserProfile(userId: Long) {
        val userIds = viewModel.getSelectedItemsUser()
        val roomIds = viewModel.getSelectedItemsRooms()
        val message = contentBinding?.vShareInput?.etInput?.text?.trim().toString()
        repostViewModel.shareUserProfile(userId, userIds, roomIds, message)
    }

    private fun shareMoment(moment: MomentItemUiModel) {
        val userIds = viewModel.getSelectedItemsUser()
        val roomIds = viewModel.getSelectedItemsRooms()
        val message = contentBinding?.vShareInput?.etInput?.text?.trim().toString()
        repostViewModel.shareMoment(
            moment = moment,
            userIds = userIds,
            roomIds = roomIds,
            message = message
        )
    }

    private fun shareCommunity(groupId: Int) {
        val userIds = viewModel.getSelectedItemsUser()
        val roomIds = viewModel.getSelectedItemsRooms()
        val message = contentBinding?.vShareInput?.etInput?.text?.toString()?.trim() ?: ""
        repostViewModel.shareCommunity(groupId, userIds, roomIds, message)
    }

    private fun forwardChatMessage(
        messageId: String,
        roomId: Long,
    ) {
        val userIds = viewModel.getSelectedItemsUser()
        val roomIds = viewModel.getSelectedItemsRooms()
        val selectedGroupsCount = viewModel.getSelectedGroupsCount()
        val message = contentBinding?.vShareInput?.etInput?.text?.toString()?.trim() ?: ""
        repostViewModel.forwardChatMessage(
            message = messageData,
            selectedGroupsCount = selectedGroupsCount,
            messageId = messageId,
            roomId = roomId,
            userIds = userIds,
            roomIds = roomIds,
            extraMessage = message
        )
    }

    companion object {
        const val LAYOUT_REPOST_TO_CHAT = 0
        const val LAYOUT_REPOST_TO_GROUP = 1
        const val LAYOUT_REPOST_TO_ROADTAPE = 2

        const val LAYOUT_REPOST_TO_CHAT_SEARCH = 3
        const val LAYOUT_REPOST_TO_GROUP_SEARCH = 4

        const val CHAT_ID_NO_MY_POST_MENU = 0
        const val MY_FEED_IMAGE_POST_ID_NO_MY_POST_MENU = 1
        const val GROUP_ID_NO_MY_POST_MENU = 2
        const val MORE_ID_NO_MY_POST_MENU = 3

        const val CHAT_ID_MY_POST_MENU = 0
        const val GROUP_ID_MY_POST_MENU = 1
        const val MORE_ID_MY_POST_MENU = 2

        const val CHAT_ID_NO_MY_REPOST_MENU = 0
        const val MORE_ID_NO_MY_REPOST_MENU = 1
    }

}
