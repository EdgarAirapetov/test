package com.numplates.nomera3.presentation.view.utils

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.androidadvance.topsnackbar.TSnackbar
import com.google.android.material.snackbar.Snackbar
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.router.BaseAct
import com.meera.core.extensions.empty

@Deprecated("Not used at all in the new design system")
class NToast(
    val act: BaseAct?,
    val type: Int = SNACKBAR_TYPE_ERROR,
    val duration: Int = Snackbar.LENGTH_SHORT,
    val text: String = String.empty(),
    val buttonText: String?,
    val click: () -> Unit?,
    val inView: View? = null,
    val dismissListener: () -> Unit = {}
) {

    init {
        act?.let {
            val context = it

            @SuppressLint("WrongConstant")
            val snackbar = if (inView == null) {
                TSnackbar.make(act.window.decorView, text, duration)
            } else {

                TSnackbar.make(inView, text, duration)
            }


            val layout = snackbar.view as TSnackbar.SnackbarLayout
            var result = 0
            val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = context.resources.getDimensionPixelSize(resourceId)
            }
            val snackView = LayoutInflater.from(layout.context).inflate(R.layout.snackbar_view, null)
            val snackbarLayout = snackView.findViewById<LinearLayout>(R.id.ll_snackbar_view)
            when (type) {
                SNACKBAR_TYPE_ERROR -> {
                    snackView.findViewById<AppCompatImageView>(R.id.iv_snackbar).setImageResource(R.drawable.alert_error)
                    snackbarLayout.setBackgroundColor(context.resources.getColor(R.color.color_notificaton_red))
                }
                SNACKBAR_TYPE_ALERT -> {
                    snackView.findViewById<AppCompatImageView>(R.id.iv_snackbar).setImageResource(R.drawable.alert_info)
                    snackbarLayout.setBackgroundColor(context.resources.getColor(R.color.color_notificaton_blue))
                }
                SNACKBAR_TYPE_SUCCESS -> {
                    snackView.findViewById<AppCompatImageView>(R.id.iv_snackbar).setImageResource(R.drawable.alert_success)
                    snackbarLayout.setBackgroundColor(context.resources.getColor(R.color.color_notificaton_green))
                }
            }
            val statusBarSnackbar = snackView.findViewById<View>(R.id.statusbar_snackbar)
            val params = statusBarSnackbar.layoutParams as LinearLayout.LayoutParams
            params.height = result
            statusBarSnackbar.layoutParams = params

            if (buttonText != null) {
                val button = snackView.findViewById<Button>(R.id.btn_snackbar)
                button.visibility = View.VISIBLE
                button.setOnClickListener {
                    click.invoke()
                    snackbar.dismiss()
                }
                button.text = buttonText
            }
            val textViewTop = snackView.findViewById<TextView>(R.id.tv_snackbar)
            textViewTop.text = text
            layout.setPadding(0, 0, 0, 0)
            layout.addView(snackView, 0)
            layout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorTransparent))

            if (act is Act) {
                snackbar.setCallback(object : TSnackbar.Callback() {
                    override fun onDismissed(snackbar: TSnackbar?, event: Int) {
                        super.onDismissed(snackbar, event)
                        act.setStatusBar()
                        dismissListener()
                    }
                })
            }

            if (text.isNotEmpty())
                snackbar.show()
        }
    }


    class Builder(private val act: BaseAct?) {

        private var snackBarType: Int = SNACKBAR_TYPE_ERROR
        private var snackBarDuration: Int = Snackbar.LENGTH_SHORT
        private var text: String = String.empty()
        private var buttonText: String? = null
        private var click: () -> Unit? = {}
        private var inView: View? = null
        private var dismissListener: () -> Unit = {}

        fun dismissListener(dismissListener: () -> Unit): Builder {
            this.dismissListener = dismissListener
            return this
        }

        fun type(type: Int): Builder {
            this.snackBarType = type
            return this
        }

        fun typeError(): Builder {
            this.snackBarType = SNACKBAR_TYPE_ERROR
            return this
        }

        fun typeAlert(): Builder {
            this.snackBarType = SNACKBAR_TYPE_ALERT
            return this
        }

        fun typeSuccess(): Builder {
            this.snackBarType = SNACKBAR_TYPE_SUCCESS
            return this
        }

        fun duration(duration: Int): Builder {
            this.snackBarDuration = duration
            return this
        }

        fun durationLong(): Builder {
            this.snackBarDuration = Snackbar.LENGTH_LONG
            return this
        }

        fun durationIndefinite(): Builder {
            this.snackBarDuration = Snackbar.LENGTH_INDEFINITE
            return this
        }

        fun text(text: String?): Builder {
            try {
                this.text = text ?: String.empty()
            } catch (e: Exception) {
                this.text = String.empty()
                e.printStackTrace()
            }
            return this
        }

        fun inView(inView: View?): Builder {
            this.inView = inView
            return this
        }

        fun button(text: String, click: () -> Unit): Builder {
            this.buttonText = text
            this.click = click
            return this
        }

        fun show() {
            NToast(
                    act = act,
                    type = snackBarType,
                    duration = snackBarDuration,
                    text = text,
                    buttonText = buttonText,
                    click = click,
                    inView = this.inView,
                    dismissListener = dismissListener
            )
        }
    }


    companion object {

        fun with(activity: BaseAct?): Builder {
            return Builder(activity)
        }

        fun with(view: View?): Builder {
            return if (view?.context is BaseAct) {
                val activity = view.context as BaseAct
                Builder(activity)
            } else {
                Builder(null)
            }
        }

        fun showError(view: View?, text: String) {
            with(view)
                .text(text)
                .show()
        }

        fun showSuccess(view: View?, text: String) {
            with(view)
                .typeSuccess()
                .text(text)
                .show()
        }

        const val SNACKBAR_TYPE_ERROR = 1
        const val SNACKBAR_TYPE_ALERT = 2
        const val SNACKBAR_TYPE_SUCCESS = 3
    }


}
