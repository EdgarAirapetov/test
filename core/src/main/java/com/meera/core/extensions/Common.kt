package com.meera.core.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Looper
import android.provider.Settings
import android.util.TypedValue
import android.view.View
import com.meera.core.R
import java.text.DecimalFormat
import kotlin.math.ln
import kotlin.math.pow

private const val MAX_MESSAGE_FOR_PRETTY = 1000

fun isMainThread(): Boolean = Looper.myLooper() == Looper.getMainLooper()

fun dpToPx(dp: Int) = (dp * Resources.getSystem().displayMetrics.density).toInt()

fun pxToDp(px: Int) = (px / Resources.getSystem().displayMetrics.density).toInt()

fun spToPx(sp: Float) = sp * Resources.getSystem().displayMetrics.scaledDensity

val Int.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()

val Int.sp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()

val Float.dp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )

val Float.sp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this,
        Resources.getSystem().displayMetrics
    )

val Int.px: Int
    get() = ((this / Resources.getSystem().displayMetrics.density) + 0.5f).toInt()

val Float.px: Float
    get() = (this / Resources.getSystem().displayMetrics.density) + 0.5f

@SuppressLint("HardwareIds")
fun getHardwareId(act: Activity): String {
    try {
        val id = Settings.Secure.getString(
            act.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        id?.let {
            return it
        } ?: run {
            return ""
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return ""
    }
}

@SuppressLint("HardwareIds")
fun getHardwareId(context: Context): String {
    try {
        val id = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        id?.let {
            return it
        } ?: run {
            return ""
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return ""
    }
}

@Deprecated("Please use \"tryCatch()\" with catch-block")
inline fun tryCatch(block: () -> Unit) {
    try {
        block()
    } catch (e: Exception) {
        // Timber.e("TRY_CATCH ________________: ${e.simpleName}: ${e.message}") // TODO: Disabled
        e.printStackTrace()
    }
}

@Deprecated("Please use \"tryCatch()\" with catch-block")
inline fun <T> tryCatch(blockTry: () -> T, blockCatch: () -> T): T {
    return try {
        blockTry.invoke()
    } catch (e: Exception) {
        //Timber.e("TRY_CATCH ________________: ${e.simpleName}: ${e.message}")
        e.stackTrace.forEach {
            if (it != null) {
                //Timber.e("TRY_CATCH: $it")
            }
        }
        blockCatch()
    }
}


fun Int.isTrue(): Boolean = this == 1

fun Int.isFalse(): Boolean = this == 0

fun Int.toBooleanOrNull(): Boolean? {
    return when (this) {
        0 -> false
        1 -> true
        else -> null
    }
}

fun Boolean.toInt(): Int = if (this) 1 else 0

fun Boolean?.toInt(): Int = if (this == true) 1 else 0

fun Boolean?.toFloat(): Float = if (this == true) 1f else 0f

fun Boolean?.isTrue(): Boolean = this == true

fun Boolean?.isNotTrue(): Boolean = this != true

fun Boolean?.isFalse(): Boolean = this == false

fun Boolean?.orFalse(): Boolean = this ?: false

fun Boolean?.isNotFalse(): Boolean = this != false

fun Int?.toBoolean(): Boolean = this == 1

fun lerp(start: Float, end: Float, fraction: Float): Float {
    return start + (end - start) * fraction
}

fun Boolean.visibleGone(): Int = if (this) View.VISIBLE else View.GONE

fun Boolean.visibleInvisible(): Int = if (this) View.VISIBLE else View.INVISIBLE

fun Long.asCountString(postfixData: CountStringPostfixData = CountStringPostfixData()): String {
    return when {
        this >= 10_000 -> {
            val (divider, postfix) = if (this >= 1_000_000) {
                1_000_000.0 to postfixData.million
            } else {
                1_000.0 to postfixData.thousand
            }
            DecimalFormat(".#")
                .apply {
                    decimalFormatSymbols = decimalFormatSymbols.apply {
                        decimalSeparator = ','
                    }
                }
                .format(this / divider)
                .replace(",0", "")
                .plus(postfix)
        }

        this in (1000..9999) -> {
            DecimalFormat()
                .apply {
                    decimalFormatSymbols = decimalFormatSymbols.apply {
                        groupingSeparator = ' '
                    }
                }
                .format(this)
        }

        this >= 0 -> this.toString()
        else -> ""
    }
}

data class CountStringPostfixData(
    val thousand: String = " ТЫС.",
    val million: String = " МЛН"
)

fun Int.asPrettyCount(): String {
    val count = this
    if (count < MAX_MESSAGE_FOR_PRETTY) return count.toString()
    val exp = (ln(count.toDouble()) / ln(MAX_MESSAGE_FOR_PRETTY.toDouble())).toInt()
    return String.format("%.1f%c", count / MAX_MESSAGE_FOR_PRETTY.toDouble().pow(exp.toDouble()), "kMGTPE"[exp - 1])
}

fun Long.fromMillisToSec(): Long = this / 1000

fun Intent.isLauncher(): Boolean {
    return action == Intent.ACTION_MAIN
        && hasCategory(Intent.CATEGORY_LAUNCHER)
}

fun Int.toMinOneThousandSubscribersCountStr(context: Context): String {
    val (divider, postfix) = if (this >= 1_000_000) {
        1_000_000.0 to context.getString(R.string.million_subscribers)
    } else {
        1_000.0 to context.getString(R.string.thousand_subscribers)
    }
    val decimal = DecimalFormat(".#")
        .apply {
            decimalFormatSymbols = decimalFormatSymbols.apply {
                decimalSeparator = ','
            }
        }
        .format(this / divider)
        .replace(",0", "")
    return decimal.plus(postfix)
}


fun IntRange.convert(number: Int, target: IntRange): Int {
    val ratio = number.toFloat() / (endInclusive - start)
    return (ratio * (target.endInclusive - target.start)).toInt()
}

fun Int.toHexWithoutAlpha(): String {
    return String.format("#%06X", 0xFFFFFF and this)
}
