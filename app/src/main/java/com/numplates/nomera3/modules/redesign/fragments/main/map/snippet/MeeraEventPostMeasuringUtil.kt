package com.numplates.nomera3.modules.redesign.fragments.main.map.snippet

import android.view.View
import androidx.fragment.app.Fragment
import com.meera.core.utils.blur.BlurHelper
import com.meera.core.utils.graphics.getScreenWidth
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.appInfo.data.entity.Settings
import com.numplates.nomera3.modules.comments.ui.entity.PostDetailsMode
import com.numplates.nomera3.modules.feed.ui.MeeraPostCallback
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.viewholder.MeeraImagePostHolder
import com.numplates.nomera3.modules.newroads.data.ISensitiveContentManager
import com.numplates.nomera3.modules.remotestyle.presentation.formatter.AllRemoteStyleFormatter

class MeeraEventPostMeasuringUtil(private val fragment: Fragment, settings: Settings?) {

    private val view = View.inflate(fragment.context, R.layout.meera_view_event_snippet_stub, null)
    private val holder = MeeraImagePostHolder(
        view = view,
        parentWidth = getScreenWidth(fragment),
        needToShowCommunityLabel = false
    ).apply {
        initCallbacks(
            meeraPostCallback = object : MeeraPostCallback {
                override fun getParentWidth(): Int? {
                    return null
                }
            },
            contentManager = object : ISensitiveContentManager {
                override fun isMarkedAsNonSensitivePost(postId: Long?): Boolean = false
                override fun markPostAsNotSensitiveForUser(postId: Long?, parentPostId: Long?) = Unit
            },
            blurHelper = BlurHelper(
                context = fragment.requireContext(),
                lifecycle = fragment.lifecycle,
            ),
            zoomyProvider = null,
            audioFeedHelper = null,
            volumeStateCallback = null
        )
        isEventsEnabled = true
        postDetailsMode = PostDetailsMode.EVENT_SNIPPET
        isInSnippet = true
    }
    private val formatter = AllRemoteStyleFormatter(settings)

    fun calculateSnippetHeight(post: PostUIEntity): Int {
        holder.bind(post.copy(tagSpan = post.tagSpan?.copy(showFullText = false)))
        formatter.formatDefault(holder)
        val wMeasureSpec = View.MeasureSpec.makeMeasureSpec(
            fragment.resources.displayMetrics.widthPixels,
            View.MeasureSpec.EXACTLY
        )
        val hMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        holder.view.measure(wMeasureSpec, hMeasureSpec)
        return holder.view.measuredHeight
    }
}
