package com.numplates.nomera3.modules.maps.domain.model

import com.numplates.nomera3.R
import com.numplates.nomera3.modules.maps.ui.entity.UIMapStyleEntity
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class MapMode {
    DAY, NIGHT, AUTO
}

fun MapMode.isLight(): Boolean {
    return when (this) {
        MapMode.DAY -> true
        MapMode.NIGHT -> false
        MapMode.AUTO -> {
            val formatter = SimpleDateFormat("HH:mm", Locale.US)
            val currentTimeString = formatter.format(Calendar.getInstance().time)
            try {
                val currentTime = formatter.parse(currentTimeString)
                val morningTime = formatter.parse("05:00")
                val eveningTime = formatter.parse("21:00")

                currentTime
                    ?.let { it.before(eveningTime) && it.after(morningTime) }
                    ?: true
            } catch (e: ParseException) {
                Timber.e(e)
                true
            }
        }
    }
}

fun MapMode.isDark(): Boolean {
    return !isLight()
}

fun MapMode.toUIMapStyleEntity(): UIMapStyleEntity {
    val styleResId = if (isLight()) R.raw.meera_map else R.raw.map_dark
    return UIMapStyleEntity(styleResId)
}
