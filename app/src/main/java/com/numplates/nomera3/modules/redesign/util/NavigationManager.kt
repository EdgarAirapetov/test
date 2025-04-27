package com.numplates.nomera3.modules.redesign.util

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.annotation.NavigationRes
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.meera.core.extensions.gone
import com.meera.core.extensions.navHost
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.uikit.widgets.navigation.BottomType
import com.meera.uikit.widgets.navigation.NavigationBarActions
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.main.MeeraMainContainerFragment
import com.numplates.nomera3.modules.redesign.fragments.main.ToolbarNavbarInteraction
import com.numplates.nomera3.modules.redesign.fragments.main.map.MainMapFragment
import timber.log.Timber

class NavigationManager private constructor(
    val act: MeeraAct,
    private val mainContainerFragment: MeeraMainContainerFragment
) {

    @NavigationRes
    private var graphId: Int? = null

    val topNavHost: NavHostFragment by lazy { mainContainerFragment.navHost(R.id.fragment_first_container_view) }
    val navHost: NavHostFragment by lazy { mainContainerFragment.navHost(R.id.fragment_second_container_view) }

    private val alphaCoverView: View? by lazy { mainContainerFragment.view?.findViewById(R.id.alpha_cover) }
    private val containerView: View? by lazy { mainContainerFragment.view?.findViewById(R.id.fragment_second_container_view) }
    private val topContainerView: View? by lazy { mainContainerFragment.view?.findViewById(R.id.fragment_first_container_view) }

    private var currentCallback: OnBackPressedCallback? = null

    val mapModeLiveData: MutableLiveData<Boolean> = MutableLiveData(false)

    var isRoadOpen: Boolean = true
    var isMapMode: Boolean = false
        set(value) {
            field = value
            mapModeLiveData.value = value
        }
    val topNavController: NavController by lazy { topNavHost.navController }
    val navController: NavController by lazy { navHost.navController }
    val toolbarAndBottomInteraction: ToolbarNavbarInteraction by lazy { mainContainerFragment }

    val mainMapFragment: MainMapFragment
        get() = mainContainerFragment.childFragmentManager.findFragmentById(R.id.fragment_map) as MainMapFragment

    fun initGraph(@NavigationRes resId: Int, startDestinationArgs: Bundle? = null) {
        Timber.d("initGraph resId=$resId, startDestinationArgs=$startDestinationArgs,")
        graphId = resId
        initPopBackStack()

        getBottomBehaviour()?.state = STATE_HIDDEN
        alphaCoverView?.visible()
        containerView?.visible()
        navController.setGraph(resId, startDestinationArgs)
        val navOptions = NavOptions.Builder()
            .setPopUpTo(navController.graph.startDestinationId, true)
            .build()

        navController.navigate(navController.graph.startDestinationId, startDestinationArgs, navOptions)
        getBottomBehaviour()?.state = STATE_EXPANDED

        alphaCoverView?.setThrottledClickListener {
            getBottomBehaviour()?.state = STATE_HIDDEN
        }
    }

    private fun initPopBackStack() {
        currentCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val result = navController.popBackStack()
                if (result.not()) {
                    getBottomBehaviour()?.state = STATE_HIDDEN
                }
            }
        }.also {
            act.onBackPressedDispatcher.addCallback(mainContainerFragment.viewLifecycleOwner, it)
        }
    }

    fun getBottomBehaviour(): BottomSheetBehavior<View>? {
        val view = containerView ?: return null
        return BottomSheetBehavior.from(view)
    }

    fun getTopContainer() = topContainerView

    fun getTopBehaviour(isOnlyMapMode: Boolean = false): BottomSheetBehavior<View>? {
        if (isOnlyMapMode) {
            if (isMapMode.not()) {
                Timber.d("Try to get top behaviour in non-mapMode")
                return null
            }
        }
        val view = topContainerView ?: return null
        return BottomSheetBehavior.from(view)
    }

    fun getForceUpdatedTopBehavior(): BottomSheetBehavior<View>? {
        val view = mainContainerFragment.view?.findViewById<View?>(R.id.fragment_first_container_view) ?: return null
        return BottomSheetBehavior.from(view)
    }

    fun clearState(callback: OnBackPressedCallback? = currentCallback) {
        Timber.d("Clearing state. graphId: $graphId, callback: $callback")
        graphId?.let { navController.popBackStack(it, true) }
        callback?.remove()
        graphId = null
        currentCallback = null

        alphaCoverView?.gone()
        containerView?.gone()
    }

    fun logOutDoPassAndSetState(
        @IdRes destinationId: Int = R.id.mainRoadFragment,
        selectedNavigationTab: BottomType = BottomType.Map,
        action: ((FragmentManager) -> Unit)? = null
    ) {
        if (topNavController.popBackStack(R.id.main_flow_graph, false)) {
            Timber.e("Can't pop back stack, interrupted passing")
            return
        }
        clearCounterButtonsState()
        topNavController.safeNavigate(resId = destinationId)
        toolbarAndBottomInteraction.getNavigationView()
            .setNavigationBarAction(NavigationBarActions.SelectItem(selectedNavigationTab))

        action?.invoke(topNavHost.childFragmentManager)
    }

    private fun clearCounterButtonsState() {
        val toolbar = toolbarAndBottomInteraction.getToolbar()
        val navBar = toolbarAndBottomInteraction.getNavigationView()

        toolbar.notificationsCount = 0
        navBar.setNavigationBarAction(NavigationBarActions.ShowAnimatedChatBadge(isShow = false))
    }

    fun setContainerViewTransparent() {
        containerView?.setBackgroundColor(Color.TRANSPARENT)
    }

    companion object Builder {
        private var instance: NavigationManager? = null

        fun build(act: MeeraAct, fragment: MeeraMainContainerFragment): NavigationManager {
            if (instance == null) instance = NavigationManager(act, fragment)
            return instance as NavigationManager
        }

        fun getManager(): NavigationManager =
            instance ?: error("BottomNavigationManager instance wasn't initialized")

        fun clear() {
            instance?.clearState()
            instance = null
        }
    }
}

fun BottomSheetBehavior<View>.isHiddenState(): Boolean = state == STATE_HIDDEN

fun BottomSheetBehavior<View>.setHiddenState() {
    state = STATE_HIDDEN
}

fun BottomSheetBehavior<View>.setExpandedState() {
    state = STATE_EXPANDED
}
