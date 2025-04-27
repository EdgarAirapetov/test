package com.numplates.nomera3.modules.chat.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.meera.core.extensions.click
import com.meera.core.extensions.displayWidth
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.pxToDp
import com.numplates.nomera3.R
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

private const val BARS_CONTAINER_MARGIN_LEFT = 102
private const val BARS_CONTAINER_MARGIN_RIGHT = 115
private const val BAR_WITH_SPACE_WIDTH = 4
private const val DURATION_STRING_FORMAT = "%01d:%02d"

class MeeraVoiceMessagePreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var onPlayClicked: (isPlay: Boolean) -> Unit = { }
    var onProgressChangedByUser: (progress: Int) -> Unit = { }

    private var isPlaying = false
    private var ivPlay: ImageView? = null
    private var bars: VoiceMessagePreviewBarsView? = null
    private var tvDuration: TextView? = null

    init {
        View.inflate(context, R.layout.meera_voice_message_preview_view, this)
        ivPlay = findViewById(R.id.iv_play_btn)
        bars = findViewById(R.id.view_amplitude_bars)
        tvDuration = findViewById(R.id.tv_voice_duration)
    }

    fun showPreviewBars(list: List<Int>) {
        if (list.isEmpty()) return
        val barsCount = calculateBarsCount()
        val amplitudes = generateListAmplitudes(list, barsCount)
        bars?.showBars(amplitudes)
        playControl()
    }

    @SuppressLint("DefaultLocale")
    fun setDuration(duration: Long) {
        val time = String.format(
            DURATION_STRING_FORMAT,
            TimeUnit.MILLISECONDS.toMinutes(duration),
            TimeUnit.MILLISECONDS.toSeconds(duration) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
        )
        tvDuration?.text = time
    }

    fun setMaxProgress(progress: Int) {
        bars?.max = progress
    }

    fun setProgress(progress: Int) {
        bars?.progress = progress
    }

    fun setStopState() {
        isPlaying = false
        setProgress(0)
        ivPlay?.setImageResource(R.drawable.ic_meera_play_voice_preview)
    }

    fun setPauseState() {
        isPlaying = false
        ivPlay?.setImageResource(R.drawable.ic_meera_play_voice_preview)
    }

    private fun calculateBarsCount(): Int {
        val leftMarginPx = dpToPx(BARS_CONTAINER_MARGIN_LEFT)
        val rightMarginPx = dpToPx(BARS_CONTAINER_MARGIN_RIGHT)
        val barsContainerWidth = context.displayWidth - (leftMarginPx + rightMarginPx)
        return pxToDp(barsContainerWidth) / BAR_WITH_SPACE_WIDTH
    }

    private fun playControl() {
        bars?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) onProgressChangedByUser(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        ivPlay?.click {
            isPlaying = if (!isPlaying) {
                onPlayClicked.invoke(true)
                ivPlay?.setImageResource(R.drawable.ic_meera_pause_voice_preview)
                true
            } else {
                onPlayClicked.invoke(false)
                ivPlay?.setImageResource(R.drawable.ic_meera_play_voice_preview)
                false
            }

        }
    }

    private fun generateListAmplitudes(list: List<Int>, barsCount: Int): List<Int> {
        val diff = list.size / barsCount
        if(diff == 1) {
            val remain = list.size - barsCount
            val first = list.take(barsCount - (remain - 1))
            val second = list.takeLast(remain)
            return (first + second).dropLast(1)
        } else if(diff > 1) {
            val batchSize: Float = list.size.toFloat() / barsCount.toFloat()
            val chunked = list.chunked(batchSize.roundToInt())
            val amplitudes = chunked.map { batch -> batch.average().toInt() }
            return if (amplitudes.size > barsCount) {
                amplitudes.subList(0, barsCount)
            } else {
                amplitudes
            }
        }
        return emptyList()
    }



}
