package com.meera.core.permission

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.meera.core.extensions.orFalse
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.CompositeDisposable

const val PERMISSION_MEDIA_CODE = 1042

class PermissionDelegate(
    var activity: Activity?,
    viewLifecycleOwner: LifecycleOwner
) : DefaultLifecycleObserver {

    private val compositeDisposable = CompositeDisposable()
    private val rxPermissions = activity?.let(::RxPermissions)

    init {
        viewLifecycleOwner.lifecycle.addObserver(this)
    }

    fun setPermissions(listener: Listener?, permission: String?, vararg permissions: String?) {
        rxPermissions?.let {
            val newPermissions = arrayOfNulls<String>(permissions.size + 1)
            System.arraycopy(permissions, 0, newPermissions, 0, permissions.size)
            newPermissions[permissions.size] = permission
            compositeDisposable.add(
                rxPermissions.request(*newPermissions)
                    .subscribe(
                        { granted: Boolean ->
                            if (granted) {
                                listener?.onGranted()
                            } else {
                                listener?.onDenied()
                            }
                        }, { error: Throwable? -> listener?.onError(error) }
                    )
            )
        }
    }

    private fun Context.checkingPermissionIsGranted(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) == PERMISSION_GRANTED

    private fun Activity?.shouldOpenSettings(permission: String): Boolean =
        this?.shouldShowRequestPermissionRationale(permission).orFalse()

    fun checkMediaPermissions(listener: Listener?) {
        val context = activity ?: return

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                if (context.checkingPermissionIsGranted(READ_MEDIA_VISUAL_USER_SELECTED)) {
                    listener?.onGranted()
                } else {
                    val openSettings = activity?.shouldOpenSettings(READ_MEDIA_VISUAL_USER_SELECTED).orFalse()
                    if (openSettings) listener?.needOpenSettings() else listener?.onDenied()
                }
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                if ((context.checkingPermissionIsGranted(READ_MEDIA_IMAGES) ||
                        context.checkingPermissionIsGranted(READ_MEDIA_VIDEO))
                ) {
                    listener?.onGranted()
                } else {
                    val openSettingImage = activity?.shouldOpenSettings(READ_MEDIA_IMAGES).orFalse()
                    val openSettingVideo = activity?.shouldOpenSettings(READ_MEDIA_VIDEO).orFalse()

                    if (openSettingImage || openSettingVideo) listener?.needOpenSettings() else listener?.onDenied()
                }
            }

            else -> {
                if (context.checkingPermissionIsGranted(WRITE_EXTERNAL_STORAGE)) {
                    listener?.onGranted()
                } else {
                    val openSettings = activity?.shouldOpenSettings(WRITE_EXTERNAL_STORAGE).orFalse()
                    if (openSettings) listener?.needOpenSettings() else listener?.onDenied()
                }
            }
        }
    }

    fun setMediaPermissions() {
        // Permission request logic
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity?.requestPermissions(
                arrayOf(
                    READ_MEDIA_IMAGES,
                    READ_MEDIA_VIDEO,
                    READ_MEDIA_VISUAL_USER_SELECTED
                ), PERMISSION_MEDIA_CODE
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity?.requestPermissions(
                arrayOf(
                    READ_MEDIA_IMAGES,
                    READ_MEDIA_VIDEO
                ), PERMISSION_MEDIA_CODE
            )
        } else {
            activity?.requestPermissions(arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE), PERMISSION_MEDIA_CODE)
        }
    }

    @SuppressLint("NewApi")
    fun setPermissionsWithSettingsOpening(listener: Listener?, permission: String?, vararg permissions: String?) {
        rxPermissions?.let {
            val newPermissions = arrayOfNulls<String>(permissions.size + 1)
            System.arraycopy(permissions, 0, newPermissions, 0, permissions.size)
            newPermissions[permissions.size] = permission
            compositeDisposable.add(
                rxPermissions.request(*newPermissions)
                    .subscribe(
                        { granted: Boolean ->
                            if (granted) {
                                listener?.onGranted()
                                return@subscribe
                            }

                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                                listener?.onDenied()
                                return@subscribe
                            }

                            val nonNullPermissions = newPermissions.requireNoNulls()
                            var needOpenSettings = false
                            for (checkingPermission in nonNullPermissions) {
                                val shouldShowRequestPermissionRationale =
                                    activity?.shouldShowRequestPermissionRationale(checkingPermission) ?: false

                                if (!shouldShowRequestPermissionRationale) {
                                    needOpenSettings = true
                                    break
                                }
                            }

                            if (needOpenSettings) listener?.needOpenSettings() else listener?.onDenied()
                        }, { error: Throwable? -> listener?.onError(error) }
                    )
            )
        }
    }

    fun checkPermissions(listener: Listener?, permission: String?, vararg permissions: String?) {
        rxPermissions?.let {
            val newPermissions = arrayOfNulls<String>(permissions.size + 1)
            System.arraycopy(permissions, 0, newPermissions, 0, permissions.size)
            newPermissions[permissions.size] = permission
            val nonNullPermissions = newPermissions.requireNoNulls()
            var needOpenSettings = false
            var allGranted = true
            for (checkingPermission in nonNullPermissions) {
                if (!rxPermissions.isGranted(checkingPermission)) {
                    allGranted = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val shouldShowRequestPermissionRationale =
                            activity?.shouldShowRequestPermissionRationale(checkingPermission) ?: false

                        if (shouldShowRequestPermissionRationale) {
                            needOpenSettings = true
                            break
                        }
                    }
                }
            }

            if (allGranted) {
                listener?.onGranted()
                return
            }

            if (needOpenSettings) listener?.needOpenSettings() else listener?.onDenied()
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        compositeDisposable.clear()
        activity = null
    }

    interface Listener {
        fun onGranted()
        fun onDenied() = Unit
        fun needOpenSettings() = Unit
        fun onError(error: Throwable?) = Unit
    }

}
