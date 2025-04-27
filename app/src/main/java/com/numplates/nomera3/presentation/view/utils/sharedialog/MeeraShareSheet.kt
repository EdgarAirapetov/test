package com.numplates.nomera3.presentation.view.utils.sharedialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.empty
import com.meera.core.extensions.getScreenHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.visible
import com.meera.core.utils.KeyboardHeightProvider
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogBehDelegate
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogState
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.meera.uikit.widgets.tablayout.UiKitTwoLinesTabLayout
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.Post
import com.numplates.nomera3.databinding.MeeraBottomShareMenuContainerBinding
import com.numplates.nomera3.databinding.MeeraShareBottomSheetBinding
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
import com.numplates.nomera3.modules.moments.comments.presentation.MeeraMomentShareBottomSheetSetupUtil
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentItemUiModel
import com.numplates.nomera3.modules.share.ui.ShareItemsCallback
import com.numplates.nomera3.modules.share.ui.adapter.MeeraShareItemAdapter
import com.numplates.nomera3.modules.share.ui.entity.UIShareItem
import com.numplates.nomera3.modules.share.ui.entity.UIShareMessageEntity
import com.numplates.nomera3.modules.share.ui.model.SharingDialogMode
import com.numplates.nomera3.modules.tags.ui.base.SuggestedTagListMenu
import com.numplates.nomera3.presentation.model.enums.WhoCanCommentPostEnum
import com.numplates.nomera3.presentation.utils.bottomsheet.BottomSheetCloseUtil
import com.numplates.nomera3.presentation.utils.bottomsheet.toAmplitudePropertyHow
import com.numplates.nomera3.presentation.view.utils.sharedialog.adapter.MeeraShareGroupAdapter
import com.numplates.nomera3.presentation.viewmodel.RepostViewModel
import com.numplates.nomera3.presentation.viewmodel.SharePostViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.SharePostViewEvent
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator
import kotlinx.coroutines.delay
import timber.log.Timber
import kotlin.properties.Delegates

private const val SHARE_BOTTOM_SHEET_TAG = "MeeraShareBottomSheet"
private const val KEYBOARD_HIDE_DELAY = 300L
private const val POST_SETTINGS_MENU = "POST_SETTINGS_MENU"
private const val COLLAPSED_HEIGHT_RATIO = 1.6
private const val EVENT_SHARE_LAYOUT_HEIGHT_DP = 490
private const val SELECT_MAX_USER = 10
private const val LIMIT_SEARCH_GROUP = 50
private const val LIMIT_SEARCH_GROUP_PAGINATION = 20
private const val COUNT_BUTTON_MENU_NO_MY_POST = 4
private const val COUNT_BUTTON_MENU_MY_POST = 3
private const val COUNT_BUTTON_MENU_NO_MY_REPOST = 2

data class MeeraShareBottomSheetData(
    val groupId: Long = 0,
    val post: Post? = null,
    val event: EventUiModel? = null,
    val mode: SharingDialogMode = SharingDialogMode.DEFAULT,
    val postOrigin: DestinationOriginEnum? = null,
    val callback: IOnSharePost? = null
)

class MeeraShareSheet: UiKitBottomSheetDialog<MeeraShareBottomSheetBinding>() {

    private var data: MeeraShareBottomSheetData = MeeraShareBottomSheetData()

    private var viewModel by Delegates.notNull<SharePostViewModel>()
    private var repostViewModel by Delegates.notNull<RepostViewModel>()

    private var groupsAdapter: MeeraShareGroupAdapter? = null
    private var friendsAdapter: MeeraShareItemAdapter? = null

    private val expandedStateHeight = getScreenHeight()
    private val collapsedStateHeight = (expandedStateHeight / COLLAPSED_HEIGHT_RATIO).toInt()
    private var bottomBinding by Delegates.notNull<MeeraBottomShareMenuContainerBinding>()
    private var newSuggestionsMenu: SuggestedTagListMenu? = null

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraShareBottomSheetBinding
        get() = MeeraShareBottomSheetBinding::inflate

    private val momentCommentsBottomSheetSetupUtil = MeeraMomentShareBottomSheetSetupUtil()
    private val bottomSheetCloseUtil = BottomSheetCloseUtil(object : BottomSheetCloseUtil.Listener {
        override fun bottomSheetClosed(method: BottomSheetCloseUtil.BottomSheetCloseMethod) {
            viewModel.logPostShareClose(method.toAmplitudePropertyHow())
        }
    })

    private val shareItemsCallback = object : ShareItemsCallback {
        override fun onChecked(item: UIShareItem, isChecked: Boolean) {
            viewModel.itemSearchChecked(item, isChecked)
        }

        override fun canBeChecked(): Boolean {
            return viewModel.canBeChecked()
        }
    }

    private var shareType: ShareDialogType? = ShareDialogType.SharePost
    private var isSecondRepost = false
    private var shareDialogEvent: (event: ShareBottomSheetEvent) -> Unit = { }
    private var tabLayout: UiKitTwoLinesTabLayout? = null
    private var currentLayout = -1
    private var paginator: RecyclerViewPaginator? = null
    private var analyticsPostShare: AnalyticsPostShare? = null
    private var selectedCommunity: CommunityEntity? = null
    private var keyboardHeightProvider: KeyboardHeightProvider? = null
    private var whoCanComment = WhoCanCommentPostEnum.EVERYONE
    private var messageData: UIShareMessageEntity? = null
    private var activeSnackbar: UiKitSnackBar? = null


    override fun getBehaviorDelegate(): UiKitBottomSheetDialogBehDelegate {
        return UiKitBottomSheetDialogBehDelegate.Builder()
            .setBottomSheetState(UiKitBottomSheetDialogState.EXPANDED)
            .setDraggable(true)
            .setSkipCollapsed(true)
            .create(dialog)
    }

    override fun createDialogState(): UiKitBottomSheetDialogParams {
        return UiKitBottomSheetDialogParams(
            labelText = if (data.mode != SharingDialogMode.SUGGEST_EVENT_SHARING)
                context?.getString(R.string.general_share) else null
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SharePostViewModel::class.java)
        repostViewModel = ViewModelProvider(this).get(RepostViewModel::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setOnShowListener { dialogInterface ->
                val bottomSheetDialog = dialogInterface as BottomSheetDialog
                val bottomSheetView = bottomSheetDialog.findViewById<View>(R.id.design_bottom_sheet) ?: return@setOnShowListener
                setupBottomSheetBehavior(bottomSheetView = bottomSheetView)
                setupBottomSheetHeight(bottomSheetView)
                initBottomContainer(bottomSheetDialog)
                setupBottomSheetCustomLogic(bottomSheetDialog)
            }
        }
    }

    private fun setupBottomSheetCustomLogic(
        bottomSheetDialog: BottomSheetDialog
    ) {
        momentCommentsBottomSheetSetupUtil.setup(
            fragment = this,
            dialog = bottomSheetDialog,
            bottomBinding = bottomBinding,
            mainBinding = contentBinding ?: return
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetCloseUtil.reset()
        initSuggestionsMenu()
        data.postOrigin?.let {
            val publicType = when {
                data.event != null -> AmplitudePropertyPublicType.MAP_EVENT
                data.post?.hasPostVideo() == true -> AmplitudePropertyPublicType.VIDEO_POST
                else -> AmplitudePropertyPublicType.POST
            }
            analyticsPostShare = AnalyticsPostShare(
                postId = data.post?.id ?: 0,
                authorId = data.post?.user?.userId ?: NO_USER_ID,
                where = it.toAmplitudePropertyWhere(),
                publicType = publicType
            )
        }

        view.post {
            contentBinding?.appbarShareSearch?.visible()
            when (data.mode) {
                SharingDialogMode.DEFAULT -> showSharing()
                SharingDialogMode.SUGGEST_EVENT_SHARING -> showEventSharingSuggestionLayout()
            }
        }

        if (savedInstanceState == null) {
            when (shareType) {
                is ShareDialogType.SharePost -> {
                    data.postOrigin?.let {
                        val publicType = when {
                            data.event != null -> AmplitudePropertyPublicType.MAP_EVENT
                            data.post?.hasPostVideo() == true -> AmplitudePropertyPublicType.VIDEO_POST
                            else -> AmplitudePropertyPublicType.POST
                        }
                        viewModel.logPostShareOpen(
                            postId = data.post?.id ?: 0,
                            momentId = 0,
                            authorId = data.post?.user?.userId ?: NO_USER_ID,
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

        isSecondRepost = data.post?.parentPost != null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        bottomSheetCloseUtil.onDismiss()
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        keyboardHeightProvider?.release()
        shareDialogEvent.invoke(ShareBottomSheetEvent.OnDismissDialog)
        activeSnackbar?.dismiss()
    }

    fun show(
        fm: FragmentManager,
        data: MeeraShareBottomSheetData,
    ): MeeraShareSheet {
        val dialog = MeeraShareSheet()
        dialog.data = data
        dialog.isCancelable = this.isCancelable
        dialog.show(fm, SHARE_BOTTOM_SHEET_TAG)
        return dialog
    }

    fun showByType(
        fm: FragmentManager,
        shareType: ShareDialogType,
        postOrigin: DestinationOriginEnum? = null,
        event: (event: ShareBottomSheetEvent) -> Unit
    ): MeeraShareSheet {
        val dialog = MeeraShareSheet()
        var data = MeeraShareBottomSheetData(
            groupId = -1L,
            post = Post(),
            event = null,
            postOrigin = postOrigin,
            callback = IOnSharePostStub()
        )
        dialog.shareType = shareType
        dialog.shareDialogEvent = event
        if (shareType is ShareDialogType.ShareCommunity) {
            data = data.copy(groupId = shareType.groupId.toLong())
        }
        dialog.data = data
        dialog.isCancelable = this.isCancelable
        dialog.show(fm, SHARE_BOTTOM_SHEET_TAG)
        return dialog
    }

    private fun initSuggestionsMenu() {
        contentBinding?.tagsList?.also {
            it.root.post {
                val editTextAutoCompletable = bottomBinding.vShareInput
                val bottomSheetBehavior: BottomSheetBehavior<View> = it.let {
                    BottomSheetBehavior.from(it.root)
                }
                bottomSheetBehavior.isDraggable = false
                bottomSheetBehavior.isHideable = false
                bottomSheetBehavior.peekHeight = 0
                SuggestedTagListMenu(
                    fragment = this,
                    editText = editTextAutoCompletable,
                    recyclerView = it.recyclerTags,
                    bottomSheetBehavior = bottomSheetBehavior,
                    fullscreenTagsList = true
                ).also { newSuggestedTagListMenu ->
                    editTextAutoCompletable.suggestionMenu = newSuggestedTagListMenu
                    newSuggestionsMenu = newSuggestedTagListMenu
                }
            }
        }
    }

    private fun setupBottomSheetBehavior(bottomSheetView: View) {
        BottomSheetBehavior.from(bottomSheetView).apply {
            isHideable = true
            skipCollapsed = false
            peekHeight = collapsedStateHeight
            hideFriction = 0.01F
            state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun setupBottomSheetHeight(bottomSheetView: View) {
        val viewGroupLayoutParams = bottomSheetView.layoutParams
        viewGroupLayoutParams?.height = expandedStateHeight
        bottomSheetView.layoutParams = viewGroupLayoutParams
    }

    private fun initBottomContainer(bottomSheetDialog: BottomSheetDialog) {
        val bottomContainer = bottomSheetDialog.findViewById<ViewGroup>(R.id.fl_bottom_container) ?: return
        bottomBinding = MeeraBottomShareMenuContainerBinding.inflate(LayoutInflater.from(context))
        bottomContainer.addView(bottomBinding.root)
    }

    private fun showSharing() {
        contentBinding?.layoutEventShare?.root?.gone()
        bottomBinding.root.visible()
        setupSharingUi()
        contentBinding?.appbarShareSearch?.visible()
        bottomBinding.cvMenuContainer.visibility
    }

    private fun showEventSharingSuggestionLayout() {
        getBehavior()?.peekHeight = dpToPx(EVENT_SHARE_LAYOUT_HEIGHT_DP)
        getBehavior()?.state = BottomSheetBehavior.STATE_COLLAPSED

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
            layoutEventShare.tvEventShareEnable.apply { post { requestLayout() } }
            bottomBinding.root.gone()
        }
    }

    private fun setupSharingUi() {
        rootBinding?.ibBottomSheetDialogAction?.gone()
        bottomBinding.btnShareSend.isEnabled = false
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
        setSearchInputListeners()
        setSearchInputTextChangeListener()
        setSendRepostClickListener()
        if (data.mode == SharingDialogMode.SUGGEST_EVENT_SHARING) {
            selectChatTab()
            getBehavior()?.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun shareDialogTypeConfig() {
        when (shareType) {
            is ShareDialogType.ShareProfile,
            is ShareDialogType.ShareCommunity,
            is ShareDialogType.ShareMoment -> {
                tabLayout = bottomBinding.vBottomMenuHideRoadAndGroupButtons
            }

            is ShareDialogType.SharePost -> {
                when {
                    isSecondRepost -> {
                        tabLayout = bottomBinding.vBottomMenuHideRoadAndGroupButtons
                        bottomBinding.vBottomMenu.gone()
                    }
                    data.post?.user?.userId == viewModel.getOwnUserId() -> {
                        tabLayout = bottomBinding.vBottomMenuHideRoad
                        bottomBinding.vBottomMenu.gone()
                    }
                    else -> {
                        tabLayout = bottomBinding.vBottomMenu
                    }
                }
            }

            is ShareDialogType.MessageForwarding -> {
                tabLayout = null
            }

            else -> {
                tabLayout = bottomBinding.vBottomMenuHideMoreButton
            }
        }
    }

    private fun showTabMenu(show: Boolean = true) {
        tabLayout?.isVisible = show
    }

    /**
     * To use images in a text field, use this TextViewWithImages
     * @see com.numplates.nomera3.presentation.view.ui.TextViewWithImages
     */
    @SuppressLint("SetTextI18n")
    private fun initPostLayout() {
        contentBinding?.vPost?.setIconDescription(R.drawable.ic_outlined_repost_s)
        contentBinding?.vPost?.cellTitleVerified = data.post?.user?.profileVerified.toBoolean()
        data.post?.user?.name?.let {
            contentBinding?.vPost?.setTitleValue(it)
        }

        if (data.post?.smallImage != null && data.post?.smallImage != String.empty()) {
            contentBinding?.vPost?.setLeftUserPicConfig(UserpicUiModel(userAvatarUrl = data.post?.smallImage))
        } else {
            contentBinding?.vPost?.setLeftUserPicConfig(UserpicUiModel(userAvatarUrl = data.post?.smallUrl))
        }
    }

    private fun initBottomMenu(tab: UiKitTwoLinesTabLayout?) {
        tab?.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    newSuggestionsMenu?.dismiss()
                    hideEmptyGroups()
                    hideEmptyFriends()
                    hideEmptySearch()
                    clearInput()
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

                override fun onTabReselected(tab: TabLayout.Tab?) = Unit
            }
        )
    }

    private fun clearInput() {
        contentBinding?.appbarShareSearch?.clear()
    }

    private fun setKeyboardHeightObserver() {
        if (this.shareType is ShareDialogType.MessageForwarding) return
        keyboardHeightProvider?.observer = {
            adjustRootView(it)
        }
    }

    private fun adjustRootView(keyboardHeight: Int) {
        showTabMenu(keyboardHeight <= 0)
        shareDialogTypeConfig()
    }

    private fun setButtonListeners() {
        rootBinding?.ivBottomSheetDialogSettings?.setThrottledClickListener {
            val where = when (currentLayout) {
                LAYOUT_REPOST_TO_ROADTAPE -> AmplitudePropertyWhere.SELF_FEED
                LAYOUT_REPOST_TO_GROUP -> AmplitudePropertyWhere.COMMUNITY
                else -> AmplitudePropertyWhere.OTHER
            }
            viewModel.logPostShareSettingsTap(where)
            showCommentsMenu()
        }
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

    private fun hideEmptyGroups() {
        contentBinding?.vGroupEmptyState?.gone()
    }

    private fun hideEmptyFriends() {
        contentBinding?.vEmptyState?.gone()
    }

    private fun hideEmptySearch() {
        bottomBinding.vShareInput.visible()
        bottomBinding.btnShareSend.visible()
        contentBinding?.vSearchEmptyState?.gone()
    }

    @SuppressLint("SetTextI18n")
    private fun showEmptyFriends() {
        contentBinding?.vEmptyState?.visible()
        bottomBinding.vShareInput.gone()
        contentBinding?.rvSharePostList?.gone()
        contentBinding?.vSearchFriendsBtn?.setThrottledClickListener {
            data.callback?.onShareFindFriend()
            shareDialogEvent.invoke(ShareBottomSheetEvent.OnClickFindFriendButton)
            dismiss()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showEmptySearch() {
        contentBinding?.vSearchEmptyState?.visible()
        contentBinding?.vEmptyState?.gone()
        contentBinding?.vGroupEmptyState?.gone()
        bottomBinding.btnShareSend.gone()
        bottomBinding.vShareInput.gone()
    }

    private fun initSelectedMenuAllPost(position: Int?){
        when (position) {
            CHAT_ID_NO_MY_POST_MENU -> initBottomChatMenu()
            MY_FEED_IMAGE_POST_ID_NO_MY_POST_MENU -> initBottomFeedMenu()
            GROUP_ID_NO_MY_POST_MENU -> initBottomGroupMenu()
            MORE_ID_NO_MY_POST_MENU -> shareMore()
        }
    }

    private fun initUnselectedMenuAllPost(position: Int?){
        when (position) {
            CHAT_ID_NO_MY_POST_MENU -> contentBinding?.rvSharePostList?.gone()
            MY_FEED_IMAGE_POST_ID_NO_MY_POST_MENU -> contentBinding?.vPost?.gone()
            GROUP_ID_NO_MY_POST_MENU -> contentBinding?.rvSharePostList?.gone()
            MORE_ID_NO_MY_POST_MENU -> Timber.d("No action state")
        }
    }

    private fun initSelectedMenuMyPost(position: Int?){
        when (position) {
            CHAT_ID_MY_POST_MENU -> initBottomChatMenu()
            GROUP_ID_MY_POST_MENU -> initBottomGroupMenu()
            MORE_ID_MY_POST_MENU -> shareMore()
        }
    }

    private fun initSelectedMenuNoMyRepost(position: Int?){
        when (position) {
            CHAT_ID_NO_MY_REPOST_MENU -> initBottomChatMenu()
            MORE_ID_NO_MY_REPOST_MENU -> shareMore()
        }
    }

    private fun initUnselectedMenuMyPost(position: Int?){
        when (position) {
            CHAT_ID_MY_POST_MENU -> contentBinding?.rvSharePostList?.gone()
            GROUP_ID_MY_POST_MENU -> contentBinding?.rvSharePostList?.gone()
            MORE_ID_MY_POST_MENU -> Timber.d("No action state")
        }
    }

    private fun initUnselectedMenuNoMyRepost(position: Int?){
        when (position) {
            CHAT_ID_NO_MY_REPOST_MENU -> contentBinding?.rvSharePostList?.gone()
            MORE_ID_NO_MY_REPOST_MENU -> Timber.d("No action state")
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

    private fun shareMore() {
        val shareDialogType = shareType ?: return
        when (shareDialogType) {
            is ShareDialogType.SharePost -> repostViewModel.getPostLink(data.post?.id ?: 0)
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

    private fun showPostLayout() {
        setSendButtonEnabled()
        rootBinding?.ivBottomSheetDialogSettings?.visible()
        currentLayout = LAYOUT_REPOST_TO_ROADTAPE
    }

    private fun showFriendsLayout() {
        configureFriendsAdapter()
        setSendButtonDisabled()
    }

    private fun showGroupsLayout() {
        configureGroupAdapter()
        setSendButtonDisabled()
    }

    private fun setSendButtonEnabled() {
        bottomBinding.btnShareSend.isEnabled = true
    }

    private fun setSendButtonDisabled() {
        bottomBinding.btnShareSend.isEnabled = false
    }


    private fun initGroupsLiveData() {
        viewModel.liveMyGroups.observe(viewLifecycleOwner) {
            onSuccess { data ->
                if (data.isNotEmpty()) {
                    hideEmptySearch()
                }
                groupsAdapter?.submitList(data)
            }
            onProgress {}
            onError { _, _ -> }
        }
    }

    private fun showEmptyGroups() {
        bottomBinding.vShareInput.gone()
        bottomBinding.btnShareSend.gone()

        contentBinding?.apply {
            vGroupEmptyState.visible()
            appbarShareSearch.gone()
            vSearchGroupBtn.setThrottledClickListener {
                data.callback?.onShareFindGroup()
                dismiss()
            }
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

    private fun initRepostLiveData() {
        repostViewModel.liveEvent.observe(this) {
            handleViewEvent(it)
        }

        viewModel.sharePostLiveEvent.observe(viewLifecycleOwner) {
            handleViewEvent(it)
        }
    }

    private fun setSearchInputListeners() {
        contentBinding?.appbarShareSearch?.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                getBehavior()?.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        contentBinding?.appbarShareSearch?.setThrottledClickListener {
            getBehavior()?.state = BottomSheetBehavior.STATE_EXPANDED
        }
        contentBinding?.appbarShareSearch?.setCloseButtonClickedListener {
            view?.hideKeyboard()
        }
    }

    private fun setSearchInputTextChangeListener() {
        contentBinding?.appbarShareSearch?.doAfterSearchTextChanged { text ->
            analyticsPostShare?.let {
                if (text.isNotEmpty()) {
                    analyticsPostShare = it.copy(search = true)
                }
            }

            if (getBehavior()?.state != BottomSheetBehavior.STATE_EXPANDED) {
                getBehavior()?.state = BottomSheetBehavior.STATE_EXPANDED
            }

            when (currentLayout) {
                LAYOUT_REPOST_TO_CHAT, LAYOUT_REPOST_TO_CHAT_SEARCH -> {
                    if (text.isNotEmpty()) {
                        configureFriendsSearchAdapter()
                    } else {
                        configureFriendsAdapter()
                    }
                }

                LAYOUT_REPOST_TO_GROUP, LAYOUT_REPOST_TO_GROUP_SEARCH -> {
                    if (text.isNotEmpty()) {
                        configureGroupSearchAdapter()
                    } else {
                        configureGroupAdapter()
                    }
                }
            }
        }
    }

    private fun setSendRepostClickListener() {
        bottomBinding.btnShareSend.setThrottledClickListener {
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
                            textAdded = bottomBinding.vShareInput.text?.trim()?.isEmpty() == true
                        )
                    )
                }

                else -> {
                    analyticsPostShare?.let {
                        val textAdded = bottomBinding.vShareInput.text?.trim()?.isEmpty() == true
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
        bottomBinding.btnShareSend.isEnabled = false
        val post = data.post
        if (selectedCommunity != null && post != null) {
            selectedCommunity?.let { selectedGroup ->
                shareToGroup(selectedGroup, post)
            }
        } else {
            val message = bottomBinding.vShareInput.text?.trim()
            post?.let { viewModel.sendRepost(post, message.toString(), whoCanComment.state) }

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

    private fun forwardChatMessage(
        messageId: String,
        roomId: Long,
    ) {
        val userIds = viewModel.getSelectedItemsUser()
        val roomIds = viewModel.getSelectedItemsRooms()
        val selectedGroupsCount = viewModel.getSelectedGroupsCount()
        val message = bottomBinding.vShareInput.text.toString().trim()
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

    private fun shareToGroup(community: CommunityEntity, post: Post) {
        val message = bottomBinding.vShareInput.text?.trim()
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
        val message = bottomBinding.vShareInput.text?.trim().toString()
        repostViewModel.shareUserProfile(userId, userIds, roomIds, message)
    }

    private fun shareMoment(moment: MomentItemUiModel) {
        val userIds = viewModel.getSelectedItemsUser()
        val roomIds = viewModel.getSelectedItemsRooms()
        val message = bottomBinding.vShareInput.text?.trim().toString()
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
        val message = bottomBinding.vShareInput.text.toString().trim()
        repostViewModel.shareCommunity(groupId, userIds, roomIds, message)
    }

    private fun handleViewEvent(event: SharePostViewEvent) {
        when (event) {
            is SharePostViewEvent.onSuccessGroupRepost -> {
                data.callback?.onShareToGroupSuccess(selectedCommunity?.name)
                dismiss()
            }

            is SharePostViewEvent.onSuccessRoadTypeRepost -> {
                data.callback?.onShareToRoadSuccess()
                dismiss()
            }

            is SharePostViewEvent.onSuccessMessageRepost -> {
                data.callback?.onShareToChatSuccess(event.repostTargetCount)
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
                bottomBinding?.btnShareSend?.isEnabled = true
                showCommonRepostError(event.errorMessage)
            }

            is SharePostViewEvent.onErrorRoadTypeRepost -> {
                bottomBinding?.btnShareSend?.isEnabled = true
                showCommonRepostError()
            }

            is SharePostViewEvent.OnErrorMessageRepost -> {
                bottomBinding?.btnShareSend?.isEnabled = true
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
                data.callback?.onOpenShareOutside()
                shareLinkOutside(context, event.postLink)
                dismiss()
            }

            is SharePostViewEvent.onErrorSharePostLink,
            is SharePostViewEvent.onErrorShareMomentLink -> {
                showCommonRepostError()
            }

            is SharePostViewEvent.onSuccessShareMomentLink -> {
                data.callback?.onOpenShareOutside()
                shareLinkOutside(context, event.momentLink)
                dismiss()
            }

            is SharePostViewEvent.PlaceHolderShareEvent -> handlePlaceHolder(event.placeHolder)
            is SharePostViewEvent.BlockSendBtn -> setSendButtonDisabled()
            is SharePostViewEvent.UnBlockSendBtn -> setSendButtonEnabled()
        }
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

    private fun initGroupClickListener(community: CommunityEntity) {
        selectedCommunity = community
        contentBinding?.rvSharePostList?.gone()
        contentBinding?.vPost?.visible()
        bottomBinding.btnShareSend.isEnabled = true
        contentBinding?.appbarShareSearch?.gone()
        rootBinding?.ibBottomSheetDialogAction?.visible()
        rootBinding?.ibBottomSheetDialogAction?.setThrottledClickListener {
            contentBinding?.rvSharePostList?.visible()
            contentBinding?.vPost?.gone()
            bottomBinding.btnShareSend.isEnabled = false
            contentBinding?.appbarShareSearch?.visibility
            rootBinding?.ibBottomSheetDialogAction?.gone()
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

    @SuppressLint("SetTextI18n")
    private fun setTitleDialog(countCheckUser: Int) {
        val shareDialogLabel = resources.getString(
            when (shareType) {
                is ShareDialogType.MessageForwarding -> R.string.menu_chat_forward_message
                is ShareDialogType.ShareCommunity -> R.string.share_community
                is ShareDialogType.ShareProfile -> R.string.share_profile
                else -> R.string.general_share
            }
        )
        rootBinding?.tvBottomSheetDialogLabel?.visible()
        rootBinding?.tvBottomSheetDialogLabel?.text = shareDialogLabel
        rootBinding?.tvBottomSheetDialogLabelExtra?.text = "$countCheckUser/$SELECT_MAX_USER"
    }

    private fun handlePlaceHolder(placeHolder: SharePlaceHolderEnum) {
        hideEmptySearch()
        when (placeHolder) {
            SharePlaceHolderEnum.EMPTY_SEARCH-> showEmptySearch()
            SharePlaceHolderEnum.EMPTY_SHARE_ITEMS -> showEmptyFriends()
            SharePlaceHolderEnum.EMPTY_SHARE_GROUPS -> showEmptyGroups()
            SharePlaceHolderEnum.ERROR_SEARCH,
            SharePlaceHolderEnum.ERROR_SHARE_ITEMS -> Timber.d("Error share")
            SharePlaceHolderEnum.OK -> hideEmptyFriends()
            else -> Timber.d("Empty state share")
        }
    }

    private fun showCommonRepostError(message: String? = null) {
        val messageToShow = message ?: getString(R.string.error_while_processing_repost)
        activeSnackbar = UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = messageToShow,
                    avatarUiState = AvatarUiState.ErrorIconState,
                )
            )
        )
        activeSnackbar?.show()
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
