package com.meera.core.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.meera.core.R
import timber.log.Timber

/**
 * Send email Noomeera tech support
 */
fun Activity.writeToTechSupport() {
    val mailto = "mailto:${getString(R.string.auth_write_to_support_email)}" +
            "?cc=" +
            "&subject=" +
            "&body=" + Uri.encode(getString(R.string.auth_write_to_support_message_plain,
        Build.MANUFACTURER + " - " + Build.MODEL,
        Build.VERSION.SDK_INT.toString(),
        this.getAppVersionName(),
        getHardwareId(this)))

    val emailIntent = Intent(Intent.ACTION_SENDTO)
    emailIntent.data = Uri.parse(mailto)
    try {
        startActivity(Intent.createChooser(emailIntent, getString(R.string.general_send_email)))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.openSettingsScreen() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", this.packageName, null)
    intent.data = uri
    startActivity(intent)
}

fun Context.getAppVersionName(): String {
    return try {
        packageManager.getPackageInfo(packageName, 0).versionName
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        String.empty()
    }
}

fun Context.getAppVersionCode(): Int {
    return try {
        val pInfo: PackageInfo = this.packageManager.getPackageInfo(this.packageName, 0)
        pInfo.versionCode
    } catch (e: Exception) {
        e.printStackTrace()
        -1
    }
}

@SuppressLint("MissingPermission")
fun Context.vibrate(){
    Timber.e("Dbg: context.vibrate 1")
    val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(VIBRATOR_SERVICE) as Vibrator
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vib.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        vib.vibrate(30)
    }
}

@SuppressLint("MissingPermission")
fun Context.lightVibrate(){
    Timber.e("Dbg: context.vibrate 2")
    val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(VIBRATOR_SERVICE) as Vibrator
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vib.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        vib.vibrate(30)
    }
}

fun Context.showKeyboard(view: View) {
    view.requestFocus()
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

fun Context.hideKeyboard(view: View) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
    view.clearFocus()
}

fun Activity.toast(message: CharSequence, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, message, duration).show()
}

fun Context?.toast(text: String?, duration: Int = Toast.LENGTH_SHORT) =
    this?.let { Toast.makeText(it, text, duration).show() }

fun Context?.toast(@StringRes textId: Int, duration: Int = Toast.LENGTH_SHORT) =
    this?.let { Toast.makeText(it, textId, duration).show() }

fun Context?.getColorCompat(@ColorRes color: Int) = this?.let { ContextCompat.getColor(it, color) }
    ?: run { 0 }

fun Context?.getDrawableCompat(@DrawableRes drawableRes: Int) =
    this?.let { ContextCompat.getDrawable(it, drawableRes) }

inline val Context.displayWidth: Int
    get() = resources.displayMetrics.widthPixels

inline val Context.displayHeight: Int
    get() = resources.displayMetrics.heightPixels

fun Context?.createProgressBar(
    strokeWidth: Int = 3.dp,
    radius: Int = 16.dp
): CircularProgressDrawable? {
    this?.let {
        val circularProgressDrawable = CircularProgressDrawable(this)
        circularProgressDrawable.strokeWidth = strokeWidth.toFloat()
        circularProgressDrawable.centerRadius = radius.toFloat()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            circularProgressDrawable.colorFilter =
                BlendModeColorFilter(this.getColorCompat(R.color.ui_purple), BlendMode.SRC_ATOP)
        } else {
            circularProgressDrawable.setColorFilter(this.getColorCompat(R.color.ui_purple), PorterDuff.Mode.SRC_ATOP)
        }
        circularProgressDrawable.start()
        return circularProgressDrawable
    } ?: run { return null }
}

fun Context?.getStatusBarHeight(): Int {
    this?.let {
        val resources = this.resources
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    } ?: run { return 0 }
}

fun Context?.getNavigationBarHeight(): Int {
    this?.let {
        val resources = this.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    } ?: run { return 0 }
}

fun Context?.getToolbarHeight(): Int {
    this?.let {
        val attrs = intArrayOf(R.attr.actionBarSize)
        val ta = this.obtainStyledAttributes(attrs)
        val toolBarHeight = ta.getDimensionPixelSize(0, -1)
        ta.recycle()
        return toolBarHeight
    } ?: run { return 0 }
}

fun Context?.getSilentState(): Boolean {
    val am = this?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    return when (am?.ringerMode) {
        AudioManager.RINGER_MODE_SILENT -> true
        AudioManager.RINGER_MODE_VIBRATE -> true
        AudioManager.RINGER_MODE_NORMAL -> false
        else -> false
    }
}

fun Context.color(@ColorRes idRes: Int): Int =
    ContextCompat.getColor(this, idRes)

fun Context.colorStateList(@ColorRes idRes: Int): ColorStateList? =
    ContextCompat.getColorStateList(this, idRes)

fun Context.string(@StringRes idRes: Int): String =
    this.getString(idRes)

fun Context.string(@StringRes idRes: Int, vararg vars: Any?): String =
    this.getString(idRes, *vars)

fun Context.text(@StringRes idRes: Int): CharSequence =
    this.getText(idRes)

fun Context.pluralString(@PluralsRes idRes: Int,
                         quantity: Int,
                         vararg vars: Any?): String =
    if (vars.isEmpty()) {
        this.resources.getQuantityString(idRes, quantity, quantity)
    } else {
        this.resources.getQuantityString(idRes, quantity, *vars)
    }

fun Context?.stringNullable(@StringRes idRes: Int): String =
    this?.getString(idRes) ?: ""

fun Context.drawable(@DrawableRes drawableRes: Int): Drawable? =
    ContextCompat.getDrawable(this, drawableRes)

fun ImageView?.applyColorFilter(context: Context?, @ColorRes idRes: Int,
                                mode: PorterDuff.Mode = PorterDuff.Mode.SRC_IN) {
    context ?: return
    this ?: return
    setColorFilter(context.color(idRes), mode)
}

fun Context.copyToClipBoard(text: String, success: () -> Unit = {}) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("clip_data", text)
    clipboard.setPrimaryClip(clip)
    success.invoke()
}

fun Context.getAuthority() = "${applicationContext.packageName}.fileprovider"

fun Context.getProviderAuthority() = "${applicationContext.packageName}.provider"

