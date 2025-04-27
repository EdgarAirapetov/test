package com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.gone
import com.meera.core.extensions.inflate
import com.meera.core.extensions.visible
import com.meera.core.utils.getAge
import com.numplates.nomera3.R
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.presentation.view.widgets.VipView

class BaseSettingsListSearchUsersAdapter(
        private var items: MutableList<UserSimple>,
        private val callback: OnUserInteractionCallback
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    interface OnUserInteractionCallback {

        fun onUserAvatarClick(user: UserSimple)

        fun onUserChecked(user: UserSimple, position: Int, isChecked: Boolean)

    }


    fun updateDataSet(users: List<UserSimple>) {
        this.items.addAll(users)
        notifyDataSetChanged()
    }

    fun deleteItem(position: Int) {
        this.items.removeAt(position)
        notifyItemRemoved(position)
    }

    fun clearList() {
        this.items.clear()
        notifyDataSetChanged()
    }

    // --- For saving state check box in recycler view

    fun disableCheckbox(position: Int) {
        changeCheckboxState(position, false)
    }

    fun enableCheckbox(position: Int) {
        changeCheckboxState(position, true)
    }

    private fun changeCheckboxState(position: Int, isChecked: Boolean) {
        val item = getItem(position)
        item.isChecked = isChecked
        notifyItemChanged(position)
    }


    override fun getItemCount(): Int = items.size


    fun getItem(position: Int) = items[position]

    fun getAllItems() = items


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return UsersViewHolderV2(parent.inflate(R.layout.item_user_search_checkbox),callback)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val user = getItem(position)
        if (holder is UsersViewHolderV2) {
            holder.bind(user)
        }
    }


    private class UsersViewHolderV2(val itemView: View,
                                    private val callback: OnUserInteractionCallback) : RecyclerView.ViewHolder(itemView) {

        //private val ivAvatar: ImageView = view.iv_user_avatar
        private val vipView: VipView = itemView.findViewById(R.id.vipView_user_avatar)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvAge: TextView = itemView.findViewById(R.id.tvAge)
        private val checkBox: CheckBox = itemView.findViewById(R.id.cbSelectMember)
        private val uniqueNameTextView: TextView = itemView.findViewById(R.id.uniqueNameTextView)

        fun bind(user: UserSimple) {
            vipView.setUp(
                    vipView.context,
                    user.avatarSmall,
                    user.accountType ?: 0,
                    user.accountColor ?: 0
            )
            /*Glide.with(itemView.context)
                    .load(user.avatarSmall)
                    .apply(RequestOptions.circleCropTransform())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.fill_8_round)
                    .into(ivAvatar)*/

            tvName.text = user.name
            // Age
            var age = ""
            val city = user.city?.name?: ""
            user.birthday?.let {
                age = getAge(it)
            }
            if (age.isEmpty()){
                tvAge.text = city
            }else{
                if (city.isNotEmpty())
                    tvAge.text = "$age, $city"
                else tvAge.text = age
            }
//            val age = NTime.getAge(user.birthday ?: 0)
//            val city = user.city?.name
//            tvAge.text = "$age, $city"

            // Check box handle
            checkBox.isChecked = user.isChecked

            checkBox.setOnClickListener {
                callback.onUserChecked(user, layoutPosition, checkBox.isChecked)
            }

            vipView.setOnClickListener {
                callback.onUserAvatarClick(user)
            }

            user.uniqueName?.let { uName: String ->
                if (uName.isNotEmpty()) {
                    val formattedUniqueName = "@$uName"
                    uniqueNameTextView.text = formattedUniqueName
                    uniqueNameTextView.visible()
                } else {
                    uniqueNameTextView.gone()
                }
            } ?: kotlin.run {
                uniqueNameTextView.gone()
            }

        }
    }
}
