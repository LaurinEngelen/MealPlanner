package com.app.mealplanner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.app.mealplanner.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                com.app.mealplanner.R.id.navigation_recipes -> {
                    replaceFragment(RecipesFragment())
                }
                com.app.mealplanner.R.id.navigation_favorites -> {
                    replaceFragment(FavoritesFragment())
                }
            }
            true
        }

        // Initial fragment load
        if (savedInstanceState == null) {
            replaceFragment(RecipesFragment())
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(com.app.mealplanner.R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }
}