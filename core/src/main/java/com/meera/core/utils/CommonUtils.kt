package com.meera.core.utils

import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.meera.core.R
import com.meera.core.extensions.dp
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.PaddingState
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState

enum class ApprovedIconSize {
    SMALL, MEDIUM, LARGE
}

data class TopAuthorApprovedUserModel(
    val approved: Boolean,
    val interestingAuthor: Boolean,
    val approvedIconSize: ApprovedIconSize = ApprovedIconSize.MEDIUM,
    val isVip: Boolean = false,
    @DrawableRes val customIcon: Int? = null,
    @DrawableRes val customIconTopContent: Int? = null
)

fun TextView.enableApprovedIcon(
    enabled: Boolean,
    isVip: Boolean = false,
    size: ApprovedIconSize = ApprovedIconSize.MEDIUM,
    padding: Int = 4.dp,
    topContentMaker: Boolean = false,
    @DrawableRes customIcon: Int? = null
) {
    if (enabled) {
        val drawableRes = when {
            customIcon != null -> {
                customIcon
            }

            topContentMaker -> {
                R.drawable.ic_approved_author_gold_14
            }

            isVip -> {
                when (size) {
                    ApprovedIconSize.SMALL -> R.drawable.ic_verified_gold_14dp
                    ApprovedIconSize.MEDIUM -> R.drawable.ic_verified_gold_16dp
                    ApprovedIconSize.LARGE -> R.drawable.ic_verified_gold_24dp
                }
            }

            else -> {
                when (size) {
                    ApprovedIconSize.SMALL -> R.drawable.ic_verified_purple_14dp
                    ApprovedIconSize.MEDIUM -> R.drawable.ic_filled_verified_s_colored
                    ApprovedIconSize.LARGE -> R.drawable.ic_filled_verified_m_colored
                }
            }
        }
        compoundDrawablePadding = padding
        setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, drawableRes, 0)
    } else if (topContentMaker) {
        compoundDrawablePadding = padding
        setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_approved_author_gold_14, 0)
    } else {
        compoundDrawablePadding = 0
        setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
    }
}

fun TextView.enableTopContentAuthorApprovedUser(
    params: TopAuthorApprovedUserModel
) {
    when {
        params.approved -> {
            enableApprovedIcon(
                enabled = true,
                isVip = params.isVip,
                size = params.approvedIconSize,
                customIcon = params.customIcon
            )
        }

        params.interestingAuthor -> {
            enableApprovedIcon(
                enabled = true,
                topContentMaker = true,
                customIcon = params.customIconTopContent
            )
        }

        else -> {
            enableApprovedIcon(
                enabled = false
            )
        }
    }
}

fun Context.convertDpToPx(dp: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        this.resources.displayMetrics
    )
}

const val IS_APP_REDESIGNED: Boolean = true

fun checkAppRedesigned(
    isRedesigned: () -> Unit = {},
    isNotRedesigned: () -> Unit = {}
) {
    if (IS_APP_REDESIGNED) {
        isRedesigned.invoke()
    } else {
        isNotRedesigned.invoke()
    }
}

private const val SNACK_BAR_DEFAULT_VERTICAL_PADDING = 16

fun showCommonError(
    content: CharSequence?,
    view: View,
    anchorView: View? = null,
    paddingBottom: Int = SNACK_BAR_DEFAULT_VERTICAL_PADDING,
) {
    UiKitSnackBar.make(
        view = view,
        params = SnackBarParams(
            snackBarViewState = SnackBarContainerUiState(
                messageText = content,
                avatarUiState = AvatarUiState.ErrorIconState
            ),
            duration = BaseTransientBottomBar.LENGTH_SHORT,
            dismissOnClick = true,
            paddingState = PaddingState(bottom = paddingBottom)
        )
    ).apply {
        setAnchorView(anchorView)
        show()
    }
}

fun showCommonSuccessMessage(
    content: CharSequence?,
    view: View,
    anchorView: View? = null,
    paddingBottom: Int = SNACK_BAR_DEFAULT_VERTICAL_PADDING,
) {
    showCommonSuccessMessage(
        content = content,
        view = view,
        anchorView = anchorView,
        paddingState = PaddingState(bottom = paddingBottom)
    )
}

fun showCommonSuccessMessage(
    content: CharSequence?,
    view: View,
    anchorView: View? = null,
    paddingState: PaddingState,
) {
    UiKitSnackBar.make(
        view = view,
        params = SnackBarParams(
            snackBarViewState = SnackBarContainerUiState(
                messageText = content,
                avatarUiState = AvatarUiState.SuccessIconState
            ),
            duration = BaseTransientBottomBar.LENGTH_SHORT,
            dismissOnClick = true,
            paddingState = paddingState
        )
    ).apply {
        setAnchorView(anchorView)
        show()
    }
}

fun getNotificationPendingIntentFlag(): Int {
    val sdkVersion = Build.VERSION.SDK_INT
    return if (sdkVersion >= Build.VERSION_CODES.S && sdkVersion < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        PendingIntent.FLAG_MUTABLE
    } else if (sdkVersion == Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        PendingIntent.FLAG_IMMUTABLE
    } else {
        PendingIntent.FLAG_IMMUTABLE
    }
}
