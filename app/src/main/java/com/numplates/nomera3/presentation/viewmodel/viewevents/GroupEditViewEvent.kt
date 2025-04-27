package com.numplates.nomera3.presentation.viewmodel.viewevents

sealed class GroupEditViewEvent {

    data class SuccessGroupCreate(val groupId: Int) : GroupEditViewEvent()

    object SuccessGroupEdit : GroupEditViewEvent()

    object SuccessGroupDeleted : GroupEditViewEvent()

    object FailureGroupCreate : GroupEditViewEvent()

    object FailureGroupCreateExist : GroupEditViewEvent()

    object NoInternetConnection : GroupEditViewEvent()

    object FailureGroupEdit : GroupEditViewEvent()

    object FailureGroupEditExist : GroupEditViewEvent()

    object FailureGroupDeleted : GroupEditViewEvent()

    object ErrorNameSizeMoreThenTree : GroupEditViewEvent()

}