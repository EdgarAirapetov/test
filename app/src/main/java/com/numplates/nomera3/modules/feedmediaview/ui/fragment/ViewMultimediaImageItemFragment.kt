package com.numplates.nomera3.modules.feedmediaview.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.github.chrisbanes.photoview.PhotoView
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.glideClear
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlideFullSize
import com.meera.core.extensions.onMeasured
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentViewMultimediaImageItemBinding
import com.numplates.nomera3.modules.feed.ui.entity.MediaAssetEntity
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.presentation.view.ui.customView.photoView.OnPhotoViewGestureListener

const val ARG_MULTIMEDIA_ITEM_ASSET_DATA = "ARG_MULTIMEDIA_ITEM_ASSET_DATA"
private const val DEFAULT_MEDIUM_SCALE = 1.75f
private const val DEFAULT_MAXIMUM_SCALE = 3f
private const val LONG_IMAGE_ASPECT_THRESHOLD = 9f / 42f
private const val WIDE_IMAGE_ASPECT_THRESHOLD = 21f / 9f

class ViewMultimediaImageItemFragment : MeeraBaseFragment(
    layout = R.layout.fragment_view_multimedia_image_item ), ViewMultimediaGesturesListener {

    private val binding by viewBinding(FragmentViewMultimediaImageItemBinding::bind)

    private var currentItem: MediaAssetEntity? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initContentData()
        initListeners()
    }

    override fun disableGestures(){
        binding?.vContentTouchBlocker?.visible()
    }

    override fun enableGestures(){
        binding?.vContentTouchBlocker?.gone()
    }

    private fun toggleTouchesInViewPager(isBlocked: Boolean) {
        if (isAdded) {
            setFragmentResult(
                requestKey = KEY_VIEW_PAGER_BLOCK_TOUCHES,
                result = bundleOf(
                    KEY_VIEW_PAGER_IS_BLOCKED to isBlocked
                )
            )
        }
    }

    private fun toggleTapTouchesInViewPager(tapIsActive: Boolean) {
        if(isAdded) {
            setFragmentResult(
                requestKey = KEY_VIEW_PAGER_CONTENT_TAP,
                result = bundleOf(
                    KEY_VIEW_PAGER_LONG_TAP_IS_ACTIVE to tapIsActive
                )
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListeners() {
        binding?.apply {
            ivViewMultimediaImageItemImage.setOnMatrixChangeListener {
                val scale = binding?.ivViewMultimediaImageItemImage?.scale ?: return@setOnMatrixChangeListener
                blockReducingOriginalScale(scale)
                toggleTouchesInViewPager(scale > 1f)
            }
            ivViewMultimediaImageItemImage.setGestureListener(object: OnPhotoViewGestureListener {
                override fun onLongTap() {
                    toggleTapTouchesInViewPager(tapIsActive = true)
                }

                override fun onLongTapRelease() {
                    toggleTapTouchesInViewPager(tapIsActive = false)
                }

            })
            vContentTouchBlocker.setOnTouchListener { _, _ -> true }
        }
    }

    private fun blockReducingOriginalScale(scale: Float) {
        if (scale < 1f) binding?.ivViewMultimediaImageItemImage?.attacher?.update()
    }

    private fun initContentData() {
        val args = arguments ?: run {
            activity?.onBackPressed()
            return
        }
        currentItem = args.getParcelable(ARG_MULTIMEDIA_ITEM_ASSET_DATA)
        if(currentItem?.isAvailable == false){
            showUnavailableView()
        } else {
            initContent()
        }
    }

    private fun initContent() {
        binding?.ivViewMultimediaImageItemImage?.apply {
            currentItem?.let { initImageScaleThresholds(this, it) }
            glideClear()
            loadGlideFullSize(currentItem?.image)
        }
    }

    private fun showUnavailableView() {
        binding?.incViewMultimediaUnavailableLayout?.tvMediaUnavailableHeader?.text = getString(R.string.image_unavailable_default_title)
        binding?.incViewMultimediaUnavailableLayout?.root?.visible()
    }

    override fun onDestroyView() {
        binding.ivViewMultimediaImageItemImage.glideClear()
        super.onDestroyView()
    }

    private fun initImageScaleThresholds(photoView: PhotoView, item: MediaAssetEntity) {
        val aspect = item.aspect.toDouble()
        val enableZoomToFit = aspect < LONG_IMAGE_ASPECT_THRESHOLD || aspect > WIDE_IMAGE_ASPECT_THRESHOLD
        if (enableZoomToFit) {
            photoView.onMeasured {
                val imageAspect = (aspect.takeIf { it != 0.0 } ?: 1.0).toFloat()
                val viewAspect = photoView.width.toFloat() / photoView.height
                val aspectRatio = if (viewAspect > imageAspect) viewAspect / imageAspect else imageAspect / viewAspect
                val zoomToFitScale = aspectRatio.coerceAtLeast(DEFAULT_MEDIUM_SCALE)
                val maxZoomScale = DEFAULT_MAXIMUM_SCALE / DEFAULT_MEDIUM_SCALE * zoomToFitScale
                photoView.maximumScale = maxZoomScale
                photoView.mediumScale = zoomToFitScale
            }
        } else {
            photoView.maximumScale = DEFAULT_MAXIMUM_SCALE
            photoView.mediumScale = DEFAULT_MEDIUM_SCALE
        }
    }
}
