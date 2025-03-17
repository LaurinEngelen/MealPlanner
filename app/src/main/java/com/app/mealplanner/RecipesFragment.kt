package com.app.mealplanner;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.app.mealplanner.databinding.FragmentRecipesBinding;
import java.util.ArrayList;
import java.util.List;

public class RecipesFragment extends Fragment {

    private FragmentRecipesBinding binding;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipes = new ArrayList<>();
    private int i = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRecipesBinding.inflate(inflater, container, false);
        setupRecyclerView();
        return binding.getRoot();
    }

    private void setupRecyclerView() {
        recipes = createRecipes();
        recipeAdapter = new RecipeAdapter(recipes);
        binding.recyclerViewRecipes.setAdapter(recipeAdapter);
        binding.recyclerViewRecipes.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewRecipes.addItemDecoration(new MarginItemDecoration(16));

        ItemTouchHelper.SimpleCallback swipeHandler = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Recipe swipedRecipe = recipes.get(position);
                switch (direction) {
                    case ItemTouchHelper.LEFT:
                        Toast.makeText(getContext(), "Disliked: " + swipedRecipe.getName(), Toast.LENGTH_SHORT).show();
                        recipeAdapter.removeRecipe(position);
                        break;
                    case ItemTouchHelper.RIGHT:
                        Toast.makeText(getContext(), "Liked: " + swipedRecipe.getName(), Toast.LENGTH_SHORT).show();
                        recipeAdapter.removeRecipe(position);
                        break;
                }
                if (recipes.isEmpty()) {
                    List<String> ingredients = new ArrayList<>();
                    ingredients.add("Zutat " + i);
                    Recipe newRecipe = new Recipe(i, "Rezept " + i, null, ingredients, "Zubereitungstext " + i);
                    recipeAdapter.addRecipe(newRecipe);
                    i++;
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeHandler);
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewRecipes);
    }

    private List<Recipe> createRecipes() {
        List<Recipe> list = new ArrayList<>();
        List<String> ingredients = List.of("Zutat 1", "Zutat 2", "Zutat 3");
        String preparation = "Schritt 1...\nSchritt 2...\nSchritt 3...";
        list.add(new Recipe(0, "Rezept 1", null, ingredients, preparation));
        list.add(new Recipe(1, "Rezept 2", null, ingredients, preparation));
        return list;
    }

    private static class MarginItemDecoration extends RecyclerView.ItemDecoration {
        private final int margin;

        public MarginItemDecoration(int margin) {
            this.margin = margin;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = margin;
            }
            outRect.left = margin;
            outRect.right = margin;
            outRect.bottom = margin;
        }
    }
}