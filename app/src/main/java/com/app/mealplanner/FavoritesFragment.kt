package com.app.mealplanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.mealplanner.databinding.FragmentFavoritesBinding

class FavoritesFragment : Fragment(), FavoritesRepository.Observer {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private lateinit var recipeAdapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        setupRecyclerView()
        FavoritesRepository.getInstance().addObserver(this)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        FavoritesRepository.getInstance().removeObserver(this)
        _binding = null
    }

    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter(FavoritesRepository.getInstance().getFavoriteRecipes().toMutableList())
        binding.recyclerViewFavorites.adapter = recipeAdapter
        binding.recyclerViewFavorites.layoutManager = LinearLayoutManager(context)
    }

    override fun onFavoritesChanged() {
        recipeAdapter.notifyDataSetChanged()
    }
}