package com.youngfeng.android.assistant.web.controller

import com.yanzhenjie.andserver.annotation.PostMapping
import com.yanzhenjie.andserver.annotation.RequestMapping
import com.yanzhenjie.andserver.annotation.ResponseBody
import com.yanzhenjie.andserver.annotation.RestController
import com.youngfeng.android.assistant.app.MobileAssistantApplication
import com.youngfeng.android.assistant.util.AudioUtil
import com.youngfeng.android.assistant.web.entity.AudioEntity
import com.youngfeng.android.assistant.web.entity.HttpResponseEntity

@RestController
@RequestMapping("/audio")
class AudioController {
    private val mContext by lazy { MobileAssistantApplication.getInstance() }

    @PostMapping("/all")
    @ResponseBody
    fun getAllAudios(): HttpResponseEntity<List<AudioEntity>> {
        val audios = AudioUtil.getAllAudios(mContext)
        return HttpResponseEntity.success(audios)
    }
}