package com.youngfeng.android.assistant.web

import com.youngfeng.android.assistant.R
import com.youngfeng.android.assistant.app.MobileAssistantApplication

/**
 * 错误码由模块加具体错误码，注意不同模块的错误码应该从01开始，到19结束，最多20个错误
 *
 * @author Scott Smith 2021/11/2 14:00
 */

enum class HttpModule(var value: Int) {
    FileModule(1), ImageModule(2), AudioModule(3)
}

enum class HttpError(var code: String, var value: String) {
    // 文件模块
    NoReadExternalStoragePerm(
        "01",
        MobileAssistantApplication.getInstance().getString(R.string.no_read_external_storage_perm)
    ),
    FileIsNotADir("02", MobileAssistantApplication.getInstance().getString(R.string.this_file_is_not_a_dir)),
    NoWriteExternalStoragePerm(
        "03",
        MobileAssistantApplication.getInstance().getString(R.string.no_read_external_storage_perm)
    ),
    InvalidFileName("04", MobileAssistantApplication.getInstance().getString(R.string.invalid_file_name)),
    FileNameEmpty("05", MobileAssistantApplication.getInstance().getString(R.string.file_name_cant_empty)),
    FolderCantEmpty("06", MobileAssistantApplication.getInstance().getString(R.string.folder_cant_empty)),
    CreateFileFail("06", MobileAssistantApplication.getInstance().getString(R.string.file_create_fail)),
    FilePathCantEmpty("07", MobileAssistantApplication.getInstance().getString(R.string.file_path_cant_empty)),
    DeleteFileFail("08", MobileAssistantApplication.getInstance().getString(R.string.delete_file_fail)),
    RenameFileFail("09", MobileAssistantApplication.getInstance().getString(R.string.rename_file_fail)),
    MoveFileFail("10", MobileAssistantApplication.getInstance().getString(R.string.move_file_fail)),

    // 图片模块
    DeleteImageFail("01", MobileAssistantApplication.getInstance().getString(R.string.delete_image_fail)),
    DeleteMultiImageFail("02", MobileAssistantApplication.getInstance().getString(R.string.delete_image_fail)),
    ImageFileNotExist("03", MobileAssistantApplication.getInstance().getString(R.string.image_not_exist)),
    DeleteAlbumFail("04", "删除相册失败"),

    // 音频模块
    DeleteAudioFail("01", MobileAssistantApplication.getInstance().getString(R.string.delete_audio_file_fail));

    fun getString(strRes: Int): String {
        return MobileAssistantApplication.getInstance().getString(strRes)
    }
}
