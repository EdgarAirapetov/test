package com.numplates.nomera3.modules.search.domain.mapper.result

import com.numplates.nomera3.FRIEND_STATUS_CONFIRMED
import com.numplates.nomera3.FRIEND_STATUS_INCOMING
import com.numplates.nomera3.FRIEND_STATUS_NONE
import com.numplates.nomera3.FRIEND_STATUS_OUTGOING
import com.numplates.nomera3.domain.util.Mapper
import com.numplates.nomera3.modules.peoples.ui.content.entity.UserSearchResultUiEntity
import com.numplates.nomera3.modules.search.ui.entity.SearchItem

class FriendStatusButtonMapper : Mapper<Int, SearchItem.User.ButtonState> {

    fun mapForPeoples(entity: Int): UserSearchResultUiEntity.ButtonState {
        return when (entity) {
            FRIEND_STATUS_NONE -> {
                UserSearchResultUiEntity.ButtonState.ShowAdd
            }
            FRIEND_STATUS_INCOMING -> {
                UserSearchResultUiEntity.ButtonState.ShowIncome
            }
            FRIEND_STATUS_OUTGOING -> {
                UserSearchResultUiEntity.ButtonState.Hide
            }
            FRIEND_STATUS_CONFIRMED -> {
                UserSearchResultUiEntity.ButtonState.Hide
            }
            else -> {
                UserSearchResultUiEntity.ButtonState.Hide
            }
        }
    }

    override fun map(entity: Int): SearchItem.User.ButtonState {
        return when (entity) {
            FRIEND_STATUS_NONE -> {
                SearchItem.User.ButtonState.ShowAdd
            }
            FRIEND_STATUS_INCOMING -> {
                SearchItem.User.ButtonState.ShowIncome
            }
            FRIEND_STATUS_OUTGOING -> {
                SearchItem.User.ButtonState.Hide
            }
            FRIEND_STATUS_CONFIRMED -> {
                SearchItem.User.ButtonState.Hide
            }
            else -> {
                SearchItem.User.ButtonState.Hide
            }
        }
    }
}
