package com.example.kitchen.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.example.kitchen.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

public class PictureDialogFragment extends DialogFragment {

    private OnPictureDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnPictureDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnPictureDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_photo_select).setItems(R.array.photo_options, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        mListener.onSelectCamera();
                        break;
                    case 1:
                        mListener.onSelectGallery();
                        break;
                }
            }
        });
        return builder.create();
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
