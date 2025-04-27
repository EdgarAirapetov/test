package com.meera.core.extensions

import android.annotation.SuppressLint
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import androidx.annotation.ColorInt

/**
 * range = range.first..range.last + 1 - Костыль/ Ренд всегда выдает длину на 1 меньше
 * */
fun String.addClickableText(@ColorInt color: Int, keyStr: String, onClickListener: ()->Unit): SpannableStringBuilder? {
    var spnStr = SpannableStringBuilder(this)
    var range = keyStr.toRegex().find(spnStr, 0)?.range ?: return spnStr
    range = range.first..range.last + 1
    return spnStr.apply {
        if (range.first < 0 || range.last > count()) return@apply
        val clickableSpan =
            object : ClickableSpan() {
                override fun onClick(p0: View) {
                    onClickListener.invoke()
                }

                override fun updateDrawState(tp: TextPaint) {
                    tp.isUnderlineText = false
                }
            }

        color(color, range)
        setSpanExclusive(clickableSpan, range)
    }
}


private val CHAR_POOL: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

@Suppress("detekt:FunctionOnlyReturningConstant")
fun String.Companion.empty() = ""

fun String.isEmail(): Boolean {
    val p = "^(\\w)+(\\.\\w+)*@(\\w)+((\\.\\w+)+)\$".toRegex()
    return matches(p)
}

fun String.countLines() = lines().count()

fun String.isMultiline() = this.countLines() > 1

@SuppressLint("DefaultLocale")
fun String.equalsIgnoreCase(other: String) = this.lowercase().contentEquals(other.lowercase())

/**
 * Extension method to get ClickableSpan.
 * e.g.
 * val loginLink = getClickableSpan(context.getColorCompat(R.color.colorAccent), { })
 */
fun getClickableSpan(color: Int, action: (view: View) -> Unit): ClickableSpan {
    return object : ClickableSpan() {
        override fun onClick(view: View) {
            action(view)
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = color
        }
    }
}


fun generateLongText(size: Int, wordSize: Int = 7): String {
    var text = String.empty()
    (0..size).forEachIndexed { index, _ ->
        if (index !=0 && index % wordSize == 0) {
            text += " "
        }
        text += "a"
    }
    return text
}

fun String.needToUpdateStr(serverVersion: String?): Boolean {
    //if has empty string dont show dialog
    if (this.isEmpty() || serverVersion.isNullOrEmpty()) {
        return false
    }

    // split version
    var current = this.split(".")
    var server = serverVersion.split(".")

    // if we dont have current or server version dont show dialog
    if (current.isNullOrEmpty() || server.isNullOrEmpty()) {
        try {
            if (current.isNullOrEmpty()) {
                val intCurrent = this.toInt()
                current = listOf(intCurrent.toString())
            }
            if (server.isNullOrEmpty()) {
                val intServer = serverVersion.toInt()
                server = listOf(intServer.toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    //if appver length > server length it might be smth went wrong on server
    var indices = current.size

    if (current.size > server.size) indices = server.size
    else if (current.size < server.size) indices = current.size

    try {
        // checking each param with each
        for (i in 0 until indices) {
            //if current less then server show dialog
            if (current[i].toInt() < server[i].toInt()) {
                return true
            } else if (current[i].toInt() > server[i].toInt())
                return false
        }

        // server version higher then current
        if (current.size < server.size) {
            return true
            //return if our size
        } else if (current.size > server.size) return false

    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
    return false
}

fun generateNBSP(count: Int): String {
    var str = String.empty()
    repeat(count) { str += "\u00A0" }
    return str
}

fun randomString(length: Int) =
    (1..length)
        .map { i -> kotlin.random.Random.nextInt(0, CHAR_POOL.size) }
        .map(CHAR_POOL::get)
        .joinToString("")

const val LOREM_IPSUM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore " +
        "et dolore magna aliqua. Quis blandit turpis cursus in hac habitasse platea. " +
        "Orci phasellus egestas tellus rutrum tellus pellentesque eu tincidunt tortor." +
        " Ac odio tempor orci dapibus ultrices. In nisl nisi scelerisque eu ultrices vitae. " +
        "Bibendum neque egestas congue quisque egestas diam. Vitae auctor eu augue ut lectus arcu bibendum at varius. " +
        "Sit amet consectetur adipiscing elit duis tristique sollicitudin nibh. " +
        "Porttitor eget dolor morbi non arcu risus quis varius quam. Quisque egestas diam in arcu cursus euismod. " +
        "Pellentesque habitant morbi tristique senectus et netus et malesuada fames. " +
        "Sit amet aliquam id diam. Cursus mattis molestie a iaculis at erat pellentesque. " +
        "Aliquam vestibulum morbi blandit cursus risus at ultrices. Diam quis enim lobortis scelerisque fermentum dui faucibus in. " +
        "In hac habitasse platea dictumst. Eu ultrices vitae auctor eu augue. " +
        "Pharetra et ultrices neque ornare aenean euismod elementum nisi. " +
        "Eget arcu dictum varius duis at consectetur lorem. Duis at consectetur lorem donec massa sapien faucibus.\n" +
        "Convallis tellus id interdum velit. Ipsum dolor sit amet consectetur adipiscing elit. Felis bibendum ut tristique et." +
        " Tempor id eu nisl nunc. Consequat mauris nunc congue nisi. Morbi tincidunt ornare massa eget egestas. " +
        "Augue eget arcu dictum varius duis at consectetur. Nisl tincidunt eget nullam non nisi est sit amet. " +
        "Id neque aliquam vestibulum morbi blandit cursus risus at. " +
        "Bibendum arcu vitae elementum curabitur vitae nunc sed velit. " +
        "Amet venenatis urna cursus eget nunc scelerisque viverra."

