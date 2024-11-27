package com.example.apptt;

public class Deuda {
    private String Fecha;
    private double IngresoMensual;
    private double PagoMensual;
    private double Relaciondeendeudamiento;
    private String TipoDeuda;

    public Deuda(){

    }

    public Deuda(String Fecha,double IngresoMensual, double PagoMensual, double Relaciondeendeudamiento, String TipoDeuda){
        this.Fecha = Fecha;
        this.IngresoMensual = IngresoMensual;
        this.PagoMensual = PagoMensual;
        this.Relaciondeendeudamiento = Relaciondeendeudamiento;
        this.TipoDeuda = TipoDeuda;

    }

    public double getIngresoMensual(){
        return IngresoMensual;
    }

    public double getPagoMensual(){
        return PagoMensual;
    }

    public double getRelaciondeendeudamiento() {
        return Relaciondeendeudamiento;
    }

    public String getTipoDeuda() {
        return TipoDeuda;
    }
    public String getFecha() {return Fecha;}
}
