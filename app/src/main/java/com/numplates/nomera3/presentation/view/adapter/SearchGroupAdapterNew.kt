package com.numplates.nomera3.presentation.view.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.communities.data.entity.CommunityEntity
import com.meera.core.extensions.inflate
import kotlin.properties.Delegates


class SearchGroupAdapterNew : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    internal var collection: List<CommunityEntity?> by Delegates.observable(emptyList()) { _, _, _ ->
        notifyDataSetChanged()
    }

    internal var clickListener: (CommunityEntity?) -> Unit = { _ -> }


    override fun getItemCount(): Int = collection.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(parent.inflate(R.layout.item_group_search_new))


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val vh = holder as ViewHolder

        vh.bind(collection[position], clickListener)
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivGroupAvatar: ImageView = itemView.findViewById(R.id.iv_group_avatar)
        private val tvGroupName: TextView = itemView.findViewById(R.id.tv_group_name)
        private val tvCountMembers: TextView = itemView.findViewById(R.id.tv_count_members)

        fun bind(community: CommunityEntity?, clickListener: (CommunityEntity?) -> Unit) {
            Glide.with(itemView.context)
                    .load(community?.avatar)
                    .placeholder(R.drawable.ic_group_avatar_new)
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivGroupAvatar)

            tvGroupName.text = community?.name
            tvCountMembers.text =
                    itemView.context.getString(R.string.group_members_count, community?.users)

            itemView.setOnClickListener { clickListener(community) }
        }
    }

}