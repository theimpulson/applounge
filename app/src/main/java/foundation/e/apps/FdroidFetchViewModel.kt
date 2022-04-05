package foundation.e.apps

import android.widget.TextView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import foundation.e.apps.api.fdroid.FdroidRepository
import foundation.e.apps.api.fdroid.models.FdroidEntity
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.utils.enums.Origin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 *
 */
@HiltViewModel
class FdroidFetchViewModel @Inject constructor(
    private val fdroidRepository: FdroidRepository
): ViewModel() {

    private val fdroidEntries = mutableMapOf<String, FdroidEntity?>()

    fun setAuthorNameIfNeeded(textView: TextView, fusedApp: FusedApp) {
        viewModelScope.launch {
            var authorNameToDisplay = textView.text
            withContext(Dispatchers.Default) {
                fusedApp.run {
                    try {
                        if (author == "unknown" && origin == Origin.CLEANAPK) {

                            withContext(Dispatchers.Main) {
                                textView.text = FdroidEntity.DEFAULT_FDROID_AUTHOR_NAME
                            }

                            var result = fdroidEntries[package_name]
                            if (result == null) {
                                result = fdroidRepository.getFdroidInfo(package_name)?.also {
                                    fdroidEntries[package_name] = it
                                }
                            }
                            result?.authorName?.let {
                                authorNameToDisplay = it
                            }
                        }
                    }
                    catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            withContext(Dispatchers.Main) {
                textView.text = authorNameToDisplay
            }
        }
    }

}