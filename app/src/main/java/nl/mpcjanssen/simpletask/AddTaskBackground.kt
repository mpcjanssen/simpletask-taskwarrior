/**
 * This file is part of Simpletask.

 * Copyright (c) 2009-2012 Todo.txt contributors (http://todotxt.com)
 * Copyright (c) 2013- Mark Janssen

 * LICENSE:

 * Todo.txt Touch is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.

 * Todo.txt Touch is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.

 * You should have received a copy of the GNU General Public License along with Todo.txt Touch.  If not, see
 * //www.gnu.org/licenses/>.

 * @author Mark Janssen
 * *
 * @license http://www.gnu.org/licenses/gpl.html
 * *
 * @copyright 2009-2012 Todo.txt contributors (http://todotxt.com)
 * *
 * @copyright 2013- Mark Janssen
 */
package nl.mpcjanssen.simpletask

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import nl.mpcjanssen.simpletask.task.TaskList
import nl.mpcjanssen.simpletask.util.Config
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import java.io.IOException

class AddTaskBackground : Activity() {
    val TAG = "AddTaskBackground"

    public override fun onCreate(instance: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(instance)

        val intent = intent
        val action = intent.action
        longToast(intent.dataString)

        val append_text = Config.shareAppendText
        if (intent.type.startsWith("text/")) {
            if (Intent.ACTION_SEND == action) {
                Log.d(TAG, "Share")
                var share_text = ""
                if (STWApplication.atLeastAPI(21) && intent.hasExtra(Intent.EXTRA_STREAM)) {
                    val uri = intent.extras.get(Intent.EXTRA_STREAM) as Uri?
                    try {
                        val `is` = contentResolver.openInputStream(uri)
                        share_text = `is`.reader().readText()
                        `is`.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                } else if (intent.hasExtra(Intent.EXTRA_TEXT)) {
                    val text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT)
                    if (text != null) {
                        share_text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT).toString()
                    }
                }
                addBackgroundTask(share_text, append_text)
            } else if ("com.google.android.gm.action.AUTO_SEND" == action) {
                // Called as note to self from google search/now
                noteToSelf(intent, append_text)

            } else if (Constants.INTENT_BACKGROUND_TASK == action) {
                Log.d(TAG, "Adding background task")
                if (intent.hasExtra(Constants.EXTRA_BACKGROUND_TASK)) {
                    addBackgroundTask(intent.getStringExtra(Constants.EXTRA_BACKGROUND_TASK), append_text)
                } else {
                    Log.w(TAG, "Task was not in extras")
                }

            }
        } else {
            val imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            imageUri?.let {
                Log.i(TAG, "Added link to content: $imageUri")
                addBackgroundTask(imageUri.toString(), append_text)
            }
        }
    }

    private fun noteToSelf(intent: Intent, append_text: String) {
        val task = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (intent.hasExtra(Intent.EXTRA_STREAM)) {
            Log.d(TAG, "Voice note added.")
        }
        addBackgroundTask(task, append_text)
    }

    private fun addBackgroundTask(sharedText: String, appendText: String) {
        val todoList = TaskList
        Log.d(TAG, "Adding background tasks to todolist {} " + todoList)

        val tasks = sharedText.split("\r\n|\r|\n".toRegex()).filter { it.isNotBlank()}.map { "$it$appendText" }

        todoList.add(tasks)
        toast(R.string.task_added)
        finish()
    }
}
