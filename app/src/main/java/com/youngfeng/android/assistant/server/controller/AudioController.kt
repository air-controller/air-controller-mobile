package com.youngfeng.android.assistant.server.controller

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
import com.youngfeng.android.assistant.server.HttpError
import com.youngfeng.android.assistant.server.HttpModule
import com.youngfeng.android.assistant.server.entity.AudioEntity
import com.youngfeng.android.assistant.server.entity.HttpResponseEntity
import com.youngfeng.android.assistant.server.request.DeleteAudioRequest
import com.youngfeng.android.assistant.server.util.ErrorBuilder
import com.youngfeng.android.assistant.util.AudioUtil
import java.io.File
import java.util.Locale

@RestController
@RequestMapping("/audio")
class AudioController {
    private val mContext by lazy { AirControllerApp.getInstance() }

    @PostMapping("/all")
    @ResponseBody
    fun getAllAudios(): HttpResponseEntity<List<AudioEntity>> {
        val audios = AudioUtil.getAllAudios(mContext)
        return HttpResponseEntity.success(audios)
    }

    @PostMapping("/delete")
    @ResponseBody
    fun delete(httpRequest: HttpRequest, @RequestBody request: DeleteAudioRequest): HttpResponseEntity<Any> {
        val languageCode = httpRequest.getHeader("languageCode")
        val locale = if (!TextUtils.isEmpty(languageCode)) Locale(languageCode!!) else Locale("en")

        try {
            val paths = request.paths

            for (path in paths) {
                val audioFile = File(path)

                val isSuccess = audioFile.delete()
                if (!isSuccess) {
                    val response = ErrorBuilder().locale(locale).module(HttpModule.AudioModule).error(HttpError.DeleteAudioFail).build<Any>()
                    response.msg = mContext.getString(locale, R.string.delete_audio_file_fail).replace(
                        "%s",
                        audioFile.absolutePath
                    )
                    return response
                } else {
                    MediaScannerConnection.scanFile(mContext, arrayOf(audioFile.absolutePath), null, null)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val response = ErrorBuilder().locale(locale).module(HttpModule.AudioModule).error(HttpError.DeleteAudioFail).build<Any>()
            response.msg = e.message
            return response
        }

        return HttpResponseEntity.success()
    }

    @GetMapping("/item/{id}")
    fun findById(@PathVariable("id") id: String): AudioEntity {
        val audioEntity = AudioUtil.findById(mContext, id)

        if (null != audioEntity) {
            return audioEntity
        } else {
            throw IllegalArgumentException("Audio item is not exist, id: $id")
        }
    }
}
