package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.modules.feed.data.repository.PostRepository
import javax.inject.Inject

class ReadOnboardingUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    fun invoke(): Boolean = postRepository.readOnboarding()
}
