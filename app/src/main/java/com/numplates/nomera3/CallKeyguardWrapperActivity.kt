package com.numplates.nomera3

import android.app.KeyguardManager
import android.os.Bundle
import android.view.WindowManager

/**
 * An activity that is used for displaying a call screen on a locked device.
 * This moves the keyguard code out of the main [Act]
 *
 * This activity is ONLY supposed to open on the lock screen and shouldn't be used elsewhere
 */
class CallKeyguardWrapperActivity : Act() {

    override fun onCreate(savedInstanceState: Bundle?) {
        dismissKeyguardForCall(true)
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        dismissKeyguardForCall(false)
        super.onDestroy()
    }

    private fun dismissKeyguardForCall(shouldDismiss: Boolean) {
        val flagsDisableKeyguard = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        val keyguardManager = applicationContext.getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        val keyguardLock = keyguardManager.newKeyguardLock(KEYGUARD_LOCK_TAG)

        if (shouldDismiss) {
            window.apply {
                setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG)
                addFlags(flagsDisableKeyguard)
            }
            keyguardLock?.disableKeyguard()
        } else {
            window.clearFlags(flagsDisableKeyguard)
            keyguardLock.reenableKeyguard()
        }
    }

    override fun onCallFinished() {
        dismissKeyguardForCall(false)
        finish()
    }

}
