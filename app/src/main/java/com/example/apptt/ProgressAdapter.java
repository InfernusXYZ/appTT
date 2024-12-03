package com.example.apptt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ProgressAdapter extends RecyclerView.Adapter<ProgressAdapter.ProgressViewHolder> {

    private List<String> conceptos;
    private List<Double> montosRestantes;
    private List<Double> montosOriginales;
    private List<String> keys;
    private Context context;

    public ProgressAdapter(Context context,List<String> conceptos, List<Double> montosRestantes, List<Double> montosOriginales, List<String> keys) {
        this.context = context;
        this.conceptos = conceptos;
        this.montosRestantes = montosRestantes;
        this.montosOriginales = montosOriginales;
        this.keys = keys;
    }

    @NonNull
    @Override
    public ProgressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_progress_bar, parent, false);
        return new ProgressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgressViewHolder holder, int position) {
        String concepto = conceptos.get(position);
        double montoRestante = montosRestantes.get(position);
        double montoOriginal = montosOriginales.get(position);

        // Calcula el progreso en porcentaje
        int progreso = (int) ((montoOriginal - montoRestante) / montoOriginal * 100);

        // Configura las vistas
        holder.tvConcepto.setText(concepto);
        holder.progressBar.setProgress(progreso);
        holder.tvMontoRestante.setText(String.format("Monto restante: $%.2f", montoRestante));

        if (progreso >=100){
            holder.progressBar.setProgress(100);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (context instanceof Activity && ! ((Activity) context).isFinishing() && !((Activity)context).isDestroyed()){
                    mostrarDialogoEliminarRegistro(keys.get(position), concepto);
                }
            },500);
        }
    }

    @Override
    public int getItemCount() {
        return conceptos.size();
    }

    static class ProgressViewHolder extends RecyclerView.ViewHolder {
        TextView tvConcepto, tvMontoRestante;
        ProgressBar progressBar;

        public ProgressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvConcepto = itemView.findViewById(R.id.tvConcepto);
            progressBar = itemView.findViewById(R.id.progressBar);
            tvMontoRestante = itemView.findViewById(R.id.tvMontoRestante);
        }
    }

    private void mostrarDialogoEliminarRegistro(String key, String concepto) {
        new AlertDialog.Builder(context)
                .setTitle("Registro completado")
                .setMessage("El progreso de \"" + concepto + "\" ha llegado al 100%. ¿Deseas eliminar este registro?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    eliminarRegistroDeFirebase(key);
                    new Handler(Looper.getMainLooper()).postDelayed(()->{
                        Intent intent = ((Activity)context).getIntent();
                        ((Activity)context).finish();
                        context.startActivity(intent);
                    },300);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void eliminarRegistroDeFirebase(String key) {
        DatabaseReference debesRef = FirebaseDatabase.getInstance()
                .getReference("Debes")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(key);

        // Eliminar el registro de "Debes"
        debesRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Obtener el concepto correspondiente al registro eliminado
                    String conceptoEliminado = conceptos.get(keys.indexOf(key)); // Asegúrate de que "conceptos" esté sincronizado con "keys"

                    // Buscar y eliminar registros relacionados en "Deudas"
                    eliminarRegistrosDeudas(conceptoEliminado);

                    Toast.makeText(context, "Registro eliminado correctamente.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Error al eliminar el registro en 'Deber'.", Toast.LENGTH_SHORT).show());
    }

    private void eliminarRegistrosDeudas(String conceptoEliminado) {
        DatabaseReference deudasRef = FirebaseDatabase.getInstance()
                .getReference("Deudas")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        deudasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot deudaSnapshot : snapshot.getChildren()) {
                    String tipoDeuda = deudaSnapshot.child("TipoDeuda").getValue(String.class);
                    if (tipoDeuda != null && tipoDeuda.equals(conceptoEliminado)) {
                        // Eliminar el registro en "Deudas"
                        deudaSnapshot.getRef().removeValue()
                                .addOnSuccessListener(aVoid -> Log.d("EliminarDeudas", "Registro de 'Deudas' eliminado: " + tipoDeuda))
                                .addOnFailureListener(e -> Log.e("EliminarDeudas", "Error al eliminar registro de 'Deudas'.", e));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error al cargar los registros de 'Deudas'.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
