package com.youngfeng.android.assistant.server.controller

import android.Manifest
import android.media.MediaScannerConnection
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yanzhenjie.andserver.annotation.CrossOrigin
import com.yanzhenjie.andserver.annotation.GetMapping
import com.yanzhenjie.andserver.annotation.PathVariable
import com.yanzhenjie.andserver.annotation.PostMapping
import com.yanzhenjie.andserver.annotation.QueryParam
import com.yanzhenjie.andserver.annotation.RequestBody
import com.yanzhenjie.andserver.annotation.RequestMapping
import com.yanzhenjie.andserver.annotation.RequestParam
import com.yanzhenjie.andserver.annotation.ResponseBody
import com.yanzhenjie.andserver.annotation.RestController
import com.yanzhenjie.andserver.http.HttpRequest
import com.yanzhenjie.andserver.http.HttpResponse
import com.yanzhenjie.andserver.http.multipart.MultipartFile
import com.yanzhenjie.andserver.util.MediaType
import com.youngfeng.android.assistant.app.AirControllerApp
import com.youngfeng.android.assistant.db.RoomDatabaseHolder
import com.youngfeng.android.assistant.db.entity.ZipFileRecord
import com.youngfeng.android.assistant.event.Permission
import com.youngfeng.android.assistant.event.RequestPermissionsEvent
import com.youngfeng.android.assistant.server.HttpError
import com.youngfeng.android.assistant.server.HttpModule
import com.youngfeng.android.assistant.server.entity.AudioEntity
import com.youngfeng.android.assistant.server.entity.DeleteResult
import com.youngfeng.android.assistant.server.entity.HttpResponseEntity
import com.youngfeng.android.assistant.server.request.IdsRequest
import com.youngfeng.android.assistant.server.response.RangeSupportResponseBody
import com.youngfeng.android.assistant.server.util.ErrorBuilder
import com.youngfeng.android.assistant.util.AudioUtil
import com.youngfeng.android.assistant.util.CommonUtil
import com.youngfeng.android.assistant.util.MD5Helper
import com.youngfeng.android.assistant.util.PathHelper
import net.lingala.zip4j.ZipFile
import org.greenrobot.eventbus.EventBus
import pub.devrel.easypermissions.EasyPermissions
import java.io.File

@CrossOrigin
@RestController
@RequestMapping("/audio")
class AudioController {
    private val mContext by lazy { AirControllerApp.getInstance() }
    private val mGson by lazy { Gson() }

    @PostMapping("/all")
    @ResponseBody
    fun getAllAudios(): HttpResponseEntity<List<AudioEntity>> {
        val audios = AudioUtil.getAllAudios(mContext)
        return HttpResponseEntity.success(audios)
    }

    @PostMapping("/delete")
    @ResponseBody
    fun delete(
        @RequestBody request: IdsRequest
    ): HttpResponseEntity<Any> {
        val deleteResult = AudioUtil.deleteByIds(mContext, request.ids)

        return when (deleteResult.result) {
            DeleteResult.SUCCESS -> {
                HttpResponseEntity.success()
            }
            DeleteResult.PARTIAL -> {
                val response = ErrorBuilder().module(HttpModule.AudioModule).error(HttpError.DeleteAudioPartialFailure).build<Any>()
                response.msg = response.msg?.format("%s", deleteResult.failedCount.toString())
                response
            }
            else -> {
                ErrorBuilder().module(HttpModule.AudioModule).error(HttpError.DeleteAudioFail).build()
            }
        }
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

    @GetMapping("/download")
    fun download(@QueryParam("ids") ids: String): File? {
        val idList = mGson.fromJson<List<String>>(ids, object : TypeToken<List<String>>() {}.type)

        if (idList.isEmpty()) return null

        if (idList.size == 1) {
            val id = idList[0]
            val image = AudioUtil.findById(mContext, id) ?: return null

            val file = File(image.path)

            if (file.isFile) {
                if (file.exists()) {
                    return file
                }
            }

            return null
        }

        val audios = mutableListOf<AudioEntity>()
        idList.forEach { id ->
            AudioUtil.findById(mContext, id)?.apply {
                audios.add(this)
            }
        }

        CommonUtil.findZipCacheWithPaths(mContext, audios.map { it.path })?.apply { return this }

        return compressAudios(audios).file
    }

    private fun compressAudios(audios: List<AudioEntity>): ZipFile {
        val db = RoomDatabaseHolder.getRoomDatabase(mContext)
        val zipFileRecordDao = db.zipFileRecordDao()

        val originalFilesMD5Json = mutableMapOf<String, String>()
        val sortedOriginalPathsMD5 = MD5Helper.md5(audios.map { it.path }.sorted().joinToString(","))

        val zipFile = ZipFile("${PathHelper.zipFileDir().absolutePath}/audios_${System.currentTimeMillis()}.zip")

        audios.forEach {
            val file = File(it.path)

            zipFile.addFile(file)

            originalFilesMD5Json[file.absolutePath] = MD5Helper.md5(file)
        }

        val record = ZipFileRecord(
            name = zipFile.file.name,
            path = zipFile.file.path,
            md5 = MD5Helper.md5(zipFile.file),
            originalFilesMD5 = mGson.toJson(originalFilesMD5Json),
            originalPathsMD5 = sortedOriginalPathsMD5,
            createTime = System.currentTimeMillis(),
            isMultiOriginalFile = true
        )

        zipFileRecordDao.insert(record)

        return zipFile
    }
}
