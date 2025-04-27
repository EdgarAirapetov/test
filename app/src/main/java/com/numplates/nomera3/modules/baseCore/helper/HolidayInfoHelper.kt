package com.numplates.nomera3.modules.baseCore.helper

import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.BuildConfig
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.holidays.ui.entity.Hats
import com.numplates.nomera3.modules.holidays.ui.entity.HolidayInfo
import com.numplates.nomera3.modules.holidays.ui.entity.MainButton
import com.numplates.nomera3.modules.holidays.ui.entity.OnBoarding
import com.numplates.nomera3.modules.holidays.ui.entity.Product
import com.numplates.nomera3.modules.holidays.ui.entity.RoomStyle
import com.numplates.nomera3.presentation.viewmodel.MainActivityViewModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

interface HolidayInfoHelper {
    fun currentHoliday(): HolidayInfo

    fun setCurrentHoliday(model: HolidayInfo?)

    fun isHolidayExistAndMatches(): Boolean

    fun getHatLink(accountType: AccountTypeEnum): String?

    fun todayDate(): String

    fun getYesterdaySavedDate(): Date

    fun wasShownYesterday(): Boolean

    fun wasShownToday(): Boolean

    fun isHolidayIntroduced(): Boolean

    fun sameUser(): Boolean
}

class HolidayInfoHelperImpl @Inject constructor(val appSettings: AppSettings) : HolidayInfoHelper {

    private var holidayInfo: HolidayInfo? = null
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun currentHoliday(): HolidayInfo {
        holidayInfo?.let {
            return it
        } ?: kotlin.run {
            val mainButton = MainButton(
                appSettings.holidayMainButtonDefault,
                appSettings.holidayMainButtonActive
            )
            val onboarding = OnBoarding(
                appSettings.holidayOnboardingTitle,
                appSettings.holidayOnboardingDesc,
                appSettings.holidayOnboardingIcon,
                appSettings.holidayOnboardingBtn,
            )
            val hats = Hats(
                appSettings.holidayHatRegular,
                appSettings.holidayHatPremium,
                appSettings.holidayHatVip,
            )
            val room = RoomStyle(
                appSettings.holidayRoomType,
                appSettings.holidayRoomBgDialog,
                appSettings.holidayRoomBgAnon,
                appSettings.holidayRoomBgGroup,
            )
            val product = Product(
                appSettings.holidayPrdId,
                appSettings.holidayPrdAppleId,
                appSettings.holidayPrdCustomTitle,
                appSettings.holidayPrdDesc,
                Product.ImageItem(
                    appSettings.holidayPrdImgLink,
                    appSettings.holidayPrdImgLinkSmall
                ),
                appSettings.holidayPrdItunesId,
                appSettings.holidayPrdPlayId,
                appSettings.holidayPrdType
            )
            val info = HolidayInfo(
                appSettings.holidayId,
                appSettings.holidayCode,
                appSettings.holidayTitle,
                mainButton,
                appSettings.holidayStartTime,
                appSettings.holidayFinishTime,
                onboarding,
                hats,
                room,
                product
            )
            holidayInfo = info
            return info
        }
    }

    override fun setCurrentHoliday(model: HolidayInfo?) {
        if (model != null) {
            holidayInfo = model
            appSettings.holidayId = model.id ?: -1L
            appSettings.holidayCode = model.code ?: ""
            appSettings.holidayTitle = model.title
            appSettings.holidayMainButtonActive = model.mainButtonLinkEntity.active ?: ""
            appSettings.holidayMainButtonDefault = model.mainButtonLinkEntity.default ?: ""
            appSettings.holidayStartTime = model.startTime ?: -1L
            appSettings.holidayFinishTime = model.finishTime ?: -1L
            appSettings.holidayOnboardingTitle = model.onBoardingEntity.title ?: ""
            appSettings.holidayOnboardingDesc = model.onBoardingEntity.description ?: ""
            appSettings.holidayOnboardingIcon = model.onBoardingEntity.icon ?: ""
            appSettings.holidayOnboardingBtn = model.onBoardingEntity.buttonText ?: ""
            appSettings.holidayHatVip = model.hatsLink.vip ?: ""
            appSettings.holidayHatPremium = model.hatsLink.premium ?: ""
            appSettings.holidayHatRegular = model.hatsLink.general ?: ""
            appSettings.holidayRoomType = model.chatRoomEntity.type ?: ""
            appSettings.holidayRoomBgDialog = model.chatRoomEntity.background_dialog ?: ""
            appSettings.holidayRoomBgAnon = model.chatRoomEntity.background_anon ?: ""
            appSettings.holidayRoomBgGroup = model.chatRoomEntity.background_group ?: ""
            appSettings.holidayPrdId = model.product?.id ?: -1
            appSettings.holidayPrdAppleId = model.product?.appleProductId ?: ""
            appSettings.holidayPrdCustomTitle = model.product?.customTitle ?: ""
            appSettings.holidayPrdDesc = model.product?.description ?: ""
            appSettings.holidayPrdImgLink = model.product?.imageItem?.link ?: ""
            appSettings.holidayPrdImgLinkSmall = model.product?.imageItem?.linkSmall ?: ""
            appSettings.holidayPrdItunesId = model.product?.itunesProductId ?: ""
            appSettings.holidayPrdPlayId = model.product?.playMarketProductId ?: ""
            appSettings.holidayPrdType = model.product?.type ?: -1L
        } else {
            appSettings.holidayId = -1L
            appSettings.holidayTitle = ""
            appSettings.holidayCode = ""
            appSettings.holidayMainButtonActive = ""
            appSettings.holidayMainButtonDefault = ""
            appSettings.holidayStartTime = -1L
            appSettings.holidayFinishTime = -1L
            appSettings.holidayOnboardingTitle = ""
            appSettings.holidayOnboardingDesc = ""
            appSettings.holidayOnboardingIcon = ""
            appSettings.holidayOnboardingBtn = ""
            appSettings.holidayHatVip = ""
            appSettings.holidayHatPremium = ""
            appSettings.holidayHatRegular = ""
            appSettings.holidayRoomType = ""
            appSettings.holidayRoomBgDialog = ""
            appSettings.holidayRoomBgAnon = ""
            appSettings.holidayRoomBgGroup = ""
            appSettings.holidayPrdId = -1L
            appSettings.holidayPrdAppleId = ""
            appSettings.holidayPrdCustomTitle = ""
            appSettings.holidayPrdDesc = ""
            appSettings.holidayPrdImgLink = ""
            appSettings.holidayPrdImgLinkSmall = ""
            appSettings.holidayPrdItunesId = ""
            appSettings.holidayPrdPlayId = ""
            appSettings.holidayPrdType = -1L
        }
    }

    override fun isHolidayExistAndMatches(): Boolean {
        val holidayInfo = currentHoliday()
        if (holidayInfo.id != -1L) {
            val currentTime = System.currentTimeMillis() / 1000
            val startTime = holidayInfo.startTime ?: -1L
            val finishTime = holidayInfo.finishTime ?: -1L
            if (finishTime != -1L && startTime != -1L &&
                startTime <= currentTime && currentTime <= finishTime
            ) {
                return true
            }
        }
        return false
    }

    override fun getYesterdaySavedDate(): Date {
        val yesterdaySaved: String? = appSettings.holidayCalendarShowDate
        return if (yesterdaySaved != null) {
            return try {
                dateFormat.parse(yesterdaySaved) ?: Calendar.getInstance().time
            } catch (e: Exception) {
                Timber.e(e)
                Calendar.getInstance().time
            }
        } else {
            Calendar.getInstance().time
        }
    }

    override fun getHatLink(accountType: AccountTypeEnum): String? {
        return if (isHolidayExistAndMatches()) {
            when (accountType) {
                AccountTypeEnum.ACCOUNT_TYPE_PREMIUM -> holidayInfo?.hatsLink?.premium
                AccountTypeEnum.ACCOUNT_TYPE_VIP -> holidayInfo?.hatsLink?.vip
                else -> holidayInfo?.hatsLink?.general
            }
        } else {
            null
        }
    }

    override fun todayDate(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 0)
        return dateFormat.format(cal.time)
    }

    override fun wasShownYesterday(): Boolean {
        val yesterdayCal = Calendar.getInstance()
        yesterdayCal.add(Calendar.DAY_OF_YEAR, -1)
        val lastDateCal = Calendar.getInstance()
        lastDateCal.time = getYesterdaySavedDate()
        val yesterday = yesterdayCal.get(Calendar.DAY_OF_YEAR)
        val lastDate = lastDateCal.get(Calendar.DAY_OF_YEAR)
        val isDayValid = when {
            yesterday > MainActivityViewModel.YEAR_DAYS_THRESHOLD_MAX &&
                lastDate <= MainActivityViewModel.YEAR_DAYS_THRESHOLD_MIN -> true
            yesterday <= lastDate -> true
            else -> false
        }
        return isDayValid
    }

    override fun wasShownToday(): Boolean = appSettings.holidayCalendarShowDate == todayDate()

    override fun isHolidayIntroduced(): Boolean = appSettings.isHolidayIntroduced &&
        appSettings.holidayIntroducedVersion == BuildConfig.VERSION_NAME

    override fun sameUser(): Boolean = appSettings.holidayCalendarShownToUserWithId == appSettings.readUID()

}
