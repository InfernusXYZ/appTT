package com.example.apptt;

public class Ingreso {
    private String Categoria;
    private String Tipo;
    private double Monto;

    public  Ingreso(){}

    public Ingreso(String Categoria,String Tipo, Double Monto){
        this.Categoria=Categoria;
        this.Tipo=Tipo;
        this.Monto=Monto;
    }

    public String getCategoria(){return Categoria;}
    public String getTipo(){return Tipo;}
    public double getMonto(){return Monto;}

}
