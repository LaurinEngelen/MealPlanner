package com.app.mealplanner

data class Recipe(
    val id: Int,
    val name: String,
    val image: Int? = null, // Referenz auf ein Drawable (optional)
    val ingredients: List<String>, // Liste der Zutaten
    val preparation: String // Zubereitungsschritte
)