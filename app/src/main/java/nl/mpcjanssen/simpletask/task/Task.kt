package nl.mpcjanssen.simpletask.task


import hirondelle.date4j.DateTime
import org.json.JSONException
import org.json.JSONObject
import java.util.*


data class Task(
        val uuid: String,
        val description: String,
        val annotations: List<String>,
        val project: String?,
        val tags: List<String>,
        val urgency: Double,
        val status: String,
        val dueDate: DateTime?,
        val startDate: DateTime?,
        val waitDate: DateTime?,
        val endDate: DateTime?,
        val entryDate: DateTime) {

    val isCompleted = status == "completed"

    val isDeleted = status == "deleted"

    val displayText = description

    val asCliTxt: String
        get() {
            val resultBuilder = StringBuilder()
            resultBuilder.append(description.trim())
            resultBuilder.append(" entry:$entryDate")
            dueDate?.let {
                resultBuilder.append(" due:$it")
            }
            startDate?.let {
                resultBuilder.append(" start:$it")
            }
            waitDate?.let {
                resultBuilder.append(" wait:$it")
            }
            project?.let {
                resultBuilder.append(" proj:$project")
            }
            if (tags.isNotEmpty()) {
                val tagsString = tags.joinToString(" ") { "+$it" }
                resultBuilder.append(" $tagsString")
            }

            resultBuilder.append(" status:$status")
            return resultBuilder.toString()
        }

    val links: List<String>
        get() {
            val text = description + " " + annotations.joinToString(" ")
            return MATCH_URI.findAll(text).map { it.value }.toList()
        }

    fun matchesQuickFilter(filterProjects: Set<String>?, filterTags: Set<String>?): Boolean {
        val matchProjects = when {
            filterProjects == null -> true
            filterProjects.isEmpty() -> true
            project in filterProjects -> true
            filterProjects.contains("-") && project.isNullOrBlank() -> true
            else -> false
        }
        val matchTags = when {
            filterTags == null -> true
            filterTags.isEmpty() -> true
            tags.intersect(filterTags).isNotEmpty() -> true
            filterTags.contains("-") && tags.isEmpty() -> true
            else -> false
        }
        return matchProjects && matchTags
    }


    companion object {
        val MATCH_URI = Regex("[a-z]+://(\\S+)")
        fun fromJSON(jsonStr: String): Task {
            try {
                val json = JSONObject(jsonStr)
                val uuid = json.getString("uuid")
                val desc = json.getString("description")

                val tags = ArrayList<String>()
                json.optJSONArray("tags")?.let {
                    (0 until it.length()).mapTo(tags) { i -> it.getString(i) }
                }
                val annotations = ArrayList<String>()
                json.optJSONArray("annotations")?.let {
                    (0 until it.length())
                            .map { i -> it.getJSONObject(i) }
                            .mapTo(annotations) { it.getString("description") }
                }
                val project = json.optString("project", null)

                val status = json.getString("status")

                val endDate = json.getISO8601Date("end")
                val entryDate = json.getISO8601Date("entry") as DateTime // entry is always set, it's an error if not
                val waitDate = json.getISO8601Date("wait")
                val dueDate = json.getISO8601Date("due")
                val startDate = json.getISO8601Date("start")
                val urgency = json.getDouble("urgency")

                return Task(
                        uuid = uuid,
                        description = desc,
                        annotations = annotations,
                        project = project,
                        tags = tags,
                        urgency = urgency,
                        status = status,
                        dueDate = dueDate,
                        waitDate = waitDate,
                        endDate = endDate,
                        entryDate = entryDate,
                        startDate = startDate)
            } catch (e: JSONException) {
                error("failed to parse JSON" + e.message)
            }
        }
    }
}

fun List<Task>.asCliList(): String {
    return this.joinToString("\n") { it.asCliTxt }
}

fun JSONObject.getISO8601Date(name: String) : DateTime? {
    val iso8601dateFormatRegex = Regex("([0-9]{4})([0-9]{2})([0-9]{2})T([0-9]{2})([0-9]{2})([0-9]{2})Z")


    fun fromISO8601(dateStr: String): DateTime {
        val (year, month, day, hour, minutes, seconds) =
                iso8601dateFormatRegex.matchEntire(dateStr)?.destructured ?: return DateTime.now(TimeZone.getDefault())
        val UTCDate = DateTime("$year-$month-$day $hour:$minutes:$seconds")
        return UTCDate.changeTimeZone(TimeZone.getTimeZone("UTC"), TimeZone.getDefault())
    }
    return this.optString(name, null)?.let {
        fromISO8601(it)
    }
}



