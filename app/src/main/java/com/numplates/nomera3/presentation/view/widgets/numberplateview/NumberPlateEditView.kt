package com.numplates.nomera3.presentation.view.widgets.numberplateview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.hideKeyboard
import com.meera.core.utils.graphics.NGraphics
import com.meera.db.models.userprofile.VehicleEntity
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.UserCardVehicleModel
import com.numplates.nomera3.data.network.Vehicle
import com.numplates.nomera3.data.network.core.INetworkValues
import com.numplates.nomera3.data.network.core.INetworkValues.ACCOUNT_TYPE_REGULAR
import com.numplates.nomera3.data.network.core.INetworkValues.ACCOUNT_TYPE_VIP
import com.numplates.nomera3.data.network.core.INetworkValues.ROAD_TO_ARMENIA
import com.numplates.nomera3.data.network.core.INetworkValues.ROAD_TO_BELORUS
import com.numplates.nomera3.data.network.core.INetworkValues.ROAD_TO_GEORGIA
import com.numplates.nomera3.data.network.core.INetworkValues.ROAD_TO_KAZAKHSTAN
import com.numplates.nomera3.data.network.core.INetworkValues.ROAD_TO_RUSSIA
import com.numplates.nomera3.data.network.core.INetworkValues.ROAD_TO_UKRAINE
import com.numplates.nomera3.presentation.view.widgets.numberplateview.maskformatter.MaskFormatter
import timber.log.Timber
import java.util.Locale

/**
 * Created by artem on 08.06.18
 */
class NumberPlateEditView : LinearLayout, INetworkValues {

    internal lateinit var view: View

    internal lateinit var alPlate: ViewGroup

    internal var etNum: AppCompatEditText? = null
    internal var etRegion: AppCompatEditText? = null
    internal var etSuffix: AppCompatEditText? = null
    internal var etPrefix: AppCompatEditText? = null
    internal var sep: TextView? = null
    internal var ivPlate: ImageView? = null
    internal var ivFlag: ImageView? = null
    private var plate: NumberPlateEnum? = null
    private var fullNumberString: String? = null
    private var brand: String? = null
    private var model: String? = null

    private var number: String? = null
    private var region: String? = null
    private var suffix: String? = null
    private var prefix: String? = null

    var readOnly = false

    constructor(context: Context) : super(context) {
        init(context, null)

    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val typedArray = context.theme.obtainStyledAttributes(
                attrs, R.styleable.NumberPlateEditView, 0, 0)
        try {
            readOnly = typedArray.getBoolean(
                    R.styleable.NumberPlateEditView_readOnly, readOnly)
        } finally {
            typedArray.recycle()
        }
    }

    private fun initLayout(numPlate: NumberPlateEnum?) {
        this.plate = numPlate

        if (::view.isInitialized)
            this.removeView(view)

        try {
            view = (context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                .inflate(plate?.resourceId ?: NumberPlateEnum.COMMON.resourceId, null)

            addView(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            alPlate = view.findViewById(R.id.alPlate)
            ivFlag = view.findViewById(R.id.iv_flag)
            etNum = plate?.etNumberId?.let { view.findViewById(it) }
            etRegion = plate?.etRegionId?.let { view.findViewById(it) }
            etSuffix = plate?.etSuffixId?.let { view.findViewById(it) }
            ivPlate = view.findViewById(R.id.ivPlate)
            etPrefix = plate?.etPrefixId?.let { view.findViewById(it) }
            sep = view.findViewById(R.id.sep)

            etNum?.isEnabled = !readOnly
            etRegion?.isEnabled = !readOnly
            etSuffix?.isEnabled = !readOnly
            etPrefix?.isEnabled = !readOnly

            if (ivFlag != null) plate?.flagId?.let { ivFlag?.setImageResource(it) }
            requestLayout()

            if (!readOnly) {
                etNum?.isSaveEnabled = false
                etRegion?.isSaveEnabled = false
                etSuffix?.isSaveEnabled = false
                etPrefix?.isSaveEnabled = false

                etNum?.setOnClickListener { etNum?.setText("") }
                etRegion?.setOnClickListener { etRegion?.setText("") }
                etSuffix?.setOnClickListener { etSuffix?.setText("") }
                etPrefix?.setOnClickListener { etPrefix?.setText("") }

                when (plate) {
                    NumberPlateEnum.RU_AUTO -> {
                        etNum?.addTextChangedListener(MaskFormatter(plate?.numPattern, etNum, plate?.regexPattern))
                        etRegion?.addTextChangedListener(MaskFormatter(plate?.regionPattern, etRegion, plate?.regexPattern))
                        etNum?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.numPattern != null && etNum?.length() == plate?.numPattern?.length) {
                                    etNum?.clearFocus()
                                    etRegion?.requestFocus()
                                }
                            }
                        })
                    }
                    NumberPlateEnum.RU_AUTO_GRAY-> {
                        etPrefix?.addTextChangedListener(MaskFormatter(plate?.prefixPattern, etPrefix, plate?.regexPattern))
                        etPrefix?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.prefixPattern != null && etPrefix?.length() == plate?.prefixPattern?.length) {
                                    etPrefix?.clearFocus()
                                    etNum?.requestFocus()
                                }
                            }
                        })
                        etNum?.addTextChangedListener(MaskFormatter(plate?.numPattern, etNum, plate?.regexPattern))
                        etNum?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.numPattern != null && etNum?.length() == plate?.numPattern?.length) {
                                    etNum?.clearFocus()
                                    etSuffix?.requestFocus()
                                }
                            }
                        })
                        etNum?.handleBackSpaceClickForEmptyString(etPrefix)
                        etSuffix?.addTextChangedListener(MaskFormatter(plate?.suffixPattern, etSuffix, plate?.regexPattern))
                        etSuffix?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.suffixPattern != null && etSuffix?.length() == plate?.suffixPattern?.length) {
                                    etSuffix?.clearFocus()
                                    etRegion?.requestFocus()
                                }
                            }
                        })
                        etSuffix?.handleBackSpaceClickForEmptyString(etNum)
                        etRegion?.addTextChangedListener(MaskFormatter(plate?.regionPattern, etRegion, plate?.regexPattern))
                        etRegion?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.regionPattern != null && etRegion?.length() == plate?.regionPattern?.length) {
                                    etRegion?.clearFocus()
                                    hideKeyboard()
                                }
                            }
                        })
                        etRegion?.handleBackSpaceClickForEmptyString(etSuffix)
                    }

                    NumberPlateEnum.RU_MOTO,
                    NumberPlateEnum.RU_MOTO_GRAY -> {
                        etNum?.addTextChangedListener(MaskFormatter(plate?.numPattern, etNum, plate?.regexPattern))
                        etNum?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.numPattern != null && etNum?.length() == plate?.numPattern?.length) {
                                    etNum?.clearFocus()
                                    etSuffix?.requestFocus()
                                }
                            }
                        })
                        etSuffix?.addTextChangedListener(MaskFormatter(plate?.suffixPattern, etSuffix, plate?.regexPattern))
                        etSuffix?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.numPattern != null && etSuffix?.length() == plate?.suffixPattern?.length) {
                                    etSuffix?.clearFocus()
                                    etRegion?.requestFocus()
                                }
                            }
                        })
                        etSuffix?.handleBackSpaceClickForEmptyString(etNum)

                        etRegion?.addTextChangedListener(MaskFormatter(plate?.regionPattern, etRegion, plate?.regexPattern))
                        etRegion?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.regionPattern != null && etRegion?.length() == plate?.regionPattern?.length) {
                                    etRegion?.clearFocus()
                                    hideKeyboard()
                                }
                            }
                        })
                        etRegion?.handleBackSpaceClickForEmptyString(etSuffix)
                    }

                    NumberPlateEnum.UA_AUTO -> {
                        etNum?.addTextChangedListener(MaskFormatter(plate?.numPattern, etNum, plate?.regexPattern))
                    }
                    NumberPlateEnum.UA_AUTO_GRAY -> {
                        etNum?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.numPattern != null && etNum?.length() == plate?.numPattern?.length) {
                                    etNum?.clearFocus()
                                    etSuffix?.requestFocus()
                                }
                            }
                        })
                        etNum?.handleBackSpaceClickForEmptyString(etPrefix)
                        etSuffix?.addTextChangedListener(MaskFormatter(plate?.suffixPattern, etSuffix, plate?.regexPattern))
                        etPrefix?.addTextChangedListener(MaskFormatter(plate?.prefixPattern, etPrefix, plate?.regexPattern))
                        etPrefix?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.numPattern != null && etPrefix?.length() == plate?.prefixPattern?.length) {
                                    etPrefix?.clearFocus()
                                    etNum?.requestFocus()
                                    etNum?.requestFocus()
                                }
                            }
                        })
                        etSuffix?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.suffixPattern != null && etSuffix?.length() == plate?.suffixPattern?.length) {
                                    etSuffix?.clearFocus()
                                    hideKeyboard()
                                }
                            }
                        })
                        etSuffix?.handleBackSpaceClickForEmptyString(etNum)
                    }

                    NumberPlateEnum.UA_MOTO,
                    NumberPlateEnum.UA_MOTO_GRAY -> { etNum?.addTextChangedListener(MaskFormatter(plate?.numPattern, etNum, plate?.regexPattern))
                        etNum?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.numPattern != null && etNum?.length() == plate?.numPattern?.length) {
                                    etNum?.clearFocus()
                                    etSuffix?.requestFocus()
                                }
                            }
                        })
                        etNum?.handleBackSpaceClickForEmptyString(etPrefix)
                        etSuffix?.addTextChangedListener(MaskFormatter(plate?.suffixPattern, etSuffix, plate?.regexPattern))
                        etPrefix?.addTextChangedListener(MaskFormatter(plate?.prefixPattern, etPrefix, plate?.regexPattern))
                        etPrefix?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.numPattern != null && etPrefix?.length() == plate?.prefixPattern?.length) {
                                    etPrefix?.clearFocus()
                                    etNum?.requestFocus()
                                    etNum?.requestFocus()
                                }
                            }
                        })
                        etSuffix?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.suffixPattern != null && etSuffix?.length() == plate?.suffixPattern?.length) {
                                    etSuffix?.clearFocus()
                                    hideKeyboard()
                                }
                            }
                        })
                        etSuffix?.handleBackSpaceClickForEmptyString(etNum)
                    }

                    NumberPlateEnum.GE_AUTO -> {
                        etNum?.addTextChangedListener(MaskFormatter(plate?.numPattern, etNum, plate?.regexPattern))
                    }
                    NumberPlateEnum.GE_AUTO_GRAY -> {
                        etPrefix?.addTextChangedListener(MaskFormatter(plate?.prefixPattern, etPrefix, plate?.regexPattern))
                        etPrefix?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.prefixPattern != null && etPrefix?.length() == plate?.prefixPattern?.length) {
                                    etPrefix?.clearFocus()
                                    etNum?.requestFocus()
                                }
                            }
                        })
                        etNum?.addTextChangedListener(MaskFormatter(plate?.numPattern, etNum, plate?.regexPattern))
                        etNum?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.numPattern != null && etNum?.length() == plate?.numPattern?.length) {
                                    etNum?.clearFocus()
                                    etSuffix?.requestFocus()
                                }
                            }
                        })
                        etNum?.handleBackSpaceClickForEmptyString(etPrefix)
                        etSuffix?.addTextChangedListener(MaskFormatter(plate?.suffixPattern, etSuffix, plate?.regexPattern))
                        etSuffix?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.suffixPattern != null && etSuffix?.length() == plate?.suffixPattern?.length) {
                                    etSuffix?.clearFocus()
                                    hideKeyboard()
                                }
                            }
                        })
                        etSuffix?.handleBackSpaceClickForEmptyString(etNum)

                    }
                    NumberPlateEnum.GE_MOTO,
                    NumberPlateEnum.GE_MOTO_GRAY -> {
                        etNum?.addTextChangedListener(MaskFormatter(plate?.numPattern, etNum, plate?.regexPattern))
                        etNum?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.numPattern != null && etNum?.length() == plate?.numPattern?.length) {
                                    etNum?.clearFocus()
                                    hideKeyboard()
                                }
                            }
                        })
                        etSuffix?.addTextChangedListener(MaskFormatter(plate?.suffixPattern, etSuffix, plate?.regexPattern))
                        etSuffix?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.suffixPattern != null && etSuffix?.length() == plate?.suffixPattern?.length) {
                                    etSuffix?.clearFocus()
                                    etNum?.requestFocus()
                                }
                            }
                        })
                        etNum?.handleBackSpaceClickForEmptyString(etSuffix)
                    }

                    NumberPlateEnum.KZ_AUTO,
                    NumberPlateEnum.KZ_AUTO_GRAY -> {
                        etNum?.addTextChangedListener(MaskFormatter(plate?.numPattern, etNum, plate?.regexPattern))
                        etNum?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.numPattern != null && etNum?.length() == plate?.numPattern?.length) {
                                    etNum?.clearFocus()
                                    etSuffix?.requestFocus()
                                }
                            }
                        })
                        etSuffix?.addTextChangedListener(MaskFormatter(plate?.suffixPattern, etSuffix, plate?.regexPattern))
                        etSuffix?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.numPattern != null && etSuffix?.length() == plate?.suffixPattern?.length) {
                                    etSuffix?.clearFocus()
                                    etRegion?.requestFocus()
                                }
                            }
                        })
                        etSuffix?.handleBackSpaceClickForEmptyString(etNum)
                        etRegion?.addTextChangedListener(MaskFormatter(plate?.regionPattern, etRegion, plate?.regexPattern))
                        etRegion?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.regionPattern != null && etRegion?.length() == plate?.regionPattern?.length) {
                                    etRegion?.clearFocus()
                                    hideKeyboard()
                                }
                            }
                        })
                        etRegion?.handleBackSpaceClickForEmptyString(etSuffix)
                    }
                    NumberPlateEnum.KZ_MOTO,
                    NumberPlateEnum.KZ_MOTO_GRAY -> {
                        etNum?.addTextChangedListener(MaskFormatter(plate?.numPattern, etNum, plate?.regexPattern))
                        etNum?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.numPattern != null && etNum?.length() == plate?.numPattern?.length) {
                                    etNum?.clearFocus()
                                    etSuffix?.requestFocus()
                                }
                            }
                        })
                        etNum?.handleBackSpaceClickForEmptyString(etRegion)
                        etSuffix?.addTextChangedListener(MaskFormatter(plate?.suffixPattern, etSuffix, plate?.regexPattern))
                        etSuffix?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.numPattern != null && etSuffix?.length() == plate?.suffixPattern?.length) {
                                    etSuffix?.clearFocus()
                                    hideKeyboard()
                                }
                            }
                        })
                        etSuffix?.handleBackSpaceClickForEmptyString(etNum)
                        etRegion?.addTextChangedListener(MaskFormatter(plate?.regionPattern, etRegion, plate?.regexPattern))
                        etRegion?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.numPattern != null && etRegion?.length() == plate?.regionPattern?.length) {
                                    etRegion?.clearFocus()
                                    etNum?.requestFocus()
                                }
                            }
                        })
                    }

                    NumberPlateEnum.AM_AUTO -> {
                        etNum?.addTextChangedListener(MaskFormatter(plate?.numPattern, etNum, plate?.regexPattern))
                    }
                    NumberPlateEnum.AM_AUTO_GRAY -> {
                        etRegion?.addTextChangedListener(MaskFormatter(plate?.regionPattern, etRegion, plate?.regexPattern))
                        etRegion?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.regionPattern != null && etRegion?.length() == plate?.regionPattern?.length) {
                                    etRegion?.clearFocus()
                                    etSuffix?.requestFocus()
                                }
                            }
                        })
                        etSuffix?.addTextChangedListener(MaskFormatter(plate?.suffixPattern, etSuffix, plate?.regexPattern))
                        etSuffix?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.suffixPattern != null && etSuffix?.length() == plate?.suffixPattern?.length) {
                                    etSuffix?.clearFocus()
                                    etNum?.requestFocus()
                                }
                            }
                        })
                        etSuffix?.handleBackSpaceClickForEmptyString(etRegion)
                        etNum?.addTextChangedListener(MaskFormatter(plate?.numPattern, etNum, plate?.regexPattern))
                        etNum?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.numPattern != null && etNum?.length() == plate?.numPattern?.length) {
                                    etNum?.clearFocus()
                                    hideKeyboard()
                                }
                            }
                        })
                        etNum?.handleBackSpaceClickForEmptyString(etSuffix)
                    }
                    NumberPlateEnum.AM_MOTO,
                    NumberPlateEnum.AM_MOTO_GRAY -> {
                        etRegion?.addTextChangedListener(MaskFormatter(plate?.regionPattern, etRegion, plate?.regexPattern))
                        etRegion?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.regionPattern != null && etRegion?.length() == plate?.regionPattern?.length) {
                                    etRegion?.clearFocus()
                                    etSuffix?.requestFocus()
                                }
                            }
                        })
                        etSuffix?.addTextChangedListener(MaskFormatter(plate?.suffixPattern, etSuffix, plate?.regexPattern))
                        etSuffix?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.suffixPattern != null && etSuffix?.length() == plate?.suffixPattern?.length) {
                                    etSuffix?.clearFocus()
                                    etNum?.requestFocus()
                                }
                            }
                        })
                        etSuffix?.handleBackSpaceClickForEmptyString(etRegion)
                        etNum?.addTextChangedListener(MaskFormatter(plate?.numPattern, etNum, plate?.regexPattern))
                        etNum?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.numPattern != null && etNum?.length() == plate?.numPattern?.length) {
                                    etNum?.clearFocus()
                                    hideKeyboard()
                                }
                            }
                        })
                        etNum?.handleBackSpaceClickForEmptyString(etSuffix)
                    }
                    NumberPlateEnum.BY_AUTO,
                    NumberPlateEnum.BY_AUTO_GRAY -> {
                        etNum?.addTextChangedListener(MaskFormatter(plate?.numPattern, etNum, plate?.regexPattern))
                        etNum?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.numPattern != null && etNum?.length() == plate?.numPattern?.length) {
                                    etNum?.clearFocus()
                                    etSuffix?.requestFocus()
                                }
                            }
                        })
                        etSuffix?.addTextChangedListener(MaskFormatter(plate?.suffixPattern, etSuffix, plate?.regexPattern))
                        etSuffix?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.numPattern != null && etSuffix?.length() == plate?.suffixPattern?.length) {
                                    etSuffix?.clearFocus()
                                    etRegion?.requestFocus()
                                }
                            }
                        })
                        etSuffix?.handleBackSpaceClickForEmptyString(etNum)
                        etRegion?.addTextChangedListener(MaskFormatter(plate?.regionPattern, etRegion, plate?.regexPattern))
                        etRegion?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.regionPattern != null && etRegion?.length() == plate?.regionPattern?.length) {
                                    etRegion?.clearFocus()
                                    hideKeyboard()
                                }
                            }
                        })
                        etRegion?.handleBackSpaceClickForEmptyString(etSuffix)
                    }
                    NumberPlateEnum.BY_MOTO,
                    NumberPlateEnum.BY_MOTO_GRAY -> {
                        etNum?.addTextChangedListener(MaskFormatter(plate?.numPattern, etNum, plate?.regexPattern))
                        etNum?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.numPattern != null && etNum?.length() == plate?.numPattern?.length) {
                                    etNum?.clearFocus()
                                    etSuffix?.requestFocus()
                                }
                            }
                        })
                        etSuffix?.addTextChangedListener(MaskFormatter(plate?.suffixPattern, etSuffix, plate?.regexPattern))
                        etSuffix?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.numPattern != null && etSuffix?.length() == plate?.suffixPattern?.length) {
                                    etSuffix?.clearFocus()
                                    etRegion?.requestFocus()
                                }
                            }
                        })
                        etSuffix?.handleBackSpaceClickForEmptyString(etNum)
                        etRegion?.addTextChangedListener(MaskFormatter(plate?.regionPattern, etRegion, plate?.regexPattern))
                        etRegion?.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.regionPattern != null && etRegion?.length() == plate?.regionPattern?.length) {
                                    etRegion?.clearFocus()
                                    hideKeyboard()
                                }
                            }
                        })
                        etRegion?.handleBackSpaceClickForEmptyString(etSuffix)
                    }

                    NumberPlateEnum.PEDESTRIAN -> {
                        etNum!!.addTextChangedListener(MaskFormatter(plate!!.numPattern, etNum, plate!!.regexPattern))
                        etNum!!.hint = plate!!.numPattern
                    }//                    etRegion.setEnabled(false);
                    //                    etRegion.setText(R.string.by);
                    NumberPlateEnum.COMMON -> {
                        etNum!!.addTextChangedListener(MaskFormatter(plate!!.numPattern, etNum, plate!!.regexPattern))
                        etNum!!.hint = plate!!.numPattern
                        etNum!!.addTextChangedListener(object : Watcher() {
                            override fun onChar(lenght: Int) {
                                if (plate?.numPattern != null && lenght == plate!!.numPattern.length) {
                                    etNum!!.clearFocus()
                                    etPrefix!!.requestFocus()
                                }
                            }
                        })
                        etPrefix!!.addTextChangedListener(MaskFormatter(plate!!.prefixPattern, etPrefix, plate!!.regexPattern))
                        etPrefix!!.hint = plate!!.prefixPattern
                    }//                    etRegion.setText(R.string.by);
                    //                    etRegion.setEnabled(false);
                    else -> {}
                }
            }

        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun validate(): Boolean {
        if (plate != null) {    //TODO условие временно, до тех пор пока не будет обёртка не авто и мото номеров
            var patternLenght = 0

            if (plate?.numPattern != null) {
                patternLenght += plate!!.numPattern.length
            }
            if (plate?.prefixPattern != null) {
                patternLenght += plate!!.prefixPattern!!.length
            }
            if (plate?.suffixPattern != null) {
                patternLenght += plate!!.suffixPattern!!.length
            }
            if (plate?.regionPattern != null) {
                patternLenght += plate!!.regionPattern!!.length
            }
            //        int patternLenght = (plate.numPattern + plate.prefixPattern + plate.suffixPattern + plate.regionPattern).length();
            // По доке, только для российских номеров можно заполнять код региона между 2-3 символами,
            // а для других стран все поля должны быть заполнены
            if (plate == NumberPlateEnum.RU_AUTO) {
                val fullNumber  = getFullNumberString()
                return fullNumber != null && fullNumber.length >= patternLenght - 1
            }
            return getFullNumberString() != null && getFullNumberString()!!.length >= patternLenght
        }
        return false
    }

    /**
     * Notify validateCallback every time when numberPlate changed
     * */
    fun addOnValidateListener(validateCallback: () -> Unit){
        val listener = object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                Timber.d(s.toString())
                validateCallback.invoke()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        }
        etNum?.addTextChangedListener(listener)
        etRegion?.addTextChangedListener(listener)
        etSuffix?.addTextChangedListener(listener)
        etPrefix?.addTextChangedListener(listener)

    }

    fun bitmap(): Bitmap? {
        if (validate()) {
            clearFocus()
            etNum?.isCursorVisible = true
            if (etPrefix != null) {
                etPrefix?.isCursorVisible = true
            }
            etSuffix?.isCursorVisible = true
            etRegion?.isCursorVisible = true
            return NGraphics.getBitmapFromView(alPlate)

        }
        return null
    }


    @SuppressLint("DefaultLocale")
    private fun initNumber(builder: Builder) {

        initLayout(builder.plate)

        if (builder.number == null) {
            builder.number = ""
        }

        this.fullNumberString = builder.number
        this.brand = builder.brand
        this.model = builder.model

        val length = fullNumberString!!.length

        when (plate) {
            NumberPlateEnum.RU_AUTO,
            NumberPlateEnum.RU_AUTO_GRAY -> {
                val d6 = if (length > 6) 6 else length
                val d9 = if (length > 9) 9 else length
                number = fullNumberString?.substring(0, d6)
                region = fullNumberString?.substring(d6, d9)
            }
            NumberPlateEnum.RU_MOTO,
            NumberPlateEnum.RU_MOTO_GRAY -> {
                val d4 = if (length > 4) 4 else length
                val d6 = if (length > 6) 6 else length
                val d9 = if (length > 9) 9 else length
                number = fullNumberString!!.substring(0, d4)
                suffix = fullNumberString!!.substring(d4, d6)
                region = fullNumberString!!.substring(d6, d9)
            }
            NumberPlateEnum.UA_AUTO,
            NumberPlateEnum.UA_AUTO_GRAY -> {
                val d8 = if (length > 8) 8 else length
                number = fullNumberString!!.substring(0, d8)
                suffix = null
            }
            NumberPlateEnum.UA_MOTO,
            NumberPlateEnum.UA_MOTO_GRAY -> {
                val d2 = if (length > 2) 2 else length
                val d6 = if (length > 6) 6 else length
                val d8 = if (length > 8) 8 else length
                val d10 = if (length > 10) 10 else length
                prefix = fullNumberString!!.substring(0, d2)
                number = fullNumberString!!.substring(d2, d6)
                suffix = fullNumberString!!.substring(d6, d8)
                region = fullNumberString!!.substring(d8, d10)
            }
            NumberPlateEnum.BY_AUTO,
            NumberPlateEnum.BY_AUTO_GRAY -> {
                val d4 = if (length > 4) 4 else length
                val d6 = if (length > 6) 6 else length
                val d7 = if (length > 7) 7 else length
                number = fullNumberString!!.substring(0, d4)
                suffix = fullNumberString!!.substring(d4, d6)
                region = fullNumberString!!.substring(d6, d7)
            }
            NumberPlateEnum.BY_MOTO,
            NumberPlateEnum.BY_MOTO_GRAY -> {
                val d4 = if (length > 4) 4 else length
                val d6 = if (length > 6) 6 else length
                val d7 = if (length > 7) 7 else length
                number = fullNumberString!!.substring(0, d4)
                suffix = fullNumberString!!.substring(d4, d6)
                region = fullNumberString!!.substring(d6, d7)
            }
            NumberPlateEnum.GE_AUTO,
            NumberPlateEnum.GE_AUTO_GRAY -> {
                val d7 = if (length > 7) 7 else length
                number = fullNumberString!!.substring(0, d7)
            }
            NumberPlateEnum.GE_MOTO,
            NumberPlateEnum.GE_MOTO_GRAY -> {
                val d2 = if (length > 2) 2 else length
                val d6 = if (length > 6) 6 else length
                suffix = fullNumberString!!.substring(0, d2)
                suffix = fullNumberString!!.substring(0, d2)
                number = fullNumberString!!.substring(d2, d6)
            }
            NumberPlateEnum.KZ_AUTO,
            NumberPlateEnum.KZ_AUTO_GRAY -> {
                val d3 = if (length > 3) 3 else length
                val d6 = if (length > 6) 6 else length
                val d8 = if (length > 8) 8 else length
                number = fullNumberString!!.substring(0, d3)
                suffix = fullNumberString!!.substring(d3, d6)
                region = fullNumberString!!.substring(d6, d8)
            }
            NumberPlateEnum.KZ_MOTO,
            NumberPlateEnum.KZ_MOTO_GRAY -> {
                val d2 = if (length > 2) 2 else length
                val d4 = if (length > 4) 4 else length
                val d6 = if (length > 6) 6 else length
                number = fullNumberString!!.substring(0, d2)
                suffix = fullNumberString!!.substring(d2, d4)
                region = fullNumberString!!.substring(d4, d6)
            }
            NumberPlateEnum.AM_AUTO,
            NumberPlateEnum.AM_AUTO_GRAY -> {
                val d7 = if (length > 7) 7 else length
                number = fullNumberString!!.substring(0, d7)
            }
            NumberPlateEnum.AM_MOTO,
            NumberPlateEnum.AM_MOTO_GRAY -> {
                val d2 = if (length > 2) 2 else length
                val d4 = if (length > 4) 4 else length
                val d7 = if (length > 7) 7 else length
                region = fullNumberString!!.substring(0, d2)
                suffix = fullNumberString!!.substring(d2, d4)
                number = fullNumberString!!.substring(d4, d7)
            }
            NumberPlateEnum.PEDESTRIAN -> {
                number = context!!.resources.getString(R.string.pedestrian)
            }
            NumberPlateEnum.COMMON -> {
                number = context!!.resources.getString(R.string.pedestrian)
            }
            else -> {}
        }
        if (plate != NumberPlateEnum.PEDESTRIAN && plate != NumberPlateEnum.COMMON) {
            etNum?.setText(number?.uppercase(Locale.getDefault()))
            etSuffix?.setText(suffix?.uppercase(Locale.getDefault()))
            etPrefix?.setText(prefix?.uppercase(Locale.getDefault()))
            etRegion?.setText(region?.uppercase(Locale.getDefault()))
        } else {
            etNum?.setText(number)
        }
    }

    fun initNumber() {

        initLayout(plate)

        if (number == null) {
            number = ""
        }

        this.fullNumberString = number

        val length = fullNumberString!!.length

        when (plate) {
            NumberPlateEnum.RU_AUTO -> {
                val d6 = if (length > 6) 6 else length
                val d9 = if (length > 9) 9 else length
                number = fullNumberString?.substring(0, d6)
                region = fullNumberString?.substring(d6, d9)
            }
            NumberPlateEnum.RU_MOTO -> {
                val d4 = if (length > 4) 4 else length
                val d6 = if (length > 6) 6 else length
                val d9 = if (length > 9) 9 else length
                number = fullNumberString!!.substring(0, d4)
                suffix = fullNumberString!!.substring(d4, d6)
                region = fullNumberString!!.substring(d6, d9)
            }
            NumberPlateEnum.UA_AUTO -> {
                val d8 = if (length > 8) 8 else length
                number = fullNumberString!!.substring(0, d8)
                suffix = null
            }
            NumberPlateEnum.UA_MOTO -> {
                val d2 = if (length > 2) 2 else length
                val d6 = if (length > 6) 6 else length
                val d8 = if (length > 8) 8 else length
                val d10 = if (length > 10) 10 else length
                prefix = fullNumberString!!.substring(0, d2)
                number = fullNumberString!!.substring(d2, d6)
                suffix = fullNumberString!!.substring(d6, d8)
                region = fullNumberString!!.substring(d8, d10)
            }
            NumberPlateEnum.BY_AUTO -> {
                val d4 = if (length > 4) 4 else length
                val d6 = if (length > 6) 6 else length
                val d7 = if (length > 7) 7 else length
                number = fullNumberString!!.substring(0, d4)
                suffix = fullNumberString!!.substring(d4, d6)
                region = fullNumberString!!.substring(d6, d7)
            }
            NumberPlateEnum.BY_MOTO -> {
                val d4 = if (length > 4) 4 else length
                val d6 = if (length > 6) 6 else length
                val d7 = if (length > 7) 7 else length
                number = fullNumberString!!.substring(0, d4)
                suffix = fullNumberString!!.substring(d4, d6)
                region = fullNumberString!!.substring(d6, d7)
            }
            NumberPlateEnum.GE_AUTO -> {
                val d7 = if (length > 7) 7 else length
                number = fullNumberString!!.substring(0, d7)
            }
            NumberPlateEnum.GE_MOTO -> {
                val d2 = if (length > 2) 2 else length
                val d6 = if (length > 6) 6 else length
                suffix = fullNumberString!!.substring(0, d2)
                number = fullNumberString!!.substring(d2, d6)
            }
            NumberPlateEnum.KZ_AUTO -> {
                val d3 = if (length > 3) 3 else length
                val d6 = if (length > 6) 6 else length
                val d8 = if (length > 8) 8 else length
                number = fullNumberString!!.substring(0, d3)
                suffix = fullNumberString!!.substring(d3, d6)
                region = fullNumberString!!.substring(d6, d8)
            }
            NumberPlateEnum.KZ_MOTO -> {
                val d2 = if (length > 2) 2 else length
                val d4 = if (length > 4) 4 else length
                val d6 = if (length > 6) 6 else length
                number = fullNumberString!!.substring(0, d2)
                suffix = fullNumberString!!.substring(d2, d4)
                region = fullNumberString!!.substring(d4, d6)
            }
            NumberPlateEnum.AM_AUTO -> {
                val d7 = if (length > 7) 7 else length
                number = fullNumberString!!.substring(0, d7)
            }
            NumberPlateEnum.AM_MOTO -> {
                val d2 = if (length > 2) 2 else length
                val d4 = if (length > 4) 4 else length
                val d7 = if (length > 7) 7 else length
                region = fullNumberString!!.substring(0, d2)
                suffix = fullNumberString!!.substring(d2, d4)
                number = fullNumberString!!.substring(d4, d7)
            }
            NumberPlateEnum.PEDESTRIAN -> {
                number = context!!.resources.getString(R.string.pedestrian)
            }
            NumberPlateEnum.COMMON -> {
                number = context!!.resources.getString(R.string.pedestrian)
            }
            else -> {}
        }
        if (plate != NumberPlateEnum.PEDESTRIAN && plate != NumberPlateEnum.COMMON) {
            etNum?.setText(number?.uppercase(Locale.getDefault()))
            etSuffix?.setText(suffix?.uppercase(Locale.getDefault()))
            etPrefix?.setText(prefix?.uppercase(Locale.getDefault()))
            etRegion?.setText(region?.uppercase(Locale.getDefault()))
        } else {
            etNum?.setText(number)
        }

        Timber.i(" NUMBER_VEHICLE: init number done  number: ${etNum?.text}")
    }

    fun getFullNumberString(): String? {
        if (plate == null) {
            return null
        }
        try {
            number = etNum?.text.toString().replace(" ", "")
            region = etRegion?.text.toString().replace(" ", "")
            suffix = etSuffix?.text.toString().replace(" ", "")
        } catch (ignored: NullPointerException) {
        }

        if (etPrefix != null) {
            prefix = etPrefix?.text.toString().replace(" ", "")
        } else {
            prefix = ""
        }

        when (plate) {
            NumberPlateEnum.RU_AUTO -> {
                fullNumberString = number!! + region!!
            }
            NumberPlateEnum.RU_AUTO_GRAY -> {
                fullNumberString = prefix + number + suffix + region
            }
            NumberPlateEnum.RU_MOTO,
            NumberPlateEnum.RU_MOTO_GRAY -> {
                fullNumberString = number + suffix + region
            }

            NumberPlateEnum.UA_AUTO -> {
                fullNumberString = number
            }
            NumberPlateEnum.UA_AUTO_GRAY -> {
                fullNumberString = prefix + number + suffix
            }

            NumberPlateEnum.UA_MOTO,
            NumberPlateEnum.UA_MOTO_GRAY -> {
                fullNumberString = prefix + number + suffix
            }

            NumberPlateEnum.BY_AUTO,
            NumberPlateEnum.BY_AUTO_GRAY -> {
                fullNumberString = number + suffix + region
            }

            NumberPlateEnum.BY_MOTO,
            NumberPlateEnum.BY_MOTO_GRAY -> {
                fullNumberString = number + suffix + region
            }

            NumberPlateEnum.GE_AUTO -> {
                fullNumberString = number
            }
            NumberPlateEnum.GE_AUTO_GRAY -> {
                fullNumberString = prefix + number + suffix
            }

            NumberPlateEnum.GE_MOTO,
            NumberPlateEnum.GE_MOTO_GRAY -> {
                fullNumberString = suffix + number
            }

            NumberPlateEnum.KZ_AUTO,
            NumberPlateEnum.KZ_AUTO_GRAY -> {
                fullNumberString = number + suffix + region
            }

            NumberPlateEnum.KZ_MOTO,
            NumberPlateEnum.KZ_MOTO_GRAY -> {
                fullNumberString = number + suffix + region
            }

            NumberPlateEnum.AM_AUTO -> {
                fullNumberString = number
            }
            NumberPlateEnum.AM_AUTO_GRAY -> {
                fullNumberString = region + suffix + number
            }

            NumberPlateEnum.AM_MOTO,
            NumberPlateEnum.AM_MOTO_GRAY -> {
                fullNumberString = region + suffix + number
            }

            NumberPlateEnum.PEDESTRIAN -> {
                fullNumberString = number
            }
            NumberPlateEnum.COMMON -> {
                fullNumberString = brand!! + model!!
                this.brand = brand
                model = model
            }
            else -> {}
        }
        return fullNumberString

    }


    fun setType(plate: NumberPlateEnum) {
        this.plate = plate
    }


    fun setVehicle(vehicle: Vehicle) {
        this.number = vehicle.number
        if (vehicle.type == null || vehicle.country == null) {
            return
        }
        when (vehicle.country!!.countryId!!.toString()) {
            //россия
            ROAD_TO_RUSSIA.toString() -> {
                if ("1" == vehicle.type!!.typeId) {
                    plate = NumberPlateEnum.RU_AUTO
                } else if ("2" == vehicle.type!!.typeId) {
                    plate = NumberPlateEnum.RU_MOTO
                }
            }
            //украина
            ROAD_TO_UKRAINE.toString() -> {
                if ("1" == vehicle.type!!.typeId) {
                    plate = NumberPlateEnum.UA_AUTO
                } else if ("2" == vehicle.type!!.typeId) {
                    plate = NumberPlateEnum.UA_MOTO
                }
            }
            //беларусь
            ROAD_TO_BELORUS.toString() -> {
                if ("1" == vehicle.type!!.typeId) {
                    plate = NumberPlateEnum.BY_AUTO
                } else if ("2" == vehicle.type!!.typeId) {
                    plate = NumberPlateEnum.BY_MOTO
                }
            }
            //грузия
            ROAD_TO_GEORGIA.toString() -> {
                if ("1" == vehicle.type!!.typeId) {
                    plate = NumberPlateEnum.GE_AUTO
                } else if ("2" == vehicle.type!!.typeId) {
                    plate = NumberPlateEnum.GE_MOTO
                }
            }
            //Казахстан
            ROAD_TO_KAZAKHSTAN.toString() -> {
                if ("1" == vehicle.type!!.typeId) {
                    plate = NumberPlateEnum.KZ_AUTO
                } else if ("2" == vehicle.type!!.typeId) {
                    plate = NumberPlateEnum.KZ_MOTO
                }
            }
            //армения
            ROAD_TO_ARMENIA.toString() -> {
                if ("1" == vehicle.type!!.typeId) {
                    plate = NumberPlateEnum.AM_AUTO
                } else if ("2" == vehicle.type!!.typeId) {
                    plate = NumberPlateEnum.AM_MOTO
                }
            }
        }
    }


    /**
     * New API
     * @param vehicle
     * @return
     */
    fun setVehicleNew(vehicle: VehicleEntity) {
        this.number = vehicle.number

        Timber.i("NUMBER: ${vehicle.number}")
        if (vehicle.type == null || vehicle.country == null) {
            return
        }
        val vehicleCountry = vehicle.country
        val vehicleType = vehicle.type
        when (vehicleCountry?.countryId) {
            //россия
            3159L -> {
                if (1 == vehicleType?.typeId) {
                    plate = NumberPlateEnum.RU_AUTO
                } else if (2 == vehicleType?.typeId) {
                    plate = NumberPlateEnum.RU_MOTO
                }
            }
            //украина
            9908L -> {
                if (1 == vehicleType?.typeId) {
                    plate = NumberPlateEnum.UA_AUTO
                } else if (2 == vehicleType?.typeId) {
                    plate = NumberPlateEnum.UA_MOTO
                }
            }
            //беларусь
            248L -> {
                if (1 == vehicleType?.typeId) {
                    plate = NumberPlateEnum.BY_AUTO
                } else if (2 == vehicleType?.typeId) {
                    plate = NumberPlateEnum.BY_MOTO
                }
            }
            //грузия
            1280L -> {
                if (1 == vehicleType?.typeId) {
                    plate = NumberPlateEnum.GE_AUTO
                } else if (2 == vehicleType?.typeId) {
                    plate = NumberPlateEnum.GE_MOTO
                }
            }
            //Казахстан
            1894L -> {
                if (1 == vehicleType?.typeId) {
                    plate = NumberPlateEnum.KZ_AUTO
                } else if (2 == vehicleType?.typeId) {
                    plate = NumberPlateEnum.KZ_MOTO
                }
            }
            //армения
            245L -> {
                if (1 == vehicleType?.typeId) {
                    plate = NumberPlateEnum.AM_AUTO
                } else if (2 == vehicleType?.typeId) {
                    plate = NumberPlateEnum.AM_MOTO
                }
            }
        }
    }

    fun setCommon(typeId: String, brand: String, model: String) {

        if (typeId == "0") {
            this.number = context.resources.getString(R.string.pedestrian)
            plate = NumberPlateEnum.PEDESTRIAN
            Timber.i("PEDESTRIAN")

        } else {
            plate = NumberPlateEnum.COMMON

            this.brand = brand
            this.model = model

        }
    }


    fun setNum(num: String) {
        this.number = num
    }



    class Builder(private val view: NumberPlateEditView) {
        internal var enabled: Boolean = false
        var number: String? = null
        var brand: String? = null
        var model: String? = null
        var plate: NumberPlateEnum? = null

        fun setType(plate: NumberPlateEnum): Builder {
            this.plate = plate
            return this
        }


        fun setVehicle(vehicle: Vehicle): Builder {
            this.number = vehicle.number
            if (vehicle.type == null || vehicle.country == null) {
                return this
            }
            when (vehicle.country?.countryId.toString()) {
                //россия
                ROAD_TO_RUSSIA.toString() -> {
                    if ("1" == vehicle.type?.typeId) {
                        plate = NumberPlateEnum.RU_AUTO
                    } else if ("2" == vehicle.type?.typeId) {
                        plate = NumberPlateEnum.RU_MOTO
                    }
                }
                //украина
                ROAD_TO_UKRAINE.toString() -> {
                    if ("1" == vehicle.type!!.typeId) {
                        plate = NumberPlateEnum.UA_AUTO
                    } else if ("2" == vehicle.type!!.typeId) {
                        plate = NumberPlateEnum.UA_MOTO
                    }
                }
                //беларусь
                ROAD_TO_BELORUS.toString() -> {
                    if ("1" == vehicle.type!!.typeId) {
                        plate = NumberPlateEnum.BY_AUTO
                    } else if ("2" == vehicle.type!!.typeId) {
                        plate = NumberPlateEnum.BY_MOTO
                    }
                }
                //грузия
                ROAD_TO_GEORGIA.toString() -> {
                    if ("1" == vehicle.type!!.typeId) {
                        plate = NumberPlateEnum.GE_AUTO
                    } else if ("2" == vehicle.type!!.typeId) {
                        plate = NumberPlateEnum.GE_MOTO
                    }
                }
                //Казахстан
                ROAD_TO_KAZAKHSTAN.toString() -> {
                    if ("1" == vehicle.type!!.typeId) {
                        plate = NumberPlateEnum.KZ_AUTO
                    } else if ("2" == vehicle.type!!.typeId) {
                        plate = NumberPlateEnum.KZ_MOTO
                    }
                }
                //армения
                ROAD_TO_ARMENIA.toString() -> {
                    if ("1" == vehicle.type!!.typeId) {
                        plate = NumberPlateEnum.AM_AUTO
                    } else if ("2" == vehicle.type!!.typeId) {
                        plate = NumberPlateEnum.AM_MOTO
                    }
                }
            }
            return this
        }


        /**
         * New API
         * @param vehicle
         * @return
         */
        fun setVehicleNew(number: String?, countyId: Long?, typeId: Int?): Builder {
            this.number = number

            Timber.i("NUMBER: ${number}")
            if (typeId == null || countyId == null) {
                return this
            }

            when (countyId) {
                //россия
                3159L -> {
                    if (1 == typeId) {
                        plate = NumberPlateEnum.RU_AUTO
                    } else if (2 == typeId) {
                        plate = NumberPlateEnum.RU_MOTO
                    }
                }
                //украина
                9908L -> {
                    if (1 == typeId) {
                        plate = NumberPlateEnum.UA_AUTO
                    } else if (2 == typeId) {
                        plate = NumberPlateEnum.UA_MOTO
                    }
                }
                //беларусь
                248L -> {
                    if (1 == typeId) {
                        plate = NumberPlateEnum.BY_AUTO
                    } else if (2 == typeId) {
                        plate = NumberPlateEnum.BY_MOTO
                    }
                }
                //грузия
                1280L -> {
                    if (1 == typeId) {
                        plate = NumberPlateEnum.GE_AUTO
                    } else if (2 == typeId) {
                        plate = NumberPlateEnum.GE_MOTO
                    }
                }
                //Казахстан
                1894L -> {
                    if (1 == typeId) {
                        plate = NumberPlateEnum.KZ_AUTO
                    } else if (2 == typeId) {
                        plate = NumberPlateEnum.KZ_MOTO
                    }
                }
                //армения
                245L -> {
                    if (1 == typeId) {
                        plate = NumberPlateEnum.AM_AUTO
                    } else if (2 == typeId) {
                        plate = NumberPlateEnum.AM_MOTO
                    }
                }
            }
            return this
        }


        fun setVehicleNew(vehicle: UserCardVehicleModel): Builder {
            this.number = vehicle.number
            when (vehicle.countryId.toLong()) {
                //россия
                3159L -> {
                    if (1 == vehicle.type) {
                        plate = NumberPlateEnum.RU_AUTO
                    } else if (2 == vehicle.type) {
                        plate = NumberPlateEnum.RU_MOTO
                    }
                }
                //украина
                9908L -> {
                    if (1 == vehicle.type) {
                        plate = NumberPlateEnum.UA_AUTO
                    } else if (2 == vehicle.type) {
                        plate = NumberPlateEnum.UA_MOTO
                    }
                }
                //беларусь
                248L -> {
                    if (1 == vehicle.type) {
                        plate = NumberPlateEnum.BY_AUTO
                    } else if (2 == vehicle.type) {
                        plate = NumberPlateEnum.BY_MOTO
                    }
                }
                //грузия
                1280L -> {
                    if (1 == vehicle.type) {
                        plate = NumberPlateEnum.GE_AUTO
                    } else if (2 == vehicle.type) {
                        plate = NumberPlateEnum.GE_MOTO
                    }
                }
                //Казахстан
                1894L -> {
                    if (1 == vehicle.type) {
                        plate = NumberPlateEnum.KZ_AUTO
                    } else if (2 == vehicle.type) {
                        plate = NumberPlateEnum.KZ_MOTO
                    }
                }
                //армения
                245L -> {
                    if (1 == vehicle.type) {
                        plate = NumberPlateEnum.AM_AUTO
                    } else if (2 == vehicle.type) {
                        plate = NumberPlateEnum.AM_MOTO
                    }
                }
            }
            return this
        }

        fun setVehicleNewGray(vehicle: VehicleEntity): Builder {
            this.number = vehicle.number

            Timber.i("NUMBER: ${vehicle.number}")
            if (vehicle.type == null || vehicle.country == null) {
                return this
            }
            val vehicleCountry = vehicle.country
            val vehicleType = vehicle.type
            when (vehicleCountry?.countryId) {
                //россия
                3159L -> {
                    if (1 == vehicleType?.typeId) {
                        plate = NumberPlateEnum.RU_AUTO_GRAY
                    } else if (2 == vehicleType?.typeId) {
                        plate = NumberPlateEnum.RU_MOTO_GRAY
                    }
                }
                //украина
                9908L -> {
                    if (1 == vehicleType?.typeId) {
                        plate = NumberPlateEnum.UA_AUTO_GRAY
                    } else if (2 == vehicleType?.typeId) {
                        plate = NumberPlateEnum.UA_MOTO_GRAY
                    }
                }
                //беларусь
                248L -> {
                    if (1 == vehicleType?.typeId) {
                        plate = NumberPlateEnum.BY_AUTO_GRAY
                    } else if (2 == vehicleType?.typeId) {
                        plate = NumberPlateEnum.BY_MOTO_GRAY
                    }
                }
                //грузия
                1280L -> {
                    if (1 == vehicleType?.typeId) {
                        plate = NumberPlateEnum.GE_AUTO_GRAY
                    } else if (2 == vehicleType?.typeId) {
                        plate = NumberPlateEnum.GE_MOTO_GRAY
                    }
                }
                //Казахстан
                1894L -> {
                    if (1 == vehicleType?.typeId) {
                        plate = NumberPlateEnum.KZ_AUTO_GRAY
                    } else if (2 == vehicleType?.typeId) {
                        plate = NumberPlateEnum.KZ_MOTO_GRAY
                    }
                }
                //армения
                245L -> {
                    if (1 == vehicleType?.typeId) {
                        plate = NumberPlateEnum.AM_AUTO_GRAY
                    } else if (2 == vehicleType?.typeId) {
                        plate = NumberPlateEnum.AM_MOTO_GRAY
                    }
                }
            }
            return this
        }

        fun setNum(num: String): Builder {
            this.number = num
            return this
        }

        fun build() {
            view.initNumber(this)
        }
    }

    fun setBackgroundPlate(vehicle: UserCardVehicleModel, accountType: Int?, accountColor: Int?) {
        val backgroundPlate: Int = NumberPlateHelper.setBackground(
            vehicle.type.toString(),
            vehicle.countryId.toString(),
            accountType,
            accountColor)

        if (backgroundPlate != 0) {
            if (accountType != ACCOUNT_TYPE_REGULAR) {
                etNum?.setTextColor(ContextCompat.getColor(context, R.color.ui_white))
                etRegion?.setTextColor(ContextCompat.getColor(context, R.color.ui_white))
                etSuffix?.setTextColor(ContextCompat.getColor(context, R.color.ui_white))
                etPrefix?.setTextColor(ContextCompat.getColor(context, R.color.ui_white))
                sep?.setTextColor(ContextCompat.getColor(context, R.color.ui_white))
            }
            ivPlate?.setImageDrawable(ContextCompat.getDrawable(context, backgroundPlate))
        }
    }

    fun setBackgroundPlate(vehicle: Vehicle, accountType: Int?, accountColor: Int?) {
        val backgroundPlate: Int = NumberPlateHelper.setBackground(
                vehicle.type?.typeId,
                vehicle.country?.countryId.toString(),
                accountType,
                accountColor)

        if (backgroundPlate != 0) {
            if (accountType != ACCOUNT_TYPE_REGULAR) {
                etNum?.setTextColor(ContextCompat.getColor(context, R.color.ui_white))
                etRegion?.setTextColor(ContextCompat.getColor(context, R.color.ui_white))
                etSuffix?.setTextColor(ContextCompat.getColor(context, R.color.ui_white))
                etPrefix?.setTextColor(ContextCompat.getColor(context, R.color.ui_white))
                sep?.setTextColor(ContextCompat.getColor(context, R.color.ui_white))
            }
            ivPlate?.setImageDrawable(ContextCompat.getDrawable(context, backgroundPlate))
        }
    }

    fun setBackgroundPlate(
        typeId: Int?,
        countryId: Long?,
        accountType: Int?,
        accountColor: Int?
    ) {
        val backgroundPlate: Int = NumberPlateHelper.setBackground(
            typeId.toString(),
            countryId?.toString(),
            accountType,
            accountColor
        )
        if (backgroundPlate != 0) {
            if (accountType == ACCOUNT_TYPE_VIP) {
                etNum?.setTextColor(ContextCompat.getColor(context, R.color.ui_yellow))
                etRegion?.setTextColor(ContextCompat.getColor(context, R.color.ui_yellow))
                etSuffix?.setTextColor(ContextCompat.getColor(context, R.color.ui_yellow))
                etPrefix?.setTextColor(ContextCompat.getColor(context, R.color.ui_yellow))
                sep?.setTextColor(ContextCompat.getColor(context, R.color.ui_yellow))
            } else if (accountType != ACCOUNT_TYPE_REGULAR) {
                etNum?.setTextColor(ContextCompat.getColor(context, R.color.ui_white))
                etRegion?.setTextColor(ContextCompat.getColor(context, R.color.ui_white))
                etSuffix?.setTextColor(ContextCompat.getColor(context, R.color.ui_white))
                etPrefix?.setTextColor(ContextCompat.getColor(context, R.color.ui_white))
                sep?.setTextColor(ContextCompat.getColor(context, R.color.ui_white))
            }
            ivPlate?.setImageResource(0)
            ivPlate?.setImageDrawable(ContextCompat.getDrawable(context, backgroundPlate))
        }
    }

    fun setBackgroundPlateForGarage(
        typeId: Int?,
        countryId: Long?,
        accountType: Int,
        accountColor: Int
    ) {
        val backgroundPlate: Int = NumberPlateHelper.setBackground(
                typeId.toString(), countryId?.toString(),
                accountType, accountColor)

        if (backgroundPlate != 0) {
            if (accountType == ACCOUNT_TYPE_VIP) {
                etNum?.setTextColor(ContextCompat.getColor(context, accountColor))
                etRegion?.setTextColor(ContextCompat.getColor(context, accountColor))
                etSuffix?.setTextColor(ContextCompat.getColor(context, accountColor))
                etPrefix?.setTextColor(ContextCompat.getColor(context, accountColor))
                sep?.setTextColor(ContextCompat.getColor(context, accountColor))
            } else if (accountType != ACCOUNT_TYPE_REGULAR) {
                etNum?.setTextColor(ContextCompat.getColor(context, R.color.ui_white))
                etRegion?.setTextColor(ContextCompat.getColor(context, R.color.ui_white))
                etSuffix?.setTextColor(ContextCompat.getColor(context, R.color.ui_white))
                etPrefix?.setTextColor(ContextCompat.getColor(context, R.color.ui_white))
                sep?.setTextColor(ContextCompat.getColor(context, R.color.ui_white))
            }
            ivPlate?.setImageResource(0)
            ivPlate?.setImageDrawable(ContextCompat.getDrawable(context, backgroundPlate))
        }
    }

    abstract class Watcher : TextWatcher {

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            onChar(charSequence.length)
        }

        override fun afterTextChanged(editable: Editable) = Unit
        abstract fun onChar(lenght: Int)
    }

    fun EditText.handleBackSpaceClickForEmptyString(clearedFocusEditText: EditText?) {
        this.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                if (length() == 0) {
                    clearFocus()
                    clearedFocusEditText?.requestFocus()
                }
            }
            false
        }
    }

    fun setupForPost(vehicle: Vehicle){
        if (vehicle.type?.typeId == "1") {
            val params = FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            params.setMargins(
                dpToPx(-104),
                dpToPx(-22),
                dpToPx(-104),
                dpToPx(-22)
            )
            layoutParams = params
        } else if (vehicle.type?.typeId == "2") {
            val params = FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            params.setMargins(
                dpToPx(-62),
                dpToPx(-42),
                dpToPx(-62),
                dpToPx(-42)
            )
            layoutParams = params
        }
        scaleX = 0.3f
        scaleY = 0.3f
    }


    companion object {
        const val NONE = 0
        const val PREFIX = 1
        const val SUFFIX = 2
        const val NUMBER = 3
        const val REGION = 4
    }
}
