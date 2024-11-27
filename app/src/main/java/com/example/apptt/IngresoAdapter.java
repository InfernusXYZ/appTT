package com.example.apptt;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;
import androidx.annotation.NonNull;
import java.util.List;

public class IngresoAdapter extends RecyclerView.Adapter<IngresoAdapter.IngresoViewHolder> {
    private List<Ingreso> ingresoList;
    public IngresoAdapter(List<Ingreso>ingresoList){
        this.ingresoList=ingresoList;
    }

    @NonNull
    @Override
    public IngresoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ingreso,parent,false);
        return new IngresoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngresoViewHolder holder, int position){
        Ingreso ingreso = ingresoList.get(position);
        holder.tvFecha.setText(ingreso.getFecha());
        holder.tvCategoria.setText(ingreso.getCategoria());
        holder.tvMonto.setText(String.valueOf(ingreso.getMonto()));
    }

    @Override
    public int getItemCount(){
        return ingresoList.size();
    }

    public static class IngresoViewHolder extends RecyclerView.ViewHolder{
        TextView tvCategoria, tvFecha, tvMonto;

        public IngresoViewHolder(@NonNull View itemView){
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
            tvMonto = itemView.findViewById(R.id.tvMonto);
        }
    }
}
