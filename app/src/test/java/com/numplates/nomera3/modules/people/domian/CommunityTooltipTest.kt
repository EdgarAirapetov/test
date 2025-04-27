package com.numplates.nomera3.modules.people.domian

import com.numplates.nomera3.modules.peoples.domain.repository.PeopleRepository
import com.numplates.nomera3.modules.peoples.domain.usecase.GetSelectCommunityTooltipShownUseCase
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock

internal class CommunityTooltipTest {

    private val repository: PeopleRepository = mock()

    @Test
    fun `Assert show tooltip communities here count of times`() {
        val times = getSelectCommunityTooltipShownTimes()
        Mockito.`when`(
            repository.getSelectCommunityTooltipShown()
        ).thenReturn(times)
        val useCase = GetSelectCommunityTooltipShownUseCase(repository)
        val actual = useCase.invoke()
        val expected = getSelectCommunityTooltipShownTimes()
        Assert.assertEquals(expected, actual)
    }

    private fun getSelectCommunityTooltipShownTimes() = 3
}
