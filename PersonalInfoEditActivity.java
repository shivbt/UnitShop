package com.ikai.unitshop;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class PersonalInfoEditActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText sellerNameEditText;
    private FirebaseUser user;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info_edit);

        // Get reference to progress dialog.
        progressDialog = new ProgressDialog(this);

        // Get reference to seller edit text.
        sellerNameEditText = findViewById(R.id.seller_name_edit_text);

        // Get reference to firebase auth.
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // Set seller name into edit text.
        user = mAuth.getCurrentUser();
        if (user != null) {
            sellerNameEditText.setText(user.getDisplayName());
        }

        // Get reference to save and cancel button and set on click listeners.
        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(this);
        Button cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        // Check for save button click.
        if (id == R.id.save_button) {
            // Get seller name from edit text.
            String sellerName = sellerNameEditText.getText().toString();

            // Show loading dialog that we are saving seller name.
            showLoadingDialogue("Saving Data...");

            // Save it to firebase authentication database. Handle error and completion.
            UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest
                    .Builder()
                    .setDisplayName(sellerName)
                    .build();
            user.updateProfile(userProfileChangeRequest)
                    .addOnFailureListener( new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            // Dismiss loading dialog if showing.
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }

                            showErrorMessageDialogue("Some error just happened. " +
                                    "Please try again in a little bit.");

                        }
                    }).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    // Dismiss loading dialog if showing.
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                    // Finish this activity.
                    finish();

                }
            });
        }

        // Check for cancel button click.
        if (id == R.id.cancel_button) {
            // Here simply finish the activity.
            finish();
        }
    }

    // Helper function to show the loading dialogue.
    private void showLoadingDialogue(final String loadingMessage) {
        progressDialog.setMessage(loadingMessage);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(false);
        progressDialog.show();
    }

    // Helper function to show the error message.
    private void showErrorMessageDialogue(final String errorMessage) {
        ErrorMessageDialogue msgDialog = new ErrorMessageDialogue();
        Bundle bundle = new Bundle();
        bundle.putString("Message", errorMessage);
        msgDialog.setArguments(bundle);
        msgDialog.show(getSupportFragmentManager(), "Message");
    }

}
