package com.numplates.nomera3.modules.uploadpost.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.widget.TextViewCompat
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.newHeight
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewPostTextBackgroundBinding
import com.numplates.nomera3.modules.appInfo.ui.entity.PostBackgroundItemUiModel
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.uploadpost.ui.data.PostBackgroundTextSize
import com.numplates.nomera3.presentation.view.ui.TextViewWithImages
import com.numplates.nomera3.presentation.view.ui.edittextautocompletable.EditTextAutoCompletable
import kotlin.math.min

class PostTextBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private var height: Int = 0
    private var minTextSize: Float = PostBackgroundTextSize.SIZE_1.absoluteValue
    private var maxTextSize: Float = PostBackgroundTextSize.SIZE_5.absoluteValue
    private var maxTextSizeForNotEditable: Int = 10
    private var marginVerticalForNonEditable = 10.dp
    private var maxLineCount: Int = 9
    private val ASPECT_RATIO = 0.7

    private var postBackground: PostBackgroundItemUiModel? = null

    private var showDefaultInput: () -> Unit = {}
    private var onTextChanged: (String) -> Unit = {}

    private var textListenerActive = true

    private var isEditable = false

    private var isRendered = false

    private var convertedSizes: ArrayList<Pair<Int, Int>> = arrayListOf()

    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.view_post_text_background, this, false)
        .apply(::addView)
        .let(ViewPostTextBackgroundBinding::bind)


    fun bind(postUIEntity: PostUIEntity) {
        this.isEditable = false

        setupHeight()
        setupActiveViews()
        setMarginsForNonEditableState()

        setTextColor(postUIEntity.fontColor ?: String.empty())

        setupAutoSizeTextView(isEditable = false)
        setTextSize(postUIEntity.fontSize)
        renderBackground(backgroundUrl = postUIEntity.backgroundUrl)
    }

    fun showAsEditable(
        text: String,
        background: PostBackgroundItemUiModel,
        inputSelection: Int?,
        onTextChanged: (String) -> Unit,
        showDefaultInput: () -> Unit
    ) {
        this.isEditable = true
        this.showDefaultInput = showDefaultInput
        this.postBackground = background
        this.onTextChanged = onTextChanged

        isVisible = true

        setupHeight()
        setupActiveViews()
        setTextColor(textColor = background.fontColor)
        setHighlightTextColor(textColor = getHighlightColor(background))
        setupText(text = text, inputSelection = inputSelection)
        renderBackground(backgroundUrl = background.url)

        isRendered = true
    }

    fun showAsEditable(text: String, selection: Int) {
        this.isEditable = true
        isVisible = true

        binding.etPostTextBackground.apply {
            setText(text)
            setSelection(min(selection, length()))
            requestFocus()
        }
    }

    fun getTextView(): TextViewWithImages {
        return binding.tvPostTextBackground
    }

    fun setBackground(background: PostBackgroundItemUiModel) {
        this.postBackground = background

        setTextColor(background.fontColor)
        setHighlightTextColor(textColor = getHighlightColor(background))
        binding.etPostTextBackground.highlightAllUniqueNamesAndHashTags()
        renderBackground(background.url)
    }

    fun getPostBackground(): PostBackgroundItemUiModel? {
        return postBackground
    }

    fun getRelativeFontSize(): Int {
        val currentTextSize = binding.etPostTextBackground.textSize.toInt()
        for (convertedSize in convertedSizes) {
            if (convertedSize.second == currentTextSize) return convertedSize.first
        }
        return PostBackgroundTextSize.SIZE_1.relativeValue
    }

    fun hide() {
        isVisible = false
    }

    fun isShowing(): Boolean {
        return isVisible
    }

    fun getEditText(): EditTextAutoCompletable = binding.etPostTextBackground

    fun setText(text: String) {
        textListenerActive = false

        getEditText().setText(text)
    }

    fun setupHeight() {
        if (height != 0) return

        height = (width * ASPECT_RATIO).toInt()
        newHeight(height)
    }

    fun isInputFullyVisible(text: String, isVisible: (Boolean) -> Unit) {
        if (!isRendered) setMinTextSize()

        setTextToAutoSizeView(
            text = text,
            onCompleted = {
                binding.etPostTextBackground.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    binding.tvPostTextBackground.textSize
                )
                setText(text)
                binding.etPostTextBackground.post {
                    isVisible(isInputFullyVisible())
                }
            })
    }

    private fun isInputFullyVisible(): Boolean {
        val view = binding.etPostTextBackground
        val lineCount = view.lineCount

        return if (lineCount == maxLineCount) {
            binding.tvPostTextBackground.width >= view.layout.getLineWidth(lineCount - 1)
        } else {
            lineCount < maxLineCount
        }
    }

    private fun setupActiveViews() {
        if (isEditable) {
            binding.etPostTextBackground.visible()
            binding.tvPostTextBackground.invisible()
        } else {
            binding.etPostTextBackground.gone()
            binding.tvPostTextBackground.visible()
        }
    }

    private fun setMarginsForNonEditableState() {
        binding.tvPostTextBackground.setMargins(
            binding.tvPostTextBackground.marginStart,
            marginVerticalForNonEditable,
            binding.tvPostTextBackground.marginEnd,
            marginVerticalForNonEditable
        )
    }

    private fun setupAutoSizeTextView(isEditable: Boolean = true) {
        var presets = intArrayOf(
            convertTextSizeValueToPixel(PostBackgroundTextSize.SIZE_5.absoluteValue),
            convertTextSizeValueToPixel(PostBackgroundTextSize.SIZE_4.absoluteValue),
            convertTextSizeValueToPixel(PostBackgroundTextSize.SIZE_3.absoluteValue),
            convertTextSizeValueToPixel(PostBackgroundTextSize.SIZE_2.absoluteValue),
            convertTextSizeValueToPixel(PostBackgroundTextSize.SIZE_1.absoluteValue)
        )

        if (isEditable) {
            convertedSizes.clear()
            val reversedPresets = presets.reversed()
            for (i in reversedPresets.indices) {
                convertedSizes.add(
                    Pair(PostBackgroundTextSize.findByRelativeValue(i + 1).relativeValue, reversedPresets[i])
                )
            }
        } else {
            for (size in PostBackgroundTextSize.SIZE_1.absoluteValue.toInt() - 1 downTo maxTextSizeForNotEditable) {
                presets = presets.plus(convertTextSizeValueToPixel(size.toFloat()))
            }
        }

        TextViewCompat.setAutoSizeTextTypeUniformWithPresetSizes(
            binding.tvPostTextBackground,
            presets,
            TypedValue.COMPLEX_UNIT_PX
        )
    }

    private fun setupText(text: String, inputSelection: Int?) {
        binding.etPostTextBackground.isInvisible = true
        setupAutoSizeTextView()
        binding.tvPostTextBackground.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            convertTextSizeValueToPixel(maxTextSize).toFloat()
        )
        binding.etPostTextBackground.apply {
            setTextToAutoSizeView(text = text, onCompleted = {
                setTextSize(TypedValue.COMPLEX_UNIT_PX, binding.tvPostTextBackground.textSize)

                setText(text)
                setSelection(if (inputSelection != null && length() > inputSelection) inputSelection else length())

                isVisible = true
            })

            addTextChangedListener(onTextChanged = { onChangedText, _, _, _ ->
                if (textListenerActive.not()) {
                    textListenerActive = true
                    return@addTextChangedListener
                }

                setTextToAutoSizeView(
                    text = onChangedText.toString(),
                    onCompleted = {
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, binding.tvPostTextBackground.textSize)

                        binding.etPostTextBackground.post {
                            if (isEditable && isShowing()) {
                                if (isInputFullyVisible()) {
                                    onTextChanged.invoke(onChangedText.toString())
                                } else {
                                    this@PostTextBackgroundView.invisible()
                                    showDefaultInput.invoke()
                                }
                            }
                        }
                    }
                )
            })

            requestFocus()
        }
    }

    private fun setTextToAutoSizeView(text: String, onCompleted: () -> Unit) {
        with(binding.tvPostTextBackground) {
            this.text = text
            post { onCompleted.invoke() }
        }
    }

    private fun setHighlightTextColor(textColor: Int) {
        binding.etPostTextBackground.highlightColorRes = textColor
    }

    private fun setTextColor(textColor: String) {
        if (textColor.isEmpty()) return
        binding.etPostTextBackground.setTextColor(Color.parseColor("#$textColor"))
        binding.tvPostTextBackground.setTextColor(Color.parseColor("#$textColor"))
    }

    private fun setTextSize(textSize: Int?) {
        val postBackgroundTextSize = PostBackgroundTextSize.findByRelativeValue(textSize)
        binding.etPostTextBackground.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            convertTextSizeValueToPixel(postBackgroundTextSize.absoluteValue).toFloat()
        )
        binding.tvPostTextBackground.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            convertTextSizeValueToPixel(postBackgroundTextSize.absoluteValue).toFloat()
        )
    }

    @SuppressLint("RestrictedApi")
    private fun setMinTextSize() {
        with(binding.tvPostTextBackground) {
            setAutoSizeTextTypeWithDefaults(TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE)
            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                convertTextSizeValueToPixel(minTextSize).toFloat()
            )
            setAutoSizeTextTypeWithDefaults(TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM)
        }
    }

    private fun convertTextSizeValueToPixel(absoluteValue: Float): Int {
        val density = resources.displayMetrics.density
        return (absoluteValue * density / (1 - density / absoluteValue)).toInt()
    }

    private fun renderBackground(backgroundUrl: String?) {
        if (!backgroundUrl.isNullOrEmpty()) {
            if (isEditable) binding.lProgress.root.visible()
            Glide.with(context)
                .load(backgroundUrl)
                .override(Target.SIZE_ORIGINAL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .fitCenter()
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.lProgress.root.gone()
                        return true
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        resource?.let { binding.ivPostTextBackground.setImageDrawable(it) }
                        binding.lProgress.root.gone()
                        return true
                    }

                })
                .into(binding.ivPostTextBackground)
        } else {
            binding.ivPostTextBackground.setImageDrawable(null)
            binding.lProgress.root.gone()
        }
    }

    private fun getHighlightColor(background: PostBackgroundItemUiModel): Int =
        ContextCompat.getColor(
            context,
            if (background.isWhiteFont()) R.color.uiKitColorForegroundLink else R.color.uiKitColorForegroundAddNavy
        )
}
