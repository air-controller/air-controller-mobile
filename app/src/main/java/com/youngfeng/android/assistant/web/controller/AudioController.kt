package com.youngfeng.android.assistant.web.controller

import android.media.MediaScannerConnection
import com.yanzhenjie.andserver.annotation.PostMapping
import com.yanzhenjie.andserver.annotation.RequestBody
import com.yanzhenjie.andserver.annotation.RequestMapping
import com.yanzhenjie.andserver.annotation.ResponseBody
import com.yanzhenjie.andserver.annotation.RestController
import com.youngfeng.android.assistant.R
import com.youngfeng.android.assistant.app.MobileAssistantApplication
import com.youngfeng.android.assistant.util.AudioUtil
import com.youngfeng.android.assistant.web.HttpError
import com.youngfeng.android.assistant.web.HttpModule
import com.youngfeng.android.assistant.web.entity.AudioEntity
import com.youngfeng.android.assistant.web.entity.HttpResponseEntity
import com.youngfeng.android.assistant.web.request.DeleteAudioRequest
import com.youngfeng.android.assistant.web.util.ErrorBuilder
import java.io.File
import java.lang.Exception

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

    @PostMapping("/delete")
    @ResponseBody
    fun delete(@RequestBody request: DeleteAudioRequest): HttpResponseEntity<Any> {
        try {
            val paths = request.paths

            for (path in paths) {
                val audioFile = File(path)

                val isSuccess = audioFile.delete()
                if (!isSuccess) {
                   val response =  ErrorBuilder().module(HttpModule.AudioModule).error(HttpError.DeleteAudioFail).build<Any>()
                    response.msg = mContext.getString(R.string.delete_audio_file_fail).replace("%s", audioFile.absolutePath)
                    return response
                } else {
                    MediaScannerConnection.scanFile(mContext, arrayOf(audioFile.absolutePath), null, null)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val response =  ErrorBuilder().module(HttpModule.AudioModule).error(HttpError.DeleteAudioFail).build<Any>()
            response.msg = e.message
            return response
        }

        return HttpResponseEntity.success()
    }
}