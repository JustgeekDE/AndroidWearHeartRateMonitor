package de.justgeek.foregroundservicetest

import android.os.Environment
import android.text.format.DateFormat
import android.util.Log
import de.justgeek.foregroundservicetest.Sensor.SensorData
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class FileStorage {
  val TAG = "FileStorage"

  private val timeOffset: Long
  private val startTime: Long

  constructor() {
    this.timeOffset = System.nanoTime()
    this.startTime = Date().time / 1000L
  }

  fun storeData(name: String, data: List<SensorData>): Boolean {
    Log.d(TAG, "Storing new file: " + name)
    val file = createNewFile(name) ?: return false;

    file.write("Timestamp, seconds, time, accuracy, values\n".toByteArray())

    for (entry in data) {
      var dataString = ""
      val sensorTimeInUTC = Date().time + (entry.timestamp - System.nanoTime()) / 1000000L
      val sensorTimeSinceStart = (entry.timestamp - timeOffset) / 1000000L

      dataString += (entry.timestamp).toString()
      dataString += ", " + (sensorTimeSinceStart / 1000).toString()
      dataString += ", " + getDate(sensorTimeInUTC)
      dataString += ", " + sensorTimeInUTC
      dataString += ", " + entry.accuracy.toString()
      for (value in entry.values) {
        dataString += ", " + value.toString()
      }
      dataString += "\n"
      file.write(dataString.toByteArray())
    }

    try {
      file.close()
    } catch (e: IOException) {
    }
    return true

  }

  private fun createNewFile(filename: String): FileOutputStream? {
    val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    try {
      var fileName = String.format("%s/%s.-.%d.csv", downloadDir.getAbsolutePath(), filename, startTime)
      fileName = fileName.replace(" ", ".")
      return FileOutputStream(File(fileName), false)
    } catch (e: FileNotFoundException) {
    }

    return null

  }

  private fun getDate(time: Long): String {
    val cal = Calendar.getInstance(Locale.ENGLISH)
    cal.timeInMillis = time - (60 * 60 * 1000)
    return DateFormat.format("HH:mm.ss", cal).toString()
  }
}



