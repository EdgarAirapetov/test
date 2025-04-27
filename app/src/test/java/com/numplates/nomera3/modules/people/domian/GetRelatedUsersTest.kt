package com.numplates.nomera3.modules.people.domian

import com.meera.core.extensions.empty
import com.numplates.nomera3.modules.peoples.domain.models.PeopleRelatedUserModel
import com.numplates.nomera3.modules.peoples.domain.repository.PeopleRepository
import com.numplates.nomera3.modules.peoples.domain.usecase.GetRelatedUsersUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock

internal class GetRelatedUsersTest {

    private val repository: PeopleRepository = mock()

    @Test
    @ExperimentalCoroutinesApi
    fun getRelatedUsersTest() = runTest {
        val fakeRelatedUsers = getFakeRelatedUsers()
        `when`(
            repository.getRelatedUsers(
                limit = 20,
                offset = 0
            )
        ).thenReturn(fakeRelatedUsers)
        val useCase = GetRelatedUsersUseCase(repository)
        val actual = useCase.invoke(limit = 20, offset = 0)
        val expected = getFakeRelatedUsers()
        Assert.assertEquals(expected, actual)
    }

    private fun getFakeRelatedUsers(): List<PeopleRelatedUserModel> {
        return listOf(
            PeopleRelatedUserModel(
                userId = 0,
                name = String.empty(),
                accountColor = 0,
                accountType = 0,
                approved = 0,
                avatar = String.empty(),
                birthday = 0,
                cityId = 0,
                cityName = String.empty(),
                countryId = 0,
                countryName = String.empty(),
                gender = 0,
                settingsFlags = null,
                mutualFriends = null,
                mutualTotalCount = 0,
                hasFriendRequest = false,
                topContentMaker = false
            )
        )
    }
}
