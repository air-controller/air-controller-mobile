package com.youngfeng.android.assistant.util

import android.content.Context
import android.provider.MediaStore
import com.youngfeng.android.assistant.web.entity.VideoEntity
import com.youngfeng.android.assistant.web.entity.VideoFolder

object VideoUtil {

    fun getAllVideos(context: Context): List<VideoEntity> {
        throw NotImplementedError()
    }

    fun getAllVideoFolders(context: Context): List<VideoFolder> {
        val projection = arrayOf(
            MediaStore.Video.VideoColumns._ID,
            MediaStore.Video.VideoColumns.BUCKET_ID,
            MediaStore.Video.VideoColumns.DATA,
            MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME
        )

        val orderBy = "${MediaStore.Video.VideoColumns.DATE_TAKEN} DESC"
        val map = mutableMapOf<String, VideoFolder>()

        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            orderBy,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val bucketIdIndex =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.BUCKET_ID)
                val bucketNameIndex =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME)
                val videoUriIndex =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA)
                val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns._ID)

                do {
                    val bucketId = cursor.getString(bucketIdIndex)

                    val album = map[bucketId] ?: let {
                        val bucketName = cursor.getString(bucketNameIndex)
                        val videoId = cursor.getLong(idIndex)

                        val album = VideoFolder(
                            id = bucketId,
                            name = bucketName,
                            coverVideoId = videoId
                        )
                        map[bucketId] = album

                        album
                    }

                    album.videoCount++
                } while (cursor.moveToNext())
            }
        }

        return map.values.toList()
    }
}
