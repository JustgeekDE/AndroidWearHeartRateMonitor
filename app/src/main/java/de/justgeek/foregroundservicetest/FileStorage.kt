package de.justgeek.foregroundservicetest

import android.hardware.SensorEvent
import android.os.Environment
import android.text.format.DateFormat
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class FileStorage {

  private val name: String
  private val timeOffset: Long

  constructor(name: String) {
    this.name = name
    this.timeOffset = System.nanoTime()
  }

  fun storeData(data: List<SensorEvent>): Boolean {
    val file = createNewFile(name) ?: return false;

    file.write("Timestamp, seconds, time, accuracy, values\n".toByteArray())

    for (entry in data) {
      var dataString = ""
      val sensorTimeInUTC = Date().time + (entry.timestamp - System.nanoTime()) / 1000000L
      val sensorTimeSinceStart = (entry.timestamp - timeOffset) / 1000000L

      dataString += (entry.timestamp).toString()
      dataString += ", " + (sensorTimeSinceStart / 1000).toString()
      dataString += ", " + getDate(sensorTimeInUTC)
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
    val timestamp = System.currentTimeMillis() / 1000
    val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    try {
      var fileName = String.format("%s/%s.-.%d.csv", downloadDir.getAbsolutePath(), filename, timestamp)
      fileName = fileName.replace(" ", ".")
      return FileOutputStream(File(fileName), false)
    } catch (e: FileNotFoundException) {
    }

    return null

  }

  private fun getDate(time: Long): String {
    val cal = Calendar.getInstance(Locale.ENGLISH)
    cal.timeInMillis = time
    return DateFormat.format("hh:mm.ss", cal).toString()
  }
}



