package com.app.mealplanner

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.mealplanner.databinding.RecipeItemBinding

class RecipeAdapter(private val recipes: MutableList<Recipe>) :
    RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(private val binding: RecipeItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val nameTextView: TextView = binding.recipeName
        val recipeImage: ImageView = binding.recipeImage
        val ingredientsList: TextView = binding.ingredientsList
        val preparationText: TextView = binding.preparationText
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = RecipeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val currentRecipe = recipes[position]
        holder.nameTextView.text = currentRecipe.name

        // Bild laden (wenn vorhanden)
        if (currentRecipe.image != null) {
            holder.recipeImage.setImageResource(currentRecipe.image)
        } else {
            // Hier könntest du ein Standardbild setzen, falls kein Bild vorhanden ist
            holder.recipeImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // Zutatenliste anzeigen
        holder.ingredientsList.text = currentRecipe.ingredients.joinToString("\n")

        // Zubereitung anzeigen
        holder.preparationText.text = currentRecipe.preparation
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
}