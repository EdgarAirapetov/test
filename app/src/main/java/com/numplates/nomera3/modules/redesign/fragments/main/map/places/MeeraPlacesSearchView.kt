package com.numplates.nomera3.modules.redesign.fragments.main.map.places

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.meera.core.extensions.debouncedAction1
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.onMeasured
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.showKeyboard
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraViewPlacesSearchBinding
import com.numplates.nomera3.modules.places.ui.PlacesDividerItemDecorator
import com.numplates.nomera3.modules.places.ui.adapter.PlacesAdapter
import com.numplates.nomera3.modules.places.ui.model.PlacesSearchEvent
import com.numplates.nomera3.modules.places.ui.model.PlacesSearchUiState

class MeeraPlacesSearchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private var eventListener: ((PlacesSearchEvent) -> Unit)? = null

    private val binding: MeeraViewPlacesSearchBinding = LayoutInflater.from(context)
        .inflate(R.layout.meera_view_places_search, this, false)
        .apply(::addView)
        .let(MeeraViewPlacesSearchBinding::bind)

    private var placesAdapter: PlacesAdapter? = null
    private var behavior: BottomSheetBehavior<*>? = null

    init {
        onMeasured {
            behavior = createBottomSheetBehavior()
        }
        binding.rvPlacesSearchResults.addItemDecoration(PlacesDividerItemDecorator(binding.root.context))
        placesAdapter = PlacesAdapter { item ->
            eventListener?.invoke(PlacesSearchEvent.PlaceSelected(item.place))
        }
        binding.etPlacesSearchInput.forceShowBtnClose = false

        binding.rvPlacesSearchResults.adapter = placesAdapter
        binding.rvPlacesSearchResults.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        context.hideKeyboard(this@MeeraPlacesSearchView)
                    }
                }
            }
        })
        binding.ivClose.setThrottledClickListener {
            eventListener?.invoke(PlacesSearchEvent.Canceled)
        }

        binding.etPlacesSearchInput.setCloseButtonClickedListener {
            binding.etPlacesSearchInput.hideKeyboard()
            binding.etPlacesSearchInput.forceShowBtnClose = false
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            binding.etPlacesSearchInput.forceBtnCloseVisibility(imeVisible)
            insets
        }
    }

    private fun createBottomSheetBehavior(): BottomSheetBehavior<*> {
        return BottomSheetBehavior.from(findViewById<FrameLayout>(R.id.vg_map_places_search_bottomsheet)).apply {
            isHideable = true
            isDraggable = true
            skipCollapsed = true
            isHideable = true
            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) = Unit
                override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
            })
            state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    fun expand() {
        behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val debouncedSearch = binding.root.findViewTreeLifecycleOwner()
            ?.lifecycleScope
            ?.debouncedAction1<String>(SEARCH_DEBOUNCE_DELAY) { searchText ->
                eventListener?.invoke(PlacesSearchEvent.PlaceSearched(searchText))
            }
        binding.etPlacesSearchInput.doAfterSearchTextChanged {
            val text = it
            if (text.length >= MIN_TEXT_LENGTH_TO_SEARCH) {
                debouncedSearch?.invoke(text)
            } else {
                eventListener?.invoke(PlacesSearchEvent.SearchCleared)
            }
        }
    }

    fun setEventListener(listener: ((PlacesSearchEvent) -> Unit)?) {
        eventListener = listener
    }

    fun setState(state: PlacesSearchUiState) {
        when (state) {
            PlacesSearchUiState.Error -> {
                binding.ivPlacesSearchInfoImage.setImageResource(R.drawable.img_places_search_error)
                binding.tvPlacesSearchInfoText.text = resources.getString(R.string.places_search_error)
            }
            is PlacesSearchUiState.Result -> {
                placesAdapter?.submitList(state.places)
            }
            PlacesSearchUiState.NoResults -> {
                binding.ivPlacesSearchInfoImage.setImageResource(R.drawable.img_places_search_no_results)
                binding.tvPlacesSearchInfoText.text = resources.getString(R.string.general_search_no_results)
            }
            else ->  {
                expand()
                placesAdapter?.submitList(emptyList())
                placesAdapter?.notifyDataSetChanged()
            }
        }
        val isInfoUiVisible = state is PlacesSearchUiState.NoResults || state is PlacesSearchUiState.Error
        binding.ivPlacesSearchInfoImage.isVisible = isInfoUiVisible
        binding.tvPlacesSearchInfoText.isVisible = isInfoUiVisible
        binding.pbPlacesSearchProgress.isVisible = state is PlacesSearchUiState.Progress
        binding.rvPlacesSearchResults.isVisible = state is PlacesSearchUiState.Result
    }

    fun setSearchText(text: String) {
        binding.etPlacesSearchInput.searchInputText = text
    }

    fun setKeyboardVisible(visible: Boolean) {
        if (visible) {
            binding.etPlacesSearchInput.showKeyboard()
        } else {
            binding.etPlacesSearchInput.hideKeyboard()
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 1000L
        private const val MIN_TEXT_LENGTH_TO_SEARCH = 3
    }
}
