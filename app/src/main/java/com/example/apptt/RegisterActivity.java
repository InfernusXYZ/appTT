package com.example.apptt;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

    EditText etNombre,etApellido,etCorreo,etContrasena,etContrasena2,ettelefono;
    Button btnRegistrar;
    DatabaseHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        etNombre = findViewById(R.id.et_nombre);
        etApellido = findViewById(R.id.et_apellido);
        etCorreo = findViewById(R.id.et_correo);
        etContrasena = findViewById(R.id.et_contrasena);
        etContrasena2 = findViewById(R.id.et_confirmar_contrasena);
        ettelefono = findViewById(R.id.et_telefono);
        btnRegistrar = findViewById(R.id.btn_registrar);
        db = new DatabaseHelper(this);
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombre = etNombre.getText().toString();
                String apellido = etApellido.getText().toString();
                String correo = etCorreo.getText().toString();
                String contrasena = etContrasena.getText().toString();
                String confirmar = etContrasena2.getText().toString();
                String telefono = ettelefono.getText().toString();

                if (nombre.isEmpty() ||apellido.isEmpty()|| correo.isEmpty()|| contrasena.isEmpty()|| confirmar.isEmpty()|| telefono.isEmpty()){
                    Toast.makeText(RegisterActivity.this, "Porfavor llene todos los campos",Toast.LENGTH_SHORT).show();
                }
                else {
                    boolean correoexiste = db.checkMailExists(correo);
                    if (!correoexiste) {
                        boolean contrasenasiguales = contrasena.equals(confirmar);
                        if (contrasenasiguales) {
                            boolean insert = db.insertUser(nombre, apellido, correo, contrasena, telefono);
                            if (insert) {
                                Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(RegisterActivity.this, "Registro fallido", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "El correo ya fue utilizado", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
