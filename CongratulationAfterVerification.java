package com.ikai.unitshop;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

/**
 * Created by shiv on 16/11/17.
 */

public class CongratulationAfterVerification extends DialogFragment {

    Communicator communicator;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        communicator = ((Communicator) getActivity());

        // Inflate the congrates layout and show the dialogue.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.congrates_after_verification_pop_up, null);

        // Load gif using Glide Library.
        ImageView gifImageView = view.findViewById(R.id.verified_gif);
        Glide.with(this).asGif().load(R.drawable.account_verified).into(gifImageView);

        builder.setView(view);
        setCancelable(false);

        // Setting positive button
        builder.setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                communicator.communicate();
                dismiss();
            }
        });
        return (builder.create());
    }

    interface Communicator {
        void communicate();
    }
}
