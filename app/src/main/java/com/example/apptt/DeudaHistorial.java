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

public class DeudaHistorial extends AppCompatActivity {

    private RecyclerView recyclerViewHistorialD;
    private DeudaAdapter deudaAdapter;
    private List<Deuda> deudaList;
    private Button btnReg;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_deuda_historial);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Deudas").child(mAuth.getCurrentUser().getUid());

        recyclerViewHistorialD = findViewById(R.id.HistorialDeuda);
        recyclerViewHistorialD.setLayoutManager(new LinearLayoutManager(this));
        deudaList = new ArrayList<>();
        deudaAdapter = new DeudaAdapter(deudaList);
        recyclerViewHistorialD.setAdapter(deudaAdapter);

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
}