package foundation.e.apps.categories

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import foundation.e.apps.R
import foundation.e.apps.categories.model.CategoriesRVAdapter
import foundation.e.apps.databinding.FragmentGamesBinding

@AndroidEntryPoint
class GamesFragment : Fragment(R.layout.fragment_games) {

    private var _binding: FragmentGamesBinding? = null
    private val binding get() = _binding!!

    private val categoriesViewModel: CategoriesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGamesBinding.bind(view)

        val categoriesRVAdapter = CategoriesRVAdapter()
        val recyclerView = binding.recyclerView

        recyclerView.apply {
            adapter = categoriesRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            visibility = View.GONE
        }

        categoriesViewModel.getCategoriesList("games")
        categoriesViewModel.categoriesList.observe(viewLifecycleOwner, {
            categoriesRVAdapter.setData(it)
            binding.progressBar.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
