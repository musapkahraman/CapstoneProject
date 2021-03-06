package com.example.kitchen.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.kitchen.R;
import com.example.kitchen.adapters.OnRecipeClickListener;
import com.example.kitchen.adapters.OnShoppingListClickListener;
import com.example.kitchen.adapters.OnStorageClickListener;
import com.example.kitchen.data.firebase.RecipeViewModel;
import com.example.kitchen.data.firebase.models.RecipeModel;
import com.example.kitchen.data.local.KitchenViewModel;
import com.example.kitchen.data.local.entities.Food;
import com.example.kitchen.data.local.entities.Recipe;
import com.example.kitchen.data.local.entities.Ware;
import com.example.kitchen.fragments.BookmarksFragment;
import com.example.kitchen.fragments.OnFragmentScrollListener;
import com.example.kitchen.fragments.RecipesFragment;
import com.example.kitchen.fragments.ShoppingFragment;
import com.example.kitchen.fragments.StorageFragment;
import com.example.kitchen.utility.AppConstants;
import com.example.kitchen.utility.DeviceUtils;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnRecipeClickListener,
        OnStorageClickListener, OnShoppingListClickListener, OnFragmentScrollListener,
        DeviceUtils.InternetConnectionListener {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String KEY_NAV_INDEX = "navigator-index-key";
    private static final String KEY_ACTIVITY_TITLE = "activity-title-key";
    private static final int INDEX_RECIPES = 0;
    private static final int INDEX_BOOKMARKS = 1;
    private static final int INDEX_STORAGE = 2;
    private static final int INDEX_SHOPPING_LIST = 3;
    @BindView(R.id.fab) FloatingActionButton mFab;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_view) NavigationView mNavigationView;
    @BindView(R.id.progress_bar_recipes_fragment) ProgressBar mProgressBar;
    private KitchenViewModel mKitchenViewModel;
    private RecipeViewModel mRecipeViewModel;
    private SharedPreferences mSharedPreferences;
    private String mActivityTitle;
    private int mSelectionIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mKitchenViewModel = ViewModelProviders.of(this).get(KitchenViewModel.class);
        mRecipeViewModel = ViewModelProviders.of(this).get(RecipeViewModel.class);
        mSharedPreferences = getPreferences(Context.MODE_PRIVATE);
        if (savedInstanceState == null) {
            boolean isStartedByAppWidget = getIntent()
                    .getBooleanExtra(AppConstants.EXTRA_APP_WIDGET, false);
            if (isStartedByAppWidget) {
                mSelectionIndex = INDEX_SHOPPING_LIST;
            } else {
                mSelectionIndex = mSharedPreferences.getInt(KEY_NAV_INDEX, 0);
            }
            showSelectedContent();
        } else {
            mSelectionIndex = savedInstanceState.getInt(KEY_NAV_INDEX);
            mActivityTitle = savedInstanceState.getString(KEY_ACTIVITY_TITLE);
            if (getSupportActionBar() != null) getSupportActionBar().setTitle(mActivityTitle);
        }
        mFab.setOnClickListener(getFabClickListener());
        // Set navigation drawer.
        ActionBarDrawerToggle toggle = getDrawerListener();
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        mNavigationView.setNavigationItemSelectedListener(this);
        // Show user image, name and email address in navigation header.
        View navHeader = mNavigationView.getHeaderView(0);
        ImageView userImage = navHeader.findViewById(R.id.iv_nav_header);
        TextView userName = navHeader.findViewById(R.id.tv_nav_header_title);
        TextView userEmail = navHeader.findViewById(R.id.tv_nav_header_subtitle);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Uri uri = user.getPhotoUrl();
            if (uri != null) {
                Glide.with(this).load(user.getPhotoUrl()).into(userImage);
            } else {
                userImage.setVisibility(View.GONE);
            }
            userName.setText(user.getDisplayName());
            userEmail.setText(user.getEmail());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_NAV_INDEX, mSelectionIndex);
        outState.putString(KEY_ACTIVITY_TITLE, mActivityTitle);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        int lastIndex = mSelectionIndex;
        switch (id) {
            case R.id.nav_recipes:
                mSelectionIndex = INDEX_RECIPES;
                break;
            case R.id.nav_bookmarks:
                mSelectionIndex = INDEX_BOOKMARKS;
                break;
            case R.id.nav_storage:
                mSelectionIndex = INDEX_STORAGE;
                break;
            case R.id.nav_shopping_list:
                mSelectionIndex = INDEX_SHOPPING_LIST;
                break;
            case R.id.nav_logout:
                final Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // User is now signed out.
                                startActivity(intent);
                                finish();
                            }
                        });
                break;
        }
        if (mSelectionIndex != lastIndex) {
            mFab.hide();
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    @Override
    public void onRecipeClick(Recipe recipe, boolean isEditable) {
        if (recipe == null)
            return;
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(AppConstants.EXTRA_RECIPE, recipe);
        intent.putExtra(AppConstants.EXTRA_EDITABLE, isEditable);
        startActivity(intent);
    }

    @Override
    public void onStorageItemClick(Food food) {
        Log.v(LOG_TAG, "Clicked on: " + food.name);
    }

    @Override
    public void onShoppingListItemClick(Ware ware) {
        Log.v(LOG_TAG, "Clicked on: " + ware.name);
    }

    @Override
    public void onScrollDown() {
        mFab.hide();
    }

    @Override
    public void onScrollUp() {
        mFab.show();
    }

    @Override
    public void onConnectionTestResult(boolean success) {
        if (!success)
            Snackbar.make(mToolbar, R.string.connect_internet_try_again, Snackbar.LENGTH_LONG).show();
    }

    private void showSelectedContent() {
        mProgressBar.setVisibility(View.GONE);
        switch (mSelectionIndex) {
            case INDEX_RECIPES:
                mFab.show();
                mProgressBar.setVisibility(View.VISIBLE);
                fetchRecipes();
                break;
            case INDEX_BOOKMARKS:
                mFab.show();
                mKitchenViewModel.getRecipes().observe(this, new Observer<List<Recipe>>() {
                    @Override
                    public void onChanged(@Nullable List<Recipe> recipes) {
                        if (recipes != null)
                            showBookmarks(recipes);
                    }
                });
                break;
            case INDEX_STORAGE:
                mFab.show();
                showStorage();
                break;
            case INDEX_SHOPPING_LIST:
                mFab.show();
                showShoppingList();
                break;
        }
    }

    private void fetchRecipes() {
        DeviceUtils.startConnectionTest(this);
        // Fetch recipe data from firebase.
        mRecipeViewModel.getDataSnapshotLiveData().observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                // Prevent turning back to online recipes fragment after publishing a recipe.
                if (mSelectionIndex != 0) return;
                // Translate data from snapshot data set into local data set.
                List<Recipe> localRecipes = new ArrayList<>();
                if (dataSnapshot != null) {
                    for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                        RecipeModel fetchedRecipe = null;
                        try {
                            fetchedRecipe = recipeSnapshot.getValue(RecipeModel.class);
                        } catch (DatabaseException e) {
                            Log.e(LOG_TAG, e.getMessage());
                        }
                        if (fetchedRecipe != null) {
                            float total = (float) fetchedRecipe.totalRating;
                            float count = (float) fetchedRecipe.ratingCount;
                            float rating = count != 0 ? total / count : 0;
                            localRecipes.add(new Recipe(0, fetchedRecipe.title,
                                    fetchedRecipe.imageUrl, fetchedRecipe.prepTime,
                                    fetchedRecipe.cookTime, fetchedRecipe.language,
                                    fetchedRecipe.cuisine, fetchedRecipe.course,
                                    fetchedRecipe.writerUid, fetchedRecipe.writerName,
                                    fetchedRecipe.servings, 0,
                                    recipeSnapshot.getKey(), rating));
                        }
                    }
                }
                // Sort the list in descending order by rating.
                Collections.sort(localRecipes, new Comparator<Recipe>() {
                    @Override
                    public int compare(Recipe o1, Recipe o2) {
                        return (int) (o2.rating - o1.rating);
                    }
                });
                showRecipes(localRecipes);
            }
        });
    }

    private void showRecipes(List<Recipe> recipes) {
        mProgressBar.setVisibility(View.GONE);
        for (Recipe recipe : recipes) Log.v(LOG_TAG, recipe.toString());
        mActivityTitle = getString(R.string.recipes);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(mActivityTitle);
        Bundle args = new Bundle();
        args.putParcelableArrayList(AppConstants.KEY_RECIPES, (ArrayList<Recipe>) recipes);
        RecipesFragment recipesFragment = new RecipesFragment();
        recipesFragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, recipesFragment).commit();
    }

    private void showBookmarks(List<Recipe> recipes) {
        for (Recipe recipe : recipes) Log.v(LOG_TAG, recipe.toString());
        mActivityTitle = getString(R.string.bookmarks);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(mActivityTitle);
        Bundle args = new Bundle();
        args.putParcelableArrayList(AppConstants.KEY_RECIPES, (ArrayList<Recipe>) recipes);
        BookmarksFragment bookmarksFragment = new BookmarksFragment();
        bookmarksFragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, bookmarksFragment).commit();
    }

    private void showStorage() {
        mActivityTitle = getString(R.string.storage);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(mActivityTitle);
        StorageFragment storageFragment = new StorageFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, storageFragment).commit();
    }

    private void showShoppingList() {
        mActivityTitle = getString(R.string.shopping_list);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(mActivityTitle);
        ShoppingFragment shoppingFragment = new ShoppingFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, shoppingFragment).commit();
    }

    private View.OnClickListener getFabClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                switch (mSelectionIndex) {
                    case INDEX_RECIPES:
                    case INDEX_BOOKMARKS:
                        intent = new Intent(MainActivity.this, RecipeEditActivity.class);
                        startActivity(intent);
                        break;
                    case INDEX_STORAGE:
                        intent = new Intent(MainActivity.this, StorageAddActivity.class);
                        startActivity(intent);
                        break;
                    case INDEX_SHOPPING_LIST:
                        intent = new Intent(MainActivity.this, ShoppingAddActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        };
    }

    private ActionBarDrawerToggle getDrawerListener() {
        return new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // Save index in local storage to be able to start from the point where the user
                // leaves the app.
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt(KEY_NAV_INDEX, mSelectionIndex);
                editor.apply();
                showSelectedContent();
            }
        };
    }
}
