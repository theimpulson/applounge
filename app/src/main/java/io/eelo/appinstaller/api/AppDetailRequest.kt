package io.eelo.appinstaller.api

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.application.model.data.FullData
import io.eelo.appinstaller.utils.Common
import io.eelo.appinstaller.utils.Constants
import io.eelo.appinstaller.utils.Error

class AppDetailRequest(private val id: String) {

    companion object {
        private val reader = ObjectMapper().readerFor(Result::class.java)
    }

    fun request(callback: (Error?, FullData?) -> Unit) {
        try {
            val url = Constants.BASE_URL + "apps?action=app_detail&id=$id"
            val urlConnection = Common.createConnection(url)
            val result = reader.readValue<Result>(urlConnection.inputStream)
            urlConnection.disconnect()
            callback.invoke(null, result.app)
        } catch (e: Exception) {
            callback.invoke(Error.findError(e), null)
        }
    }

    class Result @JsonCreator
    constructor(@JsonProperty("app") val app: FullData,
                @JsonProperty("success") private val success: Boolean)
}