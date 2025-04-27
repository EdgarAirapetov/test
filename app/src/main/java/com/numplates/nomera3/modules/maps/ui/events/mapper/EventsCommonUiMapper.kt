package com.numplates.nomera3.modules.maps.ui.events.mapper

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.maps.domain.events.model.EventType
import com.numplates.nomera3.modules.maps.ui.events.model.AddressUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.DistanceAddressUiModel
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt

class EventsCommonUiMapper @Inject constructor(context: Context) {

    private val resources = context.resources

    fun mapDayOfWeekShort(date: LocalDate): String = resources
        .getStringArray(R.array.days_of_week_short)[date.dayOfWeek.value - 1]

    fun mapDayOfWeekFull(date: LocalDate): String = resources
        .getStringArray(R.array.days_of_week_full)[date.dayOfWeek.value - 1]

    fun mapMonthShort(date: LocalDate): String = resources
        .getStringArray(R.array.month_names_short)[date.month.value - 1]

    @StringRes
    fun mapEventTypeTitleResId(eventType: EventType): Int {
        return when (eventType) {
            EventType.EDUCATION -> R.string.map_events_type_education
            EventType.ART -> R.string.map_events_type_art
            EventType.CONCERT -> R.string.map_events_type_concert
            EventType.SPORT -> R.string.map_events_type_sport
            EventType.TOURISM -> R.string.map_events_type_tourism
            EventType.GAMES -> R.string.map_events_type_games
            EventType.PARTY -> R.string.map_events_type_party
        }
    }

    @DrawableRes
    fun mapEventTypeImgPinResId(eventType: EventType): Int {
        return when (eventType) {
            EventType.EDUCATION -> R.drawable.ic_map_event_pin_education
            EventType.ART -> R.drawable.ic_map_event_pin_art
            EventType.CONCERT -> R.drawable.ic_map_event_pin_concert
            EventType.SPORT -> R.drawable.ic_map_event_pin_sport
            EventType.TOURISM -> R.drawable.ic_map_event_pin_tourism
            EventType.GAMES -> R.drawable.ic_map_event_pin_games
            EventType.PARTY -> R.drawable.ic_map_event_pin_party
        }
    }

    @DrawableRes
    fun mapEventTypeImgResId(eventType: EventType): Int {
        return when (eventType) {
            EventType.EDUCATION -> R.drawable.ic_map_event_education
            EventType.ART -> R.drawable.ic_map_event_art
            EventType.CONCERT -> R.drawable.ic_map_event_concert
            EventType.SPORT -> R.drawable.ic_map_event_sport
            EventType.TOURISM -> R.drawable.ic_map_event_tourism
            EventType.GAMES -> R.drawable.ic_map_event_games
            EventType.PARTY -> R.drawable.ic_map_event_party
        }
    }

    @DrawableRes
    fun mapEventTypeImgSmallResId(eventType: EventType): Int {
        return when (eventType) {
            EventType.EDUCATION -> R.drawable.ic_map_event_education_small
            EventType.ART -> R.drawable.ic_map_event_art_small
            EventType.CONCERT -> R.drawable.ic_map_event_concert_small
            EventType.SPORT -> R.drawable.ic_map_event_sport_small
            EventType.TOURISM -> R.drawable.ic_map_event_tourism_small
            EventType.GAMES -> R.drawable.ic_map_event_games_small
            EventType.PARTY -> R.drawable.ic_map_event_party_small
        }
    }

    @DrawableRes
    fun mapEventTypePlaceholderResId(eventType: EventType?): Int? {
        return when (eventType) {
            EventType.EDUCATION -> R.drawable.ic_map_event_education_placeholder
            EventType.ART -> R.drawable.ic_map_event_art_placeholder
            EventType.CONCERT -> R.drawable.ic_map_event_concert_placeholder
            EventType.SPORT -> R.drawable.ic_map_event_sport_placeholder
            EventType.TOURISM -> R.drawable.ic_map_event_tourism_placeholder
            EventType.GAMES -> R.drawable.ic_map_event_games_placeholder
            EventType.PARTY -> R.drawable.ic_map_event_party_placeholder
            else -> return null
        }
    }

    @ColorRes
    fun mapEventTypeColorResId(eventType: EventType): Int {
        return when (eventType) {
            EventType.EDUCATION -> R.color.map_event_education_bg
            EventType.ART -> R.color.map_event_art_bg
            EventType.CONCERT -> R.color.map_event_concert_bg
            EventType.SPORT -> R.color.map_event_sport_bg
            EventType.TOURISM -> R.color.map_event_tourism_bg
            EventType.GAMES -> R.color.map_event_games_bg
            EventType.PARTY -> R.color.map_event_party_bg
        }
    }

    fun mapEventDistanceAddress(addressUiModel: AddressUiModel): DistanceAddressUiModel {
        val distanceFormatted = addressUiModel.distanceMeters?.roundToInt()?.let(::formatDistance).orEmpty()
        return DistanceAddressUiModel(
            distanceString = distanceFormatted,
            addressString = "${addressUiModel.name}, ${addressUiModel.addressString}"
        )
    }

    private fun formatDistance(distanceMeters: Int): String {
        val km = distanceMeters / 1000
        val metres = distanceMeters % 1000
        return if (km > 0) "$km.${metres / 100} км" else "$metres м"
    }
}
