package com.app.mealplanner

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.app.mealplanner.databinding.DialogImportInstagramBinding
import com.app.mealplanner.network.CHATGPTApiService
import com.app.mealplanner.model.Recipe
import kotlin.concurrent.thread

class ImportInstagramDialogFragment : BottomSheetDialogFragment() {
    private var _binding: DialogImportInstagramBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = DialogImportInstagramBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonImportInstagram.setOnClickListener {
            val url = binding.inputInstagramLink.text.toString().trim()
            if (url.isNotEmpty()) {
                binding.textRecipeResult.text = "Rezept wird extrahiert ..."
                thread {
                    val service = CHATGPTApiService(requireContext())
                    val result = service.extractRecipeFromUrl(url)
                    Handler(Looper.getMainLooper()).post {
                        if (result != null && result.isNotEmpty()) {
                            // AddRecipeDialogFragment öffnen und Rezepttext übergeben
                            val addRecipeDialog = AddRecipeDialogFragment()
                            val args = Bundle()
                            args.putString("imported_recipe_text", result)
                            addRecipeDialog.arguments = args

                            // Listener setzen - sowohl den normalen Listener als auch direkten Update
                            addRecipeDialog.setOnRecipeAddedListener(object : AddRecipeDialogFragment.OnRecipeAddedListener {
                                override fun onRecipeAdded(recipe: Recipe) {
                                    // Versuche das Fragment zu finden und direkt zu aktualisieren
                                    val recipesFragment = parentFragmentManager.findFragmentByTag("RezepteFragment") as? RecipesFragment
                                        ?: parentFragmentManager.fragments.find { it is RecipesFragment } as? RecipesFragment

                                    recipesFragment?.addNewRecipeToTop(recipe)
                                }
                            })

                            addRecipeDialog.show(parentFragmentManager, "AddRecipeDialogFragment")
                            dismiss()
                        } else {
                            binding.textRecipeResult.text = "Kein Rezept gefunden oder Fehler bei der Extraktion."
                        }
                    }
                }
            } else {
                binding.textRecipeResult.text = "Bitte einen gültigen Link eingeben."
            }
        }
    }
}
