package com.youngfeng.android.assistant.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.youngfeng.android.assistant.db.entity.UploadFileRecord

@Dao
interface UploadFileRecordDao {
    @Insert
    fun insert(uploadFileRecord: UploadFileRecord)

    @Update()
    fun update(uploadFileRecord: UploadFileRecord)

    @Query(
        """
        SELECT * FROM t_upload_file_record WHERE md5 = :md5
    """
    )
    fun findWithMd5(md5: String): List<UploadFileRecord>
}
