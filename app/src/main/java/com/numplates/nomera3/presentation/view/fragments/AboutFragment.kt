package com.numplates.nomera3.presentation.view.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.getToolbarHeight
import com.numplates.nomera3.App
import com.numplates.nomera3.BuildConfig
import com.numplates.nomera3.NOOMEERA_USER_AGREEMENT_URL
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentAboutBinding
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import javax.inject.Inject

class AboutFragment : BaseFragmentNew<FragmentAboutBinding>() {

    @Inject
    lateinit var amplitudeHelper: AnalyticsInteractor

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAboutBinding
        get() = FragmentAboutBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        App.component.inject(this)
        act.setColorStatusBarNavLight()
        val params = binding?.toolbarAbout?.layoutParams as CollapsingToolbarLayout.LayoutParams
        val statusBarHeight = context.getStatusBarHeight()
        val toolbarHeight = context.getToolbarHeight()
        params.height = statusBarHeight + toolbarHeight
        binding?.toolbarAbout?.layoutParams = params

        binding?.collapsingToolbarAbout?.setPadding(0, statusBarHeight, 0, 0)

        binding?.toolbarAbout?.setNavigationIcon(R.drawable.arrowback_white)
        binding?.toolbarAbout?.setNavigationOnClickListener { act.onBackPressed() }

        binding?.tvAboutUserAgreement?.setOnClickListener {
            val uriUrl = Uri.parse(NOOMEERA_USER_AGREEMENT_URL)
            val launchBrowser = Intent(Intent.ACTION_VIEW, uriUrl)
            startActivity(launchBrowser)
        }

        binding?.tvAboutCooperation?.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "info@noomeera.com", null))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Cooperation request")
            startActivity(Intent.createChooser(emailIntent, "Send email..."))
        }


        binding?.tvAboutWebsite?.setOnClickListener {
            val uriUrl = Uri.parse("http://noomera.ru")
            val launchBrowser = Intent(Intent.ACTION_VIEW, uriUrl)
            startActivity(launchBrowser)
        }

        binding?.tvAppVersion?.text = getString(R.string.about_app_version, BuildConfig.VERSION_NAME)
    }
}
