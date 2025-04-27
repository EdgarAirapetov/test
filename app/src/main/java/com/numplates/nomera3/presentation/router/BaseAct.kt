package com.numplates.nomera3.presentation.router

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.utils.NTime
import com.numplates.nomera3.presentation.view.utils.PermissionManager
import java.io.Serializable


abstract class BaseAct : AppCompatActivity() {
    val nTime by lazy { NTime(this) }
    val app by lazy { application as App }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nTime.setServerTime(System.currentTimeMillis() / 1000)
    }

    fun requestRuntimePermission(code: Int, permission: Array<String>) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onRequestPermissionsResult(code, permission, PermissionManager.grantedResults(permission))
        } else if (!PermissionManager.checkRuntimePermissions(this, permission)) {
            ActivityCompat.requestPermissions(this, permission, code)
        } else {
            onRequestPermissionsResult(code, permission, PermissionManager.grantedResults(permission))
        }
    }

    fun setLightStatusBarNotTransparent() = setLightStatusBar(R.color.white_1000)

    fun setLightStatusBar(@ColorRes statusBarColor: Int = R.color.colorTransparent) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            window.navigationBarColor = Color.BLACK
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                window.statusBarColor = ContextCompat.getColor(baseContext, R.color.status_bar_semi_transparent)
            } else {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
        } else {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            window.navigationBarColor = ContextCompat.getColor(baseContext, R.color.white_1000)
            window.statusBarColor = ContextCompat.getColor(baseContext, statusBarColor)
        }
    }

    fun setStatusNavigationBarColor(
        @ColorRes statusBarColor: Int = R.color.colorTransparent,
        @ColorRes navigationBarColor: Int
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            window.navigationBarColor = Color.BLACK
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                window.statusBarColor = ContextCompat.getColor(baseContext, R.color.status_bar_semi_transparent)
            } else {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
        } else {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            window.navigationBarColor = ContextCompat.getColor(baseContext, navigationBarColor)
            window.statusBarColor = ContextCompat.getColor(baseContext, statusBarColor)
        }
    }

    fun setDialogColorStatusBar() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                window.statusBarColor = resources.getColor(R.color.colorTransparent)
            } else window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        window.statusBarColor = resources.getColor(R.color.colorTransparent)
        window.navigationBarColor = resources.getColor(R.color.ui_black_60)
    }

    fun setColorStatusBar() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                window.statusBarColor = resources.getColor(R.color.colorTransparent)
            } else window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        window.statusBarColor = resources.getColor(R.color.colorTransparent)
        window.navigationBarColor = Color.BLACK
    }

    fun setColorStatusBarNavLight() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            window.navigationBarColor = Color.BLACK
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                window.statusBarColor = resources.getColor(R.color.colorTransparent)
            } else window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            window.navigationBarColor = resources.getColor(R.color.white_1000)
        }
        window.statusBarColor = resources.getColor(R.color.colorTransparent)
    }

    fun getBundle(vararg args: Arg<*, *>): Bundle {
        val bundle = Bundle()
        for (arg in args) {
            val key = arg.key as String?
            val value = arg.value

            if (value is Boolean) {
                bundle.putBoolean(key, (value as Boolean?)!!)
            }
            if (value is Byte) {
                bundle.putByte(key, (value as Byte?)!!)
            }
            if (value is Char) {
                bundle.putChar(key, (value as Char?)!!)
            }
            if (value is Int) {
                bundle.putInt(key, (value as Int?)!!)
            }
            if (value is Long) {
                bundle.putLong(key, (value as Long?)!!)
            }
            if (value is Float) {
                bundle.putFloat(key, (value as Float?)!!)
            }
            if (value is Double) {
                bundle.putDouble(key, (value as Double?)!!)
            }
            if (value is String) {
                bundle.putString(key, value as String?)
            }
            if (value is Parcelable) {
                bundle.putParcelable(key, value as Parcelable?)
            }
            if (value is Serializable) {
                bundle.putSerializable(key, value as Serializable?)
            }
        }
        return bundle
    }
}
