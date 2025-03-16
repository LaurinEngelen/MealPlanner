package com.app.mealplanner

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.mealplanner.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var recipeAdapter: RecipeAdapter
    private var recipes: MutableList<Recipe> = mutableListOf()
    private var i = 0

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Bottom Navigation View Setup
        setupBottomNavigation()

        // RecyclerView Setup
        setupRecyclerView()
    }

    private fun setupBottomNavigation() {

        val bottomNavigationView: BottomNavigationView = binding.navView
        bottomNavigationView.setOnItemSelectedListener{
            // Hier die jeweilige Aktion durchführen
            true
        }
    }

    private fun setupRecyclerView() {
        recipes = createRecipes().toMutableList()
        recipeAdapter = RecipeAdapter(recipes)
        // Kein findViewById mehr notwendig
        binding.recyclerViewRecipes.adapter = recipeAdapter
        binding.recyclerViewRecipes.layoutManager = LinearLayoutManager(this)

        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val swipedRecipe = recipes[position]
                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        Toast.makeText(this@MainActivity, "Disliked: ${swipedRecipe.name}", Toast.LENGTH_SHORT).show()
                        recipeAdapter.removeRecipe(position)
                    }
                    ItemTouchHelper.RIGHT -> {
                        Toast.makeText(this@MainActivity, "Liked: ${swipedRecipe.name}", Toast.LENGTH_SHORT).show()
                        recipeAdapter.removeRecipe(position)
                    }
                }
                if (recipes.isEmpty()){
                    // Hinzufügen von neuen Rezepten
                    val ingredients = arrayListOf("Zutat".plus(i))
                    val newRecipe = Recipe(i, "Rezept".plus(i), ingredients)
                    recipeAdapter.addRecipe(newRecipe)
                    Log.d("LIST", "notified")
                    i++
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewRecipes)
    }
    //createRecipes muss implementiert werden
    private fun createRecipes(): List<Recipe> {
        val list: MutableList<Recipe> = mutableListOf()
        val ingredients = arrayListOf("Zutat1", "Zutat2")
        list.add(Recipe(0, "Rezept1", ingredients))
        list.add(Recipe(1, "Rezept2", ingredients))
        return list
    }
}