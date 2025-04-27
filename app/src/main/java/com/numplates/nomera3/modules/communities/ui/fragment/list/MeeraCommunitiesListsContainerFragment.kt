package com.numplates.nomera3.modules.communities.ui.fragment.list

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.gone
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentGroupBinding
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.tracker.FireBaseAnalytics
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.adapter.RoomsPagerAdapter
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.addOnPageChangeListener
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

private const val SELECT_TAB_GROUP_USER = 0
private const val SELECT_TAB_COMMUNITIES_ALL = 1
private const val ONE_ELEMENT = 1

class MeeraCommunitiesListsContainerFragment : MeeraBaseDialogFragment(
    layout = R.layout.meera_fragment_group,
    behaviourConfigState = ScreenBehaviourState.Full
) {
    val binding by viewBinding(MeeraFragmentGroupBinding::bind)

    @Inject
    lateinit var fbAnalytic: FireBaseAnalytics

    var openAll = false
    private var communitiesAllFragment: MeeraCommunitiesListAllFragment? = null
    private var groupsUserFragment: MeeraUserCommunitiesListFragment? = null
    private var adapter: RoomsPagerAdapter? = null
    private val disposbles = CompositeDisposable()

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    override fun onAttach(context: Context) {
        super.onAttach(context)
        App.component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFragments()
        init()
        onStartFragment()
    }


    private fun onStartFragment() {
        if (openAll) {
            binding?.pager?.currentItem = ONE_ELEMENT
            openAll = false
        }
        if (binding?.pager?.currentItem == 0) fbAnalytic.logScreenForFragment(groupsUserFragment?.javaClass?.simpleName)
        else if (binding?.pager?.currentItem == ONE_ELEMENT) fbAnalytic.logScreenForFragment(
            communitiesAllFragment?.javaClass?.simpleName
        )
    }

    override fun onStop() {
        super.onStop()
        disposbles.clear()
    }

    override fun onDestroyView() {
        adapter = null
        super.onDestroyView()
    }

    private fun initFragments() {
        if (communitiesAllFragment == null){
            communitiesAllFragment = MeeraCommunitiesListAllFragment().apply {
                refreshListsCallback = {
                    groupsUserFragment?.refreshList()
                }
            }
        }
        if (groupsUserFragment == null) {
            groupsUserFragment = MeeraUserCommunitiesListFragment().apply {
                refreshListsCallback = {
                    refreshFragmentGroups()
                }
                openAllCommunitiesList =
                    { this@MeeraCommunitiesListsContainerFragment.binding.pager?.currentItem = ONE_ELEMENT }
            }
            groupsUserFragment?.updateVisibilitySearchField{
                binding.vSearchGroup.visible()
            }
        }

        childFragmentManager.let {
            adapter = RoomsPagerAdapter(it)

            groupsUserFragment?.let { groupsUserFragment ->
                communitiesAllFragment?.let { communitiesAllFragment ->
                    adapter?.addFragments(mutableListOf(groupsUserFragment, communitiesAllFragment))
                }
            }

            adapter?.addTitleOfFragment(String.format(getString(R.string.groups_my)).uppercase())
            adapter?.addTitleOfFragment(String.format(getString(R.string.groups_all)).uppercase())
            binding?.pager?.adapter = adapter
            binding?.tabs?.setupWithViewPager(binding?.pager)

            binding?.pager?.addOnPageChangeListener(
                onPageSelected = { page ->
                    if (page == 0) {
                        binding.vSearchGroup.clear()
                        groupsUserFragment?.getCountCommunityListSize()?.let { itemCount ->
                            if (itemCount > 0){
                                groupsUserFragment?.refreshList()
                                binding.vSearchGroup.visible()
                            } else {
                                binding.vSearchGroup.gone()
                            }
                        }
                        fbAnalytic.logScreenForFragment(groupsUserFragment?.javaClass?.simpleName)
                    }
                    else {
                        binding.vSearchGroup.clear()
                        communitiesAllFragment?.refreshList()
                        if (page == ONE_ELEMENT) {
                            binding.vSearchGroup.visible()
                            fbAnalytic.logScreenForFragment(communitiesAllFragment?.javaClass?.simpleName)
                        }
                    }
                })
        }
        fbAnalytic.logScreenForFragment(groupsUserFragment?.javaClass?.simpleName)
    }

    private fun init() {
        initListeners()
    }

    private fun initListeners() {
        binding?.vSearchGroup?.doAfterSearchTextChanged { groupName ->
            when (binding?.tabs?.selectedTabPosition) {
                SELECT_TAB_GROUP_USER -> {
                    groupsUserFragment?.searchUserGroup(groupName)
                }

                SELECT_TAB_COMMUNITIES_ALL -> {
                    communitiesAllFragment?.searchAllCommunities(groupName)
                }
            }
        }
        binding?.vSearchGroup?.setClearButtonClickedListener {
            binding.vSearchGroup.clear()
            when (binding?.tabs?.selectedTabPosition) {
                SELECT_TAB_GROUP_USER -> {
                    groupsUserFragment?.refreshList()
                }

                SELECT_TAB_COMMUNITIES_ALL -> {
                    communitiesAllFragment?.refreshList()
                }
            }
        }
        binding?.vSearchGroup?.setCloseButtonClickedListener {
            when (binding?.tabs?.selectedTabPosition) {
                SELECT_TAB_GROUP_USER -> {
                    groupsUserFragment?.refreshList()
                }

                SELECT_TAB_COMMUNITIES_ALL -> {
                    communitiesAllFragment?.refreshList()
                }
            }
        }
        binding.vNavGroup.backButtonClickListener = {
            findNavController().popBackStack()
        }
        binding.vAddGroup.setThrottledClickListener {
            findNavController().safeNavigate(
                R.id.action_meeraCommunitiesListsContainerFragment_to_meeraCommunityEditFragment,
                bundleOf(IArgContainer.ARG_IS_GROUP_CREATOR to true)
            )
            communitiesAllFragment?.logAmplitudeCreateCommunityTap()
        }
    }

    fun refreshFragmentGroups() {
        groupsUserFragment?.refreshList()
        communitiesAllFragment?.refreshList()
    }
}
