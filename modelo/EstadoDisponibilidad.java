package modelo;

/** Disponibilidad de una moto para el reparto. */
public enum EstadoDisponibilidad {
    DISPONIBLE("Disponible"),
    ASIGNADA("Asignada"),
    NO_DISPONIBLE("No disponible");

    private final String etiqueta;

    EstadoDisponibilidad(String etiqueta) { this.etiqueta = etiqueta; }

    @Override
    public String toString() { return etiqueta; }
}
