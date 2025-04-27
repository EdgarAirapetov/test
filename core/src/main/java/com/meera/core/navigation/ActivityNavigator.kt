package com.meera.core.navigation

import android.content.Context

interface ActivityNavigator {

    fun navigateToMomentsSettings(context: Context)

    fun navigateToMusicPlayer(context: Context, isAdding: Boolean)

    fun navigateToMediaKeyboard(context: Context, isAdding: Boolean)
}
