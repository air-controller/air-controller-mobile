package com.youngfeng.android.assistant.server.controller

import android.Manifest
import android.media.MediaScannerConnection
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yanzhenjie.andserver.annotation.CrossOrigin
import com.yanzhenjie.andserver.annotation.GetMapping
import com.yanzhenjie.andserver.annotation.PathVariable
import com.yanzhenjie.andserver.annotation.PostMapping
import com.yanzhenjie.andserver.annotation.QueryParam
import com.yanzhenjie.andserver.annotation.RequestBody
import com.yanzhenjie.andserver.annotation.RequestMapping
import com.yanzhenjie.andserver.annotation.RequestParam
import com.yanzhenjie.andserver.annotation.ResponseBody
import com.yanzhenjie.andserver.annotation.RestController
import com.yanzhenjie.andserver.http.HttpRequest
import com.yanzhenjie.andserver.http.HttpResponse
import com.yanzhenjie.andserver.http.multipart.MultipartFile
import com.yanzhenjie.andserver.util.MediaType
import com.youngfeng.android.assistant.app.AirControllerApp
import com.youngfeng.android.assistant.db.RoomDatabaseHolder
import com.youngfeng.android.assistant.db.entity.ZipFileRecord
import com.youngfeng.android.assistant.event.Permission
import com.youngfeng.android.assistant.event.RequestPermissionsEvent
import com.youngfeng.android.assistant.server.HttpError
import com.youngfeng.android.assistant.server.HttpModule
import com.youngfeng.android.assistant.server.entity.DeleteResult
import com.youngfeng.android.assistant.server.entity.HttpResponseEntity
import com.youngfeng.android.assistant.server.entity.VideoEntity
import com.youngfeng.android.assistant.server.entity.VideoFolder
import com.youngfeng.android.assistant.server.request.GetVideosRequest
import com.youngfeng.android.assistant.server.request.IdsRequest
import com.youngfeng.android.assistant.server.response.RangeSupportResponseBody
import com.youngfeng.android.assistant.server.util.ErrorBuilder
import com.youngfeng.android.assistant.util.CommonUtil
import com.youngfeng.android.assistant.util.MD5Helper
import com.youngfeng.android.assistant.util.PathHelper
import com.youngfeng.android.assistant.util.VideoUtil
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import org.greenrobot.eventbus.EventBus
import pub.devrel.easypermissions.EasyPermissions
import java.io.File

@CrossOrigin
@RestController
@RequestMapping("/video")
class VideoController {
    private val mContext by lazy { AirControllerApp.getInstance() }
    private val mGson by lazy { Gson() }

    @PostMapping("/folders")
    @ResponseBody
    fun videoFolders(): HttpResponseEntity<List<VideoFolder>> {
        val videoFolders = VideoUtil.getAllVideoFolders(mContext)
        return HttpResponseEntity.success(videoFolders)
    }

    @PostMapping("/videosInFolder")
    fun getVideosInFolder(@RequestBody request: GetVideosRequest): HttpResponseEntity<List<VideoEntity>> {
        val videos = VideoUtil.getVideosByFolderId(mContext, request.folderId)
        return HttpResponseEntity.success(videos)
    }

    @PostMapping("/videos")
    fun getAllVideos(): HttpResponseEntity<List<VideoEntity>> {
        val videos = VideoUtil.getAllVideos(mContext)
        return HttpResponseEntity.success(videos)
    }

    @PostMapping("/deleteVideos")
    @ResponseBody
    fun deleteVideos(@RequestBody request: IdsRequest): HttpResponseEntity<Any> {
        val deleteResult = VideoUtil.deleteVideoByIds(mContext, request.ids)
        return when (deleteResult.result) {
            DeleteResult.SUCCESS -> {
                HttpResponseEntity.success()
            }
            DeleteResult.PARTIAL -> {
                val response = ErrorBuilder().module(HttpModule.VideoModule).error(HttpError.DeleteVideoPartialFailure).build<Any>()
                response.msg = response.msg?.format("%s", deleteResult.failedCount.toString())
                response
            }
            else -> {
                ErrorBuilder().module(HttpModule.VideoModule).error(HttpError.DeleteVideoFail).build()
            }
        }
    }

    @PostMapping("/deleteVideoFolders")
    @ResponseBody
    fun deleteVideoFolders(@RequestBody request: IdsRequest): HttpResponseEntity<Any> {
        val deleteResult = VideoUtil.deleteVideoFolderByIds(mContext, request.ids)
        return when (deleteResult.result) {
            DeleteResult.SUCCESS -> {
                HttpResponseEntity.success()
            }
            DeleteResult.PARTIAL -> {
                val response = ErrorBuilder().module(HttpModule.VideoModule).error(HttpError.DeleteVideoFolderPartialFailure).build<Any>()
                response.msg = response.msg?.format("%s", deleteResult.failedCount.toString())
                response
            }
            else -> {
                ErrorBuilder().module(HttpModule.VideoModule).error(HttpError.DeleteVideoFolderFail).build()
            }
        }
    }

    @CrossOrigin
    @GetMapping("/item/{id}")
    fun findById(request: HttpRequest, response: HttpResponse, @PathVariable("id") id: String): com.yanzhenjie.andserver.http.ResponseBody {
        val videoEntity = VideoUtil.findById(mContext, id)

        if (null != videoEntity) {
            val rangeHeader = request.getHeader("Range")
            val videoFile = File(videoEntity.path)

            return RangeSupportResponseBody(
                contentType = MediaType("video", videoFile.extension),
                file = videoFile,
                rangeHeader = rangeHeader
            ).attachToResponse(response)
        } else {
            throw IllegalArgumentException("Video item is not exist, id: $id")
        }
    }

    @ResponseBody
    @PostMapping("/uploadVideos")
    fun uploadVideos(
        @RequestParam("videos") videos: Array<MultipartFile>,
        @RequestParam("folder") folder: String?
    ): HttpResponseEntity<Any> {
        if (!EasyPermissions.hasPermissions(
                mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        ) {
            EventBus.getDefault().post(
                RequestPermissionsEvent(
                    arrayOf(
                        Permission.WriteExternalStorage,
                    )
                )
            )
            return HttpResponseEntity.commonError(HttpError.LackOfNecessaryPermissions)
        }

        PathHelper.videoRootDir()?.let { videoRootDir ->
            videos.onEach { video ->
                val file = if (folder.isNullOrEmpty() || folder == "null") {
                    File(videoRootDir, "AirController/${video.filename}")
                } else {
                    File(folder, "${video.filename}")
                }
                video.transferTo(file)
                MediaScannerConnection.scanFile(
                    mContext, arrayOf(file.path), null, null
                )
            }
            return HttpResponseEntity.success()
        } ?: return ErrorBuilder().module(HttpModule.VideoModule).error(HttpError.UploadVideoFailure)
            .build()
    }

    @CrossOrigin
    @GetMapping("/downloadVideos")
    fun downloadVideos(@QueryParam("ids") ids: String): File? {
        val idList = mGson.fromJson<List<String>>(ids, object : TypeToken<List<String>>() {}.type)

        if (idList.isEmpty()) return null

        if (idList.size == 1) {
            val id = idList[0]
            val video = VideoUtil.findById(mContext, id) ?: return null

            val file = File(video.path)

            if (file.isFile) {
                if (file.exists()) {
                    return file
                }
            }

            return null
        }

        val videos = mutableListOf<VideoEntity>()
        idList.forEach { id ->
            VideoUtil.findById(mContext, id)?.apply {
                videos.add(this)
            }
        }

        CommonUtil.findZipCacheWithPaths(mContext, videos.map { it.path })?.apply { return this }

        return compressVideos(videos).file
    }

    @CrossOrigin
    @GetMapping("/downloadVideoFolders")
    fun downloadVideoFolders(@QueryParam("ids") ids: String): File? {
        val idList = mGson.fromJson<List<String>>(ids, object : TypeToken<List<String>>() {}.type)

        if (idList.isEmpty()) return null

        if (idList.size == 1) {
            val id = idList[0]
            val videos = VideoUtil.getVideosByFolderId(mContext, id)
            if (videos.isEmpty()) return null

            CommonUtil.findZipCacheWithPaths(mContext, videos.map { it.path })?.apply { return this }
        }

        val videoFolders = mutableListOf<VideoFolder>()
        val videos = mutableListOf<VideoEntity>()
        idList.forEach {
            VideoUtil.findVideoFolderById(mContext, it)?.apply {
                videoFolders.add(this)

                VideoUtil.getVideosByFolderId(mContext, id).apply {
                    videos.addAll(this)
                }
            }
        }
        CommonUtil.findZipCacheWithPaths(mContext, videos.map { it.path }.toMutableList())?.apply { return this }

        return compressVideoFolders(videoFolders).file
    }

    private fun compressVideoFolders(videoFolders: List<VideoFolder>): ZipFile {
        val db = RoomDatabaseHolder.getRoomDatabase(mContext)
        val zipFileRecordDao = db.zipFileRecordDao()

        val originalFilesMD5Json = mutableMapOf<String, String>()

        val zipFile = ZipFile("${PathHelper.zipFileDir().absolutePath}/videoFolders_${System.currentTimeMillis()}.zip")

        val videos = mutableListOf<VideoEntity>()
        videoFolders.forEach { videoFolder ->
            VideoUtil.findVideoFolderById(mContext, videoFolder.id)?.apply {
                VideoUtil.getVideosByFolderId(mContext, this.id).onEach {
                    val file = File(it.path)

                    originalFilesMD5Json[file.absolutePath] = MD5Helper.md5(file)

                    zipFile.addFile(file, ZipParameters().apply { fileNameInZip = "${videoFolder.name}/${file.name}" })

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

    private fun compressVideos(videos: List<VideoEntity>): ZipFile {
        val db = RoomDatabaseHolder.getRoomDatabase(mContext)
        val zipFileRecordDao = db.zipFileRecordDao()

        val originalFilesMD5Json = mutableMapOf<String, String>()
        val sortedOriginalPathsMD5 = MD5Helper.md5(videos.map { it.path }.sorted().joinToString(","))

        val zipFile = ZipFile("${PathHelper.zipFileDir().absolutePath}/videos_${System.currentTimeMillis()}.zip")

        videos.forEach {
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
