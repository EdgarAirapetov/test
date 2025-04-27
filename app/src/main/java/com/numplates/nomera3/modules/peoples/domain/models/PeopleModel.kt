package com.numplates.nomera3.modules.peoples.domain.models

data class PeopleModel(
    var approvedUsers: List<PeopleApprovedUserModel>,
    val relatedUsers: List<PeopleRelatedUserModel>
)
