package nl.tiltekstwerk.fitfoody;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.hardware.Camera.PictureCallback;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.melnykov.fab.FloatingActionButton;
import android.hardware.Camera.ShutterCallback;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by alexander on 3/30/2016.
 */
public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback{
    Camera camera;
    @InjectView(R.id.surfaceView)
    SurfaceView surfaceView;
    @InjectView(R.id.btn_take_photo)
    FloatingActionButton btn_take_photo;
    SurfaceHolder surfaceHolder;
    private Bitmap bmp,bmp1;
    PictureCallback jpegCallback;
    ShutterCallback shutterCallback;
    private FileInputStream fis;
    private BitmapFactory.Options o, o2;
    public static final String DATA_PATH = Environment.getExternalStorageDirectory() + "/DCIM/";


    private void copyData() throws IOException{
        File tess_data_dir = null;
        if (!getDirc("tessdata").mkdir()) {
            tess_data_dir = getDirc("tessdata");
            tess_data_dir.mkdir();
        }
        else{
            tess_data_dir=getDirc("tessdata");
        }


        InputStream myInput = this.getAssets().open("eng.traineddata");

        OutputStream myOutput = new FileOutputStream(tess_data_dir.getAbsolutePath()+"/eng.traineddata");
        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    public Bitmap decodeFile(File f) {
        Bitmap b = null;
        try {

            // Decode image size
            o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();
            int IMAGE_MAX_SIZE = 1000;
            int scale = 1;
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(
                        2,
                        (int) Math.round(Math.log(IMAGE_MAX_SIZE
                                / (double) Math.max(o.outHeight, o.outWidth))
                                / Math.log(0.5)));
            }

            // Decode with inSampleSize
            o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return b;
    }


    public String detectText(Bitmap bitmap) {


        TessBaseAPI tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.setDebug(true);


        tessBaseAPI.init(DATA_PATH, "eng"); //Init the Tess with the trained data file, with english language
        //For example if we want to only detect numbers
        tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "1234567890");
//        tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-qwertyuiop[]}{POIU" +
//                "YTREWQasdASDfghFGHjklJKLl;L:'\"\\|~`xcvXCVbnmBNM,./<>?");


        tessBaseAPI.setImage(bitmap);

        String text = tessBaseAPI.getUTF8Text();



        tessBaseAPI.end();

        return text;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.camera_activity);
        ButterKnife.inject(this);
//        copyAssets();
        surfaceHolder=surfaceView.getHolder();
        //install a surfaceHolder.callBack so we get notified when the
        // underlying surface is created and destroyed;
        surfaceHolder.addCallback(this);
        //deprecated settings, but required on android version prior to 3.0
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        btn_take_photo.setOnClickListener(new FloatingActionButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraImage();
            }
        });

        jpegCallback=new PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                FileOutputStream outputStream=null;
                File file_image=getDirc("Camera demo");

                if (!file_image.exists() && !file_image.mkdirs()){
                    Toast.makeText(getApplicationContext(), "Can't create dir to save", Toast.LENGTH_SHORT).show();
                    return;
                }
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyymmddhh");
                String date=simpleDateFormat.format(new Date());
                String photofile="Cam_Demo"+date+".jpg";
                String file_name=file_image.getAbsolutePath()+"/"+photofile;
                Bitmap bmp = BitmapFactory.decodeByteArray(data , 0, data.length);
                String text = detectText(bmp);
                File picfile=new File(file_name);
                try{
                    outputStream=new FileOutputStream(picfile);
                    outputStream.write(data);
                    outputStream.close();
                }catch (FileNotFoundException e){}
                catch(IOException ex){}
                finally {

                }

                Toast.makeText(getApplicationContext(), text,Toast.LENGTH_SHORT).show();
                refreshCamera();
                refreshGalery(picfile);
            }
        };


    }
    //refresh gallery
    private void refreshGalery(File file){
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        sendBroadcast(intent);
    }
    public void refreshCamera(){
        if(surfaceHolder.getSurface()==null){
            //preview Surface does not exit
            return;
        }
        // stop preview before making changes
        try{
            camera.stopPreview();

        }catch (Exception e){

        }
        //set preview size and make any resize, rotate or
        //reformating changes
        //start preview with new settings
        try{
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        }catch(Exception e){}

    }

    private File getDirc(String name){
        File dics= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        return new File(dics, name);
    }
    public void cameraImage(){
        //take the picture
        camera.takePicture(null, null, jpegCallback);

    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //open camera
        try{
            camera=Camera.open();
        } catch(RuntimeException ex){}
        Camera.Parameters parameters;
        parameters=camera.getParameters();
        //modify parameters
        parameters.setPreviewFrameRate(20);
        parameters.setPreviewSize(352, 288);
        camera.setParameters(parameters);
        camera.setDisplayOrientation(90);
        try{
            //The surface thas been created, now tell camera where to draw preview
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        }catch (Exception e){

        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        refreshCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //stop preview and release camera
        camera.stopPreview();
        camera.release();
        camera=null;

    }
}
