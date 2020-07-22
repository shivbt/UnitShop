package com.ikai.unitshop;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountDetailsEditActivity extends AppCompatActivity {

    private String bankName, accountHolder, accountNumber, IFSCCode, aadhaarNumber,
            UPIVPA, paytm;
    private DatabaseReference mDatabaseReference;
    private SharedPrefManager sharedPrefManager;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details_edit);

        // Set back arrow on action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        // Get reference to firebase database.
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // Make a reference to sharedPrefManager which is used for tracking.
        sharedPrefManager = new SharedPrefManager(this);

        // Get reference to save button. So we can attach listener to it.
        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (allInputsValid()) {
                    // Save everything to firebase and show the account details filled by
                    // the user in non editable mode.
                    showLoadingDialogue("Saving account details...");
                    saveAccountDetailsInFirebase();
                    // Now dismiss the progress dialogue.
                    progressDialog.dismiss();
                    Intent intent = new Intent(AccountDetailsEditActivity.this,
                            AccountDetailsNoEditActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                    finish();
                }
            }
        });

    }

    // Helper function to show the loading dialogue.
    private void showLoadingDialogue(final String loadingMeassage) {
        progressDialog = new ProgressDialog(AccountDetailsEditActivity.this);
        progressDialog.setMessage(loadingMeassage);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(false);
        progressDialog.show();
    }

    private void saveAccountDetailsInFirebase() {
        // Implement firebase saving code here.
        AccountDetails accountDetails = new AccountDetails(bankName, accountHolder, accountNumber,
                IFSCCode, aadhaarNumber, UPIVPA, paytm);
        mDatabaseReference.child("UNiTSellers").child(FirebaseAuth.getInstance().getUid())
                .child("accountDetails").setValue(accountDetails).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                     sharedPrefManager.setAccountDetailsFilled();
//                        Log.d("Shiv", "Success");
                    }
                }
        ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showErrorMessageDialogue("You do not have internet connection. Please connect" +
                        " with mobile data or wifi network");
            }
        });

    }

    private boolean allInputsValid() {
        bankName = ((EditText) findViewById(R.id.bank_name_edit_text)).getText().toString();
        accountHolder = ((EditText) findViewById(R.id.account_holder_edit_text)).getText()
                .toString();
        accountNumber = ((EditText) findViewById(R.id.account_number_edit_text)).getText()
                .toString();
        IFSCCode = ((EditText) findViewById(R.id.ifsc_code_edit_text)).getText().toString();
        aadhaarNumber = ((EditText) findViewById(R.id.aadhaar_number_edit_text)).getText()
                .toString();
        UPIVPA = ((EditText) findViewById(R.id.upi_vpa_edit_text)).getText().toString();
        paytm = ((EditText) findViewById(R.id.paytm_number_edit_text)).getText().toString();

        // Validate the inputs.
        if ((bankName.length() > 4) && (accountHolder.length() > 2) && (accountNumber.length() > 9)
                && (IFSCCode.length() == 11) && (aadhaarNumber.length() == 12)) {
            return true;
        }

        showErrorMessageDialogue("Please enter all input fields correctly!");
        return false;
    }

    // Helper function to show the error message.
    private void showErrorMessageDialogue(final String errorMessage) {
        ErrorMessageDialogue msgDialog = new ErrorMessageDialogue();
        Bundle bundle = new Bundle();
        bundle.putString("Message", errorMessage);
        msgDialog.setArguments(bundle);
        msgDialog.show(getSupportFragmentManager(), "Message");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Finish the activity when user presses back arrow.
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }
}
