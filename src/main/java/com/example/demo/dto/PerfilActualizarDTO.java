package com.example.demo.dto;

public class PerfilActualizarDTO {
    
    private String nombre;
    private String contraseniaActual;
    private String nuevaContrasenia;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getContraseniaActual() {
        return contraseniaActual;
    }

    public void setContraseniaActual(String contraseniaActual) {
        this.contraseniaActual = contraseniaActual;
    }

    public String getNuevaContrasenia() {
        return nuevaContrasenia;
    }

    public void setNuevaContrasenia(String nuevaContrasenia) {
        this.nuevaContrasenia = nuevaContrasenia;
    }
}
