package foundation.e.apps.XAPK



object StringUtils {


    fun parseInt(num: String): Int? {
        return try {
            num.toInt()
        } catch (e: Exception) {
            null
        }
    }

}
