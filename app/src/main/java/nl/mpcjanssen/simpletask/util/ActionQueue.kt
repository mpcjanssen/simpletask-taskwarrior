package nl.mpcjanssen.simpletask.util

import android.os.Handler
import android.os.Looper
import android.util.Log

object ActionQueue : Thread() {
    private var mHandler: Handler? = null
    private val TAG = ActionQueue::class.java.simpleName
    init {
        start()
    }
    override fun run(): Unit {
        Looper.prepare()
        mHandler = Handler() // the Handler hooks up to the current Thread
        Looper.loop()
    }

    fun hasPending () : Boolean {
        return mHandler?.hasMessages(0) ?: false
    }

    fun add(description: String, r: Runnable, silent: Boolean = false) {
        while (mHandler == null) {
            if (!silent) {
                Log.d(TAG, "Queue handler is null, waiting")
            }
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        if (!silent) {
            Log.i(TAG, "Adding to queue: $description" )
            mHandler?.post(LoggingRunnable(description, r))
        } else {
            mHandler?.post (r)
        }

    }
//    private fun buildStackTraceString(elements: Array<StackTraceElement>) : String {
//        val sb = StringBuilder();
//        elements.reverse()
//        elements.forEach {
//            sb.append("\n" + it.toString());
//        }
//        return sb.toString();
//    }
}

class LoggingRunnable (val description: String, val runnable: Runnable) : Runnable {
    private val TAG = ActionQueue::class.java.simpleName

    override fun toString(): String {
        return description
    }

    override fun run() {
        Log.i(TAG, "Executing action " + description)
        runnable.run()
    }
}

