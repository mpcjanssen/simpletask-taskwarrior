/**
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).

 * Copyright (c) 2009-2012 Todo.txt contributors (http://todotxt.com)
 * Copyright (c) 2015 Vojtech Kral

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
 * *
 * @copyright 2015 Vojtech Kral
 */
package nl.mpcjanssen.simpletask

import android.content.*
import android.os.Bundle
import android.preference.*
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.MenuItem
import nl.mpcjanssen.simpletask.util.Config
import nl.mpcjanssen.simpletask.util.FontManager
import java.util.*

class Preferences : ThemedPreferenceActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    lateinit var prefs: SharedPreferences
    lateinit var app: STWApplication

    private lateinit var localBroadcastManager: LocalBroadcastManager
    private lateinit var m_broadcastReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        app = application as STWApplication
        localBroadcastManager = app.localBroadCastManager
        val intentFilter = IntentFilter()
        intentFilter.addAction(Constants.BROADCAST_THEME_CHANGED)

        m_broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, receivedIntent: Intent) {
                if (receivedIntent.action == Constants.BROADCAST_THEME_CHANGED) {
                    Log.i(TAG, "Reloading preference screen with fragment ${intent.getStringExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT)}")
                    recreate()
                }
            }
        }
        Log.i(TAG, "Registering broadcast receiver")
        localBroadcastManager.registerReceiver(m_broadcastReceiver, intentFilter)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            getString(R.string.theme_pref_key) -> {
                onContentChanged()
                val broadcastIntent = Intent(Constants.BROADCAST_THEME_CHANGED)
                intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, AppearancePrefFragment::class.java.name)
                localBroadcastManager.sendBroadcast(broadcastIntent)
            }
            getString(R.string.datebar_relative_size) -> {
                val broadcastIntent = Intent(Constants.BROADCAST_DATEBAR_SIZE_CHANGED)
                localBroadcastManager.sendBroadcast(broadcastIntent)
            }
            getString(R.string.custom_font_size),
            getString(R.string.font_size) -> {
                val broadcastIntent = Intent(Constants.BROADCAST_UPDATE_UI)
                localBroadcastManager.sendBroadcast(broadcastIntent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        prefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        localBroadcastManager.unregisterReceiver(m_broadcastReceiver)
    }

    override fun onBuildHeaders(target: MutableList<Header>) {
        val allHeaders = ArrayList<Header>()
        loadHeadersFromResource(R.xml.preference_headers, allHeaders)
        target.addAll(allHeaders)
    }

    override fun isValidFragment(fragmentName: String): Boolean {
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
        // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                finish()
                return true
            }
            else -> {
                return false
            }
        }
    }

    abstract class PrefFragment(val xmlId: Int) : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(xmlId)
        }

    }

    class AppearancePrefFragment : PrefFragment(R.xml.appearance_preferences), SharedPreferences.OnSharedPreferenceChangeListener {
        private val fonts = FontManager.enumerateFonts()
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            if (activity.getString(R.string.font_key) == key) {
                updateFontSummary()
            }
        }
        fun updateFontSummary() {
            val fontPref = findPreference(getString(R.string.font_key)) ?: return
            fontPref.valueInSummary(fonts[fontPref.sharedPreferences.getString(fontPref.key, "")])
            fontPref.setOnPreferenceChangeListener { preference, any ->
                preference.summary = "%s"
                preference.valueInSummary(fonts[any])
                true
            }

        }
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            updateFontSummary()
        }
    }

    class InterfacePrefFragment : PrefFragment(R.xml.interface_preferences) {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            val appendTextPref = findPreference(getString(R.string.share_task_append_text)) as EditTextPreference
            appendTextPref.valueInSummary()
            appendTextPref.setOnPreferenceChangeListener { preference, any ->
                preference.summary = getString(R.string.share_task_append_text_summary)
                preference.valueInSummary(any)
                true
            }
        }
    }

    class DonatePrefFragment : PrefFragment(R.xml.donate_preferences) {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val screen = preferenceScreen
            val toHide: Preference
            if (Config.hasDonated()) {
                toHide = screen.findPreference("donate")
            } else {
                toHide = screen.findPreference("donated")
            }
            screen.removePreference(toHide)
        }
    }

    companion object {
        internal val TAG = Preferences::class.java.simpleName
    }
}

// Helper to replace %s in all setting summaries not only ListPreferences
fun Preference.valueInSummary(any: Any? = null) {
    this.summary = this.summary.replaceFirst(Regex("%s"), any?.toString() ?: this.sharedPreferences.getString(this.key, ""))
    }
