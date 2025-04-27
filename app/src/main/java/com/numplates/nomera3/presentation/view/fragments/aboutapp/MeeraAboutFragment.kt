package com.numplates.nomera3.presentation.view.fragments.aboutapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.safeNavigate
import com.numplates.nomera3.App
import com.numplates.nomera3.BuildConfig
import com.numplates.nomera3.NOOMEERA_USER_AGREEMENT_URL
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentAboutBinding
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitType
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.presentation.router.IArgContainer

private const val CONST_MEERA_URL = "http://noomera.ru"
private const val CONST_MAILTO_SCHEME = "mailto"
private const val CONST_MEERA_EMAIL = "info@noomeera.com"
private const val CONST_EXTRA_SUBJECT = "Cooperation request"
private const val CONST_COLLABA_TITLE = "Send email..."

sealed interface AboutFragmentAction {
    data object SupportAction : AboutFragmentAction
    data object AgreementAction : AboutFragmentAction
    data object CollaborationAction : AboutFragmentAction
    data object WebSiteAction : AboutFragmentAction
}

class MeeraAboutFragment : MeeraBaseDialogFragment(R.layout.meera_fragment_about, ScreenBehaviourState.Full) {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(MeeraFragmentAboutBinding::bind)

    private var userId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initRecycler()
        initAppVersion()
        userId = requireArguments().getLong(IArgContainer.ARG_USER_ID)
    }

    private fun initToolbar() {
        binding.apply {
            aboutFragmentContent.let { recycler -> binding.aboutFragmentNaw.addScrollableView(recycler) }
            aboutFragmentNaw.title = getString(R.string.about_meera)
            aboutFragmentNaw.backButtonClickListener = { findNavController().popBackStack() }
        }
    }

    private fun initRecycler() {
        binding.aboutFragmentContent.adapter = AboutAppAdapter(
            getRecyclerItems(),
            ::onActionListener
        )
    }

    private fun initAppVersion() {
        binding.aboutFragmentAppVersion.text = getString(R.string.about_app_version, BuildConfig.VERSION_NAME)
    }

    private fun onActionListener(action: AboutFragmentAction) {
        when (action) {
            AboutFragmentAction.AgreementAction -> navigateToAgreement()
            AboutFragmentAction.CollaborationAction -> navigateToCollaboration()
            AboutFragmentAction.SupportAction -> navigateToSupport(userId)
            AboutFragmentAction.WebSiteAction -> navigateToWebSite()
        }
    }

    private fun navigateToWebSite() {
        val uriUrl = Uri.parse(CONST_MEERA_URL)
        val launchBrowser = Intent(Intent.ACTION_VIEW, uriUrl)
        startActivity(launchBrowser)
    }

    private fun navigateToSupport(userId: Long?) {
        findNavController().safeNavigate(
            R.id.action_meeraAboutFragment_to_meeraChatFragment,
            bundle = bundleOf(
                IArgContainer.ARG_CHAT_INIT_DATA to ChatInitData(
                    initType = ChatInitType.FROM_PROFILE,
                    userId = userId
                )
            )
        )
    }

    private fun navigateToCollaboration() {
        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(CONST_MAILTO_SCHEME, CONST_MEERA_EMAIL, null))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, CONST_EXTRA_SUBJECT)
        startActivity(Intent.createChooser(emailIntent, CONST_COLLABA_TITLE))
    }

    private fun navigateToAgreement() {
        val uriUrl = Uri.parse(NOOMEERA_USER_AGREEMENT_URL)
        val launchBrowser = Intent(Intent.ACTION_VIEW, uriUrl)
        startActivity(launchBrowser)
    }

    /**
     * R.string.about_meera_description - текст не окончательный. Будет меняться.
     * */
    private fun getRecyclerItems() = listOf(
        AboutFragmentAdapterItem.Content(
            imageId = R.drawable.meera_about_image,
            text = getString(R.string.meera_about_meera_description)
        ),
        AboutFragmentAdapterItem.Agreement(getString(R.string.user_agreement)),
        AboutFragmentAdapterItem.Collaba(getString(R.string.cooperation_with_noomeera)),
        AboutFragmentAdapterItem.WebSite(getString(R.string.web_site)),
        AboutFragmentAdapterItem.Support(getString(R.string.technical_support_label))
    )
}
