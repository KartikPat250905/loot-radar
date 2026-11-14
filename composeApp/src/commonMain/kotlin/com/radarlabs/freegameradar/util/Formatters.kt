package com.radarlabs.freegameradar.util

import kotlin.math.round

fun formatWorth(value: Double): String {
    val positiveValue = value.coerceAtLeast(0.0)
    return when {
        positiveValue >= 1_000_000_000.0 -> {
            val b = round((positiveValue / 1_000_000_000.0) * 100) / 100
            if (b >= 1000.0) {
                "$${formatDecimal(b / 1000.0, 2)}T"
            } else {
                "$${formatDecimal(b, 2)}B"
            }
        }
        positiveValue >= 1_000_000.0 -> {
            val m = round((positiveValue / 1_000_000.0) * 100) / 100
            if (m >= 1000.0) {
                "$${formatDecimal(m / 1000.0, 2)}B"
            } else {
                "$${formatDecimal(m, 2)}M"
            }
        }
        positiveValue >= 100_000.0 -> {
            val k = round((positiveValue / 1_000.0) * 10) / 10
            if (k >= 1000.0) {
                "$${formatDecimal(k / 1000.0, 2)}M"
            } else {
                "$${formatDecimal(k, 1)}K"
            }
        }
        else -> {
            val rounded = round(positiveValue * 100) / 100
            val intPart = rounded.toLong()
            val decPart = round((rounded - intPart) * 100).toLong().coerceIn(0, 99)
            val decStr = if (decPart < 10) "0$decPart" else "$decPart"
            "$${formatWithCommas(intPart)}.$decStr"
        }
    }
}

private fun formatDecimal(value: Double, decimals: Int): String {
    val multiplier = if (decimals == 2) 100.0 else 10.0
    val rounded = round(value * multiplier) / multiplier
    val intPart = rounded.toLong()
    val decPart = round((rounded - intPart) * multiplier).toLong().coerceIn(0, (multiplier - 1).toLong())
    val decStr = if (decimals == 2) {
        if (decPart < 10) "0$decPart" else "$decPart"
    } else {
        "$decPart"
    }
    return "${formatWithCommas(intPart)}.$decStr"
}

private fun formatWithCommas(number: Long): String {
    val str = number.toString()
    val sb = StringBuilder()
    val length = str.length
    for (i in 0 until length) {
        if (i > 0 && (length - i) % 3 == 0) {
            sb.append(',')
        }
        sb.append(str[i])
    }
    return sb.toString()
}
