package com.ksh.aljoker;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ksh.vhosts.VhostsActivity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import eu.chainfire.libsuperuser.Shell;
import ir.mahdi.mzip.zip.ZipArchive;

import static com.ksh.aljoker.Constant.SHARED_IMEI;
import static com.ksh.aljoker.Constant.SHARED_email;

public class home extends AppCompatActivity {

    private static final int CHOOSE_IMAGE = 101;

    TextView textView;
    ImageView imageView;
    EditText editText;
    String expiryDate;
    ProgressBar progressBar;
    SharedPreferences pref;
    TextView remTimeTextView;
    TextView timeExpire;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    StorageReference ref;
    String  email,imei;
    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int i;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences preferences =getSharedPreferences(Constant.SHARED_PREF,Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.commit();
                Intent intent = new Intent(home.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        findViewById(R.id.downf).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                opendownf();
            }
        });

        findViewById(R.id.downf2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                opendownf2();
            }
        });
        findViewById(R.id.downf3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                opendownf3();
            }
        });

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cleargb();
            }
        });
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearkr();
            }
        });
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearvn();
            }
        });
        //هوست
        // findViewById(R.id.downhost).setOnClickListener(new View.OnClickListener() {
        //      @Override
        //      public void onClick(View view) {
        //          hostdo();
        //       }
        //  });
        findViewById(R.id.datacl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Shell.SU.run("rm -rf /storage/emulated/0/Android/data/delete");
                Shell.SU.run("rm -rf /storage/emulated/0/Aljoker");

                AlertDialog alertDialog = new AlertDialog.Builder(home.this ,R.style.MyAlertDialogStyle).create();
                alertDialog.setTitle("VIP");
                alertDialog.setMessage("تم مسح الملفات بنجاح");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();

            }
        });

        pref = getApplicationContext().getSharedPreferences(Constant.SHARED_PREF, Context.MODE_PRIVATE);
        email = pref.getString(SHARED_email, "");
        imei = pref.getString(SHARED_IMEI, "");
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reffs = database.getReference().child("AlJoker");

        expiryDate = getIntent().getExtras().getString("expDate");
        timeExpire = (TextView) findViewById(R.id.time);
        remTimeTextView = (TextView) findViewById(R.id.time2);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy");
        Log.d("checkday", expiryDate);
        CountDownTimer mCountDownTimer;
        i = 0;
        try {
            mCountDownTimer = new CountDownTimer(dateFormat.parse(expiryDate).getTime() - new Date().getTime(), 1000) {
                public void onTick(long millisUntilFinished) {
                    long days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished);
                    long millisUntilFinished2 = millisUntilFinished - TimeUnit.DAYS.toMillis(days);
                    long hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished2);
                    long millisUntilFinished3 = millisUntilFinished2 - TimeUnit.HOURS.toMillis(hours);
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished3);
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished3 - TimeUnit.MINUTES.toMillis(minutes));
                    TextView textView = remTimeTextView;
                    StringBuilder sb = new StringBuilder();
                    sb.append(getPaddedNumber(days));
                    sb.append(":");
                    sb.append(getPaddedNumber(hours));
                    sb.append(":");
                    sb.append(getPaddedNumber(minutes));
                    sb.append(":");
                    sb.append(getPaddedNumber(seconds));
                    textView.setText(sb.toString());
                    TextView textView2 = timeExpire;
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append(getPaddedNumber(days));
                    sb2.append(":");
                    sb2.append(getPaddedNumber(hours));
                    sb2.append(":");
                    sb2.append(getPaddedNumber(minutes));
                    sb2.append(":");
                    sb2.append(getPaddedNumber(seconds));
                    textView2.setText(sb2.toString());
                }

                @SuppressLint("WrongConstant")
                public void onFinish() {
                    remTimeTextView.setText("Finish!");
                    timeExpire.setText("Finish!");
                    Toast.makeText(home.this, "انتهى اشتراكك تواصل معنا لتجديد.", 0).show();
                    SharedPreferences preferences =getSharedPreferences(Constant.SHARED_PREF,Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.commit();
                    Intent intent = new Intent(home.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            };
            mCountDownTimer.start();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SharedPreferences prefs = getSharedPreferences("PMH", i);
        String expirationTime = prefs.getString("expiration_time", null);
        if ( expirationTime == null) {
            return;
        }
        try {
            if (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(expirationTime).after(new Date())) {
                SharedPreferences sharedPreferences = prefs;
                return;
            }
            SharedPreferences.Editor editor = getSharedPreferences("PMH", i).edit();
            editor.remove("expiration_time");
            editor.apply();
            SharedPreferences sharedPreferences2 = prefs;
            Toast.makeText(this, "انتهى اشتراكك تواصل معنا لتجديد.", 0).show();
            finish();
        } catch (ParseException e) {
            SharedPreferences sharedPreferences3 = prefs;
            e.printStackTrace();

        }

    }
    public String getPaddedNumber(long number) {
        return String.format("%02d", new Object[]{Long.valueOf(number)});
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




    public static void directs(String str, String str2) {
        File file = new File(str, str2);
        if (!file.isDirectory() && !file.mkdirs()) {
        }
    }

    public static void Zupy(InputStream inputStream, String str) {
        String str2 = "ZipManager";
        directs(str, BuildConfig.FLAVOR);
        byte[] bArr = new byte[10240];
        try {
            ZipInputStream zipInputStream = new ZipInputStream(inputStream);
            while (true) {
                ZipEntry nextEntry = zipInputStream.getNextEntry();
                if (nextEntry != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Unzipping ");
                    sb.append(nextEntry.getName());
                    Log.v(str2, sb.toString());
                    if (nextEntry.isDirectory()) {
                        directs(str, nextEntry.getName());
                    } else {
                        File file = new File(str, nextEntry.getName());
                        if (!file.exists()) {
                            if (!file.createNewFile()) {
                                StringBuilder sb2 = new StringBuilder();
                                sb2.append("Failed to create file ");
                                sb2.append(file.getName());
                                Log.w(str2, sb2.toString());
                            } else {
                                FileOutputStream fileOutputStream = new FileOutputStream(file);
                                while (true) {
                                    int read = zipInputStream.read(bArr);
                                    if (read == -1) {
                                        break;
                                    }
                                    fileOutputStream.write(bArr, 0, read);
                                }
                                zipInputStream.closeEntry();
                                fileOutputStream.close();
                            }
                        }
                    }
                } else {
                    zipInputStream.close();
                    return;
                }
            }
        } catch (Exception e) {
            Log.e(str2, "unzip", e);
        }
    }
    File wherehost = new File(Environment.getExternalStorageDirectory() + "/Android/data/delete/hosts");

    File filePath1 = new File(Environment.getExternalStorageDirectory(),"/Android/data/delete/int/int.zip");
    File filePath2 = new File(Environment.getExternalStorageDirectory(),"/Android/data/delete/kr/kr.zip");
    File filePath3 = new File(Environment.getExternalStorageDirectory(),"/Android/data/delete/vn/vn.zip");
    File filePath4 = new File(Environment.getExternalStorageDirectory(),"/Android/data/delete/re/re.zip");
    File directtxx = new File(Environment.getExternalStorageDirectory() + "/Android/data/delete/Modded/libUE4.so");


    public void playthishack1() {
        if (filePath1.exists()){
            ZipArchive zipArchive = new ZipArchive();
            zipArchive.unzip(Environment.getExternalStorageDirectory() + "/Android/data/delete/int/int.zip", Environment.getExternalStorageDirectory() + "/Android/data/delete/int/","jk123");
            Intent intent = new Intent(this, VhostsActivity.class);
            startActivity(intent);
        }else {
            AlertDialog alertDialog = new AlertDialog.Builder(home.this ,R.style.MyAlertDialogStyle).create();
            alertDialog.setTitle("VIP");
            alertDialog.setMessage("قم بتحميل الملفات اولأ");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();        }

    }

    public void playthishack2() {
        if (filePath2.exists()){
            ZipArchive zipArchive = new ZipArchive();
            zipArchive.unzip(Environment.getExternalStorageDirectory() + "/Android/data/delete/kr/kr.zip", Environment.getExternalStorageDirectory() + "/Android/data/delete/kr/","jk123");
            Intent intent = new Intent(this, play2.class);
            startActivity(intent);
        }else {
            AlertDialog alertDialog = new AlertDialog.Builder(home.this ,R.style.MyAlertDialogStyle).create();
            alertDialog.setTitle("VIP");
            alertDialog.setMessage("قم بتحميل الملفات اولأ");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();        }

    }

    public void playthishack3() {
        if (filePath3.exists()){
            ZipArchive zipArchive = new ZipArchive();
            zipArchive.unzip(Environment.getExternalStorageDirectory() + "/Android/data/delete/vn/vn.zip", Environment.getExternalStorageDirectory() + "/Android/data/delete/vn/","jk123");
            Intent intent = new Intent(this, play3.class);
            startActivity(intent);
        }else {
            AlertDialog alertDialog = new AlertDialog.Builder(home.this ,R.style.MyAlertDialogStyle).create();
            alertDialog.setTitle("VIP");
            alertDialog.setMessage("قم بتحميل الملفات اولأ");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();        }

    }



    public void hostdo() {
        if (wherehost.exists()){
            Shell.SU.run("mount -o rw,remount -t rootfs /system");
            Shell.SU.run("chgrp system /system/etc/");
            Shell.SU.run("mv /storage/emulated/0/Android/data/delete/hosts /system/etc/hosts");
            AlertDialog alertDialog = new AlertDialog.Builder(home.this ,R.style.MyAlertDialogStyle).create();
            alertDialog.setTitle("VIP");
            alertDialog.setMessage("تم تثبيت ملف الهوست !!");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        } else if (filePath1.exists()){
            ZipArchive zipArchive = new ZipArchive();
            zipArchive.unzip(Environment.getExternalStorageDirectory() + "/Android/data/delete/int.zip", Environment.getExternalStorageDirectory() + "/Android/data/delete/","jk123");
            AlertDialog alertDialog = new AlertDialog.Builder(home.this ,R.style.MyAlertDialogStyle).create();
            alertDialog.setTitle("VIP");
            alertDialog.setMessage("تم تحميل ملف الهوست قم بالضغط مره اخرى لتثبيت");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }else {
            AlertDialog alertDialog = new AlertDialog.Builder(home.this ,R.style.MyAlertDialogStyle).create();
            alertDialog.setTitle("VIP");
            alertDialog.setMessage("لايوجد ملف هوست الرجاء قم بتحميل الملفات");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();        }

    }

    public void opendownf() {
        if (filePath1.exists()){

         playthishack1();

        }else {
            Log.e("============", "1");
            Toast.makeText(getApplicationContext(),"جاري التحميل الرجاء الانتظار", Toast.LENGTH_SHORT).show();
            downloadb();

        }
        File direct = new File(Environment.getExternalStorageDirectory() + "/Android");

        if (!direct.exists()) {
            direct.mkdirs();
        }
        File directt = new File(Environment.getExternalStorageDirectory() + "/Android/data");

        if (!directt.exists()) {
            directt.mkdirs();
        }
        File directts = new File(Environment.getExternalStorageDirectory() + "/Android/data/delete");

        if (!directts.exists()) {
            directts.mkdirs();
        }

        File directx = new File(Environment.getExternalStorageDirectory() + "/Aljoker");

        if (!directx.exists()) {
            directx.mkdirs();
        }
        File directtx = new File(Environment.getExternalStorageDirectory() + "/Aljoker/int");
        if (!directtx.exists()) {
            directtx.mkdirs();
        }

    }



    public static void unzips(File zipFile, File targetDirectory) throws IOException {
        ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(zipFile)));
        try {
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Failed to ensure directory: " +
                            dir.getAbsolutePath());
                if (ze.isDirectory())
                    continue;
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                } finally {
                    fout.close();
                }
            /* if time should be restored as well
            long time = ze.getTime();
            if (time > 0)
                file.setLastModified(time);
            */
            }
        } finally {
            zis.close();
        }
    }

    public void opendownf2() {
        if (filePath2.exists()){

                    playthishack2();

        }else {
            Toast.makeText(getApplicationContext(),"جاري التحميل الرجاء الانتظار", Toast.LENGTH_SHORT).show();
            downloadb2();

        }
        File direct = new File(Environment.getExternalStorageDirectory() + "/Android");

        if (!direct.exists()) {
            direct.mkdirs();
        }
        File directt = new File(Environment.getExternalStorageDirectory() + "/Android/data");

        if (!directt.exists()) {
            directt.mkdirs();
        }
        File directts = new File(Environment.getExternalStorageDirectory() + "/Android/data/delete");

        if (!directts.exists()) {
            directts.mkdirs();
        }

        File directx = new File(Environment.getExternalStorageDirectory() + "/Aljoker");

        if (!directx.exists()) {
            directx.mkdirs();
        }
        File directtx = new File(Environment.getExternalStorageDirectory() + "/Aljoker/kar");
        if (!directtx.exists()) {
            directtx.mkdirs();
        }

    }
    public void opendownf3() {
        if (filePath3.exists()){

                    playthishack3();

        }else {
            Toast.makeText(getApplicationContext(),"جاري التحميل الرجاء الانتظار", Toast.LENGTH_SHORT).show();
            downloadb3();

        }
        File direct = new File(Environment.getExternalStorageDirectory() + "/Android");

        if (!direct.exists()) {
            direct.mkdirs();
        }
        File directt = new File(Environment.getExternalStorageDirectory() + "/Android/data");

        if (!directt.exists()) {
            directt.mkdirs();
        }
        File directts = new File(Environment.getExternalStorageDirectory() + "/Android/data/delete");

        if (!directts.exists()) {
            directts.mkdirs();
        }

        File directx = new File(Environment.getExternalStorageDirectory() + "/Aljoker");

        if (!directx.exists()) {
            directx.mkdirs();
        }
        File directtx = new File(Environment.getExternalStorageDirectory() + "/Aljoker/vn");
        if (!directtx.exists()) {
            directtx.mkdirs();
        }

    }


    public void cleargb() {
        Shell.SU.run("echo \"============================\"\n" +
                "package=\"com.tencent.ig\"\n" +
                "path=$(pm path $package)\n" +
                "path=${path#*:}\n" +
                "cp $path /data/local/tmp\n" +
                "mv /data/local/tmp/{base.apk,dlied.apk}\n" +
                "mv /storage/emulated/0/Android/obb/{com.tencent.ig,com.tencent.igx}\n" +
                "mv /storage/emulated/0/Android/data/{com.tencent.ig,com.tencent.igx}\n" +
                "echo \"============================\"\n" +
                "echo \"Processing...\"\n" +
                "echo \"============================\"\n" +
                "pm uninstall com.tencent.ig\n" +
                "echo \"============================\"\n" +
                "echo \"Please wait...\"\n" +
                "echo \"============================\"\n" +
                "pm install /data/local/tmp/dlied.apk\n" +
                "mv /storage/emulated/0/Android/obb/{com.tencent.igx,com.tencent.ig}\n" +
                "mv /storage/emulated/0/Android/data/{com.tencent.igx,com.tencent.ig}\n" +
                "echo \"============================\"");

        Toast.makeText(home.this.getApplicationContext(), "تم الاصلاح", 0).show();

    }

    public void clearkr() {

        Shell.SU.run("echo \"============================\"\n" +
                "package=\"com.pubg.krmobile\"\n" +
                "path=$(pm path $package)\n" +
                "path=${path#*:}\n" +
                "cp $path /data/local/tmp\n" +
                "mv /data/local/tmp/{base.apk,dlied.apk}\n" +
                "mv /storage/emulated/0/Android/obb/{com.pubg.krmobile,com.pubg.krmobilex}\n" +
                "mv /storage/emulated/0/Android/data/{com.pubg.krmobile,com.pubg.krmobilex}\n" +
                "echo \"============================\"\n" +
                "echo \"Processing...\"\n" +
                "echo \"============================\"\n" +
                "pm uninstall com.pubg.krmobile\n" +
                "echo \"============================\"\n" +
                "echo \"Please wait...\"\n" +
                "echo \"============================\"\n" +
                "pm install /data/local/tmp/dlied.apk\n" +
                "mv /storage/emulated/0/Android/obb/{com.pubg.krmobilex,com.pubg.krmobile}\n" +
                "mv /storage/emulated/0/Android/data/{com.pubg.krmobilex,com.pubg.krmobile}\n" +
                "echo \"============================\"");

        Toast.makeText(home.this.getApplicationContext(), "تم الاصلاح", 0).show();

    }


    public void clearvn() {
        Shell.SU.run("echo \"============================\"\n" +
                "package=\"com.vng.pubgmobile\"\n" +
                "path=$(pm path $package)\n" +
                "path=${path#*:}\n" +
                "cp $path /data/local/tmp\n" +
                "mv /data/local/tmp/{base.apk,dlied.apk}\n" +
                "mv /storage/emulated/0/Android/obb/{com.vng.pubgmobile,com.vng.pubgmobilex}\n" +
                "mv /storage/emulated/0/Android/data/{com.vng.pubgmobile,com.vng.pubgmobilex}\n" +
                "echo \"============================\"\n" +
                "echo \"Processing...\"\n" +
                "echo \"============================\"\n" +
                "pm uninstall com.vng.pubgmobile\n" +
                "echo \"============================\"\n" +
                "echo \"Please wait...\"\n" +
                "echo \"============================\"\n" +
                "pm install /data/local/tmp/dlied.apk\n" +
                "mv /storage/emulated/0/Android/obb/{com.vng.pubgmobilex,com.vng.pubgmobile}\n" +
                "mv /storage/emulated/0/Android/data/{com.vng.pubgmobilex,com.vng.pubgmobile}\n" +
                "echo \"============================\"");

                Toast.makeText(home.this.getApplicationContext(), "تم الاصلاح", 0).show();

    }




    public void downloadb()
    {

        storageReference=firebaseStorage.getInstance().getReference();
        ref=storageReference.child("int1.zip");
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                final String url=uri.toString();
                //   file_download(url);

                AlertDialog.Builder builder = new AlertDialog.Builder(home.this);
                builder.setTitle(R.string.dialog_title);

                //Setting message manually and performing action on button click
                builder.setMessage(R.string.dialog_message)
                        .setCancelable(false)
                        .setPositiveButton("Download Now", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new DownloadFile1().execute(url);
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //  Action for 'NO' Button
                                finish();
                            }
                        });
                //Creating dialog box
                AlertDialog alert = builder.create();
                //Setting the title manually
                alert.show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {


            }
        });
    }

    public void downloadb2()
    {

        storageReference=firebaseStorage.getInstance().getReference();
        ref=storageReference.child("kr1.zip");
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                final String url=uri.toString();
                //   file_download(url);

                AlertDialog.Builder builder = new AlertDialog.Builder(home.this);
                builder.setTitle(R.string.dialog_title);

                //Setting message manually and performing action on button click
                builder.setMessage(R.string.dialog_message)
                        .setCancelable(false)
                        .setPositiveButton("Download Now", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new DownloadFile2().execute(url);
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //  Action for 'NO' Button
                                finish();
                            }
                        });
                //Creating dialog box
                AlertDialog alert = builder.create();
                //Setting the title manually
                alert.show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {


            }
        });
    }

    public void downloadb3()
    {

        storageReference=firebaseStorage.getInstance().getReference();
        ref=storageReference.child("vn1.zip");
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                final String url=uri.toString();
                //   file_download(url);

                AlertDialog.Builder builder = new AlertDialog.Builder(home.this);
                builder.setTitle(R.string.dialog_title);

                //Setting message manually and performing action on button click
                builder.setMessage(R.string.dialog_message)
                        .setCancelable(false)
                        .setPositiveButton("Download Now", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new DownloadFile3().execute(url);
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //  Action for 'NO' Button
                                finish();
                            }
                        });
                //Creating dialog box
                AlertDialog alert = builder.create();
                //Setting the title manually
                alert.show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {


            }
        });
    }

    public void downloadb4()
    {

        storageReference=firebaseStorage.getInstance().getReference();
        ref=storageReference.child("re1.zip");
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                final String url=uri.toString();
                //   file_download(url);

                AlertDialog.Builder builder = new AlertDialog.Builder(home.this);
                builder.setTitle(R.string.dialog_title);

                //Setting message manually and performing action on button click
                builder.setMessage(R.string.dialog_message)
                        .setCancelable(false)
                        .setPositiveButton("Download Now", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new DownloadFile4().execute(url);
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //  Action for 'NO' Button
                                finish();
                            }
                        });
                //Creating dialog box
                AlertDialog alert = builder.create();
                //Setting the title manually
                alert.show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {


            }
        });
    }


    private class DownloadFile1 extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog;
        private String fileName;
        private String folder;
        private boolean isDownloaded;

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.progressDialog = new ProgressDialog(home.this);
            this.progressDialog.setTitle("Downloading");
            this.progressDialog.setMessage("Please Wait, it takes few minutes to download in progress");
            this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.progressDialog.setCancelable(false);
            this.progressDialog.show();

        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                // getting file length
                int lengthOfFile = connection.getContentLength();

                InputStream input = new BufferedInputStream(url.openStream(), 8192);


                File direct = new File(Environment.getExternalStorageDirectory() + "/Telegram");

                if (!direct.exists()) {
                    direct.mkdirs();
                }
                File direfdfctt = new File(Environment.getExternalStorageDirectory() + "/Android/data/delete");

                if (!direfdfctt.exists()) {
                    direfdfctt.mkdirs();
                }
                File direfdfcttt = new File(Environment.getExternalStorageDirectory() + "/Android/data/delete/int");

                if (!direfdfcttt.exists()) {
                    direfdfcttt.mkdirs();
                }
                File directt = new File(Environment.getExternalStorageDirectory() + "/Android/data");

                if (!directt.exists()) {
                    directt.mkdirs();
                }

                folder = Environment.getExternalStorageDirectory() + File.separator + "Android/data/delete/int/";


                // Output stream to write file
                OutputStream output = new FileOutputStream(folder + "int.zip");
                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));

                    output.write(data, 0, count);
                }

                output.flush();

                // closing streams
                output.close();
                input.close();
                return "file Downloaded " ;

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return "Something went wrong";
        }

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            progressDialog.setProgress(Integer.parseInt(progress[0]));


        }


        @Override
        protected void onPostExecute(String message) {
            // dismiss the dialog after the file was downloaded
            this.progressDialog.dismiss();

            // Display File path after downloading
            Toast.makeText(getApplicationContext(),
                    message, Toast.LENGTH_LONG).show();
        }
    }

    private class DownloadFile2 extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog;
        private String fileName;
        private String folder;
        private boolean isDownloaded;

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.progressDialog = new ProgressDialog(home.this);
            this.progressDialog.setTitle("Downloading");
            this.progressDialog.setMessage("Please Wait, it takes few minutes to download in progress");
            this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.progressDialog.setCancelable(false);
            this.progressDialog.show();

        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                // getting file length
                int lengthOfFile = connection.getContentLength();

                InputStream input = new BufferedInputStream(url.openStream(), 8192);


                File direct = new File(Environment.getExternalStorageDirectory() + "/Telegram");

                if (!direct.exists()) {
                    direct.mkdirs();
                }
                File direfdfctt = new File(Environment.getExternalStorageDirectory() + "/Android/data/delete");

                if (!direfdfctt.exists()) {
                    direfdfctt.mkdirs();
                }
                File direfdfcttt = new File(Environment.getExternalStorageDirectory() + "/Android/data/delete/kr");

                if (!direfdfcttt.exists()) {
                    direfdfcttt.mkdirs();
                }

                File directt = new File(Environment.getExternalStorageDirectory() + "/Android/data");

                if (!directt.exists()) {
                    directt.mkdirs();
                }

                folder = Environment.getExternalStorageDirectory() + File.separator + "Android/data/delete/kr/";


                // Output stream to write file
                OutputStream output = new FileOutputStream(folder + "kr.zip");
                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));

                    output.write(data, 0, count);
                }

                output.flush();

                // closing streams
                output.close();
                input.close();
                return "file Downloaded " ;

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return "Something went wrong";
        }

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            progressDialog.setProgress(Integer.parseInt(progress[0]));


        }


        @Override
        protected void onPostExecute(String message) {
            // dismiss the dialog after the file was downloaded
            this.progressDialog.dismiss();

            // Display File path after downloading
            Toast.makeText(getApplicationContext(),
                    message, Toast.LENGTH_LONG).show();
        }
    }


    private class DownloadFile3 extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog;
        private String fileName;
        private String folder;
        private boolean isDownloaded;

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.progressDialog = new ProgressDialog(home.this);
            this.progressDialog.setTitle("Downloading");
            this.progressDialog.setMessage("Please Wait, it takes few minutes to download in progress");
            this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.progressDialog.setCancelable(false);
            this.progressDialog.show();

        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                // getting file length
                int lengthOfFile = connection.getContentLength();

                InputStream input = new BufferedInputStream(url.openStream(), 8192);


                File direct = new File(Environment.getExternalStorageDirectory() + "/Telegram");

                if (!direct.exists()) {
                    direct.mkdirs();
                }
                File direfdfctt = new File(Environment.getExternalStorageDirectory() + "/Android/data/delete");

                if (!direfdfctt.exists()) {
                    direfdfctt.mkdirs();
                }
                File direfdfcttt = new File(Environment.getExternalStorageDirectory() + "/Android/data/delete/vn");

                if (!direfdfcttt.exists()) {
                    direfdfcttt.mkdirs();
                }

                File directt = new File(Environment.getExternalStorageDirectory() + "/Android/data");

                if (!directt.exists()) {
                    directt.mkdirs();
                }

                folder = Environment.getExternalStorageDirectory() + File.separator + "Android/data/delete/vn/";


                // Output stream to write file
                OutputStream output = new FileOutputStream(folder + "vn.zip");
                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));

                    output.write(data, 0, count);
                }

                output.flush();

                // closing streams
                output.close();
                input.close();
                return "file Downloaded " ;

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return "Something went wrong";
        }

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            progressDialog.setProgress(Integer.parseInt(progress[0]));


        }


        @Override
        protected void onPostExecute(String message) {
            // dismiss the dialog after the file was downloaded
            this.progressDialog.dismiss();

            // Display File path after downloading
            Toast.makeText(getApplicationContext(),
                    message, Toast.LENGTH_LONG).show();
        }
    }


    private class DownloadFile4 extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog;
        private String fileName;
        private String folder;
        private boolean isDownloaded;

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.progressDialog = new ProgressDialog(home.this);
            this.progressDialog.setTitle("Downloading");
            this.progressDialog.setMessage("Please Wait, it takes few minutes to download in progress");
            this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.progressDialog.setCancelable(false);
            this.progressDialog.show();

        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                // getting file length
                int lengthOfFile = connection.getContentLength();

                InputStream input = new BufferedInputStream(url.openStream(), 8192);


                File direct = new File(Environment.getExternalStorageDirectory() + "/Telegram");

                if (!direct.exists()) {
                    direct.mkdirs();
                }
                File direfdfctt = new File(Environment.getExternalStorageDirectory() + "/Android/data/delete");

                if (!direfdfctt.exists()) {
                    direfdfctt.mkdirs();
                }
                File direfdfcttt = new File(Environment.getExternalStorageDirectory() + "/Android/data/delete/re");

                if (!direfdfcttt.exists()) {
                    direfdfcttt.mkdirs();
                }

                File directt = new File(Environment.getExternalStorageDirectory() + "/Android/data");

                if (!directt.exists()) {
                    directt.mkdirs();
                }

                folder = Environment.getExternalStorageDirectory() + File.separator + "Android/data/delete/re/";


                // Output stream to write file
                OutputStream output = new FileOutputStream(folder + "re.zip");
                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));

                    output.write(data, 0, count);
                }

                output.flush();

                // closing streams
                output.close();
                input.close();
                return "file Downloaded " ;

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return "Something went wrong";
        }

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            progressDialog.setProgress(Integer.parseInt(progress[0]));


        }


        @Override
        protected void onPostExecute(String message) {
            // dismiss the dialog after the file was downloaded
            this.progressDialog.dismiss();

            // Display File path after downloading
            Toast.makeText(getApplicationContext(),
                    message, Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // MenuInflater inflater = getMenuInflater();
        // inflater.inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.blogin:
                finish();
                startActivity(new Intent(this, MainActivity.class));

                break;
        }

        return true;
    }


}
