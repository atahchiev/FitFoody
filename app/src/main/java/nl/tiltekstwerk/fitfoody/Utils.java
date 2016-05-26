package nl.tiltekstwerk.fitfoody;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Georgi on 23.5.2016 г..
 */
public class Utils {
    public static boolean copyData(Context ctx, String path, String fileName) throws IOException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        if( Float.parseFloat(ctx.getString(R.string.version)) > prefs.getFloat("LastInstalledVersion", (float) 0.0 ) ) {
            InputStream myInput = ctx.getAssets().open(fileName);

            OutputStream myOutput = new FileOutputStream(path + fileName);
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

            return true;
        }

        return false;
    }

    public static File getDirc(String name){
        File dics= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        return new File(dics, name);
    }
}
