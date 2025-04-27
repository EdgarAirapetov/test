package com.numplates.nomera3.modules.hashtag.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.meera.core.extensions.asCountString
import com.meera.core.extensions.click
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentHashtagBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentScreenOpenWhere
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.fragment.BaseFeedFragment
import com.numplates.nomera3.modules.feed.ui.fragment.NetworkRoadType
import com.numplates.nomera3.modules.hashtag.ui.adapter.HashtagAdapter
import com.numplates.nomera3.modules.post_view_statistic.data.PostViewRoadSource
import com.numplates.nomera3.modules.remotestyle.presentation.formatter.AllRemoteStyleFormatter
import com.numplates.nomera3.presentation.router.IArgContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class HashtagFragment : BaseFeedFragment<FragmentHashtagBinding>() {
    private var hashtag: String? = null
    private val hashtagAdapter = HashtagAdapter()

    override fun getHashtag(): String? {
        return hashtag
    }

    override fun getAnalyticPostOriginEnum() = DestinationOriginEnum.HASHTAG

    override fun getAmplitudeWhereFromOpened(): AmplitudePropertyWhere = AmplitudePropertyWhere.OTHER

    override fun getAmplitudeWhereProfileFromOpened(): AmplitudePropertyWhere = AmplitudePropertyWhere.FEED

    override fun getWhereFromHashTagPressed(): AmplitudePropertyWhere = AmplitudePropertyWhere.OTHER

    override fun getAmplitudeWhereMomentOpened() = AmplitudePropertyMomentScreenOpenWhere.OTHER

    override fun setTotalPostsCount(count: Long) {
        showTotalPostsCount(count)
        showProgress()
    }

    override fun getFormatter(): AllRemoteStyleFormatter {
        return AllRemoteStyleFormatter(feedViewModel.getSettings())
    }

    override fun onClickScrollUpButton() {
        lifecycleScope.launch(Dispatchers.Main) {
            scrollToTop()
            onRefresh()
        }
    }

    override fun scrollToTopWithoutRefresh() {
        scrollToTop()
    }

    override fun getPostViewRoadSource(): PostViewRoadSource {
        return PostViewRoadSource.Hashtag
    }

    override var needToShowProfile: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hashtag = arguments?.getString(IArgContainer.ARG_HASHTAG)
        if (hashtag?.contains("#") != true) hashtag = "#$hashtag"
        initToolbar()
        initContentRecycler()
        binding?.srlPosts?.setOnRefreshListener {
            onRefresh()
        }
    }

    override fun onDestroyView() {
        binding?.rvContent?.onDestroyView()
        binding?.rvContent?.adapter = null
        super.onDestroyView()
    }

    private fun onRefresh() {
        binding?.srlPosts?.isRefreshing = false
        startLoadPosts()
        showProgress()
    }

    private fun initToolbar() {
        binding?.statusBarHashtag?.updateLayoutParams {
            height = context.getStatusBarHeight()
        }
        binding?.tvHashtag?.text = hashtag?.lowercase(Locale.getDefault())
        binding?.ivBackToolbar?.click { act.onBackPressed() }
    }

    private fun initContentRecycler() {
        initPostsLiveObservable()
        binding?.rvContent?.layoutManager = LinearLayoutManager(context)
        initPostsAdapter(
            lottieAnimationView = binding?.btnScrollRefreshPostRoad,
            recyclerView = binding?.rvContent,
            roadType = NetworkRoadType.HASHTAG
        )
        val concatAdapter = ConcatAdapter(hashtagAdapter, getAdapterPosts())
        binding?.rvContent?.adapter = concatAdapter
        binding?.rvContent?.apply {
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        }
        onRefresh()
    }

    private fun showTotalPostsCount(count: Long) {
        val postsCountString = count.asCountString()
        val postsCountPrefix = getString(R.string.posts_by_hashtag_count_prefix)
        hashtagAdapter.setTotalPostsCountText("$postsCountPrefix $postsCountString")
    }

    private fun showProgress() {
        binding?.rvContent?.visible()
        binding?.pbContentLoading?.gone()
    }

    private fun scrollToTop() {
        binding?.rvContent?.scrollToPosition(0)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentHashtagBinding
        get() = FragmentHashtagBinding::inflate

}
