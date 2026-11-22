package modelo;

/**
 * Tipos de mantenimiento y su intervalo referencial en km (según los
 * requerimientos: aceite 3.000, cadena 6.000, frenos 8.000, llantas 15.000).
 */
public enum TipoServicio {
    ACEITE("Aceite", 3000),
    CADENA("Cadena", 6000),
    FRENOS("Frenos", 8000),
    LLANTAS("Llantas", 15000);

    private final String nombre;
    private final int intervaloKm;

    TipoServicio(String nombre, int intervaloKm) {
        this.nombre = nombre;
        this.intervaloKm = intervaloKm;
    }

    public String getNombre() { return nombre; }
    public int getIntervaloKm() { return intervaloKm; }

    @Override
    public String toString() { return nombre; }
}
