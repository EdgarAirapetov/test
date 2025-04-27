package com.numplates.nomera3.modules.chat.helpers

import android.animation.LayoutTransition
import android.content.Context
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.meera.core.extensions.dp
import com.meera.core.extensions.setListener
import com.meera.core.extensions.setMargins
import com.numplates.nomera3.databinding.FragmentChatBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

private const val FAVORITES_ANIMATION_SCALE = .1F
private const val FAVORITES_ANIMATION_DURATION = 350L
private const val FAVORITES_ANIMATION_MARGIN_START = 20
private const val FAVORITES_ANIMATION_MARGIN_TOP = 15


fun createAddToFavoritesImageViewForAnimation(context: Context, ivMediaPreview: ImageView?): ImageView? {
    val ivAnimation = ImageView(context)
    val oldImageView = ivMediaPreview ?: return null
    ivAnimation.layoutParams = FrameLayout.LayoutParams(oldImageView.width, oldImageView.height)
    ivAnimation.setMargins(
        top = oldImageView.y.toInt(),
        start = oldImageView.x.toInt()
    )
    ivAnimation.setImageDrawable(oldImageView.drawable)
    return ivAnimation
}

fun createAddToFavoritesLottieViewForAnimation(
    context: Context,
    lavMediaPreview: LottieAnimationView?,
    lottieUrl: String?
): LottieAnimationView? {
    val lavAnimation = LottieAnimationView(context)
    val oldLottieView = lavMediaPreview ?: return null
    lavAnimation.layoutParams = FrameLayout.LayoutParams(oldLottieView.width, oldLottieView.height)
    lavAnimation.setMargins(
        top = oldLottieView.y.toInt(),
        start = oldLottieView.x.toInt()
    )
    lavAnimation.repeatCount = LottieDrawable.INFINITE
    lavAnimation.setFailureListener { Timber.e(it) }
    lavAnimation.setAnimationFromUrl(lottieUrl)
    lavAnimation.frame = oldLottieView.frame
    lavAnimation.playAnimation()
    return lavAnimation
}

fun FragmentChatBinding?.calculateAndLaunchFavoritesAnimation(
    imageView: ImageView,
    pointTo: Point,
    onLaunchFavoritesLottieAnimation: () -> Unit
) {
    val translationX =
        pointTo.x - imageView.marginStart - (imageView.layoutParams.width / 2) + FAVORITES_ANIMATION_MARGIN_START.dp
    val translationY =
        pointTo.y - imageView.marginTop - (imageView.layoutParams.height / 2) + FAVORITES_ANIMATION_MARGIN_TOP.dp
    imageView.apply {
        alpha = 1F
    }.animate()
        .translationX(translationX.toFloat())
        .translationY(translationY.toFloat())
        .scaleX(FAVORITES_ANIMATION_SCALE)
        .scaleY(FAVORITES_ANIMATION_SCALE)
        .alpha(0F)
        .setDuration(FAVORITES_ANIMATION_DURATION)
        .setListener(onAnimationEnd = {
            this?.apply {
                val layoutTransition = root.layoutTransition
                layoutTransition.disableTransitionType(LayoutTransition.DISAPPEARING)
                root.removeView(imageView)
                layoutTransition.enableTransitionType(LayoutTransition.DISAPPEARING)
                onLaunchFavoritesLottieAnimation.invoke()
            }
        })
}

fun calculateAndLaunchFavoritesAnimation(
    root: View,
    imageView: ImageView,
    pointTo: Point,
    onLaunchFavoritesLottieAnimation: () -> Unit
) {
    val translationX =
        pointTo.x - imageView.marginStart - (imageView.layoutParams.width / 2) + FAVORITES_ANIMATION_MARGIN_START.dp
    val translationY =
        pointTo.y - imageView.marginTop - (imageView.layoutParams.height / 2) + FAVORITES_ANIMATION_MARGIN_TOP.dp
    imageView.apply {
        alpha = 1F
    }.animate()
        .translationX(translationX.toFloat())
        .translationY(translationY.toFloat())
        .scaleX(FAVORITES_ANIMATION_SCALE)
        .scaleY(FAVORITES_ANIMATION_SCALE)
        .alpha(0F)
        .setDuration(FAVORITES_ANIMATION_DURATION)
        .setListener(onAnimationEnd = {
            root?.apply {
                val layoutTransition = (root as? FrameLayout)?.layoutTransition
                layoutTransition?.disableTransitionType(LayoutTransition.DISAPPEARING)
                (root as? FrameLayout)?.removeView(imageView)
                layoutTransition?.enableTransitionType(LayoutTransition.DISAPPEARING)
                onLaunchFavoritesLottieAnimation.invoke()
            }
        })
}

fun FragmentChatBinding?.showAddToFavoritesChatAnimation(scope: CoroutineScope) {
    val binding = this
    scope.launch {
        binding?.sendMessageContainer?.lavAddToFavorites?.playAnimation()
        delay(binding?.sendMessageContainer?.lavAddToFavorites?.duration ?: 0L)
        binding?.sendMessageContainer?.lavAddToFavorites?.cancelAnimation()
    }
}

fun getImagePositionInImageView(imageView: ImageView): Point {
    val f = FloatArray(9)
    imageView.imageMatrix.getValues(f)

    val scaleX = f[Matrix.MSCALE_X]
    val scaleY = f[Matrix.MSCALE_Y]

    val d: Drawable = imageView.drawable
    val origW: Int = d.intrinsicWidth
    val origH: Int = d.intrinsicHeight

    val actW = Math.round(origW * scaleX)
    val actH = Math.round(origH * scaleY)

    val imgViewW: Int = imageView.width
    val imgViewH: Int = imageView.height

    val top = (imgViewH - actH) / 2
    val left = (imgViewW - actW) / 2

    return Point(left, top)
}

