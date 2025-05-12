package com.app.mealplanner

import AddRecipeDialogFragment
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.mealplanner.model.Recipe
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File


class RecipesFragment : Fragment(R.layout.fragment_recipes) {

    private lateinit var adapter: RecipeAdapter
    private val swipedRecipes = mutableListOf<Int>() // Session-based list

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        copyRecipesToExternalStorage()
        mergeRecipesFromAssetsAndInternalStorage()

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewRecipes)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = RecipeAdapter(mutableListOf<Recipe>()) { recipeId ->
            val recipe = loadRecipes().find { it.id.toString() == recipeId }
            if (recipe != null) {
                onRecipeSwiped(recipe)
            }
        }
        recyclerView.adapter = adapter

        val recipes = loadRecipes()
        val filteredRecipes = filterRecipes(recipes)
        adapter.updateRecipes(filteredRecipes)

        // Floating Action Button
        val fabAddRecipe: View = view.findViewById(R.id.fabAddRecipe)
        fabAddRecipe.setOnClickListener {
            showAddRecipeDialog()
        }

        // Add swipe functionality
        val itemTouchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false // No move functionality needed
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val recipe = adapter.getRecipes()[position]
                onRecipeSwiped(recipe) // Handle swipe action
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun showAddRecipeDialog() {
        val dialog = AddRecipeDialogFragment()
        dialog.setOnRecipeAddedListener(object : AddRecipeDialogFragment.OnRecipeAddedListener {
            override fun onRecipeAdded(recipe: Recipe) {
                val recipes = loadRecipes()
                val filteredRecipes = filterRecipes(recipes)
                adapter.updateRecipes(filteredRecipes) // RecyclerView aktualisieren
            }
        })
        dialog.show(parentFragmentManager, "AddRecipeDialog")
    }

    private fun loadRecipes(): MutableList<Recipe> {
        val recipesFile = File(requireContext().filesDir, "recipes.json")
        return if (recipesFile.exists()) {
            try {
                val json = recipesFile.readText()
                Log.d("RecipesFragment", "JSON Content: $json") // Log the JSON content

                val type = object : TypeToken<MutableList<Recipe>>() {}.type
                val recipes: MutableList<Recipe> = Gson().fromJson(json, type)

// Log each parsed recipe
                recipes.forEach { recipe ->
                    Log.d("RecipesFragment", "Parsed Recipe: $recipe")
                }

                recipes
            } catch (e: Exception) {
                e.printStackTrace()
                mutableListOf()
            }
        } else {
            mutableListOf()
        }
    }

    private fun loadFavorites(): MutableList<Recipe> {
        val favoritesFile = File(requireContext().filesDir, "favorites.json")
        return if (favoritesFile.exists()) {
            val json = favoritesFile.readText()
            val type = object : TypeToken<MutableList<Recipe>>() {}.type
            Gson().fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    private fun filterRecipes(recipes: MutableList<Recipe>): MutableList<Recipe> {
        val favoriteIds = loadFavorites().map { it.id }
        return recipes.filter { it.id !in swipedRecipes && it.id !in favoriteIds }.toMutableList()
    }

    private fun onRecipeSwiped(recipe: Recipe) {
        swipedRecipes.add(recipe.id) // Hide recipe for the session
        val updatedRecipes = filterRecipes(loadRecipes())
        adapter.updateRecipes(updatedRecipes)

        // Add the swiped recipe to favorites
        val favorites = loadFavorites()
        if (!favorites.any { it.id == recipe.id }) {
            favorites.add(recipe)
            saveFavorites(favorites)
        }
    }

    private fun saveFavorites(favorites: MutableList<Recipe>) {
        val favoritesFile = File(requireContext().filesDir, "favorites.json")
        val json = Gson().toJson(favorites)
        favoritesFile.writeText(json)
    }

    fun addFavorite(recipe: Recipe) {
        val favorites = loadFavorites()
        if (!favorites.any { it.id == recipe.id }) {
            favorites.add(recipe)
            saveFavorites(favorites) // Save updated favorites
        }
    }

    fun removeFavorite(recipe: Recipe) {
        val favorites = loadFavorites()
        if (favorites.any { it.id == recipe.id }) {
            favorites.removeIf { it.id == recipe.id }
            saveFavorites(favorites) // Save updated favorites
        }
    }

    private fun mergeRecipesFromAssetsAndInternalStorage() {
        val recipesFile = File(requireContext().filesDir, "recipes.json")

        // Load recipes from assets
        val assetRecipes: MutableList<Recipe> = try {
            val json = requireContext().assets.open("recipes.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<MutableList<Recipe>>() {}.type
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            mutableListOf()
        }

        // Check if the internal file exists
        if (!recipesFile.exists()) {
            // If the internal file doesn't exist, save the asset recipes to internal storage
            recipesFile.writeText(Gson().toJson(assetRecipes))
            Log.d("RecipesFragment", "Internal recipes.json created from assets.")
            return
        }

        // Load recipes from internal storage
        val internalRecipes: MutableList<Recipe> = try {
            val json = recipesFile.readText()
            val type = object : TypeToken<MutableList<Recipe>>() {}.type
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            mutableListOf()
        }

        // Merge recipes, avoiding duplicates by `id`
        val mergedRecipes = (internalRecipes + assetRecipes).distinctBy { it.id }.toMutableList()

        // Save merged recipes back to internal storage
        recipesFile.writeText(Gson().toJson(mergedRecipes))
        Log.d("RecipesFragment", "Internal recipes.json updated with merged recipes.")
    }

    private fun copyRecipesToExternalStorage() {
        val internalFile = File(requireContext().filesDir, "recipes.json")
        val externalFile = File(requireContext().getExternalFilesDir(null), "recipes.json")
        if (internalFile.exists()) {
            internalFile.copyTo(externalFile, overwrite = true)
            Log.d(
                "RecipesFragment",
                "recipes.json copied to external storage: ${externalFile.absolutePath}"
            )
        } else {
            Log.d("RecipesFragment", "recipes.json file does not exist in internal storage.")
        }
    }
}