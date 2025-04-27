package com.numplates.nomera3.presentation.view.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.bumptech.glide.request.RequestOptions
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlideWithCacheAndErrorPlaceHolder
import com.meera.core.extensions.visible
import com.meera.core.utils.graphics.OvalTouchAreaFilter
import com.meera.core.views.NavigationBarViewContract
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.HolidayInfoHelper
import timber.log.Timber
import javax.inject.Inject

const val HOLIDAY_HALLOWEEN = "halloween"
const val HOLIDAY_NEW_YEAR = "newyear"

class NavigationBarView @JvmOverloads constructor(
        context: Context,
        attributeSet: AttributeSet? = null,
        defStyleAtr: Int = 0,
) : FrameLayout(context, attributeSet, defStyleAtr), NavigationBarViewContract {

    var ivChatBtn: ImageView? = null
    var ivRoadBtn: ImageView? = null
    var vRoadBtn: View? = null
    var ivMapBtn: ImageView? = null
    var ivPeoplesBtn: ImageView? = null
    var ivProfileBtn: ImageView? = null
    var tvChatCounter: TextView? = null
    var tvCallCounter: TextView? = null
    var tv_map: TextView? = null
    var tvPeoples: TextView? = null
    var tv_road: TextView? = null
    var tv_messenger: TextView? = null
    var tv_profile: TextView? = null
    var ivDotUnreadEvent: ImageView? = null
    var vCoverChat: View? = null
    var ivDotProfileNotification: ImageView? = null
    var vCoverDotProfileNotification: View? = null
    private var ivDotPeopleNotification: ImageView? = null
    private var vCoverDotPeopleNotification: View? = null
    private val UNSEND_MESSAGE_SIGN = "!"
    private var chatCounter: Int = 0
    private var needToShowUnSendBadge = false

    private var needToShowEventCounter = false

    @Inject
    lateinit var holidayHelper: HolidayInfoHelper

    init {
        App.component.inject(this)
        val view = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.navigation_bar_view, this)
        vCoverChat = view.findViewById(R.id.v_cover_dot_unread_counter)
        ivDotProfileNotification = view.findViewById(R.id.iv_dot_profile_notification)
        vCoverDotProfileNotification = view.findViewById(R.id.v_cover_dot_profile_notification)
        ivChatBtn = view.findViewById(R.id.ivChatBtn)
        ivRoadBtn = view.findViewById(R.id.ivRoadBtn)
        vRoadBtn = view.findViewById(R.id.v_road_btn)
        ivMapBtn = view.findViewById(R.id.ivMapBtn)
        ivPeoplesBtn = view.findViewById(R.id.ivFriendListBtn)
        ivProfileBtn = view.findViewById(R.id.ivProfileBtn)
        tvChatCounter = view.findViewById(R.id.tvChatCounter)
        tvCallCounter = view.findViewById(R.id.tvCallCounter)
        tv_map = view.findViewById(R.id.tv_map)
        tvPeoples = view.findViewById(R.id.tv_peoples)
        tv_road = view.findViewById(R.id.tv_road)
        tv_messenger = view.findViewById(R.id.tv_messenger)
        tv_profile = view.findViewById(R.id.tv_profile)
        ivDotUnreadEvent = view.findViewById(R.id.iv_dot_unread_counter)
        ivDotPeopleNotification = view.findViewById(R.id.iv_dot_people_notification)
        vCoverDotPeopleNotification = view.findViewById(R.id.v_cover_dot_people_notification)
        when (holidayHelper.currentHoliday().code) {
            HOLIDAY_HALLOWEEN -> {
                ivRoadBtn?.setImageResource(R.drawable.selector_button_road_halloween)
            }
            HOLIDAY_NEW_YEAR -> {
                ivRoadBtn?.setImageResource(R.drawable.selector_button_road_new_year_unselected)
            }
            else -> {
                ivRoadBtn?.setImageResource(R.drawable.selector_button_road)
            }
        }
    }

    override fun setListener(listener: NavigationBarViewContract.NavigatonBarListener) {
        val naviBarListener = OnClickListener { view ->
            when (view.id) {
                R.id.ivChatBtn -> listener.onClickChat()
                R.id.tv_messenger -> listener.onClickChat()
                R.id.v_road_btn -> listener.onClickRoad()
                R.id.tv_road -> listener.onClickRoad()
                R.id.ivMapBtn -> listener.onClickMap()
                R.id.tv_map -> listener.onClickMap()
                R.id.ivFriendListBtn -> listener.onClickPeoples()
                R.id.tv_peoples -> listener.onClickPeoples()
                R.id.ivProfileBtn -> listener.onClickProfile()
                R.id.tv_profile -> listener.onClickProfile()
                else -> {
                }
            }
        }

        ivChatBtn?.setOnClickListener(naviBarListener)
        ivRoadBtn?.setOnClickListener(naviBarListener)
        vRoadBtn?.setOnTouchListener(OvalTouchAreaFilter())
        vRoadBtn?.setOnClickListener(naviBarListener)
        ivMapBtn?.setOnClickListener(naviBarListener)
        ivPeoplesBtn?.setOnClickListener(naviBarListener)
        ivProfileBtn?.setOnClickListener(naviBarListener)
        tv_map?.setOnClickListener(naviBarListener)
        tvPeoples?.setOnClickListener(naviBarListener)
        tv_road?.setOnClickListener(naviBarListener)
        tv_messenger?.setOnClickListener(naviBarListener)
        tv_profile?.setOnClickListener(naviBarListener)
    }

    var chatMessagesCountListener: (Int) -> Unit = {}
    override fun updateChatCounter(chatCounter: Int, callCounter: Long) {
        this.chatCounter = chatCounter
        chatMessagesCountListener(chatCounter)
        if (needToShowUnSendBadge) return
        if (tvChatCounter == null || tvCallCounter == null) {
            return
        }
        tvChatCounter?.text = chatCounter.toString()
        tvCallCounter?.text = callCounter.toString()
        if (chatCounter == 0) {
            tvChatCounter?.visibility = View.GONE
            if (needToShowEventCounter) {
                ivDotUnreadEvent?.visible()
                vCoverChat?.visible()
            }
        } else {
            tvChatCounter?.visibility = View.VISIBLE
            ivDotUnreadEvent?.gone()
            vCoverChat?.gone()
        }
        if (callCounter == 0L) {
            tvCallCounter?.visibility = View.GONE
        } else {
            tvCallCounter?.visibility = View.VISIBLE
        }
    }

    override fun updateProfileIndicator(needToShow: Boolean) {
        if (needToShow) {
            Timber.e("NavigationBar showCounter")
            ivDotProfileNotification?.visible()
            vCoverDotProfileNotification?.visible()
        } else {
            Timber.e("NavigationBar hideCounter")
            ivDotProfileNotification?.gone()
            vCoverDotProfileNotification?.gone()
        }
    }

    override fun updatePeopleBadge(needToShow: Boolean) {
        ivDotPeopleNotification?.isVisible = needToShow
        vCoverDotPeopleNotification?.isVisible = needToShow
    }

    override fun showUnreadEventsCounter(needToShow: Boolean) {
        Timber.d("showUnreadEventsCounter: needToShow = $needToShow")
        this.needToShowEventCounter = needToShow
        if (tvChatCounter == null || ivDotUnreadEvent == null)
            return

        if (tvChatCounter?.visibility == View.VISIBLE || !needToShow) {
            ivDotUnreadEvent?.gone()
            vCoverChat?.gone()

        } else if (tvChatCounter?.visibility == View.GONE && needToShow) {
            ivDotUnreadEvent?.visible()
            vCoverChat?.visible()
        }

    }

    fun incrementChatCounter() {
        if (tvChatCounter == null) return
        var counterTxt = tvChatCounter!!.text.toString()
        try {
            if (counterTxt.isEmpty()) {
                counterTxt = "0"
            }
            var counter = counterTxt.toLong()
            counter++
            tvChatCounter?.text = counter.toString()
            tvChatCounter?.visibility = View.VISIBLE
        } catch (e: Exception) { /* IGNORE */
            Timber.e(e)
        }
    }

    fun selectMap(isSelected: Boolean?) {
        ivMapBtn?.isSelected = isSelected!!
        tv_map?.isSelected = isSelected
    }

    fun selectGroups(isSelected: Boolean?) {
        ivPeoplesBtn?.isSelected = isSelected!!
        tvPeoples?.isSelected = isSelected
    }

    fun setPeoples(isSelected: Boolean) {
        ivPeoplesBtn?.isSelected = isSelected
        tvPeoples?.isSelected = isSelected
    }

    fun selectRoad(isSelected: Boolean?) {
        ivRoadBtn?.isSelected = isSelected!!
        tv_road?.isSelected = isSelected
        selectRoadWithHoliday(isSelected)
    }

    fun selectMessenger(isSelected: Boolean?) {
        ivChatBtn?.isSelected = isSelected!!
        tv_messenger?.isSelected = isSelected
    }

    fun selectProfile(isSelected: Boolean?) {
        ivProfileBtn?.isSelected = isSelected!!
        tv_profile?.isSelected = isSelected
    }

    override fun updateUnreadBadge(needToShowBadge: Boolean) {
        Timber.d("updateUnreadBadge with value = $needToShowBadge")
        needToShowUnSendBadge = needToShowBadge
        if (needToShowBadge) {
            tvChatCounter?.text = UNSEND_MESSAGE_SIGN
            tvChatCounter?.visible()
        } else {
            updateChatCounter(chatCounter, 0)
        }
    }

    override fun updateNavBarBasedOnHoliday() {
        selectRoadWithHoliday(ivRoadBtn?.isSelected)
    }

    private fun selectRoadWithHoliday(isSelected: Boolean?) {
        if (holidayHelper.isHolidayExistAndMatches()) setHolidayRoadButton(isSelected)
        else ivRoadBtn?.setImageResource(R.drawable.selector_button_road)
    }

    private fun setHolidayRoadButton(isSelected: Boolean?) {
        val currentHoliday = holidayHelper.currentHoliday()
        when (currentHoliday.code) {
            HOLIDAY_HALLOWEEN ->
                ivRoadBtn?.setImageResource(R.drawable.selector_button_road_halloween)
            HOLIDAY_NEW_YEAR -> setHolidayNewYearButton(isSelected)
            else -> setHolidayForUnknownType(isSelected)
        }
    }

    private fun setHolidayForUnknownType(isSelected: Boolean?){
        if (holidayHelper.isHolidayExistAndMatches()) {
            val currentHoliday = holidayHelper.currentHoliday()
            if (currentHoliday.code == HOLIDAY_HALLOWEEN) {
                ivRoadBtn?.setImageResource(R.drawable.selector_button_road_halloween)
            } else {
                val active = currentHoliday.mainButtonLinkEntity.active
                val def = currentHoliday.mainButtonLinkEntity.default
                if (isSelected == true) {
                    ivRoadBtn?.loadGlideWithCacheAndErrorPlaceHolder(active, RequestOptions.errorOf(R.drawable.roadselected))
                } else if (isSelected == false){
                    ivRoadBtn?.loadGlideWithCacheAndErrorPlaceHolder(def, RequestOptions.errorOf(R.drawable.iconroad))
                }
            }

        } else {
            ivRoadBtn?.setImageResource(R.drawable.selector_button_road)
        }
    }

    private fun setHolidayNewYearButton(isSelected: Boolean?) {
        if (isSelected == true) ivRoadBtn?.setImageResource(R.drawable.selector_button_road_new_year_selected)
        else ivRoadBtn?.setImageResource(R.drawable.selector_button_road_new_year_unselected)
    }

}
