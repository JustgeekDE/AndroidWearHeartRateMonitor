package de.justgeek.foregroundservicetest.Sensor

import android.hardware.SensorEvent

class SensorData {
  val accuracy: Int
  val timestamp: Long
  val values: FloatArray

  constructor(event : SensorEvent) {
    this.accuracy = event.accuracy
    this.timestamp = event.timestamp
    this.values = event.values
  }

  constructor(timestamp: Long, accuracy: Int, values: FloatArray) {
    this.timestamp = timestamp
    this.accuracy = accuracy
    this.values = values
  }

  constructor(values: FloatArray) {
    this.accuracy = -1
    this.values = values
    this.timestamp = System.nanoTime()
  }
}