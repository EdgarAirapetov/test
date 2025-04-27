package com.numplates.nomera3.presentation.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.meera.core.preferences.AppSettings.Companion.KEY_PUSH_ON
import com.meera.core.preferences.AppSettings.Companion.KEY_PUSH_ON_ANSWER_COMMENT
import com.meera.core.preferences.AppSettings.Companion.KEY_PUSH_ON_FRIEND_REQUEST
import com.meera.core.preferences.AppSettings.Companion.KEY_PUSH_ON_GROUP_INVITE
import com.meera.core.preferences.AppSettings.Companion.KEY_PUSH_ON_MESSAGE
import com.meera.core.preferences.AppSettings.Companion.KEY_PUSH_ON_NEW_GIFT
import com.meera.core.preferences.AppSettings.Companion.KEY_PUSH_ON_POST_COMMENT
import com.meera.core.preferences.AppSettings.Companion.KEY_PUSH_ON_POST_GROUP
import com.numplates.nomera3.R
import timber.log.Timber

/**
 * created by c7j on 27.10.18
 *
 * IMPORTANT: default values are (probably) initialized in App.java onCreate() method
 */
class ProfileNotificationsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.push_preferences, rootKey)
        try {
            setUpListPreferences()
        } catch (e: Exception) {
            Timber.e(e, "setUpListPreferences()")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = super.onCreateView(inflater, container, savedInstanceState)
        if (view != null) {
            view.setBackgroundColor(resources.getColor(android.R.color.white))
            view.isClickable = true
        }
        return view
    }

    private fun setUpListPreferences() {
        val yearFilter: SwitchPreference? = findPreference<Preference>(KEY_PUSH_ON) as SwitchPreference?
        yearFilter!!.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener({ preference: Preference?, newValue: Any ->
                if ((newValue == true)) {
                    findPreference<Preference>(KEY_PUSH_ON_POST_GROUP)!!.isEnabled = true
                    findPreference<Preference>(KEY_PUSH_ON_POST_COMMENT)!!.isEnabled = true
                    findPreference<Preference>(KEY_PUSH_ON_MESSAGE)!!.isEnabled = true
                    findPreference<Preference>(KEY_PUSH_ON_POST_GROUP)!!.isEnabled = true
                    findPreference<Preference>(KEY_PUSH_ON_ANSWER_COMMENT)!!.isEnabled = true
                    findPreference<Preference>(KEY_PUSH_ON_FRIEND_REQUEST)!!.isEnabled = true
                    findPreference<Preference>(KEY_PUSH_ON_GROUP_INVITE)!!.isEnabled = true
                    findPreference<Preference>(KEY_PUSH_ON_NEW_GIFT)!!.isEnabled = true
                } else {
                    findPreference<Preference>(KEY_PUSH_ON_POST_GROUP)!!.isEnabled = false
                    findPreference<Preference>(KEY_PUSH_ON_POST_COMMENT)!!.isEnabled = false
                    findPreference<Preference>(KEY_PUSH_ON_MESSAGE)!!.isEnabled = false
                    findPreference<Preference>(KEY_PUSH_ON_POST_GROUP)!!.isEnabled = false
                    findPreference<Preference>(KEY_PUSH_ON_ANSWER_COMMENT)!!.isEnabled = false
                    findPreference<Preference>(KEY_PUSH_ON_FRIEND_REQUEST)!!.isEnabled = false
                    findPreference<Preference>(KEY_PUSH_ON_GROUP_INVITE)!!.isEnabled = false
                    findPreference<Preference>(KEY_PUSH_ON_NEW_GIFT)!!.isEnabled = false
                }
                true
            })
    }
}
