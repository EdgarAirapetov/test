package com.numplates.nomera3.modules.post_view_statistic.data

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.meera.core.di.scopes.AppScope
import com.meera.db.DataStore
import com.numplates.nomera3.modules.auth.AuthUser
import com.numplates.nomera3.modules.post_view_statistic.domain.UploadPostViewUseCase
import com.numplates.nomera3.modules.post_view_statistic.presentation.PostCollisionDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AppScope
class PostViewStatisticRepositoryImpl @Inject constructor(
    private val dataStore: DataStore,
    private val uploadPostViewsUseCase: UploadPostViewUseCase
) : PostViewStatisticRepository {

    private var domainAuthUser: AuthUser? = null

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var lastInsertPostViewToRoomJob: Job? = null

    override fun tryUploadPostViews() {
        coroutineScope.launch {
            if (lastInsertPostViewToRoomJob?.isActive == true) {
                lastInsertPostViewToRoomJob?.join()
            }

            try {
                uploadPostViewsUseCase.execute()
            } catch (exception: Throwable) {
                FirebaseCrashlytics.getInstance().recordException(exception)
            }
        }
    }

    override fun detectPostView(postViewModel: PostCollisionDetector.PostViewDetectModel) {
        if (postViewModel.isThisUserPost()) {
            return
        }

        Timber.d("detect_post_view зарегистрирован просмотр поста : $postViewModel")

        val groupId = (postViewModel.roadSource as? PostViewRoadSource.Community)?.groupId ?: 0

        val resultPostViewData = postViewModel.postViewData.copy(
            viewDuration = postViewModel.viewDuration,
            stopViewTime = postViewModel.stopViewTime,
            viewUserId = domainAuthUser?.userId ?: -1,
            roadSource = postViewModel.roadSource.stringRepresentation,
            groupId = groupId
        )

        lastInsertPostViewToRoomJob = coroutineScope.launch {
            dataStore.postViewStatisticDao().insert(resultPostViewData)
        }
    }

    private fun PostCollisionDetector.PostViewDetectModel.isThisUserPost(): Boolean {
        return postViewData.postUserId == domainAuthUser?.userId
    }
}
