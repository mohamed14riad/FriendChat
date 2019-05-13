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
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import mohamed14riad.friendchat.R;
import mohamed14riad.friendchat.models.Profile;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;
import static mohamed14riad.friendchat.activities.MainActivity.twoPaneMode;

public class FriendsListAdapter extends FirebaseRecyclerAdapter<Profile, FriendsListAdapter.FriendViewHolder> {
    private Context context;
    private OnFriendClickListener friendClickListener;

    private int selectedItem = -1;

    public FriendsListAdapter(@NonNull FirebaseRecyclerOptions<Profile> options, Context context, OnFriendClickListener friendClickListener) {
        super(options);
        this.context = context;
        this.friendClickListener = friendClickListener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FriendViewHolder(LayoutInflater.from(context).inflate(R.layout.friends_list_item, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull final FriendViewHolder holder, int position, @NonNull final Profile model) {
        Glide.with(holder.itemView)
                .load(model.getPhoto())
                .apply(new RequestOptions().placeholder(R.drawable.ic_profile).error(R.drawable.ic_profile))
                .apply(bitmapTransform(new CircleCrop()))
                .into(holder.image);

        if (model.getStatus()) {
            holder.status.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_green));
        } else {
            holder.status.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_grey));
        }

        holder.name.setText(model.getName());

        if (model.getFavorite()) {
            holder.favoriteIcon.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_favorite));
        } else {
            holder.favoriteIcon.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_unfavorite));
        }

        if (twoPaneMode && selectedItem != -1) {
            holder.itemView.setSelected(true);
        }
    }

    public interface OnFriendClickListener {
        void onFriendClick(int position);

        void onFavoriteIconClick(int position);
    }

    public class FriendViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView image;
        private ImageView status;
        private TextView name;
        private ImageButton favoriteIcon;

        FriendViewHolder(View itemView) {
            super(itemView);

            image = (ImageView) itemView.findViewById(R.id.itemFriendImage);
            status = (ImageView) itemView.findViewById(R.id.itemFriendStatus);
            name = (TextView) itemView.findViewById(R.id.itemFriendName);
            favoriteIcon = (ImageButton) itemView.findViewById(R.id.itemFriendFavorite);

            itemView.setOnClickListener(this);
            favoriteIcon.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.itemFriendFavorite) {
                friendClickListener.onFavoriteIconClick(getAdapterPosition());
            } else {
                if (twoPaneMode) {
                    itemView.setSelected(true);
                    selectedItem = getAdapterPosition();
                }

                friendClickListener.onFriendClick(getAdapterPosition());
            }
        }
    }
}
