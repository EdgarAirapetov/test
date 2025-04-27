package com.numplates.nomera3.modules.user.ui.dialog

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import com.meera.core.extensions.addClickWithDataBold
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.string
import com.meera.core.utils.convertUnixDate
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.registration.ui.phoneemail.HeaderDialogType
import com.numplates.nomera3.presentation.view.utils.NToast


private const val BLOCK_REASON_TEXT_MARGIN_BOTTOM = 8

class BlockUserByAdminDialogFragment : AppCompatDialogFragment() {
    private lateinit var mDialog: Dialog

    private lateinit var ivCloseDialog: ImageView
    private lateinit var tvBlockReasonLabel: TextView
    private lateinit var tvBlockReasonText: TextView
    private lateinit var tvBlocDateTitle: TextView
    private lateinit var tvBlockDate: TextView
    private lateinit var btnWriteSupport: Button
    private lateinit var tvBlockUserDescription: TextView
    private lateinit var tvHeader: TextView
    private val rangeEmail = 45..62
    private var root: View? = null


    var blockReason: String? = String.empty()
    var blockDateUnixtimeSec: Long? = 0L
    var closeDialogClickListener: () -> Unit = { }
    var writeSupportClickListener: () -> Unit = { }
    var headerDialogType: HeaderDialogType = HeaderDialogType.GroupRoadType


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val root = FrameLayout(requireContext())
        root.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        mDialog = dialog

        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_user_blocked_by_admin, container, false)
        root = view
        initViews(view)
        return view
    }

    private fun initViews(view: View) {
        ivCloseDialog = view.findViewById(R.id.iv_close_dialog)
        tvBlockReasonLabel = view.findViewById(R.id.tv_block_reason_label)
        tvBlockReasonText = view.findViewById(R.id.tv_block_reason)
        tvBlocDateTitle = view.findViewById(R.id.tv_bloc_date_title)
        tvBlockDate = view.findViewById(R.id.tv_block_date)
        btnWriteSupport = view.findViewById(R.id.btn_write_support)
        tvBlockUserDescription = view.findViewById(R.id.tv_description_block_user)
        tvHeader = view.findViewById(R.id.textView16)

        // Block reason  is optional type
        blockReason?.let { text ->
            if (text.isNotEmpty()) {
                tvBlockReasonText.text = text
            } else {
                tvBlockReasonLabel.gone()
                tvBlockReasonText.gone()
            }
        }

        if (blockDateUnixtimeSec != 0L) {
            tvBlockDate.text = getString(
                    R.string.popup_block_user_until,
                    convertUnixDate(blockDateUnixtimeSec))
        } else {
            tvBlocDateTitle.gone()
            tvBlockDate.gone()
            tvBlockReasonText.setMargins(bottom = BLOCK_REASON_TEXT_MARGIN_BOTTOM.dp)
        }

        // Click listeners
        ivCloseDialog.setOnClickListener { closeDialogClickListener() }
        btnWriteSupport.setOnClickListener { writeSupportClickListener() }

        if (headerDialogType == HeaderDialogType.BlockedProfileType) {
            tvBlockUserDescription.text = context?.getString(R.string.desc_block_user_by_admin)
        } else {
            val spanText = SpannableStringBuilder(context?.getString(R.string.desc_block_user_by_admin_email))
            spanText.addClickWithDataBold("", rangeEmail) {
                onEmailClicked()
            }
            tvBlockUserDescription.text = spanText
            tvBlockUserDescription.movementMethod = LinkMovementMethod.getInstance()
        }

        setupHeader()
    }

    private fun setupHeader() {
        tvHeader.text = when (headerDialogType) {
            HeaderDialogType.BlockedProfileType -> getString(R.string.profile_blocked_str)
            HeaderDialogType.GroupRoadType -> getString(R.string.community_prohibited)
            HeaderDialogType.MainRoadType -> getString(R.string.blocked_to_create_post)
        }
    }


    private fun onEmailClicked() {
        copyTextToClipBoard()
        val act = activity as? Act
        act?.let {
            NToast.with(it)
                .text(act.string(R.string.copied_into_buffer))
                .typeSuccess()
                .inView(mDialog.window?.decorView)
                .show()
        }
    }

    private fun copyTextToClipBoard() {
        val clipData = ClipData.newPlainText(
            "text",
            context?.getString(R.string.technical_support_mail)
        )
        val clipboardManager =
            context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(clipData)
    }

}
