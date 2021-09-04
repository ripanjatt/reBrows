package com.ripanjatt.rebrows.util

import android.app.Activity
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ripanjatt.rebrows.activities.Home
import com.ripanjatt.rebrows.adapters.setDrawable
import com.ripanjatt.rebrows.adapters.setIncognito
import com.ripanjatt.rebrows.databinding.DownloadTabBinding
import com.ripanjatt.rebrows.databinding.LoadingSheetBinding
import com.ripanjatt.rebrows.databinding.MenuSheetBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

object LayoutUtil {

    fun checkSpeed(home: Home) {
        val sheet = BottomSheetDialog(home)
        sheet.setCancelable(false)
        sheet.setContentView(LoadingSheetBinding.inflate(home.layoutInflater).root)
        sheet.show()
        thread {
            val speed = Util.getSpeed()
            sheet.dismiss()
            val builder = AlertDialog.Builder(home)
            builder.setTitle("Speed")
            builder.setMessage(speed)
            builder.setNegativeButton("Ok", null)
            CoroutineScope(Main).launch {
                builder.show()
            }
        }
    }

    fun menuDialog(activity: Activity): BottomSheetDialog {
        val builder = BottomSheetDialog(activity)
        val binding = MenuSheetBinding.inflate(activity.layoutInflater)
        binding.sheetSpeedMode.setDrawable(Util.getSpeedMode(activity.applicationContext))
        binding.sheetIncognito.setIncognito(Util.getIncognito(activity.applicationContext))
        binding.sheetClose.setOnClickListener {
            builder.dismiss()
        }
        builder.setContentView(binding.root)
        builder.show()
        return builder
    }
    
    fun createDownloadTabs(isDownloaded: Boolean, activity: Activity): View {
        val binding = DownloadTabBinding.inflate(activity.layoutInflater)
        binding.downloadsRecycler.layoutManager = LinearLayoutManager(activity)
        binding.downloadsRecycler.adapter = Repository.getAdapters(isDownloaded)
        return binding.root
    }
}