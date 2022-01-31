package foundation.e.apps.manager.database.fused

import androidx.room.Entity
import androidx.room.PrimaryKey
import foundation.e.apps.api.fused.data.Origin
import foundation.e.apps.api.fused.data.Status

@Entity
data class FusedDownload(
    @PrimaryKey val id: String,
    val origin: Origin,
    var status: Status,
    val name: String,
    val package_name: String,
    val downloadLink: String,
    var downloadId: Long = 0,
    val orgStatus: Status = Status.UNAVAILABLE
)
