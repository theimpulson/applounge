package io.eelo.appinstaller.utils

object Constants {

    // Global
    const val BASE_URL = "https://api.cleanapk.org/"
    const val DOWNLOAD_URL = "https://apk.cleanapk.org/"
    const val STORAGE_PERMISSION_REQUEST_CODE = 0

    // Search
    const val RESULTS_PER_PAGE = 20

    // Application
    const val APK_FOLDER = "/AppInstaller/"
    const val APPLICATION_PACKAGE_NAME_KEY = "application_package_name"
    const val APPLICATION_DESCRIPTION_KEY = "application_description"
    const val ERROR_APP_NO_INTERNET = 40
    const val ERROR_APP_SERVER_UNAVAILABLE = 41
    const val ERROR_APP_REQUEST_TIMEOUT = 42
    const val ERROR_APP_UNKNOWN = 43
    const val ERROR_APP_INSTALL_FAILED = 44

    // Categories
    const val CATEGORY_KEY = "category_key"

    // Home
    const val CURRENTLY_SELECTED_FRAGMENT_KEY = "currently_selected_fragment"

    // Errors
    const val ERROR_NO_INTERNET = 30
    const val ERROR_SERVER_UNAVAILABLE = 31
    const val ERROR_REQUEST_TIMEOUT = 32
    const val ERROR_UNKNOWN = 33
}
