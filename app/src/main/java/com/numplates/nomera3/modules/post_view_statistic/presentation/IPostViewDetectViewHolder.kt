package com.numplates.nomera3.modules.post_view_statistic.presentation

import android.graphics.Rect
import com.meera.db.models.PostViewLocalData

private const val COLLISION_DETECT_TIME_MS = 1000L

interface IPostViewDetectViewHolder {
    fun getPostViewData(): PostViewLocalData
    fun getViewAreaCollisionRect(): Rect
    fun getDetectTime(): Long {
        return COLLISION_DETECT_TIME_MS
    }

    fun Rect.merge(rect: Rect?): Rect {
        if (rect == null) {
            return this
        }

        val resultLeft = when {
            this.left <= 0 -> {
                rect.left
            }
            rect.left <= 0 -> {
                this.left
            }
            else -> {
                left.coerceAtMost(rect.left)
            }
        }

        val resultTop = when {
            this.top <= 0 -> {
                rect.top
            }
            rect.top <= 0 -> {
                this.top
            }
            else -> {
                top.coerceAtMost(rect.top)
            }
        }

        return Rect(
            resultLeft,
            resultTop,
            right.coerceAtLeast(rect.right),
            bottom.coerceAtLeast(rect.bottom)
        )
    }
}
