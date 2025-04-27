package com.meera.core.extensions

import android.widget.Toast
import androidx.fragment.app.Fragment

fun Fragment.toast(message: CharSequence, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this.context, message, duration).show()
}
