package app.personal;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Environment;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

public class LoginActivity extends AppCompatActivity {
    static {
        System.setProperty(
                "org.apache.poi.javax.xml.stream.XMLInputFactory",
                "com.fasterxml.aalto.stax.InputFactoryImpl"
        );
        System.setProperty(
                "org.apache.poi.javax.xml.stream.XMLOutputFactory",
                "com.fasterxml.aalto.stax.OutputFactoryImpl"
        );
        System.setProperty(
                "org.apache.poi.javax.xml.stream.XMLEventFactory",
                "com.fasterxml.aalto.stax.EventFactoryImpl"
        );
    }

    private static DownloadManager downloadManager;
    DataBaseHelper myDatabase;
    private ProgressDialog progressDialog;


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private long referenceID;
    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        myDatabase = new DataBaseHelper(LoginActivity.this);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        verifyStoragePermissions();
        verifyFolder();

        SharedPreferences.Editor editor = getSharedPreferences("personal", MODE_PRIVATE).edit();
        SharedPreferences prefs = getSharedPreferences("personal", MODE_PRIVATE);

        if (!prefs.getBoolean("firstLoad", true)) {
            editor.putBoolean("firstLoad", true);
            editor.apply();

            myDatabase.readPlanificacion(LoginActivity.this);
        }

        final EditText usernameEditText = findViewById(R.id.txtDniSupervisor);
        final Button loginButton = findViewById(R.id.login);

        usernameEditText.requestFocus();

        if (usernameEditText.getText().toString().equals("VersionWeb")) {
            loginButton.setEnabled(true);
        }

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (usernameEditText.getText().length() == 8 ||
                        usernameEditText.getText().toString().equals("VersionWeb")) {
                    loginButton.setEnabled(true);
                } else {
                    loginButton.setEnabled(false);
                }
            }
        };

        // usernameEditText.addTextChangedListener(afterTextChangedListener);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyStoragePermissions();

                progressDialog = ProgressDialog.show(LoginActivity.this, "",
                        "Sincronizando base de datos, espere por favor ....", true);

                new Thread()
                {
                    public void run()
                    {
                        try
                        {
                            if (makeAnalisis1(usernameEditText.getText().toString())) {
                                ObjectAgro cuadrilla = myDatabase
                                        .getCuadrilla(usernameEditText.getText().toString());

                                if (cuadrilla.id != null) {
                                    progressDialog.dismiss();
                                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    i.putExtra("dniSupervisor", usernameEditText.getText().toString());
                                    startActivity(i);
                                } else {
                                    MostrarDialogo("Ingrese un usuario valido");
                                }
                            } else {
                                MostrarDialogo("Usuario no valido");
                            }
                        }
                        catch (Exception e)
                        {
                            Log.e("tag",e.getMessage());
                            progressDialog.dismiss();
                        }
                    }
                }.start();

            }
        });
    }

    private void verifyStoragePermissions() {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        boolean checkPermission = (permission == PackageManager.PERMISSION_GRANTED);

//        Toast toastPermission = Toast.makeText(this,
//                "Is permission granted? " + checkPermission,
//                Toast.LENGTH_SHORT);
//
//        LinearLayout toastLayoutPermission = (LinearLayout) toastPermission.getView();
//        TextView toastTVPermission = (TextView) toastLayoutPermission.getChildAt(0);
//        toastTVPermission.setTextSize(30);
//        toastPermission.show();

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void verifyFolder() {
        File folder = new File(Environment.getExternalStorageDirectory().toString() +
                File.separator + "personal");

        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        } else {
            myDatabase.LeerInicial(LoginActivity.this);
        }

        if (success) {
            myDatabase.LeerInicial(LoginActivity.this);
        } else {
            // Do something else on failure
        }
    }

    private boolean makeAnalisis1(String user) {
        File folder = new File(Environment.getExternalStorageDirectory().toString() +
                File.separator + "personal" + File.separator + "analisis1.xls");

        if (!folder.exists()) {
            String response = downloadFile("https://www.agrisoftweb.com/api/excel/exportar?user=" + user);
            if (response.equals("OK")) {
                myDatabase.LeerInicial(LoginActivity.this);
            }

            return (response.equals("OK"));
        } else {
            return true;
        }
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    public void MostrarDialogo(String message) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Mensaje")
                .setMessage(message)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete

                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private String downloadFile(String url) {
        try {
            String targetFileName = "analisis1.xls";

            URL u = new URL(url);
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.connect();

            int rCode = c.getResponseCode();

            if (c.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + c.getResponseCode()
                        + " " + c.getResponseMessage();
            }

            String PATH_op = Environment.getExternalStorageDirectory() + "/personal/" + targetFileName;

            FileOutputStream f = new FileOutputStream(new File(PATH_op));

            InputStream in = c.getInputStream();

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = in.read(data)) != -1) {

                total += count;
                // publishing the progress....
                f.write(data, 0, count);
            }

            f.close();

            // myDatabase.readPlanificacion(LoginActivity.this);
        } catch (IOException e) {
            e.printStackTrace();
            return e.toString();
        }// TODO Auto-generated catch block
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();

        return "OK";
    }
}


