package com.numplates.nomera3.presentation.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.meera.core.utils.enableApprovedIcon
import com.meera.core.utils.getAge
import com.numplates.nomera3.R
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.network.core.INetworkValues.ACCOUNT_TYPE_REGULAR
import com.numplates.nomera3.presentation.view.widgets.VipView

/**
 * This adapter is used for both subscribers and subscriptions list
 * as they have same ui
 * */
class SubscriptionAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val collection = mutableListOf<SubscriptionAdapterModel>()
    var onActionBtnClicked: (SubscriptionAdapterModel) -> Unit = {}
    var onProfileAreaClickCallback: (SubscriptionAdapterModel) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return SubscriptionViewHolder(v)
    }

    override fun getItemCount(): Int = collection.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as SubscriptionViewHolder).bind(collection[position], onActionBtnClicked, onProfileAreaClickCallback, position)
    }

    /**
     * Insert list at the end of the collection
     * */
    fun addData(data: MutableList<SubscriptionAdapterModel>) {
        collection.clear()
        collection.addAll(data)
        notifyDataSetChanged()
    }

    fun addDataSearch(data: MutableList<SubscriptionAdapterModel>){ // with removing duplicate users then better to use RX with dispose
        collection.addAll(data)
        val newList = collection.distinctBy {
            it.user.userId
        }
        collection.clear()
        collection.addAll(newList)
        notifyDataSetChanged()
    }

    fun clearData() {
        collection.clear()
        notifyDataSetChanged()
    }

    fun removeItem(item: Long) {
        val itemPos = getPositionById(item)
        if (itemPos != -1) {
            collection.removeAt(itemPos)
            notifyItemRemoved(itemPos)
        }
    }

    private fun getPositionById(userId: Long): Int {
        for (i in collection.indices) {
            if (collection[i].user.userId == userId)
                return i
        }
        return -1
    }

    /**
     * This viewHolder is used for both subscribers and subscriptions list
     * */
    inner class SubscriptionViewHolder(val v: View) : RecyclerView.ViewHolder(v) {

        private val tvName: TextView = v.findViewById(R.id.tvName)
        private val ivAction: ImageView = v.findViewById(R.id.ivStatus)
        private val vvImage: VipView = v.findViewById(R.id.vipView_friend_holder)
        private val tvAgeLoc: TextView = v.findViewById(R.id.tv_user_age_loc)
        private val iconContainer: FrameLayout = v.findViewById(R.id.action_icon_container)
        private val profileContainer: LinearLayout = v.findViewById(R.id.profile_area)
        private val separator: View = v.findViewById(R.id.v_separator_user)
        private val uniqueNameTextView: TextView = v.findViewById(R.id.uniqueNameTextView)


        fun bind(
                model: SubscriptionAdapterModel,
                onActionBtnClicked: (SubscriptionAdapterModel) -> Unit = {},
                onProfileAreaClicked: (SubscriptionAdapterModel) -> Unit = {},
                position: Int
        ) {
            if (position == collection.size - 1) {
                separator.gone()
            } else {
                separator.visible()
            }

            // setup action btn
            if (model.isSubscription) {
                ivAction.setImageResource(R.drawable.ic_unsubscribe_gray_no_alpha)
            } else {
                ivAction.setImageResource(R.drawable.ic_delete_gray)
            }

            iconContainer.setOnClickListener {
                onActionBtnClicked(model)
            }

            profileContainer.setOnClickListener {
                onProfileAreaClicked(model)
            }

            //setup user avatar
            vvImage.setUp(
                    itemView.context,
                    model.user.avatarSmall,
                    model.user.accountType ?: ACCOUNT_TYPE_REGULAR,
                    model.user.accountColor ?: 0,
                    true
            )
            //setup name
            tvName.text = model.user.name ?: ""
            tvName.enableApprovedIcon(
                enabled = model.user.approved == 1,
                isVip = model.user.accountType != ACCOUNT_TYPE_REGULAR
            )

            //setup city and birth.
            tvAgeLoc.visible()
            model.user.birthday?.let {
                tvAgeLoc.text = getAge(it)
            } ?: kotlin.run {
                tvAgeLoc.text = ""
            }
            model.user.city?.let {
                if (it.name != "") {
                    if (tvAgeLoc.text != "")
                        tvAgeLoc.text = String.format("${tvAgeLoc.text}, ${it.name}")
                    else tvAgeLoc.text = it.name ?: ""
                }
            }

            model.user.uniqueName?.let { uName: String ->
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


    /**
     * This model is used by SubscriptionAdapter
     * */
    data class SubscriptionAdapterModel(
            var user: UserSimple,
            var isSubscription: Boolean = true  // if isSubscription == false => subscriber else subscription
    )
}
