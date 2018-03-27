package de.justgeek.foregroundservicetest

import android.app.IntentService
import android.app.Notification
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
import de.justgeek.foregroundservicetest.Sensor.*


class ForegroundService : IntentService("Foreground") {
  enum class SENORS(val index: Int) { HEARTRATE(0), ROTATION(1), BATTERY(2), ACCELERATION(3) }

  private val TAG = "Forgeground service"

  private var isStarted = false;
  private lateinit var wakelock: PowerManager.WakeLock
  private lateinit var notificationBuilder: NotificationCompat.Builder

  private lateinit var dataCollectors: List<DataCollector>

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
    notificationBuilder = createNotification()
    val notification = updateNotification(notificationId, null)

    dataCollectors = setupSensors()

    val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
    wakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
        "MyWakelockTag")
    wakelock.acquire()

    startForeground(1, notification)
  }

  private fun createNotification(): NotificationCompat.Builder {
    val notificationIntent = Intent(this, MainActivity::class.java)

    val pendingIntent = PendingIntent.getActivity(this, 0,
        notificationIntent, 0)

    val notificationBuilder = NotificationCompat.Builder(this)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("Heart rate sensor")
        .setContentText("Collecting heart rate")
        .setOngoing(true)
        .setContentIntent(pendingIntent)
    return notificationBuilder
  }

  private fun updateNotification(notificationId: Int, heartrate: String?): Notification? {
    var message = "Collecting heart rate"
    if (heartrate != null) {
      message = heartrate
    }

    notificationBuilder.setContentText(message)
    val notification = notificationBuilder.build()
    val notificationManager = NotificationManagerCompat.from(this)
    notificationManager.notify(notificationId, notification)
    return notification
  }

  private fun setupSensors(): List<DataCollector> {
    val collectors = mutableListOf<DataCollector>()

    val heartrate = IntervalAverageSensorCollector(getSystemService(Context.SENSOR_SERVICE) as SensorManager, 15, retries = 10)
    val rotation = IntervalSensorCollector(getSystemService(Context.SENSOR_SERVICE) as SensorManager, 5)
    val acceleration = StatisticsSensorCollector(getSystemService(Context.SENSOR_SERVICE) as SensorManager, sensorThreshhold = 0)

    if (!heartrate.setSensor(Sensor.TYPE_HEART_RATE)) {
      Log.d(TAG, "No heart rate sensor found, falling back to light sensor")
      heartrate.setSensor(Sensor.TYPE_LIGHT)
    }

    if (!rotation.setSensor(Sensor.TYPE_ROTATION_VECTOR)) {
      rotation.setSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
    }

    if (!acceleration.setSensor(Sensor.TYPE_LINEAR_ACCELERATION)) {
      rotation.setSensor(Sensor.TYPE_ACCELEROMETER)
    }

    collectors.add(SENORS.HEARTRATE.index, heartrate)
    collectors.add(SENORS.ROTATION.index, rotation)
    collectors.add(SENORS.BATTERY.index, BatteryCollector(applicationContext, 60))
    collectors.add(SENORS.ACCELERATION.index, acceleration)

    return collectors
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
      for (collector in dataCollectors) {
        collector.start()
      }
    }
    Log.d(TAG, "Start returned")
  }

  fun stop() {
    Log.d(TAG, "Stop called")
    for (collector in dataCollectors) {
      collector.stop()
    }
    isStarted = false
  }

  fun isStarted(): Boolean {
    return isStarted
  }

  fun getValues(sensor: SENORS): List<SensorData> {
    return dataCollectors[sensor.index].values;
  }

  override fun onDestroy() {
    Log.d(TAG, "Destroying " + this.hashCode())
    this.stop()
    wakelock.release()
    super.onDestroy()
  }
}
