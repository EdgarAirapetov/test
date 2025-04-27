package com.numplates.nomera3.presentation.view.widgets.numberplateview

import com.numplates.nomera3.R

/**
 * Created by artem on 08.06.18
 */
enum class NumberPlateEnum {


    RU_AUTO(
            resourceId = R.layout.numplate_ru,
            backgroundId = null,
            flag = null,
            regexPattern = "[ABEKMHOPCTYXАВЕКМНОРСТУХ]",
            numberEtId = R.id.tvNum,
            numPattern = "a000aa",
            regionEtId = R.id.tvRegion,
            regionPattern = "000",
            etSuffixId = null,
            suffixPattern = null,
            etPrefixId = null,
            prefixPattern = null),

    RU_AUTO_GRAY(
        resourceId = R.layout.numplate_ru_gray,
        backgroundId = null,
        flag = null,
        regexPattern = "[ABEKMHOPCTYXАВЕКМНОРСТУХ]",
        numberEtId = R.id.tv_number,
        numPattern = "000",
        regionEtId = R.id.tv_region,
        regionPattern = "000",
        etSuffixId = R.id.tv_suffix,
        suffixPattern = "aa",
        etPrefixId = R.id.tv_prefix,
        prefixPattern = "a"),

    RU_MOTO(
            resourceId = R.layout.numplate_ru_moto,
            backgroundId = null,
            flag = null,
            regexPattern = "[ABEKMHOPCTYXАВЕКМНОРСТУХ]",
            numberEtId = R.id.tvNum,
            numPattern = "0000",
            regionEtId = R.id.tvRegion,
            regionPattern = "00",
            etSuffixId = R.id.tvSuffix,
            suffixPattern = "aa",
            etPrefixId = null,
            prefixPattern = null),

    RU_MOTO_GRAY(
        resourceId = R.layout.numplate_ru_moto_gray,
        backgroundId = null,
        flag = null,
        regexPattern = "[ABEKMHOPCTYXАВЕКМНОРСТУХ]",
        numberEtId = R.id.tv_number,
        numPattern = "0000",
        regionEtId = R.id.tv_region,
        regionPattern = "00",
        etSuffixId = R.id.tv_suffix,
        suffixPattern = "aa",
        etPrefixId = null,
        prefixPattern = null),

    UA_AUTO(
            resourceId = R.layout.numplate_ua,
            backgroundId = null,
            flag = null,
            regexPattern = "[ABEIKMHOPCTXАВЕКМНОРСТХ]",
            numberEtId = R.id.tvNum,
            numPattern = "aa0000aa",
            regionEtId = R.id.tvRegion,
            regionPattern = "",
            etSuffixId = R.id.tvSuffix,
            suffixPattern = "",
            etPrefixId = 0,
            prefixPattern = ""),

    UA_AUTO_GRAY(
        resourceId = R.layout.numplate_ua_gray,
        backgroundId = null,
        flag = null,
        regexPattern = "[ABEIKMHOPCTXАВЕКМНОРСТХ]",
        numberEtId = R.id.tv_number,
        numPattern = "0000",
        regionEtId = null,
        regionPattern = null,
        etSuffixId = R.id.tv_suffix,
        suffixPattern = "aa",
        etPrefixId = R.id.tv_prefix,
        prefixPattern = "aa"),

    UA_MOTO(
            resourceId = R.layout.numplate_ua_moto,
            backgroundId = null,
            flag = null,
            regexPattern = "[ABEIKMHOPCTXАВЕКМНОРСТХ]",
            numberEtId = R.id.tvNum,
            numPattern = "0000",
            regionEtId = null,
            regionPattern = null,
            etSuffixId = R.id.tvSuffix,
            suffixPattern = "aa",
            etPrefixId = R.id.tvPrefix,
            prefixPattern = "aa"),

    UA_MOTO_GRAY(
        resourceId = R.layout.numplate_ua_moto_gray,
        backgroundId = null,
        flag = null,
        regexPattern = "[ABEIKMHOPCTXАВЕКМНОРСТХ]",
        numberEtId = R.id.tv_number,
        numPattern = "0000",
        regionEtId = null,
        regionPattern = null,
        etSuffixId = R.id.tv_suffix,
        suffixPattern = "aa",
        etPrefixId = R.id.tv_prefix,
        prefixPattern = "aa"),

    BY_AUTO(
            resourceId = R.layout.numplate_by,
            backgroundId = null,
            flag = null,
            regexPattern = "[ABEIKMHOPCTXАВЕКМНОРСТХ]",
            numberEtId = R.id.tvNum,
            numPattern = "0000",
            regionEtId = R.id.tvRegion,
            regionPattern = "0",
            etSuffixId = R.id.tvSuffix,
            suffixPattern = "aa",
            etPrefixId = null,
            prefixPattern = null),

    BY_AUTO_GRAY(
        resourceId = R.layout.numplate_by_gray,
        backgroundId = null,
        flag = null,
        regexPattern = "[ABEIKMHOPCTXАВЕКМНОРСТХ]",
        numberEtId = R.id.tv_number,
        numPattern = "0000",
        regionEtId = R.id.tv_region,
        regionPattern = "0",
        etSuffixId = R.id.tv_suffix,
        suffixPattern = "aa",
        etPrefixId = null,
        prefixPattern = null),

    BY_MOTO(
            resourceId = R.layout.numplate_by_moto,
            backgroundId = null,
            flag = null,
            regexPattern = "[ABEIKMHOPCTXАВЕКМНОРСТХ]",
            numberEtId = R.id.tvNum,
            numPattern = "0000",
            regionEtId = R.id.tvRegion,
            regionPattern = "0",
            etSuffixId = R.id.tvSuffix,
            suffixPattern = "aa",
            etPrefixId = null,
            prefixPattern = null),

    BY_MOTO_GRAY(
        resourceId = R.layout.numplate_by_moto_gray,
        backgroundId = null,
        flag = null,
        regexPattern = "[ABEIKMHOPCTXАВЕКМНОРСТХ]",
        numberEtId = R.id.tv_number,
        numPattern = "0000",
        regionEtId = R.id.tv_region,
        regionPattern = "0",
        etSuffixId = R.id.tv_suffix,
        suffixPattern = "aa",
        etPrefixId = null,
        prefixPattern = null),

    GE_AUTO(
            resourceId = R.layout.numplate_ge,
            backgroundId = null,
            flag = null,
            regexPattern = "[QWERTYUIOPASDFGHJKLZXCVBNM]",
            numberEtId = R.id.tvNum,
            numPattern = "aa000aa",
            regionEtId = null,
            regionPattern = null,
            etSuffixId = null,
            suffixPattern = null,
            etPrefixId = null,
            prefixPattern = null),

    GE_AUTO_GRAY(
        resourceId = R.layout.numplate_ge_gray,
        backgroundId = null,
        flag = null,
        regexPattern = "[QWERTYUIOPASDFGHJKLZXCVBNM]",
        numberEtId = R.id.tv_number,
        numPattern = "000",
        regionEtId = null,
        regionPattern = null,
        etSuffixId = R.id.tv_suffix,
        suffixPattern = "aa",
        etPrefixId = R.id.tv_prefix,
        prefixPattern = "aa"),

    GE_MOTO(
            resourceId = R.layout.numplate_ge_moto,
            backgroundId = null,
            flag = null,
            regexPattern = "[QWERTYUIOPASDFGHJKLZXCVBNM]",
            numberEtId = R.id.tvNum,
            numPattern = "0000",
            regionEtId = null,
            regionPattern = null,
            etSuffixId = R.id.tvSuffix,
            suffixPattern = "aa",
            etPrefixId = 0,
            prefixPattern = null),

    GE_MOTO_GRAY(
        resourceId = R.layout.numplate_ge_moto_gray,
        backgroundId = null,
        flag = null,
        regexPattern = "[QWERTYUIOPASDFGHJKLZXCVBNM]",
        numberEtId = R.id.tv_number,
        numPattern = "0000",
        regionEtId = null,
        regionPattern = null,
        etSuffixId = R.id.tv_suffix,
        suffixPattern = "aa",
        etPrefixId = null,
        prefixPattern = null),

    KZ_AUTO(
            resourceId = R.layout.numplate_kz,
            backgroundId = null,
            flag = null,
            regexPattern = "[QWERTYUIOPASDFGHJKLZXCVBNM]",
            numberEtId = R.id.tvNum,
            numPattern = "000",
            regionEtId = R.id.tvRegion,
            regionPattern = "00",
            etSuffixId = R.id.tvSuffix,
            suffixPattern = "aaa",
            etPrefixId = null,
            prefixPattern = null),

    KZ_AUTO_GRAY(
        resourceId = R.layout.numplate_kz_gray,
        backgroundId = null,
        flag = null,
        regexPattern = "[QWERTYUIOPASDFGHJKLZXCVBNM]",
        numberEtId = R.id.tv_number,
        numPattern = "000",
        regionEtId = R.id.tv_region,
        regionPattern = "00",
        etSuffixId = R.id.tv_suffix,
        suffixPattern = "aa",
        etPrefixId = null,
        prefixPattern = null),

    KZ_MOTO(
            resourceId = R.layout.numplate_kz_moto,
            backgroundId = null,
            flag = null,
            regexPattern = "[QWERTYUIOPASDFGHJKLZXCVBNM]",
            numberEtId = R.id.tvNum,
            numPattern = "00",
            regionEtId = R.id.tvRegion,
            regionPattern = "00",
            etSuffixId = R.id.tvSuffix,
            suffixPattern = "aa",
            etPrefixId = null,
            prefixPattern = null),

    KZ_MOTO_GRAY(
        resourceId = R.layout.numplate_kz_moto_gray,
        backgroundId = null,
        flag = null,
        regexPattern = "[QWERTYUIOPASDFGHJKLZXCVBNM]",
        numberEtId = R.id.tv_number,
        numPattern = "00",
        regionEtId = R.id.tv_region,
        regionPattern = "00",
        etSuffixId = R.id.tv_suffix,
        suffixPattern = "aa",
        etPrefixId = null,
        prefixPattern = null),


    AM_AUTO(
            resourceId = R.layout.numplate_am,
            backgroundId = null,
            flag = null,
            regexPattern = "[JKNTEWYABHQRIPDFGZCMXՈUSOL]",
            numberEtId = R.id.tvNum,
            numPattern = "00aa000",
            regionEtId = null,
            regionPattern = null,
            etSuffixId = null,
            suffixPattern = null,
            etPrefixId = null,
            prefixPattern = null),

    AM_AUTO_GRAY(
        resourceId = R.layout.numplate_am_gray,
        backgroundId = null,
        flag = null,
        regexPattern = "[JKNTEWYABHQRIPDFGZCMXՈUSOL]",
        numberEtId = R.id.tv_number,
        numPattern = "000",
        regionEtId = R.id.tv_region,
        regionPattern = "00",
        etSuffixId = R.id.tv_suffix,
        suffixPattern = "aa",
        etPrefixId = null,
        prefixPattern = null),

    AM_MOTO(
            resourceId = R.layout.numplate_am_moto,
            backgroundId = null,
            flag = null,
            regexPattern = "[JKNTEWYABHQRIPDFGZCMXՈUSOL]",
            numberEtId = R.id.tvNum,
            numPattern = "000",
            regionEtId = R.id.tvRegion,
            regionPattern = "00",
            etSuffixId = R.id.tvSuffix,
            suffixPattern = "aa",
            etPrefixId = null,
            prefixPattern = null),

    AM_MOTO_GRAY(
        resourceId = R.layout.numplate_am_moto_gray,
        backgroundId = null,
        flag = null,
        regexPattern = "[JKNTEWYABHQRIPDFGZCMXՈUSOL]",
        numberEtId = R.id.tv_number,
        numPattern = "000",
        regionEtId = R.id.tv_region,
        regionPattern = "00",
        etSuffixId = R.id.tv_suffix,
        suffixPattern = "aa",
        etPrefixId = null,
        prefixPattern = null),

    //---для снегоходов используются мото номера из-за схожести

    PEDESTRIAN(
            resourceId = R.layout.number_pedestrian,
            backgroundId = null,
            flag = null,
            regexPattern = "[ABHQRIPDFGZCMXՈUSOL]",
            numberEtId = R.id.tvNum,
            numPattern = "aa",
            regionEtId = R.id.tvRegion,
            regionPattern = null,
            etSuffixId = R.id.tvSuffix,
            suffixPattern = "000",
            etPrefixId = 0,
            prefixPattern = null),
    COMMON(
            resourceId = R.layout.number_common,
            backgroundId = R.drawable.number_common,
            flag = R.drawable.ru,
            regexPattern = null,
            numberEtId = R.id.tvNum,
            numPattern = "Aa",
            regionEtId = 0,
            regionPattern = null,
            etSuffixId = R.id.tvSuffix,
            suffixPattern = "Aa",
            etPrefixId = 0,
            prefixPattern = null);

    internal var resourceId: Int = 0
    internal var backgroundId: Int? = 0
    internal var flagId: Int? = 0

    internal var etNumberId: Int = 0
    internal var numPattern: String
    internal var etRegionId: Int? = 0
    internal var regionPattern: String?
    internal var etSuffixId: Int? = 0
    internal var suffixPattern: String?
    internal var etPrefixId: Int? = 0
    internal var prefixPattern: String?


    internal var regexPattern: String?


    constructor(resourceId: Int,
                backgroundId: Int?,
                flag: Int?,
                regexPattern: String?,
                numberEtId: Int,
                numPattern: String,
                regionEtId: Int?,
                regionPattern: String?,
                etSuffixId: Int?,
                suffixPattern: String?,
                etPrefixId: Int?,
                prefixPattern: String?) {
        this.resourceId = resourceId
        this.backgroundId = backgroundId
        this.flagId = flag
        this.regexPattern = regexPattern
        this.etNumberId = numberEtId
        this.numPattern = numPattern
        this.etRegionId = regionEtId
        this.regionPattern = regionPattern
        this.etSuffixId = etSuffixId
        this.suffixPattern = suffixPattern
        this.etPrefixId = etPrefixId
        this.prefixPattern = prefixPattern
    }

    constructor(builder: Builder) {

        this.resourceId = builder.layoutId
        this.backgroundId = builder.backgroundId
        this.flagId = builder.flagId
        this.regexPattern = builder.regexPattern

        if (builder.etNumberId != 0) {
            this.etNumberId = builder.etNumberId
        } else {
            etNumberId = R.id.tvNum
        }
        this.numPattern = builder.numPattern

        if (builder.etRegionId != 0) {
            this.etRegionId = builder.etRegionId
        } else {
            etRegionId = R.id.tvRegion
        }
        this.regionPattern = builder.regionPattern

        if (builder.etSuffixId != 0) {
            this.etSuffixId = builder.etSuffixId
        } else {
            etSuffixId = R.id.tvSuffix
        }
        this.suffixPattern = builder.suffixPattern

        if (builder.etPrefixId != 0) {
            this.etPrefixId = builder.etPrefixId
        } else {
            etPrefixId = R.id.tvPrefix
        }
        this.prefixPattern = builder.prefixPattern


    }

    class Builder {

        internal var layoutId: Int = 0
        internal var backgroundId: Int? = 0
        internal var flagId: Int? = 0
        internal var regexPattern: String? = null
        internal var etNumberId: Int = 0
        internal lateinit var numPattern: String
        internal var etRegionId: Int? = 0
        internal var regionPattern: String? = null
        internal var etSuffixId: Int? = 0
        internal var suffixPattern: String? = null
        internal var etPrefixId: Int? = 0
        internal var prefixPattern: String? = null

        fun layoutId(layoutId: Int): Builder {
            this.layoutId = layoutId
            return this
        }

        fun backgroundId(backgroundId: Int?): Builder {
            this.backgroundId = backgroundId
            return this
        }

        fun flagId(flagId: Int?): Builder {
            this.flagId = flagId
            return this
        }

        fun regexPattern(regexPattern: String?): Builder {
            this.regexPattern = regexPattern
            return this
        }

        fun etNumberId(etNumberId: Int): Builder {
            this.etNumberId = etNumberId
            return this
        }

        fun numPattern(numPattern: String): Builder {
            this.numPattern = numPattern
            return this
        }

        fun etRegionId(etRegionId: Int?): Builder {
            this.etRegionId = etRegionId
            return this
        }

        fun regionPattern(regionPattern: String?): Builder {
            this.regionPattern = regionPattern
            return this
        }

        fun etSuffixId(etSuffixId: Int?): Builder {
            this.etSuffixId = etSuffixId
            return this
        }

        fun suffixPattern(suffixPattern: String?): Builder {
            this.suffixPattern = suffixPattern
            return this
        }

        fun etPrefixId(etPrefixId: Int?): Builder {
            this.etPrefixId = etPrefixId
            return this
        }

        fun prefixPattern(prefixPattern: String?): Builder {
            this.prefixPattern = prefixPattern
            return this
        }

        //        public NumberPlateClass build(){
        //            return new NumberPlateClass(this);
        //        }

    }

}
