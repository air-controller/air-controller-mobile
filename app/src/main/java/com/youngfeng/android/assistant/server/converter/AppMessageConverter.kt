package com.youngfeng.android.assistant.server.converter

import android.graphics.Bitmap
import com.google.gson.Gson
import com.yanzhenjie.andserver.annotation.Converter
import com.yanzhenjie.andserver.framework.MessageConverter
import com.yanzhenjie.andserver.framework.body.FileBody
import com.yanzhenjie.andserver.framework.body.StreamBody
import com.yanzhenjie.andserver.http.ResponseBody
import com.yanzhenjie.andserver.util.IOUtils
import com.yanzhenjie.andserver.util.MediaType
import com.youngfeng.android.assistant.server.body.AudioItemBody
import com.youngfeng.android.assistant.server.body.VideoItemBody
import com.youngfeng.android.assistant.server.entity.AudioEntity
import com.youngfeng.android.assistant.server.entity.HttpResponseEntity
import com.youngfeng.android.assistant.server.entity.VideoEntity
import com.youngfeng.android.assistant.server.response.HttpResponseEntityBody
import com.youngfeng.android.assistant.server.util.JsonUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.lang.reflect.Type
import java.nio.charset.Charset

@Converter
class AppMessageConverter : MessageConverter {
    private val mGson by lazy(mode = LazyThreadSafetyMode.NONE) { Gson() }

    override fun convert(output: Any?, mediaType: MediaType?): ResponseBody? {
        if (output is HttpResponseEntity<*>) {
            val json = mGson.toJson(output)
            return HttpResponseEntityBody(json)
        } else {
            if (output is Bitmap) {
                val bos = ByteArrayOutputStream()
                output.compress(Bitmap.CompressFormat.JPEG, 100, bos)

                val inputStream = ByteArrayInputStream(bos.toByteArray())
                bos.flush()
                bos.close()

                return StreamBody(inputStream, bos.size().toLong(), MediaType.IMAGE_JPEG)
            }

            if (output is File) {
                return FileBody(output)
            }

            if (output is AudioEntity) {
                return AudioItemBody(output)
            }

            if (output is VideoEntity) {
                return VideoItemBody(output)
            }

            return null
        }
    }

    override fun <T : Any?> convert(stream: InputStream, mediaType: MediaType?, type: Type): T? {
        val charset: Charset? = mediaType?.charset
        return if (null == charset) {
            JsonUtils.parseJson(IOUtils.toString(stream), type)
        } else {
            JsonUtils.parseJson(IOUtils.toString(stream, charset), type)
        }
    }
}
