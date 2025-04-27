package com.numplates.nomera3.presentation.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.meera.core.common.LIGHT_STATUSBAR
import com.meera.core.extensions.clickAnimate
import com.meera.core.extensions.empty
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.utils.checkAppRedesigned
import com.meera.core.utils.getAge
import com.meera.referrals.ui.ReferralFragment
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.core.INetworkValues
import com.numplates.nomera3.databinding.FragmentProfileSettingsBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.FriendInviteTapProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.rating.AmplitudePropertyRatingWhere
import com.numplates.nomera3.modules.chat.ChatFragmentNew
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitType
import com.numplates.nomera3.modules.profilesettings.model.ProfileSettingsEffect
import com.numplates.nomera3.modules.purchase.ui.vip.FragmentUpgradeToVipNew
import com.numplates.nomera3.modules.purchase.ui.vip.UpdateStatusFragment
import com.numplates.nomera3.modules.rateus.presentation.PopUpRateUsDialogFragment
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserProfileModel
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.fragments.meerasettings.MeeraRateUsFlowController
import com.numplates.nomera3.presentation.view.fragments.privacysettings.PrivacyFragmentNew
import com.numplates.nomera3.presentation.view.utils.apphints.TooltipLifecycleAware
import com.numplates.nomera3.presentation.viewmodel.ProfileSettingsViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

/*
* макет: https://zpl.io/VKRn78E
* */


class ProfileSettingsFragmentNew : BaseFragmentNew<FragmentProfileSettingsBinding>() {

    private val profileSettingsViewModel by viewModels<ProfileSettingsViewModel> {
        App.component.getViewModelFactory()
    }

    private var scrollYPos = 0
    private var accountButtonTooltip: TooltipLifecycleAware? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupLiveObservers()
        screenScrollListener()
        accountButtonTooltip = TooltipLifecycleAware(this)
    }

    override fun onReturnTransitionFragment() {
        profileSettingsViewModel.refreshOwnUserProfile()
    }

    override fun onDestroy() {
        act.resetMap()
        super.onDestroy()
    }

    private fun setupToolbar() {
        binding?.toolbar?.setNavigationIcon(R.drawable.arrowback)
        binding?.toolbar?.setNavigationOnClickListener {
            act.onBackPressed()
        }
    }

    private fun setupLiveObservers() {
        lifecycleScope.launchWhenResumed {
            profileSettingsViewModel.getOwnProfileFlow().collect(::renderViews)
        }
        profileSettingsViewModel.profileSettingsEffectFlow
            .onEach(::handleProfileSettingsEffect)
            .launchIn(lifecycleScope)
    }

    private fun handleProfileSettingsEffect(effect: ProfileSettingsEffect) {
        when (effect) {
            is ProfileSettingsEffect.SupportUserIdFound -> {
                onActivityInteraction?.onAddFragment(
                    fragment = ChatFragmentNew(),
                    isLightStatusBar = LIGHT_STATUSBAR,
                    mapArgs = hashMapOf(
                        IArgContainer.ARG_CHAT_INIT_DATA to ChatInitData(
                            initType = ChatInitType.FROM_PROFILE,
                            userId = effect.userId
                        )
                    )
                )
            }
            else -> Unit
        }
    }

    private fun screenScrollListener() {
        val screenListener = ViewTreeObserver.OnScrollChangedListener {
            binding?.scrollContainerProfileSettings?.let { scrollView ->
                scrollView.scrollY
                    .also { scrollYPos = it }
                    .also { if (it > 4) hideAllScreenTooltip() }
            }
        }

        binding?.scrollContainerProfileSettings?.addOnAttachStateChangeListener(object :
            View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(p0: View) {
                binding?.scrollContainerProfileSettings?.viewTreeObserver?.addOnScrollChangedListener(screenListener)
            }

            override fun onViewDetachedFromWindow(p0: View) {
                binding?.scrollContainerProfileSettings?.viewTreeObserver?.removeOnScrollChangedListener(screenListener)
            }
        })
    }

    private fun hideAllScreenTooltip() {
        accountButtonTooltip?.hideTooltip()
    }

    private fun renderViews(user: UserProfileModel) {
        Timber.d("UserPROFILE: $user")
        // установить уникальное имя пользователя
        user.uniquename.let { userUniqueName: String ->
            binding?.uniqueNameTextView?.text = "@$userUniqueName"
        }

        binding?.ivAvatar?.setUp(
            act,
            user.avatarSmall,
            user.accountType ?: 0,
            user.accountColor ?: 0
        )

        // Name
        binding?.tvName?.text = user.name
        // Description
        var birthday = ""

        user.birthday?.let { birth ->
            birthday = if (user.hideBirthday == 1)
                ""
            else getAge(birth)
        }

        val city = user.coordinates?.cityName ?: run { String.empty() }
        if (birthday != "")
            birthday += ", "
        binding?.tvInfo?.text = "$birthday $city"

        // Account type
        when (user.accountType) {
            1 -> {
                binding?.tvUpgrade?.setText(R.string.upgrade_silver_vip)
                binding?.ivUpgradeImage?.setImageResource(R.drawable.crown_silver_rounded)
                binding?.ivUpgrade?.apply {
                    setOnClickListener {
                        clickAnimate()
                        binding?.ivUpgradeImage?.clickAnimate()
                        add(UpdateStatusFragment(), Act.COLOR_STATUSBAR_BLACK_NAVBAR)
                    }
                }
            }

            2 -> {
                binding?.tvUpgrade?.setText(R.string.upgrade_golden_vip)
                binding?.ivUpgradeImage?.setImageResource(R.drawable.btn_upgrade)
                binding?.ivUpgrade?.apply {
                    setOnClickListener {
                        clickAnimate()
                        binding?.ivUpgradeImage?.clickAnimate()
                        add(UpdateStatusFragment(), Act.COLOR_STATUSBAR_BLACK_NAVBAR)
                    }
                }
            }

            else -> {
                binding?.tvUpgrade?.setText(R.string.upgrade_to_vip)
                binding?.ivUpgradeImage?.setImageResource(R.drawable.btn_upgrade)
                binding?.ivUpgrade?.setOnClickListener {
                    binding?.ivUpgrade?.clickAnimate()
                    binding?.ivUpgradeImage?.clickAnimate()
                    if (user.accountType == INetworkValues.ACCOUNT_TYPE_PREMIUM
                        || user.accountType == INetworkValues.ACCOUNT_TYPE_VIP
                    ) {
                        add(UpdateStatusFragment(), Act.LIGHT_STATUSBAR)
                    } else
                        add(FragmentUpgradeToVipNew(), Act.LIGHT_STATUSBAR)

                }
            }
        }

        // Click listeners
        setupClickViews()
    }


    private fun setupClickViews() {
        // Notifications
        binding?.llNotifications?.setOnClickListener {
            add(PushNotificationsSettingsFragment(), Act.LIGHT_STATUSBAR)
        }

        // Profile info
        binding?.ivProfileInfoImage?.setOnClickListener {
            binding?.ivProfileInfoImage?.clickAnimate()
            profileSettingsViewModel.logProfileEditTap()
            val arg = Arg(IArgContainer.ARG_CALLED_FROM_PROFILE, true)
            checkAppRedesigned(
                isRedesigned = {},
                isNotRedesigned = {
                    add(UserPersonalInfoFragment(), Act.LIGHT_STATUSBAR, arg)
                }
            )
        }

        // Privacy policy
        binding?.llPrivacy?.setOnClickListener {
            checkAppRedesigned(
                isRedesigned = {},
                isNotRedesigned = { add(PrivacyFragmentNew(), Act.LIGHT_STATUSBAR) }
            )
        }

        // About
        binding?.llAbout?.setOnClickListener {
            checkAppRedesigned(
                isRedesigned = {},
                isNotRedesigned = { add(AboutFragment(), Act.COLOR_STATUSBAR_LIGHT_NAVBAR) }
            )
        }

        binding?.vgSupport?.setThrottledClickListener {
            profileSettingsViewModel.supportClicked()
        }

        // Logout
        binding?.llLogout?.setOnClickListener {
            act.logOutWithDelegate()
        }

        // Invite friends
        binding?.llInviteFriends?.setOnClickListener {
            profileSettingsViewModel.logInviteFriend(FriendInviteTapProperty.SETTINGS)
            add(ReferralFragment(), Act.LIGHT_STATUSBAR)
        }

        binding?.ivAccountImage?.setOnClickListener {
            binding?.ivAccountImage?.clickAnimate()
            profileSettingsViewModel.logInviteFriend(FriendInviteTapProperty.NAVBAR)
            add(ReferralFragment(), Act.LIGHT_STATUSBAR)
        }

        binding?.llRateUs?.setOnClickListener {
            checkAppRedesigned(
                isRedesigned = {
                    MeeraRateUsFlowController().startRateUsFlow(childFragmentManager)
                },
                isNotRedesigned = {
                    childFragmentManager.let { fm ->
                        PopUpRateUsDialogFragment.show(
                            manager = fm,
                            tag = "rateUS",
                            where = AmplitudePropertyRatingWhere.SETTINGS
                        )
                    }
                }
            )
        }
    }

    override fun onDestroyView() {
        accountButtonTooltip = null
        super.onDestroyView()
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentProfileSettingsBinding
        get() = FragmentProfileSettingsBinding::inflate

}
