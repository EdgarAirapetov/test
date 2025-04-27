package com.numplates.nomera3.modules.baseCore.ui.permission

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.numplates.nomera3.Act
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.CompositeDisposable

@Deprecated("Use permissions delegate from CORE")
class PermissionDelegate(act: Act?, viewLifecycleOwner: LifecycleOwner) : DefaultLifecycleObserver {

    private val compositeDisposable = CompositeDisposable()
    private val rxPermissions = act?.let(::RxPermissions)

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

    fun isGranted(vararg permissions: String?): Boolean {
        permissions.forEach {
            val isGranted = rxPermissions?.isGranted(it) ?: false
            if (isGranted.not()) {
                return false
            }
        }

        return true
    }

    override fun onDestroy(owner: LifecycleOwner) {
        compositeDisposable.clear()
    }

    interface Listener {
        fun onGranted()
        fun onDenied()
        fun onError(error: Throwable?)
    }
}
