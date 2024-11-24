package com.example.apptt;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.NonNull;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AhorroActivity extends AppCompatActivity {

    private EditText etIngresosMensuales, etAhorroMensual, etmetaahorro;
    private TextView tvHistorialAhorro, tvHistorialahorromen, tvHistorialporcentge, tvprogreso, tvmeta, tvahorroAct, tvprogresotexto;
    private PieChart pieChart;
    private BarChart barChart;
    private List<String> historialList;
    private List<String> historialahorro;
    private List<String> historialporcentaje;
    private double metaAhorro = 0.0;
    private boolean alertaMostrada = false;
    private double progresoAhorro = 0.0;
    private DatabaseReference metaRef, ahorroRef;
    private Button btnmeta, btnreinicio;
    private ProgressBar progressBarahorro;
    private ValueEventListener metaListener;
    private ValueEventListener ahorrosListener;
    private boolean isListenerInitialized = false;
    private String ingresosStr, ahorroStr;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

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

        progressBarahorro = findViewById(R.id.progressbarraahorro);
        etIngresosMensuales = findViewById(R.id.et_ingresos_mensuales);
        etAhorroMensual = findViewById(R.id.et_ahorro_mensual);
        etmetaahorro = findViewById(R.id.et_meta);
        /*tvHistorialAhorro = findViewById(R.id.tv_historial_ahorro);
        tvHistorialahorromen = findViewById(R.id.tv_historial_ahorro_men);
        tvHistorialporcentge = findViewById(R.id.tv_historial_porcentage);*/
        tvprogresotexto = findViewById(R.id.tvProgresoTexto);
        tvahorroAct = findViewById(R.id.tvProgresoPorcentaje);
        tvmeta = findViewById(R.id.tvMeta);
        tvprogreso = findViewById(R.id.tvprogresometa);
        pieChart = findViewById(R.id.pieChart);
        //barChart = findViewById(R.id.barChart);
        Button btnCalcular = findViewById(R.id.btn_calcular);
        Button btnBorrarHistorial = findViewById(R.id.btn_borrar_historial);
        Button btnEnciclopedia = findViewById(R.id.btn_enciclopedia);
        Button btnBalance = findViewById(R.id.btn_balance);
        Button btnAhorros = findViewById(R.id.btn_ahorros);
        Button btnEndeudamiento = findViewById(R.id.btn_endeudamiento);
        Button btninsert = findViewById(R.id.btn_inAhorro);
        btnmeta = findViewById(R.id.btn_meta);
        btnreinicio = findViewById(R.id.btn_reiniciometa);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();


        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        metaRef = FirebaseDatabase.getInstance().getReference("Metas").child(userId);
        ahorroRef = FirebaseDatabase.getInstance().getReference("Ahorros").child(userId);
        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("AhorroPrefs", Context.MODE_PRIVATE);

        historialList = new ArrayList<>();
        historialahorro= new ArrayList<>();
        historialporcentaje = new ArrayList<>();
        // Cargar historial desde SharedPreferences
        loadHistorial();
        if (!isListenerInitialized) {
            cargarMetaAhorro();
            isListenerInitialized = true;
        }
        // Inicializar las gráficas en blanco
        initializeCharts();
        cargarTotalesFirebaseYActualizarGraficas();

        btninsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AhorroActivity.this,InsertAhorro.class);
                startActivity(intent);
                finish();
            }
        });

        btnmeta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarmetaahorro();
            }
        });

        btnreinicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmarReinicioMeta();
            }
        });

        btnCalcular.setOnClickListener(v -> {
            guardarahorro();
            /*String ingresosStr = etIngresosMensuales.getText().toString();
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
                    //historialList.add(ingmen);
                    //historialahorro.add(ahomen);
                    //historialporcentaje.add(peraho);
                    //actualizarHistorial();

                    // Guardar historial en SharedPreferences
                    //saveHistorial();

                    //Toast.makeText(AhorroActivity.this, "Tasa de ahorro: " + porcentajeAhorro + "%", Toast.LENGTH_LONG).show();

                    //actualizarGraficas(ingresosMensuales, ahorroMensual);

                    // Vaciar los campos llenables
                    //etIngresosMensuales.setText("");
                    //etAhorroMensual.setText("");
                //} else {
                //    Toast.makeText(AhorroActivity.this, "El ingreso mensual debe ser mayor a 0.", Toast.LENGTH_SHORT).show();
                //}
            //} else {
              //  Toast.makeText(AhorroActivity.this, "Por favor, complete ambos campos antes de calcular.", Toast.LENGTH_SHORT).show();
            //}
        });

        btnBorrarHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(AhorroActivity.this,HistorialAhorro.class);
            startActivity(intent);
            /*historialList.clear();
            historialahorro.clear();
            historialporcentaje.clear();
            actualizarHistorial();
            initializeCharts(); // Reiniciar gráficas a estado en blanco
            clearHistorial(); // Limpiar historial en SharedPreferences
            Toast.makeText(AhorroActivity.this, "Historial y gráficas borradas", Toast.LENGTH_SHORT).show();
        */
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

    private void cargarTotalesFirebaseYActualizarGraficas() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DatabaseReference referenciaAhorros = FirebaseDatabase.getInstance().getReference("Ahorros").child(user.getUid());

        referenciaAhorros.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double totalIngresos = 0;
                double totalAhorro = 0;

                for (DataSnapshot mesSnapshot : snapshot.getChildren()) {
                    Double ingresoMensual = mesSnapshot.child("IngresoMensual").getValue(Double.class);
                    Double ahorroMensual = mesSnapshot.child("AhorroMensual").getValue(Double.class);

                    if (ingresoMensual != null) totalIngresos += ingresoMensual;
                    if (ahorroMensual != null) totalAhorro += ahorroMensual;
                }

                actualizarGraficas(totalIngresos, totalAhorro);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Error al cargar los datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarGraficas(double totalIngresos, double totalAhorro) {
        List<PieEntry> pieEntries = new ArrayList<>();
        //List<BarEntry> barEntries = new ArrayList<>();

        if (totalIngresos > 0 || totalAhorro > 0) {
            pieEntries.add(new PieEntry((float) totalIngresos, "Ingresos"));
            pieEntries.add(new PieEntry((float) totalAhorro, "Ahorro"));
            /*barEntries.add(new BarEntry(1, (float) totalIngresos));
            barEntries.add(new BarEntry(2, (float) totalAhorro));*/
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Distribución Total de Ingresos y Ahorro");
        pieDataSet.setColors(new int[]{getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.colorAccent)});
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate(); // refrescar la gráfica

        /*BarDataSet barDataSet = new BarDataSet(barEntries, "Total Ingresos y Ahorro");
        barDataSet.setColors(new int[]{getResources().getColor(R.color.colorPrimaryDark), getResources().getColor(R.color.colorAccentLight)});
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.invalidate(); // refrescar la gráfica*/
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
        /*ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(1, 0)); // Entrada inicial en blanco
        BarDataSet barDataSet = new BarDataSet(barEntries, ""); // Crear conjunto de datos
        barDataSet.setColors(new int[]{R.color.gray}, this); // Establecer color
        BarData barData = new BarData(barDataSet); // Crear datos para la gráfica de barras
        barChart.setData(barData); // Asignar datos a la gráfica de barras
        barChart.invalidate(); // Actualizar gráfica*/
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

    @Override
    protected void onStart(){
        super.onStart();
        cargarMetaAhorro();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (metaListener != null){
            metaRef.removeEventListener(metaListener);
        }
        if (ahorrosListener != null){
            ahorroRef.removeEventListener(ahorrosListener);
        }
        isListenerInitialized = false;
    }

    private void cargarMetaAhorro(){
      metaListener = metaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    metaAhorro = snapshot.getValue(Double.class);
                    if (metaAhorro > 0){
                        desactivarEdicionMeta();
                    }
                       actualizarprogreso();
                    }
                }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AhorroActivity.this,"Error al cargar meta",Toast.LENGTH_SHORT).show();
            }
        });

       ahorrosListener = ahorroRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progresoAhorro = 0.0;
                for (DataSnapshot ahorroSnapshot: snapshot.getChildren()){
                    Ahorro ahorro = ahorroSnapshot.getValue(Ahorro.class);
                    if (ahorro != null){
                        progresoAhorro += ahorro.getAhorroMensual();
                    }
                }
                actualizarprogreso();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AhorroActivity.this,"Error al cargar ahorros",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarmetaahorro(){
        String metainput =  etmetaahorro.getText().toString().trim();
        if (!metainput.isEmpty()){
            alertaMostrada = false;
            actualizarprogreso();
            metaAhorro = Double.parseDouble(metainput);
            metaRef.setValue(metaAhorro);
            etmetaahorro.setText("");
            desactivarEdicionMeta();
            Toast.makeText(this,"Meta de ahorro guardada",Toast.LENGTH_SHORT).show();
            actualizarprogreso();
        }else{
            Toast.makeText(this,"Por favor ingresar una meta valida",Toast.LENGTH_SHORT).show();
        }

    }

    private void desactivarEdicionMeta(){
        etmetaahorro.setEnabled(false);
        btnmeta.setEnabled(false);

    }

    private void activarEdicionMeta(){
        etmetaahorro.setEnabled(true);
        btnmeta.setEnabled(true);

    }

    private void actualizarprogreso(){
        int progreso = metaAhorro > 0?(int)((progresoAhorro/metaAhorro)*100):0;
        progressBarahorro.setProgress(progreso);

        ObjectAnimator progressanimator = ObjectAnimator.ofInt(progressBarahorro, "progress",0,progreso);
        progressanimator.setDuration(500);
        progressanimator.setInterpolator(new DecelerateInterpolator());
        progressanimator.start();

        tvahorroAct.setText(progreso + "%");
        tvmeta.setText("Meta: $" + metaAhorro);
        tvprogreso.setText("Ahorro Actual: $"+ progresoAhorro);

        if (progreso>=100 && !alertaMostrada ){
            alertaMostrada=true;
            new Handler(Looper.getMainLooper()).postDelayed(()->{
                if(!isFinishing()){
                    mostrarmensajeexito();
                }
            },300);

        }
    }

    private void mostrarmensajeexito(){
        new AlertDialog.Builder(this)
                .setTitle("Meta Alcanzada")
                .setMessage("Favor de darle al boton borrar meta y ahorros, para comenzar, con una nueva")
                .setPositiveButton("Ok",(dialog, which) -> {
                    alertaMostrada = false;
                })
                .setCancelable(false)
                .show();
    }

    private void confirmarReinicioMeta(){
        new AlertDialog.Builder(this)
                .setTitle("Borrado de meta y ahorros actuales")
                .setMessage("¿Estas seguro de que deseas reiniciar tu meta de ahorro y registro de ahorros previos")
                .setPositiveButton("Si",(dialog, which) -> reiniciarMeta())
                .setNegativeButton("Cancelar",null)
                .show();

    }

    private void reiniciarMeta(){
        ahorroRef.removeValue();
        metaRef.setValue(0.0);
        progresoAhorro = 0.0;
        alertaMostrada = false;
        metaAhorro = 0.0;
        activarEdicionMeta();
        actualizarprogreso();
        Toast.makeText(this,"Listo para una nueva meta de ahorro",Toast.LENGTH_SHORT).show();
    }

    private void clearHistorial() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("historial");// Limpiar historial
        editor.remove("historial2");
        editor.remove("historial3");
        editor.apply();
    }

    private void guardarahorro(){
        ingresosStr = etIngresosMensuales.getText().toString();
        ahorroStr = etAhorroMensual.getText().toString();


        if(TextUtils.isEmpty(ingresosStr)||TextUtils.isEmpty(ahorroStr)){
            Toast.makeText(this,"Llene los campos correspondientes",Toast.LENGTH_SHORT).show();
            return;
        }

        double Ingresomensual = Double.parseDouble(ingresosStr);
        double Ahorromensual = Double.parseDouble(ahorroStr);
        double porcentajeAhorro = (Ahorromensual / Ingresomensual) * 100;
        if (Ingresomensual <= Ahorromensual){
            Toast.makeText(this,"No puede ahorra mas de lo que gana", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar el AlertDialog basado en el porcentaje de ahorro
        mostrarAlertaPorcentajeAhorro(porcentajeAhorro);

        String userID = mAuth.getCurrentUser().getUid();

        Map<String,Object> AhorroMap = new HashMap<>();
        AhorroMap.put("IngresoMensual",Ingresomensual);
        AhorroMap.put("AhorroMensual",Ahorromensual);
        AhorroMap.put("TasaAhorro",porcentajeAhorro);

        etIngresosMensuales.setText("");
        etAhorroMensual.setText("");

        mDatabase.child("Ahorros").child(userID).push().setValue(AhorroMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(AhorroActivity.this,"Datos guardados correctamente",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(AhorroActivity.this,"Error al guardar los datos",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void mostrarAlertaPorcentajeAhorro(double porcentajeAhorro) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AhorroActivity.this);
        builder.setTitle("Tasa de Ahorro");

        // Verificar el rango del porcentaje y mostrar el mensaje adecuado
        if (porcentajeAhorro >= 40) {
            builder.setMessage("¡Felicidades! Estás ahorrando más del 40% de tus ingresos. ¡Sigue así!");
        } else if (porcentajeAhorro >= 20 && porcentajeAhorro < 30) {
            builder.setMessage("Tu tasa de ahorro es entre el 20% y el 30%. Intenta aumentar un poco más para mejorar tu estabilidad financiera.");
        } else if (porcentajeAhorro >= 30 && porcentajeAhorro < 40) {
            builder.setMessage("Tu tasa de ahorro es buena. Si continúas con este ritmo, lograrás tus metas de ahorro más rápido.");
        } else {
            builder.setMessage("Tu tasa de ahorro es baja. Intenta ahorrar un poco más cada mes. Revisa tus gastos y ve qué áreas puedes ajustar.");
        }

        builder.setPositiveButton("Ok", null);
        builder.show();
    }
}
