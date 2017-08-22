package nl.mpcjanssen.simpletask.sort

import java.util.*
import java.text.Collator

class AlphabeticalStringComparator(caseSensitive: Boolean) : Comparator<String> {
    internal var bCaseSensitive = true

    init {
        this.bCaseSensitive = caseSensitive
    }

    override fun compare(t1: String?, t2: String?): Int {
        var a = t1
        var b = t2
        if (a == null) {
            a = ""
        }
        if (b == null) {
            b = ""
        }

        val collator = Collator.getInstance(Locale.getDefault())

        if (bCaseSensitive) {
            collator.strength = Collator.TERTIARY
        } else {
            collator.strength = Collator.SECONDARY
        }
        return collator.compare(a, b)
    }
}
