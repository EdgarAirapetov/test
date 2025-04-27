package com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters

import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.viewpager.widget.PagerAdapter
import com.meera.core.extensions.inflate
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.model.enums.CallSettingsEnum
import com.numplates.nomera3.presentation.view.adapter.FriendsFollowersActionCallback
import com.numplates.nomera3.presentation.view.widgets.CustomRowSelector

class FriendsFollowersPrivacyAdapter constructor(
    private val friendsFollowersCallback: FriendsFollowersActionCallback
) : PagerAdapter() {

    private var rowSelector: CustomRowSelector? = null

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as NestedScrollView
    }

    override fun getCount() = VIEWS_COUNT

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as NestedScrollView)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view: View
        when (position) {
            PRIVACY_INFO_POSITION -> {
                view = container.inflate(R.layout.layout_friends_followers_privacy_info)
            }
            else -> {
                view = container.inflate(R.layout.layout_friends_followers_privacy_select)
                initPrivacy(view)
            }
        }
        container.addView(view)
        return view
    }

    fun clearSelections() {
        rowSelector?.clearSelections()
    }

    private fun initPrivacy(view: View) {
        rowSelector = view.findViewById(R.id.vg_privacy_selector)
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
                friendsFollowersCallback.onPrivacyClicked(model)
            }
        })
    }

    companion object {
        const val PRIVACY_INFO_POSITION = 0
        const val PRIVACY_SELECT_POSITION = 1
        private const val VIEWS_COUNT = 2
    }
}
