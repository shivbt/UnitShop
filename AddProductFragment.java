package com.ikai.unitshop;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.ikai.unitshop.Constants.GRID_CATEGORY_WIDTH_WITH_MARGIN;

/**
 * Created by shiv on 22/11/17.
 */

public class AddProductFragment extends Fragment implements ConfirmBeforeItemDelete.Communicator
        , View.OnClickListener {

    private List<ShopCategory> shopCategoryList = new ArrayList<>();
    private GridCategoryAdapter gridCategoryAdapter;
    private ProgressDialog progressDialog;
    private DatabaseReference databaseReference;
    private String userKey;
    private Context activityContext;
    private final static int ADD_CATEGORY_REQUEST = 4;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_product_fragment, container, false);
        activityContext = getActivity();

        // Get reference to firebase database root.
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Get respective user key.
        userKey = FirebaseAuth.getInstance().getUid();

        // Get reference to progress dialogue.
        progressDialog = new ProgressDialog(activityContext);

        // Get reference to floating action button.
        FloatingActionButton fab = view.findViewById(R.id.fab_button);

        // Set on click listener to fab. And when it is clicked CategoryChooserActivity
        // will be opened to choose category.
        fab.setOnClickListener(this);

        // Get reference to recycler view.
        RecyclerView recyclerView = view.findViewById(R.id.category_recycler_view);

        // Initialise grid category adapter and listeners.
        gridCategoryAdapter = new GridCategoryAdapter(activityContext, shopCategoryList);

        // Get display width.
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int widthInDp = convertPixelsToDp(displayMetrics.widthPixels, displayMetrics);

        // Now get span count for grid layout.
        int spanCount = ((int) Math.floor(widthInDp / GRID_CATEGORY_WIDTH_WITH_MARGIN));

        // Make recycler layout manager to grid layout.
        recyclerView.setLayoutManager(new GridLayoutManager(activityContext, spanCount));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(gridCategoryAdapter);

        // Handle recycler view item click and long press.
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(activityContext,
                recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, int recyclerViewId) {

                // Start ProductDetailsActivity with supplying category name from here
                // to show the products.
                Intent intent = new Intent(activityContext, ProductDetailsActivity.class);
                intent.putExtra("Category", shopCategoryList.get(position).getCategoryName());
                startActivity(intent);
                // Apply left to right transition.
                getActivity().overridePendingTransition(R.anim.enter, R.anim.exit);

            }


            @Override
            public void onLongItemClick(View view, int position, int recyclerViewId) {
                showPopUpAndHandleOptionClick(view, position);
            }
        }));

        // Show progress dialogue while fetching the data.
        showLoadingDialogue("Loading Categories...");

        // Fetch firebase data and put that into shopCategoryList and notify the adapter.
        fetchDataAndNotifyAdapter();

        return view;
    }

    // Helper function to covert pixel into dp.
    private static int convertPixelsToDp(float px, DisplayMetrics metrics){
        return (int)Math.ceil((double)(px / ((float)metrics.densityDpi /
                DisplayMetrics.DENSITY_DEFAULT)));
    }

    // Helper function to show the popUp menu associated with given view.
    void showPopUpAndHandleOptionClick(final View view, final int position) {
        PopupMenu popUp = new PopupMenu(activityContext, view);
        popUp.inflate(R.menu.category_card_long_press_menu);
        popUp.setGravity(Gravity.CENTER);
        popUp.show();
        popUp.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.delete_card) {
                    String category = shopCategoryList.get(position).getCategoryName();
                    String title = "Delete " + category + " Category?";
                    String message = "Product associated with " + category + " category will be "
                            + "deleted permanently.\nAre you sure to delete " + category
                            + " category?";
                    showConfirmationDialogue(title, message, position);
                    return true;
                }
                return false;
            }
        });
    }

    // Helper function to fetch data from firebase and then add it to shopCategoryList
    // and then notify the adapter for data change.
    private void fetchDataAndNotifyAdapter() {
        // Get reference to ShopCategories.
        final DatabaseReference categoryImageRef = databaseReference.child("ShopCategories");

        // Fetch all categories for this particular seller.
        DatabaseReference tempReference = databaseReference.child("UNiTSellers").child(userKey)
                .child("categories");
        tempReference.keepSynced(true);
        tempReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    ArrayList<String> categories = ((ArrayList<String>) dataSnapshot.getValue());
                    if (categories != null) {
                        for (final String category : categories) {

                            // On every category find corresponding url.
                            Query temp = categoryImageRef.orderByKey().equalTo(category);
                            temp.keepSynced(true);
                            temp.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot != null) {
                                        // Now add both category name and respective url.
                                        HashMap<String, String> hashMap = ((HashMap<String, String>)
                                                dataSnapshot.getValue());
                                        if (hashMap != null) {
                                            shopCategoryList.add(new ShopCategory(category,
                                                    hashMap.get(category)));

                                            // Finally notify the adapter that data has been
                                            // changed and dismiss the progress dialogue
                                            // if showing.
                                            gridCategoryAdapter.notifyDataSetChanged();
                                            if (progressDialog.isShowing()){
                                                progressDialog.dismiss();
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Dismiss the progress dialogue if showing.
                                    if (progressDialog.isShowing()){
                                        progressDialog.dismiss();
                                    }

                                    /**
                                     * TODO Show some default image since we are unable to get it.
                                     */

                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Dismiss the progress dialogue if showing.
                if (progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                switch (databaseError.getCode()) {
                    case DatabaseError.NETWORK_ERROR:
                        showErrorMessageDialogue("Make sure you have internet" +
                                " connection through mobile data or wifi.");
                        break;
                    case DatabaseError.DISCONNECTED:
                        showErrorMessageDialogue("We are disconnected. Please" +
                                " try again in a little bit.");
                        break;
                    default: showErrorMessageDialogue("Snap! Some error has just happened " +
                            "during getting the categories. Please try again in a little bit.");
                }
            }
        });

    }

    // Helper function to show confirmation dialogue before any action.
    private void showConfirmationDialogue(final String title, final String message,
                                          final int position) {
        ConfirmBeforeItemDelete confirmDialog = new ConfirmBeforeItemDelete();
        Bundle bundle = new Bundle();
        bundle.putString("Message", message);
        bundle.putString("Title", title);
        bundle.putInt("Position", position);
        bundle.putString("FragmentTag", "Store");
        confirmDialog.setArguments(bundle);
        confirmDialog.show(getFragmentManager(), "ConfirmDialogue");
    }

    // Helper function to show the loading dialogue.
    private void showLoadingDialogue(final String loadingMessage) {
        progressDialog.setMessage(loadingMessage);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(false);
        progressDialog.show();
    }

    @Override
    public void communicate(final int position, final int recyclerViewId) {
        final ShopCategory shopCategory = shopCategoryList.get(position);

        // Delete the category from list.
        shopCategoryList.remove(position);
        showLoadingDialogue("Deleting the category...");

        // Delete that category from UNiTSellers database.
        DatabaseReference sellerCategoryUpdate = databaseReference.child("UNiTSellers")
                .child(userKey).child("categories");
        final ArrayList<String> categoryList = new ArrayList<>();
        int size = shopCategoryList.size();
        for (int i = 0; i < size; i++) {
            categoryList.add(shopCategoryList.get(i).getCategoryName());
        }
        sellerCategoryUpdate.setValue(categoryList)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        /**
                         * TODO Delete all associated product with this category by Cloud function.
                         */

                        // Dismiss progress dialogue if it is showing.
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                        // Notify the adapter for item removal.
                        gridCategoryAdapter.notifyItemRemoved(position);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        // Undo the local deletion. And notify the adapter.
                        shopCategoryList.add(shopCategory);
                        gridCategoryAdapter.notifyDataSetChanged();

                        // Dismiss progress dialogue if it is showing.
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                        // Show the error message dialogue.
                        showErrorMessageDialogue("Snap! Some error has happened during deletion." +
                                " Please try again in a little bit");
                    }
                });
        sellerCategoryUpdate.keepSynced(true);
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
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.fab_button) {
            Intent intent = new Intent(activityContext, CategoryChooserActivity.class);
            intent.putExtra("AddCategory", true);
            startActivityForResult(intent, ADD_CATEGORY_REQUEST);
            getActivity().overridePendingTransition(R.anim.enter, R.anim.exit);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_CATEGORY_REQUEST) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> categoryList = data.getStringArrayListExtra("Categories");
                if (categoryList.size() > 1) {
                    showLoadingDialogue("Creating selected category...");
                } else {
                    showLoadingDialogue("Creating selected categories...");
                }
                // Now insert the selected category/ categories into firebase database. And
                // notify the adapter that new category is added.
                insertCategoryInFirebaseAndNotifyAdapter(categoryList);
            }
        }
    }

    private void insertCategoryInFirebaseAndNotifyAdapter(
            final ArrayList<String> selectedCategoryList) {

        // Make a reference to ShopCategories database.
        final DatabaseReference catImageRef = databaseReference.child("ShopCategories");

        // Remove duplicate categories if user has selected same category once more.
        int size = selectedCategoryList.size();
        for (int i = 0; i < size; i++) {
            // Check whether selected category is present in the list already or not. If yes,
            // then remove it from the selectedCategoryList to avoid duplicates. In such a way
            // duplicated data will not be inserted in database.
            if (isPresentInList(selectedCategoryList.get(i))) {
                selectedCategoryList.remove(i);

                // Because you have removed current item so decrement size by 1 and also
                // decrement i by 1 to point to same location when it will be incremented
                // by for loop. In this way you can check all categories otherwise something
                // will be missed.
                i = i - 1;
                size = size - 1;
            }
        }

        // Now check if user has selected any unique category then add it to firebase. Otherwise
        // show him a error message that user has not selected any new category.
        if (selectedCategoryList.size() > 0) {
            // First add the selected category to UNiTSellers database.
            DatabaseReference temp = databaseReference.child("UNiTSellers").child(userKey)
                    .child("categories");

            ArrayList<String> catListToAdd = new ArrayList<>(selectedCategoryList);
            int beforeInsertCatListSize = shopCategoryList.size();

            // Add previously selected categories to the catListToAdd.
            for (int i = 0; i < beforeInsertCatListSize; i++) {
                catListToAdd.add(shopCategoryList.get(i).getCategoryName());
            }
            temp.setValue(catListToAdd)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    // Now fetch the category images for selected category/ categories.
                    @Override
                    public void onSuccess(Void aVoid) {
                        for (final String category : selectedCategoryList) {
                            Query catImageQuery = catImageRef.orderByKey().equalTo(category);
                            catImageQuery.keepSynced(true);
                            catImageQuery.addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                    boolean error = true;
                                    if (dataSnapshot != null) {
                                        // Now add both category name and respective url.
                                        Object obj = dataSnapshot.getValue();
                                        if (obj != null) {
                                            // Make error false here because we have successfully
                                            // read the data.
                                            error = false;
                                            shopCategoryList.add(new ShopCategory(category,
                                                    dataSnapshot.getValue().toString()));

                                            // Finally notify the adapter that data has been
                                            // changed.
                                            gridCategoryAdapter.notifyDataSetChanged();

                                            // Dismiss the progress dialogue if showing.
                                            if (progressDialog.isShowing()) {
                                                progressDialog.dismiss();
                                            }
                                        }
                                    }
                                    if (error) {
                                        // Dismiss progress dialogue if showing.
                                        if (progressDialog.isShowing()) {
                                            progressDialog.dismiss();
                                        }

                                        /**
                                         * TODO Show Some default image then because reading failed.
                                         */
                                    }
                                }

                                @Override
                                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onChildRemoved(DataSnapshot dataSnapshot) {

                                }

                                @Override
                                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Dismiss the progress dialogue if showing.
                                    if (progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }

                                    /**
                                     * TODO show default image if we are unable to get it.
                                     */
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Dismiss progress dialogue if it is showing.
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                        // Show the error message dialogue.
                        showErrorMessageDialogue("Snap! Some error has happened during adding " +
                                "the category. Please try again in a little bit");
                    }
                });
            temp.keepSynced(true);
        } else {
            // Dismiss the progress dialogue if it is showing.
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            showErrorMessageDialogue("You have not selected any new category. If you" +
                    " want to add more category then please select new category that you have " +
                    "not selected before.");
        }
    }

    // Helper function which check whether given category is present in shopCategoryList.
    private boolean isPresentInList(final String category) {
        int size = shopCategoryList.size();
        for (int i = 0; i < size; i++) {
            if (category.contentEquals(shopCategoryList.get(i).getCategoryName())) {
                return true;
            }
        }
        return false;
    }

}
