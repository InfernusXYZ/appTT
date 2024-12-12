package com.example.apptt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    EditText etCorreo;
    Button btRecuperar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        etCorreo = findViewById(R.id.et_correo_recuperar);
        btRecuperar = findViewById(R.id.btn_enviar_contrasena);
        auth = FirebaseAuth.getInstance();

        btRecuperar.setOnClickListener(v -> {
            String email = etCorreo.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa tu correo electrónico.", Toast.LENGTH_SHORT).show();
            } else {
                sendPasswordResetEmail(email);
            }
        });
    }

    private void sendPasswordResetEmail(String email) {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Correo de recuperación enviado.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error al enviar el correo: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ForgotPasswordActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }
}
