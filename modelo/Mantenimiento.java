package modelo;

import java.time.LocalDate;
import java.util.Locale;

/** Registro de un servicio realizado a una moto. Es inmutable. */
public class Mantenimiento {

    private final TipoServicio tipo;
    private final LocalDate fecha;
    private final int kilometraje;
    private final double costo;
    private final String observaciones;

    public Mantenimiento(TipoServicio tipo, LocalDate fecha, int kilometraje,
                         double costo, String observaciones) {
        this.tipo = tipo;
        this.fecha = fecha;
        this.kilometraje = kilometraje;
        this.costo = costo;
        this.observaciones = observaciones == null ? "" : observaciones;
    }

    /** Km en el que toca el próximo servicio de este tipo (km actual + intervalo). */
    public int getProximoKm() {
        return kilometraje + tipo.getIntervaloKm();
    }

    public TipoServicio getTipo() { return tipo; }
    public LocalDate getFecha() { return fecha; }
    public int getKilometraje() { return kilometraje; }
    public double getCosto() { return costo; }
    public String getObservaciones() { return observaciones; }

    @Override
    public String toString() {
        String texto = fecha + " | " + tipo + " | " + kilometraje + " km | USD "
                + String.format(Locale.US, "%.2f", costo);
        return observaciones.isEmpty() ? texto : texto + " | " + observaciones;
    }
}
