package com.youngfeng.android.assistant.web.controller

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import androidx.core.content.ContextCompat
import com.yanzhenjie.andserver.annotation.*
import com.yanzhenjie.andserver.http.HttpRequest
import com.youngfeng.android.assistant.app.AirControllerApp
import com.youngfeng.android.assistant.ext.isValidFileName
import com.youngfeng.android.assistant.web.HttpError
import com.youngfeng.android.assistant.web.HttpModule
import com.youngfeng.android.assistant.web.entity.FileEntity
import com.youngfeng.android.assistant.web.entity.HttpResponseEntity
import com.youngfeng.android.assistant.web.request.*
import com.youngfeng.android.assistant.web.util.ErrorBuilder
import java.io.*
import java.lang.Exception
import java.util.Locale

@RestController
@RequestMapping("/file")
open class FileController {
    private val mContext by lazy { AirControllerApp.getInstance() }

    @PostMapping("/list")
    @ResponseBody
    fun getFileList(httpRequest: HttpRequest, @RequestBody requestBody: GetFileListRequest): HttpResponseEntity<List<FileEntity>> {
        val languageCode = httpRequest.getHeader("languageCode")
        val locale = if (!TextUtils.isEmpty(languageCode)) Locale(languageCode!!) else Locale("en")

        // 先判断是否存在读取外部存储权限
        if (ContextCompat.checkSelfPermission(
                AirControllerApp.getInstance(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return ErrorBuilder().locale(locale).module(HttpModule.FileModule).error(HttpError.NoReadExternalStoragePerm).build()
        }

        var path = requestBody.path
        if (TextUtils.isEmpty(path)) {
            path = Environment.getExternalStorageDirectory().absolutePath
        }

        val dir = File(path)

        if (!dir.isDirectory) {
            return ErrorBuilder().locale(locale).module(HttpModule.FileModule).error(HttpError.FileIsNotADir).build()
        }

        val files = dir.listFiles()

        var data = mutableListOf<FileEntity>()
        files?.forEach {
            val fileEntity = FileEntity(
                it.name,
                it.parentFile?.absolutePath ?: "",
                if (it.isFile) it.length() else 0,
                it.isDirectory,
                it.lastModified(),
                it.listFiles()?.isEmpty() ?: false
            )
            data.add(fileEntity)
        }

        data = data.sortedWith(object : Comparator<FileEntity> {
            override fun compare(a: FileEntity, b: FileEntity): Int {
                if (a.isDir && !b.isDir) {
                    return -1;
                }

                if (!a.isDir && b.isDir) {
                    return 1;
                }

                return a.name.lowercase().compareTo(b.name.lowercase());
            }
        }).toMutableList()

        for (entity in data) {
            Log.e("排序", "${entity.name}")
        }

        return HttpResponseEntity.success(data)
    }

    @PostMapping("/create")
    @ResponseBody
    fun createFile(httpRequest: HttpRequest, @RequestBody request: CreateFileRequest): HttpResponseEntity<Map<String, Any>> {
        val languageCode = httpRequest.getHeader("languageCode")
        val locale = if (!TextUtils.isEmpty(languageCode)) Locale(languageCode!!) else Locale("en")

        // 先判断是否存在写入外部存储权限
        if (ContextCompat.checkSelfPermission(
                AirControllerApp.getInstance(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return ErrorBuilder().locale(locale).module(HttpModule.FileModule).error(HttpError.NoWriteExternalStoragePerm).build()
        }

        val fileName = request.name
        if (TextUtils.isEmpty(fileName)) {
            return ErrorBuilder().locale(locale).module(HttpModule.FileModule).error(HttpError.FileNameEmpty).build()
        }

        if (!fileName.isValidFileName()) {
            return ErrorBuilder().locale(locale).module(HttpModule.FileModule).error(HttpError.InvalidFileName).build()
        }

        val folder = request.folder

        if (TextUtils.isEmpty(folder)) {
            return ErrorBuilder().locale(locale).module(HttpModule.FileModule).error(HttpError.FolderCantEmpty).build()
        }

        val file = File("$folder/$fileName")

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
                    ErrorBuilder().locale(locale).module(HttpModule.FileModule).error(HttpError.CreateFileFail)
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
                    ErrorBuilder().locale(locale).module(HttpModule.FileModule).error(HttpError.CreateFileFail)
                        .build()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val response = ErrorBuilder().locale(locale).module(HttpModule.FileModule).error(HttpError.CreateFileFail).build<Map<String, Any>>()
            response.msg = e.message
            return response
        }
    }

    /**
     * 删除文件.
     */
    @PostMapping("/delete")
    @ResponseBody
    fun delete(httpRequest: HttpRequest, @RequestBody request: DeleteFileRequest): HttpResponseEntity<Map<String, Any>> {
        val languageCode = httpRequest.getHeader("languageCode")
        val locale = if (!TextUtils.isEmpty(languageCode)) Locale(languageCode!!) else Locale("en")

        // 先判断是否存在写入外部存储权限
        if (ContextCompat.checkSelfPermission(
                AirControllerApp.getInstance(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return ErrorBuilder().locale(locale).module(HttpModule.FileModule).error(HttpError.NoWriteExternalStoragePerm).build()
        }

        val path = request.file

        if (TextUtils.isEmpty(path)) {
            return ErrorBuilder().locale(locale).module(HttpModule.FileModule).error(HttpError.FilePathCantEmpty).build()
        }

        val file = File(path)
        try {
            val isSuccess = file.deleteRecursively()
            return if (isSuccess) {
                HttpResponseEntity.success(
                    mapOf(
                        "path" to path,
                        "isDir" to request.isDir
                    )
                )
            } else {
                ErrorBuilder().locale(locale).module(HttpModule.FileModule).error(HttpError.DeleteFileFail).build()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val response = ErrorBuilder().locale(locale).module(HttpModule.FileModule).error(HttpError.DeleteFileFail).build<Map<String, Any>>()
            response.msg = e.message
            return response
        }
    }

    /**
     * 删除多个文件
     */
    @PostMapping("/deleteMulti")
    @ResponseBody
    fun deleteMulti(httpRequest: HttpRequest, @RequestBody request: DeleteMultiFileRequest): HttpResponseEntity<Any> {
        val languageCode = httpRequest.getHeader("languageCode")
        val locale = if (!TextUtils.isEmpty(languageCode)) Locale(languageCode!!) else Locale("en")

        // 先判断是否存在写入外部存储权限
        if (ContextCompat.checkSelfPermission(
                AirControllerApp.getInstance(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return ErrorBuilder().locale(locale).module(HttpModule.FileModule).error(HttpError.NoWriteExternalStoragePerm).build()
        }

        try {
            val paths = request.paths

            var deleteCount = 0

            paths.forEachIndexed { index, path ->
                val file = File(path)

                if (file.exists()) {
                    if (file.isDirectory) {
                        if (file.deleteRecursively()) {
                            deleteCount++
                            MediaScannerConnection.scanFile(mContext, arrayOf(path), null, null)
                        }
                    } else {
                        if (file.delete()) {
                            deleteCount++
                            MediaScannerConnection.scanFile(mContext, arrayOf(path), null, null)
                        }
                    }
                }
            }

            if (deleteCount == paths.size) {
                return HttpResponseEntity.success()
            } else {
                val response = ErrorBuilder().locale(locale).module(HttpModule.FileModule).error(HttpError.DeleteFileFail).build<Any>()
                response.msg = "${deleteCount}删除成功，${paths.size - deleteCount}删除失败"
                return response
            }
        } catch (e: IOException) {
            e.printStackTrace()
            val response = ErrorBuilder().locale(locale).module(HttpModule.FileModule).error(HttpError.DeleteFileFail).build<Any>()
            response.msg = e.message
            return response
        }
    }

    /**
     * 重命名文件.
     */
    @PostMapping("/rename")
    @ResponseBody
    fun rename(httpRequest: HttpRequest, @RequestBody request: RenameFileRequest): HttpResponseEntity<Map<String, String>> {
        val languageCode = httpRequest.getHeader("languageCode")
        val locale = if (!TextUtils.isEmpty(languageCode)) Locale(languageCode!!) else Locale("en")

        // 先判断是否存在写入外部存储权限
        if (ContextCompat.checkSelfPermission(
                AirControllerApp.getInstance(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return ErrorBuilder().locale(locale).module(HttpModule.FileModule).error(HttpError.NoWriteExternalStoragePerm).build()
        }

        val fileName = request.file

        if (TextUtils.isEmpty(fileName)) {
            return ErrorBuilder().locale(locale).module(HttpModule.FileModule).error(HttpError.FileNameEmpty).build()
        }

        val folder = request.folder

        val file = File("$folder/$fileName")

        val newName = request.newName
        val newFile = File("$folder/$newName")

        try {
            val isSuccess = file.renameTo(newFile)
            return if (isSuccess) {
                HttpResponseEntity.success(
                    mapOf(
                        "folder" to folder,
                        "newName" to newName
                    )
                )
            } else {
                ErrorBuilder().locale(locale).module(HttpModule.FileModule).error(HttpError.RenameFileFail).build()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val response = ErrorBuilder().locale(locale).module(HttpModule.FileModule).error(HttpError.RenameFileFail).build<Map<String, String>>()
            response.msg = e.message
            return response
        }
    }

    @PostMapping("/move")
    @ResponseBody
    fun move(httpRequest: HttpRequest, @RequestBody request: MoveFileRequest): HttpResponseEntity<Map<String, String>> {
        val languageCode = httpRequest.getHeader("languageCode")
        val locale = if (!TextUtils.isEmpty(languageCode)) Locale(languageCode!!) else Locale("en")

        // 先判断是否存在写入外部存储权限
        if (ContextCompat.checkSelfPermission(
                AirControllerApp.getInstance(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return ErrorBuilder().locale(locale).module(HttpModule.FileModule).error(HttpError.NoWriteExternalStoragePerm).build()
        }

        val fileName = request.fileName

        if (TextUtils.isEmpty(fileName)) {
            return ErrorBuilder().locale(locale).module(HttpModule.FileModule).error(HttpError.FileNameEmpty).build()
        }

        val oldFolder = request.oldFolder

        val file = File("$oldFolder/$fileName")

        val newFolder = request.newFolder
        val newFile = File("$newFolder/$$fileName")

        try {
            val isSuccess = file.renameTo(newFile)
            return if (isSuccess) {
                HttpResponseEntity.success(
                    mapOf(
                        "newFolder" to newFolder,
                        "name" to fileName
                    )
                )
            } else {
                ErrorBuilder().locale(locale).module(HttpModule.FileModule).error(HttpError.MoveFileFail).build()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val response = ErrorBuilder().locale(locale).module(HttpModule.FileModule).error(HttpError.MoveFileFail).build<Map<String, String>>()
            response.msg = e.message
            return response
        }
    }

    @ResponseBody
    @PostMapping("/downloadedFiles")
    fun getDownloadFileList(httpRequest: HttpRequest): HttpResponseEntity<List<FileEntity>> {
        val languageCode = httpRequest.getHeader("languageCode")
        val locale = if (!TextUtils.isEmpty(languageCode)) Locale(languageCode!!) else Locale("en")

        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        if (null == downloadDir) {
            return ErrorBuilder().locale(locale).module(HttpModule.Download).error(HttpError.GetDownloadDirFail).build()
        }

        if (!downloadDir.exists()) {
            return ErrorBuilder().locale(locale).module(HttpModule.Download).error(HttpError.DownloadDirNotExist).build()
        }

        var data = downloadDir.listFiles()?.map {
            FileEntity(
                isDir = it.isDirectory,
                name = it.name,
                folder = downloadDir.absolutePath,
                size = if (it.isFile) it.length() else it.totalSpace,
                changeDate = it.lastModified(),
                isEmpty = if (it.isDirectory) it.listFiles()?.size ?: -1 <= 0 else true
            )
        }

        data = data?.sortedWith(object : Comparator<FileEntity> {
            override fun compare(a: FileEntity, b: FileEntity): Int {
                if (a.isDir && !b.isDir) {
                    return -1;
                }

                if (!a.isDir && b.isDir) {
                    return 1;
                }

                return a.name.lowercase().compareTo(b.name.lowercase());
            }
        })

        if (null != data) {
            for (entity in data) {
                Log.e("排序", "${entity.name}")
            }
        }
        return HttpResponseEntity.success(data)
    }
}
