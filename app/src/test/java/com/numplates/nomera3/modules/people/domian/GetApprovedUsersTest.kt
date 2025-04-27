package com.numplates.nomera3.modules.people.domian

import com.meera.core.extensions.empty
import com.numplates.nomera3.modules.peoples.domain.models.PeopleApprovedUserModel
import com.numplates.nomera3.modules.peoples.domain.repository.PeopleRepository
import com.numplates.nomera3.modules.peoples.domain.usecase.GetApprovedUsersUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock

internal class GetApprovedUsersTest {

    private val peopleRepository: PeopleRepository = mock()
    private val useCase = GetApprovedUsersUseCase(peopleRepository)

    @Test
    @ExperimentalCoroutinesApi
    fun getApprovedUsersTest() = runTest {
        val testApprovedUsers = getTestApprovedUsersList()
        Mockito.`when`(
            peopleRepository.getApprovedUsers(
                limit = 20,
                offset = 0
            )
        ).thenReturn(testApprovedUsers)
        val actual = useCase.invoke(limit = 20, offset = 0)
        val expected = getTestApprovedUsersList()
        Assert.assertEquals(expected, actual)
    }

    private fun getTestApprovedUsersList(): List<PeopleApprovedUserModel> {
        return listOf(
            PeopleApprovedUserModel(
                userId = 0,
                subscribersCount = 0,
                userName = String.empty(),
                accountType = 0,
                approved = 0,
                accountColor = 0,
                topContentMaker = 0,
                avatarSmall = String.empty(),
                uniqueName = String.empty(),
                settingsFlags = null,
                isUserSubscribed = false,
                posts = listOf()
            )
        )
    }
}
