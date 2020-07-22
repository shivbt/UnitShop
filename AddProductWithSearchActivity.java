package com.ikai.unitshop;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ikai.unitshop.Adapters.GridProductCardAdapter;
import com.ikai.unitshop.Adapters.ProductSuggestionsAdapter;
import com.ikai.unitshop.DataModel.ProductFieldsGlobal;
import com.ikai.unitshop.DataModel.ProductFieldsInShop;
import com.ikai.unitshop.DataModel.SearchModel;
import com.ikai.unitshop.DialogFragments.NumberOfItemInputFrag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ikai.unitshop.Constants.GRID_CATEGORY_WIDTH_WITH_MARGIN;

public class AddProductWithSearchActivity extends AppCompatActivity implements View.OnClickListener
        , ConfirmBeforeItemDelete.Communicator {

    // String object to hold category name.
    private String category;

    private View separator;
    private FrameLayout productArea;
    private TextView manuallyAddText;

    // Firebase database related objects.
    private DatabaseReference mDatabaseReference;
    private DatabaseReference searchDatabaseReference;
    private DatabaseReference searchResultDatabaseReference;

    // Data model lists for product details in suggestion.
    private List<ProductFieldsGlobal> productFieldsGlobalSuggestionList = new ArrayList<>();

    // Data model lists for product details in product recycler view.
    private List<ProductFieldsGlobal> productFieldsGlobalList = new ArrayList<>();
    private List<ProductFieldsInShop> productFieldsInShopList = new ArrayList<>();

    // String object to store user key.
    private String userKey;

    // Declare an object for ProductSuggestionsAdapter.
    private ProductSuggestionsAdapter productSuggestionsAdapter;

    // Declare an object for GridProductCardAdapter.
    private GridProductCardAdapter productCardAdapter;

    // Declare a static int variable for response code.
    private static int REQUEST_CODE = 601;

    // Declare a progress dialog object to show when we do some network operation.
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product_with_search);

        // Get category name form intent.
        category = getIntent().getStringExtra("Category");

        // Get user key from authentication.
        userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize progress dialog.
        progressDialog = new ProgressDialog(this);

        // Get search and search result database references.
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        searchDatabaseReference = mDatabaseReference.child("ProductSearchGlobal/" + userKey);
        searchResultDatabaseReference = mDatabaseReference
                .child("ProductSearchGlobalResult/" + userKey);

        // Get reference to each views.
        AutoCompleteTextView searchBox = findViewById(R.id.search_box);
        ImageView micIcon = findViewById(R.id.mic_icon);
        separator = findViewById(R.id.separator);
        RecyclerView suggestionRecyclerView = findViewById(R.id.suggestion_recycler_view);
        RecyclerView productRecyclerView = findViewById(R.id.product_details);
        productArea = findViewById(R.id.frame_layout);
        manuallyAddText = findViewById(R.id.manually_add_text);

        // Initialize productSuggestionsAdapter and set it to suggestionRecyclerView.
        productSuggestionsAdapter = new ProductSuggestionsAdapter(this
                , productFieldsGlobalSuggestionList);
        suggestionRecyclerView.setAdapter(productSuggestionsAdapter);

        // Get display width.
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayWidthInDp = convertPixelsToDp(displayMetrics.widthPixels, displayMetrics);

        // Now get span count for grid layout.
        int spanCount = ((int) Math.floor(displayWidthInDp / GRID_CATEGORY_WIDTH_WITH_MARGIN));
        /**
         * TODO Check whether above GRID_CATEGORY_WIDTH_WITH_MARGIN is right or not in terms of dp.
         */

        // Get grid layout manager for productRecyclerView and set it.
        productRecyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
        productRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Initialize productAdapter and set it to productRecyclerView.
        productCardAdapter = new GridProductCardAdapter(this
                , productFieldsInShopList
                , productFieldsGlobalList
                , new ProductAdapterListener());
        productRecyclerView.setAdapter(productCardAdapter);

        // Add text change listener to search box.
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Check whether we have some keyword or not i.e. length of keyword > 0.
                if (charSequence.length() > 0) {

                    // Diminish other areas and only focus on search area.
                    productArea.setForeground(new ColorDrawable(ContextCompat
                            .getColor(AddProductWithSearchActivity.this, R.color.shade_color)));

                    // Search the entered string and show the suggestions.
                    searchAndShowSuggestions(charSequence.toString());
                } else {
                    // Now you can give focus to all other areas.
                    productArea.setForeground(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        // Set on click listener for manuallyAddText.
        manuallyAddText.setOnClickListener(this);

        // Set on click listener for mic icon.
        micIcon.setOnClickListener(this);

        // Add on item touch listener for suggestion recycler view.
        suggestionRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this,
                suggestionRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, int recyclerViewId) {

                // Start ProductInputActivity and pass category and content of suggestion
                // to intent.
                Intent intent = new Intent(AddProductWithSearchActivity.this
                        , ProductsInputActivity.class);
                intent.putExtra("Category", category);
                intent.putExtra("ProductGlobal", productFieldsGlobalSuggestionList.get(position));

                // Clear the suggestion list and make separator not visible if visible
                // and notify the adapter.
                productFieldsGlobalSuggestionList.clear();
                if (separator.getVisibility() == View.VISIBLE) {
                    separator.setVisibility(View.GONE);
                }
                productSuggestionsAdapter.notifyDataSetChanged();

                // Now start the activity.
                startActivityForResult(intent, REQUEST_CODE);

            }

            @Override
            public void onLongItemClick(View view, int position, int recyclerViewId) { }

        })
        );

        // Add on item touch listener on productRecyclerView.
        productRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this,
                productRecyclerView, new ProductClickAndLongClickListener())
        );

    }

    // Helper function to covert pixel into dp.
    private static int convertPixelsToDp(float px, DisplayMetrics metrics){
        return (int)Math.ceil((double)(px / ((float)metrics.densityDpi /
                DisplayMetrics.DENSITY_DEFAULT)));
    }

    // Helper function to search given string in firebase database and show suggestions.
    private void searchAndShowSuggestions(final String searchString) {
        // Write this search string in firebase where firebase cloud function will search
        // this string and store result back to firebase database.

        // Number of suggestion to show.
        int numberOfSuggestion = 6;

        // Insert searchString into database to search.
        SearchModel searchModel = new SearchModel(
                category,
                searchString,
                numberOfSuggestion
        );
        searchDatabaseReference.setValue(searchModel);

        // Listen for the result from the search result.
        searchResultDatabaseReference.child(userKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        // If we have some suggestions previously then clear it.
                        if (productFieldsGlobalSuggestionList.size() > 0) {
                            productFieldsGlobalSuggestionList.clear();
                        }

                        if (dataSnapshot != null) {
                            int totalResults = dataSnapshot.child("total").getValue(int.class);

                            // Check whether we got some hits of product or not.
                            if (totalResults > 0) {

                                // Hide manually add text if visible.
                                if (manuallyAddText.getVisibility() == View.VISIBLE) {
                                    manuallyAddText.setVisibility(View.GONE);
                                }

                                productFieldsGlobalSuggestionList = dataSnapshot
                                        .child("sources")
                                        .getValue(productFieldsGlobalSuggestionList.getClass());

                                // Check whether we have some suggestions.
                                if (productFieldsGlobalSuggestionList.size() > 0) {

                                    // Show the separator and notify the adapter.
                                    if (separator.getVisibility() == View.GONE) {
                                        separator.setVisibility(View.VISIBLE);
                                    }
                                    // Now notify the adapter.
                                    productSuggestionsAdapter.notifyDataSetChanged();

                                } else {

                                    // Show the separator if not visible.
                                    if (separator.getVisibility() == View.GONE) {
                                        separator.setVisibility(View.VISIBLE);
                                    }

                                    // Show manually addition of product.
                                    if (manuallyAddText.getVisibility() == View.GONE) {
                                        manuallyAddText.setVisibility(View.VISIBLE);
                                    }

                                }

                            } else {

                                // Show the separator if not visible.
                                if (separator.getVisibility() == View.GONE) {
                                    separator.setVisibility(View.VISIBLE);
                                }

                                // Show manually addition of product.
                                if (manuallyAddText.getVisibility() == View.GONE) {
                                    manuallyAddText.setVisibility(View.VISIBLE);
                                }

                            }

                        } else {

                            // Hide the separator if visible.
                            if (separator.getVisibility() == View.VISIBLE) {
                                separator.setVisibility(View.GONE);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                        // Make separator non visible if it's visible.
                        if (separator.getVisibility() == View.VISIBLE) {
                            separator.setVisibility(View.GONE);
                        }

                    }
                }
        );

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check for request code that we have passed. And also check whether
        // result is ok or not.
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {

            // Get passed product details and show it to user.
            ProductFieldsGlobal productFieldsGlobal = data.getParcelableExtra("ProductGlobal");
            ProductFieldsInShop productFieldsInShop = data.getParcelableExtra("ProductInShop");

            // Add passed product into corresponding lists if both content is not null.
            if (productFieldsGlobal != null && productFieldsInShop != null) {

                productFieldsGlobalList.add(productFieldsGlobal);
                productFieldsInShopList.add(productFieldsInShop);

                // Now notify the adapter to show the added products
                productCardAdapter.notifyDataSetChanged();

            }

        }

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();  // Get id of the clicked view.

        // Check id for 'add manually' click.
        if (id == R.id.manually_add_text) {

            // Start the ProductInputActivity to fill the product details manually.
            Intent intent = new Intent(AddProductWithSearchActivity.this
                    , ProductsInputActivity.class);
            intent.putExtra("Category", category);
            startActivityForResult(intent, REQUEST_CODE);

        }

        // Check if for 'mic icon' click.
        if (id == R.id.mic_icon) {
            /**
             * Todo implement take search string from voice.
             */
        }

    }

    // Helper function to show the loading dialogue.
    private void showLoadingDialogue(final String loadingMessage) {
        progressDialog.setMessage(loadingMessage);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(false);
        progressDialog.show();
    }

    /**
     * Class that implement GridProductCardAdapter.OnViewClickListener for adapter
     * of productRecyclerView.
     */
    class ProductAdapterListener implements GridProductCardAdapter.OnViewClickListener
            , NumberOfItemInputFrag.Communicator {

        private int position;

        @Override
        public void increaseButtonClick(int position) {
            this.position = position;

            // Show loading dialog while incrementing and saving to database.
            showLoadingDialogue("Increasing quantity...");

            // Get increased result.
            String result = getResultAfterAddOrSubtract(position, true);

            // Save result to database.
            updateNumberOfItemsToDatabaseAndNotifyAdapter(result);
        }

        @Override
        public void decreaseButtonClick(int position) {
            this.position = position;

            // Show loading dialog while decrementing and saving to database.
            showLoadingDialogue("Increasing quantity...");

            // Get decreased result.
            String result = getResultAfterAddOrSubtract(position, false);

            // Save result to database.
            updateNumberOfItemsToDatabaseAndNotifyAdapter(result);
        }

        @Override
        public void numberOfItemsClick(int position) {
            this.position = position;

            // Show the dialog to take new value from the user.
            NumberOfItemInputFrag inputFragment = new NumberOfItemInputFrag();
            inputFragment.show(getSupportFragmentManager(), "NumberOfItemsUpdate");
        }

        // Helper function to either add or subtract by 1 depending upon isAddition flag.
        // And return result after operation.
        private String getResultAfterAddOrSubtract(final int position, boolean isAddition) {
            // Get operand first.
            String operand = productFieldsInShopList.get(position).number_of_items;

            // Now extract integer value from the operand.
            String intResult = "";
            int length = operand.length();
            int characterPositionInOperand = 0;
            for (int i = 0; i < length; i++) {

                int asciiValue = (int) operand.charAt(i);

                // Check whether current character is number. If so then append it to
                // intResult string otherwise simply break the loop and save the position
                // it will be used later, because in number of items we can see number first
                // no other character can come between two numbers.
                if (asciiValue >= 48 && asciiValue <= 57) {
                    intResult = intResult + operand.charAt(i);
                } else {
                    characterPositionInOperand = i;
                    break;
                }

            }
            // Parse int value form intResult  string.
            int intOperand = Integer.parseInt(intResult);

            // Now if isAddition is true then increment the intOperand by 1 value otherwise
            // it will be subtraction so decrement intOperand by 1.
            if (isAddition) {
                intOperand = intOperand + 1;
            } else {
                intOperand = intOperand - 1;
            }

            // Now combine resultant intOperand with unit of number of items.
            String resultString = "" + intOperand;
            for (int i = characterPositionInOperand; i < length; i++) {
                resultString = resultString + operand.charAt(i);
            }

            // Finally return the result with unit of number of items.
            return resultString;

        }

        @Override
        public void communicate(String newValue) {
            // Find out position at which the unit starts in number of items.
            String prevValueWithUnit = productFieldsInShopList.get(position).number_of_items;
            int size = prevValueWithUnit.length();
            int i = 0;
            for (; i < size; i++){
                int asciiValue = (int) prevValueWithUnit.charAt(i);
                // Check when we get a non number value. And break the loop.
                if (asciiValue > 57) {
                    break;
                }
            }

            // Now make a result string with previous given unit.
            String result = "" + newValue;
            for (; i < size; i++) {
                result = result + prevValueWithUnit.charAt(i);
            }

            // Show loading dialog while updating number of items in the database.
            showLoadingDialogue("Updating quantity...");

            // Now save it to database and notify the adapter.
            updateNumberOfItemsToDatabaseAndNotifyAdapter(result);
        }

        // Helper function to update number of items in database and notify the adapter
        // if insertion to database successful.
        private void updateNumberOfItemsToDatabaseAndNotifyAdapter(final String result) {
            // Get product details first.
            final ProductFieldsInShop productFieldsInShop = productFieldsInShopList.get(position);

            // Get user key and push id.
            String userKey = productFieldsInShop.user_key;
            String pushId = productFieldsInShop.id;

            // Make a map to update number of items field.
            Map<String, Object> newData = new HashMap<>();
            newData.put("number_of_items", result);

            // Insert updated value in database.
            mDatabaseReference
                    .child("UNiTSellerToProduct/" + userKey + "/" + category + "/" + pushId)
                    .updateChildren(newData, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError,
                                                       DatabaseReference databaseReference) {

                                    // Dismiss the progress dialog if showing.
                                    if (progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }

                                    // If some error has happened then show the error else update
                                    // the value in the list and notify the adapter.
                                    if (databaseError != null) {

                                        // Show the error.
                                        showDatabaseErrorInQuantityUpdate(databaseError);

                                    } else {

                                        // Update the number of items in the list also and notify
                                        // the adapter.
                                        ProductFieldsInShop temp = productFieldsInShopList
                                                .get(position);
                                        temp.number_of_items = result;
                                        // Now notify the adapter.
                                        productCardAdapter.notifyDataSetChanged();

                                    }
                                }
                            }
                    );

        }

        @Override
        public void onLastItemReached() { }

        // Helper function to show error occurred during database fetching.
        private void showDatabaseErrorInQuantityUpdate(final DatabaseError databaseError) {
            switch (databaseError.getCode()) {
                case DatabaseError.DISCONNECTED:
                    showErrorMessageDialogue("Error in updating the quantity. " +
                            "You are disconnected. Please try again in a little bit.");
                    break;

                case DatabaseError.NETWORK_ERROR:
                    showErrorMessageDialogue("Error in updating the quantity. " +
                            "Please make sure you have internet connection.");
                    break;

                case DatabaseError.UNAVAILABLE:
                    showErrorMessageDialogue("Error in updating the quantity. " +
                            "Server is unavailable. We are sorry for that. Try " +
                            "again in a little bit.");
                    break;

                default:
                    showErrorMessageDialogue("Error in updating the quantity. " +
                            "Some unknown error has just happened. Please try " +
                            "again in a little bit.");
            }
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

    /**
     * Class to handle on item click and long click for product recycler view i.e.
     * productRecyclerView and for result product recycler view i.e. resultProductRecyclerView.
     */
    class ProductClickAndLongClickListener implements
            RecyclerItemClickListener.OnItemClickListener {

        @Override
        public void onItemClick(View view, int position, int recyclerViewId) {

            // Initialize an intent object to start ProductsInputActivity.
            Intent intent = new Intent(AddProductWithSearchActivity.this
                    , ProductsInputActivity.class);

            // Put clicked category into intent extra.
            intent.putExtra("Category", category);

            // Check id for productRecyclerView.
            if (recyclerViewId == R.id.product_details) {

                // Insert both local as well as global fields of product in the intent extra.
                // And start the ProductsInputActivity activity.
                intent.putExtra("ProductGlobal", productFieldsGlobalList.get(position));
                intent.putExtra("ProductInShop", productFieldsInShopList.get(position));
                startActivity(intent);

            }

        }

        @Override
        public void onLongItemClick(View view, int position, int recyclerViewId) {

            // Show pop up to delete the product and manage option click.
            showPopUpAndHandleOptionClick(view, position, recyclerViewId);

        }

        // Helper function to show the popUp menu associated with given view.
        void showPopUpAndHandleOptionClick(final View view, final int position
                , final int recyclerViewId ) {

            // Initialize pop up and it's content and show it.
            PopupMenu popUp = new PopupMenu(AddProductWithSearchActivity.this, view);
            popUp.inflate(R.menu.category_card_long_press_menu);
            popUp.setGravity(Gravity.CENTER);
            popUp.show();
            popUp.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.delete_card) {

                        // Get product name.
                        String productName = productFieldsGlobalList.get(position).name;

                        // Initialize details for confirmation dialog.
                        String title = "Delete " + productName + " Product?";
                        String message = "Product will be deleted permanently.\nAre you sure" +
                                " to delete " + productName + " product?";
                        showConfirmationDialogue(title, message, position, recyclerViewId);
                        return true;

                    }
                    return false;
                }
            });

        }

        // Helper function to show confirmation dialogue before any action.
        private void showConfirmationDialogue(final String title, final String message,
                                              final int position, final int recyclerViewId) {
            ConfirmBeforeItemDelete confirmDialog = new ConfirmBeforeItemDelete();
            Bundle bundle = new Bundle();
            bundle.putString("Message", message);
            bundle.putString("Title", title);
            bundle.putInt("Position", position);
            bundle.putInt("RecyclerViewId", recyclerViewId);
            confirmDialog.setArguments(bundle);
            confirmDialog.show(getSupportFragmentManager(), "ConfirmDialogue");
        }

    }

    // Callback from ConfirmBeforeDeletion Fragment.
    @Override
    public void communicate(final int position, final int recyclerViewId) {

        // Show loading dialog while deleting.
        showLoadingDialogue("Deleting the product...");

        // Declare string variables to hold user_key and id. These will be used get reference
        // in firebase database.
        String userKey, id;

        // Check id for productRecyclerView.
        if (recyclerViewId == R.id.product_details) {

            // Get user_key and id from ProductFieldsInShop data madel for clicked
            // product position.
            ProductFieldsInShop productFieldsInShop = productFieldsInShopList.get(position);
            userKey = productFieldsInShop.user_key;
            id = productFieldsInShop.id;

            // Now delete the value from database at "/UNiTSellerToProduct/{userKey}/
            // {clickedCategory}/{id}". And add success and failure listener.
            String deleteRef = "/UNiTSellerToProduct/" + userKey + "/" + category
                    + "/" + id;
            mDatabaseReference.child(deleteRef).removeValue().addOnSuccessListener(
                    new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            // Dismiss progress dialog if showing.
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }

                            // Remove that product from productFieldsGlobalList as well as
                            // productFieldsInShopList and notify the productRecycleViewAdapter.
                            productFieldsInShopList.remove(position);
                            productFieldsGlobalList.remove(position);
                            productCardAdapter.notifyDataSetChanged();

                        }
                    }
            ).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    // Dismiss progress dialog if showing.
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                    // Now show the error message showing that deletion is unsuccessful.
                    showErrorMessageDialogue("Snap! Some error just happened while deleting" +
                            " the product. Please try again in a little bit.");

                }
            });

        }

    }

}
