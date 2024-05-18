package com.michael.utils

import kotlinx.datetime.*

fun todayDate(): LocalDate {
    val now = Clock.System.now()
    val tz = TimeZone.currentSystemDefault()
    return now.toLocalDateTime(tz).date
}

fun todayDateTime(): LocalDateTime {
    val now = Clock.System.now()
    val tz = TimeZone.currentSystemDefault()
    return now.toLocalDateTime(tz)
//    return LocalDateTime.now()
}