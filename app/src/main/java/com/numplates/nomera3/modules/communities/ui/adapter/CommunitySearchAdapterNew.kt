package com.numplates.nomera3.modules.communities.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.meera.core.extensions.click
import com.meera.core.extensions.gone
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.communities.data.entity.CommunityEntity
import com.numplates.nomera3.modules.communities.ui.entity.CommunityListItemUIModel
import com.meera.core.extensions.pluralString
import com.meera.core.extensions.visible

class CommunitySearchAdapterNew(
    private var list: MutableList<CommunityListItemUIModel>
) : RecyclerView.Adapter<CommunitySearchAdapterNew.ViewHolder>() {

    private var adapterInteractor: IOnSearchGroup? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_group, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position])
    }

    override fun getItemCount(): Int = list.size

    fun setInteractor(interactor: IOnSearchGroup) {
        adapterInteractor = interactor
    }

    fun replace(items: List<CommunityListItemUIModel>?) {
        clear()
        items?.let {
            list.clear()
            list.addAll(it)
            notifyDataSetChanged()
        }
    }

    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }

    fun addItems(items: List<CommunityListItemUIModel>?) {
        items?.let {
            list.addAll(it)
            notifyDataSetChanged()
        }
    }

    fun setItemSubscriptionStatus(position: Int, subscribed: Boolean) {
        if (list.isEmpty()) return
        list[position].isMember = subscribed
        notifyItemChanged(position)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private var ivPicture: ImageView = view.findViewById(R.id.ivPicture)
        private var tvName: TextView = view.findViewById(R.id.tvName)
        private var tvUserCount: TextView = view.findViewById(R.id.tvUserCount)
        private var ivJoinGroup: ImageView? = view.findViewById(R.id.iv_join_icon)
        private var tvUserStatus: TextView? = view.findViewById(R.id.tv_user_status)
        private var ivPrivacy: ImageView = view.findViewById(R.id.iv_groups_holder_privacy)
        private var clContent: ConstraintLayout = view.findViewById(R.id.clContent)
        private lateinit var community: CommunityListItemUIModel

        init {
            clContent.setOnClickListener {
                adapterInteractor?.onGroupClicked(community)
            }
        }

        fun bindData(data: CommunityListItemUIModel) {
            community = data
            data.let { groupRecyclerModel ->
                groupRecyclerModel.let { groupEntity ->

                    Glide.with(ivPicture.context)
                        .load(groupEntity.coverImage)
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.ic_group_avatar_new)
                        .into(ivPicture)


                    if (groupEntity.isPrivate) {
                        ivPrivacy.visible()
                    } else {
                        ivPrivacy.gone()
                    }

                    if (groupEntity.isMember
                        || groupEntity.userStatus == CommunityEntity.USER_STATUS_NOT_YET_APPROVED) {
                        ivJoinGroup?.gone()
                    } else {
                        ivJoinGroup?.visible()
                    }

                    when {
                        groupRecyclerModel.isCreator -> {
                            tvUserStatus?.text = itemView.context?.getString(R.string.author)
                            tvUserStatus?.visible()
                        }
                        groupRecyclerModel.isModerator -> {
                            tvUserStatus?.text = itemView.context?.getString(R.string.moderator)
                            tvUserStatus?.visible()
                        }
                        else -> {
                            tvUserStatus?.gone()
                        }
                    }

                    tvName.text = groupEntity.name

                    tvUserCount.text =
                        tvUserCount.context.pluralString(R.plurals.group_members_plural, groupEntity.memberCount)

                    ivJoinGroup?.click {
                        adapterInteractor?.onGroupJoinClicked(
                            groupRecyclerModel,
                            bindingAdapterPosition
                        )
                    }

                }
            }
        }
    }

    interface IOnSearchGroup {
        fun onGroupClicked(community: CommunityListItemUIModel)
        fun onGroupJoinClicked(model: CommunityListItemUIModel, position: Int)
    }
}