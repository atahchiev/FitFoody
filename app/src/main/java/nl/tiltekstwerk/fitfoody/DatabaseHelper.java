package nl.tiltekstwerk.fitfoody;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.IOException;

/**
 * Created by Georgi on 23.5.2016 г..
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static String DB_PATH = "/data/data/nl.tiltekstwerk.fitfoody/";

    private static String DB_NAME = "ingredients.db";

    private SQLiteDatabase database;

    private final Context context;

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     */
    public DatabaseHelper(Context context) {

        super(context, DB_NAME, null, 1);
        this.context = context;
    }

    public SQLiteDatabase getDatabase() throws SQLException {

        if(this.database == null) {
            try {
                boolean updated = Utils.copyData(this.context, DB_PATH, DB_NAME);

                String path = DB_PATH + DB_NAME;
                SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);

                if(updated){
                    db.execSQL("CREATE VIRTUAL TABLE fts_search_table USING fts4 (content='ingredients', keywords)");
                    db.execSQL("INSERT INTO fts_search_table (docid, keywords) SELECT _id, keywords FROM ingredients");
                }

                this.database = db;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this.database;
    }

    @Override
    public synchronized void close() {

        if(this.database != null)
            this.database.close();

        super.close();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDatabase() {


    }
}
