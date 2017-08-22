/**
 * @copyright 2013- Mark Janssen
 */
package nl.mpcjanssen.simpletask

import android.app.Activity
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.*
import android.support.v4.content.LocalBroadcastManager
import nl.mpcjanssen.simpletask.remote.*
import nl.mpcjanssen.simpletask.task.TaskList
import nl.mpcjanssen.simpletask.util.Config
import nl.mpcjanssen.simpletask.util.appVersion
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.verbose
import java.util.*

class STWApplication : Application(), FileSelectedListener , AnkoLogger {

    lateinit var localBroadCastManager: LocalBroadcastManager
    override fun onCreate() {
        app = this
        super.onCreate()
        localBroadCastManager = LocalBroadcastManager.getInstance(this)


        info("Created task list " + TaskList)
        info("onCreate()")
        info("Started ${appVersion(this)}}")
        scheduleOnNewDay()
        loadTodoList("Initial load")
    }


    private fun scheduleOnNewDay() {
        // Schedules activities to run on a new day
        // - Refresh widgets and UI
        // - Cleans up logging

        val calendar = Calendar.getInstance()

        // Prevent alarm from triggering for today when setting it
        calendar.add(Calendar.DATE, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 2)
        calendar.set(Calendar.SECOND, 0)

        info("Scheduling daily UI updateCache alarm, first at ${calendar.time}")
        val pi = PendingIntent.getBroadcast(this, 0,
                Intent(this, AlarmReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
        val am = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY, pi)
    }

    fun switchTodoFile(newTodo: String) {
        Config.rcFileName = newTodo
        loadTodoList("from file switch")
    }

    fun loadTodoList(reason: String) {
        verbose("Reloading file: $reason")
        TaskList.reload()
    }

    override fun fileSelected(fileName: String) {
        Config.rcFileName = fileName
        loadTodoList("from fileChanged")
    }

    fun browseForNewFile(act: Activity) {
        FileDialog.browseForNewFile(
                act,
                Config.rcFile.parent,
                listener = object : FileSelectedListener {
                    override fun fileSelected(fileName: String) {
                        switchTodoFile(fileName)
                    }
                })
    }

    companion object {
        fun atLeastAPI(api: Int): Boolean = android.os.Build.VERSION.SDK_INT >= api
        lateinit var app : STWApplication
    }
}

