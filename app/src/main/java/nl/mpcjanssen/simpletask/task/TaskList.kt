/**
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).
 *
 *
 * Copyright (c) 2009-2012 Todo.txt contributors (http://todotxt.com)
 *
 *
 * LICENSE:
 *
 *
 * Todo.txt Touch is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 *
 *
 * Todo.txt Touch is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 *
 * You should have received a copy of the GNU General Public License along with Todo.txt Touch.  If not, see
 * //www.gnu.org/licenses/>.

 * @author Todo.txt contributors @yahoogroups.com>
 * *
 * @license http://www.gnu.org/licenses/gpl.html
 * *
 * @copyright 2009-2012 Todo.txt contributors (http://todotxt.com)
 */
package nl.mpcjanssen.simpletask.task

import android.content.Intent
import android.util.Log
import nl.mpcjanssen.simpletask.*

import nl.mpcjanssen.simpletask.remote.TaskWarrior
import nl.mpcjanssen.simpletask.util.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.collections.ArrayList

/**
 * Implementation of the in memory representation of the Task list
 * uses an ActionQueue to ensure modifications and access of the underlying task list are
 * sequential.

 * @author Mark Janssen
 */




object TaskList : AnkoLogger {

    val tags = ArrayList<String>()
    val projects = ArrayList<String>()
    private val todoItems = ArrayList<Task>()
    private val selectedUUIDs = CopyOnWriteArraySet<String>()
    internal val TAG = TaskList::class.java.simpleName

    fun queue(description: String, body: () -> Unit) {
        val r = Runnable(body)
        ActionQueue.add(description, r)
    }

    fun add(items: List<Task>, atEnd: Boolean) {
        queue("Add task ${items.size} atEnd: $atEnd") {
            if (atEnd) {
                todoItems.addAll(items)
            } else {
                todoItems.addAll(0, items)
            }
        }
    }

    fun add(t: Task, atEnd: Boolean) {
        add(listOf(t), atEnd)
    }

    fun removeAll(tasks: List<Task>) {
        queue("Remove") {
            TaskWarrior.callTaskForSelection(tasks, "delete")
            val deletedUUIDs = tasks.map(Task::uuid)
            selectedUUIDs.removeAll(deletedUUIDs)
            reload()
        }
    }

    fun size(): Int {
        return todoItems.size
    }

    fun uncomplete(tasks: List<Task>) {
        queue("Uncomplete") {
            Log.d(TAG,"Uncompleting tasks")
            TaskWarrior.callTaskForSelection(tasks, "modify", "status:pending")
            reload()
        }
    }

    fun complete(tasks: List<Task>) {
        queue("Complete") {
            TaskWarrior.callTaskForSelection(tasks, "done")
            reload()
        }

    }

    fun defer(deferString: String, tasks: List<Task>, dateType: DateType) {
        queue("Defer") {
            val prefix = when (dateType) {
                DateType.THRESHOLD -> "wait"
                else -> "due"
            }
            TaskWarrior.callTaskForSelection(tasks, "modify", "$prefix:$deferString")
            reload()
        }
    }

    var selection: List<Task> = ArrayList()
        get() {
            return todoItems.filter {
                it.uuid in selectedUUIDs
            }
        }

    fun applyQuickFilter(projectsFilter: Set<String>?, tagsFilter: Set<String>?): Sequence<Task> {
        info("Applyinq quickfilter $projectsFilter, $tagsFilter")
        return todoItems.asSequence().filter {
            it.matchesQuickFilter(projectsFilter, tagsFilter)
        }

    }

    fun sync() {
        queue("Sync") {
            STWApplication.app.localBroadCastManager.sendBroadcast(Intent(Constants.BROADCAST_SYNC_START))
            TaskWarrior.callTask("sync")
            reload()
        }
    }
    fun reload(reason: String = "", reloadConfig: Boolean = false) {
        val logText = "Reload: " + reason
        queue(logText) {

            STWApplication.app.localBroadCastManager.sendBroadcast(Intent(Constants.BROADCAST_SYNC_START))
            if (!Config.hasKeepSelection) {
                TaskList.clearSelection()
            }
            todoItems.clear()
            todoItems.addAll(TaskWarrior.taskList(Config.activeReport, reloadConfig))
            projects.clear()
            projects.addAll(TaskWarrior.allProjects())
            tags.clear()
            tags.addAll(TaskWarrior.allTags())
            broadcastRefreshUI(STWApplication.app.localBroadCastManager)
        }
    }

    fun isSelected(item: Task): Boolean {
        return item.uuid in selectedUUIDs
    }

    fun numSelected(): Int {
        return selectedUUIDs.size
    }

    fun selectTasks(items: List<Task>) {
        queue("Select") {
            val uuids = items.map(Task::uuid)
            selectedUUIDs.addAll(uuids)
            broadcastRefreshSelection(STWApplication.app.localBroadCastManager)
        }
    }

    fun selectTask(item: Task?) {
        item?.let {
            selectTasks(listOf(item))
        }
    }

    fun deselectTask(item: Task) {
        deselectTasks(listOf(item))
    }

    private fun deselectTasks(items: List<Task>) {
        queue("Deselect") {
            val uuids = items.map(Task::uuid)
            selectedUUIDs.removeAll(uuids)
            broadcastRefreshSelection(STWApplication.app.localBroadCastManager)
        }
    }


    fun clearSelection() {
        queue("Clear selection") {
            selectedUUIDs.clear()
            broadcastRefreshSelection(STWApplication.app.localBroadCastManager)
        }
    }

    fun add(tasks: List<String>) {
        queue( "Adding ${tasks.size} tasks" ) {
            tasks.forEach {
                TaskWarrior.callTask("add", *it.split(" ").toTypedArray())
            }
            reload()
        }
    }

    fun updateTags(tasks: List<Task>, tagsToAdd: ArrayList<String>, tagsToRemove: ArrayList<String>) {
        queue("Update tags") {
            val args = ArrayList<String>()
            args.add("modify")
            tagsToAdd.forEach {
                args.add("+$it")
            }
            tagsToRemove.forEach {
                args.add("-$it")
            }
            TaskWarrior.callTaskForSelection(tasks, *args.toTypedArray())
            reload()


        }
    }
    fun updateProject(tasks: List<Task>, project: String) {
        queue("Update tags") {
            val args = ArrayList<String>()
            args.add("modify")
            args.add("project:$project")
            TaskWarrior.callTaskForSelection(tasks, *args.toTypedArray())
            reload()
        }
    }
}

