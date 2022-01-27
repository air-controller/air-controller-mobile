package com.youngfeng.android.assistant.db

import android.content.Context
import androidx.room.Room

object RoomDatabaseHolder {
    private var roomDatabase: AppDatabase? = null

    fun getRoomDatabase(context: Context): AppDatabase {
        if (null == roomDatabase) {
            roomDatabase = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "air_controller").build()
        }

        return roomDatabase!!
    }
}
