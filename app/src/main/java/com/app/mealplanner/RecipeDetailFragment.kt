import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.mealplanner.R
import com.app.mealplanner.model.Recipe
import com.bumptech.glide.Glide
import java.io.File

class RecipeDetailFragment : Fragment() {

    companion object {
        private const val ARG_RECIPE = "recipe"

        fun newInstance(recipe: Recipe): RecipeDetailFragment {
            val fragment = RecipeDetailFragment()
            val args = Bundle()
            args.putSerializable(ARG_RECIPE, recipe)
            fragment.arguments = args
            return fragment
        }
    }

    private var recipe: Recipe? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recipe = arguments?.getSerializable(ARG_RECIPE) as? Recipe
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recipe_detail, container, false)

        val closeButton: View = view.findViewById(R.id.close_button)
        closeButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val titleTextView: TextView = view.findViewById(R.id.recipe_title)
        val descriptionTextView: TextView = view.findViewById(R.id.recipe_description)
        val recipeImageView: ImageView = view.findViewById(R.id.recipe_image) // Recipe image view
        val recipePrepTime: TextView = view.findViewById(R.id.recipe_prep_time)

        recipe?.let {
            titleTextView.text = it.name
            descriptionTextView.text = it.description

            // Bild-Pfad korrekt auflösen (relativ zu filesDir, falls nicht absolut)
            val imagePath = it.image
            val imageFile = if (!imagePath.isNullOrEmpty() && !File(imagePath).isAbsolute) File(requireContext().filesDir, imagePath) else if (!imagePath.isNullOrEmpty()) File(imagePath) else null
            if (imageFile != null && imageFile.exists()) {
                Glide.with(this)
                    .load(imageFile)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_dialog_alert)
                    .into(recipeImageView)
            } else {
                recipeImageView.setImageResource(android.R.drawable.ic_menu_gallery)
            }
            recipePrepTime.text = "Zubereitung: ${it.prepTime}"
        }

        // Dynamische Portionsangabe und Notizen setzen
        val notesTitle: TextView = view.findViewById(R.id.recipe_notes_title)
        val notesContent: TextView = view.findViewById(R.id.recipe_notes_content)
        notesContent.text = recipe?.notes ?: "Keine Notizen vorhanden."


        val ingredientsButton: Button = view.findViewById(R.id.ingredients_button)
        val instructionsButton: Button = view.findViewById(R.id.instructions_button)
        val nutritionsButton: Button = view.findViewById(R.id.nutrition_button)

        ingredientsButton.setOnClickListener {
            loadContent(R.layout.recipe_detail_ingredients)
        }

        instructionsButton.setOnClickListener {
            loadContent(R.layout.recipe_detail_instructions)
        }

        nutritionsButton.setOnClickListener {
            loadContent(R.layout.recipe_detail_nutritions)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadContent(R.layout.recipe_detail_ingredients) // Automatically load ingredients
    }

    private fun loadContent(layoutResId: Int) {
        val contentContainer = requireView().findViewById<ViewGroup>(R.id.content_container)
        contentContainer.removeAllViews()
        LayoutInflater.from(context).inflate(layoutResId, contentContainer, true)

        when (layoutResId) {
            R.layout.recipe_detail_ingredients -> {
                val recyclerView = contentContainer.findViewById<RecyclerView>(R.id.ingredients_recycler_view)
                recyclerView?.adapter = IngredientsAdapter((recipe?.ingredients ?: emptyList()).toMutableList(), android.R.color.black, onDelete = { _ -> }, onStartDrag = { _ -> }, showEditIcons = false)
                recyclerView?.layoutManager = LinearLayoutManager(requireContext())
                // Portionsangabe dynamisch setzen
                val servingsCount: TextView? = contentContainer.findViewById(R.id.servings_count)
                if (recipe != null && recipe?.servings != null && recipe?.servings!! > 0) {
                    servingsCount?.text = recipe?.servings.toString()
                } else {
                    servingsCount?.text = "-"
                }
            }
            R.layout.recipe_detail_instructions -> {
                val recyclerView = contentContainer.findViewById<RecyclerView>(R.id.instructions_recycler_view)
                recyclerView?.adapter = PreparationsAdapter((recipe?.preparations ?: emptyList()).toMutableList(), android.R.color.black, onDelete = { _ -> }, onStartDrag = { _ -> }, showEditIcons = false)
                recyclerView?.layoutManager = LinearLayoutManager(requireContext())
            }
        }
    }
}