package com.numplates.nomera3.modules.peoples.ui.content.holder

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.databinding.ItemBloggerMediaContentListBinding
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.adapter.MeeraBloggerMediaContentAdapter
import com.numplates.nomera3.modules.peoples.ui.content.decorator.BloggerMediaContentOffSetsDecorator
import com.numplates.nomera3.modules.peoples.ui.content.decorator.setBloggerMediaSnapHelper
import com.numplates.nomera3.modules.peoples.ui.content.entity.BloggerMediaContentListUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.preloader.getBloggerImagePreload

private const val MEDIA_CONTENT_TOP_OFF_SET = 16
private const val MEDIA_CONTENT_HORIZONTAL_OFF_SET = 8

class MeeraBloggerMediaContentListHolder(
    private val binding: ItemBloggerMediaContentListBinding,
    actionListener: (FriendsContentActions) -> Unit,
    private val scrollListener: (innerPosition: Int, rootPosition: Int) -> Unit
) : BasePeoplesViewHolder<BloggerMediaContentListUiEntity, ItemBloggerMediaContentListBinding>(binding) {

    private var mediaContentAdapter = MeeraBloggerMediaContentAdapter(actionListener)

    init {
        initAdapter()
        initScrollListener()
        initImagePreloader()
    }

    override fun bind(item: BloggerMediaContentListUiEntity) {
        super.bind(item)
        mediaContentAdapter.submitList(item.bloggerPostList)
    }

    fun getRecyclerView() = binding.vgMediaList

    fun scrollToPosition(position: Int) {
        binding.vgMediaList.layoutManager?.scrollToPosition(position)
    }

    fun getContext(): Context = binding.root.context

    private fun initAdapter() = with(binding.vgMediaList) {
        adapter = mediaContentAdapter
        val manager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        manager.recycleChildrenOnDetach = true
        layoutManager = manager
        addItemDecoration(
            BloggerMediaContentOffSetsDecorator(
                mTop = MEDIA_CONTENT_TOP_OFF_SET,
                mLeft = MEDIA_CONTENT_HORIZONTAL_OFF_SET
            )
        )
        itemAnimator = null
        setHasFixedSize(true)
        setBloggerMediaSnapHelper()
        isNestedScrollingEnabled = false
        mediaContentAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }


    private fun initScrollListener() {
        binding.vgMediaList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val firstVisiblePosition = binding.vgMediaList.getFirstVisiblePosition()
                    scrollListener.invoke(firstVisiblePosition, absoluteAdapterPosition)
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }

    private fun initImagePreloader() {
        val imageLoader = getBloggerImagePreload(mediaContentAdapter)
        binding.vgMediaList.addOnScrollListener(imageLoader)
    }
}

