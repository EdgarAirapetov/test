package com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.core.utils.pagination.RecyclerPaginationListener
import com.meera.db.models.userprofile.UserSimple
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.buttons.ButtonType
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraBaseSettingsUserListFragmentBinding
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraBaseSettingsListShimmerAdapter
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraBaseSettingsUserListAction
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraBaseSettingsUserListAdapter
import com.numplates.nomera3.presentation.viewmodel.viewevents.ListUsersSearchViewEvent

private const val DELAY_UPDATE_RETURN_TRANSITION = 1500L

/**
 * Show already added users in settings
 */
abstract class MeeraBaseSettingsUserListFragment : MeeraBaseDialogFragment(
    R.layout.meera_base_settings_user_list_fragment,
    ScreenBehaviourState.Full
) {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    abstract fun screenConfiguration(): MeeraBaseSettingsUserListConfiguration
    abstract fun transitToAddUsersFragment()
    abstract fun removeUserFromList(userIds: List<Long>, adapterPosition: Int)
    abstract fun removeAllUsersFromList()
    abstract fun showUsersRequest(limit: Int, offset: Int)
    abstract fun lastPage(): Boolean
    abstract fun loading(): Boolean

    private val binding by viewBinding(MeeraBaseSettingsUserListFragmentBinding::bind)
    private var baseAdapter: MeeraBaseSettingsUserListAdapter? = null
    private val shimmerAdapter = MeeraBaseSettingsListShimmerAdapter()
    private var screenConfiguration: MeeraBaseSettingsUserListConfiguration? = null
    private val listShimmer = List(2){""}
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        screenConfiguration = screenConfiguration()
        initViews()
        initRecycler()
        loading()
        showUsersRequest(BASE_LIST_USERS_PAGE_SIZE, 0)
    }

    /**
     * Fetch adapter list users
     */
    fun addUsersToAdapter(users: List<UserSimple>) {
        baseAdapter?.submitList(users)
    }


    /**
     * Call inside live progress visibility
     */
    fun loadProgressVisibility(isVisible: Boolean) {
        if (isVisible) {
            shimmerVisibility(true)
        } else
            shimmerVisibility(false)
    }

    /**
     * Call inside live view events
     */
    fun handleEvents(event: ListUsersSearchViewEvent) {
        when (event) {
            is ListUsersSearchViewEvent.OnErrorLoadUsers -> showCommonErrorMessage()
            is ListUsersSearchViewEvent.OnSuccessRemoveUser -> {
                loadProgressVisibility(false)
                updateItems()
                baseAdapter?.currentList?.let {
                    if (it.size == 1) emptyListState()
                }
            }

            is ListUsersSearchViewEvent.OnFailureRemoveUser -> showCommonErrorMessage()
            is ListUsersSearchViewEvent.OnSuccessRemoveAllUsers -> findNavController().popBackStack()
            is ListUsersSearchViewEvent.OnErrorRemoveAllUsers -> showCommonErrorMessage()
            else -> {}
        }
    }

    override fun onResume() {
        super.onResume()
        if (delayUpdateOnReturnTransition()) {
            doDelayed(DELAY_UPDATE_RETURN_TRANSITION) {
                updateItems()
            }
        } else {
            updateItems()
        }
    }

    open fun delayUpdateOnReturnTransition(): Boolean = true

    fun shimmerVisibility(visible: Boolean){
        if (visible){
            binding.rvSettingsShimmerRecycler.visible()
            binding.rvSettingsUserListRecycler.gone()
        }else{
            binding.rvSettingsShimmerRecycler.gone()
            binding.rvSettingsUserListRecycler.visible()
        }
    }

    private fun updateItems() {
        showUsersRequest(BASE_LIST_USERS_PAGE_SIZE, 0)
    }

    private fun initViews() {
        binding.vSettingsUserListNavView.backButtonClickListener = { findNavController().popBackStack() }
        binding.vSettingsUserListNavView.title = screenConfiguration?.screenTitle
        binding.vSettingsUserListDeleteAllBtn.setThrottledClickListener { showDeleteAllUsersDialog() }
        binding.vSettingsUserListAddBtn.setThrottledClickListener { transitToAddUsersFragment() }
        baseAdapter = MeeraBaseSettingsUserListAdapter(actionListener = this::initBaseSettingsUserListAction)
        screenConfiguration?.addUserItemTitle?.let {
            binding.tvSettingsUserListDescription.text = it
        } ?: binding.tvSettingsUserListDescription.gone()
        if (screenConfiguration?.isShowDeleteAllItem != true) {
            binding.vSettingsUserListDeleteAllBtn.gone()
        }
    }

    private fun initBaseSettingsUserListAction(action: MeeraBaseSettingsUserListAction) {
        when (action) {
            is MeeraBaseSettingsUserListAction.DeleteUserAction -> {
                showDeleteUserDialog(
                    userName = action.userName,
                    userId = action.userId,
                    adapterPosition = action.adapterPosition
                )
            }
        }
    }

    private fun deleteAllUsers() {
        removeAllUsersFromList()
        baseAdapter?.submitList(listOf<UserSimple>())
    }

    private fun showDeleteAllUsersDialog() {
        MeeraConfirmDialogBuilder()
            .setHeader(screenConfiguration?.dialogListTitleRes ?: 0)
            .setDescription(screenConfiguration?.dialogListSubtitleRes ?: 0)
            .setTopBtnText(screenConfiguration?.confirmationButtonTextRes ?: 0)
            .setTopBtnType(ButtonType.FILLED)
            .setBottomBtnText(R.string.cancel)
            .setTopClickListener {
                deleteAllUsers()
                updateItems()
            }
            .show(childFragmentManager)
    }

    private fun showDeleteUserDialog(
        userName: String?,
        userId: Long,
        adapterPosition: Int
    ) {
        MeeraConfirmDialogBuilder()
            .setHeader(getString(screenConfiguration?.dialogItemTitleRes ?: 0, userName))
            .setDescription(screenConfiguration?.dialogItemSubtitleRes ?: 0)
            .setTopBtnText(screenConfiguration?.confirmationButtonTextRes ?: 0)
            .setTopBtnType(ButtonType.FILLED)
            .setBottomBtnText(R.string.cancel)
            .setTopClickListener {
                removeUserFromList(listOf(userId), adapterPosition)
                findNavController().popBackStack()
            }
            .show(childFragmentManager)
    }

    private fun emptyListState(){
        binding.let {
            it.tvSettingsUserListDescription.gone()
            it.rvSettingsUserListRecycler.gone()
            it.vSettingsUserListDeleteAllBtn.gone()
            it.ivSettingsUserListEmptyPic.visible()
            it.tvSettingsUserListEmptyText.visible()
        }
    }


    private fun initRecycler() {
        val layoutM = LinearLayoutManager(context)
        initRecycler(layoutM)
        updateItems()
        binding.rvSettingsUserListRecycler.addOnScrollListener(object : RecyclerPaginationListener(layoutM) {
            override fun loadMoreItems() {
                showUsersRequest(BASE_LIST_USERS_PAGE_SIZE, baseAdapter?.currentList?.size ?: 0)
            }
            override fun isLastPage(): Boolean = lastPage()

            override fun isLoading(): Boolean = loading()
        })
    }

    private fun initRecycler(layoutM: RecyclerView.LayoutManager){
        binding.rvSettingsUserListRecycler.apply {
            binding.vSettingsUserListNavView.addScrollableView(this)
            setHasFixedSize(true)
            layoutManager = layoutM
            adapter = baseAdapter
        }

        binding.rvSettingsShimmerRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = shimmerAdapter
            shimmerAdapter.submitList(listShimmer)
        }
    }

    private fun showCommonErrorMessage() {
        UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(R.string.error_try_later),
                    avatarUiState = AvatarUiState.ErrorIconState
                ),
                duration = BaseTransientBottomBar.LENGTH_SHORT,
                dismissOnClick = true,
            )

        ).show()
    }

    companion object {
        const val BASE_LIST_USERS_PAGE_SIZE = 20
    }
}
