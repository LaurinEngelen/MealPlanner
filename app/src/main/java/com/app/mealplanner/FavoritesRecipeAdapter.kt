package com.app.mealplanner

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.mealplanner.databinding.NewRecipeItemBinding
import com.app.mealplanner.model.Recipe

class FavoritesRecipeAdapter(
    private var recipes: MutableList<Recipe>,
    private val onSwipe: (String) -> Unit
) : RecyclerView.Adapter<FavoritesRecipeAdapter.FavoritesRecipeViewHolder>() {

    class FavoritesRecipeViewHolder(private val binding: NewRecipeItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val recipeImage: ImageView = binding.recipeImage
        val recipeTitle: TextView = binding.recipeTitle
        val recipeTime: TextView = binding.recipeTime
        val recipeIcon: ImageView = binding.recipeIcon
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesRecipeViewHolder {
        val binding = NewRecipeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoritesRecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoritesRecipeViewHolder, position: Int) {
        val currentRecipe = recipes[position]
        holder.recipeTitle.text = currentRecipe.name

        if (currentRecipe.image != null) {
            holder.recipeImage.setImageResource(currentRecipe.image as Int)
        } else {
            holder.recipeImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.recipeTime.text = "${currentRecipe.cookingTime} min"
        holder.recipeIcon.setImageResource(R.drawable.ic_timer)
    }

    override fun getItemCount(): Int = recipes.size

    fun updateRecipes(newRecipes: MutableList<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }
}