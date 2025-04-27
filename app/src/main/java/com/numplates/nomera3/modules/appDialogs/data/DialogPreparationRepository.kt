package com.numplates.nomera3.modules.appDialogs.data

interface DialogPreparationRepository {

   fun isOnBoardingReady(): Boolean

   fun isOutCallsReady(): Boolean
}