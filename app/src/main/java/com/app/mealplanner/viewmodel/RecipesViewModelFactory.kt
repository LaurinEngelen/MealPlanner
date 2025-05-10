package com.app.mealplanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.mealplanner.repository.RecipesRepository

class RecipesViewModelFactory(private val repository: RecipesRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipesViewModel::class.java)) {
            return RecipesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}