package com.youngfeng.android.assistant.db

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomDatabaseHolder {
    private var roomDatabase: AppDatabase? = null

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE t_upload_file_record(
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    path TEXT NOT NULL,
                    name TEXT NOT NULL,
                    size INTEGER NOT NULL,
                    md5 TEXT NOT NULL,
                    upload_time INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    fun getRoomDatabase(context: Context): AppDatabase {
        if (null == roomDatabase) {
            roomDatabase = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "air_controller")
                .addMigrations(MIGRATION_1_2)
                .build()
        }

        return roomDatabase!!
    }
}
