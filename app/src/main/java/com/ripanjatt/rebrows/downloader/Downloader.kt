package com.ripanjatt.rebrows.downloader

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.ripanjatt.rebrows.activities.Home
import com.ripanjatt.rebrows.util.DownloadHandler
import com.ripanjatt.rebrows.util.DownloadItem
import com.ripanjatt.rebrows.util.Repository
import com.ripanjatt.rebrows.util.Util
import javaDownloader.Downloader
import javaDownloader.Listeners
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import java.io.File
import java.io.IOException
import java.net.URL
import kotlin.concurrent.thread

object Downloader {

    private val path = "${Environment.getExternalStorageDirectory()}/reBrows/"

    fun saveImage(url: String, home: Home) {
        thread {
            try {
                val bitmap = BitmapFactory.decodeStream(URL(url).openStream())
                val fileName = "Image" + (System.currentTimeMillis() % 10000)
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    if(SDK_INT >= Build.VERSION_CODES.Q)
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/reBrows")
                }
                val resolver = home.applicationContext.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: throw IOException("Failed to create new MediaStore record.")
                CoroutineScope(Main).launch {
                    Toast.makeText(
                        home.applicationContext,
                        "Downloading...",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                resolver.openOutputStream(uri)?.use {
                    if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it))
                        throw IOException("Failed to save bitmap.")
                } ?: throw IOException("Failed to open output stream.")
                Repository.setDownloads(
                    DownloadHandler(
                        DownloadItem(fileName, url, uri.toString(), "image/jpg", ((bitmap.width * bitmap.height) / 2).toLong(), 100, isPaused = false, true),
                        null
                    ),
                    home.applicationContext
                )
                CoroutineScope(Main).launch {
                    Toast.makeText(
                        home.applicationContext,
                        "Image saved!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("Error", "$e")
                CoroutineScope(Main).launch {
                    Toast.makeText(
                        home.applicationContext,
                        "Downloading failed!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun saveFile(url: String, fileName: String, mimeType: String, length: Long, home: Home) {
        thread {
            val item = DownloadItem(fileName, url, "", mimeType, length, 0, isPaused = false, false)
            if(SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val values = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/reBrows")
                    }
                    val resolver = home.applicationContext.contentResolver
                    val uri = resolver.insert(MediaStore.Files.getContentUri("external"), values)
                        ?: throw IOException("Failed to create new MediaStore record.")
                    val downloader = Downloader(url, fileName, length)
                    CoroutineScope(Main).launch {
                        Toast.makeText(home.applicationContext, "Starting...", Toast.LENGTH_SHORT).show()
                        Repository.setDownloads(DownloadHandler(item, downloader), home.applicationContext)
                    }
                    downloader.download(resolver.openOutputStream(uri), resolver.openInputStream(uri))
                    downloader.setOnProgressListener(object: Listeners.ProgressListener {
                        override fun onStart() {
                        }

                        override fun onProgress(p0: Double, p1: Long, p2: Long) {
                            item.progress = p0.toInt()
                            Repository.notifyAdapters()
                        }

                        override fun onSpeedInKB(p0: Double) {

                        }

                        override fun onComplete() {
                            Log.e("Test", "Done!")
                            CoroutineScope(Main).launch {
                                Repository.removeDownloads(item, home.applicationContext)
                                item.uri = uri.toString()
                                item.isDownloaded = true
                                item.isPaused = false
                                Repository.setDownloads(DownloadHandler(item, null), home.applicationContext)
                            }
                        }
                    })
                } catch (e: Exception) {
                    Log.e("Error", "$e")
                }
            } else {
                val downloader = Downloader(url, fileName, path, length)
                CoroutineScope(Main).launch {
                    Toast.makeText(home.applicationContext, "Starting...", Toast.LENGTH_SHORT).show()
                    Repository.setDownloads(DownloadHandler(item, downloader), home.applicationContext)
                }
                downloader.setOnProgressListener(object: Listeners.ProgressListener {
                    override fun onStart() {

                    }

                    override fun onProgress(p0: Double, p1: Long, p2: Long) {
                        item.progress = p0.toInt()
                        Repository.notifyAdapters()
                    }

                    override fun onSpeedInKB(p0: Double) {

                    }

                    override fun onComplete() {
                        Log.e("Test", "Done!")
                        CoroutineScope(Main).launch {
                            val uri = FileProvider.getUriForFile(
                                home.applicationContext,
                                "com.ripanjatt.rebrows.provider",
                                File("$path/$fileName")
                            )
                            Util.notifyFiles(home, File("$path/$fileName"))
                            Repository.removeDownloads(item, home.applicationContext)
                            item.uri = uri.toString()
                            item.isDownloaded = true
                            item.isPaused = false
                            Repository.setDownloads(DownloadHandler(item, null), home.applicationContext)
                        }
                        Util.notifyFiles(home, File("$path/$fileName"))
                    }
                })
                downloader.download()
            }
        }
    }
}