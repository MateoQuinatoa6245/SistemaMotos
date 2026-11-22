package modelo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Moto a combustión: todos los servicios aplican (aceite, cadena, frenos y llantas). */
public class Moto extends Vehiculo {

    public Moto(String placa, String marca, String modelo, int anio, int kilometrajeActual) {
        super(placa, marca, modelo, anio, kilometrajeActual);
    }

    @Override
    public String getTipo() {
        return "Moto";
    }

    @Override
    public List<TipoServicio> getServiciosAplicables() {
        return Collections.unmodifiableList(Arrays.asList(TipoServicio.values()));
    }
}
