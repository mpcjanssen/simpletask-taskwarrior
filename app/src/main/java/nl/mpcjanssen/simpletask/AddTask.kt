/**
 * This file is part of Simpletask.
 *
 * @copyright 2013- Mark Janssen
 */
package nl.mpcjanssen.simpletask

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.view.WindowManager
import kotlinx.android.synthetic.main.add_task.*
import nl.mpcjanssen.simpletask.task.TaskList
import nl.mpcjanssen.simpletask.util.Config

class AddTask : ThemedActionBarActivity() {

    private val share_text: String? = null

    private var m_broadcastReceiver: BroadcastReceiver? = null
    private var localBroadcastManager: LocalBroadcastManager? = null

    /*
        Deprecated functions still work fine.
        For now keep using the old version, will updated if it breaks.
     */
    @Suppress("DEPRECATION")
    public override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
        super.onCreate(savedInstanceState)

        STWApplication.app.loadTodoList("before adding tasks")

        val intentFilter = IntentFilter()
        intentFilter.addAction(Constants.BROADCAST_UPDATE_UI)
        intentFilter.addAction(Constants.BROADCAST_SYNC_START)
        intentFilter.addAction(Constants.BROADCAST_SYNC_DONE)

        localBroadcastManager = STWApplication.app.localBroadCastManager

        m_broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Constants.BROADCAST_SYNC_START) {
                    setProgressBarIndeterminateVisibility(true)
                } else if (intent.action == Constants.BROADCAST_SYNC_DONE) {
                    setProgressBarIndeterminateVisibility(false)
                }
            }
        }
        localBroadcastManager!!.registerReceiver(m_broadcastReceiver, intentFilter)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        setContentView(R.layout.add_task)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)

        val fab = findViewById(R.id.fab) as FloatingActionButton?
        fab?.setOnClickListener {
            saveTasksAndClose()
        }

        setHint()

        if (share_text != null) {
            textInputField.setText(share_text)
        }

        setTitle(R.string.addtask)

//        val preFillString = if (intent.hasExtra(ActiveFilter.INTENT_JSON)) {
//            val filter  = ActiveFilter()
//            filter.initFromIntent(intent)
//            filter.prefill
//        } else {
//            ""
//        }
//        textInputField.setText(preFillString)

        setWordWrap(Config.isWordWrap)

        val textIndex = 0
        textInputField.setSelection(textIndex)

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

    }

    fun setWordWrap(bool: Boolean) {
        textInputField.setHorizontallyScrolling(!bool)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        val inflater = menuInflater
        inflater.inflate(R.menu.add_task, menu)

        // Set checkboxes
        val menuWordWrap = menu.findItem(R.id.menu_word_wrap)
        menuWordWrap.isChecked = Config.isWordWrap

        val menuPreFill = menu.findItem(R.id.menu_prefill_next)
        menuPreFill.isChecked = Config.isAddTagsCloneTags

        val menuShowHint = menu.findItem(R.id.menu_show_edittext_hint)
        menuShowHint.isChecked = Config.isShowEditTextHint

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
        // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                finish()
            }
            R.id.menu_prefill_next -> {
                Config.isAddTagsCloneTags = !Config.isAddTagsCloneTags
                item.isChecked = !item.isChecked
            }
            R.id.menu_word_wrap -> {
                val newVal = !Config.isWordWrap
                Config.isWordWrap = newVal
                setWordWrap(newVal)
                item.isChecked = !item.isChecked
            }
            R.id.menu_show_edittext_hint -> {
                Config.isShowEditTextHint = !Config.isShowEditTextHint
                setHint()
                item.isChecked = !item.isChecked
            }
            R.id.menu_help -> {
                showHelp()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun showHelp() {
        val i = Intent(this, HelpScreen::class.java)
        i.putExtra(Constants.EXTRA_HELP_PAGE, getText(R.string.help_add_task))
        startActivity(i)
    }

    private fun setHint() {
        if (Config.isShowEditTextHint) {
            textInputField.setHint(R.string.tasktexthint)
        } else {
            textInputField.hint = null
        }
    }

    private fun saveTasksAndClose() {

        val input: String = textInputField.text.toString()

        // Don't add empty tasks
        if (input.trim { it <= ' ' }.isEmpty()) {
            Log.i(TAG, "Not adding empty line")
            finish()
            return
        }

        // Update the TaskList with changes
        // Create new tasks
        val enteredTasks = getLines().filter { it.isNotBlank()}
        TaskList.add(enteredTasks)
        finish()
    }


    override fun onBackPressed() {
        saveTasksAndClose()
        super.onBackPressed()
    }

    private fun getLines() : List<String> {
        val input = textInputField.text.toString()
        return input.split("\r\n|\r|\n".toRegex())
    }

    public override fun onPause() {

        super.onPause()
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (localBroadcastManager != null) {
            localBroadcastManager!!.unregisterReceiver(m_broadcastReceiver)
        }
    }

    companion object {
        private val TAG = "AddTask"
    }
}
