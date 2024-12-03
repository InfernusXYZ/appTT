package com.example.apptt;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DeudaHistorial extends AppCompatActivity {

    private RecyclerView recyclerViewHistorialD;
    private DeudaAdapter deudaAdapter;
    private List<Deuda> deudaList;
    private Button btnReg;

    private DatabaseReference mDatabase,mdeudas,mpagos;
    private FirebaseAuth mAuth;
    private TextView tvdeuda1,tvdeuda2,tvdeuda3,tvdeuda4,tvdeuda5,tvtotal;
    private double totalPagos = 0.0;
    private double totalDeudas = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_deuda_historial);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Deudas").child(mAuth.getCurrentUser().getUid());

        tvtotal = findViewById(R.id.tvdeudatotal);
        tvdeuda1 = findViewById(R.id.tvdeuda1);
        tvdeuda2 = findViewById(R.id.tvdeuda2);
        tvdeuda3 = findViewById(R.id.tvdeuda3);
        tvdeuda4 = findViewById(R.id.tvdeuda4);
        tvdeuda5 = findViewById(R.id.tvdeuda5);
        recyclerViewHistorialD = findViewById(R.id.HistorialDeuda);
        recyclerViewHistorialD.setLayoutManager(new LinearLayoutManager(this));
        deudaList = new ArrayList<>();
        deudaAdapter = new DeudaAdapter(deudaList);
        recyclerViewHistorialD.setAdapter(deudaAdapter);

        cargarDatosFirebase();
        cargarHistorial();
        cargarAhorrosdesdeFirebase();

        btnReg = findViewById(R.id.btnregresarD);
        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeudaHistorial.this, EndeudamientoActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void cargarAhorrosdesdeFirebase(){
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                deudaList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Deuda deuda = snapshot.getValue(Deuda.class);
                    if (deuda != null) {
                        Log.d("HistorialDeuda", "Deuda:" + deuda.getTipoDeuda() +", "+ deuda.getIngresoMensual()+", "+deuda.getPagoMensual()+", "+deuda.getRelaciondeendeudamiento());
                        deudaList.add(deuda);
                    }else{
                        Log.d("HistorialDeuda","Ingreso nulo o error de mapeo");
                    }
                }
                deudaAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DeudaHistorial.this,"Error al cargar los datos",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarHistorial(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();

        // Referencia a la base de datos de "Debes"
        DatabaseReference debesRef = FirebaseDatabase.getInstance()
                .getReference("Debes")
                .child(userId);

        // Leer los conceptos de "Debes"
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

                    // Limitar a los 5 primeros conceptos
                    if (conceptos.size() == 5) break;
                }

                if (conceptos.isEmpty()) {
                    Toast.makeText(DeudaHistorial.this, "No tienes conceptos en 'Debes'.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Ahora obtenemos los pagos de "Deudas" para calcular el monto restante
                DatabaseReference deudasRef = FirebaseDatabase.getInstance()
                        .getReference("Deudas")
                        .child(userId);

                deudasRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot deudasSnapshot) {
                        // Calcular el monto restante para cada concepto
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

                            // Monto restante = monto original - suma de pagos
                            montosRestantes.add(montoOriginal - sumaPagos);
                        }

                        // Asignar los valores a los TextView correspondientes
                        actualizarTextViews(conceptos, montosRestantes);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(DeudaHistorial.this, "Error al cargar deudas.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DeudaHistorial.this, "Error al cargar conceptos de 'Debes'.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarTextViews(List<String> conceptos, List<Double> montosRestantes) {
        // TextViews para mostrar los conceptos
        TextView[] Deudas = {tvdeuda1, tvdeuda2, tvdeuda3, tvdeuda4, tvdeuda5};

        for (int i = 0; i < Deudas.length; i++) {
            if (i < conceptos.size()) {
                Deudas[i].setText(conceptos.get(i)+"\n"+String.format("$%.2f",montosRestantes.get(i)));
            } else {
                Deudas[i].setText(""); // Dejar vacío si no hay suficientes conceptos
            }
        }
    }

    private void cargarDatosFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();

        // Referencias a las tablas en Firebase
        mdeudas = FirebaseDatabase.getInstance().getReference("Debes").child(userId);
        mpagos = FirebaseDatabase.getInstance().getReference("Deudas").child(userId);

        // Escuchar la tabla de Deudas
        mdeudas.addValueEventListener(new ValueEventListener() {
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
        mpagos.addValueEventListener(new ValueEventListener() {
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
    private void calcularDeudaTotal() {
        double deudaRestante = totalDeudas - totalPagos;

        // Actualizar el TextView con la deuda restante
        tvtotal.setText(String.format("Deuda Total Restante: $%.2f", deudaRestante));
    }
}