package mohamed14riad.friendchat.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import mohamed14riad.friendchat.R;
import mohamed14riad.friendchat.models.Profile;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class RequestsListAdapter extends RecyclerView.Adapter<RequestsListAdapter.RequestViewHolder> {
    private ArrayList<Profile> requests;
    private Context context;
    private OnRequestClickListener requestClickListener;

    public RequestsListAdapter(ArrayList<Profile> requests, Context context, OnRequestClickListener requestClickListener) {
        this.requests = requests;
        this.context = context;
        this.requestClickListener = requestClickListener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RequestViewHolder(LayoutInflater.from(context).inflate(R.layout.requests_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Profile profile = requests.get(position);
        Glide.with(holder.itemView)
                .load(profile.getPhoto())
                .apply(new RequestOptions().placeholder(R.drawable.ic_profile).error(R.drawable.ic_profile))
                .apply(bitmapTransform(new CircleCrop()))
                .into(holder.image);

        holder.name.setText(profile.getName());
    }

    @Override
    public int getItemCount() {
        if (requests.isEmpty()) {
            return 0;
        } else {
            return requests.size();
        }
    }

    public interface OnRequestClickListener {
        void onAcceptClick(int position);

        void onDeclineClick(int position);
    }

    public class RequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView image;
        private TextView name;
        private Button accept, decline;

        RequestViewHolder(View itemView) {
            super(itemView);

            image = (ImageView) itemView.findViewById(R.id.itemRequestImage);
            name = (TextView) itemView.findViewById(R.id.itemRequestName);
            accept = (Button) itemView.findViewById(R.id.itemRequestAccept);
            decline = (Button) itemView.findViewById(R.id.itemRequestDecline);

            accept.setOnClickListener(this);
            decline.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.itemRequestAccept) {
                requestClickListener.onAcceptClick(getAdapterPosition());
            } else if (view.getId() == R.id.itemRequestDecline) {
                requestClickListener.onDeclineClick(getAdapterPosition());
            }
        }
    }
}
