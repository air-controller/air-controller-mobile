package com.youngfeng.android.assistant.web.controller

import android.Manifest
import android.content.pm.PackageManager
import android.os.Environment
import android.text.TextUtils
import androidx.core.content.ContextCompat
import com.yanzhenjie.andserver.annotation.*
import com.youngfeng.android.assistant.app.MobileAssistantApplication
import com.youngfeng.android.assistant.web.HttpError
import com.youngfeng.android.assistant.web.HttpModule
import com.youngfeng.android.assistant.web.entity.FileEntity
import com.youngfeng.android.assistant.web.entity.HttpResponseEntity
import com.youngfeng.android.assistant.web.request.GetFileListRequest
import com.youngfeng.android.assistant.web.util.ErrorBuilder
import java.io.File

@RestController
@RequestMapping("/file")
open class FileController {

    @PostMapping("/list")
    @ResponseBody
    fun getFileList(@RequestBody requestBody: GetFileListRequest): HttpResponseEntity<List<FileEntity>> {
        // 先判断是否存在读取外部存储权限
        if (ContextCompat.checkSelfPermission(MobileAssistantApplication.getInstance(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            return ErrorBuilder().module(HttpModule.FileModule).error(HttpError.NoReadExternalStoragePerm).build();
        }

        var path = requestBody.path
        if (TextUtils.isEmpty(path)) {
            path = Environment.getExternalStorageDirectory().absolutePath;
        }

        val dir = File(path)

        if (!dir.isDirectory) {
            return ErrorBuilder().module(HttpModule.FileModule).error(HttpError.FileIsNotADir).build()
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