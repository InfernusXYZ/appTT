package com.example.apptt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BalanceActivity extends AppCompatActivity {

    // Variable para almacenar el historial y los totales de ingresos y gastos
    private StringBuilder historial = new StringBuilder();
    private StringBuilder historial2 = new StringBuilder();
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "HistorialPrefs";
    private static final String HISTORIAL_KEY = "Historial";
    private static final String HISTORIAL_KEY2 = "Historial2";
    private double totalIngresos = 0.0;
    private double totalGastos = 0.0;
    private PieChart pieChart;
    private ArrayList<PieEntry> entries = new ArrayList<>();

    // Nuevas variables para el gráfico de ingresos por categoría
    private PieChart pieChartIngresos;
    private Map<String, Double> ingresosPorCategoria;
    private PieChart pieChartGastos;
    private Map<String, Double> gastosPorCategoria;
    private static final int[] COLORES_CATEGORIAS = {
            Color.rgb(64, 89, 128),  // Azul
            Color.rgb(149, 165, 124), // Verde
            Color.rgb(217, 184, 162), // Beige
            Color.rgb(191, 134, 134), // Rosa
            Color.rgb(179, 48, 80),   // Rojo
            Color.rgb(193, 179, 215) // Lavanda
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);
        Toast.makeText(BalanceActivity.this, "Se encuentra en el módulo de Balance Mensual", Toast.LENGTH_SHORT).show();

        //Colores en titulo
        TextView textViewTitle = findViewById(R.id.textViewTitle1);
        String text = "Polliwallet";
        // Crea un SpannableString
        SpannableString spannableString = new SpannableString(text);
        // Aplica el color "secondary" para "Poli"
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.secondary)), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Aplica el color "grey" para "wallet"
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.grey)), 5, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Establece el SpannableString en el TextView
        textViewTitle.setText(spannableString);

        final TextView tvDescripcion1 = findViewById(R.id.tv_descripcion1);
        tvDescripcion1.setText("Aquí podrás escribir el texto de la descripción...");

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Inicializar el TextView del Historial y el Balance Mensual
        final TextView tvHistorial = findViewById(R.id.tv_historial);
        final TextView tvHistorialg = findViewById(R.id.tv_historialg);
        final TextView tvBalanceMensual = findViewById(R.id.tv_balance_mensual);

        // Cargar historial guardado si existe
        String historialGuardado = sharedPreferences.getString(HISTORIAL_KEY, "");
        historial.append(historialGuardado);
        tvHistorial.setText(historial.toString());

        // Recuperar los totales de ingresos y gastos guardados
        totalIngresos = Double.longBitsToDouble(sharedPreferences.getLong("totalIngresos", Double.doubleToLongBits(0.0)));
        totalGastos = Double.longBitsToDouble(sharedPreferences.getLong("totalGastos", Double.doubleToLongBits(0.0)));

        // Inicializar la gráfica de pastel del balance
        pieChart = findViewById(R.id.pie_chart);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        configurarGraficoPrincipal();
        actualizarGrafico();  // Inicializa el gráfico al iniciar la actividad

        //Inicializacion del nuevo gráfico de ingresos
        pieChartIngresos = findViewById(R.id.pie_chart_ingresos);
        ingresosPorCategoria = new HashMap<>();
        configurarGraficoIngresos();

        //Inicialización del gráfico de gastos
        pieChartGastos = findViewById(R.id.pie_chart_gastos);
        gastosPorCategoria = new HashMap<>();
        configurarGraficoGastos();

        // Recuperar datos guardados de ingresos por categoría
        Map<String, ?> todasLasCategorias = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : todasLasCategorias.entrySet()) {
            if (entry.getKey().startsWith("categoria_")) {
                String categoria = entry.getKey().substring(10);
                double valor = Double.longBitsToDouble((Long) entry.getValue());
                ingresosPorCategoria.put(categoria, valor);
            }
        }

        // Recuperar datos guardados de gastos por categoría
        Map<String, ?> todosLosGastos = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : todosLosGastos.entrySet()) {
            if (entry.getKey().startsWith("categoria_gasto_")) {
                String categoria = entry.getKey().substring(16); // "categoria_gasto_" tiene 16 caracteres
                double valor = Double.longBitsToDouble((Long) entry.getValue());
                gastosPorCategoria.put(categoria, valor);
            }
        }

        // ---------- INICIA SECCIÓN DE INGRESOS ----------

        // Inicializar Spinners para Categoría y Tipo de Ingresos
        Spinner spinnerCategoria = findViewById(R.id.spinner_categoria);
        Spinner spinnerTipo = findViewById(R.id.spinner_tipo);

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

        // Inicializar el campo de texto para ingresar el monto de Ingresos
        EditText etMonto = findViewById(R.id.et_monto);

        // Manejar el botón para guardar el monto de Ingresos
        Button btnGuardarMonto = findViewById(R.id.btn_guardar_monto);
        btnGuardarMonto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String categoria = spinnerCategoria.getSelectedItem().toString();
                String tipo = spinnerTipo.getSelectedItem().toString();
                String montoStr = etMonto.getText().toString();

                if (!montoStr.isEmpty()) {
                    double monto = Double.parseDouble(montoStr);
                    totalIngresos += monto;

                    // Actualizar ingresos por categoría
                    double montoActual = ingresosPorCategoria.getOrDefault(categoria, 0.0);
                    ingresosPorCategoria.put(categoria, montoActual + monto);

                    // Guardar en SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putLong("categoria_" + categoria, Double.doubleToLongBits(montoActual + monto));
                    editor.apply();

                    // Agregar información al historial
                    /*historial.append("Ingreso: \n")
                            .append("Categoría: ").append(categoria)
                            .append(", Tipo: ").append(tipo)
                            .append(", Monto: $").append(monto)
                            .append("\n");*/
                    //historial.append("Ingresos totales:\n")
                    //        .append("$").append(totalIngresos);

                    // Actualizar el balance mensual
                    actualizarBalance(tvBalanceMensual);
                    actualizaringresos(tvHistorial);
                    actualizarGrafico();
                    actualizarGraficoIngresos();

                    // Guardar el historial en SharedPreferences
                    //guardarHistorial();

                    // Actualizar el TextView del historial
                    //tvHistorial.setText(historial.toString());

                    // Solo limpiar el campo de monto
                    etMonto.setText("");

                    Toast.makeText(BalanceActivity.this, "Monto de Ingresos guardado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BalanceActivity.this, "Por favor, ingrese un monto", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Inicializar Spinners para Categoría y Tipo de Gastos
        Spinner spinnerCategoriaGastos = findViewById(R.id.spinner_categoria_gastos);
        Spinner spinnerTipoGastos = findViewById(R.id.spinner_tipo_gastos);

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
        EditText etMontoGastos = findViewById(R.id.et_monto_gastos);

        // Manejar el botón para guardar el monto de Gastos
        Button btnGuardarMontoGastos = findViewById(R.id.btn_guardar_monto_gastos);
        btnGuardarMontoGastos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String categoriaGastos = spinnerCategoriaGastos.getSelectedItem().toString();
                String tipoGastos = spinnerTipoGastos.getSelectedItem().toString();
                String montoStrGastos = etMontoGastos.getText().toString();

                if (!montoStrGastos.isEmpty()) {
                    double montoGastos = Double.parseDouble(montoStrGastos);
                    totalGastos += montoGastos;

                    // Actualizar gastos por categoría
                    double montoActual = gastosPorCategoria.getOrDefault(categoriaGastos, 0.0);
                    gastosPorCategoria.put(categoriaGastos, montoActual + montoGastos);

                    // Guardar en SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putLong("categoria_gasto_" + categoriaGastos,
                            Double.doubleToLongBits(montoActual + montoGastos));
                    editor.apply();

                    // Agregar información al historial
                    /*historial.append("Gasto: \n")
                            .append("Categoría: ").append(categoriaGastos)
                            .append(", Tipo: ").append(tipoGastos)
                            .append(", Monto: $").append(montoGastos)
                            .append("\n");*/

                    //historial2.append("Gastos totales: \n")
                    //      .append(totalGastos);

                    // Actualizar el balance mensual
                    actualizarBalance(tvBalanceMensual);
                    actualizarGastos(tvHistorialg);
                    actualizarGrafico();
                    actualizarGraficoGastos();

                    // Guardar el historial en SharedPreferences
                    //guardarHistorial();

                    // Actualizar el TextView del historial
                    //tvHistorialg.setText(historial2.toString());

                    // Solo limpiar el campo de monto
                    etMontoGastos.setText("");

                    Toast.makeText(BalanceActivity.this, "Monto de Gastos guardado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BalanceActivity.this, "Por favor, ingrese un monto de Gastos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // ---------- INICIA SECCIÓN DEL BOTÓN BORRAR HISTORIAL ----------

        // Manejar el botón para borrar el historial
        Button btnBorrarHistorial = findViewById(R.id.btn_borrar_historial);
        btnBorrarHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Limpiar historial y resetear los totales
                historial.setLength(0);
                totalIngresos = 0.0;
                totalGastos = 0.0;

                // Limpiar los mapas de categorías
                ingresosPorCategoria.clear();
                gastosPorCategoria.clear();

                // Actualizar el TextView para mostrar el historial vacío
                tvHistorial.setText("0.00");
                tvHistorialg.setText("0.00");

                // Actualizar el balance a cero
                actualizarBalance(tvBalanceMensual);

                // Limpiar y actualizar todas las gráficas
                entries.clear();
                pieChart.clear();
                pieChartIngresos.clear();
                pieChartGastos.clear();

                // Reconfigurar las gráficas vacías
                configurarGraficoPrincipal();
                configurarGraficoIngresos();
                configurarGraficoGastos();

                // Forzar la actualización visual de las gráficas
                pieChart.invalidate();
                pieChartIngresos.invalidate();
                pieChartGastos.invalidate();

                // Limpiar completamente SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear(); // Elimina todos los datos guardados
                editor.apply();

                Toast.makeText(BalanceActivity.this, "Historial y gráficas borradas", Toast.LENGTH_SHORT).show();
            }
        });

        // ---------- INICIA SECCIÓN DE NAVEGACIÓN ----------

        // Inicializar los botones de navegación de la parte inferior
        Button btnEnciclopedia = findViewById(R.id.btn_enciclopedia);
        Button btnBalance = findViewById(R.id.btn_balance);
        Button btnAhorros = findViewById(R.id.btn_ahorros);
        Button btnEndeudamiento = findViewById(R.id.btn_endeudamiento);

        // Navegar a la actividad "Enciclopedia"
        btnEnciclopedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BalanceActivity.this, EncyclopediaActivity.class);
                startActivity(intent);
            }
        });

        // Recargar la actividad "Balance"
        btnBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreate(); // Recargar la actividad Balance
            }
        });

        // Navegar a la actividad "Ahorros"
        btnAhorros.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BalanceActivity.this, AhorroActivity.class);
                startActivity(intent);
            }
        });

        // Navegar a la actividad "Endeudamiento"
        btnEndeudamiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BalanceActivity.this, EndeudamientoActivity.class);
                startActivity(intent);
            }
        });
    }

    // Método para actualizar el balance mensual
    private void actualizarBalance(TextView tvBalanceMensual) {
        double balanceMensual = totalIngresos - totalGastos;
        tvBalanceMensual.setText(String.format("$ %.2f", balanceMensual));

    }

    private void actualizarGastos(TextView tvHistorialg) {
        tvHistorialg.setText("Gastos Totales\n\n"+String.format("$ %.2f", totalGastos));
    }

    private void actualizaringresos(TextView tvHistorial) {
        tvHistorial.setText("Ingresos Totales\n\n"+String.format("$ %.2f", totalIngresos));
    }

    private void configurarGraficoPrincipal() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText("Balance Total");
        pieChart.setCenterTextSize(16f);
        pieChart.animateY(1000);

        // Agregar datos iniciales vacíos (igual que en las otras gráficas)
        ArrayList<PieEntry> entriesIniciales = new ArrayList<>();
        entriesIniciales.add(new PieEntry(1f, "Sin datos"));

        PieDataSet dataSetInicial = new PieDataSet(entriesIniciales, "");
        dataSetInicial.setColor(Color.LTGRAY);
        dataSetInicial.setValueTextSize(0f); // Ocultar los valores
        dataSetInicial.setValueTextColor(Color.TRANSPARENT); // Hacer transparente el texto

        PieData pieDataInicial = new PieData(dataSetInicial);
        pieChart.setData(pieDataInicial);
        pieChart.invalidate();
    }

    private void configurarGraficoIngresos() {
        pieChartIngresos.setUsePercentValues(true);
        pieChartIngresos.getDescription().setEnabled(false);
        pieChartIngresos.setDrawHoleEnabled(true);
        pieChartIngresos.setHoleColor(Color.WHITE);
        pieChartIngresos.setTransparentCircleRadius(61f);
        pieChartIngresos.setDrawCenterText(true);
        pieChartIngresos.setCenterText("Ingresos por Categoría");
        pieChartIngresos.setCenterTextSize(16f);
        pieChartIngresos.animateY(1000);

        // Agregar datos iniciales vacíos
        ArrayList<PieEntry> entriesIniciales = new ArrayList<>();
        entriesIniciales.add(new PieEntry(1f, "Sin ingresos"));

        PieDataSet dataSetInicial = new PieDataSet(entriesIniciales, "");
        dataSetInicial.setColor(Color.LTGRAY);
        dataSetInicial.setValueTextSize(0f);

        PieData pieDataInicial = new PieData(dataSetInicial);
        pieChartIngresos.setData(pieDataInicial);
        pieChartIngresos.invalidate();
    }

    private void configurarGraficoGastos() {
        pieChartGastos.setUsePercentValues(true);
        pieChartGastos.getDescription().setEnabled(false);
        pieChartGastos.setDrawHoleEnabled(true);
        pieChartGastos.setHoleColor(Color.WHITE);
        pieChartGastos.setTransparentCircleRadius(61f);
        pieChartGastos.setDrawCenterText(true);
        pieChartGastos.setCenterText("Gastos por Categoría");
        pieChartGastos.setCenterTextSize(16f);
        pieChartGastos.animateY(1000);

        // Agregar datos iniciales vacíos
        ArrayList<PieEntry> entriesIniciales = new ArrayList<>();
        entriesIniciales.add(new PieEntry(1f, "Sin gastos"));

        PieDataSet dataSetInicial = new PieDataSet(entriesIniciales, "");
        dataSetInicial.setColor(Color.LTGRAY);
        dataSetInicial.setValueTextSize(0f);

        PieData pieDataInicial = new PieData(dataSetInicial);
        pieChartGastos.setData(pieDataInicial);
        pieChartGastos.invalidate();
    }

    private void actualizarGraficoIngresos() {
        entries.clear();
        ArrayList<PieEntry> entriesIngresos = new ArrayList<>();

        // Agregar entradas solo si hay valores mayores que 0
        for (Map.Entry<String, Double> entry : ingresosPorCategoria.entrySet()) {
            if (entry.getValue() > 0) {
                entriesIngresos.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            }
        }

        PieDataSet dataSetIngresos = new PieDataSet(entriesIngresos, "\n Categorías de Ingresos");
        dataSetIngresos.setColors(COLORES_CATEGORIAS);
        dataSetIngresos.setValueTextSize(12f);
        dataSetIngresos.setValueTextColor(Color.BLACK);

        PieData pieDataIngresos = new PieData(dataSetIngresos);
        pieChartIngresos.setData(pieDataIngresos);
        pieChartIngresos.invalidate();
    }

    private void actualizarGraficoGastos() {
        entries.clear();
        ArrayList<PieEntry> entriesGastos = new ArrayList<>();

        // Agregar entradas solo si hay valores mayores que 0
        for (Map.Entry<String, Double> entry : gastosPorCategoria.entrySet()) {
            if (entry.getValue() > 0) {
                entriesGastos.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            }
        }

        PieDataSet dataSetGastos = new PieDataSet(entriesGastos, "\n Categorías de Gastos");
        dataSetGastos.setColors(COLORES_CATEGORIAS);
        dataSetGastos.setValueTextSize(12f);
        dataSetGastos.setValueTextColor(Color.BLACK);

        PieData pieDataGastos = new PieData(dataSetGastos);
        pieChartGastos.setData(pieDataGastos);
        pieChartGastos.invalidate();
    }

    // En el método actualizarGrafico()
    private void actualizarGrafico() {
        ArrayList<PieEntry> entriesGrafico = new ArrayList<>();

        // Añadir las nuevas entradas con los datos de ingresos y gastos solo si hay valores mayores que 0
        if (totalIngresos > 0 || totalGastos > 0) {
            if (totalIngresos > 0) {
                entriesGrafico.add(new PieEntry((float) totalIngresos, "Ingresos"));
            }
            if (totalGastos > 0) {
                entriesGrafico.add(new PieEntry((float) totalGastos, "Gastos"));
            }

            PieDataSet dataSetGrafico = new PieDataSet(entriesGrafico, "\n Distribución de Ingresos y Gastos");
            dataSetGrafico.setColors(ColorTemplate.COLORFUL_COLORS);
            dataSetGrafico.setValueTextSize(12f);
            dataSetGrafico.setValueTextColor(Color.BLACK); // Mostrar valores cuando hay datos

            PieData pieDataGrafico = new PieData(dataSetGrafico);
            pieChart.setData(pieDataGrafico);
        } else {
            // Si no hay datos, mostrar el estado inicial
            ArrayList<PieEntry> entriesIniciales = new ArrayList<>();
            entriesIniciales.add(new PieEntry(1f, "Sin datos"));

            PieDataSet dataSetInicial = new PieDataSet(entriesIniciales, "");
            dataSetInicial.setColor(Color.LTGRAY);
            dataSetInicial.setValueTextSize(0f);
            dataSetInicial.setValueTextColor(Color.TRANSPARENT);

            PieData pieDataInicial = new PieData(dataSetInicial);
            pieChart.setData(pieDataInicial);
        }

        pieChart.invalidate();
    }


    // Modificar el método para borrar historial para incluir el reseteo del nuevo gráfico
    private void borrarHistorial() {
        historial.setLength(0);
        totalIngresos = 0.0;
        totalGastos = 0.0;
        ingresosPorCategoria.clear();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        actualizarGrafico();
        actualizarGraficoIngresos();
        actualizarGraficoGastos();
    }

    // Método para guardar el historial en SharedPreferences
    private void guardarHistorial() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(HISTORIAL_KEY, historial.toString());
        editor.putString(HISTORIAL_KEY2, historial2.toString());
        editor.apply();
    }

}
