package com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.appbar.AppBarLayout
import com.meera.core.extensions.empty
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentBaseSettingsUserTypeBinding
import com.numplates.nomera3.modules.usersettings.ui.models.PrivacySettingUiModel
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_PRIVACY_TYPE_VALUE
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.changeprofilesettings.ChangeProfileSettingsListener
import timber.log.Timber

/**
 * Select kind users (everybody, friends, nobody)
 */
abstract class BaseSettingsUserTypeFragment : BaseFragmentNew<FragmentBaseSettingsUserTypeBinding>(),
    ChangeProfileSettingsListener {

    private var currentRadioPos = 0

    private var countBlacklist: Int? = null

    private var countWhitelist: Int? = null

    protected val privacyType by lazy { arguments?.getInt(ARG_PRIVACY_TYPE_VALUE) }

    protected var closedProfile: Boolean = false

    abstract fun screenTitle(): String

    abstract fun settingTypeTitle(): String

    abstract fun actionDescription(): String

    abstract fun actionTransitBlacklist(userCount: Int?)

    abstract fun actionTransitWhitelist(userCount: Int?)

    abstract fun sendSettingUserType(typeEnum: SettingsUserTypeEnum)

    abstract fun refreshCounters()

    abstract fun hasBlackWhiteLists(): Boolean

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentBaseSettingsUserTypeBinding
        get() = FragmentBaseSettingsUserTypeBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        countBlacklist = arguments?.getInt(IArgContainer.ARG_COUNT_USERS_BLACKLIST)
        countWhitelist = arguments?.getInt(IArgContainer.ARG_COUNT_USERS_WHITELIST)
        closedProfile = arguments?.getBoolean(IArgContainer.ARG_PROFILE_CLOSED_VALUE) ?: false

        setupToolbar()
        initRadioButtons(privacyType, countBlacklist, countWhitelist)
        initViews()
    }

    override fun onReturnTransitionFragment() {
        super.onReturnTransitionFragment()
        refreshCounters()
    }

    private fun initRadioButtons(privacyType: Int?, countBlacklist: Int?, countWhitelist: Int?) {
        Timber.e("SETUP Views: priType: $privacyType countBlack:$countBlacklist countWhite:$countWhitelist")
        this.currentRadioPos = privacyType ?: 0
        when (privacyType) {
            SettingsUserTypeEnum.ALL.key -> {
                this.currentRadioPos = SettingsUserTypeEnum.ALL.key
                binding?.radioGroupUsers?.check(R.id.rb_everything)
                binding?.rbEverything?.isChecked = true
                everythingExceptions()
            }
            SettingsUserTypeEnum.FRIENDS.key -> {
                this.currentRadioPos = SettingsUserTypeEnum.FRIENDS.key
                binding?.radioGroupUsers?.check(R.id.rb_friends)
                binding?.rbFriends?.isChecked = true
                friendsExceptions()
            }
            SettingsUserTypeEnum.NOBODY.key -> {
                this.currentRadioPos = SettingsUserTypeEnum.NOBODY.key
                binding?.radioGroupUsers?.check(R.id.rb_nobody)
                binding?.rbNobody?.isChecked = true
                nobodyExceptions()
            }
        }

        setAllCounters(countBlacklist, countWhitelist)
    }


    private fun setAllCounters(countBlacklist: Int?, countWhitelist: Int?) {
        binding?.tvAlwaysAllow?.let {
            handleCounts(countWhitelist, it)
        }

        binding?.tvNotAllow?.let {
            handleCounts(countBlacklist, it)
        }
    }


    private fun initViews() {
        binding?.tvToolbarTitle?.text = screenTitle()

        val actionDescription = actionDescription()

        if (settingTypeTitle().isNotEmpty()) {
            binding?.tvSettingTypeTitle?.visible()
            binding?.tvSettingTypeTitle?.text = settingTypeTitle()
        }

        if (actionDescription.isNotEmpty()) {
            binding?.tvActionDescription?.visible()
            binding?.tvActionDescription?.text = actionDescription
        }

        if (hasBlackWhiteLists().not()) {
            binding?.tvExceptionDescription?.gone()
            binding?.vgExceptions?.gone()
        }

        // Blacklist
        binding?.containerNotAllow?.setOnClickListener {
            actionTransitBlacklist(countBlacklist)
        }
        // Whitelist
        binding?.containerAlwaysAllow?.setOnClickListener {
            actionTransitWhitelist(countWhitelist)
        }


        // RadioButton controls
        binding?.radioGroupUsers?.setOnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                R.id.rb_everything -> {
                    everythingExceptions()
                    sendSettingUserType(SettingsUserTypeEnum.ALL)
                }
                R.id.rb_friends -> {
                    friendsExceptions()
                    sendSettingUserType(SettingsUserTypeEnum.FRIENDS)
                }
                R.id.rb_nobody -> {
                    nobodyExceptions()
                    sendSettingUserType(SettingsUserTypeEnum.NOBODY)
                }
            }
        }
    }


    private fun setupToolbar() {
        val params = binding?.statusBarSettingsUserType?.layoutParams as AppBarLayout.LayoutParams
        params.height = context.getStatusBarHeight()
        binding?.statusBarSettingsUserType?.layoutParams = params
        binding?.toolbar?.setNavigationIcon(R.drawable.arrowback)
        binding?.toolbar?.setNavigationOnClickListener { act.onBackPressed() }
    }


    private fun handleCounts(count: Int?, textView: TextView) {
        textView.text = when (count) {
            null -> String.empty()
            in 1..Int.MAX_VALUE -> count.toString()
            else -> getString(R.string.settings_add)
        }
    }

    private fun everythingExceptions() {
        binding?.containerNotAllow?.visible()
        binding?.containerAlwaysAllow?.gone()
        binding?.notAllowDivider?.gone()
    }

    private fun friendsExceptions() {
        binding?.containerNotAllow?.visible()
        binding?.containerAlwaysAllow?.visible()
        binding?.notAllowDivider?.visible()
    }

    private fun nobodyExceptions() {
        binding?.containerAlwaysAllow?.visible()
        binding?.containerNotAllow?.gone()
        binding?.notAllowDivider?.gone()
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
            SettingsUserTypeEnum.ALL.key -> {
                binding?.radioGroupUsers?.check(R.id.rb_everything)
                binding?.rbEverything?.isChecked = true
            }
            SettingsUserTypeEnum.FRIENDS.key -> {
                binding?.radioGroupUsers?.check(R.id.rb_friends)
                binding?.rbFriends?.isChecked = true
            }
            SettingsUserTypeEnum.NOBODY.key -> {
                binding?.radioGroupUsers?.check(R.id.rb_nobody)
                binding?.rbNobody?.isChecked = true
            }
        }
    }

}
