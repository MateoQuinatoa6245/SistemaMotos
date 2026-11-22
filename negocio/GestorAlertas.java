package negocio;

import modelo.EstadoDisponibilidad;
import modelo.EstadoMantenimiento;
import modelo.EstadoServicio;
import modelo.Mantenimiento;
import modelo.TipoServicio;
import modelo.Vehiculo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Módulo 3 (RF5 y RF6): calcula el estado de mantenimiento de las motos,
 * genera alertas por criticidad y bloquea/libera motos según su estado.
 * No guarda datos: trabaja sobre las motos que recibe.
 */
public class GestorAlertas {

    /** RF5: estado de cada servicio aplicable a la moto. */
    public List<EstadoServicio> calcularEstados(Vehiculo moto) {
        List<EstadoServicio> estados = new ArrayList<>();
        for (TipoServicio tipo : moto.getServiciosAplicables()) {
            Mantenimiento ultimo = moto.getUltimoMantenimiento(tipo);
            if (ultimo == null) {
                estados.add(new EstadoServicio(tipo, EstadoMantenimiento.SIN_HISTORIAL, 0, 0));
                continue;
            }
            int proximoKm = ultimo.getProximoKm();
            int restantes = proximoKm - moto.getKilometrajeActual();
            EstadoMantenimiento estado;
            if (restantes < 0) {
                estado = EstadoMantenimiento.VENCIDO;                        // superó el km
            } else if ((long) restantes * 10 <= tipo.getIntervaloKm()) {
                estado = EstadoMantenimiento.PROXIMO;                        // resta 10 % o menos
            } else {
                estado = EstadoMantenimiento.AL_DIA;
            }
            estados.add(new EstadoServicio(tipo, estado, proximoKm, restantes));
        }
        return estados;
    }

    /** RF5: el estado general de la moto es el más crítico de sus servicios. */
    public EstadoMantenimiento estadoGeneral(Vehiculo moto) {
        EstadoMantenimiento general = EstadoMantenimiento.SIN_HISTORIAL;
        for (EstadoServicio s : calcularEstados(moto)) {
            if (s.getEstado().compareTo(general) > 0) {
                general = s.getEstado();
            }
        }
        return general;
    }

    /** Menor cantidad de km restantes entre los servicios con historial (para ordenar el ranking). */
    public int kmRestantesMinimo(Vehiculo moto) {
        int minimo = Integer.MAX_VALUE;
        for (EstadoServicio s : calcularEstados(moto)) {
            if (s.getEstado() != EstadoMantenimiento.SIN_HISTORIAL && s.getKmRestantes() < minimo) {
                minimo = s.getKmRestantes();
            }
        }
        return minimo;
    }

    /** RF5: informe en texto del estado de una moto. */
    public String informeEstado(Vehiculo moto) {
        StringBuilder sb = new StringBuilder();
        sb.append(moto.getTipo()).append(" [").append(moto.getPlaca()).append("] - Estado general: ")
          .append(estadoGeneral(moto).getMensaje());
        for (EstadoServicio s : calcularEstados(moto)) {
            sb.append("\n   - ").append(s);
        }
        return sb.toString();
    }

    /**
     * RF6 (regla de estado): una moto con mantenimiento vencido pasa a
     * "No disponible". Cuando deja de estar vencida se libera: vuelve a
     * "Asignada" si tiene repartidor o a "Disponible" si no.
     */
    public void actualizarDisponibilidad(Vehiculo moto) {
        if (estadoGeneral(moto) == EstadoMantenimiento.VENCIDO) {
            moto.setDisponibilidad(EstadoDisponibilidad.NO_DISPONIBLE);
        } else if (moto.getDisponibilidad() == EstadoDisponibilidad.NO_DISPONIBLE) {
            moto.setDisponibilidad(moto.getRepartidor() != null
                    ? EstadoDisponibilidad.ASIGNADA : EstadoDisponibilidad.DISPONIBLE);
        }
    }

    /**
     * RF6: recalcula/bloquea las motos y devuelve las alertas ordenadas por
     * criticidad (vencidos primero). El filtro por estado es opcional (null = todas).
     */
    public List<String> generarAlertas(List<Vehiculo> motos, EstadoMantenimiento filtro) {
        List<Alerta> alertas = new ArrayList<>();
        for (Vehiculo moto : motos) {
            actualizarDisponibilidad(moto);
            for (EstadoServicio s : calcularEstados(moto)) {
                EstadoMantenimiento e = s.getEstado();
                if ((e == EstadoMantenimiento.VENCIDO || e == EstadoMantenimiento.PROXIMO)
                        && (filtro == null || filtro == e)) {
                    String tipo = s.getTipo().getNombre().toLowerCase();
                    String mensaje = e == EstadoMantenimiento.VENCIDO
                            ? "ALERTA CRÍTICA: Moto " + moto.getPlaca() + " tiene " + tipo
                                + " vencido. Moto bloqueada."
                            : "AVISO: Moto " + moto.getPlaca() + " tiene " + tipo
                                + " próximo a vencer (restan " + s.getKmRestantes() + " km).";
                    alertas.add(new Alerta(e, s.getKmRestantes(), mensaje));
                }
            }
        }
        Collections.sort(alertas, new Comparator<Alerta>() {
            @Override
            public int compare(Alerta a, Alerta b) {
                int porEstado = b.estado.compareTo(a.estado);          // más crítico primero
                return porEstado != 0 ? porEstado : Integer.compare(a.kmRestantes, b.kmRestantes);
            }
        });

        List<String> mensajes = new ArrayList<>();
        for (Alerta a : alertas) {
            mensajes.add(a.mensaje);
        }
        if (mensajes.isEmpty()) {
            mensajes.add("No hay alertas pendientes.");
        }
        return mensajes;
    }

    /** Dato interno para ordenar alertas. */
    private static class Alerta {
        private final EstadoMantenimiento estado;
        private final int kmRestantes;
        private final String mensaje;

        private Alerta(EstadoMantenimiento estado, int kmRestantes, String mensaje) {
            this.estado = estado;
            this.kmRestantes = kmRestantes;
            this.mensaje = mensaje;
        }
    }
}
