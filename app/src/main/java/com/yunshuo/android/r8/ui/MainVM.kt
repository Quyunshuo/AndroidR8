package com.yunshuo.android.r8.ui

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yunshuo.android.r8.data.LocalRepo
import com.yunshuo.android.r8.data.MoodStatus
import com.yunshuo.android.r8.data.UserMood
import kotlinx.coroutines.launch

class MainVM : ViewModel() {

    val moodList = mutableStateListOf<UserMood>()

    fun syncHistoryRecord() {
        viewModelScope.launch {
            runCatching {
                LocalRepo.getAllUserMood()
            }.onSuccess {
                moodList.addAll(it)
            }.onFailure {
                Log.d("QQQ", "syncHistoryRecord: ${it.message}")
            }
        }
    }

    fun save(mood: MoodStatus, influencingEvent: String) {
        viewModelScope.launch {
            runCatching {
                LocalRepo.insertUserMood(mood, influencingEvent)
            }.onSuccess {
                moodList.add(0, it)
            }.onFailure {
                Log.d("QQQ", "save: ${it.message}")
            }
        }
    }
}