package com.youngfeng.android.assistant.server.controller

import android.media.MediaScannerConnection
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yanzhenjie.andserver.annotation.CrossOrigin
import com.yanzhenjie.andserver.annotation.GetMapping
import com.yanzhenjie.andserver.annotation.PostMapping
import com.yanzhenjie.andserver.annotation.QueryParam
import com.yanzhenjie.andserver.annotation.RequestBody
import com.yanzhenjie.andserver.annotation.RequestMapping
import com.yanzhenjie.andserver.annotation.RequestParam
import com.yanzhenjie.andserver.annotation.ResponseBody
import com.yanzhenjie.andserver.annotation.RestController
import com.yanzhenjie.andserver.http.multipart.MultipartFile
import com.youngfeng.android.assistant.app.AirControllerApp
import com.youngfeng.android.assistant.db.RoomDatabaseHolder
import com.youngfeng.android.assistant.db.entity.ZipFileRecord
import com.youngfeng.android.assistant.server.HttpError
import com.youngfeng.android.assistant.server.HttpModule
import com.youngfeng.android.assistant.server.entity.*
import com.youngfeng.android.assistant.server.request.GetAlbumImagesRequest
import com.youngfeng.android.assistant.server.request.IdsRequest
import com.youngfeng.android.assistant.server.util.ErrorBuilder
import com.youngfeng.android.assistant.util.CommonUtil
import com.youngfeng.android.assistant.util.MD5Helper
import com.youngfeng.android.assistant.util.PathHelper
import com.youngfeng.android.assistant.util.PhotoUtil
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import java.io.File

@CrossOrigin
@RestController
@RequestMapping("/image")
class ImageController {
    private val mContext by lazy { AirControllerApp.getInstance() }
    private val mGson by lazy { Gson() }

    companion object {
        private const val POS_ALL = 1
        private const val POS_CAMERA = 2
        private const val POS_ALBUM = 3
    }

    @PostMapping("/albums")
    @ResponseBody
    fun getAlbums(): HttpResponseEntity<List<AlbumEntity>> {
        val albums = PhotoUtil.getAllAlbums(mContext)
        return HttpResponseEntity.success(albums)
    }

    @PostMapping("/all")
    @ResponseBody
    fun getAllImages(): HttpResponseEntity<List<ImageEntity>> {
        val images = PhotoUtil.getAllImages(mContext)
        return HttpResponseEntity.success(images)
    }

    @PostMapping("/daily")
    fun getDailyImages(): HttpResponseEntity<List<DailyImageEntity>> {
        throw NotImplementedError()
    }

    @PostMapping("/monthly")
    fun getMonthlyImages(): HttpResponseEntity<List<MonthlyImageEntity>> {
        throw NotImplementedError()
    }

    @PostMapping("/albumImages")
    @ResponseBody
    fun getAlbumImages(): HttpResponseEntity<List<ImageEntity>> {
        val images = PhotoUtil.getAlbumImages(mContext)
        return HttpResponseEntity.success(images)
    }

    @PostMapping("/deleteImages")
    @ResponseBody
    fun deleteImages(
        @RequestBody request: IdsRequest
    ): HttpResponseEntity<Any> {
        val deleteResult = PhotoUtil.deleteImageByIds(mContext, request.ids)
        return when (deleteResult.result) {
            DeleteResult.SUCCESS -> {
                HttpResponseEntity.success()
            }
            DeleteResult.PARTIAL -> {
                val response = ErrorBuilder().module(HttpModule.ImageModule).error(HttpError.DeleteImagePartialFailure).build<Any>()
                response.msg = response.msg?.format("%s", deleteResult.failedCount.toString())
                response
            }
            else -> {
                ErrorBuilder().module(HttpModule.ImageModule).error(HttpError.DeleteImageFail).build()
            }
        }
    }

    @PostMapping("/deleteAlbums")
    @ResponseBody
    fun deleteAlbums(
        @RequestBody request: IdsRequest
    ): HttpResponseEntity<Any> {
        val deleteResult = PhotoUtil.deleteAlbumByIds(mContext, request.ids)
        return when (deleteResult.result) {
            DeleteResult.SUCCESS -> {
                HttpResponseEntity.success()
            }
            DeleteResult.PARTIAL -> {
                val response = ErrorBuilder().module(HttpModule.ImageModule).error(HttpError.DeleteAlbumPartialFailure).build<Any>()
                response.msg = response.msg?.format("%s", deleteResult.failedCount.toString())
                response
            }
            else -> {
                ErrorBuilder().module(HttpModule.ImageModule).error(HttpError.DeleteAlbumFail).build()
            }
        }
    }

    @PostMapping("/imagesOfAlbum")
    @ResponseBody
    fun getImagesOfAlbum(@RequestBody request: GetAlbumImagesRequest): HttpResponseEntity<List<ImageEntity>> {
        val images = PhotoUtil.getImagesOfAlbum(mContext, request.id)
        return HttpResponseEntity.success(images)
    }

    /**
     * Upload photo API.
     *
     * @param pos Present specific position, 1: All pictures, path: .../DCIM/xxx, 2: Camera, path:
     * .../DCIM/Camera/xxx 3: Specific album, path can't empty in this situation.
     */
    @PostMapping("/uploadPhotos")
    @ResponseBody
    fun uploadPhoto(
        @RequestParam("pos") pos: Int,
        @RequestParam("photos") photos: Array<MultipartFile>,
        @RequestParam("path") path: String? = null
    ): HttpResponseEntity<List<ImageEntity>> {
        val images = photos.map { photo ->
            var targetPhoto: File? = null

            val fileName = photo.filename ?: photo.name

            when (pos) {
                POS_ALL -> PathHelper.photoRootDir()?.apply {
                    targetPhoto = File(this, "AirController/${fileName}")
                }
                POS_CAMERA -> PathHelper.cameraDir()?.apply {
                    targetPhoto = File(this, fileName)
                }
                else -> path?.apply {
                    targetPhoto = File(path, fileName)
                }
            }

            targetPhoto?.let {
                photo.transferTo(it)

                MediaScannerConnection.scanFile(mContext, arrayOf(it.path), null, null)

                PhotoUtil.findImageByPath(mContext, it.path)?.apply {
                    this.createTime = if (createTime == null || createTime!! <= 0L) System.currentTimeMillis() else createTime
                }
            } ?: return ErrorBuilder().module(HttpModule.ImageModule)
                .error(HttpError.GetPhotoDirFailure)
                .build()
        }

        return HttpResponseEntity.success(images)
    }

    @GetMapping("/downloadImages")
    fun downloadImages(@QueryParam("ids") ids: String): File? {
        val idList = mGson.fromJson<List<String>>(ids, object : TypeToken<List<String>>() {}.type)

        if (idList.isEmpty()) return null

        if (idList.size == 1) {
            val id = idList[0]
            val image = PhotoUtil.findImageById(mContext, id) ?: return null

            val file = File(image.path)

            if (file.isFile) {
                if (file.exists()) {
                    return file
                }
            }

            return null
        }

        val images = mutableListOf<ImageEntity>()
        idList.forEach { id ->
            PhotoUtil.findImageById(mContext, id)?.apply {
                images.add(this)
            }
        }

        CommonUtil.findZipCacheWithPaths(mContext, images.map { it.path })?.apply { return this }

        return compressImages(images).file
    }

    @GetMapping("/downloadAlbums")
    fun downloadAlbums(@QueryParam("ids") ids: String): File? {
        val idList = mGson.fromJson<List<String>>(ids, object : TypeToken<List<String>>() {}.type)

        if (idList.isEmpty()) return null

        if (idList.size == 1) {
            val id = idList[0]
            val images = PhotoUtil.getImagesOfAlbum(mContext, id)
            if (images.isEmpty()) return null

            CommonUtil.findZipCacheWithPaths(mContext, images.map { it.path })?.apply { return this }
        }

        val albums = mutableListOf<AlbumEntity>()
        val videos = mutableListOf<ImageEntity>()
        idList.forEach {
            PhotoUtil.findAlbumById(mContext, it)?.apply {
                albums.add(this)
                PhotoUtil.getImagesOfAlbum(mContext, this.id).apply {
                    videos.addAll(this)
                }
            }
        }
        CommonUtil.findZipCacheWithPaths(mContext, videos.map { it.path }.toMutableList())?.apply { return this }

        return compressAlbums(albums).file
    }

    private fun compressAlbums(albums: List<AlbumEntity>): ZipFile {
        val db = RoomDatabaseHolder.getRoomDatabase(mContext)
        val zipFileRecordDao = db.zipFileRecordDao()

        val originalFilesMD5Json = mutableMapOf<String, String>()

        val zipFile = ZipFile("${PathHelper.zipFileDir().absolutePath}/albums_${System.currentTimeMillis()}.zip")

        val videos = mutableListOf<ImageEntity>()
        albums.forEach { album ->
            PhotoUtil.findAlbumById(mContext, album.id)?.apply {
                PhotoUtil.getImagesOfAlbum(mContext, this.id).onEach {
                    val file = File(it.path)

                    zipFile.addFile(file, ZipParameters().apply { fileNameInZip = "${album.name}/${file.name}" })

                    originalFilesMD5Json[file.absolutePath] = MD5Helper.md5(file)

                    videos.add(it)
                }
            }
        }

        val sortedOriginalPathsMD5 = MD5Helper.md5(videos.map { it.path }.sorted().joinToString(","))

        val record = ZipFileRecord(
            name = zipFile.file.name,
            path = zipFile.file.path,
            md5 = MD5Helper.md5(zipFile.file),
            originalFilesMD5 = mGson.toJson(originalFilesMD5Json),
            originalPathsMD5 = sortedOriginalPathsMD5,
            createTime = System.currentTimeMillis(),
            isMultiOriginalFile = true
        )

        zipFileRecordDao.insert(record)

        return zipFile
    }

    private fun compressImages(images: List<ImageEntity>): ZipFile {
        val db = RoomDatabaseHolder.getRoomDatabase(mContext)
        val zipFileRecordDao = db.zipFileRecordDao()

        val originalFilesMD5Json = mutableMapOf<String, String>()
        val sortedOriginalPathsMD5 = MD5Helper.md5(images.map { it.path }.sorted().joinToString(","))

        val zipFile = ZipFile("${PathHelper.zipFileDir().absolutePath}/images_${System.currentTimeMillis()}.zip")

        images.forEach {
            val file = File(it.path)

            zipFile.addFile(file)

            originalFilesMD5Json[file.absolutePath] = MD5Helper.md5(file)
        }

        val record = ZipFileRecord(
            name = zipFile.file.name,
            path = zipFile.file.path,
            md5 = MD5Helper.md5(zipFile.file),
            originalFilesMD5 = mGson.toJson(originalFilesMD5Json),
            originalPathsMD5 = sortedOriginalPathsMD5,
            createTime = System.currentTimeMillis(),
            isMultiOriginalFile = true
        )

        zipFileRecordDao.insert(record)

        return zipFile
    }
}
