package cz.covid19cz.app.utils

import android.util.Log
import cz.covid19cz.app.BuildConfig

/**
 * Created by stepansonsky on 07/01/16.
 */
object L {

    fun d(text: String) {
        val logStrings = createLogStrings(text)
        if (BuildConfig.DEBUG) {
            Log.d(logStrings[0], logStrings[1])
        }
    }

    fun i(text: String) {
        val logStrings = createLogStrings(text)
    }

    fun w(text: String) {
        val logStrings = createLogStrings(text)
        if (BuildConfig.DEBUG) {
            Log.w(logStrings[0], logStrings[1])
        }
    }

    fun e(text: String) {
        val logStrings = createLogStrings(text)
    }

    fun e(throwable: Throwable) {
        if (BuildConfig.DEBUG) {
            Log.e("L", throwable.message, throwable)
        }
    }

    private fun createLogStrings(text: String): Array<String> {
        val ste = Thread.currentThread().stackTrace
        val line = "(" + (ste[4].fileName + ":" + ste[4].lineNumber + ")")
        return arrayOf(line, text)
    }
}
