package com.app.mealplanner

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.app.mealplanner.databinding.DialogAddRecipeMenuBinding

class AddRecipeMenuDialogFragment(
    private val onManual: () -> Unit,
    private val onInstagram: () -> Unit,
    private val onWebsite: () -> Unit
) : BottomSheetDialogFragment() {
    private var _binding: DialogAddRecipeMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = DialogAddRecipeMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fabManual.setOnClickListener {
            dismiss()
            onManual()
        }
        binding.fabInstagram.setOnClickListener {
            dismiss()
            onInstagram()
        }
        binding.fabWebsite.setOnClickListener {
            dismiss()
            onWebsite()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

