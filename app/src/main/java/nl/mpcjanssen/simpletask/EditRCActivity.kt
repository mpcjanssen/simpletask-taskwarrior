package nl.mpcjanssen.simpletask

import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.EditText
import android.widget.TextView
import nl.mpcjanssen.simpletask.task.TaskList
import nl.mpcjanssen.simpletask.util.Config
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick


class EditRCActivity : ThemedActionBarActivity() {
    lateinit var editText: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = EditRCActivityView()
        view.setContentView(this)
        val rcContents = Config.rcFile.readLines()
        editText = view.contents
        editText.setText(rcContents.joinToString("\n"))
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
        // ensure file has ending "\n"
        val newContents = editText.text.split("\n").joinToString (separator = "\n", postfix = "\n")
        Config.rcFile.writeText(newContents)
        TaskList.reload("updated rc file", true)
        finish()
    }

}

class EditRCActivityView : AnkoComponent<EditRCActivity> {
    lateinit var contents: EditText
    override fun createView(ui: AnkoContext<EditRCActivity>) = with(ui) {
        verticalLayout {
            contents = editText {
                hint = "Contents"
                gravity = Gravity.TOP or Gravity.START
                Config.tasklistTextSize?.let { textSize = it }
            }.lparams(height = matchParent, width = matchParent)
        }
    }
}

