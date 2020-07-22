package com.ikai.unitshop;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ShopInfoEditActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText shopNameEdit, mobNumberEdit, shopAddressEdit, pinCodeEdit;
    private Seller sellerDetails;
    private DatabaseReference mUserRef;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_info_edit);

        // Get reference to progress dialog.
        progressDialog = new ProgressDialog(this);

        // Get reference to all edit texts.
        shopNameEdit = findViewById(R.id.shop_name_edit_text);
        mobNumberEdit = findViewById(R.id.mobile_number_edit_text);
        shopAddressEdit = findViewById(R.id.shop_address_edit_text);
        pinCodeEdit = findViewById(R.id.pin_code_edit_text);

        // Get reference to firebase database reference.
        mUserRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("UNiTSellers")
                .child(FirebaseAuth.getInstance().getUid());

        // Fill previous values into their respective field.
        fillPreviousValues();

        // Get reference to save and cancel button and set on click listener.
        Button saveButton = findViewById(R.id.save_button);
        Button cancelButton = findViewById(R.id.cancel_button);
        saveButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

    }

    // Helper function to fill previous values to their respective fields.
    private void fillPreviousValues() {

        // Get values from firebase database and fill respective field.
        mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    sellerDetails = dataSnapshot.getValue(Seller.class);
                    if (sellerDetails != null) {

                        // Set the values.
                        shopNameEdit.setText(sellerDetails.shopName);
                        mobNumberEdit.setText(sellerDetails.sellerMobNumber);
                        shopAddressEdit.setText(sellerDetails.shopAddress);
                        pinCodeEdit.setText(sellerDetails.shopPinCode);

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        // Check for save button click.
        if (id == R.id.save_button) {

            // Show loading dialog while updating the seller data.
            showLoadingDialogue("Saving the data...");

            // Get everything from edit text and save it to database.
            // First create a map and insert all data in it.
            Map<String, Object> newSellerData = new HashMap<>();
            newSellerData.put("shopName", shopNameEdit.getText().toString());
            newSellerData.put("sellerMobNumber", mobNumberEdit.getText().toString());
            newSellerData.put("shopAddress", shopAddressEdit.getText().toString());
            newSellerData.put("shopPinCode", pinCodeEdit.getText().toString());

            // Now update the database atomically.
            mUserRef.updateChildren(newSellerData, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError,
                                       DatabaseReference databaseReference) {

                    // Dismiss the progress dialog if showing and finish this activity.
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                    // Check whether there is some error or not. If yes then show it to
                    // seller and if no then simply exit.
                    if (databaseError == null) {
                        finish();
                    } else {
                        // Show error why updation is canceled / failed.
                        showDatabaseFetchError(databaseError);
                    }

                }
            });

        }

        // Check for cancel button click.
        if (id == R.id.cancel_button) {
            // Simply finish this activity.
            finish();
        }

    }

    // Helper function to show error occurred during database fetching.
    private void showDatabaseFetchError(DatabaseError databaseError) {
        switch (databaseError.getCode()) {
            case DatabaseError.DISCONNECTED:
                showErrorMessageDialogue("Error in saving the data. " +
                        "You are disconnected. Please try again in a little bit");
                break;

            case DatabaseError.NETWORK_ERROR:
                showErrorMessageDialogue("Error in saving tha data. " +
                        "Please make sure you have internet connection.");
                break;

            case DatabaseError.UNAVAILABLE:
                showErrorMessageDialogue("Error in saving the data. " +
                        "Server is unavailable. We are sorry for that. Try " +
                        "again in a little bit");
                break;

            default:
                showErrorMessageDialogue("Error in saving the data. " +
                        "Some unknown error has just happened. Please try " +
                        "again in a little bit");
        }
    }

    // Helper function to show the error message.
    private void showErrorMessageDialogue(final String errorMessage) {
        ErrorMessageDialogue msgDialog = new ErrorMessageDialogue();
        Bundle bundle = new Bundle();
        bundle.putString("Message", errorMessage);
        msgDialog.setArguments(bundle);
        msgDialog.show(getSupportFragmentManager(), "Message");
    }

    // Helper function to show the loading dialogue.
    private void showLoadingDialogue(final String loadingMessage) {
        progressDialog.setMessage(loadingMessage);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(false);
        progressDialog.show();
    }
}
