package com.youngfeng.android.assistant.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.youngfeng.android.assistant.db.entity.ZipFileRecord

@Dao
interface ZipFileRecordDao {

    @Insert
    fun insert(zipFileRecord: ZipFileRecord)

    @Query("SELECT * FROM t_zip_file_record WHERE original_paths_md5 = :md5")
    fun findByOriginalPathsMd5(md5: String): Array<ZipFileRecord>

    @Query("SELECT * FROM t_zip_file_record")
    fun findAll(): List<ZipFileRecord>

    @Delete
    fun delete(zipFileRecord: ZipFileRecord): Int
}
