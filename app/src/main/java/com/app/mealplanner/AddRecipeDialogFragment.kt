package com.app.mealplanner

import IngredientsAdapter
import PreparationsAdapter
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.ItemTouchHelper
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
    private var recipeToEdit: Recipe? = null // Variable to hold the recipe being edited

    interface OnRecipeAddedListener {
        fun onRecipeAdded(recipe: Recipe)
    }

    private var listener: OnRecipeAddedListener? = null

    fun setOnRecipeAddedListener(listener: OnRecipeAddedListener) {
        this.listener = listener
    }

    fun setRecipeToEdit(recipe: Recipe) {
        this.recipeToEdit = recipe
    }

    private val ingredients = mutableListOf<String>()
    private lateinit var ingredientsAdapter: IngredientsAdapter

    private val preparations = mutableListOf<String>()
    private lateinit var preparationsAdapter: PreparationsAdapter

    private lateinit var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener

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

        // Adapter initialisieren, bevor sie verwendet werden
        ingredientsAdapter = IngredientsAdapter(
            ingredients,
            android.R.color.black,
            onDelete = { pos ->
                ingredients.removeAt(pos)
                ingredientsAdapter.notifyItemRemoved(pos)
            },
            onStartDrag = { viewHolder ->
                // Drag-Logik
            },
            showEditIcons = true // Edit-Icons explizit anzeigen
        )
        preparationsAdapter = PreparationsAdapter(
            preparations,
            android.R.color.black,
            onDelete = { pos ->
                preparations.removeAt(pos)
                preparationsAdapter.notifyItemRemoved(pos)
            },
            onStartDrag = { viewHolder ->
                // Drag-Logik
            },
            showEditIcons = true // Edit-Icons explizit anzeigen
        )

        // Rezepttext aus Argumenten übernehmen und Felder vorausfüllen
        val importedRecipeText = arguments?.getString("imported_recipe_text")
        if (!importedRecipeText.isNullOrEmpty()) {
            val nameInput: EditText = view.findViewById(R.id.inputRecipeTitle)
            val descriptionInput: EditText = view.findViewById(R.id.inputDescription)
            val servingsInput: EditText = view.findViewById(R.id.inputServings)
            val prepTimeInput: EditText = view.findViewById(R.id.inputPrepTime)
            val notesInput: EditText = view.findViewById(R.id.inputNotes)

            try {
                val json = com.google.gson.JsonParser.parseString(importedRecipeText).asJsonObject
                android.util.Log.d("AddRecipeDialog", "JSON: $json")
                nameInput.setText(json["name"]?.asString ?: "")
                servingsInput.setText(json["servings"]?.asInt?.toString() ?: "")
                prepTimeInput.setText(json["prepTime"]?.asString ?: "")
                notesInput.setText(json["notes"]?.asString ?: "")

                // Beschreibung korrekt setzen
                descriptionInput.setText(json["description"]?.asString ?: "")

                // Zutaten als Liste
                ingredients.clear()
                json["ingredients"]?.asJsonArray?.forEach { elem ->
                    ingredients.add(elem.asString)
                }
                ingredientsAdapter.notifyDataSetChanged()

                // Zubereitung: Nur Array zulassen
                preparations.clear()
                val preparationsElement = json["preparations"]
                android.util.Log.d("AddRecipeDialog", "preparationsElement: $preparationsElement")
                if (preparationsElement != null && preparationsElement.isJsonArray) {
                    preparationsElement.asJsonArray.forEach { elem ->
                        android.util.Log.d("AddRecipeDialog", "preparation step: ${elem.asString}")
                        preparations.add(elem.asString)
                    }
                } else {
                    android.util.Log.e("AddRecipeDialog", "preparations ist kein Array!")
                }
                android.util.Log.d("AddRecipeDialog", "preparations list: $preparations")
                preparationsAdapter.notifyDataSetChanged()

            } catch (e: Exception) {
                android.util.Log.e("AddRecipeDialog", "Rezept konnte nicht geladen werden: ${e.message}")
            }
        }

        // If editing an existing recipe, pre-fill the fields
        recipeToEdit?.let { recipe ->
            val nameInput: EditText = view.findViewById(R.id.inputRecipeTitle)
            val descriptionInput: EditText = view.findViewById(R.id.inputDescription)
            val servingsInput: EditText = view.findViewById(R.id.inputServings)
            val prepTimeInput: EditText = view.findViewById(R.id.inputPrepTime)
            val notesInput: EditText = view.findViewById(R.id.inputNotes)

            nameInput.setText(recipe.name)
            descriptionInput.setText(recipe.description ?: "")
            servingsInput.setText(recipe.servings?.toString() ?: "")
            prepTimeInput.setText(recipe.prepTime ?: "")
            notesInput.setText(recipe.notes ?: "")

            // Pre-fill ingredients
            ingredients.clear()
            recipe.ingredients?.let { recipeIngredients ->
                ingredients.addAll(recipeIngredients)
            }
            ingredientsAdapter.notifyDataSetChanged()

            // Pre-fill preparations
            preparations.clear()
            recipe.preparations?.let { recipePreparations ->
                preparations.addAll(recipePreparations)
            }
            preparationsAdapter.notifyDataSetChanged()

            // Set the existing image if available
            selectedImagePath = recipe.image
            if (!recipe.image.isNullOrEmpty()) {
                val selectedImageView = view.findViewById<ImageView>(R.id.selectedImageView)
                val uploadPlaceholder = view.findViewById<View>(R.id.uploadPlaceholder)

                val imageFile = File(requireContext().filesDir, recipe.image)
                if (imageFile.exists()) {
                    selectedImageView.setImageURI(Uri.fromFile(imageFile))
                    selectedImageView.visibility = View.VISIBLE
                    uploadPlaceholder.visibility = View.GONE
                }
            }
        }

        val nameInput: EditText = view.findViewById(R.id.inputRecipeTitle)
        val preparationInput: EditText = view.findViewById(R.id.inputDescription)
        val newIngredientInput: EditText = view.findViewById(R.id.inputNewIngredient)
        val servingsInput: EditText = view.findViewById(R.id.inputServings)
        val prepTimeInput: EditText = view.findViewById(R.id.inputPrepTime)
        val notesInput: EditText = view.findViewById(R.id.inputNotes)
        val saveButton: Button = view.findViewById(R.id.buttonAddRecipe)
        val ingredientsRecyclerView: RecyclerView = view.findViewById(R.id.ingredientsRecyclerView)
        val newPreparationInput: EditText = view.findViewById(R.id.inputNewInstruction)
        val preparationsRecyclerView: RecyclerView = view.findViewById(R.id.instructionsRecyclerView)


        val backButton: View = view.findViewById(R.id.backButton) // Replace with the actual ID of the back symbol
        backButton.setOnClickListener {
            dismiss() // Close the dialog
        }

        val uploadButton = view.findViewById<View>(R.id.buttonUploadImage)
        val selectedImageView = view.findViewById<ImageView>(R.id.selectedImageView)
        val uploadPlaceholder = view.findViewById<View>(R.id.uploadPlaceholder)
        uploadButton.setOnClickListener {
            openImagePicker()
        }

        // Set up RecyclerView
        val ingredientTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition
                ingredientsAdapter.onItemMove(from, to)
                return true
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })
        ingredientTouchHelper.attachToRecyclerView(ingredientsRecyclerView)

        val preparationTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition
                preparationsAdapter.onItemMove(from, to)
                return true
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })
        preparationTouchHelper.attachToRecyclerView(preparationsRecyclerView)

        ingredientsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        ingredientsRecyclerView.adapter = ingredientsAdapter

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

        // Button neu anlegen
        val addRecipeButton: Button = view.findViewById(R.id.buttonAddRecipe)
        addRecipeButton.visibility = View.VISIBLE
        addRecipeButton.isEnabled = true
        // Tastatur-Listener: Button ausblenden, wenn Keyboard sichtbar ist
        globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = android.graphics.Rect()
            requireActivity().window.decorView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = requireActivity().window.decorView.height
            val keypadHeight = screenHeight - rect.bottom
            if (keypadHeight > screenHeight * 0.15) {
                if (addRecipeButton.visibility != View.GONE) {
                    addRecipeButton.visibility = View.GONE
                }
            } else {
                if (addRecipeButton.visibility != View.VISIBLE) {
                    addRecipeButton.visibility = View.VISIBLE
                }
            }
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)

        saveButton.setOnClickListener {
            val name = nameInput.text.toString()
            val description = preparationInput.text.toString()
            val servings = servingsInput.text.toString().toIntOrNull() ?: 0
            val prepTime = prepTimeInput.text.toString()
            val notes = notesInput.text.toString()
            var imagePath: String? = selectedImagePath // Verwende das heruntergeladene Bild

            // Falls kein heruntergeladenes Bild vorhanden, aber ein manuell ausgewähltes Bild
            if (imagePath == null && imageUri != null) {
                imagePath = saveImageToInternalStorageAndReturnPath(imageUri!!, name)
            }

            if (name.isNotEmpty() && ingredients.isNotEmpty() && preparations.isNotEmpty()) {
                val recipe = if (recipeToEdit != null) {
                    // Editing existing recipe - keep the same ID
                    Recipe(
                        id = recipeToEdit!!.id,
                        name = name,
                        description = description,
                        ingredients = ingredients.toList(),
                        preparations = preparations.toList(),
                        image = imagePath,
                        servings = servings,
                        prepTime = prepTime,
                        notes = notes
                    )
                } else {
                    // Creating new recipe - generate new ID
                    Recipe(
                        id = System.currentTimeMillis().toInt(),
                        name = name,
                        description = description,
                        ingredients = ingredients.toList(),
                        preparations = preparations.toList(),
                        image = imagePath,
                        servings = servings,
                        prepTime = prepTime,
                        notes = notes
                    )
                }

                android.util.Log.d("AddRecipeDialog", "Saving recipe: ${recipe.name} with ID: ${recipe.id}")
                saveRecipe(recipe)
                android.util.Log.d("AddRecipeDialog", "Recipe saved, calling listener")
                listener?.onRecipeAdded(recipe)
                android.util.Log.d("AddRecipeDialog", "Listener called, dismissing dialog")
                dismiss()
            } else {
                android.util.Log.e("AddRecipeDialog", "Recipe validation failed - name: '${name}', ingredients: ${ingredients.size}, preparations: ${preparations.size}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        view?.viewTreeObserver?.removeOnGlobalLayoutListener(globalLayoutListener)
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

        if (recipeToEdit != null) {
            // Editing existing recipe - replace the existing one
            val index = recipes.indexOfFirst { it.id == recipe.id }
            if (index != -1) {
                recipes[index] = recipe
                android.util.Log.d("AddRecipeDialog", "Existing recipe updated at index $index with ID: ${recipe.id}")
            } else {
                // Fallback: if not found, add as new
                recipes.add(recipe)
                android.util.Log.d("AddRecipeDialog", "Recipe not found for editing, added as new with ID: ${recipe.id}")
            }
        } else {
            // Creating new recipe - add to list
            recipes.add(recipe)
            android.util.Log.d("AddRecipeDialog", "New recipe added with ID: ${recipe.id}")
        }

        recipesFile.writeText(Gson().toJson(recipes))
        android.util.Log.d("AddRecipeDialog", "Recipe saved to file")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            val selectedImageView = view?.findViewById<ImageView>(R.id.selectedImageView)
            val uploadPlaceholder = view?.findViewById<View>(R.id.uploadPlaceholder)
            if (imageUri != null && selectedImageView != null && uploadPlaceholder != null) {
                selectedImageView.setImageURI(imageUri)
                selectedImageView.visibility = View.VISIBLE
                uploadPlaceholder.visibility = View.GONE
                Toast.makeText(requireContext(), "Image selected successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Image selection failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageToInternalStorage(imageUri: Uri, recipeId: Int, recipeName: String) {
        // Nicht mehr benötigt, Logik in saveImageToInternalStorageAndReturnPath
    }

    private fun saveImageToInternalStorageAndReturnPath(imageUri: Uri, recipeName: String): String? {
        return try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val imageDir = File(requireContext().filesDir, "images")
            if (!imageDir.exists()) {
                imageDir.mkdir()
            }
            val sanitizedRecipeName = recipeName.replace("[^a-zA-Z0-9]".toRegex(), "_")
            val fileName = "${System.currentTimeMillis()}_${sanitizedRecipeName}.jpg"
            val imageFile = File(imageDir, fileName)
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            // Nur relativen Pfad speichern
            "images/$fileName"
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to save image: ${e.message}", Toast.LENGTH_SHORT).show()
            null
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
