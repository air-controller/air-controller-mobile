package com.youngfeng.android.assistant.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.youngfeng.android.assistant.db.dao.ZipFileRecordDao
import com.youngfeng.android.assistant.db.entity.ZipFileRecord

@Database(entities = [ZipFileRecord::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun zipFileRecordDao(): ZipFileRecordDao
}
