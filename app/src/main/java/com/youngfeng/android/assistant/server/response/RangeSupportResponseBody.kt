package com.youngfeng.android.assistant.server.response

import com.yanzhenjie.andserver.http.HttpResponse
import com.yanzhenjie.andserver.http.ResponseBody
import com.yanzhenjie.andserver.util.IOUtils
import com.yanzhenjie.andserver.util.MediaType
import java.io.File
import java.io.OutputStream

class RangeSupportResponseBody(
    private val contentType: MediaType? = null,
    private val file: File,
    rangeHeader: String?
) : ResponseBody {
    private val responseRangeHeader = mutableMapOf<String, String>()

    private var start: Long = -1
    private var end: Long = -1

    companion object {
        private const val DEFAULT_CACHE_SIZE = 35 * 1024 * 1024
    }

    init {
        if (file.isDirectory) {
            throw IllegalArgumentException("File '$file' is a directory.")
        }

        if (!file.exists()) {
            throw IllegalArgumentException("File '$file' does not exist.")
        }

        if (!file.canRead()) {
            throw IllegalArgumentException("File '$file' cannot be read.")
        }

        rangeHeader?.apply {
            val fileLen = file.length()
            val range = parseRangeHeader(this, fileLen)

            val cacheByteLen = DEFAULT_CACHE_SIZE
            start = range.first

            end = start + cacheByteLen
            if (end > range.last) {
                end = range.last
            }

            responseRangeHeader["Content-Range"] = "bytes $start-$end/${fileLen}"
            responseRangeHeader["Accept-Ranges"] = "bytes"
        }
    }

    override fun isRepeatable(): Boolean {
        return true
    }

    override fun contentLength(): Long {
        return if (start == -1L) file.length() else end - start + 1
    }

    override fun contentType(): MediaType? {
        return contentType ?: MediaType.parseMediaType("application/octet-stream")
    }

    override fun writeTo(output: OutputStream) {
        val inputStream = file.inputStream()

        if (start == -1L) {
            IOUtils.write(inputStream, output)
            IOUtils.closeQuietly(inputStream)
        } else {
            val startTime = System.currentTimeMillis()
            inputStream.skip(start)

            var readBytes = -1
            var totalReadBytes = 0L

            val currentDataLen = end - start + 1

            val buffer = ByteArray(10 * 1024 * 1024)

            val shouldReadLen = if (buffer.size > currentDataLen) currentDataLen else buffer.size
            readBytes = inputStream.read(buffer, 0, shouldReadLen.toInt())

            while (readBytes != -1) {
                totalReadBytes += readBytes
                if (totalReadBytes > currentDataLen) {
                    break
                }
                output.write(buffer, 0, readBytes)
                readBytes = inputStream.read(buffer)
            }
        }
        output.flush()
        IOUtils.closeQuietly(inputStream)
    }

    fun attachToResponse(response: HttpResponse): RangeSupportResponseBody {
        response.setHeader("Content-Type", contentType().toString())
        responseRangeHeader.onEach { (key, value) -> response.addHeader(key, value) }
        response.setHeader("Last-Modified", file.lastModified().toString())
        response.setHeader("ETag", file.name)

        response.status = HttpResponse.SC_PARTIAL_CONTENT

        return this
    }

    private fun parseRangeHeader(rangeHeader: String, totalLen: Long): LongRange {
        var rangeArr = rangeHeader.split("=")
        if (rangeArr.size != 2) {
            throw IllegalArgumentException("Invalid range header: $rangeHeader")
        }

        val range = rangeArr[1]
        rangeArr = range.split("-")

        val start = rangeArr[0].toLongOrNull() ?: 0L

        var end = totalLen - 1
        if (rangeArr.size >= 2) {
            end = rangeArr[1].toLongOrNull() ?: end
        }

        return start..end
    }
}
