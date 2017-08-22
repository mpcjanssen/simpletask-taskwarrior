package nl.mpcjanssen.simpletask.sort

import nl.mpcjanssen.simpletask.task.Task
import java.util.*

class AlphabeticalComparator(caseSensitive: Boolean, val stringLambda: (Task)->String?) : Comparator<Task> {
    val stringComp = AlphabeticalStringComparator(caseSensitive)
    override fun compare(t1: Task?, t2: Task?): Int {
        if (t1 === t2) {
            return 0
        } else if (t1 == null) {
            return -1
        } else if (t2 == null) {
            return 1
        }
        return stringComp.compare(stringLambda(t1), stringLambda(t2))
    }
}
