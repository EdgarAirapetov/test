package com.noomeera.nmravatarssdk.ui

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

fun AlertDialog.Builder.positiveButton(
    text: String = "OK",
    handleClick: (which: Int) -> Unit = {}
) {
    this.setPositiveButton(text) { _, which -> handleClick(which) }
}

fun AlertDialog.Builder.negativeButton(
    text: String = "Cancel",
    handleClick: (dialogInterface: DialogInterface, which: Int) -> Unit = { _: DialogInterface, _: Int -> }
) {
    this.setNegativeButton(text) { dialogInterface, which -> handleClick(dialogInterface, which) }
}
