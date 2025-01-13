package com.yunshuo.android.r8.data

import androidx.annotation.Keep

/**
 * 代码结构用于测试 [Keep] 的效果
 */
@Keep
class TextProvider {

    private val text: String
        get() = LocalRepo.textList.random()

    fun getRandomText(): String = text
}