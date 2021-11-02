package com.youngfeng.android.assistant.web.controller

import com.yanzhenjie.andserver.annotation.*
import com.youngfeng.android.assistant.web.HttpError
import com.youngfeng.android.assistant.web.HttpModule
import com.youngfeng.android.assistant.web.entity.FileEntity
import com.youngfeng.android.assistant.web.entity.HttpResponseEntity
import com.youngfeng.android.assistant.web.request.GetFileListRequest
import com.youngfeng.android.assistant.web.util.ErrorBuilder
import java.io.File

@RestController
@RequestMapping("/file")
class FileController {

    /**
     * 以下代码仅为举例，逻辑仍需完善
     */
    @PostMapping("/list")
    @ResponseBody
    fun getFileList(@RequestBody requestBody: GetFileListRequest): HttpResponseEntity<List<FileEntity>> {
        val dir = File(requestBody.path)

        if (!dir.isDirectory) {
            return ErrorBuilder().mode(HttpModule.FileModule).error(HttpError.FileIsNotADir).build()
        }

        val files = dir.listFiles()

        val data = mutableListOf<FileEntity>()
        files?.forEach {
            val fileEntity = FileEntity(
                it.name,
                it.absolutePath,
                if (it.isFile) it.totalSpace else 0, it.isDirectory,
                it.listFiles()?.isEmpty()
            )
            data.add(fileEntity)
        }

        return HttpResponseEntity.success(data)
    }

}