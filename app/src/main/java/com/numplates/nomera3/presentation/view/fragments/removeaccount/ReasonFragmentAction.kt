package com.numplates.nomera3.presentation.view.fragments.removeaccount

internal const val LEAVING_WHILE = 1
internal const val DIFFICULT_FIGURE = 2
internal const val DONT_FEEL_SAFE_ACTION = 3
internal const val UGLY_APP_ACTION = 4
internal const val DONT_LIKE_MODERATION_ACTION = 5
internal const val UNPLEASANT_COMMUNICATION_ACTION = 6
internal const val UNINTERESTING_CONTENT_ACTION = 7
internal const val SPEND_TOO_MUCH_TIME_ACTION = 8
internal const val TECHNICAL_SUPPORT = 9
internal const val CONTINUE_BUTTON_ACTION = 10

sealed class ReasonFragmentAction(val reason: Int) {
    data object LeavingWhileAction : ReasonFragmentAction(LEAVING_WHILE)
    data object DifficultFigureAction : ReasonFragmentAction(DIFFICULT_FIGURE)
    data object DontFeelSafeAction : ReasonFragmentAction(DONT_FEEL_SAFE_ACTION)
    data object UglyAppAction : ReasonFragmentAction(UGLY_APP_ACTION)
    data object DontLikeModerationAction : ReasonFragmentAction(DONT_LIKE_MODERATION_ACTION)
    data object UnpleasantCommunicationAction : ReasonFragmentAction(UNPLEASANT_COMMUNICATION_ACTION)
    data object UninterestingContentAction : ReasonFragmentAction(UNINTERESTING_CONTENT_ACTION)
    data object SpendTooMuchTimeAction : ReasonFragmentAction(SPEND_TOO_MUCH_TIME_ACTION)
    data object AnotherReasonAction : ReasonFragmentAction(TECHNICAL_SUPPORT)
    data object ContinueButtonAction : ReasonFragmentAction(CONTINUE_BUTTON_ACTION)
}
