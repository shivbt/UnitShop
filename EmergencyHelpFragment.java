package com.ikai.unitshop;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * Created by shiv on 22/11/17.
 */

public class EmergencyHelpFragment extends Fragment {

    private static final int CALL_PHONE_PERMISSION_REQUEST = 73;
    private static final int REQUEST_PERMISSION_SETTING = 76;
    private final String PHONE_NUMBER = "7001596413";
    private Activity activityContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate view here and return it.
        View view = inflater.inflate(R.layout.emergency_help_fragment, container, false);

        // Get activity context.
        activityContext = getActivity();

        // Get reference to call and message floating action buttons.
        FloatingActionButton phoneButton, messageButton;
        phoneButton = view.findViewById(R.id.phone_icon);
        messageButton = view.findViewById(R.id.message_icon);

        // Set on click listener for phone button.
        phoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Make a call to given number by launching phone app.
                // But first check that permission is granted or not.
                checkCallPermissionAndMakeCall();
            }
        });

        // Set on click listener for message button.
        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Make a message to given number by launching message app.
                // Here first check that permission is granted or not.
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse("sms:"));
                sendIntent.putExtra("address", PHONE_NUMBER);
                startActivity(sendIntent);
            }
        });

        return view;
    }

    // Helper function to check whether call permission is given by the user and make call
    // accordingly.
    private void checkCallPermissionAndMakeCall() {

        // Get reference to sharedPrefManager. We will use it later.
        SharedPreferences permissionStatus = activityContext.getSharedPreferences(
                "permissionStatus", Context.MODE_PRIVATE
        );

        // Check whether CALL_PHONE permission is granted or not. And perform call accordingly.
        if (ActivityCompat.checkSelfPermission(activityContext, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(activityContext,
                    Manifest.permission.CALL_PHONE)) {

                //Show a dialog showing the message fro need of this permission.
                showNeedDialogAndAskPermission();

            } else if (permissionStatus.getBoolean(Manifest.permission.CALL_PHONE, false)) {

                // Previously Permission Request was cancelled with 'Don't Ask Again',
                // Redirect to Settings after showing Information about why you need the
                // permission.
                AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
                builder.setTitle("Allow UNiT to access phone?");
                builder.setMessage("This lets UNiT to Call our team for your help.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", activityContext.getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        Toast.makeText(activityContext, "Go to Permissions to Grant Phone access",
                                Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();

            } else {

                // Here just request the permission
                ActivityCompat.requestPermissions(activityContext,
                        new String[]{Manifest.permission.CALL_PHONE},
                        CALL_PHONE_PERMISSION_REQUEST
                );

            }

            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(Manifest.permission.CALL_PHONE, true);
            editor.apply();

        } else {

            //You already have the permission, so make the call.
            makeCall();
        }
    }

    // Helper function to show a dialog showing the need of phone permission.
    private void showNeedDialogAndAskPermission() {

        AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
        builder.setTitle("Allow UNiT to access phone?");
        builder.setMessage("This lets UNiT to Call our team for your help.");
        builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                ActivityCompat.requestPermissions(activityContext,
                        new String[]{Manifest.permission.CALL_PHONE},
                        CALL_PHONE_PERMISSION_REQUEST
                );
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check for CALL_PHONE_PERMISSION_REQUEST request code.
        if (requestCode == CALL_PHONE_PERMISSION_REQUEST) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Call permission is granted, so make tha call.
                makeCall();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activityContext,
                        Manifest.permission.CALL_PHONE)) {

                    //Show a dialog showing the message fro need of this permission.
                    showNeedDialogAndAskPermission();

                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check whether user has given permission through setting. For this first check
        // request code we have sent to settings.
        if (requestCode == REQUEST_PERMISSION_SETTING) {

            // Now check for permission granted.
            if (ActivityCompat.checkSelfPermission(activityContext, Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED) {
                // Now we got the permission, so make the call.
                makeCall();
            }
        }

    }

    // Helper function to make call to given number.
    private void makeCall() {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + PHONE_NUMBER));
        startActivity(callIntent);
    }
}
