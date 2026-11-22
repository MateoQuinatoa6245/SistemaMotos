package negocio;

import modelo.EstadoDisponibilidad;
import modelo.EstadoMantenimiento;
import modelo.Mantenimiento;
import modelo.Repartidor;
import modelo.TipoServicio;
import modelo.Vehiculo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Módulos 1 y 2 (RF1 a RF4): registra repartidores y motos, asigna motos,
 * registra mantenimientos y consulta el historial.
 * Las listas son privadas (encapsulamiento); después de cada cambio de
 * km o mantenimiento se pide a GestorAlertas que actualice la disponibilidad.
 */
public class GestorFlota {

    private final List<Vehiculo> vehiculos;
    private final List<Repartidor> repartidores;
    private final GestorAlertas gestorAlertas;

    public GestorFlota(GestorAlertas gestorAlertas) {
        this.vehiculos = new ArrayList<>();
        this.repartidores = new ArrayList<>();
        this.gestorAlertas = gestorAlertas;
    }

    // ---------------------------------------------------------------- RF1

    /** RF1: registra un repartidor. Rechaza cédulas repetidas. */
    public void registrarRepartidor(Repartidor repartidor) {
        if (repartidor == null) {
            throw new IllegalArgumentException("El repartidor no puede ser nulo.");
        }
        if (buscarRepartidor(repartidor.getCedula()) != null) {
            throw new IllegalArgumentException("Ya existe un repartidor con la cédula "
                    + repartidor.getCedula() + ".");
        }
        repartidores.add(repartidor);
    }

    /** RF1: registra una moto. Rechaza placas repetidas y km negativo. La moto inicia "Disponible". */
    public void registrarVehiculo(Vehiculo vehiculo) {
        if (vehiculo == null) {
            throw new IllegalArgumentException("La moto no puede ser nula.");
        }
        if (vehiculo.getKilometrajeActual() < 0) {
            throw new IllegalArgumentException("El kilometraje no puede ser negativo.");
        }
        if (buscarPorPlaca(vehiculo.getPlaca()) != null) {
            throw new IllegalArgumentException("Ya existe una moto con la placa " + vehiculo.getPlaca() + ".");
        }
        vehiculos.add(vehiculo);
    }

    // ---------------------------------------------------------------- RF2

    /**
     * RF2: asigna una moto a un repartidor. No asigna motos con mantenimiento
     * vencido (No disponible) ni motos ya asignadas.
     */
    public String asignarMoto(String cedula, String placa) {
        Repartidor repartidor = obtenerRepartidor(cedula);
        Vehiculo moto = obtenerVehiculo(placa);
        gestorAlertas.actualizarDisponibilidad(moto);   // por si el estado cambió

        if (moto.getDisponibilidad() == EstadoDisponibilidad.NO_DISPONIBLE) {
            throw new IllegalArgumentException("La moto " + moto.getPlaca()
                    + " tiene mantenimiento vencido y no se puede asignar.");
        }
        if (moto.getDisponibilidad() == EstadoDisponibilidad.ASIGNADA) {
            throw new IllegalArgumentException("La moto " + moto.getPlaca() + " ya está asignada a "
                    + moto.getRepartidor().getNombre() + ".");
        }
        moto.setRepartidor(repartidor);
        moto.setDisponibilidad(EstadoDisponibilidad.ASIGNADA);
        return "Moto " + moto.getPlaca() + " asignada a " + repartidor.getNombre() + ".";
    }

    /** RF2: libera una moto asignada al repartidor indicado. */
    public String liberarMoto(String cedula, String placa) {
        Repartidor repartidor = obtenerRepartidor(cedula);
        Vehiculo moto = obtenerVehiculo(placa);

        if (moto.getRepartidor() == null || !moto.getRepartidor().getCedula().equals(repartidor.getCedula())) {
            throw new IllegalArgumentException("La moto " + moto.getPlaca()
                    + " no está asignada a " + repartidor.getNombre() + ".");
        }
        moto.setRepartidor(null);
        if (moto.getDisponibilidad() == EstadoDisponibilidad.ASIGNADA) {
            moto.setDisponibilidad(EstadoDisponibilidad.DISPONIBLE);
        }   // si estaba bloqueada (No disponible) sigue bloqueada
        return "Moto " + moto.getPlaca() + " liberada de " + repartidor.getNombre() + ".";
    }

    // ---------------------------------------------------------------- RF3

    /**
     * RF3: registra un mantenimiento. El km no puede ser menor al último de la
     * moto, la fecha no puede ser futura y el costo no puede ser negativo.
     * Actualiza el km de la moto, recalcula su estado y devuelve el mensaje
     * "Próximo [tipo] a los [km] km.".
     */
    public String registrarMantenimiento(String placa, TipoServicio tipo, LocalDate fecha,
                                         int kilometraje, double costo, String observaciones) {
        Vehiculo moto = obtenerVehiculo(placa);
        if (!moto.getServiciosAplicables().contains(tipo)) {
            throw new IllegalArgumentException("El servicio '" + tipo + "' no aplica a una "
                    + moto.getTipo().toLowerCase() + ".");
        }
        if (fecha.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha del mantenimiento no puede ser futura.");
        }
        if (costo < 0) {
            throw new IllegalArgumentException("El costo no puede ser negativo.");
        }
        if (kilometraje < moto.getKilometrajeActual()) {
            throw new IllegalArgumentException("El kilometraje no puede ser menor al último de la moto ("
                    + moto.getKilometrajeActual() + " km).");
        }

        Mantenimiento mantenimiento = new Mantenimiento(tipo, fecha, kilometraje, costo, observaciones);
        moto.agregarMantenimiento(mantenimiento);
        moto.setKilometrajeActual(kilometraje);
        gestorAlertas.actualizarDisponibilidad(moto);   // si ya no está vencida, se libera
        return "Próximo " + tipo.getNombre().toLowerCase() + " a los " + mantenimiento.getProximoKm() + " km.";
    }

    /** Utilidad: actualiza el km de la moto (no permite valores menores al actual). */
    public void actualizarKilometraje(String placa, int nuevoKm) {
        Vehiculo moto = obtenerVehiculo(placa);
        if (nuevoKm < moto.getKilometrajeActual()) {
            throw new IllegalArgumentException("El nuevo kilometraje no puede ser menor al actual ("
                    + moto.getKilometrajeActual() + ").");
        }
        moto.setKilometrajeActual(nuevoKm);
        gestorAlertas.actualizarDisponibilidad(moto);
    }

    // ---------------------------------------------------------------- RF4

    /**
     * RF4: historial de una moto, del más reciente al más antiguo. Los filtros
     * (tipo, fecha inicial, fecha final) son opcionales: null = sin filtro.
     */
    public List<Mantenimiento> consultarHistorial(String placa, TipoServicio tipo,
                                                  LocalDate inicio, LocalDate fin) {
        Vehiculo moto = obtenerVehiculo(placa);
        if (inicio != null && fin != null && inicio.isAfter(fin)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la final.");
        }
        List<Mantenimiento> resultado = new ArrayList<>();
        for (Mantenimiento m : moto.getHistorial()) {
            boolean cumple = (tipo == null || m.getTipo() == tipo)
                    && (inicio == null || !m.getFecha().isBefore(inicio))
                    && (fin == null || !m.getFecha().isAfter(fin));
            if (cumple) {
                resultado.add(m);
            }
        }
        Collections.sort(resultado, new Comparator<Mantenimiento>() {
            @Override
            public int compare(Mantenimiento a, Mantenimiento b) {
                int porFecha = b.getFecha().compareTo(a.getFecha());   // más reciente primero
                return porFecha != 0 ? porFecha : Integer.compare(b.getKilometraje(), a.getKilometraje());
            }
        });
        return resultado;
    }

    /** Suma los costos de una lista de mantenimientos. */
    public static double totalCostos(List<Mantenimiento> lista) {
        double total = 0;
        for (Mantenimiento m : lista) {
            total += m.getCosto();
        }
        return total;
    }

    // ------------------------------------------------------------ búsquedas

    /** Busca por placa (sin distinguir mayúsculas). Devuelve null si no existe. */
    public Vehiculo buscarPorPlaca(String placa) {
        for (Vehiculo v : vehiculos) {
            if (v.getPlaca().equalsIgnoreCase(placa)) {
                return v;
            }
        }
        return null;
    }

    public Repartidor buscarRepartidor(String cedula) {
        for (Repartidor r : repartidores) {
            if (r.getCedula().equals(cedula)) {
                return r;
            }
        }
        return null;
    }

    public Vehiculo obtenerVehiculo(String placa) {
        Vehiculo v = buscarPorPlaca(placa);
        if (v == null) {
            throw new IllegalArgumentException("No se encontró ninguna moto con la placa " + placa + ".");
        }
        return v;
    }

    public Repartidor obtenerRepartidor(String cedula) {
        Repartidor r = buscarRepartidor(cedula);
        if (r == null) {
            throw new IllegalArgumentException("No se encontró ningún repartidor con la cédula " + cedula + ".");
        }
        return r;
    }

    public List<Vehiculo> getVehiculos() { return Collections.unmodifiableList(vehiculos); }
    public List<Repartidor> getRepartidores() { return Collections.unmodifiableList(repartidores); }
}
