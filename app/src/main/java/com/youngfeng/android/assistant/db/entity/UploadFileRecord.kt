package com.youngfeng.android.assistant.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "t_upload_file_record")
data class UploadFileRecord(
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    var path: String,
    var name: String,
    var size: Long,
    var md5: String,
    @ColumnInfo(name = "upload_time")
    var uploadTime: Long
)
