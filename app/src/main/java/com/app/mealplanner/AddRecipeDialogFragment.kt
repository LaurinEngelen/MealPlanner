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

    private val preparations = mutableListOf<String>()
    private lateinit var preparationsAdapter: PreparationsAdapter

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
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent) // Transparent background
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
        val newPreparationInput: EditText = view.findViewById(R.id.inputNewInstruction)
        val preparationsRecyclerView: RecyclerView = view.findViewById(R.id.instructionsRecyclerView)


        val backButton: View = view.findViewById(R.id.backButton) // Replace with the actual ID of the back symbol
        backButton.setOnClickListener {
            dismiss() // Close the dialog
        }

        // Set up RecyclerView
        ingredientsAdapter = IngredientsAdapter(ingredients, android.R.color.black)
        ingredientsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        ingredientsRecyclerView.adapter = ingredientsAdapter

        preparationsAdapter = PreparationsAdapter(preparations, android.R.color.black)
        preparationsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        preparationsRecyclerView.adapter = preparationsAdapter

        // Add ingredient on Enter key press
        newIngredientInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                val ingredientText = newIngredientInput.text.toString().trim()
                if (ingredientText.isNotEmpty()) {
                    ingredients.add(ingredientText) // Add ingredient to the list
                    ingredientsAdapter.notifyDataSetChanged() // Update RecyclerView
                    ingredientsRecyclerView.scrollToPosition(ingredients.size - 1) // Scroll to the last item
                    newIngredientInput.text.clear() // Clear input field
                }
                true // Event handled
            } else {
                false
            }
        }

        newPreparationInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                val preparationText = newPreparationInput.text.toString().trim()
                if (preparationText.isNotEmpty()) {
                    preparations.add(preparationText) // Add preparation step to the list
                    preparationsAdapter.notifyDataSetChanged() // Update RecyclerView
                    preparationsRecyclerView.scrollToPosition(preparations.size - 1) // Scroll to the last item
                    newPreparationInput.text.clear() // Clear input field
                }
                true // Event handled
            } else {
                false
            }
        }

        saveButton.setOnClickListener {
            val name = nameInput.text.toString()
            val servings = servingsInput.text.toString().toIntOrNull() ?: 0
            val prepTime = "${prepHoursInput.text}:${prepMinutesInput.text}"
            val notes = notesInput.text.toString()

            if (name.isNotEmpty() && ingredients.isNotEmpty() && preparations.isNotEmpty()) {
                val newRecipe = Recipe(
                    id = System.currentTimeMillis().toInt(),
                    name = name,
                    ingredients = ingredients,
                    preparations = preparations,
                    image = null, // Replace with actual image handling if needed
                    servings = servings,
                    prepTime = prepTime,
                    notes = notes
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

        // Generiere eine neue ID basierend auf der höchsten existierenden ID
        val newId = (recipes.maxOfOrNull { it.id } ?: 0) + 1
        val recipeWithId = recipe.copy(id = newId)

        recipes.add(recipeWithId)
        recipesFile.writeText(Gson().toJson(recipes))

    }
}