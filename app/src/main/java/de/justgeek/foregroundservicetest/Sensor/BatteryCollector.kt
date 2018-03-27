package de.justgeek.foregroundservicetest.Sensor

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager.EXTRA_LEVEL
import android.os.BatteryManager.EXTRA_SCALE
import android.util.Log
import de.justgeek.foregroundservicetest.RepeatableThread
import de.justgeek.foregroundservicetest.RepeatingThread


class BatteryCollector : RepeatableThread, DataCollector {
  val TAG = "BatteryCollector"

  override var values: MutableList<SensorData> = mutableListOf<SensorData>()

  private val context: Context
  private val samplingThread: RepeatingThread

  constructor(context: Context, interval: Int = 30) {
    this.context = context
    this.samplingThread = RepeatingThread(this, interval)
  }

  override fun start() {
    samplingThread.start()
  }

  override fun stop() {
    samplingThread.stop()
  }

  override fun run() {
    sampleBattery()
  }

  override fun cancel() {
  }

  private fun sampleBattery() {
    val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    val batteryStatus = context.registerReceiver(null, ifilter)

    val level = batteryStatus.getIntExtra(EXTRA_LEVEL, -1)
    val scale = batteryStatus.getIntExtra(EXTRA_SCALE, -1)

    val batteryPct = level / scale.toFloat()
    val values = FloatArray(3)
    values[0] = batteryPct
    values[1] = level.toFloat()
    values[2] = scale.toFloat()

    Log.v(TAG, "Adding new battery value " + batteryPct + " ( " + level + " / " + scale + " )")
    this.values.add(SensorData(values))
  }
}