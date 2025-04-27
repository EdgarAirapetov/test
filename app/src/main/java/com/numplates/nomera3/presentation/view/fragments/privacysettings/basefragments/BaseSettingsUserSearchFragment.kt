package com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.widget.RxTextView
import com.meera.core.extensions.clearText
import com.meera.core.extensions.empty
import com.meera.core.utils.pagination.RecyclerPaginationListener
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentBaseSettingsUserSearchBinding
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.BaseSettingsListSearchUsersAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

/**
 * Search users and add to settings
 */
abstract class BaseSettingsUserSearchFragment :
        BaseFragmentNew<FragmentBaseSettingsUserSearchBinding>(),
        BaseSettingsListSearchUsersAdapter.OnUserInteractionCallback {

    private val disposables = CompositeDisposable()

    lateinit var listAdapter: BaseSettingsListSearchUsersAdapter

    private var queryText: String = String.empty()

    var isSearchMode: Boolean by Delegates.observable(false) { onChange, old, new ->
        // Timber.e("isSearch mode (OBSERVABLE) $new")
        searchModeObservable(new)
    }


    lateinit var config: BaseSettingsUserSearchConfiguration

    abstract fun screenConfiguration(): BaseSettingsUserSearchConfiguration

    abstract fun onUsersSelectedDone()

    abstract fun searchUsers(text: String, offset: Int)

    abstract fun showLoadedUsers(offset: Int)

    abstract fun userAvatarClick(user: UserSimple)

    abstract fun userChecked(user: UserSimple, isChecked: Boolean)

    abstract fun lastPage(): Boolean

    abstract fun loading(): Boolean

    // true -> if text.length > 0
    abstract fun searchModeObservable(isSearchMode: Boolean)

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentBaseSettingsUserSearchBinding
        get() = FragmentBaseSettingsUserSearchBinding::inflate


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        config = screenConfiguration()
        initViews()
    }

    override fun onResume() {
        super.onResume()
        // Init RxSearch
        binding?.etSearchName?.let { et->
            disposables.add(
                    RxTextView.textChanges(et)
                            .map { text -> text.toString().trim() }
                            .distinctUntilChanged()
                            .debounce(300, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ text ->
                                this.queryText = text

                                if (text.isNotEmpty()) {
                                    //Timber.e("Show search users (BASE)")
                                    isSearchMode = true

                                    clearList()
                                    searchUsers(text, 0)
                                } else {
                                    //Timber.e("Show LOADED Users (BASE)")
                                    isSearchMode = false

                                    clearList()
                                    // Request first request (if needed) - Show already added users
                                    if (config.isRequestGetUsers) {
                                        showLoadedUsers(0)
                                    }
                                }
                            }, { error -> Timber.e("ERROR observe text changes: $error") })
            )
        }
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }


    private fun initViews() {
        binding?.ivBackSettingsUserSearch?.setOnClickListener { act.onBackPressed() }
        binding?.tvScreenTitleSettingsUserSearch?.text = config.screenTitle
        binding?.ivSelectDone?.setOnClickListener {
            onUsersSelectedDone()
        }

        binding?.ivClearText?.setOnClickListener {
            binding?.etSearchName?.clearText()
        }

        // Setup Recycler
        val layoutM = LinearLayoutManager(context)
        listAdapter = BaseSettingsListSearchUsersAdapter(mutableListOf(), this)
        binding?.rvUsers?.apply {
            setHasFixedSize(true)
            layoutManager = layoutM
            adapter = listAdapter
        }

        // Pagination
        binding?.rvUsers?.addOnScrollListener(object : RecyclerPaginationListener(layoutM) {
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

    /**
     * Should call from Fragment (live list users)
     */
    fun updateDataSet(users: MutableList<UserSimple>) {
        listAdapter.updateDataSet(users)
    }

    fun getAdapterItemCount() = listAdapter.itemCount


    private fun clearList() {
        listAdapter.clearList()
    }


    override fun onUserAvatarClick(user: UserSimple) {
        userAvatarClick(user)
    }

    override fun onUserChecked(user: UserSimple, position: Int, isChecked: Boolean) {
        userChecked(user, isChecked)

        // Set checked in Adapter
        if (isChecked) {
            listAdapter.enableCheckbox(position)
        } else
            listAdapter.disableCheckbox(position)
    }


    /**
     * Set done button colored if list has checked items
     */
    fun changeColorDoneButton(isHasChecked: Boolean) {
        if (isHasChecked) {
            binding?.ivSelectDone?.setColorFilter(ContextCompat.getColor(act, R.color.colorPrimary))
        } else
            binding?.ivSelectDone?.setColorFilter(ContextCompat.getColor(act, R.color.ui_gray))
    }


    data class BaseSettingsUserSearchConfiguration(
            val screenTitle: String,
            val isRequestGetUsers: Boolean = false,
            val isShowEmptyResultPlaceholder: Boolean = false
    )


    companion object {
        const val BASE_LIST_USERS_PAGE_SIZE = 20
    }

}
