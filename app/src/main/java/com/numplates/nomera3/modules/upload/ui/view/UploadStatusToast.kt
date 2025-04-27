package com.numplates.nomera3.modules.upload.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.shape.ShapeAppearanceModel
import com.meera.core.extensions.dp
import com.meera.core.extensions.getScreenHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraCustomUploadToastBinding
import com.numplates.nomera3.modules.upload.ui.model.StatusToastAction
import com.numplates.nomera3.modules.upload.ui.model.StatusToastActionUiModel
import com.numplates.nomera3.modules.upload.ui.model.StatusToastState
import com.numplates.nomera3.modules.upload.ui.model.StatusToastUiModel
import com.numplates.nomera3.presentation.utils.runOnUiThread

class UploadStatusToast @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAtr: Int = 0
) : FrameLayout(context, attributeSet, defStyleAtr) {

    var onDismiss: (() -> Unit)? = null
    var actionListener: ((StatusToastAction) -> Unit)? = null
    private var hideEnabled = true
    private var wasHidden = false
    private var transitionY = 0F
    private var moved = 0F
    private var movementTime = 0L
    private var DEFAULT_BOTTOM_MARGIN_OFFSET = 8.dp
    private var additionalBottomMargin: Int = 0
    private var screenHeight = 0
    private var hideAnimationInProcess = false
    private var showAnimationInProcess = false

    private val binding =
        MeeraCustomUploadToastBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    init {
        gone()
    }

    fun show(uiModel: StatusToastUiModel) {
        hideEnabled = uiModel.state == StatusToastState.Error
        wasHidden = false
        if (uiModel.state is StatusToastState.Info) {
            loadInfoIcon()
        } else {
            initUploadMediaAttachment(uiModel.imageUrl, true)
        }
        showIconPlay(uiModel.canPlayContent)
        handleUploadState(uiModel.state)
        handleAction(uiModel.action)
        show()
    }

    fun show() {
        animateShow()
    }


    fun hide(force: Boolean = false) {
        if (hideEnabled || force) animateHide(byUser = false)
    }

    fun setupAddToFavorites(mediaUrl: String?) {
        val url = mediaUrl ?: return
        setupAddToFavoritesToastViews()
        loadStaticMedia(
            path = url,
            isMoment = false
        )
    }

    fun setAdditionalBottomMargin(margin: Int) {
        additionalBottomMargin = margin
    }

    override fun post(action: Runnable?): Boolean {
        screenHeight = getScreenHeight()
        return super.post(action)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!hideEnabled || wasHidden) return true
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                transitionY = event.rawY
                movementTime = System.currentTimeMillis()
            }

            MotionEvent.ACTION_MOVE -> {
                moved = event.rawY - transitionY
                moveView(moved)
            }

            MotionEvent.ACTION_UP -> {
                val endTime = System.currentTimeMillis() - movementTime

                if (moved > SWIPE_THRESHOLD) {
                    animateHide(duration = endTime, byUser = true)
                } else {
                    animateShow()
                }
            }
        }
        return true
    }

    private fun moveView(to: Float) {
        if (to < 0) return
        animate()
            .yBy(to)
            .setDuration(0)
            .start()
    }

    private fun animateShow() {
        visible()
        post {
            val translation = screenHeight - height - additionalBottomMargin + DEFAULT_BOTTOM_MARGIN_OFFSET
            showAnimationInProcess = true
            animate()
                .y(translation.toFloat())
                .setDuration(ANIMATION_DURATION)
                .withEndAction {
                    showAnimationInProcess = false
                }
                .start()
        }
    }

    private fun animateHide(duration: Long = ANIMATION_DURATION, byUser: Boolean) {
        wasHidden = true
        post {
            animate()
                .y(screenHeight.toFloat())
                .setDuration(duration)
                .withEndAction {
                    hideAnimationInProcess = false
                    gone()
                    if (byUser) {
                        onDismiss?.invoke()
                    }
                }
                .start()
        }
    }

    private fun handleUploadState(status: StatusToastState) {
        when (status) {
            is StatusToastState.Progress -> showUploadInProgress(status.message)
            is StatusToastState.Success -> showUploadSuccess(status)
            StatusToastState.Error -> showUploadFailed()
            is StatusToastState.Info -> showInfo(status)
        }
    }

    private fun handleAction(action: StatusToastActionUiModel?) {
        if (action != null) {
            binding.uploadToastActionText.visible()
            binding.uploadToastActionText.text = action.actionTitle
            binding.uploadToastActionText.setThrottledClickListener {
                actionListener?.invoke(action.action)
            }
        } else {
            binding.uploadToastActionText.gone()
            binding.uploadToastActionText.setOnClickListener(null)
        }
    }

    private fun initUploadMediaAttachment(path: String?, isMoment: Boolean) {
        if (path == null) {
            loadDefaultIcon()
        } else {
            loadStaticMedia(path = path, isMoment = isMoment)
        }
    }

    private fun loadInfoIcon() {
        binding.uploadToastIcon.setImageResource(R.drawable.ic_app)
    }

    private fun loadDefaultIcon() {
        Glide.with(context)
            .load(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_post_upload_placeholder
                )
            )
            .into(binding.uploadToastIcon)
    }

    private fun loadStaticMedia(path: String, isMoment: Boolean) {
        Glide.with(context)
            .load(if (isMoment) path else Uri.parse(path))
            .error(ContextCompat.getDrawable(context, R.drawable.ic_post_upload_placeholder))
            .addListener(object : RequestListener<Drawable> {
                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    (resource as? Animatable)?.stop()

                    runOnUiThread {
                        binding.uploadToastIcon.setImageDrawable(resource)
                    }

                    return true
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }
            }).into(binding.uploadToastIcon)
    }

    private fun showIconPlay(show: Boolean) {
        binding.uploadToastIconPlay.isVisible = show
    }

    private fun setupAddToFavoritesToastViews() {
        hideEnabled = true
        binding.uploadToastText.text = context?.getText(R.string.file_added_to_favorites)
        binding.uploadToastProgress.gone()
        binding.uploadToastActionText.gone()
        binding.uploadToastSuccessIcon.gone()
        binding.uploadToastIconError.gone()
        binding.uploadToastIconPlay.gone()
        binding.uploadToastIcon.visible()
        binding.uploadToastIcon.shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setAllCornerSizes(0F)
            .build()
    }

    private fun showUploadInProgress(message: String? = null) {
        hideEnabled = true
        binding.uploadToastText.text = message ?: context?.getText(R.string.road_upload_post_progress_text)
        binding.uploadToastProgress.visible()
        binding.uploadToastActionText.gone()
        binding.uploadToastSuccessIcon.gone()
        binding.uploadToastIconError.gone()
    }

    private fun showUploadFailed() {
        hideEnabled = false
        binding.uploadToastActionText.visible()
        binding.uploadToastProgress.gone()
        binding.uploadToastText.text = context.getText(R.string.road_upload_post_error_text)
        binding.uploadToastText.visible()
        binding.uploadToastIconContainer.visible()
        binding.uploadToastIconError.visible()
    }

    private fun showUploadSuccess(status: StatusToastState.Success) {
        hideEnabled = true
        binding.uploadToastText.text = status.message ?: context.getText(R.string.road_upload_post_success_text)
        binding.uploadToastText.visible()
        binding.uploadToastProgress.gone()
        binding.uploadToastActionText.gone()
        binding.uploadToastIconError.gone()
        binding.uploadToastSuccessIcon.visible()
    }

    private fun showInfo(info: StatusToastState.Info) {
        hideEnabled = true
        binding.uploadToastActionText.visible()
        binding.uploadToastProgress.gone()
        binding.uploadToastText.text = info.message
        binding.uploadToastText.visible()
        binding.uploadToastIconError.gone()
        binding.uploadToastSuccessIcon.gone()
    }

    companion object {
        private const val ANIMATION_DURATION = 150L
        private const val SWIPE_THRESHOLD = 200
    }
}
