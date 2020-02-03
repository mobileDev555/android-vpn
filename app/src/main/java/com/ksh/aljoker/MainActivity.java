package com.ksh.aljoker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import eu.chainfire.libsuperuser.Shell;

import static com.ksh.aljoker.Constant.SHARED_IMEI;
import static com.ksh.aljoker.Constant.SHARED_email;
import static com.ksh.aljoker.Constant.SHARED_login;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    static final int REQUEST_WRITE_STORAGE_REQUEST_CODE = 101;
    TextView textView;
    EditText editTextEmail;
    ProgressBar progressBar;
    private String androidDeviceId;
    private Context context;
    SharedPreferences pref;
    String IMEI = "";
    private String currentDate;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference().child("AlJoker");

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_main);
        if (getDefaults("user_name", this) != null && getDefaults("user_pass", this) != null) {
            editTextEmail.setText(getDefaults("Key", this));
        }
        textView = (TextView) findViewById(R.id.textView);
        currentDate = getCurrentDate();

        pref = getApplicationContext().getSharedPreferences(Constant.SHARED_PREF, Context.MODE_PRIVATE);

        Log.e("prefvalue","login : "+pref.getString(SHARED_login, "false")+
                "Key : "+pref.getString(SHARED_email, "")+
                "imei : "+pref.getString(SHARED_IMEI, ""));


        editTextEmail = (EditText) findViewById(R.id.keylogin);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);

        pref = getApplicationContext().getSharedPreferences(Constant.SHARED_PREF, Context.MODE_PRIVATE);
        String email = pref.getString(SHARED_email, "");
        String imei = pref.getString(SHARED_IMEI, "");
        if(email != null && !email.equals("")){

            editTextEmail.setText(email);
            progressBar.setVisibility(View.VISIBLE);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {

                    if(isTimeAutomatic(MainActivity.this)){
                        progressBar.setVisibility(View.GONE);
                        currentDate = getCurrentDate();
                        userLogin();
                    }else{
                        Toast.makeText(MainActivity.this, "You must set Time Automatic", Toast.LENGTH_SHORT).show();


                    }

                    return;
                    /*if (pref.getString(SHARED_login, "false").equals("true")) {
                        //  if (!pref.getString(SHARED_email, "").equals("") && !pref.getString(SHARED_IMEI, "").equals("")) {
                        Intent intent = new Intent(MainActivity.this, home.class);
                        intent.putExtra("expDate", getDefaults("expireDate",MainActivity.this));
                        startActivity(intent);

                        finish();
                        return;
                        //    }
                    }*/
                }
            }, 2000);


        }






        androidDeviceId = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);

        textView.setText("Your key : " + androidDeviceId);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            IMEI = getUniqueIMEIId(MainActivity.this);
        } else {
            requestAppPermissions();
        }
        findViewById(R.id.blogin).setOnClickListener(this);
    }


    private void requestAppPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        if (hasReadPermissions() && hasWritePermissions() && hasPhonestatePermissions()) {
            return;
        }

        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_WRITE_STORAGE_REQUEST_CODE); // your request code
    }

    private boolean hasReadPermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }
    public boolean isTimeAutomatic(Context c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.Global.getInt(c.getContentResolver(), Settings.Global.AUTO_TIME, 0) == 1;
        } else {
            return Settings.System.getInt(c.getContentResolver(), Settings.System.AUTO_TIME, 0) == 1;
        }
    }
    private boolean hasWritePermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean hasPhonestatePermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    IMEI = getUniqueIMEIId(MainActivity.this);
                } else {
                    Toast.makeText(MainActivity.this, "تم رفض الإذن لقراءة حالة الهاتف للوصول إلى تسجيل الدخول هذا!", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }


    private void userLogin() {
        final String email = editTextEmail.getText().toString().trim();
        if (IMEI != null && !IMEI.equals("")) {
            if (email.isEmpty()) {
                editTextEmail.setError("الرجاء قم بآدخال الكود");
                editTextEmail.requestFocus();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            Query loginQuery = ref.orderByChild("Key").equalTo(email);

            ref.orderByChild("Key").equalTo(email).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            progressBar.setVisibility(View.GONE);
                            if (dataSnapshot.exists()) {
                                DataSnapshot getchildsnap = null;
                                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                                    getchildsnap = childSnapshot;
                                }
                                String getkey = getchildsnap.getKey();
                                if (!getchildsnap.child("imei").getValue().toString().equals("null")) {
                                    if (getchildsnap.child("imei").getValue().toString().equals(IMEI) && !getchildsnap.child("expireDate").getValue().toString().equals(currentDate)) {

                                        SharedPreferences.Editor login_edit = pref.edit();
                                        login_edit.putString(SHARED_login, "true");
                                        login_edit.putString(SHARED_email, email);
                                        login_edit.putString(SHARED_IMEI, IMEI);
                                        login_edit.commit();

                                        setDefaults("Key", email, MainActivity.this);
                                        Intent intent = new Intent(MainActivity.this, home.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.putExtra("expDate", getchildsnap.child("expireDate").getValue().toString());
                                        startActivity(intent);
                                        setDefaults("expireDate",getchildsnap.child("expireDate").getValue().toString(),MainActivity.this);
                                        finish();
                                    } else {
                                        Toast.makeText(MainActivity.this, "مستخدم غير صالح! حاول مجددا", Toast.LENGTH_SHORT).show();
                                    }
                                } else {

                                    ref.child(getkey).child("imei").setValue(IMEI);
                                    if (getchildsnap.child("subtime").getValue().toString().equals("30")){
                                        ref.child(getkey).child("expireDate").setValue(getCalculatedDate("dd-MM-yyyy", + 31) );
                                    }
                                    if (getchildsnap.child("subtime").getValue().toString().equals("7")){
                                        ref.child(getkey).child("expireDate").setValue(getCalculatedDate("dd-MM-yyyy", + 8));
                                    }
                                    if (getchildsnap.child("subtime").getValue().toString().equals("1")){
                                        ref.child(getkey).child("expireDate").setValue(getCalculatedDate("dd-MM-yyyy", + 2) );
                                    }
                                    if (getchildsnap.child("subtime").getValue().toString().equals("14")){
                                        ref.child(getkey).child("expireDate").setValue(getCalculatedDate("dd-MM-yyyy", + 15) );
                                    }
                                    if (getchildsnap.child("subtime").getValue().toString().equals("60")){
                                        ref.child(getkey).child("expireDate").setValue(getCalculatedDate("dd-MM-yyyy", + 61) );
                                    }

                                    SharedPreferences.Editor login_edit = pref.edit();
                                    login_edit.putString(SHARED_login, "true");
                                    login_edit.putString(SHARED_email, email);
                                    login_edit.putString(SHARED_IMEI, IMEI);
                                    login_edit.commit();
                                    setDefaults("Key", email, MainActivity.this);
                                    setDefaults("expireDate",getchildsnap.child("expireDate").getValue().toString(),MainActivity.this);

                                    Intent intent = new Intent(MainActivity.this, home.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.putExtra("expDate", getchildsnap.child("expireDate").getValue().toString());
                                    startActivity(intent);
                                    finish();
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "الاشتراك غير صحيح !", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        } else {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                IMEI = getUniqueIMEIId(MainActivity.this);
                Toast.makeText(MainActivity.this, "حاول مرة اخرى!", Toast.LENGTH_SHORT).show();
            } else {
                requestAppPermissions();
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

    }
    public static void setDefaults(String key, String value, Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(key, value);
        editor.commit();
    }
    public static String getDefaults(String key, Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, null);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.blogin:
                if(isTimeAutomatic(MainActivity.this)){

                    currentDate = getCurrentDate();
                    Shell.SU.run("rm -rf/data/data/com.ksh.aljoker/vvb");
                    userLogin();
                }else{
                    Toast.makeText(MainActivity.this, "You must set Time Automatic", Toast.LENGTH_SHORT).show();


                }
                break;
        }
    }
    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.US);
        Log.d("datecheck", sdf.format(calendar.getTime()));
        return sdf.format(calendar.getTime());
    }
    public static String getCalculatedDate(String dateFormat, int days) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat s = new SimpleDateFormat(dateFormat);
        cal.add(Calendar.DAY_OF_YEAR, days);
        return s.format(new Date(cal.getTimeInMillis()));
    }
    public String getUniqueIMEIId(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            @SuppressLint("MissingPermission")
            String imei = telephonyManager.getDeviceId();
            Log.e("imeifetch", "=" + imei);
            if (imei != null && !imei.isEmpty()) {
                return imei;
            } else {
                return Build.SERIAL;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "not_found";
    }

}