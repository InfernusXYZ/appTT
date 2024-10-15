package com.example.apptt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.widget.TextView;

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
import java.util.Arrays;
import java.util.List;

public class AhorroActivity extends AppCompatActivity {

    private EditText etIngresosMensuales, etAhorroMensual;
    private TextView tvHistorialAhorro, tvHistorialahorromen, tvHistorialporcentge;
    private PieChart pieChart;
    private BarChart barChart;
    private List<String> historialList;
    private List<String> historialahorro;
    private List<String> historialporcentaje;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ahorro);
        Toast.makeText(AhorroActivity.this, "Se encuentra en el módulo de Tasa de Ahorros", Toast.LENGTH_SHORT).show();

        //Colores en titulo
        TextView textViewTitle = findViewById(R.id.textViewTitle);
        String text = "Polliwallet";
        // Crea un SpannableString
        SpannableString spannableString = new SpannableString(text);
        // Aplica el color "secondary" para "Poli"
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.secondary)), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Aplica el color "grey" para "wallet"
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.grey)), 5, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Establece el SpannableString en el TextView
        textViewTitle.setText(spannableString);

        etIngresosMensuales = findViewById(R.id.et_ingresos_mensuales);
        etAhorroMensual = findViewById(R.id.et_ahorro_mensual);
        tvHistorialAhorro = findViewById(R.id.tv_historial_ahorro);
        tvHistorialahorromen = findViewById(R.id.tv_historial_ahorro_men);
        tvHistorialporcentge = findViewById(R.id.tv_historial_porcentage);
        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);
        Button btnCalcular = findViewById(R.id.btn_calcular);
        Button btnBorrarHistorial = findViewById(R.id.btn_borrar_historial);
        Button btnEnciclopedia = findViewById(R.id.btn_enciclopedia);
        Button btnBalance = findViewById(R.id.btn_balance);
        Button btnAhorros = findViewById(R.id.btn_ahorros);
        Button btnEndeudamiento = findViewById(R.id.btn_endeudamiento);

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("AhorroPrefs", Context.MODE_PRIVATE);

        historialList = new ArrayList<>();
        historialahorro= new ArrayList<>();
        historialporcentaje = new ArrayList<>();
        // Cargar historial desde SharedPreferences
        loadHistorial();

        // Inicializar las gráficas en blanco
        initializeCharts();

        btnCalcular.setOnClickListener(v -> {
            String ingresosStr = etIngresosMensuales.getText().toString();
            String ahorroStr = etAhorroMensual.getText().toString();

            if (!ingresosStr.isEmpty() && !ahorroStr.isEmpty()) {
                double ingresosMensuales = Double.parseDouble(ingresosStr);
                double ahorroMensual = Double.parseDouble(ahorroStr);

                if (ingresosMensuales > 0) {
                    double porcentajeAhorro = (ahorroMensual / ingresosMensuales) * 100;
                    String ingmen = "$" + ingresosMensuales +"\n";
                    String ahomen = "$" + ahorroMensual +"\n";
                    String peraho = porcentajeAhorro +"%\n";
                    /*String resultado = "\nIngresos Mensuales: $" + ingresosMensuales +
                            ", \nAhorro Mensual: $" + ahorroMensual +
                            ", \nTasa de Ahorro: " + porcentajeAhorro + "%\n";*/
                    historialList.add(ingmen);
                    historialahorro.add(ahomen);
                    historialporcentaje.add(peraho);
                    actualizarHistorial();

                    // Guardar historial en SharedPreferences
                    saveHistorial();

                    Toast.makeText(AhorroActivity.this, "Tasa de ahorro: " + porcentajeAhorro + "%", Toast.LENGTH_LONG).show();

                    actualizarGraficas(ingresosMensuales, ahorroMensual);

                    // Vaciar los campos llenables
                    etIngresosMensuales.setText("");
                    etAhorroMensual.setText("");
                } else {
                    Toast.makeText(AhorroActivity.this, "El ingreso mensual debe ser mayor a 0.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(AhorroActivity.this, "Por favor, complete ambos campos antes de calcular.", Toast.LENGTH_SHORT).show();
            }
        });

        btnBorrarHistorial.setOnClickListener(v -> {
            historialList.clear();
            historialahorro.clear();
            historialporcentaje.clear();
            actualizarHistorial();
            initializeCharts(); // Reiniciar gráficas a estado en blanco
            clearHistorial(); // Limpiar historial en SharedPreferences
            Toast.makeText(AhorroActivity.this, "Historial y gráficas borradas", Toast.LENGTH_SHORT).show();
        });

        btnEnciclopedia.setOnClickListener(v -> startActivity(new Intent(AhorroActivity.this, EncyclopediaActivity.class)));
        btnBalance.setOnClickListener(v -> startActivity(new Intent(AhorroActivity.this, BalanceActivity.class)));
        btnAhorros.setOnClickListener(v -> recreate());
        btnEndeudamiento.setOnClickListener(v -> startActivity(new Intent(AhorroActivity.this, EndeudamientoActivity.class)));
    }

    private void actualizarHistorial() {
        StringBuilder historialTexto = new StringBuilder("Ingresos mensuales:\n\n");
        StringBuilder historialAhorro = new StringBuilder("Ahorro mensual\n\n");
        StringBuilder historialporcentage = new StringBuilder("Tasa de Ahorro:\n\n");
        for (String item : historialList) {
            historialTexto.append(item).append("\n");
        }
        for (String item : historialahorro){
            historialAhorro.append(item).append("\n");
        }
        for (String item : historialporcentaje){
            historialporcentage.append(item).append("\n");
        }
        tvHistorialAhorro.setText(historialTexto.toString());
        tvHistorialahorromen.setText(historialAhorro.toString());
        tvHistorialporcentge.setText(historialporcentage.toString());
    }

    private void actualizarGraficas(double ingresosMensuales, double ahorroMensual) {
        List<PieEntry> pieEntries = new ArrayList<>();
        List<BarEntry> barEntries = new ArrayList<>();

        if (ingresosMensuales > 0 || ahorroMensual > 0) {
            pieEntries.add(new PieEntry((float) ingresosMensuales, "Ingresos Mensuales"));
            pieEntries.add(new PieEntry((float) ahorroMensual, "Ahorro Mensual"));
            barEntries.add(new BarEntry(1, (float) ingresosMensuales));
            barEntries.add(new BarEntry(2, (float) ahorroMensual));
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Distribución de Ingresos y Ahorro");
        pieDataSet.setColors(new int[]{getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.colorAccent)});
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate(); // refrescar la gráfica

        BarDataSet barDataSet = new BarDataSet(barEntries, "Ingresos y Ahorro");
        barDataSet.setColors(new int[]{getResources().getColor(R.color.colorPrimaryDark), getResources().getColor(R.color.colorAccentLight)});
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.invalidate(); // refrescar la gráfica
    }

    private void initializeCharts() {
        // Inicializar gráfica de pastel en blanco
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(0, "")); // Entrada inicial en blanco
        PieDataSet pieDataSet = new PieDataSet(pieEntries, ""); // Crear conjunto de datos
        pieDataSet.setColors(new int[]{R.color.gray}, this); // Establecer color
        PieData pieData = new PieData(pieDataSet); // Crear datos para la gráfica de pastel
        pieChart.setData(pieData); // Asignar datos a la gráfica de pastel
        pieChart.invalidate(); // Actualizar gráfica

        // Inicializar gráfica de barras en blanco
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(1, 0)); // Entrada inicial en blanco
        BarDataSet barDataSet = new BarDataSet(barEntries, ""); // Crear conjunto de datos
        barDataSet.setColors(new int[]{R.color.gray}, this); // Establecer color
        BarData barData = new BarData(barDataSet); // Crear datos para la gráfica de barras
        barChart.setData(barData); // Asignar datos a la gráfica de barras
        barChart.invalidate(); // Actualizar gráfica
    }

    private void saveHistorial() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("historial", String.join(";", historialList)); // Guardar como string separado por ;
        editor.putString("historial2", String.join(";", historialahorro));
        editor.putString("historial3", String.join(";",historialporcentaje));
        editor.apply();
    }

    private void loadHistorial() {
        String historialGuardado = sharedPreferences.getString("historial", "");
        String historialGuardado2 = sharedPreferences.getString("historial2","");
        String historialGuardado3 = sharedPreferences.getString("historial3", "");
        if (!historialGuardado.isEmpty()) {
            String[] items = historialGuardado.split(";"); // Dividir por ;
            String[] items2 = historialGuardado2.split(";");
            String[] items3 = historialGuardado3.split(";");
            historialList.addAll(Arrays.asList(items)); // Agregar al historial
            historialahorro.addAll(Arrays.asList(items2));
            historialporcentaje.addAll(Arrays.asList(items3));
            actualizarHistorial(); // Actualizar la vista
        }
    }

    private void clearHistorial() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("historial");// Limpiar historial
        editor.remove("historial2");
        editor.remove("historial3");
        editor.apply();
    }
}
