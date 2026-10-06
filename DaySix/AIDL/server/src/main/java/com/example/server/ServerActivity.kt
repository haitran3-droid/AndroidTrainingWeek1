package com.example.server

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.view.WindowInsets
import android.widget.LinearLayout
import android.widget.TextView

class ServerActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server)
        findViewById<TextView>(R.id.server_pid).text = getString(R.string.server_pid, Process.myPid())
        val root = findViewById<LinearLayout>(R.id.root)
        val padding = (20 * resources.displayMetrics.density).toInt()
        root.setOnApplyWindowInsetsListener { view, insets ->
            val bars = if (Build.VERSION.SDK_INT >= 30) insets.getInsets(WindowInsets.Type.systemBars()) else android.graphics.Insets.of(insets.systemWindowInsetLeft, insets.systemWindowInsetTop, insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            view.setPadding(padding + bars.left, padding + bars.top, padding + bars.right, padding + bars.bottom)
            insets
        }
    }
}
