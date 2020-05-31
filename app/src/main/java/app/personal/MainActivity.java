package app.personal;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
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

    String dniSupervisor = "", current_idCosto = "", current_idPC = "";
    DataBaseHelper myDatabase;
    ArrayList<ObjectRecord> records = new ArrayList<>();
    ArrayList<ObjectRecordProyeccionCosechas> recordsInventario = new ArrayList<>();
    Spinner spWorkerZone, spCondicion, spClon, spSectores, spIndiceMazorca;
    EditText txtHora, txtQr;
    //MIOS
    EditText txtEstadio1,txtEstadio2, txtNroArbol, txtEstadio3, txtEstadio4;
    EditText dtpFecha;

    Calendar myCalendar = Calendar.getInstance();

    TableLayout table;
    String  fecha, id_zonaTrabajo, zonatrabajo,id_clon, decripcion_clon;
    String  id_sector_g,descripcion_sector ,id_IM_g, descripcion_IM,qr = "";
    String myIMEI;
    ArrayList<ObjectAgro> listClon = new ArrayList<ObjectAgro>();
    ArrayList<ObjectAgro> listSectores = new ArrayList<ObjectAgro>();
    ArrayList<ObjectAgro> listIndiceMazorca = new ArrayList<ObjectAgro>();
    ArrayList<ObjectAgro> workerZone = new ArrayList<ObjectAgro>();
    float estadio1_int;
    float estadio2_int;
    float estadio3_int;
    float estadio4_int;
    String nroArbol;



    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public void verifyStoragePermissions() {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myDatabase = new DataBaseHelper(MainActivity.this);

        Bundle bundle = getIntent().getExtras();
        dniSupervisor = bundle.getString("dniSupervisor");
        getFromDB();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");

        fecha = formatter.format(new Date());

        //Mio
        txtEstadio1 = (EditText) findViewById(R.id.txtEstadio1);
        txtEstadio2 = (EditText) findViewById(R.id.txtEstadio2);
        txtNroArbol = (EditText) findViewById(R.id.txtNroArbol);
        txtEstadio3 = (EditText) findViewById(R.id.txtEstadio3);
        txtEstadio4 = (EditText) findViewById(R.id.txtEstadio4);


        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                //updateLabel();
                String myFormat = "yyyy/MM/dd";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                fecha = sdf.format(myCalendar.getTime());
                dtpFecha.setText(fecha);
            }
        };

        dtpFecha = findViewById(R.id.dtpFecha);
        if (dtpFecha != null) {
            dtpFecha.setFocusable(false);
            dtpFecha.setClickable(true);
            dtpFecha.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new DatePickerDialog(MainActivity.this, date, myCalendar
                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                }
            });
        }

        spClon = findViewById(R.id.spClon);
        spClon.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if ((i == 0)) {
                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.parseColor("#717171"));
                }

                if (listClon.size() > 0) {
                    if (!(i == 0)) {
                        int pos = i - 1;
                        id_clon = listClon.get(pos).id;
                        decripcion_clon = listClon.get(pos).descripcion;
                    }
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spSectores = findViewById(R.id.spSectores);
        spSectores.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if ((i == 0)) {
                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.parseColor("#717171"));
                }

                if (listSectores.size() > 0) {
                    if (!(i == 0)) {
                        int pos = i - 1;
                        id_sector_g = listSectores.get(pos).id;
                        descripcion_sector = listSectores.get(pos).descripcion;
                    }
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spIndiceMazorca= findViewById(R.id.spIndiceMazorca);
        spIndiceMazorca.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if ((i == 0)) {
                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.parseColor("#717171"));
                }

                if (listIndiceMazorca.size() > 0) {
                    if (!(i == 0)) {
                        int pos = i - 1;
                        id_IM_g = listIndiceMazorca.get(pos).id;
                        descripcion_IM = listIndiceMazorca.get(pos).descripcion;
                    }
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



        spWorkerZone = findViewById(R.id.spWorkerZone);
        spWorkerZone.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if ((i == 0)) {
                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.parseColor("#717171"));
                }

                if (workerZone.size() > 0) {
                    if (!(i == 0)) {
                        int pos = i - 1;
                        id_zonaTrabajo = workerZone.get(pos).id;
                        zonatrabajo = workerZone.get(pos).descripcion;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });




        txtHora = findViewById(R.id.txtHoras);
        txtQr = findViewById(R.id.txtQR);
        txtQr.setFocusable(false);
        txtQr.setClickable(true);
        txtQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new IntentIntegrator(MainActivity.this).initiateScan();
            }
        });

        final Button btnSicRec = findViewById(R.id.btnSicRec);
        //final Button bexportar = findViewById(R.id.bexportar);
        final Button btnAdd = findViewById(R.id.btnAdd);
        Button btnSave = findViewById(R.id.btnSave);



        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDatabase.saveTablePermanent(dniSupervisor);
                MostrarDialogo("Datos Guardados permanentemente");
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (txtEstadio1.getText().toString().isEmpty() && txtEstadio2.getText().toString().isEmpty()
                        && txtEstadio3.getText().toString().isEmpty() && txtEstadio4.getText().toString().isEmpty()){
                    MostrarDialogo("Debe llenar al menos un campo de estadio");
                    return;
                }

                if (txtEstadio1.getText().toString().isEmpty()){
                    estadio1_int = 0;
                }else {
                    try{
                        estadio1_int = Float.parseFloat(txtEstadio1.getText().toString());
                    }catch (Exception e){
                        MostrarDialogo("El campo Estadio 1 debe ser un decimal");
                        return;
                    }
                }

                if (txtEstadio2.getText().toString().isEmpty()){
                    estadio2_int = 0;
                }else{
                    try{
                        estadio2_int = Float.parseFloat(txtEstadio2.getText().toString());
                    }catch (Exception e){
                        MostrarDialogo("El campo Estadio 2 debe ser un decimal");
                        return;
                    }
                }



                if (txtNroArbol.getText().toString().isEmpty()){
                    nroArbol = "0";
                }else {
                    try{
                        int b = Integer.parseInt(txtNroArbol.getText().toString());
                        nroArbol = String.valueOf(b);
                    }catch (Exception e){
                        MostrarDialogo("El campo arbol debe ser un entero");
                        return;
                    }
                }

                if (txtEstadio3.getText().toString().isEmpty()){
                    estadio3_int = 0;
                }else {
                    try{
                        estadio3_int = Float.parseFloat(txtEstadio3.getText().toString());
                    }catch (Exception e){
                        MostrarDialogo("El campo Estadio 3 debe ser un decimal");
                        return;
                    }
                }



                if (txtEstadio4.getText().toString().isEmpty()){
                    estadio4_int = 0;
                }else {
                    try{
                        estadio4_int = Float.parseFloat(txtEstadio4.getText().toString());
                    }catch (Exception e){
                        MostrarDialogo("El campo Estadio 4 debe ser un decimal");
                        return;
                    }
                }

                if (dtpFecha.getText().toString().isEmpty()) {
                    MostrarDialogo("Ingrese la fecha");
                    return;
                }

                if (txtQr.getText().toString().isEmpty()) {
                    MostrarDialogo("Escanee un QR");
                    return;
                }

                if (    spClon.getSelectedItemPosition() <= 0 ||
                        spClon.getSelectedItem() == null ||
                        spWorkerZone.getSelectedItem() == null ||
                        spWorkerZone.getSelectedItemPosition() <= 0 ||
                        spSectores.getSelectedItem() == null ||
                        spSectores.getSelectedItemPosition() <= 0 ||
                        spIndiceMazorca.getSelectedItem() == null ||
                        spIndiceMazorca.getSelectedItemPosition() <= 0
                        ) {

                    MostrarDialogo("Debe seleccionar todos las opciones");
                    return;
                }

                if (current_idPC.length() == 0) {
                    myDatabase.insertMapeo2(fecha,id_zonaTrabajo,  id_sector_g, id_clon, id_IM_g,nroArbol ,
                            estadio1_int,
                            estadio2_int,
                            estadio3_int, estadio4_int,
                            txtQr.getText().toString());

                    MostrarDialogo("Se agrego la información");
                } else {
                    myDatabase.updateMapeo2(current_idPC,fecha,id_zonaTrabajo,  id_sector_g, id_clon, id_IM_g, nroArbol ,
                            estadio1_int,
                            estadio2_int,
                            estadio3_int, estadio4_int,
                            txtQr.getText().toString());

                    MostrarDialogo("Se actualizo la información");

                    current_idPC = "";
                    resetTableStyle();
                }

                RestoreDatabase();
                spClon.setSelection(0);
                spWorkerZone.setSelection(0);
                spSectores.setSelection(0);
                spIndiceMazorca.setSelection(0);
                txtEstadio1.setText("");
                txtEstadio4.setText("");
                txtNroArbol.setText("");
                txtEstadio2.setText("");
                txtEstadio3.setText("");
                txtQr.setText("");
            }
        });

        table = findViewById(R.id.tabla);

        StringBuilder sbSupervisor = new StringBuilder("Supervisor: ");
        sbSupervisor.append(dniSupervisor);

        StringBuilder sbCuadrilla = new StringBuilder("Cuadrilla: ");

        btnSicRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFromDB();


                workerZone = myDatabase.RestoreFromDbZonaTrabajo();
                LLenar(spWorkerZone, 2);
                listClon = myDatabase.RestoreFromDbClon();
                LLenar(spClon, 5);
                listIndiceMazorca = myDatabase.RestoreFromDbIndiceMazorca();
                LLenar(spIndiceMazorca,15);

                listSectores = myDatabase.RestoreFromDbSectores();
                LLenar(spSectores,16);


                MostrarDialogo("Se importo satisfactoriamente");
            }
        });

        Button btnRestore = findViewById(R.id.btnRestore);
        btnRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDatabase.deleteAllProyeccionCosechas();
                myDatabase.LeerInicial(MainActivity.this);
                RestoreDatabase();
                MostrarDialogo("Se restauro satisfactoriamente");
            }
        });

        Button btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (current_idPC.length() == 0) {
                    MostrarDialogo("Seleccione un registro para borrar");
                } else {
                    myDatabase.deleteMapeo(current_idPC);
                    current_idPC = "";
                    spClon.setSelection(0);
                    spWorkerZone.setSelection(0);
                    spSectores.setSelection(0);
                    spIndiceMazorca.setSelection(0);
                    txtEstadio1.setText("");
                    txtEstadio4.setText("");
                    txtNroArbol.setText("");
                    txtEstadio2.setText("");
                    txtEstadio3.setText("");
                    txtQr.setText("");
                    RestoreDatabase();
                    MostrarDialogo("Se elimino el registro");
                }
            }
        });

        Button btnExport = findViewById(R.id.btnExport);
        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Android_id = Settings.Secure.getString(getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                verifyStoragePermissions();
                // myDatabase.writeExcelFile(0, dniSupervisor);
                File txt = myDatabase.writeTXTFile(Android_id);
                if (txt != null) {
                    MostrarDialogo("Se exporto satisfactoriamente");
                    myDatabase.deleteAllProyeccionCosechas();
                    try {
                        sendFile(dniSupervisor, txt);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    RestoreDatabase();
                    SharedPreferences.Editor editor = getSharedPreferences("personal", MODE_PRIVATE).edit();
                    editor.putBoolean("firstLoad", false);
                    editor.apply();
                } else {
                    MostrarDialogo("Error al exportar");
                }

            }
        });

        TextView tvSupervisor = findViewById(R.id.txtSupervisor);
        tvSupervisor.setText(sbSupervisor.toString());
        TextView tvCuadrilla = findViewById(R.id.tvCuadrilla);

        TextView tvFecha = findViewById(R.id.tvFecha);
        RestoreDatabase();
    }

    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

    private final OkHttpClient client = new OkHttpClient();

    public void sendFile(String user, File file) throws Exception {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "logo-square.png",
                        RequestBody.create(MEDIA_TYPE_PNG, file))
                .build();

        Request request = new Request.Builder()
                .url("https://www.agrisoftweb.com/api/excel/importar?user=" + user)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            System.out.println(response.body().string());
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null)
            if (result.getContents() != null) {
                txtQr.setText(result.getContents());
            } else {
                txtQr.setText("Error");
            }
    }

    private void RestoreDatabase() {
        recordsInventario = myDatabase.QueryTable2(dniSupervisor);
        table.removeAllViews();
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.READ_PHONE_STATE );
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Log.i("Mensaje", "No se tiene permiso.");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_PHONE_STATE }, 225);
        } else {
            Log.i("Mensaje", "Se tiene permiso!");
        }



        String strZonaTrabajo = "";
        String strParcela = "";
        String strIndiceMaiz = "";
        String strClon = "";

        for (int i = 0; i < recordsInventario.size(); i++) {
            final TableRow row = (TableRow) LayoutInflater.from(MainActivity.this).inflate(R.layout.attrib_row, null);

            ((TextView) row.findViewById(R.id.tdId)).setText(String.valueOf(recordsInventario.get(i).id_PC));
            strZonaTrabajo = recordsInventario.get(i).zonatrabajo.replace(" ","");
            ((TextView) row.findViewById(R.id.ZonaTrabajo)).setText(strZonaTrabajo);
            ((TextView) row.findViewById(R.id.ZonaTrabajo2)).setText(String.valueOf(recordsInventario.get(i).zonatrabajo));
            strParcela = recordsInventario.get(i).sector.replace(" ","");
            ((TextView) row.findViewById(R.id.tdParcela)).setText(strParcela);
            ((TextView) row.findViewById(R.id.tdParcela2)).setText(String.valueOf(recordsInventario.get(i).sector));
            strIndiceMaiz = recordsInventario.get(i).IM.replace(" ", "");
            ((TextView) row.findViewById(R.id.tdIndiceMaiz)).setText(strIndiceMaiz);
            ((TextView) row.findViewById(R.id.tdIndiceMaiz2)).setText(recordsInventario.get(i).IM);
            strClon = recordsInventario.get(i).clone.replace(" ", "");
            ((TextView) row.findViewById(R.id.tdClon)).setText(strClon);
            ((TextView) row.findViewById(R.id.tdClon2)).setText(recordsInventario.get(i).clone);
            ((TextView) row.findViewById(R.id.tdNroArbol)).setText(recordsInventario.get(i).nro_arbol);
            ((TextView) row.findViewById(R.id.tdEstadio1)).setText(recordsInventario.get(i).estadio1);
            ((TextView) row.findViewById(R.id.tdEstadio2)).setText(recordsInventario.get(i).estadio2);
            ((TextView) row.findViewById(R.id.tdEstadio3)).setText(recordsInventario.get(i).estadio3);
            ((TextView) row.findViewById(R.id.tdEstadio4)).setText(recordsInventario.get(i).estadio4);
            ((TextView) row.findViewById(R.id.tdQR)).setText(recordsInventario.get(i).qr);

            row.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    resetTableStyle();

                    TableRow t = (TableRow) view;
                    TextView firstTextView = (TextView) t.getChildAt(0);
                    String firstText = firstTextView.getText().toString();

                    workerZone = myDatabase.RestoreFromDbZonaTrabajo();
                    LLenar(spWorkerZone, 2);
                    listClon = myDatabase.RestoreFromDbClon();
                    LLenar(spClon, 5);
                    listIndiceMazorca = myDatabase.RestoreFromDbIndiceMazorca();
                    LLenar(spIndiceMazorca,15);

                    listSectores = myDatabase.RestoreFromDbSectores();
                    LLenar(spSectores,16);

                    if (current_idPC.equals(firstText)) {
                        current_idPC = "";
                        selectRow("","","","","","","0", new Date().toString(), "0", "");
                        spClon.setSelection(0);
                        spWorkerZone.setSelection(0);
                        spSectores.setSelection(0);
                        spIndiceMazorca.setSelection(0);
                        return;
                    }

                    current_idPC = firstText;

                    selectRow(((TextView) t.getChildAt(11)).getText().toString(),
                            ((TextView) t.getChildAt(13)).getText().toString(),
                            ((TextView) t.getChildAt(15)).getText().toString(),
                            ((TextView) t.getChildAt(17)).getText().toString(),
                            ((TextView) t.getChildAt(9)).getText().toString(),
                            ((TextView) t.getChildAt(8)).getText().toString(),
                            ((TextView) t.getChildAt(4)).getText().toString(),
                            ((TextView) t.getChildAt(6)).getText().toString(),
                            ((TextView) t.getChildAt(2)).getText().toString(),
                            ((TextView) t.getChildAt(19)).getText().toString());

//                    Toast.makeText(getApplicationContext(), "value was " + firstText,
//                            Toast.LENGTH_LONG).show();
                    view.setBackgroundColor(Color.RED);
                }
            });

            table.addView(row);
        }

        table.requestLayout();
    }

    public void LLenar(Spinner sp, int type) {
        ArrayList<String> asp1 = new ArrayList<>();



        if (type == 2) {
            asp1.add("Seleccione Zona de Trabajo");


            for (int i = 1; i < workerZone.size(); i++) {
                asp1.add(workerZone.get(i).descripcion);
            }
        }

        if (type == 5) {
            asp1.add("Seleccione Clon");

            for (int i = 1; i < listClon.size(); i++) {
                asp1.add(listClon.get(i).descripcion);
            }
        }

        if (type == 15) {
            asp1.add("Seleccione Indice Mazorca");

            for (int i = 1; i < listIndiceMazorca.size(); i++) {
                asp1.add(listIndiceMazorca.get(i).descripcion);
            }
        }

        if (type == 16) {
            asp1.add("Seleccione Sector");

            for (int i = 1; i < listSectores.size(); i++) {
                asp1.add(listSectores.get(i).descripcion);
            }
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, asp1);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sp.setAdapter(adapter);
    }

    /*
    public void Dialogo(final int type) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialogo);
        final EditText text = (EditText) dialog.findViewById(R.id.eddes);

        Button dialogButton = (Button) dialog.findViewById(R.id.button);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String desc = text.getText().toString();

                if (desc.isEmpty()) {
                    MostrarDialogo("Ingrese Descripción");
                    return;
                }

                if (type == 1) {
                    myDatabase.insertRecordsClon("1", desc, 1);
                }

                if (type == 2) {

                    myDatabase.insertRecordsClon("1", desc, 2);

                }

                if (type == 3) {

                    myDatabase.insertRecordsClon("1", desc, 3);

                }

                if (type == 4) {

                    myDatabase.insertRecordsClon("1", desc, 4);

                }

                if (type == 5) {

                    myDatabase.insertRecordsClon("1", desc, 5);

                }

                patron = myDatabase.RestoreFromDbPatron();
                plagas = myDatabase.RestoreFromDbPlagas();
                actividades = myDatabase.RestoreFromDbActividades();
                zonatrabajo = myDatabase.RestoreFromDbZonaTrabajo();
                clon = myDatabase.RestoreFromDbClon();


                Log.d("patron", "" + patron.size());
                Log.d("plagas", "" + plagas.size());
                Log.d("actividades", "" + actividades.size());
                Log.d("zonatrabajo", "" + zonatrabajo.size());
                Log.d("clon", "" + clon.size());

                LLenar(sp1, 1);
                LLenar(sp2, 3);
                LLenar(sp3, 2);
                LLenar(sp4, 5);


                dialog.dismiss();
            }
        });

        dialog.show();

    }
*/
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

    public void getFromDB() {
        verifyStoragePermissions();
    }

    public void selectRow( String estadio1, String estadio2, String estadio3, String estadio4,String nro_arbol,String id_clona,String id_sectora,String id_IMa, String id_workerZonea, String QR) {
        fecha = fecha;
        selectSpinnerItemByValue(spClon, id_clona);
        selectSpinnerItemByValue(spWorkerZone, id_workerZonea);
        selectSpinnerItemByValue(spIndiceMazorca, id_IMa);
        selectSpinnerItemByValue(spSectores, id_sectora);
        txtQr.setText(QR);
        txtEstadio4.setText(estadio4);
        txtEstadio1.setText(estadio1);
        txtEstadio2.setText(estadio2);
        txtNroArbol.setText(nro_arbol);
        txtEstadio3.setText(estadio3);

    }

    public void resetTableStyle() {
        TableLayout layout = (TableLayout) findViewById(R.id.tabla);

        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);

            if (child instanceof TableRow) {
                TableRow row = (TableRow) child;

                row.setBackgroundColor(Color.parseColor("#C8C4C4"));

//                for (int x = 0; x < row.getChildCount(); x++) {
//                    View view = row.getChildAt(x);
//                    view.setBackgroundColor(Color.BLUE);
//                }
            }
        }
    }

    public static void selectSpinnerItemByValue(Spinner spnr, String value) {
        for (int i = 0; i < spnr.getCount(); i++) {
            if (spnr.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spnr.setSelection(i);
                break;
            } else {
                spnr.setSelection(0);
            }
        }
    }

    public String getWorkerZoneId(String value) {
        for (int i = 0; i < workerZone.size(); i++) {
            if (workerZone.get(i).descripcion.equals(value)) return workerZone.get(i).id;
        }

        return "";
    }

}

