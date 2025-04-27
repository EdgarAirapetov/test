package com.numplates.nomera3.modules.appInfo.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.meera.core.extensions.click
import com.meera.core.extensions.gone
import com.meera.core.extensions.string
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.appInfo.ui.entity.ForceUpdateDialogEntity
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyActionType
import javax.inject.Inject

/**
 * Use ForceUpdateDialog.showUpdateDialog method to show dialog
 * */
class ForceUpdateDialog : AppCompatDialogFragment() {

    private lateinit var dialog: AlertDialog.Builder
    private lateinit var alertDialog: AlertDialog
    var onDismissListener: OnDismissListener? = null
    private var title: TextView? = null
    private var subtitle: TextView? = null
    private var closeBtn: ImageView? = null
    private var applyBtnText: TextView? = null
    private var applyBtn: LinearLayout? = null
    @Inject
    lateinit var amplitudeHelper: AnalyticsInteractor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = AlertDialog.Builder(context)
        val mView = LayoutInflater.from(context).inflate(R.layout.dialog_force_update_app, null)
        dialog.setView(mView)
        alertDialog = dialog.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        initView(mView)
        arguments?.let { bundle ->
            val dialogParams = bundle.getParcelable<ForceUpdateDialogEntity?>(FORCE_UPDATE_DIALOG_PARAMS)
            dialogParams?.let { renderDialog(it) }
        }
        return alertDialog
    }


    private fun initView(v: View) {
        title = v.findViewById(R.id.tv_title)
        subtitle = v.findViewById(R.id.tv_subtitle)
        closeBtn = v.findViewById(R.id.iv_close)
        applyBtn = v.findViewById(R.id.ll_btn_dismiss)
        applyBtnText = v.findViewById(R.id.tv_btn)
    }

    private fun renderDialog(entity: ForceUpdateDialogEntity) {
        title?.text = entity.title ?: context?.string(R.string.force_update_title)
        subtitle?.text = entity.subtitle ?: context?.string(R.string.force_update_desc)
        applyBtnText?.text = entity.btnTitle ?: context?.string(R.string.force_update_btn_text)
        if (entity.canBeClosed) {
            closeBtn?.click {
                amplitudeHelper.logForceUpdate(AmplitudePropertyActionType.CLOSE)
                dismiss()
            }
        } else {
            closeBtn?.gone()
        }
        applyBtn?.click { sendToMarket() }
    }

    // отпвка пользователя в маркет
    private fun sendToMarket() {
        amplitudeHelper.logForceUpdate(AmplitudePropertyActionType.UPDATE)
        val marketIntent = Intent(Intent.ACTION_VIEW)
        marketIntent.data = Uri.parse(App.GOOGLE_PLAY_MARKET_URL)
        startActivity(marketIntent)
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.onDismiss()
    }

    companion object {
        const val FORCE_UPDATE_DIALOG_PARAMS = "FORCE_UPDATE_DIALOG_PARAMS"
        const val TAG_FORCE_UPDATE_DIALOG = "ForceUpdateDialog"

        fun showUpdateDialog(entity: ForceUpdateDialogEntity, fm: FragmentManager?) {
            fm?.let { manager ->
                if (manager.findFragmentByTag(TAG_FORCE_UPDATE_DIALOG) != null) return
                val dialog = ForceUpdateDialog()
                val params = Bundle()
                params.putParcelable(FORCE_UPDATE_DIALOG_PARAMS, entity)
                dialog.arguments = params
                dialog.isCancelable = false
                dialog.show(manager, TAG_FORCE_UPDATE_DIALOG)
            }
        }

        fun newInstance(entity: ForceUpdateDialogEntity?): ForceUpdateDialog {
            val dialog = ForceUpdateDialog()
            val params = Bundle()
            params.putParcelable(FORCE_UPDATE_DIALOG_PARAMS, entity)
            dialog.arguments = params
            dialog.isCancelable = false
            return dialog

        }
    }
}
