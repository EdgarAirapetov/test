package com.numplates.nomera3.modules.feed.ui.util

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideScaleDown
import com.meera.core.extensions.isOnTheScreen
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.setVisible
import com.meera.core.extensions.showScaleUp
import com.meera.core.extensions.visible
import com.meera.core.keyboard.getRootView
import com.numplates.nomera3.modules.baseCore.helper.hide
import com.numplates.nomera3.modules.feed.ui.adapter.MediaLoadingState
import com.numplates.nomera3.modules.feed.ui.data.LoadingPostVideoInfoUIModel
import com.numplates.nomera3.modules.feed.ui.view.MeeraPostLoaderView
import com.numplates.nomera3.presentation.download.DownloadMediaHelper

private const val LOTTIE_ANIMATION = "modified_loader.json"
private const val MARKER_LOADING_START = "loading_start"
private const val MARKER_SUCCESS_START = "success_start"

private const val MAX_START_LOADING_TIME_DELTA = 1000

class PostMediaDownloadControllerUtil(
    private var loaderView: MeeraPostLoaderView?,
    private var onStopDownloadClick: (() -> Unit)?
) {

    private val lottieAnimationView = loaderView?.getAnimationView()
    private val cancelMediaDownload = loaderView?.getCancelView()

    private var currentState: MediaLoadingState = MediaLoadingState.NONE

    init {
        lottieAnimationView?.setThrottledClickListener { onStopDownloadClick?.invoke() }
    }

    fun setupLoading(postId: Long?, loadingInfo: LoadingPostVideoInfoUIModel) {
        val loadingState = loadingInfo.loadingState
        attachPostIdToViewToCompareItWhenToastWillSent(postId)
        if (currentState == loadingState) return
        currentState = loadingState
        when (loadingState) {
            MediaLoadingState.NONE -> hideViews()
            MediaLoadingState.LOADING -> setLoadingState(loadingInfo)
            MediaLoadingState.LOADING_NO_CANCEL_BUTTON -> setLoadingState(loadingInfo)
            MediaLoadingState.SUCCESS -> setSuccessState(loadingTime = loadingInfo.loadingTime)
            MediaLoadingState.FAIL,
            MediaLoadingState.CANCELED -> setFailState()
        }
    }

    fun currentState() = currentState


    fun clearResources() {
        lottieAnimationView?.removeCallbacks(null)
        lottieAnimationView?.cancelAnimation()
        lottieAnimationView?.setOnClickListener(null)
        loaderView?.tag = null
        loaderView = null
        onStopDownloadClick = null
    }

    private fun isDeltaExceedsMaxValue(loadingTime: Long): Boolean {
        val startLoadingTime = System.currentTimeMillis()
        return if (startLoadingTime - loadingTime > MAX_START_LOADING_TIME_DELTA) {
            hideViews()
            true
        } else {
            false
        }
    }

    private fun setLoadingState(loadingInfo : LoadingPostVideoInfoUIModel, isShowCancelButton: Boolean = true) {
        lottieAnimationView?.apply {
            cancelAnimation()
            setAnimation(LOTTIE_ANIMATION)
            setVisible(loadingInfo.isShowLoadingProgress)
            removeAllAnimatorListeners()
            repeatMode = LottieDrawable.RESTART
            repeatCount = LottieDrawable.INFINITE
            setMinAndMaxFrame(MARKER_LOADING_START, MARKER_SUCCESS_START, true)
            frame = minFrame.toInt()
            playLottieAnimationFromRecyclerViewOnBind()
            if (isShowCancelButton) showCancelIcon()
        }
    }

    /**
     * **See Also** [Lottie Library Issue 1495](https://github.com/airbnb/lottie-android/issues/1495)
     *
     * Post the start of the lottie animation from RecyclerView's onBind() instead of immediately calling it.
     *
     * Sometimes the animation would get stuck on a starting frame even after calling playAnimation()
     *
     * The cause is most likely the view's unusual state in onBind, when it's attached to window,
     * but isShown(), which is used in playAnimation(), returns false because parent is null.
     *
     * Another fix is to up the lottie library version to 5.0.1+, which changes the visibility handling,
     * but also removes setScale() APIs which breaks some things in other places.
     *
     * TODO (BR-20336) Update lottie version and remove this fix
     */
    private fun LottieAnimationView.playLottieAnimationFromRecyclerViewOnBind() {
        post { playAnimation() }
    }

    private fun setSuccessState(loadingTime: Long) {
        lottieAnimationView?.apply {
            if (isDeltaExceedsMaxValue(loadingTime)) return
            if (isAnimating.not()) {
                hideViews()
                return
            }
            hide()
            hideCancelIcon()
        }
    }

    private fun setFailState() {
        lottieAnimationView?.apply {
            if (isAnimating.not()) {
                hideViews()
                return
            }
            currentState = MediaLoadingState.NONE
            repeatMode = LottieDrawable.RESTART
            repeatCount = 0
            this@apply.hide()
            cancelMediaDownload?.hideScaleDown()
        }
    }

    private fun showCancelIcon() {
        cancelMediaDownload?.apply {
            visible()
            scaleX = 0F
            scaleY = 0F
            showScaleUp()
        }
    }

    private fun hideCancelIcon() {
        cancelMediaDownload?.hideScaleDown()
    }

    /**
     * Записываем postID в loaderView чтобы когда придет сообщения об успехе
     * понять показывать ли нижнюю плашку когда эта ячейка поста на экране пользователя
     *
     * Это нужно чтобы не показывать одновременно "успешную" нижнюю плашку и успешную галочку
     * в ячейке поста.
     */
    private fun attachPostIdToViewToCompareItWhenToastWillSent(postId: Long?) {
        loaderView?.tag = postId
    }

    private fun hideViews() {
        lottieAnimationView?.gone()
        cancelMediaDownload?.gone()
    }

    companion object {

        private val checkUtil = NeedToShowSuccessDownloadToastUtil()

        fun needToShowSuccessDownloadToast(
            act: AppCompatActivity,
            postMediaDownloadType: DownloadMediaHelper.PostMediaDownloadType?
        ): Boolean {
            return checkUtil.needToShowSuccessDownloadToast(act = act, postMediaDownloadType = postMediaDownloadType)
        }
    }

    /**
     * Утилита сообщает нужно ли отображать плашку об успешно загруженном видео на
     * основе [DownloadMediaHelper.PostMediaDownloadType].
     */
    private class NeedToShowSuccessDownloadToastUtil {

        fun needToShowSuccessDownloadToast(
            act: AppCompatActivity,
            postMediaDownloadType: DownloadMediaHelper.PostMediaDownloadType?
        ): Boolean {
            val allActivityViews = act.getRootView().getAllViews()
            val needToShowToastOnDuplicatePage = needToShowToastOnDuplicatePage(
                allActivityViews = allActivityViews,
                postMediaDownloadType = postMediaDownloadType
            )
            val loaderInGlobalViewTree = isLoaderInGlobalViewTree(
                allActivityViews = allActivityViews,
                postMediaDownloadType = postMediaDownloadType
            )
            return if (!loaderInGlobalViewTree) return true else needToShowToastOnDuplicatePage
        }

        private fun isLoaderInGlobalViewTree(
            allActivityViews: List<View>?,
            postMediaDownloadType: DownloadMediaHelper.PostMediaDownloadType?
        ): Boolean {
            val postId = postMediaDownloadType?.postId
            return allActivityViews?.find { checkedView ->
                checkedView is MeeraPostLoaderView && checkedView.isTaggedViewOnScreen(postId)
            } != null
        }

        private fun needToShowToastOnDuplicatePage(
            allActivityViews: List<View>?,
            postMediaDownloadType: DownloadMediaHelper.PostMediaDownloadType?
        ): Boolean {
            val postFragmentContainer = allActivityViews?.find {
                it.isTaggedViewOnScreen("post_detail_container")
            }
            return when (postMediaDownloadType) {
                is DownloadMediaHelper.PostMediaDownloadType.PostRoadDownload -> postFragmentContainer != null
                is DownloadMediaHelper.PostMediaDownloadType.PostDetailDownload -> postFragmentContainer == null
                else -> true
            }
        }

        private fun View.isTaggedViewOnScreen(requiredTag: Any?) = tag == requiredTag && isOnTheScreen()

        private fun View.getAllViews(): List<View> {
            if (this !is ViewGroup || childCount == 0) return listOf(this)
            return children
                .toList()
                .flatMap { it.getAllViews() }
                .plus(this as View)
        }
    }
}
