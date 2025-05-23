package com.numplates.nomera3

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.meera.core.devtool.applyDevToolInterceptor
import okhttp3.OkHttpClient
import java.io.InputStream

@GlideModule
class GlideModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)

        if (BuildConfig.DEBUG) {
            val httpClient = OkHttpClient.Builder()
            httpClient.applyDevToolInterceptor()
            registry.replace(
                GlideUrl::class.java, InputStream::class.java,
                OkHttpUrlLoader.Factory(httpClient.build())
            )
        }
    }
}
