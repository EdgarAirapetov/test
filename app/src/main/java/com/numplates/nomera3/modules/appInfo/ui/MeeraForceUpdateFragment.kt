package com.numplates.nomera3.modules.appInfo.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.App
import com.numplates.nomera3.BuildConfig
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraForceUpdateAppFragmentBinding
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.appInfo.ui.entity.ForceUpdateDialogEntity
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyActionType
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import javax.inject.Inject

/**
 * Use ForceUpdateDialog.showUpdateDialog method to show dialog
 * */
class MeeraForceUpdateFragment : MeeraBaseFragment(
    layout = R.layout.meera_force_update_app_fragment,
) {

    @Inject
    lateinit var amplitudeHelper: AnalyticsInteractor

    private var dialogParams: ForceUpdateDialogEntity? = null

    private val binding by viewBinding(MeeraForceUpdateAppFragmentBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialogParams = savedInstanceState?.getParcelable(ForceUpdateDialog.FORCE_UPDATE_DIALOG_PARAMS)
        initView(dialogParams)
    }

    override fun onResume() {
        super.onResume()
        if (BuildConfig.VERSION_NAME == dialogParams?.appVersion){
            findNavController().popBackStack()
        }
    }

    private fun initView(dialogParams: ForceUpdateDialogEntity?){
        binding?.vUpdateBtn?.setThrottledClickListener {
            sendToMarket()
        }

        arguments?.let { bundle ->
            dialogParams?.let {
                if(it.canBeClosed){
                    binding?.vCloseBtn?.visible()
                    binding?.vCloseBtn?.setThrottledClickListener {
                        amplitudeHelper.logForceUpdate(AmplitudePropertyActionType.CLOSE)
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

    // отпвка пользователя в маркет
    private fun sendToMarket() {
        amplitudeHelper.logForceUpdate(AmplitudePropertyActionType.UPDATE)
        val marketIntent = Intent(Intent.ACTION_VIEW)
        marketIntent.data = Uri.parse(App.GOOGLE_PLAY_MARKET_URL)
        startActivity(marketIntent)
    }
}
