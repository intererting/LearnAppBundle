package com.yly.learnappbundle

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import java.util.*

class MainActivity : AppCompatActivity() {

    private var sessionId = 0

    companion object {
        const val TAG = "MainActivity"
    }

    private val listener = SplitInstallStateUpdatedListener { state ->
        if (state.sessionId() == sessionId) {
            when (state.status()) {
                SplitInstallSessionStatus.FAILED -> {
                    Log.d(TAG, "Module install failed with ${state.errorCode()}")
                    Toast.makeText(
                        application,
                        "Module install failed with ${state.errorCode()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                SplitInstallSessionStatus.INSTALLED -> {
                    Toast.makeText(
                        application,
                        "Storage module installed successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> Log.d(TAG, "Status: ${state.status()}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.test).setOnClickListener {
            //通过SPI加载storage模块化
            val services = ServiceLoader.load(MyAABInterface::class.java, MyAABInterface::class.java.classLoader)
            val utils = services.iterator().next()
            utils.test()
        }
    }
}