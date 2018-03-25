package de.justgeek.foregroundservicetest

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager.EXTRA_LEVEL
import android.os.BatteryManager.EXTRA_SCALE
import android.util.Log


class BatteryCollector {

  val TAG = "BatteryCollector"

  var values: MutableList<SensorData> = mutableListOf<SensorData>()

  private val context: Context
  private var mainThreadRunning = false
  private val interval: Int

  constructor(context: Context, interval: Int = 30) {
    this.context = context
    this.interval = interval
  }

  fun start() {
    if (mainThreadRunning == false) {
      mainThreadRunning = true
      Thread(Runnable {
        while (mainThreadRunning == true) {
          Log.d(TAG, "Main Thread")
          sampleBattery()
          Thread.sleep(interval * 1000L)
        }
        Log.d(TAG, "Main Thread finished")
      }).start()
    }
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

  fun stop() {
    mainThreadRunning = false
  }


}