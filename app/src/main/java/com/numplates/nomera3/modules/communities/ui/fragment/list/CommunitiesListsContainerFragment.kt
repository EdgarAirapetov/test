package com.numplates.nomera3.modules.communities.ui.fragment.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.android.material.appbar.AppBarLayout
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.core.extensions.visibleAppearAnimate
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentGroupNewBinding
import com.numplates.nomera3.modules.communities.ui.fragment.CommunityEditFragment
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunitiesListsContainerViewModel
import com.numplates.nomera3.modules.peoples.ui.utils.PeopleCommunitiesNavigator
import com.numplates.nomera3.modules.tracker.FireBaseAnalytics
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_IS_GROUP_CREATOR
import com.numplates.nomera3.presentation.view.adapter.RoomsPagerAdapter
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.addOnPageChangeListener
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

private const val ARROW_DOWN_PADDING = 2
private const val MIN_BACK_STACK_COUNT = 1

class CommunitiesListsContainerFragment : BaseFragmentNew<FragmentGroupNewBinding>(), IOnBackPressed {

    @Inject
    lateinit var fbAnalytic: FireBaseAnalytics

    var openAll = false
    private lateinit var communitiesAllFragment: CommunitiesListAllFragment
    private lateinit var groupsUserFragment: UserCommunitiesListFragment
    private var adapter: RoomsPagerAdapter? = null
    private val disposbles = CompositeDisposable()
    private val viewModel by viewModels<CommunitiesListsContainerViewModel> {
        App.component.getViewModelFactory()
    }

    override fun onStart() {
        super.onStart()
        binding?.nvbCommunity?.selectGroups(true)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        App.component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFragments()
        init()
    }

    override fun onStartFragment() {
        super.onStartFragment()
        if (openAll) {
            binding?.pager?.currentItem = 1
            openAll = false
        }
        if (binding?.pager?.currentItem == 0) fbAnalytic.logScreenForFragment(groupsUserFragment.javaClass.simpleName)
        else if (binding?.pager?.currentItem == 1) fbAnalytic.logScreenForFragment(
            communitiesAllFragment.javaClass.simpleName
        )
    }

    override fun onStop() {
        super.onStop()
        disposbles.clear()
        binding?.nvbCommunity?.selectGroups(false)
    }

    override fun onDestroyView() {
        adapter = null
        super.onDestroyView()
    }

    override fun onBackPressed(): Boolean {
        val isSelectFragmentMenuVisible = binding?.layoutSelectFragmentAction?.root?.isVisible ?: false
        if (isSelectFragmentMenuVisible) {
            hideSelectTabMenu()
            return true
        }
        return false
    }

    private fun initFragments() {
        communitiesAllFragment = CommunitiesListAllFragment().apply {
            refreshListsCallback = { groupsUserFragment.refreshList() }
        }
        groupsUserFragment = UserCommunitiesListFragment().apply {
            refreshListsCallback = { refreshFragmentGroups() }
            openAllCommunitiesList = { binding?.pager?.currentItem = 1 }
        }

        childFragmentManager.let {
            adapter = RoomsPagerAdapter(it)
            adapter?.addFragments(mutableListOf(groupsUserFragment, communitiesAllFragment))
            adapter?.addTitleOfFragment(String.format(act.getString(R.string.groups_my)))
            adapter?.addTitleOfFragment(String.format(getString(R.string.groups_all)))
            binding?.pager?.adapter = adapter
            binding?.tabs?.setupWithViewPager(binding?.pager)
            binding?.nvbCommunity?.let { onActivityInteraction?.onGetNavigationBar(it) }

            binding?.pager?.addOnPageChangeListener(
                onPageSelected = { page ->
                    if (page == 0) fbAnalytic.logScreenForFragment(groupsUserFragment.javaClass.simpleName)
                    else if (page == 1) fbAnalytic.logScreenForFragment(communitiesAllFragment.javaClass.simpleName)
                })
        }
        fbAnalytic.logScreenForFragment(groupsUserFragment.javaClass.simpleName)
    }

    private fun init() {
        initNavigationBar()
        initListeners()
        initToolbarState()
        initNavBarState()
    }

    private fun initListeners() {
        binding?.ivSearch?.click { add(CommunitiesSearchFragment(), Act.LIGHT_STATUSBAR) }
        binding?.ivAddGroup?.click {
            add(
                CommunityEditFragment().apply {
                    refreshCallback = { groupsUserFragment.refreshList() }
                },
                Act.LIGHT_STATUSBAR,
                Arg(ARG_IS_GROUP_CREATOR, true)
            )
            communitiesAllFragment.logAmplitudeCreateCommunityTap()
        }
    }

    private fun initNavBarState() {
        binding?.nvbCommunity?.isVisible = !isBackStackNotEmpty()
    }

    private fun isBackStackNotEmpty(): Boolean {
        val adapter = act.getNavigationAdapter() ?: return false
        return adapter.getFragmentsCount() > MIN_BACK_STACK_COUNT
    }

    private fun initToolbarState() {
        binding?.tvTitle?.setCompoundDrawablesRelativeWithIntrinsicBounds(
            0,
            0,
            R.drawable.ic_arrow_down_black_16,
            0
        )
        binding?.tvTitle?.compoundDrawablePadding = ARROW_DOWN_PADDING.dp
        binding?.tvTitle?.click {
            val isSelectTabVisible = binding?.layoutSelectFragmentAction?.root?.isVisible ?: false
            if (!isSelectTabVisible) {
                showSelectTabMenu()
            } else {
                hideSelectTabMenu()
            }
        }
        binding?.layoutSelectFragmentAction?.tvSelectPeopleTab?.setThrottledClickListener {
            viewModel.logPeopleSection()
            selectPeople()
            hideSelectTabMenu()
        }
        binding?.layoutSelectFragmentAction?.tvSelectCommunityTab?.setThrottledClickListener {
            hideSelectTabMenu()
        }
        binding?.layoutSelectFragmentAction?.vShadow?.click {
            hideSelectTabMenu()
        }
    }

    private fun showSelectTabMenu() {
        binding?.layoutSelectFragmentAction?.root?.visibleAppearAnimate()
        binding?.tabs?.gone()
    }

    private fun hideSelectTabMenu() {
        binding?.layoutSelectFragmentAction?.root?.gone()
        binding?.tabs?.visible()
    }

    private fun selectPeople() {
        (requireParentFragment() as? PeopleCommunitiesNavigator)?.selectPeople()
    }

    private fun initNavigationBar() {
        //status bar
        val layoutParamsStatusBar =
            binding?.statusBarGroups?.layoutParams as AppBarLayout.LayoutParams
        layoutParamsStatusBar.height = context.getStatusBarHeight()
        binding?.statusBarGroups?.layoutParams = layoutParamsStatusBar
    }

    fun refreshFragmentGroups() {
        Timber.e("MUST Refresh GROUPS")
        // Refresh user groups if data changed
        groupsUserFragment.refreshList()
        communitiesAllFragment.refreshList()
    }

    override fun updateScreenOnTapNavBar() {
        super.updateScreenOnTapNavBar()
        if (this::communitiesAllFragment.isInitialized && this::groupsUserFragment.isInitialized) {
            communitiesAllFragment.updateScreen()
            groupsUserFragment.updateScreen()
        }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentGroupNewBinding
        get() = FragmentGroupNewBinding::inflate
}
