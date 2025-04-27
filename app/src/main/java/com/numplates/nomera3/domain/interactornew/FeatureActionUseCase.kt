package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.data.network.FeatureActionResponse
import com.numplates.nomera3.data.network.core.ResponseWrapper

class FeatureActionUseCase(val apiMain: ApiMain) {

    suspend fun actionOnFeature(featureId: Long, actionDismiss: Boolean): ResponseWrapper<FeatureActionResponse> {
        return apiMain.actionOnFeature(
            hashMapOf(
                PARAM_FEATURE_ID to featureId,
                PARAM_ACTION to if (actionDismiss) ACTION_HIDE else ACTION_SHOW))
    }


    companion object {
        const val ACTION_SHOW = "show"
        const val ACTION_HIDE = "hide"
        const val PARAM_FEATURE_ID = "feature_id"
        const val PARAM_ACTION = "action"
    }

}

