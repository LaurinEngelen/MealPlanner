package com.app.mealplanner.model

data class Recipe(
    var id: Int,
    val name: String,
    val description: String? = null,
    val ingredients: List<String>,
    val preparations: List<String>,
    val image: String? = null,
    val servings: Int? = null,
    val prepTime: String? = null,
    val notes: String? = null
)
