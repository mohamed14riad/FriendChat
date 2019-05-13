package mohamed14riad.friendchat.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import mohamed14riad.friendchat.R;
import mohamed14riad.friendchat.models.Message;
import mohamed14riad.friendchat.utils.AppConstants;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class ChatListAdapter extends FirebaseRecyclerAdapter<Message, RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_ME = 333;
    private static final int VIEW_TYPE_OTHER = 444;

    private Context context;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    public ChatListAdapter(@NonNull FirebaseRecyclerOptions<Message> options, Context context) {
        super(options);
        this.context = context;

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).getSenderUid().equals(user.getUid())) {
            return VIEW_TYPE_ME;
        } else {
            return VIEW_TYPE_OTHER;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case VIEW_TYPE_ME:
                View viewChatMine = layoutInflater.inflate(R.layout.item_chat_mine, parent, false);
                viewHolder = new MyChatViewHolder(viewChatMine);
                break;
            case VIEW_TYPE_OTHER:
                View viewChatOther = layoutInflater.inflate(R.layout.item_chat_other, parent, false);
                viewHolder = new OtherChatViewHolder(viewChatOther);
                break;
        }
        return viewHolder;
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull Message model) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_ME:
                configureMyChatViewHolder((MyChatViewHolder) holder, model);
                break;
            case VIEW_TYPE_OTHER:
                configureOtherChatViewHolder((OtherChatViewHolder) holder, model);
                break;
        }
    }

    private void configureMyChatViewHolder(final MyChatViewHolder myChatViewHolder, Message message) {
        Glide.with(myChatViewHolder.itemView)
                .load(message.getSenderImage())
                .apply(new RequestOptions().placeholder(R.drawable.ic_profile).error(R.drawable.ic_profile))
                .apply(bitmapTransform(new CircleCrop()))
                .into(myChatViewHolder.messageSenderImageView);

        if (message.getType().equals(AppConstants.TYPE_TEXT)) {
            myChatViewHolder.messageTxtView.setText(message.getText());

            myChatViewHolder.messageTxtView.setVisibility(View.VISIBLE);
            myChatViewHolder.messageImage.setVisibility(View.GONE);
        } else if (message.getType().equals(AppConstants.TYPE_IMAGE)) {
            String imageUrl = message.getImageUrl();
            if (imageUrl.startsWith("gs://")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                storageReference.getDownloadUrl().addOnCompleteListener(
                        new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    String downloadUrl = task.getResult().toString();
                                    Glide.with(myChatViewHolder.itemView)
                                            .load(downloadUrl)
                                            .into(myChatViewHolder.messageImage);
                                } else {
                                    Log.w("", context.getString(R.string.download_error), task.getException());
                                }
                            }
                        });
            } else {
                Glide.with(myChatViewHolder.itemView)
                        .load(message.getImageUrl())
                        .into(myChatViewHolder.messageImage);
            }

            myChatViewHolder.messageImage.setVisibility(View.VISIBLE);
            myChatViewHolder.messageTxtView.setVisibility(View.GONE);
        }
    }

    private void configureOtherChatViewHolder(final OtherChatViewHolder otherChatViewHolder, Message message) {
        Glide.with(otherChatViewHolder.itemView)
                .load(message.getSenderImage())
                .apply(new RequestOptions().placeholder(R.drawable.ic_profile).error(R.drawable.ic_profile))
                .apply(bitmapTransform(new CircleCrop()))
                .into(otherChatViewHolder.messageSenderImageView);

        if (message.getType().equals(AppConstants.TYPE_TEXT)) {
            otherChatViewHolder.messageTxtView.setText(message.getText());

            otherChatViewHolder.messageTxtView.setVisibility(View.VISIBLE);
            otherChatViewHolder.messageImage.setVisibility(View.GONE);
        } else if (message.getType().equals(AppConstants.TYPE_IMAGE)) {
            String imageUrl = message.getImageUrl();
            if (imageUrl.startsWith("gs://")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                storageReference.getDownloadUrl().addOnCompleteListener(
                        new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    String downloadUrl = task.getResult().toString();
                                    Glide.with(otherChatViewHolder.itemView)
                                            .load(downloadUrl)
                                            .into(otherChatViewHolder.messageImage);
                                } else {
                                    Log.w("", context.getString(R.string.download_error), task.getException());
                                }
                            }
                        });
            } else {
                Glide.with(otherChatViewHolder.itemView)
                        .load(message.getImageUrl())
                        .into(otherChatViewHolder.messageImage);
            }

            otherChatViewHolder.messageImage.setVisibility(View.VISIBLE);
            otherChatViewHolder.messageTxtView.setVisibility(View.GONE);
        }
    }

    public class MyChatViewHolder extends RecyclerView.ViewHolder {
        private ImageView messageSenderImageView, messageImage;
        private TextView messageTxtView;

        MyChatViewHolder(View itemView) {
            super(itemView);
            messageSenderImageView = (ImageView) itemView.findViewById(R.id.messageSenderImageView);
            messageTxtView = (TextView) itemView.findViewById(R.id.messageTxtView);
            messageImage = (ImageView) itemView.findViewById(R.id.messageImage);
        }
    }

    public class OtherChatViewHolder extends RecyclerView.ViewHolder {
        private ImageView messageSenderImageView, messageImage;
        private TextView messageTxtView;

        OtherChatViewHolder(View itemView) {
            super(itemView);
            messageSenderImageView = (ImageView) itemView.findViewById(R.id.messageSenderImageView);
            messageTxtView = (TextView) itemView.findViewById(R.id.messageTxtView);
            messageImage = (ImageView) itemView.findViewById(R.id.messageImage);
        }
    }
}
