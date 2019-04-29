package foundation.e.apps.api

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Error
import foundation.e.apps.utils.Constants
import java.lang.Exception

class ListCategoriesRequest {

    companion object {
        private val reader = ObjectMapper().readerFor(ListCategoriesResult::class.java)
    }

    fun request(callback: (Error?, ListCategoriesResult?) -> Unit) {
        try {
            val url = Constants.BASE_URL + "apps?action=list_cat"
            val urlConnection = Common.createConnection(url)
            val result = reader.readValue<ListCategoriesResult>(urlConnection.inputStream)
            urlConnection.disconnect()
            callback.invoke(null, result)
        } catch (e: Exception) {
            callback.invoke(Error.findError(e), null)
        }
    }

    class ListCategoriesResult @JsonCreator
    constructor(@JsonProperty("success") success: Boolean,
                @JsonProperty("apps") val appsCategories: Array<String>,
                @JsonProperty("games") val gamesCategories: Array<String>)

}