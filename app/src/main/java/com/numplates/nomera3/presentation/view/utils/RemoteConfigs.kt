package com.numplates.nomera3.presentation.view.utils

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.numplates.nomera3.domain.interactornew.IsNeedForceGetFbFlagsUseCase
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

//Firebase
private const val MINIMUM_FETCH_INTERVAL_IN_SECONDS: Long = 3600
private const val FORCE_UPDATE_FETCH_INTERVAL = 0L

class RemoteConfigs @Inject constructor(
    val isNeedForceGetFbFlagsUseCase: IsNeedForceGetFbFlagsUseCase,
    val togglesContainer: FeatureTogglesContainer,
    val context: Context
) {

    private var firebaseRemoteConfig: FirebaseRemoteConfig? = null

    //TODO https://nomera.atlassian.net/browse/BR-22101
    val postVideoSaving: Boolean
        get() = firebaseRemoteConfig?.getBoolean("postVideoSaving") ?: false

    private val isChatMessageEditingEnabled: Boolean
        get() = firebaseRemoteConfig?.getBoolean("isChatMessageEditingEnabled") ?: false

    private val isChatGroupComplaintsEnabled: Boolean
        get() = firebaseRemoteConfig?.getBoolean("isGroupChatComplaintEnabled") ?: false

    private val isChatStickerSuggestionsEnabled: Boolean
        get() = firebaseRemoteConfig?.getBoolean("isChatStickersSuggestsEnabled") ?: false

    private val isChatLastMessageRecognizedText: Boolean
        get() = firebaseRemoteConfig?.getBoolean("chatAudioRecognitionInListEnabled") ?: false

    private val isMapEventsEnabled: Boolean
        get() = firebaseRemoteConfig?.getBoolean("isMapEventsEnabled") ?: false

    private val isShakeEnabled: Boolean
        get() = firebaseRemoteConfig?.getBoolean("isShakeEnabled") ?: false

    private val isChatListSearchEnabled: Boolean
        get() = firebaseRemoteConfig?.getBoolean("isChatListSearchEnabled") ?: false


    private val isDetaledReactionsForPostsEnabled: Boolean
        get() = firebaseRemoteConfig?.getBoolean("isDetaledReactionsForPostsEnabled") ?: false

    private val isDetaledReactionsForCommentsEnabled: Boolean
        get() = firebaseRemoteConfig?.getBoolean("isDetaledReactionsForCommentsEnabled") ?: false

    private val isAvatarCarouselEnabled: Boolean
        get() = firebaseRemoteConfig?.getBoolean("isAvatarCarouselEnabled") ?: true

    private val isPostsWithBackgroundEnabled: Boolean
        get() = firebaseRemoteConfig?.getBoolean("postsWithBackground") ?: false

    private val isPostMediaPositioningEnabled: Boolean
        get() = firebaseRemoteConfig?.getBoolean("postMediaPositioning") ?: false

    private val showTimeOfDayReactions: Boolean
        get() = firebaseRemoteConfig?.getBoolean("showTimeOfDayReactions") ?: false

    private val is18plusProfileEnabled: Boolean
        get() = firebaseRemoteConfig?.getBoolean("is18plusProfileEnabled") ?: false

    private val isMomentsEnabled: Boolean
        get() = firebaseRemoteConfig?.getBoolean("isMomentsEnabled") ?: false

    private val isMediaExpandEnabled: Boolean
        get() = firebaseRemoteConfig?.getBoolean("isMediaExpandEnabled") ?: false

    private val editPostFeatureToggleEnabled: Boolean
        get() = firebaseRemoteConfig?.getBoolean("isPostEditingEnabled") ?: false

    private val momentViewsFeatureToggleEnabled: Boolean
        get() = firebaseRemoteConfig?.getBoolean("isMomentViewsEnabled") ?: false

    init {
        FirebaseApp.initializeApp(context)
        firebaseRemoteConfig = Firebase.remoteConfig
    }

    fun initializeWithCachedValues() {
        with(togglesContainer) {
            chatMessageEditFeatureToggle.remoteValue = isChatMessageEditingEnabled
            chatGroupComplaintsFeatureToggle.remoteValue = isChatGroupComplaintsEnabled
            chatLastMessageRecognizedText.remoteValue = isChatLastMessageRecognizedText
            shakeFeatureToggle.remoteValue = isShakeEnabled
            chatStickerSuggestionsFeatureToggle.remoteValue = isChatStickerSuggestionsEnabled
            detailedReactionsForPostFeatureToggle.remoteValue = isDetaledReactionsForPostsEnabled
            detailedReactionsForCommentsFeatureToggle.remoteValue = isDetaledReactionsForCommentsEnabled
            avatarCarouselFeatureToggle.remoteValue = isAvatarCarouselEnabled
            mapEventsFeatureToggle.remoteValue = isMapEventsEnabled
            chatSearchFeatureToggle.remoteValue = isChatListSearchEnabled
            postsWithBackgroundFeatureToggle.remoteValue = isPostsWithBackgroundEnabled
            postMediaPositioningFeatureToggle.remoteValue = isPostMediaPositioningEnabled
            timeOfDayReactionsFeatureToggle.remoteValue = showTimeOfDayReactions
            is18plusProfileFeatureToggle.remoteValue = is18plusProfileEnabled
            momentsFeatureToggle.remoteValue = isMomentsEnabled
            feedMediaExpandFeatureToggle.remoteValue = isMediaExpandEnabled
            editPostFeatureToggle.remoteValue = editPostFeatureToggleEnabled
            momentViewsFeatureToggle.remoteValue = momentViewsFeatureToggleEnabled
        }
    }

    suspend fun getFlags() {
        val isNeedForceUpdt = isNeedForceGetFbFlagsUseCase.invoke()
        firebaseRemoteConfig?.setConfigSettingsAsync(getConfigSettings(isNeedForceUpdt))
        repeat (getTimes(isNeedForceUpdt)) {
            suspendCoroutine { continuation ->
                firebaseRemoteConfig?.fetchAndActivate()?.addOnCompleteListener {
                    initializeWithCachedValues()
                    continuation.resume(Unit)
                }
            }
        }

    }

    private fun getConfigSettings(isNeedForceUpdt: Boolean) = remoteConfigSettings {
        minimumFetchIntervalInSeconds = if (isNeedForceUpdt) {
            FORCE_UPDATE_FETCH_INTERVAL
        } else {
            MINIMUM_FETCH_INTERVAL_IN_SECONDS
        }
    }

    /**
     * Когда устанавливаем интервал в 0, первый запрес пришлет старые данные и только следующий - актуальные.
     * То есть необходимо перезайти вприложение и запросить флаги повторно.
     * Чтобы это обойти делаем два запроса подряд.
     * */
    private fun getTimes(isNeedForceUpdt: Boolean) = if (isNeedForceUpdt) {
        2
    } else {
        1
    }
}
