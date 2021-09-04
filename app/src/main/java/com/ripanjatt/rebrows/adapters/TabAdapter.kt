package com.ripanjatt.rebrows.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.ripanjatt.rebrows.activities.Home
import com.ripanjatt.rebrows.databinding.TabHolderBinding
import com.ripanjatt.rebrows.util.Repository

class TabAdapter(val home: Home): RecyclerView.Adapter<TabAdapter.Holder>()  {

    private val list = Repository.getTabs()
    private val map = HashMap<Int, FrameLayout>()

    class Holder(val binding: TabHolderBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            TabHolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.binding.pagerTitle.text = list[position].pageTitle
        holder.binding.pagerFrame.removeAllViews()
        map[position] = holder.binding.pagerFrame
        val view = list[position].getView()
        holder.binding.pagerFrame.addView(view)
        holder.binding.clicker.setOnClickListener {
            holder.binding.pagerFrame.removeAllViews()
            home.hideTabs(list[position])
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun removeAll() {
        map.forEach { (_, frameLayout) ->
            frameLayout.removeAllViews()
        }
        map.clear()
    }
}