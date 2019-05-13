package mohamed14riad.friendchat.widget;

import android.content.Context;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;

import mohamed14riad.friendchat.R;
import mohamed14riad.friendchat.data.DatabaseHelper;
import mohamed14riad.friendchat.models.Profile;

public class WidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context context;
    private DatabaseHelper databaseHelper;
    private ArrayList<Profile> favorites = new ArrayList<>();

    public WidgetFactory(Context context) {
        this.context = context;
        databaseHelper = new DatabaseHelper(context);
    }

    @Override
    public void onCreate() {
        favorites = databaseHelper.getProfiles();
    }

    @Override
    public void onDataSetChanged() {
        favorites.clear();
        favorites = databaseHelper.getProfiles();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return favorites.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.app_widget_item);
        Profile profile = favorites.get(position);
        remoteViews.setTextViewText(R.id.item_widget_name, profile.getName());
        remoteViews.setTextViewText(R.id.item_widget_email, profile.getEmail());
        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
