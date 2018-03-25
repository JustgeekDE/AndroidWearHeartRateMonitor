package de.justgeek.foregroundservicetest

import android.hardware.Sensor

import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class SensorCollector : SensorEventListener {

  val TAG = "SensorCollector"

  private var sensorManager: SensorManager
  private lateinit var sensor: Sensor

  var values: MutableList<SensorData> = mutableListOf<SensorData>()

  private val sensorThreshhold: Int
  private var maxRetries: Int
  private var retries = 0
  private var mainThreadRunning = false
  private val interval: Int

  private var isSampling = false

  constructor(sensorManager: SensorManager, interval: Int = 30, retries: Int = 5, sensorThreshhold: Int = SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) {
    this.sensorManager = sensorManager
    this.maxRetries = retries
    this.sensorThreshhold = sensorThreshhold
    this.interval = interval
  }

  fun listAllSensors() {
    val deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
    for (sensor: Sensor in deviceSensors) {
      Log.d(TAG, "Sensor:" + sensor.name + ": " + sensor.stringType + ", power: " + sensor.power + " type: " + sensor.type)
    }
  }

  fun start() {
    if (mainThreadRunning == false) {
      mainThreadRunning = true
      Thread(Runnable {
        while (mainThreadRunning == true) {
          Log.d(TAG, "Main Thread")
          startSampling()
          Thread.sleep(interval * 1000L)
        }
        stopSampling()
        Log.d(TAG, "Main Thread finished")
      }).start()
    }
  }

  fun stop() {
    mainThreadRunning = false
  }

  fun setSensor(sensorType: Int): Boolean {
    var sensor = sensorManager.getDefaultSensor(sensorType)
    if (sensor != null) {
      Log.d(TAG, "Got sensor: " + sensor)
      Log.d(TAG, "Reporting: " + sensor.reportingMode)
      Log.d(TAG, "Power: " + sensor.power)
      this.sensor = sensor
      Log.d(TAG, "Registered sensor listener")
    } else {
      Log.e(TAG, "No sensor of type " + sensorType + " found")
      return false
    }
    return true;
  }

  private fun startSampling() {
    this.retries = maxRetries
    startSensorListener()
  }

  private fun stopSampling() {
    stopSensorListener()
  }

  private fun stopSensorListener() {
    Log.d(TAG, "Stopping sensor listener")
    isSampling = false
    sensorManager.unregisterListener(this);
    Log.d(TAG, "Stoped sensor listener")
  }

  private fun startSensorListener() {
    Log.d(TAG, "Registering sensor listener")
    if (!isSampling) {
      isSampling = true
      sensorManager.registerListener(this, sensor, 1000, 100000)
    }
  }

  override fun onSensorChanged(event: SensorEvent?) {
    Log.v(TAG, "Got new sensor information")
    if (!isSampling) {
      stopSensorListener()
    }

    val eventSensor = event?.sensor ?: return

    if (eventSensor.type == sensor.type) {
      retries -= 1
      Log.d(TAG, "Got new sensor information")
      Log.d(TAG, "Value: " + event.values[0] + " accuracy: " + event.accuracy)

      if (event.accuracy >= sensorThreshhold) {
        this.values.add(SensorData(event))
        stopSensorListener()
      } else {
        if (retries <= 0) {
          Log.d(TAG, "Aborting due to too many retries")
          this.values.add(SensorData(event))
          stopSensorListener()
        }
      }
    }
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}