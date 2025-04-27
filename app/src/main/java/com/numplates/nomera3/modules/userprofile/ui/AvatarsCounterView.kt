package com.numplates.nomera3.modules.userprofile.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isInvisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meera.uikit.widgets.dp
import com.numplates.nomera3.databinding.CustomviewAvatarCounterBinding
import com.numplates.nomera3.databinding.ItemAvatarCounterBinding
import com.numplates.nomera3.modules.feed.ui.getScreenWidth
import java.util.concurrent.TimeUnit

class AvatarsCounterView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = CustomviewAvatarCounterBinding.inflate(LayoutInflater.from(context), this)

    private val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

    private val adapter by lazy { ItemsAdapter() }

    private var rvAvatars: RecyclerView? = null

    var totalCount: Int = 0
        set(value) {
            field = value
            val llm = (rvAvatars?.layoutManager as? LinearLayoutManager)
            var current = llm?.findFirstVisibleItemPosition() ?: 0
            if (current == -1) current = 0
            selectPosition(current)
            binding.rvItems.isInvisible = value < 2
        }

    private fun scrollToPosition(position: Int) {
        val width = getScreenWidth() - HORIZONTAL_MARGIN
        val offset = width / 2
        linearLayoutManager.scrollToPositionWithOffset(position, offset)
    }

    init {
        binding.rvItems.layoutManager = linearLayoutManager
        binding.rvItems.adapter = adapter
    }

    fun setupViewPager(viewPager2: RecyclerView?) {
        this.rvAvatars = viewPager2
        update()
    }

    private fun update() {
        rvAvatars?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                val llm = (recyclerView.layoutManager as? LinearLayoutManager)
                var position = llm?.findFirstVisibleItemPosition() ?: 0
                if (position == -1) position = 0
                selectPosition(position)
            }
        })
    }

    fun selectPosition(position: Int) {
        adapter.submitList(generateListOfBooleansAllFalseExcept(totalCount, position))
        if (adapter.isScrollable()) scrollToPosition(position)
    }

    private fun generateListOfBooleansAllFalseExcept(count: Int, truePosition: Int): List<Boolean> {
        val list = mutableListOf<Boolean>()
        for (i in 0 until count) list.add(i == truePosition)
        return list
    }

    inner class ItemsAdapter : RecyclerView.Adapter<ItemsAdapter.ItemVH>() {
        private var itemSize = ITEM_MAX_SIZE
            set(value) {
                val oldValue = field
                field = value
                if (value != oldValue) notifyDataSetChanged()
            }

        val list = mutableListOf<Boolean>()

        fun isScrollable(): Boolean {
            return itemSize == ITEM_MIN_SIZE
        }

        fun submitList(list: List<Boolean>?) {
            val count = list?.size ?: 0
            if (count != 0) calculateSize(count)
            this.list.clear()
            list?.let { this.list.addAll(it) }
            postDelayed({
                notifyDataSetChanged()
            }, TimeUnit.MILLISECONDS.toMillis(ANIMATION_DELAY))

        }

        private fun calculateSize(count: Int) {
            val screenWidth = getScreenWidth() - HORIZONTAL_MARGIN
            val preSize = (screenWidth / count) - ITEM_HORIZONTAL_MARGIN

            itemSize = if (preSize in ITEM_MIN_SIZE..ITEM_MAX_SIZE) preSize else {
                if (preSize < ITEM_MIN_SIZE) ITEM_MIN_SIZE
                else ITEM_MAX_SIZE
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ItemVH(
            ItemAvatarCounterBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: ItemVH, position: Int) = holder.onBind(list[position], itemSize)

        inner class ItemVH(val binding: ItemAvatarCounterBinding) : RecyclerView.ViewHolder(binding.root) {
            fun onBind(isSelected: Boolean, itemSize: Int) {
                binding.vItem.updateLayoutParams<ViewGroup.LayoutParams> { width = itemSize }
                binding.root.isSelected = isSelected
            }
        }
    }

    companion object {
        val HORIZONTAL_MARGIN = 32.dp
        val ITEM_HORIZONTAL_MARGIN = 4.dp
        val ITEM_MAX_SIZE = 45.dp
        val ITEM_MIN_SIZE = 2.dp
        const val ANIMATION_DELAY: Long = 200
    }
}
