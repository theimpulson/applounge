package foundation.e.apps.XAPK

object FormatUtils {


    fun formatPercent(progress: Long, count: Long): Int {
        return if (count < progress) {
            0
        } else {
            (progress * 1f / count * 100f).toInt()
        }
    }

}
