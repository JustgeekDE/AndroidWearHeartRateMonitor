package de.justgeek.foregroundservicetest

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import java.util.*

class SensorCollector : SensorEventListener {

  val TAG = "SensorCollector"

  private lateinit var sensorManager: SensorManager
  private lateinit var sensor: Sensor

  private var values: MutableList<SensorEvent> = mutableListOf<SensorEvent>()

  private var maxRetries: Int
  private var retries = 0

  constructor(sensorManager: SensorManager, retries: Int) {
    this.sensorManager = sensorManager
    this.maxRetries = retries
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
    sensorManager.unregisterListener(this);
  }

  private fun startSensorListener() {
    sensorManager.registerListener(this, sensor, 1000)
  }

  override fun onSensorChanged(event: SensorEvent?) {
    val eventSensor = event?.sensor ?: return

    if (eventSensor.type == sensor.type) {
      retries -= 1
      Log.d(TAG, "Got new sensor information")
      Log.d(TAG, "Value: " + event.values[0] + " accuracy: " + event.accuracy)

      if (event.accuracy >= SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) {
        this.values.add(event)
        stopSensorListener()
      } else {
        if (retries <= 0) {
          stopSensorListener()
        }
      }
    }
  }
  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

}