package de.justgeek.foregroundservicetest

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.widget.Button
import android.widget.TextView
import de.justgeek.foregroundservicetest.ForegroundService.LocalBinder


class MainActivity : WearableActivity() {

  val TAG = "MainActivity"
  private val MY_PERMISSIONS_REQUEST_POWER: Int = 0
  private val MY_PERMISSIONS_REQUEST_BODY_SENSORS: Int = 1
  private val MY_PERMISSIONS_REQUEST_STORAGE: Int = 2

  var serviceStarted = false
  var saved = false
  private var mService: ForegroundService? = null
  var mBound = false
  var runDisplayUpdate = true
  var storageHelper = FileStorage()


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // Enables Always-on
    setAmbientEnabled()

    wireButtons()
    requestPermissions()
    startService("start")
    setButtonLabel()
  }

  private fun requestPermissions() {
    requestPermission(Manifest.permission.BODY_SENSORS, MY_PERMISSIONS_REQUEST_BODY_SENSORS)
    requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_STORAGE)
    requestPermission(Manifest.permission.WAKE_LOCK, MY_PERMISSIONS_REQUEST_POWER)
  }

  private fun requestPermission(permission: String, id: Int) {
    if (ContextCompat.checkSelfPermission(this, permission)
        != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this,
          arrayOf(permission), id)
    }
  }

  private fun wireButtons() {
    findViewById<Button>(R.id.startButton).setOnClickListener { _ ->
      buttonToggle()
    }
    findViewById<Button>(R.id.exitButton).setOnClickListener { _ ->
      Log.d(TAG, "Exit pressed")
      onExitPressed()
    }
    findViewById<Button>(R.id.saveButton).setOnClickListener { _ ->
      Log.d(TAG, "Save pressed")
      onSavePressed()
    }
    findViewById<Button>(R.id.saveButton).setTextColor(getColor(R.color.light_grey))
    saved = false

    Log.d(TAG, "Save button wired")
  }

  override fun onDestroy() {
    super.onDestroy()
    unbindService(mConnection)
  }

  override fun onPause() {
    runDisplayUpdate = false
    super.onPause()
  }

  override fun onResume() {
    super.onResume()
    runDisplayUpdate = true


    val handler = Handler()
    val r = object : Runnable {
      override fun run() {
        if (runDisplayUpdate) {
          handler.postDelayed(this, 5000)
          updateDisplay()
        }
      }
    }
    handler.postDelayed(r, 0)
  }

  private fun updateDisplay() {
    if (mService != null) {
      val values = (this.mService as ForegroundService).getValues(ForegroundService.SENORS.HEARTRATE)
      val count = values.size
      var lastHeartRate = ""
      if (count > 0) {
        lastHeartRate += (values.last().values[0]).toInt()
      }
      findViewById<TextView>(R.id.heartRate).setText(lastHeartRate)
      findViewById<TextView>(R.id.count).setText("" + count)
    }
  }

  private fun buttonToggle() {
    if (serviceStarted) {
      mService?.stop()
      serviceStarted = false
    } else {
      mService?.start()
      serviceStarted = true
      setSaveEnabled(true)
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

  fun onExitPressed() {
    finish()
  }

  fun onSavePressed() {
    if ((mService != null) && (saved == false)) {
      val foregroundService = this.mService as ForegroundService
      Thread(Runnable {
        Log.d(TAG, "Starting to store data")
        setSaveButtonColor(R.color.red)
        Thread.sleep(250)

        storageHelper.storeData("heartrate", foregroundService.getValues(ForegroundService.SENORS.HEARTRATE))
        storageHelper.storeData("rotation", foregroundService.getValues(ForegroundService.SENORS.ROTATION))
        storageHelper.storeData("battery", foregroundService.getValues(ForegroundService.SENORS.BATTERY))
        storageHelper.storeData("acceleration", foregroundService.getValues(ForegroundService.SENORS.ACCELERATION))
        saved = true
        Log.d(TAG, "Done storing data")

        setSaveEnabled(false)
      }).start()

    } else {
      Log.d(TAG, "Ignoring save, because there is no new data")
    }
  }

  private fun setSaveButtonColor(color: Int) {
    runOnUiThread(Runnable {
      findViewById<Button>(R.id.saveButton).setTextColor(getColor(color))
    })
  }

  private fun setSaveEnabled(enabled: Boolean) {
    if(enabled) {
      saved = false
      setSaveButtonColor(R.color.green)
    } else {
      saved = true
      setSaveButtonColor(R.color.grey)
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
