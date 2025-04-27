package com.numplates.nomera3.modules.moments.show.data.exception

abstract class MomentException(val code: Int) : Exception()

class MomentNotFoundException(override val message: String?) : MomentException(MOMENT_NOT_FOUND_CODE)

class MomentActionNotAllowedException(override val message: String?) : MomentException(MOMENT_ACTION_NOT_ALLOWED_CODE)

class MomentAlreadyRemovedException(override val message: String?) : MomentException(MOMENT_ALREADY_REMOVED_CODE)

class MomentInvalidParamsException(override val message: String?) : MomentException(MOMENT_INVALID_PARAMS_CODE)

class MomentServerErrorException(override val message: String?) : MomentException(MOMENT_SERVER_ERROR_CODE)

class MomentUnknownException(override val message: String?) : MomentException(-1)
