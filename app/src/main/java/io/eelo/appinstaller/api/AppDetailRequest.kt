package io.eelo.appinstaller.api

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.application.model.data.FullData
import io.eelo.appinstaller.utils.Constants
import java.net.URL

class AppDetailRequest(private val id: String) {

    companion object {
        private val reader = ObjectMapper().readerFor(Result::class.java)
    }

    fun request(): FullData {
        return reader.readValue<Result>(URL(Constants.BASE_URL + "apps?action=app_detail&id=$id")).app
    }

    class Result @JsonCreator
    constructor(@JsonProperty("app") val app: FullData,
                @JsonProperty("success") private val success: Boolean)
}