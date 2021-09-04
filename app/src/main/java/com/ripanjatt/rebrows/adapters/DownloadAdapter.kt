package com.ripanjatt.rebrows.adapters

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.ripanjatt.rebrows.activities.Home
import com.ripanjatt.rebrows.databinding.DownloadItemBinding
import com.ripanjatt.rebrows.downloader.Downloader
import com.ripanjatt.rebrows.util.Repository
import com.ripanjatt.rebrows.util.Util

class DownloadAdapter(private val isDownloaded: Boolean, private val home: Home): RecyclerView.Adapter<DownloadAdapter.DownloadHolder>() {

    val list = Repository.getList(isDownloaded)

    class DownloadHolder(val binding: DownloadItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadHolder {
        return DownloadHolder(
            DownloadItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: DownloadHolder, position: Int) {
        holder.binding.item = list[position].item
        if(isDownloaded) {
            holder.binding.root.setOnClickListener {
                Util.openWithUri(Uri.parse(list[position].item.uri), home)
            }
        } else {
            if(list[position].downloader == null) {
                list[position].item.isPaused = true
                holder.binding.downloadAction.setPause(list[position].item.isPaused)
            }
            holder.binding.downloadAction.setOnClickListener {
                Log.e("Test", "Clicked!")
                if(list[position].downloader == null) {
                    val temp = list[position].item
                    Repository.removeDownloads(list[position].item, home.applicationContext)
                    Downloader.saveFile(
                        temp.url,
                        temp.title,
                        temp.mimeType,
                        temp.size,
                        home
                    )
                } else {
                    if(list[position].item.isPaused) {
                        list[position].item.isPaused = false
                        list[position].downloader?.resume()
                    } else {
                        list[position].item.isPaused = true
                        list[position].downloader?.pause()
                    }
                    holder.binding.downloadAction.setPause(list[position].item.isPaused)
                }
            }
        }
        holder.binding.root.setOnLongClickListener {
            val builder = AlertDialog.Builder(home)
            builder.setTitle("Remove download?")
            builder.setNegativeButton("No", null)
            builder.setPositiveButton("Yes") { _, _ ->
                Repository.removeDownloads(list[position].item, home.applicationContext)
            }
            builder.show()
            return@setOnLongClickListener false
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun notifyAdapter() {
        list.clear()
        list.addAll(Repository.getList(isDownloaded))
        notifyDataSetChanged()
    }
}