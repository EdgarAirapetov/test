package com.numplates.nomera3.modules.moments.show.presentation.view.music

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewSelectedTrackBinding
import com.numplates.nomera3.modules.feed.ui.entity.UiMedia

class SelectedTrackView(context: Context, attrs: AttributeSet) :
    ConstraintLayout(context, attrs) {
    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.view_selected_track, this, false)
        .apply(::addView)
        .let(ViewSelectedTrackBinding::bind)

    private val CONTENT_SCROLL_PERIOD = 50L
    private val CONTENT_SCROLL_DX = 3
    private val CONTENT_MARGIN_END_VIEW_STATE_DP = 15
    private val CONTENT_MARGIN_END_EDIT_STATE_DP = 25

    private var state: SelectedTrackViewState = SelectedTrackViewState.VIEWING
    private var onRemove: () -> Unit = {}

    private var contentScrollingHandler: Handler? = null
    private var media: UiMedia? = null

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.SelectedTrackView, 0, 0)

        try {
            val stateInt = ta.getInt(
                R.styleable.SelectedTrackView_state,
                SelectedTrackViewState.VIEWING.state
            )
            state = SelectedTrackViewState.findByState(stateInt)
        } finally {
            ta.recycle()
        }

        initStateView(state)
        initRemoveView()
    }

    fun bind(onRemove: () -> Unit) {
        this.onRemove = onRemove
    }

    fun setContentInfo(media: UiMedia, onClickAction: () -> Unit) {
        this.media = media
        initStateView(state)
        initTrackContentViews(media)
        visibility = View.VISIBLE

        binding.vSelectedTrackClickableArea.setOnClickListener { onClickAction.invoke() }
    }

    fun show() {
        visibility = View.VISIBLE
    }

    fun hide() {
        visibility = View.GONE
        clearHandler()
        clearTrackContentViews()
    }

    private fun initTrackContentViews(media: UiMedia) {
        binding.apply {
            listOf(
                stcvSelectedTrackContentFirst,
                stcvSelectedTrackContentSecond
            ).forEach { contentView ->
                contentView.bind(
                    artistName = media.artist ?: "",
                    trackName = media.track ?: ""
                )
            }
        }

        startAnimateTrackContentViews()
    }

    private fun clearTrackContentViews() {
        binding.apply {
            listOf(
                stcvSelectedTrackContentFirst,
                stcvSelectedTrackContentSecond
            ).forEach { contentView ->
                contentView.clear()
            }
        }
    }

    private fun startAnimateTrackContentViews() {
        clearHandler()
        contentScrollingHandler = Handler(context.mainLooper)
        val runnable = object : Runnable {
            override fun run() {
                if (binding.hsvSelectedTrackContainer.scrollX >= binding.stcvSelectedTrackContentFirst.width) {
                    binding.hsvSelectedTrackContainer.scrollX = 0
                }
                binding.hsvSelectedTrackContainer.smoothScrollBy(CONTENT_SCROLL_DX, 0)
                postHandler(this)
            }
        }
        postHandler(runnable)
    }

    private fun postHandler(runnable: Runnable) {
        contentScrollingHandler?.postDelayed(runnable, CONTENT_SCROLL_PERIOD)
    }

    private fun clearHandler() {
        contentScrollingHandler?.removeCallbacksAndMessages(null)
        contentScrollingHandler = null
    }

    private fun initStateView(state: SelectedTrackViewState) {
        binding.llSelectedTrackRemoveBtn.isVisible = state == SelectedTrackViewState.EDITING
        val lp = binding.hsvSelectedTrackContainer.layoutParams as FrameLayout.LayoutParams
        lp.setMargins(
            lp.leftMargin,
            lp.topMargin,
            when (state) {
                SelectedTrackViewState.VIEWING -> CONTENT_MARGIN_END_VIEW_STATE_DP
                SelectedTrackViewState.EDITING -> CONTENT_MARGIN_END_EDIT_STATE_DP
            },
            lp.bottomMargin)
        binding.hsvSelectedTrackContainer.layoutParams = lp
    }

    private fun initRemoveView() {
        binding.llSelectedTrackRemoveBtn.setOnClickListener {
            hide()
            onRemove.invoke()
        }
    }

    private fun setScrollInitState() {
        binding.hsvSelectedTrackContainer.scrollX = 0
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (media == null) return
        startAnimateTrackContentViews()
    }

    override fun onDetachedFromWindow() {
        clearHandler()
        setScrollInitState()
        super.onDetachedFromWindow()
    }
}
