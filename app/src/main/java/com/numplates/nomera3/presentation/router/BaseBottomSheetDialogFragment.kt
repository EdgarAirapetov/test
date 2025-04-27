package com.numplates.nomera3.presentation.router

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meera.core.base.BaseFragment
import com.numplates.nomera3.Act
import com.numplates.nomera3.presentation.view.ui.BottomSheetDialogEventsListener
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet

abstract class BaseBottomSheetDialogFragment<T : ViewBinding> : BottomSheetDialogFragment()  {

    /**
     * Техдолг: https://nomera.atlassian.net/browse/BR-17556
     *
     * Необходимо во всех диалогах-шторках, которые наследуют класс [BaseBottomSheetDialogFragment]:
     * Удалить лямду-колбэк onDismiss(), а вместо него задействовать интерфейс
     * [BottomSheetDialogEventsListener].
     *
     * В качестве примера можно брать класс [ViewMomentPositionFragment].
     * При добавлении других колбэков шторок, необходимо добавлять их по примеру колбэка
     * onDismiss() в [MeeraMenuBottomSheet].
     */
    var dialogListener: BottomSheetDialogEventsListener? = null

    private var listener: Listener? = null

    protected val binding: T?
        get() = _binding

    private var _binding: T? = null

    protected var act: Act? = null

    protected var isDraggableBehavior = true

    abstract val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> T

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? Listener ?: parentFragment as? Listener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    fun setListener(dialogListener: BottomSheetDialogEventsListener) {
        this.dialogListener = dialogListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        act = activity as? Act
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        state: Bundle?
    ): View? {
        _binding = bindingInflater.invoke(inflater, container, false)
        return requireNotNull(_binding).root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setOnShowListener { dialog ->
                dialogListener?.onCreateDialog()
                (dialog as? BottomSheetDialog)?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)?.let {
                    BottomSheetBehavior.from(it).apply {
                        isDraggable = isDraggableBehavior
                    }
                }
            }
            setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
                    onBackKeyPressed()
                }
                false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        act = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener?.onDismissBaseDialog()
    }

    open fun add(fragment: BaseFragment, isLightStatusBar: Int, vararg args: Arg<*, *>) {
        act?.addFragment(fragment, isLightStatusBar, *args)
    }

    open fun onBackKeyPressed() = Unit

    interface Listener {
        fun onDismissBaseDialog() = Unit
    }
}
