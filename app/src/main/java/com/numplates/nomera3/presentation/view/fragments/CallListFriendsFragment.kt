package com.numplates.nomera3.presentation.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.jakewharton.rxbinding2.widget.RxTextView
import com.meera.core.extensions.clearText
import com.meera.core.extensions.empty
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.meera.core.utils.pagination.RecyclerPaginationListener
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentCallListFriendsBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_PAGER_PROFILE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_TRANSIT_FROM
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.view.adapter.ListUsersAdapter
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.viewmodel.CallListUsersViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.ChatGroupViewEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

/**
 * NOT ONLY Friends -> All users SEARCH
 */
class CallListFriendsFragment : BaseFragmentNew<FragmentCallListFriendsBinding>() {

    companion object {
        const val USER_LIST_TYPE_BLACKLIST = 0
        const val USER_LIST_TYPE_WHITELIST = 1
        const val LIST_PAGE_LIMIT = 20
    }
    private val listUsersViewModel: CallListUsersViewModel
            by lazy { ViewModelProviders.of(this).get(CallListUsersViewModel::class.java) }

    private val disposables = CompositeDisposable()

    private var userListType: Int? = null

    private var currOffset = 0

    private var searchQuery = String.empty()

    private var isSearchMode = false
    private var observableListMode: Int by Delegates.observable(0) { d, old, new ->
        // Handler modifications
        isSearchMode = new == 1
    }


    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCallListFriendsBinding
        get() = FragmentCallListFriendsBinding::inflate


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userListType = arguments?.getInt(IArgContainer.ARG_SETTINGS_CALL_USER_LIST_TYPE)
        listUsersViewModel.init(userListType)
        listUsersViewModel.observeUserCounters()

        setupToolbar()
        initRecycler()
        initLiveObservables()
    }


    override fun onResume() {
        super.onResume()
        searchUser()
    }


    override fun onStop() {
        super.onStop()
        disposables.clear()
    }



    private fun setupToolbar() {
        // Set title
        val title = arguments?.getString(IArgContainer.ARG_FRAGMENT_TITLE, String.empty())
        binding?.tvHeaderMembersSelectionTop?.text = title

        val params = binding?.statusBarCallListFriends?.layoutParams as AppBarLayout.LayoutParams
        params.height = context.getStatusBarHeight()
        binding?.statusBarCallListFriends?.layoutParams = params
        binding?.toolbarCallListFriends?.setNavigationIcon(R.drawable.arrowback)
        binding?.toolbarCallListFriends?.setNavigationOnClickListener {
            binding?.etSearchName?.clearText()
            act.onBackPressed()
        }
    }

    private fun  initRecycler() {
        binding?.rvCallListFriends?.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(act)
        binding?.rvCallListFriends?.layoutManager = layoutManager
        binding?.rvCallListFriends?.adapter = listUsersViewModel.adapter

        // Pagination
        binding?.rvCallListFriends?.addOnScrollListener(object : RecyclerPaginationListener(layoutManager) {

            override fun loadMoreItems() {
                currOffset += LIST_PAGE_LIMIT

                if (!isSearchMode) {
                    userListType?.let { type ->
                        when (type) {
                            USER_LIST_TYPE_BLACKLIST -> listUsersViewModel.getUsersBlackList(currOffset, false)
                            USER_LIST_TYPE_WHITELIST -> listUsersViewModel.getUsersWhitelist(currOffset, false)
                        }
                    }
                } else {
                    when (userListType) {
                        USER_LIST_TYPE_BLACKLIST ->
                            listUsersViewModel.searchUsersBlackList(searchQuery, LIST_PAGE_LIMIT, currOffset, false)
                        USER_LIST_TYPE_WHITELIST ->
                            listUsersViewModel.searchUsersWhiteList(searchQuery, LIST_PAGE_LIMIT, currOffset, false)
                    }
                }
            }

            override fun isLastPage(): Boolean = listUsersViewModel.isLastPage

            override fun isLoading(): Boolean = listUsersViewModel.isLoading
        })
    }

    private fun initLiveObservables() {
         // Observe and handle view events
         listUsersViewModel.liveViewEvents.observe(viewLifecycleOwner, Observer { viewEvents ->
             handleEvents(viewEvents)
         })
         listUsersViewModel.liveProgress.observe(viewLifecycleOwner,Observer { visible ->
             if (visible) {
                 binding?.progressLoading?.visible()
             } else {
                 binding?.progressLoading?.gone()
             }
         })
         listUsersViewModel.liveUsers.observe(viewLifecycleOwner,Observer { users ->
             if(users.isNotEmpty()){
                 (binding?.rvCallListFriends?.adapter as? ListUsersAdapter)?.updateDataSet(users)
             }
         })
    }

    private fun searchUser() {
        disposables.add(
                RxTextView.textChanges(binding?.etSearchName!!)
                        .map { text -> text.toString().trim() }
                        .distinctUntilChanged()
                        .debounce(300, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ queryText ->
                            Timber.e("Query TEXT:|-> $queryText")
                            this.searchQuery = queryText.toString()

                            if (queryText.isNotEmpty()) {
                                observableListMode = 1
                                when (userListType) {
                                    USER_LIST_TYPE_BLACKLIST ->
                                        listUsersViewModel.searchUsersBlackList(queryText.toString(), LIST_PAGE_LIMIT, 0)
                                    USER_LIST_TYPE_WHITELIST ->
                                        listUsersViewModel.searchUsersWhiteList(queryText.toString(), LIST_PAGE_LIMIT, 0)
                                }
                            } else {
                                observableListMode = 0
                                listUsersViewModel.clearList()
                                userListType?.let { type ->
                                    when (type) {
                                        USER_LIST_TYPE_BLACKLIST -> listUsersViewModel.getUsersBlackList()
                                        USER_LIST_TYPE_WHITELIST -> listUsersViewModel.getUsersWhitelist()
                                    }
                                }
                            }
                        }, { error -> Timber.e(error) })
        )
    }

    private fun handleEvents(event: ChatGroupViewEvent) {
        when (event) {
            is ChatGroupViewEvent.ErrorLoadFriendList ->
                NToast.with(view)
                        .text(getString(R.string.error_load_users))
                        .show()
            is ChatGroupViewEvent.ErrorSaveSetting ->
                NToast.with(view)
                        .text(getString(R.string.error_save_settings))
                        .show()
            is ChatGroupViewEvent.OnUserAvatarClicked -> {
                add(
                    UserInfoFragment(), Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
                    Arg(ARG_USER_ID, event.user.userId),
                    Arg(ARG_PAGER_PROFILE, false),
                    Arg(ARG_TRANSIT_FROM, AmplitudePropertyWhere.FRIEND.property)
                )
            }
            else -> {}
        }
    }


}
