package com.example.apptt;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private EditText etCorreo, etContrasena;
    DatabaseHelper db;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        etCorreo = findViewById(R.id.et_usuario);
        etContrasena = findViewById(R.id.et_contrasena);
        Button btnIngresar = findViewById(R.id.btn_ingresar);
        btnIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              iniciarsesion();
            }
        });
    }

    private void iniciarsesion(){
        String correo = etCorreo.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();

        if(TextUtils.isEmpty(correo)||TextUtils.isEmpty(contrasena)){
            Toast.makeText(this,"Favor de ingresar su correo y contraseña",Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(correo,contrasena).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(MainActivity.this,"Acceso concedido",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this,EncyclopediaActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    String errorMessage = task.getException() != null? task.getException().getMessage():"Error al iniciar sesion";
                    Toast.makeText(MainActivity.this,errorMessage,Toast.LENGTH_SHORT).show();
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
