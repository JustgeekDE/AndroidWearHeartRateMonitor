package de.justgeek.foregroundservicetest.Sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import de.justgeek.foregroundservicetest.RepeatableThread
import de.justgeek.foregroundservicetest.RepeatingThread

abstract class SensorCollector : SensorEventListener, RepeatableThread, DataCollector {
  protected var TAG = "SensorCollector"

  protected var sensorManager: SensorManager
  protected lateinit var sensor: Sensor

  override var values: MutableList<SensorData> = mutableListOf<SensorData>()

  protected val sensorThreshhold: Int
  protected val samplingThread: RepeatingThread

  protected var isSampling = false

  constructor(sensorManager: SensorManager, interval: Int = 30, sensorThreshhold: Int = SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) {
    this.sensorManager = sensorManager
    this.sensorThreshhold = sensorThreshhold
    this.samplingThread = RepeatingThread(this, interval)
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
      Log.d(TAG, "Got sensor: " + sensor + "reporting mode: " + sensor.reportingMode + "power: " + sensor.power)
      this.sensor = sensor
    } else {
      Log.e(TAG, "No sensor of type " + sensorType + " found")
      return false
    }
    return true;
  }

  protected fun stopSensorListener() {
    Log.d(TAG, "Stopping sensor listener")
    isSampling = false
    sensorManager.unregisterListener(this);
    Log.d(TAG, "Stoped sensor listener")
  }

  protected fun startSensorListener() {
    Log.d(TAG, "Registering sensor listener")
    if (!isSampling) {
      isSampling = true
      sensorManager.registerListener(this, sensor, 1000, 100000)
    }
  }

  override fun onSensorChanged(event: SensorEvent?) {
    if (!isSampling) {
      stopSensorListener()
    }

    val eventSensor = event?.sensor ?: return

    if (eventSensor.type == sensor.type) {
      storeEvent(event)
    }
  }


  abstract fun storeEvent(event: SensorEvent)

  override fun run() {
  }

  override fun cancel() {
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}