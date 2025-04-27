package com.numplates.nomera3.modules.userprofile.ui.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.modules.baseCore.ui.location.LocationContract
import com.numplates.nomera3.modules.userprofile.domain.maper.compare
import com.numplates.nomera3.modules.userprofile.ui.action.NestedRecyclerAction
import com.numplates.nomera3.modules.userprofile.ui.action.outsideAction.IOutStatusFloor
import com.numplates.nomera3.modules.userprofile.ui.action.outsideAction.OutsideUserProfileAction
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityGalleryFloor
import com.numplates.nomera3.modules.userprofile.ui.entity.UserUIEntity
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction
import com.numplates.nomera3.modules.userprofile.ui.viewholder.BannerFloorViewHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.BaseUserViewHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.BirthdayFloorViewHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.ClosedProfileFloorViewHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.DefaultSkeletonFloorViewHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.FriendSubscribeFloorViewHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.GalleryFloorViewHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.GarageFloorViewHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.GiftsFloorViewHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.GroupFloorViewHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.HolidayFloorViewHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.MapFloorViewHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.MutualSubscribersHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.ProfileSuggestionsFloorViewHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.RoadFloorViewHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.SubscribeSkeletonFloorViewHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.SubscribersFloorViewHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.UpdateAppViewHolder

class UserProfileAdapter(
    private val locationContract: LocationContract,
    private val userProfileAction: NestedRecyclerAction,
    private val profileUIActionHandler: (UserProfileUIAction) -> Unit = { _ -> },
) : RecyclerView.Adapter<BaseUserViewHolder<UserUIEntity>>() {

    private val settOfActions = mutableSetOf<OutsideUserProfileAction>()
    private val asyncDiffer = AsyncListDiffer(this,
        object : DiffUtil.ItemCallback<UserUIEntity>() {
            override fun areItemsTheSame(oldItem: UserUIEntity, newItem: UserUIEntity): Boolean {
                return oldItem.type == newItem.type
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: UserUIEntity, newItem: UserUIEntity): Boolean {
                return oldItem.compare(newItem)
            }
        }
    )

    override fun getItemViewType(position: Int) = asyncDiffer.currentList[position].type.value

    override fun getItemCount() = asyncDiffer.currentList.size

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseUserViewHolder<UserUIEntity> {
        val viewHolder = when (viewType) {
            UserProfileAdapterType.UPDATE_BTN.value -> {
                UpdateAppViewHolder(parent, profileUIActionHandler)
            }
            UserProfileAdapterType.FRIEND_SUBSCRIBE_FLOOR.value -> {
                FriendSubscribeFloorViewHolder(parent, profileUIActionHandler)
            }
            UserProfileAdapterType.SUBSCRIBERS_FLOOR.value -> {
                SubscribersFloorViewHolder(parent, profileUIActionHandler)
            }
            UserProfileAdapterType.BANNER_FLOOR.value -> {
                BannerFloorViewHolder(parent, profileUIActionHandler)
            }
            UserProfileAdapterType.GALLERY_FLOOR.value -> {
                GalleryFloorViewHolder(parent, userProfileAction,profileUIActionHandler)
            }
            UserProfileAdapterType.GROUPS_FLOOR.value -> {
                GroupFloorViewHolder(parent, profileUIActionHandler)
            }
            UserProfileAdapterType.GIFTS_FLOOR.value -> {
                GiftsFloorViewHolder(parent, profileUIActionHandler)
            }
            UserProfileAdapterType.ROAD_FLOOR.value -> {
                RoadFloorViewHolder(parent, profileUIActionHandler)
            }
            UserProfileAdapterType.GARAGE_FLOOR.value -> {
                GarageFloorViewHolder(parent, profileUIActionHandler)
            }
            UserProfileAdapterType.BIRTHDAY_FLOOR.value -> {
                BirthdayFloorViewHolder(parent, profileUIActionHandler)
            }
            UserProfileAdapterType.CLOSED_PROFILE_FLOOR.value -> {
                ClosedProfileFloorViewHolder(parent)
            }
            UserProfileAdapterType.MAP_FLOOR.value -> {
                MapFloorViewHolder(parent, locationContract, profileUIActionHandler)
            }
            UserProfileAdapterType.HOLIDAY_FLOOR.value -> {
                HolidayFloorViewHolder(parent, profileUIActionHandler)
            }
            UserProfileAdapterType.DEFAULT_SKELETON_FLOOR.value -> {
                DefaultSkeletonFloorViewHolder(parent)
            }
            UserProfileAdapterType.SUBSCRIBE_SKELETON_FLOOR.value -> {
                SubscribeSkeletonFloorViewHolder(parent)
            }
            UserProfileAdapterType.MUTUAL_SUBSCRIBERS_FLOOR.value -> {
                MutualSubscribersHolder(parent, profileUIActionHandler)
            }
            UserProfileAdapterType.PROFILE_SUGGESTIONS.value -> {
                ProfileSuggestionsFloorViewHolder(parent, userProfileAction, profileUIActionHandler)
            }

            else -> error("Please specify a correct view holder type.")
        }
        return viewHolder as BaseUserViewHolder<UserUIEntity>
    }

    override fun onBindViewHolder(
        holder: BaseUserViewHolder<UserUIEntity>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty() && asyncDiffer.currentList[position] is UserEntityGalleryFloor) {
            when (payloads[0]) {
                is UserPayload.ShowGalleryProgress -> {
                    (holder as? GalleryFloorViewHolder)?.showProgress()
                }
                is UserPayload.HideGalleryProgress -> {
                    (holder as? GalleryFloorViewHolder)?.hideProgress()
                }
                else -> {
                    super.onBindViewHolder(holder, position, payloads)
                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onBindViewHolder(holder: BaseUserViewHolder<UserUIEntity>, position: Int) {
        holder.bind(getData(position) ?: return)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : UserUIEntity> getData(position: Int): T? {
        return asyncDiffer.currentList[position] as? T
    }

    fun callIOutStatusFloor(): IOutStatusFloor? {
        return settOfActions.findLast { it is IOutStatusFloor } as? IOutStatusFloor
    }

    fun refresh(newData: List<UserUIEntity>, callback: () -> Unit = {}) {
        asyncDiffer.submitList(newData, callback)
    }

    fun showProgressGallery() {
        asyncDiffer.currentList.forEachIndexed { index, userUIEntity ->
            if (userUIEntity is UserEntityGalleryFloor) {
                (asyncDiffer.currentList[index] as? UserEntityGalleryFloor)?.isLoading = true
                notifyItemChanged(index, UserPayload.ShowGalleryProgress)
            }
        }
    }

    fun hideProgressGallery() {
        asyncDiffer.currentList.forEachIndexed { index, userUIEntity ->
            if (userUIEntity is UserEntityGalleryFloor) {
                (asyncDiffer.currentList[index] as? UserEntityGalleryFloor)?.isLoading = false
                notifyItemChanged(index, UserPayload.HideGalleryProgress)
            }
        }
    }
}

enum class UserProfileAdapterType(var value: Int) {
    UPDATE_BTN(0),
    SUBSCRIBERS_FLOOR(1),
    BANNER_FLOOR(2),
    GALLERY_FLOOR(3),
    GROUPS_FLOOR(4),
    GIFTS_FLOOR(5),
    ROAD_FLOOR(6),
    GARAGE_FLOOR(7),
    BIRTHDAY_FLOOR(8),
    MAP_FLOOR(9),
    HOLIDAY_FLOOR(10),
    FRIEND_SUBSCRIBE_FLOOR(11),
    DEFAULT_SKELETON_FLOOR(12),
    SUBSCRIBE_SKELETON_FLOOR(13),
    MUTUAL_SUBSCRIBERS_FLOOR(14),
    PROFILE_SUGGESTIONS(15),
    CLOSED_PROFILE_FLOOR(16),
    BLOCKED_PROFILE_FLOOR(17),
    BLOCKED_ME_PROFILE_FLOOR(18)
}

sealed class UserPayload {
    object ShowGalleryProgress : UserPayload()
    object HideGalleryProgress : UserPayload()
}
