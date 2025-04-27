package com.numplates.nomera3.modules.rateus.data

import com.meera.core.di.scopes.AppScope
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.modules.appInfo.data.entity.Settings
import com.numplates.nomera3.modules.appInfo.domain.usecase.GetAppInfoAsyncUseCase
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val USER_ID_KEY = "user_id"
private const val CONTENT_KEY = "content"

interface RateUsRepository {
    suspend fun rateUs(rating: Int, comment: String)
    suspend fun hideRatePost()
    fun isRated(): Boolean
    fun getLastRatedTime(): Long
    fun saveLastShow()
    fun writeIsRated(value: Boolean)
}

@AppScope
class RateUsRepositoryImpl @Inject constructor(
    private val appSettings: AppSettings,
    private val webSocket: WebSocketMainChannel,
    getAppInfoAsyncUseCase: GetAppInfoAsyncUseCase
) : RateUsRepository {
    private var isRated = false
    private val settings = getAppInfoAsyncUseCase.executeAsync()

    override fun saveLastShow() {
        appSettings.writeLastRatedTime(System.currentTimeMillis())
    }

    override fun writeIsRated(value: Boolean) {
        appSettings.writeIsRated(value)
    }

    override fun isRated(): Boolean {
        return appSettings.readIsRated()
    }

    override fun getLastRatedTime(): Long {
        return appSettings.readLastRatedTime()
    }

    override suspend fun rateUs(rating: Int, comment: String) {
        appSettings.writeLastRatedTime(System.currentTimeMillis())
        appSettings.writeIsRated(true)
        isRated = true
        trySendData(rating, comment)
    }

    override suspend fun hideRatePost() {
        if (!isRated) {
            appSettings.writeIsRated(false)
            appSettings.writeLastRatedTime(System.currentTimeMillis())
        }

        isRated = false
    }

    private suspend fun trySendData(rating: Int, comment: String) {
        try {
            val settings = settings.await()
            sendCommentToChat(settings, rating, comment)
        } catch (e: Exception) {
            Timber.e(e.message)
        }
    }

    private suspend fun sendCommentToChat(data: Settings, rating: Int, comment: String) {
        val stars = getStarsStringForRating(rating)
        val userId = data.appInfo[0].value ?: return
        val rateUsMessage = "$stars\n${comment}"

        Timber.d("Rate us message sent = $rateUsMessage")

        val payload = hashMapOf(
            USER_ID_KEY to userId,
            CONTENT_KEY to rateUsMessage
        )

        suspendCoroutine<Unit> { cont ->
            webSocket.pushNewMessage(payload)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    cont.resume(Unit)
                }, { exception ->
                    cont.resumeWithException(exception)
                })
        }
    }

    private fun getStarsStringForRating(rating: Int): String {
        var res = ""
        repeat(rating) {
            res += "⭐"
        }
        return res
    }
}
