package io.eelo.appinstaller.utils

import io.eelo.appinstaller.R
import java.io.IOException
import java.net.SocketTimeoutException

enum class Error(val description: Int) {
    NO_RESULTS(R.string.error_no_results),
    NO_INTERNET(R.string.error_no_internet),
    SERVER_UNAVAILABLE(R.string.error_server_unavailable),
    REQUEST_TIMEOUT(R.string.error_request_timeout),
    UNKNOWN(R.string.error_unknown),
    APK_UNAVAILABLE(R.string.error_apk_unavailable),
    APK_CORRUPT(R.string.error_apk_corrupt),
    INSTALL_FAILED(R.string.error_install_failed);

    companion object {
        fun findError(e: Exception): Error {
            return when (e::class) {
                CustomException::class -> (e as CustomException).error
                SocketTimeoutException::class -> return REQUEST_TIMEOUT
                IOException::class -> return SERVER_UNAVAILABLE
                else -> UNKNOWN
            }
        }
    }

    class CustomException(val error: Error) : Exception(error.name.toLowerCase())
}
