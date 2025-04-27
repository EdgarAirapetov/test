package com.numplates.nomera3.modules.moments.show.data

import androidx.work.Data
import androidx.work.ListenableWorker
import com.meera.core.di.scopes.AppScope
import com.meera.core.extensions.fromJson
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.toInt
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UserWrapperWithCounter
import com.numplates.nomera3.data.dbmodel.UsersWrapper
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.comments.data.api.OrderType
import com.numplates.nomera3.modules.comments.data.entity.CommentsEntityResponse
import com.numplates.nomera3.modules.comments.data.entity.SendCommentResponse
import com.numplates.nomera3.modules.feed.data.entity.FeedUpdateEvent
import com.numplates.nomera3.modules.feed.data.repository.PostRepository
import com.numplates.nomera3.modules.fileuploads.data.model.PartialUploadSourceType
import com.numplates.nomera3.modules.fileuploads.domain.usecase.PartialFileUploadUseCase
import com.numplates.nomera3.modules.moments.core.MomentsApi
import com.numplates.nomera3.modules.moments.show.data.entity.MomentInfoModel
import com.numplates.nomera3.modules.moments.show.data.entity.MomentPagingParams
import com.numplates.nomera3.modules.moments.show.data.exception.MOMENT_NOT_FOUND_CODE
import com.numplates.nomera3.modules.moments.show.data.exception.MOMENT_SERVER_ERROR_CODE
import com.numplates.nomera3.modules.moments.show.data.exception.MomentNotFoundException
import com.numplates.nomera3.modules.moments.show.data.exception.MomentServerErrorException
import com.numplates.nomera3.modules.moments.show.data.exception.MomentUnknownException
import com.numplates.nomera3.modules.moments.show.data.mapper.MomentsModelMapper
import com.numplates.nomera3.modules.moments.show.domain.GetMomentDataUseCase
import com.numplates.nomera3.modules.moments.show.domain.MomentItemModel
import com.numplates.nomera3.modules.moments.show.domain.MomentLinkModel
import com.numplates.nomera3.modules.moments.show.domain.MomentsAction
import com.numplates.nomera3.modules.moments.show.domain.UserMomentsStateUpdateModel
import com.numplates.nomera3.modules.moments.show.domain.model.MomentRepositoryEvent
import com.numplates.nomera3.modules.moments.user.data.mapper.UserMomentsPreviewMapper
import com.numplates.nomera3.modules.moments.user.domain.model.UserMomentsModel
import com.numplates.nomera3.modules.moments.user.domain.model.UserMomentsPreviewModel
import com.numplates.nomera3.modules.newroads.data.entities.MediaEntity
import com.numplates.nomera3.modules.newroads.data.entities.MediaEntityData
import com.numplates.nomera3.modules.newroads.data.entities.MediaKeyboardEntity
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.search.data.states.UserState
import com.numplates.nomera3.modules.user.data.repository.UserRepository
import com.numplates.nomera3.modules.user.domain.effect.UserSettingsEffect
import com.numplates.nomera3.modules.user.domain.usecase.GetUserSettingsStateChangedUseCase
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.File
import javax.inject.Inject

const val ARG_MOMENT_ID = "ARG_MOMENT_ID"
const val ARG_MOMENT_DURATION = "ARG_MOMENT_DURATION"

//private const val FILE_NAME = "file"
private const val VIDEO_MP4_NAME = "video/mp4"
private const val IMAGE_NAME = "image/*"
private const val TEXT_PLANE_NAME = "text/plain"

private const val EVENT_STREAM_BUFFER = 100
private const val MAX_MOMENTS_COUNT_FOR_LAST_STATE = 3

@AppScope
class MomentsRepositoryImpl @Inject constructor(
    private val api: MomentsApi,
    private val fileUploadUseCase: PartialFileUploadUseCase,
    private val mapper: MomentsModelMapper,
    private val getUserSettingsStateChangedUseCase: GetUserSettingsStateChangedUseCase,
    private val getUserUidUseCase: GetUserUidUseCase,
    usersRepository: UserRepository,
    postsRepository: PostRepository,
) : MomentsRepository {

    private var cachedMomentData = hashMapOf<GetMomentDataUseCase.MomentsSource, GetMomentGroupsResponseDto>()

    private var lastLoadedMomentUserId =
        hashMapOf<GetMomentDataUseCase.MomentsSource, Int>()

    private var lastMomentGroupsPagingParams = hashMapOf<GetMomentDataUseCase.MomentsSource, MomentPagingParams>()

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val eventStream = MutableSharedFlow<MomentRepositoryEvent>(0, EVENT_STREAM_BUFFER)

    private val isAllUserMomentsViewedValues = HashMap<Long, Boolean>()

    init {
        observeUserBlockStatus(usersRepository.getUserStateObserver())
        observeUserFriendStatusFlow()
        observeReactiveFeedSubscriptionRx(postsRepository.getFeedStateObserver())
    }

    override fun getLastLoadedMomentUserId(momentsSource: GetMomentDataUseCase.MomentsSource): Int {
        return lastLoadedMomentUserId.getOrDefault(momentsSource, 0)
    }

    override fun getEventStream(): Flow<MomentRepositoryEvent> {
        return eventStream
    }

    override suspend fun sendComment(
        momentItemId: Long, text: String, commentId: Long
    ): ResponseWrapper<SendCommentResponse> {
        return api.sendComment(
            params = hashMapOf(
                "moment_id" to momentItemId, "text" to text, "comment_id" to commentId
            )
        )
    }

    override suspend fun getComments(
        momentItemId: Long, limit: Long, startId: Long?, parentId: Long?, commentId: Long?, order: OrderType?
    ): ResponseWrapper<CommentsEntityResponse?> {
        val orderValue = order?.value ?: 0
        return api.getComments(
            momentItemId = momentItemId,
            limit = limit,
            startId = startId,
            parentId = parentId,
            commentId = commentId,
            order = orderValue
        )
    }

    override suspend fun shareMoment(
        momentId: Long, userIds: List<Long>, roomIds: List<Long>, comment: String
    ): MomentItemModel {
        val momentResponse = api.shareMoment(
            momentId = momentId, comment = comment, userIds = userIds, roomIds = roomIds
        )

        checkMomentOnErrorThrow(momentResponse)

        val momentItemDto = momentResponse.data
        val updatedItem = updateMomentItemByMomentId(momentId) { cachedMoment ->
            cachedMoment.copy(repostsCount = momentItemDto.repostsCount)
        }
        return mapper.mapFromDtoToModel(updatedItem ?: momentItemDto)
    }

    override suspend fun addMomentHideFromExclusion(userIds: List<Long>): ResponseWrapper<Any> {
        return api.addMomentHideFromExclusion(userIds)
    }

    override suspend fun deleteMomentHideFromExclusion(params: HashMap<String, List<Long>>): ResponseWrapper<Any> {
        return api.deleteMomentHideFromExclusion(params)
    }

    override suspend fun searchHideFromExclusion(
        name: String, limit: Int, offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>> {
        return api.searchMomentHideFromExclusion(
            name = name, limit = limit, offset = offset
        )
    }

    override suspend fun getMomentLink(
        momentId: Long
    ): MomentLinkModel {
        val linkDto = api.getMomentLink(momentId).data
        return mapper.mapFromDtoToModel(linkDto)
    }

    override suspend fun updateUserMomentsState(
        action: MomentsAction, userMomentsStateUpdate: UserMomentsStateUpdateModel
    ) {
        eventStream.emit(
            MomentRepositoryEvent.UserMomentsStateUpdated(
                action = action, userMomentsStateUpdate = userMomentsStateUpdate
            )
        )
    }

    override suspend fun updateProfileUserMomentsState(userId: Long) {
        eventStream.emit(
            MomentRepositoryEvent.ProfileUserMomentsStateUpdated(
                userId = userId, userMomentsModel = getLastUserMomentsState(userId)
            )
        )
    }

    override fun getPagingParams(momentsSource: GetMomentDataUseCase.MomentsSource): MomentPagingParams {
        var pagingParams = lastMomentGroupsPagingParams[momentsSource]
        if (pagingParams == null) {
            pagingParams = MomentPagingParams()
            lastMomentGroupsPagingParams[momentsSource] = pagingParams
        }
        return pagingParams
    }

    override fun setPagingParams(
        momentsSource: GetMomentDataUseCase.MomentsSource,
        momentPagingParams: MomentPagingParams
    ) {
        lastMomentGroupsPagingParams[momentsSource] = momentPagingParams
    }

    override suspend fun getMomentsHideFromExclusions(
        limit: Int, offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>> {
        return api.getMomentsHideFromExclusionsWithCounter(
            limit = limit, offset = offset
        )
    }

    override suspend fun addMomentNotShowExclusion(userIds: List<Long>): ResponseWrapper<Any> {
        val notShowResponse = api.addMomentNotShowExclusion(userIds)
        filterOutMomentsUserIds(userIds)
        return notShowResponse
    }

    override suspend fun deleteMomentNotShowExclusion(params: HashMap<String, List<Long>>): ResponseWrapper<Any> {
        return api.deleteMomentNotShowExclusion(params)
    }

    override suspend fun searchNotShowExclusion(
        name: String, limit: Int, offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>> {
        return api.searchMomentNotShowExclusion(
            name = name, limit = limit, offset = offset
        )
    }

    override suspend fun getMomentsNotShowExclusions(
        limit: Int, offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>> {
        return api.getMomentsNotShowExclusionsWithCounter(
            limit = limit, offset = offset
        )
    }

    override suspend fun addMoment(
        filePath: String,
        isVideo: Boolean,
        gpsX: Double,
        gpsY: Double,
        place: String?,
        media: String,
        mediaKeyboard: Array<String>
    ): ListenableWorker.Result {
        val response = upload(
            filePath = filePath,
            isVideo = isVideo,
            gpsX = gpsX,
            gpsY = gpsY,
            place = place,
            media = media,
            mediaKeyboard = mediaKeyboard
        )

        val responseData = response.data

        if (responseData != null) {
            val moments = responseData.momentIds

            val momentId = moments.firstOrNull()?: return ListenableWorker.Result.failure()

            val duration =
                responseData.data[momentId.toString()]?.duration ?: return ListenableWorker.Result.failure()

            val data = Data.Builder().apply {
                putLong(ARG_MOMENT_ID, momentId)
                putInt(ARG_MOMENT_DURATION, duration)
            }.build()

            return ListenableWorker.Result.success(data)
        } else {
            return ListenableWorker.Result.failure()
        }
    }

    override suspend fun deleteMoment(momentId: Long, userId: Long) {
        val changedCacheKeyAndItems = cachedMomentData.map { (key, value) ->
            val modifiedGroups = value.momentGroups.mapNotNull { group ->
                val modifiedMoments = group.moments.filter { it.id != momentId }
                if (modifiedMoments.isNotEmpty()) group.copy(moments = modifiedMoments) else null
            }
            val modifiedGroupsDto = value.copy(momentGroups = modifiedGroups)
            key to modifiedGroupsDto
        }
        changedCacheKeyAndItems.forEach { (source, changedCacheItem) ->
            cachedMomentData[source] = changedCacheItem
        }
        checkAndUpdateUserMomentsState(action = MomentsAction.DELETED, userId = userId)
        api.deleteMoment(momentId)
    }

    override fun updateCommentCounter(momentId: Long) {
        coroutineScope.launch {
            val fetchedItem = api.getMomentById(momentId).data
            val updatedItem = updateMomentItemByMomentId(momentId) { cachedMoment ->
                cachedMoment.copy(commentsCount = fetchedItem.commentsCount, user = fetchedItem.user)
            }
            updatedItem?.let { emitMomentActionBarStateUpdate(it) }
        }
    }

    override fun newMomentCreated() {
        coroutineScope.launch {
            val userMomentsStateUpdate = UserMomentsStateUpdateModel(
                userId = getUserUidUseCase.invoke(), hasMoments = true, hasNewMoments = true
            )
            val event = MomentRepositoryEvent.UserMomentsStateUpdated(
                action = MomentsAction.CREATED, userMomentsStateUpdate = userMomentsStateUpdate
            )
            eventStream.emit(event)
        }
    }

    override suspend fun setCommentAvailability(momentId: Long, commentAvailability: Int) {
        api.setCommentAvailability(
            momentId = momentId, commentAvailability = commentAvailability
        )
        val fetchedMoment = api.getMomentById(momentId = momentId)
        val updatedItem = updateMomentItemByMomentId(momentId) { cachedMoment ->
            cachedMoment.copy(
                commentAvailability = fetchedMoment.data.commentAvailability,
                iCanComment = fetchedMoment.data.iCanComment,
                user = fetchedMoment.data.user
            )
        }
        updatedItem?.let { emitMomentActionBarStateUpdate(it) }
    }

    override fun updateMomentReactions(
        momentId: Long,
        reactionList: List<ReactionEntity>,
    ) {
        updateMomentItemByMomentId(momentId) { cachedMoment ->
            cachedMoment.copy(reactions = reactionList)
        }
    }

    override fun updateUserSubscriptions(userIds: List<Long>, isAdded: Boolean) {
        cachedMomentData.values.flatMap { groupsDto ->
            groupsDto.momentGroups.flatMap { it.moments }
        }.forEach { itemDto ->
            if (userIds.contains(itemDto.userId)) {
                itemDto.user?.settingsFlags?.subscription_on = isAdded.toInt()
            }
        }
        coroutineScope.launch {
            userIds.forEach { userId ->
                eventStream.emit(
                    MomentRepositoryEvent.MomentUserSubscriptionUpdated(userId = userId, isSubscribed = isAdded)
                )
            }
        }
    }

    override fun updateUserBlockStatus(remoteUserId: Long, isBlockedByMe: Boolean) {
        updateMomentUserByPredicate(predicate = {
            it.userId == remoteUserId && it.user?.blacklistedByMe.toBoolean() != isBlockedByMe
        }) { user ->
            user?.copy(blacklistedByMe = isBlockedByMe.toInt())
        }
        coroutineScope.launch {
            eventStream.emit(
                MomentRepositoryEvent.MomentUserBlockStatusUpdated(
                    userId = remoteUserId, isBlockedByMe = isBlockedByMe
                )
            )
        }
    }

    override fun getMomentsFromCache(momentsSource: GetMomentDataUseCase.MomentsSource): MomentInfoModel {
        val cachedResult = cachedMomentData[momentsSource] ?: getEmptyResponse()
        return mapper.mapFromDtoToModel(cachedResult)
    }

    override suspend fun getMomentsFromRest(
        userId: Long,
        targetMomentId: Long?,
        momentsSource: GetMomentDataUseCase.MomentsSource,
    ): MomentInfoModel {
        val response = api.getMomentGroups(
            userId = userId.toInt(),
            startId = 0,
            limit = 0,
            targetMomentId = targetMomentId,
            type = momentsSource.value
        )
        cachedMomentData[momentsSource] = response.data
        lastLoadedMomentUserId[momentsSource] = response.data.momentGroups.lastOrNull()?.userId?.toInt() ?: 0
        lastMomentGroupsPagingParams[momentsSource] = MomentPagingParams(
            startId = 0,
            limit = 0
        )
        return mapper.mapFromDtoToModel(dto = cachedMomentData[momentsSource] ?: response.data)
    }

    override suspend fun getMomentsPaginated(
        userId: Long, startId: Int, limit: Int, momentsSource: GetMomentDataUseCase.MomentsSource, sessionId: String?,
    ): MomentInfoModel {
        val response = api.getMomentGroups(
            userId = userId.toInt(),
            startId = startId,
            limit = limit,
            type = momentsSource.value,
            sessionId = sessionId
        )
        val paginatedData = response.data
        val cachedData = cachedMomentData[momentsSource]
        val resultData = if (sessionId != null) GetMomentGroupsResponseDto(
            momentGroups = (cachedData?.momentGroups ?: emptyList()) + paginatedData.momentGroups,
            session = paginatedData.session
        ) else {
            paginatedData
        }

        lastLoadedMomentUserId[momentsSource] = resultData.momentGroups.lastOrNull()?.userId?.toInt() ?: 0
        lastMomentGroupsPagingParams[momentsSource] = MomentPagingParams(
            sessionId = sessionId,
            startId = startId,
            limit = limit
        )
        val filteredResultData =
            resultData.copy(momentGroups = filterByUniqueMomentGroups(resultData.momentGroups))

        cachedMomentData[momentsSource] = filteredResultData
        return mapper.mapFromDtoToModel(dto = cachedMomentData[momentsSource] ?: filteredResultData)
    }

    override suspend fun getMomentById(momentId: Long): MomentItemModel {
        val response = api.getMomentById(momentId)
        when (response.err?.code) {
            MOMENT_NOT_FOUND_CODE -> throw MomentNotFoundException(response.err.userMessage)
            MOMENT_SERVER_ERROR_CODE -> throw MomentServerErrorException(response.err.userMessage)
            null -> return mapper.mapFromDtoToModel(response.data)
            else -> throw MomentUnknownException(response.err.userMessage)
        }
    }

    override suspend fun getUpdatedMomentViewCount(momentId: Long): Long {
        val fetchedData = api.getMomentById(momentId).data
        val viewsCount = fetchedData.viewsCount
        updateMomentItemByMomentId(momentId) { cachedMoment ->
            cachedMoment.copy(viewsCount = viewsCount, user = fetchedData.user)
        }
        return viewsCount
    }

    override suspend fun setMomentViewed(momentId: Long, userId: Long) {
        updateMomentItemByMomentId(momentId) { cachedMoment ->
            cachedMoment.copy(viewed = true.toInt())
        }
        checkAndUpdateUserMomentsState(action = MomentsAction.VIEWED, userId = userId)
        api.setMomentViewed(momentId)
    }

    override suspend fun momentComplain(remoteUserId: Long, reasonId: Int, momentId: Long) {
        api.addComplainV2(
            hashMapOf(
                "user_id" to remoteUserId, "reason_id" to reasonId, "moment_id" to momentId
            )
        )
    }

    private fun filterByUniqueMomentGroups(momentsGroups: List<MomentGroupDto>): List<MomentGroupDto> {
        return momentsGroups.distinctBy { it.userId }
    }

    private suspend fun checkAndUpdateUserMomentsState(
        action: MomentsAction, userId: Long
    ) {
        val userMomentsGroup = getUserMomentsGroup(userId)
        val isAllMomentsViewed = isAllUserMomentsViewed(userMomentsGroup)
        val userMomentsStateUpdate = UserMomentsStateUpdateModel(
            userId = userId, hasMoments = userMomentsGroup != null, hasNewMoments = !isAllMomentsViewed
        )
        val event = MomentRepositoryEvent.UserMomentsStateUpdated(
            action = action, userMomentsStateUpdate = userMomentsStateUpdate
        )
        eventStream.emit(event)
        isAllUserMomentsViewedValues[userId] = isAllMomentsViewed
    }

    private fun getLastUserMomentsState(userId: Long): UserMomentsModel {
        val momentGroup =
            cachedMomentData.firstNotNullOfOrNull { item -> item.value.momentGroups.firstOrNull { it.userId == userId } }
                ?: return UserMomentsModel(
                    hasMoments = false, hasNewMoments = false, countNew = 0, countTotal = 0, previews = emptyList()
                )

        val moments = momentGroup.moments
        val newMoments = moments.filter { !it.viewed.toBoolean() }
        val viewedMoments = moments.filter { it.viewed.toBoolean() }
        val previews = arrayListOf<UserMomentsPreviewModel>()
        if (moments.isNotEmpty()) {
            val previewsDto = arrayListOf<MomentItemDto>()
            previewsDto.addAll(newMoments.take(MAX_MOMENTS_COUNT_FOR_LAST_STATE))
            previewsDto.addAll(viewedMoments.take(MAX_MOMENTS_COUNT_FOR_LAST_STATE - previewsDto.size))

            previews.addAll(previewsDto.map { UserMomentsPreviewMapper.mapUserMomentsPreviewModel(it) })
        }

        return UserMomentsModel(
            hasMoments = moments.isNotEmpty(),
            hasNewMoments = newMoments.isNotEmpty(),
            countNew = newMoments.size,
            countTotal = moments.size,
            previews = previews
        )
    }

    private fun checkMomentOnErrorThrow(resultData: ResponseWrapper<MomentItemDto>) {
        if (resultData.err == null) {
            return
        }

        val userMessage = resultData.err?.userMessage

        throw if (!userMessage.isNullOrEmpty()) MomentUserMessageException(userMessage) else MomentException
    }

    private suspend fun emitMomentActionBarStateUpdate(updateItemDto: MomentItemDto) {
        eventStream.emit(
            MomentRepositoryEvent.MomentActionBarStateUpdated(
                mapper.mapFromDtoToModel(updateItemDto)
            )
        )
    }

    private fun getEmptyResponse(): GetMomentGroupsResponseDto {
        return GetMomentGroupsResponseDto(
            momentGroups = emptyList(), session = null
        )
    }

    private suspend fun upload(
        filePath: String,
        isVideo: Boolean,
        gpsX: Double,
        gpsY: Double,
        place: String? = null,
        media: String,
        mediaKeyboard: Array<String>
    ): ResponseWrapper<AddMomentResponse?> {
        val mediaType = if (isVideo) {
            VIDEO_MP4_NAME.toMediaTypeOrNull()
        } else {
            IMAGE_NAME.toMediaTypeOrNull()
        }
        val uploadId = fileUploadUseCase.invoke(
            fileToUpload = File(filePath),
            mediaType = mediaType,
            sourceType = PartialUploadSourceType.MOMENT
        )
        val uploadBody = uploadId.toRequestBody(TEXT_PLANE_NAME.toMediaTypeOrNull())

        val mediaEntity: MediaEntity? = if (media.isNotEmpty()) {
            media.fromJson(MediaEntity::class.java)
        } else {
            null
        }
        val mediaKeyboardEntity: List<MediaKeyboardEntity>? = if (mediaKeyboard.isNotEmpty()) {
            mediaKeyboard.map { it.fromJson(MediaKeyboardEntity::class.java) }
        } else {
            null
        }
        val mediaEntityData = MediaEntityData(mediaEntity, mediaKeyboardEntity)
        val mediaData = mapOf(uploadId to mediaEntityData)

        return api.addMoment(uploadBody, gpsX, gpsY, place, mediaData)
    }

    private fun observeUserFriendStatusFlow() {
        getUserSettingsStateChangedUseCase.invoke().onEach { event ->
            when (event) {
                is UserSettingsEffect.UserFriendStatusChanged -> {
                    handleUserFriendStatusUpdate(event)
                }

                else -> Unit
            }
        }.catch {
            Timber.d("MomentSub | exception thrown in collect=$it")
        }.launchIn(coroutineScope)
    }

    private fun observeUserBlockStatus(userStateObserver: PublishSubject<UserState>) {
        userStateObserver.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
            .filter { it is UserState.BlockStatusUserChanged }
            .subscribe(::handleReactiveBlockUserUpdate) { Timber.e(it) }
    }

    private fun observeReactiveFeedSubscriptionRx(observable: Observable<FeedUpdateEvent>) {
        observable.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
            .filter { it is FeedUpdateEvent.FeedUserSubscriptionChanged }
            .subscribe(::handleReactiveFeedUserSubscriptionUpdate) { Timber.e(it) }
    }

    private suspend fun handleUserFriendStatusUpdate(event: UserSettingsEffect.UserFriendStatusChanged) {
        cachedMomentData.values.forEach { value ->
            val matchingMoment = value.momentGroups.flatMap { it.moments }.find { it.user?.userId == event.userId }

            if (matchingMoment != null) {
                val updatedUser = api.getMomentById(matchingMoment.id).data.user
                updateMomentUserByPredicate(predicate = { it.userId == updatedUser?.userId }, updater = { updatedUser })
                return
            }
        }
    }

    private fun handleReactiveFeedUserSubscriptionUpdate(event: FeedUpdateEvent) {
        if (event !is FeedUpdateEvent.FeedUserSubscriptionChanged) return
        updateUserSubscriptions(listOf(event.userId), event.isSubscribed)
    }

    private fun handleReactiveBlockUserUpdate(newState: UserState) {
        if (newState !is UserState.BlockStatusUserChanged) return
        updateUserBlockStatus(remoteUserId = newState.userId, isBlockedByMe = newState.isBlocked)
    }

    private fun filterOutMomentsUserIds(userIds: List<Long>) {
        val changedCacheKeyAndValues = cachedMomentData.map { (key, value) ->
            val modifiedGroups = value.momentGroups.mapNotNull { group ->
                val filteredMoments = group.moments.filter { item ->
                    userIds.contains(item.userId).not()
                }
                if (filteredMoments.isEmpty()) null else group.copy(moments = filteredMoments)
            }
            key to value.copy(momentGroups = modifiedGroups)
        }
        changedCacheKeyAndValues.forEach { (key, value) ->
            cachedMomentData[key] = value
        }
    }

    private fun updateMomentItemByMomentId(momentId: Long, updater: (MomentItemDto) -> MomentItemDto): MomentItemDto? {
        return modifyCacheItem(predicate = { it.id == momentId }, updater = updater)
    }

    private fun updateMomentUserByPredicate(
        predicate: (MomentItemDto) -> Boolean, updater: (UserSimple?) -> UserSimple?
    ): MomentItemDto? {
        return modifyCacheItem(predicate = predicate) { cachedMoment ->
            cachedMoment.copy(user = updater(cachedMoment.user))
        }
    }

    private fun modifyCacheItem(
        predicate: (MomentItemDto) -> Boolean, updater: (MomentItemDto) -> MomentItemDto
    ): MomentItemDto? {
        var lastUpdatedItemDto: MomentItemDto? = null
        val changedCacheKeyAndItems = cachedMomentData.map { (key, value) ->
            val modifiedGroups = value.momentGroups.map { group ->
                group.copy(moments = group.moments.map { moment ->
                    if (predicate(moment)) {
                        updater.invoke(moment).also { lastUpdatedItemDto = it }
                    } else {
                        moment
                    }
                })
            }
            val modifiedGroupsDto = value.copy(momentGroups = modifiedGroups)
            key to modifiedGroupsDto
        }
        changedCacheKeyAndItems.forEach { (source, changedCacheItem) ->
            cachedMomentData[source] = changedCacheItem
        }
        return lastUpdatedItemDto
    }

    private fun getUserMomentsGroup(userId: Long): MomentGroupDto? {
        return cachedMomentData.firstNotNullOfOrNull { item ->
            item.value.momentGroups.firstOrNull { it.userId == userId }
        }
    }

    private fun isAllUserMomentsViewed(momentGroup: MomentGroupDto?): Boolean {
        if (momentGroup == null) return true
        return momentGroup.moments.all { it.viewed.toBoolean() }
    }

}

class MomentGroupDtoComparator : Comparator<MomentGroupDto> {
    override fun compare(firstGroup: MomentGroupDto, secondGroup: MomentGroupDto): Int {
        if (firstGroup == secondGroup) return 0
        val firstIsViewed = firstGroup.moments.all { it.viewed.toBoolean() }
        val secondIsViewed = secondGroup.moments.all { it.viewed.toBoolean() }
        return when {
            firstIsViewed != secondIsViewed -> if (firstIsViewed) 1 else -1
            else -> {
                val firstGroupLatestCreatedAt = firstGroup.moments.maxOfOrNull { it.createdAt } ?: Long.MIN_VALUE
                val secondGroupLatestCreatedAt = secondGroup.moments.maxOfOrNull { it.createdAt } ?: Long.MIN_VALUE
                val compareResult = if (firstGroupLatestCreatedAt < secondGroupLatestCreatedAt) 1 else -1
                val reverseIfNeeded = if (firstIsViewed) -1 else 1
                compareResult * reverseIfNeeded
            }
        }
    }
}

data class MomentUserMessageException(val userMessage: String) : Exception()
object MomentException : Exception()
