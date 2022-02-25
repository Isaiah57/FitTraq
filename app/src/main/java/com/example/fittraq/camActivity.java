package com.example.fittraq;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.LoaderCallbackInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class camActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2{

    Mat mRGBA;
    Mat mRGBAT;
    private static final String DATA_PATH = Environment.getExternalStorageDirectory() + "/";
    public static final String TESS_DATA = "tessdata";

    private Button openConfirm;

    CameraBridgeViewBase cBVB;
    BaseLoaderCallback baseLCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i("MainActivity", "onManagerConnected: OpenCV loaded");
                cBVB.enableView();
            }
            super.onManagerConnected(status);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(camActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
        setContentView(R.layout.activity_camera);

        cBVB = findViewById(R.id.camSurface);
        cBVB.setVisibility(SurfaceView.VISIBLE);
        cBVB.setCvCameraViewListener(this);

        openConfirm = findViewById(R.id.button1);
        openConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bmp = Bitmap.createBitmap(mRGBA.cols(), mRGBA.rows(), Bitmap.Config.ARGB_8888);
                String output = DATA_PATH + "imgs/";
                String imgFilePath = output+"/ocr.png";

                File dir = new File(output);

                prepareTessData();

                if(!dir.exists()){
                    dir.mkdir();
                }

                try {
                    Utils.matToBitmap(mRGBA, bmp);
                }catch (Exception e){
                    Log.d("MainActivity", "onClick: matToBitmap failure");
                }


                try(FileOutputStream out = new FileOutputStream(imgFilePath)){
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
                } catch (IOException e){
                    Log.d("camActivity", "onClick : File save failure");
                }

                Intent i = new Intent(camActivity.this, confirmActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                i.putExtra("fromCam", true);

                startActivity(i);

            }
        });
    }

    private void prepareTessData(){
        try{
            File dir = getExternalFilesDir(TESS_DATA);
            if(!dir.exists()){
                if (!dir.mkdir()) {
                    Toast.makeText(getApplicationContext(), "The folder " + dir.getPath() + "was not created", Toast.LENGTH_SHORT).show();
                }
            }
            String fileList[] = getAssets().list("");
            for(String fileName : fileList){
                String pathToDataFile = dir + "/" + fileName;
                if(!(new File(pathToDataFile)).exists()){
                    InputStream in = getAssets().open(fileName);
                    OutputStream out = new FileOutputStream(pathToDataFile);
                    byte [] buff = new byte[1024];
                    int len ;
                    while(( len = in.read(buff)) > 0){
                        out.write(buff,0,len);
                    }
                    in.close();
                    out.close();
                }
            }
        }catch(Exception e){
            Log.d("camActivity", "Prepare Tess Data : Prep failed");
        }
    }

    @Override
    public void onRequestPermissionsResult(int reqCode, @NonNull String[] permissions, @NonNull int[] gRes){
        if (reqCode == 1) {
            if (gRes.length > 0 && gRes[0] == PackageManager.PERMISSION_GRANTED) {
                cBVB.setCameraPermissionGranted();
            }
            //They don't want us to use the camera otherwise

            return;
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(OpenCVLoader.initDebug()){
            Log.d("MainActivity", "onResume: OpenCV Initialized");
            baseLCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }else{
            Log.d("MainActivity", "onResume: OpenCV not initialized");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLCallback);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(cBVB != null){
            cBVB.disableView();
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(cBVB != null){
            cBVB.disableView();
        }
    }

    @Override
    public void onCameraViewStopped(){
        mRGBA.release();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRGBA = new Mat(height, width, CvType.CV_8UC4);
        mRGBAT = new Mat(height, width, CvType.CV_8UC1);
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRGBA = inputFrame.rgba();
        mRGBAT = inputFrame.gray();


        return mRGBA;
    }
}
