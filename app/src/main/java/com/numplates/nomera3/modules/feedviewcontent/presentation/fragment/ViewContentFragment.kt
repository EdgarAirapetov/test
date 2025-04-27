package com.numplates.nomera3.modules.feedviewcontent.presentation.fragment

import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.meera.core.extensions.click
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.lightVibrate
import com.meera.core.utils.checkAppRedesigned
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentViewContentBinding
import com.numplates.nomera3.modules.auth.util.needAuth
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegate
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegateImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyMenuAction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.createAmplitudeReactionsParams
import com.numplates.nomera3.modules.feed.ui.adapter.ContentActionBar
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feedviewcontent.presentation.adapter.ViewContentAdapter
import com.numplates.nomera3.modules.feedviewcontent.presentation.custom.ViewContentGestures
import com.numplates.nomera3.modules.feedviewcontent.presentation.data.ContentGroupUiModel
import com.numplates.nomera3.modules.feedviewcontent.presentation.data.ContentItemUiModel
import com.numplates.nomera3.modules.feedviewcontent.presentation.dialog.ContentBottomSheetDialogListener
import com.numplates.nomera3.modules.feedviewcontent.presentation.dialog.ViewContentAuthorDialog
import com.numplates.nomera3.modules.feedviewcontent.presentation.dialog.ViewContentAuthorDialog.Companion.showDialog
import com.numplates.nomera3.modules.feedviewcontent.presentation.dialog.ViewContentWatcherDialog
import com.numplates.nomera3.modules.feedviewcontent.presentation.dialog.ViewContentWatcherDialog.Companion.showDialog
import com.numplates.nomera3.modules.feedviewcontent.presentation.mapper.toContentItemUiModel
import com.numplates.nomera3.modules.feedviewcontent.presentation.viewevents.ViewContentEvent
import com.numplates.nomera3.modules.feedviewcontent.presentation.viewmodel.ViewContentViewModel
import com.numplates.nomera3.modules.feedviewcontent.presentation.viewstates.ViewContentMessageState
import com.numplates.nomera3.modules.feedviewcontent.presentation.viewstates.ViewContentState
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingAnimationPlayListener
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingReaction
import com.numplates.nomera3.modules.reaction.ui.data.ReactionSource
import com.numplates.nomera3.modules.reaction.ui.getDefaultReactionContainer
import com.numplates.nomera3.modules.reaction.ui.util.reactionCount
import com.numplates.nomera3.modules.reactionStatistics.ui.MeeraReactionsStatisticsBottomSheetFragment
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsEntityType
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsStatisticsBottomSheetFragment
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_PHOTO_WHERE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_POST_ORIGIN
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import com.numplates.nomera3.presentation.view.ui.bottomMenu.ReactionsStatisticBottomMenu

const val ARG_VIEW_CONTENT_DATA = "ARG_VIEW_CONTENT_DATA"
const val KEY_VIEW_PAGER_BLOCK_TOUCHES = "KEY_VIEW_PAGER_BLOCK_TOUCHES"
const val KEY_VIEW_PAGER_IS_BLOCKED = "KEY_VIEW_PAGER_IS_BLOCKED"

class ViewContentFragment :
    BaseFragmentNew<FragmentViewContentBinding>(),
    ContentBottomSheetDialogListener,
    MeeraMenuBottomSheet.Listener,
    SaveMediaFileDelegate by SaveMediaFileDelegateImpl() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentViewContentBinding
        get() = FragmentViewContentBinding::inflate

    private val viewModel by viewModels<ViewContentViewModel> { App.component.getViewModelFactory() }
    private var viewContentGestures: ViewContentGestures? = null
    private var adapter: ViewContentAdapter? = null
    private var currentItem: ContentItemUiModel? = null
    private var currentGroup: ContentGroupUiModel? = null
    private var post: PostUIEntity? = null
    private var originEnum: DestinationOriginEnum? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initClickListeners()
        initViewPagerBlockListener()
        initGestures()
        observeViewContentEvents()
        initData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewContentGestures?.destroyGesturesInterceptor()
    }

    override fun onClickDownloadContent() = downloadImage()

    override fun onClickSubscribePost() {
        viewModel.onTriggerViewEvent(ViewContentEvent.SubscribePost(currentGroup?.postId))
        logAnalytic(AmplitudePropertyMenuAction.POST_FOLLOW)
    }

    override fun onClickUnsubscribePost() {
        viewModel.onTriggerViewEvent(ViewContentEvent.UnsubscribePost(currentGroup?.postId))
        logAnalytic(AmplitudePropertyMenuAction.POST_UNFOLLOW)
    }

    override fun onClickComplainOnPost() {
        viewModel.onTriggerViewEvent(ViewContentEvent.AddPostComplaint(currentGroup?.postId))
        logAnalytic(AmplitudePropertyMenuAction.POST_REPORT)
    }

    override fun onCancelByUser(menuTag: String?) {
        logAnalytic(AmplitudePropertyMenuAction.CANCEL)
    }

    private fun initData() {
        post = arguments?.getParcelable(ARG_VIEW_CONTENT_DATA) as? PostUIEntity
        arguments?.getSerializable(ARG_PHOTO_WHERE)?.let {
            val where = it as AmplitudePropertyWhere
            viewModel.onTriggerViewEvent(ViewContentEvent.SendAnalytic(post, where))
        }
        originEnum = arguments?.getSerializable(ARG_POST_ORIGIN) as? DestinationOriginEnum
        logAnalytic()
        viewModel.onTriggerViewEvent(ViewContentEvent.GetContent(post))
    }

    private fun initViews() {
        adapter = ViewContentAdapter(
            fragment = this,
            fragmentManager = childFragmentManager,
            viewContentReactionsListener = object : ViewContentReactionsListener {
                override fun onReactionBottomSheetShow() = needAuth() {
                    if (viewModel.getFeatureTogglesContainer().detailedReactionsForPostFeatureToggle.isEnabled) {
                        checkAppRedesigned(
                            isRedesigned = {
                                MeeraReactionsStatisticsBottomSheetFragment.getInstance(
                                    entityId = post?.postId ?: return@checkAppRedesigned,
                                    entityType = ReactionsEntityType.POST
                                ).show(childFragmentManager)
                            },
                            isNotRedesigned = {
                                ReactionsStatisticsBottomSheetFragment.getInstance(
                                    entityId = post?.postId ?: return@checkAppRedesigned,
                                    entityType = ReactionsEntityType.POST
                                ).show(childFragmentManager)
                            }
                        )

                    } else {
                        val reactions = post?.reactions ?: return@needAuth
                        val sortedReactions = reactions.sortedByDescending { reactionEntity -> reactionEntity.count }
                        val menu = ReactionsStatisticBottomMenu(context)
                        menu.addTitle(R.string.reactions, sortedReactions.reactionCount())
                        sortedReactions.forEachIndexed { index, value ->
                            menu.addReaction(value, index != sortedReactions.size - 1)
                        }
                        menu.show(childFragmentManager)
                    }
                }

                override fun onReactionLongClicked(
                    showPoint: Point,
                    reactionTip: TextView,
                    viewsToHide: List<View>,
                    reactionHolderViewId: ContentActionBar.ReactionHolderViewId
                ) {
                    post?.apply {
                        val reactionsParams = createAmplitudeReactionsParams(originEnum)
                        act.getReactionBubbleViewController().showReactionBubble(
                            reactionSource = ReactionSource.Post(
                                postId = postId,
                                reactionHolderViewId = reactionHolderViewId,
                                originEnum = originEnum
                            ),
                            reactionsParams = reactionsParams,
                            showPoint = showPoint,
                            viewsToHide = viewsToHide,
                            reactionTip = reactionTip,
                            currentReactionsList = reactions ?: emptyList(),
                            contentActionBarType = ContentActionBar.ContentActionBarType.DARK,
                            containerInfo = act.getDefaultReactionContainer(),
                            postedAt = date
                        )
                    }
                }

                override fun onReactionRegularClicked(reactionHolderViewId: ContentActionBar.ReactionHolderViewId) {
                    post?.apply {
                        val reactionsParams = createAmplitudeReactionsParams(originEnum)
                        act.getReactionBubbleViewController().onSelectDefaultReaction(
                            reactionSource = ReactionSource.Post(
                                postId = postId,
                                reactionHolderViewId = reactionHolderViewId,
                                originEnum = originEnum
                            ),
                            reactionsParams = reactionsParams,
                            currentReactionsList = reactions ?: emptyList(),
                            isShouldVibrate = false
                        )
                    }
                }

                override fun onFlyingAnimationInitialized(flyingReaction: FlyingReaction) {
                    val container = act.getRootView() as? ViewGroup
                    container?.addView(flyingReaction)
                    flyingReaction.startAnimationFlying()
                    flyingReaction.setFlyingAnimationPlayListener(object : FlyingAnimationPlayListener {
                        override fun onFlyingAnimationPlayed(playedFlyingReaction: FlyingReaction) {
                            container?.removeView(playedFlyingReaction)
                        }
                    })
                }
            })

        binding?.vpViewContentContainer?.apply {
            adapter = this@ViewContentFragment.adapter
        }
    }

    private fun initGestures() {
        viewContentGestures = ViewContentGestures().apply {
            initGesturesInterceptor(
                extendedGestureOverlayView = binding?.govViewContentGestureInterceptor,
                viewPager2 = binding?.vpViewContentContainer
            )
            onVerticalSwipe = { activity?.onBackPressed() }
        }
    }

    private fun initClickListeners() {
        binding?.ivViewContentBackArrow?.click { activity?.onBackPressed() }
        binding?.ivViewContentMenu?.click { openContentMenu() }
    }

    private fun downloadImage() {
        val imageUrl = currentItem?.contentUrl ?: return
        saveImageOrVideoFile(
            imageUrl = imageUrl,
            act = act,
            viewLifecycleOwner = viewLifecycleOwner,
            successListener = {
                showCommonSuccessMessage(R.string.image_saved)
                logAnalytic(AmplitudePropertyMenuAction.SAVE)
            }
        )
    }

    private fun openContentMenu() {
        requireContext()?.lightVibrate()
        val data = currentItem ?: return
        needAuth {
            if (viewModel.getUid() == data.user?.userId) openAuthorDialog() else openWatcherDialog()
        }
    }

    private fun openAuthorDialog() {
        ViewContentAuthorDialog(context).also { dialog ->
            dialog.createAuthorDialog()
            dialog.showDialog(childFragmentManager)
        }
    }

    private fun openWatcherDialog() {
        ViewContentWatcherDialog(context).also { dialog ->
            dialog.createWatcherDialog(
                isEventPost = currentGroup?.isEventPost.isTrue(),
                isPostSubscribed = currentGroup?.isPostSubscribed ?: false
            )
            dialog.showDialog(childFragmentManager)
        }
    }

    private fun initViewPagerBlockListener() {
        childFragmentManager.setFragmentResultListener(
            KEY_VIEW_PAGER_BLOCK_TOUCHES,
            viewLifecycleOwner
        ) { _, bundle ->
            val blocked = bundle.getBoolean(KEY_VIEW_PAGER_IS_BLOCKED)
            viewContentGestures?.isTouchesBlocked = blocked
        }
    }

    private fun observeViewContentEvents() {
        viewModel.getViewContentState().observe(viewLifecycleOwner) { event ->
            when (event) {
                is ViewContentState.ContentDataReceived -> {
                    currentGroup = event.contentGroup
                    adapter?.submitList(event.contentGroup.contentList)
                    initContentItem()
                }

                is ViewContentState.PostSubscriptionUpdated -> currentGroup = event.contentGroup
            }
        }
        viewModel.getViewContentMessageState().observe(viewLifecycleOwner) { event ->
            when (event) {
                is ViewContentMessageState.ShowSuccess -> showCommonSuccessMessage(event.message)
                is ViewContentMessageState.ShowError -> showCommonError(event.error)
            }
        }
        viewModel.getLiveReactions().observe(viewLifecycleOwner) { postUpdate ->
            post = post?.updateModel(postUpdate)
            post?.apply {
                val currentFragment = adapter?.getFragmentByItemId(itemId = postId)
                currentFragment?.updateActionBar(
                    toContentItemUiModel()
                )
            }
        }
    }

    private fun initContentItem() {
        val contentList = adapter?.getCurrentList() ?: return
        if (contentList.isNotEmpty()) {
            val index = 0
            currentItem = adapter?.getItemFromPosition(index)
            binding?.vpViewContentContainer?.setCurrentItem(index, false)
        }
    }

    private fun logAnalytic(action: AmplitudePropertyMenuAction? = null) {
        arguments?.getSerializable(ARG_PHOTO_WHERE)?.let {
            val where = it as AmplitudePropertyWhere
            viewModel.onTriggerViewEvent(ViewContentEvent.SendAnalytic(post, where, action))
        }
    }
}
