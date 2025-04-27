package com.numplates.nomera3.presentation.view.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.meera.core.utils.getAge
import com.numplates.nomera3.R
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.network.core.INetworkValues
import com.meera.core.utils.enableApprovedIcon
import com.numplates.nomera3.presentation.view.widgets.VipView

class OutgoingFriendshipRequestsAdapter(
        private var requestList: MutableList<UserSimple> = mutableListOf(),
        private var itemClickListener: OutgoingFriendshipRequestsClickListener
) : RecyclerView.Adapter<OutgoingFriendshipRequestsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_user, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(requestList[position])
    }

    override fun getItemCount(): Int = requestList.size


    fun addElements(newUsers: MutableList<UserSimple>) {
        requestList.addAll(newUsers)
        notifyItemInserted(requestList.size - 1)
    }

    fun clear() {
        requestList.clear()
        notifyDataSetChanged()
    }

    fun removeItem(userSimple: UserSimple) {
        val index = requestList.indexOf(userSimple)

        requestList.removeAt(index)
        notifyItemRemoved(index)
        notifyItemRangeChanged(index, requestList.size - 1)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private lateinit var userSimple: UserSimple

        private var tvName: TextView = view.findViewById(R.id.tvName)
        private var ivAction: ImageView = view.findViewById(R.id.ivStatus)
        private var vvImage: VipView = view.findViewById(R.id.vipView_friend_holder)
        private var llContainer: LinearLayout = view.findViewById(R.id.llContent)
        private var tvAgeLoc: TextView = view.findViewById(R.id.tv_user_age_loc)
        private var uniqueNameTextView: TextView = view.findViewById(R.id.uniqueNameTextView)

        init {
            llContainer.setOnClickListener {
                itemClickListener.onItemClicked(userSimple)
            }

            ivAction.setOnClickListener {
                itemClickListener.onActionClicked(userSimple)
            }
        }

        fun bind(newUserSimple: UserSimple) {
            userSimple = newUserSimple

            ivAction.setImageResource(R.drawable.outgoing_requests_gray)

            vvImage.setUp(
                    itemView.context,
                    userSimple.avatarSmall,
                    userSimple.accountType?: 0,
                    userSimple.accountColor ?: 0,
                    true
            )
            tvName.text = userSimple.name
            tvName.enableApprovedIcon(
                enabled = userSimple.approved == 1,
                isVip = userSimple.accountType != INetworkValues.ACCOUNT_TYPE_REGULAR
            )
            tvAgeLoc.visible()
            userSimple.birthday?.let {
                if (it == -1L)//логика ios
                    tvAgeLoc.text = ""
                else
                    tvAgeLoc.text = getAge(it)
            } ?: kotlin.run {
                tvAgeLoc.text = ""
            }
            userSimple.city?.name?.let {
                if (it != "") {
                    if (tvAgeLoc.text != "")
                        tvAgeLoc.text = String.format("${tvAgeLoc.text}, $it")
                    else tvAgeLoc.text = it
                }
            }

            userSimple.uniqueName?.let { uName: String ->
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
