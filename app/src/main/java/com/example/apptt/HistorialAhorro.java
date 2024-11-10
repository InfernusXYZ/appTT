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

public class HistorialAhorro extends AppCompatActivity {
    private RecyclerView recyclerViewHistorialA;
    private AhorroAdapter ahorroAdapter;
    private List<Ahorro> ahorroList;
    private Button btnReg;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_historial_ahorro);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Ahorros").child(mAuth.getCurrentUser().getUid());

        recyclerViewHistorialA = findViewById(R.id.HistorialAho);
        recyclerViewHistorialA.setLayoutManager(new LinearLayoutManager(this));
        ahorroList = new ArrayList<>();
        ahorroAdapter = new AhorroAdapter(ahorroList);
        recyclerViewHistorialA.setAdapter(ahorroAdapter);

        cargarAhorrosdesdeFirebase();

        btnReg = findViewById(R.id.btnregresarA);
        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HistorialAhorro.this, AhorroActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void cargarAhorrosdesdeFirebase(){
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ahorroList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Ahorro ahorro = snapshot.getValue(Ahorro.class);
                    if (ahorro != null) {
                        Log.d("HistorialIngresos", "Ingreso:" + ahorro.getIngresoMensual() +", "+ ahorro.getAhorroMensual()+", "+ahorro.getTasaAhorro());
                        ahorroList.add(ahorro);
                    }else{
                        Log.d("HistorialIngresos","Ingreso nulo o error de mapeo");
                    }
                }
                ahorroAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HistorialAhorro.this,"Error al cargar los datos",Toast.LENGTH_SHORT).show();
            }
        });
    }
}