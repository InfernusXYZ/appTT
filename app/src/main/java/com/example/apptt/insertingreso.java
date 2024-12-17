package com.example.apptt;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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

import org.checkerframework.checker.units.qual.C;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class insertingreso extends AppCompatActivity {
    private Button btregreso,btguardaring,btnFecha;
    private EditText etmonto, etConcepto;
    private DatabaseReference  mDatabase;
    private FirebaseAuth mAuth;
    private Spinner spinnerCategoria, spinnerTipo;
    private TextView tvFecha, tvConcepto;
    private String fechaseleccionada;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_insertingreso);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        btregreso = findViewById(R.id.btnregreso);
        btguardaring = findViewById(R.id.btn_guardar_monto);
        btnFecha = findViewById(R.id.btnFecha);
        tvFecha = findViewById(R.id.tvFecha);
        tvConcepto = findViewById(R.id.tv_concepto);
        etmonto = findViewById(R.id.et_monto);
        etConcepto = findViewById(R.id.et_concepto);
        spinnerCategoria = findViewById(R.id.spinner_categoria);
        spinnerTipo = findViewById(R.id.spinner_tipo);


        // Configurar el adaptador para el Spinner de Categoría
        ArrayAdapter<CharSequence> adapterCategoria = ArrayAdapter.createFromResource(this,
                R.array.opciones_categoria_ingresos, android.R.layout.simple_spinner_item);
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterCategoria);

        spinnerCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String categoria = spinnerCategoria.getSelectedItem().toString();
                if (categoria.equals("Otros ingresos")) {
                    tvConcepto.setVisibility(View.VISIBLE);
                    etConcepto.setVisibility(View.VISIBLE);
                } else {
                    tvConcepto.setVisibility(View.GONE);
                    etConcepto.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // Configurar el adaptador para el Spinner de Tipo de Ingresos
        ArrayAdapter<CharSequence> adapterTipo = ArrayAdapter.createFromResource(this,
                R.array.opciones_tipo_ingresos, android.R.layout.simple_spinner_item);
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapterTipo);

        inicializarfecha();
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

        btnFecha.setOnClickListener(v -> mostrarDatePicker());
    }

    private void guardarIngreso(){
        String categoria = spinnerCategoria.getSelectedItem().toString();
        String tipo = spinnerTipo.getSelectedItem().toString();
        String montoStr = etmonto.getText().toString();
        String concepto = etConcepto.getText().toString();

        if (categoria.equals("Otros ingresos")&&TextUtils.isEmpty(concepto)){
            Toast.makeText(this,"Ingrese el concepto por favor",Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(montoStr)||fechaseleccionada == null ||fechaseleccionada.isEmpty()){
            Toast.makeText(this,"Ingrese todos los campos por favor",Toast.LENGTH_SHORT).show();
            return;
        }

        double monto = Double.parseDouble(montoStr);
        String userID = mAuth.getCurrentUser().getUid();

        Map<String,Object> ingresoMap = new HashMap<>();
        if (categoria.equals("Otros ingresos")){
            ingresoMap.put("Fecha",fechaseleccionada);
            ingresoMap.put("Categoria", categoria);
            ingresoMap.put("Tipo", tipo);
            ingresoMap.put("Concepto",concepto);
            ingresoMap.put("Monto", monto);
        }else {
            ingresoMap.put("Fecha",fechaseleccionada);
            ingresoMap.put("Categoria", categoria);
            ingresoMap.put("Tipo", tipo);
            ingresoMap.put("Concepto",categoria);
            ingresoMap.put("Monto", monto);
        }

        mDatabase.child("Ingresos").child(userID).push().setValue(ingresoMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(insertingreso.this,"Ingreso guardado correctamente",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(insertingreso.this,BalanceActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    Toast.makeText(insertingreso.this,"Error al guardar el ingreso",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void mostrarDatePicker(){
        final Calendar calendar= Calendar.getInstance();
        int anio = calendar.get(Calendar.YEAR);
        int mes = calendar.get(Calendar.MONTH);
        int dia = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                fechaseleccionada = dayOfMonth + "/" + (month+1) + "/" + year;
                tvFecha.setText(fechaseleccionada);
        },
                anio,mes,dia);
                datePickerDialog.show();
    }

    private void inicializarfecha(){
        final Calendar calendar= Calendar.getInstance();
        int anio = calendar.get(Calendar.YEAR);
        int mes = calendar.get(Calendar.MONTH);
        int dia = calendar.get(Calendar.DAY_OF_MONTH);
        fechaseleccionada = dia + "/" + (mes+1) + "/" + anio;
        tvFecha.setText(fechaseleccionada);
    }
}