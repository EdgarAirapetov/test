package com.numplates.nomera3.modules.user.ui.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.inflate
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.user.ui.entity.UserComplainEntity
import com.numplates.nomera3.modules.user.ui.entity.UserComplainItemType
import kotlin.properties.Delegates


private const val MARGIN_RIGHT_WITH_SHOW_DETAIL = 42
private const val MARGIN_RIGHT_WITHOUT_SHOW_DETAIL = 16


class UserComplainReasonAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    internal var collection: List<UserComplainEntity> by Delegates.observable(emptyList()) { _, _, _ ->
        notifyDataSetChanged()
    }

    var itemClickListener: (UserComplainEntity) -> Unit = { _ -> }

    override fun getItemCount(): Int = collection.size

    override fun getItemViewType(position: Int): Int {
        return if (collection[position].itemType == UserComplainItemType.HEADER)
            UserComplainItemType.HEADER.key else UserComplainItemType.COMPLAIN.key
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            UserComplainItemType.HEADER.key ->
                UserComplainHeaderViewHolder(parent.inflate(R.layout.item_user_complain_header))
            UserComplainItemType.COMPLAIN.key ->
                UserComplainViewHolder(parent.inflate(R.layout.item_user_complain_old))
            else -> throw RuntimeException("ViewType must be defined")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder.itemViewType) {
            UserComplainItemType.HEADER.key ->
                (holder as UserComplainHeaderViewHolder).bind(collection[position])
            UserComplainItemType.COMPLAIN.key ->
                (holder as UserComplainViewHolder).bind(collection[position], itemClickListener)
        }
    }

    class UserComplainHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvTitleHeader: TextView = itemView.findViewById(R.id.tv_title_header)

        fun bind(item: UserComplainEntity) {
            tvTitleHeader.text = item.title
        }
    }


    class UserComplainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val showMore: FrameLayout = itemView.findViewById(R.id.show_more)

        fun bind(item: UserComplainEntity, itemClickListener: (UserComplainEntity) -> Unit) {
            tvTitle.text = item.title

            if (item.isShowDetail) {
                showMore.visible()
                tvTitle.setMargins(end = MARGIN_RIGHT_WITH_SHOW_DETAIL.dp)
            } else {
                showMore.gone()
                tvTitle.setMargins(end = MARGIN_RIGHT_WITHOUT_SHOW_DETAIL.dp)
            }

            itemView.click { itemClickListener(item) }
        }
    }

}
