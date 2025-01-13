package com.yunshuo.android.r8.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserMoodDao {

    /**
     * 插入情绪状态记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserMood(userMood: UserMood): Long

    /**
     * 获取所有情绪状态记录，按时间降序排序
     */
    @Query("SELECT * FROM user_mood_table ORDER BY timestamp DESC")
    suspend fun getAllUserMood(): List<UserMood>
}