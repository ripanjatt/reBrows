package com.ripanjatt.rebrows.util

import java.io.Serializable

data class DownloadItem(val title: String,
                        val url: String,
                        var uri: String,
                        val mimeType: String,
                        val size: Long,
                        var progress: Int,
                        var isPaused: Boolean,
                        var isDownloaded: Boolean
) : Serializable
