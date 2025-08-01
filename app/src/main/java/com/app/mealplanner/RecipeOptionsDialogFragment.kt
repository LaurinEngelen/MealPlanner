package com.app.mealplanner

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.app.mealplanner.databinding.DialogRecipeOptionsBinding
import com.app.mealplanner.model.Recipe

class RecipeOptionsDialogFragment(
    private val recipe: Recipe,
    private val onEdit: (Recipe) -> Unit,
    private val onDelete: (Recipe) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogRecipeOptionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = DialogRecipeOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set recipe title
        binding.recipeTitle.text = recipe.name

        binding.btnEdit.setOnClickListener {
            dismiss()
            onEdit(recipe)
        }

        binding.btnDelete.setOnClickListener {
            dismiss()
            confirmDelete()
        }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(requireContext())
            .setTitle("Rezept löschen")
            .setMessage("Möchten Sie das Rezept \"${recipe.name}\" wirklich löschen?")
            .setPositiveButton("Löschen") { _, _ ->
                onDelete(recipe)
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
