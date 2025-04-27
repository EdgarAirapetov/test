package com.numplates.nomera3.presentation.router

import android.content.Context
import android.content.Intent
import com.meera.core.navigation.ActivityNavigator
import com.numplates.nomera3.modules.moments.wrapper.MomentsWrapperActivity
import com.numplates.nomera3.modules.moments.wrapper.MomentsWrapperActivity.Companion.IS_ADDING_MUSIC
import com.numplates.nomera3.modules.moments.wrapper.MomentsWrapperActivity.Companion.OPEN_MEDIA_KEYBOARD_EXTRA
import com.numplates.nomera3.modules.moments.wrapper.MomentsWrapperActivity.Companion.OPEN_MUSIC_PLAYER_EXTRA_NAME
import com.numplates.nomera3.modules.moments.wrapper.MomentsWrapperActivity.Companion.OPEN_MUSIC_PLAYER_EXTRA_VALUE
import javax.inject.Inject

class ActivityNavigatorImpl @Inject constructor() : ActivityNavigator {

    override fun navigateToMomentsSettings(context: Context) = context.startActivity(
        Intent(context, MomentsWrapperActivity::class.java)
    )

    override fun navigateToMusicPlayer(context: Context, isAdding: Boolean) {
        val intent = Intent(context, MomentsWrapperActivity::class.java)
        intent.putExtra(OPEN_MUSIC_PLAYER_EXTRA_NAME, OPEN_MUSIC_PLAYER_EXTRA_VALUE)
        intent.putExtra(IS_ADDING_MUSIC, isAdding)
        context.startActivity(intent)
    }

    override fun navigateToMediaKeyboard(context: Context, isAddingMusic: Boolean) {
        context.startActivity(
            Intent(context, MomentsWrapperActivity::class.java).apply {
                putExtra(OPEN_MEDIA_KEYBOARD_EXTRA, true)
                putExtra(IS_ADDING_MUSIC, isAddingMusic)
            }
        )
    }


}
