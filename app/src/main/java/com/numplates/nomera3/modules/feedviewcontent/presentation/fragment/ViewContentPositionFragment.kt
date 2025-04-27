package com.numplates.nomera3.modules.feedviewcontent.presentation.fragment

import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.github.chrisbanes.photoview.PhotoView
import com.meera.core.extensions.loadGlideFullSize
import com.meera.core.extensions.onMeasured
import com.numplates.nomera3.databinding.FragmentViewContentPositionBinding
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.feed.ui.adapter.ContentActionBar
import com.numplates.nomera3.modules.feedviewcontent.presentation.data.ContentItemUiModel
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingReaction
import com.numplates.nomera3.presentation.router.BaseFragmentNew

const val ARG_CONTENT_ITEM = "ARG_CONTENT_ITEM"
private const val DEFAULT_MEDIUM_SCALE = 1.75f
private const val DEFAULT_MAXIMUM_SCALE = 3f

class ViewContentPositionFragment : BaseFragmentNew<FragmentViewContentPositionBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentViewContentPositionBinding
        get() = FragmentViewContentPositionBinding::inflate

    private var currentItem: ContentItemUiModel? = null
    private var viewContentReactionsListener: ViewContentReactionsListener? = null

    fun bind(viewContentReactionsListener: ViewContentReactionsListener) {
        this.viewContentReactionsListener = viewContentReactionsListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initContentData()
        initListeners()
        setupActionBar()
    }

    fun updateActionBar(contentItemUiModel: ContentItemUiModel) {
        this.currentItem = contentItemUiModel

        val params = ContentActionBar.Params(
            isEnabled = true,
            reactions = contentItemUiModel.postReactions ?: emptyList(),
            userAccountType = AccountTypeEnum.ACCOUNT_TYPE_VIP.value,
            commentCount = 0,
            repostCount = 0,
            commentsIsHide = true,
            isMoment = false
        )

        binding?.viewContentPostActionBar?.apply {
            update(
                params = params,
                reactionHolderViewId = getReactionHolderViewId()
            )
        }
    }

    private fun toggleTouchesInViewPager(isBlocked: Boolean) {
        setFragmentResult(
            requestKey = KEY_VIEW_PAGER_BLOCK_TOUCHES,
            result = bundleOf(
                KEY_VIEW_PAGER_IS_BLOCKED to isBlocked
            )
        )
    }

    private fun initListeners() {
        binding?.ivViewContentImage?.setOnMatrixChangeListener {
            val scale = binding?.ivViewContentImage?.scale ?: return@setOnMatrixChangeListener
            blockReducingOriginalScale(scale)
            toggleTouchesInViewPager(scale > 1f)
        }
    }

    private fun setupActionBar() {
        val params = ContentActionBar.Params(
            isEnabled = true,
            reactions = currentItem?.postReactions ?: emptyList(),
            userAccountType = AccountTypeEnum.ACCOUNT_TYPE_VIP.value,
            commentCount = 0,
            repostCount = 0,
            commentsIsHide = true,
            isMoment = false
        )
        binding?.viewContentPostActionBar?.init(params, ReactionListener(), false)
    }

    /**
     * Если масштаб фото меньше исходного, начинает делать сброс матрицы.
     * Необходимо для блокировки исходного масштаба фото.
     */
    private fun blockReducingOriginalScale(scale: Float) {
        if (scale < 1f) binding?.ivViewContentImage?.attacher?.update()
    }

    private fun initContentData() {
        val args = arguments ?: run {
            activity?.onBackPressed()
            return
        }
        currentItem = args.getParcelable(ARG_CONTENT_ITEM)
        initViews()
    }

    private fun initViews() {
        initContent()
    }

    private fun initContent() {
        binding?.ivViewContentImage?.apply {
            currentItem?.let { initImageScaleThresholds(this, it) }
            loadGlideFullSize(currentItem?.contentUrl)
        }
    }

    private fun initImageScaleThresholds(photoView: PhotoView, item: ContentItemUiModel) {
        if (item.enableZoomToFit) {
            photoView.onMeasured {
                val imageAspect = (item.aspect.takeIf { it != 0.0 } ?: 1.0).toFloat()
                val viewAspect = photoView.width.toFloat() / photoView.height
                val aspectRatio = if (viewAspect > imageAspect) viewAspect / imageAspect else imageAspect / viewAspect
                val zoomToFitScale = aspectRatio.coerceAtLeast(DEFAULT_MEDIUM_SCALE)
                val maxZoomScale = DEFAULT_MAXIMUM_SCALE / DEFAULT_MEDIUM_SCALE * zoomToFitScale
                photoView.maximumScale = maxZoomScale
                photoView.mediumScale = zoomToFitScale
            }
        } else {
            photoView.maximumScale = DEFAULT_MAXIMUM_SCALE
            photoView.mediumScale = DEFAULT_MEDIUM_SCALE
        }
    }

    inner class ReactionListener : ContentActionBar.Listener {
        override fun onReactionButtonDisabledClick() = Unit

        override fun onCommentsClick() = Unit

        override fun onRepostClick() = Unit

        override fun onReactionBadgeClick() {
            viewContentReactionsListener?.onReactionBottomSheetShow()
        }

        override fun onReactionLongClick(
            showPoint: Point,
            reactionTip: TextView,
            viewsToHide: List<View>,
            reactionHolderViewId: ContentActionBar.ReactionHolderViewId
        ) {
            viewContentReactionsListener?.onReactionLongClicked(
                showPoint,
                reactionTip,
                viewsToHide,
                reactionHolderViewId
            )
        }

        override fun onFlyingAnimationInitialized(flyingReaction: FlyingReaction) {
            viewContentReactionsListener?.onFlyingAnimationInitialized(flyingReaction)
        }

        override fun onReactionRegularClick(reactionHolderViewId: ContentActionBar.ReactionHolderViewId) {
            viewContentReactionsListener?.onReactionRegularClicked(reactionHolderViewId)
        }
    }
}
