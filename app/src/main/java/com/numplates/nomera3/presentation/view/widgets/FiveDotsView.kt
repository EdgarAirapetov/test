package com.numplates.nomera3.presentation.view.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.core.INetworkValues
import com.numplates.nomera3.presentation.view.callback.IOnFiveDotsViewClicked
import java.util.*


class FiveDotsView
@JvmOverloads
constructor(context: Context,
         attrs: AttributeSet? = null,
         defStyle: Int = 0) : FrameLayout(context, attrs, defStyle), INetworkValues {

    var backgroundsSilver: List<ImageView>
    var backgroundsWhite: List<ImageView>
    var coloredDots: List<ImageView>

    private var iOnFiveDotsViewClicked: IOnFiveDotsViewClicked? = null

    var premiumColors: MutableList<Int> = ArrayList()
    var activeDot = 0
    var activeColor = INetworkValues.COLOR_RED

    init {
        (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)
                as LayoutInflater).inflate(R.layout.five_dots_view, this)

        backgroundsSilver = mutableListOf(
                findViewById(R.id.iv_dots_silver_bg1),
                findViewById(R.id.iv_dots_silver_bg2),
                findViewById(R.id.iv_dots_silver_bg3),
                findViewById(R.id.iv_dots_silver_bg4),
                findViewById(R.id.iv_dots_silver_bg5)
        )
        backgroundsWhite = mutableListOf(
                findViewById(R.id.iv_dots_white_bg1),
                findViewById(R.id.iv_dots_white_bg2),
                findViewById(R.id.iv_dots_white_bg3),
                findViewById(R.id.iv_dots_white_bg4),
                findViewById(R.id.iv_dots_white_bg5)
        )
        coloredDots = mutableListOf(
                findViewById(R.id.iv_dots_red),
                findViewById(R.id.iv_dots_green),
                findViewById(R.id.iv_dots_blue),
                findViewById(R.id.iv_dots_purple),
                findViewById(R.id.iv_dots_pink)
        )

        premiumColors.add(INetworkValues.COLOR_RED)
        premiumColors.add(INetworkValues.COLOR_GREEN)
        premiumColors.add(INetworkValues.COLOR_BLUE)
        premiumColors.add(INetworkValues.COLOR_PURPLE)
        premiumColors.add(INetworkValues.COLOR_PINK)

        setColor(activeColor)

        handleDotsClick()
    }

    private fun setColor(accountColor: Int) {
        activeColor = accountColor
        activeDot = premiumColors.indexOf(activeColor)
        for (i in coloredDots.indices) {
            backgroundsSilver[i].visibility = if (i == activeDot) VISIBLE else INVISIBLE
        }
        for (i in coloredDots.indices) {
            backgroundsSilver[i].invalidate()
            backgroundsWhite[i].invalidate()
            coloredDots[i].invalidate()
        }
    }

    fun setUpCallback(iOnFiveDotsViewClicked: IOnFiveDotsViewClicked?) {
        this.iOnFiveDotsViewClicked = iOnFiveDotsViewClicked
    }

    private fun handleDotsClick() {
        val views = mutableListOf<ImageView>()
        views.addAll(coloredDots)
        views.addAll(backgroundsWhite)
        views.forEach { v ->
            v.setOnClickListener {
                onDotClicked(it)
            }
        }
    }

    private fun onDotClicked(v: View) {
        when (v.id) {
            R.id.iv_dots_red, R.id.iv_dots_white_bg1 -> {
                activeDot = 0
                activeColor = INetworkValues.COLOR_RED
            }
            R.id.iv_dots_green, R.id.iv_dots_white_bg2 -> {
                activeDot = 1
                activeColor = INetworkValues.COLOR_GREEN
            }
            R.id.iv_dots_blue, R.id.iv_dots_white_bg3 -> {
                activeDot = 2
                activeColor = INetworkValues.COLOR_BLUE
            }
            R.id.iv_dots_purple, R.id.iv_dots_white_bg4 -> {
                activeDot = 3
                activeColor = INetworkValues.COLOR_PURPLE
            }
            R.id.iv_dots_pink, R.id.iv_dots_white_bg5 -> {
                activeDot = 4
                activeColor = INetworkValues.COLOR_PINK
            }
            else -> {
                activeDot = 0
                activeColor = INetworkValues.COLOR_RED
            }
        }
        setColor(premiumColors[activeDot])
        iOnFiveDotsViewClicked?.onFiveDotsViewClicked(activeColor)
    }

}