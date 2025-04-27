package com.numplates.nomera3.modules.baseCore.helper.amplitude.editor

import android.net.Uri
import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.meera.application_api.analytic.addProperty
import com.meera.core.utils.files.FileManager
import com.meera.core.utils.files.FileUtilsImpl.Companion.MEDIA_TYPE_VIDEO
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.noomeera.nmrmediatools.NMRVideoAmplitude
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import javax.inject.Inject

interface AmplitudeEditor {
    /**
     * Отмечаем, когда пользователь отредактировал медиа.
     */
    fun videoEditorAction(
        nmrAmplitude: NMRVideoAmplitude,
        editorParams: AmplitudeEditorParams = AmplitudeEditorParams(),
    )

    fun photoEditorAction(
        nmrAmplitude: NMRPhotoAmplitude,
        editorParams: AmplitudeEditorParams = AmplitudeEditorParams(),
    )

    fun editorOpenAction(
        where: AmplitudePropertyWhere? = AmplitudePropertyWhere.OTHER,
        automaticOpen: Boolean = false,
        type: AmplitudeEditorTypeProperty?,
    )

    fun getEditorType(uri: Uri): AmplitudeEditorTypeProperty

}

class AmplitudeEditorParams(
    val where: AmplitudePropertyWhere? = AmplitudePropertyWhere.OTHER,
    val automaticOpen: Boolean = false
)

class AmplitudeHelperEditorImpl @Inject constructor(
    private val getUserUidUseCase: GetUserUidUseCase,
    private val delegate: AmplitudeEventDelegate,
    private val fileManager: FileManager
) : AmplitudeEditor {

    override fun getEditorType(uri: Uri): AmplitudeEditorTypeProperty =
        when (fileManager.getMediaType(uri)) {
            MEDIA_TYPE_VIDEO -> AmplitudeEditorTypeProperty.VIDEO
            else -> AmplitudeEditorTypeProperty.PHOTO
        }

    override fun editorOpenAction(
        where: AmplitudePropertyWhere?,
        automaticOpen: Boolean,
        type: AmplitudeEditorTypeProperty?
    ) {
        delegate.logEvent(
            eventName = EditorEventName.EDITOR_OPEN,
            properties = {
                it.apply {
                    addProperty(where ?: AmplitudePropertyWhere.OTHER)
                    addProperty(AmplitudePropertyEditorConst.AUTOMATIC_OPEN, automaticOpen)
                    type?.let(::addProperty)
                }
            }
        )
    }

    override fun videoEditorAction(
        nmrAmplitude: NMRVideoAmplitude,
        editorParams: AmplitudeEditorParams
    ) {
        delegate.logEvent(
            eventName = EditorEventName.VIDEO_EDITOR,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyEditorConst.USER_ID, getUserUidUseCase.invoke())
                    addProperty(editorParams.where ?: AmplitudePropertyWhere.OTHER)
                    addProperty(AmplitudePropertyEditorConst.AUTOMATIC_OPEN, editorParams.automaticOpen)
                    addProperty(AmplitudePropertyEditorConst.MAIN_CHANGE, nmrAmplitude.mainChange)
                    addProperty(AmplitudePropertyEditorConst.WHAT_FILTER_CHOOSE, nmrAmplitude.whatFilterChoose)
                    addProperty(AmplitudePropertyEditorConst.DURATION_CHANGE, nmrAmplitude.durationChange)
                    addProperty(AmplitudePropertyEditorConst.WHAT_DURATION_CHOOSE, nmrAmplitude.whatDurationChoose)
                    addProperty(AmplitudePropertyEditorConst.SOUND_CHANGE, nmrAmplitude.soundChange)
                    addProperty(AmplitudePropertyEditorConst.DRAWING_ADD, nmrAmplitude.drawingAdd)
                    addProperty(AmplitudePropertyEditorConst.TEXT_ADD, nmrAmplitude.textAdd)
                    addProperty(AmplitudePropertyEditorConst.STICKERS_ADD, nmrAmplitude.stickersAdd)
                    addProperty(AmplitudePropertyEditorConst.STICKERS_QUANTITY, nmrAmplitude.stickersQuantity)
                }
            }
        )
    }

    override fun photoEditorAction(
        nmrAmplitude: NMRPhotoAmplitude,
        editorParams: AmplitudeEditorParams
    ) {
        val processingAmplitude = nmrAmplitude.nmrPhotoProcessingAmplitude
        delegate.logEvent(
            eventName = EditorEventName.PHOTO_EDITOR,
            properties = { properties ->
                properties.apply {
                    addProperty(AmplitudePropertyEditorConst.USER_ID, getUserUidUseCase.invoke())
                    addProperty(editorParams.where ?: AmplitudePropertyWhere.OTHER)
                    addProperty(AmplitudePropertyEditorConst.AUTOMATIC_OPEN, editorParams.automaticOpen)
                    addProperty(AmplitudePropertyEditorConst.MAIN_CHANGE, nmrAmplitude.mainChange)
                    addProperty(AmplitudePropertyEditorConst.WHAT_FILTER_CHOOSE, nmrAmplitude.whatFilterChoose)
                    addProperty(AmplitudePropertyEditorConst.INTENSITY_FILTER_CHANGE, nmrAmplitude.intensityFilterChange)
                    addProperty(AmplitudePropertyEditorConst.WHAT_CROP_PHOTO, nmrAmplitude.whatCropPhoto)
                    addProperty(AmplitudePropertyEditorConst.ROTATION_SCALE_USE, nmrAmplitude.rotationScaleUse)
                    addProperty(AmplitudePropertyEditorConst.TURN_USE, nmrAmplitude.turnUse)
                    addProperty(AmplitudePropertyEditorConst.VERTICAL_REFLECTION_USE, nmrAmplitude.verticalReflectionUse)
                    addProperty(AmplitudePropertyEditorConst.HORIZONTAL_REFLECTION_USE, nmrAmplitude.horizontalReflectionUse)
                    addProperty(AmplitudePropertyEditorConst.PROCESSING_CHANGE, processingAmplitude.processingChange)
                    addProperty(AmplitudePropertyEditorConst.AUTO_IMPROVEMENT_CHANGE, processingAmplitude.autoImprovementChange)
                    addProperty(AmplitudePropertyEditorConst.BRIGHTNESS_CHANGE, processingAmplitude.brightnessChange)
                    addProperty(AmplitudePropertyEditorConst.EXPOSITION_CHANGE, processingAmplitude.expositionChange)
                    addProperty(AmplitudePropertyEditorConst.CONTRAST_CHANGE, processingAmplitude.contrastChange)
                    addProperty(AmplitudePropertyEditorConst.GAMMA_CHANGE, processingAmplitude.gammaChange)
                    addProperty(AmplitudePropertyEditorConst.DEFINITION_CHANGE, processingAmplitude.definitionChange)
                    addProperty(AmplitudePropertyEditorConst.TEMPERATURE_CHANGE, processingAmplitude.temperatureChange)
                    addProperty(AmplitudePropertyEditorConst.SETURATION_CHANGE, processingAmplitude.saturationChange)
                    addProperty(AmplitudePropertyEditorConst.ILLUMINATION_CHANGE, processingAmplitude.illuminationChange)
                    addProperty(AmplitudePropertyEditorConst.SHADOWS_CHANGE, processingAmplitude.shadowsChange)
                    addProperty(AmplitudePropertyEditorConst.VIGNETTE_CHANGE, processingAmplitude.vignetteChange)
                    addProperty(AmplitudePropertyEditorConst.SHARPNESS_CHANGE, processingAmplitude.sharpnessChange)
                    addProperty(AmplitudePropertyEditorConst.MIXING_COLORS_CHANGE, processingAmplitude.mixingColorsChange)
                    addProperty(AmplitudePropertyEditorConst.BLUR_CHANGE, processingAmplitude.blurChange)
                    addProperty(AmplitudePropertyEditorConst.DRAWING_ADD, nmrAmplitude.drawingAdd)
                    addProperty(AmplitudePropertyEditorConst.TEXT_ADD, nmrAmplitude.textAdd)
                    addProperty(AmplitudePropertyEditorConst.STICKERS_ADD, nmrAmplitude.stickersAdd)
                    addProperty(AmplitudePropertyEditorConst.STICKERS_QUANTITY, nmrAmplitude.stickersQuantity)
                    addProperty(AmplitudePropertyEditorConst.UNDO_USE, nmrAmplitude.undoUse)
                    addProperty(AmplitudePropertyEditorConst.REDO_USE, nmrAmplitude.redoUse)
                }
            }
        )
    }
}
