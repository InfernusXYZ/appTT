package com.example.apptt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AhorroAdapter extends RecyclerView.Adapter<AhorroAdapter.AhorroViewHolder> {
    private List<Ahorro> ahorroList;
    public AhorroAdapter(List<Ahorro>ahorroList){this.ahorroList=ahorroList;}

        @NonNull
        @Override
        public AhorroAdapter.AhorroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ahorro,parent,false);
            return new AhorroAdapter.AhorroViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AhorroViewHolder holder, int position){
            Ahorro ahorro = ahorroList.get(position);
            holder.tvIngresomensual.setText("$"+String.valueOf(ahorro.getIngresoMensual()));
            holder.tvAhorromensual.setText("$"+String.valueOf(ahorro.getAhorroMensual()));
            holder.tvTasaAhorro.setText(String.valueOf(ahorro.getTasaAhorro())+"%");
        }

        @Override
        public int getItemCount(){
            return ahorroList.size();
        }

        public static class AhorroViewHolder extends RecyclerView.ViewHolder{
            TextView tvIngresomensual, tvAhorromensual, tvTasaAhorro;

            public AhorroViewHolder(@NonNull View itemView){
                super(itemView);
                tvIngresomensual = itemView.findViewById(R.id.tvIngresoMensual);
                tvAhorromensual = itemView.findViewById(R.id.tvAhorroMensual);
                tvTasaAhorro = itemView.findViewById(R.id.tvTasaAhorro);
            }
        }
}
