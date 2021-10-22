package foundation.e.apps.categories

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import foundation.e.apps.api.fused.FusedAPIRepository
import foundation.e.apps.api.fused.data.CategoryApp
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val fusedAPIRepository: FusedAPIRepository
) : ViewModel() {

    val categoriesList: MutableLiveData<List<CategoryApp>> = MutableLiveData()

    fun getCategoriesList(listType: String) {
        viewModelScope.launch {
            categoriesList.postValue(fusedAPIRepository.getCategoriesList(listType))
        }
    }
}
