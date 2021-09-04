package com.ripanjatt.rebrows.util

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.ripanjatt.rebrows.R
import com.ripanjatt.rebrows.activities.Home
import java.io.File
import java.net.URL
import java.text.DecimalFormat

object Util {

    private const val speedLink = "https://cdn.glitch.com/27dd262f-e516-43fe-b5da-d6670ebd93cb%2FPROJECT%20REPORT%20final.docx?v=1630415531898"

    fun getSpeed(): String {
        var speed = "Connection timeout!"
        try {
            val connection = URL(speedLink).openConnection()
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.connect()
            val length = connection.contentLengthLong
            val stream = connection.getInputStream()
            val bytes = ByteArray(1024)
            val start = System.currentTimeMillis()
            var read = stream.read(bytes)
            while(read != -1) {
                read = stream.read(bytes)
            }
            val value = DecimalFormat("0.00").format(length.toDouble() / (System.currentTimeMillis() - start).toDouble())
            speed = "$value KiloBytes/sec"
        } catch (e: Exception) {
            Log.e("Error", "$e")
        }
        return speed
    }

    fun aboutDialog(home: Home) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(home)
        builder.setTitle("About")
        builder.setMessage("Developed by: @ripanjatt\n" +
                "Instagram: @ripanjatt\n" +
                "Website: https://store-ripanjatt.herokuapp.com/")
        builder.setNegativeButton("Ok", null)
        builder.show()
    }

    fun openWithUri(uri: Uri, home: Home) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.data = uri
            home.startActivity(intent)
        } catch (e: java.lang.Exception) {
            Log.e("Error", "" + e)
            Toast.makeText(home, "No App found!", Toast.LENGTH_SHORT).show()
        }
    }

    fun exitDialog(activity: Activity) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Exit?")
        builder.setNegativeButton("No", null)
        builder.setPositiveButton("Yes") { dialog, _ ->
            dialog.dismiss()
            activity.finish()
        }
        builder.show()
    }

    fun getSpeedMode(context: Context): Boolean {
        return context.getSharedPreferences("settings", Context.MODE_PRIVATE).getBoolean("speedMode", false)
    }

    fun setSpeedMode(mode: Boolean, context: Context) {
        val editor = context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit()
        editor.putBoolean("speedMode", mode)
        editor.apply()
    }

    fun getIncognito(context: Context): Boolean {
        return context.getSharedPreferences("settings", Context.MODE_PRIVATE).getBoolean("incognito", false)
    }

    fun setIncognito(mode: Boolean, context: Context) {
        val editor = context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit()
        editor.putBoolean("incognito", mode)
        editor.apply()
        if(mode) Toast.makeText(context, "Incognito On", Toast.LENGTH_SHORT).show()
        else Toast.makeText(context, "Incognito Off", Toast.LENGTH_SHORT).show()
    }

    fun getMipmap(mimeType: String): Int {
        return when {
            mimeType.contains("video") -> R.mipmap.video_flat
            mimeType.contains("image") -> R.mipmap.image_simplify
            mimeType.contains("audio") -> R.mipmap.music_flow
            else -> R.mipmap.file_blue
        }
    }

    fun notifyFiles(home: Home, file: File) {
        try {
            MediaScannerConnection.scanFile(home.applicationContext, arrayOf(file.path), null, null)
        } catch (e: Exception) {
            Log.e("Error", "" + e)
        }
    }

    fun getPermission(activity: Activity) {
        if(!checkPermissions(activity)) {
            if(SDK_INT >= Build.VERSION_CODES.Q) {
                activity.requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 8192)
            } else {
                activity.requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 8192)
            }
        }
    }

    private fun checkPermissions(activity: Activity): Boolean {
        return if(SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(activity.applicationContext,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(activity.applicationContext,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(activity.applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}