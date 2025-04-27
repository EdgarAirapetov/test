package ru.grigoryev.devtools

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.annotation.StyleRes
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.crashreporter.CrashReporterPlugin
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
import com.facebook.soloader.SoLoader
import com.mera.bridge.devtools.IDevToolsBridge
import com.numplates.devtools.BuildConfig
import com.numplates.devtools.R
import com.pandulapeter.beagle.Beagle
import com.pandulapeter.beagle.common.configuration.Appearance
import com.pandulapeter.beagle.common.configuration.Behavior
import com.pandulapeter.beagle.common.configuration.toText
import com.pandulapeter.beagle.common.contracts.BeagleListItemContract
import com.pandulapeter.beagle.modules.AppInfoButtonModule
import com.pandulapeter.beagle.modules.BugReportButtonModule
import com.pandulapeter.beagle.modules.DeveloperOptionsButtonModule
import com.pandulapeter.beagle.modules.DividerModule
import com.pandulapeter.beagle.modules.LifecycleLogListModule
import com.pandulapeter.beagle.modules.LogListModule
import com.pandulapeter.beagle.modules.NetworkLogListModule
import com.pandulapeter.beagle.modules.PaddingModule
import com.pandulapeter.beagle.modules.ScreenCaptureToolboxModule
import com.pandulapeter.beagle.modules.SingleSelectionListModule
import com.pandulapeter.beagle.modules.SwitchModule
import com.pandulapeter.beagle.modules.TextModule
import leakcanary.LeakCanary
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val ID_LOCALE_GROUP = "localeGroup"
private const val ID_ENV_GROUP = "envGroup"
private const val ID_LEAK_CANARY = "LeakCanarySwitchModule"
private const val ID_CHUCKER = "ChuckerSwitchModule"
private const val ID_CHAT_STICKER_SUGGESTIONS = "ChatStickerSuggestionsSwitchModule"
private const val ID_MOMENTS = "Moments"
private const val ID_DETAILED_REACTIONS_FOR_POST = "DetailedReactionsForPostSwitchModule"
private const val ID_DETAILED_REACTIONS_FOR_COMMENT = "DetailedReactionsForCommentSwitchModule"
private const val ID_HIDDEN_AGE_AND_SEX = "HiddenAgeAndSexSwitchModule"
private const val ID_AVATAR_CAROUSEL = "AvatarCarouselSwitchModule"
private const val ID_TIME_OF_DAY_REACTIONS = "IdTimeOfDayReactions"
private const val ID_EDIT_POST = "EditPostSwitchModule"
private const val ID_MOMENT_VIEWS = "MomentViewsSwitchModule"
private const val ID_VIDEO_EDITOR_FORCED_RESIZE = "VideoEditorResizingSwitchModule"
private const val ID_ROAD_CUSTOM_MAX_VIDEO_DURATION = "RoadCustomMaxVideoDurationSwitchModule"
private const val ID_MAP_EVENTS_GROUP = "mapEventsGroup"
private const val ID_MAP_FRIENDS_GROUP = "mapFriendsGroup"
private const val ID_CHAT_SEARCH_GROUP = "searchEventsGroup"
private const val BUILD_INFORMATION_TITLE = "Build Info"

object DevTools {

    private val networkFlipperPlugin by lazy { NetworkFlipperPlugin() }
    private val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private var buildVersion: Int = 0
    private var uid: Long = 0L
    private var serverName: String = ""
    private var heapSize: Int = 0
    private var largeHeapSize: Int = 0
    private var isLowMemDevice = false
    private var bridge: IDevToolsBridge? = null

    private var isInitialStickerSuggestionsValue = true
    private var isInitialMomentsValue = true
    private var isInitialDetailedReactionsForPostValue = true
    private var isInitialDetailedReactionsForCommentValue = true
    private var isInitialHiddenAgeAndSexValue = true
    private var isInitialAvatarCarouselEnabledValue = true
    private var isTimeOfDayReactionsValue = true
    private var editPostValue = true

    @JvmStatic
    fun init(
        app: Application,
        buildVersion: Int,
        uid: Long,
        serverName: String,
        heapSize: Int,
        largeHeapSize: Int,
        isLowMemDevice: Boolean,
        @StyleRes theme: Int,
        envChanged: (result: String) -> Unit,
        localeChanged: (result: String) -> Unit,
        stickerSuggestionsEnabledToggled: (enabled: Boolean) -> Unit,
        detailedReactionsForPostEnabledToggled: (enabled: Boolean) -> Unit,
        detailedReactionsForCommentEnabledToggled: (enabled: Boolean) -> Unit,
        hiddenAgeAndSexToggled: (enabled: Boolean) -> Unit,
        avatarCarouselEnabledToggled: (enabled: Boolean) -> Unit,
        leakCanaryChanged: (enabled: Boolean) -> Unit,
        momentsToggled: (enabled: Boolean) -> Unit,
        isLeakCanaryEnabled: Boolean,
        mapEventsToggleChanged: (enabled: Boolean?) -> Unit,
        chatSearchToggleChanged: (enabled: Boolean?) -> Unit,
        videoEditorResizeChanged: (enabled: Boolean) -> Unit,
        isChuckerEnabled: (enabled: Boolean) -> Unit,
        roadMaxVideoDurationEnabled: (enabled: Boolean) -> Unit,
        isTimeOfDayReactionsEnabled: (enabled: Boolean) -> Unit,
        editPostFeatureToggleEnabled: (enabled: Boolean) -> Unit,
        onClickRedesignSection: () -> Unit,
        momentViewsFeatureToggleEnabled: (enabled: Boolean) -> Unit,
        mapFriendsToggleChanged: (enabled: Boolean?) -> Unit
    ) {
        this.bridge = (app as? IDevToolsBridge)
        this.buildVersion = buildVersion
        this.uid = uid
        this.serverName = serverName
        this.heapSize = heapSize
        this.largeHeapSize = largeHeapSize
        this.isLowMemDevice = isLowMemDevice
        app.initFlipper()
        app.registerActivityCallbacks()
        initBeagle(
            app = app,
            theme = theme,
            envChanged = envChanged,
            localeChanged = localeChanged,
            stickerSuggestionsEnabledToggled = stickerSuggestionsEnabledToggled,
            detailedReactionsForPostEnabledToggled = detailedReactionsForPostEnabledToggled,
            detailedReactionsForCommentEnabledToggled = detailedReactionsForCommentEnabledToggled,
            hiddenAgeAndSexToggled = hiddenAgeAndSexToggled,
            avatarCarouselEnabledToggled = avatarCarouselEnabledToggled,
            isLeakCanaryEnabled = isLeakCanaryEnabled,
            momentsToggled = momentsToggled,
            leakCanaryChanged = leakCanaryChanged,
            mapEventsToggleChanged = mapEventsToggleChanged,
            videoEditorResizeChanged = videoEditorResizeChanged,
            chatSearchToggleChanged = chatSearchToggleChanged,
            isChuckerEnabled = isChuckerEnabled,
            roadMaxVideoDurationEnabled = roadMaxVideoDurationEnabled,
            isTimeOfDayReactionsEnabled = isTimeOfDayReactionsEnabled,
            editPostFeatureToggleEnabled = editPostFeatureToggleEnabled,
            onClickRedesignSection = onClickRedesignSection,
            momentViewsFeatureToggleEnabled = momentViewsFeatureToggleEnabled,
            mapFriendsToggleChanged = mapFriendsToggleChanged
        )
    }

    @JvmStatic
    fun applyInterceptors(okHttpBuilder: OkHttpClient.Builder) {
        okHttpBuilder.addNetworkInterceptor(FlipperOkhttpInterceptor(networkFlipperPlugin))
    }

    private fun initBeagle(
        app: Application,
        @StyleRes theme: Int,
        envChanged: (result: String) -> Unit,
        localeChanged: (result: String) -> Unit,
        stickerSuggestionsEnabledToggled: (enabled: Boolean) -> Unit,
        detailedReactionsForPostEnabledToggled: (enabled: Boolean) -> Unit,
        detailedReactionsForCommentEnabledToggled: (enabled: Boolean) -> Unit,
        hiddenAgeAndSexToggled: (enabled: Boolean) -> Unit,
        avatarCarouselEnabledToggled: (enabled: Boolean) -> Unit,
        momentsToggled: (enabled: Boolean) -> Unit,
        leakCanaryChanged: (enabled: Boolean) -> Unit,
        isLeakCanaryEnabled: Boolean,
        mapEventsToggleChanged: (enabled: Boolean?) -> Unit,
        videoEditorResizeChanged: (enabled: Boolean) -> Unit,
        chatSearchToggleChanged: (enabled: Boolean?) -> Unit,
        isChuckerEnabled: (enabled: Boolean) -> Unit,
        roadMaxVideoDurationEnabled: (enabled: Boolean) -> Unit,
        isTimeOfDayReactionsEnabled: (enabled: Boolean) -> Unit,
        editPostFeatureToggleEnabled: (enabled: Boolean) -> Unit,
        onClickRedesignSection: () -> Unit,
        momentViewsFeatureToggleEnabled: (enabled: Boolean) -> Unit,
        mapFriendsToggleChanged: (enabled: Boolean?) -> Unit,
    ) {
//        LeakCanary.config = LeakCanary.config.copy(dumpHeap = isLeakCanaryEnabled)
        LeakCanary.config = LeakCanary.config.copy(dumpHeap = false)

        Beagle.initialize(
            application = app,
            appearance = Appearance(
                themeResourceId = theme
            ),
            behavior = Behavior(
                shakeDetectionBehavior = Behavior.ShakeDetectionBehavior(
                    threshold = null,
                    hapticFeedbackDuration = 0
                )
            ),
        )
        val mapEventsModule = getFeatureToggleOptions(app).let { featureToggleOptions ->
            SingleSelectionListModule(
                id = ID_MAP_EVENTS_GROUP,
                title = app.getString(R.string.feature_toggle_map_events_title),
                items = featureToggleOptions,
                isValuePersisted = true,
                initiallySelectedItemId = featureToggleOptions.first().id,
                onSelectionChanged = {
                    Beagle.hide()
                    mapEventsToggleChanged.invoke(toggleEnabledValue(app, it?.name.orEmpty()))
                }
            )
        }
        mapEventsToggleChanged.invoke(toggleEnabledValue(app, mapEventsModule.getCurrentValue(Beagle).orEmpty()))

        val mapFriendsModule = getFeatureToggleOptions(app).let { featureToggleOptions ->
            SingleSelectionListModule(
                id = ID_MAP_FRIENDS_GROUP,
                title = app.getString(R.string.feature_toggle_map_friends_title),
                items = featureToggleOptions,
                isValuePersisted = true,
                initiallySelectedItemId = featureToggleOptions.first().id,
                onSelectionChanged = {
                    Beagle.hide()
                    mapFriendsToggleChanged.invoke(toggleEnabledValue(app, it?.name.orEmpty()))
                }
            )
        }
        mapFriendsToggleChanged.invoke(toggleEnabledValue(app, mapFriendsModule.getCurrentValue(Beagle).orEmpty()))
        val searchChatsModule = getFeatureToggleOptions(app).let { featureToggleOptions ->
            SingleSelectionListModule(
                id = ID_CHAT_SEARCH_GROUP,
                title = app.getString(R.string.feature_toggle_chat_search_title),
                items = featureToggleOptions,
                isValuePersisted = true,
                initiallySelectedItemId = featureToggleOptions.first().id,
                onSelectionChanged = {
                    Beagle.hide()
                    chatSearchToggleChanged.invoke(toggleEnabledValue(app, it?.name.orEmpty()))
                }
            )
        }
        chatSearchToggleChanged.invoke(toggleEnabledValue(app, searchChatsModule.getCurrentValue(Beagle).orEmpty()))
        Beagle.set(
            PaddingModule(),
            AppInfoButtonModule(),
            DeveloperOptionsButtonModule(),
            PaddingModule(),

            TextModule("REDESIGN SCREEN", TextModule.Type.SECTION_HEADER),
            TextModule("Go to redesign app", TextModule.Type.NORMAL) {
                Beagle.hide()
                onClickRedesignSection()
            },
            TextModule("--------------------------------", TextModule.Type.SECTION_HEADER),
            TextModule(app.getString(R.string.menu_general), TextModule.Type.SECTION_HEADER),
            TextModule("Build version: $buildVersion", TextModule.Type.NORMAL),
            TextModule("Git version: ${BuildConfig.GIT_VERSION_NAME}", TextModule.Type.NORMAL),
            TextModule("Build time: ${getBuildTime(app)}", TextModule.Type.NORMAL),
            TextModule("Git branch: ${BuildConfig.GIT_BRANCH_NAME}", TextModule.Type.NORMAL),
            TextModule("Uid: $uid", TextModule.Type.NORMAL),
            TextModule("Heap Size(Large): $heapSize($largeHeapSize)MB"),
            TextModule("Low Mem Device: $isLowMemDevice"),
            ScreenCaptureToolboxModule(),
            com.pandulapeter.beagle.modules.DeviceInfoModule(),
            listOf(
                RadioGroupOption(app.getString(R.string.menu_lang_ru)),
                RadioGroupOption(app.getString(R.string.menu_lang_en)),
            ).let { radioGroupOptions ->
                SingleSelectionListModule(
                    id = ID_LOCALE_GROUP,
                    title = app.getString(R.string.menu_language),
                    items = radioGroupOptions,
                    isValuePersisted = true,
                    initiallySelectedItemId = radioGroupOptions.first().id,
                    onSelectionChanged = {
                        Beagle.hide()
                        localeChanged.invoke(it?.name.orEmpty())
                    }
                )
            },
            listOf(
                RadioGroupOption(app.getString(R.string.menu_env_dev)),
                RadioGroupOption(app.getString(R.string.menu_env_stage)),
                RadioGroupOption(app.getString(R.string.menu_env_prod))
            ).let { radioGroupOptions ->
                SingleSelectionListModule(
                    id = ID_ENV_GROUP,
                    title = app.getString(R.string.menu_environment),
                    items = radioGroupOptions,
                    isValuePersisted = true,
                    initiallySelectedItemId = radioGroupOptions.first().id,
                    onSelectionChanged = {
                        Beagle.hide()
                        envChanged.invoke(it?.name.orEmpty())
                    }
                )
            },
            DividerModule(),
            TextModule(app.getString(R.string.menu_logs), TextModule.Type.SECTION_HEADER),
            NetworkLogListModule(),
            LogListModule(),
            LifecycleLogListModule(),
            DividerModule(),
            TextModule(app.getString(R.string.toggles), TextModule.Type.SECTION_HEADER),
            SwitchModule(
                id = ID_CHAT_STICKER_SUGGESTIONS,
                isValuePersisted = true,
                onValueChanged = {
                    if (isInitialStickerSuggestionsValue) {
                        isInitialStickerSuggestionsValue = false
                        return@SwitchModule
                    }
                    stickerSuggestionsEnabledToggled.invoke(it)
                },
                text = app.getString(R.string.sticker_suggestions_debug_title)
            ),
            SwitchModule(
                id = ID_MOMENTS,
                isValuePersisted = true,
                onValueChanged = {
                    if (isInitialMomentsValue) {
                        isInitialMomentsValue = false
                        return@SwitchModule
                    }
                    momentsToggled.invoke(it)
                },
                text = app.getString(R.string.moments_debug_title)
            ),
            SwitchModule(
                id = ID_VIDEO_EDITOR_FORCED_RESIZE,
                text = app.getString(R.string.video_editor_forced_resizing),
                isValuePersisted = false,
                onValueChanged = {
                    videoEditorResizeChanged.invoke(it)
                }
            ),
            SwitchModule(
                id = ID_ROAD_CUSTOM_MAX_VIDEO_DURATION,
                text = app.getString(R.string.video_road_custom_max_duration),
                isValuePersisted = false,
                onValueChanged = {
                    roadMaxVideoDurationEnabled.invoke(it)
                }
            ),
            SwitchModule(
                id = ID_DETAILED_REACTIONS_FOR_POST,
                isValuePersisted = true,
                onValueChanged = {
                    if (isInitialDetailedReactionsForPostValue) {
                        isInitialDetailedReactionsForPostValue = false
                        return@SwitchModule
                    }
                    detailedReactionsForPostEnabledToggled.invoke(it)
                },
                text = app.getString(R.string.detailed_reactions_post_debug_title)
            ),
            SwitchModule(
                id = ID_DETAILED_REACTIONS_FOR_COMMENT,
                isValuePersisted = true,
                onValueChanged = {
                    if (isInitialDetailedReactionsForCommentValue) {
                        isInitialDetailedReactionsForCommentValue = false
                        return@SwitchModule
                    }
                    detailedReactionsForCommentEnabledToggled.invoke(it)
                },
                text = app.getString(R.string.detailed_reactions_comment_debug_title)
            ),
            SwitchModule(
                id = ID_HIDDEN_AGE_AND_SEX,
                isValuePersisted = true,
                initialValue = true,
                onValueChanged = {
                    hiddenAgeAndSexToggled.invoke(it)
                },
                text = app.getString(R.string.hidden_age_and_sex_debug_title)
            ),
            SwitchModule(
                id = ID_AVATAR_CAROUSEL,
                isValuePersisted = true,
                initialValue = true,
                onValueChanged = {
                    avatarCarouselEnabledToggled.invoke(it)
                },
                text = app.getString(R.string.avatar_carousel_debug_title)
            ),
            SwitchModule(
                id = ID_TIME_OF_DAY_REACTIONS,
                isValuePersisted = true,
                onValueChanged = {
                    if (isTimeOfDayReactionsValue) {
                        isTimeOfDayReactionsValue = false
                        return@SwitchModule
                    }
                    isTimeOfDayReactionsEnabled.invoke(it)
                },
                text = app.getString(R.string.id_time_of_day_reactions)
            ),
            SwitchModule(
                id = ID_EDIT_POST,
                isValuePersisted = true,
                onValueChanged = {
                    if (editPostValue) {
                        editPostValue = false
                        return@SwitchModule
                    }
                    editPostFeatureToggleEnabled.invoke(it)
                },
                text = app.getString(R.string.edit_post_debug_toggle)
            ),
            SwitchModule(
                id = ID_MOMENT_VIEWS,
                isValuePersisted = true,
                initialValue = true,
                onValueChanged = {
                    momentViewsFeatureToggleEnabled.invoke(it)
                },
                text = app.getString(R.string.moment_views_debug_toggle)
            ),
            mapEventsModule,
            searchChatsModule,
            DividerModule(),
            TextModule(app.getString(R.string.menu_other), TextModule.Type.SECTION_HEADER),
//            createButtonModule(titleResourceId = R.string.menu_leak_canary, onItemSelected = {
//                app.startActivity(LeakCanary.newLeakDisplayActivityIntent())
//            }),
            SwitchModule(
                id = ID_LEAK_CANARY,
                isValuePersisted = true,
                onValueChanged = {
                    LeakCanary.config = LeakCanary.config.copy(dumpHeap = it)
                    leakCanaryChanged.invoke(it)
//                    LeakCanary.showLeakDisplayActivityLauncherIcon(it)
                },
                text = app.getString(R.string.menu_on_leak_canary)
            ),
            SwitchModule(
                id = ID_CHUCKER,
                isValuePersisted = true,
                onValueChanged = {
                    isChuckerEnabled.invoke(it)
                },
                text = app.getString(R.string.menu_on_chucker)
            ),
            BugReportButtonModule()
        )
    }

    private fun getBuildTime(app: Application): String {
        try {
            val ai = app.packageManager.getPackageInfo(app.packageName, 0)
            return SimpleDateFormat.getInstance().format(
                Date(
                    ai.lastUpdateTime
                )
            )
        } catch (e: Exception) {
            Log.e("Devtools", e.toString())
        }
        return sdf.format(Date(BuildConfig.BUILD_TIME))
    }

    private fun Application.registerActivityCallbacks() {
        val application = this
        application.registerActivityLifecycleCallbacks(
            object : ActivityCallbackAdapter() {
                override fun onActivityStarted(activity: Activity) {
                    super.onActivityStarted(activity)
                    application.unregisterActivityLifecycleCallbacks(this)
                }
            }
        )
    }

    private fun Application.initFlipper() {
        SoLoader.init(this, false)

        if (BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(this)) {
            val client = AndroidFlipperClient.getInstance(this)
            client.addPlugin(InspectorFlipperPlugin(this, DescriptorMapping.withDefaults()))
            client.addPlugin(SharedPreferencesFlipperPlugin(this))
            client.addPlugin(DatabasesFlipperPlugin(this))
            client.addPlugin(CrashReporterPlugin.getInstance())
            client.addPlugin(networkFlipperPlugin)
            client.start()
        }
    }

    private fun getFeatureToggleOptions(app: Application) = listOf(
        RadioGroupOption(app.getString(R.string.feature_toggle_value_remote)),
        RadioGroupOption(app.getString(R.string.feature_toggle_value_disabled)),
        RadioGroupOption(app.getString(R.string.feature_toggle_value_enabled))
    )

    private fun toggleEnabledValue(app: Application, strValue: String): Boolean? = when (strValue) {
        app.getString(R.string.feature_toggle_value_enabled) -> true
        app.getString(R.string.feature_toggle_value_disabled) -> false
        else -> null
    }
}

internal abstract class ActivityCallbackAdapter : Application.ActivityLifecycleCallbacks {
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivityCreated(activity: Activity, bundle: Bundle?) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
}

private data class RadioGroupOption(
    val name: String,
) : BeagleListItemContract {

    override val title = name.toText()
}
