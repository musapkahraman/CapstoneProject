package com.example.kitchen.adapters;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.kitchen.R;
import com.example.kitchen.data.Recipe;

import java.util.ArrayList;
import java.util.List;

public class NotebookAdapter extends RecyclerView.Adapter<NotebookAdapter.RecipeCardViewHolder> {
    private final OnRecipeClickListener mRecipeClickListener;
    private List<Recipe> mRecipes;
    private List<Recipe> mFilteredRecipes;
    private final ArrayList<Recipe> mSelectedRecipes = new ArrayList<>();
    private boolean mMultiSelect = false;
    private final ActionMode.Callback mActionModeCallbacks = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mMultiSelect = true;
            menu.add(R.string.delete);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == 0) {
                for (Recipe recipe : mSelectedRecipes) {
                    mRecipes.remove(recipe);
                }
            }
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mMultiSelect = false;
            mSelectedRecipes.clear();
            notifyDataSetChanged();
        }
    };

    public NotebookAdapter(OnRecipeClickListener recipeClickListener) {
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
        View view = inflater.inflate(R.layout.item_notebook_card, parent, false);
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
        private final ImageView recipeImageView;
        private int recipeId;
        private String recipeName;
        private final CardView recipeCard;

        private RecipeCardViewHolder(View itemView) {
            super(itemView);
            recipeNameTextView = itemView.findViewById(R.id.tv_recipe_name);
            recipeImageView = itemView.findViewById(R.id.iv_card_recipe_image);
            recipeCard = itemView.findViewById(R.id.card_recipe);
            itemView.setOnClickListener(this);
        }

        private void bind(int position) {
            final Recipe current = mFilteredRecipes.get(position);
            recipeId = current.id;
            recipeName = current.title;
            recipeNameTextView.setText(recipeName);
            String url = current.photoUrl;
            if (url != null && url.length() != 0) {
                RequestOptions options = new RequestOptions();
                Glide.with(itemView).load(url).apply(options.centerCrop()).into(recipeImageView);
            }

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    ((AppCompatActivity) view.getContext()).startSupportActionMode(mActionModeCallbacks);
                    selectRecipe(current);
                    return true;
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectRecipe(current);
                }
            });

            if (mSelectedRecipes.contains(current)) {
                recipeCard.setBackgroundColor(itemView.getResources().getColor(R.color.selected_card_back));
            } else {
                recipeCard.setBackgroundColor(itemView.getResources().getColor(R.color.card_back));
            }

        }

        void selectRecipe(Recipe recipe) {
            if (mMultiSelect) {
                if (mSelectedRecipes.contains(recipe)) {
                    mSelectedRecipes.remove(recipe);
                    recipeCard.setBackgroundColor(itemView.getResources().getColor(R.color.card_back));
                } else {
                    mSelectedRecipes.add(recipe);
                    recipeCard.setBackgroundColor(itemView.getResources().getColor(R.color.selected_card_back));
                }
            }
        }

        @Override
        public void onClick(View v) {
            mRecipeClickListener.onRecipeClick(recipeId, recipeName);
        }
    }
}
