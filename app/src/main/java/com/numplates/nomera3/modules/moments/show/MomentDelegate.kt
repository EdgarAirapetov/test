package com.numplates.nomera3.modules.moments.show

import com.numplates.nomera3.domain.interactornew.GetUserSmallAvatarUseCase
import com.numplates.nomera3.modules.feed.data.entity.FeedUpdateEvent
import com.numplates.nomera3.modules.feed.domain.usecase.ForceUpdatePostUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.UpdatePostParams
import com.numplates.nomera3.modules.feed.ui.adapter.FeedAdapter
import com.numplates.nomera3.modules.feed.ui.data.MOMENTS_POST_ID
import com.numplates.nomera3.modules.feed.ui.viewmodel.RoadTypesEnum
import com.numplates.nomera3.modules.moments.settings.notshow.domain.MomentSettingsNotShowAddExclusionUseCase
import com.numplates.nomera3.modules.moments.show.data.CarouselMomentsHelper
import com.numplates.nomera3.modules.moments.show.data.entity.MomentInfoModel
import com.numplates.nomera3.modules.moments.show.data.entity.MomentPagingParams
import com.numplates.nomera3.modules.moments.show.data.entity.MomentPagingParams.Companion.FORBID_PAGING_TICKET
import com.numplates.nomera3.modules.moments.show.domain.DEFAULT_MOMENTS_PAGE_LIMIT
import com.numplates.nomera3.modules.moments.show.domain.GetLastLoadedMomentUserIdUseCase
import com.numplates.nomera3.modules.moments.show.domain.GetMomentDataPaginatedUseCase
import com.numplates.nomera3.modules.moments.show.domain.GetMomentDataUseCase
import com.numplates.nomera3.modules.moments.show.domain.GetMomentsPagingParamsBySourceUseCase
import com.numplates.nomera3.modules.moments.show.domain.PreloadPaginatedMomentsUseCase
import com.numplates.nomera3.modules.moments.show.domain.SetMomentsPagingParamsBySourceUseCase
import com.numplates.nomera3.modules.moments.show.presentation.adapter.MomentItemAdapter
import com.numplates.nomera3.modules.moments.util.getMomentPaginationLimitsForRoadType
import com.numplates.nomera3.modules.moments.util.getMomentSourceForRoadType
import com.numplates.nomera3.modules.moments.util.isAddCreateMomentForRoadType
import com.numplates.nomera3.modules.moments.util.isMomentPagingOnLastPage
import com.numplates.nomera3.modules.moments.util.isMomentsPagingUsedForRoadType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

class MomentDelegateImpl @Inject constructor(
    private val forceUpdatePostUseCase: ForceUpdatePostUseCase,
    private val getMomentsUseCase: GetMomentDataUseCase,
    private val getMomentsPaginatedUseCase: GetMomentDataPaginatedUseCase,
    private val hideUserMomentsUseCase: MomentSettingsNotShowAddExclusionUseCase,
    private val preloadPaginatedMomentsUseCase: PreloadPaginatedMomentsUseCase,
    private val carouselMomentsHelper: CarouselMomentsHelper,
    private val getUserSmallAvatarUseCase: GetUserSmallAvatarUseCase,
    private val getLastLoadedMomentUseCase: GetLastLoadedMomentUserIdUseCase,
    private val getPagingParamsBySourceUseCase: GetMomentsPagingParamsBySourceUseCase,
    private val setPagingParamsBySourceUseCase: SetMomentsPagingParamsBySourceUseCase
) : MomentDelegate {

    private var roadType: RoadTypesEnum? = null
    private var momentSource: GetMomentDataUseCase.MomentsSource? = null
    private var coroutineScope: CoroutineScope? = null
    private var isRequestingMomentsPage: Boolean = false
    private var isInitialUploadActive: Boolean = false

    override fun initCoroutineScope(scope: CoroutineScope) {
        coroutineScope = scope
    }

    override fun initRoadType(roadTypeEnum: RoadTypesEnum?) {
        roadType = roadTypeEnum
        momentSource = getMomentSourceForRoadType(roadType) ?: GetMomentDataUseCase.MomentsSource.Main
    }

    override fun getMomentPagingParams(): MomentPagingParams {
        return getPagingParamsBySourceUseCase.invoke(getMomentSource())
    }

    override fun isMomentsLastPage(): Boolean {
        return getPagingParamsBySourceUseCase.invoke(getMomentSource()).isLastPage
    }

    override fun isUpdatingMoments(): Boolean {
        return isRequestingMomentsPage
    }

    override fun isMomentPagingListTicketValid(ticket: String?): Boolean {
        return ticket == null || (ticket != FORBID_PAGING_TICKET && ticket != getMomentPagingParams().lastConsumedPagingTicket)
    }

    override fun updateAllMomentsFromCache() {
        Timber.tag("MomentDebug").d("getting moments from cache, source = $roadType")
        val momentsSource = getMomentSourceForRoadType(roadType) ?: return
        coroutineScope?.launch(Dispatchers.IO) {
            runCatching {
                getMomentsPaginatedUseCase.invoke(
                    momentsSource = momentsSource,
                    isFromCache = true
                )
            }.onFailure {
                Timber.e(it, "getAllMomentsFromCache() in MomentDelegate encountered an error")
            }.onSuccess { moments ->
                updateMoments(moments, getMomentPagingParams(), asPayload = true)
            }
        }
    }

    override fun updateMomentsAndViewedPosition(lastWatchedGroupId: Long) {
        Timber.tag("MomentDebug").d("updateMomentsAndViewedPosition, source = $roadType")
        val momentsSource = getMomentSourceForRoadType(roadType) ?: return
        coroutineScope?.launch(Dispatchers.IO) {
            runCatching {
                getMomentsPaginatedUseCase.invoke(
                    momentsSource = momentsSource,
                    isFromCache = true
                )
            }.onFailure {
                Timber.e(
                    it,
                    "updateMomentsAndViewedPosition() in MomentDelegate encountered an error"
                )
            }.onSuccess { moments ->
                updateMoments(
                    moments,
                    getMomentPagingParams(),
                    asPayload = true,
                    scrollToGroupId = lastWatchedGroupId
                )
            }
        }
    }

    override fun initialLoadMoments(scrollToStart: Boolean) {
        Timber.tag("MomentDebug").d("initialLoadMoments, source = $roadType")
        val roadTypeEnum = roadType ?: return
        val momentSource = getMomentSourceForRoadType(roadType) ?: return
        if (isInitialUploadActive) return

        isInitialUploadActive = true

        coroutineScope?.launch(Dispatchers.IO) {
            val momentInfoModel = if (isMomentsPagingUsedForRoadType(roadTypeEnum)) {
                val momentLimitForRoadType = getMomentPaginationLimitsForRoadType(roadTypeEnum)
                if (momentLimitForRoadType == null) {
                    isInitialUploadActive = false
                    return@launch
                }
                val pagingParams = MomentPagingParams(newLoad = true, limit = momentLimitForRoadType)
                getMomentsPageInternal(
                    momentSource = momentSource,
                    params = pagingParams
                )
            } else {
                getAllMomentsInternal()
            }
            if (momentInfoModel != null) {
                preloadMomentsMediaContent(momentInfoModel)
                updateMoments(
                    momentInfoModel = momentInfoModel,
                    pagingParams = getMomentPagingParams(),
                    scrollToStart = scrollToStart
                )
            }
            isInitialUploadActive = false
        }
    }

    override fun requestAllMoments() {
        Timber.tag("MomentDebug").d("requestAllMoments, source = $roadType")
        coroutineScope?.launch(Dispatchers.IO) {
            val momentInfoModel = getAllMomentsInternal()
            if (momentInfoModel != null) {
                preloadMomentsMediaContent(momentInfoModel)
                updateMoments(momentInfoModel, getMomentPagingParams())
            }
        }
    }

    override fun requestMomentsPage(
        forceNewLoad: Boolean,
        limit: Int,
        pagingTicket: String?
    ) {
        Timber.tag("MomentDebug").d("requestMomentsPage, source = $roadType")

        if (forceNewLoad) {
            setPagingParamsBySourceUseCase.invoke(
                momentsSource = getMomentSource(),
                momentPagingParams = MomentPagingParams()
            )
        }
        if (!isMomentPagingListTicketValid(pagingTicket)) return
        val momentSource = getMomentSourceForRoadType(roadType) ?: return

        if (isRequestingMomentsPage) return

        isRequestingMomentsPage = true

        coroutineScope?.launch(Dispatchers.IO) {
            val updatedPagingParams = getMomentPagingParams().copy(
                lastConsumedPagingTicket = pagingTicket,
                limit = limit,
                newLoad = forceNewLoad
            )

            val momentInfoModel = getMomentsPageInternal(
                momentSource,
                updatedPagingParams
            )
            if (momentInfoModel != null) {
                preloadMomentsMediaContent(momentInfoModel)
                updateMoments(momentInfoModel, getMomentPagingParams())
            }
            isRequestingMomentsPage = false
        }
    }

    override fun hideUserMoments(userId: Long, onSuccess: () -> Unit) {
        coroutineScope?.launch {
            runCatching {
                hideUserMomentsUseCase.invoke(listOf(userId))
                updateAllMomentsFromCache()
            }.onSuccess {
                onSuccess.invoke()
            }
        }
    }

    private fun getMomentSource(): GetMomentDataUseCase.MomentsSource {
        return momentSource ?: GetMomentDataUseCase.MomentsSource.User
    }

    private suspend fun getAllMomentsInternal(): MomentInfoModel? {
        val momentSource = getMomentSourceForRoadType(roadType) ?: return null
        return runCatching {
            getMomentsUseCase.invoke(
                getFromCache = false,
                momentsSource = momentSource
            )
        }.onFailure {
            Timber.e(it, "getAllMoments() in MomentDelegateImpl encountered an error")
        }.onSuccess {
            setPagingParamsBySourceUseCase.invoke(
                momentsSource = momentSource,
                momentPagingParams = MomentPagingParams(
                    newLoad = true,
                    isLastPage = true,
                    limit = 0,
                    lastProducedPagingTicket = FORBID_PAGING_TICKET
                )
            )
        }.getOrNull()
    }

    private suspend fun getMomentsPageInternal(
        momentSource: GetMomentDataUseCase.MomentsSource,
        params: MomentPagingParams
    ): MomentInfoModel? {

        val oldPagingParams = getMomentPagingParams()

        return runCatching {
            getMomentsPaginatedUseCase.invoke(
                momentsSource = momentSource,
                isFromCache = false,
                sessionId = params.sessionId,
                startId = params.startId,
                limit = params.limit
            )
        }.onSuccess { moments ->
            Timber.tag("MomentDebug")
                .d("received momentsPageInternal, size = ${moments.momentGroups.size}")
            setPagingParamsBySourceUseCase.invoke(
                momentsSource = momentSource,
                momentPagingParams = getNewPagingParamsFromNewPage(moments, params, momentSource))
        }.onFailure {
            Timber.e(
                it,
                "getPaginatedMoments() in MomentDelegateImpl encountered an error, resetting pagingParams to prev version"
            )
            setPagingParamsBySourceUseCase.invoke(momentsSource = momentSource, momentPagingParams = oldPagingParams)
        }.getOrNull()
    }

    private suspend fun getNewPagingParamsFromNewPage(
        newPageModel: MomentInfoModel,
        prevParams: MomentPagingParams,
        momentsSource: GetMomentDataUseCase.MomentsSource
    ): MomentPagingParams {
        return if (newPageModel.lastPageSize < prevParams.limit || prevParams.limit == 0) {
            prevParams.copy(
                sessionId = newPageModel.session,
                lastProducedPagingTicket = prevParams.lastProducedPagingTicket,
                isLastPage = true,
            )
        } else {
            val lastMomentUserId = getLastLoadedMomentUseCase.invoke(
                momentsSource = momentsSource
            )

            prevParams.copy(
                sessionId = newPageModel.session,
                lastProducedPagingTicket = UUID.randomUUID().toString(),
                startId = lastMomentUserId
            )
        }
    }

    private fun preloadMomentsMediaContent(momentInfoModel: MomentInfoModel) {
        coroutineScope?.launch(Dispatchers.IO) {
            preloadPaginatedMomentsUseCase.invoke(momentInfoModel)
        }
    }

    private fun updateMoments(
        momentInfoModel: MomentInfoModel,
        pagingParams: MomentPagingParams,
        asPayload: Boolean = false,
        scrollToGroupId: Long? = null,
        scrollToStart: Boolean = false
    ) {
        val roadType = roadType ?: return
        when (roadType) {
            RoadTypesEnum.SUBSCRIPTION,
            RoadTypesEnum.MAIN -> {
                updateViewHolder(
                    roadType = roadType,
                    momentInfoModel = momentInfoModel,
                    pagingParams = pagingParams,
                    asPayload = asPayload,
                    scrollToGroupId = scrollToGroupId,
                    scrollToStart = scrollToStart
                )
            }
            else -> {
                // update something else(e.g. livedata for a carousel view on the map)
            }
        }
    }

    private fun updateViewHolder(
        roadType: RoadTypesEnum,
        momentInfoModel: MomentInfoModel,
        pagingParams: MomentPagingParams,
        asPayload: Boolean = false,
        scrollToGroupId: Long? = null,
        scrollToStart: Boolean = false
    ) {
        Timber.tag("MomentDebug").d("updateViewHolder, source = $roadType, pagingParams=$pagingParams")
        coroutineScope?.launch(Dispatchers.IO) {
            val updateEvent = FeedUpdateEvent.FeedUpdateMoments(
                momentHolderId = MOMENTS_POST_ID,
                roadType = roadType,
                asPayload = asPayload,
                scrollToGroupId = scrollToGroupId,
                scrollToStart = scrollToStart,
                moments = carouselMomentsHelper.getMomentsForCarousel(
                    momentInfoModel = momentInfoModel,
                    pagingTicket = pagingParams.lastProducedPagingTicket,
                    addCreateMomentItem = isAddCreateMomentForRoadType(roadType),
                    isPagingUsed = isMomentsPagingUsedForRoadType(roadType) && !isMomentPagingOnLastPage(pagingParams)
                ),
                momentsInfo = momentInfoModel,
                momentsBlockAvatar = getUserSmallAvatarUseCase.invoke()
            )
            val updatePostParams = UpdatePostParams(updateEvent)
            forceUpdatePostUseCase.execute(updatePostParams)
        }
    }

    override fun getRoadType(): RoadTypesEnum? {
        return roadType
    }
}

interface MomentDelegate {

    fun initCoroutineScope(scope: CoroutineScope)

    fun initRoadType(roadTypeEnum: RoadTypesEnum?)

    /**
     * [MomentPagingParams] of the last paging request
     */
    fun getMomentPagingParams(): MomentPagingParams?

    fun isMomentsLastPage(): Boolean

    fun isUpdatingMoments(): Boolean

    /**
     * Check if the ticket with which the paging request is being made is valid
     *
     * We do this to avoid calling [requestMomentsPage] in the short period
     * after we returned a list but it is still in the process
     * of being submitted to the adapter
     *
     * The risk of this happening is increased since we have to
     * first submit data to the [FeedAdapter],
     * and then submit that data to the [MomentItemAdapter],
     * and both have async behavior during updating
     */
    fun isMomentPagingListTicketValid(ticket: String?): Boolean

    /**
     * Get items directly from cache without sorting
     */
    fun updateAllMomentsFromCache()

    fun updateMomentsAndViewedPosition(lastWatchedGroupId: Long)

    fun initialLoadMoments(scrollToStart: Boolean = false)

    fun requestAllMoments()

    /**
     * Get moments data with new page appended
     *
     * If paging ticket is valid, it will be "consumed", and further requests
     * with the same ticket will be skipped and return null
     *
     * Upon receiving the page data, [MomentPagingParams.lastProducedPagingTicket]
     * is set with a new paging ticket(if [MomentPagingParams.isLastPage] == false),
     * so that it can be used later to request the next page
     */
    fun requestMomentsPage(
        forceNewLoad: Boolean = false,
        limit: Int = DEFAULT_MOMENTS_PAGE_LIMIT,
        pagingTicket: String? = null,
    )

    fun hideUserMoments(
        userId: Long,
        onSuccess: () -> Unit = {}
    )

    fun getRoadType(): RoadTypesEnum?
}
