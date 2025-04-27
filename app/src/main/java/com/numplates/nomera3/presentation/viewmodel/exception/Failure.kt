package com.numplates.nomera3.presentation.viewmodel.exception


sealed class Failure {

    class NetworkConnection : Failure()

    class ServerError(userMessage: String) : Failure() {
        var message: String = userMessage
    }

    class DatabaseError : Failure()


    /** * Extend this class for feature specific failures.*/
    open class FeatureFailure : Failure()


    data class UserBlockedFailureWithoutHideContent(
            var reason: String?,
            var expired: Long?
    ) : FeatureFailure()

    data class UserBlockedFailureWithHideContent(var reason: String?) : FeatureFailure()

}
