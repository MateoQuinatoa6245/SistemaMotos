package interfaz;

import modelo.EstadoMantenimiento;
import modelo.Mantenimiento;
import modelo.Moto;
import modelo.MotoElectrica;
import modelo.Repartidor;
import modelo.TipoServicio;
import modelo.Vehiculo;
import negocio.GestorAlertas;
import negocio.GestorFlota;
import negocio.GestorReportes;
import negocio.Validador;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Interfaz de consola del Sistema de Control de Mantenimiento para Motos de Reparto.
 * Solo pide datos y muestra resultados; la lógica está en el paquete negocio
 * y los datos en el paquete modelo.
 */
public class Main {

    private static final Scanner SC = new Scanner(System.in);
    private static final GestorAlertas ALERTAS = new GestorAlertas();
    private static final GestorFlota FLOTA = new GestorFlota(ALERTAS);
    private static final GestorReportes REPORTES = new GestorReportes(FLOTA, ALERTAS);

    public static void main(String[] args) {
        cargarDatosDemo();

        int opcion = -1;
        do {
            mostrarMenu();
            try {
                opcion = Validador.entero("opción", leer("Elija una opción"));
                switch (opcion) {
                    case 1:  registrarRepartidor(); break;
                    case 2:  registrarMoto(); break;
                    case 3:  asignarOLiberar(); break;
                    case 4:  registrarMantenimiento(); break;
                    case 5:  consultarHistorial(); break;
                    case 6:  estadoMantenimiento(); break;
                    case 7:  alertas(); break;
                    case 8:  gastoPorMoto(); break;
                    case 9:  reporteFlota(); break;
                    case 10: actualizarKilometraje(); break;
                    case 11: verFlota(); break;
                    case 0:  System.out.println("Saliendo del sistema..."); break;
                    default: System.out.println("Opción inválida. Intente de nuevo.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private static void mostrarMenu() {
        System.out.println("\n===== Control de mantenimiento de motos de reparto =====");
        System.out.println("-- Motos y repartidores --");
        System.out.println(" 1. Registrar repartidor        (RF1)");
        System.out.println(" 2. Registrar moto              (RF1)");
        System.out.println(" 3. Asignar / liberar moto      (RF2)");
        System.out.println("-- Mantenimientos --");
        System.out.println(" 4. Registrar mantenimiento     (RF3)");
        System.out.println(" 5. Consultar historial         (RF4)");
        System.out.println("-- Alertas y estado --");
        System.out.println(" 6. Estado de mantenimiento     (RF5)");
        System.out.println(" 7. Alertas y bloqueos          (RF6)");
        System.out.println("-- Reportes --");
        System.out.println(" 8. Gasto de mantenimiento      (RF7)");
        System.out.println(" 9. Reporte de la flota         (RF8)");
        System.out.println("-- Otros --");
        System.out.println("10. Actualizar kilometraje");
        System.out.println("11. Ver motos y repartidores");
        System.out.println(" 0. Salir");
    }

    // ------------------------------------------------------------ Módulo 1

    private static void registrarRepartidor() {
        System.out.println("\n--- Registrar repartidor ---");
        String nombre = Validador.texto("nombre", leer("Nombre"));
        String cedula = Validador.cedula(leer("Cédula (10 dígitos)"));
        String telefono = Validador.telefono(leer("Teléfono"));
        FLOTA.registrarRepartidor(new Repartidor(nombre, cedula, telefono));
        System.out.println("Registro exitoso.");
    }

    private static void registrarMoto() {
        System.out.println("\n--- Registrar moto ---");
        int tipo = Validador.entero("tipo", leer("Tipo (1 = Moto, 2 = Moto eléctrica)"));
        if (tipo != 1 && tipo != 2) {
            throw new IllegalArgumentException("El tipo debe ser 1 o 2.");
        }
        String placa = Validador.texto("placa", leer("Placa")).toUpperCase();
        String marca = Validador.texto("marca", leer("Marca"));
        String modelo = Validador.texto("modelo", leer("Modelo"));
        int anio = Validador.anio(leer("Año"));
        int km = Validador.entero("kilometraje", leer("Kilometraje actual"));

        Vehiculo moto = (tipo == 1)
                ? new Moto(placa, marca, modelo, anio, km)
                : new MotoElectrica(placa, marca, modelo, anio, km);
        FLOTA.registrarVehiculo(moto);
        System.out.println("Registro exitoso.");
    }

    private static void asignarOLiberar() {
        System.out.println("\n--- Asignar / liberar moto ---");
        String cedula = Validador.cedula(leer("Cédula del repartidor"));
        String placa = Validador.texto("placa", leer("Placa de la moto"));
        int accion = Validador.entero("acción", leer("Acción (1 = Asignar, 2 = Liberar)"));
        if (accion == 1) {
            System.out.println(FLOTA.asignarMoto(cedula, placa));
        } else if (accion == 2) {
            System.out.println(FLOTA.liberarMoto(cedula, placa));
        } else {
            throw new IllegalArgumentException("La acción debe ser 1 o 2.");
        }
    }

    // ------------------------------------------------------------ Módulo 2

    private static void registrarMantenimiento() {
        System.out.println("\n--- Registrar mantenimiento ---");
        String placa = Validador.texto("placa", leer("Placa"));
        TipoServicio tipo = leerTipoServicio(true);
        LocalDate fecha = Validador.fecha("fecha", leer("Fecha (AAAA-MM-DD)"));
        int km = Validador.entero("kilometraje", leer("Kilometraje del servicio"));
        double costo = Validador.decimal("costo", leer("Costo (USD)"));
        String observaciones = leer("Observaciones (opcional)").trim();

        String mensaje = FLOTA.registrarMantenimiento(placa, tipo, fecha, km, costo, observaciones);
        System.out.println("Servicio guardado y kilometraje actualizado.");
        System.out.println(mensaje);
    }

    private static void consultarHistorial() {
        System.out.println("\n--- Historial de mantenimientos ---");
        String placa = Validador.texto("placa", leer("Placa"));
        TipoServicio tipo = leerTipoServicio(false);
        LocalDate inicio = leerFechaOpcional("Fecha inicial");
        LocalDate fin = leerFechaOpcional("Fecha final");

        List<Mantenimiento> lista = FLOTA.consultarHistorial(placa, tipo, inicio, fin);
        if (lista.isEmpty()) {
            System.out.println("Sin mantenimientos registrados.");
            return;
        }
        for (Mantenimiento m : lista) {
            System.out.println(" - " + m);
        }
        System.out.println("Total acumulado: USD "
                + String.format(Locale.US, "%.2f", GestorFlota.totalCostos(lista)));
    }

    // ------------------------------------------------------------ Módulo 3

    private static void estadoMantenimiento() {
        System.out.println("\n--- Estado de mantenimiento ---");
        String placa = leer("Placa (vacío = toda la flota)").trim();
        if (placa.isEmpty()) {
            if (FLOTA.getVehiculos().isEmpty()) {
                System.out.println("No hay motos registradas.");
            }
            for (Vehiculo moto : FLOTA.getVehiculos()) {
                System.out.println(ALERTAS.informeEstado(moto));
            }
        } else {
            System.out.println(ALERTAS.informeEstado(FLOTA.obtenerVehiculo(placa)));
        }
    }

    private static void alertas() {
        System.out.println("\n--- Alertas y bloqueos ---");
        EstadoMantenimiento filtro = leerEstadoOpcional();
        for (String mensaje : ALERTAS.generarAlertas(FLOTA.getVehiculos(), filtro)) {
            System.out.println(mensaje);
        }
    }

    // ------------------------------------------------------------ Módulo 4

    private static void gastoPorMoto() {
        System.out.println("\n--- Gasto de mantenimiento por moto ---");
        String placa = Validador.texto("placa", leer("Placa"));
        LocalDate inicio = Validador.fecha("fecha inicial", leer("Fecha inicial (AAAA-MM-DD)"));
        LocalDate fin = Validador.fecha("fecha final", leer("Fecha final (AAAA-MM-DD)"));
        System.out.println(REPORTES.gastoPorMoto(placa, inicio, fin));
    }

    private static void reporteFlota() {
        System.out.println();
        System.out.println(REPORTES.reporteFlota(leerEstadoOpcional()));
    }

    // -------------------------------------------------------------- Otros

    private static void actualizarKilometraje() {
        System.out.println("\n--- Actualizar kilometraje ---");
        String placa = Validador.texto("placa", leer("Placa"));
        Vehiculo moto = FLOTA.obtenerVehiculo(placa);
        System.out.println("Kilometraje actual: " + moto.getKilometrajeActual());
        int nuevoKm = Validador.entero("kilometraje", leer("Nuevo kilometraje"));
        FLOTA.actualizarKilometraje(placa, nuevoKm);
        System.out.println("Kilometraje actualizado. Disponibilidad: " + moto.getDisponibilidad() + ".");
    }

    private static void verFlota() {
        System.out.println("\n--- Motos ---");
        if (FLOTA.getVehiculos().isEmpty()) {
            System.out.println("No hay motos registradas.");
        }
        for (Vehiculo moto : FLOTA.getVehiculos()) {
            System.out.println(moto.mostrarDatos());
        }
        System.out.println("\n--- Repartidores ---");
        if (FLOTA.getRepartidores().isEmpty()) {
            System.out.println("No hay repartidores registrados.");
        }
        for (Repartidor r : FLOTA.getRepartidores()) {
            System.out.println(r);
        }
    }

    // ---------------------------------------------------- lectura de datos

    /** Muestra el texto y lee una línea; si ya no hay entrada, termina el programa. */
    private static String leer(String etiqueta) {
        System.out.print(etiqueta + ": ");
        if (!SC.hasNextLine()) {
            System.out.println();
            System.exit(0);
        }
        return SC.nextLine();
    }

    private static LocalDate leerFechaOpcional(String etiqueta) {
        String texto = leer(etiqueta + " AAAA-MM-DD (vacío = sin límite)").trim();
        return texto.isEmpty() ? null : Validador.fecha(etiqueta.toLowerCase(), texto);
    }

    /** Menú de tipos de servicio. Si es obligatorio no admite vacío; si no, vacío = todos. */
    private static TipoServicio leerTipoServicio(boolean obligatorio) {
        TipoServicio[] tipos = TipoServicio.values();
        StringBuilder opciones = new StringBuilder();
        for (int i = 0; i < tipos.length; i++) {
            opciones.append(i + 1).append(" = ").append(tipos[i]).append(i < tipos.length - 1 ? ", " : "");
        }
        String texto = leer("Tipo (" + opciones + (obligatorio ? "" : "; vacío = todos") + ")").trim();
        if (texto.isEmpty() && !obligatorio) {
            return null;
        }
        int numero = Validador.entero("tipo", texto);
        if (numero < 1 || numero > tipos.length) {
            throw new IllegalArgumentException("El tipo debe estar entre 1 y " + tipos.length + ".");
        }
        return tipos[numero - 1];
    }

    private static EstadoMantenimiento leerEstadoOpcional() {
        String texto = leer("Filtro por estado (1 = Al día, 2 = Próxima a vencer, 3 = Vencida, "
                + "4 = Sin historial; vacío = sin filtro)").trim();
        if (texto.isEmpty()) {
            return null;
        }
        switch (Validador.entero("estado", texto)) {
            case 1:  return EstadoMantenimiento.AL_DIA;
            case 2:  return EstadoMantenimiento.PROXIMO;
            case 3:  return EstadoMantenimiento.VENCIDO;
            case 4:  return EstadoMantenimiento.SIN_HISTORIAL;
            default: throw new IllegalArgumentException("El estado debe estar entre 1 y 4.");
        }
    }

    /** Datos de ejemplo para probar el sistema sin escribir todo a mano. */
    private static void cargarDatosDemo() {
        FLOTA.registrarRepartidor(new Repartidor("Juan Pérez", "1712345678", "0991234567"));
        FLOTA.registrarRepartidor(new Repartidor("María López", "1723456789", "0987654321"));

        FLOTA.registrarVehiculo(new Moto("ABC-123", "Yamaha", "SZ 150", 2024, 1000));
        FLOTA.registrarVehiculo(new Moto("XYZ-789", "Suzuki", "GN 125", 2023, 1000));
        FLOTA.registrarVehiculo(new MotoElectrica("ELE-001", "Zero", "SR/F", 2025, 1000));
        FLOTA.registrarVehiculo(new Moto("NEW-001", "Honda", "CB 125", 2026, 0));   // sin historial

        LocalDate fecha = LocalDate.of(2026, 9, 1);
        FLOTA.registrarMantenimiento("ABC-123", TipoServicio.ACEITE, fecha, 2000, 25.00, "Cambio de aceite");
        FLOTA.registrarMantenimiento("XYZ-789", TipoServicio.FRENOS, fecha, 2000, 40.00, "Pastillas nuevas");
        FLOTA.registrarMantenimiento("ELE-001", TipoServicio.FRENOS, fecha, 2000, 35.00, "Revisión de frenos");

        FLOTA.asignarMoto("1712345678", "ABC-123");
        FLOTA.asignarMoto("1723456789", "XYZ-789");

        FLOTA.actualizarKilometraje("ABC-123", 4800);    // aceite: restan 200 km -> próxima a vencer
        FLOTA.actualizarKilometraje("XYZ-789", 10500);   // frenos vencidos -> moto bloqueada
        FLOTA.actualizarKilometraje("ELE-001", 3000);    // al día
    }
}
