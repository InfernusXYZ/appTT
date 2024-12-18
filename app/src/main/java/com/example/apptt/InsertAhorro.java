package com.example.apptt;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class InsertAhorro extends AppCompatActivity {

    private TextView tvFecha;
    private EditText etingmen, etahorromen;
    private Button btncalcular, btnregresar, btnfecha;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String fechaseleccionada;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_insert_ahorro);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        tvFecha = findViewById(R.id.tvFecha);
        etingmen = findViewById(R.id.et_ingresos_mensuales);
        etahorromen = findViewById(R.id.et_ahorro_mensual);
        btnfecha = findViewById(R.id.btnFecha);
        btncalcular = findViewById(R.id.btn_calcular);
        btnregresar = findViewById(R.id.btnregresar);

        inicializarfecha();
        cargaringresomensual();

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

        btnfecha.setOnClickListener(v -> mostrarDatePicker());
    }

    private void guardarahorro() {
        String Ingresomen = etingmen.getText().toString();
        String Ahorromen = etahorromen.getText().toString();

        if (TextUtils.isEmpty(Ingresomen) || TextUtils.isEmpty(Ahorromen)) {
            Toast.makeText(this, "Llene los campos correspondientes", Toast.LENGTH_SHORT).show();
            return;
        }

        double Ingresomensual = Double.parseDouble(Ingresomen);
        double Ahorromensual = Double.parseDouble(Ahorromen);
        double porcentajeAhorro = (Ahorromensual / Ingresomensual) * 100;

        if (Ingresomensual <= Ahorromensual) {
            Toast.makeText(this, "No puede ahorrar más de lo que gana", Toast.LENGTH_SHORT).show();
            return;
        }

        String userID = mAuth.getCurrentUser().getUid();

        Map<String, Object> AhorroMap = new HashMap<>();
        AhorroMap.put("Fecha", fechaseleccionada);
        AhorroMap.put("IngresoMensual", Ingresomensual);
        AhorroMap.put("AhorroMensual", Ahorromensual);
        AhorroMap.put("TasaAhorro", porcentajeAhorro);

        mDatabase.child("Ahorros").child(userID).push().setValue(AhorroMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(InsertAhorro.this, "Ingreso guardado correctamente", Toast.LENGTH_SHORT).show();
                // Iniciar la actividad para mostrar la alerta
                mostrarAlertaPorcentajeAhorro(porcentajeAhorro);
            } else {
                Toast.makeText(InsertAhorro.this, "Error al guardar el ingreso", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private String obtenerfecha(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date date=new Date();
        return dateFormat.format(date);
    }

    private void cargaringresomensual(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("Ingresos")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

// Primero obtenemos el último mes/año registrado
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String ultimoMesAno = null;
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/yyyy", Locale.getDefault());

                // Determinamos el último mes/año registrado
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Ingreso ingreso = snapshot.getValue(Ingreso.class);
                    if (ingreso != null) {
                        String fecha = ingreso.getFecha(); // Suponiendo que la fecha está en formato "dd/MM/yyyy"
                        if (fecha != null) {
                            try {
                                String[] partesFecha = fecha.split("/");
                                if (partesFecha.length == 3) {
                                    String mesAno = partesFecha[1] + "/" + partesFecha[2];
                                    if (ultimoMesAno == null || dateFormat.parse(mesAno).after(dateFormat.parse(ultimoMesAno))) {
                                        ultimoMesAno = mesAno;
                                    }
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                // Usamos el último mes/año registrado o un valor predeterminado si no hay registros
                String mesAnoSeleccionado = (ultimoMesAno != null) ? ultimoMesAno : "01/1970";

                // Ahora procesamos los ingresos con el mes/año seleccionado
                cargarIngresosPorMesAno(mesAnoSeleccionado);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Error al cargar los datos de Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarIngresosPorMesAno(String mesAnoSeleccionado) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("Ingresos")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double totalIngresos = 0.0;

                // Procesar cada ingreso en la base de datos
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Ingreso ingreso = snapshot.getValue(Ingreso.class);
                    if (ingreso != null) {
                        String fecha = ingreso.getFecha(); // Suponiendo que la fecha está en formato "dd/MM/yyyy"
                        if (fecha != null) {
                            String[] partesFecha = fecha.split("/");
                            if (partesFecha.length == 3) {
                                String mesAnoIngreso = partesFecha[1] + "/" + partesFecha[2]; // Formato "MM/yyyy"

                                // Filtrar los datos por el mes/año seleccionado
                                if (mesAnoIngreso.equals(mesAnoSeleccionado)) {
                                    double monto = ingreso.getMonto();

                                    totalIngresos += monto;
                                }
                            }
                        }
                    }
                }
                etingmen.setText(String.format("%.2f", totalIngresos));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Error al cargar los datos de Firebase", Toast.LENGTH_SHORT).show();
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

    private void mostrarAlertaPorcentajeAhorro(double porcentajeAhorro) {
        AlertDialog.Builder builder = new AlertDialog.Builder(InsertAhorro.this);
        builder.setTitle("Tasa de Ahorro");

        if (porcentajeAhorro >= 40) {
            builder.setMessage("¡Felicidades! Estás ahorrando más del 40% de tus ingresos. ¡Sigue así!");
        } else if (porcentajeAhorro >= 30) {
            builder.setMessage("Tu tasa de ahorro es buena. ¡Continúa así!");
        } else if (porcentajeAhorro >= 20) {
            builder.setMessage("Tu tasa de ahorro es moderada. ¡Intenta aumentarla un poco más!");
        } else {
            builder.setMessage("Tu tasa de ahorro es baja. Ajusta tus gastos y ahorra más.");
        }

        builder.setPositiveButton("Ok", (dialog, which) -> {
            // Configuramos el resultado y cerramos la actividad
            Intent intent = new Intent(InsertAhorro.this,AhorroActivity.class);
            startActivity(intent);
            finish();
        });

        builder.setCancelable(false); // Bloquear cierre accidental
        builder.show();
    }



}