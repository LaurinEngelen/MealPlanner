package com.app.mealplanner

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.mealplanner.databinding.RecipeItemBinding

class RecipeAdapter(private val recipes: MutableList<Recipe>) :
    RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(private val binding: RecipeItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val nameTextView: TextView = binding.recipeName
        // Hier kannst du weitere Views für Zutaten etc. hinzufügen
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = RecipeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val currentRecipe = recipes[position]
        holder.nameTextView.text = currentRecipe.name
        // Hier kannst du weitere Daten setzen, zum Beispiel:
        // holder.ingredientsTextView.text = currentRecipe.ingredients.joinToString(", ")
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