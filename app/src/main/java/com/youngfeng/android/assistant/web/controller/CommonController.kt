package com.youngfeng.android.assistant.web.controller

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import com.yanzhenjie.andserver.annotation.PostMapping
import com.yanzhenjie.andserver.annotation.RequestMapping
import com.yanzhenjie.andserver.annotation.RequestParam
import com.yanzhenjie.andserver.annotation.ResponseBody
import com.yanzhenjie.andserver.annotation.RestController
import com.yanzhenjie.andserver.http.HttpRequest
import com.yanzhenjie.andserver.http.multipart.MultipartFile
import com.youngfeng.android.assistant.app.MobileAssistantApplication
import com.youngfeng.android.assistant.db.RoomDatabaseHolder
import com.youngfeng.android.assistant.db.entity.UploadFileRecord
import com.youngfeng.android.assistant.model.MobileInfo
import com.youngfeng.android.assistant.util.CommonUtil
import com.youngfeng.android.assistant.util.MD5Helper
import com.youngfeng.android.assistant.util.PathHelper
import com.youngfeng.android.assistant.web.HttpError
import com.youngfeng.android.assistant.web.HttpModule
import com.youngfeng.android.assistant.web.entity.HttpResponseEntity
import com.youngfeng.android.assistant.web.entity.InstalledAppEntity
import com.youngfeng.android.assistant.web.util.ErrorBuilder
import timber.log.Timber
import java.io.File
import java.util.Locale

@RestController
@RequestMapping("/common")
class CommonController {
    private val mContext by lazy { MobileAssistantApplication.getInstance() }

    @PostMapping("/mobileInfo")
    @ResponseBody
    fun getMobileInfo(): HttpResponseEntity<MobileInfo> {
        val batteryLevel = CommonUtil.getBatteryLevel(mContext)
        val storageSize = CommonUtil.getExternalStorageSize()

        val mobileInfo = MobileInfo(batteryLevel, storageSize)

        return HttpResponseEntity.success(mobileInfo)
    }

    @PostMapping("/installedApps")
    @ResponseBody
    fun getInstalledApps(): HttpResponseEntity<List<InstalledAppEntity>> {
        val packageManager = mContext.packageManager
        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        val apps = mutableListOf<InstalledAppEntity>()
        packages.forEach {
            val appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(it.packageName, 0)).toString()
            val packageInfo = packageManager.getPackageInfo(it.packageName, 0)
            val versionName = packageInfo.versionName
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            }
            val packageName = it.packageName
            val appFile = File(it.publicSourceDir)
            val size = appFile.length()
            val enable = it.enabled

            val app = InstalledAppEntity(
                isSystemApp = (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                name = appName,
                versionName = versionName,
                versionCode = versionCode,
                packageName = packageName,
                size = size,
                enable = enable
            )
            apps.add(app)
        }

        return HttpResponseEntity.success(apps)
    }

    @ResponseBody
    @PostMapping("/install")
    fun installApp(
        httpRequest: HttpRequest,
        @RequestParam("bundle") bundle: MultipartFile,
        @RequestParam("md5") md5: String
    ): HttpResponseEntity<Any> {
        val db = RoomDatabaseHolder.getRoomDatabase(mContext)
        val uploadFileRecordDao = db.uploadFileRecordDao()

        val size = bundle.size
        val uploadTime = System.currentTimeMillis()
        val fileName = bundle.filename ?: "${uploadTime}.apk"

        val uploadDir = PathHelper.uploadFileDir()

        val destFile = File(uploadDir, fileName)
        try {
            bundle.transferTo(destFile)

            CommonUtil.install(mContext, destFile)

            val newUploadFileRecord = UploadFileRecord(
                id = 0,
                name = fileName,
                path = destFile.absolutePath,
                size = size,
                uploadTime = uploadTime,
                md5 = MD5Helper.md5(destFile)
            )
            uploadFileRecordDao.insert(newUploadFileRecord)

            return HttpResponseEntity.success()
        } catch (e: Exception) {
            Timber.e("Upload install bundle failure, reason: ${e.message}")
            val languageCode = httpRequest.getHeader("languageCode")
            val locale = if (!TextUtils.isEmpty(languageCode)) Locale(languageCode!!) else Locale("en")

            val response = ErrorBuilder().locale(locale).module(HttpModule.CommonModule).error(HttpError.UploadInstallFileFailure).build<Any>()
            response.msg = mContext.getString(HttpError.UploadInstallFileFailure.value).replace("%s", "${e.message}")
            return response
        }
    }

    @ResponseBody
    @PostMapping("/tryToInstallFromCache")
    fun tryToInstallFromCache(
        httpRequest: HttpRequest,
        @RequestParam("fileName") fileName: String,
        @RequestParam("md5") md5: String
    ): HttpResponseEntity<Any> {
        val db = RoomDatabaseHolder.getRoomDatabase(mContext)
        val uploadFileRecords = db.uploadFileRecordDao().findWithMd5(md5)

        if (uploadFileRecords.isNotEmpty()) {
            val uploadFileRecord = uploadFileRecords.single()

            val installFile = File(uploadFileRecord.path)
            if (installFile.exists() && MD5Helper.md5(installFile) == uploadFileRecord.md5) {
                // Prepare to install.
                CommonUtil.install(mContext, installFile)

                return HttpResponseEntity.success()
            }
        }

        val languageCode = httpRequest.getHeader("languageCode")
        val locale = if (!TextUtils.isEmpty(languageCode)) Locale(languageCode!!) else Locale("en")

        return ErrorBuilder().locale(locale).module(HttpModule.CommonModule)
            .error(HttpError.InstallationFileNotFound).build()
    }
}
