package com.numplates.nomera3.modules.places.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.meera.core.extensions.clearText
import com.meera.core.extensions.debouncedAction1
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.showKeyboard
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewPlacesSearchBinding
import com.numplates.nomera3.modules.places.ui.adapter.PlacesAdapter
import com.numplates.nomera3.modules.places.ui.model.PlacesSearchEvent
import com.numplates.nomera3.modules.places.ui.model.PlacesSearchUiState

class PlacesSearchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private var eventListener: ((PlacesSearchEvent) -> Unit)? = null

    private val binding: ViewPlacesSearchBinding = LayoutInflater.from(context)
        .inflate(R.layout.view_places_search, this, false)
        .apply(::addView)
        .let(ViewPlacesSearchBinding::bind)

    private var placesAdapter: PlacesAdapter? = null

    init {
        binding.rvPlacesSearchResults.addItemDecoration(PlacesDividerItemDecorator(binding.root.context))
        placesAdapter = PlacesAdapter { item ->
            eventListener?.invoke(PlacesSearchEvent.PlaceSelected(item.place))
        }
        binding.rvPlacesSearchResults.adapter = placesAdapter
        binding.btnPlacesSearchCancel.setThrottledClickListener {
            eventListener?.invoke(PlacesSearchEvent.Canceled)
        }
        binding.ivPlacesSearchClear.setThrottledClickListener {
            binding.etPlacesSearchInput.clearText()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val debouncedSearch = binding.root.findViewTreeLifecycleOwner()
            ?.lifecycleScope
            ?.debouncedAction1<String>(SEARCH_DEBOUNCE_DELAY) { searchText ->
                eventListener?.invoke(PlacesSearchEvent.PlaceSearched(searchText))
            }
        binding.etPlacesSearchInput.addTextChangedListener {
            val text = it?.toString().orEmpty()
            if (text.length >= MIN_TEXT_LENGTH_TO_SEARCH) {
                debouncedSearch?.invoke(text)
            } else {
                eventListener?.invoke(PlacesSearchEvent.SearchCleared)
            }
            binding.ivPlacesSearchClear.isVisible = text.isNotEmpty()
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
        binding.etPlacesSearchInput.setText(text)
        binding.etPlacesSearchInput.setSelection(text.length)
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
