package com.ripanjatt.rebrows.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ripanjatt.rebrows.activities.Home
import com.ripanjatt.rebrows.databinding.HistoryItemBinding
import com.ripanjatt.rebrows.util.Repository

class HistoryAdapter(private val home: Home): RecyclerView.Adapter<HistoryAdapter.HistoryHolder>() {

    val list = Repository.getHistory()

    class HistoryHolder(val binding: HistoryItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryHolder {
        return HistoryHolder(
            HistoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: HistoryHolder, position: Int) {
        holder.binding.item = list[position]
        holder.binding.root.setOnClickListener {
            home.load(list[position].url)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun notifyAdapter() {
        list.clear()
        list.addAll(Repository.getHistory())
        this.notifyDataSetChanged()
    }
}