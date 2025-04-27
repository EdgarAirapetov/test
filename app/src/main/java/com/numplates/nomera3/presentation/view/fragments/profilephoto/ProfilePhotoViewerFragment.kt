package com.numplates.nomera3.presentation.view.fragments.profilephoto

import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.loader.glide.GlideImageLoader
import com.google.android.material.appbar.AppBarLayout
import com.meera.core.base.BaseLoadImages
import com.meera.core.base.BaseLoadImagesDelegate
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.toBoolean
import com.meera.core.utils.graphics.NGraphics
import com.meera.media_controller_api.model.MediaControllerCallback
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.numplates.nomera3.MEDIA_EXT_GIF
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentGalleryFullscreenBinding
import com.numplates.nomera3.modules.auth.util.needAuth
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegate
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegateImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeAlertPostWithNewAvatarValuesActionType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeAlertPostWithNewAvatarValuesFeedType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudePropertyReactionWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.createAmplitudeProfileReactionsParams
import com.numplates.nomera3.modules.common.ActivityToolsProvider
import com.numplates.nomera3.modules.feed.ui.adapter.ContentActionBar
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingAnimationPlayListener
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingReaction
import com.numplates.nomera3.modules.reaction.ui.data.ReactionSource
import com.numplates.nomera3.modules.reaction.ui.getDefaultReactionContainer
import com.numplates.nomera3.modules.reaction.ui.util.reactionCount
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsEntityType
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsStatisticsBottomSheetFragment
import com.numplates.nomera3.modules.userprofile.ui.model.PhotoModel
import com.numplates.nomera3.presentation.model.enums.CreateAvatarPostEnum
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_ANIMATED_AVATAR
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GALLERY_IMAGES_COUNT
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GALLERY_POSITION
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_IMAGE_URL
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_IS_ANIMATED_AVATAR
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_POST_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.view.adapter.TouchImageAdapterNew
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.postavatar.PostAvatarAlertListener
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.postavatar.PostAvatarBottomSheetFragment
import com.numplates.nomera3.presentation.view.fragments.dialogs.ConfirmDialogBuilder
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import com.numplates.nomera3.presentation.view.ui.bottomMenu.ReactionsStatisticBottomMenu
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.viewmodel.profilephoto.ProfilePhotoViewerViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.ProfilePhotoViewEvent
import timber.log.Timber
import java.io.File

const val ANIMATED_AVATAR_RESOLUTION = 600

class ProfilePhotoViewerFragment :
    BaseFragmentNew<FragmentGalleryFullscreenBinding>(),
    BaseLoadImages by BaseLoadImagesDelegate(),
    BasePermission by BasePermissionDelegate(),
    SaveMediaFileDelegate by SaveMediaFileDelegateImpl(),
    PostAvatarAlertListener {

    private lateinit var viewModel: ProfilePhotoViewerViewModel
    private var mainLayout: LinearLayout? = null
    private var viewPager: ViewPager? = null
    private var frameViewPager: FrameLayout? = null
    private var toolbar: Toolbar? = null
    private var appBarLayout: AppBarLayout? = null
    private var toolbarTitle: TextView? = null
    private var tvBackClose: TextView? = null
    private var ivMenuBtn: ImageView? = null

    private var viewPagerAdapter: TouchImageAdapterNew? = null
    private var animatedAvatarState: String? = null

    private var position = 0
    private var userID = -1L
    private var postId = -1L
    private var photoUrl = ""

    private var currentPosition = 0
    private var totalSize = 10
        set(value) {
            field = value
            val currentItem = viewPager?.currentItem ?: 0
            toolbarTitle?.isVisible = value > 1
            toolbarTitle?.text = resources.getString(R.string.from_txt, currentItem + 1, totalSize)
        }
    private var isProfilePhoto = false
    private var isOwnPhotoProfile = false
    private var isAnimatedAvatar = false

    private var isHorizonOrientation = false

    private var onPhotoDeletedCallback: () -> Unit = { }
    private var originEnum: DestinationOriginEnum? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            args.get(ARG_GALLERY_POSITION)?.let {
                position = it as Int
            }
            args.get(ARG_USER_ID)?.let {
                userID = it as Long
            }
            args.get(ARG_POST_ID)?.let {
                postId = it as Long
            }
            args.get(ARG_IMAGE_URL)?.let {
                photoUrl = it as String
            }
            args.get(ARG_GALLERY_IMAGES_COUNT)?.let {
                totalSize = it as Int
            }
            args.get(ARG_ANIMATED_AVATAR)?.let {
                animatedAvatarState = it as String
            }
            args.get(ARG_IS_ANIMATED_AVATAR)?.let {
                isAnimatedAvatar = it as Boolean
            }
        }
        isProfilePhoto = arguments?.getBoolean(IArgContainer.ARG_IS_PROFILE_PHOTO, false) ?: false
        isOwnPhotoProfile = arguments?.getBoolean(IArgContainer.ARG_IS_OWN_PROFILE, false) ?: false
        originEnum = arguments?.getSerializable(IArgContainer.ARG_GALLERY_ORIGIN) as? DestinationOriginEnum

        Timber.d("isProfilePhoto = $isProfilePhoto")
        viewModel = ViewModelProviders.of(this).get(ProfilePhotoViewerViewModel::class.java)

    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentGalleryFullscreenBinding
        get() = FragmentGalleryFullscreenBinding::inflate

    /**
     * Disable/enable carousel image preview when device rotate
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            isHorizonOrientation = true
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            isHorizonOrientation = false
        }
    }


    private fun initObservable() {
        viewModel.liveReactions.observe(viewLifecycleOwner, Observer { postUpdate ->
            val position: Int = viewPagerAdapter?.updateReactionsData(postUpdate) ?: return@Observer
            viewPager?.findViewWithTag<View>(TouchImageAdapterNew.TAG_PAGE + position)?.let { touchImageView ->
                viewPagerAdapter?.updateReactionsView(
                    view = touchImageView,
                    position = position,
                    postUpdate = postUpdate
                )
            }
        })
        viewModel.liveTotalSize.observe(viewLifecycleOwner, Observer {
            totalSize = it
        })
        viewModel.liveLoadMoreAvatars.observe(viewLifecycleOwner, Observer {
            updatePhotos(it)
        })
        viewModel.liveGoToPosition.observe(viewLifecycleOwner, Observer {
            viewPager?.setCurrentItem(it, false)
        })

        viewModel.liveTitle.observe(viewLifecycleOwner, Observer {
            toolbarTitle?.text = it
        })


        viewModel.liveRemovePhoto.observe(viewLifecycleOwner, Observer {
            setResult(true)
            onPhotoDeletedCallback()
            totalSize -= 1
            if (totalSize == 0)
                act.onBackPressed()
            loadAdapter()
            viewModel.init(currentPosition, userID, postId, photoUrl, isProfilePhoto)
        })

        viewModel.liveEvents.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ProfilePhotoViewEvent.OnPhotoUploadError -> handleErrorUpload()
                is ProfilePhotoViewEvent.OnPhotoUploadSuccess -> {
                    handleSuccessUpload(it)
                    setResult(true)
                }
                is ProfilePhotoViewEvent.AvatarRemovedSuccess -> handleAvatarRemoved()
                is ProfilePhotoViewEvent.AvatarRemovedError -> handleAvatarRemovedError()
                is ProfilePhotoViewEvent.OnAnimatedAvatarSaved -> handleAnimatedAvatarPath(it.path)
                is ProfilePhotoViewEvent.OnCreateAvatarPostSettings -> {
                    if (it.privacySettingModel?.value == CreateAvatarPostEnum.PRIVATE_ROAD.state ||
                        it.privacySettingModel?.value == CreateAvatarPostEnum.MAIN_ROAD.state
                    ) {
                        onPublishOptionsSelected(
                            imagePath = it.imagePath,
                            animation = it.animation,
                            createAvatarPost = it.privacySettingModel.value,
                            saveSettings = 1,
                            amplitudeActionType = AmplitudeAlertPostWithNewAvatarValuesActionType.PUBLISH
                        )
                    } else {
                        showPublishPostAlert(imagePath = it.imagePath, animation = it.animation)
                    }
                }
            }
        })
    }

    private fun checkAvatarPostSettings(imagePath: String, animation: String? = null) {
        viewModel.requestCreateAvatarPostSettings(imagePath = imagePath, animation = animation)
    }

    private fun showPublishPostAlert(imagePath: String, animation: String? = null) {
        PostAvatarBottomSheetFragment.getInstance(photoPath = imagePath, animation = animation)
            .show(childFragmentManager)
    }

    override fun onPublishOptionsSelected(
        imagePath: String,
        animation: String?,
        createAvatarPost: Int,
        saveSettings: Int,
        amplitudeActionType: AmplitudeAlertPostWithNewAvatarValuesActionType
    ) {
        viewModel.uploadUserAvatar(
            imagePath = imagePath,
            animation = animation,
            createAvatarPost = createAvatarPost,
            saveSettings = saveSettings
        )

        val amplitudeFeedType = when (createAvatarPost) {
            CreateAvatarPostEnum.PRIVATE_ROAD.state -> AmplitudeAlertPostWithNewAvatarValuesFeedType.SELF_FEED
            CreateAvatarPostEnum.MAIN_ROAD.state -> AmplitudeAlertPostWithNewAvatarValuesFeedType.MAIN_FEED
            else -> AmplitudeAlertPostWithNewAvatarValuesFeedType.NO_PUBLISH
        }

        val imageUriPath = File(imagePath).toUri().toString()
        (activity as? ActivityToolsProvider)?.getStatusToastViewController()
            ?.showProgress(message = getString(R.string.user_personal_info_setting_main_photo), imageUrl = imageUriPath)

        viewModel.logAlertPostWithNewAvatarAction(
            actionType = amplitudeActionType,
            feedType = amplitudeFeedType,
            toggle = saveSettings.toBoolean()
        )


        if (saveSettings.toBoolean()) {
            viewModel.logPrivacyPostWithNewAvatarChange(createAvatarPost)
        }
    }

    private fun updatePhotos(photoModels: List<PhotoModel>) {
        viewPagerAdapter?.loadMore(photoModels)
    }

    private fun handleAnimatedAvatarPath(path: String) {
        NGraphics.saveImageToDeviceFromAppDirectory(act, path) { savedImageUri ->
            viewModel.deleteFile(path)
            if (savedImageUri != null) {
                viewModel.logAvatarDownloaded(isOwnPhotoProfile, false)
                NToast.with(view)
                    .typeSuccess()
                    .text(getString(R.string.image_saved))
                    .show()
            } else {
                NToast.with(view)
                    .typeError()
                    .text(getString(R.string.avatar_save_file_fail))
                    .show()
            }
        }
    }

    private fun handleAvatarRemovedError() {
        NToast.with(view)
            .text(getString(R.string.error_while_deleting_avatar))
            .show()
    }

    private fun handleAvatarRemoved() {
        NToast.with(view)
            .text(getString(R.string.avatar_successfully_deleted))
            .typeSuccess()
            .show()
        act.onBackPressed()
    }

    private fun handleSuccessUpload(event: ProfilePhotoViewEvent.OnPhotoUploadSuccess) {
        Timber.d("handleSuccessUpload")

        val message = when (event.createAvatarPost) {
            CreateAvatarPostEnum.PRIVATE_ROAD.state, CreateAvatarPostEnum.MAIN_ROAD.state -> R.string.profile_avatar_update_success_with_post
            else -> R.string.profile_avatar_update_success
        }

        (activity as? ActivityToolsProvider)?.getStatusToastViewController()
            ?.showSuccess(message = getString(message), imageUrl = event.photoUrl)
    }

    private fun handleErrorUpload() {
        binding?.pbImageView?.gone()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        loadAdapter()
        initObservable()

        //need to show images correct
        context?.let {
            BigImageViewer.initialize(GlideImageLoader.with(it.applicationContext))
        }
        Timber.d("position = $position")
        viewModel.init(
            position = position,
            userID = userID,
            postId = postId,
            photoUrl = photoUrl,
            isProfilePhoto = isProfilePhoto
        )
    }

    private fun initView() {
        tvBackClose = binding?.tvBackClose!!
        ivMenuBtn = binding?.ivMenuToolbar!!
        mainLayout = binding?.llFragmentGalleryFullscreen!!
        viewPager = binding?.viewPagerFragmentGallery!!
        toolbarTitle = binding?.tvToolbarTitle!!
        mainLayout?.setBackgroundColor(Color.WHITE)
        appBarLayout = binding?.blGalleryFullscreen!!

        frameViewPager = binding?.flFragmentGalleryFullscreen!!

        toolbar = binding?.toolbar!!
        viewPager?.addOnPageChangeListener(Listener())


        initStatusBar()
        if (userID != -1L)
            toolbarTitle?.text = resources.getString(R.string.from_txt, 1, totalSize)
        else if (userID == -1L && isProfilePhoto)
            toolbarTitle?.text = getString(R.string.user_personal_info_photo_header)
        else if (userID == -1L && !isProfilePhoto)
            toolbarTitle?.text = getString(R.string.image)

        setupBlackStyle()
    }

    private fun setupBlackStyle() {
        appBarLayout?.setBackgroundColor(Color.BLACK)
        frameViewPager?.setBackgroundColor(Color.BLACK)
        tvBackClose?.setTextColor(Color.WHITE)
        ivMenuBtn?.setColorFilter(Color.WHITE)
        toolbarTitle?.setTextColor(Color.WHITE)
    }

    private fun initStatusBar() {
        val statusBarGallery: View? = binding?.statusBarGallery
        val params = statusBarGallery?.layoutParams as? AppBarLayout.LayoutParams
        params?.height = context.getStatusBarHeight()
        statusBarGallery?.layoutParams = params
        tvBackClose?.setOnClickListener { act.onBackPressed() }

        ivMenuBtn?.setOnClickListener {
            if (isOwnPhotoProfile) {
                showOwnProfilePhotoMenu()
            } else {
                showSaveMenu()
            }
        }
    }

    private fun showOwnProfilePhotoMenu() {
        val currentItem = viewPager?.currentItem ?: 0
        viewPagerAdapter?.gallery?.getOrNull(currentItem)?.let { photoItem ->
            photoItem.imageUrl.let { photoUrl ->
                val menu = MeeraMenuBottomSheet(context)
                menu.addItem(R.string.save_image, R.drawable.image_download_menu_item) {
                    if (photoItem.animation.isNullOrEmpty()) {
                        saveImage(photoUrl)
                    } else {
                        viewModel.generateBitmapFromAvatarState(photoItem.animation)
                    }

                    viewModel.logAvatarDownloaded(isOwnPhotoProfile, true)
                    viewModel.logPhotoActionSave()
                }
                if (currentItem != 0 || isProfilePhoto.not()) {
                    if (photoItem.post != null || isProfilePhoto.not()) {
                        menu.addItem(R.string.user_personal_info_bottom_menu_make_avatar, R.drawable.ic_my_road) {
                            showSetMainConfirmDialog(photoItem)
                            viewModel.logPhotoActionMakeTheMain()
                            if (isProfilePhoto) {
                                viewModel.logMainPhotoChangeChooseFromAvatars()
                            } else {
                                viewModel.logMainPhotoChangeChooseFromAbout()
                            }
                        }
                    }
                    menu.addItem(getString(R.string.delete_photo_txt), R.drawable.ic_delete_menu_red) {
                        try {
                            val photoToDelete = viewPagerAdapter?.gallery?.get(currentItem)
                            showDeleteConfirmDialog(photoToDelete?.id)
                        } catch (e: Exception) {
                            Timber.d(e)
                        }
                        viewModel.logPhotoActionDelete()
                    }
                }
                menu.show(childFragmentManager)
            }
        }
    }

    private fun showDeleteConfirmDialog(photoId: Long?) {
        ConfirmDialogBuilder()
            .setDescription(getString(R.string.user_personal_info_delete_photo_confirmation))
            .setLeftBtnText(getString(R.string.cancel))
            .setRightBtnText(getString(R.string.delete))
            .setRightClickListener { viewModel.onConfirmedDelete(photoId) }
            .show(childFragmentManager)
    }

    private fun showSetMainConfirmDialog(photoModel: PhotoModel) {
        ConfirmDialogBuilder()
            .setDescription(getString(R.string.user_personal_info_set_main_photo_confirmation))
            .setLeftBtnText(getString(R.string.yes))
            .setRightBtnText(getString(R.string.cancel))
            .setLeftClickListener {
                if (isProfilePhoto) {
                    viewModel.setPhotoAsMainById(photoModel.id)
                    Uri.parse(photoUrl)
                    showProgress(photoModel.imageUrl)
                } else {
                    if (photoModel.imageUrl.contains(MEDIA_EXT_GIF)) {
                        NGraphics.saveImageToDevice(
                            context = requireContext(),
                            photoModel.imageUrl,
                            { it.path?.let { checkAvatarPostSettings(it) } })
                        showProgress(photoModel.imageUrl)
                    } else {
                        NGraphics.saveImageToDevice(
                            context = requireContext(),
                            imageUrl = photoModel.imageUrl,
                            onSaved = { uri ->
                                openPhotoEditor(uri)
                            }
                        )
                    }
                }
            }.show(childFragmentManager)
    }

    private fun showProgress(imageUrl: String) {
        (activity as? ActivityToolsProvider)?.getStatusToastViewController()
            ?.showProgress(
                message = getString(R.string.user_personal_info_setting_main_photo),
                imageUrl = imageUrl
            )
    }

    private fun openPhotoEditor(imagePath: Uri) {
        viewModel.logOpenEditor()
        act.getMediaControllerFeature().open(
            uri = imagePath,
            openPlace = MediaControllerOpenPlace.Avatar,
            callback = object : MediaControllerCallback {
                override fun onPhotoReady(
                    resultUri: Uri,
                    nmrAmplitude: NMRPhotoAmplitude?
                ) {
                    resultUri.path?.let {
                        checkAvatarPostSettings(it)
                    } ?: kotlin.run {
                        showMediaEditingError()
                    }
                    nmrAmplitude?.let(viewModel::logPhotoEdits)
                }

                override fun onError() {
                    showMediaEditingError()
                }
            }
        )
    }

    private fun showMediaEditingError() {
        NToast.with(view)
            .typeError()
            .text(getString(R.string.error_editing_media))
            .show()
    }

    private fun showSaveMenu() {
        val currentItem = viewPager?.currentItem ?: 0
        viewPagerAdapter?.gallery?.getOrNull(currentItem)?.let { photoItem ->
            photoItem.imageUrl.let { photoUrl ->
                val menu = MeeraMenuBottomSheet(context)
                menu.addItem(R.string.save_image, R.drawable.image_download_menu_item) {
                    if (photoItem.animation.isNullOrEmpty()) {
                        saveImage(photoUrl)
                    } else {
                        viewModel.generateBitmapFromAvatarState(photoItem.animation, userId = userID)
                    }
                    viewModel.logAvatarDownloaded(isOwnPhotoProfile, isAnimatedAvatar.not())
                    viewModel.logPhotoActionSave()
                }
                menu.show(childFragmentManager)
            }
        }
    }

    private fun loadAdapter() {
        viewPagerAdapter = TouchImageAdapterNew(isOwnPhotoProfile)
        viewPager?.adapter = viewPagerAdapter

        //hide, show bottom navigation preview
        viewPagerAdapter?.callback = object : TouchImageAdapterNew.TouchImageAdapterInteraction {
            override fun onImageClicked() {
                if (isHorizonOrientation)
                    return
            }

            override fun onImageLongClick(imageUrl: String?) = Unit
        }
        viewPagerAdapter?.profilePhotoReactionsListener = object : ProfilePhotoReactionsListener {
            override fun onReactionBottomSheetShow(post: PostUIEntity) = needAuth() {
                if (viewModel.getFeatureTogglesContainer().detailedReactionsForPostFeatureToggle.isEnabled) {
                    ReactionsStatisticsBottomSheetFragment.getInstance(
                        entityId = post.postId,
                        entityType = ReactionsEntityType.POST
                    ).show(childFragmentManager)
                } else {
                    val reactions = post.reactions ?: return@needAuth
                    val sortedReactions = reactions.sortedByDescending { reactionEntity -> reactionEntity.count }
                    val menu = ReactionsStatisticBottomMenu(context)
                    menu.addTitle(R.string.reactions, sortedReactions.reactionCount())
                    sortedReactions.forEachIndexed { index, value ->
                        menu.addReaction(value, index != sortedReactions.size - 1)
                    }
                    menu.show(childFragmentManager)
                }
                logOpenStatisticReactionsEvent()
            }

            override fun onReactionLongClicked(
                post: PostUIEntity,
                showPoint: Point,
                reactionTip: TextView,
                viewsToHide: List<View>,
                reactionHolderViewId: ContentActionBar.ReactionHolderViewId
            ) {
                val reactionsParams = createAmplitudeProfileReactionsParams(
                    authorId = userID,
                    originEnum = originEnum,
                    where = wherePhotoWasOpened()
                )
                act.getReactionBubbleViewController().showReactionBubble(
                    reactionSource = ReactionSource.Post(
                        postId = post.postId,
                        reactionHolderViewId = reactionHolderViewId,
                        originEnum = originEnum
                    ),
                    reactionsParams = reactionsParams,
                    showPoint = showPoint,
                    viewsToHide = viewsToHide,
                    reactionTip = reactionTip,
                    currentReactionsList = post.reactions ?: emptyList(),
                    postedAt = post.date,
                    contentActionBarType = ContentActionBar.ContentActionBarType.DARK,
                    containerInfo = act.getDefaultReactionContainer()
                )
            }

            override fun onReactionRegularClicked(
                post: PostUIEntity,
                reactionHolderViewId: ContentActionBar.ReactionHolderViewId
            ) {
                val reactionsParams = createAmplitudeProfileReactionsParams(
                    authorId = userID,
                    originEnum = originEnum,
                    where = wherePhotoWasOpened()
                )
                act.getReactionBubbleViewController().onSelectDefaultReaction(
                    reactionSource = ReactionSource.Post(
                        postId = post.postId,
                        reactionHolderViewId = reactionHolderViewId,
                        originEnum = originEnum
                    ),
                    reactionsParams = reactionsParams,
                    currentReactionsList = post.reactions ?: emptyList()
                )
            }

            override fun onFlyingAnimationInitialized(flyingReaction: FlyingReaction) {
                val container = act?.getRootView() as? ViewGroup
                container?.addView(flyingReaction)
                flyingReaction.startAnimationFlying()
                flyingReaction.setFlyingAnimationPlayListener(object : FlyingAnimationPlayListener {
                    override fun onFlyingAnimationPlayed(playedFlyingReaction: FlyingReaction) {
                        container?.removeView(playedFlyingReaction)
                    }
                })
            }
        }
    }

    private fun logOpenStatisticReactionsEvent() {
        viewModel.logStatisticReactionsTap(wherePhotoWasOpened(), originEnum)
    }

    private fun wherePhotoWasOpened(): AmplitudePropertyReactionWhere {
        val where = if (isProfilePhoto) {
            AmplitudePropertyReactionWhere.AVATAR
        } else {
            AmplitudePropertyReactionWhere.PHOTO_IN_GALLERY
        }
        return where
    }

    fun loadMore() {
        Timber.d("OnLoadMoreCalled")
        viewModel.onLoadMore()
    }

    fun setOnPhotoDeletedCallback(callback: () -> Unit) {
        this.onPhotoDeletedCallback = callback
    }

    private fun saveImage(imageUrl: String) {
        saveImageOrVideoFile(
            imageUrl = imageUrl,
            act = act,
            viewLifecycleOwner = viewLifecycleOwner,
            successListener = {
                NToast.with(view)
                    .typeSuccess()
                    .text(getString(R.string.image_saved))
                    .show()
            }
        )
    }

    override fun onStart() {
        super.onStart()
        hideAllSensitive()
    }

    private fun setResult(avatarsChanged: Boolean?) {
        setFragmentResult(
            requestKey = PHOTO_VIEWER_RESULT,
            result = bundleOf(
                CURRENT_POSITION to (viewPager?.currentItem ?: 0),
                ARG_USER_ID to userID,
                AVATARS_CHANGED to avatarsChanged
            )
        )
    }

    private fun hideAllSensitive() {
        viewPagerAdapter?.gallery?.forEach {
            it.showed = false
        }
        for (i in -1..1) {
            viewPager?.findViewWithTag<View>(TouchImageAdapterNew.TAG_PAGE + (position + i))?.let { touchImageView ->
                viewPagerAdapter?.setupPage(touchImageView, position + i)
            }
        }

    }

    //paging call load more for paging
    inner class Listener : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(p: Int) {
            val count = viewPagerAdapter?.count ?: 0
            val pageSize = viewPagerAdapter?.pageSize ?: 0
            currentPosition = p
            if (isProfilePhoto) setResult(false)
            toolbarTitle?.text = resources.getString(R.string.from_txt, p + 1, totalSize)
            if (count - p < pageSize && count < totalSize)
                loadMore()
        }
    }

    companion object {
        const val PHOTO_VIEWER_RESULT = "PHOTO_VIEWER_RESULT"
        const val CURRENT_POSITION = "CURRENT_POSITION"
        const val AVATARS_CHANGED = "AVATARS_CHANGED"
    }
}
