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
import mohamed14riad.friendchat.adapters.DiscoverListAdapter;
import mohamed14riad.friendchat.models.Profile;
import mohamed14riad.friendchat.utils.AppConstants;

public class DiscoverFragment extends Fragment
        implements DiscoverListAdapter.OnPersonClickListener {

    private FirebaseAuth firebaseAuth = null;
    private FirebaseUser user = null;

    private DatabaseReference usersReference = null;
    private DatabaseReference friendsReference = null;

    private DatabaseReference requestsReference = null;

    private ValueEventListener usersEventListener = null;
    private ValueEventListener friendsEventListener = null;

    private ArrayList<String> uIds = new ArrayList<>();
    private ArrayList<Profile> people = new ArrayList<>();

    private RecyclerView discoverRecyclerView = null;

    public DiscoverFragment() {
        // Required empty public constructor
    }

    public static DiscoverFragment newInstance() {
        return new DiscoverFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        usersReference = FirebaseDatabase.getInstance().getReference(AppConstants.DB_PATH_ALL);
        friendsReference = FirebaseDatabase.getInstance().getReference().child(AppConstants.DB_PATH_FRIENDS).child(user.getUid());
        requestsReference = FirebaseDatabase.getInstance().getReference(AppConstants.DB_PATH_REQUESTS);

        friendsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                uIds.clear();
                for (DataSnapshot dataSnapshotChild : dataSnapshot.getChildren()) {
                    String personUid = dataSnapshotChild.child("uid").getValue(String.class);
                    uIds.add(personUid);
                }

                discoverPeople();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        usersEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                people.clear();
                while (iterator.hasNext()) {
                    DataSnapshot child = iterator.next();
                    final Profile profile = child.getValue(Profile.class);
                    if (!user.getUid().equals(profile.getUid())) {
                        boolean inFriends = false;
                        for (int i = 0; i < uIds.size(); i++) {
                            if (uIds.get(i).equals(profile.getUid())) {
                                inFriends = true;
                            }
                        }

                        if (!inFriends) {
                            people.add(profile);
                        }
                    }
                }

                initViews();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private void discoverPeople() {
        usersReference.addValueEventListener(usersEventListener);
    }

    private void initViews() {
        if (discoverRecyclerView != null) {
            DiscoverListAdapter adapter = new DiscoverListAdapter(people, getContext(), this);
            discoverRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            discoverRecyclerView.setAdapter(adapter);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_discover, container, false);
        discoverRecyclerView = (RecyclerView) rootView.findViewById(R.id.discover_list);
        return rootView;
    }

    @Override
    public void onAddButtonClick(int position) {
        String senderId = user.getUid();
        String receiverId = people.get(position).getUid();

        requestsReference.child(receiverId).child(senderId).child("uId").setValue(senderId);
    }

    @Override
    public void onResume() {
        super.onResume();

        friendsReference.addValueEventListener(friendsEventListener);
    }

    @Override
    public void onPause() {
        usersReference.removeEventListener(usersEventListener);
        friendsReference.removeEventListener(friendsEventListener);

        super.onPause();
    }
}
