package com.numplates.nomera3.modules.communities.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseNoSuspend
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.communities.data.repository.CommunityRepository
import com.numplates.nomera3.modules.communities.data.states.CommunityState
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class SubscribeCommunitySubjectUseCase @Inject constructor(
    private val repo: CommunityRepository
) : BaseUseCaseNoSuspend<SubscribeCommunitySubjectUseCaseParams, PublishSubject<CommunityState>> {

    override fun execute(params: SubscribeCommunitySubjectUseCaseParams): PublishSubject<CommunityState> {
        return repo.getOnSubscribeCommunityPublishSubject()
    }
}

class SubscribeCommunitySubjectUseCaseParams: DefParams()