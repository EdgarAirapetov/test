package com.numplates.nomera3.modules.communities.domain.usecase

import android.content.Context
import com.noomeera.nmravatarssdk.NMRAvatarsSDK
import com.numplates.nomera3.BuildConfig
import com.numplates.nomera3.NAME_AVATAR_ASSETS
import javax.inject.Inject

class InitializeAvatarSdkUseCase @Inject constructor(private val context: Context) {

    fun invoke() {
        if (NMRAvatarsSDK.isSdkReady(context).not()) {
            val packagedContext = context.createPackageContext(BuildConfig.APPLICATION_ID, 0)
            val stream = packagedContext.assets.open(NAME_AVATAR_ASSETS)
            NMRAvatarsSDK.setResourcesSync(context = context, zipStream = stream)
        }
    }
}
