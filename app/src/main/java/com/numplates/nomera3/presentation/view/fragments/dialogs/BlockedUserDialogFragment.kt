package com.numplates.nomera3.presentation.view.fragments.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.utils.convertUnixDate
import com.numplates.nomera3.R

@Deprecated("Not used at this time")
class BlockedUserDialogFragment : AppCompatDialogFragment() {

    private lateinit var dialog: AlertDialog.Builder
    private lateinit var alertDialog: AlertDialog

    private lateinit var ivCloseDialog: ImageView
    private lateinit var tvBlockReasonLabel: TextView
    private lateinit var tvBlockReasonText: TextView
    private lateinit var tvBlockDate: TextView
    private lateinit var btnWriteSupport: Button


    var blockReason: String? = String.empty()
    var blockDateUnixtimeSec: Long? = 0L
    var closeDialogClickListener: () -> Unit = { }
    var writeSupportClickListener: () -> Unit = { }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_pop_up_blocked_user, null)
        dialog.setView(view)
        isCancelable = false
        initViews(view)
        alertDialog = dialog.create()

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return alertDialog
    }


    private fun initViews(view: View) {
        ivCloseDialog = view.findViewById(R.id.iv_close_dialog)
        tvBlockReasonLabel = view.findViewById(R.id.tv_block_reason_label)
        tvBlockReasonText = view.findViewById(R.id.tv_block_reason)
        tvBlockDate = view.findViewById(R.id.tv_block_date)
        btnWriteSupport = view.findViewById(R.id.btn_write_support)

        // Block reason  is optional type
        blockReason?.let { text ->
            if (text.isNotEmpty()) {
                tvBlockReasonText.text = text
            } else {
                tvBlockReasonLabel.gone()
                tvBlockReasonText.gone()
            }
        }

        tvBlockDate.text = getString(R.string.popup_block_user_until, convertUnixDate(blockDateUnixtimeSec))

        // Click listeners
        ivCloseDialog.setOnClickListener { closeDialogClickListener() }
        btnWriteSupport.setOnClickListener { writeSupportClickListener() }
    }


}
