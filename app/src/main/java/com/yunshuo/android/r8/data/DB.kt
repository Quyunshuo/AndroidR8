package com.yunshuo.android.r8.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yunshuo.android.r8.App

@Database(
    version = 1,
    entities = [UserMood::class],
    exportSchema = true
)
abstract class DB : RoomDatabase() {

    companion object {

        val INSTANCE: DB by lazy {
            Room.databaseBuilder(App.app, DB::class.java, "app_db").build()
        }
    }

    abstract fun userMoodDao(): UserMoodDao
}