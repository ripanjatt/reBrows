package com.ripanjatt.rebrows.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.ripanjatt.rebrows.R
import com.ripanjatt.rebrows.util.Repository

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        Repository.loadDownloads(this)
        Handler(mainLooper).postDelayed({
            startActivity(Intent(this, Home::class.java))
            this.finish()
        }, 2000)
    }
}