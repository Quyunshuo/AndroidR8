package com.yunshuo.android.r8

import java.text.SimpleDateFormat
import java.util.Locale

object Utils {

    private val simpleDateFormat by lazy {
        SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
    }

    fun formatTimestamp(timestamp: Long): String = simpleDateFormat.format(timestamp)

    fun removeNewLines(input: String): String {
        return input.replace("\n", "").replace("\r", "")
    }
}