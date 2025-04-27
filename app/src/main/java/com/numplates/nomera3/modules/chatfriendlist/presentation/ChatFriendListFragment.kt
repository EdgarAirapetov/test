package com.numplates.nomera3.modules.chatfriendlist.presentation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentChatFriendListBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyChatCreatedFromWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.findfriends.AmplitudeFindFriendsWhereProperty
import com.numplates.nomera3.modules.chat.ChatFragmentNew
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitType
import com.numplates.nomera3.modules.search.ui.fragment.SearchMainFragment
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FROM_WHERE_CHAT_CREATED
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_WHERE_CHAT_OPEN
import com.numplates.nomera3.presentation.view.fragments.HorizontalLineDivider
import com.numplates.nomera3.presentation.view.fragments.newchat.ChatGroupFriendListFragment
import com.numplates.nomera3.presentation.view.utils.NToast
import kotlinx.coroutines.launch

class ChatFriendListFragment : BaseFragmentNew<FragmentChatFriendListBinding>() {

    private val friendListHeaderAdapter = CreateGroupChatAdapter {
        friendListViewModel.handleAction(ChatFriendListAction.OpenNewChatScreen)
    }
    private val friendListAdapter = ChatFriendListAdapter { friend -> startDialog(friend) }
    private val concatAdapter = ConcatAdapter(friendListHeaderAdapter,friendListAdapter)
    private val friendListViewModel by viewModels<ChatFriendListViewModel> { App.component.getViewModelFactory() }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentChatFriendListBinding
        get() = FragmentChatFriendListBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupStatusBar(view)
        setupToolbar()
        setupRecyclerList()
        setupLiveDataObservers()
        setupFindFriendsView()
    }

    override fun onResume() {
        super.onResume()
        setupFriendSearch()
    }

    private fun showGroupChatHeader(shouldShow: Boolean) {
        if (shouldShow) {
            concatAdapter.addAdapter(0, friendListHeaderAdapter)
        } else {
            concatAdapter.removeAdapter(friendListHeaderAdapter)
        }
    }

    private fun goToNewChatScreen() = add(ChatGroupFriendListFragment(), Act.LIGHT_STATUSBAR)

    private fun goToSearchScreen() = add(
        SearchMainFragment(),
        Act.LIGHT_STATUSBAR,
        Arg(
            IArgContainer.ARG_FIND_FRIENDS_OPENED_FROM_WHERE,
            AmplitudeFindFriendsWhereProperty.MESSAGE
        )
    )

    private fun setupFindFriendsView() {
        binding?.placeholderLayout?.tvPlaceholderAction?.setOnClickListener {
            goToSearchScreen()
        }
    }

    private fun setupStatusBar(holder: View?) {
        val statusBar = holder?.findViewById<View>(R.id.status_bar_friend_list)
        val params = statusBar?.layoutParams as AppBarLayout.LayoutParams
        params.height = context.getStatusBarHeight()
        statusBar.layoutParams = params
    }

    private fun setupToolbar() {
        binding?.btnGoBack?.setOnClickListener {
            binding?.sbvFriendlistFilter?.clearText()
            act.onBackPressed()
        }
    }

    private fun setupRecyclerList() {
        val horizontalPaddingValue = resources.getDimension(R.dimen.padding16).toInt()
        val dividerDrawable = ContextCompat.getDrawable(
            act as Context,
            R.drawable.drawable_friend_list_divider_decoration
        )
        val horizontalLineDivider =
            if (dividerDrawable != null) {
                HorizontalLineDivider(
                    dividerDrawable = dividerDrawable,
                    paddingLeft = horizontalPaddingValue,
                    paddingRight = horizontalPaddingValue
                )
            } else {
                null
            }

        binding?.rvFriendList?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = concatAdapter
            if (horizontalLineDivider != null) {
                addItemDecoration(horizontalLineDivider)
            }
        }
    }

    private fun setupLiveDataObservers() {
        friendListViewModel.friendList.observe(viewLifecycleOwner) {
            friendListAdapter.submitList(it)
        }

        friendListViewModel.viewEventsLiveData.observe(viewLifecycleOwner) { viewEvent ->
            handleEvent(viewEvent)
        }
    }

    private fun handleEvent(viewEvent: ChatFriendListViewEvent) {
        when (viewEvent) {
            is ChatFriendListViewEvent.Empty -> showEmpty()
            is ChatFriendListViewEvent.FriendList -> showFriendList()
            is ChatFriendListViewEvent.NoFriends -> showFindFriends()
            is ChatFriendListViewEvent.FailedToLoadFriendList -> showFailedToLoadFriendList()
            is ChatFriendListViewEvent.FailedToLoadFriendProfile -> showFailedToLoadFriendProfile()
            is ChatFriendListViewEvent.EmptySearchResult -> showEmptySearchResult()
            is ChatFriendListViewEvent.OpenNewChatScreen -> goToNewChatScreen()
            is ChatFriendListViewEvent.ChangeNewGroupChatButtonVisibility ->
                showGroupChatHeader(shouldShow = viewEvent.isVisible)
        }
    }

    private fun showEmpty() {
        binding?.apply {
            placeholderLayout.root.gone()
            sbvFriendlistFilter.gone()
            separator.gone()
            rvFriendList.gone()
        }
    }

    private fun showFriendList() {
        binding?.apply {
            sbvFriendlistFilter.visible()
            separator.visible()
            rvFriendList.visible()
            placeholderLayout.root.gone()
        }
    }

    private fun showFindFriends() {
        binding?.apply {
            sbvFriendlistFilter.gone()
            separator.gone()
            rvFriendList.gone()
            placeholderLayout.apply {
                ivPlaceholderImage.setImageDrawable(
                    ContextCompat.getDrawable(act as Context, R.drawable.ic_empty_friends)
                )
                tvPlaceholderText.text = getString(R.string.empty_friends_placeholder_new)
                tvPlaceholderAction.visible()
                root.visible()
            }
        }
    }

    private fun showEmptySearchResult() {
        binding?.apply {
            sbvFriendlistFilter.visible()
            separator.visible()
            rvFriendList.gone()
            placeholderLayout.apply {
                ivPlaceholderImage.setImageDrawable(
                    ContextCompat.getDrawable(act as Context, R.drawable.ic_empty_search_results)
                )
                tvPlaceholderText.text = getString(R.string.friendlist_search_results_empty)
                tvPlaceholderAction.gone()
                root.visible()
            }
        }
    }

    private fun showFailedToLoadFriendList() =
        showToast(getString(R.string.chat_friend_list_error_load))

    private fun showFailedToLoadFriendProfile() =
        showToast(getString(R.string.chat_friend_list_error_dialog))

    private fun showToast(message: String) {
        NToast.with(view).text(message).show()
    }

    private fun getCurrentNavigatorPosition() = act.navigatorViewPager.currentItem

    private fun startDialog(friend: UserSimple) {
        lifecycleScope.launch {
            val chatInitProfileSettings = friendListViewModel.getChatInitProfileSettings(friend)
            if (chatInitProfileSettings != null) {
                replace(
                    getCurrentNavigatorPosition(),
                    ChatFragmentNew(), Act.LIGHT_STATUSBAR,
                    Arg(ARG_WHERE_CHAT_OPEN, AmplitudePropertyWhere.NEW_MESSAGE),
                    Arg(ARG_FROM_WHERE_CHAT_CREATED, AmplitudePropertyChatCreatedFromWhere.COMMUNICATION),
                    Arg(
                        IArgContainer.ARG_CHAT_INIT_DATA, ChatInitData(
                            initType = ChatInitType.FROM_PROFILE,
                            userId = friend.userId
                        )
                    )
                )
            }
        }
    }

    private fun setupFriendSearch() {
        binding?.apply {
            val flow = sbvFriendlistFilter.getTextChangesFlow()
                .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
            lifecycleScope.launchWhenResumed {
                flow.collect {
                    friendListViewModel.onNewSearchQuery(it.toString()) }
            }
        }
    }
}
