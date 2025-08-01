package com.app.mealplanner

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

    companion object {
        private var sessionRecipes: MutableList<Recipe>? = null // Speichert die Reihenfolge der Rezepte während der Session
        private val swipedRecipes = mutableListOf<Int>() // Speichert dauerhaft während der App-Sitzung
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        copyRecipesToExternalStorage()
        mergeRecipesFromAssetsAndInternalStorage()

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewRecipes)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = RecipeAdapter(
            mutableListOf<Recipe>(),
            onSwipe = { recipeId ->
                val recipe = loadRecipes().find { it.id.toString() == recipeId }
                if (recipe != null) {
                    onRecipeSwiped(recipe)
                }
            },
            onLongClick = { recipe -> showRecipeOptions(recipe) } // Add long click handler
        )
        recyclerView.adapter = adapter

        // Rezepte nur beim ersten Aufruf mischen
        if (sessionRecipes == null) {
            sessionRecipes = loadRecipes().shuffled().toMutableList()
        }

        val filteredRecipes = filterRecipes(sessionRecipes!!)
        adapter.updateRecipes(filteredRecipes)

        // Floating Action Button
        val fabAddRecipe: View = view.findViewById(R.id.fabAddRecipe)
        fabAddRecipe.setOnClickListener {
            showAddRecipeMenu()
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

                if (direction == ItemTouchHelper.LEFT) {
                    // Nach links wischen: Nur für die Session ausblenden
                    swipedRecipes.add(recipe.id)
                    val updatedRecipes = filterRecipes(sessionRecipes!!)
                    adapter.updateRecipes(updatedRecipes)
                } else if (direction == ItemTouchHelper.RIGHT) {
                    // Nach rechts wischen: Zu den Favoriten hinzufügen
                    onRecipeSwiped(recipe)
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun showAddRecipeMenu() {
        val menuDialog = AddRecipeMenuDialogFragment(
            onManual = {
                showAddRecipeDialog()
            },
            onInstagram = {
                ImportInstagramDialogFragment().show(parentFragmentManager, "ImportInstagramDialog")
            },
            onWebsite = {
                ImportWebsiteDialogFragment().show(parentFragmentManager, "ImportWebsiteDialog")
            }
        )
        menuDialog.show(parentFragmentManager, "AddRecipeMenuDialog")
    }

    private fun showAddRecipeDialog() {
        val dialog = AddRecipeDialogFragment()
        dialog.setOnRecipeAddedListener(object : AddRecipeDialogFragment.OnRecipeAddedListener {
            override fun onRecipeAdded(recipe: Recipe) {
                Log.d("RecipesFragment", "New recipe added: ${recipe.name} with ID: ${recipe.id}")

                // Session-Rezepte initialisieren falls noch nicht geschehen
                if (sessionRecipes == null) {
                    sessionRecipes = loadRecipes().shuffled().toMutableList()
                    Log.d("RecipesFragment", "Session recipes initialized with ${sessionRecipes!!.size} recipes")
                }

                // Neues Rezept an den Anfang der Session-Liste setzen
                sessionRecipes!!.add(0, recipe)
                Log.d("RecipesFragment", "Recipe added to sessionRecipes. New size: ${sessionRecipes!!.size}")

                // Gefilterte Rezepte aktualisieren und anzeigen
                val filteredRecipes = filterRecipes(sessionRecipes!!)
                Log.d("RecipesFragment", "Filtered recipes size: ${filteredRecipes.size}")

                adapter.updateRecipes(filteredRecipes)
                Log.d("RecipesFragment", "Adapter updated with ${filteredRecipes.size} recipes")

                // Zur obersten Position scrollen, um das neue Rezept zu zeigen
                val recyclerView: RecyclerView = requireView().findViewById(R.id.recyclerViewRecipes)
                recyclerView.scrollToPosition(0)
                Log.d("RecipesFragment", "Scrolled to position 0")
            }
        })
        dialog.show(parentFragmentManager, "AddRecipeDialog")
    }

    private fun loadRecipes(): MutableList<Recipe> {
        val recipesFile = File(requireContext().filesDir, "recipes.json")
        return if (recipesFile.exists()) {
            try {
                val json = recipesFile.readText()
                val type = object : TypeToken<MutableList<Recipe>>() {}.type
                val recipes: MutableList<Recipe> = Gson().fromJson(json, type)

                // Entferne die Ersetzung des Bildpfads durch den absoluten Pfad!
                recipes.forEach { recipe ->
                    if (recipe.image != null) {
                        val imageFile = File(requireContext().filesDir, recipe.image)
                        if (!imageFile.exists()) {
                            recipe.image = null // Reset if the image file is missing
                        }
                    }
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
        val updatedRecipes = filterRecipes(sessionRecipes ?: loadRecipes())
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
        val imagesDir = File(requireContext().filesDir, "images")

        // Ensure the images directory exists
        if (!imagesDir.exists()) {
            imagesDir.mkdir()
        }

        // Copy recipes from assets
        val assetRecipes: MutableList<Recipe> = try {
            val json = requireContext().assets.open("recipes.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<MutableList<Recipe>>() {}.type
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            mutableListOf()
        }

        // Copy images from assets to internal storage
        try {
            requireContext().assets.list("images")?.forEach { imageName ->
                val inputStream = requireContext().assets.open("images/$imageName")
                val outputFile = File(imagesDir, imageName)
                if (!outputFile.exists()) {
                    inputStream.use { input ->
                        outputFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Check if the internal recipes file exists
        if (!recipesFile.exists()) {
            // Save the asset recipes to internal storage if the file doesn't exist
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

    fun addNewRecipeToTop(recipe: Recipe) {
        Log.d("RecipesFragment", "addNewRecipeToTop called with recipe: ${recipe.name} ID: ${recipe.id}")

        // Session-Rezepte initialisieren falls noch nicht geschehen
        if (sessionRecipes == null) {
            sessionRecipes = loadRecipes().shuffled().toMutableList()
            Log.d("RecipesFragment", "Session recipes initialized with ${sessionRecipes!!.size} recipes")
        }

        // Neues Rezept an den Anfang der Session-Liste setzen
        sessionRecipes!!.add(0, recipe)
        Log.d("RecipesFragment", "Recipe added to sessionRecipes. New size: ${sessionRecipes!!.size}")

        // Gefilterte Rezepte aktualisieren und anzeigen
        val filteredRecipes = filterRecipes(sessionRecipes!!)
        Log.d("RecipesFragment", "Filtered recipes size: ${filteredRecipes.size}")

        adapter.updateRecipes(filteredRecipes)
        Log.d("RecipesFragment", "Adapter updated with ${filteredRecipes.size} recipes")

        // Zur obersten Position scrollen, um das neue Rezept zu zeigen
        val recyclerView: RecyclerView = requireView().findViewById(R.id.recyclerViewRecipes)
        recyclerView.scrollToPosition(0)
        Log.d("RecipesFragment", "Scrolled to position 0")
    }

    private fun showRecipeOptions(recipe: Recipe) {
        val dialog = RecipeOptionsDialogFragment(
            recipe = recipe,
            onEdit = { recipeToEdit ->
                showEditRecipeDialog(recipeToEdit)
            },
            onDelete = { recipeToDelete ->
                deleteRecipe(recipeToDelete)
            }
        )
        dialog.show(parentFragmentManager, "RecipeOptionsDialog")
    }

    private fun showEditRecipeDialog(recipe: Recipe) {
        val dialog = AddRecipeDialogFragment()
        dialog.setRecipeToEdit(recipe) // Assume this method exists to pre-fill the dialog
        dialog.setOnRecipeAddedListener(object : AddRecipeDialogFragment.OnRecipeAddedListener {
            override fun onRecipeAdded(updatedRecipe: Recipe) {
                updateRecipeInList(updatedRecipe)
            }
        })
        dialog.show(parentFragmentManager, "EditRecipeDialog")
    }

    private fun updateRecipeInList(updatedRecipe: Recipe) {
        val recipesFile = File(requireContext().filesDir, "recipes.json")
        if (recipesFile.exists()) {
            val json = recipesFile.readText()
            val type = object : TypeToken<MutableList<Recipe>>() {}.type
            val recipes: MutableList<Recipe> = Gson().fromJson(json, type)

            // Find and update the recipe
            val index = recipes.indexOfFirst { it.id == updatedRecipe.id }
            if (index != -1) {
                recipes[index] = updatedRecipe
                recipesFile.writeText(Gson().toJson(recipes))

                // Update session recipes if they exist
                sessionRecipes?.let { sessionList ->
                    val sessionIndex = sessionList.indexOfFirst { it.id == updatedRecipe.id }
                    if (sessionIndex != -1) {
                        sessionList[sessionIndex] = updatedRecipe
                    }
                }

                // Update the UI
                val filteredRecipes = filterRecipes(sessionRecipes ?: loadRecipes())
                adapter.updateRecipes(filteredRecipes)
            }
        }
    }

    private fun deleteRecipe(recipe: Recipe) {
        val recipesFile = File(requireContext().filesDir, "recipes.json")
        if (recipesFile.exists()) {
            val json = recipesFile.readText()
            val type = object : TypeToken<MutableList<Recipe>>() {}.type
            val recipes: MutableList<Recipe> = Gson().fromJson(json, type)

            // Remove from recipes file
            recipes.removeIf { it.id == recipe.id }
            recipesFile.writeText(Gson().toJson(recipes))

            // Remove from session recipes if they exist
            sessionRecipes?.removeIf { it.id == recipe.id }

            // Update the UI
            val filteredRecipes = filterRecipes(sessionRecipes ?: loadRecipes())
            adapter.updateRecipes(filteredRecipes)

            // Also remove from favorites if it exists there
            removeFavorite(recipe)
        }
    }
}