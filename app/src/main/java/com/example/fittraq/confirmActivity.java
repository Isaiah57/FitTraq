package com.example.fittraq;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class confirmActivity extends Activity {

    private TessBaseAPI tessBaseAPI;
    private static final String DATA_PATH = Environment.getExternalStorageDirectory().toString()+"/imgs/ocr.png";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        Bundle extras = getIntent().getExtras();

        if(extras != null){

            Bitmap bmp = null;
            File f = new File(DATA_PATH);
            try {
                 bmp = BitmapFactory.decodeStream(new FileInputStream(f));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            tessBaseAPI = new TessBaseAPI();


            tessBaseAPI.init(Environment.getExternalStorageDirectory() + "/", "eng");

            tessBaseAPI.setImage(bmp);

            String resultingString = tessBaseAPI.getUTF8Text();

            System.out.println(resultingString);

        }


    }
}
