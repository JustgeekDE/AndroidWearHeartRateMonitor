package de.justgeek.foregroundservicetest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.widget.Button
import android.content.ComponentName
import de.justgeek.foregroundservicetest.ForegroundService.LocalBinder
import android.os.IBinder
import android.content.ServiceConnection




class MainActivity : WearableActivity() {

  val TAG = "MainActivity"
  var serviceStarted = false;
  private var mService: ForegroundService? = null
  var mBound = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // Enables Always-on
    setAmbientEnabled()

    findViewById<Button>(R.id.startButton).setOnClickListener { _ ->
      buttonToggle()
    }
    startService("start")
    setButtonLabel()
  }

  override fun onDestroy() {
    super.onDestroy()
    unbindService(mConnection)
  }

  private fun buttonToggle() {
    if (serviceStarted) {
      mService?.stop()
      serviceStarted = false
    } else {
      mService?.start()
      serviceStarted = true
    }
    setButtonLabel()
  }

  private fun setButtonLabel() {
    var started: Boolean = mService?.isStarted() ?: false
    if (started) {
      findViewById<Button>(R.id.startButton).setText("Stop")
    } else {
      findViewById<Button>(R.id.startButton).setText("Start")
    }
  }

  private fun startService(action: String) {
    val intent = Intent(applicationContext, ForegroundService::class.java)
    intent.action = action
    startService(intent)
    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
  }

  /** Defines callbacks for service binding, passed to bindService()  */
  private val mConnection = object : ServiceConnection {

    override fun onServiceConnected(className: ComponentName,
                                    service: IBinder) {
      // We've bound to LocalService, cast the IBinder and get LocalService instance
      val binder = service as LocalBinder
      this@MainActivity.mService = binder.service
      this@MainActivity.mBound = true
    }

    override fun onServiceDisconnected(arg0: ComponentName) {
      this@MainActivity.mBound = false
    }
  }
}
