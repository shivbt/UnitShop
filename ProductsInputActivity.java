package com.ikai.unitshop;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ikai.unitshop.CategoryProductFragments.BeverageFragment;
import com.ikai.unitshop.CategoryProductFragments.BreakLunchDinnerFragment;
import com.ikai.unitshop.CategoryProductFragments.CakeAndBakeryFragment;
import com.ikai.unitshop.CategoryProductFragments.ClothsFragment;
import com.ikai.unitshop.CategoryProductFragments.FastFoodFragment;
import com.ikai.unitshop.CategoryProductFragments.FootwearFragment;
import com.ikai.unitshop.CategoryProductFragments.FruitFragment;
import com.ikai.unitshop.CategoryProductFragments.GroceryFragment;
import com.ikai.unitshop.CategoryProductFragments.MedicineFragment;
import com.ikai.unitshop.CategoryProductFragments.NonVegFragment;
import com.ikai.unitshop.CategoryProductFragments.SweetFragment;
import com.ikai.unitshop.CategoryProductFragments.VegetableFragment;
import com.ikai.unitshop.DataModel.ProductFieldsGlobal;
import com.ikai.unitshop.DataModel.ProductFieldsInShop;

public class ProductsInputActivity extends AppCompatActivity implements
        ProductInputsFragToActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products_input);

        // Get selected category from the passed intent data.
        Intent intent = getIntent();
        String selectedCategory = intent.getStringExtra("Category");

        // Get passed classes i.e. ProductFieldsGlobal and ProductsFieldsInShop to the intent.
        ProductFieldsGlobal productFieldsGlobal = intent.getParcelableExtra("ProductGlobal");
        ProductFieldsInShop productFieldsInShop = intent.getParcelableExtra("ProductInShop");

        // If productFieldsGlobal is not null then insert the contents into a bundle.
        Bundle bundle = new Bundle();
        bundle.putString("Category", selectedCategory);
        if (productFieldsGlobal != null) {
            bundle.putParcelable("ProductGlobal", productFieldsGlobal);
        }

        // If productFieldsInShop is not null then insert the contents into bundle.
        if (productFieldsInShop != null) {
            bundle.putParcelable("ProductInShop", productFieldsInShop);
        }

        // Declare some fragment related objects.
        FragmentTransaction fragmentTransaction;
        FragmentManager fragmentManager = getSupportFragmentManager();

        // Check for 'Beverage' category.
        if (selectedCategory.contentEquals("Beverage")) {
            // Add beverage fragment content.
            fragmentTransaction = fragmentManager.beginTransaction();
            Fragment fragment = new BeverageFragment();

            // Set this bundle as the fragment argument.
            fragment.setArguments(bundle);

            // Here fragment tag is coupled so don't directly change it.
            fragmentTransaction.replace(R.id.activity_products_input, fragment, "Beverage");
            fragmentTransaction.commit();

        }

        // Check for 'Breakfast, Lunch & Dinner' category.
        if (selectedCategory.contentEquals("Breakfast, Lunch & Dinner")) {
            // Add beverage fragment content.
            fragmentTransaction = fragmentManager.beginTransaction();
            Fragment fragment = new BreakLunchDinnerFragment();

            // Set this bundle as the fragment argument.
            fragment.setArguments(bundle);

            // Here fragment tag is coupled so don't directly change it.
            fragmentTransaction.replace(R.id.activity_products_input, fragment, "BLD");
            fragmentTransaction.commit();

        }

        // Check for 'Cake & Bakery' category.
        if (selectedCategory.contentEquals("Cake & Bakery")) {
            // Add beverage fragment content.
            fragmentTransaction = fragmentManager.beginTransaction();
            Fragment fragment = new CakeAndBakeryFragment();

            // Set this bundle as the fragment argument.
            fragment.setArguments(bundle);

            // Here fragment tag is coupled so don't directly change it.
            fragmentTransaction.replace(R.id.activity_products_input, fragment, "CakeBakery");
            fragmentTransaction.commit();

        }

        // Check for 'Cloths' category.
        if (selectedCategory.contentEquals("Cloths")) {
            // Add beverage fragment content.
            fragmentTransaction = fragmentManager.beginTransaction();
            Fragment fragment = new ClothsFragment();

            // Set this bundle as the fragment argument.
            fragment.setArguments(bundle);

            // Here fragment tag is coupled so don't directly change it.
            fragmentTransaction.replace(R.id.activity_products_input, fragment, "Cloths");
            fragmentTransaction.commit();

        }

        // Check for 'Fast Food' category.
        if (selectedCategory.contentEquals("Fast Food")) {
            // Add beverage fragment content.
            fragmentTransaction = fragmentManager.beginTransaction();
            Fragment fragment = new FastFoodFragment();

            // Set this bundle as the fragment argument.
            fragment.setArguments(bundle);

            // Here fragment tag is coupled so don't directly change it.
            fragmentTransaction.replace(R.id.activity_products_input, fragment, "FastFood");
            fragmentTransaction.commit();

        }

        // Check for 'Footwear' category.
        if (selectedCategory.contentEquals("Footwear")) {
            // Add beverage fragment content.
            fragmentTransaction = fragmentManager.beginTransaction();
            Fragment fragment = new FootwearFragment();

            // Set this bundle as the fragment argument.
            fragment.setArguments(bundle);

            // Here fragment tag is coupled so don't directly change it.
            fragmentTransaction.replace(R.id.activity_products_input, fragment, "Footwear");
            fragmentTransaction.commit();

        }

        // Check for 'Fruit' category.
        if (selectedCategory.contentEquals("Fruit")) {
            // Add beverage fragment content.
            fragmentTransaction = fragmentManager.beginTransaction();
            Fragment fragment = new FruitFragment();

            // Set this bundle as the fragment argument.
            fragment.setArguments(bundle);

            // Here fragment tag is coupled so don't directly change it.
            fragmentTransaction.replace(R.id.activity_products_input, fragment, "Fruit");
            fragmentTransaction.commit();

        }

        // Check for 'Grocery' category.
        if (selectedCategory.contentEquals("Grocery")) {
            // Add beverage fragment content.
            fragmentTransaction = fragmentManager.beginTransaction();
            Fragment fragment = new GroceryFragment();

            // Set this bundle as the fragment argument.
            fragment.setArguments(bundle);

            // Here fragment tag is coupled so don't directly change it.
            fragmentTransaction.replace(R.id.activity_products_input, fragment, "Grocery");
            fragmentTransaction.commit();

        }

        // Check for 'Medicine' category.
        if (selectedCategory.contentEquals("Medicine")) {
            // Add beverage fragment content.
            fragmentTransaction = fragmentManager.beginTransaction();
            Fragment fragment = new MedicineFragment();

            // Set this bundle as the fragment argument.
            fragment.setArguments(bundle);

            // Here fragment tag is coupled so don't directly change it.
            fragmentTransaction.replace(R.id.activity_products_input, fragment, "Medicine");
            fragmentTransaction.commit();

        }

        // Check for 'Non-Veg' category.
        if (selectedCategory.contentEquals("Non-Veg")) {
            // Add beverage fragment content.
            fragmentTransaction = fragmentManager.beginTransaction();
            Fragment fragment = new NonVegFragment();

            // Set this bundle as the fragment argument.
            fragment.setArguments(bundle);

            // Here fragment tag is coupled so don't directly change it.
            fragmentTransaction.replace(R.id.activity_products_input, fragment, "NonVeg");
            fragmentTransaction.commit();

        }

        // Check for 'Sweet' category.
        if (selectedCategory.contentEquals("Sweet")) {
            // Add beverage fragment content.
            fragmentTransaction = fragmentManager.beginTransaction();
            Fragment fragment = new SweetFragment();

            // Set this bundle as the fragment argument.
            fragment.setArguments(bundle);

            // Here fragment tag is coupled so don't directly change it.
            fragmentTransaction.replace(R.id.activity_products_input, fragment, "Sweet");
            fragmentTransaction.commit();

        }

        // Check for 'Vegetable' category.
        if (selectedCategory.contentEquals("Vegetable")) {
            // Add beverage fragment content.
            fragmentTransaction = fragmentManager.beginTransaction();
            Fragment fragment = new VegetableFragment();

            // Set this bundle as the fragment argument.
            fragment.setArguments(bundle);

            // Here fragment tag is coupled so don't directly change it.
            fragmentTransaction.replace(R.id.activity_products_input, fragment, "Vegetable");
            fragmentTransaction.commit();

        }

    }

    @Override
    public void communicate(final ProductFieldsGlobal productFieldsGlobal
            , final ProductFieldsInShop productFieldsInShop) {

        // Send this product information to callee activity after checking that we
        // have some product information i.e. not null constraint. Otherwise simply
        // finish this activity.
        if (productFieldsGlobal != null && productFieldsInShop != null) {

            Intent intent = new Intent();
            intent.putExtra("ProductGlobal", productFieldsGlobal);
            intent.putExtra("ProductInShop", productFieldsInShop);
            setResult(RESULT_OK, intent);
            finish();

        } else {

            finish();

        }

    }
}
