package com.numplates.nomera3.modules.services.ui.content

import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesButtonsUiModel
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesCommunitiesUiModel
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesContentType
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesRecentUsersUiModel
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesRecommendedPeopleUiModel
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesUiModel
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesUserUiModel

fun MeeraServicesUiModel.equalTo(newItem: MeeraServicesUiModel): Boolean {
    return when (newItem.getServicesContentType()) {
        MeeraServicesContentType.USER -> {
            val currentEntity = this as? MeeraServicesUserUiModel
            val newEntity = newItem as? MeeraServicesUserUiModel
            currentEntity == newEntity
        }
        MeeraServicesContentType.BUTTONS -> {
            val currentEntity = this as? MeeraServicesButtonsUiModel
            val newEntity = newItem as? MeeraServicesButtonsUiModel
            currentEntity == newEntity
        }
        MeeraServicesContentType.RECENT_USERS -> {
            val currentEntity = this as? MeeraServicesRecentUsersUiModel
            val newEntity = newItem as? MeeraServicesRecentUsersUiModel
            currentEntity == newEntity
        }
        MeeraServicesContentType.RECOMMENDED_PEOPLE -> {
            val currentEntity = this as? MeeraServicesRecommendedPeopleUiModel
            val newEntity = newItem as? MeeraServicesRecommendedPeopleUiModel
            currentEntity == newEntity
        }
        MeeraServicesContentType.COMMUNITIES -> {
            val currentEntity = this as? MeeraServicesCommunitiesUiModel
            val newEntity = newItem as? MeeraServicesCommunitiesUiModel
            currentEntity == newEntity
        }
        MeeraServicesContentType.COMMUNITIES_PLACEHOLDER -> true
    }
}
