package com.youngfeng.android.assistant.web.controller

import com.yanzhenjie.andserver.annotation.PostMapping
import com.yanzhenjie.andserver.annotation.RequestBody
import com.yanzhenjie.andserver.annotation.RequestMapping
import com.yanzhenjie.andserver.annotation.ResponseBody
import com.yanzhenjie.andserver.annotation.RestController
import com.youngfeng.android.assistant.app.MobileAssistantApplication
import com.youngfeng.android.assistant.util.PhotoUtil
import com.youngfeng.android.assistant.web.HttpError
import com.youngfeng.android.assistant.web.HttpModule
import com.youngfeng.android.assistant.web.entity.*
import com.youngfeng.android.assistant.web.request.DeleteImageRequest
import com.youngfeng.android.assistant.web.util.ErrorBuilder

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
        val isSuccess = PhotoUtil.deleteImage(mContext, request.id)
        if (isSuccess) {
            return HttpResponseEntity.success()
        }

        return ErrorBuilder().module(HttpModule.ImageModule).error(HttpError.DeleteImageFail).build()
    }
}