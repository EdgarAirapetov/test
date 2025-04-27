package com.numplates.nomera3.modules.complains.ui.reason

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.click
import com.meera.core.extensions.inflate
import com.numplates.nomera3.R
import com.numplates.nomera3.R.layout
import com.numplates.nomera3.modules.complains.ui.model.UserComplainUiModel
import com.numplates.nomera3.modules.user.ui.entity.UserComplainItemType


class UserComplainReasonAdapter(
    private val itemClickListener: (UserComplainUiModel) -> Unit = { _ -> }
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val collection = mutableListOf<UserComplainUiModel>()

    override fun getItemCount(): Int = collection.size

    override fun getItemViewType(position: Int): Int {
        return if (collection[position].itemType == UserComplainItemType.HEADER)
            UserComplainItemType.HEADER.key else UserComplainItemType.COMPLAIN.key
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            UserComplainItemType.HEADER.key ->
                UserComplainHeaderViewHolder(parent.inflate(layout.item_user_complain_header))
            UserComplainItemType.COMPLAIN.key ->
                UserComplainViewHolder(parent.inflate(layout.item_user_complain))
            else -> throw RuntimeException("ViewType must be defined")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            UserComplainItemType.HEADER.key ->
                (holder as UserComplainHeaderViewHolder).bind(collection[position])
            UserComplainItemType.COMPLAIN.key ->
                (holder as UserComplainViewHolder).bind(collection[position], itemClickListener)
        }
    }

    fun updateItems(items: List<UserComplainUiModel>) {
        collection.clear()
        collection.addAll(items)
        notifyDataSetChanged()
    }

    class UserComplainHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvTitleHeader: TextView = itemView.findViewById(R.id.tv_title_header)

        fun bind(item: UserComplainUiModel) {
            tvTitleHeader.setText(item.titleRes)
        }
    }


    class UserComplainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)

        fun bind(item: UserComplainUiModel, itemClickListener: (UserComplainUiModel) -> Unit) {
            tvTitle.setText(item.titleRes)
            itemView.click { itemClickListener(item) }
        }
    }
}
