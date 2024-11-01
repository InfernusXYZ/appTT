package com.example.apptt;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

public class insertingreso extends AppCompatActivity {
    private Button btregreso,btguardaring;
    private EditText etmonto;
    private DatabaseReference  mDatabase;
    private FirebaseAuth mAuth;
    private Spinner spinnerCategoria, spinnerTipo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_insertingreso);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        btregreso = findViewById(R.id.btnregreso);
        btguardaring = findViewById(R.id.btn_guardar_monto);
        etmonto = findViewById(R.id.et_monto);
        spinnerCategoria = findViewById(R.id.spinner_categoria);
        spinnerTipo = findViewById(R.id.spinner_tipo);


        // Configurar el adaptador para el Spinner de Categoría
        ArrayAdapter<CharSequence> adapterCategoria = ArrayAdapter.createFromResource(this,
                R.array.opciones_categoria_ingresos, android.R.layout.simple_spinner_item);
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterCategoria);

        // Configurar el adaptador para el Spinner de Tipo de Ingresos
        ArrayAdapter<CharSequence> adapterTipo = ArrayAdapter.createFromResource(this,
                R.array.opciones_tipo_ingresos, android.R.layout.simple_spinner_item);
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapterTipo);

        btregreso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(insertingreso.this, BalanceActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btguardaring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               guardarIngreso();
            }
        });
    }

    private void guardarIngreso(){
        String categoria = spinnerCategoria.getSelectedItem().toString();
        String tipo = spinnerTipo.getSelectedItem().toString();
        String montoStr = etmonto.getText().toString();

        if (TextUtils.isEmpty(montoStr)){
            Toast.makeText(this,"Ingrese el monto por favor",Toast.LENGTH_SHORT).show();
            return;
        }

        double monto = Double.parseDouble(montoStr);
        String userID = mAuth.getCurrentUser().getUid();

        Map<String,Object> ingresoMap = new HashMap<>();
        ingresoMap.put("Categoria",categoria);
        ingresoMap.put("Tipo",tipo);
        ingresoMap.put("Monto",monto);

        mDatabase.child("Ingresos").child(userID).push().setValue(ingresoMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(insertingreso.this,"Ingreso guardado correctamente",Toast.LENGTH_SHORT).show();
                    finish();
                }else {
                    Toast.makeText(insertingreso.this,"Error al guardar el ingreso",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}