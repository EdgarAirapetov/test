package com.numplates.nomera3.di

import com.numplates.nomera3.modules.moments.show.MomentDelegate
import com.numplates.nomera3.modules.moments.show.MomentDelegateImpl
import com.meera.application_api.media.MediaFileMetaDataDelegate
import com.meera.core.di.scopes.AppScope
import com.numplates.nomera3.data.fcm.CommonNotificationCreator
import com.numplates.nomera3.data.fcm.CommonNotificationCreatorImpl
import com.numplates.nomera3.modules.bump.hardware.ShakeEventListener
import com.numplates.nomera3.modules.bump.hardware.ShakeEventListenerImpl
import com.numplates.nomera3.modules.bump.ui.ShakeRequestsDismissListener
import com.numplates.nomera3.modules.bump.ui.ShakeRequestsDismissListenerImpl
import com.numplates.nomera3.modules.chat.notification.ChatNotificationCenter
import com.numplates.nomera3.modules.chat.notification.MessageStyleNotificationCreator
import com.numplates.nomera3.modules.notifications.service.SyncNotificationService
import com.numplates.nomera3.modules.notifications.service.SyncNotificationServiceImpl
import com.numplates.nomera3.modules.registration.ui.AuthFinishListener
import com.numplates.nomera3.modules.registration.ui.AuthFinishListenerImpl
import com.numplates.nomera3.modules.user.ui.utils.UserBirthdayUtils
import com.numplates.nomera3.modules.user.ui.utils.UserBirthdayUtilsImpl
import com.numplates.nomera3.presentation.birthday.ui.BirthdayTextUtil
import com.numplates.nomera3.presentation.birthday.ui.BirthdayTextUtilImpl
import com.numplates.nomera3.presentation.utils.MediaFileMetaDataDelegateImpl
import com.numplates.nomera3.presentation.view.utils.MutualUsersTextUtil
import com.numplates.nomera3.presentation.view.utils.MutualUsersTextUtilImpl
import dagger.Binds
import dagger.Module

@Module
interface UtilsModule {

    @Binds
    fun provideBirthdayUtils(util: UserBirthdayUtilsImpl): UserBirthdayUtils

    @Binds
    fun provideBirthdayTextSpannableUtil(
        util: BirthdayTextUtilImpl
    ): BirthdayTextUtil

    @Binds
    fun provideMomentDelegate(
        delegate: MomentDelegateImpl
    ): MomentDelegate

    @Binds
    fun provideMessageStyleNotification(util: ChatNotificationCenter): MessageStyleNotificationCreator

    @Binds
    fun provideCommonNotificationCreator(impl: CommonNotificationCreatorImpl): CommonNotificationCreator

    @Binds
    fun provideAuthFinishListener(util: AuthFinishListenerImpl): AuthFinishListener

    @Binds
    @AppScope
    fun bindShakeEventListener(util: ShakeEventListenerImpl): ShakeEventListener

    @Binds
    fun bindWebSocketSyncNotificationService(service: SyncNotificationServiceImpl): SyncNotificationService

    @Binds
    fun bindMutualUsersTextUtil(util: MutualUsersTextUtilImpl): MutualUsersTextUtil

    @Binds
    @AppScope
    fun bindShakeRequestsDismissListener(util: ShakeRequestsDismissListenerImpl): ShakeRequestsDismissListener

    @Binds
    fun bindMediaControllerMetaDataDelegate(util: MediaFileMetaDataDelegateImpl): MediaFileMetaDataDelegate
}
