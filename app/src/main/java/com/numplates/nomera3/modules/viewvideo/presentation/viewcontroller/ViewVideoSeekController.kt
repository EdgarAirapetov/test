package com.numplates.nomera3.modules.viewvideo.presentation.viewcontroller

import android.graphics.Point
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.numplates.nomera3.modules.viewvideo.presentation.view.ViewVideoSeekBarView
import com.numplates.nomera3.modules.viewvideo.presentation.view.ViewVideoSmallSeekBarView
import com.numplates.nomera3.modules.viewvideo.presentation.view.getCurrentPosition
import java.util.Formatter
import kotlin.math.abs

private const val VIDEO_COMBINED_TIME_FORMAT = "%01d:%02d / %01d:%02d"
private const val UPDATE_ON_SHOW_DELAY = 100L

class ViewVideoSeekController(
    private val timeDisplayView: TextView,
    private val timeBar: ViewVideoSeekBarView,
    private val smallTimeBar: ViewVideoSmallSeekBarView
) {

    init {
        timeBar.isEnabled = false
    }

    private val playerListener: Player.Listener = ProgressChangeListener()

    private val sb = StringBuilder()
    private val formatter = Formatter(sb)

    private var currentDuration: Long = C.TIME_UNSET
    private var displayedDuration: Long = C.TIME_UNSET

    private var currentPosition: Long = C.TIME_UNSET
    private var displayedPosition: Long = C.TIME_UNSET

    private var displayedTimeText: String = ""

    fun providePlayerListenerInstance() = playerListener

    fun show(tapPoint: Point) {
        timeDisplayView.visible()
        timeBar.show()
        timeBar.postDelayed({ updateProgress(tapPoint = tapPoint) }, UPDATE_ON_SHOW_DELAY)
    }

    fun hide() {
        timeDisplayView.gone()
        timeBar.hide()
    }

    fun isVisible(): Boolean = timeDisplayView.isVisible

    fun updateProgress(currentPosition: Long) {
        this.currentPosition = currentPosition
        changePosition()
    }

    private fun updateProgress(player: Player? = null, tapPoint: Point? = null) {
        currentPosition = if (tapPoint != null) {
            timeBar.getCurrentPosition(positionX = tapPoint.x, totalDuration = currentDuration)
        } else {
            player?.currentPosition ?: currentPosition
        }

        changePosition()
    }

    private fun changePosition() {
        if (timeDisplayView.isVisible.not() || timeDisplayView.isAttachedToWindow.not()) return
        val positionChanged = displayedPosition != currentPosition
        val durationChanged = displayedDuration != currentDuration
        if (positionChanged || durationChanged) {
            val timeText = getStringForTime(currentPosition, currentDuration)
            displayedPosition = currentPosition
            displayedDuration = currentDuration
            if (timeText != displayedTimeText) {
                timeDisplayView.text = timeText
                displayedTimeText = timeText
            }
        }

        timeBar.setPosition(currentPosition)
        smallTimeBar.setPosition(currentPosition)
    }

    private fun updateTimeline(player: Player) {
        val duration = player.contentDuration
        if (duration < 0) return
        currentDuration = duration
        timeBar.setDuration(currentDuration)
        smallTimeBar.setDuration(currentDuration)
        updateProgress(player)
    }

    private fun getStringForTime(currentPositionMs: Long, durationMs: Long): String {
        var absCurrentPosMs = abs(currentPositionMs)
        val absDurationMs = abs(durationMs)
        if (absCurrentPosMs > absDurationMs) absCurrentPosMs = absDurationMs
        val positionTotalSeconds: Long = (absCurrentPosMs + 500) / 1000
        val positionSeconds = positionTotalSeconds % 60
        val positionMinutes = positionTotalSeconds / 60 % 60
        val durationTotalSeconds: Long = (absDurationMs + 500) / 1000
        val durationSeconds = durationTotalSeconds % 60
        val durationMinutes = durationTotalSeconds / 60 % 60
        sb.clear()
        return formatter.format(
            VIDEO_COMBINED_TIME_FORMAT,
            positionMinutes,
            positionSeconds,
            durationMinutes,
            durationSeconds
        ).toString()
    }

    private inner class ProgressChangeListener : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            if (events.containsAny(
                    Player.EVENT_PLAYBACK_STATE_CHANGED,
                    Player.EVENT_PLAY_WHEN_READY_CHANGED,
                    Player.EVENT_IS_PLAYING_CHANGED
                )
            ) {
                updateProgress(player)
            }
            if (events.containsAny(Player.EVENT_POSITION_DISCONTINUITY, Player.EVENT_TIMELINE_CHANGED)) {
                updateTimeline(player)
            }
        }
    }
}

