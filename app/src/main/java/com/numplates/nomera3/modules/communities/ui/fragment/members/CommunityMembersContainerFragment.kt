package com.numplates.nomera3.modules.communities.ui.fragment.members

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.widget.RxTextView
import com.meera.core.extensions.click
import com.meera.core.extensions.empty
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.getToolbarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.showKeyboard
import com.meera.core.extensions.visible
import com.meera.core.views.SwipeDirection
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentCommunityMembersContainerBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.communities.data.entity.CommunityMemberState
import com.numplates.nomera3.modules.communities.data.entity.CommunityUserRole
import com.numplates.nomera3.modules.communities.ui.adapter.MembersAdapter
import com.numplates.nomera3.modules.communities.ui.adapter.MembersPagerAdapter
import com.numplates.nomera3.modules.communities.ui.viewevent.CommunityMembersViewEvent
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityMembersViewModel
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.addOnPageChangeListener
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.Locale
import java.util.concurrent.TimeUnit

class CommunityMembersContainerFragment :
    BaseFragmentNew<FragmentCommunityMembersContainerBinding>() {

    private lateinit var viewModel: CommunityMembersViewModel
    private var pagerAdapter: MembersPagerAdapter? = null
    private var fragmentAllMembers: CommunityMembersListBaseFragment? = null
    private var fragmentMembershipRequests: CommunityMembersListBaseFragment? = null
    private var tvIncomingMembershipRequestsCount: TextView? = null
    private var userRole = CommunityUserRole.REGULAR
    private val disposable = CompositeDisposable()
    private var searchAdapter: MembersAdapter? = null
    private var searchPaginator: RecyclerViewPaginator? = null
    private var groupId: Int? = null

    private var isFirstInitiated = true
    private var isPrivateCommunity = false
    private var isFromPush = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(act).get(CommunityMembersViewModel::class.java)
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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
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

    private fun initToolbar() {
        val toolbarParams = binding?.toolbar?.layoutParams
        val statusBarHeight = context.getStatusBarHeight()
        val toolbarHeight = context.getToolbarHeight()
        toolbarParams?.height = toolbarHeight + statusBarHeight
        binding?.toolbar?.layoutParams = toolbarParams
        binding?.toolbarContainer?.setMargins(top = statusBarHeight)
    }

    private fun initBackFromFragment() {
        binding?.ivBack?.click {
            act.onBackPressed()
        }
    }

    private fun initBackFromSearch() {
        binding?.ivBack?.click {
            context?.hideKeyboard(requireView())
            binding?.rvSearchMembers?.gone()
            binding?.etSearchMember?.gone()
            binding?.ivClearSearch?.gone()
            binding?.membersViewPager?.visible()
            binding?.ivSearchMember?.visible()
            binding?.tvTitle?.visible()
            viewModel.isLastPage = false
            initBackFromFragment()
        }
    }

    private fun initSearch() {
        binding?.etSearchMember?.let { etSearch ->
            disposable.add(
                RxTextView.textChanges(etSearch)
                    .map { text -> text.toString().lowercase(Locale.getDefault()).trim() }
                    .debounce(DEFAULT_DEBOUNCE_TIME, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { txt ->
                            if (isFirstInitiated) {
                                isFirstInitiated = false
                                return@subscribe
                            }
                            searchAdapter?.clearMembers()
                            searchPaginator?.resetCurrentPage()
                            if (txt.isNotEmpty()) viewModel.search(txt)
                        },
                        { e ->
                            Timber.e(e)
                        }
                    )
            )
            etSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) = Unit

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    binding?.ivClearSearch?.visibility =
                        if (s?.isNotEmpty() == true) View.VISIBLE else View.GONE
                }

                override fun afterTextChanged(s: Editable?) = Unit
            })
            binding?.ivClearSearch?.click {
                etSearch.setText(String.empty())
            }

            binding?.ivSearchMember?.click {
                etSearch.visible()
                etSearch.requestFocus()
                etSearch.showKeyboard()
                binding?.ivSearchMember?.gone()
                binding?.membersTabLayout?.gone()
                binding?.membersViewPager?.gone()
                binding?.rvSearchMembers?.visible()
                binding?.tvTitle?.gone()
                initBackFromSearch()
            }
        }
        initSearchResultsList()
    }

    private fun initSearchResultsList() {
        searchAdapter = MembersAdapter(
            userId = viewModel.getUserUid(),
            userRole = userRole,
            listType = CommunityMemberState.APPROVED,
            hideAgeAndGender = viewModel.isHiddenAgeAndGender()
        )
        searchAdapter?.onMemberClicked = {
            context?.hideKeyboard(requireView())
            add(
                UserInfoFragment(), Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
                Arg(ARG_USER_ID, it.uid),
                Arg(IArgContainer.ARG_TRANSIT_FROM, AmplitudePropertyWhere.COMMUNITY.property)
            )
        }
        binding?.rvSearchMembers?.also {
            it.layoutManager = LinearLayoutManager(context)
            it.adapter = searchAdapter
            searchPaginator = RecyclerViewPaginator(
                recyclerView = it,
                isLoading = viewModel::isLoading,
                loadMore = {
                    viewModel.loadMore(
                        searchAdapter?.itemCount ?: 0,
                        CommunityMemberState.APPROVED
                    )
                },
                onLast = viewModel::isLastPage
            ).apply {
                endWithAuto = true
            }
        }
    }

    private fun initEmptyListPlaceHolder() {
        binding?.phEmptyList?.ivEmptyList?.setImageResource(R.drawable.ic_empty_search_noomeera)
        binding?.phEmptyList?.tvEmptyList?.setText(R.string.placeholder_empty_search_result)
        binding?.phEmptyList?.tvButtonEmptyList?.gone()
        binding?.phEmptyList?.root?.gone()
    }

    private fun initViewPager() {
        pagerAdapter = MembersPagerAdapter(childFragmentManager)
        if (userRole != CommunityUserRole.REGULAR
            && isPrivateCommunity
            || isFromPush
        ) {
            val fragments = mutableListOf(fragmentAllMembers!!, fragmentMembershipRequests!!)
            pagerAdapter?.setFragments(fragments)
            pagerAdapter?.setTitles(
                mutableListOf(
                    getString(R.string.community_members_tab_all),
                    getString(R.string.community_members_tab_not_approved)
                )
            )
            binding?.membersViewPager?.offscreenPageLimit = OFFSCREEN_PAGE_LIMIT
            binding?.membersViewPager?.adapter = pagerAdapter
            binding?.membersTabLayout?.setupWithViewPager(binding?.membersViewPager)
            binding?.membersTabLayout?.visible()
            binding?.membersViewPager?.addOnPageChangeListener { position ->
                Timber.e("PAGE position: $position")
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

    private fun initTabs(pagerAdapter: MembersPagerAdapter) {
        binding?.membersTabLayout?.apply {
            for (i in 0 until tabCount) {
                val v = LayoutInflater.from(context)
                    .inflate(R.layout.tab_item_community_members_container, this, false)
                v.findViewById<TextView>(R.id.tab_title)?.text = pagerAdapter.getPageTitle(i)
                if (i == 1) {
                    tvIncomingMembershipRequestsCount = v.findViewById(R.id.tab_count)
                }
                getTabAt(i)?.customView = v
            }
        }
    }

    private fun initObservers() {
        viewModel.liveViewEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                is CommunityMembersViewEvent.SuccessSearchMembers -> {
//                    setSearchResult(event.members.users)
                }
                is CommunityMembersViewEvent.SuccessGetJoinRequestsCount ->
                    setJoinRequestsCount(event.count)

                else -> {}
            }
        }
    }

//    private fun setSearchResult(users: List<UserInfoModel>?) {
//        when {
//            users?.isNotEmpty() == true -> {
//                searchAdapter?.addMembers(users)
//                binding?.rvSearchMembers?.visible()
//                binding?.tvSearchListTitle?.visible()
//                binding?.phEmptyList?.root?.gone()
//            }
//
//            !viewModel.isLastPage -> {
//                searchAdapter?.clearMembers()
//                binding?.rvSearchMembers?.gone()
//                binding?.tvSearchListTitle?.gone()
//                binding?.phEmptyList?.root?.visible()
//            }
//        }
//    }

    private fun setJoinRequestsCount(count: Int) {
        if (count > 0) {
            tvIncomingMembershipRequestsCount?.text = count.toString()
            tvIncomingMembershipRequestsCount?.visible()
        } else {
            tvIncomingMembershipRequestsCount?.gone()
        }
    }

    private fun initFragments() {
        fragmentAllMembers = CommunityMembersListApprovedFragment()
        if (userRole != CommunityUserRole.REGULAR && isPrivateCommunity || isFromPush) {
            fragmentMembershipRequests = CommunityMembersListNotApprovedFragment()
        }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCommunityMembersContainerBinding
        get() = FragmentCommunityMembersContainerBinding::inflate

    companion object {
        const val DEFAULT_DEBOUNCE_TIME = 350L
        const val OFFSCREEN_PAGE_LIMIT = 2
    }
}
