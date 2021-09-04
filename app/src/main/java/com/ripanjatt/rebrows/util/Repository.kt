package com.ripanjatt.rebrows.util

import android.content.Context
import com.ripanjatt.rebrows.activities.Home
import com.ripanjatt.rebrows.adapters.DownloadAdapter
import com.ripanjatt.rebrows.adapters.HistoryAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

object Repository {

    private val downloadList = ArrayList<DownloadHandler>()
    private val historyList = ArrayList<History>()
    private val tabList = ArrayList<Tab>()

    private var inAdapter: DownloadAdapter? = null
    private var doneAdapter: DownloadAdapter? = null
    private var historyAdapter: HistoryAdapter? = null

    fun newTab(url: String?, activity: Home): Tab {
        val tab = Tab(url, activity)
        tabList.add(tab)
        return tab
    }

    fun removeTab(tab: Tab) {
        tabList.remove(tab)
    }

    fun getTabs(): ArrayList<Tab> {
        return tabList
    }

    fun setSpeedMode(mode: Boolean) {
        tabList.forEach {
            it.speedMode(mode)
        }
    }

    fun loadDownloads(context: Context) {
        downloadList.clear()
        val downloads = Data.getDownloads(context)
        downloads.forEach {
            downloadList.add(DownloadHandler(it, null))
        }
        historyList.addAll(Data.getHistory(context))
    }

    fun removeDownloads(item: DownloadItem, context: Context) {
        for(i in 0 until downloadList.size) {
            if(item.title == downloadList[i].item.title) {
                if(downloadList[i].downloader != null && !downloadList[i].item.isDownloaded) {
                    downloadList[i].downloader?.pause()
                }
                downloadList.remove(downloadList[i])
                break
            }
        }
        inAdapter?.notifyAdapter()
        doneAdapter?.notifyAdapter()
        updateDownloads(context)
    }

    fun setDownloads(item: DownloadHandler, context: Context) {
        downloadList.add(item)
        inAdapter?.notifyAdapter()
        doneAdapter?.notifyAdapter()
        updateDownloads(context)
    }

    fun updateHistory(item: History, context: Context) {
        if(!Util.getIncognito(context)) {
            historyList.add(item)
            historyAdapter?.notifyAdapter()
            Data.setHistory(historyList, context)
        }
    }

    fun getHistory(): ArrayList<History> {
        return historyList.reversed().filter { true } as ArrayList<History>
    }

    fun clearHistory(context: Context) {
        historyList.clear()
        historyAdapter?.notifyAdapter()
        Data.setHistory(historyList, context)
    }

    fun getHistoryAdapter(): HistoryAdapter {
        return historyAdapter as HistoryAdapter
    }

    fun notifyAdapters() {
        CoroutineScope(Main).launch {
            inAdapter?.notifyAdapter()
            doneAdapter?.notifyAdapter()
        }
    }

    fun setUpAdapters(home: Home) {
        inAdapter = DownloadAdapter(false, home)
        doneAdapter = DownloadAdapter(true, home)
        historyAdapter = HistoryAdapter(home)
    }

    fun getAdapters(isDownloaded: Boolean): DownloadAdapter {
        return if(isDownloaded)
            doneAdapter as DownloadAdapter
        else
            inAdapter as DownloadAdapter
    }

    fun getList(isDownloaded: Boolean): ArrayList<DownloadHandler> {
        return downloadList.reversed().filter { it.item.isDownloaded == isDownloaded } as ArrayList<DownloadHandler>
    }

    private fun updateDownloads(context: Context) {
        val downloads = ArrayList<DownloadItem>()
        downloadList.forEach {
            downloads.add(it.item)
        }
        Data.setDownloads(downloads, context)
    }
}