package com.numplates.nomera3.modules.notifications.ui.basefragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.domain.interactornew.GetRoomDataUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.CacheCompanionUserForChatInitUseCase
import com.numplates.nomera3.modules.notifications.ui.entity.User
import com.numplates.nomera3.modules.notifications.ui.mapper.CachedUserForChatMapper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class BaseNotificationViewModel @Inject constructor(
    private val getRoomDataUseCase: GetRoomDataUseCase,
    private val cacheCompanionUserUseCase: CacheCompanionUserForChatInitUseCase,
    private val cachedCompanionMapper: CachedUserForChatMapper
): ViewModel() {

    private val _notificationClickEffect = MutableSharedFlow<BaseNotificationFragmentEffect>()
    val notificationClickEffect: SharedFlow<BaseNotificationFragmentEffect> = _notificationClickEffect


    fun onGroupChatNotificationClicked(roomId: Long?) {
        viewModelScope.launch {
            roomId?.let {
                getRoomDataUseCase.invoke(roomId)
            }?.let {
                _notificationClickEffect.emit(BaseNotificationFragmentEffect.OpenGroupChatFragment(roomId))
            }
        }
    }

    fun cacheUserProfileForChat(user: User?) {
        user?.let {
            val cachedUser = cachedCompanionMapper.mapToCachedUser(user)
            cacheCompanionUserUseCase.invoke(cachedUser)
        }
    }
}
