package io.eelo.appinstaller.api

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.utils.Constants
import java.net.URL

class ListCategoriesRequest {

    companion object {
        private val reader = ObjectMapper().readerFor(ListCategoriesResult::class.java)
    }

    fun request(): ListCategoriesResult {
        return reader.readValue<ListCategoriesResult>(URL(Constants.BASE_URL + "apps?action=list_cat"))
    }

    class ListCategoriesResult @JsonCreator
    constructor(@JsonProperty("apps") val appsCategories: Array<String>,
                @JsonProperty("games") val gamesCategories: Array<String>)

}