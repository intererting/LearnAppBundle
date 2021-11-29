package com.yly.learnappbundle

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import com.google.android.play.core.splitinstall.testing.FakeSplitInstallManager
import com.google.android.play.core.splitinstall.testing.FakeSplitInstallManagerFactory
import java.util.*

const val STORAGE_MODULE = "storage"

class MainActivity : AppCompatActivity() {

    private var sessionId = 0
    private lateinit var splitInstallManager: FakeSplitInstallManager
    private var storageModule: MyAABInterface? = null


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
        splitInstallManager = FakeSplitInstallManagerFactory.create(application)
        splitInstallManager.registerListener(listener)
        findViewById<Button>(R.id.test).setOnClickListener {
            //通过SPI加载storage模块化
//            val services = ServiceLoader.load(MyAABInterface::class.java, MyAABInterface::class.java.classLoader)
//            val utils = services.iterator().next()
//            utils.test()

            //动态加载
            if (storageModule == null) {
                if (isStorageInstalled()) {
                    initializeStorageFeature()
                } else {
                    requestStorageInstall()
                }
            }
            if (storageModule != null) {
                storageModule!!.test()
                Toast.makeText(getApplication(), "Counter saved to storage", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initializeStorageFeature() {
        val services = ServiceLoader.load(MyAABInterface::class.java, MyAABInterface::class.java.classLoader)
        storageModule = services.iterator().next()
    }

    private fun requestStorageInstall() {
        Toast.makeText(getApplication(), "Requesting storage module install", Toast.LENGTH_SHORT)
            .show()
        val request =
            SplitInstallRequest
                .newBuilder()
                .addModule("storage")
                .build()

        splitInstallManager
            .startInstall(request)
            .addOnSuccessListener { id -> sessionId = id }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error installing module: ", exception)
                Toast.makeText(
                    getApplication(),
                    "Error requesting module install",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun isStorageInstalled() =
        splitInstallManager.installedModules.contains(STORAGE_MODULE)

    override fun onDestroy() {
        splitInstallManager.unregisterListener(listener)
        super.onDestroy()
    }
}