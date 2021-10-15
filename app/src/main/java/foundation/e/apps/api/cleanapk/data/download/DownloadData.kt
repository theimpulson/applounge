package foundation.e.apps.api.cleanapk.data.download

data class DownloadData(
    val apk_file_sha1: String,
    val eelo_download_link: String,
    val signature: String
)
