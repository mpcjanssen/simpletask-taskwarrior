package nl.mpcjanssen.simpletask

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "Executing Alarm callback")
        // Update UI (widgets and main screen)
        STWApplication.app.localBroadCastManager.sendBroadcast(Intent(Constants.BROADCAST_UPDATE_UI))
    }

    companion object {
        private val TAG = AlarmReceiver::class.java.simpleName
    }
}
