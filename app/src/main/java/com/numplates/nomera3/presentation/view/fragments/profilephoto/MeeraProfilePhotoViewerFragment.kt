package com.numplates.nomera3.presentation.view.fragments.profilephoto

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.loader.glide.GlideImageLoader
import com.google.android.material.appbar.AppBarLayout
import com.meera.core.base.BaseLoadImages
import com.meera.core.base.BaseLoadImagesDelegate
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.core.keyboard.getRootView
import com.meera.core.utils.graphics.NGraphics
import com.meera.core.utils.layouts.intercept.MeeraInterceptTouchFrameLayout
import com.meera.core.utils.showCommonError
import com.meera.core.utils.showCommonSuccessMessage
import com.meera.media_controller_api.model.MediaControllerCallback
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.buttons.ButtonType
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.meera.uikit.widgets.snackbar.SnackLoadingUiState
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.numplates.nomera3.App
import com.numplates.nomera3.MEDIA_EXT_GIF
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentGalleryFullscreenBinding
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegate
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegateImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeAlertPostWithNewAvatarValuesActionType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeAlertPostWithNewAvatarValuesFeedType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudePropertyReactionWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.createAmplitudeProfileReactionsParams
import com.numplates.nomera3.modules.feed.ui.adapter.MeeraContentActionBar
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.ui.MeeraReactionBubbleViewController
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingAnimationPlayListener
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingReaction
import com.numplates.nomera3.modules.reaction.ui.data.MeeraReactionSource
import com.numplates.nomera3.modules.reaction.ui.util.reactionCount
import com.numplates.nomera3.modules.reactionStatistics.ui.MeeraReactionsStatisticsBottomSheetFragment
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsEntityType
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.modules.userprofile.ui.model.PhotoModel
import com.numplates.nomera3.presentation.model.enums.CreateAvatarPostEnum
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_ANIMATED_AVATAR
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GALLERY_IMAGES_COUNT
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GALLERY_POSITION
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_IMAGE_URL
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_IS_ANIMATED_AVATAR
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_POST_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.utils.ReactionAnimationHelper
import com.numplates.nomera3.presentation.view.adapter.MeeraTouchImageAdapterNew
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.postavatar.MeeraPostAvatarBottomSheetFragment
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.postavatar.PostAvatarAlertListener
import com.numplates.nomera3.presentation.view.ui.bottomMenu.ReactionsStatisticBottomMenu
import com.numplates.nomera3.presentation.viewmodel.profilephoto.ProfilePhotoViewerViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.ProfilePhotoViewEvent
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

private const val MEERA_MENU_BOTTOM_SHEET_DIALOG = "MEERA_MENU_BOTTOM_SHEET_DIALOG"

class MeeraProfilePhotoViewerFragment :
    MeeraBaseDialogFragment(layout = R.layout.meera_fragment_gallery_fullscreen, ScreenBehaviourState.FullScreenMoment),
    BaseLoadImages by BaseLoadImagesDelegate(), BasePermission by BasePermissionDelegate(),
    SaveMediaFileDelegate by SaveMediaFileDelegateImpl(), PostAvatarAlertListener {

    private val viewModel by viewModels<ProfilePhotoViewerViewModel>() {
        App.component.getViewModelFactory()
    }

    private var mainLayout: LinearLayout? = null
    private var viewPager: ViewPager? = null
    private var frameViewPager: FrameLayout? = null
    private var toolbar: Toolbar? = null
    private var appBarLayout: AppBarLayout? = null
    private var ivMenuBtn: ImageView? = null

    private var viewPagerAdapter: MeeraTouchImageAdapterNew? = null
    private var animatedAvatarState: String? = null
    private var reactionAnimationHelper: ReactionAnimationHelper? = null

    private var position = 0
    private var userID = -1L
    private var postId = -1L
    private var photoUrl = ""
    private val act: MeeraAct by lazy {
        requireActivity() as MeeraAct
    }
    private var currentPosition = 0
    private var totalSize = 0
        set(value) {
            field = value
            val currentItem = viewPager?.currentItem ?: 0
            if (totalSize > 1) {
                binding?.vNavView?.title = resources.getString(R.string.meera_from_txt, currentItem + 1, totalSize)
            }
        }
    private var isProfilePhoto = false
    private var isOwnPhotoProfile = false
    private var isAnimatedAvatar = false

    private var isHorizonOrientation = false

    private var originEnum: DestinationOriginEnum? = null
    private val binding by viewBinding(MeeraFragmentGalleryFullscreenBinding::bind)

    override val containerId: Int
        get() = R.id.fragment_first_container_view

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
    }

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


    @SuppressLint("SuspiciousIndentation")
    private fun initObservable() {
        viewModel.liveReactions.observe(viewLifecycleOwner, Observer { postUpdate ->
            val position: Int = viewPagerAdapter?.updateReactionsData(postUpdate) ?: return@Observer
            viewPager?.findViewWithTag<View>(MeeraTouchImageAdapterNew.TAG_PAGE + position)?.let { touchImageView ->
                viewPagerAdapter?.updateReactionsView(
                    view = touchImageView, position = position, postUpdate = postUpdate
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
            binding?.vNavView?.title = it
        })


        viewModel.liveRemovePhoto.observe(viewLifecycleOwner, Observer {
            setResult(true)
            totalSize -= 1
            if (totalSize == 0) findNavController().popBackStack()
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
                    if (it.privacySettingModel?.value == CreateAvatarPostEnum.PRIVATE_ROAD.state
                        || it.privacySettingModel?.value == CreateAvatarPostEnum.MAIN_ROAD.state
                    ) {
                        onPublishOptionsSelected(
                            imagePath = it.imagePath,
                            animation = it.animation,
                            createAvatarPost = it.privacySettingModel.value,
                            saveSettings = 1,
                            amplitudeActionType = AmplitudeAlertPostWithNewAvatarValuesActionType.PUBLISH
                        )
                    } else {
                        findNavController().popBackStack()
                        if (it.isSendAvatarPost) {
                            showPublishPostAlert(imagePath = it.imagePath, animation = it.animation)
                        } else {
                            showProgress()
                        }
                    }
                }
            }
        })

        viewModel.effect.onEach { effect -> handleViewEffects(effect) }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun handleViewEffects(effect: ProfilePhotoViewerEffect) {
        when (effect) {
            is ProfilePhotoViewerEffect.OnSave -> handleSave(effect.position)
            is ProfilePhotoViewerEffect.OnMakeAvatar -> handleMakeAvatar(effect.position)
            is ProfilePhotoViewerEffect.OnRemove -> handleRemove(effect.position)
        }
    }

    private fun handleRemove(position: Int) {
        try {
            val photoToDelete = viewPagerAdapter?.gallery?.get(position)
            showDeleteConfirmDialog(photoToDelete?.id)
        } catch (e: Exception) {
            Timber.d(e)
        }
    }

    private fun showDeleteConfirmDialog(photoId: Long?) {
        MeeraConfirmDialogBuilder()
            .setHeader(R.string.user_personal_info_delete_photo_confirmation)
            .setTopBtnText(R.string.yes)
            .setTopBtnType(ButtonType.FILLED)
            .setCancelable(true)
            .setTopClickListener { viewModel.onConfirmedDelete(photoId) }
            .setBottomBtnText(R.string.cancel)
            .show(childFragmentManager)
    }

    private fun handleMakeAvatar(position: Int) {
        val photoItem = viewPagerAdapter?.gallery?.getOrNull(position) ?: return
        showSetMainConfirmDialog(photoItem)
    }

    private fun showSetMainConfirmDialog(photoModel: PhotoModel) {
        MeeraConfirmDialogBuilder()
            .setHeader(R.string.meera_user_personal_info_set_main_photo_confirmation)
            .setTopBtnText(R.string.yes)
            .setTopBtnType(ButtonType.FILLED)
            .setTopClickListener {
                if (isProfilePhoto) {
                    viewModel.setPhotoAsMainById(photoModel.id)
                    showProgress()
                } else {
                    if (photoModel.imageUrl.contains(MEDIA_EXT_GIF)) {
                        NGraphics.saveImageToDevice(context = requireContext(),
                            photoModel.imageUrl,
                            { it.path?.let { checkAvatarPostSettings(it) } })
                        showProgress()
                    } else {
                        NGraphics.saveImageToDevice(context = requireContext(),
                            imageUrl = photoModel.imageUrl,
                            onSaved = { uri ->
                                openPhotoEditor(uri)
                            })
                    }
                }
            }
            .setBottomBtnText(R.string.cancel)
            .show(childFragmentManager)
    }

    var infoSnackbar: UiKitSnackBar? = null

    private fun showProgress() {
        infoSnackbar = UiKitSnackBar.make(
            view = requireView(), params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(R.string.user_personal_info_setting_main_photo),
                    loadingUiState = SnackLoadingUiState.ProgressState
                ),
            )
        )
        infoSnackbar?.show()

    }


    private fun openPhotoEditor(imagePath: Uri, isSendAvatarPost: Boolean = true) {
        viewModel.logOpenEditor()
        act.getMediaControllerFeature().open(uri = imagePath,
            openPlace = MediaControllerOpenPlace.Avatar,
            callback = object : MediaControllerCallback {
                override fun onPhotoReady(
                    resultUri: Uri, nmrAmplitude: NMRPhotoAmplitude?
                ) {
                    resultUri.path?.let {
                        checkAvatarPostSettings(
                            imagePath = it,
                            isSendAvatarPost = isSendAvatarPost
                        )
                    } ?: kotlin.run {
                        showMediaEditingError()
                    }
                    nmrAmplitude?.let(viewModel::logPhotoEdits)
                }

                override fun onError() {
                    showMediaEditingError()
                }
            })
    }

    private fun showMediaEditingError() {
        infoSnackbar = UiKitSnackBar.make(
            view = requireView(), params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(R.string.error_editing_media),
                    avatarUiState = AvatarUiState.ErrorIconState,
                ),
            )
        )
        infoSnackbar?.show()
    }


    private fun checkAvatarPostSettings(
        imagePath: String,
        animation: String? = null,
        isSendAvatarPost: Boolean = true
    ) {
        viewModel.requestCreateAvatarPostSettings(
            imagePath = imagePath,
            animation = animation,
            isSendAvatarPost = isSendAvatarPost
        )
    }

    private fun handleSave(position: Int) {
        viewPagerAdapter?.gallery?.getOrNull(position)?.let { photoItem ->
            photoItem.imageUrl.let { photoUrl ->
                if (photoItem.animation.isNullOrEmpty()) {
                    saveImage(photoUrl)
                } else {
                    viewModel.generateBitmapFromAvatarState(photoItem.animation, userId = userID)
                }

                viewModel.logAvatarDownloaded(isOwnPhotoProfile, true)
                viewModel.logPhotoActionSave()
            }
        }
    }

    private fun showPublishPostAlert(imagePath: String, animation: String? = null) {
        MeeraPostAvatarBottomSheetFragment
            .getInstance(photoPath = imagePath, animation = animation)
            .show(this.childFragmentManager)
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

        showProgress()
        viewModel.logAlertPostWithNewAvatarAction(
            actionType = amplitudeActionType, feedType = amplitudeFeedType, toggle = saveSettings.toBoolean()
        )


        if (saveSettings.toBoolean()) {
            viewModel.logPrivacyPostWithNewAvatarChange(createAvatarPost)
        }
    }

    private fun updatePhotos(photoModels: List<PhotoModel>) {
        viewPagerAdapter?.loadMore(photoModels)
    }

    private fun handleAnimatedAvatarPath(path: String) {
        NGraphics.saveImageToDeviceFromAppDirectory(requireActivity(), path) { savedImageUri ->
            viewModel.deleteFile(path)
            if (savedImageUri != null) {
                viewModel.logAvatarDownloaded(isOwnPhotoProfile, false)
                showCommonSuccessMessage(getText(R.string.meera_saved_on_device), requireView())
            } else {
                showCommonError(getText(R.string.avatar_save_file_fail), requireView())
            }
        }
    }

    private fun handleAvatarRemovedError() {
        showCommonError(getText(R.string.error_while_deleting_avatar), requireView())
    }

    private fun handleAvatarRemoved() {
        showCommonSuccessMessage(getText(R.string.avatar_successfully_deleted), requireView())
        findNavController().popBackStack()
    }

    private fun handleSuccessUpload(event: ProfilePhotoViewEvent.OnPhotoUploadSuccess) {
        val message = when (event.createAvatarPost) {
            CreateAvatarPostEnum.PRIVATE_ROAD.state, CreateAvatarPostEnum.MAIN_ROAD.state -> R.string.profile_avatar_update_success_with_post
            else -> R.string.profile_avatar_update_success
        }

        infoSnackbar = UiKitSnackBar.make(
            view = requireView(), params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(message),
                    avatarUiState = AvatarUiState.SuccessIconState,
                ),
            )
        )
        infoSnackbar?.show()
    }

    private fun handleErrorUpload() {
        binding?.pbImageView?.gone()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initReactionAnimationHelper()
        initView()
        loadAdapter()
        initObservable()

        context?.let {
            BigImageViewer.initialize(GlideImageLoader.with(it.applicationContext))
        }
        viewModel.init(
            position = position,
            userID = userID,
            postId = postId,
            photoUrl = photoUrl,
            isProfilePhoto = isProfilePhoto
        )
    }

    override fun onDestroyView() {
        reactionAnimationHelper = null
        super.onDestroyView()
    }

    private fun initView() {
        ivMenuBtn = binding.ivMenuToolbar
        mainLayout = binding.llFragmentGalleryFullscreen
        viewPager = binding.viewPagerFragmentGallery
        mainLayout?.setBackgroundColor(Color.WHITE)
        appBarLayout = binding.blGalleryFullscreen

        frameViewPager = binding.flFragmentGalleryFullscreen

        toolbar = binding.toolbar
        viewPager?.addOnPageChangeListener(Listener())

        initStatusBar()
        if (userID != -1L) {
            if (totalSize > 1) {
                binding?.vNavView?.title = resources.getString(R.string.meera_from_txt, 1, totalSize)
            }
        } else if (userID == -1L && isProfilePhoto) binding?.vNavView?.title =
            getString(R.string.user_personal_info_photo_header)
        else if (userID == -1L && !isProfilePhoto) binding?.vNavView?.title = getString(R.string.image)

        setupBlackStyle()

        if (originEnum == DestinationOriginEnum.COMMUNITY) {
            binding.vNavView.title = String.empty()
        }
    }

    private fun initReactionAnimationHelper() {
        reactionAnimationHelper = ReactionAnimationHelper()
    }

    private fun setupBlackStyle() {
        appBarLayout?.setBackgroundColor(Color.BLACK)
        frameViewPager?.setBackgroundColor(Color.BLACK)
        ivMenuBtn?.setColorFilter(Color.WHITE)
        binding?.vNavView?.setTextColor(R.color.ui_white)
        binding?.vNavView?.setIconsTint(R.color.ui_white)
    }

    private fun initStatusBar() {
        binding?.vNavView?.closeButtonClickListener = {
            findNavController().popBackStack()
        }

        ivMenuBtn?.setThrottledClickListener {
            if (isOwnPhotoProfile) {
                showOwnProfilePhotoMenu()
            } else {
                showSaveMenu()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        binding.root.gone()
    }

    private fun showOwnProfilePhotoMenu() {
        val currentItem = viewPager?.currentItem ?: 0
        MeeraMenuBottomSheetFragment(
            isProfilePhoto = isProfilePhoto,
            currentItemPosition = currentItem,
            handleUIAction = viewModel::handleUIAction
        ).show(childFragmentManager, MEERA_MENU_BOTTOM_SHEET_DIALOG)
    }

    private fun showSaveMenu() {
        val currentItem = viewPager?.currentItem ?: 0
        MeeraMenuBottomSheetFragment(
            isProfilePhoto = isProfilePhoto,
            isOwnPhotoProfile = isOwnPhotoProfile,
            currentItemPosition = currentItem,
            handleUIAction = viewModel::handleUIAction
        ).show(childFragmentManager, MEERA_MENU_BOTTOM_SHEET_DIALOG)

    }

    private fun loadAdapter() {
        viewPagerAdapter = MeeraTouchImageAdapterNew(isOwnPhotoProfile)
        viewPager?.adapter = viewPagerAdapter

        viewPagerAdapter?.callback = object : MeeraTouchImageAdapterNew.TouchImageAdapterInteraction {
            override fun onImageClicked() {
                if (isHorizonOrientation) return
            }

            override fun onImageLongClick(imageUrl: String?) = Unit
        }
        viewPagerAdapter?.profilePhotoReactionsListener = object : MeeraProfilePhotoReactionsListener {
            override fun onReactionBottomSheetShow(post: PostUIEntity) /*= needAuth()*/ {
                if (viewModel.getFeatureTogglesContainer().detailedReactionsForPostFeatureToggle.isEnabled) {
                    NavigationManager.getManager().initGraph(
                        resId = R.navigation.bottom_reactions_statistics_graph,
                        startDestinationArgs = bundleOf(
                            MeeraReactionsStatisticsBottomSheetFragment.ARG_ENTITY_ID to post.postId,
                            MeeraReactionsStatisticsBottomSheetFragment.ARG_ENTITY_TYPE to ReactionsEntityType.POST
                        )
                    )
                } else {
                    val reactions = post.reactions ?: return//@needAuth
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
                reactionHolderViewId: MeeraContentActionBar.ReactionHolderViewId
            ) {
                val reactionsParams = createAmplitudeProfileReactionsParams(
                    authorId = userID, originEnum = originEnum, where = wherePhotoWasOpened()
                )
                act.getMeeraReactionBubbleViewController().showReactionBubble(
                    reactionSource = MeeraReactionSource.Post(
                        postId = post.postId, reactionHolderViewId = reactionHolderViewId, originEnum = originEnum
                    ),
                    reactionsParams = reactionsParams,
                    showPoint = showPoint,
                    viewsToHide = viewsToHide,
                    reactionTip = reactionTip,
                    currentReactionsList = post.reactions ?: emptyList(),
                    postedAt = post.date,
                    contentActionBarType = MeeraContentActionBar.ContentActionBarType.DARK,
                    containerInfo = getDefaultReactionContainer(requireActivity())
                )
            }

            override fun onReactionRegularClicked(
                post: PostUIEntity, reactionHolderViewId: MeeraContentActionBar.ReactionHolderViewId
            ) {
                val reactionsParams = createAmplitudeProfileReactionsParams(
                    authorId = userID, originEnum = originEnum, where = wherePhotoWasOpened()
                )
                act.getMeeraReactionBubbleViewController().onSelectDefaultReaction(
                    reactionSource = MeeraReactionSource.Post(
                        postId = post.postId, reactionHolderViewId = reactionHolderViewId, originEnum = originEnum
                    ), reactionsParams = reactionsParams, currentReactionsList = post.reactions ?: emptyList()
                )
            }

            override fun onReactionClickToShowScreenAnimation(
                reactionEntity: ReactionEntity,
                anchorViewLocation: Pair<Int, Int>
            ) {
                val reactionType = ReactionType.getByString(reactionEntity.reactionType) ?: return
                reactionAnimationHelper?.playLottieAtPosition(
                    recyclerView = null,
                    requireContext(),
                    parent = binding.flFragmentGalleryFullscreen,
                    reactionType = reactionType,
                    x = anchorViewLocation.first.toFloat(),
                    y = anchorViewLocation.second.toFloat()
                )
            }

            override fun onFlyingAnimationInitialized(flyingReaction: FlyingReaction) {
                val container = requireActivity().getRootView() as? ViewGroup
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

    private fun saveImage(imageUrl: String) {
        saveImageOrVideoFile(imageUrl = imageUrl,
            act = requireActivity(),
            viewLifecycleOwner = viewLifecycleOwner,
            successListener = {
                showCommonSuccessMessage(getText(R.string.meera_saved_on_device), requireView())
            })
    }

    override fun onStart() {
        super.onStart()
        hideAllSensitive()
    }

    private fun setResult(avatarsChanged: Boolean?) {
        setFragmentResult(
            requestKey = PHOTO_VIEWER_RESULT, result = bundleOf(
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
            viewPager?.findViewWithTag<View>(MeeraTouchImageAdapterNew.TAG_PAGE + (position + i))
                ?.let { touchImageView ->
                    viewPagerAdapter?.setupPage(touchImageView, position + i)
                }
        }
    }

    private fun getDefaultReactionContainer(activity: Activity): MeeraReactionBubbleViewController.ContainerInfo {
        val containerInterceptLayout = activity.findViewById<FrameLayout>(R.id.fl_fragment_gallery_fullscreen)
        val bypassInterceptLayout =
            activity.findViewById<MeeraInterceptTouchFrameLayout>(R.id.ll_fragment_gallery_fullscreen)
        return MeeraReactionBubbleViewController.ContainerInfo(
            container = containerInterceptLayout, bypassLayouts = listOf(bypassInterceptLayout)
        )
    }

    inner class Listener : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(p: Int) {
            val count = viewPagerAdapter?.count ?: 0
            val pageSize = viewPagerAdapter?.pageSize ?: 0
            currentPosition = p
            setResult(false)
            binding?.vNavView?.title = resources.getString(R.string.meera_from_txt, p + 1, totalSize)
            if (count - p < pageSize && count < totalSize) loadMore()
        }
    }

    companion object {
        const val PHOTO_VIEWER_RESULT = "PHOTO_VIEWER_RESULT"
        const val CURRENT_POSITION = "CURRENT_POSITION"
        const val AVATARS_CHANGED = "AVATARS_CHANGED"
    }
}
