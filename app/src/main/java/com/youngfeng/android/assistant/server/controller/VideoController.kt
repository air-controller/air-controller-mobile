package com.youngfeng.android.assistant.server.controller

import android.Manifest
import android.media.MediaScannerConnection
import android.text.TextUtils
import com.yanzhenjie.andserver.annotation.CrossOrigin
import com.yanzhenjie.andserver.annotation.GetMapping
import com.yanzhenjie.andserver.annotation.PathVariable
import com.yanzhenjie.andserver.annotation.PostMapping
import com.yanzhenjie.andserver.annotation.RequestBody
import com.yanzhenjie.andserver.annotation.RequestMapping
import com.yanzhenjie.andserver.annotation.RequestParam
import com.yanzhenjie.andserver.annotation.ResponseBody
import com.yanzhenjie.andserver.annotation.RestController
import com.yanzhenjie.andserver.http.HttpRequest
import com.yanzhenjie.andserver.http.HttpResponse
import com.yanzhenjie.andserver.http.multipart.MultipartFile
import com.yanzhenjie.andserver.util.MediaType
import com.youngfeng.android.assistant.R
import com.youngfeng.android.assistant.app.AirControllerApp
import com.youngfeng.android.assistant.event.Permission
import com.youngfeng.android.assistant.event.RequestPermissionsEvent
import com.youngfeng.android.assistant.ext.getString
import com.youngfeng.android.assistant.server.HttpError
import com.youngfeng.android.assistant.server.HttpModule
import com.youngfeng.android.assistant.server.entity.HttpResponseEntity
import com.youngfeng.android.assistant.server.entity.VideoEntity
import com.youngfeng.android.assistant.server.entity.VideoFolder
import com.youngfeng.android.assistant.server.request.DeleteVideosRequest
import com.youngfeng.android.assistant.server.request.GetVideosRequest
import com.youngfeng.android.assistant.server.response.RangeSupportResponseBody
import com.youngfeng.android.assistant.server.util.ErrorBuilder
import com.youngfeng.android.assistant.util.PathHelper
import com.youngfeng.android.assistant.util.VideoUtil
import org.greenrobot.eventbus.EventBus
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.lang.Exception
import java.util.Locale

@RestController
@RequestMapping("/video")
class VideoController {
    private val mContext by lazy { AirControllerApp.getInstance() }

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

    @PostMapping("/delete")
    fun delete(httpRequest: HttpRequest, @RequestBody request: DeleteVideosRequest): HttpResponseEntity<Any> {
        val languageCode = httpRequest.getHeader("languageCode")
        val locale = if (!TextUtils.isEmpty(languageCode)) Locale(languageCode!!) else Locale("en")

        try {
            val paths = request.paths

            var deleteItemNum = 0
            paths.forEach { path ->
                val file = File(path)
                if (!file.exists()) {
                    val response = ErrorBuilder().locale(locale).module(HttpModule.VideoModule).error(HttpError.DeleteVideoFail).build<Any>()
                    response.msg = convertToDeleteVideoError(locale, paths.size, deleteItemNum)
                    return response
                } else {
                    val isSuccess = file.deleteRecursively()
                    if (!isSuccess) {
                        val response = ErrorBuilder().locale(locale).module(HttpModule.VideoModule).error(HttpError.DeleteVideoFail).build<Any>()
                        response.msg = convertToDeleteVideoError(locale, paths.size, deleteItemNum)
                        return response
                    } else {
                        MediaScannerConnection.scanFile(mContext, arrayOf(path), null, null)
                    }
                }

                deleteItemNum ++
            }

            return HttpResponseEntity.success()
        } catch (e: Exception) {
            e.printStackTrace()
            val response = ErrorBuilder().locale(locale).module(HttpModule.ImageModule).error(HttpError.DeleteAlbumFail).build<Any>()
            response.msg = e.message
            return response
        }
    }

    private fun convertToDeleteVideoError(locale: Locale, albumNum: Int, deletedItemNum: Int): String {
        if (deletedItemNum > 0) {
            return mContext.getString(locale, R.string.place_holder_delete_part_of_success)
                .format(albumNum, deletedItemNum)
        }

        return mContext.getString(locale, R.string.delete_video_file_fail)
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
}
