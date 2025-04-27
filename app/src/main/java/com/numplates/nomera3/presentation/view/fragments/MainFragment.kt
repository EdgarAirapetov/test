package com.numplates.nomera3.presentation.view.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.meera.core.base.BaseFragment
import com.meera.core.extensions.isLocationPermitted
import com.meera.core.extensions.notificationsPermitted
import com.meera.core.extensions.register
import com.meera.core.extensions.simpleName
import com.meera.core.utils.checkAppRedesigned
import com.meera.core.views.NavigationBarViewContract
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.BuildConfig
import com.numplates.nomera3.R
import com.numplates.nomera3.data.newmessenger.ROOM_TYPE_DIALOG
import com.numplates.nomera3.databinding.FragmentPostRoadsLayoutBinding
import com.numplates.nomera3.modules.appInfo.ui.ForceUpdateDialog
import com.numplates.nomera3.modules.appInfo.ui.OnDismissListener
import com.numplates.nomera3.modules.appInfo.ui.entity.ForceUpdateDialogEntity
import com.numplates.nomera3.modules.auth.AuthStatus
import com.numplates.nomera3.modules.auth.ui.AuthViewModel
import com.numplates.nomera3.modules.auth.util.needAuth
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleWhich
import com.numplates.nomera3.modules.chat.ChatFragmentNew
import com.numplates.nomera3.modules.holidays.ui.calendar.HolidayCalendarBottomDialog
import com.numplates.nomera3.modules.holidays.ui.calendar.MeeraHolidayCalendarBottomDialogBuilder
import com.numplates.nomera3.modules.holidays.ui.entity.HolidayVisits
import com.numplates.nomera3.modules.maps.domain.model.isDark
import com.numplates.nomera3.modules.newroads.MainPostRoadsFragment
import com.numplates.nomera3.modules.newroads.fragments.BaseRoadsFragment
import com.numplates.nomera3.modules.newroads.ui.entity.MainRoadMode
import com.numplates.nomera3.modules.peoples.ui.fragments.PeoplesCommunitiesContainerFragment
import com.numplates.nomera3.modules.peoples.ui.utils.PeopleCommunitiesNavigator
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserProfileModel
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_ROOM_FROM_PUSH
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_ROOM_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_ROOM_TYPE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_SHOW_BIRTHDAY
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.numplates.nomera3.presentation.viewmodel.MainFragmentViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.MainFragmentEvents
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.LinkedList
import kotlin.properties.Delegates

private const val TAG_FRAGMENT_MANAGER = "FM_MainFragment"

class MainFragment : BaseFragment(), NavigationBarViewContract.NavigatonBarListener, IOnBackPressed {

    private var act by Delegates.notNull<Act>()

    var postListFragment: BaseFragmentNew<FragmentPostRoadsLayoutBinding>? = null
    var roomsFragment: BaseFragment? = null
    var mapFragment: BaseFragment? = null
    var peoplesFragment: BaseFragment? = null
    var userInfoFragment: BaseFragment? = null
    var currentFragment: BaseFragment? = null

    var callsDialogIsShowing = false
    var registerFragmentOpened = false
    var dialogOpened = false

    val dialogsToShow = LinkedList<AppCompatDialogFragment>()

    private val intentFilterConnectivityChange =
        IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
    private val authViewModel by viewModels<AuthViewModel>(ownerProducer = { requireActivity() })
    private val mainFragmentViewModel by viewModels<MainFragmentViewModel>()
    private val mainContainerFragmentId = R.id.fragment_container1
    private val fm: FragmentManager
        get() = childFragmentManager

    private val internetConnectionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            mainFragmentViewModel.onInternetConnectivityChanged()
        }
    }

    private val onDialogsDismissListener = object : OnDismissListener {
        override fun onDismiss() {
            if (dialogsToShow.isNotEmpty()) {
                showNextDialog()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
        act = activity as Act

        if (savedInstanceState == null) {
            if (postListFragment == null) {
                postListFragment = MainPostRoadsFragment.newInstance(MainRoadMode.POSTS)
            }

            fm.beginTransaction()
                .add(
                    mainContainerFragmentId,
                    requireNotNull(postListFragment) { "Please, make sure fragment was initialized." },
                    MainPostRoadsFragment::class.java.name,
                )
                .setPrimaryNavigationFragment(postListFragment)
                .runOnCommit { currentFragment = postListFragment }
                .commitNow()

            setStatusBar()
        } else {
            postListFragment =
                fm.findFragmentByTag(MainPostRoadsFragment::class.java.name) as? MainPostRoadsFragment
            roomsFragment =
                fm.findFragmentByTag(RoomsContainerFragment::class.java.name) as? RoomsContainerFragment
            mapFragment =
                fm.findFragmentByTag(MapFragment::class.java.name) as? MapFragment
            peoplesFragment =
                fm.findFragmentByTag(PeoplesCommunitiesContainerFragment::class.java.name) as? PeoplesCommunitiesContainerFragment
            userInfoFragment =
                fm.findFragmentByTag(UserInfoFragment::class.java.name) as? UserInfoFragment

            if (fm.primaryNavigationFragment is BaseFragment) {
                currentFragment = fm.primaryNavigationFragment as BaseFragment
            }
        }

        mainFragmentViewModel.writeFirstLogin()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.fragment_main, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainFragmentViewModel.migrateOldToken()
        initObservers()
        mainFragmentViewModel.requestAppInfo()
        mainFragmentViewModel.savePeopleContent()

        val selectedFragment = arguments?.getString(IArgContainer.ARG_SELECT_ON_MAIN_FRAGMENT)
        val isNeedShowBirthdayDialog = arguments?.getBoolean(ARG_SHOW_BIRTHDAY) ?: false
        if (selectedFragment != null) {
            when (selectedFragment) {
                IArgContainer.MAP_FRAGMENT -> onClickMap()
                IArgContainer.PEOPLES_FRAGMENT -> onClickPeoples(arguments?.getLong(ARG_USER_ID))
                IArgContainer.ROAD_FRAGMENT -> onClickRoad()
                IArgContainer.ROOMS_FRAGMENT -> onClickChat()
                IArgContainer.MESSAGES_FRAGMENT -> onClickMessages(arguments?.getLong(ARG_ROOM_ID))
                IArgContainer.USER_INFO_FRAGMENT -> onClickProfile()
            }
            if (isNeedShowBirthdayDialog && mainFragmentViewModel.isUserAuthorized()) {
                act?.showBirthdayDialogDelayed()
            }
            arguments?.clear()
        } else {
            onClickRoad(true)
        }
    }

    fun selectGroups(where: AmplitudePeopleWhereProperty = AmplitudePeopleWhereProperty.OTHER) = needAuth {
        if (act.getCurrentFragmentNullable() is PeoplesCommunitiesContainerFragment) {
            act.getCurrentFragment().updateScreenOnTapNavBar()
        }
        if (peoplesFragment == null) peoplesFragment = PeoplesCommunitiesContainerFragment()
        if (currentFragment != peoplesFragment) {
            mainFragmentViewModel.logPeopleTabBarOpened(
                where = where,
                which = AmplitudePeopleWhich.COMMUNITY
            )
        }
        commitFragmentTransaction(peoplesFragment ?: return@needAuth) {
            act.setCurrentFragment()
            (peoplesFragment as? PeopleCommunitiesNavigator)?.selectCommunities()
        }
        act.setLightStatusBar()
        act.changeStatusBarState(Act.LIGHT_STATUSBAR)
    }

    fun requestAppInfo() {
        mainFragmentViewModel.requestAppInfo()
    }

    private fun initObservers() {
        mainFragmentViewModel.liveEvents.observe(viewLifecycleOwner) { event ->
            when (event) {
                is MainFragmentEvents.AppSettingsRequestFinished -> {
                    authViewModel.getUserProfileLive()
                        .observe(viewLifecycleOwner) {
                            it?.let {
                                openRecoveryProfileFragment(it)

                                if (!it.isProfileFilled &&
                                    it.profileDeleted != 1 &&
                                    !act.getAuthenticationNavigator().isAuthScreenOpen()
                                ) {
                                    Timber.e("User NOT REGISTERED")

                                    mainFragmentViewModel.setUserRegistered(false)
                                    mainFragmentViewModel.setIsHolidayShowNeeded(false)
                                    mainFragmentViewModel.setIsUserRegistered(false)
                                    if (!registerFragmentOpened) {
                                        act.getAuthenticationNavigator().navigateToPersonalInfo()
                                        registerFragmentOpened = true
                                        dialogOpened = true
                                    }
                                } else {
                                    Timber.e("USER Registered")

                                    val registered = !act.getAuthenticationNavigator().isAuthScreenOpen()
                                    mainFragmentViewModel.setUserRegistered(registered)
                                    mainFragmentViewModel.setIsUserRegistered(registered)
                                    mainFragmentViewModel.savePreferencesBirthdayFlag(it.birthday)

                                    if (!dialogOpened) dialogOpened = true

                                    postListFragment?.onAppSettingsRequestFinished?.call()
                                }
                            }
                        }
                    act?.triggerDialogToShow()
                }

                is MainFragmentEvents.UpdateScreenEvent -> checkAppVersions(event)
                is MainFragmentEvents.ForceUpdateEvent -> {
                    checkAppRedesigned(
                        isRedesigned = {
                            showForceUpdateFragment(event.data)
                        },
                        isNotRedesigned = {
                            showForceUpdateDialog(event.data)
                        }
                    )
                }
                is MainFragmentEvents.RegisterInternetObserver -> registerInternetObserver()
                else -> {}
            }
        }

        authViewModel.authStatus.observe(viewLifecycleOwner) { authStatus ->
            requireContext().apply {
                val isGeoEnabled = isLocationPermitted()
                val isPushPermitted = NotificationManagerCompat.from(this).notificationsPermitted()
                mainFragmentViewModel.logUserAnalytics(authStatus, isGeoEnabled, isPushPermitted)
            }
            when (authStatus) {
                is AuthStatus.Authorized -> {
                    mainFragmentViewModel.requestAppInfo()
                    act.triggerDialogToShow()
                    mainFragmentViewModel.savePeopleContent()
                }

                else -> {}
            }
        }

        lifecycleScope.launch {
            act.activityViewModel.holidaysFlow.collect {
                showHolidayCalendar(it)
            }
        }
    }

    private fun openRecoveryProfileFragment(userProfileModel: UserProfileModel) {
        if (checkExistenceProfileAndOpenFragment(userProfileModel)) {
            checkAppRedesigned(
                isRedesigned = {
//                    act.addFragment(
//                        MeeraRecoveryProfileFragment(),
//                        Act.LIGHT_STATUSBAR,
//                    )
                },
                isNotRedesigned = {
                    act.addFragment(
                        RecoveryProfileFragment(),
                        Act.LIGHT_STATUSBAR,
                        Arg(IArgContainer.ARG_TIME_MILLS, userProfileModel.deletedAt)
                    )
                }
            )
        }
    }

    private fun checkExistenceProfileAndOpenFragment(userProfileModel: UserProfileModel): Boolean {
        return userProfileModel.profileDeleted == 1
//            && act.getCurrentFragment() !is
//            MeeraRecoveryProfileFragment
            && act.getCurrentFragment() !is
            RecoveryProfileFragment
    }

    private fun registerInternetObserver() {
        try {
            unregisterInternetObserver()
            internetConnectionReceiver.register(
                context = act,
                filter = intentFilterConnectivityChange
            )
        } catch (e: Exception) {
            Timber.d(e)
        }
    }

    private fun unregisterInternetObserver() {
        try {
            act.unregisterReceiver(internetConnectionReceiver)
        } catch (e: IllegalArgumentException) {
            Timber.e("Receiver not registered $e")
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun showForceUpdateDialog(data: ForceUpdateDialogEntity?) {
        if (act.getAuthenticationNavigator().isAuthorized()
            && BuildConfig.VERSION_NAME != data?.appVersion
        ) {
            val dialog = ForceUpdateDialog.newInstance(data)
            dialog.onDismissListener = onDialogsDismissListener
            dialogsToShow.add(0, dialog)
            unregisterInternetObserver()
        }
        showNextDialog()
    }

    private fun showForceUpdateFragment(data: ForceUpdateDialogEntity?) {
        if (BuildConfig.VERSION_NAME != data?.appVersion) {
//            act.addFragment(
//                MeeraForceUpdateFragment(),
//                Act.LIGHT_STATUSBAR,
//                Arg(ForceUpdateDialog.FORCE_UPDATE_DIALOG_PARAMS, data)
//            )
        }
    }

    private fun showNextDialog() {
        if (dialogsToShow.isNotEmpty()) {
            val dialog = dialogsToShow.pop()
            if (fm.findFragmentByTag(dialog.javaClass.simpleName) == null) {
                showDialog(dialog)
            }
        }
    }

    private fun showDialog(dialog: DialogFragment) {
        val transaction = fm.beginTransaction()
        transaction.add(dialog, dialog.javaClass.simpleName)
        transaction.commitAllowingStateLoss()
    }

    /**
     * Show update dialog (when user is not updated yet)
     * Временно отключил показ UpdateDialog
     * */
    private fun showUpdateDialog(event: MainFragmentEvents.UpdateScreenEvent) {
        //val callDialog = UpdateFragment()
        //callDialog.setAppInfo(CurrentInfo(event.infos, event.appVerName), false)

        fm.let {
            event.appVerName?.let { // if appVerName != null write it to the pref
                mainFragmentViewModel.setWriteShownUpdatedDialog(false)
                mainFragmentViewModel.setAppVersionName(event.appVerName ?: "")
            }
            //callDialog.show(it, "update screen")
        }
    }

    /**
     * Show updateD dialog (when user is already updated it shows one time)
     * */
    private fun showUpdatedDialog(event: MainFragmentEvents.UpdateScreenEvent) {
        //val callDialog = UpdateFragment()
        //callDialog.setAppInfo(CurrentInfo(event.infos, event.appVerName), true)

        fm.let {
            event.appVerName?.let { // if appVerName != null write it to the pref
                mainFragmentViewModel.setWriteShownUpdatedDialog(true)
            }

            //callDialog.show(it, "update screen")
        }
    }

    /**
     * Compares two string with app versions splits it with "."
     * and decides if it worth to show update dialog
     * */
    // TODO: 1/13/21 replace with extention String.needToUpdateStr
    private fun checkAppVersions(event: MainFragmentEvents.UpdateScreenEvent) {
        var currentAppVersion = BuildConfig.VERSION_NAME
        val serverVersion = event.appVerName
        act?.serverAppVersionName = event.appVerName
        act?.handleAppVersion()
        val skippedVersion = mainFragmentViewModel.getAppVersion()

        if (skippedVersion != "") currentAppVersion = skippedVersion

        //if has empty string dont show dialog
        if (currentAppVersion.isEmpty() || serverVersion.isNullOrEmpty()) return

        // split version
        var current = currentAppVersion.split(".")
        var server = serverVersion.split(".")

        Timber.d("CURRENT VERSION = $current, server = $server")

        // if we dont have current or server version dont show dialog
        if (current.isNullOrEmpty() || server.isNullOrEmpty()) {
            try {
                if (current.isNullOrEmpty()) {
                    val intCurrent = currentAppVersion.toInt()
                    current = listOf(intCurrent.toString())
                }
                if (server.isNullOrEmpty()) {
                    val intServer = serverVersion.toInt()
                    server = listOf(intServer.toString())
                }
            } catch (e: Exception) {
                Timber.e(e)
                return
            }
        }

        //if appver length > server length it might be smth went wrong on server
        var indices = current.size

        if (current.size > server.size) indices = server.size
        else if (current.size < server.size) indices = current.size

        try {
            // checking each param with each
            for (i in 0 until indices) {
                //if current less then server show dialog
                if (current[i].toInt() < server[i].toInt()) {
                    showUpdateDialog(event)
                    return
                } else if (current[i].toInt() > server[i].toInt()) return
            }

            // server version higher then current
            if (current.size < server.size) {
                showUpdateDialog(event)
                return
                //return if our size
            } else if (current.size > server.size) return

        } catch (e: Exception) {
            Timber.e(e)
            return
        }

        // if we are here it means, that appVersion and server version are the same
        // and also we need to check if updatedDialog wasShown doing nothing else we should once show it

        if (!mainFragmentViewModel.getIsShownUpdateDialog() && serverVersion == BuildConfig.VERSION_NAME) {
            showUpdatedDialog(event)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        act = context as Act
    }

    override fun onDetach() {
        super.onDetach()
    }

    private fun commitFragmentTransaction(fragment: BaseFragment, onCommit: () -> Unit = {}) {
        Timber.tag(TAG_FRAGMENT_MANAGER).d("Current fragment is ${currentFragment?.simpleName}.")
        if (fragment == currentFragment) return
        if (fm.isDestroyed) return

        fm.executePendingTransactions()

        Timber.tag(TAG_FRAGMENT_MANAGER).d("Begin transaction for ${fragment.simpleName}.")
        val transaction = fm.beginTransaction()

        currentFragment?.let { currentFragment ->
            if (currentFragment.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                Timber.tag(TAG_FRAGMENT_MANAGER)
                    .d("Hide ${fragment.simpleName} and call ON_STOP for ${currentFragment.simpleName}.")
                currentFragment.onStopFragment()
            }
            transaction.hide(currentFragment)
        }

        if (!fragment.isAdded) {
            Timber.tag(TAG_FRAGMENT_MANAGER).d("Adding ${fragment.simpleName} to fragment manager.")
            transaction.add(mainContainerFragmentId, fragment, fragment::class.java.name)
        }

        Timber.tag(TAG_FRAGMENT_MANAGER).d("Pre-commit ${fragment.simpleName} to fragment manager.")
        currentFragment = fragment
        transaction
            .attach(fragment)
            .show(fragment)
            .disallowAddToBackStack()
            .runOnCommit {
                onCommit()
                fragment.onStartFragment()
            }
            .commit()
        updateShakeState(fragment)
    }

    override fun onClickEvent() {
        if (roomsFragment == null) {
            roomsFragment = RoomsContainerFragment.newInstance(true) // todo
        }
        roomsFragment?.let {
            commitFragmentTransaction(it) {
                act?.setCurrentFragment()
            }
        }

        val fragm =
            roomsFragment as? RoomsContainerFragment // Update counter every time when click notification
        fragm?.goToEvents()

        act.setLightStatusBar()
        act.changeStatusBarState(Act.LIGHT_STATUSBAR)
    }

    override fun onClickPeoples(userIdFromPush: Long?) {
        selectPeople(AmplitudePeopleWhereProperty.TAB_BAR, userIdFromPush)
    }

    override fun onClickChat() = needAuth {
        if (act?.getCurrentFragment() is RoomsContainerFragment) {
            act?.getCurrentFragment()?.updateScreenOnTapNavBar()
        }

        if (roomsFragment == null) roomsFragment = RoomsContainerFragment() // todo
        if (currentFragment != roomsFragment) mainFragmentViewModel.openChat()

        roomsFragment?.let {
            commitFragmentTransaction(it) {
                act?.setCurrentFragment()
            }
        }
        act?.setLightStatusBar()
        act?.changeStatusBarState(Act.LIGHT_STATUSBAR)
    }

    /**
     * Переход на экран сообщений в чате с сохранением
     * стека переходов, для того чтобы работал onBackPress()
     */
    override fun onClickMessages(roomId: Long?) = needAuth {
        if (roomsFragment == null) roomsFragment = RoomsContainerFragment() // todo
        roomsFragment?.let {
            commitFragmentTransaction(it) {
                act?.setCurrentFragment()
            }
        }

        act?.setLightStatusBar()
        act?.changeStatusBarState(Act.LIGHT_STATUSBAR)

        act.addFragment(
            ChatFragmentNew(),
            Act.LIGHT_STATUSBAR,
            Arg(ARG_ROOM_TYPE, ROOM_TYPE_DIALOG),
            Arg(ARG_ROOM_ID, roomId),
            Arg(ARG_ROOM_FROM_PUSH, true)
        )
    }


    override fun onClickRoad(isLaunchedAutomatically: Boolean) {
        openMainRoad(MainRoadMode.POSTS, isLaunchedAutomatically)
    }

    private fun openMainRoad(mainRoadMode: MainRoadMode, isLaunchedAutomatically: Boolean = false) {
        if (!isAdded) return

        if (postListFragment == null) {
            postListFragment = MainPostRoadsFragment.newInstance(mainRoadMode)
        }
        val previousFragment = currentFragment
        postListFragment?.let {
            commitFragmentTransaction(it) {
                act?.setCurrentFragment()
                (roomsFragment as? RoomsContainerFragment)?.resetUserSearch()
            }
        }
        val isPreviousFragmentEqualsCurrent = previousFragment == currentFragment
        var isPreviousMainRoadTypeEqualsCurrent = true

        if (isPreviousFragmentEqualsCurrent && previousFragment is MainPostRoadsFragment) {
            if (previousFragment.getMode() != mainRoadMode) {
                isPreviousMainRoadTypeEqualsCurrent = false
            }
        }

        if (!isPreviousFragmentEqualsCurrent || !isPreviousMainRoadTypeEqualsCurrent) {
            val currentTab = (postListFragment as? MainPostRoadsFragment)?.requestCurrentFragmentEnum()
            mainFragmentViewModel.openRoad(currentTab, isLaunchedAutomatically)
        }

        if (currentFragment == postListFragment
            && mainRoadMode == MainRoadMode.POSTS
            && (postListFragment as? MainPostRoadsFragment)?.getMode() == MainRoadMode.POSTS
        ) {
            (postListFragment as? MainPostRoadsFragment)?.actionsIfTabAlreadySelected(isPreviousFragmentEqualsCurrent)
        }

        if (isLaunchedAutomatically) {
            mainFragmentViewModel.openRoad(BaseRoadsFragment.RoadTypeEnum.MAIN_ROAD, true)
        }

        currentFragment ?: return

        (postListFragment as? MainPostRoadsFragment)?.setMode(mainRoadMode)
    }

    /**
     * Вызывается из экрана профиль, для перехода на экран карта (которая не на главной дороге, а
     * открывается через нижнее меню. Можно было бы вынести в класс ACT, пока мешается метод
     * commitFragmentTransaction
     * */
    fun openMainMapScreen() = onClickMap()

    override fun onClickMap() {
        if (currentFragment is MainPostRoadsFragment) {
            (currentFragment as? MainPostRoadsFragment)?.setMode(MainRoadMode.MAP)
        } else {
            openMainRoad(MainRoadMode.MAP)
        }
    }

    fun showMainRoadMap() {
        (postListFragment as? MainPostRoadsFragment)?.setNavbarVisible(true)
        openMainRoad(MainRoadMode.MAP)
    }

    override fun onClickProfile() = needAuth {
        if (act.getCurrentFragment() is UserInfoFragment) {
            act.getCurrentFragment().updateScreenOnTapNavBar()
        }

        if (userInfoFragment == null) {
            userInfoFragment = UserInfoFragment()
        }

        userInfoFragment?.arguments =
            act.getBundle(Arg(ARG_USER_ID, authViewModel.getOwnUserId()))

        userInfoFragment?.let {
            commitFragmentTransaction(it) {
                act?.setCurrentFragment()
                (roomsFragment as? RoomsContainerFragment)?.resetUserSearch()
            }
        }
        if (currentFragment != userInfoFragment) mainFragmentViewModel.openProfile()

        if ((userInfoFragment as? UserInfoFragment)?.appbarProfileIsLifted() == false) {
            act.setColorStatusBarNavLight()
            act.changeStatusBarState(Act.COLOR_STATUSBAR_LIGHT_NAVBAR)
        }
    }

    fun hideRoadHints() {
        (postListFragment as? MainPostRoadsFragment)?.onHideHints()
    }

    fun selectPeople(amplitudePeopleWhere: AmplitudePeopleWhereProperty, userIdFromPush: Long? = null) = needAuth {
        if (act.getCurrentFragmentNullable() is PeoplesCommunitiesContainerFragment) {
            act.getCurrentFragment().updateScreenOnTapNavBar()
        }
        if (peoplesFragment == null) peoplesFragment = PeoplesCommunitiesContainerFragment().apply {
            arguments = bundleOf(ARG_USER_ID to userIdFromPush)
        }
        if (currentFragment != peoplesFragment) {
            mainFragmentViewModel.logPeopleTabBarOpened(
                which = AmplitudePeopleWhich.PEOPLE,
                where = amplitudePeopleWhere
            )
        }
        commitFragmentTransaction(peoplesFragment ?: return@needAuth) {
            act.setCurrentFragment()
            handleIsNeedToSelectPeople()
            (roomsFragment as? RoomsContainerFragment)?.resetUserSearch()
        }
        act.setLightStatusBar()
        act.changeStatusBarState(Act.LIGHT_STATUSBAR)
    }

    fun resetMap() {
        (postListFragment as? MainPostRoadsFragment)?.resetMap()
        if (mapFragment != null) (mapFragment as? MapFragment)?.resetGlobalMap()
    }

    /**
     * Refresh root fragments
     */
    override fun onReturnTransitionFragment() {
        super.onReturnTransitionFragment()
        Timber.e("REFRESH MainFrag")
        roomsFragment?.let {
            (roomsFragment as? RoomsContainerFragment)?.onReturnTransitionFragment()
        }
    }

    override fun onStart() {
        super.onStart()
        act?.triggerDialogToShow()

        mainFragmentViewModel.syncNotificationService.startListening()
    }

    override fun onStop() {
        super.onStop()
        mainFragmentViewModel.syncNotificationService.stopListening()
        unregisterInternetObserver()
    }

    override fun onBackPressed(): Boolean =
        (postListFragment is IOnBackPressed && (postListFragment as IOnBackPressed).onBackPressed()
            || mapFragment is IOnBackPressed && (mapFragment as IOnBackPressed).onBackPressed())

    fun getCurrentFragmentIntoMain(): BaseFragment? = currentFragment

    private fun showHolidayCalendar(visits: HolidayVisits) {
        if (dialogsToShow.find { it is HolidayCalendarBottomDialog } == null) {
            checkAppRedesigned(
                isRedesigned = {
                    MeeraHolidayCalendarBottomDialogBuilder()
                        .setVisits(visits)
                        .setShowGiftBtnClickListener {
                            act.activityViewModel.calendarShown()
                        }
                        .setLongLiveLollipopsBtnClickListener {
                            onDialogsDismissListener
                        }
                        .show(parentFragmentManager)
                },
                isNotRedesigned = {
                    val dialog = HolidayCalendarBottomDialog().apply {
                        onDismissListener = onDialogsDismissListener
                        onShowListener = { act.activityViewModel.calendarShown() }
                        checkAppRedesigned(
                            isRedesigned = {
//                                onOpenGiftsListener = { add(
//                                    MeeraUserGiftsFragment(),
//                                    Act.LIGHT_STATUSBAR_NOT_TRANSPARENT) }
                            },
                            isNotRedesigned = {
                                onOpenGiftsListener = { add(
                                    UserGiftsFragment(),
                                    Act.LIGHT_STATUSBAR) }
                            }
                        )
                        setVisits(visits)
                    }
                    dialogsToShow.add(dialog)
                }
            )
        }
        showNextDialog()
    }

    private fun setStatusBar() {
        act.apply {
            if (mainFragmentViewModel.mapSettings.mapMode.isDark()) {
                setColorStatusBarNavLight()
                changeStatusBarState(Act.COLOR_STATUSBAR_LIGHT_NAVBAR)
            } else {
                setLightStatusBar()
                changeStatusBarState(Act.LIGHT_STATUSBAR)
            }
        }
    }

    private fun handleIsNeedToSelectPeople() {
        val peopleCommunitiesNavigator = peoplesFragment as? PeopleCommunitiesNavigator ?: return
        if (peopleCommunitiesNavigator.isCommunitySelected()) {
            peopleCommunitiesNavigator.selectPeople()
        }
    }

    private fun updateShakeState(currentFragment: BaseFragment) {
        if (currentFragment !is MainPostRoadsFragment) {
            mainFragmentViewModel.tryToRegisterShakeEvent()
        } else {
            (postListFragment as? MainPostRoadsFragment)?.updateShakeState()
        }
    }
}
