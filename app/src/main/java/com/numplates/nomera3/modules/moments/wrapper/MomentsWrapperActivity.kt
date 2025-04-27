package com.numplates.nomera3.modules.moments.wrapper

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.moments.music.MomentsMusicListFragment
import com.numplates.nomera3.modules.moments.widgets.MeeraEditorWidgetsFragment

class MomentsWrapperActivity : Act() {

    private var needOpenWidgets = false
    private var isOpenMusicFromMoments = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_moments_wrapper)

        isOpenMusicFromMoments = intent.hasExtra(OPEN_MUSIC_PLAYER_EXTRA_NAME)
        needOpenWidgets = intent.hasExtra(OPEN_MEDIA_KEYBOARD_EXTRA)

        when {
            isOpenMusicFromMoments -> {
                openAudioPlayer()
                window.setAttributes(
                    window.attributes.apply { alpha = 0f }
                )
            }
            needOpenWidgets -> {
                setStatusBarColor(true)
                navigateTo(MeeraEditorWidgetsFragment())
            }
            else -> {
                initSettingsNavigation()
            }
        }
    }

    private fun initSettingsNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_moments_container_view)
            as? NavHostFragment
        val navController = navHostFragment?.findNavController() ?: return
        val navGraph = navController.navInflater.inflate(R.navigation.moments_settings_graph)
        navController.graph = navGraph
        setStatusBarColor(false)
    }

    @SuppressLint("CommitTransaction")
    fun navigateTo(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .add(R.id.moments_wrapper_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun openAudioPlayer() {
        setStatusBarColor(true)
        val fragment = MomentsMusicListFragment()
        val isAddingMusic = intent.extras?.getBoolean(IS_ADDING_MUSIC) ?: true
        fragment.arguments = bundleOf(IS_ADDING_MUSIC to isAddingMusic)
        fragment.show(supportFragmentManager, MomentsMusicListFragment::javaClass.name)
    }

    fun onBackPress() {
        if (isOpenMusicFromMoments) finish()
    }

    private fun setStatusBarColor(isDark: Boolean) {
        val color = if (isDark) STATUS_BAR_COLOR else R.color.uiKitColorForegroundInvers
        window.navigationBarColor = ContextCompat.getColor(baseContext, color)
        window.statusBarColor = ContextCompat.getColor(baseContext, color)
        if (!isDark) window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    companion object {
        const val OPEN_MUSIC_PLAYER_EXTRA_NAME = "OPEN_MUSIC_PLAYER_NAME"
        const val OPEN_MUSIC_PLAYER_EXTRA_VALUE = "OPEN_MUSIC_PLAYER_VALUE"
        const val OPEN_MEDIA_KEYBOARD_EXTRA = "OPEN_MEDIA_KEYBOARD_EXTRA"
        const val IS_ADDING_MUSIC = "IS_ADDING_MUSIC"

        val STATUS_BAR_COLOR = com.meera.media_controller_implementation.R.color.black_85
    }
}
