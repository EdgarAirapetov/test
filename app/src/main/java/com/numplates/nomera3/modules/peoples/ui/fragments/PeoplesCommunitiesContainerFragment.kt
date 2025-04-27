package com.numplates.nomera3.modules.peoples.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.meera.core.extensions.isFalse
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.simpleName
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentPeoplesCommunitiesContainerBinding
import com.numplates.nomera3.modules.communities.ui.fragment.list.CommunitiesListsContainerFragment
import com.numplates.nomera3.modules.peoples.ui.content.action.RefreshPeopleHandler
import com.numplates.nomera3.modules.peoples.ui.entity.PeoplesCommunitiesContainerState
import com.numplates.nomera3.modules.peoples.ui.utils.PeopleCommunitiesNavigator
import com.numplates.nomera3.modules.peoples.ui.viewmodel.PeoplesCommunitiesContainerViewModel
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed

/**
 * Контейнер для таба "Люди", который будет иметь 2 состояния:
 * 1.В данный контейнер будет добавлен [com.numplates.nomera3.modules.peoples.ui.fragments.PeoplesFragment]
 * 2.А так же фрагмент с сообществами
 * [com.numplates.nomera3.modules.communities.ui.fragment.list.CommunitiesListsContainerFragment]
 */
class PeoplesCommunitiesContainerFragment : BaseFragmentNew<FragmentPeoplesCommunitiesContainerBinding>(),
    IOnBackPressed, PeopleCommunitiesNavigator {

    override val bindingInflater: (
        LayoutInflater,
        ViewGroup?, Boolean
    ) -> FragmentPeoplesCommunitiesContainerBinding
        get() = FragmentPeoplesCommunitiesContainerBinding::inflate

    private val viewModel by viewModels<PeoplesCommunitiesContainerViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeState()
    }

    override fun isCommunitySelected(): Boolean =
        binding?.vgPeoplesContainer?.getFragment<BaseFragmentNew<*>>() is CommunitiesListsContainerFragment

    override fun onBackPressed(): Boolean {
        val childFragment: BaseFragmentNew<*>? = binding?.vgPeoplesContainer?.getFragment()
        if (childFragment is IOnBackPressed) {
            return childFragment.onBackPressed()
        }
        return false
    }

    override fun updateScreenOnTapNavBar() {
        super.updateScreenOnTapNavBar()
        when (val currentFragment: BaseFragmentNew<*>? = binding?.vgPeoplesContainer?.getFragment()) {
            is RefreshPeopleHandler -> currentFragment.onRefreshPeopleContent()
            else -> selectPeople()
        }
    }

    override fun selectPeople() = viewModel.setPeopleScreenState()

    override fun selectCommunities() = viewModel.setCommunityState()

    override fun onStartFragment() {
        super.onStartFragment()
        onStartFragmentIfNotStarted(getCurrentChildFragment())
    }

    override fun onStopFragment() {
        super.onStopFragment()
        onStopFragmentIfStarted(getCurrentChildFragment())
    }

    private fun onStopFragmentIfStarted(currentFragment: BaseFragmentNew<*>?) {
        if (currentFragment?.isFragmentStarted.isTrue()) {
            currentFragment?.onStopFragment()
        }
    }

    private fun onStartFragmentIfNotStarted(currentFragment: BaseFragmentNew<*>?) {
        if (currentFragment?.isFragmentStarted.isFalse()) {
            currentFragment?.onStartFragment()
        }
    }

    private fun observeState() {
        viewModel.fragmentContainerState.observe(viewLifecycleOwner) { state ->
            when (state) {
                PeoplesCommunitiesContainerState.PeoplesState -> {
                    val userIdFromPush = arguments?.getLong(ARG_USER_ID)
                    val fragment = PeoplesFragment().apply {
                            arguments = bundleOf(ARG_USER_ID to userIdFromPush)
                        }
                    addFragmentToContainer(fragment)
                }
                PeoplesCommunitiesContainerState.CommunitiesState -> {
                    addFragmentToContainer(CommunitiesListsContainerFragment())
                }
            }
        }
    }

    private fun getCurrentChildFragment(): BaseFragmentNew<*>? {
        return binding?.vgPeoplesContainer?.getFragment()
    }

    private fun addFragmentToContainer(
        fragment: BaseFragmentNew<*>
    ) {
        if (childFragmentManager.findFragmentByTag(fragment.simpleName) != null) return
        childFragmentManager.beginTransaction()
            .replace(R.id.vg_peoples_container, fragment, fragment.simpleName)
            .runOnCommit { registerCurrentFragmentLifecycle(fragment) }
            .commitAllowingStateLoss()
    }

    /**
     * onStartFragment(), onStopFragment() не вызываются в людях.
     * Возможно, что найдется другое решение
     */
    private fun registerCurrentFragmentLifecycle(currentFragment: BaseFragmentNew<*>) {
        currentFragment.lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    onStartFragmentIfNotStarted(currentFragment)
                    super.onStart(owner)
                }

                override fun onStop(owner: LifecycleOwner) {
                    onStopFragmentIfStarted(currentFragment)
                    super.onStop(owner)
                }
            }
        )
    }
}
