package negocio;

import modelo.EstadoMantenimiento;
import modelo.Mantenimiento;
import modelo.Repartidor;
import modelo.TipoServicio;
import modelo.Vehiculo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Módulo 4 (RF7 y RF8): reportes de gasto por moto y de estado de la flota. */
public class GestorReportes {

    private final GestorFlota flota;
    private final GestorAlertas alertas;

    public GestorReportes(GestorFlota flota, GestorAlertas alertas) {
        this.flota = flota;
        this.alertas = alertas;
    }

    /**
     * RF7: gasto total, gasto por tipo y costo por km de una moto en un periodo.
     * Km recorridos = km del último servicio del periodo - km del primero.
     * Si los km son cero no se divide.
     */
    public String gastoPorMoto(String placa, LocalDate inicio, LocalDate fin) {
        Vehiculo moto = flota.obtenerVehiculo(placa);
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Debe indicar la fecha inicial y la final.");
        }
        if (fin.isBefore(inicio)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial.");
        }
        List<Mantenimiento> lista = flota.consultarHistorial(placa, null, inicio, fin);
        if (lista.isEmpty()) {
            return "Sin gastos en el periodo.";
        }

        double total = GestorFlota.totalCostos(lista);
        int kmMin = Integer.MAX_VALUE;
        int kmMax = 0;
        for (Mantenimiento m : lista) {
            kmMin = Math.min(kmMin, m.getKilometraje());
            kmMax = Math.max(kmMax, m.getKilometraje());
        }
        int kmRecorridos = kmMax - kmMin;

        StringBuilder sb = new StringBuilder();
        sb.append("Gasto de ").append(moto.getPlaca()).append(" del ").append(inicio)
          .append(" al ").append(fin).append("\n");
        sb.append("Total: USD ").append(dinero(total)).append("\n");
        sb.append("Por tipo:");
        for (TipoServicio tipo : TipoServicio.values()) {
            double subtotal = 0;
            for (Mantenimiento m : lista) {
                if (m.getTipo() == tipo) {
                    subtotal += m.getCosto();
                }
            }
            if (subtotal > 0) {
                sb.append("\n   - ").append(tipo).append(": USD ").append(dinero(subtotal));
            }
        }
        if (kmRecorridos > 0) {
            sb.append("\nCosto por km: USD ").append(String.format(Locale.US, "%.4f", total / kmRecorridos))
              .append(" (").append(kmRecorridos).append(" km recorridos)");
        } else {
            sb.append("\nKm insuficientes para calcular el costo por km.");
        }
        return sb.toString();
    }

    /**
     * RF8: porcentaje de motos por estado, ranking de motos críticas,
     * repartidores afectados y gasto total. Sin filtro, el ranking incluye las
     * motos vencidas y próximas a vencer; con filtro, las que tengan ese estado.
     */
    public String reporteFlota(EstadoMantenimiento filtro) {
        List<Vehiculo> motos = flota.getVehiculos();
        if (motos.isEmpty()) {
            return "No hay motos registradas.";
        }

        Map<EstadoMantenimiento, Integer> conteo = new LinkedHashMap<>();
        for (EstadoMantenimiento e : EstadoMantenimiento.values()) {
            conteo.put(e, 0);
        }
        List<Vehiculo> ranking = new ArrayList<>();
        double gastoTotal = 0;
        for (Vehiculo moto : motos) {
            alertas.actualizarDisponibilidad(moto);
            EstadoMantenimiento general = alertas.estadoGeneral(moto);
            conteo.put(general, conteo.get(general) + 1);
            boolean entra = filtro == null
                    ? (general == EstadoMantenimiento.VENCIDO || general == EstadoMantenimiento.PROXIMO)
                    : general == filtro;
            if (entra) {
                ranking.add(moto);
            }
            gastoTotal += GestorFlota.totalCostos(moto.getHistorial());
        }

        Collections.sort(ranking, new Comparator<Vehiculo>() {
            @Override
            public int compare(Vehiculo a, Vehiculo b) {
                int porEstado = alertas.estadoGeneral(b).compareTo(alertas.estadoGeneral(a));
                return porEstado != 0 ? porEstado
                        : Integer.compare(alertas.kmRestantesMinimo(a), alertas.kmRestantesMinimo(b));
            }
        });

        StringBuilder sb = new StringBuilder();
        sb.append("=== REPORTE DE LA FLOTA ===\n");
        sb.append("Total de motos: ").append(motos.size()).append("\n");
        sb.append("Motos por estado:");
        for (Map.Entry<EstadoMantenimiento, Integer> e : conteo.entrySet()) {
            sb.append("\n   - ").append(e.getKey().getMensaje()).append(": ").append(e.getValue())
              .append(" (").append(String.format(Locale.US, "%.1f", e.getValue() * 100.0 / motos.size()))
              .append(" %)");
        }

        sb.append("\n\nRanking de motos críticas");
        sb.append(filtro == null ? ":" : " (filtro: " + filtro.getMensaje() + "):");
        if (ranking.isEmpty()) {
            sb.append("\n   (ninguna)");
        }
        Map<String, Repartidor> afectados = new LinkedHashMap<>();
        int puesto = 1;
        for (Vehiculo moto : ranking) {
            sb.append("\n   ").append(puesto++).append(". ").append(moto.getPlaca()).append(" - ")
              .append(alertas.estadoGeneral(moto).getMensaje());
            if (moto.getRepartidor() != null) {
                afectados.put(moto.getRepartidor().getCedula(), moto.getRepartidor());
            }
        }

        sb.append("\n\nRepartidores afectados:");
        if (afectados.isEmpty()) {
            sb.append("\n   (ninguno)");
        }
        for (Repartidor r : afectados.values()) {
            sb.append("\n   - ").append(r.getNombre()).append(" (C.I. ").append(r.getCedula()).append(")");
        }

        sb.append("\n\nGasto total de la flota: USD ").append(dinero(gastoTotal));
        return sb.toString();
    }

    private static String dinero(double valor) {
        return String.format(Locale.US, "%.2f", valor);
    }
}
