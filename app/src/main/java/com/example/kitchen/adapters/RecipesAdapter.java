package com.example.kitchen.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.kitchen.R;
import com.example.kitchen.data.local.entities.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class RecipesAdapter extends RecyclerView.Adapter<RecipesAdapter.RecipeCardViewHolder> {
    private final OnRecipeClickListener mRecipeClickListener;
    private List<Recipe> mRecipes;
    private List<Recipe> mFilteredRecipes;

    public RecipesAdapter(OnRecipeClickListener recipeClickListener) {
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
        if (charSequence == null || mRecipes == null)
            return;
        String charString = charSequence.toString();
        if (!charString.isEmpty()) {
            List<Recipe> filteredList = new ArrayList<>();
            for (Recipe row : mRecipes) {
                if (row.title.toLowerCase().contains(charString.toLowerCase()) || row.writerUid.contains(charSequence)) {
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
        @BindView(R.id.tv_recipe_title) TextView recipeNameTextView;
        @BindView(R.id.tv_card_cook_time) TextView cookTimeTextView;
        @BindView(R.id.iv_card_recipe_image) ImageView recipeImageView;
        @BindView(R.id.ratingBar) RatingBar ratingBar;
        private Recipe mRecipe;

        private RecipeCardViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        private void bind(int position) {
            mRecipe = mFilteredRecipes.get(position);
            recipeNameTextView.setText(mRecipe.title);
            int totalTime = mRecipe.cookTime + mRecipe.prepTime;
            String cookTime = String.format(itemView.getResources().getString(R.string.minutes_abbreviation), totalTime);
            cookTimeTextView.setText(cookTime);
            String url = mRecipe.imagePath;
            if (url != null && url.length() != 0) {
                RequestOptions options = new RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.ingredients)
                        .error(R.drawable.ingredients)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .priority(Priority.HIGH);
                Glide.with(itemView)
                        .load(url)
                        .apply(options)
                        .into(recipeImageView);
            }
            ratingBar.setRating(mRecipe.rating);
        }

        @Override
        public void onClick(View v) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            boolean isEditable = false;
            if (mRecipe.writerUid != null && user != null) {
                isEditable = mRecipe.writerUid.equals(user.getUid());
            }
            mRecipeClickListener.onRecipeClick(mRecipe, isEditable);
        }
    }
}
