package com.app.mealplanner

data class Recipe(
    val id: Int,
    val name: String,
    val ingredients: List<String>
)