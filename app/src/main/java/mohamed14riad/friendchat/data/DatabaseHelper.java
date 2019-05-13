package mohamed14riad.friendchat.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import mohamed14riad.friendchat.R;
import mohamed14riad.friendchat.models.Profile;

public class DatabaseHelper extends SQLiteOpenHelper {

    private Context context;

    public DatabaseHelper(Context context) {
        super(context, Contract.DATABASE_NAME, null, Contract.DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Contract.ProfilesEntry.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            db.execSQL(Contract.ProfilesEntry.DROP_TABLE);
            onCreate(db);
        }
    }

    public void insertProfile(Profile profile) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Contract.ProfilesEntry.COLUMN_UID, profile.getUid());
        values.put(Contract.ProfilesEntry.COLUMN_NAME, profile.getName());
        values.put(Contract.ProfilesEntry.COLUMN_EMAIL, profile.getEmail());
        if (profile.getFavorite()) {
            values.put(Contract.ProfilesEntry.COLUMN_FAVORITE, 1);
        } else {
            values.put(Contract.ProfilesEntry.COLUMN_FAVORITE, 0);
        }

        db.insert(Contract.ProfilesEntry.TABLE_PROFILES, null, values);
    }

    public void insertProfiles(ArrayList<Profile> profiles) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        for (int i = 0; i < profiles.size(); i++) {
            Profile profile = profiles.get(i);
            values.put(Contract.ProfilesEntry.COLUMN_UID, profile.getUid());
            values.put(Contract.ProfilesEntry.COLUMN_NAME, profile.getName());
            values.put(Contract.ProfilesEntry.COLUMN_EMAIL, profile.getEmail());
            if (profile.getFavorite()) {
                values.put(Contract.ProfilesEntry.COLUMN_FAVORITE, 1);
            } else {
                values.put(Contract.ProfilesEntry.COLUMN_FAVORITE, 0);
            }

            db.insert(Contract.ProfilesEntry.TABLE_PROFILES, null, values);
        }
    }

    public ArrayList<Profile> getProfiles() {
        ArrayList<Profile> profiles = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(Contract.ProfilesEntry.TABLE_PROFILES, null,
                null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Profile profile = new Profile();

                profile.setUid(cursor.getString(0));
                profile.setName(cursor.getString(1));
                profile.setEmail(cursor.getString(2));
                if (cursor.getInt(3) == 1) {
                    profile.setFavorite(true);
                } else {
                    profile.setFavorite(false);
                }

                profiles.add(profile);
            } while (cursor.moveToNext());
        }

        try {
            if (!cursor.isClosed()) {
                cursor.close();
                db.close();
            }
        } catch (Exception e) {
            throw new UnsupportedOperationException(context.getString(R.string.cursor_error));
        }

        return profiles;
    }

    public void deleteAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(Contract.ProfilesEntry.TABLE_PROFILES, null, null);
    }
}
