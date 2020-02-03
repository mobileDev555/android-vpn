package com.ksh.aljoker;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

import eu.chainfire.libsuperuser.Shell;

public class play extends AppCompatActivity {
    Button mButton1;
    Button mButton2;
    Button mButton3;
    Button mButton4;
    TextView mtextview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.play);
        findViewsById();
        setListenerForViews();
        //setUpButton();
        mtextview.setText("حاله الحمايه");




    }

    private void findViewsById() {
        try{
            mButton1 = (Button) findViewById(R.id.buttonStart);
            mButton2 = (Button) findViewById(R.id.buttonStart1);
            mButton3 = (Button) findViewById(R.id.buttonStart2);

            mtextview = (TextView)findViewById(R.id.textView);


        } catch (NullPointerException exc) {
            exc.printStackTrace();
        }
    }

    private void setListenerForViews() {
        mButton1.setOnClickListener(myListener);
        mButton2.setOnClickListener(myListener);
        mButton3.setOnClickListener(myListener);


    }


    String A1 = "T";
    String C7 = ".";
    String C8 = "z";
    String A4 = "e";
    String A5 = "g";
    String A8 = "m";

    String A9 = "D";
    String A10 = "o";

    String A13 = "m";
    String A14 = "e";
    String A2 = "e";
    String A3 = "l";
    String A15 = "n";
    String A16 = "t";
    String A6 = "r";
    String A7 = "a";
    String A17 = "s";

    String A18 = "M";
    String A11 = "c";
    String A12 = "u";
    String A23 = "d";

    String B1 = "M";
    String B2 = "e";
    String A19 = "o";
    String A20 = "d";
    String B3 = "m";
    String A21 = "d";
    String A22 = "e";
    String B6 = "y";

    String B7 = "M";
    String B4 = "o";
    String B5 = "r";
    String B11 = "d";

    String C1 = "g";
    String C2 = "l";
    String B10 = "e";
    String B8 = "o";
    String B9 = "d";
    String C5 = "a";
    String C6 = "l";
    String C3 = "o";
    String C4 = "b";
    String C9 = "i";
    String C10 = "p";

    String D1 = "l";
    String D2 = "i";
    String D3 = "b";
    String D4 = "U";
    String D5 = "E";
    String D6 = "4";
    String D7 = ".";
    String D8 = "s";
    String D9 = "o";

    String E1 = "l";
    String E2 = "i";
    String E3 = "b";
    String E4 = "t";
    String E5 = "p";
    String E6 = "r";
    String E7 = "t";
    String E8 = ".";
    String E9 = "s";
    String E10 = "o";

    String F1 = "l";
    String F2 = "i";
    String F3 = "b";
    String F4 = "x";
    String F5 = "g";
    String F6 = "u";
    String F7 = "a";
    String F8 = "r";
    String F9 = "d";
    String F10 = "i";
    String F11 = "a";
    String F12 = "n";
    String F13 = ".";
    String F14 = "s";
    String F15 = "o";

    String G1 = "P";
    String G2 = "G";
    String G3 = "O";
    String G4 = "N";
    String G5 = ".";
    String G6 = "s";
    String G7 = "h";


    // PackageManager pm = getPackageManager();
    //Intent launchIntent = pm.getLaunchIntentForPackage("com.tencent.ig");


    File filePathxx = new File(Environment.getExternalStorageDirectory(), "/storage/emulated/0/Aljoker/libUE4.so");
    File filePathx = new File(Environment.getExternalStorageDirectory(), "/storage/emulated/0/Android/data/delete/Modded/libUE4.so");
    View.OnClickListener myListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (view == mButton1){
                Shell.SU.run("sh /storage/emulated/0/Android/data/delete/ON.sh");
                if (!filePathx.exists()){
                    mtextview.setText("الحمايه شغاله ");
                    mtextview.setTextColor(Color.GREEN);
                } else{
                    mtextview.setText("يوجد مشكله في تشغيل الحمايه  ");
                    mtextview.setTextColor(Color.RED);
                }


            }
            if (view == mButton2){
                //Phone/Global/off
                Shell.SU.run("sh /storage/emulated/0/Android/data/delete/OFF.sh");
                mtextview.setText("تم أيقاف الحمايه  ");
                mtextview.setTextColor(Color.RED);
            }
            if (view == mButton3){
                //Phone/Global/off
                Shell.SU.run("sh /storage/emulated/0/Android/data/delete/FIX.sh");
                mtextview.setText("تم التنظيف  !!");
                mtextview.setTextColor(Color.RED);
            }

        }

    };


}
