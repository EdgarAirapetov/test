package com.numplates.nomera3.presentation.view.adapter

import android.graphics.Point
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.viewpager.widget.PagerAdapter
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView.ZOOM_FOCUS_FIXED
import com.github.piasy.biv.loader.ImageLoader
import com.github.piasy.biv.view.BigImageView
import com.github.piasy.biv.view.GlideImageViewFactory
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.uikit.widgets.blur.UiKitRealtimeBlurView
import com.meera.uikit.widgets.buttons.UiKitButton
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.feed.ui.adapter.MeeraContentActionBar
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.entity.UIPostUpdate
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingReaction
import com.numplates.nomera3.modules.reaction.ui.mapper.toMeeraContentActionBarParams
import com.numplates.nomera3.modules.userprofile.ui.model.PhotoModel
import com.numplates.nomera3.presentation.view.fragments.profilephoto.MeeraProfilePhotoReactionsListener
import java.io.File

class MeeraTouchImageAdapterNew(private val isOwnPhotoProfile: Boolean) : PagerAdapter(), InfinityAdapter {

    companion object {
        const val TAG_PAGE = "ViewPagerPage"
    }

    var callback: TouchImageAdapterInteraction? = null
    var isDeleted = false
    var profilePhotoReactionsListener: MeeraProfilePhotoReactionsListener? = null

    private var avatarsList = mutableListOf<PhotoModel>()
    val pageSize = 10

    override fun getIconResId(index: Int): Int {
        return R.drawable.avatar_placeholder
    }

    override fun getCount(): Int {
        return avatarsList.size
    }

    override fun getRealCount(): Int {
        return avatarsList.size
    }

    override fun getGallery(): List<PhotoModel> {
        return avatarsList
    }

    fun loadMore(elements: List<PhotoModel>) {
        avatarsList.addAll(elements)
        notifyDataSetChanged()
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }


    override fun instantiateItem(container: ViewGroup, position: Int): View {
        val view = LayoutInflater.from(container.context).inflate(R.layout.meera_item_big_image, container, false)
        view.tag = TAG_PAGE + position
        setupPage(view, position)
        container.addView(view)
        return view
    }

    fun setupPage(view: View, position: Int) {
        val img = view.findViewById<BigImageView>(R.id.big_image_profile_photo)
        val llSensitive = view.findViewById<LinearLayout>(R.id.ll_sensitive)
        val bvSensitive = view.findViewById<UiKitRealtimeBlurView>(R.id.bv_sensitive)
        val btnShow = view.findViewById<UiKitButton>(R.id.btn_show)
        val actionBar = view.findViewById<MeeraContentActionBar>(R.id.post_action_bar)


        img.setOnClickListener {
            callback?.onImageClicked()
        }
        img.setOnLongClickListener {
            callback?.onImageLongClick(avatarsList.get(position).imageUrl)
            true
        }
        img.setImageLoaderCallback(TapToZoomCallback(img, avatarsList.get(position)))
        img.setImageViewFactory(GlideImageViewFactory())
        avatarsList.let { listPhoto ->
            listPhoto[position].let { urlImg ->
                urlImg.imageUrl.let {
                    img.showImage(Uri.parse(it))
                }
            }
        }
        setupActionBar(avatarsList[position].post, actionBar)


        if (isAdultContent(avatarsList.get(position))) {
            llSensitive.visible()
            bvSensitive.visible()
            actionBar?.gone()

            btnShow.setThrottledClickListener {
                avatarsList.get(position).showed = true
                llSensitive.gone()
                bvSensitive.gone()
                actionBar?.visible()
                img.ssiv.isZoomEnabled = true
            }
        } else {
            bvSensitive.gone()
            llSensitive.gone()
        }
    }


    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view === obj
    }

    override fun getPreviewUrl(index: Int): String? {
        avatarsList.let { listPhoto ->
            listPhoto[index].let {
                return it.imageUrl
            }
        }
        return ""
    }

    private fun isAdultContent(photoModel: PhotoModel) =
        photoModel.isAdult && isOwnPhotoProfile.not() && photoModel.showed.not()

    private fun setupActionBar(post: PostUIEntity?, actionBar: MeeraContentActionBar?) {
        val existPost = post ?: run {
            actionBar?.isVisible = false
            return
        }
        actionBar?.isVisible = true
        val params = existPost.toMeeraContentActionBarParams()
            .copy(commentsIsHide = true, userAccountType = AccountTypeEnum.ACCOUNT_TYPE_VIP.value)
        actionBar?.init(params, ReactionListener(existPost.postId), false)
    }

    private fun updateActionBar(
        post: PostUIEntity,
        actionBar: MeeraContentActionBar?,
        reactionHolderViewId: MeeraContentActionBar.ReactionHolderViewId
    ) {
        val params = post.toMeeraContentActionBarParams()
            .copy(commentsIsHide = true, userAccountType = AccountTypeEnum.ACCOUNT_TYPE_VIP.value)

        actionBar?.update(
            params = params,
            reactionHolderViewId = reactionHolderViewId
        )
    }

    inner class ReactionListener(private val postId: Long) : MeeraContentActionBar.Listener {
        override fun onReactionButtonDisabledClick() = Unit

        override fun onCommentsClick() = Unit

        override fun onRepostClick() = Unit

        override fun onReactionBadgeClick() {
            val post = getPost() ?: return

            profilePhotoReactionsListener?.onReactionBottomSheetShow(post)
        }

        override fun onReactionLongClick(
            showPoint: Point,
            reactionTip: TextView,
            viewsToHide: List<View>,
            reactionHolderViewId: MeeraContentActionBar.ReactionHolderViewId
        ) {
            val post = getPost() ?: return

            profilePhotoReactionsListener?.onReactionLongClicked(
                post,
                showPoint,
                reactionTip,
                viewsToHide,
                reactionHolderViewId
            )
        }

        override fun onReactionClickToShowScreenAnimation(
            reactionEntity: ReactionEntity,
            anchorViewLocation: Pair<Int, Int>
        ) {
            profilePhotoReactionsListener?.onReactionClickToShowScreenAnimation(reactionEntity, anchorViewLocation)
        }

        override fun onFlyingAnimationInitialized(flyingReaction: FlyingReaction) {
            profilePhotoReactionsListener?.onFlyingAnimationInitialized(flyingReaction)
        }

        override fun onReactionRegularClick(reactionHolderViewId: MeeraContentActionBar.ReactionHolderViewId) {
            val post = getPost() ?: return

            profilePhotoReactionsListener?.onReactionRegularClicked(post, reactionHolderViewId)
        }

        private fun getPost(): PostUIEntity? {
            return avatarsList.find { it.post?.postId == postId }?.post
        }
    }

    fun updateReactionsData(payload: UIPostUpdate): Int {
        val position = avatarsList.indexOfFirst { it.post?.postId == payload.postId }
        if (position == -1) return -1
        avatarsList[position] = avatarsList[position].copy(post = avatarsList[position].post?.updateModel(payload))
        return position
    }

    fun updateReactionsView(view: View, position: Int, postUpdate: UIPostUpdate) {
        val actionBar = view.findViewById<MeeraContentActionBar>(R.id.post_action_bar)
        val post = avatarsList[position].post ?: return
        val reactionHolderViewId =
            (postUpdate as? UIPostUpdate.MeeraUpdateReaction)?.reactionUpdate?.reactionSource?.reactionHolderViewId
                ?: MeeraContentActionBar.ReactionHolderViewId.empty()
        updateActionBar(
            post = post,
            actionBar = actionBar,
            reactionHolderViewId = reactionHolderViewId
        )
    }

    inner class TapToZoomCallback(private val bigImage: BigImageView, private val photoModel: PhotoModel) :
        ImageLoader.Callback {
        override fun onFinish() = Unit
        override fun onSuccess(image: File?) {
            val view = bigImage.ssiv
            //Double tap to zoom
            if (view != null) {
                view.setMinimumDpi(80)
                view.setOnImageEventListener(object : SubsamplingScaleImageView.OnImageEventListener {
                    override fun onImageLoaded() {
                        view.setDoubleTapZoomDpi(80)
                        view.setDoubleTapZoomDuration(200)
                        view.setDoubleTapZoomStyle(ZOOM_FOCUS_FIXED)
                        view.isQuickScaleEnabled = false
                    }

                    override fun onReady() {
                        if (isAdultContent(photoModel)) view.isZoomEnabled = false
                    }

                    override fun onTileLoadError(e: Exception?) = Unit
                    override fun onPreviewReleased() = Unit
                    override fun onImageLoadError(e: Exception?) = Unit
                    override fun onPreviewLoadError(e: Exception?) = Unit
                })
            }
        }

        override fun onFail(error: Exception?) = Unit
        override fun onCacheHit(imageType: Int, image: File?) = Unit
        override fun onCacheMiss(imageType: Int, image: File?) = Unit
        override fun onProgress(progress: Int) = Unit
        override fun onStart() = Unit
    }

    interface TouchImageAdapterInteraction {
        fun onImageClicked() = Unit
        fun onImageLongClick(imageUrl: String?) {}
    }

}
