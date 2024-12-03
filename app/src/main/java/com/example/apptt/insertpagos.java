package com.example.apptt;

import android.app.AlertDialog;
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
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class insertpagos extends AppCompatActivity {

    private Spinner spinnerdeudas;
    private EditText etingresos, etpagos;
    private Button btnAgregar, btnRegresar;
    private List<String> nombresdeudas;
    private ArrayAdapter<String> adapterdeudas;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private TextView tvprogreso;
    private double progresoRestante;
    private String tipostr,ingresosStr,pagosStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insertpagos);

        mAuth = FirebaseAuth.getInstance();
        String userid = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference("Debes").child(userid);

        tvprogreso = findViewById(R.id.tvprogresodeuda);
        etingresos = findViewById(R.id.et_ingresos_mensuales);
        etpagos = findViewById(R.id.et_pagos_mensuales);
        btnAgregar = findViewById(R.id.btn_Agregar_pago);
        btnRegresar = findViewById(R.id.btn_regresarP);
        spinnerdeudas = findViewById(R.id.Spinner_deudas);
        nombresdeudas = new ArrayList<>();
        adapterdeudas = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nombresdeudas);
        adapterdeudas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerdeudas.setAdapter(adapterdeudas);


        //Con esta informacion se llena el spinner
        cargarNombresDeudas();

        spinnerdeudas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String selectedoption = spinnerdeudas.getSelectedItem().toString(); // Concepto seleccionado
                String userId = user.getUid();

                // Referencia al nodo "Debes"
                DatabaseReference debesRef = FirebaseDatabase.getInstance()
                        .getReference("Debes")
                        .child(userId);

                // Buscar la meta correspondiente al concepto seleccionado
                debesRef.orderByChild("Concepto").equalTo(selectedoption).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot metaSnapshot : snapshot.getChildren()) {
                                // Obtener el monto de la meta
                                Double montoMeta = metaSnapshot.child("monto").getValue(Double.class);

                                if (montoMeta != null) {
                                    // Referencia al nodo "Deudas"
                                    DatabaseReference deudasRef = FirebaseDatabase.getInstance()
                                            .getReference("Deudas")
                                            .child(userId);

                                    // Buscar los pagos relacionados al concepto seleccionado
                                    deudasRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot deudasSnapshot) {
                                            double sumaPagos = 0;

                                            for (DataSnapshot deudaSnapshot : deudasSnapshot.getChildren()) {
                                                String tipoDeuda = deudaSnapshot.child("TipoDeuda").getValue(String.class);

                                                // Verificar si el tipo de deuda coincide con el concepto seleccionado
                                                if (tipoDeuda != null && tipoDeuda.equals(selectedoption)) {
                                                    Double pagoMensual = deudaSnapshot.child("PagoMensual").getValue(Double.class);
                                                    sumaPagos += (pagoMensual != null) ? pagoMensual : 0;
                                                }
                                            }

                                            // Calcular el progreso restante
                                            progresoRestante = montoMeta - sumaPagos;

                                            // Actualizar el TextView con el progreso
                                            tvprogreso.setText(String.format("Deuda actual: %.2f", progresoRestante));
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(insertpagos.this, "Error al cargar las deudas.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        } else {
                            tvprogreso.setText("No se encontró información para la meta seleccionada.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(insertpagos.this, "Error al cargar la meta.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tvprogreso.setText("");
            }
        });

        btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tipostr = spinnerdeudas.getSelectedItem().toString();
                ingresosStr = etingresos.getText().toString();
                pagosStr = etpagos.getText().toString();
                if(TextUtils.isEmpty(ingresosStr)||TextUtils.isEmpty(pagosStr)||TextUtils.isEmpty(tipostr)){
                    Toast.makeText(getApplicationContext(),"Llene los campos correspondientes",Toast.LENGTH_SHORT).show();
                    return;
                }
                String montopagos = etpagos.getText().toString().trim();
                if (montopagos.isEmpty()){
                    Toast.makeText(getApplicationContext(),"Por favor ingresar un monto",Toast.LENGTH_SHORT).show();
                    return;
                }
                double montoingresado = Double.parseDouble(montopagos);
                if(montoingresado <= progresoRestante){
                    guardardeuda();
                }else{
                    Toast.makeText(getApplicationContext(),"No debes pagar mas de lo que debes",Toast.LENGTH_SHORT).show();
                }

            }
        });

        btnRegresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(insertpagos.this,EndeudamientoActivity.class);
                startActivity(intent);
                finish();
            }
        });
        etingresos.setText("");
        etpagos.setText("");
    }

    private void cargarNombresDeudas(){
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                nombresdeudas.clear();
                for (DataSnapshot snapshot: datasnapshot.getChildren()){
                    String nombredeuda = snapshot.child("Concepto").getValue(String.class);
                    if (nombredeuda != null){
                        nombresdeudas.add(nombredeuda);
                    }
                }
                adapterdeudas.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(),"Error al cargar las deudas disponibles",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardardeuda(){
        double Ingresomensuales = Double.parseDouble(ingresosStr);
        double Pagomensuales = Double.parseDouble(pagosStr);
        double relacionEndeudamiento = (Pagomensuales / Ingresomensuales) * 100;

        if (Ingresomensuales <= Pagomensuales){
            Toast.makeText(this,"No puedes pagar mas de lo que ganas", Toast.LENGTH_SHORT).show();
            return;
        }

        // Aquí verificamos si la relación de endeudamiento supera el umbral (30%)
        if (relacionEndeudamiento > 30) {
            mostrarAlertaExcesoDeuda(relacionEndeudamiento);
        }

        //guardar datos en firebase
        String userID = mAuth.getCurrentUser().getUid();
        Map<String,Object> DeudaMap = new HashMap<>();
        DeudaMap.put("Fecha",obtenerfecha());
        DeudaMap.put("TipoDeuda", tipostr);
        DeudaMap.put("IngresoMensual",Ingresomensuales);
        DeudaMap.put("PagoMensual",Pagomensuales);
        DeudaMap.put("Relaciondeendeudamiento",relacionEndeudamiento);

        etingresos.setText("");
        etpagos.setText("");
        spinnerdeudas.setSelection(0);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Deudas").child(userID).push().setValue(DeudaMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(insertpagos.this,"Ingreso guardado correctamente",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(insertpagos.this,"Error al guardar el ingreso",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String obtenerfecha(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date=new Date();
        return dateFormat.format(date);
    }

    private void mostrarAlertaExcesoDeuda(double relacionEndeudamiento) {
        new AlertDialog.Builder(this)
                .setTitle("¡Alerta! Relación de Endeudamiento Alta")
                .setMessage("Tu relación de endeudamiento es del " + String.format("%.2f", relacionEndeudamiento) + "%, lo cual es mayor al 30%. Esto indica que una gran parte de tus ingresos está siendo utilizada para pagar deudas.\n\n" +
                        "Para mejorar tu situación financiera, considera reducir tus deudas, aumentar tus ingresos o ajustar tus gastos. Mantener una relación de endeudamiento por debajo del 30% es ideal para asegurar tu estabilidad financiera.")
                .setPositiveButton("Entendido", null)
                .show();
    }

    private void Progresodeuda(){

    }
}