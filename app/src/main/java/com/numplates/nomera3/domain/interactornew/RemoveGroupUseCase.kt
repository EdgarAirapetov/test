package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.Api

class RemoveGroupUseCase(private val repository: Api) {

    fun removeGroup(groupId: Int) = repository.removeGroup(groupId)

}