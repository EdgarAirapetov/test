package com.numplates.nomera3.modules.userprofile.ui.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import com.meera.core.adapters.baserecycleradapter.BaseAsyncAdapter
import com.meera.core.adapters.baserecycleradapter.BaseDiffUtil
import com.meera.core.adapters.baserecycleradapter.BaseVH
import com.meera.core.adapters.baserecycleradapter.RecyclerData
import com.numplates.nomera3.databinding.MeeraBirthdayFloorItemBinding
import com.numplates.nomera3.databinding.MeeraBlockedProfileFloorItemBinding
import com.numplates.nomera3.databinding.MeeraClosedProfileFloorItemBinding
import com.numplates.nomera3.databinding.MeeraFriendSubscribeFloorItemBinding
import com.numplates.nomera3.databinding.MeeraGalleryFloorItemBinding
import com.numplates.nomera3.databinding.MeeraItemGarageFloorBinding
import com.numplates.nomera3.databinding.MeeraItemProfileSuggestionsFloorBinding
import com.numplates.nomera3.databinding.MeeraItemRoadFloorBinding
import com.numplates.nomera3.databinding.MeeraMutualSubscribersItemBinding
import com.numplates.nomera3.databinding.MeeraSubscriberFloorItemBinding
import com.numplates.nomera3.databinding.MeeraUpdateAppItemBinding
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.ui.Separable
import com.numplates.nomera3.modules.userprofile.ui.adapter.UserProfileAdapterType
import com.numplates.nomera3.modules.userprofile.ui.entity.GalleryPhotoEntity
import com.numplates.nomera3.modules.userprofile.ui.entity.ProfileSuggestionUiModels
import com.numplates.nomera3.modules.userprofile.ui.entity.VehicleUIModel
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction
import com.numplates.nomera3.modules.userprofile.ui.viewholder.MeeraBirthdayFloorViewHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.MeeraBlockedMeProfileFloorViewHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.MeeraBlockedProfileFloorViewHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.MeeraClosedProfileFloorViewHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.MeeraFriendSubscribeFloorViewHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.MeeraGalleryFloorViewHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.MeeraGarageFloorViewHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.MeeraMutualSubscribersHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.MeeraProfileSuggestionsFloorViewHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.MeeraRoadFloorViewHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.MeeraSubscribersFloorViewHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.MeeraUpdateAppViewHolder
import com.numplates.nomera3.presentation.model.MutualUser

class UserInfoAdapter(
    private val profileUIActionHandler: (UserProfileUIAction) -> Unit,
) :
    BaseAsyncAdapter<String, UserInfoRecyclerData>(UserInfoDiffUtil()) {
    override fun getHolderType(viewType: Int, parent: ViewGroup): BaseVH<UserInfoRecyclerData, *> {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            UserProfileAdapterType.GARAGE_FLOOR.value -> {
                val binding = MeeraItemGarageFloorBinding.inflate(inflater, parent, false)
                return MeeraGarageFloorViewHolder(binding, profileUIActionHandler)
            }

            UserProfileAdapterType.SUBSCRIBERS_FLOOR.value -> {
                val binding = MeeraSubscriberFloorItemBinding.inflate(inflater, parent, false)
                return MeeraSubscribersFloorViewHolder(binding, profileUIActionHandler)
            }

            UserProfileAdapterType.GALLERY_FLOOR.value -> {
                val binding = MeeraGalleryFloorItemBinding.inflate(inflater, parent, false)
                return MeeraGalleryFloorViewHolder(binding, profileUIActionHandler)
            }

            UserProfileAdapterType.PROFILE_SUGGESTIONS.value -> {
                val binding = MeeraItemProfileSuggestionsFloorBinding.inflate(inflater, parent, false)
                return MeeraProfileSuggestionsFloorViewHolder(binding, profileUIActionHandler)
            }

            UserProfileAdapterType.FRIEND_SUBSCRIBE_FLOOR.value -> {
                val binding = MeeraFriendSubscribeFloorItemBinding.inflate(inflater, parent, false)
                return MeeraFriendSubscribeFloorViewHolder(binding, profileUIActionHandler)
            }

            UserProfileAdapterType.MUTUAL_SUBSCRIBERS_FLOOR.value -> {
                val binding = MeeraMutualSubscribersItemBinding.inflate(inflater, parent, false)
                return MeeraMutualSubscribersHolder(binding, profileUIActionHandler)
            }

            UserProfileAdapterType.UPDATE_BTN.value -> {
                val binding = MeeraUpdateAppItemBinding.inflate(inflater, parent, false)
                return MeeraUpdateAppViewHolder(binding, profileUIActionHandler)
            }

            UserProfileAdapterType.BIRTHDAY_FLOOR.value -> {
                val binding = MeeraBirthdayFloorItemBinding.inflate(inflater, parent, false)
                return MeeraBirthdayFloorViewHolder(binding, profileUIActionHandler)
            }

            UserProfileAdapterType.CLOSED_PROFILE_FLOOR.value -> {
                val binding = MeeraClosedProfileFloorItemBinding.inflate(inflater, parent, false)
                return MeeraClosedProfileFloorViewHolder(binding)
            }

            UserProfileAdapterType.BLOCKED_PROFILE_FLOOR.value -> {
                val binding = MeeraBlockedProfileFloorItemBinding.inflate(inflater, parent, false)
                return MeeraBlockedProfileFloorViewHolder(binding, profileUIActionHandler)
            }

            UserProfileAdapterType.BLOCKED_ME_PROFILE_FLOOR.value -> {
                val binding = MeeraClosedProfileFloorItemBinding.inflate(inflater, parent, false)
                return MeeraBlockedMeProfileFloorViewHolder(binding)
            }

            UserProfileAdapterType.ROAD_FLOOR.value -> {
                val binding = MeeraItemRoadFloorBinding.inflate(inflater, parent, false)
                return MeeraRoadFloorViewHolder(binding, profileUIActionHandler)
            }

            else -> throw RuntimeException("Missing data adapter type")
        }
    }

    override fun onBindViewHolder(holder: BaseVH<UserInfoRecyclerData, *>, position: Int, payloads: MutableList<Any>) {
        if (holder is MeeraProfileSuggestionsFloorViewHolder && payloads.isNotEmpty()) {
            holder.updateList((payloads[0] as UiUserInfoUpdate.UpdateProfileSuggestions).newSuggestions)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }
}

class UserInfoDiffUtil : BaseDiffUtil<String, UserInfoRecyclerData>() {
    override fun getChangePayload(oldItem: UserInfoRecyclerData, newItem: UserInfoRecyclerData): Any? {
        if (oldItem !is UserInfoRecyclerData.ProfileSuggestionFloor) {
            return super.getChangePayload(oldItem, newItem)
        }
        if (newItem !is UserInfoRecyclerData.ProfileSuggestionFloor) {
            return super.getChangePayload(oldItem, newItem)
        }
        return UiUserInfoUpdate.UpdateProfileSuggestions(newItem.suggestions)
    }
}

sealed interface UiUserInfoUpdate {
    class UpdateProfileSuggestions(val newSuggestions: List<ProfileSuggestionUiModels>)
}

sealed interface UserInfoRecyclerData : RecyclerData<String, UserInfoRecyclerData> {


    data class UserInfoGarageFloorRecyclerData(
        val listVehicles: List<VehicleUIModel>,
        val accountTypeEnum: AccountTypeEnum,
        val vehicleCount: Int = 0,
        val isMe: Boolean = true,
        val userColor: Int? = 0,
    ) : UserInfoRecyclerData {
        override fun getItemId() = UserProfileAdapterType.GARAGE_FLOOR.value.toString()
        override fun contentTheSame(newItem: UserInfoRecyclerData) = this == newItem
        override fun itemViewType() = UserProfileAdapterType.GARAGE_FLOOR.value
    }

    data class SubscribersFloorUiEntity(
        val subscribersCount: Long = 0,
        val subscriptionCount: Long = 0,
        val friendsCount: Long = 0,
        val mutualFriendsAndSubscribersCount: Int = 0,
        val friendsRequestCount: Long = 0,
        val showFriendsSubscribers: Boolean = false,
        val isMe: Boolean = true,
        val userStatus: AccountTypeEnum,
        val city: String,
        val country: String
    ) : UserInfoRecyclerData {
        override fun getItemId() = UserProfileAdapterType.SUBSCRIBERS_FLOOR.value.toString()
        override fun contentTheSame(newItem: UserInfoRecyclerData) = this == newItem
        override fun itemViewType() = UserProfileAdapterType.SUBSCRIBERS_FLOOR.value
    }

    data class UserEntityGalleryFloor(
        val listPhotoEntity: List<GalleryPhotoEntity>,
        val accountTypeEnum: AccountTypeEnum,
        val photoCount: Int = 0,
        val isMineGallery: Boolean = true,
        var isLoading: Boolean = false,
        override var isSeparable: Boolean = true
    ) : UserInfoRecyclerData, Separable {

        override fun getItemId() = UserProfileAdapterType.GALLERY_FLOOR.value.toString()
        override fun contentTheSame(newItem: UserInfoRecyclerData) = this == newItem
        override fun itemViewType() = UserProfileAdapterType.GALLERY_FLOOR.value
    }

    data class ProfileSuggestionFloor(
        val userType: AccountTypeEnum,
        val suggestions: List<ProfileSuggestionUiModels>
    ) : UserInfoRecyclerData {
        override fun getItemId() = UserProfileAdapterType.PROFILE_SUGGESTIONS.value.toString()
        override fun contentTheSame(newItem: UserInfoRecyclerData) = this == newItem
        override fun itemViewType() = UserProfileAdapterType.PROFILE_SUGGESTIONS.value
    }

    data class UserEntityFriendSubscribeFloor(
        val isUserBlacklisted: Boolean,
        val userId: Long,
        val friendStatus: Int,
        val isSubscribed: Boolean,
        val userStatus: AccountTypeEnum,
        val approved: Boolean,
        val topContentMaker: Boolean,
        val name: String,
        var isSuggestionShowed: Boolean
    ) : UserInfoRecyclerData {
        override fun getItemId() = UserProfileAdapterType.FRIEND_SUBSCRIBE_FLOOR.value.toString()
        override fun contentTheSame(newItem: UserInfoRecyclerData) = this == newItem
        override fun itemViewType() = UserProfileAdapterType.FRIEND_SUBSCRIBE_FLOOR.value
    }

    data class MutualSubscribersUiEntity(
        var mutualSubscribersFriends: List<MutualUser>,
        var moreCount: Int,
        var userType: AccountTypeEnum
    ) : UserInfoRecyclerData {

        override fun getItemId() = UserProfileAdapterType.MUTUAL_SUBSCRIBERS_FLOOR.value.toString()
        override fun contentTheSame(newItem: UserInfoRecyclerData) = this == newItem
        override fun itemViewType() = UserProfileAdapterType.MUTUAL_SUBSCRIBERS_FLOOR.value
    }

    data object UserEntityUpdateBtn : UserInfoRecyclerData {
        override fun getItemId() = UserProfileAdapterType.UPDATE_BTN.value.toString()
        override fun contentTheSame(newItem: UserInfoRecyclerData) = this == newItem
        override fun itemViewType() = UserProfileAdapterType.UPDATE_BTN.value
    }

    data object UserEntityBirthdayFloor : UserInfoRecyclerData {
        override fun getItemId() = UserProfileAdapterType.BIRTHDAY_FLOOR.value.toString()
        override fun contentTheSame(newItem: UserInfoRecyclerData) = this == newItem
        override fun itemViewType() = UserProfileAdapterType.BIRTHDAY_FLOOR.value
    }

    data object UserEntityClosedProfileFloor : UserInfoRecyclerData {
        override fun getItemId() = UserProfileAdapterType.CLOSED_PROFILE_FLOOR.value.toString()
        override fun contentTheSame(newItem: UserInfoRecyclerData) = this == newItem
        override fun itemViewType() = UserProfileAdapterType.CLOSED_PROFILE_FLOOR.value
    }

    data object UserEntityBlockedProfileFloor : UserInfoRecyclerData {
        override fun getItemId() = UserProfileAdapterType.BLOCKED_PROFILE_FLOOR.value.toString()
        override fun contentTheSame(newItem: UserInfoRecyclerData) = this == newItem
        override fun itemViewType() = UserProfileAdapterType.BLOCKED_PROFILE_FLOOR.value
    }

    data object UserEntityBlockedMeProfileFloor : UserInfoRecyclerData {
        override fun getItemId() = UserProfileAdapterType.BLOCKED_ME_PROFILE_FLOOR.value.toString()
        override fun contentTheSame(newItem: UserInfoRecyclerData) = this == newItem
        override fun itemViewType() = UserProfileAdapterType.BLOCKED_ME_PROFILE_FLOOR.value
    }

    data class UserEntityRoadFloor(
        val postCount: Int,
        val userTypeEnum: AccountTypeEnum,
        val isMe: Boolean = true
    ) : UserInfoRecyclerData {

        override fun getItemId() = UserProfileAdapterType.ROAD_FLOOR.value.toString()
        override fun contentTheSame(newItem: UserInfoRecyclerData) = this == newItem
        override fun itemViewType() = UserProfileAdapterType.ROAD_FLOOR.value

    }
}
