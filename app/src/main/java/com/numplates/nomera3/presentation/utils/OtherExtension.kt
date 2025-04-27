package com.numplates.nomera3.presentation.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.meera.core.extensions.dpToPx
import com.numplates.nomera3.presentation.view.widgets.numberplateview.NumberPlateEditView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

inline fun <reified T : ViewModel> Fragment.viewModelsFactory(crossinline viewModelInitialization: () -> T): Lazy<T> {
    return viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return viewModelInitialization.invoke() as T
            }
        }
    }
}

fun runOnUiThread(work: () -> Unit) {
    GlobalScope.launch(Dispatchers.Main) {
        work()
    }
}

fun NumberPlateEditView.setSmallSize(typeId: Int) {
    if (typeId == 1) {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(
            dpToPx(-104),
            dpToPx(-22),
            dpToPx(-104),
            dpToPx(-22)
        )
        this.layoutParams = params
    } else if (typeId == 2) {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(
            dpToPx(-62),
            dpToPx(-42),
            dpToPx(-62),
            dpToPx(-42)
        )
        this.layoutParams = params
    }

    this.scaleX = 0.3f
    this.scaleY = 0.3f
}

fun Fragment.sendUserToAppSettings() {
    requireActivity().sendUserToAppSettings()
}

fun Activity.sendUserToAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri: Uri = Uri.fromParts("package", packageName, null)
    intent.data = uri
    startActivity(intent)
}

fun String?.isEditorTempFile(cacheDir: File): Boolean =
    this?.startsWith(cacheDir.path) ?: false

@kotlin.jvm.Throws(ActivityNotFoundException::class)
fun Activity.sendUserToAppSettingsForResult(requestCode: Int) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri: Uri = Uri.fromParts("package", packageName, null)
    intent.data = uri
    startActivityForResult(intent, requestCode)
}
