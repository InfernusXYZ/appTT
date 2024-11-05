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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    private double totalbalance = 0.0;
    private PieChart pieChart;
    private TextView tvHistorial, tvHistorialg,tvBalanceMensual;
    private ArrayList<PieEntry> entries = new ArrayList<>();
    private HashMap<String, Double> ingresosPorCategoria = new HashMap<>();
    private HashMap<String, Double> gastosPorCategoria = new HashMap<>();

    // Nuevas variables para el gráfico de ingresos por categoría
    private PieChart pieChartIngresos;
    private PieChart pieChartGastos;
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
        tvDescripcion1.setText("Durante este módulo se podrán ingresar los datos de los ingresos y los gastos que una persona cree o piensa tener durante un periodo de tiempo. Los datos se podrán visualizar mediante gráficos, un balance mensual al final y un historial con los datos ingresados.");

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Inicializar el TextView del Historial y el Balance Mensual
        tvHistorial = findViewById(R.id.tv_historial);
        tvHistorialg = findViewById(R.id.tv_historialg);
        tvBalanceMensual = findViewById(R.id.tv_balance_mensual);

        // Cargar historial guardado si existe
        String historialGuardado = sharedPreferences.getString(HISTORIAL_KEY, "");
        historial.append(historialGuardado);
        tvHistorial.setText(historial.toString());

        // Inicializar la gráfica de pastel del balance
        pieChart = findViewById(R.id.pie_chart);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
       // actualizarGrafico();  // Inicializa el gráfico al iniciar la actividad

        //Inicializacion del nuevo gráfico de ingresos
        pieChartIngresos = findViewById(R.id.pie_chart_ingresos);
        configurarGraficoIngresos();

        //Inicialización del gráfico de gastos
        pieChartGastos = findViewById(R.id.pie_chart_gastos);
        configurarGraficoGastos();

        //configurarGraficoPrincipal(totalIngresos,totalGastos);
        // ---------- INICIA SECCIÓN DE INGRESOS ----------

        // Inicializar Spinners para Categoría y Tipo de Ingresos
        //Spinner spinnerCategoria = findViewById(R.id.spinner_categoria);
        //Spinner spinnerTipo = findViewById(R.id.spinner_tipo);

        // Configurar el adaptador para el Spinner de Categoría
        /*ArrayAdapter<CharSequence> adapterCategoria = ArrayAdapter.createFromResource(this,
                R.array.opciones_categoria_ingresos, android.R.layout.simple_spinner_item);
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterCategoria);*/

        // Configurar el adaptador para el Spinner de Tipo de Ingresos
        /*ArrayAdapter<CharSequence> adapterTipo = ArrayAdapter.createFromResource(this,
                R.array.opciones_tipo_ingresos, android.R.layout.simple_spinner_item);
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapterTipo);*/

        // Inicializar el campo de texto para ingresar el monto de Ingresos
        //EditText etMonto = findViewById(R.id.et_monto);

        // Manejar el botón para guardar el monto de Ingresos
        //Button btnGuardarMonto = findViewById(R.id.btn_guardar_monto);
        /* btnGuardarMonto.setOnClickListener(new View.OnClickListener() {
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
                            historial.append("Ingreso: \n")
                            .append("Categoría: ").append(categoria)
                            .append(", Tipo: ").append(tipo)
                            .append(", Monto: $").append(monto)
                            .append("\n");*/
                    //historial.append("Ingresos totales:\n")
                    //        .append("$").append(totalIngresos);

                    // Actualizar el balance mensual
                    //actualizarBalance(tvBalanceMensual);
                    //actualizarGrafico();
                    //actualizarGraficoIngresos();

                    // Guardar el historial en SharedPreferences
                    //guardarHistorial();

                    // Actualizar el TextView del historial
                    //tvHistorial.setText(historial.toString());

                    // Solo limpiar el campo de monto
                    //etMonto.setText("");

                    //Toast.makeText(BalanceActivity.this, "Monto de Ingresos guardado", Toast.LENGTH_SHORT).show();
                //} else {
                 //   Toast.makeText(BalanceActivity.this, "Por favor, ingrese un monto", Toast.LENGTH_SHORT).show();
                //}
            //}
        //});
        Button btreging = findViewById(R.id.btnreging);
        btreging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(BalanceActivity.this, insertingreso.class);
                startActivity(intent);
            }
        });

        Button btreggas = findViewById(R.id.btnreggas);
        btreggas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(BalanceActivity.this, InsertGasto.class);
                startActivity(intent);
            }
        });

        Button bthistorialI = findViewById(R.id.btnHi);
        bthistorialI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(BalanceActivity.this, HistorialIngreso.class);
                startActivity(intent);
            }
        });

        Button bthistorialG = findViewById(R.id.btnHG);
        bthistorialG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(BalanceActivity.this, HistorialGasto.class);
                startActivity(intent);
            }
        });

        // Inicializar Spinners para Categoría y Tipo de Gastos
        //Spinner spinnerCategoriaGastos = findViewById(R.id.spinner_categoria_gastos);
        //Spinner spinnerTipoGastos = findViewById(R.id.spinner_tipo_gastos);

        // Configurar el adaptador para el Spinner de Categoría de Gastos
        /*ArrayAdapter<CharSequence> adapterCategoriaGastos = ArrayAdapter.createFromResource(this,
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
                    //actualizarBalance(tvBalanceMensual);
                    //actualizarGrafico();
                    //actualizarGraficoGastos();

                    // Guardar el historial en SharedPreferences
                    //guardarHistorial();

                    // Actualizar el TextView del historial
                    //tvHistorialg.setText(historial2.toString());

                    // Solo limpiar el campo de monto
                    //etMontoGastos.setText("");

                    //Toast.makeText(BalanceActivity.this, "Monto de Gastos guardado", Toast.LENGTH_SHORT).show();
                //} else {
                    //Toast.makeText(BalanceActivity.this, "Por favor, ingrese un monto de Gastos", Toast.LENGTH_SHORT).show();
                //}
            //}
        //});

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
                //configurarGraficoPrincipal();
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


    private void configurarGraficoPrincipal(double totalIngresos, double totalGastos) {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText("Balance Total");
        pieChart.setCenterTextSize(16f);
        pieChart.animateY(1000);

        ArrayList<PieEntry> entries = new ArrayList<>();

        // Verificar si hay datos de ingresos y gastos
        if (totalIngresos > 0 || totalGastos > 0) {
            if (totalIngresos > 0) {
                entries.add(new PieEntry((float) totalIngresos, "Ingresos"));
            }
            if (totalGastos > 0) {
                entries.add(new PieEntry((float) totalGastos, "Gastos"));
            }
        } else {
            // Si no hay datos, mostrar un mensaje de "Sin datos"
            entries.add(new PieEntry(1f, "Sin datos"));
        }

        // Configurar el conjunto de datos
        PieDataSet dataSet = new PieDataSet(entries, "Resumen Financiero");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS); // Usa una paleta de colores predefinida
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.invalidate(); // Refrescar el gráfico
        totalbalance = totalIngresos - totalGastos;
        tvBalanceMensual.setText(String.format("$ %.2f",totalbalance));
    }

    private void configurarGraficoIngresos() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("Ingresos")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ingresosPorCategoria.clear(); // Limpiar el mapa antes de llenarlo con nuevos datos
                totalIngresos = 0.0;

                // Procesar cada ingreso en la base de datos
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Ingreso ingreso = snapshot.getValue(Ingreso.class);
                    if (ingreso != null) {
                        String categoria = ingreso.getCategoria();
                        double monto = ingreso.getMonto();

                        totalIngresos +=monto;

                        // Agregar o actualizar el monto en el mapa
                        if (ingresosPorCategoria.containsKey(categoria)) {
                            ingresosPorCategoria.put(categoria, ingresosPorCategoria.get(categoria) + monto);
                        } else {
                            ingresosPorCategoria.put(categoria, monto);
                        }
                    }
                }

                // Llamar al método para actualizar el gráfico después de cargar los datos
                actualizarGraficoIngresos();
                if (totalIngresos != 0.0 || dataSnapshot.getChildrenCount() == 0) {
                    configurarGraficoPrincipal(totalIngresos, totalGastos);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Error al cargar los datos de Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarGraficoGastos() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("Gastos")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                gastosPorCategoria.clear(); // Limpiar el mapa antes de llenarlo con nuevos datos
                totalGastos = 0.0;

                // Procesar cada ingreso en la base de datos
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Gasto gasto = snapshot.getValue(Gasto.class);
                    if (gasto != null) {
                        String categoria = gasto.getCategoria();
                        double monto = gasto.getMonto();
                        totalGastos += monto;

                        // Agregar o actualizar el monto en el mapa
                        if (gastosPorCategoria.containsKey(categoria)) {
                            gastosPorCategoria.put(categoria, gastosPorCategoria.get(categoria) + monto);
                        } else {
                            gastosPorCategoria.put(categoria, monto);
                        }
                    }
                }

                // Llamar al método para actualizar el gráfico después de cargar los datos
                actualizarGraficoGastos();
                if (totalGastos != 0.0 || dataSnapshot.getChildrenCount() == 0) {
                    configurarGraficoPrincipal(totalIngresos, totalGastos);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Error al cargar los datos de Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarGraficoIngresos() {
        ArrayList<PieEntry> entriesIngresos = new ArrayList<>();

        // Agregar entradas al gráfico solo si hay valores mayores que 0
        for (Map.Entry<String, Double> entry : ingresosPorCategoria.entrySet()) {
            if (entry.getValue() > 0) {
                entriesIngresos.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            }
        }

        // Si no hay ingresos, mostrar una entrada predeterminada
        if (entriesIngresos.isEmpty()) {
            entriesIngresos.add(new PieEntry(1f, "Sin ingresos"));
        }

        PieDataSet dataSetIngresos = new PieDataSet(entriesIngresos, "Categorías de Ingresos");
        dataSetIngresos.setColors(ColorTemplate.MATERIAL_COLORS); // Usa colores predeterminados
        dataSetIngresos.setValueTextSize(12f);
        dataSetIngresos.setValueTextColor(Color.BLACK);

        PieData pieDataIngresos = new PieData(dataSetIngresos);
        pieChartIngresos.setData(pieDataIngresos);
        pieChartIngresos.invalidate(); // Refrescar el gráfico
        tvHistorial.setText("Ingresos Totales\n\n"+String.format("$ %.2f", totalIngresos));
    }

    private void actualizarGraficoGastos() {
        ArrayList<PieEntry> entriesGastos = new ArrayList<>();

        // Agregar entradas al gráfico solo si hay valores mayores que 0
        for (Map.Entry<String, Double> entry : gastosPorCategoria.entrySet()) {
            if (entry.getValue() > 0) {
                entriesGastos.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            }
        }

        // Si no hay ingresos, mostrar una entrada predeterminada
        if (entriesGastos.isEmpty()) {
            entriesGastos.add(new PieEntry(1f, "Sin gastos"));
        }

        PieDataSet dataSetGastos = new PieDataSet(entriesGastos, "Categorías de Gastos");
        dataSetGastos.setColors(ColorTemplate.MATERIAL_COLORS); // Usa colores predeterminados
        dataSetGastos.setValueTextSize(12f);
        dataSetGastos.setValueTextColor(Color.BLACK);

        PieData pieDataGastos = new PieData(dataSetGastos);
        pieChartGastos.setData(pieDataGastos);
        pieChartGastos.invalidate(); // Refrescar el gráfico
        tvHistorialg.setText("Gastos Totales\n\n"+String.format("$ %.2f", totalGastos));
    }

    // En el método actualizarGrafico()
    private void actualizarGrafico() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText("Balance Total");
        pieChart.setCenterTextSize(16f);
        pieChart.animateY(1000);

        ArrayList<PieEntry> entries = new ArrayList<>();

        // Verificar si hay datos de ingresos y gastos
        if (totalIngresos > 0 || totalGastos > 0) {
            if (totalIngresos > 0) {
                entries.add(new PieEntry((float) totalIngresos, "Ingresos"));
            }
            if (totalGastos > 0) {
                entries.add(new PieEntry((float) totalGastos, "Gastos"));
            }
        } else {
            // Si no hay datos, mostrar un mensaje de "Sin datos"
            entries.add(new PieEntry(1f, "Sin datos"));
        }

        // Configurar el conjunto de datos
        PieDataSet dataSet = new PieDataSet(entries, "Resumen Financiero");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS); // Usa una paleta de colores predefinida
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.invalidate(); // Refrescar el gráfico
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
