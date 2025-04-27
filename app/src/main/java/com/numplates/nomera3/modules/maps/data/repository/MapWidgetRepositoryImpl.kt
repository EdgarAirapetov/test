package com.numplates.nomera3.modules.maps.data.repository

import android.content.Context
import android.net.Uri
import com.meera.core.di.scopes.AppScope
import com.meera.core.extensions.toInt
import com.numplates.nomera3.data.network.ApiFileStorage
import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.modules.maps.data.mapper.MapWidgetDataMapper
import com.numplates.nomera3.modules.maps.domain.repository.MapWidgetRepository
import com.numplates.nomera3.modules.maps.domain.widget.model.GetMapWidgetPointInfoParamsModel
import com.numplates.nomera3.modules.maps.domain.widget.model.MapWidgetPointInfoModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

@AppScope
class MapWidgetRepositoryImpl @Inject constructor(
    private val apiMain: ApiMain,
    private val mapper: MapWidgetDataMapper,
    private val apiFileStorage: ApiFileStorage,
    private val context: Context
) : MapWidgetRepository {

    override suspend fun getMapWidgetPointInfo(params: GetMapWidgetPointInfoParamsModel): MapWidgetPointInfoModel {
        val dto = apiMain.getMapWidgetPointInfo(
            latitude = params.latitude,
            longitude = params.longitude,
            getWeather = params.getWeather.toInt()
        ).data
        val animationFile = if (dto.weather != null) {
            runCatching { getWeatherAnimationFile(dto.weather.animationUrl) }
                .onFailure(Timber::e)
                .getOrNull()
        } else {
            null
        }
        return mapper.mapWidgetPointInfoModel(
            dto = dto,
            animationFile = animationFile
        )
    }

    private suspend fun getWeatherAnimationFile(animationUrl: String): File {
        val animationUri = Uri.parse(animationUrl)
        val animFileName = animationUri.lastPathSegment
            ?: throw RuntimeException("No file name in weather animation URL: $animationUrl")
        val version = animationUri.getQueryParameter(KEY_VERSION)
            ?: throw RuntimeException("No version in weather animation URL: $animationUrl")
        val weatherAnimationDir = File(context.cacheDir.absoluteFile, WEATHER_ANIM_DIR)
        val animDir = File(weatherAnimationDir, animFileName)
        val versionDir = File(animDir, version)
        val animationFile = File(versionDir, animFileName)
        if (animationFile.exists().not()) {
            if (animDir.exists()) {
                animDir.deleteRecursively()
            }
            versionDir.mkdirs()
            val response = apiFileStorage.downloadFileFromUrl(animationUrl)
            withContext(Dispatchers.IO) {
                var inputStream: InputStream? = null
                var outputStream: OutputStream? = null
                try {
                    inputStream = response.byteStream()
                    outputStream = FileOutputStream(animationFile)
                    val data = ByteArray(BUFFER_SIZE_BYTES)
                    var count: Int
                    while ((inputStream.read(data).also { count = it }) != -1) {
                        outputStream.write(data, 0, count)
                    }
                    outputStream.flush()
                } finally {
                    inputStream?.close()
                    outputStream?.close()
                }
            }
        }
        return animationFile
    }

    companion object {
        private const val WEATHER_ANIM_DIR = "weather_anim"
        private const val KEY_VERSION = "v"
        private const val BUFFER_SIZE_BYTES = 4096
    }
}
