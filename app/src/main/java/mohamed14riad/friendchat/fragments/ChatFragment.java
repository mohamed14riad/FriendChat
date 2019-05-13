package mohamed14riad.friendchat.fragments;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
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

import mohamed14riad.friendchat.R;
import mohamed14riad.friendchat.adapters.ChatListAdapter;
import mohamed14riad.friendchat.models.Message;
import mohamed14riad.friendchat.models.Profile;
import mohamed14riad.friendchat.utils.AppConstants;
import mohamed14riad.friendchat.utils.ImagePickerDialog;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class ChatFragment extends Fragment
        implements View.OnClickListener, ImagePickerDialog.OnUriSelectListener {

    private static String friendId;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    private RecyclerView chatRecyclerView = null;
    private LinearLayoutManager layoutManager = null;
    private ChatListAdapter adapter = null;
    private FirebaseRecyclerOptions<Message> options = null;

    private DatabaseReference usersReference = null;
    private DatabaseReference databaseReference = null;

    private ValueEventListener usersEventListener = null;
    private ValueEventListener chatEventListener = null;

    private int pathType = 0;
    private String pathType0, pathType1;

    private ImageButton imagesBtn = null, sendButton = null;
    private EditText chatText = null;

    private Profile userProfile = null;

    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";

    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance(String friendId) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(AppConstants.KEY_FRIEND_ID, friendId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (getArguments() != null && getArguments().containsKey(AppConstants.KEY_FRIEND_ID)) {
            friendId = getArguments().getString(AppConstants.KEY_FRIEND_ID);
        } else {
            // show Toast
            return;
        }

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);

        usersReference = FirebaseDatabase.getInstance().getReference(AppConstants.DB_PATH_ALL).child(user.getUid());
        databaseReference = FirebaseDatabase.getInstance().getReference(AppConstants.DB_PATH_CHAT);
        databaseReference.keepSynced(true);

        usersEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userProfile = dataSnapshot.getValue(Profile.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        chatEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(user.getUid() + "_" + friendId)) {
                    pathType = 0;
                    pathType0 = user.getUid() + "_" + friendId;
                } else if (dataSnapshot.hasChild(friendId + "_" + user.getUid())) {
                    pathType = 1;
                    pathType1 = friendId + "_" + user.getUid();
                } else {
                    pathType = 0;
                    pathType0 = user.getUid() + "_" + friendId;
                }

                completeCreation();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private void completeCreation() {
        SnapshotParser<Message> parser = new SnapshotParser<Message>() {
            @NonNull
            @Override
            public Message parseSnapshot(DataSnapshot dataSnapshot) {
                Message message = dataSnapshot.getValue(Message.class);
                if (message != null) {
                    message.setId(dataSnapshot.getKey());
                }
                return message;
            }
        };

        DatabaseReference messagesRef;
        switch (pathType) {
            case 0:
                messagesRef = databaseReference.child(pathType0);
                break;
            case 1:
                messagesRef = databaseReference.child(pathType1);
                break;
            default:
                messagesRef = databaseReference.child(pathType0);
                break;
        }

        messagesRef.keepSynced(true);
//        messagesRef.limitToLast(10);

        options = new FirebaseRecyclerOptions.Builder<Message>().setQuery(messagesRef, parser).build();

        initViews();
    }

    private void initViews() {
        if (chatRecyclerView != null) {
            adapter = new ChatListAdapter(options, getContext());
            adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    super.onItemRangeInserted(positionStart, itemCount);
                    int messageCount = adapter.getItemCount();
                    int lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition();
                    // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                    // to the bottom of the list to show the newly added message.
                    if (lastVisiblePosition == -1 ||
                            (positionStart >= (messageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                        chatRecyclerView.scrollToPosition(positionStart);
                    }
                }
            });

            chatRecyclerView.setLayoutManager(layoutManager);
            chatRecyclerView.setAdapter(adapter);

            // Start Listening...
            adapter.startListening();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        chatRecyclerView = (RecyclerView) rootView.findViewById(R.id.chat_list);
        imagesBtn = (ImageButton) rootView.findViewById(R.id.imagesBtn);
        chatText = (EditText) rootView.findViewById(R.id.chatEdtTxt);
        sendButton = (ImageButton) rootView.findViewById(R.id.sendBtn);

        imagesBtn.setOnClickListener(this);
        sendButton.setOnClickListener(this);

        chatText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.toString().trim().length() > 0) {
                    sendButton.setEnabled(true);
                } else {
                    sendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return rootView;
    }

    public static void setFriendId(String friendId) {
        if (ChatFragment.friendId == null) {
            ChatFragment.friendId = friendId;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        usersReference.addListenerForSingleValueEvent(usersEventListener);
        databaseReference.addListenerForSingleValueEvent(chatEventListener);
    }

    @Override
    public void onStop() {
        if (adapter != null) {
            adapter.stopListening();
        }

        usersReference.removeEventListener(usersEventListener);
        databaseReference.removeEventListener(chatEventListener);

        super.onStop();
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.sendBtn) {
            if (isConnected()) {
                sendMessage();
            } else {
                Toast.makeText(getContext(), R.string.no_connection, Toast.LENGTH_SHORT).show();
            }
        } else if (view.getId() == R.id.imagesBtn) {
            displayImagePicker();
        }
    }

    private void sendMessage() {
        if (!chatText.getText().toString().isEmpty()) {
            Message friendlyMessage = new Message(user.getUid(), friendId, userProfile.getPhoto(), chatText.getText().toString(),
                    null /* no image */, AppConstants.TYPE_TEXT, System.currentTimeMillis());

            switch (pathType) {
                case 0:
                    databaseReference.child(pathType0).push().setValue(friendlyMessage);
                    break;
                case 1:
                    databaseReference.child(pathType1).push().setValue(friendlyMessage);
                    break;
                default:
                    databaseReference.child(pathType1).push().setValue(friendlyMessage);
                    break;
            }

            chatText.setText("");
        }
    }

    private void displayImagePicker() {
        ImagePickerDialog imagePickerDialog = ImagePickerDialog.newInstance(this);
        imagePickerDialog.show(getFragmentManager(), AppConstants.TAG_IMAGE_PICKER);
    }

    @Override
    public void onUriSelect(Uri imageUri) {
        if (imageUri != null) {
            final Uri uri = imageUri;

            if (isConnected()) {
                Message tempMessage = new Message(user.getUid(), friendId, userProfile.getPhoto(), null /* no text */,
                        LOADING_IMAGE_URL, AppConstants.TYPE_IMAGE, System.currentTimeMillis());

                final String path;
                switch (pathType) {
                    case 0:
                        path = pathType0;
                        break;
                    case 1:
                        path = pathType1;
                        break;
                    default:
                        path = pathType0;
                        break;
                }

                databaseReference.child(path).push().setValue(tempMessage, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            String key = databaseReference.getKey();

                            StorageReference storageReference =
                                    FirebaseStorage.getInstance().getReference(AppConstants.STORAGE_PATH_IMAGES)
                                            .child(user.getUid()).child(key);

                            new ImageTask(storageReference, uri, key, path).execute();

                        } else {
                            Log.e("TAG", databaseError.getMessage());
                        }
                    }
                });
            } else {
                Toast.makeText(getContext(), R.string.no_connection, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ImageTask extends AsyncTask<Void, Void, Void> {

        StorageReference storageReference;
        Uri imageUri;
        String key;
        String path;

        ImageTask(StorageReference storageReference, Uri imageUri, String key, String path) {
            this.storageReference = storageReference;
            this.imageUri = imageUri;
            this.key = key;
            this.path = path;
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

                        Message message = new Message(user.getUid(), friendId, userProfile.getPhoto(), null /* no text */,
                                downloadUri.toString(), AppConstants.TYPE_IMAGE, System.currentTimeMillis());

                        databaseReference.child(path).child(key).setValue(message);
                    } else {
                        // Handle failures
                    }
                }
            });

            return null;
        }
    }
}
