package com.numplates.nomera3.presentation.viewmodel.profilephoto

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.meera.core.extensions.doAsyncViewModel
import com.meera.core.utils.files.FileManager
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.numplates.nomera3.domain.interactornew.CheckMainFilterRecommendedUseCase
import com.numplates.nomera3.domain.interactornew.DeletePhotoUseCase
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.ProcessAnimatedAvatar
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyAvatarDownloadFrom
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyAvatarPhotoType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.editor.AmplitudeEditor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.editor.AmplitudeEditorParams
import com.numplates.nomera3.modules.baseCore.helper.amplitude.editor.AmplitudeEditorTypeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeAlertPostWithNewAvatarValuesActionType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeAlertPostWithNewAvatarValuesFeedType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudePhotoActionValuesWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeProfile
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudePropertyReactionWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudeReactions
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.entity.UIPostUpdate
import com.numplates.nomera3.modules.feed.ui.entity.toAmplitudePropertyWhence
import com.numplates.nomera3.modules.feed.ui.mapper.toUIPostUpdate
import com.numplates.nomera3.modules.reaction.data.MeeraReactionUpdate
import com.numplates.nomera3.modules.reaction.data.ReactionUpdate
import com.numplates.nomera3.modules.reaction.domain.repository.ReactionRepository
import com.numplates.nomera3.modules.reaction.ui.data.MeeraReactionSource
import com.numplates.nomera3.modules.reaction.ui.data.ReactionSource
import com.numplates.nomera3.modules.user.domain.usecase.DeleteUserAvatarUseCase
import com.numplates.nomera3.modules.user.domain.usecase.GetLocalUserSettingsUseCase
import com.numplates.nomera3.modules.user.domain.usecase.RemoveAvatarItemUseCase
import com.numplates.nomera3.modules.user.domain.usecase.RemoveGalleryItemUseCase
import com.numplates.nomera3.modules.user.domain.usecase.UpdateUserAvatarUseCase
import com.numplates.nomera3.modules.user.domain.usecase.UploadUserAvatarUseCase
import com.numplates.nomera3.modules.userprofile.domain.maper.PhotoModelMapper
import com.numplates.nomera3.modules.userprofile.domain.usecase.GetPostUseCase
import com.numplates.nomera3.modules.userprofile.domain.usecase.GetProfileUseCase
import com.numplates.nomera3.modules.userprofile.domain.usecase.GetUserAvatarsUseCase
import com.numplates.nomera3.modules.userprofile.domain.usecase.GetUserGalleryUseCase
import com.numplates.nomera3.modules.userprofile.domain.usecase.SetAvatarAsMainUseCase
import com.numplates.nomera3.modules.userprofile.ui.model.PhotoModel
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.view.fragments.profilephoto.ProfilePhotoViewerAction
import com.numplates.nomera3.presentation.view.fragments.profilephoto.ProfilePhotoViewerEffect
import com.numplates.nomera3.presentation.viewmodel.viewevents.ProfilePhotoViewEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ProfilePhotoViewerViewModel @Inject constructor(
    val photoModelMapper: PhotoModelMapper,
    val reactionRepository: ReactionRepository,
    val gson: Gson,
    val getUserAvatarUseCase: GetUserAvatarsUseCase,
    val setAvatarAsMainUseCase: SetAvatarAsMainUseCase,
    val getUserGalleryUseCase: GetUserGalleryUseCase,
    val getPostUseCase: GetPostUseCase,
    val removeGalleryItemUseCase: RemoveGalleryItemUseCase,
    val removeAvatarItemUseCase: RemoveAvatarItemUseCase,
    val deletePhotoUseCase: DeletePhotoUseCase,
    val uploadAvatarUseCase: UploadUserAvatarUseCase,
    val getUserUidUseCase: GetUserUidUseCase,
    val deleteUserAvatarUseCase: DeleteUserAvatarUseCase,
    val updateUserAvatarUseCase: UpdateUserAvatarUseCase,
    val amplitudeHelper: AnalyticsInteractor,
    val processAnimatedAvatar: ProcessAnimatedAvatar,
    val amplitudeProfile: AmplitudeProfile,
    val getLocalUserSettingsUseCase: GetLocalUserSettingsUseCase,
    val fileManager: FileManager,
    val _featureTogglesContainer: FeatureTogglesContainer,
    val getProfileUseCase: GetProfileUseCase,
    val amplitudeReactions: AmplitudeReactions,
    val checkMainFilterRecommendedUseCase: CheckMainFilterRecommendedUseCase,
    val amplitudeEditor: AmplitudeEditor
) : ViewModel() {

    var liveLoadMoreAvatars: MutableLiveData<List<PhotoModel>> = MutableLiveData()
    var liveTotalSize: MutableLiveData<Int> = MutableLiveData()
    val liveReactions: MutableLiveData<UIPostUpdate> = MutableLiveData()
    var liveGoToPosition: MutableLiveData<Int> = MutableLiveData()
    var liveTitle: MutableLiveData<String> = MutableLiveData()
    var liveRemovePhoto = MutableLiveData<Long>()
    var liveEvents = MutableLiveData<ProfilePhotoViewEvent>()

    private val _effect: MutableSharedFlow<ProfilePhotoViewerEffect> = MutableSharedFlow()
    val effect: Flow<ProfilePhotoViewerEffect> by lazy { _effect }


    //Кол-во эллементов в ViewPager
    private var numberOfElements = 0

    //шаг (дозагрузка дополнительных элементов)
    private var step = 10

    //url изображения, с которого нужно начать отображение
    private var imgToShowFirst: Int = 0

    //id пользователя
    private var userID = -1L

    private var postId = -1L

    private var isProfilePhoto = false

    //если фото одно, то отображаем только этот url
    private var photoToShow = ""

    //при первой загрузки переходим на позицию
    private var isFirstRequest = true
    private val disposables = CompositeDisposable()

    fun init(position: Int, userID: Long, postId: Long, photoUrl: String, isProfilePhoto: Boolean) {
        refreshValues()
        photoToShow = photoUrl
        this.userID = userID
        this.postId = postId
        this.isProfilePhoto = isProfilePhoto
        imgToShowFirst = position

        when {
            userID != -1L -> requestData()
            postId != -1L -> requestPostData()
            photoToShow.isNotEmpty() -> showPhoto()
        }

        initListenReaction()
        initListenReactionMeera()
    }

    fun handleUIAction(action: ProfilePhotoViewerAction) {
        when (action) {
            is ProfilePhotoViewerAction.MakeAvatar -> handleMakeAvatar(action.position)
            is ProfilePhotoViewerAction.Remove -> handleRemovePhoto(action.position)
            is ProfilePhotoViewerAction.Save -> handleSavePhoto(action.position)
        }
    }

    private fun handleMakeAvatar(position: Int) {
        launchEffect(ProfilePhotoViewerEffect.OnMakeAvatar(position))
        logPhotoActionMakeTheMain()
        if (isProfilePhoto) {
            logMainPhotoChangeChooseFromAvatars()
        } else {
            logMainPhotoChangeChooseFromAbout()
        }
    }

    private fun launchEffect(effect: ProfilePhotoViewerEffect) = viewModelScope.launch {
        _effect.emit(effect)
    }

    private fun handleSavePhoto(position: Int) {
        launchEffect(ProfilePhotoViewerEffect.OnSave(position))
    }

    private fun handleRemovePhoto(position: Int) {
        launchEffect(ProfilePhotoViewerEffect.OnRemove(position))

        logPhotoActionDelete()
    }

    fun getUuid() = getUserUidUseCase.invoke()

    fun deleteFile(path: String) = fileManager.deleteFile(path)

    fun getFeatureTogglesContainer(): FeatureTogglesContainer {
        return _featureTogglesContainer
    }

    private fun refreshValues() {
        numberOfElements = 0
        step = 10
        imgToShowFirst = 0
        isFirstRequest = true
    }

    private fun showPhoto() {
        val data = PhotoModel(
            id = -1,
            imageUrl = photoToShow,
            post = PostUIEntity(),
            isAdult = false
        )
        liveLoadMoreAvatars.postValue(listOf(data))
    }

    private fun requestData() {
        if (isProfilePhoto) getAvatars() else getGallery()
    }

    private fun requestPostData() = viewModelScope.launch {
        runCatching {
            val post = getPostUseCase.invoke(postId = postId)
            liveTotalSize.postValue(1)
            post.getImageUrl()?.let { imageUrl ->
                liveLoadMoreAvatars.postValue(
                    listOf(
                        PhotoModel(
                            id = postId,
                            post = post,
                            imageUrl = imageUrl,
                            isAdult = false
                        )
                    )
                )
            }
        }.onFailure {
            Timber.e(it)
        }
    }

    fun onLoadMore() {
        if (userID != -1L)
            requestData()
    }

    private fun getAvatars() = viewModelScope.launch {
        runCatching {
            val avatars =
                getUserAvatarUseCase.invoke(userId = userID, limit = step + imgToShowFirst, offset = numberOfElements)
            numberOfElements += avatars.avatars.size
            liveTotalSize.postValue(avatars.count)
            liveLoadMoreAvatars.postValue(avatars.avatars.map { photoModelMapper.avatarModelToPhotoModel(it) })
            if (isFirstRequest) {
                isFirstRequest = false
                liveGoToPosition.postValue(imgToShowFirst)
            }
        }.onFailure {
            Timber.e(it)
        }
    }

    private fun getGallery() = viewModelScope.launch {
        runCatching {
            val gallery =
                getUserGalleryUseCase.invoke(userId = userID, limit = step + imgToShowFirst, offset = numberOfElements)
            numberOfElements += gallery.items.size
            liveTotalSize.postValue(gallery.count)
            liveLoadMoreAvatars.postValue(gallery.items.map { photoModelMapper.userGalleryModelToPhotoModel(it) })
            if (isFirstRequest) {
                isFirstRequest = false
                liveGoToPosition.postValue(imgToShowFirst)
            }
        }.onFailure {
            Timber.e(it)
        }
    }

    override fun onCleared() {
        Timber.d("onCleared called!!!")
        disposables.clear()
    }

    fun onConfirmedDelete(photoId: Long?) {
        val existPhotoId: Long = photoId ?: return
        viewModelScope.launch {
            kotlin.runCatching {
                if (isProfilePhoto) {
                    removeAvatarItemUseCase.invoke(existPhotoId)
                } else {
                    removeGalleryItemUseCase.invoke(existPhotoId)
                }
            }.onSuccess {
                liveRemovePhoto.value = existPhotoId
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    fun setPhotoAsMainById(photoId: Long) {
        viewModelScope.launch {
            kotlin.runCatching {
                setAvatarAsMainUseCase.invoke(photoId)
            }.onSuccess {
                liveEvents.value = ProfilePhotoViewEvent.OnPhotoUploadSuccess(it.big, 0)
            }.onFailure {
                Timber.e(it)
                liveEvents.value = ProfilePhotoViewEvent.OnPhotoUploadError
            }
        }
    }

    fun uploadUserAvatar(imagePath: String, animation: String?, createAvatarPost: Int, saveSettings: Int) {
        viewModelScope.launch {
            runCatching {
                updateUserAvatarUseCase.invoke(
                    imagePath = imagePath,
                    animation = animation,
                    createAvatarPost = createAvatarPost,
                    saveSettings = saveSettings
                )
            }.onSuccess {
                deleteTempImageFile(imagePath)
                liveEvents.value = ProfilePhotoViewEvent.OnPhotoUploadSuccess(it.avatarBig, createAvatarPost)
            }.onFailure {
                Timber.e(it)
                deleteTempImageFile(imagePath)
                liveEvents.value = ProfilePhotoViewEvent.OnPhotoUploadError
            }
        }
    }

    fun requestCreateAvatarPostSettings(imagePath: String, animation: String?, isSendAvatarPost: Boolean = true) {
        viewModelScope.launch {
            kotlin.runCatching {
                getLocalUserSettingsUseCase.invoke().find { it.key == SettingsKeyEnum.CREATE_AVATAR_POST.key }
            }.onSuccess {
                liveEvents.postValue(
                    ProfilePhotoViewEvent.OnCreateAvatarPostSettings(
                        privacySettingModel = it,
                        imagePath = imagePath,
                        animation = animation,
                        isSendAvatarPost = isSendAvatarPost
                    )
                )
            }.onFailure {
                liveEvents.postValue(
                    ProfilePhotoViewEvent.OnCreateAvatarPostSettings(
                        privacySettingModel = null,
                        imagePath = imagePath,
                        animation = animation
                    )
                )
                Timber.e(it)
            }
        }
    }


    private fun initListenReaction() {
        val disposable = reactionRepository
            .getCommandReactionStream()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { reactionUpdate ->
                proceedReaction(reactionUpdate)
            }

        disposables.add(disposable)
    }

    private fun initListenReactionMeera() {
        val disposable = reactionRepository
            .getCommandReactionStreamMeera()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { reactionUpdate ->
                proceedReactionMeera(reactionUpdate)
            }

        disposables.add(disposable)
    }

    private fun proceedReaction(reactionUpdate: ReactionUpdate) {
        when (reactionUpdate.reactionSource) {
            is ReactionSource.Post -> {
                updatePostReaction(reactionUpdate)
            }

            else -> Unit
        }
    }

    private fun proceedReactionMeera(reactionUpdate: MeeraReactionUpdate) {
        when (reactionUpdate.reactionSource) {
            is MeeraReactionSource.Post -> {
                updatePostReactionMeera(reactionUpdate)
            }

            else -> Unit
        }
    }

    private fun updatePostReaction(reactionUpdate: ReactionUpdate) {
        liveReactions.postValue(reactionUpdate.toUIPostUpdate())
    }

    private fun updatePostReactionMeera(reactionUpdate: MeeraReactionUpdate) {
        liveReactions.postValue(reactionUpdate.toUIPostUpdate())
    }


    private fun deleteTempImageFile(filePath: String?) {
        filePath?.let {
            doAsyncViewModel({
                try {
                    val extension = filePath.substring(filePath.lastIndexOf("."))
                    Timber.d("Temp image file extension: $extension")
                    if (extension != ".gif")
                        return@doAsyncViewModel fileManager.deleteFile(it)
                    else
                        return@doAsyncViewModel false
                } catch (e: Exception) {
                    Timber.e(e)
                    return@doAsyncViewModel false
                }
            }, { isDeleted ->
                Timber.d("Temp image file isDeleted: $isDeleted")
            })
        }
    }

    fun generateBitmapFromAvatarState(avatarState: String, userId: Long? = null) {
        viewModelScope.launch {
            var uniqueName: String? = null
            if (userId != null) {
                kotlin.runCatching {
                    getProfileUseCase.invoke(userId).uniquename
                }.onSuccess {
                    uniqueName = it
                }
            }
            kotlin.runCatching {
                val bitmap = processAnimatedAvatar.createBitmap(avatarState)
                processAnimatedAvatar.saveInFileWithWaterMarkWithUniqueName(bitmap, uniqueName)
            }.onSuccess { path ->
                liveEvents.postValue(ProfilePhotoViewEvent.OnAnimatedAvatarSaved(path))
            }.onFailure {
                Timber.i(it)
            }
        }
    }


    fun onNewPhotoClicked() {
        amplitudeHelper.logAvatarPickerOpen()
    }

    fun logAvatarDownloaded(isMe: Boolean, isPhoto: Boolean) {
        val from =
            if (isMe) AmplitudePropertyAvatarDownloadFrom.OWN_PROFILE else AmplitudePropertyAvatarDownloadFrom.OTHER_PROFILE
        val type =
            if (isPhoto) AmplitudePropertyAvatarPhotoType.PHOTO else AmplitudePropertyAvatarPhotoType.ANIMATED_AVATAR
        amplitudeHelper.logAvatarDownloaded(from, type)
    }

    fun logOpenEditor() = viewModelScope.launch {
        amplitudeEditor.editorOpenAction(
            automaticOpen = true,
            where = AmplitudePropertyWhere.PROFILE,
            type = AmplitudeEditorTypeProperty.PHOTO
        )
    }

    fun logPhotoEdits(nmrAmplitude: NMRPhotoAmplitude) =
        viewModelScope.launch {
            amplitudeEditor.photoEditorAction(
                editorParams = AmplitudeEditorParams(
                    where = AmplitudePropertyWhere.PROFILE,
                    automaticOpen = true
                ),
                nmrAmplitude = nmrAmplitude
            )
        }

    fun logPhotoActionSave() {
        amplitudeProfile.photoActionSave(
            userId = getUuid(),
            authorId = userID,
            where = if (isProfilePhoto) AmplitudePhotoActionValuesWhere.AVATAR_PROFILE else AmplitudePhotoActionValuesWhere.GALLERY
        )
    }

    fun logPhotoActionPhotoChange() {
        amplitudeProfile.photoActionPhotoChange(
            userId = getUuid(),
            authorId = userID,
            where = if (isProfilePhoto) AmplitudePhotoActionValuesWhere.AVATAR_PROFILE else AmplitudePhotoActionValuesWhere.GALLERY
        )
    }

    fun logPhotoActionAvatarCreate() {
        amplitudeProfile.photoActionAvatarCreate(
            userId = getUuid(),
            authorId = userID,
            where = if (isProfilePhoto) AmplitudePhotoActionValuesWhere.AVATAR_PROFILE else AmplitudePhotoActionValuesWhere.GALLERY
        )
    }

    fun logPhotoActionDelete() {
        amplitudeProfile.photoActionDelete(
            userId = getUuid(),
            authorId = userID,
            where = if (isProfilePhoto) AmplitudePhotoActionValuesWhere.AVATAR_PROFILE else AmplitudePhotoActionValuesWhere.GALLERY
        )
    }

    fun logPhotoActionMakeTheMain() {
        amplitudeProfile.photoActionMakeTheMain(
            userId = getUuid(),
            authorId = userID,
            where = if (isProfilePhoto) AmplitudePhotoActionValuesWhere.AVATAR_PROFILE else AmplitudePhotoActionValuesWhere.GALLERY
        )
    }

    fun logMainPhotoChangeChooseFromAbout() {
        amplitudeProfile.mainPhotoChangesChooseFromAbout(getUuid())
    }

    fun logMainPhotoChangeChooseFromAvatars() {
        amplitudeProfile.mainPhotoChangesChooseFromAvatars(getUuid())
    }


    fun logAlertPostWithNewAvatarAction(
        actionType: AmplitudeAlertPostWithNewAvatarValuesActionType,
        feedType: AmplitudeAlertPostWithNewAvatarValuesFeedType,
        toggle: Boolean
    ) {
        amplitudeProfile.alertPostWithNewAvatarAction(
            actionType = actionType,
            feedType = feedType,
            toggle = toggle,
            userId = getUuid()
        )
    }

    fun logPrivacyPostWithNewAvatarChange(createAvatarPost: Int) {
        amplitudeProfile.privacyPostWithNewAvatarChangeAlert(createAvatarPost)
    }

    fun logStatisticReactionsTap(where: AmplitudePropertyReactionWhere, originEnum: DestinationOriginEnum?) {
        amplitudeReactions.statisticReactionsTap(
            where = where,
            whence = originEnum.toAmplitudePropertyWhence(),
            recFeed = checkMainFilterRecommendedUseCase.invoke()
        )
    }
}
