import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.mealplanner.R
import com.app.mealplanner.model.Recipe
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class AddRecipeDialogFragment : DialogFragment() {

    private var selectedImagePath: String? = null
    private var imageUri: Uri? = null

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

        val uploadButton = view.findViewById<LinearLayout>(R.id.buttonUploadImage)
        uploadButton.setOnClickListener {
            openImagePicker()
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
            val description = preparationInput.text.toString() // Capture the description
            val servings = servingsInput.text.toString().toIntOrNull() ?: 0
            val prepTime = "${prepHoursInput.text}:${prepMinutesInput.text}"
            val notes = notesInput.text.toString()

            if (name.isNotEmpty() && ingredients.isNotEmpty() && preparations.isNotEmpty()) {
                val newRecipe = Recipe(
                    id = System.currentTimeMillis().toInt(),
                    name = name,
                    description = description, // Save the description
                    ingredients = ingredients,
                    preparations = preparations,
                    image = selectedImagePath,
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

        if (imageUri != null) {
            saveImageToInternalStorage(imageUri!!, recipeWithId.id, recipeWithId.name)
            recipeWithId.image = selectedImagePath // Aktualisiere den Bildpfad im Rezept
        }

        recipes.add(recipeWithId)
        recipesFile.writeText(Gson().toJson(recipes))

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            if (imageUri != null) {
                Toast.makeText(requireContext(), "Image selected successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Image selection failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageToInternalStorage(imageUri: Uri, recipeId: Int, recipeName: String) {
        try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val imageDir = File(requireContext().filesDir, "image")
            if (!imageDir.exists()) {
                imageDir.mkdir()
            }

            val sanitizedRecipeName = recipeName.replace("[^a-zA-Z0-9]".toRegex(), "_")
            selectedImagePath = "${imageDir.absolutePath}/${recipeId}_${sanitizedRecipeName}.jpg"
            val imageFile = File(imageDir, "${recipeId}_${sanitizedRecipeName}.jpg")

            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            selectedImagePath = imageFile.absolutePath
            Toast.makeText(requireContext(), "Image saved successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to save image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 1001
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }
}
