package mx.hgo.fracccionamientos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ControlVehicular extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView imgimagen,imgTakePlaca,imgencabezado;
    EditText txtPlaca;
    Button btnGuardarRegistro;
    String cadena,placa,fecha,hora,ruta,mensaje1,mensaje2;
    TextView latitud,longitud,direccion;
    private LocationManager locationManager;
    private Context context;
    public static String direc,municipio,estado;
    public static String direccionTurno;
    public static Double lat,lon;
    private static final int CODIGO_SOLICITUD_PERMISO = 1;
    private static final String TAG = "ControlVehicular";
    private Activity activity;
    int acceso = 0;
    AlertDialog alert = null;
    String cargarInfoUsuario;
    int bandera = 0;
    /*********** SHARE PREFERENCE ************/
    SharedPreferences share;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_vehicular);

        imgimagen = findViewById(R.id.imgPlaca);
        imgencabezado = findViewById(R.id.imgHeader);
        imgTakePlaca = findViewById(R.id.imgTomarPlaca);
        txtPlaca = findViewById(R.id.txtPlacaRevision);
        btnGuardarRegistro = findViewById(R.id.btnGuardarRevision);
        latitud = findViewById(R.id.lblLatitudR);
        longitud = findViewById(R.id.lblLongitudR);
        direccion = findViewById(R.id.lblDireccionR);
        cargarDatos();

        context = getApplicationContext();
        activity = this;
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(acceso == 0){
            solicitarPermisoLocalizacion();
        } else {
            Toast.makeText(getApplicationContext()," **EL GPS** ES OBLIGATORIO PARA EL CORRECTO FUNCIONAMIENTO DEL APLICATIVO",Toast.LENGTH_LONG).show();
        }
        locationStart();


        imgTakePlaca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bandera = 1;
                llamarItem();
            }
        });

        btnGuardarRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtPlaca.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(),"LA PLACA ES OBLIGATORIA",Toast.LENGTH_SHORT).show();
                }else if(txtPlaca.getText().length() < 3){
                    Toast.makeText(getApplicationContext(), " LA PLACA NO PUEDE SER MENOR A TRES LETRAS.", Toast.LENGTH_LONG).show();
                }else if(bandera == 0){
                    Toast.makeText(getApplicationContext(), " LA FOTO DE LA PLACA ES NECESARIA", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), " UN MOMENTO POR FAVOR, ESTO PUEDE TARDAR UNOS SEGUNDOS.", Toast.LENGTH_LONG).show();
                    insertImagen();
                    insertRevision();
                }

            }
        });
        imgencabezado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ControlVehicular.this,Revision.class);
                startActivity(i);
            }
        });

        txtPlaca.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId){
                    case EditorInfo.IME_ACTION_NEXT:
                        btnGuardarRegistro.setFocusableInTouchMode(true);
                        break;
                }
                return false;
            }
        });
    }

    //********************************** IMAGEN ***********************************//
    //****************************** ABRE LA CAMARA ***********************************//
    private  void llamarItem(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getApplicationContext().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent,REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imgimagen.setImageBitmap(imageBitmap);
            imagen();
        }else if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_CANCELED){
            Toast.makeText(getApplicationContext(), "CANCELO LA TOMA DE IMAGEN", Toast.LENGTH_LONG).show();
           imgimagen.clearAnimation();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //********************************** SE CONVIERTE A BASE64 ***********************************//

    private void imagen()
    {
        imgimagen.buildDrawingCache();
        Bitmap bitmap = imgimagen.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,70,baos);
        byte[] imgBytes = baos.toByteArray();
        String imgString = android.util.Base64.encodeToString(imgBytes, android.util.Base64.NO_WRAP);
        cadena = imgString;

        imgBytes = android.util.Base64.decode(imgString, android.util.Base64.NO_WRAP);
        Bitmap decoded= BitmapFactory.decodeByteArray(imgBytes,0,imgBytes.length);
        imgimagen.setImageBitmap(decoded);
        System.out.print("IMAGEN" + imgimagen);
        System.out.print(cadena);

    }
    //********************************** INSERTA IMAGEN AL SERVIDOR ***********************************//
    public void insertImagen() {
        placa = txtPlaca.getText().toString().toUpperCase();

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("Description",placa +".jpg" )
                .add("ImageData", cadena)
                .build();

        Request request = new Request.Builder()
                .url("http://187.174.102.132:58/api/Multimedia/")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Looper.prepare(); // to be able to make toast
                Toast.makeText(getApplicationContext(), "ERROR AL GUARDAR SU IMAGEN, POR FAVOR, VERIFIQUE SU CONEXIÓN A INTERNET", Toast.LENGTH_LONG).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = response.body().toString();  /********** ME REGRESA LA RESPUESTA DEL WS ****************/

                    ControlVehicular.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imgimagen.setImageResource(R.drawable.ic_tomar_placa);
                            bandera = 0;
                            //Toast.makeText(getApplicationContext(), " UN MOMENTO POR FAVOR, ESTO PUEDE TARDAR UNOS SEGUNDOS.", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    /******************INSERT A LA BD***********************************/
    private void insertRevision(){
        placa = txtPlaca.getText().toString().toUpperCase();
        ruta = "http://187.174.102.132:58/Images/"+placa+".jpg";

        //*************** FECHA **********************//
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        fecha = dateFormat.format(date);

        //*************** HORA **********************//
        Date time = new Date();
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        hora = timeFormat.format(time);

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("Placa",placa)
                .add("Usuario",cargarInfoUsuario)
                .add("Longitud",lon.toString())
                .add("Latitud",lat.toString())
                .add("Dia",fecha)
                .add("Hora",hora)
                .add("Ruta",ruta)
                .add("Imagen", cadena)
                .build();
        Request request = new Request.Builder()
                .url("http://187.174.102.132:58/api/Placas/")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Looper.prepare();
                Toast.makeText(getApplicationContext(),"ERROR AL GUARDAR SU REGISTRO, POR FAVOR, VERIFIQUE SU CONEXIÓN A INTERNET",Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    String myResponse = response.body().toString();
                    final String resp = myResponse;
                    ControlVehicular.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println(resp);
                            txtPlaca.setText("");
                            Toast.makeText(getApplicationContext(), "INFORMACIÓN GUARDADA CON EXITO", Toast.LENGTH_SHORT).show();

                        }
                    });
                }

            }
        });
    }

    /************************************ PERMISO DE GPS ***********************************************/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case CODIGO_SOLICITUD_PERMISO :
                int resultado = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);

                if(checarStatusPermiso(resultado)) {
                    if (!locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER)) {
                        alertaGPS();
                    }
                } else {
                    Toast.makeText(activity, "EL PERMISO DE GPS NO ESTA ACTIVO", Toast.LENGTH_SHORT).show();
                }
        }
    }

    //************************************ PERMISOS GPS ***********************************************//

    public void solicitarPermisoLocalizacion(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)){
            Toast.makeText(ControlVehicular.this, "PERMISOS ACTIVADOS", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(activity, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, CODIGO_SOLICITUD_PERMISO);
        }
    }

    private void alertaGPS(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("EL SISTEMA DE GPS ESTA DESACTIVADO, ¿DESEA ACTIVARLO?")
                .setCancelable(false)
                .setPositiveButton("SÍ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        acceso = 1;
                        startActivity(new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        acceso = 1;
                        dialogInterface.cancel();
                    }
                });
        alert = builder.create();
        alert.show();
    }

    public boolean checarStatusPermiso(int resultado){
        if(resultado == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    /***********************************************************************************************************************/
    //Apartir de aqui empezamos a obtener la direciones y coordenadas
    public void locationStart() {
        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ControlVehicular.Localizacion Local = new ControlVehicular.Localizacion();
        Local.setControlVehicular(this);
        final boolean gpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (LocationListener) Local);
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) Local);
        mensaje1 = "LOCALIZACIÓN AGREGADA";
        mensaje2 = "";
        Log.i("HERE", mensaje1);
    }

    public void setLocation(Location loc) {
        //Obtener la direccion de la calle a partir de la latitud y la longitud
        if (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(
                        loc.getLatitude(), loc.getLongitude(), 1);
                if (!list.isEmpty()) {
                    Address DirCalle = list.get(0);
                    direc = DirCalle.getAddressLine(0);
                    municipio = DirCalle.getLocality();
                    estado = DirCalle.getAdminArea();
                    if(municipio != null) {
                        municipio = DirCalle.getLocality();
                    }else{
                        municipio = "SIN INFORMACION";
                    }
                    direccion.setText(direc +" " +municipio +" "+estado);
                    direccionTurno = direc +" " +municipio +" "+estado;
                    Log.i("HERE", "dir" + direc + "mun"+ municipio + "est"+ estado);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /* Aqui empieza la Clase Localizacion */
    public class Localizacion implements LocationListener {
        ControlVehicular controlVehicular;
        public ControlVehicular getControlVehicular() {
            return controlVehicular;
        }
        public void setControlVehicular(ControlVehicular controlVehicular1) {
            this.controlVehicular = controlVehicular1;
        }
        @Override
        public void onLocationChanged(Location loc) {
            // Este metodo se ejecuta cada vez que el GPS recibe nuevas coordenadas
            // debido a la deteccion de un cambio de ubicacion
            loc.getLatitude();
            loc.getLongitude();
            lat = loc.getLatitude();
            lon = loc.getLongitude();
            String Text = "Lat = "+ loc.getLatitude() + "\n Long = " + loc.getLongitude();
            mensaje1 = Text;
            latitud.setText(lat.toString());
            longitud.setText(lon.toString());
            Log.i("HERE", mensaje1);
            this.controlVehicular.setLocation(loc);
        }
        @Override
        public void onProviderDisabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es desactivado
            mensaje1 = "GPS DESACTIVADO";
        }
        @Override
        public void onProviderEnabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es activado
            mensaje1 = "GPS ACTIVADO";
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }
    }
    public void cargarDatos(){
        share = getSharedPreferences("main",MODE_PRIVATE);
        cargarInfoUsuario = share.getString("USER","");
    }
}
