package com.ikai.unitshop;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountDetailsNoEditActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details_no_edit);

        // Set back arrow on action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        // Get reference to progress dialog and show it while fetching the account details.
        progressDialog = new ProgressDialog(AccountDetailsNoEditActivity.this);
        showLoadingDialogue("Fetching account details...");

        // Get reference to firebase real-time database.
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // Fetch account details from database.
        fetchAccountDetailsAndShow();

        // Handle edit icon click. When this is clicked show editable account details activity.
        ImageView editIcon = findViewById(R.id.edit_icon);
        editIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AccountDetailsNoEditActivity.this,
                        AccountDetailsEditActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter, R.anim.exit);
                finish();
            }
        });
    }

    // Helper function to fetch account details and show it to user.
    private void fetchAccountDetailsAndShow() {
        mDatabaseReference = mDatabaseReference.child("UNiTSellers")
                .child(FirebaseAuth.getInstance().getUid()).child("accountDetails");
        mDatabaseReference.keepSynced(true);
        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            boolean error = true;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Check whether we have got data or not.
                if (dataSnapshot != null) {
                    AccountDetails accountDetails = dataSnapshot.getValue(AccountDetails.class);
                    if (accountDetails != null) {
                        error = false;
                        fillAccountDetailsInUI(accountDetails);
                    }
                }

                // Dismiss the progress dialogue if it is showing.
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                // Show the error dialogue if some error has happened.
                if (error) {
                    showErrorMessageDialogue("Snap! Some error has just happened during " +
                            "getting the account details. Please try agian after in a" +
                            " little bit.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                switch (databaseError.getCode()) {
                    case DatabaseError.NETWORK_ERROR:
                        showErrorMessageDialogue("Make sure you have internet connection " +
                                "through mobile data or wifi.");break;
                    case DatabaseError.DISCONNECTED:
                        showErrorMessageDialogue("We are disconnected. Please try again in" +
                                " a little bit.");break;
                    default: showErrorMessageDialogue("Snap! Some error has just happened during " +
                            "getting the account details. Please try again in a little bit.");
                }
            }
        });
    }

    // Helper function to fill account details in UI.
    private void fillAccountDetailsInUI(AccountDetails accountDetails) {
        // Get reference to every text view and then allot details to it.
        TextView bankName = findViewById(R.id.bank_name_text);
        TextView accountHolder = findViewById(R.id.holder_name_text);
        TextView accountNumber = findViewById(R.id.account_no_text);
        TextView ifscCode = findViewById(R.id.ifsc_code_text);
        TextView aadhaarNumber = findViewById(R.id.aadhaar_no_text);
        TextView upiVPA = findViewById(R.id.upi_vpa_text);
        TextView paytmNumber = findViewById(R.id.paytm_no_text);

        // Set account detail text fields.
        bankName.setText(accountDetails.bankName);
        accountHolder.setText(accountDetails.accountHolder);
        accountNumber.setText(accountDetails.accountNumber);
        ifscCode.setText(accountDetails.IFSCCode);
        aadhaarNumber.setText(accountDetails.aadhaarNumber);
        String upiVPAString = accountDetails.UPIVPA;
        String paytm = accountDetails.paytm;

        // Check whether user has given upi and paytm.
        if (upiVPAString != null) {
            upiVPA.setText(upiVPAString);
        }
        if (paytm != null) {
            paytmNumber.setText(paytm);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    // Helper function to show the loading dialogue.
    private void showLoadingDialogue(final String loadingMeassage) {
        progressDialog.setMessage(loadingMeassage);
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
