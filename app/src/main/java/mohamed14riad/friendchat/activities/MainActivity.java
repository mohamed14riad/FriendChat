package mohamed14riad.friendchat.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import mohamed14riad.friendchat.R;
import mohamed14riad.friendchat.fragments.DiscoverFragment;
import mohamed14riad.friendchat.fragments.FavoritesFragment;
import mohamed14riad.friendchat.fragments.FriendsFragment;
import mohamed14riad.friendchat.fragments.ProfileFragment;
import mohamed14riad.friendchat.fragments.RequestsFragment;
import mohamed14riad.friendchat.models.Profile;
import mohamed14riad.friendchat.utils.AppConstants;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth firebaseAuth = null;
    private FirebaseUser user = null;
    private DatabaseReference databaseReference = null;

    private SharedPreferences sharedPreferences = null;

    public static boolean twoPaneMode;

    private NavigationView navigationView = null;
    private int selectedItem = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState != null && savedInstanceState.containsKey(AppConstants.KEY_SELECTED_ITEM)) {
            selectedItem = savedInstanceState.getInt(AppConstants.KEY_SELECTED_ITEM, -1);
        }

        if (selectedItem != -1) {
            onNavigationItemSelected(navigationView.getMenu().getItem(selectedItem));
        } else {
            navigationView.setCheckedItem(R.id.nav_home);
            navigationView.getMenu().performIdentifierAction(R.id.nav_home, 0);
        }

        if (findViewById(R.id.chat_container) != null) {
            twoPaneMode = true;
        } else {
            twoPaneMode = false;
        }

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference(AppConstants.DB_PATH_ALL);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (!sharedPreferences.getBoolean(AppConstants.KEY_FIRST_TIME.concat("_" + user.getEmail()), false)) {
            // one time code runs here
            addUserToDatabase();

            // first time has run.
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(AppConstants.KEY_FIRST_TIME.concat("_" + user.getEmail()), true);
            editor.apply();
        }
    }

    private void addUserToDatabase() {
        if (user != null) {
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(user.getUid())) {
                        Profile profile = new Profile(user.getUid(), user.getDisplayName(), user.getEmail(), "", true, false);
                        databaseReference.child(user.getUid()).setValue(profile);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(AppConstants.KEY_SELECTED_ITEM, selectedItem);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (selectedItem != 0) {
            navigationView.setCheckedItem(R.id.nav_home);
            navigationView.getMenu().performIdentifierAction(R.id.nav_home, 0);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sign_out) {
            signOutUser();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void signOutUser() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    }
                });
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            selectedItem = 0;
            // Handle the home action
            FriendsFragment friendsFragment = FriendsFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_content_container, friendsFragment, AppConstants.TAG_FRIENDS)
                    .commit();
        } else if (id == R.id.nav_profile) {
            selectedItem = 1;
            // Handle the profile action
            ProfileFragment profileFragment = ProfileFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_content_container, profileFragment, AppConstants.TAG_PROFILE)
                    .commit();
        } else if (id == R.id.nav_discover) {
            selectedItem = 2;
            // Handle the discover action
            DiscoverFragment discoverFragment = DiscoverFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_content_container, discoverFragment, AppConstants.TAG_DISCOVER)
                    .commit();
        } else if (id == R.id.nav_request) {
            selectedItem = 3;
            // Handle the requests action
            RequestsFragment requestsFragment = RequestsFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_content_container, requestsFragment, AppConstants.TAG_REQUESTS)
                    .commit();
        } else if (id == R.id.nav_favorites) {
            selectedItem = 4;
            // Handle the favorites action
            FavoritesFragment favoritesFragment = FavoritesFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_content_container, favoritesFragment, AppConstants.TAG_FAVORITES)
                    .commit();
        } else if (id == R.id.nav_share) {
            // Handle the share action
            startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(this)
                    .setType("text/plain")
                    .setSubject(getString(R.string.share_subject))
                    .setText(getString(R.string.share_text)) // I will change it later.
                    .getIntent(), getString(R.string.action_share)));
        } else if (id == R.id.nav_feedback) {
            // Handle the feedback action
            Intent mailToIntent = new Intent(Intent.ACTION_SEND);
            mailToIntent.setData(Uri.parse("mailto:"));
            mailToIntent.setType("text/plain");
            mailToIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.developer_email)});
            mailToIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject));
            startActivity(Intent.createChooser(mailToIntent, getString(R.string.send_mail)));
        } else if (id == R.id.nav_about) {
            // Handle the about action
            startActivity(new Intent(this, AboutActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
