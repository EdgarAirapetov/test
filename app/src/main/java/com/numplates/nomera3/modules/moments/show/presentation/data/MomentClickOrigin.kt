package com.numplates.nomera3.modules.moments.show.presentation.data

import android.os.Parcelable
import com.numplates.nomera3.modules.newroads.fragments.BaseRoadsFragment
import com.numplates.nomera3.modules.newroads.fragments.MeeraBaseRoadsFragment
import kotlinx.parcelize.Parcelize

sealed class MomentClickOrigin: Parcelable {

    @Parcelize
    object Subscriptions: MomentClickOrigin()

    @Parcelize
    object Main: MomentClickOrigin()

    @Parcelize
    object Map: MomentClickOrigin()

    @Parcelize
    object Profile: MomentClickOrigin()

    @Parcelize
    object User: MomentClickOrigin()

    companion object {
        fun fromRoadType(roadType: BaseRoadsFragment.RoadTypeEnum): MomentClickOrigin {
            return when (roadType) {
                BaseRoadsFragment.RoadTypeEnum.MAIN_ROAD -> Main
                BaseRoadsFragment.RoadTypeEnum.SUBSCRIPTIONS_ROAD -> Subscriptions
                else -> error("Unsupported road type")
            }
        }

        fun fromRoadType(roadType: MeeraBaseRoadsFragment.RoadTypeEnum): MomentClickOrigin {
            return when (roadType) {
                MeeraBaseRoadsFragment.RoadTypeEnum.MAIN_ROAD -> Main
                MeeraBaseRoadsFragment.RoadTypeEnum.SUBSCRIPTIONS_ROAD -> Subscriptions
                else -> error("Unsupported road type")
            }
        }

        fun fromUserAvatar(): MomentClickOrigin {
            return User
        }

        fun fromUserProfile(): MomentClickOrigin {
            return Profile
        }
    }
}
