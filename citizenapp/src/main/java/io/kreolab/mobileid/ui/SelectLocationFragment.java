/*
 * Copyright (c) 2018.  citizenapp project
 *
 */

package io.kreolab.mobileid.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class SelectLocationFragment extends DialogFragment implements DialogInterface.OnClickListener {

    public static String STATE_KEY_ARRAY ="REGISTRATION_STATES_KEYS_ARRAY";

    private OnItemSelectedListener mOnItemSelectedListener;
    private String[] listOfStates;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mOnItemSelectedListener = (OnItemSelectedListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement OnItemSelectedListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        listOfStates =  getArguments().getStringArray(STATE_KEY_ARRAY);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
        alertBuilder.setTitle("Select location")
                .setItems(listOfStates, this);

        return alertBuilder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

        mOnItemSelectedListener.locationSelected(listOfStates[which]);
    }


    interface OnItemSelectedListener {
        void locationSelected(String location);
    }

}
