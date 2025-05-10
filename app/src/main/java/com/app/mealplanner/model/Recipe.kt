package com.app.mealplanner.model

data class Recipe(
    val id: Int,
    val name: String,
    val ingredients: List<String>,
    val preparation: String,
    val image: String? = null
)