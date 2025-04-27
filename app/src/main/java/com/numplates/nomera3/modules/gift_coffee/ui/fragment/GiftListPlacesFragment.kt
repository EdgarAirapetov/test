package com.numplates.nomera3.modules.gift_coffee.ui.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.meera.core.extensions.clearText
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisibleAnimation
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.visible
import com.meera.core.extensions.visibleAnimation
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentGiftListPlacesBinding
import com.numplates.nomera3.modules.gift_coffee.ui.adapter.GiftListPlacesAdapter
import com.numplates.nomera3.modules.gift_coffee.ui.entity.GiftPlaceEntity
import com.numplates.nomera3.modules.gift_coffee.ui.viewevent.GiftListPlacesViewEvent
import com.numplates.nomera3.modules.gift_coffee.ui.viewmodel.GiftListPlacesViewModel
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.meera.core.utils.NSnackbar
import com.numplates.nomera3.presentation.view.utils.NToast
import com.meera.core.utils.pagination.RecyclerPaginationListener
import com.meera.core.extensions.empty
import com.meera.core.extensions.hideKeyboard
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.concurrent.TimeUnit

class GiftListPlacesFragment : BaseFragmentNew<FragmentGiftListPlacesBinding>() {

    private val viewModel by viewModels<GiftListPlacesViewModel>()
    private val listAdapter = GiftListPlacesAdapter()

    private val disposables = CompositeDisposable()

    private var queryText: String = String.empty()

    private var successCopySnackbar: NSnackbar? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentGiftListPlacesBinding
        get() = FragmentGiftListPlacesBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initObservables()
    }

    override fun onStart() {
        super.onStart()
        initSearchPlaces()
    }

    override fun onStop() {
        super.onStop()
        successCopySnackbar?.dismiss()
        disposables.clear()
    }

    private fun initViews() {
        binding?.ivBackArrowToolbar?.click { act.onBackPressed() }
        binding?.ivClearText?.click { binding?.etSearch?.clearText() }
        binding?.noSearchResultPlaceholder?.root?.setMargins(top = 41.dp)
        listAdapter.clickListener = ::copyAddress
        setClearTextTouchDelegate(binding?.ivClearText)
        binding?.pbLoading?.visible()

        val layoutMgr = LinearLayoutManager(act)
        binding?.rvPlaces?.apply {
            setHasFixedSize(true)
            layoutManager = layoutMgr
            adapter = listAdapter
        }

        // Handle list pagination
        binding?.rvPlaces?.addOnScrollListener(object : RecyclerPaginationListener(layoutMgr) {
            override fun loadMoreItems() {
                viewModel.getCoffeeAddress(
                        query = queryText,
                        offset = listAdapter.itemCount
                )
            }

            override fun isLastPage(): Boolean = viewModel.isLastPage

            override fun isLoading(): Boolean = viewModel.isLoading
        })

        // Handle empty list placeholder
        listAdapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if(listAdapter.itemCount > 0) {
                    binding?.noSearchResultPlaceholder?.root?.gone()
                } else {
                    binding?.pbLoading?.gone()
                    binding?.noSearchResultPlaceholder?.root?.visible()
                }
            }
        })
    }

    private fun initObservables() {
        viewModel.liveCoffeePlaces.observe(viewLifecycleOwner, ::fetchAdapter)
        viewModel.liveViewEvents.observe(viewLifecycleOwner, ::handleViewEvents)
    }

    private fun fetchAdapter(coffeePlaces: List<GiftPlaceEntity?>?) {
        binding?.pbLoading?.gone()
        listAdapter.addData(coffeePlaces ?: emptyList())
    }

    private fun handleViewEvents(event: GiftListPlacesViewEvent) {
        when(event){
            is GiftListPlacesViewEvent.OnErrorGetCoffeeAddress -> showErrorMessage(event.message)
        }
    }

    private fun initSearchPlaces() {
        binding?.etSearch?.let { editText ->
            disposables.add(
                    RxTextView.textChanges(editText)
                            .map { text -> text.toString().trim() }
                            .distinctUntilChanged()
                            .debounce(300, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ text ->
                                this.queryText = text
                                if(text.isNotEmpty()){
                                    // search mode
                                    binding?.ivClearText?.visibleAnimation()
                                    listAdapter.clearList()
                                    binding?.pbLoading?.visible()
                                    viewModel.getCoffeeAddress(query = text)
                                } else {
                                    // show list mode
                                    binding?.ivClearText?.invisibleAnimation()
                                    listAdapter.clearList()
                                    binding?.pbLoading?.visible()
                                    viewModel.getCoffeeAddress()
                                }
                            }, { Timber.e(it) })
            )
        }
    }

    private fun copyAddress(item: GiftPlaceEntity?) {
        context?.hideKeyboard(requireView())
        val clipboard = act.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("clip_gift_receive_place", item?.address)
        clipboard.setPrimaryClip(clip)
        showSuccessCopySnackbar()
    }

    private fun showSuccessCopySnackbar() {
        successCopySnackbar = NSnackbar.with(act)
                .typeSuccess()
                .marginBottom(64)
                .text("${getString(R.string.general_address_copied)}😎")
                .durationLong()
                .show()
    }

    private fun showErrorMessage(@StringRes message: Int) {
        binding?.pbLoading?.gone()
        NToast.with(act)
                .typeSuccess()
                .text(getString(message))
                .typeError()
                .show()
    }

    private fun setClearTextTouchDelegate(imageView: ImageView?){
        val parent = view?.parent as View
        val extraSpace = 10.dp
        parent.post {
            val touchableArea = Rect()
            imageView?.getHitRect(touchableArea)
            touchableArea.top -= extraSpace
            touchableArea.bottom += extraSpace
            touchableArea.left -= extraSpace
            touchableArea.right += extraSpace
            parent.touchDelegate = TouchDelegate(touchableArea, imageView)
        }
    }

}
