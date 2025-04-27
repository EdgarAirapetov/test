package com.numplates.nomera3.presentation.view.fragments.vehiclebrandmodelselect

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.extensions.hideKeyboard
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.noomeera.nmrmediatools.extensions.hideKeyboard
import com.numplates.nomera3.App
import com.numplates.nomera3.databinding.MeeraBottomSheetVehicleBrandModelSelectBinding

class MeeraVehicleBrandModelSelectFragment : UiKitBottomSheetDialog<MeeraBottomSheetVehicleBrandModelSelectBinding>() {

    val titleResId: Int by lazy { requireArguments().getInt(ARG_TITLE_TEXT_ID) }
    val listType: String by lazy { requireArguments().getString(ARG_LIST_TYPE) ?: "" }
    val brandId: Int? by lazy { requireArguments().getInt(ARG_BRAND_ID) }

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraBottomSheetVehicleBrandModelSelectBinding
        get() = MeeraBottomSheetVehicleBrandModelSelectBinding::inflate

    private val viewModel by viewModels<MeeraVehicleBrandModelSelectViewModel> {
        App.component.getViewModelFactory()
    }

    private val adapter by lazy { VehicleBrandModelAdapter(::onItemSelected) }


    override fun createDialogState(): UiKitBottomSheetDialogParams =
        UiKitBottomSheetDialogParams(labelText = context?.getString(titleResId))

    private fun onItemSelected(item: VehicleBrandModelItem) {
        setFragmentResult(
            ARG_VEHICLE_BRAND_MODEL_SELECT_REQUEST_KEY,
            bundleOf(ARG_LIST_TYPE to listType, ARG_SELECTED_ID to item.id, ARG_SELECTED_NAME to item.name)
        )
        dismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.init(listType, brandId)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.apply {
            behavior.peekHeight = resources.displayMetrics.heightPixels
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contentBinding?.apply{
            rvVehicleBrandModelSelect.adapter = adapter

            inputSearchVehicleBrandModelSelect.doAfterSearchTextChanged {
                viewModel.getData(it)
            }

           inputSearchVehicleBrandModelSelect.setCloseButtonClickedListener {
                requireContext().hideKeyboard(requireView())
            }

        }


        viewModel.vehicleBrandModelSelectUiState.observe(viewLifecycleOwner) {
            when (it) {
                VehicleBrandModelSelectUiState.Loading -> Unit
                is VehicleBrandModelSelectUiState.Success -> {
                    adapter.submitList(it.list)
                    contentBinding?.groupEmptyState?.isGone = it.list.isNotEmpty()
                }
            }
        }

    }

    companion object {
        const val VEHICLE_BRAND_MODEL_SELECT_BOTTOM_DIALOG_TAG = "vehicleBrandModelSelectBottomDialog"
        const val ARG_VEHICLE_BRAND_MODEL_SELECT_REQUEST_KEY = "vehicleBrandModelSelectRequestKey"

        const val ARG_TITLE_TEXT_ID = "titleTextId"
        const val ARG_LIST_TYPE = "listType"
        const val ARG_LIST_TYPE_BRANDS = "ARG_LIST_TYPE_BRANDS"
        const val ARG_LIST_TYPE_MODELS = "ARG_LIST_TYPE_MODELS"
        const val ARG_BRAND_ID = "ARG_BRAND_ID"

        const val ARG_SELECTED_ID = "selectedId"
        const val ARG_SELECTED_NAME = "selectedName"

        @JvmStatic
        fun show(
            fragmentManager: FragmentManager, titleResId: Int, listType: String, brandId: Int? = null
        ): MeeraVehicleBrandModelSelectFragment {
            val instance = MeeraVehicleBrandModelSelectFragment()
            instance.arguments =
                bundleOf(ARG_TITLE_TEXT_ID to titleResId, ARG_LIST_TYPE to listType, ARG_BRAND_ID to brandId)
            instance.show(fragmentManager, VEHICLE_BRAND_MODEL_SELECT_BOTTOM_DIALOG_TAG)
            return instance
        }

    }

}
