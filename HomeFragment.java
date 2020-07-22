package com.ikai.unitshop;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

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

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.ikai.unitshop.Constants.READ_EXTERNAL_STORAGE_REQUEST;

/**
 * Created by shiv on 22/11/17.
 * TODO: Still need to be implemented order details fragment.
 */

public class HomeFragment extends Fragment implements ConfirmBeforeItemDelete.Communicator
        , View.OnClickListener {

    private CustomPagerAdapter customPagerAdapter;
    private DatabaseReference mDatabaseReference;
    private String userKey;
    private ArrayList<String> shopImages = new ArrayList<>();
    private ArrayList<ImageView> dots = new ArrayList<>();
    private LinearLayout dotsContainer;
    private Context context;
    private int currentPositionOfSlider = 0;
    private ProgressDialog progressDialog;
    private Uri imageUri;
    private boolean addMoreSlider;
    private CircleImageView ownerPhoto;
    private FirebaseAuth mAuth;
    private ViewPager viewPager;

    // Declare progress bar object which is on owner photo.
    private ProgressBar progressBarOnOwnerPhoto;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);

        // Get reference to progress bar on shop's owner image.
        progressBarOnOwnerPhoto = view.findViewById(R.id.progress_bar_owner_photo);

        // Creating dots on the bottom side of shop images. For that get reference
        // to dots container.
        dotsContainer = view.findViewById(R.id.dots_container);

        context = getContext();  // Get context.

        // Get reference to owner photo.
        ownerPhoto = view.findViewById(R.id.owner_photo);

        // Get reference to firebase authentication
        mAuth = FirebaseAuth.getInstance();

        // Make addMoreSlider false, because in starting there is no slider to add.
        addMoreSlider = false;

        // Get reference to add more shop images icon.
        ImageView addMoreShopImagesIcon = view.findViewById(R.id.add_more_shop_images);
        // Set on click listener for above image views.
        addMoreShopImagesIcon.setOnClickListener(this);

        // Get reference to camer and delete icons.
        ImageView cameraIcon, deleteIcon;
        cameraIcon = view.findViewById(R.id.shop_image_camera_icon);
        deleteIcon = view.findViewById(R.id.shop_image_delete_icon);

        // Set on click listener to above image view.
        cameraIcon.setOnClickListener(this);
        deleteIcon.setOnClickListener(this);

        // Get a reference to View Pager.
        viewPager = view.findViewById(R.id.view_pager);

        // Get a reference to progress dialog.
        progressDialog = new ProgressDialog(getActivity());

        // Create customPagerAdapter.
        customPagerAdapter = new CustomPagerAdapter(getActivity(), shopImages);

        // Add page change listener for view pager and change dots as page changes.
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                int dotsCount = dots.size();

                // Make current dot as active and rest of all as non active dot.
                for (int i = 0; i < dotsCount; i++) {
                    dots.get(i).setImageResource(R.drawable.ic_dot_no_active_8dp);
                }
                dots.get(position).setImageResource(R.drawable.ic_dot_active_8dp);
                currentPositionOfSlider = position;

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // Set the adapter.
        viewPager.setAdapter(customPagerAdapter);

//        viewPager.getCurrentItem();

        // Get reference to firebase database root.
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // Get respective user key.
        userKey = mAuth.getUid();

        // Create dots and set current dot as active dot.
        createDots();

        // Fetch shop images from database and notify the adapter.
        fetchShopImagesAndNotifyAdapter();


//        // Create handler to change view pager's content automatically.
//        final Handler handler = new Handler();
//        final Runnable mRunnable = new Runnable() {
//            @Override
//            public void run() {
//                // If view pager is pointing to the last image then change it to first
//                // otherwise simply go to the next image of view pager.
//                if (currentPositionOfSlider == shopImages.size() - 1) {
//                    currentPositionOfSlider = 0;
//                } else {
//                    currentPositionOfSlider = currentPositionOfSlider + 1;
//                }
//                viewPager.setCurrentItem(currentPositionOfSlider, true);
//                handler.postDelayed(this, 1000);
//            }
//        };
//        handler.postDelayed(mRunnable, 2000);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Show progress bar while loading the photo if not showing.
        if (progressBarOnOwnerPhoto.getVisibility() == View.GONE) {
            progressBarOnOwnerPhoto.setVisibility(View.VISIBLE);
        }

        // Get seller photo url and load it with Glide.
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Glide.with(getActivity())
                    .load(user.getPhotoUrl())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e
                                , Object model, Target<Drawable> target, boolean isFirstResource) {

                            // Hide progress bar if showing.
                            if (progressBarOnOwnerPhoto.getVisibility() == View.VISIBLE) {
                                progressBarOnOwnerPhoto.setVisibility(View.GONE);
                            }
                            return false;

                        }

                        @Override
                        public boolean onResourceReady(Drawable resource
                                , Object model, Target<Drawable> target, DataSource dataSource
                                , boolean isFirstResource) {

                            // Hide progress bar if showing.
                            if (progressBarOnOwnerPhoto.getVisibility() == View.VISIBLE) {
                                progressBarOnOwnerPhoto.setVisibility(View.GONE);
                            }
                            return false;

                        }
                    })
                    .thumbnail(0.5f)
                    .into(ownerPhoto);

        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        // Check for camera icon click.
        if (id == R.id.shop_image_camera_icon) {
            // Open bottom sheet provided by CropImage.
            CropImage.startPickImageActivity(context, HomeFragment.this);
        }

        // Check for delete icon click.
        if (id == R.id.shop_image_delete_icon) {

            // Delete the image from firebase database and notify the adapter.
            // If we have more than one images in slider then delete the selected
            // one otherwise abort delete with displaying some error message.
            if (customPagerAdapter.getCount() > 1) {
                showConfirmationDialogue("Delete this image?", "This image will" +
                        " be permanently deleted. Do you still want to delete " +
                        "this image?", currentPositionOfSlider);
            } else {
                showErrorMessageDialogue("This is the only image for your shop." +
                        " So it can not be deleted.");
            }
        }

        // Check for add more images icon click.
        if (id == R.id.add_more_shop_images) {

            // More than 3 images can not be uploaded.
            if (customPagerAdapter.getCount() <= 2) {
                // Make addMoreSlider to true to indicate that we have to add new
                // image to slider. And call image chooser activity defined in CropImage.
                addMoreSlider = true;
                CropImage.startPickImageActivity(context, HomeFragment.this);

            } else {

                // show the error message.
                showErrorMessageDialogue("You can not add more than 3 images.");
            }
        }
    }

    // Helper function to show the loading dialogue.
    private void showLoadingDialogue(final String loadingMessage) {
        progressDialog.setMessage(loadingMessage);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(false);
        progressDialog.show();
    }

    // Helper function to delete currently showing shop image from firebase database.
    private void deleteShopImage() {

        /**
         * TODO Make cloud function to delete respective shop images from the "Storage" also.
         */

        showLoadingDialogue("Deleting...");
        final DatabaseReference shopImageRef = mDatabaseReference.child("UNiTSellers")
                .child(userKey).child("shopImages");

        // Add value listener to the shop image.
        shopImageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    // Get the result in array list of url strings.
                    ArrayList<String> images = (ArrayList<String>) dataSnapshot.getValue();
                    if (images != null) {
                        int size = images.size();

                        // If size > 1 then we have more than one image and user can delete it.
                        if (size > 1) {

                            // Get the current showing image and delete it.
                            String imageToDelete = shopImages.get(currentPositionOfSlider);
                            for (int i = 0; i < size; i++) {
                                if (images.get(i).contentEquals(imageToDelete)) {
                                    images.remove(i);
                                    break;
                                }
                            }

                            // Update database with rest of images.
                            shopImageRef.setValue(images).addOnCompleteListener(
                                    new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            // Dismiss the progress dialog if showing.
                                            if (progressDialog.isShowing()) {
                                                progressDialog.dismiss();
                                            }

                                            // Delete the image from list and notify the adapter.
                                            shopImages.remove(currentPositionOfSlider);
                                            customPagerAdapter.notifyDataSetChanged();

                                            // Remove all previous dots and make dots again. And
                                            // make currentPositionOfSlider to point to 1 less
                                            // than previous value;
                                            dots.clear();
                                            createDots();
                                        }
                                    }
                            ).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    // Dismiss the progress dialog if showing.
                                    if (progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }

                                    // Show the error message.
                                    showErrorMessageDialogue("Some internal error just happened" +
                                            " while deleting the shop image. We are working" +
                                            " on it. Please try again in a little bit.");
                                }
                            });
                        } else {

                            // Dismiss the progress dialog if showing.
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }

                            // Show the error message.
                            showErrorMessageDialogue("This is the only image for your shop." +
                                    " So it can not be deleted.");
                        }
                    } else {

                        // Dismiss the progress dialog if showing.
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                        // Show the error message.
                        showErrorMessageDialogue("Some internal error just happened" +
                                " while deleting the shop image. We are working" +
                                " on it. Please try again in a little bit.");
                    }
                } else {

                    // Dismiss the progress dialog if showing.
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                    // Show the error message.
                    showErrorMessageDialogue("Some internal error just happened" +
                            " while deleting the shop image. We are working" +
                            " on it. Please try again in a little bit.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                // Dismiss the progress dialog if showing.
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                // Show the error message.
                showErrorMessageDialogue("Some connection problem just happened" +
                         " while deleting the shop image. Please try again in a little bit.");
            }
        });

        // Keep everything in synced.
        shopImageRef.keepSynced(true);

    }

    // Helper function to fetch shop images from firebase database and notify the adapter.
    private void fetchShopImagesAndNotifyAdapter() {

        // Get reference to seller's store images and sync it for offline uses.
        DatabaseReference shopImageRef = mDatabaseReference.child("UNiTSellers").child(userKey)
                .child("shopImages");
        shopImageRef.keepSynced(true);

        // Add value listener so that we can get value.
        shopImageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean imageNotFound = true;
                if (dataSnapshot != null) {
                    ArrayList<String> images = (ArrayList<String>) dataSnapshot.getValue();

                    if (images != null) {
                        imageNotFound = false;

                        // Insert all images to shopImages.
                        for (String image : images) {
                            shopImages.add(image);

                            // Now notify the adapter.
                            customPagerAdapter.notifyDataSetChanged();
                        }
                    }
                }

                // Now check image is found or not. If not show some default image.
                if (imageNotFound) {

                    Uri imageURI = Uri.parse("android.resource://com.ikai.unitshop/"
                            + R.drawable.store);
                    shopImages.add(imageURI.toString());

                    // Now notify the adapter.
                    customPagerAdapter.notifyDataSetChanged();
                }

                // Remove all previous dots and make dots again.
                dots.clear();
                createDots();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    // Helper function to create dots on the bottom of shop image.
    private void createDots() {

        // Remove all previous dots from the container.
        dotsContainer.removeAllViews();

        int dotsCount = customPagerAdapter.getCount();
        for (int i = 0; i < dotsCount; i++) {
            ImageView dotImageView = new ImageView(context);
            dotImageView.setImageResource(R.drawable.ic_dot_no_active_8dp);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            // Get display matrix.
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            // Convert pixel to dp.
            int marginInDp = convertPixelsToDp(5, displayMetrics);

            // Set the margins and set the layout params to dotImageView.
            layoutParams.setMargins(marginInDp, marginInDp, marginInDp, marginInDp);
            dotImageView.setLayoutParams(layoutParams);

            // Finally add the dotImageView to arrayList dots and dotsContainer.
            dots.add(dotImageView);
            dotsContainer.addView(dotImageView);
        }

        // Now set current dot as active dot. If we have dotsCount > 0.
        if (dotsCount > 0) {
            dots.get(currentPositionOfSlider).setImageResource(R.drawable.ic_dot_active_8dp);
        }

    }

    // Helper function to convert pixel into dp.
    private static int convertPixelsToDp(float px, DisplayMetrics metrics){
        return (int)Math.ceil((double)(px / ((float)metrics.densityDpi /
                DisplayMetrics.DENSITY_DEFAULT)));
    }

    // Helper function to show the error message.
    private void showErrorMessageDialogue(final String errorMessage) {
        ErrorMessageDialogue msgDialog = new ErrorMessageDialogue();
        Bundle bundle = new Bundle();
        bundle.putString("Message", errorMessage);
        msgDialog.setArguments(bundle);
        msgDialog.show(getFragmentManager(), "Message");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check for image pick chooser.
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE) {

            // Check if result is ok or not.
            if (resultCode == Activity.RESULT_OK) {
                imageUri = CropImage.getPickImageResultUri(context, data);

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

                // Check whether crop activity is called for adding the image or updating
                // the current image.
                if (addMoreSlider) {

                    // Show loading dialog.
                    showLoadingDialogue("Adding the image...");

                    // Make addMoreSlider to false.
                    addMoreSlider = false;

                    // Add new image to database and view pager.
                    addImageToDatabaseAndViewPager(result.getUri());

                } else {

                    // Show loading dialog.
                    showLoadingDialogue("Setting the image...");

                    // Update the firebase database and current sliding image.
                    updateDatabaseAndSliderImage(result.getUri());
                }
            }
        }
    }

    // Helper function to show confirmation dialogue before any action.
    private void showConfirmationDialogue(final String title, final String message,
                                          final int position) {
        ConfirmBeforeItemDelete confirmDialog = new ConfirmBeforeItemDelete();
        Bundle bundle = new Bundle();
        bundle.putString("Message", message);
        bundle.putString("Title", title);
        bundle.putInt("Position", position);
        bundle.putString("FragmentTag", "Home");
        confirmDialog.setArguments(bundle);
        confirmDialog.show(getFragmentManager(), "ConfirmDialogue");
    }

    // Helper function to add given image to firebase database and view pager.
    private void addImageToDatabaseAndViewPager(final Uri imageUri) {

        // Store result to firebase storage.
        StorageReference mStorageReference = FirebaseStorage.getInstance().getReference()
                .child("UNiTShop/ShopImages").child(userKey)
                .child("Image" + (currentPositionOfSlider + 1));

        // Put the new image file and add listeners.
        mStorageReference.putFile(imageUri).addOnSuccessListener(
                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        final Uri shopImageUri = taskSnapshot.getDownloadUrl();
                        if (shopImageUri != null) {

                            // Modify the current uri to point to inserted file. And notify
                            // the adapter.
                            shopImages.add(shopImageUri.toString());
                            customPagerAdapter.notifyDataSetChanged();

                            // Update UNiTSellers/userKey/shopImages
                            DatabaseReference imageRef = mDatabaseReference
                                    .child("UNiTSellers").child(userKey)
                                    .child("shopImages");
                            imageRef.setValue(shopImages).addOnSuccessListener(
                                    new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Remove all previous dots and make dots again.
                                            dots.clear();
                                            createDots();

                                            // Dismiss the loading dialog if showing.
                                            if (progressDialog.isShowing()) {
                                                progressDialog.dismiss();
                                            }
                                        }
                                    }
                            ).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    // Uploading failed so restore to previous stage.
                                    int size = shopImages.size();
                                    for (int i = 0; i < size; i++) {
                                        if (shopImages.get(i).contentEquals(imageUri.toString())) {
                                            shopImages.remove(i);
                                            break;
                                        }
                                    }

                                    // Dismiss the loading dialog if showing.
                                    if (progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }

                                    // Show the error message.
                                    showErrorMessageDialogue("Some error just happened." +
                                            " Please try again in a little bit.");
                                }
                            });

                            // Keep everything in sync.
                            imageRef.keepSynced(true);

                        } else {
                            // Dismiss the loading dialog if showing.
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }

                            // Show the error message.
                            showErrorMessageDialogue("Some error just happened." +
                                    " Please try again in a little bit.");
                        }
                    }
                }
        ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Dismiss the loading dialog if showing.
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                // Show the error message.
                showErrorMessageDialogue("Some error just happened." +
                        " Please try again in a little bit.");
            }
        });
    }

    // Helper function to update 'shopImages' of 'UNiTSellers/userKey/' and current showing
    // image in slider.
    private void updateDatabaseAndSliderImage(final Uri imageUri) {

        // Store old image Uri. It will be used in undoing if uploading will be failed.
        final String oldImageUri = shopImages.get(currentPositionOfSlider);

        // Store result to firebase storage.
        StorageReference mStorageReference = FirebaseStorage.getInstance().getReference()
                .child("UNiTShop/ShopImages").child(userKey)
                .child("Image" + currentPositionOfSlider);

        // Put the file and add listeners.
        mStorageReference.putFile(imageUri).addOnSuccessListener(
                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri shopImageUri = taskSnapshot.getDownloadUrl();
                        if (shopImageUri != null) {

                            // Modify the current uri to point to inserted image. And notify
                            // the adapter.
                            shopImages.set(currentPositionOfSlider,
                                    shopImageUri.toString());
                            customPagerAdapter.notifyDataSetChanged();

                            // Update UNiTSellers/userKey/shopImages
                            DatabaseReference imageRef = mDatabaseReference
                                    .child("UNiTSellers").child(userKey)
                                    .child("shopImages");
                            imageRef.setValue(shopImages).addOnSuccessListener(
                                    new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Dismiss the loading dialog if showing.
                                            if (progressDialog.isShowing()) {
                                                progressDialog.dismiss();
                                            }
                                        }
                                    }
                            ).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Dismiss the loading dialog if showing.
                                    if (progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }

                                    // Restore previous image. And notify the adapter.
                                    shopImages.set(currentPositionOfSlider, oldImageUri);
                                    customPagerAdapter.notifyDataSetChanged();

                                    // Show the error message.
                                    showErrorMessageDialogue("Some error just happened." +
                                            " Please try again in a little bit.");
                                }
                            });

                            // Keep everything in sync.
                            imageRef.keepSynced(true);

                        } else {

                            // Dismiss the loading dialog if showing.
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }

                            // Show the error message.
                            showErrorMessageDialogue("Some error just happened." +
                                    " Please try again in a little bit.");
                        }

                    }
                }
        ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                // Dismiss the loading dialog if showing.
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                showErrorMessageDialogue("Some error just happened. We are working" +
                        " on it. Please try again in a little bit.");
            }
        });

    }

    // Helper function to check whether we have permission for reading and if not then
    // take permission and then call cropShopImage() function accordingly.
    private void checkPermissionAndCropShopImage() {

        // For API >= 23 we need to check specifically that we have permissions to
        // read external storage.
        if (CropImage.isReadExternalStoragePermissionsRequired(context, imageUri)) {
            // request permissions and handle the result in onRequestPermissionsResult()
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
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
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    //Show a dialog showing the message for need of this permission.
                    showNeedDialogAndAskPermission();

                }
            }
        }

    }

    // Helper function to show a dialog showing the need of access of external permission.
    private void showNeedDialogAndAskPermission() {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Allow UNiT to read external storage?");
        builder.setMessage("This lets UNiT to set your shop image and save it to our server." +
                " So that we can show this image to your consumer.");
        builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                ActivityCompat.requestPermissions(getActivity(),
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
                    .start(context, this);
        } else {
            showErrorMessageDialogue("Some error just happened in setting the image. " +
                    "Please try again in a little bit.");
        }
    }

    @Override
    public void communicate(int position, int recyclerViewId) {
        // User has pressed delete button. So delete the current image.
        deleteShopImage();
    }

}
