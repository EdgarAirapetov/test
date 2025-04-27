package com.meera.core.devtool

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import androidx.annotation.StyleRes
import com.meera.core.extensions.getAppVersionCode
import okhttp3.OkHttpClient
import ru.grigoryev.devtools.DevTools

fun OkHttpClient.Builder.applyDevToolInterceptor() {
    DevTools.applyInterceptors(this)
}

fun Application.initDevTools(
    uid: Long,
    serverName: String,
    @StyleRes styleRes: Int,
    envChanged: (result: String) -> Unit,
    localeChanged: (result: String) -> Unit,
    stickerSuggestionsEnabledToggled: (enabled: Boolean) -> Unit,
    detailedReactionsForPostEnabledToggled: (enabled: Boolean) -> Unit,
    detailedReactionsForCommentEnabledToggled: (enabled: Boolean) -> Unit,
    hiddenAgeAndSexEnabledToggled: (enabled: Boolean) -> Unit,
    avatarCarouselEnabledToggled: (enabled: Boolean) -> Unit,
    leakCanaryChanged: (enabled: Boolean) -> Unit,
    isLeakCanaryEnabled: Boolean,
    mapEventsToggleChanged: (enabled: Boolean?) -> Unit,
    videoEditorResizeChanged: (enabled: Boolean) -> Unit,
    chatSearchToggleChanged: (enabled: Boolean?) -> Unit,
    isChuckerEnabled: (enabled: Boolean) -> Unit,
    roadMaxVideoDurationEnabled: (enabled: Boolean) -> Unit,
    isTimeOfDayReactionsEnabled: (enabled: Boolean) -> Unit,
    momentsToggled: (enabled: Boolean) -> Unit,
    editPostFeatureToggleEnabled: (enabled: Boolean) -> Unit,
    onClickRedesignSection: () -> Unit,
    momentViewsFeatureToggleEnabled: (enabled: Boolean) -> Unit,
    mapFriendsToggleChanged: (enabled: Boolean?) -> Unit,
) {
    val versionCode = this.getAppVersionCode()
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    DevTools.init(
        app = this,
        buildVersion = versionCode,
        uid = uid,
        serverName = serverName,
        heapSize = activityManager.memoryClass,
        largeHeapSize = activityManager.largeMemoryClass,
        isLowMemDevice = activityManager.isLowRamDevice,
        theme = styleRes,
        envChanged = envChanged,
        localeChanged = localeChanged,
        stickerSuggestionsEnabledToggled = stickerSuggestionsEnabledToggled,
        detailedReactionsForPostEnabledToggled = detailedReactionsForPostEnabledToggled,
        detailedReactionsForCommentEnabledToggled = detailedReactionsForCommentEnabledToggled,
        hiddenAgeAndSexToggled = hiddenAgeAndSexEnabledToggled,
        avatarCarouselEnabledToggled = avatarCarouselEnabledToggled,
        isLeakCanaryEnabled = isLeakCanaryEnabled,
        leakCanaryChanged = leakCanaryChanged,
        mapEventsToggleChanged = mapEventsToggleChanged,
        videoEditorResizeChanged = videoEditorResizeChanged,
        chatSearchToggleChanged = chatSearchToggleChanged,
        isChuckerEnabled = isChuckerEnabled,
        roadMaxVideoDurationEnabled = roadMaxVideoDurationEnabled,
        isTimeOfDayReactionsEnabled = isTimeOfDayReactionsEnabled,
        momentsToggled = momentsToggled,
        editPostFeatureToggleEnabled = editPostFeatureToggleEnabled,
        onClickRedesignSection = onClickRedesignSection,
        momentViewsFeatureToggleEnabled = momentViewsFeatureToggleEnabled,
        mapFriendsToggleChanged = mapFriendsToggleChanged
    )
}
