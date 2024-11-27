package com.example.apptt;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HistorialGasto extends AppCompatActivity {
    private RecyclerView recyclerViewHistorial, recyclerViewHistorialV;
    private GastoAdapter gastoAdapter,gastoAdapterV;
    private List<Gasto> gastoList,gastoListVariable;
    private Button btnReg;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_historial_gasto);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Gastos").child(mAuth.getCurrentUser().getUid());

        recyclerViewHistorial = findViewById(R.id.HistorialGas);
        recyclerViewHistorial.setLayoutManager(new LinearLayoutManager(this));
        gastoList = new ArrayList<>();
        gastoAdapter = new GastoAdapter(gastoList);
        recyclerViewHistorial.setAdapter(gastoAdapter);

        recyclerViewHistorialV = findViewById(R.id.HistorialGasV);
        recyclerViewHistorialV.setLayoutManager(new LinearLayoutManager(this));
        gastoListVariable = new ArrayList<>();
        gastoAdapterV = new GastoAdapter(gastoListVariable);
        recyclerViewHistorialV.setAdapter(gastoAdapterV);

        cargarIngresosdesdeFirebase();

        btnReg = findViewById(R.id.btnhistorialR);
        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HistorialGasto.this, BalanceActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void cargarIngresosdesdeFirebase(){
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                gastoList.clear();
                gastoListVariable.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Gasto gasto = snapshot.getValue(Gasto.class);
                    if (gasto != null) {
                        Log.d("HistorialGastos", "Gasto:" + gasto.getCategoria() +", "+ gasto.getTipo()+", "+gasto.getMonto());
                        if ("Fijo".equalsIgnoreCase(gasto.getTipo())) {
                            gastoList.add(gasto);
                        }
                        // Agregar a la lista "Variable" si cumple la condición
                        if ("Variable".equalsIgnoreCase(gasto.getTipo())) {
                            gastoListVariable.add(gasto);
                        }
                    }else{
                        Log.d("HistorialGastos","Ingreso nulo o error de mapeo");
                    }
                }
                gastoAdapter.notifyDataSetChanged();
                gastoAdapterV.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HistorialGasto.this,"Error al cargar los datos",Toast.LENGTH_SHORT).show();
            }
        });
    }
}