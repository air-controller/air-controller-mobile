package com.youngfeng.android.assistant.web.controller

import android.media.MediaScannerConnection
import com.yanzhenjie.andserver.annotation.GetMapping
import com.yanzhenjie.andserver.annotation.PathVariable
import com.yanzhenjie.andserver.annotation.PostMapping
import com.yanzhenjie.andserver.annotation.RequestBody
import com.yanzhenjie.andserver.annotation.RequestMapping
import com.yanzhenjie.andserver.annotation.ResponseBody
import com.yanzhenjie.andserver.annotation.RestController
import com.youngfeng.android.assistant.R
import com.youngfeng.android.assistant.app.MobileAssistantApplication
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

@RestController
@RequestMapping("/video")
class VideoController {
    private val mContext by lazy { MobileAssistantApplication.getInstance() }

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
    fun delete(@RequestBody request: DeleteVideosRequest): HttpResponseEntity<Any> {
        try {
            val paths = request.paths

            var deleteItemNum = 0
            paths.forEach { path ->
                val file = File(path)
                if (!file.exists()) {
                    val response = ErrorBuilder().module(HttpModule.VideoModule).error(HttpError.DeleteVideoFail).build<Any>()
                    response.msg = convertToDeleteVideoError(paths.size, deleteItemNum)
                    return response
                } else {
                    val isSuccess = file.deleteRecursively()
                    if (!isSuccess) {
                        val response = ErrorBuilder().module(HttpModule.VideoModule).error(HttpError.DeleteVideoFail).build<Any>()
                        response.msg = convertToDeleteVideoError(paths.size, deleteItemNum)
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
            val response = ErrorBuilder().module(HttpModule.ImageModule).error(HttpError.DeleteAlbumFail).build<Any>()
            response.msg = e.message
            return response
        }
    }

    private fun convertToDeleteVideoError(albumNum: Int, deletedItemNum: Int): String {
        if (deletedItemNum > 0) {
            return mContext.getString(R.string.place_holder_delete_part_of_success)
                .format(albumNum, deletedItemNum)
        }

        return mContext.getString(R.string.delete_video_file_fail)
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
