package mohamed14riad.friendchat.data;

import android.provider.BaseColumns;

public class Contract {

    public static final String DATABASE_NAME = "Chat.db";
    public static final int DATABASE_VERSION = 1;

    // An empty private constructor makes sure that the class is not going to be initialised.
    private Contract() {
    }

    public static abstract class ProfilesEntry implements BaseColumns {
        public static final String TABLE_PROFILES = "profiles";
        public static final String COLUMN_UID = "uid";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_FAVORITE = "favorite";

        public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_PROFILES + "(" +
                COLUMN_UID + " TEXT PRIMARY KEY, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_FAVORITE + " INTEGER)";

        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_PROFILES;
    }
}
