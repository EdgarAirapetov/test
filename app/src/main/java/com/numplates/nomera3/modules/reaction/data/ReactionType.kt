package com.numplates.nomera3.modules.reaction.data

import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.meera.core.extensions.dp
import com.numplates.nomera3.R
import java.time.Instant
import java.time.LocalDateTime
import java.util.TimeZone


private val REACTION_FIRE_PADDING_COMPENSATION = -21.dp
private val REACTION_CRYING_PADDING_COMPENSATION = -21.dp
private const val MAX_MORNING_FRAME_TO_SHOW = 62
private const val MAX_EVENING_FRAME_TO_SHOW = 82

enum class ReactionType(
    @RawRes
    val resourceNoBorder: Int,
    @DrawableRes
    val resourceDrawable: Int,
    @DrawableRes
    val resourceDrawableBitten: Int,
    val value: String,
    val characterRepresentation: String,
    @StringRes
    val resourceName: Int,
    @ColorRes
    val resourceColor: Int,
    @ColorRes
    val resourceBackground: Int,
    @RawRes
    val resourcePopupAnimation: Int? = null,
    val reactionXTranslation : Int = 0,
    val maxFrame : Int = 0,
) {
    GreenLight(
        R.raw.reaction_like_lottie_meera,
        R.drawable.reaction_like,
        R.drawable.reaction_like,
        "green_light",
        "Ǟ",
        R.string.reaction_green_light,
        R.color.uiKitBackAddOrange,
        R.drawable.bg_button_yellow,
        resourcePopupAnimation = R.raw.reaction_popup_lottie_like
    ),
    Morning(
        R.raw.reaction_morning_lottie_meera,
        R.drawable.reaction_morning_meera,
        R.drawable.reaction_morning_meera,
        "good_morning",
        "Ȟ",
        R.string.reaction_moring,
        R.color.uiKitBackAddBlue,
        R.drawable.bg_button_blue,
        resourcePopupAnimation = R.raw.reaction_popup_lottie_morning,
        maxFrame = MAX_MORNING_FRAME_TO_SHOW
    ),
    Evening(
        R.raw.reaction_evening_lottie_meera,
        R.drawable.reaction_evening_meera,
        R.drawable.reaction_evening_meera,
        "good_evening",
        "Ȇ",
        R.string.reaction_night,
        R.color.uiKitForeAddGreen,
        R.drawable.bg_button_green,
        resourcePopupAnimation = R.raw.reaction_popup_lottie_evening,
        maxFrame = MAX_EVENING_FRAME_TO_SHOW
    ),
    LaughTears(
        R.raw.reaction_laugh_tears_lottie_meera,
        R.drawable.reaction_laugh_tears_meera,
        R.drawable.reaction_laugh_tears_meera,
        "laugh_tears",
        "Ǡ",
        R.string.reaction_laugh_tears,
        R.color.uiKitBackAddOrange,
        R.drawable.bg_button_yellow,
        resourcePopupAnimation = R.raw.reaction_popup_lottie_laugh
    ),
    InLove(
        R.raw.reaction_in_love_lottie_meera,
        R.drawable.reaction_in_love_meera,
        R.drawable.reaction_in_love_meera,
        "in_love",
        "ſ",
        R.string.reaction_in_love,
        R.color.uiKitForeAddRed,
        R.drawable.bg_button_red,
        resourcePopupAnimation = R.raw.reaction_popup_lottie_love
    ),
    Fire(
        R.raw.reaction_fire_lottie_meera,
        R.drawable.reaction_fire_meera,
        R.drawable.reaction_fire_meera,
        "fire",
        "Ǥ",
        R.string.reaction_fire,
        R.color.uiKitBackAddOrange,
        R.drawable.bg_button_yellow,
        resourcePopupAnimation = R.raw.reaction_popup_lottie_fire,
        reactionXTranslation = REACTION_FIRE_PADDING_COMPENSATION
    ),
    Amazing(
        R.raw.reaction_amazing_lottie_meera,
        R.drawable.reaction_amazing_meera,
        R.drawable.reaction_amazing_meera,
        "amazing",
        "Ȩ",
        R.string.reaction_amazing,
        R.color.uiKitBackAddOrange,
        R.drawable.bg_button_yellow,
        resourcePopupAnimation = R.raw.reaction_popup_lottie_amazing
    ),
    Facepalm(
        R.raw.reaction_facepalm_lottie_meera,
        R.drawable.reaction_facepalm_meera,
        R.drawable.reaction_facepalm_meera,
        "facepalm",
        "Ǩ",
        R.string.reaction_facepalm,
        R.color.uiKitBackAddOrange,
        R.drawable.bg_button_yellow,
        resourcePopupAnimation = R.raw.reaction_popup_lottie_facepalm
    ),
    Crying(
        R.raw.reaction_crying_lottie_meera,
        R.drawable.reaction_crying_meera,
        R.drawable.reaction_crying_meera,
        "crying",
        "Ǹ",
        R.string.reaction_crying,
        R.color.uiKitBackAddOrange,
        R.drawable.bg_button_yellow,
        resourcePopupAnimation = R.raw.reaction_popup_lottie_crying,
        reactionXTranslation = REACTION_CRYING_PADDING_COMPENSATION
    ),
    RedLight(
        R.raw.reaction_red_light_lottie_meera,
        R.drawable.reaction_red_light_meera,
        R.drawable.reaction_red_light_meera,
        "red_light",
        "Ǣ",
        R.string.reaction_red_light,
        R.color.uiKitForeAddRed,
        R.drawable.bg_button_red,
        resourcePopupAnimation = R.raw.reaction_popup_lottie_red_light
    );

    /**
     * Ссылка на view которая была
     */
    var selectedByView: View? = null

    companion object {

        private const val MORNING_EVENING_AVAILABLE_HOURS = 8L

        fun currentValues() = values()
            .filter { reactionType -> filterByTime(reactionType) }

        fun currentValues(showMorningEvening: Boolean) = currentValues()
            .filter { reactionType ->
                if (reactionType !in listOf(Morning, Evening)) return return@filter true
                return@filter showMorningEvening
            }

        private fun filterByTime(reactionType: ReactionType): Boolean {
            if (reactionType !in listOf(Morning, Evening)) return true
            val currentTime = LocalDateTime.now()
            val isMorning = currentTime.isAfter(
                currentTime.withHour(5)
                    .withMinute(0)
                    .withSecond(0)
            ) && currentTime.isBefore(
                currentTime.withHour(11)
                    .withMinute(0)
                    .withSecond(0)
            )
            val isNight = currentTime.isAfter(
                currentTime.withHour(22)
                    .withMinute(0)
                    .withSecond(0)
            ) || currentTime.isBefore(
                currentTime.withHour(3)
                    .withMinute(0)
                    .withSecond(0)
            )
            return if (reactionType == Morning) isMorning else isNight
        }

        val valuesMap = HashMap<String, ReactionType>().apply {
            values().forEach { reaction ->
                put(reaction.value, reaction)
            }
        }
        val characterMap = HashMap<String, ReactionType>().apply {
            values().forEach { reaction ->
                put(reaction.characterRepresentation, reaction)
            }
        }

        fun getByString(value: String?): ReactionType? {
            if (value == null) return null
            return valuesMap[value]
        }

        fun getByCharacter(character: String?): ReactionType? {
            if (character == null) return null
            return characterMap[character]
        }

        fun getDrawableFromCharacter(character: String?): Int {
            return getByCharacter(character)?.resourceDrawable ?: 0
        }

        fun getDrawableFromUnsupportedCharacter(character: String?): Int {
            val hash = character?.hashCode() ?: return 0
            return values()[hash % values().size].resourceDrawable
        }

        fun isMorningEveningActual(publishedAt: Long): Boolean {
            val publishedDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(publishedAt * 1000),
                TimeZone.getDefault().toZoneId()
            );
            return LocalDateTime.now()
                .isBefore(publishedDateTime.plusHours(MORNING_EVENING_AVAILABLE_HOURS))
        }
    }
}
