package com.numplates.nomera3.presentation.model.adaptermodel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.viewpager.widget.PagerAdapter
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.model.enums.CallSettingsEnum
import com.numplates.nomera3.presentation.view.widgets.CustomRowSelector
import timber.log.Timber

class CallsEnabledViewPagerAdapter(
        private val interactor: ICallAdapterInteractor
): PagerAdapter() {

    private val viewList = listOf(R.layout.layout_calls_info, R.layout.layout_privacy_settings)
    private var rowSelector: CustomRowSelector? = null

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layoutInflater = container.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View
        Timber.d("instantiateItem called")
        if (position == 0) {
            view = layoutInflater.inflate(R.layout.layout_calls_info, container, false)
        }
        else{
            view = layoutInflater.inflate(R.layout.layout_privacy_settings, container, false)
            initPrivacy(view)
        }

        container.addView(view)
        return view
    }

    /**
     * Init custom selector listener and setting data to view
     * */
    private fun initPrivacy(view: View) {
        rowSelector = view.findViewById(R.id.crs_selector)
        val settingsRows = mutableListOf<CustomRowSelector.CustomRowSelectorModel>()
        settingsRows.add(CustomRowSelector.CustomRowSelectorModel(
                CallSettingsEnum.ALL.key, view.context.getString(R.string.everyone)
        ))
        settingsRows.add(CustomRowSelector.CustomRowSelectorModel(
                CallSettingsEnum.FRIENDS.key, view.context.getString(R.string.profile_friend)
        ))
        settingsRows.add(CustomRowSelector.CustomRowSelectorModel(
                CallSettingsEnum.NOBODY.key, view.context.getString(R.string.nobody)
        ))
        rowSelector?.setModels(settingsRows)

        rowSelector?.setOnRowListener(object : CustomRowSelector.OnRowClickedListener{
            override fun onRowClicked(model: CustomRowSelector.CustomRowSelectorModel) {
                interactor.onRowSettingClicked(model)
            }
        })
        rowSelector?.setHeader(R.string.who_can_call_you)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as NestedScrollView
    }

    override fun getCount(): Int {
        return viewList.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as NestedScrollView)
    }

    fun clearSelections() {
        rowSelector?.clearSelections()
    }

    interface ICallAdapterInteractor{
       fun onRowSettingClicked(model: CustomRowSelector.CustomRowSelectorModel)
    }

}