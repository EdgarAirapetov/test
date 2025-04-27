package com.numplates.nomera3.modules.chat.data

//Максимальное значение, которое может прийти с обеих платформах в голосовом сообщении
const val MAX_AMPLITUDE_VALUE = 100

// Минимальное значение, которое может прийти с обеих платформах в голосовом сообщении
const val MIN_AMPLITUDE_VALUE = 0

//Значение амплитуды, которое приходит с датчика. 12тыс - это считаем громко
const val LOUD_VOICE_AMPLITUDE = 12_000.0

/**
 * Нормализует значение амплитуды и помещает его
 * в диапазон MIN_AMPLITUDE_VALUE .. MAX_AMPLITUDE_VALUE
 * */
fun normalizeVoiceAmplitude(value: Int): Int {
    var normalizedAmplitude = (value / LOUD_VOICE_AMPLITUDE) * MAX_AMPLITUDE_VALUE
    if (normalizedAmplitude > MAX_AMPLITUDE_VALUE.toDouble()) {
        normalizedAmplitude = MAX_AMPLITUDE_VALUE.toDouble()
    }
    return normalizedAmplitude.toInt()
}
