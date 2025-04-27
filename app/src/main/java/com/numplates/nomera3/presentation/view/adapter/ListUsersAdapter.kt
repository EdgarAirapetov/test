package com.numplates.nomera3.presentation.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.presentation.view.fragments.CallListFriendsFragment.Companion.USER_LIST_TYPE_BLACKLIST
import com.numplates.nomera3.presentation.view.fragments.CallListFriendsFragment.Companion.USER_LIST_TYPE_WHITELIST
import com.numplates.nomera3.presentation.view.utils.getAgeCityFormattedText
import com.numplates.nomera3.presentation.view.widgets.VipView


class ListUsersAdapter(
        private var items: MutableList<UserSimple>,
        private val usersListType: Int?,
        private val callback: OnUserActionListener
) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    interface OnUserActionListener {

        fun onUserItemSelect(user: UserSimple)

        fun onUserChecked(user: UserSimple, position: Int, isChecked: Boolean)

    }


    fun updateDataSet(items: List<UserSimple>) {
        this.items.addAll(items)
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

    fun disableCheckbox(position: Int) {
        changeCheckboxState(position, 0)
    }

    fun enableCheckbox(position: Int) {
        changeCheckboxState(position, 1)
    }


    private fun changeCheckboxState(position: Int, state: Int) {
        val item = getItem(position)
        this.items.removeAt(position)
        item.settingsFlags?.isInCallBlacklist = state
        item.settingsFlags?.isInCallWhitelist = state
        this.items.add(position, item)
        notifyItemChanged(position)
    }


    override fun getItemCount(): Int = items.size

    fun getItem(position: Int) = items[position]


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return UsersViewHolderV2(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_simple_user_checkbox, parent, false), usersListType, callback)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val user = getItem(position)
        if (holder is UsersViewHolderV2) {
            holder.bind(user)
        }
    }


    private class UsersViewHolderV2(view: View,
                                    private val usersListType: Int?,
                                    private val callback: OnUserActionListener) : RecyclerView.ViewHolder(view) {

        private val vipView: VipView = view.findViewById(R.id.vipViewFriend)
        private val tvName: TextView = view.findViewById(R.id.tvName)
        private val tvAge: TextView = view.findViewById(R.id.tvAge)
        private val checkBox: CheckBox = view.findViewById(R.id.cbSelectMember)

        fun bind(user: UserSimple) {
            vipView.setUp(
                    vipView.context,
                    user.avatarSmall,
                    user.accountType ?: 0,
                    user.accountColor ?: 0
            )
            tvName.text = user.name
            // Age
            tvAge.text = getAgeCityFormattedText(user)

            // Check box handle
            checkBox.isChecked = setUserCheck(user)

            checkBox.setOnClickListener {
                callback.onUserChecked(user, layoutPosition, checkBox.isChecked)
            }

            vipView.setOnClickListener {
                callback.onUserItemSelect(user)
            }
        }


        private fun setUserCheck(user: UserSimple): Boolean {
            return when (usersListType) {
                USER_LIST_TYPE_BLACKLIST -> {
                    if (user.settingsFlags?.isInCallBlacklist == 1) {
                        return true
                    }
                    false
                }
                USER_LIST_TYPE_WHITELIST -> {
                    if (user.settingsFlags?.isInCallWhitelist == 1) {
                        return true
                    }
                    false
                }
                else -> false
            }
        }

    }


    // ***************** Data binding **************************************************************

    /*override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ItemSimpleUserCheckboxBinding>(inflater,
                R.layout.item_simple_user_checkbox, parent, false)
        return UsersViewHolder(binding.root)
    }*/

    /*override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        val vh = holder as UsersViewHolder
        vh.binding?.user = item
        vh.binding?.isChecked = setUserCheck(item)
        vh.binding?.position = position
        vh.binding?.click = object : OnUserActionListener {
            override fun onUserItemSelect(user: UserSimple) {

            }

            override fun onUserChecked(user: UserSimple, position: Int, isChecked: Boolean) {
                Timber.e("USER Checked CALLBACK!")
                //callback.onUserChecked(user, position, isChecked)
            }
        }

        vh.binding?.cbSelectMember?.setOnCheckedChangeListener { button, b ->
            Timber.e("USER Checked CALLBACK!")
            //callback.onUserChecked(item, position, button.isChecked)
        }
    }*/


    /*private class UsersViewHolder(view: View) : RecyclerView.ViewHolder(view)  {
        var binding: ItemSimpleUserCheckboxBinding? = DataBindingUtil.bind(view)
    }*/

}
