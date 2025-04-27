package com.numplates.nomera3.modules.maps.ui.geo_popup

import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraDialogGeoPopupBinding
import com.numplates.nomera3.modules.maps.ui.geo_popup.model.GeoPopupAction
import com.numplates.nomera3.modules.maps.ui.geo_popup.model.GeoPopupOrigin
import com.numplates.nomera3.modules.maps.ui.view.MapBottomSheetDialog
import com.numplates.nomera3.presentation.utils.bottomsheet.BottomSheetCloseUtil

class MeeraGeoPopupDialog(
    private val origin: GeoPopupOrigin = GeoPopupOrigin.OTHER,
    private val activity: FragmentActivity,
    private val onEnableGeoClicked: () -> Unit,
    private val onGeoPopupAction: (GeoPopupAction, GeoPopupOrigin) -> Unit
) : MapBottomSheetDialog(activity, R.style.MapInfoDialog) {

    private val binding: MeeraDialogGeoPopupBinding = MeeraDialogGeoPopupBinding.inflate(layoutInflater)

    private val bottomSheetCloseUtil = BottomSheetCloseUtil(object : BottomSheetCloseUtil.Listener {
        override fun bottomSheetClosed(method: BottomSheetCloseUtil.BottomSheetCloseMethod) {
            onGeoPopupAction(GeoPopupAction.Close(method), origin)
        }
    })

    init {
        setContentView(binding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bottomSheet = findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.setBackgroundColor(Color.TRANSPARENT)
        setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
                bottomSheetCloseUtil.onBackButtonPressed()
            }
            false
        }
        setOnShowListener { bottomSheetCloseUtil.reset() }
        setOnCancelListener { bottomSheetCloseUtil.onCancel() }
        setOnDismissListener { bottomSheetCloseUtil.onDismiss() }

        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                bottomSheetCloseUtil.onStateChanged(newState)
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
        })

        binding.nvGeoPopup.closeButtonClickListener = {
            bottomSheetCloseUtil.onCloseButtonPressed()
            dismiss()
        }
        binding.btnGeoPopupSkip.setThrottledClickListener {
            onGeoPopupAction(GeoPopupAction.Skip, origin)
            dismiss()
        }
        binding.btnGeoPopupEnable.setThrottledClickListener {
            onEnableGeoClicked()
            onGeoPopupAction(GeoPopupAction.EnableLocation, origin)
            dismiss()
        }
    }
}
