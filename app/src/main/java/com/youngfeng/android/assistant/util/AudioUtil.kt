package com.youngfeng.android.assistant.util

import android.content.Context
import android.provider.MediaStore
import com.youngfeng.android.assistant.server.entity.AudioEntity

object AudioUtil {

    fun getAllAudios(context: Context): List<AudioEntity> {
        val contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.AudioColumns._ID,
            MediaStore.Audio.AudioColumns.DATE_ADDED,
            MediaStore.Audio.AudioColumns.DISPLAY_NAME,
            MediaStore.Audio.AudioColumns.DURATION,
            MediaStore.Audio.AudioColumns.SIZE,
            MediaStore.Audio.AudioColumns.DATA,
            MediaStore.Audio.AudioColumns.IS_MUSIC,
            MediaStore.Audio.AudioColumns.DATE_MODIFIED
        )

        val audios = mutableListOf<AudioEntity>()

        context.contentResolver.query(contentUri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns._ID)
                val dateAddedIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATE_ADDED)
                val displayNameIndex =
                    cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DISPLAY_NAME)
                val durationIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION)
                val sizeIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.SIZE)
                val dataIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA)
                val isMusicIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.IS_MUSIC)
                val dateModifiedIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATE_MODIFIED)

                do {
                    val path = cursor.getString(dataIndex)

                    val pointIndex = path.lastIndexOf("/")

                    var folder = ""

                    if (pointIndex >= 0) {
                        folder = path.substring(0, pointIndex)
                    }

                    audios.add(
                        AudioEntity(
                            id = cursor.getString(idIndex),
                            createTime = cursor.getLong(dateAddedIndex),
                            name = cursor.getString(displayNameIndex),
                            duration = cursor.getLong(durationIndex),
                            size = cursor.getLong(sizeIndex),
                            path = path,
                            isMusic = cursor.getInt(isMusicIndex) != 0,
                            folder = folder,
                            modifyDate = cursor.getLong(dateModifiedIndex)
                        )
                    )
                } while (cursor.moveToNext())
            }
        }

        return audios
    }

    fun findById(context: Context, id: String): AudioEntity? {
        val contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.AudioColumns._ID,
            MediaStore.Audio.AudioColumns.DATE_ADDED,
            MediaStore.Audio.AudioColumns.DISPLAY_NAME,
            MediaStore.Audio.AudioColumns.DURATION,
            MediaStore.Audio.AudioColumns.SIZE,
            MediaStore.Audio.AudioColumns.DATA,
            MediaStore.Audio.AudioColumns.IS_MUSIC,
            MediaStore.Audio.AudioColumns.DATE_MODIFIED
        )

        val selection = "${MediaStore.Audio.AudioColumns._ID} = ?"
        val selectionArgs = arrayOf(id)

        context.contentResolver.query(contentUri, projection, selection, selectionArgs, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns._ID)
                    val dateAddedIndex =
                        cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATE_ADDED)
                    val displayNameIndex =
                        cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DISPLAY_NAME)
                    val durationIndex =
                        cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION)
                    val sizeIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.SIZE)
                    val dataIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA)
                    val isMusicIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.IS_MUSIC)
                    val dateModifiedIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATE_MODIFIED)

                    val path = cursor.getString(dataIndex)

                    val pointIndex = path.lastIndexOf("/")

                    var folder = ""

                    if (pointIndex >= 0) {
                        folder = path.substring(0, pointIndex)
                    }
                    return AudioEntity(
                        id = cursor.getString(idIndex),
                        createTime = cursor.getLong(dateAddedIndex),
                        name = cursor.getString(displayNameIndex),
                        duration = cursor.getLong(durationIndex),
                        size = cursor.getLong(sizeIndex),
                        path = path,
                        isMusic = cursor.getInt(isMusicIndex) != 0,
                        folder = folder,
                        modifyDate = cursor.getLong(dateModifiedIndex)
                    )
                }
            }

        return null
    }
}
