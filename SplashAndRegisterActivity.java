package com.ikai.unitshop;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

// Firebase imports
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.view.View.VISIBLE;


public class SplashAndRegisterActivity extends AppCompatActivity implements
        CongratulationAfterVerification.Communicator{

    // Object to hold a reference to the app slogan.
    private TextView slogan;

    // Request code value for authentication.
    private static final int RC_SIGN_IN = 123;

    // Request code value for category choosing.
    private static final int CAT_CHOOSER = 20;

    private LayoutInflater layoutInflater;
    private LinearLayout insertPoint;
    private String mShopName, mShopAddress, mShopPinCode, mSellerName, sellerMobNumber;
    private ProgressDialog progressDialog;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_and_register);

        // Code to set custom text font style from assets folder.
        Typeface typeface = Typeface.createFromAsset(getAssets(),
                "fonts/Tangerine-Bold.ttf");

        // Make a reference to slogan and remove in time of registration. And set typeface.
        slogan = this.findViewById(R.id.slogan_text);
        slogan.setTypeface(typeface);

        // Get reference to firebase real-time database.
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Get reference to firebase authentication.
        mAuth = FirebaseAuth.getInstance();

        // Make a reference to layout inflater which will be used later.
        layoutInflater = (LayoutInflater) this.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        // Get the reference to view where the card is going to be added.
        insertPoint = findViewById(R.id.view_container);

        // Get a reference to progress dialogue.
        progressDialog = new ProgressDialog(SplashAndRegisterActivity.this);

        // Check whether user is signed in or not and show the welcome or sign in
        // screen accordingly.
        final FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userName = user.getDisplayName();
            showRegisterOrWelcomeCard();
        } else {
            // Show the register card.
            signUp();
        }

    }


    // Helper function to show welcome or register screen based on whether user is
    // registered or not.
    private void showRegisterOrWelcomeCard() {
        // If shop is registered already show him welcome screen. Otherwise show him
        // registration screen.
        String uid = mAuth.getUid();
        DatabaseReference temp = mDatabase.child("SellerProgress").child(uid);
        temp.keepSynced(true);
        temp.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean registered = false;
                if (dataSnapshot != null) {
                    SellerProgress sellerProgress = dataSnapshot.getValue(
                            SellerProgress.class);
                    if (sellerProgress != null) {
                        if (sellerProgress.registered) {
                            registered = true;
                        }
                    }
                }

                // Dismiss progress dialogue if visible.
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                // If registered already show welcome screen otherwise show
                // register screen
                if (registered) {
                    addWelcomeCard();
                } else {
                    addShopRegisterCard();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Helper function to sign up for user.
    private void signUp() {
        // Firebase UI integration.
        startActivityForResult(
                AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setLogo(R.drawable.flat_unit_logo)
                .setIsSmartLockEnabled(false)
                .setTheme(R.style.FireBaseCustomTheme)
                .setAvailableProviders(
                        Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                .build(),
        RC_SIGN_IN);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // RC_SIGN_IN is the request code you passed into startActivityForResult(...)
        // when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {

                // Get user name
                userName = mAuth.getCurrentUser().getDisplayName();

                // First congratulates him then show register or welcome screen according
                // to user progress.
                showLoadingDialogue("Getting ready...");
                showCongratulationAfterSignIn();

            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    showErrorMessageDialogue("Sorry you have pressed back button. Do not " +
                            "press back button during registration.");
                } else {
                    if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                        showErrorMessageDialogue("You do not have internet connection." +
                                "Make sure you have turned on Mobile data or connected with Wifi" +
                                "network.");
                    }

                    if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                        showErrorMessageDialogue("Some internal error encountered." +
                                "Please try again later.");
                    }
                }
            }
        }

        // Check for category choosing request code.
        if (requestCode == CAT_CHOOSER) {
            if (resultCode == RESULT_OK) {
                // Save everything to firebase database and show the main screen if insertion
                // is successful. Otherwise show the error message.
                ArrayList<String> categories = data.getStringArrayListExtra("Categories");
                Seller seller = new Seller(mShopName, mShopAddress, mShopPinCode,
                        sellerMobNumber, categories);
                showLoadingDialogue("Saving the data...");
                final String userKey = mAuth.getUid();
                mDatabase.child("UNiTSellers").child(userKey).setValue(seller)
                        .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                // Now data insertion is successful make shop registered locally
                                // as well as globally.
                                final SellerProgress sellerProgress = new SellerProgress(true);
                                mDatabase.child("SellerProgress").child(userKey)
                                        .setValue(sellerProgress);

                                // Dismiss progress dialogue.
                                progressDialog.dismiss();

                                // Set userName to mSellerName.
                                userName = mAuth.getCurrentUser().getDisplayName();

                                // Open home screen.
                                openHomeScreen();
                            }
                        }
                ).addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showErrorMessageDialogue("Some server internal error has happened. " +
                                "We are working on it. Please try again after some time.");
                    }
                });

            } else {
                showErrorMessageDialogue("Please don't press back button during choosing" +
                        " the category. Try one more time.");

                // Start the category chooser activity.
                Intent intent = new Intent(SplashAndRegisterActivity.this
                        , CategoryChooserActivity.class);
                startActivityForResult(intent, CAT_CHOOSER);
            }
        }
    }

    // Helper function to show home screen and finish this activity.
    private void openHomeScreen() {
        Intent intent = new Intent(SplashAndRegisterActivity.this, HomeScreenActivity.class);
        intent.putExtra("userName", userName);
        startActivity(intent);
        finish();
    }

    private boolean isAllInputsValid () {
        boolean allValid = false;

        // First find out all input strings.
        String shopName = ((EditText) findViewById(R.id.shop_name_edit_text)).getText()
                .toString();
        String shopAddress = ((EditText) findViewById(R.id.shop_address_edit_text)).getText()
                .toString();
        String shopMobNumber = ((EditText) findViewById(R.id.phone_number_edit_text)).getText()
                .toString();
        String shopPinCode = ((EditText) findViewById(R.id.pin_code_edit_text)).getText()
                .toString();

        // Validate all input strings.
        if ((shopName.length() >= 4) && (shopAddress.length() >= 4) &&
                (shopMobNumber.length() == 10) && (shopPinCode.length() == 6)) {

            // Now every field is ok.
            allValid = true;
            sellerMobNumber = shopMobNumber;
            mShopName = shopName;
            mShopAddress = shopAddress;
            mShopPinCode = shopPinCode;
        }

        if (!allValid) {
            showErrorMessageDialogue("Please enter correct inputs.");
        }

        return allValid;
    }

    // Helper function to show the loading dialogue.
    private void showLoadingDialogue(final String loadingMeassage) {
        progressDialog.setMessage(loadingMeassage);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(false);
        progressDialog.show();
    }

    // Helper function to show congratulation dialogue after phone number verification.
    private void showCongratulationAfterSignIn() {
        CongratulationAfterVerification verificationDialog = new CongratulationAfterVerification();
        verificationDialog.show(getFragmentManager(), "Verified");
    }

    // Helper function to show the error message.
    private void showErrorMessageDialogue(final String errorMessage) {
        ErrorMessageDialogue msgDialog = new ErrorMessageDialogue();
        Bundle bundle = new Bundle();
        bundle.putString("Message", errorMessage);
        msgDialog.setArguments(bundle);
        msgDialog.show(getSupportFragmentManager(), "Message");
    }

    // Helper function to add welcome screen
    private void addWelcomeCard() {

        // Get current Date to show greeting to the user.
        Date d = new Date();
        SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss");
        String currentDateTimeString = sdf.format(d);

        // Receive greeting message from the function.
        String greetingMessage = getWishingMessageString(
                currentDateTimeString);

        mSellerName = userName;

        View viewToBeAdded = layoutInflater.inflate(R.layout.splash_welcome_message_card_view
                , null, true);
        TextView userNameText = viewToBeAdded.findViewById(R.id.user_name);
        userNameText.setText(mSellerName);
        userNameText.setVisibility(VISIBLE);

        // Check whether you have successfully got the string without any exception
        // and show the greeting card.
        if (greetingMessage != null) {
            TextView greeting = (TextView) viewToBeAdded
                    .findViewById(R.id.greeting_message);
            greeting.setText(greetingMessage);
            viewToBeAdded.findViewById(R.id.wishing_card).setVisibility(VISIBLE);
        }

        // Remove any views if it has previously.
        insertPoint.removeAllViews();

        // Finally add the view to the correct position.
        insertPoint.addView(viewToBeAdded);

        // Finish this activity and show home screen.
        finishThisGoToHomeScreen();

    }

    private void finishThisGoToHomeScreen() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                openHomeScreen();
            }
        }, 1000);
    }

    /**
     *
     * @param timeString This contains the current time as a string.
     * @return It return the id to show the appropriate greeting message like:
     *          1 -> Good Morning
     *          2 -> Good Afternoon
     *          3 -> Good Evening
     *          4 -> Good Night
     */
    private String getWishingMessageString(String timeString) {
        // String that contains the greeting message.
        String greetingMessage;

        // Parse the timeString into hours, minutes and seconds.
        int hours, minutes, seconds;
        String temp = timeString.substring(0, 2);
        hours = Integer.parseInt(temp);
        temp = timeString.substring(3, 5);
        minutes = Integer.parseInt(temp);
        temp = timeString.substring(6, 8);
        seconds = Integer.parseInt(temp);

        // Make greeting string depending on time duration of a day.
        if (isInRange(hours, minutes, seconds, 5, 12)) {
            greetingMessage = "Good Morning";
        } else if (isInRange(hours, minutes, seconds, 12, 18)) {
            greetingMessage = "Good Afternoon";
        } else if (isInRange(hours, minutes, seconds, 18, 22)) {
            greetingMessage = "Good Evening";
        } else {
            greetingMessage = "Good Night";
        }
        return greetingMessage;
    }

    /**
     * @param hours Contains the hours in the current time
     * @param minutes Contains the minutes in the current time
     * @param seconds Contains the seconds in the current time
     * @param min Contains the least minimum possible value.
     * @param max Contains the maximum possible value.
     * @return It return a boolean value whether hours, minutes and seconds are in
     *          that range or not
     */
    private boolean isInRange(int hours, int minutes, int seconds, int min, int max) {
        return ((hours >= min) && ((hours < max) |
                (hours == max && minutes == 0 && seconds == 0)));
    }


    private void addShopRegisterCard() {
        final View viewToBeAdded = layoutInflater.inflate(R.layout.splash_shop_register_card
                , null, true);

        // Hide default slogan
        slogan.setVisibility(View.GONE);

        // On join button click add otp card.
        viewToBeAdded.findViewById(R.id.join).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Now check whether every field is ok. If so then launch category
                        // choosing activity.
                        if (isAllInputsValid()) {
                            // Start category choosing activity.
                            Intent intent = new Intent(SplashAndRegisterActivity.this
                                    , CategoryChooserActivity.class);
                            startActivityForResult(intent, CAT_CHOOSER);
                        }
                    }
                });

        // Remove any views if it has previously.
        insertPoint.removeAllViews();

        // Finally add the view to the correct position.
        insertPoint.addView(viewToBeAdded);
    }

    @Override
    public void communicate() {
        showRegisterOrWelcomeCard();
    }
}
