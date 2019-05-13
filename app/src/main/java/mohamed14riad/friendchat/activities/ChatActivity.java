package mohamed14riad.friendchat.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import mohamed14riad.friendchat.R;
import mohamed14riad.friendchat.fragments.ChatFragment;
import mohamed14riad.friendchat.models.Profile;
import mohamed14riad.friendchat.utils.AppConstants;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class ChatActivity extends AppCompatActivity {

    private String friendId = null;

    private FirebaseAuth firebaseAuth = null;
    private FirebaseUser user = null;
    private DatabaseReference databaseReference = null;
    private ValueEventListener valueEventListener = null;

    private Toolbar toolbar = null;
    private ImageView otherImage = null;
    private TextView otherName = null;

    private Profile friend = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setToolbar();

        if (getIntent() != null && getIntent().hasExtra(AppConstants.KEY_FRIEND_ID)) {
            friendId = getIntent().getStringExtra(AppConstants.KEY_FRIEND_ID);
        } else {
            Toast.makeText(this, R.string.error_open_activity + ", Friend Id Is Null.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (findViewById(R.id.chat_container) != null) {
            ChatFragment chatFragment = ChatFragment.newInstance(friendId);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.chat_container, chatFragment, AppConstants.TAG_CHAT)
                    .commit();
        }

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        databaseReference = FirebaseDatabase.getInstance().getReference(AppConstants.DB_PATH_ALL).child(friendId);
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friend = dataSnapshot.getValue(Profile.class);
                updateToolbar();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.chat_toolbar);

        otherImage = (ImageView) toolbar.findViewById(R.id.otherPersonIcon);
        otherName = (TextView) toolbar.findViewById(R.id.otherPersonName);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle(null);
                actionBar.setDisplayShowTitleEnabled(false);
            }
        }
    }

    private void updateToolbar() {
        Glide.with(this)
                .load(friend.getPhoto())
                .apply(new RequestOptions().placeholder(R.drawable.ic_profile).error(R.drawable.ic_profile))
                .apply(bitmapTransform(new CircleCrop()))
                .into(otherImage);
        otherName.setText(friend.getName());
    }

    @Override
    protected void onResume() {
        super.onResume();

        databaseReference.addValueEventListener(valueEventListener);
    }

    @Override
    protected void onStop() {
        databaseReference.removeEventListener(valueEventListener);

        super.onStop();
    }
}
