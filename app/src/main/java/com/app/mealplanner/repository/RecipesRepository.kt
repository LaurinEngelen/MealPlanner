package com.app.mealplanner.repository

import android.content.Context
import com.app.mealplanner.model.Recipe
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

class RecipesRepository(private val context: Context) {

    fun getRecipes(): List<Recipe> {
        val jsonString = readJsonFromAssets("recipes.json")
        val recipeListType = object : TypeToken<List<Recipe>>() {}.type
        return Gson().fromJson(jsonString, recipeListType)
    }

    private fun readJsonFromAssets(fileName: String): String {
        val inputStream = context.assets.open(fileName)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        return bufferedReader.use { it.readText() }
    }
}