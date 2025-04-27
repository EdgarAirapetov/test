package com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.utils.NToast

interface RoadFilterSubscriptionsCallback {
    fun onDismiss()
}

class RoadFilterSubscriptionsBottomSheet(private val callback: RoadFilterSubscriptionsCallback) :
    BottomSheetDialogFragment() {

    private val viewModel by viewModels<RoadFilterSubscriptionsViewModel>()

    private lateinit var switchMyGroups: SwitchCompat

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(
        R.layout.bottom_sheet_subscriptions_road_filter,
        container,
        false
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        handleSwitchMyGroups()
        subscribeEvent()
        subscribeStateSubscribed()
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    private fun subscribeStateSubscribed() {
        viewModel.isGroupSubscribedState.observe(viewLifecycleOwner, { isSubscribed ->
            switchMyGroups.isChecked = isSubscribed
        })
    }

    private fun subscribeEvent() {
        viewModel.event.observe(viewLifecycleOwner, { event ->
            when (event) {
                is RoadFilterSubscriptionEvent.Error -> {
                    showError()
                }
            }
        })
    }

    private fun showError() {
        val activity = (activity as? Act) ?: return

        NToast.with(activity)
            .text(getString(R.string.community_subscribe_filter_change_error_text))
            .typeError()
            .show()
    }

    private fun initViews(view: View) {
        switchMyGroups = view.findViewById(R.id.sw_switcher__my_groups)
    }

    private fun handleSwitchMyGroups() {
        switchMyGroups.isChecked = viewModel.getCurrentFilterMyGroups()
        handleCheckSwitchMyGroups()
        switchMyGroups.setOnCheckedChangeListener { _, _ ->
            handleCheckSwitchMyGroups()
        }
    }

    private fun handleCheckSwitchMyGroups() {
        if (switchMyGroups.isChecked) {
            viewModel.setFilterMyGroups(isEnabled = true)
        } else {
            viewModel.setFilterMyGroups(isEnabled = false)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        callback.onDismiss()
    }
}