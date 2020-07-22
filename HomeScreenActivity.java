package com.ikai.unitshop;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.ikai.unitshop.Constants.DRAWER_ITEM_ICONS_IDS;
import static com.ikai.unitshop.Constants.DRAWER_ITEM_NAMES;

public class HomeScreenActivity extends AppCompatActivity implements View.OnClickListener {

    private NavDrawerItemsAdapter navDrawerItemsAdapter;
    private List<NavDrawerItem> navDrawerItemList = new ArrayList<>();

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private boolean[] iconClick;
    private ImageView[] bottomNavBarIcons;
    private LinearLayout homeIconContainer, shopIconContainer, emergencyHelpIconContainer;
    private DatabaseReference mDatabaseReference;
    private RecyclerView recyclerView;
    private ProgressDialog progressDialog;
    private CircleImageView profilePhoto;
    private TextView profileName;

    // Declare a progress bar which is on profile photo.
    private ProgressBar progressBarOnProfilePhoto;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        // Set the custom toolbar.
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get reference to progress bar lying on profile photo.
        progressBarOnProfilePhoto = findViewById(R.id.progress_bar_profile_photo);

        // Get a reference to profile photo and profile name.
        profilePhoto = findViewById(R.id.profile_photo);
        profileName = findViewById(R.id.profile_name);

        // Get user name from the intent and set the profile name.
        profileName.setText(getIntent().getStringExtra("userName"));

        // Get profile photo url and load it into the profile photo using Glide.
        Glide.with(this)
                .load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
                .thumbnail(0.5f)
                .into(profilePhoto);

        // Get a reference to progress dialogue.
        progressDialog = new ProgressDialog(HomeScreenActivity.this);

        // Get a reference to firebase real-time database.
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // Check whether we can get support action bar or not. If yes then perform
        // navigation drawer settings.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Settings for navigation drawer.
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            mDrawerLayout = findViewById(R.id.drawer_layout);
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                    R.string.drawer_open, R.string.drawer_close) {
                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    invalidateOptionsMenu();
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);
                    invalidateOptionsMenu();
                }
            };
            mDrawerToggle.setDrawerIndicatorEnabled(true);
            mDrawerLayout.addDrawerListener(mDrawerToggle);

            // Get reference to recycler view that contains drawer items.
            recyclerView = findViewById(R.id.nav_drawer_items_recycler_view);
            navDrawerItemsAdapter = new NavDrawerItemsAdapter(navDrawerItemList);

            // Settings for recycler view
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            // Set adapter to recycler view.
            recyclerView.setAdapter(navDrawerItemsAdapter);

            // Set item click listener of recycler view.
            recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView,
                    new RecyclerItemClickListener.OnItemClickListener(){

                        @Override
                        public void onItemClick(View view, int position, int recyclerViewId) {
                            // Close the drawer.
                            mDrawerLayout.closeDrawer(GravityCompat.START);
                            /**
                             * Todo 1. handle different item click and launch respective activity.
                             */
                            // Handle different click and open activity related to that
                            // item click.
                            if (position == 0) {
                                // Check whether account is filled by the user or not. If yes
                                // show the non editable account details otherwise show the
                                // editable account details.
                                showAccountDetails();
                            }
                            if (position == 1) {

                            }
                            if (position == 2) {

                            }
                            if (position == 3) {

                            }
                            if (position == 4) {

                            }
                            if (position == 5) {

                            }

                        }

                        @Override
                        public void onLongItemClick(View view, int position, int recyclerViewId) {

                        }
                    }
            ));

            // Add navigation drawer items to the adapter.
            addDrawerItems();

        }

        // Initialise all bottom navigation icons containers.
        homeIconContainer = findViewById(R.id.home_icon_container);
        shopIconContainer = findViewById(R.id.shop_icon_container);
        emergencyHelpIconContainer = findViewById(R.id.emergency_help_container);

        // Get the reference to every icon.
        bottomNavBarIcons = new ImageView[3];
        bottomNavBarIcons[0] = findViewById(R.id.home_icon);
        bottomNavBarIcons[1] = findViewById(R.id.add_icon);
        bottomNavBarIcons[2] = findViewById(R.id.emergency_help_icon);

        // Variable to store icon click. First index for first icon and second for second
        // and so on...
        iconClick = new boolean[3];
        iconClick[0] = true;
        for (int i = 1; i < iconClick.length; i++) {
            iconClick[i] = false;
        }

        // Handle bottom navigation bar clicks and changing the icons on click and
        // others related to bottom navigation bar.
        handleBottomNavActivities();

        // Set onclick listener to profile photo.
        profilePhoto.setOnClickListener(this);

    }

    // Helper function to show account details if it is filled by the user otherwise
    // it will show a form to fill account details.
    private void showAccountDetails() {
        // Show progress dialogue during fetching the account details.
        showLoadingDialogue("Loading...");

        // Get account details from UNiTSellers database.
        DatabaseReference temp = mDatabaseReference.child("UNiTSellers")
                .child(FirebaseAuth.getInstance().getUid()).child("accountDetails");
        temp.keepSynced(true);
        temp.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Check whether we have got account details or not.
                Intent intent;
                if (dataSnapshot != null) {
                    AccountDetails accountDetails = dataSnapshot.getValue(AccountDetails.class);

                    // Check whether we have got account details.
                    if (accountDetails != null) {
                        intent = new Intent(HomeScreenActivity.this,
                                AccountDetailsNoEditActivity.class);

                    } else {
                        intent = new Intent(HomeScreenActivity.this,
                                AccountDetailsEditActivity.class);
                    }

                } else {
                    intent = new Intent(HomeScreenActivity.this, AccountDetailsEditActivity.class);
                }

                // Dismiss the progress dialogue if it is showing.
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                startActivity(intent);
                overridePendingTransition(R.anim.enter, R.anim.exit);
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

    // Helper function to show the error message.
    private void showErrorMessageDialogue(final String errorMessage) {
        ErrorMessageDialogue msgDialog = new ErrorMessageDialogue();
        Bundle bundle = new Bundle();
        bundle.putString("Message", errorMessage);
        msgDialog.setArguments(bundle);
        msgDialog.show(getSupportFragmentManager(), "Message");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Make progress bar on profile photo visible while loading the photo.
        if (progressBarOnProfilePhoto.getVisibility() == View.GONE) {
            progressBarOnProfilePhoto.setVisibility(View.VISIBLE);
        }

        // Get firebase user object.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Get profile photo url and load it into the profile photo using Glide.
            // Check whether we have photo url or not.
            Uri photoURL = user.getPhotoUrl();
            if (photoURL != null)
            {
                // Now load the image.
                Glide.with(this)
                        .load(photoURL)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model
                                    , Target<Drawable> target, boolean isFirstResource) {

                                // Make progress bar on profile photo non visible.
                                if (progressBarOnProfilePhoto.getVisibility() == View.VISIBLE) {
                                    progressBarOnProfilePhoto.setVisibility(View.GONE);
                                }
                                return false;

                            }

                            @Override
                            public boolean onResourceReady(Drawable resource
                                    , Object model, Target<Drawable> target
                                    , DataSource dataSource, boolean isFirstResource) {

                                // Make progress bar on profile photo non visible.
                                if (progressBarOnProfilePhoto.getVisibility() == View.VISIBLE) {
                                    progressBarOnProfilePhoto.setVisibility(View.GONE);
                                }
                                return false;

                            }
                        })
                        .thumbnail(0.5f)
                        .into(profilePhoto);

            }
            // Get username and set the profile name.
            profileName.setText(user.getDisplayName());
        }
    }

    // Helper function to show the loading dialogue.
    private void showLoadingDialogue(final String loadingMeassage) {
        progressDialog.setMessage(loadingMeassage);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(false);
        progressDialog.show();
    }

    private void addDrawerItems() {
        int i = 0;
        NavDrawerItem navDrawerItem;
        for (; i < DRAWER_ITEM_NAMES.length; i++) {
            navDrawerItem = new NavDrawerItem(DRAWER_ITEM_ICONS_IDS[i], DRAWER_ITEM_NAMES[i]);
            navDrawerItemList.add(navDrawerItem);
        }

        // Finally notify the adapter.
        navDrawerItemsAdapter.notifyDataSetChanged();

    }

    // Helper function to handle bottom navigation bar activities like clicking and all.
    private void handleBottomNavActivities() {
        // Get the width of display.
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        // Get each icon container width and set it.
        int eachIconWidth = width / 3;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(eachIconWidth,
                ViewGroup.LayoutParams.MATCH_PARENT);
        homeIconContainer.setLayoutParams(layoutParams);
        shopIconContainer.setLayoutParams(layoutParams);
        emergencyHelpIconContainer.setLayoutParams(layoutParams);

        // Set home content by default.
        setMainAreaContent(1);

        // Set click listener and handle on click event on home icon.
        handleHomeIconClick();

        // Set click listener and handle on click event on shop icon.
        handleShopIconClick();

        // Set click listener and handle on click event on emergency help icon.
        handleEmergencyHelpIconClick();

    }

    // Helper function to handle click on emergency help icon like handleHomeIconClick().
    private void handleEmergencyHelpIconClick() {
        emergencyHelpIconContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check whether it is clicked already or not. If not then change the icon
                // and content of main area of screen.
                if (!iconClick[2]) {
                    iconClick[2] = true;

                    // Condition for first icon.
                    if (iconClick[0]) {
                        iconClick[0] = false;
                        bottomNavBarIcons[0].setImageResource(R.drawable.ic_hm_n_active);
                    }

                    // Condition for second icon.
                    if (iconClick[1]) {
                        iconClick[1] = false;
                        bottomNavBarIcons[1].setImageResource(R.drawable.ic_store_n_active);
                    }

                    // Change current click icon to enable icon.
                    bottomNavBarIcons[2].setImageResource(R.drawable.ic_bell_active);

                    // Set main area to emergency help content.
                    setMainAreaContent(3);
                }
            }
        });
    }

    // Helper function to set content of main area.
    private void setMainAreaContent(final int fragmentCode) {
        // Here fragmentCode represent which fragment should be added.

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction;
        if (fragmentCode == 1) {
            // Add home fragment content.
            fragmentTransaction = fragmentManager.beginTransaction();
            Fragment fragment = new HomeFragment();
            fragmentTransaction.replace(R.id.main_area_container, fragment, "Home");
            fragmentTransaction.commit();
        }

        if (fragmentCode == 2) {
            // Add add product fragment content.
            fragmentTransaction = fragmentManager.beginTransaction();
            Fragment fragment = new AddProductFragment();
            fragmentTransaction.replace(R.id.main_area_container, fragment, "Store");
            fragmentTransaction.commit();
        }

        if (fragmentCode == 3) {
            // Add emergency help fragment content.
            fragmentTransaction = fragmentManager.beginTransaction();
            Fragment fragment = new EmergencyHelpFragment();
            fragmentTransaction.replace(R.id.main_area_container, fragment, "Emergency help");
            fragmentTransaction.commit();

        }
    }

    // Helper function to handle click on shop icon like handleHomeIconClick().
    private void handleShopIconClick() {
        shopIconContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check whether it is clicked already or not. If not then change the icon
                // and content of main area of screen.
                if (!iconClick[1]) {
                    iconClick[1] = true;
                    // Condition for first icon.
                    if (iconClick[0]) {
                        iconClick[0] = false;
                        bottomNavBarIcons[0].setImageResource(R.drawable.ic_hm_n_active);
                    }
                    // Condition for third icon.
                    if (iconClick[2]) {
                        iconClick[2] = false;
                        bottomNavBarIcons[2].setImageResource(R.drawable.ic_bell_n_active);
                    }

                    // Change current click icon to enabled icon.
                    bottomNavBarIcons[1].setImageResource(R.drawable.ic_store_active);

                    // Set main area content to shop fragment content.
                    setMainAreaContent(2);
                }
            }
        });
    }

    // Helper function to handle click on home icon.
    private void handleHomeIconClick() {
        homeIconContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check whether it is clicked already or not. If not then change the icon
                // and content of main area of screen.
                if (!iconClick[0]) {
                    // Change the iconClick[0] = true and change icon to show that it is enabled.
                    iconClick[0] = true;
                    // Change all other icon.
                    // Condition for second icon.
                    if (iconClick[1]) {
                        iconClick[1] = false;
                        bottomNavBarIcons[1].setImageResource(R.drawable.ic_store_n_active);
                    }

                    // Condition for third icon.
                    if (iconClick[2]) {
                        iconClick[2] = false;
                        bottomNavBarIcons[2].setImageResource(R.drawable.ic_bell_n_active);
                    }

                    // Change current click icon to enabled icon.
                    bottomNavBarIcons[0].setImageResource(R.drawable.ic_hm_active);

                    // Set main area content to home fragment.
                    setMainAreaContent(1);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        // Check click for profile photo.
        if (id == R.id.profile_photo) {
            // Open profile editor activity.
            Intent intent = new Intent(HomeScreenActivity.this, ProfileEditorActivity.class);
            startActivity(intent);
        }
    }
}
