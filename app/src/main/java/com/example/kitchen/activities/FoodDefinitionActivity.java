package com.example.kitchen.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.kitchen.R;
import com.example.kitchen.data.firebase.FoodViewModel;
import com.example.kitchen.utility.CheckUtils;
import com.example.kitchen.utility.MeasurementUtils;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FoodDefinitionActivity extends AppCompatActivity {
    @BindView(R.id.text_edit_food_name) TextInputEditText mFoodNameView;
    @BindView(R.id.text_edit_volume_amount) TextInputEditText mVolumeAmountView;
    @BindView(R.id.text_edit_weight_amount) TextInputEditText mWeightAmountView;
    @BindView(R.id.spinner_volume) Spinner mVolumeSpinner;
    @BindView(R.id.spinner_weight) Spinner mWeightSpinner;
    @BindView(R.id.tv_equals) TextView mEqualsLabel;
    @BindView(R.id.check_countable) CheckBox mCountableCheckBox;
    @BindView(R.id.btn_link_define_food) Button mAddFoodButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_definition);
        ButterKnife.bind(this);
        // Set a spinner with volume types.
        ArrayAdapter<CharSequence> volumeAdapter = ArrayAdapter.createFromResource(this,
                R.array.volume_array, android.R.layout.simple_spinner_item);
        volumeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mVolumeSpinner.setAdapter(volumeAdapter);
        // Set a spinner with weight types.
        ArrayAdapter<CharSequence> weightAdapter = ArrayAdapter.createFromResource(this,
                R.array.weight_array, android.R.layout.simple_spinner_item);
        weightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mWeightSpinner.setAdapter(weightAdapter);
        // Define the food either as countable or not.
        mCountableCheckBox.setOnCheckedChangeListener(getCountableCheckBoxOnCheckedChangeListener());
        // Add the food into firebase real time database.
        mAddFoodButton.setOnClickListener(getAddButtonOnClickListener());
    }

    private CompoundButton.OnCheckedChangeListener getCountableCheckBoxOnCheckedChangeListener() {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mVolumeAmountView.setVisibility(View.GONE);
                    mWeightAmountView.setVisibility(View.GONE);
                    mVolumeSpinner.setVisibility(View.GONE);
                    mWeightSpinner.setVisibility(View.GONE);
                    mEqualsLabel.setVisibility(View.GONE);
                } else {
                    mVolumeAmountView.setVisibility(View.VISIBLE);
                    mWeightAmountView.setVisibility(View.VISIBLE);
                    mVolumeSpinner.setVisibility(View.VISIBLE);
                    mWeightSpinner.setVisibility(View.VISIBLE);
                    mEqualsLabel.setVisibility(View.VISIBLE);
                }
            }
        };
    }

    private View.OnClickListener getAddButtonOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CheckUtils.isEmptyEditText(FoodDefinitionActivity.this, mFoodNameView))
                    return;
                String foodName = CheckUtils.validateTitle(mFoodNameView.getText().toString());
                mFoodNameView.setText(foodName);
                float conversionMultiplier = 0;
                if (!mCountableCheckBox.isChecked()) {
                    int volumeAmount = CheckUtils.
                            getNonZeroPositiveIntegerFromField(
                                    FoodDefinitionActivity.this, mVolumeAmountView);
                    if (volumeAmount == -1) return;
                    int weightAmount = CheckUtils
                            .getNonZeroPositiveIntegerFromField(
                                    FoodDefinitionActivity.this, mWeightAmountView);
                    if (weightAmount == -1) return;
                    int volumeType = mVolumeSpinner.getSelectedItemPosition();
                    int weightType = mWeightSpinner.getSelectedItemPosition();
                    conversionMultiplier = MeasurementUtils.getConversionMultiplier(
                            FoodDefinitionActivity.this,
                            volumeAmount, volumeType, weightAmount, weightType);
                }
                FoodViewModel foodViewModel = ViewModelProviders
                        .of(FoodDefinitionActivity.this).get(FoodViewModel.class);
                foodViewModel.addFood(foodName, conversionMultiplier);
                Snackbar.make(mFoodNameView,
                        String.format(getString(R.string.food_added), foodName), Snackbar.LENGTH_SHORT)
                        .show();
            }
        };
    }
}