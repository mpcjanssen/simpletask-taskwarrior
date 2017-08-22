package nl.mpcjanssen.simpletask.util

import me.smichel.android.KPreferences.Preferences
import nl.mpcjanssen.simpletask.R
import nl.mpcjanssen.simpletask.STWApplication
import nl.mpcjanssen.simpletask.remote.TaskWarrior
import java.io.File
import java.io.IOException

object Config : Preferences(STWApplication.app) {
    val TAG = "Config"

    val projectTerm = getString(R.string.project_prompt)

    val tagTerm = getString(R.string.tag_prompt)

    var lastScrollPosition by IntPreference(R.string.ui_last_scroll_position, -1)

    var lastScrollOffset by IntPreference(R.string.ui_last_scroll_offset, -1)

    var isWordWrap by BooleanPreference(R.string.word_wrap_key, true)

    var isShowEditTextHint by BooleanPreference(R.string.show_edittext_hint, true)

    val backClearsFilter by BooleanPreference(R.string.back_clears_filter, false)

    val sortCaseSensitive by BooleanPreference(R.string.ui_sort_case_sensitive, true)

    fun hasDonated(): Boolean {
        try {
            STWApplication.app.packageManager.getInstallerPackageName("nl.mpcjanssen.simpletask.donate")
            return true
        } catch (e: IllegalArgumentException) {
            return false
        }
    }

    var isAddTagsCloneTags by BooleanPreference(R.string.clone_tags_key, false)

    // Takes an argument f, an expression that maps theme strings to IDs
    val activeTheme: Int
        get() {
            return when (activeThemeString) {
                "dark" -> R.style.AppTheme_NoActionBar
                "black" -> R.style.AppTheme_Black_NoActionBar
                else -> R.style.AppTheme_Light_NoActionBar
            }
        }

    val activeActionBarTheme: Int
        get() {
            return when (activeThemeString) {
                "dark" -> R.style.AppTheme_ActionBar
                "black" -> R.style.AppTheme_Black_ActionBar
                else -> R.style.AppTheme_Light_DarkActionBar
            }
        }

    val activePopupTheme: Int
        get() {
            return if (isDarkTheme) {
                R.style.AppTheme_ActionBar
            } else {
                R.style.AppTheme_Black_ActionBar
            }
        }

    val isDarkTheme: Boolean
        get() {
            return when (activeThemeString) {
                "dark", "black" -> true
                else -> false
            }
        }

    private val _widgetTheme by StringPreference(R.string.widget_theme_pref_key, "light_darkactionbar")
    val isDarkWidgetTheme: Boolean
        get() = _widgetTheme == "dark"

    val activeThemeString by StringPreference(R.string.theme_pref_key, "light_darkactionbar")


    val dateBarRelativeSize: Float
        get() {
            val dateBarSize by IntPreference(R.string.datebar_relative_size, 80)
            return dateBarSize / 100.0f
        }

    val tasklistTextSize: Float?
        get() {
            val customSize by BooleanPreference(R.string.custom_font_size, false)
            if (!customSize) {
                return 14.0f
            }
            val font_size by IntPreference(R.string.font_size, 14)
            return font_size.toFloat()
        }

    val showCompleteCheckbox by BooleanPreference(R.string.ui_complete_checkbox, true)

    val showConfirmationDialogs by BooleanPreference(R.string.ui_show_confirmation_dialogs, true)

    var rcFileName by StringOrNullPreference(R.string.taskrc_file_key)

    val rcFile: File
        get()  {
            rcFileName?.let {
                return File(it)
            }
            return TaskWarrior.createDefaultRc()
        }

    val hasKeepSelection by BooleanPreference(R.string.keep_selection, false)

    val shareAppendText by StringPreference(R.string.share_task_append_text, " +background")

    var latestChangelogShown by IntPreference(R.string.latest_changelog_shown, 0)

    val hasColorDueDates by BooleanPreference(R.string.color_due_date_key, true)

    var activeReport: String by StringPreference(R.string.active_report_name, "next")
    var quickProjectsFilter: Set<String>? by StringSetOrNullPreference(R.string.quick_projects_filter, null)
    var quickTagsFilter: Set<String>? by StringSetOrNullPreference(R.string.quick_tags_filter, null)


}