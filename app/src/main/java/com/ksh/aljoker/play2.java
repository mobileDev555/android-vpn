package com.ksh.aljoker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ksh.vhosts.VhostsActivity;
import com.ksh.vhosts.util.LogUtils;
import com.ksh.vhosts.vservice.VhostsService;
import com.suke.widget.SwitchButton;

import java.io.File;

import eu.chainfire.libsuperuser.Shell;

public class play2 extends AppCompatActivity {
    private static final String TAG = "checkResult";

    TextView remTimeTextView;
    TextView timeExpire;
    Button mButton1;
    Button mButton2;
    Button mButton3;
    Button mButton4;
    TextView mtextview;
    private SwitchButton vpnStart;
    public static final String HOSTS_URL = "HOSTS_URL";
    public static final String HOSTS_URI = "HOST_URI";
    public static final String NET_HOST_FILE = "net_hosts";
    private Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.play2);
        findViewsById();
        setListenerForViews();
        //setUpButton();
        mtextview.setText("حاله الحمايه");

        vpnStart = findViewById(R.id.button_start_vpn2);
        vpnStart.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (isChecked) {
                    if (checkHostUri() == -1) {
                        try {
                            selectFile();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        startVPN();
                    }
                } else {
                    shutdownVPN();
                }
            }
        });
        try {
            selectFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(vpnStateReceiver,
                new IntentFilter(VhostsService.BROADCAST_VPN_STATE));
    }

    private void launch() {
        Uri uri = getIntent().getData();
        if (uri == null) return;
        String data_str = uri.toString();
        if ("on".equals(data_str)) {
            if (!VhostsService.isRunning())
                VhostsService.startVService(this,1);
            finish();
        } else if ("off".equals(data_str)) {
            VhostsService.stopVService(this);
            finish();
        }
    }

    private int checkHostUri() {
        SharedPreferences settings = getSharedPreferences(VhostsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        try {
            Uri hostUri = Uri.parse(settings.getString(HOSTS_URI, null));
            File file = new File(hostUri.toString());
            if(file.exists()){
                Log.d(TAG, "checkHostUri: Host file found!");
                getContentResolver().openInputStream(Uri.fromFile(file)).close();
                return 1;
            }else{
                Log.d(TAG, "checkHostUri: File not found!");
                return -1;
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "HOSTS FILE NOT FOUND", e);
            return -1;
        }
    }

    private boolean waitingForVPNStart;

    private BroadcastReceiver vpnStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (VhostsService.BROADCAST_VPN_STATE.equals(intent.getAction())) {
                if (intent.getBooleanExtra("running", false))
                    waitingForVPNStart = false;
            }
        }
    };

    private void startVPN() {
        waitingForVPNStart = false;
        Intent vpnIntent = VhostsService.prepare(this);
        if (vpnIntent != null)
            startActivityForResult(vpnIntent, VhostsActivity.VPN_REQUEST_CODE);
        else
            onActivityResult(VhostsActivity.VPN_REQUEST_CODE, RESULT_OK, null);
    }

    private void shutdownVPN() {
        if (VhostsService.isRunning())
            startService(new Intent(this, VhostsService.class).setAction(VhostsService.ACTION_DISCONNECT));
        vpnStart.setChecked(false);
    }


    private void selectFile() throws Exception {
        SharedPreferences settings = getSharedPreferences(VhostsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath()+"/host.txt");
        Log.d(TAG, "selectFile: "+uri.toString());
        try {
            editor.putString(HOSTS_URI, uri.toString());
            editor.apply();
            if (checkHostUri() == 1) {
                vpnStart.setChecked(true);
            } else {
                Toast.makeText(this, R.string.permission_error, Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            LogUtils.e(TAG, "permission error", e);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VhostsActivity.VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            waitingForVPNStart = true;
            startService(new Intent(this, VhostsService.class).setAction(VhostsService.ACTION_CONNECT));
            vpnStart.setChecked(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean flag = !waitingForVPNStart && !VhostsService.isRunning();
        vpnStart.setChecked(!flag);
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

        //mradioButton12.setOnClickListener(myListener);

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

    File filePathxx = new File(Environment.getExternalStorageDirectory(), "/storage/emulated/0/Aljoker/kr/libUE4.so");
    File filePathx = new File(Environment.getExternalStorageDirectory(), "/storage/emulated/0/Android/data/delete/kr/Modded/libUE4.so");
    View.OnClickListener myListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (view == mButton1){
                Shell.SU.run("sh /storage/emulated/0/Android/data/delete/kr/ON.sh");
                if (!filePathx.exists()){
                    mtextview.setText("تم تشغيل الحمالية ");
                    mtextview.setTextColor(Color.GREEN);
                } else{
                    mtextview.setText("يوجد مشكله في تشغيل الحمايه  ");
                    mtextview.setTextColor(Color.RED);
                }


            }
            if (view == mButton2){
                //Phone/Global/off
                Shell.SU.run("sh /storage/emulated/0/Android/data/delete/kr/OFF.sh");
                mtextview.setText("تم تعطيل الحمايه  ");
                mtextview.setTextColor(Color.RED);
            }
            if (view == mButton3){
                //Phone/Global/off
                Shell.SU.run("sh /storage/emulated/0/Android/data/delete/kr/FIX.sh");
                mtextview.setText("تم التنظيف   !!");
                mtextview.setTextColor(Color.RED);
            }

        }

    };


}
