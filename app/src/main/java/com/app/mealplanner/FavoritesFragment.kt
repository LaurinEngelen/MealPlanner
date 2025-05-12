package com.app.mealplanner

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.mealplanner.model.Recipe
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    private lateinit var adapter: FavoritesRecipeAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewFavorites)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = FavoritesRecipeAdapter(mutableListOf(), onSwipe = {}) { recipe ->
            removeFavorite(recipe)
        }
        recyclerView.adapter = adapter

        val favorites = loadFavorites()
        if (favorites.isNotEmpty()) {
            adapter.updateRecipes(favorites)
        } else {
            println("Keine Favoriten gefunden.")
        }
    }

    private fun removeFavorite(recipe: Recipe) {
        val favoritesFile = File(requireContext().filesDir, "favorites.json")
        if (favoritesFile.exists()) {
            val json = favoritesFile.readText()
            val type = object : TypeToken<MutableList<Recipe>>() {}.type
            val favorites: MutableList<Recipe> = Gson().fromJson(json, type)
            favorites.remove(recipe)
            favoritesFile.writeText(Gson().toJson(favorites))
            adapter.removeRecipe(recipe)
        }
    }

    private fun loadFavorites(): MutableList<Recipe> {
        return try {
            val json = requireContext().assets.open("favorites.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<MutableList<Recipe>>() {}.type
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            println("Error loading favorites: ${e.message}")
            mutableListOf()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // Enables the options menu
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_favorites, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_favorites -> {
                clearFavorites()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun clearFavorites() {
        val favoritesFile = File(requireContext().filesDir, "favorites.json")
        if (favoritesFile.exists()) {
            favoritesFile.delete() // Deletes the file
        }
        adapter.updateRecipes(mutableListOf()) // Updates the RecyclerView
        println("Favoritenliste wurde gelöscht.")
    }

    override fun onResume() {
        super.onResume()
        loadFavoritesAndUpdateUI()
    }

    private fun loadFavoritesAndUpdateUI() {
        val favoritesFile = File(requireContext().filesDir, "favorites.json")
        if (favoritesFile.exists()) {
            val json = favoritesFile.readText()
            val type = object : TypeToken<MutableList<Recipe>>() {}.type
            val favorites: MutableList<Recipe> = Gson().fromJson(json, type)
            adapter.updateRecipes(favorites) // Aktualisiert die RecyclerView
        }
    }
}