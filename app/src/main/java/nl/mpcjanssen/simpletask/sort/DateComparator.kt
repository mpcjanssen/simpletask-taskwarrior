package nl.mpcjanssen.simpletask.sort

import hirondelle.date4j.DateTime
import nl.mpcjanssen.simpletask.task.Task

class DateComparator(val dateLambda: (Task) -> DateTime?) : Comparator<Task> {

    override fun compare(a: Task?, b: Task?): Int {
        if (a === b) {
            return 0
        } else if (a == null) {
            return -1
        } else if (b == null) {
            return 1
        }
        val aDate = dateLambda(a)
        val bDate = dateLambda(b)
        if ( aDate == null && bDate == null) {
            return 0
        } else if (aDate == null) {
            return 1
        } else if (bDate == null) {
            return -1
        }
        return aDate.compareTo(bDate)
    }
}
