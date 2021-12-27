package com.youngfeng.android.assistant.web.body

import com.yanzhenjie.andserver.framework.body.FileBody
import com.yanzhenjie.andserver.util.MediaType
import com.youngfeng.android.assistant.web.entity.AudioEntity
import java.io.File

class AudioItemBody(audio: AudioEntity) : FileBody(File(audio.path)) {

    override fun contentType(): MediaType {
        return MediaType.parseMediaType("audio/mpeg")
    }
}
