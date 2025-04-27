package com.numplates.nomera3.modules.appInfo.data.repository

import com.meera.core.di.scopes.AppScope
import com.numplates.nomera3.modules.appInfo.data.entity.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import javax.inject.Inject

private const val SETTINGS_GET_TIMEOUT = 1000L

/**
 * Репозиторий-хелпер. Оборачивает репозиторй [AppInfoRepository]
 * и позволяет получать [AppSettings] разными способами
 *
 * Рекомендуется получать Settings через этот репозиторий.
 *
 * Самый простой способ:
 *
 */
interface AppInfoAsyncRepository {
    /**
     * Сброить кэш. При следующем вызове настройки будут получены заново.
     */
    fun resetCache()

    /**
     * Получить Settings без блокировки потока вернув Coroutine Deffer
     */
    fun executeAsync(): Deferred<Settings>

    /**
     * Получить Settings заблокировав поток
     * (если Settings уже в кэше, то поток будет заблокирован несущественно только на время вызова)
     *
     * Можно вызывать всегда когда есть уверенность, что настройки уже были получены. Например
     * после старта приложения
     */
    fun executeBlocking(): Settings?
}

@AppScope
class AppInfoAsyncRepositoryImpl @Inject constructor(
    private val repository: AppInfoRepository
) : AppInfoAsyncRepository {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var deferredJob: Deferred<Settings> = requestSettingsAsync()

    private fun requestSettingsAsync() = scope.async {
        repository.getSettings()
    }

    override fun resetCache() {
        repository.resetCache()
        deferredJob = requestSettingsAsync()
    }

    override fun executeAsync(): Deferred<Settings> {
        return deferredJob
    }

    override fun executeBlocking(): Settings? {
        return runBlocking {
            try {
                coroutineScope {
                    withTimeout(SETTINGS_GET_TIMEOUT) {
                        deferredJob.await()
                    }
                }
            } catch (exception: Exception) {
                Timber.e(exception)
                null
            }
        }
    }
}
