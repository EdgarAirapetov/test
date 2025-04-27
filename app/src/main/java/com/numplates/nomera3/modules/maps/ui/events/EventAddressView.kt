package com.numplates.nomera3.modules.maps.ui.events

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewEventAddressBinding
import com.numplates.nomera3.modules.maps.ui.events.model.DistanceAddressUiModel

class EventAddressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
): ConstraintLayout(context, attrs, defStyle) {

    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.view_event_address, this)
        .let(ViewEventAddressBinding::bind)

    fun setModel(uiModel: DistanceAddressUiModel, listener: (() -> Unit?)? = null) {
        binding.root.setThrottledClickListener {
            listener?.invoke()
        }
        binding.tvEventAddressDistance.text = uiModel.distanceString
        binding.tvEventAddressAddress.text = uiModel.addressString
    }
}
