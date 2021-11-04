package com.youngfeng.android.assistant.web.controller

import android.Manifest
import android.content.pm.PackageManager
import android.os.Environment
import android.text.TextUtils
import androidx.core.content.ContextCompat
import com.yanzhenjie.andserver.annotation.*
import com.youngfeng.android.assistant.app.MobileAssistantApplication
import com.youngfeng.android.assistant.ext.isValidFileName
import com.youngfeng.android.assistant.web.HttpError
import com.youngfeng.android.assistant.web.HttpModule
import com.youngfeng.android.assistant.web.entity.FileEntity
import com.youngfeng.android.assistant.web.entity.HttpResponseEntity
import com.youngfeng.android.assistant.web.request.CreateFileRequest
import com.youngfeng.android.assistant.web.request.DeleteFileRequest
import com.youngfeng.android.assistant.web.request.GetFileListRequest
import com.youngfeng.android.assistant.web.request.RenameFileRequest
import com.youngfeng.android.assistant.web.util.ErrorBuilder
import java.io.File
import java.lang.Exception

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

    @PostMapping("/create")
    @ResponseBody
    fun createFile(@RequestBody request: CreateFileRequest): HttpResponseEntity<Map<String, Any>> {
        // 先判断是否存在写入外部存储权限
        if (ContextCompat.checkSelfPermission(MobileAssistantApplication.getInstance(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            return ErrorBuilder().module(HttpModule.FileModule).error(HttpError.NoWriteExternalStoragePerm).build();
        }

        val fileName = request.name
        if (TextUtils.isEmpty(fileName)) {
            return ErrorBuilder().module(HttpModule.FileModule).error(HttpError.FileNameEmpty).build()
        }

        if (!fileName.isValidFileName()) {
            return ErrorBuilder().module(HttpModule.FileModule).error(HttpError.InvalidFileName).build()
        }

        val folder = request.folder

        if (TextUtils.isEmpty(folder)) {
            return ErrorBuilder().module(HttpModule.FileModule).error(HttpError.FolderCantEmpty).build()
        }

        val file = File("$folder/$fileName")

        // TODO: 这里应该先判断文件是否已存在

        val type = request.type
        try {
            if (type == CreateFileRequest.TYPE_DIR) {
                val isSuccess = file.mkdir()
                return if (isSuccess) {
                    HttpResponseEntity.success(
                        mapOf(
                            "name" to fileName,
                            "isDir" to true
                        )
                    )
                } else {
                    ErrorBuilder().module(HttpModule.FileModule).error(HttpError.CreateFileFail)
                        .build()
                }
            } else {
                val isSuccess = file.createNewFile()
                return if (isSuccess) {
                    HttpResponseEntity.success(
                        mapOf(
                            "name" to fileName,
                            "isDir" to false
                        )
                    )
                } else {
                    ErrorBuilder().module(HttpModule.FileModule).error(HttpError.CreateFileFail)
                        .build()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val response = ErrorBuilder().module(HttpModule.FileModule).error(HttpError.CreateFileFail).build<Map<String, Any>>()
            response.msg = e.message
            return response
        }
    }

    /**
     * 删除文件
     */
    @PostMapping("/delete")
    @ResponseBody
    fun delete(@RequestBody request: DeleteFileRequest): HttpResponseEntity<Map<String, Any>> {
        // 先判断是否存在写入外部存储权限
        if (ContextCompat.checkSelfPermission(MobileAssistantApplication.getInstance(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            return ErrorBuilder().module(HttpModule.FileModule).error(HttpError.NoWriteExternalStoragePerm).build();
        }

        val path = request.file

        if (TextUtils.isEmpty(path)) {
            return ErrorBuilder().module(HttpModule.FileModule).error(HttpError.FilePathCantEmpty).build();
        }

        val file = File(path)
        try {
            val isSuccess = file.deleteRecursively()
            return if (isSuccess) {
                HttpResponseEntity.success(mapOf(
                    "path" to path,
                    "isDir" to request.isDir
                ))
            } else {
                ErrorBuilder().module(HttpModule.FileModule).error(HttpError.DeleteFileFail).build()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val response = ErrorBuilder().module(HttpModule.FileModule).error(HttpError.DeleteFileFail).build<Map<String, Any>>()
            response.msg = e.message
            return response
        }
    }

    /**
     * 重命名文件
     */
    @PostMapping("/rename")
    @ResponseBody
    fun rename(@RequestBody request: RenameFileRequest): HttpResponseEntity<Map<String, String>> {
        // 先判断是否存在写入外部存储权限
        if (ContextCompat.checkSelfPermission(MobileAssistantApplication.getInstance(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            return ErrorBuilder().module(HttpModule.FileModule).error(HttpError.NoWriteExternalStoragePerm).build();
        }

        val fileName = request.file

        if (TextUtils.isEmpty(fileName)) {
            return ErrorBuilder().module(HttpModule.FileModule).error(HttpError.FileNameEmpty).build();
        }

        val folder = request.folder

        val file = File("$folder/$fileName")

        val newName = request.newName
        val newFile = File("$folder/$newName")

        try {
            val isSuccess = file.renameTo(newFile)
            return if (isSuccess) {
                HttpResponseEntity.success(mapOf(
                    "folder" to folder,
                    "newName" to newName
                ))
            } else {
                ErrorBuilder().module(HttpModule.FileModule).error(HttpError.RenameFileFail).build();
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val response = ErrorBuilder().module(HttpModule.FileModule).error(HttpError.RenameFileFail).build<Map<String, String>>()
            response.msg = e.message
            return response
        }
    }
}