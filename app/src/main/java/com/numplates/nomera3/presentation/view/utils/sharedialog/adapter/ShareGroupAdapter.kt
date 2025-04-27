package com.numplates.nomera3.presentation.view.utils.sharedialog.adapter

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.meera.core.extensions.gone
import com.meera.core.extensions.inflate
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.communities.data.entity.CommunitiesListItemEntity
import com.numplates.nomera3.modules.communities.data.entity.CommunityEntity
import java.util.*


class ShareGroupAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<CommunitiesListItemEntity>()
    private val dataCopy = mutableListOf<CommunitiesListItemEntity>()
    var onClick: ((community: CommunityEntity?) -> Unit)? = null

    var groupRepostId = -1L

    fun addData(data: List<CommunitiesListItemEntity>) {
        this.data.addAll(data)
        dataCopy.addAll(data)
        notifyDataSetChanged()
    }

    fun setData(data: List<CommunitiesListItemEntity>) {
        this.data.clear()
        this.data.addAll(data)
        dataCopy.addAll(data)
        notifyDataSetChanged()
    }

    fun clear() {
        this.data.clear()
        dataCopy.clear()
        notifyDataSetChanged()
    }

    @SuppressLint("DefaultLocale")
    fun filter(text: String, block: ((count: Int) -> Unit)? = null) {
        var query = text
        data.clear()
        if (query.isEmpty()) {
            data.addAll(dataCopy)
        } else {
            query = text.lowercase(Locale.getDefault())
            for (item in dataCopy) {
                if (item.community?.name != null && item.community.name!!.lowercase(Locale.getDefault())
                        .contains(query)) {
                    data.add(item)
                }
            }
        }
        block?.invoke(data.size)
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long = data[position].community?.groupId?.toLong() ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = parent.inflate(R.layout.item_group_share_menu)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) holder.bind(data[position])
    }

    inner class ViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var ivPicture: ImageView = itemView.findViewById(R.id.ivPicture)
        private var tvName: TextView = itemView.findViewById(R.id.tvName)
        private var tvUserCount: TextView = itemView.findViewById(R.id.tvUserCount)
        private var ivShowGroup: ImageView = itemView.findViewById(R.id.ivShowGroup)
        private var ivPrivacy: ImageView = itemView.findViewById(R.id.iv_groups_holder_privacy)
        private var flShareGroupContainer: FrameLayout = itemView.findViewById(R.id.fl_share_group_item_container)
        private var v_separator_group:View = itemView.findViewById(R.id.v_separator_group)

        fun bind(data: CommunitiesListItemEntity) {
            data.community?.let { groupEntity ->
                itemView.setOnClickListener {
                    onClick?.invoke(data.community)
                }
                if (groupEntity.groupId.toLong() == groupRepostId)
                    flShareGroupContainer.visible()
                else flShareGroupContainer.gone()



                Glide.with(ivPicture.context)
                        .load(groupEntity.avatar)
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.ic_group_avatar_new)
                        .into(ivPicture)

                if (groupEntity.private == 0)
                    ivPrivacy.gone()
                else
                    ivPrivacy.visible()


                ivShowGroup.gone()


                tvName.text = groupEntity.name

                tvUserCount.text = tvUserCount.context.getString(R.string.groups_members, groupEntity.users)

            }
        }
    }
}
