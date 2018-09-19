package io.eelo.appinstaller.search

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException
import java.net.URL

class SearchEngine(private val serverPath: String, private val keyword: String, private val resultsPerPage: Int) {
    private val jsonReader = ObjectMapper().readerFor(SearchResult::class.java)

    @Throws(IOException::class)
    fun search(page: Int): SearchResult {
        val url = URL(serverPath + "apps?action=search&keyword=" + keyword + "&page=" + page + "&nres=" + resultsPerPage)
        return jsonReader.readValue(url.openStream())
    }

}
