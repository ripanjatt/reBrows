package com.ripanjatt.rebrows.adapters

import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import com.ripanjatt.rebrows.R
import com.ripanjatt.rebrows.util.Util
import java.text.DecimalFormat
import kotlin.math.pow

@BindingAdapter("setDrawable")
fun Button.setDrawable(mode: Boolean) {
    if(mode) {
        this.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_directions_run_active_24, 0, 0, 0)
        this.setBackgroundResource(R.drawable.circular)
        this.setTextColor(ContextCompat.getColor(context, R.color.baby_blue))
    } else {
        this.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_directions_run_24, 0, 0, 0)
        this.setBackgroundColor(Color.TRANSPARENT)
        this.setTextColor(ContextCompat.getColor(context, R.color.black_custom))
    }
}

@BindingAdapter("setIncognito")
fun Button.setIncognito(mode: Boolean) {
    if(mode) {
        this.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_remove_red_eye_active_24, 0, 0, 0)
        this.setBackgroundResource(R.drawable.circular)
        this.setTextColor(ContextCompat.getColor(context, R.color.baby_blue))
    } else {
        this.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_remove_red_eye_24, 0, 0, 0)
        this.setBackgroundColor(Color.TRANSPARENT)
        this.setTextColor(ContextCompat.getColor(context, R.color.black_custom))
    }
}

@BindingAdapter("setMipmap")
fun ImageView.setMipmap(mimeType: String) {
    this.setImageResource(Util.getMipmap(mimeType))
}

@BindingAdapter("setPause")
fun ImageButton.setPause(isPaused: Boolean) {
    if(isPaused) {
        this.setImageResource(R.drawable.ic_baseline_play_arrow_24)
    } else {
        this.setImageResource(R.drawable.ic_baseline_pause_24)
    }
}

@BindingAdapter("setVisibility")
fun ImageButton.setVisibility(isDownloaded: Boolean) {
    this.visibility = if(isDownloaded) View.GONE else View.VISIBLE
}

@BindingAdapter("setSize")
fun TextView.setSize(long: Long) {
    this.text = (DecimalFormat("0.00").format((long.toDouble() / 1024.0.pow(2))) + " MB")
}