package com.numplates.nomera3.presentation.view.fragments.dialogs

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.numplates.nomera3.databinding.FragmetConfirmDialogBinding
import com.meera.core.extensions.visible
import com.meera.core.extensions.gone

@Deprecated("Transited to core. Must delete")
class ConfirmDialogFragment(
        private var leftBtnClicked: () -> Unit = {},
        private var rightBtnClicked: () -> Unit = {},
        private var leftBtnText: String = "",
        private var rightBtnText: String = "",
        private var description: String = "",
        private var header: String = "",
        private var topBtnText: String = "",
        private var bottomBtnText: String = "",
        private var middleBtnText: String = "",
        private var topBtnClicked: () -> Unit = {},
        private var bottomBtnClicked: () -> Unit = {},
        private var middleBtnClicked: () -> Unit = {},
        private var isHorizontal: Boolean,
        private var needTopBtn: Boolean
): AppCompatDialogFragment() {

    private var isSizeConfigured = false
    private var binding : FragmetConfirmDialogBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmetConfirmDialogBinding.inflate(inflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }


    override fun onResume() {
        super.onResume()
        if (!isSizeConfigured) {
            val displayMetrics = DisplayMetrics()

            (context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(displayMetrics)
            // The absolute width of the available display size in pixels.
            val displayWidth = displayMetrics.widthPixels

            // Initialize a new window manager layout parameters
            val layoutParams = WindowManager.LayoutParams()

            // Copy the alert dialog window attributes to new layout parameter instance
            layoutParams.copyFrom(dialog?.window?.attributes)

            // Set alert dialog width equal to screen width 80%
            val dialogWindowWidth = (displayWidth * 0.85f).toInt()
            // Set alert dialog height equal to screen height 70%

            // Set the width and height for the layout parameters
            // This will bet the width and height of alert dialog
            layoutParams.width = dialogWindowWidth
            //layoutParams.height = displayHeight

            // Apply the newly created layout parameters to the alert dialog window
            dialog?.window?.attributes = layoutParams
        }

        isSizeConfigured = true
    }

    private fun configureView() {
        binding?.tvConfirmDialogHeader?.text = header
        binding?.tvConfirmDialogTxt?.text = description
        binding?.tvLeftBtn?.text = leftBtnText
        binding?.tvRightBtn?.text = rightBtnText

        binding?.tvUpBtn?.text = topBtnText
        binding?.tvBottomBtn?.text = bottomBtnText
        binding?.tvMiddleBtn?.text = middleBtnText

        binding?.tvLeftBtn?.setOnClickListener {
            leftBtnClicked.invoke()
            dismiss()
        }
        binding?.tvRightBtn?.setOnClickListener {
            rightBtnClicked.invoke()
            dismiss()
        }

        binding?.tvUpBtn?.setOnClickListener {
            topBtnClicked.invoke()
            dismiss()
        }
        binding?.tvBottomBtn?.setOnClickListener {
            bottomBtnClicked.invoke()
            dismiss()
        }
        binding?.tvMiddleBtn?.setOnClickListener {
            middleBtnClicked.invoke()
            dismiss()
        }

        if (isHorizontal){
            binding?.llVerticalBtnContainer?.visible()
            binding?.tvUpBtn?.visibility = if (needTopBtn) View.VISIBLE else View.GONE
            binding?.tvLeftBtn?.gone()
            binding?.tvRightBtn?.gone()
        }else {
            binding?.llVerticalBtnContainer?.gone()
            binding?.tvLeftBtn?.visible()
            binding?.tvRightBtn?.visible()
        }

    }


    override fun show(manager: FragmentManager, tag: String?) {
        if (manager.findFragmentByTag(tag) != null)
            return
        super.show(manager, tag)
    }
}

class ConfirmDialogBuilder {
    private var leftBtnText = ""
    private var rightBtnText = ""
    private var topBtnText = ""
    private var bottomBtnText = ""
    private var middleBtnText = ""
    private var description = ""
    private var header = ""
    private var isHorizontal = false
    private var isCancelable = false
    private var needTopButton = true

    private var leftBtnClicked: () -> Unit = {}
    private var rightBtnClicked: () -> Unit = {}

    private var topBtnClicked: () -> Unit = {}
    private var bottomBtnClicked: () -> Unit = {}
    private var middleBtnClicked: () -> Unit = {}

    fun setCancelable(canCancel: Boolean): ConfirmDialogBuilder {
        isCancelable = canCancel
        return this
    }

    fun setNeedTopButton(needTopBtn: Boolean): ConfirmDialogBuilder {
        this.needTopButton = needTopBtn
        return this
    }

    fun setHorizontal(isHorizontal: Boolean): ConfirmDialogBuilder {
        this.isHorizontal = isHorizontal
        return this
    }

    fun setHeader(header: String): ConfirmDialogBuilder{
        this.header = header
        return this
    }

    fun setDescription(description: String): ConfirmDialogBuilder{
        this.description = description
        return this
    }

    fun setLeftBtnText(leftBtnText: String): ConfirmDialogBuilder{
        this.leftBtnText = leftBtnText
        return this
    }

    fun setRightBtnText(rightBtnText: String): ConfirmDialogBuilder{
        this.rightBtnText = rightBtnText
        return this
    }

    fun setTopBtnText(topBtnText: String): ConfirmDialogBuilder{
        this.topBtnText = topBtnText
        return this
    }

    fun setBottomBtnText(bottomBtnText: String): ConfirmDialogBuilder{
        this.bottomBtnText = bottomBtnText
        return this
    }

    fun setMiddleBtnText(middleBtnText: String): ConfirmDialogBuilder{
        this.middleBtnText = middleBtnText
        return this
    }

    fun setLeftClickListener(leftBtnClicked: () -> Unit): ConfirmDialogBuilder{
        this.leftBtnClicked = leftBtnClicked
        return this
    }

    fun setRightClickListener(rightBtnClicked: () -> Unit): ConfirmDialogBuilder{
        this.rightBtnClicked = rightBtnClicked
        return this
    }

    fun setTopClickListener(topBtnClicked: () -> Unit): ConfirmDialogBuilder{
        this.topBtnClicked = topBtnClicked
        return this
    }

    fun setBottomClickListener(bottomBtnClicked: () -> Unit): ConfirmDialogBuilder{
        this.bottomBtnClicked = bottomBtnClicked
        return this
    }

    fun setMiddleClickListener(middleBtnClicked: () -> Unit): ConfirmDialogBuilder{
        this.middleBtnClicked = middleBtnClicked
        return this
    }

    fun show(fm: FragmentManager): ConfirmDialogFragment{
        val dialog = ConfirmDialogFragment(
                leftBtnClicked,
                rightBtnClicked,
                leftBtnText,
                rightBtnText,
                description,
                header,
                topBtnText,
                bottomBtnText,
                middleBtnText,
                topBtnClicked,
                bottomBtnClicked,
                middleBtnClicked,
                isHorizontal,
                needTopButton
        )

        dialog.isCancelable = this.isCancelable
        dialog.show(fm, "ConfirmDialogFragment")
        return dialog
    }

}
