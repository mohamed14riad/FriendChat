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
import mohamed14riad.friendchat.adapters.RequestsListAdapter;
import mohamed14riad.friendchat.models.Profile;
import mohamed14riad.friendchat.utils.AppConstants;

public class RequestsFragment extends Fragment implements RequestsListAdapter.OnRequestClickListener {

    private FirebaseAuth firebaseAuth = null;
    private FirebaseUser user = null;

    private DatabaseReference usersReference = null;
    private DatabaseReference friendsReference = null;
    private DatabaseReference requestsReference = null;

    private ValueEventListener usersEventListener = null;
    private ValueEventListener requestsEventListener = null;

    private ArrayList<String> uIds = new ArrayList<>();
    private ArrayList<Profile> requests = new ArrayList<>();

    private Profile userProfile = null;

    private RecyclerView requestsRecyclerView = null;

    public RequestsFragment() {
        // Required empty public constructor
    }

    public static RequestsFragment newInstance() {
        return new RequestsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        usersReference = FirebaseDatabase.getInstance().getReference(AppConstants.DB_PATH_ALL);
        friendsReference = FirebaseDatabase.getInstance().getReference().child(AppConstants.DB_PATH_FRIENDS);
        requestsReference = FirebaseDatabase.getInstance().getReference(AppConstants.DB_PATH_REQUESTS);

        requestsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                uIds.clear();
                for (DataSnapshot dataSnapshotChild : dataSnapshot.getChildren()) {
                    String personUid = dataSnapshotChild.child("uId").getValue(String.class);
                    uIds.add(personUid);
                }

                getRequests();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        usersEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                requests.clear();
                while (iterator.hasNext()) {
                    DataSnapshot child = iterator.next();
                    Profile profile = child.getValue(Profile.class);
                    if (user.getUid().equals(profile.getUid())) {
                        userProfile = profile;
                    }
                    for (int i = 0; i < uIds.size(); i++) {
                        if (uIds.get(i).equals(profile.getUid())) {
                            requests.add(profile);
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

    private void getRequests() {
        usersReference.addValueEventListener(usersEventListener);
    }

    private void initViews() {
        if (requestsRecyclerView != null) {
            RequestsListAdapter adapter = new RequestsListAdapter(requests, getContext(), this);
            requestsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            requestsRecyclerView.setAdapter(adapter);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_requests, container, false);
        requestsRecyclerView = (RecyclerView) rootView.findViewById(R.id.requests_list);
        return rootView;
    }

    @Override
    public void onAcceptClick(int position) {
        Profile newFriend = requests.get(position);
        String newFriendId = requests.get(position).getUid();

        friendsReference.child(user.getUid()).child(newFriendId).setValue(newFriend);
        friendsReference.child(newFriendId).child(user.getUid()).setValue(userProfile);

        requestsReference.child(user.getUid()).child(newFriendId).removeValue();
        requestsReference.child(newFriendId).child(user.getUid()).removeValue();
    }

    @Override
    public void onDeclineClick(int position) {
        String personId = requests.get(position).getUid();

        requestsReference.child(user.getUid()).child(personId).removeValue();
    }

    @Override
    public void onResume() {
        super.onResume();

        requestsReference.child(user.getUid()).addValueEventListener(requestsEventListener);
    }

    @Override
    public void onPause() {
        usersReference.removeEventListener(usersEventListener);
        requestsReference.child(user.getUid()).removeEventListener(requestsEventListener);

        super.onPause();
    }
}
