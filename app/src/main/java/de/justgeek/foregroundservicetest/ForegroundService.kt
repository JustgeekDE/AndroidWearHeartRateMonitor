package de.justgeek.foregroundservicetest

import android.app.IntentService
import android.content.Intent
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.support.v4.app.NotificationManagerCompat
import android.os.PowerManager


class ForegroundService : IntentService("Foreground") {
  private val TAG = "Forgeground service"
  private var mainThreadRunning = false;
  private lateinit var heartRateCollector: SensorCollector

  override fun onCreate() {
    Log.d(TAG, "Creating service " + this.hashCode())
    super.onCreate()

    val notificationId = 1
    val notificationIntent = Intent(this, MainActivity::class.java)

    val pendingIntent = PendingIntent.getActivity(this, 0,
        notificationIntent, 0)

    val notification = NotificationCompat.Builder(this)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("Heart rate sensor")
        .setContentText("Collecting heart rate")
        .setOngoing(true)
        .setContentIntent(pendingIntent).build()


    val notificationManager = NotificationManagerCompat.from(this)
    notificationManager.notify(notificationId, notification)

    heartRateCollector = SensorCollector(getSystemService(Context.SENSOR_SERVICE) as SensorManager, 5)
    heartRateCollector.setSensor(Sensor.TYPE_LIGHT)

    startForeground(1, notification)

//        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
//        val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag")
//        wl.acquire()

  }

  override fun onHandleIntent(intent: Intent?) {
    if (intent?.action == "stop") {
      Log.d(TAG, "stoping service")
      mainThreadRunning = false;
    } else if (intent?.action == "start" && !mainThreadRunning) {
      Log.d(TAG, "starting service")
      mainThread()
    }
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Log.d(TAG, "onStartCommand received")
    Log.i(TAG, "Received start id " + startId + ": " + flags + " / " + intent?.action);
    super.onStartCommand(intent, flags, startId)
    if (intent?.action == "stop") {
      Log.d(TAG, "stoping service")
      mainThreadRunning = false;
    }

    return Service.START_REDELIVER_INTENT
  }

  private fun mainThread() {
    mainThreadRunning = true
    while (mainThreadRunning == true) {
      Log.d(TAG, "Main Thread")
      heartRateCollector.startSampling()
      Thread.sleep(30000)
    }
  }

  override fun onDestroy() {
    Log.d(TAG, "Destroying " + this.hashCode())
    super.onDestroy()
  }
}