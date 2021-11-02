package com.youngfeng.android.assistant.web.controller

import com.yanzhenjie.andserver.annotation.PostMapping
import com.yanzhenjie.andserver.annotation.RequestMapping
import com.yanzhenjie.andserver.annotation.RestController
import com.youngfeng.android.assistant.web.entity.*

@RestController
@RequestMapping("/image")
class ImageController {

    @PostMapping("/albums")
    fun getAlbums(): HttpResponseEntity<List<AlbumEntity>> {
        throw NotImplementedError()
    }

    @PostMapping("/all")
    fun getAllImages(): HttpResponseEntity<List<ImageEntity>> {
        throw NotImplementedError()
    }

    @PostMapping("/daily")
    fun getDailyImages(): HttpResponseEntity<List<DailyImageEntity>> {
        throw NotImplementedError()
    }

    @PostMapping("/monthly")
    fun getMonthlyImages(): HttpResponseEntity<List<MonthlyImageEntity>> {
        throw NotImplementedError()
    }
}