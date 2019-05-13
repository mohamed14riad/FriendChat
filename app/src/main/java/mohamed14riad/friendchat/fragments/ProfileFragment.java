package mohamed14riad.friendchat.fragments;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Iterator;

import mohamed14riad.friendchat.R;
import mohamed14riad.friendchat.models.Profile;
import mohamed14riad.friendchat.utils.AppConstants;
import mohamed14riad.friendchat.utils.ImagePickerDialog;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class ProfileFragment extends Fragment
        implements View.OnClickListener, ImagePickerDialog.OnUriSelectListener {

    private ImageView profilePicture = null;
    private SwitchCompat switchCompat = null;

    private FirebaseAuth firebaseAuth = null;
    private FirebaseUser user = null;

    private DatabaseReference usersReference = null;
    private DatabaseReference friendsReference = null;

    private ValueEventListener usersEventListener = null;
    private ValueEventListener friendsEventListener = null;

    private ArrayList<String> friendsIds = new ArrayList<>();

    private Uri imageUri = null, tempUri = null;
    private String photoUrl;

    private boolean status, tempStatus = true;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        usersReference = FirebaseDatabase.getInstance().getReference().child(AppConstants.DB_PATH_ALL).child(user.getUid());
        friendsReference = FirebaseDatabase.getInstance().getReference().child(AppConstants.DB_PATH_FRIENDS);

        usersEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Profile profile = dataSnapshot.getValue(Profile.class);
                photoUrl = profile.getPhoto();
                status = profile.getStatus();

                refresh();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // ...
            }
        };

        friendsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> root = dataSnapshot.getChildren().iterator();
                friendsIds.clear();
                while (root.hasNext()) {
                    DataSnapshot friendsOfOneUser = root.next();

                    // check if current user is in friends of other users.
                    if (friendsOfOneUser.hasChild(user.getUid())) {
                        String id = friendsOfOneUser.getKey();
                        friendsIds.add(id);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        profilePicture = (ImageView) rootView.findViewById(R.id.profile_picture);
        profilePicture.setOnClickListener(this);

        Button saveButton = (Button) rootView.findViewById(R.id.saveProfileInfo);
        saveButton.setOnClickListener(this);

        switchCompat = (SwitchCompat) rootView.findViewById(R.id.status_switch);
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                tempStatus = isChecked;
            }
        });

        return rootView;
    }

    private void refresh() {
        if (imageUri != null) {
            Glide.with(this)
                    .load(imageUri)
                    .apply(new RequestOptions().placeholder(R.drawable.ic_profile).error(R.drawable.ic_profile))
                    .apply(bitmapTransform(new CircleCrop()))
                    .into(profilePicture);
        } else if (tempUri != null) {
            Glide.with(this)
                    .load(tempUri)
                    .apply(new RequestOptions().placeholder(R.drawable.ic_profile).error(R.drawable.ic_profile))
                    .apply(bitmapTransform(new CircleCrop()))
                    .into(profilePicture);
        } else {
            Glide.with(this)
                    .load(photoUrl)
                    .apply(new RequestOptions().placeholder(R.drawable.ic_profile).error(R.drawable.ic_profile))
                    .apply(bitmapTransform(new CircleCrop()))
                    .into(profilePicture);
        }

        switchCompat.setChecked(status);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.profile_picture) {
            displayImagePicker();
        } else if (view.getId() == R.id.saveProfileInfo) {
            if (user != null) {
                if (tempUri != null) {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference(AppConstants.STORAGE_PATH_IMAGES)
                            .child(user.getUid()).child(AppConstants.STORAGE_PATH_PROFILE);

                    new ImageTask(storageReference, tempUri).execute();
                }

                usersReference.child("status").setValue(tempStatus);
                for (int i = 0; i < friendsIds.size(); i++) {
                    friendsReference.child(friendsIds.get(i)).child(user.getUid()).child("status").setValue(tempStatus);
                }

                Toast.makeText(getContext(), R.string.udpate_message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void displayImagePicker() {
        ImagePickerDialog imagePickerDialog = ImagePickerDialog.newInstance(this);
        imagePickerDialog.show(getFragmentManager(), AppConstants.TAG_IMAGE_PICKER);
    }

    @Override
    public void onUriSelect(Uri imageUri) {
        this.imageUri = imageUri;
        if (imageUri != null) {
            this.tempUri = imageUri;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        usersReference.addValueEventListener(usersEventListener);
        friendsReference.addValueEventListener(friendsEventListener);
    }

    @Override
    public void onPause() {
        usersReference.removeEventListener(usersEventListener);
        friendsReference.removeEventListener(friendsEventListener);

        super.onPause();
    }

    private class ImageTask extends AsyncTask<Void, Void, Void> {

        StorageReference storageReference;
        Uri imageUri;

        public ImageTask(StorageReference storageReference, Uri imageUri) {
            this.storageReference = storageReference;
            this.imageUri = imageUri;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            UploadTask uploadTask = storageReference.putFile(imageUri);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String result = downloadUri.toString();

                        usersReference.child("photo").setValue(result);
                        for (int i = 0; i < friendsIds.size(); i++) {
                            friendsReference.child(friendsIds.get(i)).child(user.getUid()).child("photo").setValue(result);
                        }

                        imageUri = null;
                        tempUri = null;
                    } else {
                        // Handle failures...
                    }
                }
            });

            return null;
        }
    }
}
