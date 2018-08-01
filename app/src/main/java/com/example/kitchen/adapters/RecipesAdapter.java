package com.example.kitchen.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.kitchen.R;
import com.example.kitchen.data.local.entities.Recipe;

import java.util.ArrayList;
import java.util.List;

public class RecipesAdapter extends RecyclerView.Adapter<RecipesAdapter.RecipeCardViewHolder> {
    private final RecipeClickListener mRecipeClickListener;
    private List<Recipe> mRecipes;
    private List<Recipe> mFilteredRecipes;

    public RecipesAdapter(RecipeClickListener recipeClickListener) {
        mRecipeClickListener = recipeClickListener;
    }

    @Override
    public int getItemCount() {
        if (mFilteredRecipes != null) {
            return mFilteredRecipes.size();
        } else {
            return 0;
        }
    }

    @NonNull
    @Override
    public RecipeCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_recipe_card, parent, false);
        return new RecipeCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeCardViewHolder holder, int position) {
        if (mFilteredRecipes != null) {
            holder.bind(position);
        }
    }

    public void setRecipes(List<Recipe> recipes) {
        mRecipes = recipes;
        mFilteredRecipes = recipes;
        notifyDataSetChanged();
    }

    public void filter(CharSequence charSequence) {
        if (charSequence == null)
            return;
        String charString = charSequence.toString();
        if (!charString.isEmpty()) {
            List<Recipe> filteredList = new ArrayList<>();
            for (Recipe row : mRecipes) {
                if (row.title.toLowerCase().contains(charString.toLowerCase()) || row.writer.contains(charSequence)) {
                    filteredList.add(row);
                }
            }
            mFilteredRecipes = filteredList;
            notifyDataSetChanged();
        } else {
            setRecipes(mRecipes);
        }
    }

    public class RecipeCardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView recipeNameTextView;
        private final TextView cookTimeTextView;
        private final ImageView recipeImageView;
        private final RatingBar ratingBar;
        private Recipe current;

        private RecipeCardViewHolder(View itemView) {
            super(itemView);
            recipeNameTextView = itemView.findViewById(R.id.tv_recipe_name);
            cookTimeTextView = itemView.findViewById(R.id.tv_card_cook_time);
            recipeImageView = itemView.findViewById(R.id.iv_card_recipe_image);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            itemView.setOnClickListener(this);
        }

        private void bind(int position) {
            current = mFilteredRecipes.get(position);
            recipeNameTextView.setText(current.title);
            int totalTime = current.cookTime + current.prepTime;
            String cookTime = String.format(itemView.getResources().getString(R.string.minutes_abbreviation), totalTime);
            cookTimeTextView.setText(cookTime);
            ratingBar.setRating(current.rating);
            String url = current.photoUrl;
            if (url != null && url.length() != 0) {
                RequestOptions options = new RequestOptions();
                Glide.with(itemView).load(url).apply(options.centerCrop()).into(recipeImageView);
            }
        }

        @Override
        public void onClick(View v) {
            mRecipeClickListener.onRecipeClick(current, false);
        }
    }
}
