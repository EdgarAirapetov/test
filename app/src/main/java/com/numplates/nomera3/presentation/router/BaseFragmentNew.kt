package com.numplates.nomera3.presentation.router

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.utils.ScreenArgs
import com.numplates.nomera3.modules.reaction.ui.custom.ReactionBubble
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.viewmodel.viewevents.SingleLiveEvent
import kotlin.properties.Delegates

abstract class BaseFragmentNew<T : ViewBinding> : com.meera.core.base.BaseFragment(),
    BasePermission by BasePermissionDelegate() {

    protected val binding: T?
        get() = _binding

    private var _binding: T? = null

    abstract val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> T

    var act by Delegates.notNull<Act>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        act = activity as Act
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        state: Bundle?
    ): View? {
        _binding = bindingInflater.invoke(inflater, container, false)
        return requireNotNull(_binding).root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    var onAppSettingsRequestFinished = SingleLiveEvent<Int>()

    fun showCommonError(@StringRes stringRes: Int? = null) {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            NToast.with(view)
                .typeError()
                .text(getString(stringRes ?: R.string.no_internet))
                .show()
        }
    }

    fun showCommonSuccessMessage(@StringRes stringRes: Int? = null) {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            NToast.with(view)
                .typeSuccess()
                .text(getString(stringRes ?: R.string.no_internet))
                .show()
        }
    }

    fun add(
        fragment: com.meera.core.base.BaseFragment,
        isLightStatusBar: Int,
        vararg args: Arg<*, *>
    ) {
        isFragmentAdding = true
        act.addFragment(fragment, isLightStatusBar, *args)
    }

    fun add(
        fragment: com.meera.core.base.BaseFragment,
        isLightStatusBar: Int,
        args: ScreenArgs
    ) {
        isFragmentAdding = true
        act.addFragment(fragment, isLightStatusBar, args)
    }

    fun replace(
        position: Int,
        fragment: com.meera.core.base.BaseFragment,
        isLightStatusBar: Int, vararg args: Arg<*, *>
    ) {
        act.replaceFragment(position, fragment, isLightStatusBar, *args)
    }

    protected fun clickCheckBubble(click: () -> Unit) {
        if (isBubbleNotExist()) {
            click()
        }
    }

    private fun isBubbleNotExist(): Boolean {
        val act = context as? Act ?: return false
        val bubble = (act.getRootView() as? ViewGroup)?.children?.find { it is ReactionBubble } as? ReactionBubble
        return bubble == null
    }
}
