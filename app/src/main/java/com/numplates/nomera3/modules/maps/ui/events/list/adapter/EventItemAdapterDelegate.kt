package com.numplates.nomera3.modules.maps.ui.events.list.adapter

import android.app.Activity
import android.view.View
import androidx.core.content.ContextCompat
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegate
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.core.utils.graphics.getScreenWidth
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemEventsListBinding
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListItem
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListItemPayload
import com.numplates.nomera3.modules.maps.ui.events.participants.view.model.EventParticipantsUiAction
import com.numplates.nomera3.modules.maps.ui.events.participants.view.model.EventParticipantsUiModel
import com.numplates.nomera3.modules.maps.ui.model.MapUiAction
import com.numplates.nomera3.modules.tags.data.entity.SpanDataClickType
import com.numplates.nomera3.presentation.utils.spanTagsTextInPosts

private const val HORIZONTAL_MARGIN_COUNT = 2

fun eventItemAdapterDelegate(itemActionListener: (MapUiAction.EventsListUiAction) -> Unit) =
    adapterDelegate<EventsListItem.EventItemUiModel, EventsListItem>(R.layout.item_events_list) {
        val binding = ItemEventsListBinding.bind(itemView)
        val hMargins = with(context.resources) {
            getDimensionPixelSize(R.dimen.map_events_list_item_horizontal_margin) * HORIZONTAL_MARGIN_COUNT +
                getDimensionPixelSize(R.dimen.map_events_lists_bottomsheet_horizontal_margin) * HORIZONTAL_MARGIN_COUNT
        }
        val itemWidth = getScreenWidth(context as Activity) - hMargins
        bind { payloads ->
            when (val latestPayload = payloads.lastOrNull()) {
                is EventsListItemPayload.EventItemParticipation -> {
                    EventItemParticipationBinder(
                        binding = binding,
                        eventParticipants = latestPayload.eventParticipants
                    ).bind()
                }
                else -> {
                    EventItemBinder(
                        itemWidth = itemWidth,
                        binding = binding,
                        item = item,
                        itemActionListener = itemActionListener
                    ).bind()
                }
            }
        }
    }

private class EventItemParticipationBinder(
    private val binding: ItemEventsListBinding,
    private val eventParticipants: EventParticipantsUiModel,
) {
    fun bind() {
        binding.epvItemEventsListParticipants.setModel(eventParticipants)
    }
}

private class EventItemBinder(
    private val itemWidth: Int,
    private val binding: ItemEventsListBinding,
    private val item: EventsListItem.EventItemUiModel,
    private val itemActionListener: (MapUiAction.EventsListUiAction) -> Unit
) {
    private val context = binding.root.context

    fun bind() {
        bindType()
        bindDateTime()
        bindStatus()
        bindTitle()
        bindDistanceAddress()
        bindHostAvatar()
        bindParticipation()
        adjustTextSize()
        setupActions()
    }

    private fun bindType() {
        binding.ukicvItemEventsListEventType.setImageResId(item.eventLabel.imgResId)
        binding.ukicvItemEventsListEventType.getConfig()?.let { config ->
            val updatedConfig = config.copy(text = context.getString(item.eventLabel.titleResId))
            binding.ukicvItemEventsListEventType.setConfig(updatedConfig)
        }
    }

    private fun bindDateTime() {
        binding.ukcvItemEventsListDateTime.text = "${item.eventLabel.date}, ${item.eventLabel.time}"
    }

    private fun bindStatus() {
        if (item.eventStatus != null) {
            binding.tvItemEventsListStatus.setText(item.eventStatus.statusTextResId)
            binding.tvItemEventsListStatus.setTextColor(
                ContextCompat.getColor(context, item.eventStatus.textColorResId)
            )
            binding.tvItemEventsListStatus.visible()
        } else {
            binding.tvItemEventsListStatus.gone()
        }
    }

    private fun bindTitle() {
        spanTagsTextInPosts(
            context = context,
            tvText = binding.tvItemEventsListTitle,
            post = item.eventTitleTagSpan,
            linkColor = R.color.ui_post_text_link,
            click = { clickType ->
                if (clickType is SpanDataClickType.ClickMore) {
                    itemActionListener.invoke(MapUiAction.EventsListUiAction.OpenEventPost(item))
                }
            }
        )
    }

    private fun adjustTextSize() {
        binding.tvItemEventsListTitle.textSize = TEXT_SIZE_LARGE_SP
        val hMeasureSpec = View.MeasureSpec.makeMeasureSpec(itemWidth, View.MeasureSpec.EXACTLY)
        binding.root.measure(hMeasureSpec, View.MeasureSpec.UNSPECIFIED)
        if (binding.tvItemEventsListTitle.lineCount > 1) {
            binding.tvItemEventsListTitle.textSize = TEXT_SIZE_DEFAULT_SP
        }
    }

    private fun bindDistanceAddress() {
        if (item.eventLabel.distanceAddress != null) {
            binding.eavItemEventsListDistanceAddress.setModel(item.eventLabel.distanceAddress)
            binding.eavItemEventsListDistanceAddress.visible()
        } else {
            binding.eavItemEventsListDistanceAddress.gone()
        }
    }

    private fun bindHostAvatar() {
        binding.ukuiItemEventsListHost.setConfig(
            UserpicUiModel(userAvatarUrl = item.hostAvatar)
        )
    }

    private fun bindParticipation() {
        binding.epvItemEventsListParticipants.setModel(item.participants)
    }

    private fun setupActions() {
        binding.epvItemEventsListParticipants.setActionListener { eventParticipantsUiAction ->
            val uiAction = when (eventParticipantsUiAction) {
                EventParticipantsUiAction.JoinEvent -> MapUiAction.EventsListUiAction.JoinEvent(item)
                EventParticipantsUiAction.LeaveEvent -> {

                    MapUiAction.EventsListUiAction.LeaveEvent(item)
                }
                EventParticipantsUiAction.ShowEventCreator -> null
                EventParticipantsUiAction.ShowEventParticipants -> MapUiAction.EventsListUiAction.ShowEventParticipants(item)
                EventParticipantsUiAction.ShowEventOnMap -> null
                EventParticipantsUiAction.HandleJoinAnimationFinished -> null
                EventParticipantsUiAction.NavigateToEvent -> null
            }
            uiAction?.let(itemActionListener::invoke)
        }
        binding.ukuiItemEventsListHost.setThrottledClickListener {
            itemActionListener.invoke(MapUiAction.EventsListUiAction.ShowEventHostProfile(item))
        }
        binding.eavItemEventsListDistanceAddress.setThrottledClickListener {
            itemActionListener.invoke(MapUiAction.EventsListUiAction.NavigateToEvent(item))
        }
        binding.tvItemEventsListTitle.setThrottledClickListener {
            itemActionListener.invoke(MapUiAction.EventsListUiAction.OpenEventPost(item))
        }
        binding.root.setThrottledClickListener {
            itemActionListener.invoke(MapUiAction.EventsListUiAction.OpenEventPost(item))
        }
    }

    companion object {
        private const val TEXT_SIZE_DEFAULT_SP = 18f
        private const val TEXT_SIZE_LARGE_SP = 24f
    }
}

