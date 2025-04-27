package com.meera.core.extensions

import android.net.Uri
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.meera.core.R
import timber.log.Timber

fun NavController.safeNavigate(directions: NavDirections) {
    try {
        navigate(directions)
    } catch (e: Exception) {
        Timber.e(e)
    }
}

fun NavController.safeNavigate(directions: NavDirections, bundle: Bundle) {
    val mergedBundle = bundleOf().apply {
        putAll(directions.arguments)
        putAll(bundle)
    }

    try {
        navigate(directions.actionId, mergedBundle)
    } catch (e: Exception) {
        Timber.e(e)
    }
}

fun NavController.safeNavigate(@IdRes resId: Int) {
    try {
        navigate(resId)
    } catch (e: Exception) {
        Timber.e(e)
    }
}

fun NavController.safeNavigate(@IdRes resId: Int, bundle: Bundle) {
    try {
        navigate(resId, bundle)
    } catch (e: Exception) {
        Timber.e(e)
    }
}

fun NavController.safeNavigate(
    @IdRes resId: Int, bundle: Bundle? = null,
    navBuilder: (NavOptions.Builder) -> NavOptions.Builder
) {
    val navOptions = navBuilder(NavOptions.Builder()).build()

    try {
        navigate(resId = resId, args = bundle, navOptions = navOptions)
    } catch (e: Exception) {
        Timber.e(e)
    }
}

fun NavOptions.Builder.makeToTopAndRestoreState(): NavOptions.Builder = setLaunchSingleTop(true).setRestoreState(true)

fun NavController.isFragmentInBackStack(destinationId: Int) =
    try {
        getBackStackEntry(destinationId)
        true
    } catch (e: Exception) {
        Timber.e(e)
        false
    }

fun NavController.safeNavigate(direction: Uri) {
    try {
        navigate(direction)
    } catch (e: Exception) {
        Timber.e(e)
    }
}

fun NavOptions.Builder.addAnimationTransitionByDefault(): NavOptions.Builder {
    setEnterAnim(R.anim.meera_slide_left)
    setExitAnim(R.anim.meera_wait_anim)
    setPopEnterAnim(R.anim.meera_wait_anim)
    setPopExitAnim(R.anim.meera_slide_right)
    return this@addAnimationTransitionByDefault
}

fun AppCompatActivity.naveHost(@IdRes id: Int): NavHostFragment =
    supportFragmentManager.findFragmentById(id) as? NavHostFragment ?: error("Can't find navHostFragment of Activity")

fun Fragment.navHost(@IdRes id: Int): NavHostFragment =
    childFragmentManager.findFragmentById(id) as? NavHostFragment ?: error("Can't find navHostFragment of Fragment")

