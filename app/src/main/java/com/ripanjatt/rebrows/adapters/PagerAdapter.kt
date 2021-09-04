package com.ripanjatt.rebrows.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ripanjatt.rebrows.activities.Home
import com.ripanjatt.rebrows.databinding.PagerLayoutBinding

class PagerAdapter(private val list: ArrayList<View>, home: Home): RecyclerView.Adapter<PagerAdapter.PagerHolder>() {

    class PagerHolder(val binding: PagerLayoutBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerHolder {
        return PagerHolder(
            PagerLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: PagerHolder, position: Int) {
        holder.binding.frame.removeAllViews()
        holder.binding.frame.addView(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }
}