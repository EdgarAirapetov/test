package com.numplates.nomera3.modules.userprofile.data.repository

import com.meera.core.extensions.empty
import com.meera.core.preferences.AppSettings
import com.meera.db.DataStore
import com.meera.db.models.userprofile.UserProfileNew
import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.data.network.EmptyModel
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.baseCore.domain.model.CoordinatesModel
import com.numplates.nomera3.modules.billing.BillingClientWrapper
import com.numplates.nomera3.modules.feed.domain.mapper.toPostUIEntity
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.user.data.mapper.UserProfileDomainMapper
import com.numplates.nomera3.modules.user.data.mapper.UserProfileDtoDomainMapper
import com.numplates.nomera3.modules.userprofile.data.api.UserProfileApi
import com.numplates.nomera3.modules.userprofile.data.mapper.ProfileSuggestionMapper
import com.numplates.nomera3.modules.userprofile.data.mapper.UserAvatarsMapper
import com.numplates.nomera3.modules.userprofile.data.mapper.UserGalleryMapper
import com.numplates.nomera3.modules.userprofile.data.mapper.UserProfileDtoToDbMapper
import com.numplates.nomera3.modules.userprofile.domain.model.AvatarModel
import com.numplates.nomera3.modules.userprofile.domain.model.ProfileSuggestionModel
import com.numplates.nomera3.modules.userprofile.domain.model.UserAvatarsModel
import com.numplates.nomera3.modules.userprofile.domain.model.UserGalleryModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserProfileModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

private const val GALLERY_LIMIT = 100

class ProfileRepositoryImpl @Inject constructor(
    private val api: UserProfileApi,
    private val apiMain: ApiMain,
    private val dataStore: DataStore,
    private val userAvatarsMapper: UserAvatarsMapper,
    private val userGalleryMapper: UserGalleryMapper,
    private val dbMapper: UserProfileDomainMapper,
    private val dtoMapper: UserProfileDtoDomainMapper,
    private val dtoToDbMapper: UserProfileDtoToDbMapper,
    private val billingClientWrapper: BillingClientWrapper,
    private val profileSuggestionMapper: ProfileSuggestionMapper,
    private val appSettings: AppSettings
) : ProfileRepository {

    override suspend fun requestProfileDbModel(userId: Long, gpsX: Float?, gpsY: Float?, withoutSideEffects: Boolean) =
        withContext(Dispatchers.IO) {
            val res = if (withoutSideEffects) api.getProfileV3(userId, gpsX, gpsY)
            else api.getProfile(userId, gpsX, gpsY)
            if (res.data != null) {
                val dbEntity = dtoToDbMapper.mapDtoToDb(res.data)
                checkHolidayProductPrice(dbEntity)
                return@withContext dbEntity
            } else throw (IllegalThreadStateException("Response data is null"))
        }

    override suspend fun requestProfile(userId: Long, withoutSideEffects: Boolean): UserProfileModel =
        withContext(Dispatchers.IO) {
            val location = appSettings.readLastLocation()?.let { (lat, lon) ->
                CoordinatesModel(lat = lat, lon = lon)
            }
            val gpsX = location?.lat?.toFloat()
            val gpsY = location?.lon?.toFloat()
            val profile = if (withoutSideEffects) api.getProfileV3(userId, gpsX, gpsY).data
            else api.getProfile(userId, gpsX, gpsY).data
            val price = getProductPriceByMarketId(profile?.holidayProduct?.playMarketProductId)
            return@withContext dtoMapper.mapDataToDomain(
                profile = profile,
                price = price
            )
        }

    override suspend fun setProfileViewed(userId: Long): ResponseWrapper<EmptyModel> {
        return withContext(Dispatchers.IO) {
            return@withContext api.setProfileViewed(userId)
        }
    }

    override suspend fun makeCallUnvailables(
        userId: Long
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            return@withContext api.postCallUnavailable(userId).data ?: false
        } catch (exception: Exception) {
            Timber.e("call_unavailable ${exception.message}")
            return@withContext false
        }
    }

    override suspend fun requestOwnProfileSynch(): UserProfileNew {
        val profile = api.getOwnUserProfile().data
        profile?.let { updatePref(profile.accountColor, profile.accountType) }
        return dtoToDbMapper.mapDtoToDb(profile)
    }

    private fun updatePref(accountColor: Int?, accountType: Int?) {
        accountColor?.let { appSettings.accountColor = it }
        accountType?.let { appSettings.accountType = it }
    }

    private suspend fun checkHolidayProductPrice(data: UserProfileNew) {
        try {
            data.holidayProduct?.playMarketProductId.let { marketId ->
                if (marketId.isNullOrBlank()) return
                val productDetail = billingClientWrapper.queryProductDetail(marketId)
                data.holidayProduct?.price = productDetail?.oneTimePurchaseOfferDetails?.formattedPrice.orEmpty()
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private suspend fun getProductPriceByMarketId(marketId: String?): String {
        return try {
            if (marketId.isNullOrBlank()) return String.empty()
            val productDetail = billingClientWrapper.queryProductDetail(marketId)
            productDetail?.oneTimePurchaseOfferDetails?.formattedPrice.orEmpty()
        } catch (e: Exception) {
            Timber.e(e)
            return String.empty()
        }
    }

    override suspend fun updateOwnProfileDb(profile: UserProfileNew): Long = withContext(Dispatchers.IO) {
        updatePref(profile.accountColor, profile.accountType)
        return@withContext dataStore.userProfileDao().insert(profile)
    }

    override fun getOwnProfileModelFlow(): Flow<UserProfileModel> =
        dataStore.userProfileDao().getUserProfileFlow().filterNotNull().map {
            dbMapper.mapDataToDomain(it)
        }

    override suspend fun getOwnLocalProfile() = withContext(Dispatchers.IO) {
        val dbProfile = dataStore.userProfileDao().getUserProfile()
        return@withContext if (dbProfile != null) dbMapper.mapDataToDomain(dbProfile) else null
    }

    override suspend fun setAvatarAsMain(photoId: Long): AvatarModel {
        val responseData = api.setAvatarAsMain(photoId).data
        return userAvatarsMapper.avatarDtoToAvatarModel(responseData)
    }

    override suspend fun getAvatars(userId: Long, limit: Int, offset: Int): UserAvatarsModel {
        val responseData = api.getAvatars(userId, limit, offset).data
        return userAvatarsMapper.userAvatarsDtoToUserAvatarsModel(responseData)
    }

    override suspend fun getGallery(userId: Long, limit: Int, offset: Int): UserGalleryModel {
        val responseData = api.getGallery(userId, GALLERY_LIMIT, offset).data
        return userGalleryMapper.userGalleryDtoToUserGalleryModel(responseData)
    }

    override suspend fun getPost(postId: Long): PostUIEntity {
        val response = apiMain.getPost(postId)?.data
        return response?.toPostUIEntity() ?: throw IllegalThreadStateException("Response data is null")
    }

    override suspend fun getProfileSuggestions(userId: Long): List<ProfileSuggestionModel> {
        val response = api.getProfileSuggestions(userId).data
        return response?.users?.map(profileSuggestionMapper::mapUserSimpleToModel) ?: emptyList()
    }
}
