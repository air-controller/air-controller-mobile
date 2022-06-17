package com.youngfeng.android.assistant.web.controller

import android.media.MediaScannerConnection
import android.text.TextUtils
import com.yanzhenjie.andserver.annotation.GetMapping
import com.yanzhenjie.andserver.annotation.PathVariable
import com.yanzhenjie.andserver.annotation.PostMapping
import com.yanzhenjie.andserver.annotation.RequestBody
import com.yanzhenjie.andserver.annotation.RequestMapping
import com.yanzhenjie.andserver.annotation.ResponseBody
import com.yanzhenjie.andserver.annotation.RestController
import com.yanzhenjie.andserver.http.HttpRequest
import com.youngfeng.android.assistant.R
import com.youngfeng.android.assistant.app.AirControllerApp
import com.youngfeng.android.assistant.ext.getString
import com.youngfeng.android.assistant.util.VideoUtil
import com.youngfeng.android.assistant.web.HttpError
import com.youngfeng.android.assistant.web.HttpModule
import com.youngfeng.android.assistant.web.entity.HttpResponseEntity
import com.youngfeng.android.assistant.web.entity.VideoEntity
import com.youngfeng.android.assistant.web.entity.VideoFolder
import com.youngfeng.android.assistant.web.request.DeleteVideosRequest
import com.youngfeng.android.assistant.web.request.GetVideosRequest
import com.youngfeng.android.assistant.web.util.ErrorBuilder
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

    @GetMapping("/item/{id}")
    fun findById(@PathVariable("id") id: String): VideoEntity {
        val videoEntity = VideoUtil.findById(mContext, id)

        if (null != videoEntity) {
            return videoEntity
        } else {
            throw IllegalArgumentException("Video item is not exist, id: $id")
        }
    }
}
