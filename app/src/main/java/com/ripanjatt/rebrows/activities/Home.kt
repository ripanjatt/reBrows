package com.ripanjatt.rebrows.activities

import android.os.Bundle
import android.view.ContextMenu
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.ripanjatt.rebrows.R
import com.ripanjatt.rebrows.adapters.PagerAdapter
import com.ripanjatt.rebrows.adapters.TabAdapter
import com.ripanjatt.rebrows.adapters.setDrawable
import com.ripanjatt.rebrows.adapters.setIncognito
import com.ripanjatt.rebrows.databinding.ActivityHomeBinding
import com.ripanjatt.rebrows.downloader.Downloader
import com.ripanjatt.rebrows.util.*
import java.text.SimpleDateFormat
import java.util.*

class Home : AppCompatActivity() {

    private var currentTab: Tab? = null

    private lateinit var binding: ActivityHomeBinding
    private lateinit var tabAdapter: TabAdapter
    private var menuSheet: BottomSheetDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        tabAdapter = TabAdapter(this)
        Util.getPermission(this)
        currentTab = Repository.newTab(null, this)
        binding.mainFrame.addView(currentTab?.getView())
        Repository.setUpAdapters(this)
        setUpPager()
        binding.downloadsView.downloadsGoBack.setOnClickListener {
            onBackPressed()
        }
        binding.history.historyGoBack.setOnClickListener {
            onBackPressed()
        }
        binding.settings.settingsGoBack.setOnClickListener {
            onBackPressed()
        }
        binding.history.clearHistory.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Clear history?")
            builder.setNegativeButton("No", null)
            builder.setPositiveButton("Yes") { _, _ ->
                Repository.clearHistory(this)
            }
            builder.show()
        }
        binding.addTab.setOnClickListener {
            currentTab = Repository.newTab(null, this@Home)
            hideTabs(currentTab!!)
        }
    }

    fun menu(view: View) {
        if(menuSheet?.isShowing == true) {
            menuSheet?.dismiss()
        }
        when(view.id) {
            R.id.menuPrev -> {
                currentTab?.goBack()
            }
            R.id.menuHome -> {
                currentTab?.goHome()
            }
            R.id.menuButton -> {
                menuSheet = LayoutUtil.menuDialog(this)
            }
            R.id.menuTabs -> {
                showTabs()
            }
            R.id.menuNext -> {
                currentTab?.goForward()
            }
            R.id.sheetSettings -> {
                binding.home.visibility = View.GONE
                binding.settings.root.visibility = View.VISIBLE
            }
            R.id.sheetExit -> {
                Util.exitDialog(this)
            }
            R.id.sheetSpeedMode -> {
                Repository.setSpeedMode(!Util.getSpeedMode(this))
                Util.setSpeedMode(!Util.getSpeedMode(this), this)
                (view as Button).setDrawable(Util.getSpeedMode(this))
                Toast.makeText(this, "Speed Mode toggled!", Toast.LENGTH_SHORT).show()
            }
            R.id.sheetDownloads -> {
                binding.home.visibility = View.GONE
                binding.downloadsView.root.visibility = View.VISIBLE
            }
            R.id.sheetHistory -> {
                binding.home.visibility = View.GONE
                binding.history.root.visibility = View.VISIBLE
            }
            R.id.sheetIncognito -> {
                Util.setIncognito(!Util.getIncognito(this), this)
                (view as Button).setIncognito(Util.getIncognito(this))
            }
            R.id.checkSpeed -> {
                LayoutUtil.checkSpeed(this)
            }
            R.id.about -> {
                Util.aboutDialog(this)
            }
        }
    }

    fun hideTabs(tab: Tab) {
        currentTab = tab
        binding.pager.adapter = null
        tabAdapter.removeAll()
        binding.tabSelector.visibility = View.GONE
        binding.home.visibility = View.VISIBLE
        binding.mainFrame.addView(tab.getView())
    }

    fun load(url: String) {
        onBackPressed()
        currentTab?.loadCurrent(url)
    }

    private fun showTabs() {
        binding.mainFrame.removeAllViews()
        binding.pager.adapter = tabAdapter
        binding.pager.scrollToPosition(Repository.getTabs().indexOf(currentTab))
        binding.home.visibility = View.GONE
        binding.tabSelector.visibility = View.VISIBLE
    }

    private fun setUpPager() {
        binding.pager.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.pager.clipToPadding = false
        binding.pager.clipChildren = false
        val list = arrayListOf(
            LayoutUtil.createDownloadTabs(false, this),
            LayoutUtil.createDownloadTabs(true, this)
        )
        binding.downloadsView.pager.adapter = PagerAdapter(list, this)
        binding.downloadsView.tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.downloadsView.pager.setCurrentItem(binding.downloadsView.tabLayout.selectedTabPosition, true)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
        binding.downloadsView.pager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.downloadsView.tabLayout.selectTab(binding.downloadsView.tabLayout.getTabAt(position))
            }
        })
        binding.history.historyRecycler.layoutManager = LinearLayoutManager(this)
        binding.history.historyRecycler.adapter = Repository.getHistoryAdapter()
        ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.UP) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if(Repository.getTabs().size == 1) {
                    currentTab?.goHome()
                    hideTabs(currentTab!!)
                } else {
                    if(viewHolder.adapterPosition == Repository.getTabs().indexOf(currentTab!!)) {
                        Repository.removeTab(Repository.getTabs()[viewHolder.adapterPosition])
                        currentTab = Repository.getTabs()[0]
                    } else {
                        Repository.removeTab(Repository.getTabs()[viewHolder.adapterPosition])
                    }
                }
                tabAdapter.removeAll()
                tabAdapter.notifyDataSetChanged()
            }
        }).attachToRecyclerView(binding.pager)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val results = currentTab?.getHitTestResults()
        if(results != null) {
            when(results.type) {
                WebView.HitTestResult.SRC_ANCHOR_TYPE -> {
                    menu?.add(0, 1, 0, "Open in new Tab")
                        ?.setOnMenuItemClickListener {
                            binding.mainFrame.removeAllViews()
                            currentTab = Repository.newTab(results.extra, this)
                            binding.mainFrame.addView(currentTab?.getView())
                            return@setOnMenuItemClickListener false
                        }
                }
                WebView.HitTestResult.IMAGE_TYPE -> {
                    menu?.add(0, 1, 0, "Download Image")
                        ?.setOnMenuItemClickListener {
                            Repository.updateHistory(
                                History(results.extra.toString(), SimpleDateFormat.getDateTimeInstance().format(
                                    Date()
                                )),
                                this@Home
                            )
                            Downloader.saveImage(results.extra.toString(), this@Home)
                            return@setOnMenuItemClickListener false
                        }
                }
            }
        }
    }

    override fun onBackPressed() {
        when {
            binding.settings.root.visibility == View.VISIBLE -> {
                binding.settings.root.visibility = View.GONE
                binding.home.visibility = View.VISIBLE
            }
            binding.history.root.visibility == View.VISIBLE -> {
                binding.history.root.visibility = View.GONE
                binding.home.visibility = View.VISIBLE
            }
            binding.downloadsView.root.visibility == View.VISIBLE -> {
                binding.downloadsView.root.visibility = View.GONE
                binding.home.visibility = View.VISIBLE
            }
            binding.tabSelector.visibility == View.VISIBLE -> {
                currentTab?.let { hideTabs(it) }
            }
            currentTab?.goBack() != true -> {
                Util.exitDialog(this)
            }
        }
    }
}