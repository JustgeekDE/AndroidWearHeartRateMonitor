package de.justgeek.foregroundservicetest.Sensor

interface DataCollector {
  fun start()
  fun stop()

  val values: List<SensorData>
}