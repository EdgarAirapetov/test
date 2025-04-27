package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.modules.userprofile.data.repository.GetProfileRepository
import io.reactivex.Single
import org.phoenixframework.Message
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(val repository: GetProfileRepository) {

    fun getProfile(): Single<Message> {
        return repository.getProfile()
    }

}
