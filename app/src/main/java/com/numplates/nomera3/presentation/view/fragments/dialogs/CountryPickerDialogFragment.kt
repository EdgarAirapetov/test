package com.numplates.nomera3.presentation.view.fragments.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.data.network.Country
import com.numplates.nomera3.databinding.PersonalInfoCountrySelectorFragmentBinding

/*
* Диалог выбора страны на экранах Регистрация / Ваш профиль
* */
class CountryPickerDialogFragment(
        private val countriesList: List<Country>
) : DialogFragment(), CountryPickerDialogCallback {

    private lateinit var recyclerView: RecyclerView
    private lateinit var dialog: AlertDialog

    private var binding: PersonalInfoCountrySelectorFragmentBinding? = null

    private var onDismissListener: (Country?) -> Unit = {}
    private var isSizeConfigured: Boolean = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = PersonalInfoCountrySelectorFragmentBinding.inflate(LayoutInflater.from(context))
        initViews()

        // todo maybe setAdapter would be better
        dialog = AlertDialog.Builder(requireContext())
                .setView(binding?.root)
                .setCancelable(false)
                .create()

        return dialog
    }

    override fun onResume() {
        super.onResume()

        if (!isSizeConfigured) {
            isSizeConfigured = true
            initDialogSize()
        }
    }

    private fun initDialogSize() {
        val displayWidth = DisplayMetrics().let { displayMetrics: DisplayMetrics ->
            val systemService = requireContext().getSystemService(Context.WINDOW_SERVICE)
            systemService as WindowManager
            systemService.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }

        val layoutParams = WindowManager.LayoutParams()
        dialog.window?.attributes?.let { layoutParams.copyFrom(it) }

        val dialogWindowWidth = (displayWidth * 0.85f).toInt()
        layoutParams.width = dialogWindowWidth
        dialog.window?.attributes = layoutParams
    }

    override fun onCountryClicked(country: Country?) {
        onDismissListener(country)
        dismiss()
    }

    fun setOnDismissListener(listener: (Country?) -> Unit) {
        onDismissListener = listener
    }

    private fun initViews() {
        binding?.countries?.also {
            recyclerView = it
            recyclerView.layoutManager = requireContext().createVerticalLinearLayoutManager()
            recyclerView.adapter = CountryPickerDialogAdapter(countriesList, this)
        }
    }

    private fun Context.createVerticalLinearLayoutManager(): LinearLayoutManager {
        return LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
