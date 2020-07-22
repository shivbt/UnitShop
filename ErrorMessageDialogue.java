package com.ikai.unitshop;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by shiv on 15/11/17.
 */

public class ErrorMessageDialogue extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Initializing and setting rest of things
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.error_pop_up, null);
        TextView text_view = view.findViewById(R.id.error_message);
        Bundle bundle = getArguments();
        String message_string = bundle.getString("Message", "Some error has happened. " +
                "Please try again after some time");
        text_view.setText(message_string);
        builder.setView(view);
        setCancelable(false);

        // Setting positive button
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        return (builder.create());
    }
}
