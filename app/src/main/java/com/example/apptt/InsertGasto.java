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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class InsertGasto extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private Spinner spinnerCategoriaGastos, spinnerTipoGastos;
    private EditText etMontoGastos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_insert_gasto);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // Inicializar Spinners para Categoría y Tipo de Gastos
        spinnerCategoriaGastos = findViewById(R.id.spinner_categoria_gastos);
        spinnerTipoGastos = findViewById(R.id.spinner_tipo_gastos);

        // Configurar el adaptador para el Spinner de Categoría de Gastos
        ArrayAdapter<CharSequence> adapterCategoriaGastos = ArrayAdapter.createFromResource(this,
                R.array.opciones_categoria_gastos, android.R.layout.simple_spinner_item);
        adapterCategoriaGastos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoriaGastos.setAdapter(adapterCategoriaGastos);

        // Configurar el adaptador para el Spinner de Tipo de Gastos
        ArrayAdapter<CharSequence> adapterTipoGastos = ArrayAdapter.createFromResource(this,
                R.array.opciones_tipo_gastos, android.R.layout.simple_spinner_item);
        adapterTipoGastos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoGastos.setAdapter(adapterTipoGastos);

        // Inicializar el campo de texto para ingresar el monto de Gastos
        etMontoGastos = findViewById(R.id.et_monto_gastos);
        Button btnguardargastos = findViewById(R.id.btn_guardar_monto_gastos);
        Button btnregresar = findViewById(R.id.btnregreso);

        btnguardargastos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarGastos();
            }
        });
        btnregresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(InsertGasto.this, BalanceActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void guardarGastos() {
        String categoria = spinnerCategoriaGastos.getSelectedItem().toString();
        String tipo = spinnerTipoGastos.getSelectedItem().toString();
        String montoStr = etMontoGastos.getText().toString();

        if (TextUtils.isEmpty(montoStr)) {
            Toast.makeText(this, "Ingrese el monto por favor", Toast.LENGTH_SHORT).show();
            return;
        }

        double monto = Double.parseDouble(montoStr);
        String userID = mAuth.getCurrentUser().getUid();

        Map<String, Object> gastoMap = new HashMap<>();
        gastoMap.put("Fecha",obtenerfecha());
        gastoMap.put("Categoria", categoria);
        gastoMap.put("Tipo", tipo);
        gastoMap.put("Monto", monto);

        mDatabase.child("Gastos").child(userID).push().setValue(gastoMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(InsertGasto.this, "Ingreso guardado correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(InsertGasto.this, "Error al guardar el ingreso", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private String obtenerfecha(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date=new Date();
        return dateFormat.format(date);
    }
}