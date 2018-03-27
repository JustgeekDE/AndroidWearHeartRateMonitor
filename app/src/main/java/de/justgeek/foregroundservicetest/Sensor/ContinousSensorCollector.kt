package de.justgeek.foregroundservicetest.Sensor

import android.hardware.SensorEvent
import android.hardware.SensorManager

class ContinousSensorCollector : SensorCollector {

  constructor(sensorManager: SensorManager, interval: Int = 30, sensorThreshhold: Int = SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) : super(sensorManager, interval, sensorThreshhold) {
    TAG = "ContinousSensorCollector"
  }

  override fun start() {
    startSensorListener()
    samplingThread.start()
  }

  override fun stop() {
    stopSensorListener()
    samplingThread.stop()
  }

  override fun storeEvent(event: SensorEvent) {
    if (event.accuracy >= sensorThreshhold) {
      this.values.add(SensorData(event))
    }
  }
}