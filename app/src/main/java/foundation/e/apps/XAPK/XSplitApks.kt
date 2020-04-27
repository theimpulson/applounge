package foundation.e.apps.XAPK

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class XSplitApks(
    @Expose
    @SerializedName("file")
    var fileName: String,
    @Expose
    @SerializedName("id")
    var _id: String
)
