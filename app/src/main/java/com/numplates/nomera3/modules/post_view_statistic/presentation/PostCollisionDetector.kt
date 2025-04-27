package com.numplates.nomera3.modules.post_view_statistic.presentation

import android.graphics.Rect
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.meera.core.extensions.getScreenHeight
import com.meera.core.extensions.getScreenWidth
import com.meera.db.models.PostViewLocalData
import com.numplates.nomera3.Act
import com.numplates.nomera3.BuildConfig
import com.numplates.nomera3.modules.appInfo.data.entity.Settings
import com.numplates.nomera3.modules.newroads.MainPostRoadsFragment
import com.numplates.nomera3.modules.post_view_statistic.data.PostViewRoadSource
import com.numplates.nomera3.presentation.utils.runOnUiThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.Serializable

private const val COLLISION_CHECK_REFRESH_TIMER_MS = 100L

/**
 * CollisionDetector каждые @param COLLISION_CHECK_REFRESH_TIMER_MS
 * проверяет статичный прямоугольник (представляющий середину экрана)
 * на пересечение с динамическими прямоугольниками (представляющие контент поста)
 *
 * Если прямоугольники пересекались больше заданного времени, то
 * в момент окончания пересечение записывается просмотр поста в базу
 */
class PostCollisionDetector private constructor(
    private val detectTime: Long,
    private var postViewHighlightEnable: Boolean = false,
    private var roadSource: PostViewRoadSource?,
    private var recyclerView: RecyclerView?,
    private var collisionRect: Rect?,
    private var fragment: Fragment?,
    private var detectPostViewCallback: ((PostViewDetectModel) -> Unit)?,
    private var postUploadPostViewsCallback: (() -> Unit)?
) {
    private val tickerChannel = ticker(COLLISION_CHECK_REFRESH_TIMER_MS)
    private var timerJob: Job? = null

    private var previousIsRoadVisible: Boolean = false
    private var currentViewedPosts = mutableListOf<PostViewDetectModel>()

    private val lifecycleObserver = object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun onResume() {
            startDetectRoadPosts()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun onPause() {
            cancelRefreshTimer()

            if (isRoadVisible()) {
                forceCommitDetectPostViews(tryCommitPostViewsAfter = true)
            }
        }
    }

    init {
        fragment?.lifecycle?.addObserver(lifecycleObserver)
    }

    fun setPostViewHighlightEnable(value: Boolean) {
        postViewHighlightEnable = value
    }

    fun release() {
        currentViewedPosts.clear()
        recyclerView = null
        timerJob?.cancel()
        timerJob = null
        fragment?.lifecycle?.removeObserver(lifecycleObserver)
        detectPostViewCallback = null
        postUploadPostViewsCallback = null
        fragment = null
        collisionRect = null
        roadSource = null
    }

    private fun isPostViewHighlightEnable(): Boolean {
        if (!BuildConfig.DEBUG) {
            return false
        }

        return postViewHighlightEnable
    }

    private fun startDetectRoadPosts() {
        timerJob = fragment?.lifecycleScope?.launch(Dispatchers.Default) {
            val iterator = tickerChannel.iterator()
            while (iterator.hasNext()) {
                checkAllVisibleItems()
                iterator.next()
            }
        }
    }

    private fun forceCommitDetectPostViews(tryCommitPostViewsAfter: Boolean) {
        finishViewingPost(currentViewedPosts, mutableListOf())

        if (tryCommitPostViewsAfter) {
            postUploadPostViewsCallback?.invoke()
        }
    }

    private fun cancelRefreshTimer() {
        timerJob?.cancel()
    }

    private fun isRoadVisible(): Boolean {
        return try {
            val activity = (fragment?.activity as? Act) ?: return false
            val isViewPagerRoadVisible =
                (activity.getCurrentFragment() as? MainPostRoadsFragment)?.getCurrentFragment() == fragment
            val isDedicatedFragmentVisible = activity.getCurrentFragment() == fragment

            isViewPagerRoadVisible || isDedicatedFragmentVisible
        } catch (exception: Exception) {
            FirebaseCrashlytics.getInstance().recordException(exception)
            return false
        }
    }

    private fun isRoadVisibleAndCheckOnStopBeVisible(): Boolean {
        val newIsRoadVisible = isRoadVisible()

        if (newIsRoadVisible.not()) {
            if (previousIsRoadVisible) {
                forceCommitDetectPostViews(tryCommitPostViewsAfter = false)

                previousIsRoadVisible = false
            }

            return newIsRoadVisible
        }

        previousIsRoadVisible = newIsRoadVisible

        return newIsRoadVisible
    }

    private fun checkAllVisibleItems() {
        val isRoadVisible = isRoadVisibleAndCheckOnStopBeVisible()

        if (isRoadVisible.not()) {
            return
        }

        val newIterationDetectedPost = mutableListOf<PostViewDetectModel>()

        @Suppress("detekt:LoopWithTooManyJumpStatements")
        recyclerView?.let { rv ->
            for (index in 0 until rv.childCount) {
                val viewChild = rv.getChildAt(index) ?: continue
                val child = rv.getChildViewHolder(viewChild) ?: continue
                val analyticDetectPost = child as? IPostViewDetectViewHolder ?: continue

                val postRect = analyticDetectPost.getViewAreaCollisionRect()

                collisionRect?.let { rect ->
                    roadSource?.let { source ->
                        if (postRect.intersect(rect)) {
                            newIterationDetectedPost.add(
                                PostViewDetectModel(
                                    detectTime = detectTime,
                                    roadSource = source,
                                    postViewData = analyticDetectPost.getPostViewData(),
                                    viewHolder = analyticDetectPost
                                )
                            )
                        }
                    }
                }
            }
        }

        compareIteration(currentViewedPosts, newIterationDetectedPost)
    }

    @Suppress("detekt:LoopWithTooManyJumpStatements")
    @Synchronized
    private fun compareIteration(
        previousCollisionPosts: MutableList<PostViewDetectModel>,
        nowCollisionPosts: MutableList<PostViewDetectModel>
    ) {
        val iterator = nowCollisionPosts.iterator()

        while (iterator.hasNext()) {
            val newIterationViewedPost = iterator.next()

            if (isStartViewPost(previousCollisionPosts, newIterationViewedPost)) {
                continue
            }

            if (isContinueViewPost(previousCollisionPosts, newIterationViewedPost)) {
                continue
            }
        }

        finishViewingPost(previousCollisionPosts, nowCollisionPosts)
    }

    private fun isStartViewPost(
        previousCollisionPosts: MutableList<PostViewDetectModel>,
        newIterationViewedPost: PostViewDetectModel
    ): Boolean {
        val isPostStartView = previousCollisionPosts.contains(newIterationViewedPost).not()
        return if (isPostStartView) {
            previousCollisionPosts.add(newIterationViewedPost)

            if (isPostViewHighlightEnable()) {
                runOnUiThread {
                    (newIterationViewedPost.viewHolder as? RecyclerView.ViewHolder?)?.itemView?.apply {
                        clearAnimation()
                        animate().setStartDelay(100).setDuration(10).alpha(0.5f)
                    }
                }
            }

            true
        } else {
            false
        }
    }

    private fun isContinueViewPost(
        previousCollisionPosts: MutableList<PostViewDetectModel>,
        newIterationViewedPost: PostViewDetectModel
    ): Boolean {
        val continueViewPost =
            previousCollisionPosts.find {
                (it.postViewData?.postId != null)
                    && (it.postViewData?.postId == newIterationViewedPost.postViewData?.postId)
            }
        return if (continueViewPost != null) {
            continueViewPost.updateViewDuration(COLLISION_CHECK_REFRESH_TIMER_MS)
            true
        } else {
            false
        }
    }

    @Synchronized
    private fun finishViewingPost(
        previousCollisionPosts: MutableList<PostViewDetectModel>,
        nowCollisionPosts: MutableList<PostViewDetectModel>
    ) {
        val listIterator = previousCollisionPosts.listIterator()

        while (listIterator.hasNext()) {
            val previousViewedPost = listIterator.next()

            if (nowCollisionPosts.contains(previousViewedPost).not()) {
                listIterator.remove()

                if (isPostViewHighlightEnable()) {
                    runOnUiThread {
                        (previousViewedPost.viewHolder as? RecyclerView.ViewHolder?)?.itemView?.alpha = 1f
                    }
                }

                if (previousViewedPost.isViewTimeValidForFinish()) {
                    previousViewedPost.finish()
                    detectPostViewCallback?.invoke(previousViewedPost)
                }
            }
        }
    }

    /**
     * Короткоживущая UI модель для подсчёта времени нахождения постов на экране
     */
    data class PostViewDetectModel(
        val detectTime: Long,
        var roadSource: PostViewRoadSource,
        var postViewData: PostViewLocalData,
        var viewHolder: IPostViewDetectViewHolder
    ) : Serializable {
        var viewDuration = 0L
            private set

        var stopViewTime = 0L
            private set

        fun isViewTimeValidForFinish(): Boolean {
            return viewDuration > detectTime
        }

        fun updateViewDuration(value: Long) {
            viewDuration += value
        }

        fun finish() {
            stopViewTime = System.currentTimeMillis()
        }
    }

    companion object {
        private const val TOP_LINE_RATIO = 0.25
        private const val BOTTOM_LINE_RATIO = 0.75

        private const val DETECT_DURATION_SETTING_NAME = "post_view_min_duration"

        fun getDurationMsFromSettings(settings: Settings?): Long? {
            val durationSec = settings?.appInfo?.find { checkedValue ->
                checkedValue.name == DETECT_DURATION_SETTING_NAME
            }?.value?.toLong() ?: return null

            return durationSec * 1000
        }

        fun create(
            detectTime: Long?,
            postViewHighlightEnable: Boolean,
            recyclerView: RecyclerView,
            roadFragment: Fragment,
            roadSource: PostViewRoadSource,
            detectPostViewCallback: (PostViewDetectModel) -> Unit,
            postUploadPostViewsCallback: () -> Unit
        ): PostCollisionDetector? {
            if (detectTime == null || detectTime == 0L) {
                val message = "Ошибка состояния. Логирование просмотров постов отключено."

                FirebaseCrashlytics.getInstance().recordException(
                    IllegalStateException(
                        message
                    )
                )

                Timber.e(message)
                return null
            }

            val topLine = getScreenHeight() * TOP_LINE_RATIO
            val bottomLine = getScreenHeight() * BOTTOM_LINE_RATIO
            val collisionRect = Rect(
                0,
                topLine.toInt(),
                getScreenWidth(),
                bottomLine.toInt()
            )

            return PostCollisionDetector(
                detectTime,
                postViewHighlightEnable,
                roadSource,
                recyclerView,
                collisionRect,
                roadFragment,
                detectPostViewCallback,
                postUploadPostViewsCallback
            )
        }
    }
}
