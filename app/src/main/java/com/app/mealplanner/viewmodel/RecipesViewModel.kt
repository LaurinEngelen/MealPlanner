package com.app.mealplanner.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mealplanner.model.Recipe
import com.app.mealplanner.repository.RecipesRepository
import kotlinx.coroutines.launch

class RecipesViewModel(private val repository: RecipesRepository) : ViewModel() {

    private val _recipes = MutableLiveData<List<Recipe>>()
    val recipes: LiveData<List<Recipe>> get() = _recipes

    fun loadRecipes() {
        viewModelScope.launch {
            _recipes.value = repository.getRecipes()
        }
    }
}