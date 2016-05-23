package nl.tiltekstwerk.fitfoody;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

        try {
            copyData("eng.traineddata");
            copyData("ingredients.db");
        }
        catch(Exception e){
            Log.e("FUCK JAVAAAAAAAA", e.toString());
        }
    }

    private void copyData(String fileName) throws IOException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if( Float.parseFloat(getString(R.string.version)) > prefs.getFloat("LastInstalledVersion", (float) 0.0 ) ) {
            InputStream myInput = this.getAssets().open(fileName);

            OutputStream myOutput = new FileOutputStream(DATA_PATH + "/" + fileName);
            //transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }

            //Close the streams
            myOutput.flush();
            myOutput.close();
            myInput.close();
        }
    }

    public static final String DATA_PATH = "/data/data/nl.tiltekstwerk.fitfoody/";
}
