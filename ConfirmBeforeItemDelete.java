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
 * Created by shiv on 23/11/17.
 */

public class ConfirmBeforeItemDelete extends DialogFragment {
    Communicator communicator;
    private int position;

    private int recyclerViewId = -1;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Initializing and setting rest of things
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.error_pop_up, null);
        TextView text_view = view.findViewById(R.id.error_message);
        TextView title_view = view.findViewById(R.id.title);

        // Get passed argument.
        Bundle bundle = getArguments();

        // Get message and title string and everything from bundle and set the
        // text views accordingly.
        String temp = bundle.getString("Message", "Are you sure?");
        text_view.setText(temp);
        temp = bundle.getString("Title", "Action");
        title_view.setText(temp);
        position = bundle.getInt("Position");
        recyclerViewId = bundle.getInt("RecyclerViewId", -1);

        // Make a reference to callee activity/ fragment.
        String fragmentTag = bundle.getString("FragmentTag");
        if (fragmentTag != null) {
            communicator = ((Communicator) getFragmentManager()
                    .findFragmentByTag(fragmentTag));
        } else {
            communicator = ((Communicator) getActivity());
        }

        // Set the builder.
        builder.setView(view);

        // Make cancelable to false. So that out of region click will not close the dialogue.
        setCancelable(false);

        // Setting positive and negative buttons and handle clicking.
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                communicator.communicate(position, recyclerViewId);
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
            }
        });

        return (builder.create());
    }

    public interface Communicator {
        void communicate(final int position, final int recyclerViewId);
    }
}
