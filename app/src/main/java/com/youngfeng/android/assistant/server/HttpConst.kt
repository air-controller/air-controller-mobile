package com.youngfeng.android.assistant.server

import com.youngfeng.android.assistant.R
import com.youngfeng.android.assistant.app.AirControllerApp

/**
 * 错误码由模块加具体错误码，注意不同模块的错误码应该从01开始，到19结束，最多20个错误
 *
 * @author Scott Smith 2021/11/2 14:00
 */

enum class HttpModule(var value: Int) {
    FileModule(1), ImageModule(2), AudioModule(3), VideoModule(4),
    Download(5), CommonModule(6), ContactModule(7), SystemModule(99)
}

enum class HttpError(var code: String, var value: Int) {
    // 文件模块
    NoReadExternalStoragePerm(
        "01",
        R.string.no_read_external_storage_perm
    ),
    FileIsNotADir("02", R.string.this_file_is_not_a_dir),
    NoWriteExternalStoragePerm(
        "03",
        R.string.no_read_external_storage_perm
    ),
    InvalidFileName("04", R.string.invalid_file_name),
    FileNameEmpty("05", R.string.file_name_cant_empty),
    FolderCantEmpty("06", R.string.folder_cant_empty),
    CreateFileFail("06", R.string.file_create_fail),
    FilePathCantEmpty("07", R.string.file_path_cant_empty),
    DeleteFileFail("08", R.string.delete_file_fail),
    RenameFileFail("09", R.string.rename_file_fail),
    MoveFileFail("10", R.string.move_file_fail),
    DeleteFilePartialFailure("11", R.string.delete_file_partial_failure),

    // 图片模块
    DeleteImageFail("01", R.string.delete_image_fail),
    DeleteMultiImageFail("02", R.string.delete_image_fail),
    ImageFileNotExist("03", R.string.image_not_exist),
    DeleteAlbumFail("04", R.string.delete_album_fail),
    GetPhotoDirFailure("05", R.string.get_photo_dir_failure),
    DeleteImagePartialFailure("06", R.string.delete_image_partial_failure),
    DeleteAlbumPartialFailure("07", R.string.delete_album_partial_failure),

    // 音频模块
    DeleteAudioFail("01", R.string.delete_audio_file_fail),
    UploadAudioFail("02", R.string.upload_audio_file_fail),
    DeleteAudioPartialFailure("03", R.string.delete_audio_partial_failure),

    // 视频模块
    DeleteVideoFail("01", R.string.delete_video_file_fail),
    UploadVideoFailure("02", R.string.upload_video_file_failure),
    DeleteVideoPartialFailure("03", R.string.delete_video_partial_failure),
    DeleteVideoFolderPartialFailure("04", R.string.delete_video_folder_partial_failure),
    DeleteVideoFolderFail("05", R.string.delete_video_folder_fail),

    // 下载模块
    GetDownloadDirFail("01", R.string.get_download_dir_fail),
    DownloadDirNotExist("02", R.string.download_dir_not_exist),

    // Contact module
    UploadPhotoAndNewContactFailure("01", R.string.create_contact_failure),
    ContactNotFound("02", R.string.contact_not_found),
    UpdatePhotoFailure("03", R.string.update_photo_failure),
    CreateContactFailure("04", R.string.create_contact_failure),
    RawContactNotFound("05", R.string.contact_not_found),
    DeleteRawContactsFailure("06", R.string.delete_contact_failure),

    // Common module
    UploadInstallFileFailure("01", R.string.install_bundle_upload_failure),
    InstallationFileNotFound("02", R.string.installation_package_not_found),
    WebAccessIsNotAllowed("03", R.string.web_access_is_not_allowed),
    PasswdIsInCorrect("04", R.string.web_passwd_is_not_correct),

    // System module, process common error
    LackOfNecessaryPermissions("01", R.string.lack_of_necessary_permissions);

    fun getString(strRes: Int): String {
        return AirControllerApp.getInstance().getString(strRes)
    }
}
