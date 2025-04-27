package com.meera.core.base

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.meera.core.R
import com.meera.core.navigation.Screens
import com.meera.core.views.NavigationBarViewContract
import timber.log.Timber

interface OnActivityInteractionCallback {

    fun onGetNavigationBar(navBar: NavigationBarViewContract)

    fun onAddFragment(screen: Screens)

    fun onAddFragment(fragment: BaseFragment, isLightStatusBar: Int, mapArgs: Map<String, Any?> = emptyMap())

    fun onSetStatusBar()

    fun openPreviousViewPagerItem()
}


open class BaseFragment(@LayoutRes layout: Int = R.layout.empty_layout) : Fragment(layout) {

    private val TAG = "BASE_FRAGMENT"
    private val CHILD_CLASS = this.javaClass.simpleName

    var onActivityInteraction: OnActivityInteractionCallback? = null

    var isFragmentStarted = false
    var isFragmentAdding = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            onActivityInteraction = context as OnActivityInteractionCallback
        } catch (e: ClassCastException) {
            Timber.e(e)
            throw ClassCastException("$context must implement interface OnActivityInteractionCallback")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(TAG, "ON_CREATE: $CHILD_CLASS")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e(TAG, "ON_VIEW_CREATED:  $CHILD_CLASS")
    }

    override fun onStart() {
        super.onStart()
        Log.e(TAG, "ON_START: $CHILD_CLASS")
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "ON_RESUME:  $CHILD_CLASS")
    }

    override fun onPause() {
        super.onPause()
        Log.e(TAG, "ON_PAUSE:  $CHILD_CLASS")
    }

    override fun onStop() {
        super.onStop()
        Log.e(TAG, "ON_STOP:  $CHILD_CLASS")
    }

    override fun onDestroyView() {
        onActivityInteraction = null
        super.onDestroyView()
        Log.e(TAG, "ON_DESTROY_VIEW: $CHILD_CLASS")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "ON_DESTROY: $CHILD_CLASS")
    }


    open fun onStartFragment() {
        Log.e(TAG, "ON_START_FRAGMENT: $CHILD_CLASS")
        isFragmentStarted = true
        isFragmentAdding = false
        onShowHints()
    }

    open fun onShowHints() {}

    /**
     * Called when we leave this screen
     */
    open fun onStopFragment() {
        Log.e(TAG, "ON_STOP_FRAGMENT: $CHILD_CLASS")
        isFragmentStarted = false
        if (isAdded) onHideHints()
    }

    open fun onHideHints() {}

    open fun onAppHidden() {
        Log.e(TAG, "ON_APP_HIDDEN: $CHILD_CLASS")
    }

    /**
     * Called on each new open fragment when the transition is completed
     */
    open fun onOpenTransitionFragment() {
        Log.e(TAG, "ON_OPEN_TRANSITION_FRAGMENT: $CHILD_CLASS")
    }

    /**
     * Called when start view pager back navigation animation
     */
    open fun onStartAnimationTransitionFragment() {
        Log.e(TAG, "ON_START_ANIMATION_TRANSITION_FRAGMENT: $CHILD_CLASS")
    }

    /**
     * Called on the first fragment when the opening
     * transition of the second fragment is completed
     */
    open fun onReturnTransitionFragment() {
        Log.e(TAG, "ON_RETURN_TRANSITION_FRAGMENT: $CHILD_CLASS")
    }

    open fun updateScreenOnTapNavBar() {
        //Timber.d("Update screen and ScrollUp when navigation bar icon tap");
    }

    fun navAddFragment(screen: Screens) {
        onActivityInteraction?.onAddFragment(screen)
    }

}


