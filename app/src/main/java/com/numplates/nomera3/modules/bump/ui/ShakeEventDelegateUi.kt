package com.numplates.nomera3.modules.bump.ui

import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.flowWithLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.bump.ui.entity.ShakeEventDelegateUiEvent
import com.numplates.nomera3.modules.bump.ui.fragment.ShakeBottomDialogFragment
import com.numplates.nomera3.modules.bump.ui.viewmodel.ShakeEventViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

const val LOCATION_PERMISSION_KEY = 120

class ShakeEventDelegateUi constructor(
    private val manager: FragmentManager,
    private val storeOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle,
    private val scope: CoroutineScope,
    private val shakeEventUiHandler: ShakeEventUiHandler
) {
    private var globalFragmentLifecycleImpl: GlobalFragmentsLifecycleImpl? = null
    private val viewModel: ShakeEventViewModel by lazy {
        ViewModelProvider(
            storeOwner,
            App.component.getViewModelFactory()
        )[ShakeEventViewModel::class.java]
    }

    init {
        observeUiEffect()
    }

    fun triggerShowShakeOrLocationDialog(isShowDialogByShake: Boolean) =
        viewModel.triggerShakeActionChanged(isShowDialogByShake)

    fun showShakeOrLocationDialogByClick() {
        viewModel.triggerShowShakeOrLocationDialog(false)
    }

    fun connectWebSocket() {
        viewModel.observeFindFriendsByShake()
    }

    fun registerFragmentsLifecycleChange() {
        globalFragmentLifecycleImpl = GlobalFragmentsLifecycleImpl()
        manager.registerFragmentLifecycleCallbacks(
            globalFragmentLifecycleImpl ?: return,
            true
        )
    }

    fun unRegisterFragmentsLifecycleChange() {
        val lifecycleListener = globalFragmentLifecycleImpl ?: return
        manager.unregisterFragmentLifecycleCallbacks(lifecycleListener)
        globalFragmentLifecycleImpl = null
    }

    fun closeShakeDialog() {
        val shakeDialog = manager.findFragmentByTag(ShakeBottomDialogFragment.SHAKE_BOTTOM_DIALOG_TAG)
        if (shakeDialog == null || shakeDialog !is ShakeBottomDialogFragment) return
        shakeDialog.dismiss()
    }

    /**
     * Проверяем, что является ли текущий фрагмент [DialogFragment] или [BottomSheetDialogFragment]
     * Исключение [ShakeBottomDialogFragment]
     */
    fun isAllowToRegisterShakeByDialog(fragment: Fragment): Boolean {
        return when (fragment) {
            is ShakeBottomDialogFragment -> true
            is DialogFragment -> false
            else -> true
        }
    }

    fun removeShakeUserIfShakeAdded() {
        if (shakeBottomSheetIsAdded()) {
            viewModel.removeShakeUser()
        }
    }

    private fun observeUiEffect() {
        viewModel.shakeUiEffectFlow
            .flowWithLifecycle(lifecycle)
            .onEach(::handleUiEffect)
            .launchIn(scope)
    }

    private fun handleUiEffect(event: ShakeEventDelegateUiEvent) {
        when (event) {
            is ShakeEventDelegateUiEvent.ShowShakeDialog -> {
                if (!shakeBottomSheetIsAdded()) {
                    shakeEventUiHandler.showShakeDialog(event.showDialogByShake)
                }
            }
            ShakeEventDelegateUiEvent.ShowGeoEnabledDialog -> {
                shakeEventUiHandler.showLocationDialog()
            }
            ShakeEventDelegateUiEvent.ShowShakeFriendRequests -> {
                shakeEventUiHandler.showShakeFriendRequestsDialog()
            }
            is ShakeEventDelegateUiEvent.ShowErrorToast -> {
                showErrorToastInsideShakeDialog(event.errorMessageRes)
            }
        }
    }

    private fun shakeBottomSheetIsAdded() =
        manager.findFragmentByTag(ShakeBottomDialogFragment.SHAKE_BOTTOM_DIALOG_TAG) != null

    private fun getShakeBottomDialog(): ShakeBottomDialogFragment? {
        return manager.findFragmentByTag(ShakeBottomDialogFragment.SHAKE_BOTTOM_DIALOG_TAG) as? ShakeBottomDialogFragment
    }

    private fun showErrorToastInsideShakeDialog(@StringRes errorMessage: Int) {
        getShakeBottomDialog()?.showErrorToast(errorMessage)
    }

    /**
     * Нам необходимо слушать изменения каждого фрагмента через жц.
     * Сделано это для того, чтобы не подписываться/отписываться на ShakeEventListener в определенных местах.
     */
    private inner class GlobalFragmentsLifecycleImpl : FragmentManager.FragmentLifecycleCallbacks() {

        /**
         * Если сенсорный эвент не нужен, то в onResumed() отписываемся.
         * Возможны 2 сценария:
         * 1.Возможна кастомная реализация, когда нужно подписаться на ShakeEventListener.
         * 2.Отписываемся от ShakeEventListener, если в определенном фрагменте он не нужен
         */
        override fun onFragmentResumed(
            fragmentManager: FragmentManager,
            fragment: Fragment
        ) {
            super.onFragmentResumed(fragmentManager, fragment)
            if (isAllowToRegisterShakeByDialog(fragment) && fragment is ShakeRegisterUiHandler) {
                fragment.registerShake()
                return
            }
            if (isAllowToRegisterShakeByDialog(fragment) && fragment.isAllowToRegisterShakeInCurrentScreen()) return
            viewModel.unregisterEventListener()
        }

        override fun onFragmentPaused(
            fragmentManager: FragmentManager,
            fragment: Fragment
        ) {
            super.onFragmentPaused(fragmentManager, fragment)
            if (isAllowToRegisterShakeByDialog(fragment) && fragment.isAllowToRegisterShakeInCurrentScreen()) return
            viewModel.registerEventListener()
        }
    }
}
