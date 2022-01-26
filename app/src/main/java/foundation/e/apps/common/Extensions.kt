package foundation.e.apps.common

fun Double.isAFullNumber(): Boolean {
    return this % 1 == 0.0
}
