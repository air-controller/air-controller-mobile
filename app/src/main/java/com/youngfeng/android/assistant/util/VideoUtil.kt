package com.youngfeng.android.assistant.util

import android.content.Context
import android.media.MediaScannerConnection
import android.provider.MediaStore
import com.youngfeng.android.assistant.server.entity.DeleteResultEntity
import com.youngfeng.android.assistant.server.entity.VideoEntity
import com.youngfeng.android.assistant.server.entity.VideoFolder
import java.io.File

object VideoUtil {

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
                        val videoPath = cursor.getString(videoUriIndex);

                        var folder = videoPath
                        val index = folder.lastIndexOf("/")
                        if (index != -1) {
                            folder = folder.substring(0, index)
                        }

                        val album = VideoFolder(
                            id = bucketId,
                            name = bucketName ?: "Unknown folder",
                            coverVideoId = videoId,
                            path = folder
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

    fun getVideosByFolderId(context: Context, folderId: String): List<VideoEntity> {
        val projection = arrayOf(
            MediaStore.Video.VideoColumns._ID,
            MediaStore.Video.VideoColumns.DISPLAY_NAME,
            MediaStore.Video.VideoColumns.DATA,
            MediaStore.Video.VideoColumns.DURATION,
            MediaStore.Video.VideoColumns.SIZE,
            MediaStore.Video.VideoColumns.DATE_ADDED,
            MediaStore.Video.VideoColumns.DATE_MODIFIED
        )

        val selection = "${MediaStore.Video.VideoColumns.BUCKET_ID} = ?"
        val selectionArgs = arrayOf(folderId)

        val videos = mutableListOf<VideoEntity>()

        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns._ID)
                val nameIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DISPLAY_NAME)
                val dataIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA)
                val durationIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION)
                val sizeIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.SIZE)
                val dateAddedIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATE_ADDED)
                val dateModified =
                    cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATE_MODIFIED)

                do {
                    val id = cursor.getLong(idIndex)
                    val name = cursor.getString(nameIndex)
                    val path = cursor.getString(dataIndex)
                    val duration = cursor.getLong(durationIndex)
                    val size = cursor.getLong(sizeIndex)
                    val createTime = cursor.getLong(dateAddedIndex)
                    val modifyTime = cursor.getLong(dateModified)

                    videos.add(
                        VideoEntity(
                            id = id,
                            name = name,
                            path = path,
                            duration = duration,
                            size = size,
                            createTime = createTime,
                            lastModifyTime = modifyTime
                        )
                    )
                } while (cursor.moveToNext())
            }
        }

        return videos
    }

    fun getAllVideos(context: Context): List<VideoEntity> {
        val projection = arrayOf(
            MediaStore.Video.VideoColumns._ID,
            MediaStore.Video.VideoColumns.DISPLAY_NAME,
            MediaStore.Video.VideoColumns.DATA,
            MediaStore.Video.VideoColumns.DURATION,
            MediaStore.Video.VideoColumns.SIZE,
            MediaStore.Video.VideoColumns.DATE_ADDED,
            MediaStore.Video.VideoColumns.DATE_MODIFIED
        )

        val videos = mutableListOf<VideoEntity>()

        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns._ID)
                val nameIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DISPLAY_NAME)
                val dataIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA)
                val durationIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION)
                val sizeIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.SIZE)
                val dateAddedIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATE_ADDED)
                val dateModified =
                    cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATE_MODIFIED)

                do {
                    val id = cursor.getLong(idIndex)
                    val name = cursor.getString(nameIndex)
                    val path = cursor.getString(dataIndex)
                    val duration = cursor.getLong(durationIndex)
                    val size = cursor.getLong(sizeIndex)
                    val createTime = cursor.getLong(dateAddedIndex)
                    val modifyTime = cursor.getLong(dateModified)

                    videos.add(
                        VideoEntity(
                            id = id,
                            name = name,
                            path = path,
                            duration = duration,
                            size = size,
                            createTime = createTime,
                            lastModifyTime = modifyTime
                        )
                    )
                } while (cursor.moveToNext())
            }
        }

        return videos
    }

    fun findById(context: Context, id: String): VideoEntity? {
        val projection = arrayOf(
            MediaStore.Video.VideoColumns._ID,
            MediaStore.Video.VideoColumns.DISPLAY_NAME,
            MediaStore.Video.VideoColumns.DATA,
            MediaStore.Video.VideoColumns.DURATION,
            MediaStore.Video.VideoColumns.SIZE,
            MediaStore.Video.VideoColumns.DATE_ADDED,
            MediaStore.Video.VideoColumns.DATE_MODIFIED
        )

        val videos = mutableListOf<VideoEntity>()
        val selection = "${MediaStore.Video.VideoColumns._ID} = ?"
        val selectionArgs = arrayOf(id)

        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns._ID)
                val nameIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DISPLAY_NAME)
                val dataIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA)
                val durationIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION)
                val sizeIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.SIZE)
                val dateAddedIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATE_ADDED)
                val dateModified =
                    cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATE_MODIFIED)

                val id = cursor.getLong(idIndex)
                val name = cursor.getString(nameIndex)
                val path = cursor.getString(dataIndex)
                val duration = cursor.getLong(durationIndex)
                val size = cursor.getLong(sizeIndex)
                val createTime = cursor.getLong(dateAddedIndex)
                val modifyTime = cursor.getLong(dateModified)

                return VideoEntity(
                    id = id,
                    name = name,
                    path = path,
                    duration = duration,
                    size = size,
                    createTime = createTime,
                    lastModifyTime = modifyTime
                )
            }
        }

        return null
    }

    fun findByPath(context: Context, videoPath: String): VideoEntity? {
        val projection = arrayOf(
            MediaStore.Video.VideoColumns._ID,
            MediaStore.Video.VideoColumns.DISPLAY_NAME,
            MediaStore.Video.VideoColumns.DATA,
            MediaStore.Video.VideoColumns.DURATION,
            MediaStore.Video.VideoColumns.SIZE,
            MediaStore.Video.VideoColumns.DATE_ADDED,
            MediaStore.Video.VideoColumns.DATE_MODIFIED
        )

        val videos = mutableListOf<VideoEntity>()
        val selection = "${MediaStore.Video.VideoColumns.DATA} = ?"
        val selectionArgs = arrayOf(videoPath)

        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns._ID)
                val nameIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DISPLAY_NAME)
                val dataIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA)
                val durationIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION)
                val sizeIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.SIZE)
                val dateAddedIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATE_ADDED)
                val dateModified =
                    cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATE_MODIFIED)

                val id = cursor.getLong(idIndex)
                val name = cursor.getString(nameIndex)
                val path = cursor.getString(dataIndex)
                val duration = cursor.getLong(durationIndex)
                val size = cursor.getLong(sizeIndex)
                val createTime = cursor.getLong(dateAddedIndex)
                val modifyTime = cursor.getLong(dateModified)

                return VideoEntity(
                    id = id,
                    name = name,
                    path = path,
                    duration = duration,
                    size = size,
                    createTime = createTime,
                    lastModifyTime = modifyTime
                )
            }
        }

        return null
    }

    fun deleteVideoByIds(context: Context, ids: List<String>): DeleteResultEntity {
        var successCount = 0
        ids.forEach {
            findById(context, it)?.apply {
                if (delete(context, this)) {
                    successCount ++
                }
            }
        }

        return if (successCount == ids.size) {
            DeleteResultEntity.success()
        } else if (successCount > 0 && successCount < ids.size) {
            DeleteResultEntity.partial(failedCount = ids.size - successCount)
        } else {
            DeleteResultEntity.failed()
        }
    }

    fun delete(context: Context, video: VideoEntity): Boolean {
        try {
            val file = File(video.path)
            if (file.exists()) {
                if (file.delete()) {
                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(video.path),
                        arrayOf("video/*")
                    ) { _, _ -> }
                    return true
                }
            }
            return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun findVideoFolderById(context: Context, id: String): VideoFolder? {
        val projection = arrayOf(
            MediaStore.Video.VideoColumns._ID,
            MediaStore.Video.VideoColumns.BUCKET_ID,
            MediaStore.Video.VideoColumns.DATA,
            MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME
        )

        val selection = "${MediaStore.Video.VideoColumns.BUCKET_ID} = ?"
        val selectionArgs = arrayOf(id)

        val orderBy = "${MediaStore.Video.VideoColumns.DATE_TAKEN} DESC"
        val map = mutableMapOf<String, VideoFolder>()

        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
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
                        val videoPath = cursor.getString(videoUriIndex);

                        var folder = videoPath
                        val index = folder.lastIndexOf("/")
                        if (index != -1) {
                            folder = folder.substring(0, index)
                        }

                        val album = VideoFolder(
                            id = bucketId,
                            name = bucketName ?: "Unknown folder",
                            coverVideoId = videoId,
                            path = folder
                        )
                        map[bucketId] = album

                        album
                    }

                    album.videoCount++
                } while (cursor.moveToNext())
            }
        }

        return map.values.firstOrNull()
    }

    fun deleteVideoFolderByIds(context: Context, ids: List<String>): DeleteResultEntity {
        var successCount = 0

        ids.forEach { id ->
            findVideoFolderById(context, id)?.let {
                if (deleteFolder(context, it)) {
                    successCount ++
                }
            }
        }

        return if (successCount == ids.size) {
            DeleteResultEntity.success()
        } else if (successCount > 0 && successCount < ids.size) {
            DeleteResultEntity.partial(failedCount = ids.size - successCount)
        } else {
            DeleteResultEntity.failed()
        }
    }

    fun deleteFolder(context: Context, videoFolder: VideoFolder): Boolean {
        val videos = getVideosByFolderId(context, videoFolder.id)
        var isSuccess = true
        for (video in videos) {
            if (!delete(context, video)) {
                isSuccess = false
                break
            }
        }

        if (isSuccess) {
            MediaScannerConnection.scanFile(context, arrayOf(videoFolder.path), arrayOf("image/*"), null)
        }

        return isSuccess
    }
}
