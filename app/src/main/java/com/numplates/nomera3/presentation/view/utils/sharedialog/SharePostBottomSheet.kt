package com.numplates.nomera3.presentation.view.utils.sharedialog

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TouchDelegate
import android.view.View
import android.view.View.MeasureSpec.UNSPECIFIED
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.meera.core.extensions.animateHeight
import com.meera.core.extensions.click
import com.meera.core.extensions.displayHeight
import com.meera.core.extensions.dp
import com.meera.core.extensions.getColorCompat
import com.meera.core.extensions.getDrawableCompat
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.getToolbarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.invisible
import com.meera.core.extensions.isEllipsized
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.newSize
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setPaddingBottom
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.simpleName
import com.meera.core.extensions.updatePadding
import com.meera.core.extensions.visible
import com.meera.core.preferences.AppSettings
import com.meera.core.utils.KeyboardHeightProvider
import com.meera.core.utils.getAge
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.Post
import com.numplates.nomera3.data.network.core.INetworkValues
import com.numplates.nomera3.databinding.FragmentShareContainerBinding
import com.numplates.nomera3.databinding.ItemShareBottomSheetMenuBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyPublicType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereSent
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AnalyticsPostShare
import com.numplates.nomera3.modules.baseCore.helper.amplitude.NO_USER_ID
import com.numplates.nomera3.modules.communities.data.entity.CommunityEntity
import com.numplates.nomera3.modules.communities.utils.shareLinkOutside
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.entity.toAmplitudePropertyWhere
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentItemUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventChipsType
import com.numplates.nomera3.modules.maps.ui.events.model.EventChipsUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventUiModel
import com.numplates.nomera3.modules.share.ui.ShareItemsCallback
import com.numplates.nomera3.modules.share.ui.adapter.ShareItemAdapter
import com.numplates.nomera3.modules.share.ui.entity.UIShareItem
import com.numplates.nomera3.modules.share.ui.entity.UIShareMessageEntity
import com.numplates.nomera3.modules.share.ui.model.SharingDialogMode
import com.numplates.nomera3.modules.tags.ui.base.SuggestedTagListMenu
import com.numplates.nomera3.presentation.model.enums.WhoCanCommentPostEnum
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment
import com.numplates.nomera3.presentation.utils.bottomsheet.BottomSheetCloseUtil
import com.numplates.nomera3.presentation.utils.bottomsheet.toAmplitudePropertyHow
import com.numplates.nomera3.presentation.utils.setTextNoSpans
import com.numplates.nomera3.presentation.utils.spanTagsText
import com.numplates.nomera3.presentation.view.adapter.newfriends.FriendModel
import com.numplates.nomera3.presentation.view.ui.bottomMenu.SurveyBottomMenu
import com.numplates.nomera3.presentation.view.ui.edittextautocompletable.EditTextAutoCompletable
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.view.utils.sharedialog.adapter.ShareGroupAdapter
import com.numplates.nomera3.presentation.viewmodel.RepostViewModel
import com.numplates.nomera3.presentation.viewmodel.SharePostViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.SharePostViewEvent
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject

private const val INPUT_TOUCH_EXTRA_SPACE = 32
private const val INPUT_TOUCH_EXTRA_RIGHT_SPACE = 16
private const val EXTRA_SPACE_TAG = 50
private const val KEYBOARD_HIDE_DELAY = 300L

class SharePostBottomSheet(
    private var groupId: Long = -1L,
    private val post: Post,
    private val event: EventUiModel?,
    private val callback: IOnSharePost,
    private val mode: SharingDialogMode = SharingDialogMode.DEFAULT,
    private val postOrigin: DestinationOriginEnum? = null,
) : BaseBottomSheetDialogFragment<FragmentShareContainerBinding>() {

    private var shareType: ShareDialogType? = ShareDialogType.SharePost

    private var shareDialogEvent: (event: ShareBottomSheetEvent) -> Unit = { }

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

    private var rootView: FrameLayout? = null
    private var mainContainer: FrameLayout? = null
    private var fl_share_container: FrameLayout? = null
    private var fl_bottom_container: FrameLayout? = null
    private var tv_share_title: TextView? = null
    private var tv_share_subtitle: TextView? = null
    private var iv_close_share: ImageView? = null
    private var cl_share_bottom_container: ConstraintLayout? = null
    private var mcv_share_chat: MaterialCardView? = null
    private var mcv_share_road: MaterialCardView? = null
    private var mcv_share_group: MaterialCardView? = null
    private var mcv_share_more: MaterialCardView? = null
    private var ll_road: LinearLayout? = null
    private var ll_group: LinearLayout? = null
    private var ll_more: LinearLayout? = null
    private var tv_share_chat: TextView? = null
    private var tv_share_road: TextView? = null
    private var tv_share_group: TextView? = null
    private var tv_share_more: TextView? = null
    private var et_share_input: EditTextAutoCompletable? = null
    private var btn_share_send: ImageView? = null
    private var commentSettingsBtn: ImageView? = null

    private var viewPostHeight = 0
    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    private var isBottomContainerVisible = true
    private var keyboardHeightProvider: KeyboardHeightProvider? = null
    private var bottomContainerMenuHeight = 0
    private var bottomContainerInputHeight = 0

    private var groupsAdapter: ShareGroupAdapter? = null
    private var friendsAdapter: ShareItemAdapter? = null
    private lateinit var viewModel: SharePostViewModel
    private var selectedCommunity: CommunityEntity? = null
    private val selectedUsers = mutableListOf<FriendModel>()
    private var paginator: RecyclerViewPaginator? = null
    private var currentLayout = -1
    private var isSecondRepost = false
    private var whoCanComment = WhoCanCommentPostEnum.EVERYONE
    private var messageData: UIShareMessageEntity? = null

    private var analyticsPostShare: AnalyticsPostShare? = null

    private val bottomSheetCloseUtil = BottomSheetCloseUtil(object : BottomSheetCloseUtil.Listener {
        override fun bottomSheetClosed(method: BottomSheetCloseUtil.BottomSheetCloseMethod) {
            viewModel.logPostShareClose(method.toAmplitudePropertyHow())
        }
    })

    private val repostViewModel by viewModels<RepostViewModel>()

    @Inject
    lateinit var appSettings: AppSettings

    init {
        App.component.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SharePostViewModel::class.java)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentShareContainerBinding
        get() = FragmentShareContainerBinding::inflate

    private var tagsBehavior: View? = null
    private var suggestionsMenu: SuggestedTagListMenu? = null

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

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        isSecondRepost = post.parentPost != null

        view.post {
            val dialog = dialog as BottomSheetDialog
            rootView = dialog.findViewById(R.id.container)
            mainContainer = dialog.findViewById(R.id.design_bottom_sheet)
            fl_bottom_container = dialog.findViewById(R.id.fl_bottom_container)
            cl_share_bottom_container = fl_bottom_container?.findViewById(R.id.cl_share_bottom_container)
            fl_share_container = view.findViewById(R.id.fl_share_container)
            tv_share_title = view.findViewById(R.id.tv_share_title)
            tv_share_subtitle = view.findViewById(R.id.tv_share_subtitle)
            iv_close_share = view.findViewById(R.id.iv_close_share)
            commentSettingsBtn = view.findViewById(R.id.iv_setting_comments)
            binding?.layoutList?.rvSharePostList?.apply {
                addItemDecoration(ShareDividerItemDecorator(requireContext()))
                visible()
            }
            binding?.layoutList?.appbarShareSearch?.visible()

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

                else -> {}
            }
        }
    }

    private fun setupSharingUi() {
        createBottomMenu()

        (dialog as? BottomSheetDialog)?.let(::initSuggestionsMenu)

        initPostLayout()
        bottomSheetBehavior = BottomSheetBehavior.from(mainContainer!!)
        setBehaviorListener()
        val commonHeight = calculateMainContainerHeight()
        val viewGroupLayoutParams = mainContainer?.layoutParams
        viewGroupLayoutParams?.height = commonHeight
        mainContainer?.layoutParams = viewGroupLayoutParams
        mainContainer?.setPaddingBottom(bottomContainerInputHeight + bottomContainerMenuHeight)

        keyboardHeightProvider = KeyboardHeightProvider(dialog?.window!!.decorView)
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
            bottomSheetBehavior?.peekHeight = binding?.layoutEventShare?.root?.height ?: 0
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun showEventSharingSuggestionLayout() {
        configureFriendsAdapter()
        binding?.apply {
            layoutEventShare.tvEventShareEnable.setThrottledClickListener {
                showSharing()
            }
            layoutEventShare.tvEventShareSkip.setThrottledClickListener {
                dismiss()
            }
            layoutEventShare.ibEventShareClose.setThrottledClickListener {
                dismiss()
            }
            vgShareContainerHeader.gone()
            flShareContainer.gone()
            layoutEventShare.root.visible()
        }
    }

    private fun showSharing() {
        binding?.apply {
            setupSharingUi()
            vgShareContainerHeader.visible()
            flShareContainer.visible()
            layoutEventShare.root.gone()
        }
    }

    override fun onBackKeyPressed() {
        super.onBackKeyPressed()
        bottomSheetCloseUtil.onBackButtonPressed()
    }

    private fun shareDialogTypeConfig() {
        when (shareType) {
            is ShareDialogType.ShareProfile,
            is ShareDialogType.ShareCommunity,
            is ShareDialogType.ShareMoment,
            -> hideRoadAndGroupButtons()

            is ShareDialogType.SharePost -> {}
            else -> hideMoreButton()
        }
    }


    private fun hideRoadAndGroupButtons() {
        ll_road?.gone()
        ll_group?.gone()
    }

    private fun hideMoreButton() {
        ll_more?.gone()
    }

    private fun getDialogTitle(vararg shareTo: String): String {
        return when (shareType) {
            is ShareDialogType.SharePost -> {
                return when {
                    shareTo.isNotEmpty() -> getString(R.string.share_to, *shareTo)
                    else -> getString(R.string.general_share)
                }
            }

            is ShareDialogType.ShareProfile -> getString(R.string.share_profile)
            is ShareDialogType.ShareMoment -> getString(R.string.moment_share)
            is ShareDialogType.ShareCommunity -> getString(R.string.share_community)
            is ShareDialogType.MessageForwarding -> getString(R.string.menu_chat_forward_message)
            else -> getString(R.string.general_share)
        }
    }


    private fun initSuggestionsMenu(dialog: BottomSheetDialog) {
        dialog.findViewById<View>(R.id.tags_list)?.also { tagListView ->
            tagListView.post {
                tagsBehavior = tagListView
                tagListView.visible()
                val recyclerTags = tagListView.findViewById<RecyclerView>(R.id.recycler_tags)
                val behavior = BottomSheetBehavior.from(tagListView)
                et_share_input?.let { et ->
                    suggestionsMenu = SuggestedTagListMenu(
                        fragment = this,
                        editText = et,
                        recyclerView = recyclerTags,
                        bottomSheetBehavior = behavior
                    )
                    et_share_input?.suggestionMenu = suggestionsMenu
                }
            }
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
                btn_share_send?.isEnabled = true
                showCommonRepostError(event.errorMessage)
            }

            is SharePostViewEvent.onErrorRoadTypeRepost -> {
                btn_share_send?.isEnabled = true
                showCommonRepostError()
            }

            is SharePostViewEvent.OnErrorMessageRepost -> {
                btn_share_send?.isEnabled = true
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
            is SharePostViewEvent.onErrorShareMomentLink,
            -> {
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
            SharePlaceHolderEnum.EMPTY_SEARCH -> showPlaceHolder(
                R.drawable.ic_empty_search_noomeera,
                R.string.no_matches,
                null
            )

            SharePlaceHolderEnum.EMPTY_SHARE_ITEMS -> showEmptyFriends()
            SharePlaceHolderEnum.ERROR_SEARCH,
            SharePlaceHolderEnum.ERROR_SHARE_ITEMS,
            -> {

            }

            SharePlaceHolderEnum.OK -> hideEmptyFriends()
            else-> Unit
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

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

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
        dialogListener?.onDismissDialog()
        super.onDismiss(dialog)
        bottomSheetCloseUtil.onDismiss()
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        keyboardHeightProvider?.release()
    }

    private fun createBottomMenu() {
        fl_bottom_container?.setBackgroundColor(Color.WHITE)
        val shareBinding = ItemShareBottomSheetMenuBinding.inflate(LayoutInflater.from(context))
        mcv_share_chat = shareBinding.mcvShareChat
        mcv_share_road = shareBinding.mcvShareRoad
        mcv_share_group = shareBinding.mcvShareGroup
        mcv_share_more = shareBinding.mcvShareMore
        ll_road = shareBinding.llRoad
        ll_group = shareBinding.llGroup
        ll_more = shareBinding.llMore
        tv_share_chat = shareBinding.tvShareChat
        tv_share_road = shareBinding.tvShareRoad
        tv_share_group = shareBinding.tvShareGroup
        tv_share_more = shareBinding.tvShareMore
        et_share_input = shareBinding.itemInputLayout.etShareInput
        btn_share_send = shareBinding.itemInputLayout.btnShareSend

        setInputTextTouchDelegate(et_share_input)

        shareDialogTypeConfig()

        if (post.user?.userId == appSettings.readUID() && !isSecondRepost) {
            ll_road?.gone()
        }
        if (isSecondRepost) {
            ll_road?.gone()
            ll_group?.gone()
        }

        fl_bottom_container?.addView(shareBinding.root)

        hideIfNeedBottomNav { isHide ->
            if (isHide) shareBinding.clShareBottomContainer.gone()
        }

        fl_bottom_container?.post {
            selectChatTab()
        }

        et_share_input?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        et_share_input?.setOnClickListener {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }


    }

    private fun setInputTextTouchDelegate(editText: EditText?) {
        val parent = editText?.parent as View
        val extraSpace = INPUT_TOUCH_EXTRA_SPACE.dp
        parent.post {
            val touchableArea = Rect()
            editText.getHitRect(touchableArea)
            touchableArea.top -= extraSpace
            touchableArea.bottom += extraSpace
            touchableArea.left -= extraSpace
            touchableArea.right += INPUT_TOUCH_EXTRA_RIGHT_SPACE.dp
            parent.touchDelegate = TouchDelegate(touchableArea, editText)
        }
    }

    private fun showCommentsMenu() {
        val menu = SurveyBottomMenu()
        menu.isEvent = event != null
        menu.isRoad = selectedCommunity == null
        menu.state = whoCanComment
        menu.allClickedListener = {
            whoCanComment = WhoCanCommentPostEnum.EVERYONE
            hideIndicator()
        }

        menu.noOneClickedListener = {
            whoCanComment = WhoCanCommentPostEnum.NOBODY
            showIndicator()
        }

        menu.friendsClickedListener = {
            whoCanComment = WhoCanCommentPostEnum.FRIENDS
            showIndicator()
        }

        menu.show(parentFragmentManager)
    }

    private fun showIndicator() {
        binding?.vCommentSettingIndicator?.visible()
        binding?.vCoverCommentSettingIndicator?.visible()
    }

    private fun hideIndicator() {
        binding?.vCommentSettingIndicator?.gone()
        binding?.vCoverCommentSettingIndicator?.gone()
    }

    private fun setBehaviorListener() {
        bottomSheetBehavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset < -0.2 && isBottomContainerVisible) {
                    isBottomContainerVisible = false
                    animateBottomContainer(fl_bottom_container?.height ?: 0)
                } else if (slideOffset > -0.2 && !isBottomContainerVisible) {
                    if (!isGroupTabSelected() && (binding?.layoutPost?.root?.isVisible == true || isChatTabSelected())) {
                        isBottomContainerVisible = true
                        animateBottomContainer(0)
                    } else if (isGroupTabSelected()) {
                        isBottomContainerVisible = true
                        animateBottomContainer(
                            if (selectedCommunity != null) {
                                0
                            } else {
                                bottomContainerInputHeight + bottomContainerMenuHeight + 1.dp
                            }
                        )
                    }
                }
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                bottomSheetCloseUtil.onStateChanged(newState)
            }
        })
    }

    private fun animateBottomContainer(height: Int) {
        view?.post {
            try {
                fl_bottom_container?.animate()?.cancel()
                fl_bottom_container?.animate()
                    ?.translationY(height.toFloat())
                    ?.setDuration(150)
                    ?.start()
            } catch (e: Exception) {
                Timber.e(e)
                // ignore
            }
        }
    }

    private fun isRoadTabSelected(): Boolean {
        return mcv_share_road?.strokeWidth != 0
    }

    private fun isGroupTabSelected(): Boolean {
        return mcv_share_group?.strokeWidth != 0
    }

    private fun isChatTabSelected(): Boolean {
        return mcv_share_chat?.strokeWidth != 0
    }

    @SuppressLint("SetTextI18n")
    private fun initPostLayout() {
        /**
         * To use images in a text field, use this TextViewWithImages
         * @see com.numplates.nomera3.presentation.view.ui.TextViewWithImages
         */

        binding?.layoutPost?.clVideo?.gone()

        val vip = post.user?.accountType == INetworkValues.ACCOUNT_TYPE_VIP
        val tagSpan = post.tagSpan
        if (post.text != null && post.text!!.isNotEmpty()) {
            if (tagSpan != null) {
                val linkColor = if (vip) {
                    R.color.ui_yellow
                } else R.color.ui_purple
                binding?.layoutPost?.tvSharePostText?.let { tvSharePostText ->
                    spanTagsText(
                        context = requireContext(),
                        tvText = tvSharePostText,
                        post = tagSpan,
                        linkColor = linkColor
                    ) {}
                }
            } else {
                binding?.layoutPost?.tvSharePostText?.setTextNoSpans(post.text)
            }
        } else {
            binding?.layoutPost?.tvSharePostText?.gone()
        }

        var needToShowImagePreview = false
        if (post.smallImage != null && post.smallImage != "") {
            binding?.layoutPost?.ivSharePostImage?.loadGlide(post.smallImage)
            needToShowImagePreview = true
        } else {
            binding?.layoutPost?.mcvSharePostImage?.gone()
        }

        if (post.video != null) {
            binding?.layoutPost?.mcvSharePostImage?.visible()

            val videoDurationInSeconds = post.videoDurationInSeconds
            if (videoDurationInSeconds != null) {
                binding?.layoutPost?.tvVideoTime?.text = convertSecondsToDuration(videoDurationInSeconds)
                binding?.layoutPost?.tvVideoTime?.visible()
                binding?.layoutPost?.clVideo?.visible()
            } else {
                binding?.layoutPost?.tvVideoTime?.gone()
                binding?.layoutPost?.clVideo?.gone()
            }

            val options: RequestOptions = RequestOptions()
                .error(R.drawable.img_error)
            binding?.layoutPost?.ivSharePostImage?.let { ivSharePostImage ->
                Glide.with(binding?.layoutPost?.ivSharePostImage?.context!!)
                    .load(post.video)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply(options)
                    .into(ivSharePostImage)
            }
        } else if (!needToShowImagePreview) {
            binding?.layoutPost?.mcvSharePostImage?.gone()
        }

        post.user?.let { user ->
            if (user.birthday != null) {
                var age: String
                user.birthday?.let { birth ->
                    age = getAge(birth)
                    if (age.isNotEmpty()) {
                        binding?.layoutPost?.tvSharePostName?.text = "${user.name}, $age"
                        if (binding?.layoutPost?.tvSharePostName?.isEllipsized() == true) {
                            binding?.layoutPost?.tvSharePostAge?.text = ", $age"
                            binding?.layoutPost?.tvSharePostAge?.visible()
                        }
                    }
                }
            } else {
                binding?.layoutPost?.tvSharePostName?.text = post.user?.name
            }
            binding?.layoutPost?.vipviewSharePost?.setUp(
                requireContext(),
                user.avatarSmall,
                user.accountType ?: INetworkValues.ACCOUNT_TYPE_REGULAR,
                user.accountColor ?: INetworkValues.COLOR_RED
            )
        }

        if (event != null && viewModel.isEventsOnMapEnabled) {
            if (event.tagSpan != null) {
                val linkColor = if (vip) R.color.ui_yellow else R.color.ui_purple
                binding?.layoutPost?.tvSharePostTitle?.let { textView ->
                    spanTagsText(
                        context = requireContext(),
                        tvText = textView,
                        post = event.tagSpan,
                        linkColor = linkColor
                    ) {}
                }
            } else {
                binding?.layoutPost?.tvSharePostTitle?.setTextNoSpans(event.title)
            }
            binding?.layoutPost?.tvSharePostTitle?.visible()
            val eventChipsUiModel = EventChipsUiModel(
                type = EventChipsType.LIGHT,
                label = viewModel.mapEventLabel(event)
            )
            binding?.layoutPost?.ecvSharePostEventChips?.setModel(eventChipsUiModel)
            binding?.layoutPost?.ecvSharePostEventChips?.visible()
            binding?.layoutPost?.tvShareType?.setText(R.string.event)
        } else {
            binding?.layoutPost?.tvSharePostTitle?.gone()
            binding?.layoutPost?.ecvSharePostEventChips?.gone()
            binding?.layoutPost?.tvShareType?.setText(R.string.post)
        }
    }

    private fun convertSecondsToDuration(videoDurationInSeconds: Int): String {
        val minutesInt = videoDurationInSeconds / 60
        var minutes: String = minutesInt.toString()
        if (minutes.length == 1) minutes = "0$minutes"
        var seconds: String = (videoDurationInSeconds % 60).toString()
        if (seconds.length == 1) seconds = "0$seconds"
        return String.format("%s:%s", minutes, seconds)
    }

    private fun showPostLayout() {
        val containerHeight = requireContext().displayHeight -
            bottomContainerInputHeight -
            bottomContainerMenuHeight -
            context.getToolbarHeight() -
            context.getStatusBarHeight()
        if (this.containerHeight == 0) this.containerHeight = containerHeight
        Timber.d("Bazaleev: viewPostHeight = $viewPostHeight, containerHeight = $containerHeight")
        binding?.layoutPost?.root?.newSize(MATCH_PARENT, containerHeight)
        setRootViewHeight(containerHeight)
        if (fl_bottom_container?.translationY != 0f) {
            animateBottomContainer(0)
        }
        binding?.layoutPost?.root?.visible()
        binding?.layoutList?.root?.gone()
        binding?.layoutEmpty?.root?.gone()
        binding?.pbShareBottomSheet?.gone()
        setSendButtonEnabled()
        commentSettingsBtn?.visible()
        when (whoCanComment) {
            WhoCanCommentPostEnum.EVERYONE -> hideIndicator()
            else -> showIndicator()
        }
        currentLayout = LAYOUT_REPOST_TO_ROADTAPE
    }

    private fun showGroupsLayout() {
        configureGroupAdapter()
        animateBottomContainer(bottomContainerInputHeight + bottomContainerMenuHeight + 1.dp)
        mainContainer?.setPaddingBottom(0)
        val containerHeight = requireContext().displayHeight -
            context.getToolbarHeight() -
            context.getStatusBarHeight()
        binding?.layoutList?.root?.newSize(MATCH_PARENT, containerHeight)
        animateRootViewHeight(containerHeight)
        binding?.layoutPost?.root?.gone()
        binding?.layoutList?.root?.visible()
        binding?.layoutEmpty?.root?.gone()
        binding?.layoutList?.rvSharePostList?.updatePadding(paddingStart = 20.dp, paddingEnd = 20.dp)
        binding?.pbShareBottomSheet?.gone()
        setSendButtonDisabled()

        commentSettingsBtn?.gone()
        hideIndicator()
    }

    private var containerHeight = 0
    private fun showFriendsLayout() {
        viewModel.clearSelectedItems()
        configureFriendsAdapter()
        mainContainer?.setPaddingBottom(bottomContainerInputHeight + bottomContainerMenuHeight + 1.dp)
        val containerHeight = requireContext().displayHeight -
            bottomContainerInputHeight -
            bottomContainerMenuHeight -
            context.getToolbarHeight() -
            context.getStatusBarHeight()
        if (this.containerHeight == 0) this.containerHeight = containerHeight
        Timber.d("Bazaleev: containerHeigh = $containerHeight, this.cont = ${this.containerHeight}")
        binding?.layoutList?.root?.newSize(MATCH_PARENT, containerHeight)
        if (mode == SharingDialogMode.SUGGEST_EVENT_SHARING) {
            setRootViewHeight(containerHeight)
        } else {
            animateRootViewHeight(containerHeight)
        }

        binding?.layoutPost?.root?.gone()
        binding?.layoutList?.root?.visible()
        binding?.layoutEmpty?.root?.gone()
        binding?.layoutList?.rvSharePostList?.updatePadding(paddingStart = 0, paddingEnd = 0)
        binding?.pbShareBottomSheet?.gone()
        setSendButtonDisabled()

        hideIndicator()
        commentSettingsBtn?.gone()

        if (shareType is ShareDialogType.ShareCommunity) {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun showEmptyGroups() {

        binding?.layoutEmpty?.ivEmptyListIcon?.setImageDrawable(context.getDrawableCompat(R.drawable.vector_icongroups))
        binding?.layoutEmpty?.tvEmptyListText?.text = getString(R.string.share_empty_group_placeholder)
        binding?.layoutEmpty?.tvEmptyListButton?.text = getString(R.string.find_group)
        binding?.layoutEmpty?.root?.updatePadding(paddingTop = 50.dp)
        binding?.layoutEmpty?.tvEmptyListButton?.setOnClickListener {
            callback.onShareFindGroup()
            dismiss()
        }
        mainContainer?.setPaddingBottom(0)
        binding?.layoutEmpty?.root?.measure(UNSPECIFIED, UNSPECIFIED)
        animateBottomContainer(bottomContainerInputHeight + bottomContainerMenuHeight + 1.dp)
        binding?.layoutEmpty?.root?.newSize(MATCH_PARENT, binding?.layoutEmpty?.root?.measuredHeight ?: 0 + 50.dp)
        binding?.layoutPost?.root?.gone()
        binding?.layoutList?.root?.gone()
        binding?.layoutEmpty?.root?.visible()
        binding?.pbShareBottomSheet?.gone()

        commentSettingsBtn?.gone()
    }

    @SuppressLint("SetTextI18n")
    private fun showEmptyFriends() {
        showPlaceHolder(
            R.drawable.ic_empty_friends,
            R.string.share_empty_friends_placeholder
        ) {
            callback.onShareFindFriend()
            shareDialogEvent.invoke(ShareBottomSheetEvent.OnClickFindFriendButton)
            dismiss()
        }
    }

    private fun showPlaceHolder(
        @DrawableRes drawable: Int,
        @StringRes text: Int,
        buttonClick: (() -> Unit)? = null,
    ) {
        binding?.layoutEmpty?.ivEmptyListIcon?.setImageDrawable(context.getDrawableCompat(drawable))
        binding?.layoutEmpty?.emptyListPlaceholderContainer?.setMargins(top = 44.dp)
        binding?.layoutEmpty?.tvEmptyListText?.text = getString(text)
        binding?.layoutEmpty?.tvEmptyListButton?.isAllCaps = false
        binding?.layoutEmpty?.tvEmptyListButton?.text = getString(R.string.find_friend)
        binding?.layoutEmpty?.root?.updatePadding(paddingTop = 50.dp, paddingBottom = 50.dp)
        if (buttonClick != null) {
            binding?.layoutEmpty?.tvEmptyListButton?.visible()
            binding?.layoutEmpty?.tvEmptyListButton?.setOnClickListener { buttonClick() }
        } else {
            binding?.layoutEmpty?.tvEmptyListButton?.gone()
        }
        mainContainer?.setPaddingBottom(bottomContainerInputHeight + bottomContainerMenuHeight)
        binding?.layoutEmpty?.root?.measure(UNSPECIFIED, UNSPECIFIED)
        binding?.layoutEmpty?.root?.newSize(
            MATCH_PARENT,
            binding?.layoutEmpty?.root?.measuredHeight?.plus(40.dp) ?: 0 + 50.dp
        )
        binding?.layoutPost?.root?.gone()
        binding?.layoutEmpty?.root?.visible()
        binding?.pbShareBottomSheet?.gone()
        tv_share_subtitle?.visible()
        tv_share_subtitle?.text = "${getString(R.string.selected)} 0/10"
        setSendButtonDisabled()
        commentSettingsBtn?.gone()
    }

    private fun hideEmptyFriends() {
        binding?.layoutEmpty?.root?.gone()
    }


    private fun calculateBottomContainer() {
        val bottomMenu = fl_bottom_container?.findViewById<ConstraintLayout>(R.id.cl_share_bottom_container)
        bottomMenu?.measure(UNSPECIFIED, UNSPECIFIED)
        hideIfNeedBottomNav { isHide ->
            bottomContainerMenuHeight = if (isHide) 0 else bottomMenu?.measuredHeight ?: 0
        }
        val bottomInput = fl_bottom_container?.findViewById<LinearLayout>(R.id.ll_share_input)
        bottomInput?.measure(UNSPECIFIED, UNSPECIFIED)
        bottomContainerInputHeight = bottomInput?.measuredHeight ?: 0
    }

    private fun animateRootViewHeight(viewContainerHeight: Int) {
        view?.post {
            try {
                val totalHeight = bottomContainerInputHeight +
                    bottomContainerMenuHeight +
                    viewContainerHeight +
                    context.getToolbarHeight()
                Timber.d("Bazaleev: totalHeight = $totalHeight")
                if (totalHeight != mainContainer?.height)
                    mainContainer.animateHeight(totalHeight, 400)
            } catch (e: Exception) {
                Timber.e(e)
                // ignore
            }
        }
    }

    private fun setRootViewHeight(viewContainerHeight: Int) {
        val totalHeight = bottomContainerInputHeight +
            bottomContainerMenuHeight +
            viewContainerHeight +
            context.getToolbarHeight()
        mainContainer?.layoutParams?.height = totalHeight
        mainContainer?.requestLayout()
    }

    private fun calculateMainContainerHeight(): Int {
        calculateBottomContainer()
        fl_share_container?.measure(UNSPECIFIED, UNSPECIFIED)
        viewPostHeight = fl_share_container?.measuredHeight ?: 0
        return bottomContainerInputHeight +
            bottomContainerMenuHeight +
            viewPostHeight +
            context.getToolbarHeight()
    }

    private fun setKeyboardHeightObserver() {
        keyboardHeightProvider?.observer = {
            adjustRootView(it)
        }
    }

    private fun isShareInputFocused(): Boolean {
        return et_share_input?.hasFocus() == true
    }

    private fun isShareSearchFocused(): Boolean {
        return binding?.layoutList?.etShareSearch?.hasFocus() == true && isChatTabSelected()
    }

    private fun changeInputHint(@StringRes newHintRes: Int) {
        binding?.layoutList?.etShareSearch?.hint = context?.getString(newHintRes)
    }

    private fun adjustRootView(keyboardHeight: Int) {
        rootView?.animate()?.cancel()

        val isInputFocused = isShareInputFocused() || isShareSearchFocused()
        if (keyboardHeight > 0 && isInputFocused) {
            suggestionsMenu?.setExtraPeekHeight(keyboardHeight + bottomContainerInputHeight + EXTRA_SPACE_TAG.dp, true)

            fl_bottom_container?.animate()
                ?.translationY(-(keyboardHeight - bottomContainerMenuHeight.toFloat()))
                ?.setDuration(150)
                ?.start()

            binding?.layoutList?.rvSharePostList?.setPaddingBottom(keyboardHeight - EXTRA_SPACE_TAG.dp)
        } else {
            when {
                binding?.layoutPost?.root?.isVisible ?: false -> {
                    fl_bottom_container?.animate()
                        ?.translationY(0f)
                        ?.setDuration(150)
                        ?.start()

                    suggestionsMenu?.setExtraPeekHeight(
                        bottomContainerMenuHeight + bottomContainerInputHeight + EXTRA_SPACE_TAG.dp,
                        true
                    )
                }

                isGroupTabSelected() -> {
                    fl_bottom_container?.translationY =
                        -(keyboardHeight - bottomContainerMenuHeight - bottomContainerInputHeight - 1.dp).toFloat()
                }

                else -> {
                    fl_bottom_container?.animate()
                        ?.translationY(0f)
                        ?.setDuration(100)
                        ?.start()

                    suggestionsMenu?.setExtraPeekHeight(
                        bottomContainerMenuHeight + bottomContainerInputHeight + EXTRA_SPACE_TAG.dp,
                        true
                    )
                }
            }

            binding?.layoutList?.rvSharePostList?.setPaddingBottom(0)
        }
    }

    private fun setButtonListeners() {
        mcv_share_chat?.setOnClickListener {
            selectChatTab()
        }
        mcv_share_road?.setOnClickListener {
            selectRoadTab()
            showPostLayout()
        }
        mcv_share_group?.setOnClickListener {
            selectGroupTab()
        }
        mcv_share_more?.setOnClickListener {
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
                            textAdded = !et_share_input?.text?.trim().isNullOrEmpty()
                        )
                    )
                }

                else -> {
                    analyticsPostShare?.let {
                        viewModel.logPostShare(
                            analyticsPostShare = it.copy(
                                whereSent = AmplitudePropertyWhereSent.OUTSIDE,
                                textAdded = !et_share_input?.text?.trim().isNullOrEmpty()
                            )
                        )
                    }
                }
            }
            shareMore()
        }
        iv_close_share?.setOnClickListener {
            if (currentLayout == LAYOUT_REPOST_TO_GROUP) {
                selectChatTab()
                showFriendsLayout()
                if (fl_bottom_container?.translationY != 0f) {
                    animateBottomContainer(0)
                }
            } else {
                bottomSheetCloseUtil.onCloseButtonPressed()
                dismiss()
            }
        }

        commentSettingsBtn?.setOnClickListener {
            val where = when {
                isRoadTabSelected() -> AmplitudePropertyWhere.SELF_FEED
                isGroupTabSelected() -> AmplitudePropertyWhere.COMMUNITY
                else -> AmplitudePropertyWhere.OTHER
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

    private fun selectRoadTab() {
        if (!isRoadTabSelected()) {
            mcv_share_chat?.strokeWidth = 0
            mcv_share_road?.strokeWidth = 2.dp
            mcv_share_group?.strokeWidth = 0
            invalidateMenuButtons()
            tv_share_chat?.setTextColor(context.getColorCompat(R.color.ui_gray))
            tv_share_road?.setTextColor(context.getColorCompat(R.color.ui_purple))
            tv_share_group?.setTextColor(context.getColorCompat(R.color.ui_gray))
            tv_share_title?.text = getDialogTitle()
            tv_share_subtitle?.gone()
            selectedCommunity = null
            selectedUsers.clear()
            changeInputHint(R.string.share_search)

            analyticsPostShare?.let {
                analyticsPostShare = it.copy(whereSent = AmplitudePropertyWhereSent.SELF_FEED)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun selectChatTab() {
        if (!isChatTabSelected()) {
            mcv_share_chat?.strokeWidth = 2.dp
            mcv_share_road?.strokeWidth = 0
            mcv_share_group?.strokeWidth = 0
            invalidateMenuButtons()
            tv_share_chat?.setTextColor(context.getColorCompat(R.color.ui_purple))
            tv_share_road?.setTextColor(context.getColorCompat(R.color.ui_gray))
            tv_share_group?.setTextColor(context.getColorCompat(R.color.ui_gray))
            binding?.layoutList?.etShareSearch?.setText("")
            tv_share_title?.text = getDialogTitle()
            tv_share_subtitle?.visible()
            tv_share_subtitle?.text = "${getString(R.string.selected)} 0/10"
            selectedCommunity = null

            showFriendsLayout()
            changeInputHint(R.string.share_search)

            analyticsPostShare?.let {
                analyticsPostShare = it.copy(whereSent = AmplitudePropertyWhereSent.CHAT)
            }
        }
    }

    private fun selectGroupTab() {
        if (!isGroupTabSelected()) {
            suggestionsMenu?.dismissMenu()
            mcv_share_chat?.strokeWidth = 0
            mcv_share_road?.strokeWidth = 0
            mcv_share_group?.strokeWidth = 2.dp
            invalidateMenuButtons()
            tv_share_chat?.setTextColor(context.getColorCompat(R.color.ui_gray))
            tv_share_road?.setTextColor(context.getColorCompat(R.color.ui_gray))
            tv_share_group?.setTextColor(context.getColorCompat(R.color.ui_purple))
            binding?.layoutList?.etShareSearch?.setText("")
            tv_share_subtitle?.gone()
            selectedUsers.clear()
            showGroupsLayout()
            changeInputHint(R.string.share_search)

            analyticsPostShare?.let {
                analyticsPostShare = it.copy(whereSent = AmplitudePropertyWhereSent.COMMUNITY)
            }
        }
    }

    private fun invalidateMenuButtons() {
        mcv_share_chat?.invalidate()
        mcv_share_road?.invalidate()
        mcv_share_group?.invalidate()
        mcv_share_more?.invalidate()
    }

    private fun setSendButtonEnabled() {
        btn_share_send?.isEnabled = true
        btn_share_send?.setColorFilter(context.getColorCompat(R.color.colorTransparent))
    }

    private fun setSendButtonDisabled() {
        btn_share_send?.isEnabled = false
        btn_share_send?.setColorFilter(context.getColorCompat(R.color.transparent_white))
    }

    private fun configureGroupAdapter() {
        if (currentLayout == LAYOUT_REPOST_TO_GROUP) return
        groupsAdapter?.clear()
        friendsAdapter = null
        groupsAdapter = ShareGroupAdapter()
        groupsAdapter?.groupRepostId = groupId
        groupsAdapter?.onClick = {
            whoCanComment = WhoCanCommentPostEnum.EVERYONE
            selectedCommunity = it
            tv_share_title?.text = getDialogTitle(it?.name ?: "")
            val imm: InputMethodManager =
                requireView().context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
            showPostLayout()
        }
        if (!groupsAdapter!!.hasObservers())
            groupsAdapter!!.setHasStableIds(true)
        binding?.layoutList?.rvSharePostList?.adapter = groupsAdapter

        initGroupsPagination()
        viewModel.getMyGroups(isShowLoading = false, isShowError = false, startIndex = 0, limit = 50)
        currentLayout = LAYOUT_REPOST_TO_GROUP
    }

    private fun configureGroupSearchAdapter() {
        groupsAdapter?.clear()
        friendsAdapter = null
        groupsAdapter = ShareGroupAdapter()
        groupsAdapter?.groupRepostId = groupId
        groupsAdapter?.onClick = {
            selectedCommunity = it
            tv_share_title?.text = getDialogTitle(it?.name ?: "")
            val imm: InputMethodManager =
                requireView().context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
            showPostLayout()
        }
        if (!groupsAdapter!!.hasObservers())
            groupsAdapter!!.setHasStableIds(true)
        binding?.layoutList?.rvSharePostList?.adapter = groupsAdapter

        initGroupsSearchPagination()
        viewModel.searchGroupRequest(binding?.layoutList?.etShareSearch?.text.toString(), offset = 0, limit = 50)
        currentLayout = LAYOUT_REPOST_TO_GROUP_SEARCH
    }

    private fun initGroupsSearchPagination() {
        binding?.layoutList?.rvSharePostList?.clearOnScrollListeners()
        binding?.layoutList?.rvSharePostList?.let { rvSharePostList ->
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
                        binding?.layoutList?.etShareSearch?.text.toString(), offset = groupsAdapter?.itemCount
                            ?: 0, limit = 20
                    )
                }
            )
        }
    }

    private fun initGroupsPagination() {
        binding?.layoutList?.rvSharePostList?.clearOnScrollListeners()
        binding?.layoutList?.rvSharePostList?.let { rvSharePostList ->
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
                            ?: 0, limit = 50
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
                    //showGroupsLayout()
                    groupsAdapter?.addData(data)
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
        friendsAdapter = ShareItemAdapter(shareItemsCallback)
        binding?.layoutList?.rvSharePostList?.adapter = friendsAdapter
        binding?.layoutList?.rvSharePostList?.itemAnimator = null
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
        friendsAdapter = ShareItemAdapter(shareItemsCallback)
        binding?.layoutList?.rvSharePostList?.adapter = friendsAdapter
        initFriendsSearchPagination()
        viewModel.searchShareItems(binding?.layoutList?.etShareSearch?.text.toString())
        currentLayout = LAYOUT_REPOST_TO_CHAT_SEARCH
    }

    private fun initFriendsSearchPagination() {
        binding?.layoutList?.rvSharePostList?.clearOnScrollListeners()
        binding?.layoutList?.rvSharePostList?.let { rvSharePostList ->
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
        binding?.layoutList?.rvSharePostList?.clearOnScrollListeners()
        binding?.layoutList?.rvSharePostList?.let { rvSharePostList ->
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
        viewModel.liveShareItems.observe(viewLifecycleOwner) {
            friendsAdapter?.submitList(it)
        }
    }

    private fun initCheckedCountLive() {
        viewModel.liveCheckedCount.observe(viewLifecycleOwner) {
            tv_share_title?.text = getDialogTitle()
            tv_share_subtitle?.text = "${getString(R.string.selected)} ${it}/10"
            if (it > 0) setSendButtonEnabled()
            else setSendButtonDisabled()
        }
    }

    private fun setSearchInputFocusListener() {
        binding?.layoutList?.etShareSearch?.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        binding?.layoutList?.etShareSearch?.click {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }


    private fun setSearchInputTextChangeListener() {
        binding?.layoutList?.etShareSearch?.addTextChangedListener(
            afterTextChanged = { text ->
                analyticsPostShare?.let {
                    if (!text.isNullOrEmpty()) {
                        analyticsPostShare = it.copy(search = true)
                    }
                }

                if (bottomSheetBehavior?.state != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                }
                if (text.isNullOrEmpty()) {
                    binding?.layoutList?.ivShareSearchClear?.invisible()
                } else {
                    binding?.layoutList?.ivShareSearchClear?.visible()
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
        )
        binding?.layoutList?.ivShareSearchClear?.setOnClickListener {
            binding?.layoutList?.etShareSearch?.setText("")
            binding?.layoutList?.ivShareSearchClear?.invisible()
        }
    }


    private fun setSendRepostClickListener() {
        btn_share_send?.setOnClickListener {
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
                            textAdded = !et_share_input?.text?.trim().isNullOrEmpty()
                        )
                    )
                }

                else -> {
                    analyticsPostShare?.let {
                        viewModel.logPostShare(
                            analyticsPostShare = it.copy(textAdded = !et_share_input?.text?.trim().isNullOrEmpty())
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
            else -> {}
        }
    }

    private fun sendButtonSharePostConfig() {
        btn_share_send?.isEnabled = false
        if (selectedCommunity != null) {
            selectedCommunity?.let { selectedGroup ->
                shareToGroup(selectedGroup, post)
            }
        } else {
            val message = et_share_input?.text?.trim() ?: ""
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
        val message = et_share_input?.text?.trim() ?: ""
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
        val message = et_share_input?.text?.trim().toString()
        repostViewModel.shareUserProfile(userId, userIds, roomIds, message)
    }

    private fun shareMoment(moment: MomentItemUiModel) {
        val userIds = viewModel.getSelectedItemsUser()
        val roomIds = viewModel.getSelectedItemsRooms()
        val message = et_share_input?.text?.trim().toString()
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
        val message = et_share_input?.text?.trim().toString()
        repostViewModel.shareCommunity(groupId, userIds, roomIds, message)
    }

    private fun forwardChatMessage(
        messageId: String,
        roomId: Long,
    ) {
        val userIds = viewModel.getSelectedItemsUser()
        val roomIds = viewModel.getSelectedItemsRooms()
        val selectedGroupsCount = viewModel.getSelectedGroupsCount()
        val message = et_share_input?.text?.trim().toString()
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

    private fun hideIfNeedBottomNav(block: (isHide: Boolean) -> Unit) {
        if (shareType is ShareDialogType.MessageForwarding) {
            block.invoke(true)
        } else {
            block.invoke(false)
        }
    }

    companion object {
        const val LAYOUT_REPOST_TO_CHAT = 0
        const val LAYOUT_REPOST_TO_GROUP = 1
        const val LAYOUT_REPOST_TO_ROADTAPE = 2

        const val LAYOUT_REPOST_TO_CHAT_SEARCH = 3
        const val LAYOUT_REPOST_TO_GROUP_SEARCH = 4
    }
}
