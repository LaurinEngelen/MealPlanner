package com.app.mealplanner

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.mealplanner.databinding.FragmentRecipesBinding

class RecipesFragment : Fragment() {
    private var binding: FragmentRecipesBinding? = null
    private var recipeAdapter: RecipeAdapter? = null
    private var recipes: MutableList<Recipe> = ArrayList()
    private var i = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecipesBinding.inflate(inflater, container, false)
        setupRecyclerView()
        return binding!!.root
    }

    private fun setupRecyclerView() {
        recipes = createRecipes().toMutableList()
        recipeAdapter = RecipeAdapter(recipes)
        binding!!.recyclerViewRecipes.adapter = recipeAdapter
        binding!!.recyclerViewRecipes.layoutManager = LinearLayoutManager(context)
        binding!!.recyclerViewRecipes.addItemDecoration(MarginItemDecoration(16))

        val swipeHandler: ItemTouchHelper.SimpleCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position: Int = viewHolder.bindingAdapterPosition
                val swipedRecipe = recipes[position]
                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        Toast.makeText(
                            context,
                            "Disliked: " + swipedRecipe.name,
                            Toast.LENGTH_SHORT
                        ).show()
                        recipeAdapter!!.removeRecipe(position)
                    }

                    ItemTouchHelper.RIGHT -> {
                        Toast.makeText(context, "Liked: " + swipedRecipe.name, Toast.LENGTH_SHORT)
                            .show()
                        recipeAdapter!!.removeRecipe(position)
                        FavoritesRepository.getInstance().addFavoriteRecipe(swipedRecipe)
                    }
                }
                if (recipes.isEmpty()) {
                    val ingredients: MutableList<String> = ArrayList()
                    ingredients.add("Zutat $i")
                    val newRecipe = Recipe(
                        i, "Rezept $i", null, ingredients,
                        "Zubereitungstext $i"
                    )
                    recipeAdapter!!.addRecipe(newRecipe)
                    i++
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding!!.recyclerViewRecipes)
    }

    private fun createRecipes(): List<Recipe> {
        val list: MutableList<Recipe> = ArrayList()
        val ingredients = listOf("Zutat 1", "Zutat 2", "Zutat 3")
        val preparation = "Schritt 1...\nSchritt 2...\nSchritt 3..."
        list.add(Recipe(0, "Rezept 1", null, ingredients, preparation))
        list.add(Recipe(1, "Rezept 2", null, ingredients, preparation))
        return list
    }
}

class MarginItemDecoration(private val margin: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = margin
        }
        outRect.left = margin
        outRect.right = margin
        outRect.bottom = margin
    }
}