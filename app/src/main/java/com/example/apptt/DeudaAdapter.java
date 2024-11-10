package com.example.apptt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DeudaAdapter extends RecyclerView.Adapter<DeudaAdapter.DeudaViewHolder> {
    private List<Deuda> deudaList;
    public DeudaAdapter(List<Deuda>deudaList){this.deudaList=deudaList;}

    @NonNull
    @Override
    public DeudaAdapter.DeudaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_deuda,parent,false);
        return new DeudaAdapter.DeudaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeudaViewHolder holder, int position){
        Deuda deuda = deudaList.get(position);
        holder.tvIngresomensual.setText("$"+String.valueOf(deuda.getIngresoMensual()));
        holder.tvPagomensual.setText("$"+String.valueOf(deuda.getPagoMensual()));
        holder.tvTasaenedeudamiento.setText(String.valueOf(deuda.getRelaciondeendeudamiento())+"%");
        holder.tvTipo.setText(deuda.getTipoDeuda());
    }

    @Override
    public int getItemCount(){
        return deudaList.size();
    }

    public static class DeudaViewHolder extends RecyclerView.ViewHolder{
        TextView tvIngresomensual, tvPagomensual, tvTasaenedeudamiento, tvTipo;

        public DeudaViewHolder(@NonNull View itemView){
            super(itemView);
            tvIngresomensual = itemView.findViewById(R.id.tvIngresos);
            tvPagomensual = itemView.findViewById(R.id.tvPagos);
            tvTasaenedeudamiento = itemView.findViewById(R.id.tvendeudaminento);
            tvTipo = itemView.findViewById(R.id.tvTipo);
        }
    }
}
