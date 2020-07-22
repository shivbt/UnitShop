package com.ikai.unitshop;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.ikai.unitshop.DataModel.GetProductInShop;
import com.ikai.unitshop.DataModel.ProductFieldsGlobal;
import com.ikai.unitshop.DataModel.ProductFieldsInShop;
import com.ikai.unitshop.DataModel.SearchModel;
import com.ikai.unitshop.DialogFragments.NumberOfItemInputFrag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ikai.unitshop.Constants.GRID_CATEGORY_WIDTH_WITH_MARGIN;
import static com.ikai.unitshop.Constants.PRODUCT_CARD_HEIGHT;

public class ProductDetailsActivity extends AppCompatActivity implements View.OnClickListener
        , ConfirmBeforeItemDelete.Communicator {

    // Declare objects for layout.
    private FloatingActionButton addFab, searchFab, uploadFab;
    private Animation fabOpen, fabClose, rotateForward, rotateBackward;
    private boolean isFabOpen = false;
    private String clickedCategory;
    private View separator;
    private FrameLayout productArea;

    // Firebase database objects.
    private DatabaseReference mDatabaseReference;
    private DatabaseReference searchResultDatabaseReference;
    private DatabaseReference searchDatabaseReference;
    private DatabaseReference GETDatabaseReference;
    private DatabaseReference GETResultDatabaseReference;

    // Data model lists for product details in suggestion.
    private List<ProductFieldsGlobal> productFieldsGlobalSuggestionList = new ArrayList<>();
    private List<ProductFieldsInShop> productFieldsInShopSuggestionList = new ArrayList<>();

    // Data model lists for product details in product showing.
    private List<ProductFieldsGlobal> productFieldsGlobalList = new ArrayList<>();
    private List<ProductFieldsInShop> productFieldsInShopList = new ArrayList<>();

    // Data model lists to show result of search.
    private List<ProductFieldsGlobal> productFieldsGlobalClickedList = new ArrayList<>();
    private List<ProductFieldsInShop> productFieldsInShopClickedList = new ArrayList<>();

//    private LinearLayout resultContainer;
    private LinearLayout productNotAddedLayout;
    private RecyclerView productRecyclerView;

    // Variable to hold number of products that can be fitted the given display height.
    int numberOfFittedProduct;

    // Variable to store user id;
    private String userKey;

    // A boolean Variable to hold result whether fetchProductAndNotifyTheAdapter is called
    // one time or more time.
    private boolean fetchProductAndNotifyTheAdapterIsCalledOneTime = true;

    // Declare Adapter to show the product in productRecyclerView and
    // resultProductRecyclerView.
    private GridProductCardAdapter productRecycleViewAdapter;
    private GridProductCardAdapter resultProductRecycleViewAdapter;

    // Declare fields to show resulting product card.
    private RecyclerView resultProductRecyclerView;

    // Declare a progress dialog object. This will be needed when we show loading dialog.
    private ProgressDialog progressDialog;

    // Declare progress bar object.
    private ProgressBar progressBar;

    // Declare a TextView object that show that there is no more product to load.
    private TextView noMoreProductTextView;

    // Declare a variable to fetch products from that point.
    int from = 0;


//    // Declare field for result container.
//    private ImageView increaseButton, decreaseButton;
//    private ViewPager resultViewPager;
//    private TextView productNameText, companyNameText, netWeightOrQuantityText;
//    private CustomPagerAdapter resultCustomPagerAdapter;
//    private ArrayList<ImageView> dots = new ArrayList<>();
//    private LinearLayout dotsContainer;



    // Adapter for the suggestion box recycler view.
    private ProductSuggestionsAdapter suggestionInShopAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        // Get clicked category name from passed intent.
        clickedCategory = getIntent().getStringExtra("Category");

        // Initialize progress dialog object.
        progressDialog = new ProgressDialog(this);

        // Get reference to progress bar view.
        progressBar = findViewById(R.id.loading_bar);

        // Get reference to mic icon. And set On click listener on it.
        ImageView micIcon = findViewById(R.id.mic_icon);
        micIcon.setOnClickListener(this);

        // Get reference to no more products to load text view.
        noMoreProductTextView = findViewById(R.id.no_more_product_text);

//        // Get reference to result container to show the search result.
//        resultContainer = findViewById(R.id.result_container);
//
//        // Get references for result container fields.
//        increaseButton = findViewById(R.id.increase_button);
//        decreaseButton = findViewById(R.id.decrease_button);
//        resultViewPager = findViewById(R.id.view_pager);
//        productNameText = findViewById(R.id.product_name);
//        companyNameText = findViewById(R.id.company_name);
//        netWeightOrQuantityText = findViewById(R.id.product_net_weight_or_quantity);
//        dotsContainer = findViewById(R.id.dots_container);

        // Get reference to product not added text view to show if there is no
        // products in the shop.
        productNotAddedLayout = findViewById(R.id.product_not_added_layout);

        // Get reference to product details screen.
        productArea = findViewById(R.id.frame_layout);

        // Get reference to all floating action buttons.
        addFab = findViewById(R.id.fab_add_product);
        searchFab = findViewById(R.id.fab_search);
        uploadFab = findViewById(R.id.fab_upload);

        // Initialize all animations related to floating action buttons.
        fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close);
        rotateForward = AnimationUtils.loadAnimation(this, R.anim.rotate_forward);
        rotateBackward = AnimationUtils.loadAnimation(this, R.anim.rotate_backward);

        // Set click listeners for every floating action buttons.
        addFab.setOnClickListener(this);
        searchFab.setOnClickListener(this);
        uploadFab.setOnClickListener(this);

        // Get reference to firebase database.
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // Get user id form authentication database.
        userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get reference to search result firebase database.
        searchResultDatabaseReference = mDatabaseReference.child("ProductSearchResultInShop");

        // Get reference to firebase database where we can write data to search.
        searchDatabaseReference = mDatabaseReference.child("ProductSearchInShop/" + userKey);

        // Get reference to firebase database container where we can write 'GET' product command.
        GETDatabaseReference = mDatabaseReference.child("GetProductInShop/" + userKey);

        // Get reference to firebase database container where we get result of 'GET' command.
        GETResultDatabaseReference = mDatabaseReference.child("GetProductInShopResult/" + userKey);

        // Get reference to suggestion box recycler view and separator between search box
        // and suggestion box.
        final RecyclerView suggestionBox = findViewById(R.id.suggestion_recycler_view);
        separator = findViewById(R.id.separator);

        // Initialize the suggestion box adapter.
        suggestionInShopAdapter = new ProductSuggestionsAdapter(this,
                productFieldsGlobalSuggestionList);

        // Set suggestionInShopAdapter to suggestionBox.
        suggestionBox.setAdapter(suggestionInShopAdapter);

        // Handle item click in suggestion box.
        suggestionBox.addOnItemTouchListener(new RecyclerItemClickListener(this,
                suggestionBox, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, int recyclerViewId) {

                // Make suggestion box and separator non visible.
                if (separator.getVisibility() == View.VISIBLE) {
                    separator.setVisibility(View.GONE);
                }

                // Now show the result.
                showClickedProductAndClearList(position);

            }


            @Override
            public void onLongItemClick(View view, int position, int recyclerViewId) {

            }
        }));

        // Get reference to search box and add text change listener.
        AutoCompleteTextView searchBox = findViewById(R.id.search_box);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Check whether we have some keyword or not i.e. length of keyword > 0.
                if (charSequence.length() > 0) {

                    // Diminish other areas and only focus on search area.
                    productArea.setForeground(new ColorDrawable(ContextCompat
                            .getColor(ProductDetailsActivity.this, R.color.shade_color)));

                    // Search the entered string and show the suggestions.
                    searchAndShowSuggestions(charSequence.toString());

                } else {

                    // Now you can give focus to all other areas.
                    productArea.setForeground(null);

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Get reference to product recycler view.
        productRecyclerView = findViewById(R.id.product_details);

        // Get reference to resulting product recycler view.
        resultProductRecyclerView = findViewById(R.id.resulting_product_detail);

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

        // Get and set the adapter for productRecyclerView.
        productRecycleViewAdapter = new GridProductCardAdapter(
                ProductDetailsActivity.this,
                productFieldsInShopList,
                productFieldsGlobalList,
                new ProductAdapterListener()
        );
        productRecyclerView.setAdapter(productRecycleViewAdapter);

        // Set on item touch listener for productRecyclerView.
        productRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this,
                suggestionBox, new ProductClickAndLongClickListener()));

        // Make recycler layout manager for resultProductRecyclerView to grid layout.
        resultProductRecyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
        resultProductRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Set on item touch listener for resultProductRecyclerView.
        resultProductRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this,
                suggestionBox, new ProductClickAndLongClickListener()));

        // Get display height.
        int displayHeightInDp = convertPixelsToDp(displayMetrics.heightPixels, displayMetrics);

        // Now get number of products that can fit with that height window.
        numberOfFittedProduct = ((int)Math.floor(displayHeightInDp / PRODUCT_CARD_HEIGHT)) + 10;

        // Now fetch the products and notify the adapter. But first show loading bar.
        progressBar.setVisibility(View.VISIBLE);
        fetchProductAndNotifyTheAdapter();

    }

    // Helper function to fetch products from the specified location with number
    // of product specified by numberOfFittedProduct and notify the adapter.
    private void fetchProductAndNotifyTheAdapter() {

        // Make product recycler view visible if it not and all other recycler view non visible.
        if (productRecyclerView.getVisibility() == View.GONE) {
            productRecyclerView.setVisibility(View.VISIBLE);
        }

        // Instantiate the get product in shop model.
        final GetProductInShop productInShop = new GetProductInShop(from, numberOfFittedProduct);

        // Write get command on GETDatabaseReference.
        GETDatabaseReference.setValue(productInShop);

        // Now Listen for result.
        GETResultDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // If progress bar is visible make it non visible.
                if (progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(View.GONE);
                }

                if (dataSnapshot != null) {

                    // Get total value in the result so that we can know that how many
                    // products we got.
                    int total = dataSnapshot.child("total").getValue(int.class);
                    if (total > 0) {

                        // Get product global list.
                        List<ProductFieldsGlobal> tempGlobalList = dataSnapshot
                                .child("sources_global")
                                .getValue(productFieldsGlobalList.getClass());

                        // Get product in shop list.
                        List<ProductFieldsInShop> tempInShopList = dataSnapshot
                                .child("sources_local")
                                .getValue(productFieldsInShopList.getClass());

                        // Add tempGlobalList's contents to productFieldsGlobalList and
                        // tempInShopList's content to productFieldsInShopList.
                        int size = tempGlobalList.size();
                        for (int i = 0; i < size; i++) {
                            productFieldsGlobalList.add(tempGlobalList.get(i));
                            productFieldsInShopList.add(tempInShopList.get(i));

                            // finally notify the adapter.
                            productRecycleViewAdapter.notifyDataSetChanged();
                        }

                    } else {

                        // Check whether it is first time that fetchProductAndNotifyTheAdapter()
                        // is called or it is called upon scrolling i.e. more than one time.
                        // And according to that make respective layout visible.
                        if (fetchProductAndNotifyTheAdapterIsCalledOneTime) {
                            productNotAddedLayout.setVisibility(View.VISIBLE);
                        } else {
                            noMoreProductTextView.setVisibility(View.VISIBLE);
                        }

                    }


                } else {

                    if (fetchProductAndNotifyTheAdapterIsCalledOneTime) {
                        showErrorMessageDialogue("Some error has happened to get products." +
                                " Please try again in a little bit.");
                    } else {
                        showErrorMessageDialogue("Some error has happened to get more products." +
                                " Please try again in a little bit.");
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                // If progress bar is visible make it non visible.
                if (progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(View.GONE);
                }

                // Now show the error dialog.
                switch (databaseError.getCode()) {
                    case DatabaseError.DISCONNECTED:
                        showErrorMessageDialogue("Error in getting the products. " +
                                "You are disconnected. Please try again in a little bit.");
                        break;

                    case DatabaseError.NETWORK_ERROR:
                        showErrorMessageDialogue("Error in getting the products. " +
                                "Please make sure you have internet connection.");
                        break;

                    case DatabaseError.UNAVAILABLE:
                        showErrorMessageDialogue("Error in getting the products. " +
                                "Server is unavailable. We are sorry for that. Try " +
                                "again in a little bit.");
                        break;

                    default:
                        showErrorMessageDialogue("Error in getting the products. " +
                                "Some unknown error has just happened. Please try " +
                                "again in a little bit.");
                }

            }
        });


    }

    // Helper function to show the clicked product in suggestion box and clear the list.
    private void showClickedProductAndClearList(int clickedPosition) {

        // First clear the previous content from the lists if they have any content.
        if (productFieldsGlobalClickedList.size() > 0) {
            productFieldsGlobalClickedList.clear();
        }
        if (productFieldsInShopClickedList.size() > 0) {
            productFieldsInShopClickedList.clear();
        }

        // Get clicked product details in lists.
        productFieldsGlobalClickedList.add(productFieldsGlobalSuggestionList.get(clickedPosition));
        productFieldsInShopClickedList.add(productFieldsInShopSuggestionList.get(clickedPosition));

        // Now clear both suggestion lists. And notify the adapter.
        productFieldsGlobalSuggestionList.clear();
        productFieldsInShopSuggestionList.clear();
        // Notify the suggestion adapter.
        suggestionInShopAdapter.notifyDataSetChanged();

        // Now initialize adapter for resultProductRecyclerView.
        resultProductRecycleViewAdapter = new GridProductCardAdapter(
                ProductDetailsActivity.this
                , productFieldsInShopClickedList
                , productFieldsGlobalClickedList
                , new ResultProductAdapterListener()
        );

        // Make result layout visible and show the result. But before that make product
        // recycler view and product not found text area invisible.
        if (productRecyclerView.getVisibility() == View.VISIBLE) {
            productRecyclerView.setVisibility(View.GONE);
        }

        // Now make result layout visible.
        resultProductRecyclerView.setVisibility(View.VISIBLE);

        // Set the adapter to resultProductRecyclerView.
        resultProductRecyclerView.setAdapter(resultProductRecycleViewAdapter);

        // Notify the adapter.
        resultProductRecycleViewAdapter.notifyDataSetChanged();

    }

    // Helper function to search given string in firebase database and show suggestions.
    private void searchAndShowSuggestions(final String searchString) {
        // Write this search string in firebase where firebase cloud function will search
        // this string and store result back to firebase database.

        // Number of suggestion to show.
        int numberOfSuggestion = 6;

        // Insert searchString into database to search.
        SearchModel searchModel = new SearchModel(
                clickedCategory,
                searchString,
                numberOfSuggestion
        );
        searchDatabaseReference.setValue(searchModel);

        // Listen for the result from the search result.
        searchResultDatabaseReference.child(userKey)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        int totalResults = dataSnapshot.child("total").getValue(int.class);

                        // Check whether we got some hits of product or not.
                        if (totalResults > 0) {

                            // Clear the lists first if something there in the list.
                            if (productFieldsGlobalSuggestionList.size() > 0) {
                                productFieldsGlobalSuggestionList.clear();
                            }
                            if (productFieldsInShopSuggestionList.size() > 0) {
                                productFieldsInShopSuggestionList.clear();
                            }


                            /**
                             * TODO it's wrong we have to implement for many _sources. Check it from live demo.
                             */
                            // Get the products details.
                            productFieldsGlobalSuggestionList = dataSnapshot.child("sources_global")
                                    .getValue(productFieldsGlobalSuggestionList.getClass());
                            productFieldsInShopSuggestionList = dataSnapshot.child("sources_local")
                                    .getValue(productFieldsInShopSuggestionList.getClass());

                            // If productFieldsGlobalSuggestionList.size() > 0 then we
                            // have some results.
                            if (productFieldsGlobalSuggestionList.size() > 0) {

                                // Show the separator between suggestion box and search box.
                                separator.setVisibility(View.VISIBLE);
                                // Notify the adapter
                                suggestionInShopAdapter.notifyDataSetChanged();

                            } else {

                                // Otherwise make separator visibility gone.
                                separator.setVisibility(View.GONE);

                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                    // Make separator non visible if it's visible.
                    if (separator.getVisibility() == View.VISIBLE) {
                        separator.setVisibility(View.GONE);
                    }

                    // Show the error dialog containing the error.
//                    switch (databaseError.getCode()) {
//                        case DatabaseError.DISCONNECTED:
//                            showErrorMessageDialogue("Error in getting the suggestions. " +
//                                    "You are disconnected. Please try again in a little bit.");
//                            break;
//
//                        case DatabaseError.NETWORK_ERROR:
//                            showErrorMessageDialogue("Error in getting the suggestions. " +
//                                    "Please make sure you have internet connection.");
//                            break;
//
//                        case DatabaseError.UNAVAILABLE:
//                            showErrorMessageDialogue("Error in getting the suggestions. " +
//                                    "Server is unavailable. We are sorry for that. Try " +
//                                    "again in a little bit.");
//                            break;
//
//                        default:
//                            showErrorMessageDialogue("Error in getting the suggestions. " +
//                                    "Some unknown error has just happened. Please try " +
//                                    "again in a little bit.");
//                    }

                }
            }
        );

    }

    // Helper function to covert pixel into dp.
    private static int convertPixelsToDp(float px, DisplayMetrics metrics){
        return (int)Math.ceil((double)(px / ((float)metrics.densityDpi /
                DisplayMetrics.DENSITY_DEFAULT)));
    }

    // Helper function to animate floating action buttons.
    private void animateFabs() {
        // Check whether fab is opened or not and apply animation accordingly.
        if (isFabOpen) {

            // Rotate back the add (plus) icon on add fab.
            addFab.startAnimation(rotateBackward);

            // Close the opened fab i.e. search and upload fab.
            searchFab.startAnimation(fabClose);
            uploadFab.startAnimation(fabClose);

            // Make upload and search fab non clickable.
            searchFab.setClickable(false);
            uploadFab.setClickable(false);

            // set isFabOpen to false because fab is closed now.
            isFabOpen = false;

        } else {

            // Rotate back the add (plus) icon on add fab.
            addFab.startAnimation(rotateForward);

            // Open the closed fab i.e. search and upload fab.
            searchFab.startAnimation(fabOpen);
            uploadFab.startAnimation(fabOpen);

            // Make upload and search fab clickable.
            searchFab.setClickable(true);
            uploadFab.setClickable(true);

            // set isFabOpen to true because fab is opened now.
            isFabOpen = true;

        }
    }

    @Override
    public void onClick(View view) {
        // Store view id to check which view is clicked.
        int id = view.getId();

        // Check for add Fab click.
        if (id == R.id.fab_add_product) {
            // Animate the fab.
            animateFabs();
        }

        // Check for search fab click.
        if (id == R.id.fab_search) {
            // Animate the fab.
            animateFabs();

            // Open add product activity with search i.e. AddProductWithSearchActivity.
            Intent intent = new Intent(ProductDetailsActivity.this
                    , AddProductWithSearchActivity.class);

            // Put clicked category in intent.
            intent.putExtra("Category", clickedCategory);

            // Finally start the activity.
            startActivity(intent);

        }

        // Check for upload fab click.
        if (id == R.id.fab_upload) {
            // Animate the fab.
            animateFabs();
            /**
             * TODO open upload product activity here.
             */
        }

        // Check for mic icon click.
        if (id == R.id.mic_icon) {
            /**
             * Todo: Implement mic input functionality.
             */
        }

    }

    @Override
    public void onBackPressed() {

        // If result layout is visible make in non visible and don't exit. And make
        // product recycler view visible if is not. Otherwise simply finish() this activity.
        if (resultProductRecyclerView.getVisibility() == View.VISIBLE) {

            resultProductRecyclerView.setVisibility(View.GONE);
            if (productRecyclerView.getVisibility() == View.GONE) {
                productRecyclerView.setVisibility(View.VISIBLE);
            }

        } else {

            super.onBackPressed();
            overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);

        }

    }

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
                .child("UNiTSellerToProduct/" + userKey + "/" + clickedCategory + "/" + pushId)
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
                            ProductFieldsInShop temp = productFieldsInShopList.get(position);
                            temp.number_of_items = result;
                            // Now notify the adapter.
                            productRecycleViewAdapter.notifyDataSetChanged();

                        }
                    }
                }
            );

        }

        @Override
        public void onLastItemReached() {

            // Update the point from which we have to fetch the products i.e. update
            // the from variable.
            from = from + numberOfFittedProduct;

            // Show the progress bar to show that we are loading the products and
            // fetch the products.
            if (progressBar.getVisibility() == View.GONE) {
                progressBar.setVisibility(View.VISIBLE);
            }
            fetchProductAndNotifyTheAdapter();

        }
    }


    /**
     * Class that implement GridProductCardAdapter.OnViewClickListener for adapter
     * of resultProductRecyclerView.
     */
    class ResultProductAdapterListener implements GridProductCardAdapter.OnViewClickListener
            , NumberOfItemInputFrag.Communicator {

        private int position;

        @Override
        public void increaseButtonClick(final int position) {

            this.position = position;

            // Show loading dialog while incrementing and saving to database.
            showLoadingDialogue("Increasing quantity...");

            // Get increased result.
            String result = getResultAfterAddOrSubtract(position, true);

            // Save result to database.
            updateNumberOfItemsToDatabaseAndNotifyAdapter(result);

        }

        @Override
        public void decreaseButtonClick(final int position) {

            this.position = position;

            // Show loading dialog while decrementing and saving to database.
            showLoadingDialogue("Increasing quantity...");

            // Get decreased result.
            String result = getResultAfterAddOrSubtract(position, false);

            // Save result to database.
            updateNumberOfItemsToDatabaseAndNotifyAdapter(result);

        }

        @Override
        public void numberOfItemsClick(final int position) {

            this.position = position;

            // Show the dialog to take new value from the user.
            NumberOfItemInputFrag inputFragment = new NumberOfItemInputFrag();
            inputFragment.show(getSupportFragmentManager(), "NumberOfItemsUpdate");

        }

        // Helper function to update number of items in database and notify the adapter
        // if insertion to database successful.
        private void updateNumberOfItemsToDatabaseAndNotifyAdapter(final String result) {
            // Get product details first.
            final ProductFieldsInShop productFieldsInShop = productFieldsInShopClickedList
                    .get(position);

            // Get user key and push id.
            String userKey = productFieldsInShop.user_key;
            String pushId = productFieldsInShop.id;

            // Make a map to update number of items field.
            Map<String, Object> newData = new HashMap<>();
            newData.put("number_of_items", result);

            // Insert updated value in database.
            mDatabaseReference
                .child("UNiTSellerToProduct/" + userKey + "/" + clickedCategory + "/" + pushId)
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
                            ProductFieldsInShop temp = productFieldsInShopClickedList
                                    .get(position);
                            temp.number_of_items = result;
                            // Now notify the adapter.
                            resultProductRecycleViewAdapter.notifyDataSetChanged();

                        }
                    }
                }
            );

        }

        // Helper function to either add or subtract by 1 depending upon isAddition flag.
        // And return result after operation.
        private String getResultAfterAddOrSubtract(final int position, boolean isAddition) {
            // Get operand first.
            String operand = productFieldsInShopClickedList.get(position).number_of_items;

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
        public void communicate(final String newValue) {

            // Find out position at which the unit starts in number of items.
            String prevValueWithUnit = productFieldsInShopClickedList
                    .get(position).number_of_items;
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

        @Override
        public void onLastItemReached() {

        }
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
            Intent intent = new Intent(ProductDetailsActivity.this, ProductsInputActivity.class);

            // Put clicked category into intent extra.
            intent.putExtra("Category", clickedCategory);

            // Check id for productRecyclerView.
            if (recyclerViewId == R.id.product_details) {

                // Insert both local as well as global fields of product in the intent extra.
                // And start the ProductsInputActivity activity.
                intent.putExtra("ProductGlobal", productFieldsGlobalList.get(position));
                intent.putExtra("ProductInShop", productFieldsInShopList.get(position));
                startActivity(intent);

            }

            // Check id for resultProductRecyclerView.
            if (recyclerViewId == R.id.resulting_product_detail) {

                // Insert both local as well as global fields of result of search in the
                // intent extra. And start the ProductsInputActivity activity.
                intent.putExtra("ProductGlobal", productFieldsGlobalClickedList.get(position));
                intent.putExtra("ProductInShop", productFieldsInShopClickedList.get(position));
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
            PopupMenu popUp = new PopupMenu(ProductDetailsActivity.this, view);
            popUp.inflate(R.menu.category_card_long_press_menu);
            popUp.setGravity(Gravity.CENTER);
            popUp.show();
            popUp.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.delete_card) {

                        //Initialize an empty string to contain product name.
                        String productName = "";

                        // Check id for productRecyclerView.
                        if (recyclerViewId == R.id.product_details) {

                            // Get product name.
                            productName = productFieldsGlobalList.get(position).name;

                        }

                        // Check id for resultProductRecyclerView.
                        if (recyclerViewId == R.id.resulting_product_detail) {

                            // Get product name.
                            productName = productFieldsGlobalClickedList.get(position).name;

                        }

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
            String deleteRef = "/UNiTSellerToProduct/" + userKey + "/" + clickedCategory
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
                            productRecycleViewAdapter.notifyDataSetChanged();

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

        // Check id for resultProductRecyclerView.
        if (recyclerViewId == R.id.resulting_product_detail) {

            // Get user_key and id from ProductFieldsInShop data madel for clicked
            // product position.
            ProductFieldsInShop productFieldsInShop = productFieldsInShopClickedList.get(position);
            userKey = productFieldsInShop.user_key;
            id = productFieldsInShop.id;

            // Now delete the value from database at "/UNiTSellerToProduct/{userKey}/
            // {clickedCategory}/{id}". And add success and failure listener.
            String deleteRef = "/UNiTSellerToProduct/" + userKey + "/" + clickedCategory
                    + "/" + id;
            mDatabaseReference.child(deleteRef).removeValue().addOnSuccessListener(
                    new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            // Dismiss progress dialog if showing.
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }

                            // Remove that product from productFieldsGlobalClickedList as well as
                            // productFieldsInShopClickedList and notify the
                            // resultProductRecycleViewAdapter.
                            productFieldsInShopClickedList.remove(position);
                            productFieldsGlobalClickedList.remove(position);
                            resultProductRecycleViewAdapter.notifyDataSetChanged();

                            // Make result product layout non visible.
                            resultProductRecyclerView.setVisibility(View.GONE);

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
