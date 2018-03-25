package de.justgeek.foregroundservicetest

import android.app.IntentService
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log


class ForegroundService : IntentService("Foreground") {
  enum class SENORS { HEARTRATE, ROTATION, BATTERY }

  private val TAG = "Forgeground service"

  private var isStarted = false;
  private lateinit var heartRateCollector: SensorCollector
  private lateinit var rotationCollector: SensorCollector
  private lateinit var batteryCollector: BatteryCollector
  private lateinit var wakelock: PowerManager.WakeLock

  // Binder given to clients
  private val mBinder = LocalBinder()

  inner class LocalBinder : Binder() {
    internal// Return this instance of LocalService so clients can call public methods
    val service: ForegroundService
      get() = this@ForegroundService
  }

  override fun onBind(intent: Intent): IBinder? {
    return mBinder
  }

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

    heartRateCollector = SensorCollector(getSystemService(Context.SENSOR_SERVICE) as SensorManager, 15, retries = 10)
    if (!heartRateCollector.setSensor(Sensor.TYPE_HEART_RATE)) {
      Log.d(TAG, "No heart rate sensor found, falling back to light sensor")
      heartRateCollector.setSensor(Sensor.TYPE_LIGHT)
    }

    rotationCollector = SensorCollector(getSystemService(Context.SENSOR_SERVICE) as SensorManager, interval = 5)
    if(!rotationCollector.setSensor(Sensor.TYPE_ROTATION_VECTOR)) {
      rotationCollector.setSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
    }

    batteryCollector = BatteryCollector(applicationContext, 60)

    heartRateCollector.listAllSensors()

    val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
    wakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
        "MyWakelockTag")
    wakelock.acquire()

    startForeground(1, notification)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    super.onStartCommand(intent, flags, startId)
    return Service.START_REDELIVER_INTENT
  }

  override fun onHandleIntent(intent: Intent?) {
    Log.d(TAG, "Recieved intent")
  }

  fun start() {
    Log.d(TAG, "Start called")

    if (!isStarted) {
      isStarted = true
      heartRateCollector.start()
      rotationCollector.start()
      batteryCollector.start()
    }
    Log.d(TAG, "Start returned")
  }

  fun stop() {
    Log.d(TAG, "Stop called")
    heartRateCollector.stop()
    rotationCollector.stop()
    batteryCollector.stop()
    isStarted = false
  }

  fun isStarted(): Boolean {
    return isStarted
  }


  fun getValues(sensor: SENORS): List<SensorData> {
    if (sensor == SENORS.BATTERY) {
      return this.batteryCollector.values;
    }
    if (sensor == SENORS.ROTATION) {
      return this.rotationCollector.values;
    }
    return this.heartRateCollector.values;
  }

  override fun onDestroy() {
    Log.d(TAG, "Destroying " + this.hashCode())
    this.stop()
    wakelock.release()
    super.onDestroy()
  }
}
