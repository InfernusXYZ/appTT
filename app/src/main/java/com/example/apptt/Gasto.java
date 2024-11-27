package com.example.apptt;

public class Gasto {
    private String Fecha;
    private String Categoria;
    private String Tipo;
    private double Monto;

    public  Gasto(){}

    public Gasto(String Fecha,String Categoria,String Tipo, Double Monto){
        this.Fecha=Fecha;
        this.Categoria=Categoria;
        this.Tipo=Tipo;
        this.Monto=Monto;
    }

    public String getCategoria(){return Categoria;}
    public String getTipo(){return Tipo;}
    public double getMonto(){return Monto;}
    public String getFecha() {return Fecha;}
}
