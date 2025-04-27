package com.numplates.nomera3.di

import android.content.Context
import com.google.android.exoplayer2.SimpleExoPlayer
import com.meera.core.di.scopes.AppScope
import com.numplates.nomera3.modules.baseCore.helper.AudioFeedHelper
import com.numplates.nomera3.modules.baseCore.helper.AudioFeedHelperImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
interface AudioModule {

    /**
     * ExoPlayer Может регистрировать внутри себя бродкаст ресиверы,
     * на регистрацию которых для каждого процесса стоят определенные лимиты.
     * Так как AudioFeedHelper создается дважды для каждого типа дороги, мы получали
     * очень много инстансов плейера, которые по факту нам не нужны.
     * */
    @Binds
    fun bindAudioFeedHelper(audioFeedHelper: AudioFeedHelperImpl): AudioFeedHelper

    companion object {

        @Provides
        @AppScope
        @Named(AUDIO_EXO_PLAYER)
        fun provideExoPlayer(context: Context) =
            SimpleExoPlayer.Builder(context)
                .build()
                .apply { setHandleAudioBecomingNoisy(true) }

        const val AUDIO_EXO_PLAYER = "audio_exo_player"
    }
}
