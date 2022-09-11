package com.ojhdtapp.parabox.ui.file

import com.ojhdtapp.parabox.domain.model.File
import kotlin.math.abs

data class FilePageState(
    val isLoading: Boolean = true,
    val data: List<File> = emptyList(),
    val timeFilter: TimeFilter = TimeFilter.All,
    val extensionFilter: ExtensionFilter = ExtensionFilter.All,
    val sizeFilter: SizeFilter = SizeFilter.All,
) {
    val filterData
        get() = data
            .filter {
                timeFilter.fileCheck(it)
                        && extensionFilter.fileCheck(it)
                        && sizeFilter.fileCheck(it)
            }
}

sealed class TimeFilter(
    val label: String,
    val fileCheck: (file: File) -> Boolean
) {
    object All : TimeFilter("所有时间", { true })
    object WithinThreeDays : TimeFilter(
        "最近三天",
        { file: File -> abs(System.currentTimeMillis() - file.timestamp) < 259200000 })

    object WithinThisWeek : TimeFilter(
        "最近一周",
        { file: File -> abs(System.currentTimeMillis() - file.timestamp) < 604800000 }
    )

    object WithinThisMonth : TimeFilter(
        "最近一个月",
        { file: File -> abs(System.currentTimeMillis() - file.timestamp) < 2592000000 }
    )

    object MoreThanAMonth : TimeFilter(
        "一月前",
        { file: File -> abs(System.currentTimeMillis() - file.timestamp) >= 2592000000 }
    )

    data class Custom(val mLabel: String, val timestampStart: Long, val timestampEnd: Long) :
        TimeFilter(
            label = mLabel,
            fileCheck = { file: File -> file.timestamp in timestampStart until timestampEnd }
        )
}

sealed class ExtensionFilter(
    val label: String,
    val fileCheck: (file: File) -> Boolean
) {
    object All : ExtensionFilter(
        "所有类型",
        { true }
    )

    object Docs : ExtensionFilter(
        "文档",
        { file: File -> file.extension.lowercase() in listOf<String>("doc", "docx", "wps", "wpt") }
    )

    object Slides : ExtensionFilter(
        "演示文稿",
        { file: File -> file.extension.lowercase() in listOf<String>("ppt", "pptx", "dps", "dpt") }
    )

    object Sheets : ExtensionFilter(
        "电子表格",
        { file: File -> file.extension.lowercase() in listOf<String>("xls", "xlsx", "et", "ett") }
    )

    object Picture : ExtensionFilter(
        "图片",
        { file: File ->
            file.extension.lowercase() in listOf<String>(
                "bmp",
                "jpeg",
                "jpg",
                "png",
                "tif",
                "gif",
                "pcx",
                "tga",
                "exif",
                "fpx",
                "svg",
                "psd",
                "cdr",
                "pcd",
                "dxf",
                "ufo",
                "eps",
                "ai",
                "raw",
                "webp",
                "avif",
                "apng"
            )
        }
    )

    object Video : ExtensionFilter(
        "视频",
        { file: File ->
            file.extension.lowercase() in listOf<String>(
                "avi",
                "wmv",
                "mp4",
                "mpeg",
                "mpg",
                "mov",
                "flv",
                "rmvb",
                "rm",
                "asf"
            )
        }
    )

    object Audio : ExtensionFilter(
        "音频",
        { file: File ->
            file.extension.lowercase() in listOf<String>(
                "cd",
                "wav",
                "aiff",
                "mp3",
                "wma",
                "ogg",
                "mpc",
                "flac",
                "ape",
                "3gp",
            )
        }
    )

    object Compressed : ExtensionFilter(
        "压缩文档",
        { file: File ->
            file.extension.lowercase() in listOf<String>(
                "zip",
                "rar",
                "7z",
                "tar.bz2",
                "tar",
                "jar",
                "gz",
                "deb"
            )
        }
    )

    object PDF : ExtensionFilter(
        "便携式文档",
        { file: File ->
            file.extension.lowercase() in listOf<String>(
                "pdf",
                "epub",
                "mobi",
                "iba",
                "azw"
            )
        }
    )
}

sealed class SizeFilter(
    val label: String,
    val fileCheck: (file: File) -> Boolean
) {
    object All : SizeFilter(
        "不限大小",
        { true }
    )

    object TenMB : SizeFilter(
        "小于10MB",
        { file: File -> file.size < 10000000 }
    )

    object HundredMB : SizeFilter(
        "10MB至100MB",
        { file: File -> file.size in 10000000 until 100000000 }
    )

    object OverHundredMB : SizeFilter(
        "超过100MB",
        { file: File -> file.size > 100000000 }
    )
}