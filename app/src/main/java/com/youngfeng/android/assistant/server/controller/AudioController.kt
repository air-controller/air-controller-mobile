package com.youngfeng.android.assistant.server.controller

import android.Manifest
import android.media.MediaScannerConnection
import android.text.TextUtils
import com.yanzhenjie.andserver.annotation.CrossOrigin
import com.yanzhenjie.andserver.annotation.GetMapping
import com.yanzhenjie.andserver.annotation.PathVariable
import com.yanzhenjie.andserver.annotation.PostMapping
import com.yanzhenjie.andserver.annotation.RequestBody
import com.yanzhenjie.andserver.annotation.RequestMapping
import com.yanzhenjie.andserver.annotation.RequestParam
import com.yanzhenjie.andserver.annotation.ResponseBody
import com.yanzhenjie.andserver.annotation.RestController
import com.yanzhenjie.andserver.http.HttpRequest
import com.yanzhenjie.andserver.http.HttpResponse
import com.yanzhenjie.andserver.http.multipart.MultipartFile
import com.yanzhenjie.andserver.util.MediaType
import com.youngfeng.android.assistant.R
import com.youngfeng.android.assistant.app.AirControllerApp
import com.youngfeng.android.assistant.event.Permission
import com.youngfeng.android.assistant.event.RequestPermissionsEvent
import com.youngfeng.android.assistant.ext.getString
import com.youngfeng.android.assistant.server.HttpError
import com.youngfeng.android.assistant.server.HttpModule
import com.youngfeng.android.assistant.server.entity.AudioEntity
import com.youngfeng.android.assistant.server.entity.HttpResponseEntity
import com.youngfeng.android.assistant.server.request.DeleteAudioRequest
import com.youngfeng.android.assistant.server.response.RangeSupportResponseBody
import com.youngfeng.android.assistant.server.util.ErrorBuilder
import com.youngfeng.android.assistant.util.AudioUtil
import com.youngfeng.android.assistant.util.PathHelper
import org.greenrobot.eventbus.EventBus
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.util.Locale

@CrossOrigin
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
    fun delete(
        httpRequest: HttpRequest,
        @RequestBody request: DeleteAudioRequest
    ): HttpResponseEntity<Any> {
        val languageCode = httpRequest.getHeader("languageCode")
        val locale = if (!TextUtils.isEmpty(languageCode)) Locale(languageCode!!) else Locale("en")

        try {
            val paths = request.paths

            for (path in paths) {
                val audioFile = File(path)

                val isSuccess = audioFile.delete()
                if (!isSuccess) {
                    val response = ErrorBuilder().locale(locale).module(HttpModule.AudioModule)
                        .error(HttpError.DeleteAudioFail).build<Any>()
                    response.msg =
                        mContext.getString(locale, R.string.delete_audio_file_fail).replace(
                            "%s",
                            audioFile.absolutePath
                        )
                    return response
                } else {
                    MediaScannerConnection.scanFile(
                        mContext,
                        arrayOf(audioFile.absolutePath),
                        null,
                        null
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val response = ErrorBuilder().locale(locale).module(HttpModule.AudioModule)
                .error(HttpError.DeleteAudioFail).build<Any>()
            response.msg = e.message
            return response
        }

        return HttpResponseEntity.success()
    }

    @GetMapping("/item/{id}")
    fun findById(request: HttpRequest, response: HttpResponse, @PathVariable("id") id: String): com.yanzhenjie.andserver.http.ResponseBody {
        val audioEntity = AudioUtil.findById(mContext, id)

        if (null != audioEntity) {
            val rangeHeader = request.getHeader("Range")
            val audioFile = File(audioEntity.path)

            return RangeSupportResponseBody(
                contentType = MediaType("audio", audioFile.extension),
                file = audioFile,
                rangeHeader = rangeHeader
            ).attachToResponse(response)
        } else {
            throw IllegalArgumentException("Audio item is not exist, id: $id")
        }
    }

    @ResponseBody
    @PostMapping("/uploadAudios")
    fun uploadAudios(@RequestParam("audios") audios: Array<MultipartFile>): HttpResponseEntity<Any> {
        if (!EasyPermissions.hasPermissions(
                mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        ) {
            EventBus.getDefault().post(
                RequestPermissionsEvent(
                    arrayOf(
                        Permission.WriteExternalStorage,
                    )
                )
            )
            return HttpResponseEntity.commonError(HttpError.LackOfNecessaryPermissions)
        }

        PathHelper.audioRootDir()?.let { audioRootDir ->
            audios.onEach { audio ->
                val file = File(audioRootDir, "AirController/${audio.filename}")
                audio.transferTo(file)
                MediaScannerConnection.scanFile(
                    mContext, arrayOf(file.path), null, null
                )
            }
            return HttpResponseEntity.success()
        } ?: return ErrorBuilder().module(HttpModule.AudioModule).error(HttpError.UploadAudioFail)
            .build()
    }
}
