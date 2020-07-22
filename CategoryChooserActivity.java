package com.ikai.unitshop;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.util.ArrayList;
import java.util.List;

import static com.ikai.unitshop.Constants.CAT_LIST;

public class CategoryChooserActivity extends AppCompatActivity {

    private List<ShopCategory> shopCategoryList = new ArrayList<>();
    private ShopCategoryAdapter shopCategoryAdapter;
    private static boolean[] choosedCategory;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shop_category_chooser);
        context = this;

        // Get reference to terms and conditions layout. So if this activity is called from
        // addProductFragment we can make that layout invisible because user has already
        // accepted terms and conditions during registration.
        LinearLayout termsAndConditionsLayout = findViewById(R.id.terms_conditions_layout);

        // Now check whether this activity is called from the addProductFragment or not.
        // If yes then make terms and conditions layout invisible.
        boolean isCalledFromAddProductFragment = getIntent().getBooleanExtra("AddCategory", false);
        if (isCalledFromAddProductFragment) {
            termsAndConditionsLayout.setVisibility(View.GONE);
        }

        // Get reference to recycler view and add some settings.
        RecyclerView recyclerView = findViewById(R.id.cat_recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Get reference to category adapter and set the adapter.
        shopCategoryAdapter = new ShopCategoryAdapter(shopCategoryList);
        recyclerView.setAdapter(shopCategoryAdapter);

        // Set flex box layout manager properties.
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(CategoryChooserActivity.this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setJustifyContent(JustifyContent.SPACE_AROUND);
        layoutManager.setAlignItems(AlignItems.FLEX_START);

        // Add flex box layout manager to recycler view.
        recyclerView.setLayoutManager(layoutManager);

        // Make an array of length of category and make all of its field to zero.
        choosedCategory = new boolean[CAT_LIST.length];
        for (int i = 0; i < choosedCategory.length; i++) {
            choosedCategory[i] = false;
        }

        // Set item click listener
        recyclerView.addOnItemTouchListener(
            new RecyclerItemClickListener(this, recyclerView,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position, int recyclerViewId) {
                        boolean isCatSelected = choosedCategory[position];
                        // If category is selected already then make it unselected and
                        // vice-versa
                        Button catButton = view.findViewById(R.id.category_button);
                        if (!isCatSelected) {
                            choosedCategory[position] = true;
                            catButton.setTextColor(ContextCompat.getColor(context,
                                    R.color.white));
                            catButton.setBackgroundResource(
                                    R.drawable.round_with_solid_fill);
                        } else {
                            choosedCategory[position] = false;
                            catButton.setTextColor(ContextCompat.getColor(context,
                                    R.color.colorPrimary));
                            catButton.setBackgroundResource(
                                    R.drawable.round_with_stroke_no_fill);
                        }
                    }

                    @Override
                    public void onLongItemClick(View view, int position, int recyclerViewId) {

                    }
                }
            )
        );


        /**
         * TODO Terms and condition clicking is not handled. So implement it.
         */

        // Insert all shop category into a list.
        prepareShopCategoryData();

        // Get reference of check box.
        final CheckBox checkBox = findViewById(R.id.term_condition_checked);
        // Make check box checked if this activity is called from addProductFragment
        // because user has already accepted terms and conditions during registration.
        if (isCalledFromAddProductFragment) {
            checkBox.setChecked(true);
        }

        // Set on click listener for button click.
        Button acceptButton = findViewById(R.id.accpet_and_continue_button);

        // Make accpet button text to "Add" if this activity is called from
        // addProductFragment because then word "Add" will be more suitable.
        if (isCalledFromAddProductFragment) {
            String add = "ADD";
            acceptButton.setText(add);
        }

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            // Check whether user has choosed some category
            if (isCategoryChoosed()) {
                // Check whether terms and conditions is checked or not. If yes then finish this
                // activity by setting result ok and providing choosed category data.
                if (checkBox.isChecked()) {
                    // Make an array list to store selected categories.
                    ArrayList<String> categories = new ArrayList<>();
                    for (int i = 0; i < CAT_LIST.length; i++) {
                        if (choosedCategory[i]) {
                            categories.add(CAT_LIST[i]);
                        }
                    }

                    // Send these selected categories to callee activity.
                    Intent intent = new Intent();
                    intent.putStringArrayListExtra("Categories", categories);
                    setResult(RESULT_OK, intent);
                    finish();

                } else {
                    showErrorMessageDialogue("Please check terms and conditions.");
                }
            } else {
                showErrorMessageDialogue("Please choose your Shop category.");
            }
            }
        });
    }

    // Helper function to check whether user has choosed some category or not.
    private boolean isCategoryChoosed() {
        for (int i = 0; i < choosedCategory.length; i++) {
            if (choosedCategory[i]) {
                return true;
            }
        }
        return false;
    }

    // Helper function to insert data in shopCategoryList.
    private void prepareShopCategoryData() {
        int i = 0;
        ShopCategory mShopCategory;
        for (; i < CAT_LIST.length; i++) {
            mShopCategory = new ShopCategory(CAT_LIST[i]);
            shopCategoryList.add(mShopCategory);
        }

        // Finally notify the adapter.
        shopCategoryAdapter.notifyDataSetChanged();

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
