/**
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).

 * Copyright (c) 2009-2012 Todo.txt contributors (http://todotxt.com)

 * LICENSE:

 * Todo.txt Touch is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.

 * Todo.txt Touch is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.

 * You should have received a copy of the GNU General Public License along with Todo.txt Touch.  If not, see
 * //www.gnu.org/licenses/>.

 * @author Todo.txt contributors @yahoogroups.com>
 * *
 * @license http://www.gnu.org/licenses/gpl.html
 * *
 * @copyright 2009-2012 Todo.txt contributors (http://todotxt.com)
 */
@file:JvmName("Util")

package nl.mpcjanssen.simpletask.util

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.AssetManager
import android.support.compat.BuildConfig
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.ListView
import hirondelle.date4j.DateTime
import nl.mpcjanssen.simpletask.*
import nl.mpcjanssen.simpletask.sort.AlphabeticalStringComparator
import nl.mpcjanssen.simpletask.task.Task
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import java.io.*
import java.util.*

val TAG = "Util"

val mdParser: Parser = Parser.builder().build()
val htmlRenderer : HtmlRenderer = HtmlRenderer.builder().build()


fun getString (resId : Int) : String {
    return STWApplication.app.getString(resId)
}


fun showConfirmationDialog(cxt: Context,
                           msgid: Int,
                           okListener: DialogInterface.OnClickListener,
                           title: CharSequence) {
    val builder = AlertDialog.Builder(cxt)
    builder.setTitle(title)
    showConfirmationDialog(msgid, okListener, builder)
}

private fun showConfirmationDialog(msgid: Int,
                           okListener: DialogInterface.OnClickListener,
                           builder: AlertDialog.Builder) {
    val show = Config.showConfirmationDialogs
    builder.setMessage(msgid)
    builder.setPositiveButton(android.R.string.ok, okListener)
    builder.setNegativeButton(android.R.string.cancel, null)
    builder.setCancelable(true)
    val dialog = builder.create()
    if (show) {
        dialog.show()
    } else {
        okListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE)
    }
}

interface InputDialogListener {
    fun onClick(input: String)
}

@Throws(TodoException::class)
fun createParentDirectory(dest: File?) {
    if (dest == null) {
        throw TodoException("createParentDirectory: dest is null")
    }
    val dir = dest.parentFile
    if (dir != null && !dir.exists()) {
        createParentDirectory(dir)
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, "Could not create dirs: " + dir.absolutePath)
                throw TodoException("Could not create dirs: " + dir.absolutePath)
            }
        }
    }
}

fun setColor(ss: SpannableString, color: Int) {
    ss.setSpan(ForegroundColorSpan(color), 0,
            ss.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
}

fun getCheckedItems(listView: ListView, checked: Boolean): ArrayList<String> {
    val checks = listView.checkedItemPositions
    val items = ArrayList<String>()
    for (i in 0 until checks.size()) {
        val item = listView.adapter.getItem(checks.keyAt(i)) as String
        if (checks.valueAt(i) && checked) {
            items.add(item)
        } else if (!checks.valueAt(i) && !checked) {
            items.add(item)
        }
    }
    return items
}

fun createDeferDialog(act: Activity, titleId: Int, listener: InputDialogListener): AlertDialog {
    val keys = act.resources.getStringArray(R.array.deferOptions)
    val today = "today"
    val tomorrow = "tomorrow"
    val oneWeek = "monday"
    val oneMonth = "som"
    val values = arrayOf("", today, tomorrow, oneWeek, oneMonth, "pick")

    val builder = AlertDialog.Builder(act)
    builder.setTitle(titleId)
    builder.setItems(keys) { _, whichButton ->
        val selected = values[whichButton]
        listener.onClick(selected)
    }
    return builder.create()
}

fun alfaSortList(items: List<String>, caseSensitive: Boolean, prefix: String?): ArrayList<String> {
    val result = ArrayList<String>()
    result.addAll(items)
    Collections.sort(result, AlphabeticalStringComparator(caseSensitive))
    if (prefix != null) {
        result.add(0, prefix)
    }
    return result
}

fun alfaSortList(items: Set<String>, caseSensitive: Boolean, prefix: String? = null): ArrayList<String> {
    val temp = ArrayList<String>()
    temp.addAll(items)
    return alfaSortList(temp, caseSensitive, prefix)
}

fun appVersion(ctx: Context): String {
    val packageInfo = ctx.packageManager.getPackageInfo(
            ctx.packageName, 0)
    return "SimpleTW " + " v" + packageInfo.versionName + " (" + BuildConfig.VERSION_CODE + ")"
}

fun showChangelogOverlay(act: Activity): Dialog? {
    val builder = AlertDialog.Builder(act)
    builder.setMessage(readAsset(act.assets, "changelog.en.md"))
    builder.setCancelable(true)
    builder.setPositiveButton("OK", null)
    val dialog = builder.create()
    dialog.show()
    return dialog
}

fun markdownAssetAsHtml(ctxt: Context, name: String): String {
    var markdown: String
    markdown = try {
        readAsset(ctxt.assets, name)
    } catch (e: IOException) {
        val fallbackAsset = name.replace("\\.[a-z]{2}\\.md$".toRegex(), ".en.md")
        Log.w(TAG, "Failed to load markdown asset: $name falling back to $fallbackAsset")
        try {
            readAsset(ctxt.assets, fallbackAsset)
        } catch (e: IOException) {
            "$name and fallback $fallbackAsset not found."
        }
    }
    // Change issue numbers to links
    markdown = markdown.replace("(\\s)(#)([0-9]+)".toRegex(), "$1[$2$3](https://github.com/mpcjanssen/simpletask-android/issues/$3)")
    val document = mdParser.parse(markdown)

    return "<html><head><link rel='stylesheet' type='text/css' href='css/light.css'></head><body>" + htmlRenderer.render(document) + "</body></html>"
}

@Throws(IOException::class)
fun readAsset(assets: AssetManager, name: String): String {
    val buf = StringBuilder()
    val input = assets.open(name)
    val `in` = BufferedReader(InputStreamReader(input))
    `in`.forEachLine {
        buf.append(it).append("\n")
    }

    `in`.close()
    return buf.toString()
}

fun getRelativeWaitDate(task: Task, app: STWApplication): String? {
    val date = task.waitDate ?: return null
    return getRelativeDate(app, "T: ", date).toString()
}

fun getRelativeDueDate(task: Task, app: STWApplication): SpannableString? {
    val date = task.dueDate ?: return null
    return getRelativeDate(app, "Due: ", date)
}

/**
 * This method returns a String representing the relative date by comparing
 * the Calendar being passed in to the date / time that it is right now. It
 * will compute both past and future relative dates. E.g., "one day ago" and
 * "one day from now".
 *

 *
 * **NOTE:** If the calendar date relative to "now" is older
 * than one day, we display the actual date in its default format as
 * specified by this class. If you don't want to
 * show the actual date, but you want to show the relative date for days,
 * months, and years, you can add the other cases in by copying the logic
 * for hours, minutes, seconds.

 * @param date date to calculate difference to
 * *
 * @return String representing the relative date
 */

private fun getRelativeDate(app: STWApplication, prefix: String, date: DateTime): SpannableString? {
    val now = DateTime.today(TimeZone.getTimeZone("UTC"))
    val days = date.numDaysFrom(now)
    val months = days / 31
    val weeks = days / 7
    val years = days / 365
    val s = when {
        years == 1 -> app.getString(R.string.dates_one_year_ago)
        years > 1 -> app.getString(R.string.dates_years_ago, years)
        months == 1 -> app.getString(R.string.dates_one_month_ago)
        months > 1 -> app.getString(R.string.dates_months_ago, months)
        weeks == 1 -> app.getString(R.string.dates_one_week_ago)
        weeks > 1 -> app.getString(R.string.dates_weeks_ago, weeks)
        days == 1 -> app.getString(R.string.dates_one_day_ago)
        days > 1 -> app.getString(R.string.dates_days_ago, days)
        days == 0 -> app.getString(R.string.dates_today)
        days == -1 -> app.getString(R.string.dates_tomorrow)
        else -> date.format("YYYY-MM-DD")
    }

    val ss = SpannableString(prefix + s)

    if (Config.hasColorDueDates && prefix == "Due: ") {
        val dueTodayColor = ContextCompat.getColor(app, R.color.simple_green_light)
        val overDueColor = ContextCompat.getColor(app, R.color.simple_red_light)
        val dueTomorrowColor = ContextCompat.getColor(app, R.color.simple_blue_light)
        when {
            days == 0 -> setColor(ss, dueTodayColor)
            date.lteq(now) -> setColor(ss, overDueColor)
            days == -1 -> setColor(ss, dueTomorrowColor)
        }
    }

    return ss
}

fun getRelativeAge(task: Task, app: STWApplication): String? {
    return getRelativeDate(app, "", task.entryDate).toString()
}

fun ArrayList<HashSet<String>>.union(): Set<String> {
    return fold(HashSet()) {
        left, right ->
        left.addAll(right)
        left
    }
}

fun ArrayList<HashSet<String>>.intersection(): Set<String> {
    val intersection = this.firstOrNull()?.toHashSet() ?: return emptySet()
    for (i in 1..this.lastIndex) {
        intersection.retainAll(this[i])
        if (intersection.isEmpty()) {
            break
        }
    }
    return intersection
}

fun broadcastRefreshUI(broadcastManager: LocalBroadcastManager) {
    broadcastManager.sendBroadcast(Intent(Constants.BROADCAST_UPDATE_UI))
}

fun broadcastRefreshSelection(broadcastManager: LocalBroadcastManager) {
    broadcastManager.sendBroadcast(Intent(Constants.BROADCAST_HIGHLIGHT_SELECTION))
}

