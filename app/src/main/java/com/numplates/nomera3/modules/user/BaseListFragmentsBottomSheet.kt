package com.numplates.nomera3.modules.user

import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.base.BaseFragment
import com.meera.core.extensions.dpToPx
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.BottomSheetListFragmentsBinding
import com.numplates.nomera3.modules.user.ui.OnBottomSheetFragmentsListener
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment

private const val BOTTOM_SHEET_MARGIN_TOP = 44

/**
 * Базовый BottomSheet фрагмент для работы с переходом
 * по стеку фрагментов
 */
abstract class BaseListFragmentsBottomSheet :
        BaseBottomSheetDialogFragment<BottomSheetListFragmentsBinding>(),
        OnBottomSheetFragmentsListener {

    override val bindingInflater: (LayoutInflater,
                                   ViewGroup?,
                                   Boolean) -> BottomSheetListFragmentsBinding
        get() = BottomSheetListFragmentsBinding::inflate

    abstract fun getRootFragment(): BaseFragment

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val parentLayout = bottomSheetDialog.findViewById<View>(
                    com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let {
                val behaviour = BottomSheetBehavior.from(it)
                setupFullHeight(it)
                behaviour.peekHeight = Resources.getSystem().displayMetrics.heightPixels
                behaviour.isHideable = true
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        dialog.setOnKeyListener { _, keyCode, keyEvent ->
            if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_UP) {
                onBackFragment()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        return dialog
    }

    private fun setupFullHeight(bottomSheet: View) {
        val height = Resources.getSystem().displayMetrics.heightPixels
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = height - dpToPx(BOTTOM_SHEET_MARGIN_TOP)
        bottomSheet.layoutParams = layoutParams
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rootFragment = getRootFragment()
        rootFragment.arguments = arguments
        childFragmentManager
                .beginTransaction()
                .add(R.id.list_fragments_container, rootFragment)
                .commit()
    }

    fun addFragment(fragment: BaseFragment, tag: String, bundle: Bundle?) {
        fragment.arguments = bundle
        childFragmentManager
                .beginTransaction()
                .add(R.id.list_fragments_container, fragment, tag)
                .addToBackStack(null)
                .commit()
    }

    override fun onNextFragment(clazz: Class<out BaseFragment>,
                                tag: String,
                                bundle: Bundle?
    ) {
        addFragment(clazz.newInstance(), tag, bundle)
    }

    override fun onBackFragment() {
        if (childFragmentManager.backStackEntryCount > 0) {
            childFragmentManager.popBackStack()
        } else {
            closeBottomSheet()
        }
    }

    override fun onCloseMenu() {
        closeBottomSheet()
    }

    override fun <T> onReturnResult(result: T) {
        // may be override in child class for getting result
    }

    open fun closeBottomSheet() {
        dismiss()
    }
}
