package com.numplates.nomera3.modules.maps.ui.events

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.meera.core.extensions.addClickableText
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewEventsInfoBinding
import com.numplates.nomera3.modules.maps.domain.events.EventConstants
import com.numplates.nomera3.modules.maps.ui.events.model.EventsInfoUiModel

class EventsInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
): FrameLayout(context, attrs, defStyle) {

    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.view_events_info, this, false)
        .apply(::addView)
        .let(ViewEventsInfoBinding::bind)

    var rulesOpenListener: (() -> Unit)? = null

    init {
        binding.tvMapEventsInfoAvailableCount.text = resources.getString(
            R.string.map_events_info_bullet_1_total,
            EventConstants.MAX_USER_EVENT_COUNT
        )
        binding.tvMapEventsInfoRules.text = context.getString(R.string.map_events_info_bullet_3).addClickableText(
            ContextCompat.getColor(context, R.color.ui_purple),
            context.getString(R.string.map_events_info_bullet_3_key)
        ) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(MODERATION_RULES_URL)))
        }
        binding.tvMapEventsInfoRules.movementMethod = LinkMovementMethod.getInstance()
    }

    fun setModel(uiModel: EventsInfoUiModel) {
        binding.tvMapEventsInfoAvailableCount.text = if (uiModel.eventsAvailable != null) {
            val eventsCountString = "<b>${uiModel.eventsAvailable}/${EventConstants.MAX_USER_EVENT_COUNT}</b>"
            val eventsAvailableString = resources.getString(
                R.string.map_events_info_bullet_1_available,
                eventsCountString
            )
            Html.fromHtml(eventsAvailableString, Html.FROM_HTML_MODE_LEGACY)
        } else {
            resources.getString(
                R.string.map_events_info_bullet_1_total,
                EventConstants.MAX_USER_EVENT_COUNT
            )
        }
    }

    companion object {
        private const val MODERATION_RULES_URL = "https://noomeera.com/politics/content_moderation_rules/"
    }
}
