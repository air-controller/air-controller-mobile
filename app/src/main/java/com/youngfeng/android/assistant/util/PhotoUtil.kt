package com.youngfeng.android.assistant.util

import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import com.youngfeng.android.assistant.web.entity.AlbumEntity
import com.youngfeng.android.assistant.web.entity.ImageEntity

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
                        val bucketName = cursor.getString(bucketNameIndex)
                        val coverImageId = cursor.getString(idIndex)

                        val album = AlbumEntity(
                            id = bucketId,
                            name = bucketName,
                            coverImageId = coverImageId
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
            MediaStore.Images.ImageColumns.DISPLAY_NAME
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

                    images.add(
                        ImageEntity(id, mimeType, thumbnail, imageData, width, height, modifyDate, dateTaken, displayName)
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
            MediaStore.Images.ImageColumns.DISPLAY_NAME
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

                    images.add(
                        ImageEntity(id, mimeType, thumbnail, imageData, width, height, modifyDate, dateTaken, displayName)
                    )
                } while (it.moveToNext())
            }
        }

        return images
    }

    @JvmStatic
    fun deleteImage(context: Context, id: String): Boolean {
        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val where = "${MediaStore.Images.ImageColumns._ID} = ?"
        val selectionArgs = arrayOf(id)

        val rowsNumber = context.contentResolver.delete(contentUri, where, selectionArgs)
        return rowsNumber > 0
    }

    @JvmStatic
    fun deleteMultiImage(context: Context, ids: Array<String>): Boolean {
        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val where = "${MediaStore.Images.ImageColumns._ID} in ?"
        val selectionArgs = arrayOf("(${ids.joinToString(",")})")

        val rowsNumber = context.contentResolver.delete(contentUri, where, selectionArgs)
        return rowsNumber == ids.size
    }
}
