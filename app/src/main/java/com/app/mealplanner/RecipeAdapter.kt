package com.app.mealplanner

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.mealplanner.databinding.RecipeItemBinding
import com.app.mealplanner.model.Recipe

class RecipeAdapter(private var recipes: MutableList<Recipe>,
                    private val onSwipe: (String) -> Unit) :
    RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(private val binding: RecipeItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val nameTextView: TextView = binding.recipeName
        val recipeImage: ImageView = binding.recipeImage
        val ingredientsList: TextView = binding.ingredientsList
        val preparationList: TextView = binding.preparationList
        val recipeDescription: TextView = binding.recipeDescription
        val servings: TextView = binding.servings
        val preparationTime: TextView = binding.preparationTime
        val notes: TextView = binding.notes
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = RecipeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val currentRecipe = recipes[position]
        holder.nameTextView.text = currentRecipe.name

        if (currentRecipe.image != null) {
            holder.recipeImage.setImageResource(currentRecipe.image as Int)
        } else {
            holder.recipeImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.ingredientsList.text = currentRecipe.ingredients?.joinToString("\n") ?: ""
        holder.preparationList.text = currentRecipe.preparations?.joinToString("\n") ?: ""
        holder.servings.text = "Servings: ${currentRecipe.servings ?: "Servings: N/A"}"
        holder.preparationTime.text = "Preparation Time: ${currentRecipe.prepTime ?: "Preparation Time: N/A"}"
        holder.notes.text = currentRecipe.notes ?: ""
        holder.recipeDescription.text = currentRecipe.description ?: "No description available"
    }

    override fun getItemCount(): Int = recipes.size

    fun removeRecipe(position: Int) {
        recipes.removeAt(position)
        notifyItemRemoved(position)
    }

    fun addRecipe(recipe: Recipe) {
        recipes.add(recipe)
        notifyItemInserted(recipes.size - 1)
    }

    fun updateRecipes(newRecipes: MutableList<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }

    fun removeRecipeAt(position: Int) {
        recipes.removeAt(position)
        notifyItemRemoved(position)
    }

    fun getRecipes(): MutableList<Recipe> {
        return recipes
    }

    fun onItemSwiped(position: Int) {
        val recipeId = recipes[position].id.toString()
        onSwipe(recipeId)
        recipes.removeAt(position)
        notifyItemRemoved(position)
    }
}