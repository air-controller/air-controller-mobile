package com.youngfeng.android.assistant.util

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.youngfeng.android.assistant.web.entity.AlbumEntity

object PhotoUtil {

    @JvmStatic
    fun getAllAlbums(context: Context): List<AlbumEntity> {
        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projections = arrayOf(
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.BUCKET_ID,
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Images.ImageColumns.DATA
        )

        val orderBy = "${MediaStore.Images.ImageColumns.DATE_TAKEN} DESC"
        val map = HashMap<String, AlbumEntity>()

        context.contentResolver.query(contentUri, projections, null, null, orderBy)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val bucketIdIndex =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.BUCKET_ID)
                val bucketNameIndex =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)
                val imageUriIndex =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA)

                do {
                    val bucketId = cursor.getString(bucketIdIndex)

                    val album = map[bucketId] ?: let {
                        val bucketName = cursor.getString(bucketNameIndex)
                        val lastImageUri = Uri.parse(cursor.getString(imageUriIndex))
                        val album = AlbumEntity(
                            id = bucketId,
                            name = bucketName,
                            cover = lastImageUri.toString()
                        )
                        map[bucketId] = album

                        album
                    }

                    album.photoNum ++
                } while (cursor.moveToNext())
            }
        }

        return map.values.toList()
    }
}