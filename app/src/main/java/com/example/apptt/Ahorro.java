package com.example.apptt;

public class Ahorro {
    private String Fecha;
    private double AhorroMensual;
    private double IngresoMensual;
    private double TasaAhorro;

    public Ahorro(){

    }

    public Ahorro(String Fecha,double AhorroMensual, double IngresoMensual,double TasaAhorro){
        this.Fecha=Fecha;
        this.AhorroMensual = AhorroMensual;
        this.IngresoMensual = IngresoMensual;
        this.TasaAhorro = TasaAhorro;
    }

    public double getIngresoMensual() {
        return IngresoMensual;
    }

    public void setIngresoMensual(double IngresoMensual) {
        this.IngresoMensual = IngresoMensual;
    }

    public double getAhorroMensual() {
        return AhorroMensual;
    }

    public void setAhorroMensual(double AhorroMensual){
        this.AhorroMensual = AhorroMensual;
    }

    public double getTasaAhorro(){
        return TasaAhorro;
    }

    public void  setTasaAhorro(){
        this.TasaAhorro = TasaAhorro;
    }

    public String getFecha() {return Fecha;}
}
