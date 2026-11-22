package modelo;

/** Repartidor que puede tener una moto asignada. Atributos privados (encapsulamiento). */
public class Repartidor {

    private final String nombre;
    private final String cedula;
    private final String telefono;

    public Repartidor(String nombre, String cedula, String telefono) {
        this.nombre = nombre;
        this.cedula = cedula;
        this.telefono = telefono;
    }

    public String getNombre() { return nombre; }
    public String getCedula() { return cedula; }
    public String getTelefono() { return telefono; }

    @Override
    public String toString() {
        return nombre + " (C.I. " + cedula + ", tel. " + telefono + ")";
    }
}
