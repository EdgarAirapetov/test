package com.numplates.nomera3.presentation.viewmodel.viewevents

sealed class SubscriptionViewEvent {
    object ErrorWhileRequestingSubscriptions: SubscriptionViewEvent()

    object ErrorWhileSearchSubscriptions: SubscriptionViewEvent()

    object ErrorDeleteFromSubscription: SubscriptionViewEvent()

    object LoadFromSubscription: SubscriptionViewEvent()

    object SuccessLoadFromSubscription: SubscriptionViewEvent()

    class SuccessDeleteFromSubscription(
            var deletedUser: Long
    ): SubscriptionViewEvent()
}
