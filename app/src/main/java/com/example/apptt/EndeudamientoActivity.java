package com.example.apptt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

public class EndeudamientoActivity extends AppCompatActivity {

    private EditText etTipoDeuda, etIngresosMensuales, etPagosMensuales;
    private TextView tvHistorialEndeudamiento, tvHistorialIngresos, tvHistorialGastos, tvHistorialrelacion;
    private Button btnCalcular, btnBorrarHistorial, btnEnciclopedia, btnBalance, btnAhorros, btnEndeudamiento;
    private PieChart pieChart;
    private BarChart barChart;

    // Variables para almacenar datos
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "EndeudamientoPrefs";
    private static final String HISTORIAL_KEY = "historial";
    private static final String HISTORIAL_KEY2 = "historialI";
    private static final String HISTORIAL_KEY3 = "historialG";
    private static final String HISTORIAL_KEY4 = "historialR";

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
        tvHistorialEndeudamiento = findViewById(R.id.tv_historial_endeudamiento);
        tvHistorialIngresos = findViewById(R.id.tv_historial_ingmen);
        tvHistorialGastos = findViewById(R.id.tv_historial_pagmen);
        tvHistorialrelacion = findViewById(R.id.tv_historial_relacion);
        btnCalcular = findViewById(R.id.btn_calcular);
        btnBorrarHistorial = findViewById(R.id.btn_borrar_historial);
        pieChart = findViewById(R.id.pieChartEndeudamiento);
        barChart = findViewById(R.id.barChartEndeudamiento);

        btnEnciclopedia = findViewById(R.id.btn_enciclopedia);
        btnBalance = findViewById(R.id.btn_balance);
        btnAhorros = findViewById(R.id.btn_ahorros);
        btnEndeudamiento = findViewById(R.id.btn_endeudamiento);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        loadSavedData();

        initializeCharts();

        btnCalcular.setOnClickListener(view -> calcularEndeudamiento());
        btnBorrarHistorial.setOnClickListener(view -> borrarHistorial());

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
        tvHistorialEndeudamiento.append(historial + "\n");
        tvHistorialIngresos.append(historial2 + "\n");
        tvHistorialGastos.append(historial3 + "\n");
        tvHistorialrelacion.append(historial4 + "\n");


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

    private void saveData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(HISTORIAL_KEY, tvHistorialEndeudamiento.getText().toString());
        editor.putString(HISTORIAL_KEY2, tvHistorialIngresos.getText().toString());
        editor.putString(HISTORIAL_KEY3, tvHistorialGastos.getText().toString());
        editor.putString(HISTORIAL_KEY4, tvHistorialrelacion.getText().toString());
        editor.apply();
    }

    private void loadSavedData() {
        String historial = sharedPreferences.getString(HISTORIAL_KEY, "Tipo:\n\n");
        String historial2 = sharedPreferences.getString(HISTORIAL_KEY2, "Ingreso mensual:\n\n");
        String historial3 = sharedPreferences.getString(HISTORIAL_KEY3, "Pago mensual:\n\n");
        String historial4= sharedPreferences.getString(HISTORIAL_KEY4, "Relacion de endeudamiento:\n\n");
        tvHistorialEndeudamiento.setText(historial);
        tvHistorialIngresos.setText(historial2);
        tvHistorialGastos.setText(historial3);
        tvHistorialrelacion.setText(historial4);
    }

    private void borrarHistorial() {
        // Borrar el historial
        tvHistorialEndeudamiento.setText("Tipo:\n\n");
        tvHistorialIngresos.setText("Ingreso mensual:\n\n");
        tvHistorialGastos.setText("Pago mensual:\n\n");
        tvHistorialrelacion.setText("Relacion de endeudamiento\n\n");
        // Limpiar SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Limpiar las gráficas
        initializeCharts();
        Toast.makeText(EndeudamientoActivity.this, "Historial y gráficas borradas", Toast.LENGTH_SHORT).show();
    }
}
