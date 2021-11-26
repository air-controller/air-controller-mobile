package com.youngfeng.android.assistant.web.controller

import android.content.ContentUris
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import com.yanzhenjie.andserver.annotation.GetMapping
import com.yanzhenjie.andserver.annotation.PathVariable
import com.yanzhenjie.andserver.annotation.QueryParam
import com.yanzhenjie.andserver.annotation.RequestMapping
import com.yanzhenjie.andserver.annotation.RestController
import com.youngfeng.android.assistant.app.MobileAssistantApplication
import java.io.File

@RestController
@RequestMapping("/stream")
class StreamController {
    private val mContext by lazy { MobileAssistantApplication.getInstance() }

    @GetMapping("/image/thumbnail/{id}/{width}/{height}")
    fun imageThumbnail(
        @PathVariable("id") id: Long,
        @PathVariable("width") width: Int,
        @PathVariable("height") height: Int
    ): Bitmap {
        val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mContext.contentResolver.loadThumbnail(uri, Size(width, height), null)
        } else {
            MediaStore.Images.Thumbnails.getThumbnail(
                mContext.contentResolver,
                id,
                MediaStore.Images.Thumbnails.MINI_KIND, null
            )
        }
    }

    @GetMapping("/video/thumbnail/{id}/{width}/{height}")
    fun videoThumbnail(
        @PathVariable("id") id: Long,
        @PathVariable("width") width: Int,
        @PathVariable("height") height: Int
    ): Bitmap {
        val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mContext.contentResolver.loadThumbnail(uri, Size(width, height), null)
        } else {
            MediaStore.Images.Thumbnails.getThumbnail(
                mContext.contentResolver,
                id,
                MediaStore.Images.Thumbnails.MINI_KIND, null
            )
        }
    }

    @GetMapping("/file")
    fun file(@QueryParam("path") path: String): File {
        return File(path)
    }
}
