package nl.tiltekstwerk.fitfoody;

import android.app.Activity;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
    PictureCallback jpegCallback;
    ShutterCallback shutterCallback;
    public static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/AndroidOCRData/";
    public static final String lang = "eng";
    private static final String TAG = "Camera.java";

    public String detectText(Bitmap bitmap) {

        TessBaseAPI tessBaseAPI = new TessBaseAPI();



        tessBaseAPI.setDebug(true);
        tessBaseAPI.init(DATA_PATH, "eng"); //Init the Tess with the trained data file, with english language

        //For example if we want to only detect numbers
        tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "1234567890");
        tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-qwertyuiop[]}{POIU" +
                "YTREWQasdASDfghFGHjklJKLl;L:'\"\\|~`xcvXCVbnmBNM,./<>?");


        tessBaseAPI.setImage(bitmap);

        String text = tessBaseAPI.getUTF8Text();


        tessBaseAPI.end();

        return text;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };
        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                    return;
                } else {
                    Log.v(TAG, "Created directory " + path + " on sdcard");
                }
            }

        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity);
        ButterKnife.inject(this);
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

        if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
            try {

                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
                //GZIPInputStream gin = new GZIPInputStream(in);
                OutputStream out = new FileOutputStream(DATA_PATH
                        + "tessdata/" + lang + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
                out.close();

                Log.v(TAG, "Copied " + lang + " traineddata");
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
            }
        }
        jpegCallback=new PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                FileOutputStream outputStream=null;
                File file_image=getDirc();
                if (!file_image.exists() && !file_image.mkdirs()){
                    Toast.makeText(getApplicationContext(), "Can't create dir to save", Toast.LENGTH_SHORT).show();
                    return;
                }
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyymmddhh");
                String date=simpleDateFormat.format(new Date());
                String photofile="Cam_Demo"+date+".jpg";
                String file_name=file_image.getAbsolutePath()+"/"+photofile;
                Bitmap bitmap = BitmapFactory.decodeFile(file_name);
                File picfile=new File(file_name);
                String textje = detectText(bitmap);
                try{
                    outputStream=new FileOutputStream(picfile);
                    outputStream.write(data);
                    outputStream.close();
                }catch (FileNotFoundException e){}

                catch(IOException ex){}
                finally {

                }
                Toast.makeText(getApplicationContext(), textje,Toast.LENGTH_SHORT).show();
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
    private File getDirc(){
        File dics= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        return  new File(dics, "Camera demo");
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
