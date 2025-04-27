package com.numplates.nomera3.modules.services.ui.entity

import com.meera.uikit.widgets.userpic.UserpicStoriesStateEnum
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecentUserUiModel
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleUiEntity

sealed class MeeraServicesUiModel {
    abstract fun getServicesContentType() : MeeraServicesContentType
}

data class MeeraServicesUserUiModel(
    val id: Long,
    val userName: String,
    val uniqueName: String,
    val avatarUrl: String,
    var storiesStateEnum: UserpicStoriesStateEnum,
    val approved: Boolean,
    val interestingAuthor: Boolean
) : MeeraServicesUiModel() {
    override fun getServicesContentType() = MeeraServicesContentType.USER
}

data object MeeraServicesButtonsUiModel : MeeraServicesUiModel() {
    override fun getServicesContentType() = MeeraServicesContentType.BUTTONS
}

data class MeeraServicesRecentUsersUiModel(
    val users: List<RecentUserUiModel>
) : MeeraServicesUiModel() {
    override fun getServicesContentType() = MeeraServicesContentType.RECENT_USERS
}

data class MeeraServicesRecommendedPeopleUiModel(
    val users: List<RecommendedPeopleUiEntity>
) : MeeraServicesUiModel() {
    override fun getServicesContentType() = MeeraServicesContentType.RECOMMENDED_PEOPLE
}

data class MeeraServicesCommunitiesUiModel(
    val totalCount: Int,
    val communities: List<ServicesCommunityUiModel>
) : MeeraServicesUiModel() {
    override fun getServicesContentType() = MeeraServicesContentType.COMMUNITIES
}

data object MeeraServicesCommunitiesPlaceholderUiModel : MeeraServicesUiModel() {
    override fun getServicesContentType(): MeeraServicesContentType = MeeraServicesContentType.COMMUNITIES_PLACEHOLDER
}
