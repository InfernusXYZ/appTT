package com.example.apptt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GastoAdapter extends RecyclerView.Adapter<GastoAdapter.GastoViewHolder> {
    private List<Gasto> gastoList;
    public GastoAdapter(List<Gasto>gastoList){
        this.gastoList=gastoList;
    }

    @NonNull
    @Override
    public GastoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ingreso,parent,false);
        return new GastoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GastoViewHolder holder, int position){
        Gasto gasto = gastoList.get(position);
        holder.tvCategoria.setText(gasto.getCategoria());
        holder.tvTipo.setText(gasto.getTipo());
        holder.tvMonto.setText(String.valueOf(gasto.getMonto()));
    }

    @Override
    public int getItemCount(){
        return gastoList.size();
    }

    public static class GastoViewHolder extends RecyclerView.ViewHolder{
        TextView tvCategoria, tvTipo, tvMonto;

        public GastoViewHolder(@NonNull View itemView){
            super(itemView);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvMonto = itemView.findViewById(R.id.tvMonto);
        }
    }
}