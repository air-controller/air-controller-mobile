package com.youngfeng.android.assistant.web.converter

import android.graphics.Bitmap
import android.util.Log
import com.google.gson.Gson
import com.yanzhenjie.andserver.annotation.Converter
import com.yanzhenjie.andserver.framework.MessageConverter
import com.yanzhenjie.andserver.framework.body.FileBody
import com.yanzhenjie.andserver.framework.body.StreamBody
import com.yanzhenjie.andserver.http.ResponseBody
import com.yanzhenjie.andserver.util.IOUtils
import com.yanzhenjie.andserver.util.MediaType
import com.youngfeng.android.assistant.web.body.AudioItemBody
import com.youngfeng.android.assistant.web.body.VideoItemBody
import com.youngfeng.android.assistant.web.entity.AudioEntity
import com.youngfeng.android.assistant.web.entity.HttpResponseEntity
import com.youngfeng.android.assistant.web.entity.VideoEntity
import com.youngfeng.android.assistant.web.response.HttpResponseEntityBody
import com.youngfeng.android.assistant.web.util.JsonUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.lang.reflect.Type
import java.nio.charset.Charset

@Converter
class AppMessageConverter : MessageConverter {
    private val mGson by lazy(mode = LazyThreadSafetyMode.NONE) { Gson() }

    override fun convert(output: Any?, mediaType: MediaType?): ResponseBody {
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

            throw NotImplementedError("AppMessageConverter: convert method not implemented completed")
        }
    }

    override fun <T : Any?> convert(stream: InputStream, mediaType: MediaType?, type: Type): T? {
        val charset: Charset = mediaType?.charset
            ?: return JsonUtils.parseJson(IOUtils.toString(stream), type)
        return JsonUtils.parseJson(IOUtils.toString(stream, charset), type)
    }
}
