package com.numplates.nomera3.di

import com.meera.analytics.add_post.AddPostAnalytics
import com.meera.analytics.add_post.AddPostAnalyticsImpl
import com.meera.core.di.scopes.AppScope
import com.numplates.nomera3.modules.baseCore.data.repository.AmplitudeRepositoryImpl
import com.numplates.nomera3.modules.baseCore.data.repository.AmplitudeShakeAnalyticRepositoryImpl
import com.numplates.nomera3.modules.baseCore.domain.repository.AmplitudeRepository
import com.numplates.nomera3.modules.baseCore.domain.repository.AmplitudeShakeAnalyticRepository
import com.numplates.nomera3.modules.baseCore.helper.amplitude.add_friend.AmplitudeAddFriendAnalytic
import com.numplates.nomera3.modules.baseCore.helper.amplitude.add_friend.AmplitudeAddFriendAnalyticImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.auth.AmplitudeAuthAnalytic
import com.numplates.nomera3.modules.baseCore.helper.amplitude.auth.AmplitudeAuthAnalyticImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.AmplitudeChatAnalytic
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.AmplitudeChatAnalyticImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.mediakeyboard.AmplitudeMediaKeyboardAnalytic
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.mediakeyboard.AmplitudeMediaKeyboardAnalyticImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.comments.AmplitudeCommentsAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.comments.AmplitudeCommentsAnalyticsImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.complaints.AmplitudeComplaints
import com.numplates.nomera3.modules.baseCore.helper.amplitude.complaints.AmplitudeComplaintsImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.editor.AmplitudeEditor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.editor.AmplitudeHelperEditorImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.feed.AmplitudeFeedAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.feed.AmplitudeFeedAnalyticsImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.findfriends.AmplitudeFindFriends
import com.numplates.nomera3.modules.baseCore.helper.amplitude.findfriends.AmplitudeFindFriendsImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton.AmplitudeFollowButton
import com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton.AmplitudeHelperFollowButtonImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.geo_popup.AmplitudeGeoPopup
import com.numplates.nomera3.modules.baseCore.helper.amplitude.geo_popup.AmplitudeGeoPopupImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.map.AmplitudeMap
import com.numplates.nomera3.modules.baseCore.helper.amplitude.map.AmplitudeMapImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.map_filters.AmplitudeHelperMapFiltersImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.map_filters.AmplitudeMapFilters
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudeMapEvents
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudeMapEventsImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapfriends.AmplitudeMapFriends
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapfriends.AmplitudeMapFriendsImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.AmplitudeMapSnippet
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.AmplitudeMapSnippetImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudeHelperMomentImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudeMoment
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mutual_friends.AmplitudeMutualFriendsAnalytic
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mutual_friends.AmplitudeMutualFriendsAnalyticImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mutual_friends.AmplitudeSelectPrivacyShowFriends
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mutual_friends.AmplitudeSelectPrivacyShowFriendsImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.onboarding.AmplitudeHelperOnBoardingImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.onboarding.AmplitudeOnBoarding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleAnalyticsImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.photo.AmplitudePhotoAnalytic
import com.numplates.nomera3.modules.baseCore.helper.amplitude.photo.AmplitudePhotoAnalyticImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeFriendRequest
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeFriendRequestImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeProfile
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeProfileImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.FriendInviteTapAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.FriendInviteTapAnalyticsImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profilestatistics.AmplitudeProfileStatistics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profilestatistics.AmplitudeProfileStatisticsImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.rating.AmplitudeHelperRatingImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.rating.AmplitudeRating
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudeHelperReactionsImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudeReactions
import com.numplates.nomera3.modules.baseCore.helper.amplitude.rec_system.AmplitudeRecSystemAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.rec_system.AmplitudeRecSystemAnalyticsImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.screenshot.AmplitudeScreenshotAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.screenshot.AmplitudeScreenshotAnalyticsImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.search.AmplitudeMainSearchAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.search.AmplitudeMainSearchAnalyticsImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.sendinvitation.AmplitudeSendInvitationAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.sendinvitation.AmplitudeSendInvitationAnalyticsImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.sync_contacts.SyncContactsAnalytic
import com.numplates.nomera3.modules.baseCore.helper.amplitude.sync_contacts.SyncContactsAnalyticImpl
import dagger.Binds
import dagger.Module

//import dagger.Provides
//
//
//@Module(includes = [AnalyticsBindModule::class])
//class AnalyticsModule {
//
//    @Provides
//    @AppScope
//    fun provideAmplitudeAnalytics(application: App): AmplitudeClient =
//        Amplitude.getInstance()
//            .initialize(application.applicationContext, AMPLITUDE_API_KEY)
//            .enableForegroundTracking(application).apply {
//                setFlushEventsOnClose(false)
//                trackSessionEvents(true)
//                enableLogging(true)
//            }.apply { initAppMetrics(application) }
//
//
//    private fun initAppMetrics(application: App) {
//        val config = YandexMetricaConfig
//            .newConfigBuilder(APP_METRICA_API_KEY)
//            .withCrashReporting(true)
//            .withLocationTracking(true)
//            .withSessionTimeout(300) // 5 минут
//            .build()
//        YandexMetrica.activate(application.applicationContext, config)
//        YandexMetrica.enableActivityAutoTracking(application)
//        YandexMetricaPush.init(application.applicationContext)
//    }
//}

@Module
interface AnalyticsBindModule {

    @Binds
    @AppScope
    fun bindAmplitudeHelper(helper: AmplitudeRepositoryImpl): AmplitudeRepository

    @Binds
    fun bindAmplitudeOnBoarding(helper: AmplitudeHelperOnBoardingImpl): AmplitudeOnBoarding

    @Binds
    fun bindAmplitudeProfile(helper: AmplitudeProfileImpl): AmplitudeProfile

    @Binds
    fun bindAmplitudeOnMoment(helper: AmplitudeHelperMomentImpl): AmplitudeMoment

    @Binds
    fun bindAmplitudeReactions(helper: AmplitudeHelperReactionsImpl): AmplitudeReactions

    @Binds
    fun bindAmplitudeEditor(helper: AmplitudeHelperEditorImpl): AmplitudeEditor

    @Binds
    fun bindAmplitudeRating(helper: AmplitudeHelperRatingImpl): AmplitudeRating

    @Binds
    fun bindAmplitudeFollowButton(helper: AmplitudeHelperFollowButtonImpl): AmplitudeFollowButton

    @Binds
    fun bindAmplitudeMapFilters(helper: AmplitudeHelperMapFiltersImpl): AmplitudeMapFilters

    @Binds
    fun bindAmplitudeInvitationTap(
        helper: AmplitudeSendInvitationAnalyticsImpl
    ): AmplitudeSendInvitationAnalytics

    @Binds
    fun bindAmplitudeFriendRequest(helper: AmplitudeFriendRequestImpl): AmplitudeFriendRequest

    @Binds
    fun bindAmplitudeChat(helper: AmplitudeChatAnalyticImpl): AmplitudeChatAnalytic

    @Binds
    fun bindAmplitudeAuth(helper: AmplitudeAuthAnalyticImpl): AmplitudeAuthAnalytic

    @Binds
    fun bindAmplitudeProfileStatistics(helper: AmplitudeProfileStatisticsImpl): AmplitudeProfileStatistics

    @Binds
    fun bindAmplitudeFindFriends(helper: AmplitudeFindFriendsImpl): AmplitudeFindFriends

    @Binds
    fun bindAmplitudeComplaints(helper: AmplitudeComplaintsImpl): AmplitudeComplaints

    @Binds
    fun bindAmplitudeSelectPrivacyShowFriends(
        helper: AmplitudeSelectPrivacyShowFriendsImpl
    ): AmplitudeSelectPrivacyShowFriends

    @Binds
    fun bindAmplitudeMutualFriendsAnalytic(
        helper: AmplitudeMutualFriendsAnalyticImpl
    ): AmplitudeMutualFriendsAnalytic

    @Binds
    fun bindAmplitudeMapUserSnippet(helper: AmplitudeMapSnippetImpl): AmplitudeMapSnippet

    @Binds
    fun bindAmplitudeGeoPopup(helper: AmplitudeGeoPopupImpl): AmplitudeGeoPopup

    @Binds
    fun bindAmplitudeMap(helper: AmplitudeMapImpl): AmplitudeMap

    @Binds
    fun bindAmplitudeFeedAnalytics(helper: AmplitudeFeedAnalyticsImpl): AmplitudeFeedAnalytics

    @Binds
    fun bindAmplitudeCommentAnalytics(helper: AmplitudeCommentsAnalyticsImpl): AmplitudeCommentsAnalytics

    @Binds
    fun bindAmplitudeRecSystemAnalytics(helper: AmplitudeRecSystemAnalyticsImpl): AmplitudeRecSystemAnalytics

    @Binds
    fun bindAmplitudePeopleAnalytics(helper: AmplitudePeopleAnalyticsImpl): AmplitudePeopleAnalytics

    @Binds
    fun bindAmplitudeInviteTapAnalytics(helper: FriendInviteTapAnalyticsImpl): FriendInviteTapAnalytics

    @Binds
    fun bindAmplitudeMainSearchAnalytics(helper: AmplitudeMainSearchAnalyticsImpl): AmplitudeMainSearchAnalytics

    @Binds
    fun bindAmplitudeAddFriendAnalytic(helper: AmplitudeAddFriendAnalyticImpl): AmplitudeAddFriendAnalytic

    @Binds
    fun bindAmplitudePhotoAnalytic(helper: AmplitudePhotoAnalyticImpl): AmplitudePhotoAnalytic

    @Binds
    fun bindAmplitudeMediaKeyboardAnalytic(helper: AmplitudeMediaKeyboardAnalyticImpl): AmplitudeMediaKeyboardAnalytic

    @Binds
    fun bindAmplitudeShakeAnalytic(helper: AmplitudeShakeAnalyticRepositoryImpl): AmplitudeShakeAnalyticRepository

    @Binds
    fun bindAmplitudeSyncContactsAnalytic(helper: SyncContactsAnalyticImpl): SyncContactsAnalytic

    @Binds
    fun bindAmplitudeMapEventsAnalytic(helper: AmplitudeMapEventsImpl): AmplitudeMapEvents

    @Binds
    fun bindAmplitudeMapFriendsAnalytic(helper: AmplitudeMapFriendsImpl): AmplitudeMapFriends

    @Binds
    fun bindAmplitudeAddPostAnalytics(analytic: AddPostAnalyticsImpl): AddPostAnalytics

    @Binds
    fun bindScreenshotAnalytics(analytic: AmplitudeScreenshotAnalyticsImpl): AmplitudeScreenshotAnalytics
}

