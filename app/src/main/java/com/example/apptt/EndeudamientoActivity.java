package com.example.apptt;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class EndeudamientoActivity extends AppCompatActivity {

    private EditText etTipoDeuda, etIngresosMensuales, etPagosMensuales, etDeuda;
    private TextView tvprogreso;/*tvHistorialEndeudamiento, tvHistorialIngresos, tvHistorialGastos, tvHistorialrelacion;*/
    private Button btnvaciar,btnCalcular, btnBorrarHistorial, btnEnciclopedia, btnBalance, btnAhorros, btnEndeudamiento, btnDeuda;
    private PieChart pieChart;
    private BarChart barChart;
    private String tipostr,pagosStr,ingresosStr;
    private double totalPagos = 0.0;
    private double totalDeudas = 0.0;
    private boolean alertamostrada = false;

    // Variables para almacenar datos
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "EndeudamientoPrefs";
    private static final String HISTORIAL_KEY = "historial";
    private static final String HISTORIAL_KEY2 = "historialI";
    private static final String HISTORIAL_KEY3 = "historialG";
    private static final String HISTORIAL_KEY4 = "historialR";
    private DatabaseReference mDatabase, deudaRef, historialdebe, historialdeuda;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_endeudamiento);
        Toast.makeText(EndeudamientoActivity.this, "Se encuentra en el módulo de Relación de Endeudamiento", Toast.LENGTH_SHORT).show();

        //Colores en titulo
        TextView textViewTitle = findViewById(R.id.textViewTitle3);
        String text = "Polliwallet";
        // Crea un SpannableString
        SpannableString spannableString = new SpannableString(text);
        // Aplica el color "secondary" para "Poli"
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.secondary)), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Aplica el color "grey" para "wallet"
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.grey)), 5, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Establece el SpannableString en el TextView
        textViewTitle.setText(spannableString);

        etTipoDeuda = findViewById(R.id.et_tipo_deuda);
        etIngresosMensuales = findViewById(R.id.et_ingresos_mensuales);
        etPagosMensuales = findViewById(R.id.et_pagos_mensuales);
        etDeuda = findViewById(R.id.et_deuda);
        /*tvHistorialEndeudamiento = findViewById(R.id.tv_historial_endeudamiento);
        tvHistorialIngresos = findViewById(R.id.tv_historial_ingmen);
        tvHistorialGastos = findViewById(R.id.tv_historial_pagmen);
        tvHistorialrelacion = findViewById(R.id.tv_historial_relacion);*/
        tvprogreso = findViewById(R.id.tvdeudatotal);
        btnCalcular = findViewById(R.id.btn_calcular);
        btnDeuda = findViewById(R.id.btn_deuda);
        btnBorrarHistorial = findViewById(R.id.btn_borrar_historial);
        btnvaciar = findViewById(R.id.btn_vaciar_historial);
        pieChart = findViewById(R.id.pieChartEndeudamiento);
        barChart = findViewById(R.id.barChartEndeudamiento);

        btnEnciclopedia = findViewById(R.id.btn_enciclopedia);
        btnBalance = findViewById(R.id.btn_balance);
        btnAhorros = findViewById(R.id.btn_ahorros);
        btnEndeudamiento = findViewById(R.id.btn_endeudamiento);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        String userID = mAuth.getCurrentUser().getUid();
        historialdebe = FirebaseDatabase.getInstance().getReference("Debes").child(userID);
        historialdeuda = FirebaseDatabase.getInstance().getReference("Deudas").child(userID);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        //loadSavedData();
        cargarDatosFirebase();


        initializeCharts();
        cargarTotalesFirebaseYActualizarGraficas();

        btnDeuda.setOnClickListener(view -> agregarDeudaAFirebase());
        btnCalcular.setOnClickListener(view -> guardardeuda());
        btnvaciar.setOnClickListener(view -> confirmarReinicioDeuda());
        btnBorrarHistorial.setOnClickListener(view -> {
            Intent intent = new Intent(EndeudamientoActivity.this,DeudaHistorial.class);
            startActivity(intent);
            finish();
        });

        // Configuración de los botones de la barra inferior
        btnEnciclopedia.setOnClickListener(view -> {
            Intent intent = new Intent(EndeudamientoActivity.this, EncyclopediaActivity.class);
            startActivity(intent);
        });

        btnBalance.setOnClickListener(view -> {
            Intent intent = new Intent(EndeudamientoActivity.this, BalanceActivity.class);
            startActivity(intent);
        });

        btnAhorros.setOnClickListener(view -> {
            Intent intent = new Intent(EndeudamientoActivity.this, AhorroActivity.class);
            startActivity(intent);
        });

        btnEndeudamiento.setOnClickListener(view -> {
            Intent intent = new Intent(EndeudamientoActivity.this, EndeudamientoActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveData(); // Guardar los datos cuando la actividad se pausa (cuando se cambia de actividad)
    }

    private void calcularEndeudamiento() {
        String tipoDeuda = etTipoDeuda.getText().toString().trim();
        String ingresosMensualesStr = etIngresosMensuales.getText().toString().trim();
        String pagosMensualesStr = etPagosMensuales.getText().toString().trim();

        if (tipoDeuda.isEmpty() || ingresosMensualesStr.isEmpty() || pagosMensualesStr.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        float ingresosMensuales = Float.parseFloat(ingresosMensualesStr);
        float pagosMensuales = Float.parseFloat(pagosMensualesStr);
        float relacionEndeudamiento = (pagosMensuales / ingresosMensuales) * 100;

        // Actualizar historial
        /*String historial = "Tipo de Deuda: " + tipoDeuda + "\nIngresos Mensuales: $" + ingresosMensuales +
                "\nPagos Mensuales: $" + pagosMensuales + "\nRelación de Endeudamiento: " + relacionEndeudamiento + "%\n";*/
        String historial = tipoDeuda+"\n";
        String historial2 = "$"+ingresosMensuales+"\n";
        String historial3 = "$"+pagosMensuales+"\n";
        String historial4 = relacionEndeudamiento+"%\n";
        /*tvHistorialEndeudamiento.append(historial + "\n");
        tvHistorialIngresos.append(historial2 + "\n");
        tvHistorialGastos.append(historial3 + "\n");
        tvHistorialrelacion.append(historial4 + "\n");*/


        // Mostrar Toast con la relación de endeudamiento
        Toast.makeText(this, "Relación de Endeudamiento: " + relacionEndeudamiento + "%", Toast.LENGTH_SHORT).show();

        // Actualizar gráfica de pastel
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(pagosMensuales, "Pagos Mensuales"));
        pieEntries.add(new PieEntry(ingresosMensuales - pagosMensuales, "Pago a Deuda"));

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Relación de Endeudamiento");
        pieDataSet.setColors(new int[]{R.color.teal_200, R.color.purple_200}, this);
        PieData pieData = new PieData(pieDataSet);

        pieChart.setData(pieData);
        pieChart.invalidate(); // Refrescar la gráfica

        // Actualizar gráfica de barras
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(1, ingresosMensuales));
        barEntries.add(new BarEntry(2, pagosMensuales));

        BarDataSet barDataSet = new BarDataSet(barEntries, "Ingresos vs Pagos");
        barDataSet.setColors(new int[]{R.color.purple_500, R.color.teal_700}, this);
        BarData barData = new BarData(barDataSet);

        barChart.setData(barData);
        barChart.invalidate(); // Refrescar la gráfica

        // Vaciar los campos llenables
        etTipoDeuda.setText("");
        etIngresosMensuales.setText("");
        etPagosMensuales.setText("");
    }

    private void initializeCharts() {
        // Inicializar gráfica de pastel en blanco
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(0, ""));
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setColors(new int[]{R.color.gray}, this);
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();

        // Inicializar gráfica de barras en blanco
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(1, 0));
        BarDataSet barDataSet = new BarDataSet(barEntries, "");
        barDataSet.setColors(new int[]{R.color.gray}, this);
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.invalidate();
    }

    private void actualizarGraficas(double totalIngresos, double totalPagos) {
        List<PieEntry> pieEntries = new ArrayList<>();
        List<BarEntry> barEntries = new ArrayList<>();

        if (totalIngresos > 0 || totalPagos > 0) {
            pieEntries.add(new PieEntry((float) totalIngresos, "Ingresos"));
            pieEntries.add(new PieEntry((float) totalPagos, "Pagos"));
            barEntries.add(new BarEntry(1, (float) totalIngresos));
            barEntries.add(new BarEntry(2, (float) totalPagos));
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Distribución Total de Ingresos y Pagos");
        pieDataSet.setColors(new int[]{getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.colorAccent)});
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.setDrawEntryLabels(false);
        pieChart.invalidate(); // refrescar la gráfica

        BarDataSet barDataSet = new BarDataSet(barEntries, "Total Ingresos y Pagos");
        barDataSet.setColors(new int[]{getResources().getColor(R.color.colorPrimaryDark), getResources().getColor(R.color.colorAccentLight)});
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);

        barChart.invalidate(); // refrescar la gráfica
    }

    private void saveData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        /*editor.putString(HISTORIAL_KEY, tvHistorialEndeudamiento.getText().toString());
        editor.putString(HISTORIAL_KEY2, tvHistorialIngresos.getText().toString());
        editor.putString(HISTORIAL_KEY3, tvHistorialGastos.getText().toString());
        editor.putString(HISTORIAL_KEY4, tvHistorialrelacion.getText().toString());*/
        editor.apply();
    }

    private void loadSavedData() {
        String historial = sharedPreferences.getString(HISTORIAL_KEY, "Tipo:\n\n");
        String historial2 = sharedPreferences.getString(HISTORIAL_KEY2, "Ingreso mensual:\n\n");
        String historial3 = sharedPreferences.getString(HISTORIAL_KEY3, "Pago mensual:\n\n");
        String historial4= sharedPreferences.getString(HISTORIAL_KEY4, "Relacion de endeudamiento:\n\n");
        /*tvHistorialEndeudamiento.setText(historial);
        tvHistorialIngresos.setText(historial2);
        tvHistorialGastos.setText(historial3);
        tvHistorialrelacion.setText(historial4);*/
    }

    private void borrarHistorial() {
        // Borrar el historial
        /*tvHistorialEndeudamiento.setText("Tipo:\n\n");
        tvHistorialIngresos.setText("Ingreso mensual:\n\n");
        tvHistorialGastos.setText("Pago mensual:\n\n");
        tvHistorialrelacion.setText("Relacion de endeudamiento\n\n");*/

        historialdeuda.removeValue().addOnCompleteListener(task ->{
            if(task.isSuccessful()){
                Toast.makeText(EndeudamientoActivity.this, "Historial borrados", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(EndeudamientoActivity.this, "Error al borrar historial", Toast.LENGTH_SHORT).show();
            }
        });
        historialdebe.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Toast.makeText(EndeudamientoActivity.this, "Historial borradas", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(EndeudamientoActivity.this, "Error al borrar historial", Toast.LENGTH_SHORT).show();

            }

        });
        // Limpiar las gráficas
        initializeCharts();
        Toast.makeText(EndeudamientoActivity.this, "Historial y gráficas borradas", Toast.LENGTH_SHORT).show();
    }

    private void guardardeuda(){
        tipostr = etTipoDeuda.getText().toString();
        ingresosStr = etIngresosMensuales.getText().toString();
        pagosStr = etPagosMensuales.getText().toString();


        if(TextUtils.isEmpty(ingresosStr)||TextUtils.isEmpty(pagosStr)||TextUtils.isEmpty(tipostr)){
            Toast.makeText(this,"Llene los campos correspondientes",Toast.LENGTH_SHORT).show();
            return;
        }

        double Ingresomensuales = Double.parseDouble(ingresosStr);
        double Pagomensuales = Double.parseDouble(pagosStr);
        double relacionEndeudamiento = (Pagomensuales / Ingresomensuales) * 100;

        if (Ingresomensuales <= Pagomensuales){
            Toast.makeText(this,"No puede ahorra mas de lo que gana", Toast.LENGTH_SHORT).show();
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

        etIngresosMensuales.setText("");
        etPagosMensuales.setText("");
        etTipoDeuda.setText("");

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Deudas").child(userID).push().setValue(DeudaMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(EndeudamientoActivity.this,"Ingreso guardado correctamente",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(EndeudamientoActivity.this,"Error al guardar el ingreso",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Método para mostrar el AlertDialog si la relación de endeudamiento es mayor al 30%
    private void mostrarAlertaExcesoDeuda(double relacionEndeudamiento) {
        new AlertDialog.Builder(this)
                .setTitle("¡Alerta! Relación de Endeudamiento Alta")
                .setMessage("Tu relación de endeudamiento es del " + String.format("%.2f", relacionEndeudamiento) + "%, lo cual es mayor al 30%. Esto indica que una gran parte de tus ingresos está siendo utilizada para pagar deudas.\n\n" +
                        "Para mejorar tu situación financiera, considera reducir tus deudas, aumentar tus ingresos o ajustar tus gastos. Mantener una relación de endeudamiento por debajo del 30% es ideal para asegurar tu estabilidad financiera.")
                .setPositiveButton("Entendido", null)
                .show();
    }

    private void cargarTotalesFirebaseYActualizarGraficas() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DatabaseReference referenciaAhorros = FirebaseDatabase.getInstance().getReference("Deudas").child(user.getUid());

        referenciaAhorros.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double totalIngresos = 0;
                double totalPagos = 0;

                for (DataSnapshot mesSnapshot : snapshot.getChildren()) {
                    Double ingresoMensual = mesSnapshot.child("IngresoMensual").getValue(Double.class);
                    Double pagoMensual = mesSnapshot.child("PagoMensual").getValue(Double.class);

                    if (ingresoMensual != null) totalIngresos += ingresoMensual;
                    if (pagoMensual != null) totalPagos += pagoMensual;
                }

                actualizarGraficas(totalIngresos, totalPagos);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Error al cargar los datos", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void desactivarEdicionMeta(){
        etDeuda.setEnabled(false);
        btnDeuda.setEnabled(false);

    }

    private void activarEdicionMeta(){
        etDeuda.setEnabled(true);
        btnDeuda.setEnabled(true);

    }

    private void confirmarReinicioDeuda(){
        new AlertDialog.Builder(this)
                .setTitle("Borrado de deudas actuales")
                .setMessage("Seguro que desae eliminar la deuda actual, junto con los pagos realizador")
                .setPositiveButton("Ok",((dialog, which) -> borrarHistorial()))
                .setNegativeButton("Cancelar",null)
                .show();

    }

    private void confirmarReinicioMeta(){
        new AlertDialog.Builder(this)
                .setTitle("Libre de deudas")
                .setMessage("Felicidades las deudas fueron pagadas favor de presionar borrar deuda actual y historial para despedirte de tus deudas")
                .setPositiveButton("Ok",(dialog, which) -> {
                    alertamostrada= false;
                })
                .setCancelable(false)
                .show();

    }
    private void cargarDatosFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();

        // Referencias a las tablas en Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference("Debes").child(userId);
        deudaRef = FirebaseDatabase.getInstance().getReference("Deudas").child(userId);

        // Escuchar la tabla de Deudas
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                totalDeudas = 0.0;

                for (DataSnapshot deudaSnapshot : snapshot.getChildren()) {
                    Double monto = deudaSnapshot.child("monto").getValue(Double.class);
                    if (monto != null) {
                        totalDeudas += monto;
                    }
                }

                calcularDeudaTotal();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Error al cargar las deudas", Toast.LENGTH_SHORT).show();
            }
        });

        // Escuchar la tabla de Pagos
        deudaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                totalPagos = 0.0;

                for (DataSnapshot pagoSnapshot : snapshot.getChildren()) {
                    Double monto = pagoSnapshot.child("PagoMensual").getValue(Double.class);
                    if (monto != null) {
                        totalPagos += monto;
                    }
                }

                calcularDeudaTotal();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Error al cargar los pagos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void agregarDeudaAFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String metainput =  etDeuda.getText().toString().trim();
        Double monto = Double.parseDouble(metainput);
        if (user == null) return;

        String userId = user.getUid();
        mDatabase= FirebaseDatabase.getInstance().getReference("Debes")
                .child(userId);

        // Generar un ID único para la nueva deuda
        String deudaId = mDatabase.push().getKey();

        if (deudaId != null) {
            Map<String, Object> nuevaDeuda = new HashMap<>();
            nuevaDeuda.put("Fecha",obtenerfecha());
            nuevaDeuda.put("monto", monto);

            mDatabase.child(deudaId).setValue(nuevaDeuda)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Deuda agregada correctamente", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al agregar la deuda", Toast.LENGTH_SHORT).show();
                    });
        }
    }


    private void calcularDeudaTotal() {
        double deudaRestante = totalDeudas - totalPagos;

        // Actualizar el TextView con la deuda restante
        tvprogreso.setText(String.format("Deuda Total Restante: $%.2f", deudaRestante));

        if (deudaRestante <= 0 && !alertamostrada) {
            alertamostrada = true;
            confirmarReinicioMeta();
        }

    }

    private String obtenerfecha(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date=new Date();
        return dateFormat.format(date);
    }


}
