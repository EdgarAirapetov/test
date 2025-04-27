package com.numplates.nomera3.presentation.viewmodel.profilephoto

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.work.WorkInfo
import com.google.gson.Gson
import com.meera.core.extensions.doAsyncViewModel
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.utils.files.FileManager
import com.numplates.nomera3.App
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.UploadAlbumImageUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.tracker.FireBaseAnalytics
import com.numplates.nomera3.modules.userprofile.domain.usecase.GetUserGalleryUseCase
import com.numplates.nomera3.presentation.upload.IUploadContract
import com.numplates.nomera3.presentation.view.adapter.profilephoto.GridPhotoRecyclerModel
import com.numplates.nomera3.presentation.view.adapter.profilephoto.GridPhotoSourceFactory
import com.numplates.nomera3.presentation.view.adapter.profilephoto.ProfilePhotoStorage
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.GridProfilePhotoEvent
import com.numplates.nomera3.presentation.viewmodel.viewevents.GridProfileViewEvent
import com.numplates.nomera3.presentation.viewmodel.viewevents.SingleLiveEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.Executors
import javax.inject.Inject

class GridProfilePhotoViewModel : BaseViewModel() {

    @Inject
    lateinit var webSocket: WebSocketMainChannel

    @Inject
    lateinit var getUserGalleryUseCase: GetUserGalleryUseCase

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var uploadAlbumImageUseCase: UploadAlbumImageUseCase

    @Inject
    lateinit var getUserUidUseCase: GetUserUidUseCase

    @Inject
    lateinit var uploadHelper: IUploadContract

    @Inject
    lateinit var tracker: AnalyticsInteractor

    @Inject
    lateinit var fbAnalytic: FireBaseAnalytics

    @Inject
    lateinit var fileManager: FileManager

    private lateinit var storage: ProfilePhotoStorage
    private lateinit var dataSource: GridPhotoSourceFactory
    private var userID: Long = -1
    private val disposables = CompositeDisposable()

    var liveUploadMediaToGallery: LiveData<WorkInfo>? = null
    var liveStartImageEventSingle = SingleLiveEvent<Int>()
    var liveErrorEvent: MutableLiveData<GridProfilePhotoEvent> = MutableLiveData()
    val liveViewEvents = MutableLiveData<GridProfileViewEvent>()
    lateinit var pagedListLiveData: LiveData<PagedList<GridPhotoRecyclerModel>>

    override fun onCleared() {
        storage.clear()
    }

    fun logScreenForFragment(sceenName: String) = fbAnalytic.logScreenForFragment(sceenName)

    /**
     * Uploading photo to user's galary
     * */
    fun uploadUserPhoto(imagePath: String?, userId: Long?, needToDeletePhoto: Boolean = false) {
        if (imagePath.isNullOrEmpty() || userId == null || userId == -1L)
            return

        Timber.d("Upload user photos PATH: $imagePath")
        uploadAlbumImageUseCase.uploadAlbumImage(imagePath)?.let { upload ->
            disposables.add(
                upload
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Timber.d("Successfully upload Photo to album")
                        liveViewEvents.value = GridProfileViewEvent.OnPhotoLoadedSuccess
                        if (needToDeletePhoto)
                            deleteTempImageFile(imagePath)
                    }, { error ->
                        Timber.e("ERROR: Upload album image: $error")
                        liveViewEvents.value = GridProfileViewEvent.OnPhotoLoadedError
                        if (needToDeletePhoto)
                            deleteTempImageFile(imagePath)
                    })
            )
        }

    }

    fun uploadUserPhotos(params: List<Uri>?) {
        liveUploadMediaToGallery = uploadHelper.uploadImageToGallery(params)
    }

    fun init(user: Long) {
        App.component.inject(this)
        userID = if (user == -1L) getUserUid() else user
        provideData()
    }

    fun getUserUid() = getUserUidUseCase.invoke()

    fun onPhotoClicked(position: Int) {
        liveStartImageEventSingle.postValue(position)
    }

    /**
     * Refresh photo list
     * */
    fun refreshPhotoList() {
        dataSource.invalidate()
    }

    fun onAddPhotoClicked() {
        tracker.logAvatarPickerOpen()
    }

    private fun provideData() {
        storage = ProfilePhotoStorage(
            getUserGalleryUseCase,
            userID,
            viewModelScope,
            userID == getUserUid()
        ) { isShowErrorMessage ->
            if (isShowErrorMessage) {
                liveErrorEvent.postValue(GridProfilePhotoEvent.OnErrorSocket)
            } else {
                liveErrorEvent.postValue(GridProfilePhotoEvent.OnCloseGalleryScreen)
            }
        }

        // DataSource
        dataSource = GridPhotoSourceFactory(storage)

        // Config
        val pagedListConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(10)
            .build()

        pagedListLiveData = LivePagedListBuilder(dataSource, pagedListConfig)
            .setFetchExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()))
            .build()

    }

    /**
     * Delete photo copy after uploading it to server
     * */
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
}
