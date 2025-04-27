package com.meera.core.views

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.meera.core.R
import com.meera.core.extensions.displayHeight
import com.meera.core.extensions.displayWidth
import com.meera.core.extensions.dp
import com.meera.core.extensions.getDrawableCompat
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.updatePadding

class MenuPopup(private val context: Context?) : PopupWindow(context) {

    private val container by lazy {
        LinearLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            background = context.getDrawableCompat(R.drawable.background_main_small_corners)
            updatePadding(paddingTop = 8.dp, paddingBottom = 8.dp)
        }
    }


    fun show(anchorView: View? = null, gravity: Int = Gravity.CENTER, x: Int = 0, y: Int = 0) {
        context?.let {
            container.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            if (container.measuredHeight <= context.displayHeight - 16.dp) {
                contentView = container
                height = container.measuredHeight
            } else {
                val scrollView = ScrollView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    background = context.getDrawableCompat(R.drawable.background_main_small_corners)
                }
                scrollView.addView(container)
                contentView = scrollView
                height = context.displayHeight - 16.dp - context.getStatusBarHeight()
            }
            width = container.measuredWidth
            setBackgroundDrawable(context.getDrawableCompat(R.drawable.background_main_small_corners))
            isOutsideTouchable = true
            elevation = 20f.dp
            isFocusable = true
            animationStyle = R.style.popup_window_animation
            if (x != 0 && y != 0) {
                var rX = x
                var rY = y
                if (x - width / 2 <= 8.dp) {
                    rX = width / 2 + 8.dp
                }
                if (x + width / 2 >= context.displayWidth - 8.dp) {
                    rX = context.displayWidth - width / 2 - 8.dp
                }
                if (y - height / 2 <= context.getStatusBarHeight() + 8.dp) {
                    rY = height / 2 + context.getStatusBarHeight() + 8.dp
                }
                if (y + height / 2 >= context.displayHeight - 8.dp) {
                    rY = context.displayHeight - height / 2 - 8.dp
                }
                showAtLocation(anchorView, Gravity.START or Gravity.TOP, rX - width / 2, rY - height / 2)
            } else {
                showAsDropDown(anchorView, x, y, gravity)
            }

        }
    }


    fun addTitle(@StringRes title: Int) {
        context?.let {
            addTitle(context.getString(title))
        }
    }

    fun addTitle(title: String) {
        context?.let {
            val textView = TextView(context, null, 0, R.style.BlackRegular14)
            textView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            textView.setPadding(20.dp, 16.dp, 16.dp, 4.dp)
            textView.text = title
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            container.addView(textView)
        }
    }

    fun addDivider() {
        context?.let {
            val divider = View(context)
            divider.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                1.dp)
            divider.background = ContextCompat.getDrawable(context, R.color.color_divider_gray)
            container.addView(divider)
        }
    }

    fun addItem(title: String?, click: () -> Unit) {
        context?.let {
            addItem(title, null, click)
        }
    }

    fun addItem(@StringRes title: Int, click: () -> Unit) {
        context?.let {
            val mTitle = context.getString(title)
            addItem(mTitle, null, click)
        }
    }

    fun addItem(@DrawableRes icon: Int, @StringRes title: Int, click: () -> Unit) {
        context?.let {
            val mTitle = context.getString(title)
            val mIcon = ContextCompat.getDrawable(context, icon)
            addItem(mTitle, mIcon, click)
        }
    }

    fun addItem(title: String?, icon: Any?, click: () -> Unit) {
        context?.let {
            val itemView = BottomItemView(context)
            itemView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            icon?.let { icon ->
                itemView.setIcon(icon)
            }

            title?.let { title ->
                itemView.setTitle(title)
            }

            container.addView(itemView)

            itemView.setOnClickListener {
                dismiss()
                click.invoke()
            }
        }
    }

    fun addSwitchItem(title: String?, isChecked: Boolean = false, click: (Boolean) -> Unit) {
        context?.let {
            addSwitchItem(title, null, isChecked, click)
        }
    }

    fun addSwitchItem(@StringRes title: Int, isChecked: Boolean = false, click: (Boolean) -> Unit) {
        context?.let {
            addSwitchItem(context.getString(title), null, isChecked, click)
        }
    }

    fun addSwitchItem(icon: Any?, @StringRes title: Int, isChecked: Boolean = false, click: (Boolean) -> Unit) {
        context?.let {
            addSwitchItem(context.getString(title), icon, isChecked, click)
        }
    }

    fun addSwitchItem(title: String?, icon: Any?, isChecked: Boolean = false, click: (Boolean) -> Unit) {
        context?.let {
            val itemView = BottomItemView(context)
            itemView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            icon?.let { icon ->
                itemView.setIcon(icon)
            }
            var switchButton: SwitchCompat? = null
            title?.let {
                switchButton = itemView.createSwitchButton(title)
            }
            switchButton?.isChecked = isChecked
            container.addView(itemView)
            itemView.setOnClickListener {
                if (switchButton?.isChecked == true) {
                    switchButton?.isChecked = false
                    click.invoke(false)
                } else {
                    switchButton?.isChecked = true
                    click.invoke(true)
                }
            }
        }
    }

}
