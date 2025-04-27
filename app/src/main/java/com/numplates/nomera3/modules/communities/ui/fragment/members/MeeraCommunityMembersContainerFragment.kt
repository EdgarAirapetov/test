package com.numplates.nomera3.modules.communities.ui.fragment.members

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.gone
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.textColor
import com.meera.core.extensions.visible
import com.meera.core.views.SwipeDirection
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.MeeraUserInfoModel
import com.numplates.nomera3.databinding.MeeraCommunityMembersContainerFragmentBinding
import com.numplates.nomera3.modules.communities.data.entity.CommunityMemberState
import com.numplates.nomera3.modules.communities.data.entity.CommunityUserRole
import com.numplates.nomera3.modules.communities.ui.adapter.MeeraMembersAdapter
import com.numplates.nomera3.modules.communities.ui.adapter.MeeraMembersPagerAdapter
import com.numplates.nomera3.modules.communities.ui.adapter.UserInfoModelRecyclerData
import com.numplates.nomera3.modules.communities.ui.viewevent.CommunityMembersViewEvent
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityMembersViewModel
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.addOnPageChangeListener
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

private const val INPUT_TIMEOUT = 300L

class MeeraCommunityMembersContainerFragment :
    MeeraBaseDialogFragment(
        layout = R.layout.meera_community_members_container_fragment,
        behaviourConfigState = ScreenBehaviourState.Full
    ) {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(MeeraCommunityMembersContainerFragmentBinding::bind)
    private val viewModel by viewModels<CommunityMembersViewModel> {
        App.component.getViewModelFactory()
    }
    private var pagerAdapter: MeeraMembersPagerAdapter? = null
    private var fragmentAllMembers: MeeraCommunityMembersListBaseFragment? = null
    private var fragmentMembershipRequests: MeeraCommunityMembersListBaseFragment? = null
    private var tvIncomingMembershipRequestsCount: TextView? = null
    private var userRole = CommunityUserRole.REGULAR
    private val disposable = CompositeDisposable()
    private var searchAdapter: MeeraMembersAdapter? = null
    private var groupId: Int? = null

    private var isFirstInitiated = true
    private var isPrivateCommunity = false
    private var isFromPush = false
    private var selectTabList: Int = CommunityMemberState.APPROVED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getInt(IArgContainer.ARG_GROUP_ID)?.let {
            groupId = it
        }
        arguments?.getInt(IArgContainer.ARG_COMMUNITY_USER_ROLE)?.let {
            userRole = it
        }
        arguments?.getInt(IArgContainer.ARG_COMMUNITY_IS_PRIVATE)?.let {
            isPrivateCommunity = it.isTrue()
        }
        arguments?.getBoolean(IArgContainer.ARG_IS_FROM_PUSH, false)?.let {
            isFromPush = it
            if (it) userRole = CommunityUserRole.MODERATOR
        }
        viewModel.bind(groupId, userRole)
        viewModel.updateGroupInfo()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBackFromFragment()
        initEmptyListPlaceHolder()
        initObservers()
        initFragments()
        initViewPager()
    }

    override fun onStart() {
        super.onStart()
        isFirstInitiated = true
        initSearch()
    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        fragmentAllMembers = null
        fragmentMembershipRequests = null
    }

    private fun initBackFromFragment() {
        binding?.vNavView?.backButtonClickListener = {
            findNavController().popBackStack()
        }
    }

    @SuppressLint("CheckResult")
    private fun initSearch() {
        binding?.vInputSearch?.doAfterSearchTextChanged { userName ->
            Observable.just(userName)
                .debounce(INPUT_TIMEOUT, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { name ->
                    if (name.isNullOrEmpty()) {
                        viewModel.clearSearchMembers(selectTabList)
                    } else {
                        viewModel.newSearch(selectTabList, name)
                    }
                }
        }
        binding.vInputSearch.setClearButtonClickedListener {
            binding.vInputSearch.clear()
        }
    }

    private fun initEmptyListPlaceHolder() {
        binding?.phEmptyList?.ivEmptyList?.setImageResource(R.drawable.ic_empty_search_noomeera)
        binding?.phEmptyList?.tvEmptyList?.setText(R.string.placeholder_empty_search_result)
        binding?.phEmptyList?.tvButtonEmptyList?.gone()
        binding?.phEmptyList?.root?.gone()
    }

    private fun initViewPager() {
        pagerAdapter = MeeraMembersPagerAdapter(childFragmentManager)
        if (userRole != CommunityUserRole.REGULAR
            && isPrivateCommunity
            || isFromPush
        ) {
            val fragments = mutableListOf(fragmentAllMembers!!, fragmentMembershipRequests!!)
            pagerAdapter?.setFragments(fragments)
            pagerAdapter?.setTitles(
                mutableListOf(
                    getString(R.string.groups_all),
                    getString(R.string.meera_community_members_tab_not_approved)
                )
            )
            binding?.membersViewPager?.offscreenPageLimit = OFFSCREEN_PAGE_LIMIT
            binding?.membersViewPager?.adapter = pagerAdapter
            binding?.membersTabLayout?.setupWithViewPager(binding?.membersViewPager)
            binding?.membersTabLayout?.visible()
            binding?.membersViewPager?.addOnPageChangeListener { position ->
                if (position == 1) {
                    viewModel.logScreenForFragment(fragmentMembershipRequests?.javaClass?.simpleName.orEmpty())
                } else {
                    viewModel.logScreenForFragment(fragmentAllMembers?.javaClass?.simpleName.orEmpty())
                }
            }
            pagerAdapter?.let { initTabs(it) }
            if (isFromPush) {
                binding?.membersViewPager?.currentItem = fragments.count()
                isFromPush = false
            }
        } else {
            pagerAdapter?.setFragments(
                mutableListOf(fragmentAllMembers!!)
            )
            binding?.membersTabLayout?.gone()
            binding?.membersViewPager?.offscreenPageLimit = 1
            binding?.membersViewPager?.adapter = pagerAdapter
            binding?.membersViewPager?.setAllowedSwipeDirection(SwipeDirection.NONE)
            viewModel.logScreenForFragment(fragmentAllMembers?.javaClass?.simpleName.orEmpty())
        }
    }

    private fun initTabs(pagerAdapter: MeeraMembersPagerAdapter) {

        binding?.membersTabLayout?.apply {
            for (i in 0 until tabCount) {
                val v = LayoutInflater.from(context)
                    .inflate(R.layout.meera_tab_item_community_members_container, this, false)
                v.findViewById<TextView>(R.id.tab_title)?.text = pagerAdapter.getPageTitle(i)
                if (i == 1) {
                    tvIncomingMembershipRequestsCount = v.findViewById(R.id.tab_count)
                }
                getTabAt(i)?.customView = v
            }

            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    binding.vInputSearch.clear()
                    if (tab?.position == 1) {
                        tvIncomingMembershipRequestsCount?.setBackgroundResource(
                            R.drawable.meera_circle_tab_bg
                        )
                        tvIncomingMembershipRequestsCount?.textColor(R.color.uiKitColorForegroundPrimary)
                        selectTabList = CommunityMemberState.NOT_APPROVED
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    if (tab?.position == 1) {
                        tvIncomingMembershipRequestsCount?.setBackgroundResource(
                            R.drawable.meera_circle_unselected_count_tab_bg
                        )
                        tvIncomingMembershipRequestsCount?.textColor(R.color.uiKitColorForegroundSecondary)
                        selectTabList = CommunityMemberState.APPROVED
                    }
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                    Timber.d("Unused function")
                }
            })
        }
    }

    private fun initObservers() {
        viewModel.liveViewEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                is CommunityMembersViewEvent.SuccessSearchMembers -> setSearchResult(event.members.users)
                is CommunityMembersViewEvent.SuccessGetJoinRequestsCount ->
                    setJoinRequestsCount(event.count)

                else -> Unit
            }
        }
    }

    private fun setSearchResult(users: List<MeeraUserInfoModel>?) {
        when {
            users?.isNotEmpty() == true -> {
                searchAdapter?.submitList(users.map { UserInfoModelRecyclerData.UserInfoData(it.user.userId, it) })
                binding?.rvSearchMembers?.visible()
                binding?.tvSearchListTitle?.visible()
                binding?.phEmptyList?.root?.gone()
            }

            !viewModel.isLastPage -> {
                searchAdapter?.submitList(mutableListOf())
                binding?.rvSearchMembers?.gone()
                binding?.tvSearchListTitle?.gone()
                binding?.phEmptyList?.root?.visible()
            }
        }
    }

    private fun setJoinRequestsCount(count: Int) {
        if (count > 0) {
            tvIncomingMembershipRequestsCount?.text = count.toString()
            tvIncomingMembershipRequestsCount?.visible()
        } else {
            tvIncomingMembershipRequestsCount?.gone()
        }
    }

    private fun initFragments() {
        fragmentAllMembers = MeeraCommunityMembersListApprovedFragment()
        if (userRole != CommunityUserRole.REGULAR && isPrivateCommunity || isFromPush) {
            fragmentMembershipRequests = MeeraCommunityMembersListNotApprovedFragment()
        }
        (fragmentAllMembers as MeeraCommunityMembersListApprovedFragment).arguments = bundleOf(
            IArgContainer.ARG_GROUP_ID to groupId,
            IArgContainer.ARG_COMMUNITY_USER_ROLE to viewModel.getUserRole()
        )
        fragmentMembershipRequests?.arguments = bundleOf(
            IArgContainer.ARG_GROUP_ID to groupId,
            IArgContainer.ARG_COMMUNITY_USER_ROLE to viewModel.getUserRole()
        )
    }

    companion object {
        const val OFFSCREEN_PAGE_LIMIT = 2
    }
}
