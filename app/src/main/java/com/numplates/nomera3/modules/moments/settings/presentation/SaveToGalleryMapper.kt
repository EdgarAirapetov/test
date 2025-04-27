package com.numplates.nomera3.modules.moments.settings.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.toInt
import com.numplates.nomera3.modules.usersettings.ui.models.PrivacySettingUiModel
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import javax.inject.Inject

class SaveToGalleryMapper @Inject constructor(context: Context) {

    private val _context = context.applicationContext
    private val settingKey = SettingsKeyEnum.SAVE_MOMENTS_TO_GALLERY.key

    fun mapSaveToGallerySetting(setting: PrivacySettingUiModel): PrivacySettingUiModel {
        return if (setting.key == settingKey) {
            setting.copy(value = (setting.value.toBoolean() && isSaveToExternalGranted()).toInt())
        } else {
            setting
        }
    }

    private fun isSaveToExternalGranted(): Boolean {
        return saveToExternalPermissions().all { permission ->
            ContextCompat.checkSelfPermission(_context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun saveToExternalPermissions(): List<String> {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            emptyList()
        }
    }
}
