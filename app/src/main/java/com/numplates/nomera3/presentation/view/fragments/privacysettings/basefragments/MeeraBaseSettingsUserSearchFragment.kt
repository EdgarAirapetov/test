package com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.core.utils.pagination.RecyclerPaginationListener
import com.meera.db.models.userprofile.UserSimple
import com.noomeera.nmrmediatools.extensions.hideKeyboard
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraMomentSettingsUserAddFragmentBinding
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraBaseSettingsListSearchUsersAdapter
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraBaseSettingsSearchUserAction
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraBaseSettingsUserAddListShimmerAdapter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

/**
 * Search users and add to settings
 */

const val ARG_CHANGE_LIST_USER_REQUEST_KEY = "argChangeListUserRequestKey"
const val ARG_CHANGE_LIST_USER_KEY = "argChangeListUserKey"
private const val SEARCH_INPUT_TIMEOUT = 500L
private const val MARGIN_BOTTOM_LIST_USER= 80
abstract class MeeraBaseSettingsUserSearchFragment : MeeraBaseDialogFragment(
    R.layout.meera_moment_settings_user_add_fragment,
    ScreenBehaviourState.Full
) {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    val listAdapter: MeeraBaseSettingsListSearchUsersAdapter by lazy {
        MeeraBaseSettingsListSearchUsersAdapter(this@MeeraBaseSettingsUserSearchFragment::initSettingsUserSearch)
    }

    private val shimmerAdapter = MeeraBaseSettingsUserAddListShimmerAdapter()

    var isSearchMode: Boolean by Delegates.observable(false) { onChange, old, new ->
        searchModeObservable(new)
    }
    var config: BaseSettingsUserSearchConfiguration? = null
    private var queryText: String = String.empty()
    private val disposables = CompositeDisposable()
    private var listCheckUser = mutableListOf<UserSimple>()

    abstract fun screenConfiguration(): BaseSettingsUserSearchConfiguration

    abstract fun onUsersSelectedDone(users: List<UserSimple>)

    abstract fun searchUsers(text: String, offset: Int)

    abstract fun showLoadedUsers(offset: Int)

    abstract fun userAvatarClick(user: UserSimple)

    abstract fun lastPage(): Boolean

    abstract fun loading(): Boolean

    abstract fun searchModeObservable(isSearchMode: Boolean)
    abstract fun confirmButtonClickResult(isConfirmed: Boolean)
    abstract fun sendNotificationClosedFragment()

    protected val binding by viewBinding(MeeraMomentSettingsUserAddFragmentBinding::bind)

    private val listShimmer = List(8){""}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        config = screenConfiguration()
        initViews()
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onResume() {
        super.onResume()
        showLoadedUsers(0)
        var delayedUsers: Disposable? = null
        binding.vMomentsSettingsAddSearch.apply {
            if (searchInputText.isEmpty()){
                doAfterSearchTextChanged { searchUserName ->
                    delayedUsers?.dispose()
                    delayedUsers = loadUserList(searchUserName)
                }
            } else {
                delayedUsers?.dispose()
                delayedUsers = loadUserList(searchInputText)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sendNotificationClosedFragment()
    }

    /**
     * Should call from Fragment (live list users)
     */
    fun updateDataSet(users: MutableList<UserSimple>) {
        if (users.size > 0) {
            listAdapter.submitList(users)
            binding.vDividerElevation.visible()
        } else {
            binding.vDividerElevation.gone()
        }
    }

    fun getAdapterItemCount() = listAdapter.itemCount

    private fun initViews() {
        binding.vMomentsSettingsAddNavView.backButtonClickListener = { findNavController().popBackStack() }
        binding.vMomentsSettingsAddNavView.title = config?.screenTitle
        binding.vMomentsSettingsAddSearch.setButtonText(getString(R.string.cancel))
        binding.vMomentsSettingsAddSearch.setCloseButtonClickedListener {
            hideKeyboard()
        }
        initClickListenerConfirmButton()
        val layoutM = LinearLayoutManager(context)
        initRecycler(layoutM)
        binding?.rvMomentsSettingsAdd?.addOnScrollListener(object : RecyclerPaginationListener(layoutM) {
            override fun loadMoreItems() {
                val offset = listAdapter.itemCount
                if (isSearchMode) {
                    searchUsers(queryText, offset)
                } else {
                    showLoadedUsers(offset)
                }
            }

            override fun isLastPage(): Boolean = lastPage()

            override fun isLoading(): Boolean = loading()
        })
    }

    private fun initRecycler(layoutM: RecyclerView.LayoutManager){
        binding?.rvMomentsSettingsAdd?.apply {
            binding?.vMomentsSettingsAddNavView?.addScrollableView(this)
            layoutManager = layoutM
            adapter = listAdapter
            addItemDecoration(RecyclerItemDecoration())
        }
        binding.rvMomentsSettingsAdd.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager =
                    binding.rvMomentsSettingsAdd.layoutManager as? LinearLayoutManager
                val firstVisibleItemPosition = layoutManager?.findFirstCompletelyVisibleItemPosition()
                binding.vDividerElevation.isVisible = firstVisibleItemPosition != 0
            }
        })
        binding?.rvShimmerSettingsAdd?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = shimmerAdapter
            shimmerAdapter.submitList(listShimmer)
        }
    }

    fun shimmerVisibility(visible: Boolean){
        if (visible){
            binding?.rvShimmerSettingsAdd?.visible()
            binding.vMomentsSettingsEmptyStateGroup.gone()
            binding?.rvMomentsSettingsAdd?.gone()
        }else{
            binding?.rvShimmerSettingsAdd?.gone()
            binding?.rvMomentsSettingsAdd?.visible()
        }
    }

    private fun initClickListenerConfirmButton(){
        if (config?.isShowConfirmBottomButton == false){
            binding?.tvMomentsSettingsNavViewDone?.setThrottledClickListener {
                onUsersSelectedDone(listCheckUser)
                confirmButtonClickResult(false)
                findNavController().popBackStack()
            }
        } else {
            binding?.vMomentsSettingsDone?.setThrottledClickListener {
                onUsersSelectedDone(listCheckUser)
                confirmButtonClickResult(true)
                findNavController().popBackStack()
            }
        }
    }

    private fun initSettingsUserSearch(action: MeeraBaseSettingsSearchUserAction) {
        if (action is MeeraBaseSettingsSearchUserAction.UserChecked) onUserChecked(action.user)
    }

    private fun clearList() {
        listAdapter.submitList(listOf())
        listAdapter.clearCheckListUser()
        listCheckUser = mutableListOf()
        initVisibilityConfirmButton()
    }

    private fun onUserChecked(user: UserSimple) {
        val foundUser = listCheckUser.find { it.userId == user.userId}
        if (foundUser != null) {
            listCheckUser.remove(foundUser)
        } else {
            listCheckUser.add(user)
        }
        initVisibilityConfirmButton()
    }

    private fun initVisibilityConfirmButton(){
        when{
            listCheckUser.size > 0 && config?.isShowConfirmBottomButton == false -> {
                binding.tvMomentsSettingsNavViewDone.visible()
            }
            listCheckUser.size == 0 && config?.isShowConfirmBottomButton == false -> {
                binding.tvMomentsSettingsNavViewDone.gone()
            }
            listCheckUser.size > 0 && config?.isShowConfirmBottomButton == true -> {
                binding.vMomentsSettingsDone.visible()
            }
            listCheckUser.size == 0 && config?.isShowConfirmBottomButton == true -> {
                binding.vMomentsSettingsDone.gone()
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun loadUserList(userName: String) =
        Observable.just(userName)
            .debounce(SEARCH_INPUT_TIMEOUT, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { userName ->
                this.queryText = userName
                if (userName.isNotEmpty()) {
                    clearList()
                    shimmerVisibility(true)
                    searchUsers(userName, 0)
                } else {
                    clearList()
                    if (config?.isRequestGetUsers == true) {
                        showLoadedUsers(0)
                    }
                }
                initVisibilityConfirmButton()
            }

    inner class RecyclerItemDecoration: RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            if (parent.getChildAdapterPosition(view) == parent.adapter!!.itemCount - 1) {
                outRect.bottom = MARGIN_BOTTOM_LIST_USER.dp
            }
        }
    }

    data class BaseSettingsUserSearchConfiguration(
        val screenTitle: String,
        val isRequestGetUsers: Boolean = false,
        val isShowEmptyResultPlaceholder: Boolean = true,
        val isShowConfirmBottomButton: Boolean = true
    )

    companion object {
        const val BASE_LIST_USERS_PAGE_SIZE = 20
    }
}


