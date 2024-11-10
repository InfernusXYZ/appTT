package com.example.apptt;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class InsertAhorro extends AppCompatActivity {

    private EditText etingmen, etahorromen;
    private Button btncalcular, btnregresar;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_insert_ahorro);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        etingmen = findViewById(R.id.et_ingresos_mensuales);
        etahorromen = findViewById(R.id.et_ahorro_mensual);
        btncalcular = findViewById(R.id.btn_calcular);
        btnregresar = findViewById(R.id.btnregresar);

        btnregresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(InsertAhorro.this, AhorroActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btncalcular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarahorro();
            }
        });
    }

    private void guardarahorro(){
        String Ingresomen = etingmen.getText().toString();
        String Ahorromen = etahorromen.getText().toString();

        if(TextUtils.isEmpty(Ingresomen)||TextUtils.isEmpty(Ahorromen)){
            Toast.makeText(this,"Llene los campos correspondientes",Toast.LENGTH_SHORT).show();
            return;
        }

        double Ingresomensual = Double.parseDouble(Ingresomen);
        double Ahorromensual = Double.parseDouble(Ahorromen);
        double porcentajeAhorro = (Ahorromensual / Ingresomensual) * 100;
        if (Ingresomensual <= Ahorromensual){
            Toast.makeText(this,"No puede ahorra mas de lo que gana", Toast.LENGTH_SHORT).show();
            return;
        }

        String userID = mAuth.getCurrentUser().getUid();

        Map<String,Object> AhorroMap = new HashMap<>();
        AhorroMap.put("IngresoMensual",Ingresomensual);
        AhorroMap.put("AhorroMensual",Ahorromensual);
        AhorroMap.put("TasaAhorro",porcentajeAhorro);

        mDatabase.child("Ahorros").child(userID).push().setValue(AhorroMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(InsertAhorro.this,"Ingreso guardado correctamente",Toast.LENGTH_SHORT).show();
                    Intent intent= new Intent(InsertAhorro.this, AhorroActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    Toast.makeText(InsertAhorro.this,"Error al guardar el ingreso",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}