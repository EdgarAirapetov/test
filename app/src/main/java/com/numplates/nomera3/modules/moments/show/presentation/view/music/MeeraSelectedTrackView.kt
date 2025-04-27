package com.numplates.nomera3.modules.moments.show.presentation.view.music

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraViewSelectedTrackBinding

class MeeraSelectedTrackView(context: Context, attrs: AttributeSet) :
    ConstraintLayout(context, attrs) {
    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.meera_view_selected_track, this, false)
        .apply(::addView)
        .let(MeeraViewSelectedTrackBinding::bind)

    private val CONTENT_SCROLL_PERIOD = 50L
    private val CONTENT_SCROLL_DX = 3
    private val CONTENT_MARGIN_END_VIEW_STATE_DP = 15
    private val CONTENT_MARGIN_END_EDIT_STATE_DP = 25

    private var state: SelectedTrackViewState = SelectedTrackViewState.VIEWING
    private var onRemove: () -> Unit = {}

    private var contentScrollingHandler: Handler? = null

    private var artistName: String? = null
    private var trackName: String? = null

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
    }

    fun bind(onRemove: () -> Unit) {
        this.onRemove = onRemove
    }

    fun setContentInfo(artistName: String, trackName: String, clickListener: (() -> Unit)? = null) {
        this.artistName = artistName
        this.trackName = trackName

        initStateView(state)
        initTrackContentViews(artistName = artistName, trackName = trackName)
        binding.root.setThrottledClickListener { clickListener?.invoke() }
    }

    fun show() {
        visibility = View.VISIBLE

        val artistName = artistName
        val trackName = trackName

        if (artistName != null && trackName != null) {
            startAnimateTrackContentViews()
        }
    }

    fun hide() {
        visibility = View.GONE
        clearHandler()
    }

    private fun initTrackContentViews(artistName: String, trackName: String) {
        binding.apply {
            listOf(
                stcvSelectedTrackContentFirst,
                stcvSelectedTrackContentSecond
            ).forEach { contentView ->
                contentView.bind(artistName = artistName, trackName = trackName)
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

}

enum class SelectedTrackViewState(val state: Int) {
    VIEWING(0), EDITING(1);

    companion object {
        fun findByState(state: Int): SelectedTrackViewState {
            return entries.find { it.state == state } ?: VIEWING
        }
    }
}
