package com.numplates.nomera3.modules.feed.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
import com.meera.core.extensions.inflate
import com.meera.core.extensions.isNotTrue
import com.meera.core.extensions.isTrue
import com.meera.core.utils.blur.BlurHelper
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemFeedPostsViewedBinding
import com.numplates.nomera3.databinding.ItemRoadReferralBinding
import com.numplates.nomera3.databinding.MeeraItemProfileSuggestionsFloorBinding
import com.numplates.nomera3.databinding.MeeraItemRoadSyncContactsBinding
import com.numplates.nomera3.databinding.MeeraMomentsPostViewholderBinding
import com.numplates.nomera3.databinding.MomentCarouselShimmerBinding
import com.numplates.nomera3.modules.baseCore.helper.AudioFeedHelper
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.feed.ui.CacheUtil
import com.numplates.nomera3.modules.feed.ui.MeeraPostCallback
import com.numplates.nomera3.modules.feed.ui.data.MOMENTS_POST_ID
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.entity.UIPostUpdate
import com.numplates.nomera3.modules.feed.ui.viewholder.PostCallbackHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.MeeraBasePostHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.MeeraFeatureHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.MeeraImagePostHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.MeeraMomentsViewHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.MeeraMultimediaPostHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.MeeraPostProgressHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.MeeraPostsViewedViewHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.MeeraRateUsHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.MeeraRepostViewHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.MeeraRoadReferralHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.MeeraRoadSuggestionsViewHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.MeeraRoadSyncContactsHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.MeeraVideoPostHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.MeeraVideoRepostHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.ShimmerFeedViewHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.ShimmerMomentViewHolder
import com.numplates.nomera3.modules.moments.show.presentation.MomentCallback
import com.numplates.nomera3.modules.newroads.data.ISensitiveContentManager
import com.numplates.nomera3.modules.remotestyle.presentation.formatter.AllRemoteStyleFormatter
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.modules.volume.presentation.VolumeStateCallback
import com.numplates.nomera3.presentation.view.utils.inflateBinding
import com.numplates.nomera3.presentation.view.utils.zoomy.Zoomy.ZoomyProvider
import java.util.concurrent.TimeUnit

class MeeraFeedAdapter(
    private val needToShowCommunityLabel: Boolean = true
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var isNeedToShowRepostBtn = true

    private val collection = mutableListOf<PostUIEntity>()
    private var timeLastUpdatedMinutes = 0L
    private var postCallback: MeeraPostCallback? = null
    private var momentCallback: MomentCallback? = null
    private var volumeStateCallback: VolumeStateCallback? = null
    private var blurHelper: BlurHelper? = null
    private var contentManager: ISensitiveContentManager? = null
    private var zoomyProvider : ZoomyProvider? = null
    private var cacheUtil: CacheUtil? = null
    private var audioFeedHelper: AudioFeedHelper? = null
    private var featureTogglesContainer: FeatureTogglesContainer? = null
    private var formatter: AllRemoteStyleFormatter? = null

    init {
        stateRestorationPolicy = PREVENT_WHEN_EMPTY
    }

    private fun getCurrentTimeMinutes() = TimeUnit.MINUTES.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS)

    private fun getLayout(parent: ViewGroup, @LayoutRes layoutRes: Int) =
        LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)

    fun setPostCallback(postCallback: MeeraPostCallback) {
        this.postCallback = postCallback
    }

    fun setMomentCallback(momentCallback: MomentCallback) {
        this.momentCallback = momentCallback
    }

    fun setVolumeStateCallback(volumeStateCallback: VolumeStateCallback) {
        this.volumeStateCallback = volumeStateCallback
    }

    fun setBlurHelper(blurHelper: BlurHelper){
        this.blurHelper = blurHelper
    }

    fun setContentManager(contentManager: ISensitiveContentManager){
        this.contentManager = contentManager
    }

    fun setZoomyProvider(zoomyProvider: ZoomyProvider) {
        this.zoomyProvider = zoomyProvider
    }

    fun setCacheUtils(cacheUtil: CacheUtil) {
        this.cacheUtil = cacheUtil
    }

    fun setAudioFeedHelper(audioFeedHelper: AudioFeedHelper?) {
        this.audioFeedHelper = audioFeedHelper
    }

    fun setFeatureTogglesContainer(featureTogglesContainer: FeatureTogglesContainer){
        this.featureTogglesContainer = featureTogglesContainer
    }

    fun setRemoteStyleFormatter(formatterProvider: AllRemoteStyleFormatter) {
        this.formatter = formatterProvider
    }

    fun submitList(data: List<PostUIEntity>) {
        val currentTimeMinutes = getCurrentTimeMinutes()
        val shouldUpdateTime = if (currentTimeMinutes > timeLastUpdatedMinutes) {
            timeLastUpdatedMinutes = currentTimeMinutes
            true
        } else false
        submitList(data, isShouldUpdateTime = shouldUpdateTime)
    }

    fun submitList(data: List<PostUIEntity>, isShouldUpdateTime: Boolean) {
        val diffCallback = FeedDiffCallback(collection, data)
        DiffUtil.calculateDiff(diffCallback).dispatchUpdatesTo(this)
        collection.clear()
        collection.addAll(data)

        if (isShouldUpdateTime) {
            updateAllTimeAgoByPayloads()
        }
    }

    //ищем пост по id и обновляем без пайлоад
    fun updateItemByPostId(id: Long) {
        collection.forEachIndexed { index, postUIEntity ->
            if (postUIEntity.postId == id || postUIEntity.parentPost?.postId == id) {
                notifyItemChanged(index)
            }
        }
    }

    //если позиция null ищет и обновляет через пайлоад если не null обновляет по позиции
    fun updateItem(payload: UIPostUpdate, adapterPos: Int?) {
        if (payload is UIPostUpdate.UpdateUserMomentsState) {
            updateItems(payload)
            return
        }
        if (adapterPos == null) {
            updateItem(payload)
            return
        }
        if (!isCorrectPosition(adapterPos)) {
            return
        }

        updateModel(adapterPos, payload)
        notifyItemChanged(adapterPos, payload)
    }

    fun updateItems(payload: UIPostUpdate.UpdateUserMomentsState) {
        collection.forEachIndexed { index, postUIEntity ->
            if (payload.postMomentsBlock != null && postUIEntity.feedType == FeedType.MOMENTS) {
                updateModel(index, payload.postMomentsBlock)
            } else if (payload.userId == postUIEntity.getUserId()) {
                updateModel(index, payload)
                notifyItemChanged(index, payload)
            }
        }
    }

    //ищем пост по id и обновляем через пайлоад
    fun updateItem(payload: UIPostUpdate) {
        collection.forEachIndexed { index, postUIEntity ->
            if (payload.postId != null && postUIEntity.postId == payload.postId) {
                updateModel(index, payload)
                notifyItemChanged(index, payload)
            }
        }
    }

    fun updateVolumeState(volumeState: VolumeState, visiblePositions: Pair<Int, Int>? = null) {
        val finalPositions = visiblePositions ?: Pair(0, collection.size - 1)
        for (position in finalPositions.first..finalPositions.second) {
            if (isCorrectPosition(position)) {
                val postUIEntity = collection[position]
                if (postUIEntity.feedType != FeedType.MOMENTS) {
                    val payload = UIPostUpdate.UpdateVolumeState(postUIEntity.postId, volumeState)
                    notifyItemChanged(position, payload)
                }
            }
        }
    }

    private fun updateModel(index: Int, payload: UIPostUpdate) {
        if (isCorrectPosition(index)) {
            collection[index] = collection[index].updateModel(payload)
        }
    }

    fun updateModel(index: Int, post: PostUIEntity) {
        if (isCorrectPosition(index)) {
            collection[index] = post
            notifyItemChanged(index)
        }
    }

    fun stopCurrentAudio(position: Int) {
        audioFeedHelper?.apply {
            if (position == getHolderPosition()) {
                stopPlaying()
            }
        }
    }

    fun getMomentsItemPosition(): Int {
        return collection.indexOfFirst { it.postId == MOMENTS_POST_ID }
    }

    fun scrollMomentsToStart(viewHolder: RecyclerView.ViewHolder?, smoothScroll: Boolean = false) {
        if (viewHolder != null && viewHolder is  MeeraMomentsViewHolder) {
            viewHolder.scrollMomentsToStart(smoothScroll)
        }
    }

    private fun isCorrectPosition(pos: Int) = pos >= 0 && pos < collection.size

    fun showLoader(show: Boolean) {
        if (show) {
            collection.add(PostUIEntity(feedType = FeedType.PROGRESS))
            notifyItemInserted(collection.size - 1)
        } else {
            val index = collection.indexOfFirst { it.feedType == FeedType.PROGRESS }
            if (isCorrectPosition(index)) {
                collection.removeAt(index)
                notifyItemRemoved(index)
            }
        }
    }

    fun getItem(position: Int): PostUIEntity? =
        if (collection.size > 0 && position < collection.size) collection[position] else null

    fun getPositionById(postId: Long): Int {
        return collection.indexOfFirst { post -> post.postId == postId }
    }

    private fun updateAllTimeAgoByPayloads() {
        collection.forEachIndexed { index, post ->
            if (post.feedType != FeedType.MOMENTS) {
                notifyItemChanged(index, UIPostUpdate.UpdateTimeAgo(post.postId))
            }
        }
    }

    override fun getItemCount(): Int = collection.size

    override fun getItemViewType(position: Int) = collection.getOrNull(position)?.feedType?.viewType ?: UNDEFINED_VIEW_TYPE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val parentWidth = parent.width
        return when (viewType) {
            FeedType.MOMENTS.viewType -> {
                MeeraMomentsViewHolder(binding = parent.inflateBinding(MeeraMomentsPostViewholderBinding::inflate))
            }
            FeedType.MULTIMEDIA_POST.viewType -> {
                MeeraMultimediaPostHolder(
                    view = getLayout(parent, R.layout.meera_item_post),
                    parentWidth = parentWidth,
                    needToShowCommunityLabel = needToShowCommunityLabel,
                    isPostsWithBackground = featureTogglesContainer?.postsWithBackgroundFeatureToggle?.isEnabled.isTrue(),
                    isNeedMediaPositioning = featureTogglesContainer?.postMediaPositioningFeatureToggle?.isEnabled.isTrue()
                )
            }
            FeedType.IMAGE_POST.viewType, FeedType.IMAGE_POST_VIP.viewType -> {
                MeeraImagePostHolder(
                    view = getLayout(parent, R.layout.meera_item_post),
                    parentWidth = parentWidth,
                    needToShowCommunityLabel = needToShowCommunityLabel,
                    isPostsWithBackground = featureTogglesContainer?.postsWithBackgroundFeatureToggle?.isEnabled.isTrue(),
                    isNeedMediaPositioning = featureTogglesContainer?.postMediaPositioningFeatureToggle?.isEnabled.isTrue()
                )
            }
            FeedType.REPOST.viewType, FeedType.REPOST_VIP.viewType -> {
                MeeraRepostViewHolder(
                    view = getLayout(parent, R.layout.meera_item_repost),
                    parentWeight = parentWidth,
                    needToShowCommunityLabel = needToShowCommunityLabel,
                    isPostsWithBackgroundEnabled = featureTogglesContainer?.postsWithBackgroundFeatureToggle?.isEnabled.isTrue(),
                    isNeedMediaPositioning = featureTogglesContainer?.postMediaPositioningFeatureToggle?.isEnabled.isTrue()
                )
            }
            FeedType.VIDEO_POST.viewType, FeedType.VIDEO_POST_VIP.viewType -> {
                MeeraVideoPostHolder(
                    cacheUtilTool = cacheUtil,
                    view = getLayout(parent, R.layout.meera_item_post),
                    parentWidth = parentWidth,
                    needToShowCommunityLabel = needToShowCommunityLabel,
                    isPostsWithBackgroundEnabled = featureTogglesContainer?.postsWithBackgroundFeatureToggle?.isEnabled.isTrue()
                )
            }
            FeedType.VIDEO_REPOST.viewType, FeedType.VIDEO_REPOST_VIP.viewType -> {
                MeeraVideoRepostHolder(
                    cacheUtilTool = cacheUtil,
                    view = getLayout(parent, R.layout.meera_item_repost),
                    parentWeight = parentWidth,
                    needToShowCommunityLabel = needToShowCommunityLabel,
                    isPostsWithBackgroundEnabled = featureTogglesContainer?.postsWithBackgroundFeatureToggle?.isEnabled.isTrue()
                )
            }
            FeedType.PROGRESS.viewType -> MeeraPostProgressHolder(parent.inflate(R.layout.progress_view))
            FeedType.ANNOUNCEMENT.viewType -> MeeraFeatureHolder(
                cacheUtil = cacheUtil,
                view = parent.inflate(R.layout.meera_item_feature),
                parentWidth = parentWidth,
            )
            FeedType.SHIMMER_PLACEHOLDER.viewType -> ShimmerFeedViewHolder(parent)
            FeedType.SHIMMER_MOMENTS_PLACEHOLDER.viewType -> ShimmerMomentViewHolder(
                binding = MomentCarouselShimmerBinding.bind(parent.inflate(R.layout.moment_carousel_shimmer))
            )
            FeedType.RATE_US.viewType -> {
                MeeraRateUsHolder(
                    view = parent.inflate(R.layout.item_meera_post_rate_us),
                    callback = postCallback
                )
            }
            FeedType.POSTS_VIEWED_ROAD.viewType,
            FeedType.POSTS_VIEWED_PROFILE.viewType,
            FeedType.POSTS_VIEWED_PROFILE_VIP.viewType -> {
                val binding = parent.inflateBinding(ItemFeedPostsViewedBinding::inflate)
                MeeraPostsViewedViewHolder(binding = binding)
            }

            FeedType.SYNC_CONTACTS.viewType -> {
                val binding = parent.inflateBinding(MeeraItemRoadSyncContactsBinding::inflate)
                MeeraRoadSyncContactsHolder(binding)
            }
            FeedType.REFERRAL.viewType -> {
                val binding = parent.inflateBinding(ItemRoadReferralBinding::inflate)
                MeeraRoadReferralHolder(binding)
            }
            FeedType.SUGGESTIONS.viewType -> {
                val binding = parent.inflateBinding(MeeraItemProfileSuggestionsFloorBinding::inflate)
                MeeraRoadSuggestionsViewHolder(binding)
            }
            else -> MeeraPostProgressHolder(parent.inflate(R.layout.meera_progress_view))
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (holder is MeeraBasePostHolder) {
            holder.isEventsEnabled = featureTogglesContainer?.mapEventsFeatureToggle?.isEnabled.isTrue()
            holder.initCallbacks(
                meeraPostCallback = postCallback,
                contentManager = contentManager,
                audioFeedHelper = audioFeedHelper,
                blurHelper = blurHelper,
                zoomyProvider = zoomyProvider,
                volumeStateCallback = volumeStateCallback
            )
        }

        if (holder is PostCallbackHolder) {
            holder.initCallback(postCallback)
        }

        if (holder is MeeraMomentsViewHolder) {
            holder.initCallbacks(postCallback, momentCallback)
        }

        val payloadValid = payloads.isNotEmpty() && payloads[0] is UIPostUpdate
        if (payloadValid && holder is MeeraBasePostHolder) {
            holder.updatePayload(payloads[0] as UIPostUpdate)
        } else if (payloadValid && holder is MeeraMomentsViewHolder) {
            holder.updatePayload(payloads[0] as UIPostUpdate.UpdateMoments)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
        val item = getItem(position)
        if ((item?.event == null && item?.parentPost?.event == null)
            || featureTogglesContainer?.mapEventsFeatureToggle?.isEnabled.isNotTrue()
        ) {
            formatter?.format(holder)
        } else {
            formatter?.formatDefault(holder)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is MeeraBasePostHolder) {
            holder.clearResources()
        }

        if (holder is MeeraMomentsViewHolder) {
            momentCallback?.onMomentsCarouselBecomeNotVisible()
            holder.clearResource()
        }

        if (holder is ShimmerMomentViewHolder) {
            holder.clearResource()
        }

        if (holder is MeeraPostsViewedViewHolder) {
            holder.clearResources()
        }

        if (holder is MeeraRoadSyncContactsHolder) {
            holder.clearResources()
        }

        if (holder is MeeraRoadReferralHolder) {
            holder.clearResources()
        }

        if (holder is MeeraRoadSuggestionsViewHolder) {
            holder.clearResources()
        }
        super.onViewRecycled(holder)
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        holder.itemView.tag = true
        if (holder is MeeraBasePostHolder) {
            holder.needToShowRepostBtn = isNeedToShowRepostBtn
            holder.initCallbacks(
                meeraPostCallback = postCallback,
                contentManager = contentManager,
                audioFeedHelper = audioFeedHelper,
                blurHelper = blurHelper,
                zoomyProvider = zoomyProvider,
                volumeStateCallback = volumeStateCallback
            )
        }

        if (holder is PostCallbackHolder) {
            holder.initCallback(postCallback)
        }

        if (holder is MeeraMomentsViewHolder) {
            holder.initCallbacks(postCallback, momentCallback)
        }
        super.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.itemView.tag = false
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = collection[position]
        if (holder is MeeraBasePostHolder) {
            holder.needToShowRepostBtn = isNeedToShowRepostBtn
            holder.initCallbacks(
                meeraPostCallback = postCallback,
                contentManager = contentManager,
                audioFeedHelper = audioFeedHelper,
                blurHelper = blurHelper,
                zoomyProvider = zoomyProvider,
                volumeStateCallback = volumeStateCallback
            )
        }

        if (holder is PostCallbackHolder) {
            holder.initCallback(postCallback)
        }

        if (holder is MeeraMomentsViewHolder) {
            holder.initCallbacks(postCallback, momentCallback)
        }

        when (item.feedType) {
            FeedType.MOMENTS ->
                (holder as MeeraMomentsViewHolder).bind(item)
            FeedType.MULTIMEDIA_POST -> (holder as MeeraMultimediaPostHolder).bind(item)
            FeedType.IMAGE_POST, FeedType.IMAGE_POST_VIP ->
                (holder as MeeraImagePostHolder).bind(item)
            FeedType.VIDEO_POST, FeedType.VIDEO_POST_VIP ->
                (holder as MeeraVideoPostHolder).bind(item)
            FeedType.REPOST, FeedType.REPOST_VIP -> (holder as
                MeeraRepostViewHolder).bind(item)
            FeedType.VIDEO_REPOST, FeedType.VIDEO_REPOST_VIP ->
                (holder as MeeraVideoRepostHolder).bind(item)
            FeedType.PROGRESS -> (holder as MeeraPostProgressHolder).bind()
            FeedType.ANNOUNCEMENT -> (holder as MeeraFeatureHolder).bind(item)
            FeedType.RATE_US -> (holder as MeeraRateUsHolder).bind(item)
            FeedType.POSTS_VIEWED_ROAD,
            FeedType.POSTS_VIEWED_PROFILE, FeedType.POSTS_VIEWED_PROFILE_VIP ->
                (holder as MeeraPostsViewedViewHolder).bind(item.feedType)
            FeedType.REFERRAL -> (holder as MeeraRoadReferralHolder).bind(item)
            FeedType.SUGGESTIONS -> (holder as MeeraRoadSuggestionsViewHolder).bind(item)
            else -> Unit
        }
    }

    fun onDestroyView() {
        postCallback = null
        momentCallback = null
        volumeStateCallback = null
        blurHelper = null
        contentManager= null
        zoomyProvider = null
        cacheUtil = null
        audioFeedHelper = null
        featureTogglesContainer = null
        formatter = null
        submitList(listOf())
        notifyDataSetChanged()
    }

    private class FeedDiffCallback(
        private val oldList: List<PostUIEntity>,
        private val newList: List<PostUIEntity>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldPostItem = oldList[oldItemPosition]
            val newPostItem = newList[newItemPosition]

            return oldPostItem.postId == newPostItem.postId &&
                oldPostItem.feedType == newPostItem.feedType &&
                oldPostItem.getVideoUrl() == newPostItem.getVideoUrl()
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldPost = oldList[oldItemPosition]
            val newPost = newList[newItemPosition]

            if (oldPost.feedType == FeedType.POSTS_VIEWED_ROAD && newPost.feedType == FeedType.POSTS_VIEWED_ROAD) {
                return true
            }
            if (oldPost.feedType == FeedType.POSTS_VIEWED_PROFILE && newPost.feedType == FeedType.POSTS_VIEWED_PROFILE) {
                return true
            }
            if (oldPost.feedType == FeedType.SUGGESTIONS && newPost.feedType == FeedType.SUGGESTIONS) {
                return false
            }
            if (oldPost.feedType == FeedType.REFERRAL && newPost.feedType == FeedType.REFERRAL) {
                return false
            }

            return (oldPost.repostCount == newPost.repostCount &&
                oldPost.commentCount == newPost.commentCount &&
                oldPost.tagSpan == newPost.tagSpan &&
                oldPost.user == newPost.user &&
                oldPost.isPostSubscribed == newPost.isPostSubscribed &&
                oldPost.mainVehicle?.number == newPost.mainVehicle?.number &&
                oldPost.parentPost?.deleted == newPost.parentPost?.deleted &&
                oldPost.reactions?.equals(newPost.reactions) ?: false &&
                oldPost.needToShowFollowButton == newPost.needToShowFollowButton) &&
                oldPost.postUpdatingLoadingInfo == newPost.postUpdatingLoadingInfo &&
                oldPost.moments == newPost.moments &&
                oldPost.media == newPost.media &&
                oldPost.getVideoUrl() == newPost.getVideoUrl() &&
                (oldPost.backgroundUrl == newPost.backgroundUrl || oldPost.backgroundId == newPost.backgroundId) &&
                oldPost.assets == newPost.assets
        }
    }

    companion object {
        private const val UNDEFINED_VIEW_TYPE = -1
    }
}
