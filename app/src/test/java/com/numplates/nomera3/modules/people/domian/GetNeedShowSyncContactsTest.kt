package com.numplates.nomera3.modules.people.domian

import com.numplates.nomera3.modules.peoples.domain.repository.PeopleRepository
import com.numplates.nomera3.modules.peoples.domain.usecase.GetNeedShowSyncContactsDialogUseCase
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock

internal class GetNeedShowSyncContactsTest {

    private val repository: PeopleRepository = mock()

    @Test
    fun `should show sync contacts dialog if the user does not have permission`() {
        val needShowSyncContacts = getNeedShowSyncContactsFake()
        Mockito.`when`(
            repository.needShowPeopleSyncContactsDialog()
        ).thenReturn(needShowSyncContacts)
        val useCase = GetNeedShowSyncContactsDialogUseCase(repository)
        val actual = useCase.invoke()
        val expected = getNeedShowSyncContactsFake()
        Assert.assertEquals(expected, actual)
    }

    private fun getNeedShowSyncContactsFake(): Boolean = false
}
