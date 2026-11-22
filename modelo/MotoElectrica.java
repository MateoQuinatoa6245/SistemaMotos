package modelo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Moto eléctrica: no usa aceite, así que ese servicio no aplica.
 * Es POLIMORFISMO: el mismo método getServiciosAplicables() devuelve
 * una lista distinta según el tipo de moto.
 */
public class MotoElectrica extends Vehiculo {

    public MotoElectrica(String placa, String marca, String modelo, int anio, int kilometrajeActual) {
        super(placa, marca, modelo, anio, kilometrajeActual);
    }

    @Override
    public String getTipo() {
        return "Moto eléctrica";
    }

    @Override
    public List<TipoServicio> getServiciosAplicables() {
        return Collections.unmodifiableList(
                Arrays.asList(TipoServicio.CADENA, TipoServicio.FRENOS, TipoServicio.LLANTAS));
    }
}
