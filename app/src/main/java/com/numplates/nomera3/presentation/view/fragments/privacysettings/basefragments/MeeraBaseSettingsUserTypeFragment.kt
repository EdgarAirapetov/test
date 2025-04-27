package com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.widgets.cell.CellPosition
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentBaseSettingsUserTypeBinding
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.usersettings.ui.models.PrivacySettingUiModel
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_PRIVACY_TYPE_VALUE
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.changeprofilesettings.ChangeProfileSettingsListener
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.changeprofilesettings.MeeraChangeProfileSettingsBottomSheetFragment
import timber.log.Timber

/**
 * Select kind users (everybody, friends, nobody)
 */
abstract class MeeraBaseSettingsUserTypeFragment : UiKitBottomSheetDialog<MeeraFragmentBaseSettingsUserTypeBinding>(),
    ChangeProfileSettingsListener {

    private var currentRadioPos = 0

    private var countBlacklist: Int? = null

    private var countWhitelist: Int? = null

    protected val privacyType by lazy { arguments?.getInt(ARG_PRIVACY_TYPE_VALUE) }

    protected var closedProfile: Boolean = false

    val act by lazy { requireActivity() as MeeraAct }

    abstract fun screenTitle(): String

    abstract fun settingTypeTitle(): String

    abstract fun actionDescription(): String

    abstract fun actionTransitBlacklist(userCount: Int?)

    abstract fun actionTransitWhitelist(userCount: Int?)

    abstract fun sendSettingUserType(typeEnum: SettingsUserTypeEnum)

    abstract fun refreshCounters()

    abstract fun hasBlackWhiteLists(): Boolean

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraFragmentBaseSettingsUserTypeBinding
        get() = MeeraFragmentBaseSettingsUserTypeBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        countBlacklist = arguments?.getInt(IArgContainer.ARG_COUNT_USERS_BLACKLIST)
        countWhitelist = arguments?.getInt(IArgContainer.ARG_COUNT_USERS_WHITELIST)
        closedProfile = arguments?.getBoolean(IArgContainer.ARG_PROFILE_CLOSED_VALUE) ?: false
        initBehavior()
        initRadioButtons(privacyType, countBlacklist, countWhitelist)
        initViews()

        childFragmentManager.setFragmentResultListener(
            MeeraChangeProfileSettingsBottomSheetFragment.ARG_CHANGE_PROFILE_REQUEST_KEY, viewLifecycleOwner
        ) { requestKey, bundle ->
            val changeProfile =
                bundle.getBoolean(MeeraChangeProfileSettingsBottomSheetFragment.ARG_CHANGE_PROFILE, false)
            if (changeProfile) changeProfileSettingConfirmed()
            else changeProfileSettingCanceled()

        }
    }

    override fun createDialogState() = UiKitBottomSheetDialogParams(
        needShowToolbar = false, needShowGrabberView = true, dialogStyle = R.style.BottomSheetDialogTransparentTheme
    )

    protected fun initRadioButtons(privacyType: Int?, countBlacklist: Int?, countWhitelist: Int?) {
        Timber.d("SETUP Views: priType: $privacyType countBlack:$countBlacklist countWhite:$countWhitelist")
        this.currentRadioPos = privacyType ?: 0
        when (privacyType) {
            SettingsUserTypeEnum.ALL.key -> {
                this.currentRadioPos = SettingsUserTypeEnum.ALL.key
                contentBinding?.radioGroupUsers?.check(R.id.cell_settings_user_type_everything)
                contentBinding?.cellSettingsUserTypeEverything?.setCellRightElementChecked(true)
                everythingExceptions()
            }

            SettingsUserTypeEnum.FRIENDS.key -> {
                this.currentRadioPos = SettingsUserTypeEnum.FRIENDS.key
                contentBinding?.radioGroupUsers?.check(R.id.cell_settings_user_type_friends)
                contentBinding?.cellSettingsUserTypeFriends?.setCellRightElementChecked(true)
                friendsExceptions()
            }

            SettingsUserTypeEnum.NOBODY.key -> {
                this.currentRadioPos = SettingsUserTypeEnum.NOBODY.key
                contentBinding?.radioGroupUsers?.check(R.id.cell_settings_user_type_nobody)
                contentBinding?.cellSettingsUserTypeNobody?.setCellRightElementChecked(true)
                nobodyExceptions()
            }
        }

        setAllCounters(countBlacklist, countWhitelist)
    }

    private fun setAllCounters(countBlacklist: Int?, countWhitelist: Int?) {
        contentBinding?.cellSettingsExceptionAlwaysAllow?.setRightTextboxValue(getCounts(countWhitelist))
        contentBinding?.cellSettingsExceptionNotAllow?.setRightTextboxValue(getCounts(countBlacklist))
    }

    private fun initViews() {
        contentBinding?.nvSettingsUserType?.title = screenTitle()
        contentBinding?.nvSettingsUserType?.closeButtonClickListener = { dismiss() }
        val actionDescription = actionDescription()

        if (settingTypeTitle().isNotEmpty()) {
            contentBinding?.tvSettingTypeTitle?.visible()
            contentBinding?.tvSettingTypeTitle?.text = settingTypeTitle()
        }

        if (actionDescription.isNotEmpty()) {
            contentBinding?.tvActionDescription?.visible()
            contentBinding?.tvActionDescription?.text = actionDescription
        }

        if (hasBlackWhiteLists().not()) {
            contentBinding?.tvExceptionDescription?.gone()
            contentBinding?.vgExceptions?.gone()
        }

        // Blacklist
        contentBinding?.cellSettingsExceptionNotAllow?.setRightElementContainerClickable(false)
        contentBinding?.cellSettingsExceptionNotAllow?.setThrottledClickListener {
            actionTransitBlacklist(countBlacklist)
        }
        // Whitelist
        contentBinding?.cellSettingsExceptionAlwaysAllow?.setRightElementContainerClickable(false)
        contentBinding?.cellSettingsExceptionAlwaysAllow?.setThrottledClickListener {
            actionTransitWhitelist(countWhitelist)
        }


        // RadioButton controls
        contentBinding?.radioGroupUsers?.setOnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                R.id.cell_settings_user_type_everything -> {
                    everythingExceptions()
                    sendSettingUserType(SettingsUserTypeEnum.ALL)
                }

                R.id.cell_settings_user_type_friends -> {
                    friendsExceptions()
                    sendSettingUserType(SettingsUserTypeEnum.FRIENDS)
                }

                R.id.cell_settings_user_type_nobody -> {
                    nobodyExceptions()
                    sendSettingUserType(SettingsUserTypeEnum.NOBODY)
                }
            }
        }

        contentBinding?.cellSettingsUserTypeEverything?.setRightIconClickListener {
            checkRadioButton(R.id.cell_settings_user_type_everything)
        }
        contentBinding?.cellSettingsUserTypeFriends?.setRightIconClickListener {
            checkRadioButton(R.id.cell_settings_user_type_friends)
        }
        contentBinding?.cellSettingsUserTypeNobody?.setRightIconClickListener {
            checkRadioButton(R.id.cell_settings_user_type_nobody)
        }
    }

    private fun checkRadioButton(id: Int) {
        when (id) {
            R.id.cell_settings_user_type_everything -> {
                this.currentRadioPos = SettingsUserTypeEnum.ALL.key
                contentBinding?.radioGroupUsers?.check(R.id.cell_settings_user_type_everything)
                contentBinding?.cellSettingsUserTypeFriends?.setCellRightElementChecked(false)
                contentBinding?.cellSettingsUserTypeNobody?.setCellRightElementChecked(false)
                contentBinding?.cellSettingsUserTypeEverything?.setCellRightElementChecked(true)
                everythingExceptions()
            }

            R.id.cell_settings_user_type_friends -> {
                this.currentRadioPos = SettingsUserTypeEnum.FRIENDS.key
                contentBinding?.radioGroupUsers?.check(R.id.cell_settings_user_type_friends)
                contentBinding?.cellSettingsUserTypeEverything?.setCellRightElementChecked(false)
                contentBinding?.cellSettingsUserTypeNobody?.setCellRightElementChecked(false)
                contentBinding?.cellSettingsUserTypeFriends?.setCellRightElementChecked(true)
                friendsExceptions()
            }

            R.id.cell_settings_user_type_nobody -> {
                this.currentRadioPos = SettingsUserTypeEnum.NOBODY.key
                contentBinding?.radioGroupUsers?.check(R.id.cell_settings_user_type_nobody)
                contentBinding?.cellSettingsUserTypeEverything?.setCellRightElementChecked(false)
                contentBinding?.cellSettingsUserTypeFriends?.setCellRightElementChecked(false)
                contentBinding?.cellSettingsUserTypeNobody?.setCellRightElementChecked(true)
                nobodyExceptions()
            }

        }
    }


    private fun initBehavior() {
        val dialog = dialog as BottomSheetDialog
        val mainContainer = dialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)
        mainContainer?.let { frameLayout ->
            val bottomSheetBehavior = BottomSheetBehavior.from(frameLayout)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun getCounts(count: Int?): String {
        return when (count) {
            null -> String.empty()
            in 1..Int.MAX_VALUE -> count.toString()
            else -> getString(R.string.settings_add)
        }
    }

    private fun everythingExceptions() {
        contentBinding?.cellSettingsExceptionNotAllow?.visible()
        contentBinding?.cellSettingsExceptionNotAllow?.cellPosition = CellPosition.ALONE
        contentBinding?.cellSettingsExceptionAlwaysAllow?.gone()
    }

    private fun friendsExceptions() {
        contentBinding?.cellSettingsExceptionNotAllow?.visible()
        contentBinding?.cellSettingsExceptionNotAllow?.cellPosition = CellPosition.TOP
        contentBinding?.cellSettingsExceptionAlwaysAllow?.visible()
        contentBinding?.cellSettingsExceptionAlwaysAllow?.cellPosition = CellPosition.BOTTOM
    }

    private fun nobodyExceptions() {
        contentBinding?.cellSettingsExceptionAlwaysAllow?.visible()
        contentBinding?.cellSettingsExceptionAlwaysAllow?.cellPosition = CellPosition.ALONE
        contentBinding?.cellSettingsExceptionNotAllow?.gone()
    }

    /**
     * Update counters after add / remove new users to exclusions
     */
    fun updateCounters(key: String, settings: List<PrivacySettingUiModel>) {
        settings.forEach { setting ->
            if (setting.key == key) {
                this.countBlacklist = setting.countBlacklist
                this.countWhitelist = setting.countWhitelist
                setAllCounters(setting.countBlacklist, setting.countWhitelist)
            }
        }
    }

    override fun changeProfileSettingConfirmed() {
        closedProfile = false
    }

    override fun changeProfileSettingCanceled() {
        when (privacyType) {
            SettingsUserTypeEnum.ALL.key -> checkRadioButton(R.id.cell_settings_user_type_everything)
            SettingsUserTypeEnum.FRIENDS.key -> checkRadioButton(R.id.cell_settings_user_type_friends)
            SettingsUserTypeEnum.NOBODY.key -> checkRadioButton(R.id.cell_settings_user_type_nobody)
        }
    }
}
