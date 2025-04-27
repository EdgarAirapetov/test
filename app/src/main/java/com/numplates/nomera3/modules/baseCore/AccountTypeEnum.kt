package com.numplates.nomera3.modules.baseCore


/**
 * Премиум и випов больше нет/ В редизайне
 * */
enum class AccountTypeEnum(val value: Int) {
    ACCOUNT_TYPE_VIP(2),
    ACCOUNT_TYPE_PREMIUM(1),
    ACCOUNT_TYPE_REGULAR(0),
    ACCOUNT_TYPE_UNKNOWN(-1)
}

/**
 * Премиум и випов больше нет/ В редизайне
 * */
fun createAccountTypeEnum(accountType: Int?): AccountTypeEnum {
    return when (accountType) {
        AccountTypeEnum.ACCOUNT_TYPE_REGULAR.value,
        AccountTypeEnum.ACCOUNT_TYPE_PREMIUM.value,
        AccountTypeEnum.ACCOUNT_TYPE_VIP.value -> AccountTypeEnum.ACCOUNT_TYPE_REGULAR
        else -> AccountTypeEnum.ACCOUNT_TYPE_UNKNOWN
    }
}
