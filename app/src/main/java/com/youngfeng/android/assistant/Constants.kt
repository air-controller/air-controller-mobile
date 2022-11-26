package com.youngfeng.android.assistant

/**
 * 该类主要用于定义通用常量.
 *
 * @author Scott Smith 2021/11/19 22:52
 */
object Constants {

    object Port {
        const val HTTP_SERVER = 9527
        const val UDP_DEVICE_DISCOVER = 20000
        const val CMD_SERVER = 20001
        const val HEARTBEAT_SERVER = 20002
    }

    const val PLATFORM_ANDROID = 1

    const val SEARCH_PREFIX = "search#"
    const val SEARCH_RES_PREFIX = "search_msg_received#"
    const val RANDOM_STR_SEARCH = "a2w0nuNyiD6vYogF"
    const val RADNOM_STR_RES_SEARCH = "RBIDoKFHLX9frYTh"

    const val KEEP_TEMP_ZIP_FILE_DURATION = 60 * 60 * 1000;

    const val DATABASE_VERSION = 2

    val VIDEO_SUFFIX = arrayOf(
        "mp4",
        "avi",
        "flv",
        "mkv",
        "mov",
        "wmv",
        "3gp",
        "mpg",
        "mpeg",
        "m4v",
        "rmvb",
        "rm",
        "asf",
        "asx",
    )

    val AUDIO_SUFFIX = arrayOf(
        "mp3",
        "wav",
        "wma",
        "ogg",
        "ape",
        "flac",
        "aac",
        "m4a",
        "aif",
        "aiff",
        "mid",
        "midi",
        "rmi",
        "mka",
        "amr",
        "wv",
        "ra",
    )

    val DOCUMENT_SUFFIX = arrayOf(
        "doc",
        "docx",
        "xls",
        "xlsx",
        "ppt",
        "pptx",
        "pdf",
        "txt",
        "rtf",
        "wps",
        "wpd",
        "wks",
    )

    val IMAGE_SUFFIX = arrayOf(
        "jpg",
        "jpeg",
        "png",
        "bmp",
        "gif",
        "ico",
        "tif",
        "tiff",
        "psd",
        "svg",
        "webp",
        "raw",
        "arw",
        "cr2",
        "cr3",
        "nef",
        "nrw",
        "orf",
        "raf",
        "sr2",
        "srf",
        "srw",
        "x3f",
        "rw2",
        "rwl",
        "rwz",
        "raw",
        "arw",
        "cr2",
        "cr3",
        "nef",
        "nrw",
        "orf",
        "raf",
        "sr2",
        "srf",
        "srw",
        "x3f",
        "rw2",
        "rwl",
        "rwz",
        "raw",
        "arw",
        "cr2",
        "cr3",
        "nef",
        "nrw",
        "orf",
        "raf",
        "sr2",
        "srf",
        "srw",
        "x3f",
        "rw2",
        "rwl",
        "rwz",
        "raw",
        "arw",
        "cr2",
        "cr3",
        "nef",
        "nrw",
        "orf",
        "raf",
        "sr2",
        "srf",
        "srw",
        "x3f",
        "rw2",
        "rwl",
        "rwz",
        "raw",
        "arw",
        "cr2",
        "cr3",
        "nef",
        "nrw",
        "orf",
        "raf",
        "sr2",
        "srf",
        "srw",
        "x3f",
        "rw2"
    )

    const val ROOT_DIR_TYPE_SDCARD = 1
    const val ROOT_DIR_TYPE_DOWNLOAD = 2

    const val URL_GITHUB_STAR = "https://img.shields.io/github/stars/air-controller/air-controller-desktop?style=social"
    const val URL_GITHUB = "https://github.com/air-controller/air-controller-desktop"
}
