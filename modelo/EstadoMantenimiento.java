package modelo;

/**
 * Estado de mantenimiento. El orden de las constantes es el orden de
 * criticidad (de menos a más crítico), por eso se puede usar compareTo().
 */
public enum EstadoMantenimiento {
    SIN_HISTORIAL("Sin historial", "Sin historial"),
    AL_DIA("Al día", "AL DÍA"),
    PROXIMO("Próximo a vencer", "PRÓXIMA A VENCER"),
    VENCIDO("Vencido", "VENCIDA");

    private final String etiqueta;   // para un servicio: "Vencido"
    private final String mensaje;    // para la moto: "VENCIDA"

    EstadoMantenimiento(String etiqueta, String mensaje) {
        this.etiqueta = etiqueta;
        this.mensaje = mensaje;
    }

    public String getEtiqueta() { return etiqueta; }
    public String getMensaje() { return mensaje; }
}
