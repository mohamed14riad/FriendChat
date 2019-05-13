package mohamed14riad.friendchat.fragments;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import mohamed14riad.friendchat.R;
import mohamed14riad.friendchat.activities.ChatActivity;
import mohamed14riad.friendchat.adapters.FriendsListAdapter;
import mohamed14riad.friendchat.models.Profile;
import mohamed14riad.friendchat.utils.AppConstants;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static mohamed14riad.friendchat.activities.MainActivity.twoPaneMode;

public class FriendsFragment extends Fragment
        implements FriendsListAdapter.OnFriendClickListener {

    private FirebaseAuth firebaseAuth = null;
    private FirebaseUser user = null;

    private RecyclerView friendsRecyclerView = null;
    private LinearLayoutManager layoutManager = null;
    private FriendsListAdapter adapter = null;
    private FirebaseRecyclerOptions<Profile> options = null;

    private DatabaseReference friendsReference = null;
    private DatabaseReference userFriendsReference = null;

    private ValueEventListener friendsEventListener = null;

    private int selectedItem = -1;

    public FriendsFragment() {
        // Required empty public constructor
    }

    public static FriendsFragment newInstance() {
        return new FriendsFragment();
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(AppConstants.KEY_SELECTED_ITEM, selectedItem);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (savedInstanceState != null && savedInstanceState.containsKey(AppConstants.KEY_SELECTED_ITEM)) {
            selectedItem = savedInstanceState.getInt(AppConstants.KEY_SELECTED_ITEM, -1);
        }

        if (selectedItem == -1) {
            selectedItem = 0;
        }

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        layoutManager = new LinearLayoutManager(getContext());

        friendsReference = FirebaseDatabase.getInstance().getReference(AppConstants.DB_PATH_FRIENDS);
        friendsReference.keepSynced(true);

        userFriendsReference = friendsReference.child(user.getUid()).getRef();
        userFriendsReference.keepSynced(true);
//        userFriendsReference.orderByKey().limitToFirst(10);

        friendsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(user.getUid())) {
                    completeCreation();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private void completeCreation() {
        SnapshotParser<Profile> parser = new SnapshotParser<Profile>() {
            @NonNull
            @Override
            public Profile parseSnapshot(DataSnapshot dataSnapshot) {
                return dataSnapshot.getValue(Profile.class);
            }
        };

        options = new FirebaseRecyclerOptions.Builder<Profile>().setQuery(userFriendsReference, parser).build();

        initViews();
    }

    private void initViews() {
        if (friendsRecyclerView != null) {
            adapter = new FriendsListAdapter(options, getContext(), this);
            friendsRecyclerView.setLayoutManager(layoutManager);
            friendsRecyclerView.setAdapter(adapter);

            if (isConnected()) {
                // Start Listening...
                adapter.startListening();

                // Load Chat Fragment for the selected item
                if (twoPaneMode) {
                    if (adapter.getItemCount() != 0) {
                        ChatFragment.setFriendId(adapter.getItem(selectedItem).getUid());
                    }
                }
            } else {
                // Show Toast
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);

        friendsRecyclerView = (RecyclerView) rootView.findViewById(R.id.friends_list);

        if (twoPaneMode) {
            // Load Empty Chat Fragment until init views
            ChatFragment chatFragment = ChatFragment.newInstance(null);
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.chat_container, chatFragment, "Chat")
                    .commit();
        }

        return rootView;
    }

    @Override
    public void onFriendClick(int position) {
        selectedItem = position;
        String friendId = adapter.getItem(position).getUid();

        if (twoPaneMode) {
            // Load Chat Fragment For Selected Friend
            ChatFragment chatFragment = ChatFragment.newInstance(friendId);
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.chat_container, chatFragment, "Chat")
                    .commit();
        } else {
            // Open Chat Activity
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra(AppConstants.KEY_FRIEND_ID, friendId);
            startActivity(intent);
        }
    }

    @Override
    public void onFavoriteIconClick(int position) {
        Profile friend = adapter.getItem(position);

        if (friend.getFavorite()) {
            friendsReference.child(user.getUid()).child(friend.getUid()).child("favorite").setValue(false);
        } else {
            friendsReference.child(user.getUid()).child(friend.getUid()).child("favorite").setValue(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        friendsReference.addListenerForSingleValueEvent(friendsEventListener);
    }

    @Override
    public void onStop() {
        if (adapter != null) {
            adapter.stopListening();
        }

        friendsReference.removeEventListener(friendsEventListener);

        super.onStop();
    }
}
