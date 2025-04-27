package com.numplates.nomera3.presentation.view.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.meera.core.extensions.applyRoundedOutlineStoke
import com.numplates.nomera3.R
import com.meera.core.extensions.visible
import com.meera.core.extensions.gone
import timber.log.Timber
import java.lang.Exception

class CustomRowSelector: ConstraintLayout {

    private var vgRowRoot: ConstraintLayout? = null
    private lateinit var tvHeader: TextView
    private var viewHeaderDivider: View? = null

    private lateinit var tvMenu1: TextView
    private lateinit var tvMenu2: TextView
    private lateinit var tvMenu3: TextView

    private lateinit var ivSelected1: ImageView
    private lateinit var ivSelected2: ImageView
    private lateinit var ivSelected3: ImageView

    private var listener: OnRowClickedListener? = null
    private var models: List<CustomRowSelectorModel>? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init() {
        inflate(context, R.layout.layout_custom_row_selector, this)
        findView()
        setBackgroundRootRect()
        initClickListeners()
    }

    private fun initClickListeners() {
        tvMenu1.setOnClickListener {
            showChecked(0)
            models?.get(0)?.let {
                listener?.onRowClicked(it)
            }
        }

        tvMenu2.setOnClickListener {
            showChecked(1)
            models?.get(1)?.let {
                listener?.onRowClicked(it)
            }
        }

        tvMenu3.setOnClickListener {
            showChecked(2)
            models?.get(2)?.let {
                listener?.onRowClicked(it)
            }
        }
    }

    private fun showChecked(checkedPos: Int){
        // checked can't be less then 1 and more then 3
        val checked = when {
            checkedPos > 2 -> 2
            checkedPos < 0 -> 0
            else -> checkedPos
        }

        when(checked){
            0 -> {
                ivSelected1.visible()
                ivSelected2.gone()
                ivSelected3.gone()
            }
            1 -> {
                ivSelected1.gone()
                ivSelected2.visible()
                ivSelected3.gone()
            }
            2 -> {
                ivSelected1.gone()
                ivSelected2.gone()
                ivSelected3.visible()
            }
        }
    }

    private fun findView() {
        tvMenu1 = findViewById(R.id.tv_menu_1)
        tvMenu2 = findViewById(R.id.tv_menu_2)
        tvMenu3 = findViewById(R.id.tv_menu_3)

        tvHeader = findViewById(R.id.tv_header_row_selector)
        viewHeaderDivider = findViewById(R.id.view_header_divider)
        vgRowRoot = findViewById(R.id.vg_row_root)

        ivSelected1 = findViewById(R.id.iv_menu_1)
        ivSelected2 = findViewById(R.id.iv_menu_2)
        ivSelected3 = findViewById(R.id.iv_menu_3)
    }

    fun setHeader(@StringRes res: Int){
        tvHeader.visible()
        viewHeaderDivider?.visible()
        tvHeader.text = tvHeader.context.getText(res)
    }


    fun setOnRowListener(listener: OnRowClickedListener){
        this.listener = listener
    }

    fun setModels(models: List<CustomRowSelectorModel>){
        this.models = models
        try {
            tvMenu1.text = models[0].rowName
            tvMenu2.text = models[1].rowName
            tvMenu3.text = models[2].rowName
        }catch (e: Exception){
            Timber.e(e)
        }
    }

    fun clearSelections() {
        ivSelected1.gone()
        ivSelected2.gone()
        ivSelected3.gone()
    }

    private fun setBackgroundRootRect() {
        vgRowRoot?.applyRoundedOutlineStoke(
            cornerRadiusRes = R.dimen.material12,
            strokeWidthRes = R.dimen.material1,
            strokeColor = R.color.colorGrayC7C7
        )
    }

    data class CustomRowSelectorModel(
           var selectorModelId: Int,
           var rowName: String
    )

    interface OnRowClickedListener{
        fun onRowClicked(model: CustomRowSelectorModel)
    }
}
