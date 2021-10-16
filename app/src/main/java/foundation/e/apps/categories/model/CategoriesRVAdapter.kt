package foundation.e.apps.categories.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import foundation.e.apps.databinding.CategoriesListItemBinding
import javax.inject.Singleton

@Singleton
class CategoriesRVAdapter : RecyclerView.Adapter<CategoriesRVAdapter.ViewHolder>() {

    private var oldList = emptyList<String>()

    inner class ViewHolder(val binding: CategoriesListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CategoriesListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    }

    override fun getItemCount(): Int {
        return oldList.size
    }
}