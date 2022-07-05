package com.youngfeng.android.assistant.ext

import com.youngfeng.android.assistant.Constants
import java.io.File

/**
 * Decide whether this file is an image file.
 *
 * @return true yes, false no
 */
val File.isImage: Boolean
    get() {
        return Constants.IMAGE_SUFFIX.contains(this.extension.lowercase())
    }

val File.isVideo: Boolean
    get() {
        return Constants.VIDEO_SUFFIX.contains(this.extension.lowercase())
    }

val File.isAudio: Boolean
    get() {
        return Constants.AUDIO_SUFFIX.contains(this.extension.lowercase())
    }

val File.isDoc: Boolean
    get() {
        return Constants.DOCUMENT_SUFFIX.contains(this.extension.lowercase())
    }
