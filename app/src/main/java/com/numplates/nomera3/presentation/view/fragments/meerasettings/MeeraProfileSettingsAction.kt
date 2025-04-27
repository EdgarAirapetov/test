package com.numplates.nomera3.presentation.view.fragments.meerasettings

sealed class MeeraProfileSettingsAction {
    object MeeraPushNotificationAction : MeeraProfileSettingsAction()
    object MeeraPrivacySecurityAction : MeeraProfileSettingsAction()
    object MeeraRateAppAction : MeeraProfileSettingsAction()
    object MeeraAboutMeeraAction : MeeraProfileSettingsAction()
    object MeeraSupportAction : MeeraProfileSettingsAction()
    object MeeraRestorePurchases : MeeraProfileSettingsAction()
}
