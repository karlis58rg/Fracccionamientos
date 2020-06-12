package mx.hgo.fracccionamientos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Login extends AppCompatActivity {
    Button btnIniciarL;
    EditText txtUserL,txtPassL;
    String user,pass,respUser,cargarInfoUsuario,cargarInfoLogin;
    int bandera;
    SharedPreferences share;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        solicitarPermisosCamera();

        btnIniciarL = findViewById(R.id.btnIniciar);
        txtUserL = findViewById(R.id.txtUser);
        txtPassL = findViewById(R.id.txtPass);

        btnIniciarL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtUserL.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(),"EL USUARIO ES NECESARIO PARA INGRESAR",Toast.LENGTH_SHORT).show();
                }else if(txtPassL.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(),"LA CONTRASEÑA ES NECESARIA PARA INGRESAR",Toast.LENGTH_SHORT).show();
                }else if(txtUserL.getText().length() < 3){
                    Toast.makeText(getApplicationContext(),"EL USUARIO NO PUEDE SER MENOR A TRES LETRAS",Toast.LENGTH_SHORT).show();
                }else if(txtPassL.getText().length() < 3){
                    Toast.makeText(getApplicationContext(),"LA CONTRASEÑA NO PUEDE SER MENOR A TRES LETRAS",Toast.LENGTH_SHORT).show();
                }else {
                    getUsuaioL();
                }
            }
        });

        txtUserL.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
               switch (actionId){
                   case EditorInfo.IME_ACTION_NEXT:
                       break;
               }
                return false;
            }
        });

        txtPassL.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId){
                    case EditorInfo.IME_ACTION_NEXT:
                        btnIniciarL.setFocusableInTouchMode(true);
                        break;
                }
                return false;
            }
        });
    }

    /******************GET A LA BD***********************************/
    public void getUsuaioL() {
        user = txtUserL.getText().toString().toUpperCase();
        pass = txtPassL.getText().toString().toUpperCase();

        final OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .url("http://187.174.102.132:58/api/Usuarios?user="+user+"&pass="+pass)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Looper.prepare();
                Toast.makeText(getApplicationContext(),"ERROR AL OBTENER LA INFORMACIÓN, POR FAVOR VERIFIQUE SU CONEXIÓN A INTERNET",Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String myResponse = response.body().string();
                    myResponse = myResponse.replace('"',' ');
                    myResponse = myResponse.trim();
                    String resp = myResponse;
                    String valorUser = "false";
                    if(resp.equals(valorUser)){
                        Looper.prepare(); // to be able to make toast
                        Toast.makeText(getApplicationContext(), "LO SENTIMOS, USUARIO NO REGISTRADO", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }else{
                        bandera = 1;
                        respUser = resp;
                        Looper.prepare(); // to be able to make toast
                        guardarDatosUser();
                        Toast.makeText(getApplicationContext(), "ESTAMOS PROCESANDO SU SOLICITUD, UN MOMENTO POR FAVOR", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(Login.this,LoginUser.class);
                        startActivity(i);
                        finish();
                        Looper.loop();
                    }
                    Log.i("HERE", resp);
                }
            }

        });
    }

    private void guardarDatosUser(){
        share = getSharedPreferences("main",MODE_PRIVATE);
        editor = share.edit();
        editor.putString("USER",respUser);
        editor.putInt("STATUS",bandera);
        editor.commit();
    }

    //***************************** SE OPTIENEN TODOS LOS PERMISOS AL INICIAR LA APLICACIÓN *********************************//
    public void solicitarPermisosCamera() {
        if (ContextCompat.checkSelfPermission(Login.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Login.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(Login.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Login.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION}, 1000);

        }
    }
}
