package com.youngfeng.android.assistant.server.body

import com.yanzhenjie.andserver.framework.body.FileBody
import com.yanzhenjie.andserver.util.MediaType
import com.youngfeng.android.assistant.server.entity.VideoEntity
import java.io.File

class VideoItemBody(video: VideoEntity) : FileBody(File(video.path)) {

    override fun contentType(): MediaType {
        return MediaType.parseMediaType("video/mp4")
    }
}
