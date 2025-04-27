package com.numplates.nomera3.modules.chat.domain.usecases

import com.numplates.nomera3.modules.user.data.repository.UserRepository
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserProfileModel
import io.reactivex.Single
import javax.inject.Inject

class GetUserProfileRxUseCase @Inject constructor(
    private val userRepository: UserRepository
){
    fun invoke(): Single<UserProfileModel> = userRepository.getUserProfileRx()
}
