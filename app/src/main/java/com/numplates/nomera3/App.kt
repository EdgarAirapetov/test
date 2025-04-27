package com.numplates.nomera3

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Process
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.meera.core.devtool.initDevTools
import com.meera.core.di.CoreComponent
import com.meera.core.di.CoreComponentProvider
import com.meera.core.di.DaggerCoreComponent
import com.meera.core.di.modules.APPLICATION_COROUTINE_SCOPE
import com.meera.core.extensions.equalsIgnoreCase
import com.meera.core.network.utils.BaseUrlManager
import com.meera.core.preferences.AppSettings
import com.meera.db.DataStore
import com.mera.bridge.devtools.IDevToolsBridge
import com.mera.bridge.featuretoggle.FeatureToggleValuesBridge
import com.numplates.nomera3.App.Companion.IS_APP_USE_JETPACK_NAVIGATION
import com.numplates.nomera3.di.ApplicationComponent
import com.numplates.nomera3.di.ApplicationModule
import com.numplates.nomera3.di.DaggerApplicationComponent
import com.numplates.nomera3.modules.appInfo.domain.usecase.GetAppInfoAsyncUseCase
import com.numplates.nomera3.modules.devtools_bridge.presentation.DevToolsBridgeInteractor
import com.numplates.nomera3.modules.exoplayer.ExoPlayerCache
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainerImpl
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.registration.di.RegistrationComponent
import com.numplates.nomera3.modules.registration.di.RegistrationModule
import com.numplates.nomera3.presentation.view.utils.NTime
import com.numplates.nomera3.presentation.view.utils.RemoteConfigs
import com.numplates.nomera3.presentation.view.utils.eventbus.RxBus
import dagger.Lazy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Collections
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

private const val METRICA_PROCESS_NAME = "com.numplates.nomera3:Metrica"
private const val METRICA_PROCESS_NAME_DEV = "com.numplates.nomera3.dev:Metrica"

private const val RESTART_DELAY = 1000L

class App : Application(),
    CoreComponentProvider,
    IDevToolsBridge by DevToolsBridgeInteractor(),
    FeatureTogglesContainer by FeatureTogglesContainerImpl(),
    FeatureToggleValuesBridge {

    private val appCoreComponent: CoreComponent by lazy(LazyThreadSafetyMode.NONE) {
        DaggerCoreComponent.factory().create(this)
    }

    //used for videoUploadHelper to remember last operation
    var lastVideoOperation: UUID? = null

    @Inject
    lateinit var dataStore: DataStore

    @Inject
    lateinit var remoteConfigs: RemoteConfigs

    @Inject
    lateinit var baseUrlManager: BaseUrlManager

    @Inject
    lateinit var appSettings: Lazy<AppSettings>

    @Inject
    lateinit var appInfoUseCase: GetAppInfoAsyncUseCase

    @Inject
    @Named(APPLICATION_COROUTINE_SCOPE)
    lateinit var coroutineScope: CoroutineScope

    private var restartCallback: RestartCallback? = null

    //Айди текущей открытой комнаты, необходим для отображения пуша, только в нужной комнате
    var hashSetRooms = hashSetOf<Long>()

    var hashSetVideoToDelete = hashSetOf<String>()

    override fun onCreate() {
        super.onCreate()
        if (isMetricaProcess()) return
        instance = this
        initLoggers()
        val start = System.currentTimeMillis()
        initDagger()
        FirebaseCrashlytics.getInstance().setUserId(appSettings.get().readUID().toString())
        Timber.d("optimization App dagger init time = ${System.currentTimeMillis() - start}")

        ExoPlayerCache.initVideoCache(this)

        NTime(applicationContext)

        initDevTools(
            appSettings.get().readUID(),
            baseUrlManager.provideServerName(),
            R.style.AppTheme,
            envChanged = { env ->
                coroutineScope.launch(Dispatchers.IO) {
                    if (env != appSettings.get().readLastBeagleUrl()) {
                        restartCallback?.restartActivity {
                            baseUrlManager.changeServer(env)
                        }
                    }

                }
            },
            localeChanged = { lang ->
                if (!lang.equalsIgnoreCase(appSettings.get().locale)) {
                    appSettings.get().locale = lang.lowercase()
                    Handler(Looper.getMainLooper()).postDelayed({
                        restartApp()
                    }, 200)
                }
            },
            stickerSuggestionsEnabledToggled = { enabled ->
                chatStickerSuggestionsFeatureToggle.localValue = enabled
            },
            detailedReactionsForPostEnabledToggled = { enabled ->
                detailedReactionsForPostFeatureToggle.localValue = enabled
            },
            detailedReactionsForCommentEnabledToggled = { enabled ->
                detailedReactionsForCommentsFeatureToggle.localValue = enabled
            },
            avatarCarouselEnabledToggled = { enabled ->
                avatarCarouselFeatureToggle.localValue = enabled
            },
            hiddenAgeAndSexEnabledToggled = { enabled ->
                hiddenAgeAndSexFeatureToggle.localValue = enabled
            },
            isLeakCanaryEnabled = appSettings.get().isLeakCanaryEnabled,
            leakCanaryChanged = { leakCanaryEnabled ->
                appSettings.get().isLeakCanaryEnabled = leakCanaryEnabled
            },
            mapEventsToggleChanged = { enabled ->
                mapEventsFeatureToggle.localValue = enabled
            },
            chatSearchToggleChanged = { enabled ->
                chatSearchFeatureToggle.localValue = enabled
            },
            videoEditorResizeChanged = { enabled ->
                videoEditorFeatureToggle.localValue = enabled
            },
            roadMaxVideoDurationEnabled = { enabled ->
                roadVideoMaxDurationFeatureToggle.localValue = enabled
                appInfoUseCase.resetCache()
            },
            isChuckerEnabled = { isChuckerEnabled ->
                appSettings.get().isChuckerEnabled = isChuckerEnabled
                Handler(Looper.getMainLooper()).postDelayed({
                    restartApp()
                }, 200)
            },
            isTimeOfDayReactionsEnabled = { enabled ->
                timeOfDayReactionsFeatureToggle.localValue = enabled
            },
            momentsToggled = { enabled ->
                momentsFeatureToggle.localValue = enabled
            },
            editPostFeatureToggleEnabled = { enabled ->
                editPostFeatureToggle.localValue = enabled
            },
            onClickRedesignSection = {
                applicationContext.startActivity(
                    Intent(
                        applicationContext,
                        MeeraAct::class.java
                    ).apply { setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)  }
                )
            },
            momentViewsFeatureToggleEnabled = { enabled ->
                momentViewsFeatureToggle.localValue = enabled
            },
            mapFriendsToggleChanged = { enabled ->
                mapFriendsFeatureToggle.localValue = enabled
            },
        )
        initDevToolsBridge()
        initANRTracker()
        initAppsFlyer()
        startAppsFlyer()
        initFeatureTogglesContainer()
    }

    override fun getCoreComponent(): CoreComponent {
        return appCoreComponent
    }

    override fun isRoadMaxVideoDurationEnabled(): Boolean {
        return roadVideoMaxDurationFeatureToggle.isEnabled
    }

    fun setRestartCallbackListener(restartCallback: RestartCallback) {
        this.restartCallback = restartCallback
    }

    fun removeRestartListener() {
        restartCallback = null
    }

    fun initFeatureTogglesContainer() {
        remoteConfigs.initializeWithCachedValues()
    }

    fun restartApp() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
            val mainIntent = Intent.makeRestartActivityTask(intent?.component)
            applicationContext.startActivity(mainIntent)
            Runtime.getRuntime().exit(0)
        }, RESTART_DELAY)
    }

    private fun initAppsFlyer() {
        val conversionDataListener = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(data: MutableMap<String, Any>?) {
                data?.map { Timber.d("AppFlyer::onConversionDataSuccess: ${it.key} = ${it.value}") }
            }

            override fun onConversionDataFail(error: String?) {
                error?.also { Timber.e(it) }
            }

            override fun onAppOpenAttribution(data: MutableMap<String, String>?) {
                data?.map { Timber.d("AppFlyer::onAppOpenAttribution: ${it.key} = ${it.value}") }
            }

            override fun onAttributionFailure(error: String?) {
                error?.also { Timber.e("AppFlyer::onAttributionFailure: $it") }
            }
        }

        AppsFlyerLib.getInstance().init(APPS_FLYER_DEV_KEY, conversionDataListener, this)
    }

    private fun startAppsFlyer() {
        AppsFlyerLib.getInstance().start(this)
    }

    private fun initANRTracker() {
        /*ANRWatchDog(500)
                .setReportMainThreadOnly()
                .setANRListener {
                    Timber.e(it)
                }
                .start();*/
    }

    private fun initLoggers() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun isMetricaProcess(): Boolean {
        return getCurrentProcessName().equals(METRICA_PROCESS_NAME) || getCurrentProcessName().equals(
            METRICA_PROCESS_NAME_DEV
        )
    }

    private fun getCurrentProcessName(): String? {
        val pid = Process.myPid()
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (processInfo in (manager.runningAppProcesses ?: Collections.emptyList())) {
            if (processInfo.pid == pid) {
                return processInfo.processName
            }
        }
        return null
    }

    private fun initDagger() {
        component = DaggerApplicationComponent
            .builder()
            .coreComponent(appCoreComponent)
            .applicationModule(ApplicationModule(this))
            .build()

        component.inject(this)
    }

    companion object {

        const val IS_MOCKED_DATA: Boolean = false
        const val IS_APP_USE_JETPACK_NAVIGATION: Boolean = false

        private const val APPS_FLYER_DEV_KEY = "WwymrSKgPG9Q5TVcm9thN"

        private var instance: App? = null

        lateinit var component: ApplicationComponent

        private var registrationComponent: RegistrationComponent? = null

        val bus: RxBus by lazy { RxBus() }

        const val MAX_ITEMS_INFINITY_RECYCLER = 100000

        const val GOOGLE_PLAY_MARKET_URL =
            "https://play.google.com/store/apps/details?id=com.numplates.nomera3"

        const val PLATFORM = "android"

        const val MY_TRACKER_SDK_KEY = "94146890491819938283"

        fun get(): App? {
            return instance
        }

        fun getRegistrationComponent(): RegistrationComponent {
            return registrationComponent ?: component.addRegistrationComponent(RegistrationModule())
                .apply { registrationComponent = this }
        }

        fun clearRegistrationComponent() {
            registrationComponent = null
        }
    }
}

fun checkAppUseJetpackNavigation(
    isJetpackNavigation: () -> Unit = {},
    isNotJetpackNavigation: () -> Unit = {}
) {
    if (IS_APP_USE_JETPACK_NAVIGATION) {
        isJetpackNavigation.invoke()
    } else {
        isNotJetpackNavigation.invoke()
    }
}

/**
 * Аннотация означает, что для данной функции уже сделана редизайненная замена
 */
annotation class InRedesignExists(val description: String = "")
