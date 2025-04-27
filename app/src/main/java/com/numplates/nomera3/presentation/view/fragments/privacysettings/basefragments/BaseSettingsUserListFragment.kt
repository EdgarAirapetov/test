package com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.core.utils.pagination.RecyclerPaginationListener
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentBaseSettingsUserListBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_MESSAGE_SETTINGS_USER_EXCLUSIONS_COUNT
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.BaseSettingsUserListAdapter
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.BaseSettingsUserListAdapter.Companion.ITEM_TOP_SPACE
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.BaseSettingsUserListAdapter.Companion.ITEM_TYPE_ADD_EXCLUSION
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.BaseSettingsUserListAdapter.Companion.ITEM_TYPE_EXCLUSION_AMOUNT
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.viewmodel.viewevents.ListUsersSearchViewEvent

/**
 * Show already added users in settings
 */
abstract class BaseSettingsUserListFragment : BaseFragmentNew<FragmentBaseSettingsUserListBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentBaseSettingsUserListBinding
        get() = FragmentBaseSettingsUserListBinding::inflate

    private var usersAmount: Long? = 0

    private lateinit var listAdapter: BaseSettingsUserListAdapter

    private lateinit var screenConfiguration: BaseSettingsUserListConfiguration

    abstract fun screenConfiguration(): BaseSettingsUserListConfiguration

    abstract fun transitToAddUsersFragment()

    abstract fun removeUserFromList(userIds: List<Long>, adapterPosition: Int)

    abstract fun removeAllUsersFromList(userIds: List<Long>)

    abstract fun showUsersRequest(limit: Int, offset: Int)

    abstract fun lastPage(): Boolean

    abstract fun loading(): Boolean

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        usersAmount = arguments?.getLong(ARG_MESSAGE_SETTINGS_USER_EXCLUSIONS_COUNT, 0L)
        screenConfiguration = screenConfiguration()
        initViews()
        initRecycler()
    }

    override fun onStart() {
        super.onStart()
        listAdapter.clearUserItems()
        if (delayUpdateOnReturnTransition()) {
            doDelayed(1500L) {
                updateItems()
            }
        } else {
            updateItems()
        }
    }

    private fun updateItems() {
        initRecycler()
        showUsersRequest(BASE_LIST_USERS_PAGE_SIZE, 0)
    }

    open fun delayUpdateOnReturnTransition(): Boolean = true

    private fun initViews() {
        binding?.ivBackBaseSettingsUserList?.setThrottledClickListener { act.onBackPressed() }
        binding?.tvToolbarTitleSettingsUserList?.text = screenConfiguration.screenTitle //screenTitle()

        // Init recycler
        listAdapter = BaseSettingsUserListAdapter(screenConfiguration.isShowDeleteAllItem)

        val layoutM = LinearLayoutManager(context)

        binding?.rvSettingsUserList?.apply {
            setHasFixedSize(true)
            layoutManager = layoutM
            adapter = listAdapter
        }

        // Click listeners
        listAdapter.clickAddExclusionListener = {
            transitToAddUsersFragment()
        }

        listAdapter.clickUserMenuListener = { item, position ->
            showRemoveFromExclusionsMenu(item.user, position)
        }

        listAdapter.clickDeleteAllUsers = {
            removeAllUsersFromList(listAdapter.getAllUsersIds())
        }

        listAdapter.clickUserListener = { item, _ ->
            add(
                UserInfoFragment(), Act.LIGHT_STATUSBAR,
                Arg(IArgContainer.ARG_USER_ID, item.user?.userId),
                Arg(IArgContainer.ARG_TRANSIT_FROM, AmplitudePropertyWhere.SETTINGS.property)
            )
        }

        // Pagination
        binding?.rvSettingsUserList?.addOnScrollListener(object : RecyclerPaginationListener(layoutM) {
            override fun loadMoreItems() {
                showUsersRequest(BASE_LIST_USERS_PAGE_SIZE, listAdapter.getUserCount())
            }

            override fun isLastPage(): Boolean = lastPage()

            override fun isLoading(): Boolean = loading()
        })
    }


    private fun initRecycler() {
        val items = mutableListOf<BaseSettingsUserListAdapter.UserExclusionsItem>()
        if (screenConfiguration.isShowAddUserItem) {
            items.add(BaseSettingsUserListAdapter.UserExclusionsItem(ITEM_TYPE_ADD_EXCLUSION,
                    null, 0, screenConfiguration.addUserItemTitle))
        } else {
            items.add(BaseSettingsUserListAdapter.UserExclusionsItem(ITEM_TOP_SPACE, null, 0))
        }
        items.add(BaseSettingsUserListAdapter.UserExclusionsItem(ITEM_TYPE_EXCLUSION_AMOUNT, null, usersAmount
                ?: 0L))

        listAdapter.collection = items
    }


    /**
     * Fetch adapter list users
     */
    fun addUsersToAdapter(users: List<UserSimple>) {
        // Timber.e("ADD Users to ADAPT size:${users.size}")
        if (users.isNotEmpty()) {
            val items = mutableListOf<BaseSettingsUserListAdapter.UserExclusionsItem>()
            users.forEach { user ->
                items.add(BaseSettingsUserListAdapter.UserExclusionsItem(
                        BaseSettingsUserListAdapter.ITEM_TYPE_USER, user, 0))
            }
            listAdapter.addItems(items)
        }
    }


    /**
     * Call inside live progress visibility
     */
    fun loadProgressVisibility(isVisible: Boolean) {
        if (isVisible) {
            binding?.pbLoadProgress?.visible()
        } else
            binding?.pbLoadProgress?.gone()
    }

    /**
     * Call inside live view events
     */
    fun handleEvents(event: ListUsersSearchViewEvent) {
        when (event) {
            is ListUsersSearchViewEvent.OnErrorLoadUsers -> showCommonErrorMessage()
            is ListUsersSearchViewEvent.OnSuccessRemoveUser -> {
                loadProgressVisibility(false)
                listAdapter.removeItemAndDecreaseExclusionsCount(event.position)

                val userItems = listAdapter.collection.find {
                        item -> item.itemType == BaseSettingsUserListAdapter.ITEM_TYPE_USER
                }
                if (userItems == null) act.onBackPressed()
            }
            is ListUsersSearchViewEvent.OnFailureRemoveUser -> showCommonErrorMessage()
            is ListUsersSearchViewEvent.OnSuccessRemoveAllUsers -> act.onBackPressed()
            is ListUsersSearchViewEvent.OnErrorRemoveAllUsers -> showCommonErrorMessage()
            else -> {}
        }
    }


    fun changeExclusionUsersCount(count: Long) {
        listAdapter.changeExclusionsCount(count)
    }

    private fun showRemoveFromExclusionsMenu(user: UserSimple?, adapterPosition: Int) {
        val menu = MeeraMenuBottomSheet(activity)
        menu.addItem(screenConfiguration.removeListMenuText, screenConfiguration.removeListMenuIcon) {
            user?.let {
                removeUserFromList(mutableListOf(user.userId), adapterPosition)
            }
        }
        menu.show(childFragmentManager)
    }


    fun showDeleteAllConfirmationDialog(action: () -> Unit) {
        AlertDialog.Builder(context)
                .setTitle(getString(R.string.delete_everyone))
                .setMessage(getString(R.string.confirm_delete_list_settings))
                .setPositiveButton(R.string.delete_all) { dialog, which ->
                    action.invoke()
                }
                .setNegativeButton(R.string.general_cancel) { dialog, which ->
                    dialog.dismiss()
                }
                .show()
    }


    private fun showCommonErrorMessage() {
        NToast.with(view)
                .typeError()
                .text(getString(R.string.error_try_later))
                .show()
    }


    data class BaseSettingsUserListConfiguration(
            val screenTitle: String,
            val isShowAddUserItem: Boolean,
            val addUserItemTitle: String,
            var removeListMenuIcon: Int = R.drawable.block_user_menu_item_v2,
            var removeListMenuText: Int = R.string.settings_remove_from_exclusions,
            var isShowDeleteAllItem: Boolean = true,
    )


    companion object {
        const val BASE_LIST_USERS_PAGE_SIZE = 20
    }
}
