package com.app.mealplanner

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.mealplanner.model.Recipe
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    private lateinit var adapter: FavoritesRecipeAdapter
    private var allFavorites: MutableList<Recipe> = mutableListOf() // Store all recipes

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewFavorites)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = FavoritesRecipeAdapter(
            mutableListOf(),
            onSwipe = {}, // Handle swipe if needed
            onRemoveClick = { recipe -> removeFavorite(recipe) } // Handle remove click
        ) { recipe ->
            openRecipeDetail(recipe)
        }
        recyclerView.adapter = adapter

        val favorites = loadFavorites()
        allFavorites = favorites.toMutableList() // Save all recipes for filtering
        if (favorites.isNotEmpty()) {
            adapter.updateRecipes(favorites)
        } else {
            println("Keine Favoriten gefunden.")
        }

        val searchInput: EditText = view.findViewById<View>(R.id.topBar).findViewById(R.id.search_input)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterFavorites(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Add the listener for the Enter key
        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH || actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                filterFavorites(searchInput.text.toString()) // Apply the filter
                true // Event handled
            } else {
                false // Event not handled
            }
        }
    }

    private fun filterFavorites(query: String) {
        val filteredFavorites = if (query.isEmpty()) {
            allFavorites // Show all favorites if the search field is empty
        } else {
            allFavorites.filter { recipe ->
                recipe.name.contains(query, ignoreCase = true) // Filter by name
            }
        }
        adapter.updateRecipes(filteredFavorites.toMutableList()) // Update RecyclerView
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

    private fun openRecipeDetail(recipe: Recipe) {
        val fragment = RecipeDetailFragment.newInstance(recipe)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun loadFavorites(): MutableList<Recipe> {
        val favoritesFile = File(requireContext().filesDir, "favorites.json")
        if (!favoritesFile.exists()) {
            // Copy the file from assets to filesDir if it doesn't exist
            try {
                requireContext().assets.open("favorites.json").use { inputStream ->
                    favoritesFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            } catch (e: Exception) {
                Log.e("FavoritesFragment", "Error copying favorites.json: ${e.message}")
                return mutableListOf()
            }
        }

        return try {
            val json = favoritesFile.readText()
            val type = object : TypeToken<MutableList<Recipe>>() {}.type
            val favorites: MutableList<Recipe> = Gson().fromJson(json, type)

            // Log missing images instead of resetting the property
            favorites.forEach { recipe ->
                if (recipe.image != null) {
                    val imageFile = File(requireContext().filesDir, recipe.image)
                    if (!imageFile.exists()) {
                        Log.w("FavoritesFragment", "Image file missing for recipe: ${recipe.name}")
                    }
                }
            }

            favorites
        } catch (e: Exception) {
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