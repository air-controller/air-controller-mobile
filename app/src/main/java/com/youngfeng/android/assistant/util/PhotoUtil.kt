package com.youngfeng.android.assistant.util

import android.content.Context
import android.net.Uri
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

    @JvmStatic
    fun getAllImages(context: Context): List<ImageEntity> {
        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projections = arrayOf(
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.Thumbnails.DATA,
            MediaStore.Images.ImageColumns.DATA
        )

        val orderBy = "${MediaStore.Images.ImageColumns.DATE_TAKEN} DESC"

        val images = mutableListOf<ImageEntity>()
        context.contentResolver.query(contentUri, projections, null, null, orderBy, null)?.use {
            if (it.moveToFirst()) {
                val idIndex = it.getColumnIndex(MediaStore.Images.ImageColumns._ID)
                val thumbnailDataIndex = it.getColumnIndex(MediaStore.Images.Thumbnails.DATA)
                val imageDataIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)

                do {
                    val id = it.getString(idIndex)
                    val thumbnailData = it.getString(thumbnailDataIndex)
                    val imageData = it.getString(imageDataIndex)

                    images.add(ImageEntity(id, thumbnailData, imageData))
                } while (it.moveToNext())
            }
        }

        images.forEach { image ->
            MediaStore.Images.Thumbnails.queryMiniThumbnail(context.contentResolver, image.id.toLong(), MediaStore.Images.Thumbnails.MINI_KIND, null)?.use { cursor ->
                if (cursor.count > 0) {
                    cursor.moveToFirst()
                    val url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA));
                    image.thumbnail = url
                    println("Thumbnail, url: $url")
                }
            }
        }

        return images
    }
}