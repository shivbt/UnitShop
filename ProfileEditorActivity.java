package com.ikai.unitshop;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.ikai.unitshop.Constants.READ_EXTERNAL_STORAGE_REQUEST;

public class ProfileEditorActivity extends AppCompatActivity implements View.OnClickListener {

    private CircleImageView profilePhoto;
    private TextView sellerNameView, mobileNumberView, shopNameView, shopAddressView,
            pinCodeView;
    private DatabaseReference mDatabaseReference;
    private Seller sellerDetails;
    private Uri imageUri;
    private FirebaseUser user;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_editor);

        // Set back arrow on action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        // Get current user first.
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Get a reference to profile photo.
        profilePhoto = findViewById(R.id.profile_photo);

        if (user != null) {
            // Get the photo url. And set it to profilePhoto.
            Uri photoUrl = user.getPhotoUrl();
            if (photoUrl != null) {
                Glide.with(this)
                        .load(photoUrl)
                        .thumbnail(0.5f)
                        .into(profilePhoto);
            }
        }

        // Get reference to firebase database.
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // Get reference to progress bar.
        progressBar = findViewById(R.id.progress_bar);

        // Get reference to every text fields i.e. personal info and shop info.
        sellerNameView = findViewById(R.id.seller_name);
        mobileNumberView = findViewById(R.id.mobile_number);
        shopNameView = findViewById(R.id.shop_name);
        shopAddressView = findViewById(R.id.shop_address);
        pinCodeView = findViewById(R.id.pin_code);

        // Get reference to shop info edit and personal info edit icons and set on click
        // listeners on those.
        ImageView shopInfoEdit = findViewById(R.id.edit_shop_info);
        ImageView personalInfoEdit = findViewById(R.id.edit_personal_info);
        shopInfoEdit.setOnClickListener(this);
        personalInfoEdit.setOnClickListener(this);

        // Get a reference to camera icon click and set on click listener.
        ImageView cameraIcon = findViewById(R.id.camera_icon);
        cameraIcon.setOnClickListener(this);


    }

    @Override
    protected void onResume() {
        super.onResume();

        if (user != null) {
            // Get seller name and set it to sellerNameView.
            sellerNameView.setText(user.getDisplayName());
        }

        // Get other fields from database and set it.
        DatabaseReference temp = mDatabaseReference.child("UNiTSellers").child(user.getUid());
        temp.keepSynced(true);
        temp.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    sellerDetails = dataSnapshot.getValue(Seller.class);
                    if (sellerDetails != null) {
                        // Set every fields.
                        mobileNumberView.setText(sellerDetails.sellerMobNumber);
                        shopNameView.setText(sellerDetails.shopName);
                        shopAddressView.setText(sellerDetails.shopAddress);
                        pinCodeView.setText(sellerDetails.shopPinCode);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Show error why request is canceled.
                showDatabaseFetchError(databaseError);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Finish the activity when user presses back arrow.
            case android.R.id.home:
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    // Helper function to show error occurred during database fetching.
    private void showDatabaseFetchError(DatabaseError databaseError) {
        switch (databaseError.getCode()) {
            case DatabaseError.DISCONNECTED:
                showErrorMessageDialogue("Error in fetching the results. " +
                        "You are disconnected. Please try again in a little bit");
                break;

            case DatabaseError.NETWORK_ERROR:
                showErrorMessageDialogue("Error in fetching the results. " +
                        "Please make sure you have internet connection.");
                break;

            case DatabaseError.UNAVAILABLE:
                showErrorMessageDialogue("Error in fetching the results. " +
                        "Server is unavailable. We are sorry for that. Try " +
                        "again in a little bit");
                break;

            default:
                showErrorMessageDialogue("Error in fetching the results. " +
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

    @Override
    public void onClick(View view) {
        int id = view.getId();

        // Check for camera click
        if (id == R.id.camera_icon) {
            // Open bottom sheet provided by CropImage.
            CropImage.startPickImageActivity(this);
        }

        // Check for personal edit icon click.
        if (id == R.id.edit_personal_info) {
            // Start personal info edit activity to edit personal info.
            Intent intent = new Intent(this, PersonalInfoEditActivity.class);
            startActivity(intent);
        }

        // Check for shop edit icon click.
        if (id == R.id.edit_shop_info) {
            // Start shop info edit activity to edit shop info.
            Intent intent = new Intent(this, ShopInfoEditActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check for image pick chooser.
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE) {

            // Check if result is ok or not.
            if (resultCode == Activity.RESULT_OK) {
                imageUri = CropImage.getPickImageResultUri(this, data);

                // Check read permission and crop the image.
                checkPermissionAndCropShopImage();
            }
        }

        // Check for crop request code.
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            // Check whether result is ok or not.
            if (resultCode == Activity.RESULT_OK) {

                // Get the result from the called activity.
                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                // Save the image in storage and make photo url of firebase authentication
                // to that storage location load the image in profilePhoto using glide.
                saveImageToFirebaseAndUpdateProfilePic(result.getUri());

            }
        }

    }

    // Helper function to save image into firebase storage and point the photo url
    // of firebase authentication to saved location. And set it to profile photo holder.
    private void saveImageToFirebaseAndUpdateProfilePic(final Uri photoUri) {

        // Show progress bar while uploading the image.
        progressBar.setVisibility(View.VISIBLE);

        // Store result to firebase storage.
        StorageReference mStorageReference = FirebaseStorage.getInstance().getReference()
                .child("UNiTShop/ProfileImages").child(FirebaseAuth.getInstance().getUid());
        mStorageReference.putFile(photoUri).addOnSuccessListener(
                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        final Uri profilePicDownloadUri = taskSnapshot.getDownloadUrl();
                        if (profilePicDownloadUri != null) {

                            // Put this download url into photo url of current user.
                            UserProfileChangeRequest profileChangeRequest = new
                                    UserProfileChangeRequest.Builder()
                                    .setPhotoUri(profilePicDownloadUri)
                                    .build();
                            user.updateProfile(profileChangeRequest)
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            showErrorMessageDialogue("Updating profile" +
                                                    "picture failed. Some error just " +
                                                    "happened. Please try again in a " +
                                                    "little bit");
                                        }
                                    }).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    // Image updating is successful so change it in
                                    // profilePhoto also. While loading show progressbar
                                    // and dismiss when loading is over.
                                    Glide.with(ProfileEditorActivity.this)
                                            .load(photoUri)
                                            .listener(
                                                    new RequestListener<Drawable>() {
                                                        @Override
                                                        public boolean onLoadFailed
                                                                (@Nullable GlideException e,
                                                                 Object model,
                                                                 Target<Drawable> target,
                                                                 boolean isFirstResource) {

                                                            // Make progress bar invisible.
                                                            if (progressBar.getVisibility() ==
                                                                    View.VISIBLE) {
                                                                progressBar
                                                                        .setVisibility(View.GONE);
                                                            }
                                                            showErrorMessageDialogue("Error while" +
                                                                    "loading the image." +
                                                                    " Make sure you have good" +
                                                                    "internet connection");
                                                            return false;

                                                        }

                                                        @Override
                                                        public boolean onResourceReady(
                                                                Drawable resource, Object model,
                                                                Target<Drawable> target,
                                                                DataSource dataSource,
                                                                boolean isFirstResource) {

                                                            // Make progressbar invisible.
                                                            if (progressBar.getVisibility() ==
                                                                    View.VISIBLE) {
                                                                progressBar
                                                                        .setVisibility(View.GONE);
                                                            }

                                                            return false;
                                                        }
                                                    }
                                            )
                                            .thumbnail(0.5f)
                                            .into(profilePhoto);

                                }
                            });

                        }
                    }
                }
        );

    }

    // Helper function to check whether we have permission for reading and if not then
    // take permission and then call cropShopImage() function accordingly.
    private void checkPermissionAndCropShopImage() {

        // For API >= 23 we need to check specifically that we have permissions to
        // read external storage.
        if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
            // request permissions and handle the result in onRequestPermissionsResult()
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE_REQUEST
            );

        } else {
            // no permissions required or already granted, can start crop image activity
            cropShopImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check for READ_EXTERNAL_STORAGE_REQUEST request code.
        if (requestCode == READ_EXTERNAL_STORAGE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Call permission is granted, so call function to crop the image.
                cropShopImage();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    //Show a dialog showing the message for need of this permission.
                    showNeedDialogAndAskPermission();

                }
            }
        }

    }

    // Helper function to show a dialog showing the need of access of external permission.
    private void showNeedDialogAndAskPermission() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Allow UNiT to read external storage?");
        builder.setMessage("This lets UNiT to set your profile picture and save it to our server." +
                " So that we can show this image to your consumer.");
        builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                ActivityCompat.requestPermissions(ProfileEditorActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        READ_EXTERNAL_STORAGE_REQUEST
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

    // Helper function to crop a given bitmap and save it to firebase database.
    private void cropShopImage() {

        // Check whether we have some imageUri.
        if (imageUri != null) {
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setMultiTouchEnabled(true)
                    .setFixAspectRatio(true)
                    .start(this);
        } else {
            showErrorMessageDialogue("Some error just happened in getting the image. " +
                    "Please try again in a little bit.");
        }
    }
}
