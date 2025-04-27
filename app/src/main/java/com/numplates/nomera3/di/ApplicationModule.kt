package com.numplates.nomera3.di

import android.content.ClipboardManager
import android.content.Context
import androidx.work.WorkManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.meera.core.di.modules.HIWAY_API_RETROFIT
import com.meera.core.di.scopes.AppScope
import com.meera.core.navigation.NavigationRouter
import com.meera.core.preferences.AppSettings
import com.meera.core.preferences.PrefManager
import com.meera.core.utils.files.FileManager
import com.meera.core.utils.files.FileUtilsImpl
import com.meera.db.DataStore
import com.numplates.nomera3.App
import com.numplates.nomera3.data.fcm.NotificationHelper
import com.numplates.nomera3.data.network.ApiHiWay
import com.numplates.nomera3.domain.interactornew.ProcessAnimatedAvatar
import com.numplates.nomera3.domain.interactornew.ProcessAnimatedAvatarImpl
import com.numplates.nomera3.modules.appDialogs.data.DialogStateStorage
import com.numplates.nomera3.modules.appDialogs.ui.DialogDismissListener
import com.numplates.nomera3.modules.baseCore.ResourceManager
import com.numplates.nomera3.modules.baseCore.ResourceManagerImp
import com.numplates.nomera3.modules.baseCore.helper.HolidayInfoHelper
import com.numplates.nomera3.modules.baseCore.helper.HolidayInfoHelperImpl
import com.numplates.nomera3.modules.baseCore.helper.notification.NotificationManager
import com.numplates.nomera3.modules.baseCore.helper.notification.NotificationManagerImpl
import com.numplates.nomera3.modules.billing.BillingClientWrapper
import com.numplates.nomera3.modules.chat.helpers.resendmessage.ResendNotificationUtil
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.newroads.data.entities.FilterSettings
import com.numplates.nomera3.modules.newroads.data.entities.FilterSettingsProvider
import com.numplates.nomera3.modules.newroads.util.FilterSettingsMapper
import com.numplates.nomera3.modules.onboarding.data.OnboardingStateStorage
import com.numplates.nomera3.modules.tracker.FireBaseAnalytics
import com.numplates.nomera3.modules.tracker.ITrackerActions
import com.numplates.nomera3.modules.tracker.TrackerActionsImpl
import com.numplates.nomera3.modules.upload.LiTrVideoConverter
import com.numplates.nomera3.modules.upload.VideoConverter
import com.numplates.nomera3.modules.upload.domain.repository.UploadDataStore
import com.numplates.nomera3.presentation.download.DownloadMediaHelper
import com.numplates.nomera3.presentation.upload.IUploadContract
import com.numplates.nomera3.presentation.upload.UploadMediaHelper
import com.numplates.nomera3.presentation.view.utils.TextProcessorUtil
import com.numplates.nomera3.presentation.view.utils.TextProcessorUtilImpl
import com.numplates.nomera3.presentation.view.utils.apphints.HintManager
import com.numplates.nomera3.presentation.view.utils.camera.CameraProvider
import com.numplates.nomera3.presentation.view.utils.mediaprovider.MediaProvider
import com.numplates.nomera3.presentation.view.utils.mediaprovider.MediaProviderContract
import com.twilio.audioswitch.AudioSwitch
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import java.io.File
import javax.inject.Named

const val MAIN_ROAD_FILTER_SETTINGS = "main_road_settings"
const val CUSTOM_ROAD_FILTER_SETTINGS = "custom_road_settings"
const val CACHE_DIR = "CACHE_DIR"

@Module
class ApplicationModule(private val application: App) {

    @Provides
    @AppScope
    fun provideApp(): App = application

    @Provides
    @AppScope
    fun provideApplicationContext(): Context = application

    @Provides
    fun provideAudioSwitch(context: Context): AudioSwitch {
        return AudioSwitch(context.applicationContext)
    }

    @Provides
    @AppScope
    fun provideWorkManager(context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @AppScope
    @Named(CACHE_DIR)
    fun provideCacheDir(): File {
        return application.cacheDir
    }

    @Provides
    @AppScope
    @Named(MAIN_ROAD_FILTER_SETTINGS)
    fun provideMainRoadFilterSettings(settings: AppSettings): FilterSettings {
        return FilterSettings(MAIN_ROAD_FILTER_SETTINGS, settings)
    }

    @Provides
    @AppScope
    fun provideUploadStore(dataStore: DataStore): UploadDataStore {
        return UploadDataStore(dataStore)
    }

    @Provides
    @AppScope
    @Named(CUSTOM_ROAD_FILTER_SETTINGS)
    fun provideVideoRoadFilterSettings(settings: AppSettings): FilterSettings {
        return FilterSettings(CUSTOM_ROAD_FILTER_SETTINGS, settings)
    }

    @Provides
    @AppScope
    fun provideFilterSettingsProvider(
        @Named(MAIN_ROAD_FILTER_SETTINGS)
        mainFilterSettings: FilterSettings,
        @Named(CUSTOM_ROAD_FILTER_SETTINGS)
        customFilterSettings: FilterSettings
    ): FilterSettingsProvider {
        return FilterSettingsProvider(mainFilterSettings, customFilterSettings)
    }

    @Provides
    @AppScope
    fun provideFilterSettingsMapper(): FilterSettingsMapper {
        return FilterSettingsMapper()
    }

    @Provides
    @AppScope
    fun provideBillingClientWrapper(application: App): BillingClientWrapper {
        return BillingClientWrapper(application)
    }

    @Provides
    @AppScope
    fun provideFirebaseAnalytic(): FireBaseAnalytics {
        return FireBaseAnalytics(FirebaseAnalytics.getInstance(application))
    }

    @Provides
    fun provideMediaProvider(application: App): MediaProviderContract =
        MediaProvider(application)

    @Provides
    fun provideUploadMediaHelper(): IUploadContract =
        UploadMediaHelper(application)


    @Provides
    @AppScope
    fun provideDownloadMediaHelper(): DownloadMediaHelper = DownloadMediaHelper(application)

    @Provides
    fun provideCameraProvideBuilder(application: App): CameraProvider.Builder =
        CameraProvider.Builder(application)

    @Provides
    @AppScope
    fun provideHintManager(appSettings: AppSettings): HintManager = HintManager(appSettings)

    @Provides
    fun provideTrackerActions(): ITrackerActions = TrackerActionsImpl()

    @Provides
    fun provideNotificationHelper(application: App) =
        NotificationHelper(application.applicationContext)

    @Provides
    fun provideResendNotificationUtil(
        application: App,
        dataStore: DataStore,
        notificationHelper: NotificationHelper
    ) = ResendNotificationUtil(application, dataStore, notificationHelper)

    @Provides
    @AppScope
    fun provideHolidayInfo(appSettings: AppSettings): HolidayInfoHelper {
        return HolidayInfoHelperImpl(appSettings)
    }

    @Provides
    fun provideProcessAnimatedAvatar(
        application: App, fileManager: FileManager, appSettings: AppSettings
    ): ProcessAnimatedAvatar {
        return ProcessAnimatedAvatarImpl(application.applicationContext, fileManager, appSettings)
    }


    @Provides
    fun provideTextUtilProcessor(application: App): TextProcessorUtil {
        return TextProcessorUtilImpl(application.applicationContext)
    }

    @Provides
    fun provideNotificationManager(
        application: App,
        appSettings: AppSettings
    ): NotificationManager {
        return NotificationManagerImpl(appSettings, application)
    }

    @Provides
    fun provideResourceManage(application: App): ResourceManager {
        return ResourceManagerImp(application)
    }

    @Provides
    fun provideFileUtils(application: App): FileManager {
        return FileUtilsImpl(application)
    }

    @Provides
    @AppScope
    fun provideApiHiWay(@Named(HIWAY_API_RETROFIT) retrofit: Retrofit): ApiHiWay {
        return retrofit.create(ApiHiWay::class.java)
    }

    @Provides
    fun provideDialogStateStorage(prefManager: PrefManager): DialogStateStorage {
        return DialogStateStorage(prefManager)
    }

    @Provides
    fun provideOnboardingStateStorage(prefManager: PrefManager): OnboardingStateStorage {
        return OnboardingStateStorage(prefManager)
    }

    @AppScope
    @Provides
    fun provideDialogDismissListener() = DialogDismissListener()

    @Provides
    fun provideVideoConverter(): VideoConverter = LiTrVideoConverter()

    @AppScope
    @Provides
    fun provideNavigationRouter() = NavigationRouter()

    @AppScope
    @Provides
    fun provideFeatureTogglesContainer(application: App): FeatureTogglesContainer = application

    @Provides
    fun provideClipboardManager(application: App): ClipboardManager {
        return application.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }
}
