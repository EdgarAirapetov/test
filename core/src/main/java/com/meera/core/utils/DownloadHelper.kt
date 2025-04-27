package com.meera.core.utils

import android.content.Context
import androidx.annotation.WorkerThread
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.meera.core.network.ProgressListener
import com.meera.core.network.ProgressResponseBody
import okhttp3.OkHttpClient
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

class DownloadHelper @Inject constructor(
    private val okHttp: OkHttpClient
) {

    @Throws(IOException::class)
    fun download(
        context: Context,
        url: String,
        @WorkerThread onProgress: (progress: Int) -> Unit = {},
        @WorkerThread onComplete: (file: File?) -> Unit = {},
        @WorkerThread onLoadFailed: (e: Exception?) -> Unit = {}
    ) {
        val modifiedClient = okHttp.newBuilder()
            .addInterceptor { chain ->
                val originalResponse = chain.proceed(chain.request())
                val responseBody = originalResponse.body ?: throw IOException()
                originalResponse.newBuilder()
                    .body(ProgressResponseBody(responseBody, DownloadProgressListener(onProgress)))
                    .build()
            }
            .build()

        val factory = OkHttpUrlLoader.Factory(modifiedClient)
        Glide.get(context).registry
            .replace(GlideUrl::class.java, InputStream::class.java, factory)
        Glide.with(context)
            .asFile()
            .load(url)
            .listener(object : RequestListener<File> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<File>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Timber.e("Load failed:$e")
                    onLoadFailed(e)
                    return false
                }

                override fun onResourceReady(
                    resource: File?,
                    model: Any?,
                    target: Target<File>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    Timber.d("Resource ready:$resource")
                    onComplete(resource)
                    return false
                }
            })
            .submit()
    }

}

private class DownloadProgressListener(
    val onProgress: (progress: Int) -> Unit,
    val onCompleteLoading: () -> Unit = {}
) : ProgressListener {
    override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {
        val progress = (100 * bytesRead / contentLength).toInt()
        Timber.d("Download progress:$progress")
        onProgress(progress)
        if (done) {
            onCompleteLoading()
        }
    }
}
