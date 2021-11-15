package com.youngfeng.android.assistant.ext

import java.io.File
import java.util.Locale

/**
 * Decide whether this file is an image file.
 *
 * @return true yes, false no
 */
fun File.isImage(): Boolean {
    return this.extension.lowercase().endsWith(".jpg")
        || this.extension.lowercase().endsWith(".jpeg")
        || this.extension.lowercase().endsWith(".jfif")
}