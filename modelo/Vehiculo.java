package modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Clase abstracta que representa una moto de la flota.
 *
 * ENCAPSULAMIENTO: atributos privados con getters/setters.
 * HERENCIA: es la clase padre de Moto y MotoElectrica.
 * POLIMORFISMO: cada hija decide qué servicios aplican (getServiciosAplicables).
 */
public abstract class Vehiculo {

    private final String placa;
    private final String marca;
    private final String modelo;
    private final int anio;
    private int kilometrajeActual;
    private EstadoDisponibilidad disponibilidad;
    private Repartidor repartidor;
    private final List<Mantenimiento> historial;

    public Vehiculo(String placa, String marca, String modelo, int anio, int kilometrajeActual) {
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.kilometrajeActual = kilometrajeActual;
        this.disponibilidad = EstadoDisponibilidad.DISPONIBLE;   // RF1: inicia "Disponible"
        this.historial = new ArrayList<>();
    }

    /** Nombre del tipo de vehículo (para mostrarlo en pantalla). */
    public abstract String getTipo();

    /** Servicios de mantenimiento que aplican a este tipo de vehículo. */
    public abstract List<TipoServicio> getServiciosAplicables();

    public void agregarMantenimiento(Mantenimiento mantenimiento) {
        historial.add(mantenimiento);
    }

    /** Último servicio registrado de un tipo (el de mayor km), o null si no hay. */
    public Mantenimiento getUltimoMantenimiento(TipoServicio tipo) {
        Mantenimiento ultimo = null;
        for (Mantenimiento m : historial) {
            if (m.getTipo() == tipo && (ultimo == null || m.getKilometraje() >= ultimo.getKilometraje())) {
                ultimo = m;
            }
        }
        return ultimo;
    }

    /** Resumen de una línea con los datos de la moto. */
    public String mostrarDatos() {
        String asignada = repartidor == null ? "" : " | Repartidor: " + repartidor.getNombre();
        return getTipo() + " | Placa: " + placa + " | " + marca + " " + modelo + " (" + anio + ")"
                + " | Km: " + kilometrajeActual + " | " + disponibilidad + asignada;
    }

    public String getPlaca() { return placa; }
    public String getMarca() { return marca; }
    public String getModelo() { return modelo; }
    public int getAnio() { return anio; }

    public int getKilometrajeActual() { return kilometrajeActual; }
    public void setKilometrajeActual(int kilometrajeActual) { this.kilometrajeActual = kilometrajeActual; }

    public EstadoDisponibilidad getDisponibilidad() { return disponibilidad; }
    public void setDisponibilidad(EstadoDisponibilidad disponibilidad) { this.disponibilidad = disponibilidad; }

    public Repartidor getRepartidor() { return repartidor; }
    public void setRepartidor(Repartidor repartidor) { this.repartidor = repartidor; }

    public List<Mantenimiento> getHistorial() { return Collections.unmodifiableList(historial); }
}
