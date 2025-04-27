package com.meera.core.utils.text

import com.meera.core.utils.getAge


fun ageCityFormattedText(age: Long?, city: String?): String {
    if (age != null && city != null) return "${getAge(age)}, $city"
    if (age != null && city == null) return "${getAge(age)}"
    if (age == null && city != null) return "$city"
    return ""
}

fun limitCounterText(count: Int, maxRangeLimit: Int = 99): String =
    if (count >= maxRangeLimit) "99+" else count.toString()
