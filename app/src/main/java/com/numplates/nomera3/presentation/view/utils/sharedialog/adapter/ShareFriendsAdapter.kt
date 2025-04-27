package com.numplates.nomera3.presentation.view.utils.sharedialog.adapter

import android.annotation.SuppressLint
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
import com.numplates.nomera3.presentation.view.adapter.newfriends.FriendModel
import com.numplates.nomera3.presentation.view.widgets.VipView
import timber.log.Timber
import java.util.*

class ShareFriendsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<FriendModel>()
    private val dataCopy = mutableListOf<FriendModel>()
    private val checkedList = mutableListOf<FriendModel>()
    var observedCheckedList: ((checkedList: List<FriendModel>) -> Unit)? = null

    fun addData(data: List<FriendModel>) {
        this.data.addAll(data)
        dataCopy.addAll(data)
        notifyDataSetChanged()
    }

    fun setData(data: List<FriendModel>) {
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
                if (item.userModel.name.lowercase(Locale.getDefault()).contains(query)) {
                    data.add(item)
                }
            }
        }
        block?.invoke(data.size)
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long = data[position].userModel.userId

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(parent.inflate(R.layout.item_share_friends_list))
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) holder.bind(data[position])
    }

    inner class ViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var vipView_friend_holder : VipView = itemView.findViewById(R.id.vipView_friend_holder)
        private var tv_share_friend_name : TextView = itemView.findViewById(R.id.tv_share_friend_name)
        private var tv_user_age_loc : TextView = itemView.findViewById(R.id.tv_user_age_loc)
        private var uniqueNameTextView : TextView = itemView.findViewById(R.id.uniqueNameTextView)
        var cb_share_friend : CheckBox = itemView.findViewById(R.id.cb_share_friend)

        fun bind(data: FriendModel) {
            val item = data.userModel
            vipView_friend_holder.setUp(
                    itemView.context,
                    item.avatar,
                    item.accountType,
                    item.accountColor ?: 0,
                    true
            )
            tv_share_friend_name.text = item.name
            tv_user_age_loc.visible()
            item.birthday?.let {
                if (it == -1L)//логика ios
                    tv_user_age_loc.text = ""
                else
                    tv_user_age_loc.text = getAge(it)
            } ?: kotlin.run {
                tv_user_age_loc.text = ""
            }
            item.city?.let {
                if (it != "") {
                    if (tv_user_age_loc.text != "")
                        tv_user_age_loc.text = String.format("${tv_user_age_loc.text}, $it")
                    else tv_user_age_loc.text = it
                }
            }
            if(!cb_share_friend.isChecked && checkedList.size == 10){
                cb_share_friend.isEnabled = false
                itemView.isEnabled = false
                itemView.isClickable = false
                cb_share_friend.isClickable = false
            }else {
                cb_share_friend.isEnabled = true
                itemView.isEnabled = true

                itemView.setOnClickListener {
                    cb_share_friend.isChecked = !cb_share_friend.isChecked
                    handleClick(data)
                }
                cb_share_friend.setOnClickListener {
                    handleClick(data)
                }
            }
            Timber.d("Bazaleev: item = ${item.isChecked}")
            cb_share_friend.isChecked = item.isChecked


            data.userSimple?.uniqueName?.let { uName: String ->
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

        private fun handleClick(data: FriendModel){
            try {
                if (checkedList.size >= 10 && cb_share_friend.isChecked) {
                    cb_share_friend.isChecked = false
                    return
                }
                data.userModel.isChecked = cb_share_friend.isChecked
                if (cb_share_friend.isChecked) {
                    checkedList.add(data)
                } else {
                    checkedList.remove(data)
                }
                observedCheckedList?.invoke(checkedList)
            } catch (e: Exception) {
                Timber.e(e)
                // ignore
            }
        }
    }
}
