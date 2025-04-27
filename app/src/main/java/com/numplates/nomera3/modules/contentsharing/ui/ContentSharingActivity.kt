@file:Suppress("unused")

package com.numplates.nomera3.modules.contentsharing.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.meera.core.extensions.parcelable
import com.meera.core.extensions.parcelableArrayList
import com.meera.core.utils.checkAppRedesigned
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.contentsharing.ui.infrastructure.SharingAlertsDelegate
import com.numplates.nomera3.modules.contentsharing.ui.rooms.MeeraSharingRoomsBottomSheet
import com.numplates.nomera3.modules.contentsharing.ui.rooms.SharingRoomsBottomSheet
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

const val TAG_SHARING_ROOMS_BOTTOM_SHEET = "content_sharing_bottom_sheet"
const val TAG_SHARING_LOADER_BOTTOM_SHEET = "content_loader_bottom_sheet"

class ContentSharingActivity : AppCompatActivity() {

    private val viewModel by viewModels<ContentSharingViewModel> { App.component.getViewModelFactory() }
    private val sharingAlertsDelegate by lazy { SharingAlertsDelegate(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeToEffects()

        lifecycleScope.launchWhenResumed {
            handleSharedMediaIntent()
        }
    }

    private fun subscribeToEffects() {
        viewModel.effect
            .flowWithLifecycle(lifecycle)
            .onEach(::handleContentSharingEffect)
            .launchIn(lifecycleScope)
    }

    private fun handleContentSharingEffect(effect: ContentSharingEffect?) {
        when (effect) {
            is ContentSharingEffect.SelectChatsToUpload -> {
                checkAppRedesigned(
                    isRedesigned = {
                        MeeraSharingRoomsBottomSheet().show(fm = supportFragmentManager)
                    },
                    isNotRedesigned = {
                        SharingRoomsBottomSheet
                            .newInstance()
                            .show(supportFragmentManager, TAG_SHARING_ROOMS_BOTTOM_SHEET)
                    }
                )
            }

            is ContentSharingEffect.ShowWentWrongAlert -> {
                sharingAlertsDelegate.showWentWrongSnackbar(this)
            }

            is ContentSharingEffect.CloseSharingScreen -> {
                finish()
            }

            is ContentSharingEffect.ShowNetworkAlert -> {
                sharingAlertsDelegate.showNetworkErrorAlert(this, ::finish)
            }

            is ContentSharingEffect.ShowVideoDurationAlert -> {
                sharingAlertsDelegate.showMediaErrorAlert(this, getString(R.string.video), ::finish)
            }

            else -> Unit
        }
    }

    private fun handleSharedMediaIntent() {
        when (intent.action) {
            Intent.ACTION_SEND -> handleSingleMedia(intent)
            Intent.ACTION_SEND_MULTIPLE -> handleMultipleMedia(intent)
            else -> finish()
        }
    }

    private fun handleSingleMedia(intent: Intent) {
        if (intent.hasExtra(Intent.EXTRA_STREAM)) {
            val uri = listOf(requireNotNull(intent.parcelable<Uri>(Intent.EXTRA_STREAM)))
            viewModel.handleUIAction(ContentSharingAction.ScheduleLoadingUri(uri))
        } else if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            val link = listOf(requireNotNull(intent.getStringExtra(Intent.EXTRA_TEXT)))
            viewModel.handleUIAction(ContentSharingAction.ScheduleSendLink(link.first()))
        }
    }

    private fun handleMultipleMedia(intent: Intent) {
        if (intent.hasExtra(Intent.EXTRA_STREAM)) {
            val uri = requireNotNull(intent.parcelableArrayList<Uri>(Intent.EXTRA_STREAM))
            viewModel.handleUIAction(ContentSharingAction.ScheduleLoadingUri(uri))
        }
    }
}
