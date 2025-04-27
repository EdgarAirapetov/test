package com.numplates.nomera3.presentation.view.adapter.newuserprofile

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.meera.core.extensions.inflate
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.userprofile.ui.entity.GroupUIModel
import kotlin.properties.Delegates

class GroupsProfileListAdapterNew(private val accountType: Int?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    internal var collection: List<GroupUIModel> by Delegates.observable(emptyList()) { _, _, _ ->
        notifyDataSetChanged()
    }

    internal var clickListener: (GroupUIModel) -> Unit = { _ -> }

    override fun getItemCount(): Int = collection.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(parent.inflate(R.layout.item_profile_group))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val vh = holder as ViewHolder

        vh.bind(collection[position], accountType, clickListener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val ivAvatar: ImageView = itemView.findViewById(R.id.ivAvatar)
        private val tvMembersAmount: TextView = itemView.findViewById(R.id.tvMembersAmount)

        fun bind(group: GroupUIModel, accountType: Int?, clickListener: (GroupUIModel) -> Unit) {
            tvName.text = group.name

            Glide.with(itemView.context)
                    .load(group.avatar)
                    .placeholder(R.drawable.ic_group_avatar_new)
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivAvatar)
            tvMembersAmount.text = itemView.context
                    .getString(R.string.profile_count_of_members_group_new, group.countMembers)

            accountType?.let { type ->
                if (type == 2) {
                    tvName.setTextColor(ContextCompat.getColor(itemView.context, R.color.white_1000))
                    tvMembersAmount.setTextColor(ContextCompat.getColor(itemView.context, R.color.ui_light_gray))
                }
            }

            itemView.setOnClickListener { clickListener(group) }
        }
    }

}
