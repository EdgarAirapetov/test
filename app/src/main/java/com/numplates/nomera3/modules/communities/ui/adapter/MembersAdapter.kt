package com.numplates.nomera3.modules.communities.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.click
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.meera.core.utils.getAge
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.UserInfoModel
import com.numplates.nomera3.modules.communities.data.entity.CommunityMemberState
import com.numplates.nomera3.modules.communities.data.entity.CommunityUserRole
import com.numplates.nomera3.presentation.view.widgets.VipView


class MembersAdapter(
    private val userId: Long,
    private val userRole: Int,
    private val listType: Int,
    private val hideAgeAndGender: Boolean
) : RecyclerView.Adapter<MembersAdapter.MemberViewHolder>() {

    private val members = mutableListOf<UserInfoModel>()
    var onMemberClicked: ((UserInfoModel) -> Unit?)? = null
    var onMemberActionClicked: ((member: UserInfoModel, position: Int) -> Unit?)? = null
    var onMembershipApproveClicked: ((member: UserInfoModel, position: Int) -> Unit?)? = null

    fun setMembers(list: List<UserInfoModel>) {
        clearMembers()
        members.addAll(list)
        notifyDataSetChanged()
    }

    fun addMembers(list: List<UserInfoModel>) {
        val positionStart = members.size + 1
        members.addAll(list)
        notifyItemRangeInserted(positionStart, list.size)
    }

    fun clearMembers() {
        members.clear()
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        if (position in 0..members.lastIndex) {
            members.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun hasItems() = members.isNotEmpty()

    fun setAdmin(position: Int) {
        if (position in 0..members.lastIndex) {
            members[position].isModerator = true
            notifyItemChanged(position)
        }
    }

    fun setNotAdmin(position: Int) {
        if (position in 0..members.lastIndex) {
            members[position].isModerator = false
            notifyItemChanged(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_community_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) =
        holder.bind(members[position])

    override fun getItemCount() = members.size

    inner class MemberViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        private val tvName: TextView? = v.findViewById(R.id.tv_member_name)
        private val tvAge: TextView? = v.findViewById(R.id.tv_member_age)
        private val tvStatus: TextView? = v.findViewById(R.id.tv_member_status)
        private val ivAvatar: VipView? = v.findViewById(R.id.member_avatar)
        private val ivActions: ImageView? = v.findViewById(R.id.iv_action)
        private val tvUniqueName: TextView? = v.findViewById(R.id.tv_member_unique_name)

        fun bind(user: UserInfoModel) {
            tvName?.text = user.name
            setupUniqueName(user)
            setupAvatar(user)
            setupAgeAndCity(user)
            setupActionIcon(user)
            setupStatus(user)
            itemView.click { onMemberClicked?.invoke(user) }
        }

        private fun setupAvatar(user: UserInfoModel) {
            ivAvatar?.setUp(
                ivAvatar.context,
                user.avatar,
                user.accountType,
                user.accountColor
            )
        }

        private fun setupUniqueName(user: UserInfoModel) {
            val uniqueName = user.uniqname
            if (!uniqueName.isNullOrEmpty()) {
                tvUniqueName?.text = "@$uniqueName"
                tvUniqueName?.visible()
            } else {
                tvUniqueName?.gone()
            }
        }

        private fun setupAgeAndCity(user: UserInfoModel) {
            val birthday = if (hideAgeAndGender) null else user.birthday
            val city = user.cityName
            tvAge?.text = when {
                birthday != null && city != null -> {
                    "${getAge(birthday)}, $city"
                }
                birthday == null -> city
                else -> getAge(birthday)
            }
        }

        private fun setupActionIcon(user: UserInfoModel) {
            when {
                needToHideActionButton(user) -> {
                    ivActions?.gone()
                }
                listType == CommunityMemberState.APPROVED -> {
                    ivActions?.setImageResource(R.drawable.ic_dots_menu)
                    ivActions?.click {
                        onMemberActionClicked?.invoke(user, bindingAdapterPosition)
                    }
                    ivActions?.visible()
                }
                listType == CommunityMemberState.NOT_APPROVED -> {
                    ivActions?.setImageResource(R.drawable.empty_incoming_friends)
                    ivActions?.click {
                        onMembershipApproveClicked?.invoke(user, bindingAdapterPosition)
                    }
                    ivActions?.visible()
                }
            }
        }

        private fun needToHideActionButton(user: UserInfoModel): Boolean{
            return when {
                userId == user.uid -> true
                userRole == CommunityUserRole.REGULAR -> true
                user.isAuthor == true -> true
                else -> false
            }
        }

        private fun setupStatus(user: UserInfoModel) {
            when {
                user.isAuthor == true -> {
                    tvStatus?.visible()
                    tvStatus?.text = tvStatus?.context?.getString(R.string.author)
                }
                user.isModerator == true -> {
                    tvStatus?.visible()
                    tvStatus?.text = tvStatus?.context?.getString(R.string.moderator)
                }
                else -> tvStatus?.gone()
            }
        }
    }
}
