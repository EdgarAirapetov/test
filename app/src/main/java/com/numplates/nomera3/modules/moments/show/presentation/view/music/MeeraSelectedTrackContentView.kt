package com.numplates.nomera3.modules.moments.show.presentation.view.music

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.meera.core.extensions.empty
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraViewSelectedTrackContentBinding

class MeeraSelectedTrackContentView(context: Context, attrs: AttributeSet) :
    LinearLayout(context, attrs) {
    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.meera_view_selected_track_content, this, false)
        .apply(::addView)
        .let(MeeraViewSelectedTrackContentBinding::bind)

    fun bind(artistName: String, trackName: String) {
        binding.tvSelectedTrackContent.text = context.resources
            .getString(R.string.nmrmedia_stories_selected_track_content, artistName, trackName)
    }

    fun clear() {
        binding.tvSelectedTrackContent.text = String.empty()
    }
}
