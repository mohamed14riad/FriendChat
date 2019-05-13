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

import java.util.ArrayList;

import mohamed14riad.friendchat.R;
import mohamed14riad.friendchat.models.Profile;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class FavoritesListAdapter extends RecyclerView.Adapter<FavoritesListAdapter.FavoriteViewHolder> {
    private ArrayList<Profile> favorites;
    private Context context;
    private OnFavoriteClickListener favoriteClickListener;

    public FavoritesListAdapter(ArrayList<Profile> favorites, Context context, OnFavoriteClickListener favoriteClickListener) {
        this.favorites = favorites;
        this.context = context;
        this.favoriteClickListener = favoriteClickListener;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FavoriteViewHolder(LayoutInflater.from(context).inflate(R.layout.favorites_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        Profile profile = favorites.get(position);
        Glide.with(holder.itemView)
                .load(profile.getPhoto())
                .apply(new RequestOptions().placeholder(R.drawable.ic_profile).error(R.drawable.ic_profile))
                .apply(bitmapTransform(new CircleCrop()))
                .into(holder.image);
        holder.name.setText(profile.getName());
        if (profile.getFavorite()) {
            holder.favoriteIcon.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_favorite));
        }
    }

    @Override
    public int getItemCount() {
        if (favorites.isEmpty()) {
            return 0;
        } else {
            return favorites.size();
        }
    }

    public interface OnFavoriteClickListener {
        void onFavoriteIconClick(int position);
    }

    public class FavoriteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView image;
        private TextView name;
        private ImageButton favoriteIcon;

        FavoriteViewHolder(View itemView) {
            super(itemView);

            image = (ImageView) itemView.findViewById(R.id.itemFavoriteImage);
            name = (TextView) itemView.findViewById(R.id.itemFavoriteName);
            favoriteIcon = (ImageButton) itemView.findViewById(R.id.itemFavoriteIcon);

            favoriteIcon.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.itemFavoriteIcon) {
                favoriteClickListener.onFavoriteIconClick(getAdapterPosition());
            }
        }
    }
}
