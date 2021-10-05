package logging

import android.util.Log

class AndroidLogger(unused: String? = null) : Logger {
    @Synchronized
    override fun log(vararg values: String, logLevel: LogLevels?, timeInMs: Long?) {
        if (values.isEmpty()) return

        if (values.size == 1) {
            this.log0(values[0], logLevel)
            return
        }

        val builder = StringBuilder()
        values.forEach {
            builder.append(it).append("\t")
        }

        this.log0(builder.substring(0, builder.length-1), logLevel)
    }

    private fun log0(message: String, logLevel: LogLevels?) {
        when (logLevel) {
            LogLevels.ERROR -> Log.e("NetworkingService", message)
            LogLevels.DEBUG -> Log.d("NetworkingService", message)
            else -> Log.i("NetworkingService", message)
        }
    }

    override fun close() {
        // NOT NEEDED
    }
}
