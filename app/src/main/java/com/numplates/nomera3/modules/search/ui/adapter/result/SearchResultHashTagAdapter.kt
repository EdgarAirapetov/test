package com.numplates.nomera3.modules.search.ui.adapter.result

import android.widget.TextView
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegate
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.search.ui.entity.SearchItem

/**
 * Элемент "ХэшТэг"
 */
fun searchResultHashTagItemAdapterDelegate(selectCallback: (SearchItem.HashTag) -> Unit) =
    adapterDelegate<SearchItem.HashTag, SearchItem>(R.layout.search_result_hashtag_item) {

        val howManyPostText: TextView = findViewById(R.id.how_many_post_text)

        itemView.setOnClickListener {
            selectCallback(item)
        }

        bind {
            howManyPostText.text =
                getString(R.string.hashtag_post_count_template_text, item.count.toString())
        }
    }

fun meeraSearchHashTagShimmerAdapterDelegate() =
    adapterDelegate<SearchItem.HashtagShimmer, SearchItem>(R.layout.meera_item_search_hashtag_shimmer) {}

fun meeraSearchResultHashTagItemAdapterDelegate(selectCallback: (SearchItem.HashTag) -> Unit) =
    adapterDelegate<SearchItem.HashTag, SearchItem>(R.layout.meera_item_search_result_hashtag) {

        val tvHashtagName: TextView = findViewById(R.id.tv_hashtag_name)
        val tvPostsCount: TextView = findViewById(R.id.tv_posts_count)

        itemView.setThrottledClickListener { selectCallback(item) }

        bind {
            val hashtagText = StringBuilder("#").append(item.name).toString()
            tvHashtagName.text = hashtagText
            tvPostsCount.text =
                getString(R.string.hashtag_post_count_template_text, item.count.toString())
        }
    }
