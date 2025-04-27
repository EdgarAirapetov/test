package com.numplates.nomera3.presentation.utils.networkconn

data class NetworkStatus(

        val isConnected: Boolean,

        @Deprecated("Not used at this time")
        val networkType: Int = -1
)
