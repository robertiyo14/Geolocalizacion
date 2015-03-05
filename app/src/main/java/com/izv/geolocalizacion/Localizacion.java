package com.izv.geolocalizacion;

import android.location.Location;

import java.util.Date;

/**
 * Created by rober on 06/02/2015.
 */
public class Localizacion {

    private String fecha;
    private Location localizacion;
    private String localidad,calle;

    public Localizacion() {
    }

    public Localizacion(String fecha, Location localizacion) {
        this.fecha = fecha;
        this.localizacion = localizacion;
    }

    public Localizacion(String fecha, Location localizacion, String localidad, String calle) {
        this.fecha = fecha;
        this.localizacion = localizacion;
        this.localidad = localidad;
        this.calle = calle;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public Location getLocalizacion() {
        return localizacion;
    }

    public void setLocalizacion(Location localizacion) {
        this.localizacion = localizacion;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    @Override
    public String toString() {
        return "Localizacion{" +
                "fecha='" + fecha + '\'' +
                ", localidad='" + localidad + '\'' +
                '}';
    }
}
