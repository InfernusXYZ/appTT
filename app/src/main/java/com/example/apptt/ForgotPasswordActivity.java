package com.example.apptt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {
    EditText etCorreo;
    Button btRecuperar;
    DatabaseHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        etCorreo = findViewById(R.id.et_correo_recuperar);
        btRecuperar = findViewById(R.id.btn_enviar_contrasena);
        db = new DatabaseHelper(this);

        btRecuperar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String correo = etCorreo.getText().toString().trim();

                if (correo.isEmpty()){
                    Toast.makeText(ForgotPasswordActivity.this,"Ingresa el correo por favor",Toast.LENGTH_SHORT).show();
                }else{
                    String contrasena = db.RecoveryPassword(correo);
                    if (contrasena != null){
                        Toast.makeText(ForgotPasswordActivity.this,"Tu contraseña es:"+contrasena,Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ForgotPasswordActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }else {
                        Toast.makeText(ForgotPasswordActivity.this,"El correo no existe",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
