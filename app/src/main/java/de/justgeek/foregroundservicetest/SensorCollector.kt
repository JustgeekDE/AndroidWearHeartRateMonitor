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

  var values: MutableList<SensorEvent> = mutableListOf<SensorEvent>()

  private val sensorThreshhold: Int
  private var maxRetries: Int
  private var retries = 0

  private var isRunning = false

  constructor(sensorManager: SensorManager, retries: Int = 5, sensorThreshhold: Int = SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
    this.sensorManager = sensorManager
    this.maxRetries = retries
    this.sensorThreshhold = sensorThreshhold
  }

  fun listAllSensors() {
    val deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
    for (sensor: Sensor in deviceSensors) {
      Log.d(TAG, "Sensor:" + sensor.name + ": " + sensor.stringType + ", power: " + sensor.power + " type: " + sensor.type)
    }
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

  fun startSampling() {
    this.retries = maxRetries
    startSensorListener()
  }

  private fun stopSensorListener() {
    Log.d(TAG, "Stopping sensor listener")
    isRunning = false
    sensorManager.unregisterListener(this);
    Log.d(TAG, "Stoped sensor listener")
  }

  private fun startSensorListener() {
    Log.d(TAG, "Registering sensor listener")
    if(!isRunning){
      isRunning = true
      sensorManager.registerListener(this, sensor, 5000, 500000)
    }
  }

  override fun onSensorChanged(event: SensorEvent?) {
    Log.d(TAG, "Got new sensor information")
    if (!isRunning) {
      stopSensorListener()
    }

    val eventSensor = event?.sensor ?: return

    if (eventSensor.type == sensor.type) {
      retries -= 1
      Log.d(TAG, "Got new sensor information")
      Log.d(TAG, "Value: " + event.values[0] + " accuracy: " + event.accuracy)

      if (event.accuracy >= sensorThreshhold) {
        this.values.add(event)
        stopSensorListener()
      } else {
        if (retries <= 0) {
          Log.d(TAG, "Aborting due to too many retries")
          stopSensorListener()
        }
      }
    }
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
  fun stopSampling() {
    stopSensorListener()
  }

}