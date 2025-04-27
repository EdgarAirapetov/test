package com.numplates.nomera3.presentation.audio

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.numplates.nomera3.R


class VoiceMessageView : FrameLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    lateinit var view: View

    var isPlaying: Boolean = false
    var downloadedFilePath: String? = null
    val voiceHandler = Handler(Looper.getMainLooper())


    lateinit var visualizer: VisualizerVoiceView
    lateinit var playButton: ImageButton
    lateinit var downloadProgressBar: ProgressBar
    private var playIcon: Drawable? = null
    private var pauseIcon: Drawable? = null
    private var downloadIcon: Drawable? = null

    var localDuration: Int = 0


    init {
        init(context)
    }


    private fun init(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.voice_massage_view, this)

        visualizer = view.findViewById(R.id.visualizer_voice_message)
        playButton = view.findViewById(R.id.btn_play_voice_message)
        downloadProgressBar = view.findViewById(R.id.pb_download_voice_message)

    }


    fun setView(isIncomingMessage: Boolean, columnsHeightList: List<Int>) {
        initColors(isIncomingMessage)

        if (downloadedFilePath == null) {
            setDownloadButton()
        } else {
            setPlayButton()
        }

        // Reset VoiceView state
        isPlaying = false
        visualizer.progress = 0
        visualizer.init(columnsHeight = columnsHeightList, isIncomingMessage = isIncomingMessage)
    }

    fun setPlayButton() {
        playButton.setImageDrawable(playIcon)
        downloadProgressBar.gone()
    }

    fun setPauseButton() {
        playButton.setImageDrawable(pauseIcon)
    }

    private fun setDownloadButton() {
        playButton.setImageDrawable(downloadIcon)
        downloadProgressBar.visible()
        downloadProgressBar.progress = 100
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }


    private fun initColors(isIncomingMessage: Boolean) {
        if (isIncomingMessage) {
            playIcon = ContextCompat.getDrawable(context, R.drawable.play_incoming)
            pauseIcon = ContextCompat.getDrawable(context, R.drawable.pause_incoming)
            downloadIcon = ContextCompat.getDrawable(context, R.drawable.download_incoming)
            downloadProgressBar.progressDrawable =
                    ContextCompat.getDrawable(context, R.drawable.circle_progressbar_incoming)
        } else {
            playIcon = ContextCompat.getDrawable(context, R.drawable.play_outgoing)
            pauseIcon = ContextCompat.getDrawable(context, R.drawable.pause_outgoing)
            downloadIcon = ContextCompat.getDrawable(context, R.drawable.download_outgoing)
            downloadProgressBar.progressDrawable =
                    ContextCompat.getDrawable(context, R.drawable.circle_progressbar_outgoing)
        }
    }
}
