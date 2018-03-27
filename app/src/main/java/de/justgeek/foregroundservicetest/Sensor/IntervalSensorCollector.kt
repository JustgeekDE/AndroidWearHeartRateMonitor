package de.justgeek.foregroundservicetest.Sensor

import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.util.Log

class IntervalSensorCollector : SensorCollector {
  private var maxRetries: Int
  private var retries = 0

  constructor(sensorManager: SensorManager, interval: Int = 30, retries: Int = 5, sensorThreshhold: Int = SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) : super(sensorManager, interval, sensorThreshhold) {
    TAG = "IntervalSensorCollector"
    this.maxRetries = retries
  }

  override fun start() {
    samplingThread.start()
  }

  override fun stop() {
    samplingThread.stop()
  }

  private fun startSampling() {
    this.retries = maxRetries
    startSensorListener()
  }

  private fun stopSampling() {
    stopSensorListener()
  }

  override fun storeEvent(event: SensorEvent) {
    retries -= 1

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

  override fun run() {
    startSampling()
  }

  override fun cancel() {
    stopSampling()
  }
}