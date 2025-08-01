package com.app.mealplanner

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.mealplanner.databinding.NewRecipeItemBinding
import com.app.mealplanner.model.Recipe
import java.io.File
import com.bumptech.glide.Glide

class FavoritesRecipeAdapter(
    private var recipes: MutableList<Recipe>,
    private val onSwipe: (String) -> Unit,
    private val onRemoveClick: (Recipe) -> Unit,
    private val onClick: (Recipe) -> Unit,
    private val onLongClick: (Recipe) -> Unit // Add onLongClick callback
) : RecyclerView.Adapter<FavoritesRecipeAdapter.FavoritesRecipeViewHolder>() {

    class FavoritesRecipeViewHolder(private val binding: NewRecipeItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val recipeImage: ImageView = binding.recipeImage
        val recipeTitle: TextView = binding.recipeTitle
        val recipeTime: TextView = binding.recipeTime
        val recipeIcon: ImageView = binding.recipeIcon
        val btnRemoveFavorite: ImageView = binding.btnRemoveFavorite
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesRecipeViewHolder {
        val binding = NewRecipeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoritesRecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoritesRecipeViewHolder, position: Int) {
        val currentRecipe = recipes[position]
        holder.recipeTitle.text = currentRecipe.name

        val imagePath = currentRecipe.image
        val imageFile = if (!imagePath.isNullOrEmpty() && !File(imagePath).isAbsolute) File(holder.itemView.context.filesDir, imagePath) else if (!imagePath.isNullOrEmpty()) File(imagePath) else null
        if (imageFile != null && imageFile.exists()) {
            Glide.with(holder.itemView.context)
                .load(imageFile)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_dialog_alert)
                .into(holder.recipeImage)
        } else {
            holder.recipeImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.recipeTime.text = "${currentRecipe.prepTime} min"
        holder.recipeIcon.setImageResource(R.drawable.ic_timer)
        holder.btnRemoveFavorite.setOnClickListener {
            onRemoveClick(currentRecipe)
        }
        holder.itemView.setOnClickListener {
            onClick(currentRecipe) // Trigger the callback with the clicked recipe
        }
        holder.itemView.setOnLongClickListener {
            onLongClick(currentRecipe) // Trigger long click callback
            true
        }
    }

    override fun getItemCount(): Int = recipes.size

    fun updateRecipes(newRecipes: MutableList<Recipe>) {
        this.recipes = newRecipes
        notifyDataSetChanged() // RecyclerView aktualisieren
    }

    fun removeRecipe(recipe: Recipe) {
        val position = recipes.indexOf(recipe)
        if (position != -1) {
            recipes.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}