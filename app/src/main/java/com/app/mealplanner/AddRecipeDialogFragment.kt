import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.mealplanner.R
import com.app.mealplanner.model.Recipe
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class AddRecipeDialogFragment : DialogFragment() {

    interface OnRecipeAddedListener {
        fun onRecipeAdded(recipe: Recipe)
    }

    private var listener: OnRecipeAddedListener? = null

    fun setOnRecipeAddedListener(listener: OnRecipeAddedListener) {
        this.listener = listener
    }

    private val ingredients = mutableListOf<String>()
    private lateinit var ingredientsAdapter: IngredientsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add_recipe, container, false)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nameInput: EditText = view.findViewById(R.id.inputRecipeTitle)
        val preparationInput: EditText = view.findViewById(R.id.inputDescription)
        val newIngredientInput: EditText = view.findViewById(R.id.inputNewIngredient)
        val servingsInput: EditText = view.findViewById(R.id.inputServings)
        val prepHoursInput: EditText = view.findViewById(R.id.inputPrepHours)
        val prepMinutesInput: EditText = view.findViewById(R.id.inputPrepMinutes)
        val notesInput: EditText = view.findViewById(R.id.inputNotes)
        val saveButton: Button = view.findViewById(R.id.buttonAddRecipe)
        val ingredientsRecyclerView: RecyclerView = view.findViewById(R.id.ingredientsRecyclerView)

        val backButton: View = view.findViewById(R.id.backButton) // Replace with the actual ID of the back symbol
        backButton.setOnClickListener {
            dismiss() // Close the dialog
        }

        // Set up RecyclerView
        ingredientsAdapter = IngredientsAdapter(ingredients)
        ingredientsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        ingredientsRecyclerView.adapter = ingredientsAdapter

        // Add ingredient on Enter key press
        newIngredientInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                val ingredientText = newIngredientInput.text.toString().trim()
                if (ingredientText.isNotEmpty()) {
                    ingredients.add(ingredientText) // Zutat zur Liste hinzufügen
                    ingredientsAdapter.notifyDataSetChanged() // RecyclerView aktualisieren
                    newIngredientInput.text.clear() // Eingabefeld leeren
                }
                true // Event wurde verarbeitet
            } else {
                false
            }
        }

        saveButton.setOnClickListener {
            val name = nameInput.text.toString()
            val preparation = preparationInput.text.toString()
            val servings = servingsInput.text.toString().toIntOrNull() ?: 0
            val prepTime = "${prepHoursInput.text}:${prepMinutesInput.text}"
            val notes = notesInput.text.toString()

            if (name.isNotEmpty() && ingredients.isNotEmpty() && preparation.isNotEmpty()) {
                val newRecipe = Recipe(
                    id = System.currentTimeMillis().toInt(),
                    name = name,
                    ingredients = ingredients,
                    preparation = preparation,
                    image = null // Optional: Add image handling if needed
                )
                saveRecipe(newRecipe)
                listener?.onRecipeAdded(newRecipe)
                dismiss()
            }
        }
    }

    private fun saveRecipe(recipe: Recipe) {
        val recipesFile = File(requireContext().filesDir, "recipes.json")
        val recipes: MutableList<Recipe> = if (recipesFile.exists()) {
            val json = recipesFile.readText()
            val type = object : TypeToken<MutableList<Recipe>>() {}.type
            Gson().fromJson(json, type)
        } else {
            mutableListOf()
        }
        recipes.add(recipe)
        recipesFile.writeText(Gson().toJson(recipes))
    }
}