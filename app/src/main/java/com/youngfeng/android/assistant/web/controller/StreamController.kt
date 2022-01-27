package com.youngfeng.android.assistant.web.controller

import android.content.ContentUris
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yanzhenjie.andserver.annotation.GetMapping
import com.yanzhenjie.andserver.annotation.PathVariable
import com.yanzhenjie.andserver.annotation.QueryParam
import com.yanzhenjie.andserver.annotation.RequestMapping
import com.yanzhenjie.andserver.annotation.RestController
import com.youngfeng.android.assistant.app.MobileAssistantApplication
import com.youngfeng.android.assistant.db.RoomDatabaseHolder
import com.youngfeng.android.assistant.db.entity.ZipFileRecord
import com.youngfeng.android.assistant.util.MD5Helper
import com.youngfeng.android.assistant.util.PhotoUtil
import com.youngfeng.android.assistant.util.VideoUtil
import net.lingala.zip4j.ZipFile
import java.io.File
import java.lang.IllegalArgumentException

@RestController
@RequestMapping("/stream")
class StreamController {
    private val mContext by lazy { MobileAssistantApplication.getInstance() }
    private val mGson by lazy { Gson() }

    companion object {
        const val TAG = "StreamController"
    }

    @GetMapping("/image/thumbnail/{id}/{width}/{height}")
    fun imageThumbnail(
        @PathVariable("id") id: Long,
        @PathVariable("width") width: Int,
        @PathVariable("height") height: Int
    ): Bitmap {
        val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mContext.contentResolver.loadThumbnail(uri, Size(width, height), null)
        } else {
            MediaStore.Images.Thumbnails.getThumbnail(
                mContext.contentResolver,
                id,
                MediaStore.Images.Thumbnails.MINI_KIND, null
            )
        }
    }

    @GetMapping("/video/thumbnail/{id}/{width}/{height}")
    fun videoThumbnail(
        @PathVariable("id") id: Long,
        @PathVariable("width") width: Int,
        @PathVariable("height") height: Int
    ): Bitmap {
        val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mContext.contentResolver.loadThumbnail(uri, Size(width, height), null)
        } else {
            MediaStore.Images.Thumbnails.getThumbnail(
                mContext.contentResolver,
                id,
                MediaStore.Images.Thumbnails.MINI_KIND, null
            )
        }
    }

    @GetMapping("/file")
    fun file(@QueryParam("path") path: String): File {
        return File(path)
    }

    @GetMapping("/dir")
    fun dir(@QueryParam("path") path: String): File {
        var name = path
        val index = path.lastIndexOf("/")
        if (-1 != index) {
            name = path.substring(index + 1)
        }

        mContext.externalCacheDir?.apply {
            val zipTempFolder = File("${this.absoluteFile}/.zip")
            if (!zipTempFolder.exists()) {
                zipTempFolder.mkdirs()
            } else {
                zipTempFolder.listFiles()?.forEach { it.delete() }
            }

            val zipFile = ZipFile("${zipTempFolder}/${name}.zip")
            zipFile.addFolder(File(path))
            return zipFile.file
        }

        throw RuntimeException("Unknown error, path: $path")
    }

    @GetMapping("/image/thumbnail2")
    fun imageThumbnail2(
        @QueryParam("path") path: String,
        @QueryParam("width") width: Int,
        @QueryParam("height") height: Int
    ): Bitmap? {
        val image = PhotoUtil.findImageByPath(mContext, path)
        image?.apply {
            val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, this.id.toLong())
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mContext.contentResolver.loadThumbnail(uri, Size(width, height), null)
            } else {
                MediaStore.Images.Thumbnails.getThumbnail(
                    mContext.contentResolver,
                    this.id.toLong(),
                    MediaStore.Images.Thumbnails.MINI_KIND, null
                )
            }
        }

        return null
    }

    @GetMapping("/video/thumbnail2")
    fun videoThumbnail2(
        @QueryParam("path") path: String,
        @QueryParam("width") width: Int,
        @QueryParam("height") height: Int
    ): Bitmap? {
        val videoEntity = VideoUtil.findByPath(mContext, path)
        videoEntity?.apply {
            val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mContext.contentResolver.loadThumbnail(uri, Size(width, height), null)
            } else {
                MediaStore.Images.Thumbnails.getThumbnail(
                    mContext.contentResolver,
                    id,
                    MediaStore.Images.Thumbnails.MINI_KIND, null
                )
            }
        }

        return null
    }

    @GetMapping("/download")
    fun download(@QueryParam("paths") paths: String): File? {
        val pathList = mGson.fromJson<List<String>>(paths, object : TypeToken<List<String>>() {}.type)

        if (paths.isEmpty()) throw IllegalArgumentException("Paths can't be empty")

        val db = RoomDatabaseHolder.getRoomDatabase(mContext)
        val zipFileRecordDao = db.zipFileRecordDao()

        if (pathList.size == 1) {
            val file = File(pathList.single())

            if (file.isFile) {
                return file
            }

            if (file.isDirectory) {
                val name = file.name

                mContext.externalCacheDir?.apply {
                    val zipTempFolder = File("${this.absoluteFile}/.zip")
                    if (!zipTempFolder.exists()) {
                        zipTempFolder.mkdirs()
                    }

                    val originalPathsMD5 = MD5Helper.md5(file.path)
                    val zipFileRecord = zipFileRecordDao.findByOriginalPathsMd5(originalPathsMD5).singleOrNull()

                    if (null != zipFileRecord) {
                        val oldZipFile = File(zipFileRecord.path)

                        Log.d(TAG, "Multiple file scene match, ${zipFileRecord.path}")

                        if (oldZipFile.exists()) return oldZipFile
                    }

                    val zipFile = ZipFile("${zipTempFolder}/${name}.zip")
                    zipFile.addFolder(file)

                    val record = ZipFileRecord(
                        name = name,
                        path = zipFile.file.path,
                        md5 = MD5Helper.md5(zipFile.file),
                        originalFilesMD5 = MD5Helper.md5(file),
                        originalPathsMD5 = MD5Helper.md5(file.path),
                        createTime = System.currentTimeMillis(),
                        isMultiOriginalFile = false
                    )

                    zipFileRecordDao.insert(record)

                    return zipFile.file
                }
            }
        }

        if (pathList.size > 1) {
            mContext.externalCacheDir?.apply {
                val sortedOriginalPathsMD5 = MD5Helper.md5(pathList.sorted().joinToString(","))

                val zipFileRecord = zipFileRecordDao.findByOriginalPathsMd5(sortedOriginalPathsMD5).singleOrNull()

                if (null != zipFileRecord) {
                    if (zipFileRecord.isMultiOriginalFile) {
                        var isMatch = true

                        val originalFileMD5Map = mGson.fromJson<Map<String, String>>(zipFileRecord.originalFilesMD5, object : TypeToken<Map<String, String>>() {}.type)

                        kotlin.run {
                            pathList.forEach {
                                val file = File(it)

                                if (MD5Helper.md5(file) != originalFileMD5Map[file.absolutePath]) {
                                    isMatch = false
                                    return@run
                                }
                            }
                        }

                        if (isMatch) {
                            Log.d(TAG, "Multiple file scene match, ${zipFileRecord.path}")
                            val zipOldFile = File(zipFileRecord.path)

                            if (zipOldFile.exists()) return zipOldFile
                        }
                    }
                }

                val name = "AirController_${System.currentTimeMillis()}.zip"

                val zipTempFolder = File("${this.absoluteFile}/.zip")
                if (!zipTempFolder.exists()) {
                    zipTempFolder.mkdirs()
                }

                val zipFile = ZipFile("${zipTempFolder}/${name}")

                val originalFilesMD5Json = mutableMapOf<String, String>()

                pathList.forEach {
                    val file = File(it)
                    if (file.isDirectory) {
                        zipFile.addFolder(file)
                    }

                    if (file.isFile) {
                        zipFile.addFile(file)
                    }

                    Log.d(TAG, "Zip compressing, current file: ${file.path}")
                    originalFilesMD5Json[file.absolutePath] = MD5Helper.md5(file)
                }

                val record = ZipFileRecord(
                    name = name,
                    path = zipFile.file.path,
                    md5 = MD5Helper.md5(zipFile.file),
                    originalFilesMD5 = mGson.toJson(originalFilesMD5Json),
                    originalPathsMD5 = sortedOriginalPathsMD5,
                    createTime = System.currentTimeMillis(),
                    isMultiOriginalFile = true
                )

                zipFileRecordDao.insert(record)

                return zipFile.file
            }
        }

        return null
    }
}
