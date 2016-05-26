package nl.tiltekstwerk.fitfoody;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends AppCompatActivity {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @InjectView(R.id.openCameraActivity)
    ImageButton ib;

    @InjectView(R.id.buttonSearch)
    Button searchButton;

    @InjectView(R.id.searchText)
    EditText searchText;

    @InjectView(R.id.searchResults)
    ListView searchResults;

    @InjectView(R.id.noResults)
    TextView noResults;

    @Override
    protected void onPause() {
        super.onPause();

        // Set the version of the app. Will be set once on the first pause of the activity.
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        if(!prefs.contains("LastInstalledVersion")) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putFloat("LastInstalledVersion", Float.parseFloat(getString(R.string.version)));
            editor.commit();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), CameraActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        this.createTessdataDir();

        // Updates the db if needed when the app is opened for first time.
        this.db = this.dbHelper.getDatabase();
    }

    public void search(View v) {
        String query = this.searchText.getText().toString();
        Cursor cursor = this.searchInDb(query);
        String[] columns = {"name", "description"};
        int[] texts = {R.id.ingrName, R.id.ingrDescription};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.search_result_row, cursor, columns, texts, 0);
        this.searchResults.setAdapter(adapter);
        this.searchResults.setEmptyView(this.noResults);
    }

    private Cursor searchInDb (String query) {
        String queryLC = query.toLowerCase();

        String[] selectionArgs = { queryLC + "*" };
        String sql = "SELECT * FROM ingredients WHERE _id IN " +
                "(SELECT docid FROM fts_search_table WHERE keywords MATCH ?)";

        Cursor cursor = this.db.rawQuery(sql, selectionArgs);
        return cursor;
    }

    private void  createTessdataDir() {
        File tess_data_dir = Utils.getDirc("tessdata");
        tess_data_dir.mkdir();

        try {
            Utils.copyData(this, tess_data_dir.getAbsolutePath() + "/", "eng.traineddata");
        }
        catch(Exception e){
            Log.e("FUCK JAVAAAAAAAA", e.toString());
        }
    }

    public static final String DATA_PATH = "/data/data/nl.tiltekstwerk.fitfoody/";
    private DatabaseHelper dbHelper = new DatabaseHelper(this);
    private SQLiteDatabase db;
}
