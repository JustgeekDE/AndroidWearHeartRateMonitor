package de.justgeek.foregroundservicetest.Sensor

import android.hardware.SensorEvent
import android.hardware.SensorManager

class StatisticsSensorCollector : SensorCollector {
  var lastMinute: MutableList<SensorData> = mutableListOf<SensorData>()

  constructor(sensorManager: SensorManager, interval: Int = 60, sensorThreshhold: Int = SensorManager.SENSOR_STATUS_ACCURACY_LOW) : super(sensorManager, interval, sensorThreshhold) {
    TAG = "StatisticsSensorCollector"
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
      this.lastMinute.add(SensorData(event))
    }
  }

  override fun run() {
    val currentMinute = lastMinute
    lastMinute = mutableListOf<SensorData>()

    if (currentMinute.size > 0) {
      val nrValues = currentMinute[0].values.size
      val result = FloatArray(3 * nrValues)

      for (index in 0..nrValues - 1) {
        val data = mutableListOf<Float>()
        for (entry in currentMinute) {
          data.add(entry.values[index])
        }

        data.sort()
        result[index * 3 + 0] = data.min() ?: 0f
        result[index * 3 + 1] = data[data.size / 2]
        result[index * 3 + 2] = data.max() ?: 0f
      }
      values.add(SensorData(result))
    }
  }
}