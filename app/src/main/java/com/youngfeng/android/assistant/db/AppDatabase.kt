package com.youngfeng.android.assistant.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.youngfeng.android.assistant.Constants
import com.youngfeng.android.assistant.db.dao.UploadFileRecordDao
import com.youngfeng.android.assistant.db.dao.ZipFileRecordDao
import com.youngfeng.android.assistant.db.entity.UploadFileRecord
import com.youngfeng.android.assistant.db.entity.ZipFileRecord

@Database(entities = [ZipFileRecord::class, UploadFileRecord::class], version = Constants.DATABASE_VERSION)
abstract class AppDatabase : RoomDatabase() {

    abstract fun zipFileRecordDao(): ZipFileRecordDao

    abstract fun uploadFileRecordDao(): UploadFileRecordDao
}
