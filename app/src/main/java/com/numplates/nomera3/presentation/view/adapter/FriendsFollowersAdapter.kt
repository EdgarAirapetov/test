package com.numplates.nomera3.presentation.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.model.adaptermodel.FriendsFollowersUiModel
import com.numplates.nomera3.presentation.view.adapter.holders.FriendsFollowerHolder
import timber.log.Timber

class FriendsFollowersAdapter constructor(
    private val actionCallback: SubscriberFriendActionCallback
) : RecyclerView.Adapter<FriendsFollowerHolder>() {

    private val diffUtilItemCallback = object : DiffUtil.ItemCallback<FriendsFollowersUiModel>() {
        override fun areItemsTheSame(
            oldItem: FriendsFollowersUiModel,
            newItem: FriendsFollowersUiModel
        ): Boolean = oldItem.userSimple == newItem.userSimple
                && oldItem.subscriptionType == newItem.subscriptionType

        override fun areContentsTheSame(
            oldItem: FriendsFollowersUiModel,
            newItem: FriendsFollowersUiModel
        ): Boolean = oldItem == newItem
    }

    private val friendsFollowersListDiffer = AsyncListDiffer(this, diffUtilItemCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsFollowerHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)

        return FriendsFollowerHolder(view, actionCallback)
    }

    override fun onBindViewHolder(holder: FriendsFollowerHolder, position: Int) {
        holder.bind(friendsFollowersListDiffer.currentList[position])
    }

    override fun getItemCount() = friendsFollowersListDiffer.currentList.size

    fun updateFriends(newList: List<FriendsFollowersUiModel>) {
        friendsFollowersListDiffer.submitList(newList)
    }

    fun clearList() = friendsFollowersListDiffer.submitList(mutableListOf())

    fun getItemByPosition(position: Int): FriendsFollowersUiModel? {
        return try {
            friendsFollowersListDiffer.currentList[position]
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }
}
