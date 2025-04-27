package com.numplates.nomera3.modules.purchase.ui.vip

import com.numplates.nomera3.data.network.core.INetworkValues
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyColor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyDuration
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHaveVIPBefore
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWay
import com.numplates.nomera3.modules.purchase.domain.product.Premium
import com.numplates.nomera3.modules.purchase.domain.product.Vip
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@Suppress("PrivatePropertyName")
class UpgradeToVipLogger @Inject constructor(
    private val tracker: AnalyticsInteractor,
) {

    private val WEEK_TIMESTAMP = 1000L * 60L * 60L * 24L * 7L
    private val MONTH_TIMESTAMP = 1000L * 60L * 60L * 24L * 30L
    private val THREE_MONTH_TIMESTAMP = 1000L * 60L * 60L * 24L * 30L * 3L
    private val YEAR_TIMESTAMP = 1000L * 60L * 60L * 24L * 365L

    fun logVipBuying(
        accountColor: Int?,
        productId: String?,
        accountType: Int?,
    ) {
        var color = when (accountColor) {
            INetworkValues.COLOR_RED -> AmplitudePropertyColor.RED
            INetworkValues.COLOR_GREEN -> AmplitudePropertyColor.GREEN
            INetworkValues.COLOR_BLUE -> AmplitudePropertyColor.BLUE
            INetworkValues.COLOR_PINK -> AmplitudePropertyColor.PINK
            INetworkValues.COLOR_PURPLE -> AmplitudePropertyColor.PURPLE
            else -> AmplitudePropertyColor.NONE
        }
        if (productId?.contains("vip") == true) color = AmplitudePropertyColor.GOLD
        val hasVipBefore = if (accountType == AccountTypeEnum.ACCOUNT_TYPE_VIP.value
            || accountType == AccountTypeEnum.ACCOUNT_TYPE_PREMIUM.value
        ) {
            AmplitudePropertyHaveVIPBefore.TRUE
        } else {
            AmplitudePropertyHaveVIPBefore.FALSE
        }
        var expirationDate = System.currentTimeMillis()
        val duration = when (productId) {
            Premium.PREMIUM_WEEK.id, Vip.VIP_WEEK.id -> {
                expirationDate += WEEK_TIMESTAMP
                AmplitudePropertyDuration.WEEK
            }
            Premium.PREMIUM_MONTH.id, Vip.VIP_MONTH.id -> {
                expirationDate += MONTH_TIMESTAMP
                AmplitudePropertyDuration.MONTH
            }
            Premium.PREMIUM_THREE_MONTH.id, Vip.VIP_THREE_MONTH.id -> {
                expirationDate += THREE_MONTH_TIMESTAMP
                AmplitudePropertyDuration.THREE_MONTH
            }
            Premium.PREMIUM_ONE_YEAR.id, Vip.VIP_ONE_YEAR.id -> {
                expirationDate += YEAR_TIMESTAMP
                AmplitudePropertyDuration.YEAR
            }
            else -> AmplitudePropertyDuration.NONE
        }
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val expirationStr = format.format(expirationDate)
        tracker.logBuyVipStatus(
            color = color,
            duration = duration,
            expirationDate = expirationStr,
            haveVipBefore = hasVipBefore,
            way = AmplitudePropertyWay.BUY
        )
    }
}
