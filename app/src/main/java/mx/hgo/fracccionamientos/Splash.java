package mx.hgo.fracccionamientos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import mx.hgo.fracccionamientos.R;

public class Splash extends AppCompatActivity {
    String cargarInfoUsuario;
    int cargarInfoStatus,cargarInfoStatusLogin;
    /*********** SHARE PREFERENCE ************/
    SharedPreferences share;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        cargarDatos();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (cargarInfoUsuario.isEmpty() != false) {
                    Intent i = new Intent(Splash.this, Login.class);
                    startActivity(i);
                    finish();
                } else if (cargarInfoStatusLogin == 2) {
                    Intent i = new Intent(Splash.this, Revision.class);
                    startActivity(i);
                    finish();
                } else if (cargarInfoStatus == 1) {
                    Intent i = new Intent(Splash.this, LoginUser.class);
                    startActivity(i);
                    finish();
                }
            }
        },2500);
    }

    public void cargarDatos(){
        share = getSharedPreferences("main",MODE_PRIVATE);
        cargarInfoUsuario = share.getString("USER","");
        cargarInfoStatus = share.getInt("STATUS",0);
        cargarInfoStatusLogin = share.getInt("STATUSUSER",0);
    }
}
