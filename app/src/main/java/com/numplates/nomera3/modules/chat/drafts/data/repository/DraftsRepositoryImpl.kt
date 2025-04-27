package com.numplates.nomera3.modules.chat.drafts.data.repository

import com.meera.db.dao.DraftsDao
import com.numplates.nomera3.modules.chat.drafts.data.DraftsDataMapper
import com.numplates.nomera3.modules.chat.drafts.domain.DraftsRepository
import com.numplates.nomera3.modules.chat.drafts.domain.entity.DraftModel
import javax.inject.Inject

class DraftsRepositoryImpl @Inject constructor(
    private val draftsDao: DraftsDao,
    private val draftsMapper: DraftsDataMapper
) : DraftsRepository {

    override suspend fun getAllDrafts(): List<DraftModel> =
        draftsDao.getAllDrafts().map(draftsMapper::mapDbToDomainModel)

    override suspend fun addDraft(draft: DraftModel) = draftsDao.insertDraft(draftsMapper.mapDomainToDbModel(draft))

    override suspend fun deleteDraft(roomId: Long?) = draftsDao.deleteDraft(roomId)

    override suspend fun deleteAllDrafts() = draftsDao.deleteAllDrafts()
}
