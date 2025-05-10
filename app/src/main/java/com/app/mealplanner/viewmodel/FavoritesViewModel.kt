package com.app.mealplanner.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FavoritesViewModel : ViewModel() {

    private val _favorites = MutableLiveData<List<String>>() // Replace String with your data type
    val favorites: LiveData<List<String>> get() = _favorites

    init {
        // Initialize with some data or fetch from a repository
        _favorites.value = listOf("Favorite 1", "Favorite 2", "Favorite 3")
    }

    fun addFavorite(item: String) {
        val currentFavorites = _favorites.value?.toMutableList() ?: mutableListOf()
        currentFavorites.add(item)
        _favorites.value = currentFavorites
    }
}