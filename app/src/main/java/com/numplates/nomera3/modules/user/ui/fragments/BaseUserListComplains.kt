package com.numplates.nomera3.modules.user.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.numplates.nomera3.modules.user.BaseListFragmentsBottomSheet
import com.numplates.nomera3.modules.user.ui.OnBottomSheetFragmentsListener
import com.numplates.nomera3.modules.user.ui.entity.UserComplainEntity
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.meera.core.extensions.click
import com.numplates.nomera3.modules.user.ui.adapter.UserComplainReasonAdapter

abstract class BaseUserListComplains<T : ViewBinding> : BaseFragmentNew<T>() {

    private var interactionCallback: OnBottomSheetFragmentsListener? = null

    abstract fun getListComplains(): List<UserComplainEntity>

    abstract fun getRecycler(): RecyclerView?

    abstract fun getCloseView(): View?

    abstract fun scrollTopShadowsVisibility(isVisible: Boolean)

    abstract fun scrollBottomShadowVisibility(isVisible: Boolean)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is BaseListFragmentsBottomSheet) {
            interactionCallback = parentFragment as OnBottomSheetFragmentsListener
        } else {
            throw RuntimeException("The parent fragment must implement OnBottomSheetFragmentsListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        initClickListeners()
    }

    private fun initRecycler() {
        val adapter = UserComplainReasonAdapter()
        val layoutManager = LinearLayoutManager(requireContext())
        getRecycler()?.let { rv ->
            rv.setHasFixedSize(false)
            rv.layoutManager = layoutManager
            rv.adapter = adapter
        }
        adapter.collection = getListComplains()
        adapter.itemClickListener = { item ->
            if (!item.isShowDetail) {
                interactionCallback?.onReturnResult(item)
            } else {
                item.transitFragmentClass?.let {
                    interactionCallback?.onNextFragment(it)
                }
            }
        }

        initRecyclerScrollListener(layoutManager)
    }

    private fun initRecyclerScrollListener(layoutManager: LinearLayoutManager) {
        val itemSize = getListComplains().size
        getRecycler()?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val firstItemPos = layoutManager.findFirstCompletelyVisibleItemPosition()
                val lastItemPos = layoutManager.findLastCompletelyVisibleItemPosition()

                if (firstItemPos > 0) {
                    scrollTopShadowsVisibility(isVisible = true)
                } else {
                    scrollTopShadowsVisibility(isVisible = false)
                }

                if (lastItemPos < itemSize - 1) {
                    scrollBottomShadowVisibility(isVisible = true)
                } else {
                    scrollBottomShadowVisibility(isVisible = false)
                }
            }
        })
    }

    private fun initClickListeners() {
        getCloseView()?.click {
            interactionCallback?.onCloseMenu()
        }
    }

    fun onBackFragment() {
        interactionCallback?.onBackFragment()
    }

}
