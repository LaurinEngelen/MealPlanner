package com.app.mealplanner

import IngredientsAdapter
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.mealplanner.databinding.RecipeItemBinding
import com.app.mealplanner.model.Recipe
import com.bumptech.glide.Glide
import androidx.recyclerview.widget.GridLayoutManager
import java.io.File

class RecipeAdapter(private var recipes: MutableList<Recipe>,
                    private val onSwipe: (String) -> Unit) :
    RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(private val binding: RecipeItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val nameTextView: TextView = binding.recipeName
        val recipeImage: ImageView = binding.recipeImage
        //val ingredientsList: TextView = binding.ingredientsList
        val preparationList: TextView = binding.preparationList
        val recipeDescription: TextView = binding.recipeDescription
        val servings: TextView = binding.servings
        val preparationTime: TextView = binding.preparationTime
        //val notes: TextView = binding.notes
        val ingredientsRecyclerView = binding.ingredientsRecyclerView // Expose RecyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = RecipeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val currentRecipe = recipes[position]
        holder.nameTextView.text = currentRecipe.name

        val ingredientsAdapter = IngredientsAdapter(
            currentRecipe.ingredients?.toMutableList() ?: mutableListOf(),
            R.color.AcardTextColor,
            onDelete = { _ -> },
            onStartDrag = { _ -> },
            showEditIcons = false
        )
        val gridLayoutManager = GridLayoutManager(holder.itemView.context, if ((currentRecipe.ingredients?.size ?: 0) > 5) 2 else 1)
        holder.ingredientsRecyclerView.layoutManager = gridLayoutManager
        holder.ingredientsRecyclerView.adapter = ingredientsAdapter

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

        holder.preparationList.text = currentRecipe.preparations
            ?.mapIndexed { index, preparation -> "${index + 1}. $preparation" }
            ?.joinToString("\n") ?: ""
        holder.servings.text = Html.fromHtml("<b>Portionen:</b> ${currentRecipe.servings ?: "<b>Portionen:</b> N/A"}")
        holder.preparationTime.text = Html.fromHtml("<b>Zubereitungszeit:</b> ${currentRecipe.prepTime ?: "<b>Zubereitungszeit:</b> N/A"}")
        holder.recipeDescription.text = currentRecipe.description ?: "Keine Beschreibung verfügbar"
        /*if (currentRecipe.notes.isNullOrEmpty()) {
            holder.notes.visibility = View.GONE
            holder.itemView.findViewById<TextView>(R.id.notesLabel).visibility = View.GONE
        } else {
            holder.notes.visibility = View.VISIBLE
            holder.itemView.findViewById<TextView>(R.id.notesLabel).visibility = View.VISIBLE
            holder.notes.text = currentRecipe.notes
        }*/
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