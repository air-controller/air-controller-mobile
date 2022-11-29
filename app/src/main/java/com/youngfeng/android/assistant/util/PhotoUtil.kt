package com.youngfeng.android.assistant.util

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import com.youngfeng.android.assistant.server.entity.AlbumEntity
import com.youngfeng.android.assistant.server.entity.DeleteResultEntity
import com.youngfeng.android.assistant.server.entity.ImageEntity
import java.io.File

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

                val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID)

                do {
                    val bucketId = cursor.getString(bucketIdIndex)

                    val album = map[bucketId] ?: let {
                        val bucketName = cursor.getString(bucketNameIndex) ?: ""
                        val coverImageId = cursor.getString(idIndex)
                        val imageUri = cursor.getString(imageUriIndex)

                        var path = ""
                        if (!TextUtils.isEmpty(imageUri)) {
                            val index = imageUri.lastIndexOf("/")

                            if (index != -1) {
                                path = imageUri.substring(0, index)
                            }
                        }

                        val album = AlbumEntity(
                            id = bucketId,
                            name = bucketName,
                            coverImageId = coverImageId,
                            path = path
                        )
                        map[bucketId] = album

                        album
                    }

                    album.photoNum++
                } while (cursor.moveToNext())
            }
        }

        return map.values.toList()
    }

    @JvmStatic
    fun getAllImages(context: Context): List<ImageEntity> {
        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projections = arrayOf(
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.DATE_MODIFIED,
            MediaStore.Images.ImageColumns.MINI_THUMB_MAGIC,
            MediaStore.Images.ImageColumns.MIME_TYPE,
            MediaStore.Images.ImageColumns.WIDTH,
            MediaStore.Images.ImageColumns.HEIGHT,
            MediaStore.Images.ImageColumns.DATE_TAKEN,
            MediaStore.Images.ImageColumns.DISPLAY_NAME,
            MediaStore.Images.ImageColumns.SIZE
        )

        val orderBy = "${MediaStore.Images.ImageColumns.DATE_TAKEN} DESC"

        val images = mutableListOf<ImageEntity>()
        context.contentResolver.query(contentUri, projections, null, null, orderBy, null)?.use {
            if (it.moveToFirst()) {
                val idIndex = it.getColumnIndex(MediaStore.Images.ImageColumns._ID)
                val imageDataIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                val dateModifiedIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DATE_MODIFIED)
                val miniThumbMagicIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.MINI_THUMB_MAGIC)
                val mimeTypeIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.MIME_TYPE)
                val widthIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.WIDTH)
                val heightIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.HEIGHT)
                val dateTakenIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN)
                val displayNameIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.SIZE)

                do {
                    val id = it.getString(idIndex)
                    val imageData = it.getString(imageDataIndex)
                    val modifyDate = it.getLong(dateModifiedIndex)
                    val thumbnail = it.getString(miniThumbMagicIndex)
                    val mimeType = it.getString(mimeTypeIndex)
                    val width = it.getInt(widthIndex)
                    val height = it.getInt(heightIndex)
                    val dateTaken = it.getLong(dateTakenIndex)
                    val displayName = it.getString(displayNameIndex)
                    val size = it.getLong(sizeIndex);

                    images.add(
                        ImageEntity(id, mimeType, thumbnail, imageData, width, height, modifyDate, dateTaken, displayName, size)
                    )
                } while (it.moveToNext())
            }
        }

        return images
    }

    @JvmStatic
    fun getAlbumImages(context: Context): List<ImageEntity> {
        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projections = arrayOf(
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.DATE_MODIFIED,
            MediaStore.Images.ImageColumns.MINI_THUMB_MAGIC,
            MediaStore.Images.ImageColumns.MIME_TYPE,
            MediaStore.Images.ImageColumns.WIDTH,
            MediaStore.Images.ImageColumns.HEIGHT,
            MediaStore.Images.ImageColumns.DATE_TAKEN,
            MediaStore.Images.ImageColumns.DISPLAY_NAME,
            MediaStore.Images.ImageColumns.SIZE
        )

        val orderBy = "${MediaStore.Images.ImageColumns.DATE_TAKEN} DESC"

        val selection = "${MediaStore.Images.ImageColumns.DATA} like ?"
        val selectionArgs = arrayOf("${Environment.getExternalStorageDirectory().absolutePath}/DCIM/Camera%")

        val images = mutableListOf<ImageEntity>()
        context.contentResolver.query(contentUri, projections, selection, selectionArgs, orderBy, null)?.use {
            if (it.moveToFirst()) {
                val idIndex = it.getColumnIndex(MediaStore.Images.ImageColumns._ID)
                val imageDataIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                val dateModifiedIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DATE_MODIFIED)
                val miniThumbMagicIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.MINI_THUMB_MAGIC)
                val mimeTypeIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.MIME_TYPE)
                val widthIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.WIDTH)
                val heightIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.HEIGHT)
                val dateTakenIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN)
                val displayNameIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.SIZE)

                do {
                    val id = it.getString(idIndex)
                    val imageData = it.getString(imageDataIndex)
                    val modifyDate = it.getLong(dateModifiedIndex)
                    val thumbnail = it.getString(miniThumbMagicIndex)
                    val mimeType = it.getString(mimeTypeIndex)
                    val width = it.getInt(widthIndex)
                    val height = it.getInt(heightIndex)
                    val dateTaken = it.getLong(dateTakenIndex)
                    val displayName = it.getString(displayNameIndex)
                    val size = it.getLong(sizeIndex);

                    images.add(
                        ImageEntity(id, mimeType, thumbnail, imageData, width, height, modifyDate, dateTaken, displayName, size)
                    )
                } while (it.moveToNext())
            }
        }

        return images
    }

    fun getImagesOfAlbum(context: Context, albumId: String): List<ImageEntity> {
        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projections = arrayOf(
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.DATE_MODIFIED,
            MediaStore.Images.ImageColumns.MINI_THUMB_MAGIC,
            MediaStore.Images.ImageColumns.MIME_TYPE,
            MediaStore.Images.ImageColumns.WIDTH,
            MediaStore.Images.ImageColumns.HEIGHT,
            MediaStore.Images.ImageColumns.DATE_TAKEN,
            MediaStore.Images.ImageColumns.DISPLAY_NAME,
            MediaStore.Images.ImageColumns.SIZE
        )

        val orderBy = "${MediaStore.Images.ImageColumns.DATE_TAKEN} DESC"

        val selection = "${MediaStore.Images.ImageColumns.BUCKET_ID} = ?"
        val selectionArgs = arrayOf(albumId)

        val images = mutableListOf<ImageEntity>()
        context.contentResolver.query(contentUri, projections, selection, selectionArgs, orderBy, null)?.use {
            if (it.moveToFirst()) {
                val idIndex = it.getColumnIndex(MediaStore.Images.ImageColumns._ID)
                val imageDataIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                val dateModifiedIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DATE_MODIFIED)
                val miniThumbMagicIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.MINI_THUMB_MAGIC)
                val mimeTypeIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.MIME_TYPE)
                val widthIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.WIDTH)
                val heightIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.HEIGHT)
                val dateTakenIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN)
                val displayNameIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.SIZE)

                do {
                    val id = it.getString(idIndex)
                    val imageData = it.getString(imageDataIndex)
                    val modifyDate = it.getLong(dateModifiedIndex)
                    val thumbnail = it.getString(miniThumbMagicIndex)
                    val mimeType = it.getString(mimeTypeIndex)
                    val width = it.getInt(widthIndex)
                    val height = it.getInt(heightIndex)
                    val dateTaken = it.getLong(dateTakenIndex)
                    val displayName = it.getString(displayNameIndex)
                    val size = it.getLong(sizeIndex);

                    images.add(
                        ImageEntity(id, mimeType, thumbnail, imageData, width, height, modifyDate, dateTaken, displayName, size)
                    )
                } while (it.moveToNext())
            }
        }

        return images
    }

    fun findImageByPath(context: Context, path: String): ImageEntity? {
        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projections = arrayOf(
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.DATE_MODIFIED,
            MediaStore.Images.ImageColumns.MINI_THUMB_MAGIC,
            MediaStore.Images.ImageColumns.MIME_TYPE,
            MediaStore.Images.ImageColumns.WIDTH,
            MediaStore.Images.ImageColumns.HEIGHT,
            MediaStore.Images.ImageColumns.DATE_TAKEN,
            MediaStore.Images.ImageColumns.DISPLAY_NAME,
            MediaStore.Images.ImageColumns.SIZE
        )

        val orderBy = "${MediaStore.Images.ImageColumns.DATE_TAKEN} DESC"

        val selection = "${MediaStore.Images.ImageColumns.DATA} = ?"
        val selectionArgs = arrayOf(path)

        var result: ImageEntity? = null

        context.contentResolver.query(contentUri, projections, selection, selectionArgs, orderBy, null)?.use {
            if (it.moveToFirst()) {
                val idIndex = it.getColumnIndex(MediaStore.Images.ImageColumns._ID)
                val imageDataIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                val dateModifiedIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DATE_MODIFIED)
                val miniThumbMagicIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.MINI_THUMB_MAGIC)
                val mimeTypeIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.MIME_TYPE)
                val widthIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.WIDTH)
                val heightIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.HEIGHT)
                val dateTakenIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN)
                val displayNameIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.SIZE)

                val id = it.getString(idIndex)
                val imageData = it.getString(imageDataIndex)
                val modifyDate = it.getLong(dateModifiedIndex)
                val thumbnail = it.getString(miniThumbMagicIndex)
                val mimeType = it.getString(mimeTypeIndex)
                val width = it.getInt(widthIndex)
                val height = it.getInt(heightIndex)
                val dateTaken = it.getLong(dateTakenIndex)
                val displayName = it.getString(displayNameIndex)
                val size = it.getLong(sizeIndex);

                result = ImageEntity(id, mimeType, thumbnail, imageData, width, height, modifyDate, dateTaken, displayName, size)
            }
        }

        return result
    }

    fun findImageById(context: Context, id: String): ImageEntity? {
        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projections = arrayOf(
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.DATE_MODIFIED,
            MediaStore.Images.ImageColumns.MINI_THUMB_MAGIC,
            MediaStore.Images.ImageColumns.MIME_TYPE,
            MediaStore.Images.ImageColumns.WIDTH,
            MediaStore.Images.ImageColumns.HEIGHT,
            MediaStore.Images.ImageColumns.DATE_TAKEN,
            MediaStore.Images.ImageColumns.DISPLAY_NAME,
            MediaStore.Images.ImageColumns.SIZE
        )

        val orderBy = "${MediaStore.Images.ImageColumns.DATE_TAKEN} DESC"

        val selection = "${MediaStore.Images.ImageColumns._ID} = ?"
        val selectionArgs = arrayOf(id)

        var result: ImageEntity? = null

        context.contentResolver.query(contentUri, projections, selection, selectionArgs, orderBy, null)?.use {
            if (it.moveToFirst()) {
                val idIndex = it.getColumnIndex(MediaStore.Images.ImageColumns._ID)
                val imageDataIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                val dateModifiedIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DATE_MODIFIED)
                val miniThumbMagicIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.MINI_THUMB_MAGIC)
                val mimeTypeIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.MIME_TYPE)
                val widthIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.WIDTH)
                val heightIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.HEIGHT)
                val dateTakenIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN)
                val displayNameIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.SIZE)

                val id = it.getString(idIndex)
                val imageData = it.getString(imageDataIndex)
                val modifyDate = it.getLong(dateModifiedIndex)
                val thumbnail = it.getString(miniThumbMagicIndex)
                val mimeType = it.getString(mimeTypeIndex)
                val width = it.getInt(widthIndex)
                val height = it.getInt(heightIndex)
                val dateTaken = it.getLong(dateTakenIndex)
                val displayName = it.getString(displayNameIndex)
                val size = it.getLong(sizeIndex);

                result = ImageEntity(id, mimeType, thumbnail, imageData, width, height, modifyDate, dateTaken, displayName, size)
            }
        }

        return result
    }

    fun deleteImageByIds(context: Context, ids: List<String>): DeleteResultEntity {
        var successCount = 0
        ids.forEach { id ->
            findImageById(context, id)?.let {
                if (deleteImage(context, it)) {
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

    fun deleteImage(context: Context, image: ImageEntity): Boolean {
        try {
            val file = File(image.path)
            if (file.exists()) {
                if (file.delete()) {
                    MediaScannerConnection.scanFile(context, arrayOf(file.path), arrayOf("image/*"), null)
                    return true
                }
            }
            return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun deleteAlbumByIds(context: Context, ids: List<String>): DeleteResultEntity {
        var successCount = 0

        ids.forEach { id ->
            findAlbumById(context, id)?.let {
                if (deleteAlbum(context, it)) {
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

    fun findAlbumById(context: Context, id: String): AlbumEntity? {
        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projections = arrayOf(
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.BUCKET_ID,
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Images.ImageColumns.DATA
        )

        val selection = "${MediaStore.Images.ImageColumns.BUCKET_ID} = ?"
        val selectionArgs = arrayOf(id)

        val orderBy = "${MediaStore.Images.ImageColumns.DATE_TAKEN} DESC"

        val map = HashMap<String, AlbumEntity>()

        context.contentResolver.query(contentUri, projections, selection, selectionArgs, orderBy)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val bucketIdIndex =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.BUCKET_ID)
                val bucketNameIndex =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)
                val imageUriIndex =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA)

                val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID)

                do {
                    val bucketId = cursor.getString(bucketIdIndex)

                    val album = map[bucketId] ?: let {
                        val bucketName = cursor.getString(bucketNameIndex) ?: ""
                        val coverImageId = cursor.getString(idIndex)
                        val imageUri = cursor.getString(imageUriIndex)

                        var path = ""
                        if (!TextUtils.isEmpty(imageUri)) {
                            val index = imageUri.lastIndexOf("/")

                            if (index != -1) {
                                path = imageUri.substring(0, index)
                            }
                        }

                        val album = AlbumEntity(
                            id = bucketId,
                            name = bucketName,
                            coverImageId = coverImageId,
                            path = path
                        )
                        map[bucketId] = album

                        album
                    }

                    album.photoNum++
                } while (cursor.moveToNext())
            }
        }

        return map.values.firstOrNull()
    }

    fun deleteAlbum(context: Context, album: AlbumEntity): Boolean {
        val images = getImagesOfAlbum(context, album.id)
        var isSuccess = true
        for (image in images) {
            if (!deleteImage(context, image)) {
                isSuccess = false
                break
            }
        }

        if (isSuccess) {
            MediaScannerConnection.scanFile(context, arrayOf(album.path), arrayOf("image/*"), null)
        }

        return isSuccess
    }
}
