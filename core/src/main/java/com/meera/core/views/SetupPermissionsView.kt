package com.meera.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import com.meera.core.R
import com.meera.core.base.enums.PermissionState
import com.meera.core.extensions.textColor

class SetupPermissionsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private var view: View
    private var useDarkMode = false

    private var icon: ImageView
    private var title: TextView
    private var desc: TextView
    private var button: TextView

    private var onRequestPermissions: () -> Unit = {}
    private var onOpenSettings: () -> Unit = {}

    private var state: PermissionState = PermissionState.GRANTED

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.view_setup_permissions, this)
        icon = view.findViewById(R.id.iv_permission_icon)
        title = view.findViewById(R.id.tv_permission_title)
        desc = view.findViewById(R.id.tv_permission_desc)
        button = view.findViewById(R.id.tv_permission_action_view)
        val llRoot = view.findViewById<LinearLayout>(R.id.ll_permission_root)
        llRoot.setOnTouchListener { view, _ ->
            view.parent.requestDisallowInterceptTouchEvent(false)
            false
        }
    }

    fun bind(
        useDarkMode: Boolean = false,
        state: PermissionState = PermissionState.GRANTED,
        onRequestPermissions: () -> Unit,
        onOpenSettings: () -> Unit
    ) {
        this.useDarkMode = useDarkMode
        this.onRequestPermissions = onRequestPermissions
        this.onOpenSettings = onOpenSettings

        if (!useDarkMode) view.setBackgroundResource(R.color.colorWhite)

        updateState(state)
    }

    fun setContentTopMargin(topMargin: Int) {
        val lp = icon.layoutParams as LayoutParams
        lp.setMargins(icon.marginLeft, topMargin, icon.marginRight, icon.marginBottom)
        icon.layoutParams = lp
    }

    fun updateState(state: PermissionState) {
        this.state = state

        setupIcon()
        setupTitle()
        setupDesc()
        setupAction()
    }

    private fun setupIcon() {
        icon.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                when (state) {
                    PermissionState.NOT_GRANTED_CAN_BE_REQUESTED -> R.drawable.ic_permission_request
                    else -> R.drawable.ic_permission_settings
                }
            )
        )
    }

    private fun setupTitle() {
        title.text = context.resources.getString(
            when (state) {
                PermissionState.NOT_GRANTED_CAN_BE_REQUESTED -> R.string.allow_access_to_photo
                else -> R.string.photos_not_showing
            }
        )
        if (useDarkMode) title.textColor(R.color.ui_white)
    }

    private fun setupDesc() {
        desc.text = context.resources.getString(
            when (state) {
                PermissionState.NOT_GRANTED_CAN_BE_REQUESTED -> R.string.allow_access_to_photo_desc
                else -> R.string.photos_not_showing_desc
            }
        )
    }

    private fun setupAction() {
        button.text = context.resources.getString(
            when (state) {
                PermissionState.NOT_GRANTED_CAN_BE_REQUESTED -> R.string.allow_access
                else -> R.string.open_settings
            }
        )

        button.setOnClickListener {
            when (state) {
                PermissionState.NOT_GRANTED_CAN_BE_REQUESTED -> onRequestPermissions.invoke()
                else -> onOpenSettings.invoke()
            }
        }
    }
}
