package foundation.e.apps.utils

object Constants {

    // Global
    const val BASE_URL = "https://api.cleanapk.org/"
    const val DOWNLOAD_URL = "https://apk.cleanapk.org/"
    const val STORAGE_PERMISSION_REQUEST_CODE = 0
    const val CONNECT_TIMEOUT = 5000 // 5 seconds
    const val READ_TIMEOUT = 10000 // 10 seconds
    const val REQUEST_METHOD = "GET"

    // Search
    const val RESULTS_PER_PAGE = 20
    const val SUGGESTION_KEY = "suggestion"
    const val SUGGESTIONS_RESULTS = 5

    // Application
    const val WEB_STORE_URL = "https://cleanapk.org/#/app/"
    const val APPLICATION_PACKAGE_NAME_KEY = "application_package_name"
    const val APPLICATION_DESCRIPTION_KEY = "application_description"
    const val SELECTED_APPLICATION_SCREENSHOT_KEY = "selected_application_screenshot"

    // Categories
    const val CATEGORY_KEY = "category_key"

    // Home
    const val CURRENTLY_SELECTED_FRAGMENT_KEY = "currently_selected_fragment"

    // Updates
    const val OUTDATED_APPLICATIONS_FILENAME = "outdated_applications.txt"
    const val UPDATES_WORK_NAME = "updates_work"
    const val UPDATES_NOTIFICATION_ID = 76
    const val UPDATES_NOTIFICATION_CHANNEL_ID = "updates_notification"
    const val UPDATES_NOTIFICATION_CHANNEL_TITLE = "App updates"
    const val AUTOMATICALLY_INSTALL_UPDATES = "will"
    const val MANUALLY_INSTALL_UPDATES = "will not"
    const val UPDATES_NOTIFICATION_CLICK_EXTRA = "updates_notification_click_extra"
}
