package com.numplates.nomera3.presentation.view.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.numplates.nomera3.databinding.MeeraUserItemBinding
import com.numplates.nomera3.presentation.model.adaptermodel.FriendsFollowersUiModel
import com.numplates.nomera3.presentation.view.adapter.holders.MeeraFriendsFollowerAction
import com.numplates.nomera3.presentation.view.adapter.holders.MeeraFriendsFollowerHolder
import com.numplates.nomera3.presentation.view.utils.inflateBinding
import timber.log.Timber

class MeeraFriendsFollowersAdapter(
    private val actionListener: (action: MeeraFriendsFollowerAction) -> Unit
) : ListAdapter<FriendsFollowersUiModel, MeeraFriendsFollowerHolder>(diffUtilItemCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeeraFriendsFollowerHolder {
        return MeeraFriendsFollowerHolder(parent.inflateBinding(MeeraUserItemBinding::inflate), actionListener)
    }

    override fun onBindViewHolder(holder: MeeraFriendsFollowerHolder, position: Int) {
        holder.bind(currentList[position], position == currentList.lastIndex)
    }

    fun getItemByPosition(position: Int): FriendsFollowersUiModel? {
        return try {
            currentList[position]
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }
}

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
