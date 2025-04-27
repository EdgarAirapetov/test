package com.numplates.nomera3.modules.peoples.data

class BloggersNotFoundException(
    message: String
) : Exception(message)

class RelatedUsersNotFoundException(
    errorMessage: String
) : Exception(errorMessage)
