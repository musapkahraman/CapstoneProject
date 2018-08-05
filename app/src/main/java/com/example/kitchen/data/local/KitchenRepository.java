package com.example.kitchen.data.local;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.example.kitchen.data.local.daos.IngredientsDao;
import com.example.kitchen.data.local.daos.RecipesDao;
import com.example.kitchen.data.local.daos.StepsDao;
import com.example.kitchen.data.local.entities.Ingredient;
import com.example.kitchen.data.local.entities.Recipe;
import com.example.kitchen.data.local.entities.Step;

import java.util.List;

class KitchenRepository {
    // Define data access objects
    private final RecipesDao mRecipesDao;
    private final IngredientsDao mIngredientsDao;
    private final StepsDao mStepsDao;

    KitchenRepository(Application application) {
        KitchenDatabase db = KitchenDatabase.getDatabase(application);
        mRecipesDao = db.recipesDao();
        mIngredientsDao = db.ingredientsDao();
        mStepsDao = db.stepsDao();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.

    LiveData<Recipe> getRecipe(int id) {
        return mRecipesDao.getRecipe(id);
    }

    LiveData<List<Recipe>> getAllRecipes() {
        return mRecipesDao.getAll();
    }

    LiveData<List<Ingredient>> getIngredientsByRecipe(int recipeId) {
        return mIngredientsDao.getIngredientsByRecipe(recipeId);
    }

    LiveData<List<Ingredient>> getIngredientsByFood(int foodId) {
        return mIngredientsDao.getIngredientsByFood(foodId);
    }

    LiveData<List<Step>> getStepsByRecipe(int recipeId) {
        return mStepsDao.getStepsByRecipe(recipeId);
    }

    // Insert and delete methods need to be run on an asynchronous thread.

    public void insertRecipes(Recipe... recipes) {
        new InsertRecipeTask(mRecipesDao).execute(recipes);
    }

    public void deleteRecipes(Recipe... recipes) {
        new DeleteRecipeTask(mRecipesDao).execute(recipes);
    }

    public void insertIngredients(Ingredient... ingredients) {
        new InsertIngredientTask(mIngredientsDao).execute(ingredients);
    }

    public void deleteIngredients(Ingredient... ingredients) {
        new DeleteIngredientTask(mIngredientsDao).execute(ingredients);
    }

    public void insertSteps(Step... steps) {
        new InsertStepTask(mStepsDao).execute(steps);
    }

    public void deleteSteps(Step... steps) {
        new DeleteStepTask(mStepsDao).execute(steps);
    }

    private static class InsertRecipeTask extends AsyncTask<Recipe, Void, Void> {
        private final RecipesDao mAsyncTaskDao;

        InsertRecipeTask(RecipesDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Recipe... recipes) {
            for (Recipe recipe : recipes) {
                if (recipe != null)
                    mAsyncTaskDao.insertRecipe(recipe);
            }
            return null;
        }
    }

    private static class DeleteRecipeTask extends AsyncTask<Recipe, Void, Void> {
        private final RecipesDao mAsyncTaskDao;

        DeleteRecipeTask(RecipesDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Recipe... recipes) {
            for (Recipe recipe : recipes) {
                if (recipe != null)
                    mAsyncTaskDao.deleteRecipe(recipe);
            }
            return null;
        }
    }

    private static class InsertIngredientTask extends AsyncTask<Ingredient, Void, Void> {
        private final IngredientsDao mAsyncTaskDao;

        InsertIngredientTask(IngredientsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Ingredient... ingredients) {
            for (Ingredient ingredient : ingredients) {
                if (ingredient != null)
                    mAsyncTaskDao.insertIngredient(ingredient);
            }
            return null;
        }
    }

    private static class DeleteIngredientTask extends AsyncTask<Ingredient, Void, Void> {
        private final IngredientsDao mAsyncTaskDao;

        DeleteIngredientTask(IngredientsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Ingredient... ingredients) {
            for (Ingredient ingredient : ingredients) {
                if (ingredient != null)
                    mAsyncTaskDao.deleteIngredient(ingredient);
            }
            return null;
        }
    }

    private static class InsertStepTask extends AsyncTask<Step, Void, Void> {
        private final StepsDao mAsyncTaskDao;

        InsertStepTask(StepsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Step... steps) {
            for (Step step : steps) {
                if (step != null)
                    mAsyncTaskDao.insertStep(step);
            }
            return null;
        }
    }

    private static class DeleteStepTask extends AsyncTask<Step, Void, Void> {
        private final StepsDao mAsyncTaskDao;

        DeleteStepTask(StepsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Step... steps) {
            for (Step step : steps) {
                if (step != null)
                    mAsyncTaskDao.deleteStep(step);
            }
            return null;
        }
    }
}