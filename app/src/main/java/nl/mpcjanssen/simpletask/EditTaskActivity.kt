package nl.mpcjanssen.simpletask

import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import nl.mpcjanssen.simpletask.R.id.editText
import nl.mpcjanssen.simpletask.remote.TaskWarrior
import nl.mpcjanssen.simpletask.task.Task
import nl.mpcjanssen.simpletask.task.TaskList
import nl.mpcjanssen.simpletask.util.Config
import org.jetbrains.anko.*

class EditTaskActivity : ThemedActionBarActivity(), AnkoLogger {
    lateinit var descriptionView: EditText
    lateinit var uuidView: TextView
    lateinit var originalTask: Task
    val uuid
        get() = uuidView.text.toString()
    val description
        get() = descriptionView.text.toString()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        originalTask = TaskWarrior.getTasks(intent.getStringExtra("uuid")).first()

        verticalLayout {
            uuidView = textView {
                hint = "Description"
                gravity = Gravity.TOP or Gravity.START
                maxLines = 1
                Config.tasklistTextSize?.let { textSize = it }
            }.lparams(height = wrapContent, width = matchParent)
            descriptionView = editText {
                hint = "Description"
                gravity = Gravity.TOP or Gravity.START
                Config.tasklistTextSize?.let { textSize = it }
            }.lparams(height = wrapContent, width = wrapContent)
        }
        uuidView.text = originalTask.uuid
        descriptionView.setText(originalTask.description)
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
        if (originalTask.description!=description) {
            updatedFields["description"] = description
        }
        if (updatedFields.isEmpty()) {
            info("No changes to task $uuid")
        } else {
            info("Updating $uuid fields ${updatedFields.keys}")
            TaskList.modify(listOf(uuid), updatedFields)
        }
        finish()
    }

}