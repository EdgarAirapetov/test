package com.numplates.nomera3.di

import com.google.android.datatransport.runtime.dagger.Module
import com.google.android.datatransport.runtime.dagger.Provides
import com.google.gson.Gson
import com.meera.core.di.scopes.AppScope
import com.meera.core.network.websocket.WebSocketMainChannel
import com.numplates.nomera3.modules.calls.data.CallManagerImpl
import com.numplates.nomera3.modules.calls.domain.CallManager

@Module
class CallModule {

    @Provides
    @AppScope // Use your custom scope here
    fun provideCallManager(
        websocketChannel: WebSocketMainChannel,
        gson: Gson
    ): CallManager {
        return CallManagerImpl(websocketChannel, gson)
    }
}
