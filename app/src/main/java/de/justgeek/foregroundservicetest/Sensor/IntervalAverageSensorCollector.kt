package de.justgeek.foregroundservicetest.Sensor

import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.util.Log

class IntervalAverageSensorCollector : SensorCollector {
  private var maxRetries: Int
  private var retries = 0
  var window: MutableList<SensorEvent> = mutableListOf<SensorEvent>()
  private val windowSize: Int



  constructor(sensorManager: SensorManager, interval: Int = 30, retries: Int = 5, sensorThreshhold: Int = SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM, windowSize: Int = 5) : super(sensorManager, interval, sensorThreshhold) {
    TAG = "IntervalSensorCollector"
    this.maxRetries = retries
    this.windowSize = windowSize
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
      insertValue(event)
      stopSensorListener()
    } else {
      if (retries <= 0) {
        Log.d(TAG, "Aborting due to too many retries")
        insertValue(event)
        stopSensorListener()
      }
    }
  }

  private fun insertValue(event: SensorEvent) {
    val nrSensorValues = event.values.size
    val result = FloatArray(nrSensorValues *2)

    var index=0
    for (i in 0..(nrSensorValues-1)) {
      result[i] = event.values[i]
      index++
    }

    while (window.size >= windowSize) {
      window.removeAt(0)
    }
    window.add(event)

    this.values.add(SensorData(event))
    for (i in 0..(nrSensorValues -1)) {
      var total = 0.0F
      for (storedEvent in window) {
        total += storedEvent.values[i]
      }
      total = total / window.size
      result[nrSensorValues+i] = total
    }
    values.add(SensorData(event.timestamp, event.accuracy, result))

  }


  override fun run() {
    startSampling()
  }

  override fun cancel() {
    stopSampling()
  }
}