package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName

abstract class SimpleUser {
        abstract var uid: Long
        abstract var name: String?
        abstract var avatar: String?
        abstract var avatarDate: Long
        abstract var driver: Int
        abstract var accountColor: Int
        abstract var accountType: Int
        @SerializedName("rating") var rating: Int = 0
        @SerializedName("gps_x") var gpsX: Double = 0.toDouble()
        @SerializedName("gps_y") var gpsY: Double = 0.toDouble()
        abstract   var vehicle: Int
        abstract var number: String?

}