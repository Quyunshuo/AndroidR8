package com.yunshuo.android.r8.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_mood_table")
data class UserMood(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0,

    /**
     * 用户的情绪状态
     */
    @ColumnInfo(name = "mood")
    val mood: MoodStatus,

    /**
     * 对用户当前情绪状态影响最大的事情
     */
    @ColumnInfo(name = "influencing_event")
    val influencingEvent: String = "",

    /**
     * 记录当前的时间
     */
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)