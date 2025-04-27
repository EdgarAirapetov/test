package com.numplates.nomera3.modules.hashtag.ui.fragment

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.asCountString
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.gone
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentHashtagBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentScreenOpenWhere
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.fragment.MeeraBaseFeedFragment
import com.numplates.nomera3.modules.feed.ui.fragment.NetworkRoadType
import com.numplates.nomera3.modules.hashtag.ui.adapter.MeeraHashtagAdapter
import com.numplates.nomera3.modules.post_view_statistic.data.PostViewRoadSource
import com.numplates.nomera3.modules.remotestyle.presentation.formatter.AllRemoteStyleFormatter
import com.numplates.nomera3.modules.upload.util.UPLOAD_BUNDLE_KEY
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_SHOW_MEDIA_GALLERY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class MeeraHashtagFragment : MeeraBaseFeedFragment(R.layout.meera_fragment_hashtag) {

    private val HASHTAG_INITIAL_LOAD_POSTS_DELAY_MILLIS = 100L
    private var hashtag: String? = null
    private val hashtagAdapter by lazy { MeeraHashtagAdapter() }
    private val concatAdapter by lazy { ConcatAdapter(hashtagAdapter, getAdapterPosts()) }

    private val binding by viewBinding(MeeraFragmentHashtagBinding::bind)

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    override fun getHashtag(): String? {
        return hashtag
    }

    override fun isNotCommunityScreen() = true

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

    override fun getRefreshTopButtonView() = binding.btnScrollRefreshPostRoad

    override fun navigateEditPostFragment(post: PostUIEntity?, postStringEntity: String?) {
        findNavController().safeNavigate(
            resId = R.id.action_meeraHashTagFragment_to_meeraCreatePostFragment,
            bundle = bundleOf(
                IArgContainer.ARG_GROUP_ID to post?.groupId?.toInt(),
                ARG_SHOW_MEDIA_GALLERY to false,
                IArgContainer.ARG_POST to post,
                UPLOAD_BUNDLE_KEY to postStringEntity
            )
        )
    }

    override fun getParentContainer(): ViewGroup? = binding.clContent

    override var needToShowProfile: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hashtag = arguments?.getString(IArgContainer.ARG_HASHTAG)
        if (hashtag?.contains("#") != true) hashtag = "#$hashtag"
        initToolbar()
        initContentRecycler()
        binding.srlPosts.setOnRefreshListener {
            onRefresh()
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        })
    }

    override fun onDestroyView() {
        binding.rvContent.release()
        binding.rvContent.adapter = null
        super.onDestroyView()
    }

    private fun onRefresh() {
        binding.srlPosts.isRefreshing = false
        loadBasePosts()
        showProgress()
    }

    private fun initToolbar() {
        binding.apply {
            nvHashtag.title = hashtag?.lowercase(Locale.getDefault())
            nvHashtag.backButtonClickListener = { findNavController().popBackStack() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initRoadTypeAndViewModel(roadType = NetworkRoadType.HASHTAG)
        doDelayed(HASHTAG_INITIAL_LOAD_POSTS_DELAY_MILLIS){
            onRefresh()
        }
    }

    private fun initContentRecycler() {
        initPostsLiveObservable()
        binding.rvContent.layoutManager = LinearLayoutManager(context)
        initPostsAdapter(
            scrollToTopView = binding.btnScrollRefreshPostRoad,
            recyclerView = binding.rvContent,
            roadType = NetworkRoadType.HASHTAG
        )
        binding.rvContent.adapter = concatAdapter
        binding.rvContent.apply {
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        }
        initPostsLoadScrollListener()
    }

    private fun showTotalPostsCount(count: Long) {
        val postsCountString = count.asCountString()
        val postsCountPrefix = getString(R.string.posts_by_hashtag_count_prefix)
        hashtagAdapter.setTotalPostsCountText("$postsCountPrefix $postsCountString")
    }

    private fun showProgress() {
        binding.rvContent.visible()
        binding.pbContentLoading.gone()
    }

    private fun scrollToTop() {
        binding.rvContent.scrollToPosition(0)
    }

}
