package com.youngfeng.android.assistant.web.controller

import com.yanzhenjie.andserver.annotation.PostMapping
import com.yanzhenjie.andserver.annotation.RequestMapping
import com.yanzhenjie.andserver.annotation.ResponseBody
import com.yanzhenjie.andserver.annotation.RestController
import com.youngfeng.android.assistant.app.MobileAssistantApplication
import com.youngfeng.android.assistant.util.VideoUtil
import com.youngfeng.android.assistant.web.entity.HttpResponseEntity
import com.youngfeng.android.assistant.web.entity.VideoFolder

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

    @PostMapping("/list")
    fun getVideos() {
    }

    @PostMapping("/delete")
    fun delete() {
    }
}
