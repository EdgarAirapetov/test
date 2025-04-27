package com.meera.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.meera.core.R
import com.meera.core.base.enums.PermissionState
import com.meera.core.databinding.MeeraSetupPermissionsViewBinding
import com.meera.uikit.widgets.inflateBindingForViewGroup

class MeeraSetupPermissionsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private val binding = inflateBindingForViewGroup(MeeraSetupPermissionsViewBinding::inflate)

    private var onRequestPermissions: () -> Unit = {}
    private var onOpenSettings: () -> Unit = {}

    private var state: PermissionState = PermissionState.GRANTED

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        clipChildren = false
        clipToPadding = false
    }

    private fun setupIcon() {
        binding.ivPermissionIcon.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                when (state) {
                    PermissionState.NOT_GRANTED_CAN_BE_REQUESTED -> R.drawable.ic_meera_permission_granted_request
                    else -> R.drawable.ic_meera_permission_request
                }
            )
        )
    }

    private fun setupTitle() {
        binding.tvPermissionTitle.text = context.resources.getString(
            when (state) {
                PermissionState.NOT_GRANTED_CAN_BE_REQUESTED -> R.string.meera_allow_access_to_galery
                else -> R.string.meera_photos_and_videos_not_showing
            }
        )
    }

    private fun setupDesc() {
        binding.tvPermissionDesc.text = context.resources.getString(
            when (state) {
                PermissionState.NOT_GRANTED_CAN_BE_REQUESTED -> R.string.meera_allow_access_to_photo_desc
                else -> R.string.meera_photos_not_showing_desc
            }
        )
    }

    private fun setupAction() {
        binding.btnAction.text = context.resources.getString(
            when (state) {
                PermissionState.NOT_GRANTED_CAN_BE_REQUESTED -> R.string.allow_access
                else -> R.string.open_settings
            }
        )

        binding.btnAction.setOnClickListener {
            when (state) {
                PermissionState.NOT_GRANTED_CAN_BE_REQUESTED -> onRequestPermissions.invoke()
                else -> onOpenSettings.invoke()
            }
        }
    }

    fun bind(
        state: PermissionState = PermissionState.GRANTED,
        onRequestPermissions: () -> Unit,
        onOpenSettings: () -> Unit
    ) {
        this.onRequestPermissions = onRequestPermissions
        this.onOpenSettings = onOpenSettings
        updateState(state)
    }

    fun updateState(state: PermissionState) {
        this.state = state
        setupIcon()
        setupTitle()
        setupDesc()
        setupAction()
    }

}
