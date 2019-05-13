package mohamed14riad.friendchat.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

import mohamed14riad.friendchat.R;
import mohamed14riad.friendchat.adapters.FavoritesListAdapter;
import mohamed14riad.friendchat.data.DatabaseHelper;
import mohamed14riad.friendchat.models.Profile;
import mohamed14riad.friendchat.utils.AppConstants;

public class FavoritesFragment extends Fragment implements FavoritesListAdapter.OnFavoriteClickListener {

    private FirebaseAuth firebaseAuth = null;
    private FirebaseUser user = null;

    private DatabaseHelper databaseHelper = null;

    private DatabaseReference friendsReference = null;

    private ValueEventListener friendsEventListener = null;

    private ArrayList<Profile> favorites = new ArrayList<>();

    private RecyclerView favoritesRecyclerView = null;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    public static FavoritesFragment newInstance() {
        return new FavoritesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        databaseHelper = new DatabaseHelper(getContext());

        friendsReference = FirebaseDatabase.getInstance().getReference(AppConstants.DB_PATH_FRIENDS).child(user.getUid());

        friendsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                favorites.clear();
                while (iterator.hasNext()) {
                    DataSnapshot child = iterator.next();
                    Profile friend = child.getValue(Profile.class);
                    if (friend.getFavorite()) {
                        favorites.add(friend);
                    }
                }

                initViews();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private void initViews() {
        if (favoritesRecyclerView != null) {
            FavoritesListAdapter adapter = new FavoritesListAdapter(favorites, getContext(), this);
            favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            favoritesRecyclerView.setAdapter(adapter);

            databaseHelper.deleteAll();
            databaseHelper.insertProfiles(favorites);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_favorites, container, false);
        favoritesRecyclerView = (RecyclerView) rootView.findViewById(R.id.favorites_list);
        return rootView;
    }

    @Override
    public void onFavoriteIconClick(int position) {
        String friendId = favorites.get(position).getUid();
        friendsReference.child(friendId).child("favorite").setValue(false);
    }

    @Override
    public void onResume() {
        super.onResume();

        friendsReference.addValueEventListener(friendsEventListener);
    }

    @Override
    public void onPause() {
        friendsReference.removeEventListener(friendsEventListener);

        super.onPause();
    }
}
