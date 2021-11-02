package com.youngfeng.android.assistant.web.controller

import com.yanzhenjie.andserver.annotation.PostMapping
import com.yanzhenjie.andserver.annotation.RequestMapping
import com.yanzhenjie.andserver.annotation.RestController

@RestController
@RequestMapping("/video")
class VideoController {

    @PostMapping("/folders")
    fun videoFolders() {

    }

    @PostMapping("/list")
    fun getVideos() {

    }

    @PostMapping("/delete")
    fun delete() {

    }
}