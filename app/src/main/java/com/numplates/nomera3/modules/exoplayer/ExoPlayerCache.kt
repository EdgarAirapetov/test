package com.numplates.nomera3.modules.exoplayer

import android.content.Context
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache

private const val exoPlayerCacheSize: Long = 90 * 1024 * 1024

object ExoPlayerCache {

    fun initVideoCache(context: Context) {
        if (leastRecentlyUsedCacheEvictor != null) return

        if (leastRecentlyUsedCacheEvictor == null) {
            leastRecentlyUsedCacheEvictor = LeastRecentlyUsedCacheEvictor(exoPlayerCacheSize)
        }

        if (exoDatabaseProvider == null) {
            exoDatabaseProvider = StandaloneDatabaseProvider(context)
        }

        if (simpleCache == null) {
            leastRecentlyUsedCacheEvictor?.let { evictor ->
                exoDatabaseProvider?.let { provider ->
                    simpleCache = SimpleCache(context.cacheDir, evictor, provider)
                }
            }
        }
    }

    var simpleCache: SimpleCache? = null
        private set

    var leastRecentlyUsedCacheEvictor: LeastRecentlyUsedCacheEvictor? = null
        private set

    var exoDatabaseProvider: DatabaseProvider? = null
        private set
}
