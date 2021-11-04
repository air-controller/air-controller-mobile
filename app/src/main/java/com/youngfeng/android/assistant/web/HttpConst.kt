package com.youngfeng.android.assistant.web

import com.youngfeng.android.assistant.R
import com.youngfeng.android.assistant.app.MobileAssistantApplication

/**
 * 错误码由模块加具体错误码，注意不同模块的错误码应该从01开始，到19结束，最多20个错误
 *
 * @author Scott Smith 2021/11/2 14:00
 */

enum class HttpModule(var value: Int) {
    FileModule(1), ImageModule(2), MusicModule(3)
}

enum class HttpError(var code: String, var value: String) {
    // 文件模块
    NoReadExternalStoragePerm("01", MobileAssistantApplication.getInstance().getString(R.string.no_read_external_storage_perm)),
    FileIsNotADir("02", MobileAssistantApplication.getInstance().getString(R.string.this_file_is_not_a_dir));

    fun getString(strRes: Int): String {
        return MobileAssistantApplication.getInstance().getString(strRes)
    }
}