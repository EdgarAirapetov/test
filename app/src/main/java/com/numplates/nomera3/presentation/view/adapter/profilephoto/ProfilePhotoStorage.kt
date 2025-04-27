package com.numplates.nomera3.presentation.view.adapter.profilephoto

import androidx.paging.PositionalDataSource
import com.numplates.nomera3.data.newmessenger.response.Data
import com.numplates.nomera3.data.newmessenger.response.Image
import com.numplates.nomera3.data.newmessenger.response.Photo
import com.numplates.nomera3.modules.userprofile.domain.model.UserGalleryModel
import com.numplates.nomera3.modules.userprofile.domain.usecase.GetUserGalleryUseCase
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import java.util.Date

open class ProfilePhotoStorage(
    private val getUserGalleryUseCase: GetUserGalleryUseCase,
    private val userID: Long,
    private val scope: CoroutineScope,
    private val isMe: Boolean,
    private val errorCallback: (isShowErrorMessage: Boolean) -> Unit,
) {
    private val disposables = CompositeDisposable()
    private var currentYear: Int = 0
    private var lastDate: Date? = null
    private var separatorCount = 0

    init {
        val calendar = Calendar.getInstance()
        currentYear = calendar.get(Calendar.YEAR)
    }

    fun getDataRange(from: Int, to: Int, callback: PositionalDataSource.LoadRangeCallback<GridPhotoRecyclerModel>) {
        scope.launch {
            runCatching {
                val photos = getUserGalleryUseCase.invoke(userID, limit = to, offset = from - separatorCount)
                callback.onResult(prepareData(photos))
            }.onFailure {
                errorCallback.invoke(false)
                Timber.e(it)
            }
        }
    }

    fun getDataInitial(from: Int, to: Int, callback: PositionalDataSource.LoadInitialCallback<GridPhotoRecyclerModel>) {
        scope.launch {
            runCatching {
                val photos = getUserGalleryUseCase.invoke(userID, limit = to, offset = from - separatorCount)
                callback.onResult(prepareData(photos), 0)
            }.onFailure {
                errorCallback.invoke(false)
                Timber.e(it)
            }
        }
    }

    //TODO: https://nomera.atlassian.net/browse/BR-20163
    private fun prepareData(response: UserGalleryModel): MutableList<GridPhotoRecyclerModel> {
        val result: MutableList<GridPhotoRecyclerModel> = mutableListOf()
        val calendarCurrent = Calendar.getInstance()
        val calendar = Calendar.getInstance()

        lastDate?.let {
            calendarCurrent.time = it
        } ?: kotlin.run {
            if (response.items.isNotEmpty()) {
                lastDate = Date(response.items.first().createdAt * 1000)
                calendarCurrent.time = lastDate
            }
        }

        response?.items?.forEach {
            calendar.time = Date(it.createdAt * 1000)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)

            if (year == calendarCurrent.get(Calendar.YEAR)
                && month == calendarCurrent.get(Calendar.MONTH)
            ) {
                val data = GridPhotoRecyclerModel(GridPhotoRecyclerModel.TYPE_PHOTO)
                data.photo = Photo(
                    createdAt = it.createdAt,
                    id = it.id,
                    image = Image(
                        url = it.link,
                        imageData = Data(
                            size = "",
                            ratio = ""
                        )
                    ),
                    isAdult = it.isAdult && isMe.not()
                )
                result.add(data)
            } else {
                lastDate = calendar.time
                calendarCurrent.time = calendar.time
                val data = GridPhotoRecyclerModel(GridPhotoRecyclerModel.TYPE_PHOTO)
                data.photo = Photo(
                    createdAt = it.createdAt,
                    id = it.id,
                    image = Image(
                        url = it.link,
                        imageData = Data(
                            size = "",
                            ratio = ""
                        )
                    ),
                    isAdult = it.isAdult && isMe.not()
                )
                result.add(data)
            }
        }
        return result

    }

    fun refresh() {
        currentYear = 0
        lastDate = null
        separatorCount = 0
    }

    fun clear() {
        disposables.dispose()
    }
}
