package de.justgeek.foregroundservicetest

import android.os.Bundle
import android.support.wearable.activity.WearableActivity

import android.content.Intent
import android.util.Log
import android.widget.Button


class MainActivity : WearableActivity() {

    val TAG = "MainActivity"
    var serviceStarted = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Enables Always-on
        setAmbientEnabled()

        findViewById<Button>(R.id.startButton).setOnClickListener { _ ->
            buttonToggle()
        }
        setButtonLabel()
    }

    private fun buttonToggle() {
        if (serviceStarted) {
            sendIntent("stop")
            serviceStarted = false
        } else {
            sendIntent("start")
            serviceStarted = true
        }
        setButtonLabel()
    }

    private fun setButtonLabel() {
        if (serviceStarted) {
            findViewById<Button>(R.id.startButton).setText("Stop")
        } else {
            findViewById<Button>(R.id.startButton).setText("Start")
        }
    }

    private fun sendIntent(action: String) {
        Log.d(TAG, "Starting service")
        val intent = Intent(applicationContext, ForegroundService::class.java)
        intent.action = action
//        intent.putExtra(action, true)
        startService(intent)
    }


}
