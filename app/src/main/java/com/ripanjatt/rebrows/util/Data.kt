package com.ripanjatt.rebrows.util

import android.content.Context
import android.util.Log
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.concurrent.thread

object Data {

    fun setDownloads(list: ArrayList<DownloadItem>, context: Context) {
        thread {
            try {
                val outputStream = ObjectOutputStream(
                    FileOutputStream("${context.getExternalFilesDir(null).toString()}/downloads.db")
                )
                outputStream.writeObject(list)
                outputStream.close()
            } catch (e: Exception) {
                Log.e("Error", "$e")
            }
        }
    }

    fun getDownloads(context: Context): ArrayList<DownloadItem> {
        val list = ArrayList<DownloadItem>()
        try {
            val inputStream = ObjectInputStream(
                FileInputStream("${context.getExternalFilesDir(null).toString()}/downloads.db")
            )
            list.addAll(inputStream.readObject() as ArrayList<DownloadItem>)
            inputStream.close()
        } catch (e: Exception) {
            Log.e("Error", "$e")
        }
        return list
    }

    fun setHistory(list: ArrayList<History>, context: Context) {
        thread {
            try {
                val outputStream = ObjectOutputStream(
                    FileOutputStream("${context.getExternalFilesDir(null).toString()}/history.db")
                )
                outputStream.writeObject(list)
                outputStream.close()
            } catch (e: Exception) {
                Log.e("Error", "$e")
            }
        }
    }

    fun getHistory(context: Context): ArrayList<History> {
        val list = ArrayList<History>()
        try {
            val inputStream = ObjectInputStream(
                FileInputStream("${context.getExternalFilesDir(null).toString()}/history.db")
            )
            list.addAll(inputStream.readObject() as ArrayList<History>)
            inputStream.close()
        } catch (e: Exception) {
            Log.e("Error", "$e")
        }
        return list
    }
}