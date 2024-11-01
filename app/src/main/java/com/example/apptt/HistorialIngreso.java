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

public class HistorialIngreso extends AppCompatActivity {

    private RecyclerView recyclerViewHistorial;
    private IngresoAdapter ingresoAdapter;
    private List<Ingreso> ingresoList;
    private Button btnReg;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_historial_ingreso);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Ingresos").child(mAuth.getCurrentUser().getUid());

        recyclerViewHistorial = findViewById(R.id.HistorialIng);
        recyclerViewHistorial.setLayoutManager(new LinearLayoutManager(this));
        ingresoList = new ArrayList<>();
        ingresoAdapter = new IngresoAdapter(ingresoList);
        recyclerViewHistorial.setAdapter(ingresoAdapter);

        cargarIngresosdesdeFirebase();

        btnReg = findViewById(R.id.btnhistorialR);
        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HistorialIngreso.this, BalanceActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void cargarIngresosdesdeFirebase(){
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ingresoList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Ingreso ingreso = snapshot.getValue(Ingreso.class);
                    if (ingreso != null) {
                        Log.d("HistorialIngresos", "Ingreso:" + ingreso.getCategoria() +", "+ ingreso.getTipo()+", "+ingreso.getMonto());
                        ingresoList.add(ingreso);
                    }else{
                        Log.d("HistorialIngresos","Ingreso nulo o error de mapeo");
                    }
                }
                ingresoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HistorialIngreso.this,"Error al cargar los datos",Toast.LENGTH_SHORT).show();
            }
        });
    }

}