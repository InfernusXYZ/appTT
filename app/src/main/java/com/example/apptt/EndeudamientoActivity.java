package com.example.apptt;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class EndeudamientoActivity extends AppCompatActivity {

    private EditText etTipoDeuda, etIngresosMensuales, etPagosMensuales, etDeuda, etConcepto;
    private TextView tvprogreso;/*tvHistorialEndeudamiento, tvHistorialIngresos, tvHistorialGastos, tvHistorialrelacion;*/
    private Button btnvaciar,btnCalcular, btnBorrarHistorial, btnEnciclopedia, btnBalance, btnAhorros, btnEndeudamiento, btnDeuda;
    private PieChart pieChart;
    private BarChart barChart;
    private String tipostr,pagosStr,ingresosStr;
    private double totalPagos = 0.0;
    private double totalDeudas = 0.0;
    private boolean alertamostrada = false;
    private Spinner spinnermesesanios;

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
        String text = "Poliwallet";
        // Crea un SpannableString
        SpannableString spannableString = new SpannableString(text);
        // Aplica el color "secondary" para "Poli"
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.secondary)), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Aplica el color "grey" para "wallet"
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.grey)), 5, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Establece el SpannableString en el TextView
        textViewTitle.setText(spannableString);

        //etTipoDeuda = findViewById(R.id.et_tipo_deuda);
        //etIngresosMensuales = findViewById(R.id.et_ingresos_mensuales);
        //etPagosMensuales = findViewById(R.id.et_pagos_mensuales);
        etDeuda = findViewById(R.id.et_deuda);
        etConcepto = findViewById(R.id.et_conceptdeuda);
        /*tvHistorialEndeudamiento = findViewById(R.id.tv_historial_endeudamiento);
        tvHistorialIngresos = findViewById(R.id.tv_historial_ingmen);
        tvHistorialGastos = findViewById(R.id.tv_historial_pagmen);
        tvHistorialrelacion = findViewById(R.id.tv_historial_relacion);*/
        tvprogreso = findViewById(R.id.tvdeudatotal);
        spinnermesesanios = findViewById(R.id.spinnermesesanos);
        btnCalcular = findViewById(R.id.btn_calcular);
        btnDeuda = findViewById(R.id.btn_deuda);
        btnBorrarHistorial = findViewById(R.id.btn_borrar_historial);
        btnvaciar = findViewById(R.id.btn_vaciar_historial);
        pieChart = findViewById(R.id.pieChartEndeudamiento);
        // barChart = findViewById(R.id.barChartEndeudamiento);

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
        cargarHistorial();
        cargarmesesanios();


        initializeCharts();

        btnDeuda.setOnClickListener(view -> {
            if (etDeuda.getText().toString().isEmpty() ||etConcepto.getText().toString().isEmpty()) {
                Toast.makeText(this, "Por favor llene todos los campos. de la deuda", Toast.LENGTH_SHORT).show();
                return;
            }
                agregarDeudaAFirebase();
        });
        btnCalcular.setOnClickListener(view -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user == null){
                Toast.makeText(this,"Usuario no identificado", Toast.LENGTH_SHORT).show();
                return;
            }
            DatabaseReference debesref =FirebaseDatabase.getInstance().getReference("Debes").child(user.getUid());
            debesref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()&&snapshot.hasChildren()){
                        Intent intent = new Intent(EndeudamientoActivity.this,insertpagos.class);
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(getApplicationContext(),"Para meter un pago primero debe haber un registro",Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getApplicationContext(),"Error al verificar los registros",Toast.LENGTH_SHORT).show();
                }
            });

        });
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
       /* ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(1, 0));
        BarDataSet barDataSet = new BarDataSet(barEntries, "");
        barDataSet.setColors(new int[]{R.color.gray}, this);
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.invalidate();*/
    }

    private void actualizarGraficas(double totalIngresos, double totalPagos) {
        List<PieEntry> pieEntries = new ArrayList<>();
        //List<BarEntry> barEntries = new ArrayList<>();

        if (totalIngresos > 0 || totalPagos > 0) {
            pieEntries.add(new PieEntry((float) totalIngresos, "Ingresos"));
            pieEntries.add(new PieEntry((float) totalPagos, "Pagos"));
            //barEntries.add(new BarEntry(1, (float) totalIngresos));
            //barEntries.add(new BarEntry(2, (float) totalPagos));
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Distribución Total de Ingresos y Pagos");
        pieDataSet.setColors(new int[]{getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.colorAccent)});
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.setDrawEntryLabels(false);
        pieChart.invalidate(); // refrescar la gráfica

        /*BarDataSet barDataSet = new BarDataSet(barEntries, "Total Ingresos y Pagos");
        barDataSet.setColors(new int[]{getResources().getColor(R.color.colorPrimaryDark), getResources().getColor(R.color.colorAccentLight)});
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);

        barChart.invalidate(); // refrescar la gráfica */
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
        this.recreate();
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

        String mesAnoSeleccionado = spinnermesesanios.getSelectedItem().toString();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DatabaseReference referenciaAhorros = FirebaseDatabase.getInstance().getReference("Deudas").child(user.getUid());

        referenciaAhorros.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double totalIngresos = 0;
                double totalAhorro = 0;

                for (DataSnapshot mesSnapshot : snapshot.getChildren()) {
                    // Suponiendo que el nodo hijo tiene un campo "Fecha" en formato "dd/MM/yyyy"
                    String fechaCompleta = mesSnapshot.child("Fecha").getValue(String.class);
                    if (fechaCompleta != null) {
                        // Extraer mes y año
                        String[] partesFecha = fechaCompleta.split("/");
                        if (partesFecha.length == 3) {
                            String mesAnoIngreso = partesFecha[1] + "/" + partesFecha[2]; // Formato "MM/yyyy"

                            // Comparar con la selección del Spinner
                            if (mesAnoIngreso.equals(mesAnoSeleccionado)) {
                                Double ingresoMensual = mesSnapshot.child("IngresoMensual").getValue(Double.class);
                                Double ahorroMensual = mesSnapshot.child("PagoMensual").getValue(Double.class);

                                if (ingresoMensual != null) totalIngresos += ingresoMensual;
                                if (ahorroMensual != null) totalAhorro += ahorroMensual;
                            }
                        }
                    }
                }

                actualizarGraficas(totalIngresos, totalAhorro);
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
        String concepto = String.valueOf(etConcepto.getText());
        String metainput =  etDeuda.getText().toString().trim();
        Double monto = Double.parseDouble(metainput);
        if (user == null) return;
        if (TextUtils.isEmpty(concepto)) {
            Toast.makeText(this, "El campo 'concepto' no puede estar vacío.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(metainput)) {
            Toast.makeText(this, "El campo 'monto de deuda' no puede estar vacío.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        mDatabase= FirebaseDatabase.getInstance().getReference("Debes")
                .child(userId);

        mDatabase.orderByChild("Concepto").equalTo(concepto).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Toast.makeText(EndeudamientoActivity.this,"Este concepto ya existe",Toast.LENGTH_SHORT).show();
                    return;
                }
                long totaldeudas = snapshot.getChildrenCount();
                if (totaldeudas >= 5){
                    Toast.makeText(getApplicationContext(),"Antes de meter mas deudas termian las actuales",Toast.LENGTH_SHORT).show();
                }else{
                    // Generar un ID único para la nueva deuda
                    String deudaId = mDatabase.push().getKey();

                    if (deudaId != null) {
                        Map<String, Object> nuevaDeuda = new HashMap<>();
                        nuevaDeuda.put("Fecha",obtenerfecha());
                        nuevaDeuda.put("Concepto",concepto);
                        nuevaDeuda.put("monto", monto);

                        mDatabase.child(deudaId).setValue(nuevaDeuda)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getApplicationContext(), "Deuda agregada correctamente", Toast.LENGTH_SHORT).show();
                                    Intent intent = getIntent();
                                    finish();
                                    startActivity(intent);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getApplicationContext(), "Error al agregar la deuda", Toast.LENGTH_SHORT).show();
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(),"Error al acceder a la base de datos",Toast.LENGTH_SHORT).show();
            }
        });

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
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date date=new Date();
        return dateFormat.format(date);
    }

    private void cargarHistorial() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        DatabaseReference debesRef = FirebaseDatabase.getInstance().getReference("Debes").child(userId);

        debesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot debesSnapshot) {
                List<String> conceptos = new ArrayList<>();
                List<Double> montosOriginales = new ArrayList<>();
                List<String> keys = new ArrayList<>();

                for (DataSnapshot conceptoSnapshot : debesSnapshot.getChildren()) {
                    String concepto = conceptoSnapshot.child("Concepto").getValue(String.class);
                    Double monto = conceptoSnapshot.child("monto").getValue(Double.class);

                    if (concepto != null && monto != null) {
                        conceptos.add(concepto);
                        montosOriginales.add(monto);
                        keys.add(conceptoSnapshot.getKey());
                    }
                }

                if (conceptos.isEmpty()) {
                    Toast.makeText(EndeudamientoActivity.this, "No tienes conceptos en 'Debes'.", Toast.LENGTH_SHORT).show();
                    return;
                }

                DatabaseReference deudasRef = FirebaseDatabase.getInstance().getReference("Deudas").child(userId);
                deudasRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot deudasSnapshot) {
                        List<Double> montosRestantes = new ArrayList<>();

                        for (int i = 0; i < conceptos.size(); i++) {
                            String concepto = conceptos.get(i);
                            double montoOriginal = montosOriginales.get(i);
                            double sumaPagos = 0;

                            for (DataSnapshot deudaSnapshot : deudasSnapshot.getChildren()) {
                                String tipoDeuda = deudaSnapshot.child("TipoDeuda").getValue(String.class);
                                Double pagoMensual = deudaSnapshot.child("PagoMensual").getValue(Double.class);

                                if (tipoDeuda != null && tipoDeuda.equals(concepto)) {
                                    sumaPagos += (pagoMensual != null) ? pagoMensual : 0;
                                }
                            }

                            montosRestantes.add(montoOriginal - sumaPagos);
                        }

                        // Configura el RecyclerView con los datos obtenidos
                        configurarRecyclerView(conceptos, montosRestantes, montosOriginales,keys);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(EndeudamientoActivity.this, "Error al cargar deudas.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EndeudamientoActivity.this, "Error al cargar conceptos de 'Debes'.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarRecyclerView(List<String> conceptos, List<Double> montosRestantes, List<Double> montosOriginales, List<String>keys) {
        RecyclerView recyclerView = findViewById(R.id.recyclerViewDebes);
        ProgressAdapter adapter = new ProgressAdapter(this,conceptos, montosRestantes, montosOriginales,keys);
        recyclerView.setAdapter(adapter);
    }

    private void cargarmesesanios(){
        historialdeuda.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> mesesAnos = new HashSet<>();

                for (DataSnapshot data: snapshot.getChildren()){
                    String fecha = data.child("Fecha").getValue(String.class);
                    if (fecha != null){
                        String[] partesfecha = fecha.split("/");
                        if (partesfecha.length == 3){
                            String mesAno = partesfecha[1]+"/"+partesfecha[2];
                            mesesAnos.add(mesAno);
                        }
                    }
                }

                List<String> listameseseanos = new ArrayList<>(mesesAnos);
                Collections.sort(listameseseanos, (a, b)->{
                    try {
                        SimpleDateFormat formato = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
                        Date fechaA = formato.parse(a);
                        Date fechaB = formato.parse(b);
                        return fechaA.compareTo(fechaB);
                    }catch (Exception e){
                        e.printStackTrace();
                        return 0;
                    }
                });
                llenarSpinner(listameseseanos);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(),"Error al cargar las fechas disponibles favor de revisar si hay registros",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void llenarSpinner(List<String> listaMesesAnos){

        listaMesesAnos.add(0,"Seleccionar mes/año");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,listaMesesAnos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnermesesanios.setAdapter(adapter);

        if (listaMesesAnos.size() > 1){
            spinnermesesanios.setSelection(listaMesesAnos.size()-1);
        }else{
            spinnermesesanios.setSelection(0);
        }

        spinnermesesanios.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String mesAnosSeleccionado = listaMesesAnos.get(position);
                if (!mesAnosSeleccionado.equals("Seleccionar mes/año")){
                    cargarTotalesFirebaseYActualizarGraficas();
                }else {
                    Toast.makeText(getApplicationContext(),"Favor de seleccionar un mes/año",Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
