package com.youngfeng.android.assistant.web.controller

import android.media.MediaScannerConnection
import com.yanzhenjie.andserver.annotation.PostMapping
import com.yanzhenjie.andserver.annotation.RequestBody
import com.yanzhenjie.andserver.annotation.RequestMapping
import com.yanzhenjie.andserver.annotation.ResponseBody
import com.yanzhenjie.andserver.annotation.RestController
import com.youngfeng.android.assistant.R
import com.youngfeng.android.assistant.app.MobileAssistantApplication
import com.youngfeng.android.assistant.util.PhotoUtil
import com.youngfeng.android.assistant.web.HttpError
import com.youngfeng.android.assistant.web.HttpModule
import com.youngfeng.android.assistant.web.entity.*
import com.youngfeng.android.assistant.web.request.DeleteAlbumsRequest
import com.youngfeng.android.assistant.web.request.DeleteImageRequest
import com.youngfeng.android.assistant.web.util.ErrorBuilder
import java.io.File
import java.lang.Exception

@RestController
@RequestMapping("/image")
class ImageController {
    private val mContext by lazy { MobileAssistantApplication.getInstance() }

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

    @PostMapping("/delete")
    @ResponseBody
    fun deleteImage(@RequestBody request: DeleteImageRequest): HttpResponseEntity<Any> {
        try {
            val resultMap = HashMap<String, String>()
            val imageFiles = ArrayList<String>()
            var isAllSuccess = true
            request.paths.forEach { imgPath ->
                val imageFile = File(imgPath)
                imageFiles.add(imageFile.absolutePath)
                if (!imageFile.exists()) {
                    isAllSuccess = false
                    resultMap[imgPath] = HttpError.ImageFileNotExist.value
                } else {
                    val isSuccess = imageFile.delete()
                    if (!isSuccess) {
                        isAllSuccess = false
                        resultMap[imgPath] = HttpError.DeleteImageFail.value
                    }
                }
            }
            if (imageFiles.size > 0) {
                MediaScannerConnection.scanFile(mContext, imageFiles.toTypedArray(), null) { path, uri ->
                    println("Path: $path, uri: ${uri.path}")
                }
            }
            if (!isAllSuccess) {
                val response = ErrorBuilder().module(HttpModule.ImageModule).error(HttpError.DeleteImageFail).build<Any>()
                response.msg = resultMap.map { "${it.key}[${it.value}];" }.toString()
                return response
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val response = ErrorBuilder().module(HttpModule.ImageModule).error(HttpError.DeleteImageFail).build<Any>()
            response.msg = e.message
            return response
        }

        return HttpResponseEntity.success()
    }

    @PostMapping("/deleteAlbums")
    @ResponseBody
    fun deleteAlbums(@RequestBody request: DeleteAlbumsRequest): HttpResponseEntity<Any> {
        try {
            val paths = request.paths

            var deleteItemNum = 0
            paths.forEach { path ->
                val file = File(path)
                if (!file.exists()) {
                    val response = ErrorBuilder().module(HttpModule.ImageModule).error(HttpError.DeleteAlbumFail).build<Any>()
                    response.msg = convertToDeleteAlbumError(paths.size, deleteItemNum)
                    return response
                } else {
                    val isSuccess = file.deleteRecursively()
                    if (!isSuccess) {
                        val response = ErrorBuilder().module(HttpModule.ImageModule).error(HttpError.DeleteAlbumFail).build<Any>()
                        response.msg = convertToDeleteAlbumError(paths.size, deleteItemNum)
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

    private fun convertToDeleteAlbumError(albumNum: Int, deletedItemNum: Int): String {
        if (deletedItemNum > 0) {
            return mContext.getString(R.string.place_holder_delete_part_of_success)
                .format(albumNum, deletedItemNum)
        }

        return mContext.getString(R.string.delete_album_fail)
    }
}
