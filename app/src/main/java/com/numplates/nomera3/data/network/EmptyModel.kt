package com.numplates.nomera3.data.network

import java.io.Serializable

/**
 * created by c7j on 24.05.18
 * Various responses has empty body, use this one in that cases
 * Or if you just don't care what server sends to you...
 */
class EmptyModel : Serializable
