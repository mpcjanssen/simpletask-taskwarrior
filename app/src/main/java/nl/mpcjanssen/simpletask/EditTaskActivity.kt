package nl.mpcjanssen.simpletask

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import kotlinx.android.synthetic.main.edit_task.*
import kotlinx.android.synthetic.main.edit_task_row.view.*
import nl.mpcjanssen.simpletask.R.id.editText
import nl.mpcjanssen.simpletask.remote.TaskWarrior
import nl.mpcjanssen.simpletask.task.Task
import nl.mpcjanssen.simpletask.task.TaskList
import nl.mpcjanssen.simpletask.util.Config
import org.jetbrains.anko.*
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.text.InputType
import android.util.AttributeSet
import android.view.*


class EditTaskActivity : ThemedActionBarActivity(), AnkoLogger {

    lateinit var originalTask: Task
    val originalValues = HashMap<String,String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        originalTask = TaskWarrior.getTasks(intent.getStringExtra("uuid")).first()
        setContentView(R.layout.edit_task)
        addRow("uuid", originalTask.uuid, true)
        addRow("description", originalTask.description)
    }

    fun addRow(name: String, value: String, readOnly: Boolean = false) {
        val row = layoutInflater.inflate(R.layout.edit_task_row, null, false)
        row.rowName.text = name
        row.rowValue.setText(value)
        if (readOnly) {
            row.rowValue.inputType = InputType.TYPE_NULL
        }
        originalValues[name] = value
        edit_table.addView(row)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.edit_rc, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item == null) {
            return false
        }
        when (item.itemId) {
            R.id.menu_save_id -> {
                saveContents()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun saveContents() {
        val updatedFields = HashMap<String,String>()
        for (idx in 0 until edit_table.childCount) {
            val child = edit_table.getChildAt(idx)
            val name = child.rowName.text.toString()
            val newValue = child.rowValue.text.toString()
            if (originalValues[name] != newValue) {
                updatedFields[name] = newValue
            }
        }

        val uuid = originalTask.uuid
        if (updatedFields.isEmpty()) {
            info("No changes to task $uuid")
        } else {
            info("Updating $uuid fields ${updatedFields.keys}")
            TaskList.modify(listOf(uuid), updatedFields)
        }
        finish()
    }

}