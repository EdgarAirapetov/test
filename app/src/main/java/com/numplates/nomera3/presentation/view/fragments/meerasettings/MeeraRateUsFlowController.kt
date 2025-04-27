package com.numplates.nomera3.presentation.view.fragments.meerasettings

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResultListener

const val KEY_RATE_US_DIALOG_SEND_GRADE = "send grade to back"
private const val TAG_MEERA_RATE_US_DIALOG = "rate us dialog"
private const val TAG_MEERA_RATE_US_GOOGLE_DIALOG = "rate us to google"

class MeeraRateUsFlowController {

    private val rateUsToOurBackendDialog by lazy(LazyThreadSafetyMode.NONE) { MeeraRateUsDialog() }
    private val rateUsToGoogleDialog by lazy(LazyThreadSafetyMode.NONE) { MeeraRateUsToGooglePlayDialog() }
    fun startRateUsFlow(childFragmentManager: FragmentManager) {
        rateUsToOurBackendDialog.show(childFragmentManager, TAG_MEERA_RATE_US_DIALOG)
        rateUsToOurBackendDialog.setFragmentResultListener(KEY_RATE_US_DIALOG_SEND_GRADE) { _, _ ->
            rateUsToGoogleDialog.show(childFragmentManager, TAG_MEERA_RATE_US_GOOGLE_DIALOG)
        }
    }

}
