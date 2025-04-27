package com.numplates.nomera3.modules.redesign.util

import android.content.Context
import android.content.res.Resources
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import timber.log.Timber

private const val SAVE_STATE_OF_TAB = true

object NavigationUiSetter {

    // TODO: Добавить анимацию перхода для активити
    private fun NavOptions.Builder.animTransitionToActivity(): NavOptions.Builder {
        return this
    }

    // TODO: Добавить переход между табами нижнего меню
    private fun NavOptions.Builder.animToTransition(): NavOptions.Builder {
        return this
    }

    fun onNavDesSelectedNew(item: MenuItem, navController: NavController): Boolean {
        val builder = NavOptions.Builder()
//            .setLaunchSingleTop(true)
            .setRestoreState(SAVE_STATE_OF_TAB)
        if (
            navController.currentDestination!!.parent!!.findNode(item.itemId) is ActivityNavigator.Destination
        ) {
            builder.animTransitionToActivity()
        } else {
            builder.animToTransition()
        }

        if (item.order and Menu.CATEGORY_SECONDARY == 0) {
            builder.setPopUpTo(
                navController.graph.findStartDestination().id,
                inclusive = false,
                saveState = SAVE_STATE_OF_TAB
            )
        }
        val options = builder.build()
        return try {
            // TODO provide proper API instead of using Exceptions as Control-Flow.
            navController.navigate(item.itemId, null, options)
            // Return true only if the destination we've navigated to matches the MenuItem
            navController.currentDestination?.matchDestination(item.itemId) == true
        } catch (e: IllegalArgumentException) {
            val name = getDisplayName(navController.context, item.itemId)
            Timber.i(
                e, "Ignoring onNavDestinationSelected for MenuItem $name as it cannot be found " +
                    "from the current destination ${navController.currentDestination}"
            )
            false
        }
    }

    private fun NavDestination.matchDestination(@IdRes destId: Int): Boolean =
        hierarchy.any { it.id == destId }

    private fun getDisplayName(context: Context, id: Int): String {
        // aapt-generated IDs have the high byte nonzero,
        // so anything below that cannot be a valid resource id
        return if (id <= 0x00FFFFFF) {
            id.toString()
        } else try {
            context.resources.getResourceName(id)
        } catch (e: Resources.NotFoundException) {
            Timber.e(e)
            id.toString()
        }
    }
}
