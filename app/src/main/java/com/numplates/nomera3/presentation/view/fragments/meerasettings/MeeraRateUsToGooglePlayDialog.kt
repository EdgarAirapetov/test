package com.numplates.nomera3.presentation.view.fragments.meerasettings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraRateUsToGooglePlayDialogBinding

class MeeraRateUsToGooglePlayDialog : UiKitBottomSheetDialog<MeeraRateUsToGooglePlayDialogBinding>() {
    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraRateUsToGooglePlayDialogBinding
        get() = MeeraRateUsToGooglePlayDialogBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCloseBtnListener()
        initContentButtonsListeners()
        setDialogTopTitle()
    }

    private fun setDialogTopTitle() {
        rootBinding?.tvBottomSheetDialogLabel?.text = getString(R.string.meera_rate_us_goole_title)
    }

    private fun initCloseBtnListener() {
        rootBinding?.ivBottomSheetDialogClose?.setThrottledClickListener { this.dismiss() }
    }

    private fun initContentButtonsListeners() {
        contentBinding?.let { binding ->
            with(binding) {
                btnRateUsGoToMarket.setThrottledClickListener { goToGoogleMarket() }
                btnRateUsNotNow.setThrottledClickListener { this@MeeraRateUsToGooglePlayDialog.dismiss() }
            }
        }
    }

    private fun goToGoogleMarket() {
        val marketIntent = Intent(Intent.ACTION_VIEW)
        marketIntent.data = Uri.parse(App.GOOGLE_PLAY_MARKET_URL)
        startActivity(marketIntent)
        dismiss()
    }
}
