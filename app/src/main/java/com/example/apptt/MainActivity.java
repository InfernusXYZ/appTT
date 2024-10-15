package com.example.apptt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    EditText etCorreo, etContrasena;
    DatabaseHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etCorreo = findViewById(R.id.et_usuario);
        etContrasena = findViewById(R.id.et_contrasena);
        Button btnIngresar = findViewById(R.id.btn_ingresar);
        db = new DatabaseHelper(this);
        btnIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String correo = etCorreo.getText().toString();
                String contrasena = etContrasena.getText().toString();

                if (db.CheckUser(correo, contrasena)){
                    Toast.makeText(MainActivity.this,"Acceso concedido",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, EncyclopediaActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(MainActivity.this,"Acceso Denegado: Revise las licencias", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public void onRegisterClick(View view) {
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    public void onForgotPasswordClick(View view) {
        Intent intent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
        startActivity(intent);
    }
}
