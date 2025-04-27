package com.numplates.nomera3.modules.people.domian

import com.meera.core.extensions.empty
import com.numplates.nomera3.modules.peoples.domain.models.PeopleApprovedUserModel
import com.numplates.nomera3.modules.peoples.domain.models.PeopleModel
import com.numplates.nomera3.modules.peoples.domain.models.PeopleRelatedUserModel
import com.numplates.nomera3.modules.peoples.domain.repository.PeopleRepository
import com.numplates.nomera3.modules.peoples.domain.usecase.GetPeopleAllSavedContentUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock

internal class GetPeopleContentTest {

    private val repository: PeopleRepository = mock()

    @Test
    @ExperimentalCoroutinesApi
    fun getPeopleContentTest() = runTest {
        val fakeRelatedUsers = createPeopleContent()
        Mockito.`when`(
            repository.getAllContentDatabase()
        ).thenReturn(fakeRelatedUsers)
        val useCase = GetPeopleAllSavedContentUseCase(repository)
        val actual = useCase.invoke()
        val expected = createPeopleContent()
        Assert.assertEquals(expected, actual)
    }

    private fun createPeopleContent(): PeopleModel {
        return PeopleModel(
            approvedUsers = getTestApprovedUsersList(),
            relatedUsers = getFakeRelatedUsers()
        )
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
