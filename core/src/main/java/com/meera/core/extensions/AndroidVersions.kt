package com.meera.core.extensions

import android.os.Build

fun apiAtLeast26() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
