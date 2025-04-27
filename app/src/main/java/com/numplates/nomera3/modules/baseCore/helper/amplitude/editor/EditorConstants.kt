package com.numplates.nomera3.modules.baseCore.helper.amplitude.editor

import com.meera.application_api.analytic.model.AmplitudeName
import com.meera.application_api.analytic.model.AmplitudeProperty

enum class EditorEventName(
    private val event: String
) : AmplitudeName {

    EDITOR_OPEN("editor open"),
    PHOTO_EDITOR("photoeditor use"),
    VIDEO_EDITOR("videoeditor use");

    override val eventName: String
        get() = event
}

object AmplitudePropertyEditorConst {
    const val EDITOR_TYPE = "editor type"

    const val WHERE = "where" // Отмечаем, где пользователь перешел в редактор
    const val AUTOMATIC_OPEN = "automatic open" // Отмечаем, автоматически ли открылся редактор
    const val USER_ID = "user id"
    const val MAIN_CHANGE = "main change" // Отмечаем, вносил ли пользователь изменения в фото/видео
    const val WHAT_FILTER_CHOOSE = "what filter choose"

    const val DURATION_CHANGE = "duration change"
    const val WHAT_DURATION_CHOOSE = "what duration choose"
    const val SOUND_CHANGE = "sound change"

    const val INTENSITY_FILTER_CHANGE = "intensity filter change"
    const val WHAT_CROP_PHOTO = "what crop photo"

    // Отмечаем при подтверждении редактирования, была ли использована шкала поворота
    const val ROTATION_SCALE_USE = "rotation scale use"
    const val TURN_USE = "turn use" // Отмечаем, использовал ли пользователь поворот на 90'

    // Отмечаем, использовал ли пользователь отражение по вертикали
    const val VERTICAL_REFLECTION_USE = "vertical reflection use"

    // Отмечаем, использовал ли пользователь отражение по горизонтали
    const val HORIZONTAL_REFLECTION_USE = "horizontal reflection use"

    // Отмечаем, вносил ли пользователь изменения на вкладке "Обработка"
    const val PROCESSING_CHANGE = "processing change"
    const val AUTO_IMPROVEMENT_CHANGE = "auto improvement change"
    const val BRIGHTNESS_CHANGE = "brightness change"
    const val EXPOSITION_CHANGE = "exposition change"
    const val CONTRAST_CHANGE = "contrast change"
    const val GAMMA_CHANGE = "gamma change"

    // Отмечаем, вносил ли пользователь изменения в опции "Чёткость"
    const val DEFINITION_CHANGE = "definition change"
    const val TEMPERATURE_CHANGE = "temperature change"

    // Отмечаем, вносил ли пользователь изменения в опции "Насыщенность"
    const val SETURATION_CHANGE = "saturation change"

    // Отмечаем, вносил ли пользователь изменения в опции "Подсветка"
    const val ILLUMINATION_CHANGE = "illumination change"
    const val SHADOWS_CHANGE = "shadows change"
    const val VIGNETTE_CHANGE = "vignette change" // Отмечаем, вносил ли пользователь изменения в опции "Виньетка"
    const val SHARPNESS_CHANGE = "sharpness change" // Отмечаем, вносил ли пользователь изменения в опции "Резкость"

    // Отмечаем, вносил ли пользователь изменения в опции "Смешивание цветов"
    const val MIXING_COLORS_CHANGE = "mixing colors change"
    const val BLUR_CHANGE = "blur change" // Отмечаем, вносил ли пользователь изменения в опции "Размытие"

    const val DRAWING_ADD = "drawing add"
    const val TEXT_ADD = "text add"
    const val STICKERS_ADD = "stickers add"
    const val STICKERS_QUANTITY = "stickers quantity"

    const val UNDO_USE = "undo use"
    const val REDO_USE = "redo use"
}

enum class AmplitudeEditorTypeProperty(val property: String) : AmplitudeProperty {

    PHOTO("photo"),
    VIDEO("video");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyEditorConst.EDITOR_TYPE
}
