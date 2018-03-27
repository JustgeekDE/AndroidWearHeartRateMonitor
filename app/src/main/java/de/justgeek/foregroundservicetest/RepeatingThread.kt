package de.justgeek.foregroundservicetest


interface RepeatableThread {
  fun run()
  fun cancel()
}

class RepeatingThread {
  private var threadRunning = false
  private var thread: RepeatableThread
  private var interval: Int

  constructor(thread: RepeatableThread, interval: Int) {
    this.thread = thread
    this.interval = interval
  }

  fun start() {
    if (threadRunning == false) {
      threadRunning = true

      Thread(Runnable {
        while (threadRunning == true) {
          thread.run()
          Thread.sleep(interval * 1000L)
        }
        thread.cancel()
      }).start()
    }

  }

  fun stop() {
    threadRunning = false
  }
}