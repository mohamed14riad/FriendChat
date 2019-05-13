package mohamed14riad.friendchat.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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

import java.util.ArrayList;

import mohamed14riad.friendchat.R;
import mohamed14riad.friendchat.models.Profile;
import mohamed14riad.friendchat.utils.AppConstants;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class DiscoverListAdapter extends RecyclerView.Adapter<DiscoverListAdapter.PersonViewHolder> {
    private ArrayList<Profile> people;
    private Context context;
    private OnPersonClickListener personClickListener;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private DatabaseReference requestsReference;

    public DiscoverListAdapter(ArrayList<Profile> people, Context context, OnPersonClickListener personClickListener) {
        this.people = people;
        this.context = context;
        this.personClickListener = personClickListener;

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        requestsReference = FirebaseDatabase.getInstance().getReference(AppConstants.DB_PATH_REQUESTS);
    }

    @NonNull
    @Override
    public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PersonViewHolder(LayoutInflater.from(context).inflate(R.layout.discover_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final PersonViewHolder holder, int position) {
        Profile profile = people.get(position);
        Glide.with(holder.itemView)
                .load(profile.getPhoto())
                .apply(new RequestOptions().placeholder(R.drawable.ic_profile).error(R.drawable.ic_profile))
                .apply(bitmapTransform(new CircleCrop()))
                .into(holder.image);

        holder.name.setText(profile.getName());

        requestsReference.child(profile.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(user.getUid())) {
                    holder.addIcon.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_done));
                } else {
                    holder.addIcon.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_request));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        if (people.isEmpty()) {
            return 0;
        } else {
            return people.size();
        }
    }

    public interface OnPersonClickListener {
        void onAddButtonClick(int position);
    }

    public class PersonViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView image;
        private TextView name;
        private ImageButton addIcon;

        PersonViewHolder(View itemView) {
            super(itemView);

            image = (ImageView) itemView.findViewById(R.id.itemPersonImage);
            name = (TextView) itemView.findViewById(R.id.itemPersonName);
            addIcon = (ImageButton) itemView.findViewById(R.id.itemPersonAdd);

            addIcon.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.itemPersonAdd) {
                personClickListener.onAddButtonClick(getAdapterPosition());
                notifyItemChanged(getAdapterPosition());
            }
        }
    }
}
