package nl.mpcjanssen.simpletask.sort

import nl.mpcjanssen.simpletask.task.Task
import java.util.*

class UrgencyComparator : Comparator<Task> {

    override fun compare(a: Task?, b: Task?): Int {
        if (a === b) {
            return 0
        } else if (a == null) {
            return -1
        } else if (b == null) {
            return 1
        }
        val urgA = a.urgency
        val urgB = b.urgency
        return urgA.compareTo(urgB)
    }
}
