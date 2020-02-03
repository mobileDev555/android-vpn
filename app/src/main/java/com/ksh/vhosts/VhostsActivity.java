/*
 **Copyright (C) 2017  xfalcon
 **
 **This program is free software: you can redistribute it and/or modify
 **it under the terms of the GNU General Public License as published by
 **the Free Software Foundation, either version 3 of the License, or
 **(at your option) any later version.
 **
 **This program is distributed in the hope that it will be useful,
 **but WITHOUT ANY WARRANTY; without even the implied warranty of
 **MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 **GNU General Public License for more details.
 **
 **You should have received a copy of the GNU General Public License
 **along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **
 */

package com.ksh.vhosts;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.baidu.mobstat.StatService;
import com.ksh.aljoker.R;
import com.ksh.vhosts.util.LogUtils;
import com.ksh.vhosts.vservice.VhostsService;
import com.suke.widget.SwitchButton;

import java.io.File;

public class VhostsActivity extends AppCompatActivity {

    private static final String TAG = VhostsActivity.class.getSimpleName();
    public static final int VPN_REQUEST_CODE = 0x0F;
    public static final int SELECT_FILE_CODE = 0x05;
    public static final String PREFS_NAME = VhostsActivity.class.getName();
    public static final String IS_LOCAL = "IS_LOCAL";
    public static final String HOSTS_URL = "HOSTS_URL";
    public static final String HOSTS_URI = "HOST_URI";
    public static final String NET_HOST_FILE = "net_hosts";
    private SwitchButton vpnButton;


    private boolean isClick = false;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launch();
        StatService.autoTrace(this, true, false);
        setContentView(R.layout.play1);

        final Button vpn = findViewById(R.id.btn_vpn);
        vpn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isClick) {
                    shutdownVPN();
                    isClick = false;
                    vpn.setBackground(getDrawable(R.drawable.btn_red));
                } else {
                    if(fileExists()) {
                        try {
                            selectFile();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        startVPN();
                        Log.e("=========", ""+checkHostUri());
                        isClick = true;
                        vpn.setBackground(getDrawable(R.drawable.btn_green));
                    } else {
                        Toast.makeText(getApplicationContext(), "No file", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
//        if(flag.equals("on")) {
//            startVPN();
//            try {
//                selectFile();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else {
//            shutdownVPN();
//        }
//        finish();

        LogUtils.context = getApplicationContext();
//        vpnButton = findViewById(R.id.button_start_vpn);
//
        final Button selectHosts = findViewById(R.id.button_select_hosts);
//
//
//
//
//        if (checkHostUri() == -1) {
//            selectHosts.setText(getString(R.string.select_hosts));
//        }
//
//        vpnButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
//                if (isChecked) {
//                    startVPN();
//                    if (checkHostUri() == -1) {
//                        try {
//                            selectFile();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    } else {
//                        startVPN();
//                    }
//                } else {
//                    shutdownVPN();
//                }
//            }
//        });
//
//
        selectHosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    selectFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        selectHosts.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                startActivity(new Intent(getApplicationContext(), AdvanceActivity.class));
                return false;
            }
        });

        setURIHardCoded();

        LocalBroadcastManager.getInstance(this).registerReceiver(vpnStateReceiver, new IntentFilter(VhostsService.BROADCAST_VPN_STATE));
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


    private void selectFile() throws Exception {
        SharedPreferences settings = getSharedPreferences(VhostsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath()+"/host.txt");
        Log.d(TAG, "selectFile: "+uri.toString());
        try {
            editor.putString(HOSTS_URI, uri.toString());
            editor.apply();
            if (fileExists()) {
                Log.e("===========", "ok");
                vpnButton.setChecked(true);
            } else {
                Log.e("===========", "nossssssssss");
                vpnButton.setChecked(false);
                Toast.makeText(this, R.string.permission_error, Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            LogUtils.e(TAG, "permission error", e);
        }

    }


    public Boolean fileExists() {
        File myFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/host.txt");
        return myFile.exists();
    }

    private void startVPN() {
        waitingForVPNStart = false;
        Intent vpnIntent = VhostsService.prepare(this);
        Log.e("=========", "vpn="+vpnIntent);
        if (vpnIntent != null) {
            startActivityForResult(vpnIntent, VPN_REQUEST_CODE);
        }
        else {
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null);
        }
    }

    private int checkHostUri() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (settings.getBoolean(VhostsActivity.IS_LOCAL, true)) {
            try {
                Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath()+"/host.txt");
                getContentResolver().openInputStream(uri).close();//.openInputStream(Uri.parse(settings.getString(HOSTS_URI, ""))).close();
                return 1;
            } catch (Exception e) {
                LogUtils.e(TAG, "HOSTS FILE NOT FOUND", e);
                return -1;
            }
        } else {
            try {
                openFileInput(VhostsActivity.NET_HOST_FILE).close();
                return 2;
            } catch (Exception e) {
                LogUtils.e(TAG, "NET HOSTS FILE NOT FOUND", e);
                return -2;
            }
        }
    }

    private void setUriByPREFS(Intent intent) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        Uri uri = intent.getData();
        final int takeFlags = intent.getFlags()
                & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        try {
            getContentResolver().takePersistableUriPermission(uri, takeFlags);
            editor.putString(HOSTS_URI, uri.toString());
            editor.apply();
            if (checkHostUri() == 1) {
                setButton(true);
                setButton(false);
            } else {
                Toast.makeText(this, R.string.permission_error, Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            LogUtils.e(TAG, "permission error", e);
        }

    }

    private void setURIHardCoded(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory()+"/host.txt");
        try {
            editor.putString(HOSTS_URI, uri.toString());
            editor.apply();
            if (checkHostUri() == 1) {
                setButton(true);
                setButton(false);
            } else {
                Toast.makeText(this, R.string.permission_error, Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            LogUtils.e(TAG, "permission error", e);
        }
    }

    private void shutdownVPN() {
        if (VhostsService.isRunning())
            startService(new Intent(this, VhostsService.class).setAction(VhostsService.ACTION_DISCONNECT));
        setButton(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            waitingForVPNStart = true;
            startService(new Intent(this, VhostsService.class).setAction(VhostsService.ACTION_CONNECT));
            setButton(false);
        } else if (requestCode == SELECT_FILE_CODE && resultCode == RESULT_OK) {
            setUriByPREFS(data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setButton(!waitingForVPNStart && !VhostsService.isRunning());
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void setButton(boolean enable) {
        final SwitchButton vpnButton = (SwitchButton) findViewById(R.id.button_start_vpn);
        final Button selectHosts = (Button) findViewById(R.id.button_select_hosts);
        if (enable) {
            vpnButton.setChecked(false);
            selectHosts.setAlpha(1.0f);
            selectHosts.setClickable(true);
        } else {
            vpnButton.setChecked(true);
            selectHosts.setAlpha(.5f);
            selectHosts.setClickable(false);
        }
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(R.string.dialog_title);
        builder.setMessage(R.string.dialog_messagee);
        builder.setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    selectFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setButton(true);
            }
        });
        builder.show();
    }

}
