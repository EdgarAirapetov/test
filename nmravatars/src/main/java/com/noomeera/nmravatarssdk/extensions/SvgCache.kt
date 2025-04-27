package com.noomeera.nmravatarssdk.extensions

import com.caverock.androidsvg.SVG
import java.io.File
import java.util.concurrent.Executors

const val AUTO_CACHE = false

class SvgCache {


    companion object {
        private val cacheThreadPoolExecutor = Executors.newFixedThreadPool(4)
        private val mCache = HashMap<String, SVG>()

        fun cache(path: String) {
            cacheThreadPoolExecutor.execute {
                File(path).inputStream()
                    .use {
                        val svg = SVG.getFromInputStream(it)
                        synchronized(mCache) {
                            mCache[path] = svg
                        }
                    }
            }
        }

        fun fromFile(path: String): SVG {
            synchronized(mCache) { mCache[path] }?.let {
                return it
            }
            File(path).inputStream()
                .use {
                    val svg = SVG.getFromInputStream(it)
                    if (AUTO_CACHE) {
                        synchronized(mCache) {
                            mCache[path] = svg
                        }
                    }
                    return svg
                }
        }
    }
}